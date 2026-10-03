package com.gtocore.api.wireless.energy;

import com.gtolib.GTOCore;
import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.gto.datasynclib.util.StreamCodecs;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

final class GridCodec {

    private static final int VERSION = 3;

    private static final ByteStreamCodec<GlobalPos> GLOBAL_POS = DataSyncCodec.GLOBAL_POS_CODEC.toStreamCodec();

    private static final ByteStreamCodec<Provider.Tower> TOWER_CODEC = ByteStreamCodec.composite(
            GLOBAL_POS, Provider.Tower::pos,
            ByteStreamCodec.UUID_CODEC, Provider.Tower::owner,
            ByteStreamCodec.BIG_INTEGER_CODEC, Provider.Tower::capacity,
            ByteStreamCodec.DOUBLE_CODEC, Provider.Tower::lossWeight,
            ByteStreamCodec.INT_CODEC, Provider.Tower::tier,
            Provider.Tower::new);

    private record RelaySave(GlobalPos pos, UUID owner, int tier, int amperage, ResourceLocation target) {}

    private static final ByteStreamCodec<RelaySave> RELAY_CODEC = ByteStreamCodec.composite(
            GLOBAL_POS, RelaySave::pos,
            ByteStreamCodec.UUID_CODEC, RelaySave::owner,
            ByteStreamCodec.INT_CODEC, RelaySave::tier,
            ByteStreamCodec.INT_CODEC, RelaySave::amperage,
            StreamCodecs.RESOURCE_LOCATION_CODEC, RelaySave::target,
            RelaySave::new);

    private record NodeSave(ResourceLocation dimension, long hi, long lo) {}

    private static final ByteStreamCodec<NodeSave> NODE_CODEC = ByteStreamCodec.composite(
            StreamCodecs.RESOURCE_LOCATION_CODEC, NodeSave::dimension,
            ByteStreamCodec.LONG_CODEC, NodeSave::hi,
            ByteStreamCodec.LONG_CODEC, NodeSave::lo,
            NodeSave::new);

    private record AccountSave(UUID team, long rate, List<GlobalPos> bind, long pendingHi, long pendingLo, List<NodeSave> nodes,
                               List<Provider.Tower> towers, List<RelaySave> relays) {}

    private static final ByteStreamCodec<AccountSave> ACCOUNT_CODEC = ByteStreamCodec.composite(
            ByteStreamCodec.UUID_CODEC, AccountSave::team,
            ByteStreamCodec.LONG_CODEC, AccountSave::rate,
            ByteStreamCodec.collection(ObjectArrayList::new, GLOBAL_POS), AccountSave::bind,
            ByteStreamCodec.LONG_CODEC, AccountSave::pendingHi,
            ByteStreamCodec.LONG_CODEC, AccountSave::pendingLo,
            ByteStreamCodec.collection(ObjectArrayList::new, NODE_CODEC), AccountSave::nodes,
            ByteStreamCodec.collection(ObjectArrayList::new, TOWER_CODEC), AccountSave::towers,
            ByteStreamCodec.collection(ObjectArrayList::new, RELAY_CODEC), AccountSave::relays,
            AccountSave::new);

    private static final ByteStreamCodec<List<AccountSave>> ACCOUNTS_CODEC = ByteStreamCodec.collection(ObjectArrayList::new, ACCOUNT_CODEC);

    private GridCodec() {}

    static void encode(WirelessGrid grid, DataIOStream stream) throws IOException {
        var saves = new ObjectArrayList<AccountSave>(grid.accounts.size());
        for (var account : grid.accounts.values()) {
            if (account.removed) continue;
            var save = toSave(account);
            if (save != null) saves.add(save);
        }
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ACCOUNTS_CODEC.encode(buf, saves);
            stream.writeInt(VERSION);
            stream.writeInt(buf.readableBytes());
            stream.write(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } finally {
            buf.release();
        }
    }

    private static AccountSave toSave(EnergyAccount account) {
        var nodes = new ObjectArrayList<NodeSave>(account.nodeList.size());
        for (var node : account.nodeList) {
            if (!node.isEmpty()) nodes.add(new NodeSave(node.dimension.location(), Math.max(0, node.hi), node.lo));
        }
        boolean empty = nodes.isEmpty() && account.pendingHi == 0 && account.pendingLo == 0 && account.rate == 0 && account.bindPos == null &&
                account.towers.isEmpty() && account.relays.isEmpty();
        if (empty) return null;
        var relays = new ObjectArrayList<RelaySave>(account.relays.size());
        for (var relay : account.relays.values()) relays.add(new RelaySave(relay.pos(), relay.owner(), relay.tier(), relay.amperage(), relay.target().location()));
        return new AccountSave(account.team, account.rate, account.bindPos == null ? Collections.emptyList() : Collections.singletonList(account.bindPos),
                account.pendingHi, account.pendingLo, nodes, new ObjectArrayList<>(account.towers.values()), relays);
    }

    static WirelessGrid decode(DataIOStream stream) throws IOException {
        int version = stream.readInt();
        if (version != VERSION) {
            GTOCore.LOGGER.error("[无线电网] {} 的版本 {} 与当前支持的 {} 不符：本次运行电网不可用，不读取、不写回", WirelessGrid.DATA_NAME, version, VERSION);
            return new WirelessGrid(false);
        }
        var bytes = new byte[stream.readInt()];
        stream.readFully(bytes);
        var saves = ACCOUNTS_CODEC.decode(new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes)));
        var grid = new WirelessGrid(true);
        for (var save : saves) fromSave(grid, save);
        return grid;
    }

    private static void fromSave(WirelessGrid grid, AccountSave save) {
        var account = grid.account(save.team());
        account.rate = Math.max(0, save.rate());
        account.bindPos = save.bind().isEmpty() ? null : save.bind().get(0);
        account.pendingHi = Math.max(0, save.pendingHi());
        account.pendingLo = save.pendingLo() & U126.MASK;
        for (var node : save.nodes()) account.nodeOrCreate(dimension(node.dimension())).addWide(Math.max(0, node.hi()), node.lo() & U126.MASK);
        for (var tower : save.towers()) grid.attach(account, new Provider.Tower(tower.pos(), tower.owner(), tower.capacity(), tower.lossWeight(), ProviderRegistry.clampTier(tower.tier())));
        for (var relay : save.relays()) grid.attach(account, new Provider.Relay(relay.pos(), relay.owner(), ProviderRegistry.clampLineTier(relay.tier()), Provider.Relay.AMPERAGE, dimension(relay.target())));
        account.rebuild();
    }

    static ResourceKey<Level> dimension(ResourceLocation location) {
        return ResourceKey.create(Registries.DIMENSION, location);
    }
}
