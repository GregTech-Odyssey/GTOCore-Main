package com.gtocore.common.data;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.techtree.TechNode;
import com.gtocore.api.techtree.TechTreeManager;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datasream.codec.ByteStreamCodec;
import com.gto.datasynclib.datasream.codec.DataCodec;
import com.gto.datasynclib.datasream.data.Data;
import com.gto.datasynclib.datasream.data.MapData;
import com.gto.datasynclib.util.DataCodecs;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class GTOCodecs {

    private static final String TECH_TREE_KEY = "tree";
    private static final String TECH_NODE_KEY = "node";

    public static void init() {
        DataSyncCodec.register(AEItemKey.class, GTOCodecs.AE_ITEM_KEY_STREAM_CODEC, GTOCodecs.AE_ITEM_KEY_DATA_CODEC);
        DataSyncCodec.register(AEFluidKey.class, GTOCodecs.AE_FLUID_KEY_STREAM_CODEC, GTOCodecs.AE_FLUID_KEY_DATA_CODEC);
        DataSyncCodec.register(AEKey.class, GTOCodecs.AE_KEY_STREAM_CODEC, GTOCodecs.AE_KEY_DATA_CODEC);
        DataSyncCodec.register(GenericStack.class, GTOCodecs.GENERIC_STACK_STREAM_CODEC, GTOCodecs.GENERIC_STACK_DATA_CODEC);
        DataSyncCodec.register(TechNode.class, TECH_NODE_STREAM_CODEC, TECH_NODE_DATA_CODEC);
        DataSyncCodec.register(ResearchTag.class, RESEARCH_TAG_STREAM_CODEC, RESEARCH_TAG_DATA_CODEC);
        DataSyncCodec.register(ResearchPoints.class, RESEARCH_POINTS_STREAM_CODEC, RESEARCH_POINTS_DATA_CODEC);
    }

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
            if (!(data instanceof MapData mapData)) {
                return null;
            }
            return resolveTechNode(mapData.getString(TECH_TREE_KEY), mapData.getString(TECH_NODE_KEY));
        }

        @Override
        public @NotNull Data encode(TechNode obj) {
            MapData mapData = new MapData(2);
            mapData.putString(TECH_TREE_KEY, obj.getManager().getId());
            mapData.putString(TECH_NODE_KEY, obj.name);
            return mapData;
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
            buf.writeUtf(obj.getManager().getId());
            buf.writeUtf(obj.name);
        }

        @Override
        public TechNode decode(FriendlyByteBuf buf) {
            return resolveTechNode(buf.readUtf(), buf.readUtf());
        }
    };

    private static TechNode resolveTechNode(String treeId, String nodeId) {
        if (treeId == null || nodeId == null) {
            return null;
        }
        TechTreeManager manager = TechTreeManager.getManager(treeId);
        return manager == null ? null : manager.getNode(nodeId);
    }

    public final DataCodec<ResearchTag> RESEARCH_TAG_DATA_CODEC = new DataCodec<>() {

        @Override
        public ResearchTag decode(@NotNull Data data, int dataVersion) {
            if (!(data instanceof MapData mapData)) {
                return null;
            }
            return ResearchTag.TAGS.get(mapData.getString("id"));
        }

        @Override
        public @NotNull Data encode(ResearchTag obj) {
            MapData mapData = new MapData(1);
            mapData.putString("id", obj.getName());
            return mapData;
        }
    };
    public final ByteStreamCodec<ResearchTag> RESEARCH_TAG_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, ResearchTag obj) {
            buf.writeUtf(obj.getName());
        }

        @Override
        public ResearchTag decode(FriendlyByteBuf buf) {
            return ResearchTag.TAGS.get(buf.readUtf());
        }
    };
    public final DataCodec<ResearchPoints> RESEARCH_POINTS_DATA_CODEC = new DataCodec<>() {

        @Override
        public ResearchPoints decode(@NotNull Data data, int dataVersion) {
            if (!(data instanceof MapData mapData)) {
                return null;
            }
            ResearchPoints points = new ResearchPoints();
            for (var entry : mapData.entrySet()) {
                ResearchTag tag = ResearchTag.TAGS.get(entry.getKey());
                if (tag != null) {
                    points.put(tag, entry.getValue().getLong());
                }
            }
            return points;
        }

        @Override
        public @NotNull Data encode(ResearchPoints obj) {
            MapData mapData = new MapData(obj.size());
            for (var entry : obj.reference2LongEntrySet()) {
                mapData.putLong(entry.getKey().getName(), entry.getLongValue());
            }
            return mapData;
        }
    };

    public final ByteStreamCodec<ResearchPoints> RESEARCH_POINTS_STREAM_CODEC = new ByteStreamCodec<>() {

        @Override
        public void encode(FriendlyByteBuf buf, ResearchPoints obj) {
            buf.writeVarInt(obj.size());
            for (var entry : obj.reference2LongEntrySet()) {
                buf.writeUtf(entry.getKey().getName());
                buf.writeVarLong(entry.getLongValue());
            }
        }

        @Override
        public ResearchPoints decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            ResearchPoints points = new ResearchPoints();
            for (int i = 0; i < size; i++) {
                String tagName = buf.readUtf();
                long amount = buf.readVarLong();
                ResearchTag tag = ResearchTag.TAGS.get(tagName);
                if (tag != null) {
                    points.put(tag, amount);
                }
            }
            return points;
        }
    };
}
