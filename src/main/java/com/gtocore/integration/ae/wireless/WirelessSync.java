package com.gtocore.integration.ae.wireless;

import com.gtolib.api.network.NetworkPack;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * 按玩家过滤的小型 S2C 同步：只下发该玩家可访问的网络 {@code {id, 名称}} 与他的收藏 id，
 * 填充 {@link WirelessClientCache}。取代旧的全服广播包（旧包下发所有队伍的网络与节点坐标）。
 * <p>
 * 发送时机：登录、网络增删改、收藏变化、玩家打开任一无线界面（顺带覆盖队伍变动）。
 */
public final class WirelessSync {

    private static final NetworkPack PACK = NetworkPack.registerS2C("gtocoreWirelessNetworksS2C",
            (args, buf) -> write((ServerPlayer) args[0], buf),
            (player, buf) -> read(buf));

    private WirelessSync() {}

    /** 两端在 mod 构造期调用一次，确保包已注册。 */
    public static void init() {}

    public static void pushTo(ServerPlayer player) {
        PACK.send(player);
    }

    public static void pushTo(Collection<ServerPlayer> players) {
        for (var player : players) pushTo(player);
    }

    /** 推给所有能使用该网络的在线玩家。 */
    public static void pushNetwork(MinecraftServer server, WirelessNetwork network) {
        pushTo(usersOf(server, network));
    }

    /** 能使用该网络的在线玩家（删除网络前先算好，删完再推）。 */
    public static List<ServerPlayer> usersOf(MinecraftServer server, WirelessNetwork network) {
        return server.getPlayerList().getPlayers().stream().filter(p -> network.canUse(p.getUUID())).toList();
    }

    private static void write(ServerPlayer player, FriendlyByteBuf buf) {
        var server = Objects.requireNonNull(player.getServer());
        var networks = WirelessNetworks.get(server);
        var favorite = networks.favorite(player.getUUID());
        buf.writeUtf(favorite == null ? "" : favorite);
        var list = networks.listFor(player.getUUID());
        buf.writeVarInt(list.size());
        for (var network : list) {
            buf.writeUtf(network.id());
            buf.writeUtf(network.name());
        }
    }

    private static void read(FriendlyByteBuf buf) {
        var favorite = buf.readUtf();
        int size = buf.readVarInt();
        var map = new LinkedHashMap<String, String>(size);
        for (int i = 0; i < size; i++) {
            map.put(buf.readUtf(), buf.readUtf());
        }
        WirelessClientCache.accept(map, favorite);
    }
}
