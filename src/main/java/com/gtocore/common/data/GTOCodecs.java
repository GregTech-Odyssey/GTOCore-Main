package com.gtocore.common.data;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.recipe.ScanningRecipeExtion;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.common.machine.mana.multiblock.ResonanceFlowerMachine;
import com.gtocore.common.machine.noenergy.slotMachine.SlotMachineResult;
import com.gtocore.common.machine.noenergy.slotMachine.SlotMachineState;

import com.gregtechceu.gtceu.api.transfer.key.KeyCodecs;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.codec.DataCodec;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class GTOCodecs {

    public final DataCodec<AEKey> AE_KEY_DATA_CODEC = KeyCodecs.AE_KEY_DATA_CODEC;
    public final DataCodec<AEItemKey> AE_ITEM_KEY_DATA_CODEC = KeyCodecs.AE_ITEM_KEY_DATA_CODEC;
    public final DataCodec<AEFluidKey> AE_FLUID_KEY_DATA_CODEC = KeyCodecs.AE_FLUID_KEY_DATA_CODEC;
    public final DataCodec<GenericStack> GENERIC_STACK_DATA_CODEC = KeyCodecs.GENERIC_STACK_DATA_CODEC;
    public final DataCodec<KeyCounter> KEY_COUNTER_DATA_CODEC = KeyCodecs.KEY_COUNTER_DATA_CODEC;
    public final StreamCodec<FriendlyByteBuf, AEKey> AE_KEY_STREAM_CODEC = KeyCodecs.AE_KEY_STREAM_CODEC;
    public final StreamCodec<FriendlyByteBuf, AEItemKey> AE_ITEM_KEY_STREAM_CODEC = KeyCodecs.AE_ITEM_KEY_STREAM_CODEC;
    public final StreamCodec<FriendlyByteBuf, AEFluidKey> AE_FLUID_KEY_STREAM_CODEC = KeyCodecs.AE_FLUID_KEY_STREAM_CODEC;
    public final StreamCodec<FriendlyByteBuf, GenericStack> GENERIC_STACK_STREAM_CODEC = KeyCodecs.GENERIC_STACK_STREAM_CODEC;
    public final StreamCodec<FriendlyByteBuf, KeyCounter> KEY_COUNTER_STREAM_CODEC = KeyCodecs.KEY_COUNTER_STREAM_CODEC;

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

    public final StreamCodec<FriendlyByteBuf, TechNode> TECH_NODE_STREAM_CODEC = new StreamCodec<>() {

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
    public final StreamCodec<FriendlyByteBuf, ResearchTag> RESEARCH_TAG_STREAM_CODEC = ResearchTag.TAGS.streamCodec();
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

    public final StreamCodec<FriendlyByteBuf, ResearchPoints> RESEARCH_POINTS_STREAM_CODEC = new StreamCodec<>() {

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
    public final DataSyncCodec<TechNode> TECH_NODE_SYNC_CODEC = DataSyncCodec.register(TechNode.class, TECH_NODE_STREAM_CODEC, TECH_NODE_DATA_CODEC);
    public final DataSyncCodec<ResearchTag> RESEARCH_TAG_SYNC_CODEC = DataSyncCodec.register(ResearchTag.class, RESEARCH_TAG_STREAM_CODEC, RESEARCH_TAG_DATA_CODEC);
    public final DataSyncCodec<KeyCounter> KEY_COUNTER_SYNC_CODEC = DataSyncCodec.of(KeyCodecs.KEY_COUNTER_STREAM_CODEC, KeyCodecs.KEY_COUNTER_DATA_CODEC);

    public static void init() {
        // 字段处使用 register(...) 的类型已注册；composite(...) 构建的自定义类型在此集中注册。
        ScanningRecipeExtion.AEKEYDATACRYSTAL_CODEC.register(ScanningRecipeExtion.AEKeyDataCrystal.class);
        ResonanceFlowerMachine.addCodec();
        SlotMachineResult.addCodec();
        SlotMachineState.addCodec();
    }
}
