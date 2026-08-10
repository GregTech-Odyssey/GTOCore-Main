package com.gtocore.common.data;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.recipe.ScanningRecipeExtion;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.gto.datasynclib.datastream.codec.CombinedCodec;
import com.gto.datasynclib.datastream.codec.DataCodec;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import com.gto.datasynclib.util.DataCodecs;
import com.gto.datasynclib.util.StreamCodecs;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class GTOCodecs {

    /** KeyCounter（AEKey→long 计数集合）持久化 codec：每项 key tag 内嵌 `#` 存 amount，解码零中间对象分配、预分配容量。 */
    public final DataCodec<KeyCounter> KEY_COUNTER_DATA_CODEC = new DataCodec<>() {

        @Override
        public KeyCounter decode(@NotNull Data data, int dataVersion) {
            var list = data.asListData();
            var keyCounter = new KeyCounter();
            keyCounter.ensureCapacity(list.size()); // 预分配，避免 add 时多次扩容 rehash
            for (var entryTag : list) {
                var tag = DataCodecs.COMPOUND_TAG_CODEC.decode(entryTag, dataVersion);
                var what = AEKey.fromTagGeneric(tag);
                long amount = tag.getLong("#");
                if (what != null) {
                    keyCounter.add(what, amount);
                }
            }
            return keyCounter;
        }

        @Override
        public @NotNull Data encode(KeyCounter obj) {
            var list = new ListData(obj.size());
            for (var entry : obj) {
                var tag = entry.getKey().toTagGeneric();
                tag.putLong("#", entry.getLongValue());
                list.add(DataCodecs.COMPOUND_TAG_CODEC.encode(tag));
            }
            return list;
        }
    };
    /** KeyCounter（AEKey→long 计数集合）网络 codec：VarInt 数量 + 每项 key/amount（紧凑），解码预分配容量。 */
    public final ByteStreamCodec<KeyCounter> KEY_COUNTER_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, KeyCounter obj) {
            buf.writeVarInt(obj.size());
            for (var entry : obj) {
                AEKey.writeKey(buf, entry.getKey());
                buf.writeVarLong(entry.getLongValue());
            }
        }

        @Override
        public KeyCounter decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            var keyCounter = new KeyCounter();
            keyCounter.ensureCapacity(size); // 预分配，避免 add 时多次扩容 rehash
            for (int i = 0; i < size; i++) {
                var what = AEKey.readKey(buf);
                long amount = buf.readVarLong();
                if (what != null) {
                    keyCounter.add(what, amount);
                }
            }
            return keyCounter;
        }
    };

    public final DataCodec<AEItemKey> AE_ITEM_KEY_DATA_CODEC = new DataCodec<>() {

        @Override
        public AEItemKey decode(@NotNull Data data, int dataVersion) {
            return AEItemKey.fromTag(DataCodecs.COMPOUND_TAG_CODEC.decode(data, dataVersion));
        }

        @Override
        public @NotNull Data encode(AEItemKey obj) {
            return DataCodecs.COMPOUND_TAG_CODEC.encode(obj.toTag());
        }
    };

    public final DataCodec<AEFluidKey> AE_FLUID_KEY_DATA_CODEC = new DataCodec<>() {

        @Override
        public AEFluidKey decode(@NotNull Data data, int dataVersion) {
            return AEFluidKey.fromTag(DataCodecs.COMPOUND_TAG_CODEC.decode(data, dataVersion));
        }

        @Override
        public @NotNull Data encode(AEFluidKey obj) {
            return DataCodecs.COMPOUND_TAG_CODEC.encode(obj.toTag());
        }
    };

    public final DataCodec<AEKey> AE_KEY_DATA_CODEC = new DataCodec<>() {

        @Override
        public AEKey decode(@NotNull Data data, int dataVersion) {
            return AEKey.fromTagGeneric(DataCodecs.COMPOUND_TAG_CODEC.decode(data, dataVersion));
        }

        @Override
        public @NotNull Data encode(AEKey obj) {
            return DataCodecs.COMPOUND_TAG_CODEC.encode(obj.toTagGeneric());
        }
    };

    public final DataCodec<GenericStack> GENERIC_STACK_DATA_CODEC = new DataCodec<>() {

        @Override
        public GenericStack decode(@NotNull Data data, int dataVersion) {
            return GenericStack.readTag(DataCodecs.COMPOUND_TAG_CODEC.decode(data, dataVersion));
        }

        @Override
        public @NotNull Data encode(GenericStack obj) {
            return DataCodecs.COMPOUND_TAG_CODEC.encode(GenericStack.writeTag(obj));
        }
    };

    public final DataCodec<TechNode> TECH_NODE_DATA_CODEC = new DataCodec<>() {

        @Override
        public TechNode decode(@NotNull Data data, int dataVersion) {
            var list = data.getList();
            if (list.isEmpty()) return null;
            return resolveTechNode(list.get(0).getString(), list.get(1).getString());
        }

        @Override
        public @NotNull Data encode(TechNode obj) {
            var listData = new ListData(2);
            listData.addString(obj.getManager().getId());
            listData.addString(obj.name);
            return listData;
        }
    };

    public final ByteStreamCodec<AEItemKey> AE_ITEM_KEY_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, AEItemKey obj) {
            obj.writeToPacket(buf);
        }

        @Override
        public AEItemKey decode(FriendlyByteBuf buf) {
            return AEItemKey.fromPacket(buf);
        }
    };

    public final ByteStreamCodec<AEFluidKey> AE_FLUID_KEY_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, AEFluidKey obj) {
            obj.writeToPacket(buf);
        }

        @Override
        public AEFluidKey decode(FriendlyByteBuf buf) {
            return AEFluidKey.fromPacket(buf);
        }
    };

    public final ByteStreamCodec<AEKey> AE_KEY_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, AEKey obj) {
            AEKey.writeKey(buf, obj);
        }

        @Override
        public AEKey decode(FriendlyByteBuf buf) {
            return AEKey.readKey(buf);
        }
    };

    public final ByteStreamCodec<GenericStack> GENERIC_STACK_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, GenericStack obj) {
            AEKey.writeKey(buf, obj.what());
            buf.writeVarLong(obj.amount());
        }

        @Override
        public GenericStack decode(FriendlyByteBuf buf) {
            var what = AEKey.readKey(buf);
            if (what == null) {
                buf.readVarLong();
                return null;
            }
            return new GenericStack(what, buf.readVarLong());
        }
    };

    public final ByteStreamCodec<TechNode> TECH_NODE_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, TechNode obj) {
            var manager = obj.getManager();
            TechTreeManager.REGISTRY.streamCodec().encode(buf, manager);
            manager.streamCodec().encode(buf, obj);
        }

        @Override
        public TechNode decode(FriendlyByteBuf buf) {
            var manager = TechTreeManager.REGISTRY.streamCodec().decode(buf);
            if (manager == null) return null;
            return manager.streamCodec().decode(buf);
        }
    };

    private static TechNode resolveTechNode(String treeId, String nodeId) {
        if (treeId == null || nodeId == null) {
            return null;
        }
        TechTreeManager manager = TechTreeManager.getManager(treeId);
        return manager == null ? null : manager.getNode(nodeId);
    }

    /** 网络专用编解码器：按注册整数 id 编码（紧凑）。 */
    public final ByteStreamCodec<ResearchTag> RESEARCH_TAG_STREAM_CODEC = ResearchTag.TAGS.streamCodec();
    /** 持久化专用编解码器：按 name key 编码（自描述、跨版本稳定）。 */
    public final DataCodec<ResearchTag> RESEARCH_TAG_DATA_CODEC = ResearchTag.TAGS.dataCodec();
    public final DataCodec<ResearchPoints> RESEARCH_POINTS_DATA_CODEC = new DataCodec<>() {

        @Override
        public ResearchPoints decode(@NotNull Data data, int dataVersion) {
            var list = data.asListData();
            ResearchPoints points = new ResearchPoints();
            for (int i = 0; i < list.size(); i += 2) {
                ResearchTag tag = ResearchTag.TAGS.get(list.getString(i));
                if (tag != null) {
                    points.put(tag, list.getLong(i + 1));
                }
            }
            return points;
        }

        @Override
        public @NotNull Data encode(ResearchPoints obj) {
            var list = new ListData(obj.size() * 2);
            for (var it = obj.reference2LongEntrySet().fastIterator(); it.hasNext();) {
                var entry = it.next();
                list.addString(entry.getKey().getName());
                list.addLong(entry.getLongValue());
            }
            return list;
        }
    };

    public final ByteStreamCodec<ResearchPoints> RESEARCH_POINTS_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, ResearchPoints obj) {
            buf.writeVarInt(obj.size());
            for (var it = obj.reference2LongEntrySet().fastIterator(); it.hasNext();) {
                var entry = it.next();
                RESEARCH_TAG_STREAM_CODEC.encode(buf, entry.getKey());
                buf.writeVarLong(entry.getLongValue());
            }
        }

        @Override
        public ResearchPoints decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            ResearchPoints points = new ResearchPoints();
            for (int i = 0; i < size; i++) {
                ResearchTag tag = RESEARCH_TAG_STREAM_CODEC.decode(buf);
                long amount = buf.readVarLong();
                if (tag != null) {
                    points.put(tag, amount);
                }
            }
            return points;
        }
    };

    public final DataSyncCodec<ResearchPoints> RESEARCH_POINTS_SYNC_CODEC = DataSyncCodec.register(ResearchPoints.class, RESEARCH_POINTS_STREAM_CODEC, RESEARCH_POINTS_DATA_CODEC);
    public final DataSyncCodec<AEKey> AE_KEY_SYNC_CODEC = DataSyncCodec.register(AEKey.class, AE_KEY_STREAM_CODEC, AE_KEY_DATA_CODEC);
    public final DataSyncCodec<AEItemKey> AE_ITEM_KEY_SYNC_CODEC = DataSyncCodec.register(AEItemKey.class, AE_ITEM_KEY_STREAM_CODEC, AE_ITEM_KEY_DATA_CODEC);
    public final DataSyncCodec<AEFluidKey> AE_FLUID_KEY_SYNC_CODEC = DataSyncCodec.register(AEFluidKey.class, AE_FLUID_KEY_STREAM_CODEC, AE_FLUID_KEY_DATA_CODEC);
    public final DataSyncCodec<GenericStack> GENERIC_STACK_SYNC_CODEC = DataSyncCodec.register(GenericStack.class, GENERIC_STACK_STREAM_CODEC, GENERIC_STACK_DATA_CODEC);
    public final DataSyncCodec<TechNode> TECH_NODE_SYNC_CODEC = DataSyncCodec.register(TechNode.class, TECH_NODE_STREAM_CODEC, TECH_NODE_DATA_CODEC);
    public final DataSyncCodec<ResearchTag> RESEARCH_TAG_SYNC_CODEC = DataSyncCodec.register(ResearchTag.class, RESEARCH_TAG_STREAM_CODEC, RESEARCH_TAG_DATA_CODEC);
    /** KeyCounter 的组合 DataSyncCodec（register 一步注册到全局，返回 CombinedCodec 供 composite 组合）。 */
    public final DataSyncCodec<KeyCounter> KEY_COUNTER_SYNC_CODEC = DataSyncCodec.register(KeyCounter.class, KEY_COUNTER_STREAM_CODEC, KEY_COUNTER_DATA_CODEC);

    public static void init() {
        // 其余类型均已由字段处 DataSyncCodec.register(...) 一步注册到全局；
        // 仅 ScanningRecipeExtion.AEKEYDATACRYSTAL 是 CombinedCodec.composite 构建（不注册），在此用实例方法补注册。
        ScanningRecipeExtion.AEKEYDATACRYSTAL_CODEC.register(ScanningRecipeExtion.AEKeyDataCrystal.class);
    }
}
