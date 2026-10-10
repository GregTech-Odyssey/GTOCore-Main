package com.gtocore.api.wireless.energy;

import com.gtolib.GTOCore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.datasynclib.util.DiskByteBufCodecs;
import com.gto.datasynclib.util.VersionedFriendlyByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 无线电网存档的负载：文件头的版本号由 {@link com.gtolib.utils.FileUtils#saveVersionedToFile} 写在文件最前面，
 * 这里只读写它之后的负载。这是重建后的第一版格式，没有需要读的旧布局，也没有负载自己的版本号。
 *
 * <p>
 * 每个编解码器都是手写的 {@link StreamCodec}：{@code StreamCodec.composite}/{@code convert} 会把每个字段
 * 装进包装类型再走一遍泛型，一个字段一次分配，而一份存档里节点以百计，所以这里的基本类型直接写读缓冲区，
 * 字段顺序与各 record 的声明顺序一致。列表仍用 {@link ByteBufCodecs#collection}：它把元素原样交给元素编解码器，
 * 只有容器自己的泛型元素类型是对象，元素里的字段不装箱。所有编解码器都只建一次（static final），编码与解码
 * 路径上不新建编解码器、不复制、不经过中转缓冲区。
 * </p>
 */
final class GridCodec {

    /**
     * 维度 + 方块坐标：维度写它的 {@link ResourceLocation}（两段 Utf，与磁盘端一致），坐标写 BlockPos 的打包 long。
     * 两半都是引用类型，手写是为了让负载里的每个编解码器都是同一形状、字节布局一眼可见。
     */
    private static final StreamCodec<FriendlyByteBuf, GlobalPos> GLOBAL_POS = new StreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf stream, GlobalPos pos) {
            DiskByteBufCodecs.RESOURCE_LOCATION_CODEC.encode(stream, pos.dimension().location());
            stream.writeLong(pos.pos().asLong());
        }

        @Override
        public GlobalPos decode(FriendlyByteBuf stream) {
            var dimension = DiskByteBufCodecs.RESOURCE_LOCATION_CODEC.decode(stream);
            return GlobalPos.of(dimension(dimension), BlockPos.of(stream.readLong()));
        }
    };

    /** 一个能源塔单元：等级、数量、容量、损耗，顺序与 {@link Provider.Unit} 的声明一致。 */
    private static final StreamCodec<FriendlyByteBuf, Provider.Unit> UNIT_CODEC = new StreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf stream, Provider.Unit unit) {
            stream.writeVarInt(unit.tier());
            stream.writeVarInt(unit.count());
            // BigInteger 是引用类型：库里手写的 BIG_INTEGER（VarInt 长度 + 补码字节）不装箱，缓冲区也没有写它的方法
            ByteBufCodecs.BIG_INTEGER.encode(stream, unit.capacity());
            stream.writeVarInt(unit.loss());
        }

        @Override
        public Provider.Unit decode(FriendlyByteBuf stream) {
            int tier = stream.readVarInt();
            int count = stream.readVarInt();
            var capacity = ByteBufCodecs.BIG_INTEGER.decode(stream);
            return new Provider.Unit(tier, count, capacity, stream.readVarInt());
        }
    };

    private static final StreamCodec<FriendlyByteBuf, List<Provider.Unit>> UNITS_CODEC = ByteBufCodecs.collection(ObjectArrayList::new, UNIT_CODEC);

    /** 一座能源塔：位置、所有者、各单元、等级，顺序与 {@link Provider.Tower} 的声明一致。 */
    private static final StreamCodec<FriendlyByteBuf, Provider.Tower> TOWER_CODEC = new StreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf stream, Provider.Tower tower) {
            GLOBAL_POS.encode(stream, tower.pos());
            stream.writeUUID(tower.owner());
            UNITS_CODEC.encode(stream, tower.units());
            stream.writeVarInt(tower.tier());
        }

        @Override
        public Provider.Tower decode(FriendlyByteBuf stream) {
            var pos = GLOBAL_POS.decode(stream);
            var owner = stream.readUUID();
            var units = UNITS_CODEC.decode(stream);
            return new Provider.Tower(pos, owner, units, stream.readVarInt());
        }
    };

    /** 一个中继：位置、所有者、等级、电流、目标维度；目标维度只存名字，读时再建 ResourceKey。 */
    private record RelaySave(GlobalPos pos, UUID owner, int tier, int amperage, ResourceLocation target) {}

    private static final StreamCodec<FriendlyByteBuf, RelaySave> RELAY_CODEC = new StreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf stream, RelaySave relay) {
            GLOBAL_POS.encode(stream, relay.pos());
            stream.writeUUID(relay.owner());
            stream.writeVarInt(relay.tier());
            stream.writeVarInt(relay.amperage());
            DiskByteBufCodecs.RESOURCE_LOCATION_CODEC.encode(stream, relay.target());
        }

        @Override
        public RelaySave decode(FriendlyByteBuf stream) {
            var pos = GLOBAL_POS.decode(stream);
            var owner = stream.readUUID();
            int tier = stream.readVarInt();
            int amperage = stream.readVarInt();
            var target = DiskByteBufCodecs.RESOURCE_LOCATION_CODEC.decode(stream);
            return new RelaySave(pos, owner, tier, amperage, target);
        }
    };

    /** 一档电池：档位与 126 位存量的高低两半；只写非空档（{@code stocked} 里置位的那些）。 */
    private record BankSave(int tier, long hi, long lo) {}

    private static final StreamCodec<FriendlyByteBuf, BankSave> BANK_CODEC = new StreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf stream, BankSave bank) {
            stream.writeVarInt(bank.tier());
            stream.writeLong(bank.hi());
            stream.writeLong(bank.lo());
        }

        @Override
        public BankSave decode(FriendlyByteBuf stream) {
            int tier = stream.readVarInt();
            long hi = stream.readLong();
            return new BankSave(tier, hi, stream.readLong());
        }
    };

    private static final StreamCodec<FriendlyByteBuf, List<BankSave>> BANKS_CODEC = ByteBufCodecs.collection(ObjectArrayList::new, BANK_CODEC);

    /** 一个维度节点：维度 + 它的各档电池。 */
    private record NodeSave(ResourceLocation dimension, List<BankSave> banks) {}

    private static final StreamCodec<FriendlyByteBuf, NodeSave> NODE_CODEC = new StreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf stream, NodeSave node) {
            DiskByteBufCodecs.RESOURCE_LOCATION_CODEC.encode(stream, node.dimension());
            BANKS_CODEC.encode(stream, node.banks());
        }

        @Override
        public NodeSave decode(FriendlyByteBuf stream) {
            var dimension = DiskByteBufCodecs.RESOURCE_LOCATION_CODEC.decode(stream);
            return new NodeSave(dimension, BANKS_CODEC.decode(stream));
        }
    };

    /** 一个队伍的存档：队伍、速率、绑定、待入账余额、各维度节点、能源塔与中继。 */
    private record AccountSave(UUID team, long rate, List<GlobalPos> bind, long pendingHi, long pendingLo, List<NodeSave> nodes,
                               List<Provider.Tower> towers, List<RelaySave> relays) {}

    private static final StreamCodec<FriendlyByteBuf, List<GlobalPos>> BIND_CODEC = ByteBufCodecs.collection(ObjectArrayList::new, GLOBAL_POS);
    private static final StreamCodec<FriendlyByteBuf, List<NodeSave>> NODES_CODEC = ByteBufCodecs.collection(ObjectArrayList::new, NODE_CODEC);
    private static final StreamCodec<FriendlyByteBuf, List<Provider.Tower>> TOWERS_CODEC = ByteBufCodecs.collection(ObjectArrayList::new, TOWER_CODEC);
    private static final StreamCodec<FriendlyByteBuf, List<RelaySave>> RELAYS_CODEC = ByteBufCodecs.collection(ObjectArrayList::new, RELAY_CODEC);

    private static final StreamCodec<FriendlyByteBuf, AccountSave> ACCOUNT_CODEC = new StreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf stream, AccountSave save) {
            stream.writeUUID(save.team());
            stream.writeLong(save.rate());
            BIND_CODEC.encode(stream, save.bind());
            stream.writeLong(save.pendingHi());
            stream.writeLong(save.pendingLo());
            NODES_CODEC.encode(stream, save.nodes());
            TOWERS_CODEC.encode(stream, save.towers());
            RELAYS_CODEC.encode(stream, save.relays());
        }

        @Override
        public AccountSave decode(FriendlyByteBuf stream) {
            var team = stream.readUUID();
            long rate = stream.readLong();
            var bind = BIND_CODEC.decode(stream);
            long pendingHi = stream.readLong();
            long pendingLo = stream.readLong();
            var nodes = NODES_CODEC.decode(stream);
            var towers = TOWERS_CODEC.decode(stream);
            var relays = RELAYS_CODEC.decode(stream);
            return new AccountSave(team, rate, bind, pendingHi, pendingLo, nodes, towers, relays);
        }
    };

    private static final StreamCodec<FriendlyByteBuf, List<AccountSave>> ACCOUNTS_CODEC = ByteBufCodecs.collection(ObjectArrayList::new, ACCOUNT_CODEC);

    private GridCodec() {}

    static void encode(WirelessGrid grid, FriendlyByteBuf stream) {
        var saves = new ObjectArrayList<AccountSave>(grid.accounts.size());
        for (var account : grid.accounts.values()) {
            if (account.removed) continue;
            var save = toSave(account);
            if (save != null) saves.add(save);
        }
        ACCOUNTS_CODEC.encode(stream, saves);
    }

    private static AccountSave toSave(EnergyAccount account) {
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
        return new AccountSave(account.team, account.rate, account.bindPos == null ? Collections.emptyList() : Collections.singletonList(account.bindPos),
                account.pendingHi, account.pendingLo, nodes, new ObjectArrayList<>(account.towers.values()), relays);
    }

    /**
     * 读取文件头带版本号的存档：文件头是权威的版本来源，也是唯一一份版本号，负载里不再有第二份。
     * 读到别的版本说明文件不是这个读取器写的：记一条错误，给出不可用电网，不读取、不写回。
     */
    static WirelessGrid decodeVersioned(VersionedFriendlyByteBuf stream) {
        int fileVersion = stream.version();
        if (fileVersion != WirelessGrid.VERSION) {
            GTOCore.LOGGER.error("[无线电网] {} 的文件版本 {} 与当前支持的 {} 不符：本次运行电网不可用，不读取、不写回", WirelessGrid.DATA_NAME, fileVersion, WirelessGrid.VERSION);
            return new WirelessGrid(false);
        }
        var grid = new WirelessGrid(true);
        for (var save : ACCOUNTS_CODEC.decode(stream)) fromSave(grid, save);
        return grid;
    }

    /**
     * {@link com.gtolib.utils.FileUtils#loadVersionedFromFile} 要求的第二个读取入口：文件不带库写的文件头版本。
     * 电网存档是这个格式的第一版，从来没有过不带文件头的存档，所以这里不解读任何旧布局——按版本不符处理：
     * 记一条错误，给出不可用电网。
     */
    static WirelessGrid decodeUnversioned(FriendlyByteBuf stream) {
        GTOCore.LOGGER.error("[无线电网] {} 不带文件头版本（本格式自版本 {} 起）：本次运行电网不可用，不读取、不写回", WirelessGrid.DATA_NAME, WirelessGrid.VERSION);
        return new WirelessGrid(false);
    }

    private static void loadNode(EnergyAccount account, NodeSave save) {
        var node = account.nodeOrCreate(dimension(save.dimension()));
        for (var bank : save.banks()) node.load(Math.max(0, Math.min(GridNode.BANKS - 1, bank.tier())), Math.max(0, bank.hi()), bank.lo() & U126.MASK);
    }

    private static void fromSave(WirelessGrid grid, AccountSave save) {
        var account = grid.account(save.team());
        account.rate = Math.max(0, save.rate());
        account.bindPos = save.bind().isEmpty() ? null : save.bind().get(0);
        account.pendingHi = Math.max(0, save.pendingHi());
        account.pendingLo = save.pendingLo() & U126.MASK;
        for (var node : save.nodes()) loadNode(account, node);
        for (var tower : save.towers()) grid.attach(account, new Provider.Tower(tower.pos(), tower.owner(), tower.units(), ProviderRegistry.clampTier(tower.tier())));
        for (var relay : save.relays()) grid.attach(account, new Provider.Relay(relay.pos(), relay.owner(), ProviderRegistry.clampLineTier(relay.tier()), Provider.Relay.AMPERAGE, dimension(relay.target())));
        account.rebuild();
    }

    static ResourceKey<Level> dimension(ResourceLocation location) {
        return ResourceKey.create(Registries.DIMENSION, location);
    }
}
