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
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.datasynclib.datastream.codec.ValueCodec;
import com.gto.datasynclib.datastream.codec.ValueOps;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@UtilityClass
public class GTOCodecs {

    public final ValueCodec<AEKey> AE_KEY_DATA_CODEC = KeyCodecs.AE_KEY_DATA_CODEC;
    public final ValueCodec<AEItemKey> AE_ITEM_KEY_DATA_CODEC = KeyCodecs.AE_ITEM_KEY_DATA_CODEC;
    public final ValueCodec<AEFluidKey> AE_FLUID_KEY_DATA_CODEC = KeyCodecs.AE_FLUID_KEY_DATA_CODEC;
    public final ValueCodec<GenericStack> GENERIC_STACK_DATA_CODEC = KeyCodecs.GENERIC_STACK_DATA_CODEC;
    public final ValueCodec<KeyCounter> KEY_COUNTER_DATA_CODEC = KeyCodecs.KEY_COUNTER_DATA_CODEC;
    public final StreamCodec<FriendlyByteBuf, AEKey> AE_KEY_STREAM_CODEC = KeyCodecs.AE_KEY_STREAM_CODEC;
    public final StreamCodec<FriendlyByteBuf, AEItemKey> AE_ITEM_KEY_STREAM_CODEC = KeyCodecs.AE_ITEM_KEY_STREAM_CODEC;
    public final StreamCodec<FriendlyByteBuf, AEFluidKey> AE_FLUID_KEY_STREAM_CODEC = KeyCodecs.AE_FLUID_KEY_STREAM_CODEC;
    public final StreamCodec<FriendlyByteBuf, GenericStack> GENERIC_STACK_STREAM_CODEC = KeyCodecs.GENERIC_STACK_STREAM_CODEC;
    public final StreamCodec<FriendlyByteBuf, KeyCounter> KEY_COUNTER_STREAM_CODEC = KeyCodecs.KEY_COUNTER_STREAM_CODEC;

    public final ValueCodec<TechNode> TECH_NODE_DATA_CODEC = new ValueCodec<>() {

        @Override
        public TechNode decode(ValueOps ops, @NotNull Object data) {
            var list = ops.getList(data);
            if (list.isEmpty()) return null;
            return resolveTechNode(ops.getString(list, 0), ops.getString(list, 1));
        }

        @Override
        public @NotNull Object encode(ValueOps ops, TechNode obj) {
            var list = new ArrayList<Object>(2);
            ops.addString(list, obj.getManager().getId());
            ops.addString(list, obj.name);
            return ops.createList(list);
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
    public final ValueCodec<ResearchTag> RESEARCH_TAG_DATA_CODEC = ResearchTag.TAGS.valueCodec();
    public final ValueCodec<ResearchPoints> RESEARCH_POINTS_DATA_CODEC = new ValueCodec<>() {

        @Override
        public ResearchPoints decode(ValueOps ops, @NotNull Object data) {
            var list = ops.getList(data);
            ResearchPoints points = new ResearchPoints();
            for (int i = 0; i < list.size(); i += 2) {
                ResearchTag tag = ResearchTag.TAGS.get(ops.getString(list, i));
                if (tag != null) {
                    points.put(tag, ops.getLong(list, i + 1));
                }
            }
            return points;
        }

        @Override
        public @NotNull Object encode(ValueOps ops, ResearchPoints obj) {
            var list = new ArrayList<Object>(obj.size() * 2);
            for (var it = obj.reference2LongEntrySet().fastIterator(); it.hasNext();) {
                var entry = it.next();
                ops.addString(list, entry.getKey().getName());
                ops.addLong(list, entry.getLongValue());
            }
            return ops.createList(list);
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
