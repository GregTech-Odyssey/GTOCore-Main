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
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

final class GridCodec {

    private static final int VERSION = 4;
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static final int LEGACY_VERSION = 3;

    private static final ByteStreamCodec<GlobalPos> GLOBAL_POS = DataSyncCodec.GLOBAL_POS_CODEC.toStreamCodec();

    private static final ByteStreamCodec<Provider.Unit> UNIT_CODEC = ByteStreamCodec.composite(
            ByteStreamCodec.INT_CODEC, Provider.Unit::tier,
            ByteStreamCodec.INT_CODEC, Provider.Unit::count,
            ByteStreamCodec.BIG_INTEGER_CODEC, Provider.Unit::capacity,
            ByteStreamCodec.INT_CODEC, Provider.Unit::loss,
            Provider.Unit::new);

    private static final ByteStreamCodec<Provider.Tower> TOWER_CODEC = ByteStreamCodec.composite(
            GLOBAL_POS, Provider.Tower::pos,
            ByteStreamCodec.UUID_CODEC, Provider.Tower::owner,
            ByteStreamCodec.collection(ObjectArrayList::new, UNIT_CODEC), Provider.Tower::units,
            ByteStreamCodec.INT_CODEC, Provider.Tower::tier,
            Provider.Tower::new);

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private record LegacyTower(GlobalPos pos, UUID owner, BigInteger capacity, double lossWeight, int tier) {

        Provider.Tower migrate() {
            int loss = capacity.signum() > 0 ? (int) Math.round(lossWeight / capacity.doubleValue()) : 0;
            return new Provider.Tower(pos, owner, Collections.singletonList(new Provider.Unit(Provider.Unit.FIXED, 1, capacity, loss)), tier);
        }
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static final ByteStreamCodec<LegacyTower> LEGACY_TOWER_CODEC = ByteStreamCodec.composite(
            GLOBAL_POS, LegacyTower::pos,
            ByteStreamCodec.UUID_CODEC, LegacyTower::owner,
            ByteStreamCodec.BIG_INTEGER_CODEC, LegacyTower::capacity,
            ByteStreamCodec.DOUBLE_CODEC, LegacyTower::lossWeight,
            ByteStreamCodec.INT_CODEC, LegacyTower::tier,
            LegacyTower::new);

    private record RelaySave(GlobalPos pos, UUID owner, int tier, int amperage, ResourceLocation target) {}

    private static final ByteStreamCodec<RelaySave> RELAY_CODEC = ByteStreamCodec.composite(
            GLOBAL_POS, RelaySave::pos,
            ByteStreamCodec.UUID_CODEC, RelaySave::owner,
            ByteStreamCodec.INT_CODEC, RelaySave::tier,
            ByteStreamCodec.INT_CODEC, RelaySave::amperage,
            StreamCodecs.RESOURCE_LOCATION_CODEC, RelaySave::target,
            RelaySave::new);

    private record BankSave(int tier, long hi, long lo) {}

    private static final ByteStreamCodec<BankSave> BANK_CODEC = ByteStreamCodec.composite(
            ByteStreamCodec.INT_CODEC, BankSave::tier,
            ByteStreamCodec.LONG_CODEC, BankSave::hi,
            ByteStreamCodec.LONG_CODEC, BankSave::lo,
            BankSave::new);

    private record NodeSave(ResourceLocation dimension, List<BankSave> banks) {}

    private static final ByteStreamCodec<NodeSave> NODE_CODEC = ByteStreamCodec.composite(
            StreamCodecs.RESOURCE_LOCATION_CODEC, NodeSave::dimension,
            ByteStreamCodec.collection(ObjectArrayList::new, BANK_CODEC), NodeSave::banks,
            NodeSave::new);

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private record LegacyNode(ResourceLocation dimension, long hi, long lo) {}

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static final ByteStreamCodec<LegacyNode> LEGACY_NODE_CODEC = ByteStreamCodec.composite(
            StreamCodecs.RESOURCE_LOCATION_CODEC, LegacyNode::dimension,
            ByteStreamCodec.LONG_CODEC, LegacyNode::hi,
            ByteStreamCodec.LONG_CODEC, LegacyNode::lo,
            LegacyNode::new);

    private record AccountSave<T, N>(UUID team, long rate, List<GlobalPos> bind, long pendingHi, long pendingLo, List<N> nodes,
                                     List<T> towers, List<RelaySave> relays) {}

    private static <T, N> ByteStreamCodec<List<AccountSave<T, N>>> accountsCodec(ByteStreamCodec<T> towerCodec, ByteStreamCodec<N> nodeCodec) {
        ByteStreamCodec<AccountSave<T, N>> account = ByteStreamCodec.composite(
                ByteStreamCodec.UUID_CODEC, AccountSave::team,
                ByteStreamCodec.LONG_CODEC, AccountSave::rate,
                ByteStreamCodec.collection(ObjectArrayList::new, GLOBAL_POS), AccountSave::bind,
                ByteStreamCodec.LONG_CODEC, AccountSave::pendingHi,
                ByteStreamCodec.LONG_CODEC, AccountSave::pendingLo,
                ByteStreamCodec.collection(ObjectArrayList::new, nodeCodec), AccountSave::nodes,
                ByteStreamCodec.collection(ObjectArrayList::new, towerCodec), AccountSave::towers,
                ByteStreamCodec.collection(ObjectArrayList::new, RELAY_CODEC), AccountSave::relays,
                AccountSave::new);
        return ByteStreamCodec.collection(ObjectArrayList::new, account);
    }

    private static final ByteStreamCodec<List<AccountSave<Provider.Tower, NodeSave>>> ACCOUNTS_CODEC = accountsCodec(TOWER_CODEC, NODE_CODEC);
    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static final ByteStreamCodec<List<AccountSave<LegacyTower, LegacyNode>>> LEGACY_ACCOUNTS_CODEC = accountsCodec(LEGACY_TOWER_CODEC, LEGACY_NODE_CODEC);

    private GridCodec() {}

    static void encode(WirelessGrid grid, DataIOStream stream) throws IOException {
        var saves = new ObjectArrayList<AccountSave<Provider.Tower, NodeSave>>(grid.accounts.size());
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

    private static AccountSave<Provider.Tower, NodeSave> toSave(EnergyAccount account) {
        var nodes = new ObjectArrayList<NodeSave>(account.nodeList.size());
        for (var node : account.nodeList) {
            if (node.isEmpty()) continue;
            var banks = new ObjectArrayList<BankSave>(Integer.bitCount(node.stocked));
            for (int m = node.stocked; m != 0; m &= m - 1) {
                int t = Integer.numberOfTrailingZeros(m);
                banks.add(new BankSave(t, node.bankHi[t], node.bankLo[t]));
            }
            nodes.add(new NodeSave(node.dimension.location(), banks));
        }
        boolean empty = nodes.isEmpty() && account.pendingHi == 0 && account.pendingLo == 0 && account.rate == 0 && account.bindPos == null &&
                account.towers.isEmpty() && account.relays.isEmpty();
        if (empty) return null;
        var relays = new ObjectArrayList<RelaySave>(account.relays.size());
        for (var relay : account.relays.values()) relays.add(new RelaySave(relay.pos(), relay.owner(), relay.tier(), relay.amperage(), relay.target().location()));
        return new AccountSave<>(account.team, account.rate, account.bindPos == null ? Collections.emptyList() : Collections.singletonList(account.bindPos),
                account.pendingHi, account.pendingLo, nodes, new ObjectArrayList<>(account.towers.values()), relays);
    }

    static WirelessGrid decode(DataIOStream stream) throws IOException {
        int version = stream.readInt();
        if (version != VERSION && version != LEGACY_VERSION) {
            GTOCore.LOGGER.error("[无线电网] {} 的版本 {} 与当前支持的 {} 不符：本次运行电网不可用，不读取、不写回", WirelessGrid.DATA_NAME, version, VERSION);
            return new WirelessGrid(false);
        }
        var bytes = new byte[stream.readInt()];
        stream.readFully(bytes);
        var buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        var grid = new WirelessGrid(true);
        if (version == VERSION) {
            for (var save : ACCOUNTS_CODEC.decode(buf)) fromSave(grid, save, GridCodec::loadNode);
        } else {
            for (var save : migrateLegacy(LEGACY_ACCOUNTS_CODEC.decode(buf))) fromSave(grid, save, GridCodec::loadLegacyNode);
        }
        return grid;
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static List<AccountSave<Provider.Tower, LegacyNode>> migrateLegacy(List<AccountSave<LegacyTower, LegacyNode>> legacy) {
        var saves = new ObjectArrayList<AccountSave<Provider.Tower, LegacyNode>>(legacy.size());
        for (var save : legacy) {
            var towers = new ObjectArrayList<Provider.Tower>(save.towers().size());
            for (var tower : save.towers()) towers.add(tower.migrate());
            saves.add(new AccountSave<>(save.team(), save.rate(), save.bind(), save.pendingHi(), save.pendingLo(), save.nodes(), towers, save.relays()));
        }
        return saves;
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    private static void loadLegacyNode(EnergyAccount account, LegacyNode save) {
        account.nodeOrCreate(dimension(save.dimension())).addLegacy(Math.max(0, save.hi()), save.lo() & U126.MASK);
    }

    private static void loadNode(EnergyAccount account, NodeSave save) {
        var node = account.nodeOrCreate(dimension(save.dimension()));
        for (var bank : save.banks()) node.load(Math.max(0, Math.min(GridNode.BANKS - 1, bank.tier())), Math.max(0, bank.hi()), bank.lo() & U126.MASK);
    }

    private static <N> void fromSave(WirelessGrid grid, AccountSave<Provider.Tower, N> save, BiConsumer<EnergyAccount, N> nodeLoader) {
        var account = grid.account(save.team());
        account.rate = Math.max(0, save.rate());
        account.bindPos = save.bind().isEmpty() ? null : save.bind().get(0);
        account.pendingHi = Math.max(0, save.pendingHi());
        account.pendingLo = save.pendingLo() & U126.MASK;
        for (var node : save.nodes()) nodeLoader.accept(account, node);
        for (var tower : save.towers()) grid.attach(account, new Provider.Tower(tower.pos(), tower.owner(), tower.units(), ProviderRegistry.clampTier(tower.tier())));
        for (var relay : save.relays()) grid.attach(account, new Provider.Relay(relay.pos(), relay.owner(), ProviderRegistry.clampLineTier(relay.tier()), Provider.Relay.AMPERAGE, dimension(relay.target())));
        account.rebuild();
    }

    static ResourceKey<Level> dimension(ResourceLocation location) {
        return ResourceKey.create(Registries.DIMENSION, location);
    }
}
