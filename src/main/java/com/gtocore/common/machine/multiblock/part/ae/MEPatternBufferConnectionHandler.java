package com.gtocore.common.machine.multiblock.part.ae;

import com.gtolib.api.network.NetworkPack;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.datastream.DataComponentKey;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * 样板缓存与代理部件的连接注册表。每个代理只绑定一个缓存，但一个缓存可以连接多个代理。
 *
 * <p>
 * 服务端数据绑定到对应 {@link Level}，客户端只保留网络同步的按维度快照。
 * </p>
 */
public final class MEPatternBufferConnectionHandler {

    private static final Connection[] EMPTY_CONNECTIONS = new Connection[0];
    private static final DataComponentKey<ServerConnections> SERVER_CONNECTIONS_KEY = DataComponentKey.createNoCodec("me_pattern_buffer_connections");
    private static final Reference2ObjectOpenHashMap<ResourceKey<Level>, Connection[]> CLIENT_CONNECTIONS = new Reference2ObjectOpenHashMap<>();

    private static final NetworkPack SYNC = NetworkPack.registerS2C(
            "mePatternBufferConnectionSync", (Player ignored, FriendlyByteBuf buffer) -> readFromBuffer(buffer));

    private MEPatternBufferConnectionHandler() {}

    /** Forces network channel registration during common bootstrap. */
    public static void init() {}

    public static void unloadClient() {
        CLIENT_CONNECTIONS.clear();
    }

    public static void syncToPlayer(ServerPlayer player) {
        SYNC.send(buffer -> writeToBuffer(buffer, player.level()), player);
    }

    public static void register(Level level, BlockPos bufferPos, BlockPos proxyPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (getServerConnections(serverLevel).put(bufferPos.asLong(), proxyPos.asLong())) {
            syncDimension(serverLevel);
        }
    }

    public static void unregisterBuffer(Level level, BlockPos bufferPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        var connections = getServerConnectionsIfPresent(serverLevel);
        if (connections != null && connections.removeBuffer(bufferPos.asLong())) syncDimension(serverLevel);
    }

    public static void unregisterProxy(Level level, BlockPos proxyPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        var connections = getServerConnectionsIfPresent(serverLevel);
        if (connections != null && connections.removeProxy(proxyPos.asLong())) syncDimension(serverLevel);
    }

    public static Connection[] getClientConnections(ResourceKey<Level> dimension) {
        var connections = CLIENT_CONNECTIONS.get(dimension);
        return connections == null ? EMPTY_CONNECTIONS : connections;
    }

    private static void writeToBuffer(FriendlyByteBuf buffer, Level level) {
        buffer.writeResourceKey(level.dimension());
        var connections = level instanceof ServerLevel serverLevel ? getServerConnectionsIfPresent(serverLevel) : null;
        if (connections == null) {
            buffer.writeVarInt(0);
        } else {
            connections.writeTo(buffer);
        }
    }

    private static void readFromBuffer(FriendlyByteBuf buffer) {
        var dimension = buffer.readResourceKey(Registries.DIMENSION);
        int count = buffer.readVarInt();
        if (count == 0) {
            CLIENT_CONNECTIONS.put(dimension, EMPTY_CONNECTIONS);
            return;
        }
        var connections = new Connection[count];
        for (int i = 0; i < count; i++) {
            connections[i] = new Connection(BlockPos.of(buffer.readLong()), BlockPos.of(buffer.readLong()));
        }
        CLIENT_CONNECTIONS.put(dimension, connections);
    }

    private static ServerConnections getServerConnections(ServerLevel level) {
        var connections = ILevel.getCapability(level, SERVER_CONNECTIONS_KEY);
        if (connections == null) {
            connections = new ServerConnections();
            ILevel.setCapability(level, SERVER_CONNECTIONS_KEY, connections);
        }
        return connections;
    }

    private static @Nullable ServerConnections getServerConnectionsIfPresent(ServerLevel level) {
        return ILevel.getCapability(level, SERVER_CONNECTIONS_KEY);
    }

    private static void syncDimension(ServerLevel level) {
        if (!level.players().isEmpty()) SYNC.send(buffer -> writeToBuffer(buffer, level), level.players());
    }

    public record Connection(BlockPos bufferPos, BlockPos proxyPos) {}

    private static final class ServerConnections {

        private final Long2ObjectOpenHashMap<LongOpenHashSet> proxiesByBuffer = new Long2ObjectOpenHashMap<>();
        private final Long2LongOpenHashMap bufferByProxy = new Long2LongOpenHashMap();

        private boolean put(long bufferPos, long proxyPos) {
            if (bufferByProxy.containsKey(proxyPos)) {
                long previousBuffer = bufferByProxy.get(proxyPos);
                if (previousBuffer == bufferPos) return false;
                removeProxy(proxyPos);
            }
            var proxies = proxiesByBuffer.get(bufferPos);
            if (proxies == null) {
                proxies = new LongOpenHashSet(1);
                proxiesByBuffer.put(bufferPos, proxies);
            }
            proxies.add(proxyPos);
            bufferByProxy.put(proxyPos, bufferPos);
            return true;
        }

        private boolean removeBuffer(long bufferPos) {
            var proxies = proxiesByBuffer.remove(bufferPos);
            if (proxies == null) return false;
            var iterator = proxies.iterator();
            while (iterator.hasNext()) bufferByProxy.remove(iterator.nextLong());
            return true;
        }

        private boolean removeProxy(long proxyPos) {
            if (!bufferByProxy.containsKey(proxyPos)) return false;
            long bufferPos = bufferByProxy.remove(proxyPos);
            var proxies = proxiesByBuffer.get(bufferPos);
            proxies.remove(proxyPos);
            if (proxies.isEmpty()) proxiesByBuffer.remove(bufferPos);
            return true;
        }

        private void writeTo(FriendlyByteBuf buffer) {
            buffer.writeVarInt(bufferByProxy.size());
            var entries = proxiesByBuffer.long2ObjectEntrySet().fastIterator();
            while (entries.hasNext()) {
                var entry = entries.next();
                long bufferPos = entry.getLongKey();
                var proxies = entry.getValue().iterator();
                while (proxies.hasNext()) {
                    buffer.writeLong(bufferPos);
                    buffer.writeLong(proxies.nextLong());
                }
            }
        }
    }
}
