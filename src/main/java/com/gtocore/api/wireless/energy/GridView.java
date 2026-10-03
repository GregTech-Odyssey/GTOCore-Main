package com.gtocore.api.wireless.energy;

import com.gtolib.api.data.Dimension;

import com.gregtechceu.gtceu.uipro.data.UICodecs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.datastream.codec.ByteStreamCodec;
import com.gto.datasynclib.util.StreamCodecs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 电网星图的同步快照：拓扑（节点与线路，变化才换实例）与实时数据（每秒一帧）。快照实例不重写 equals，同步只比较引用。
 */
public final class GridView {

    public static final int MAX_NODES = 128;
    public static final int MAX_LINES = 256;
    public static final int WINDOWS = 4;
    private static final Dimension[] BY_ID = byId();

    public record NodeInfo(ResourceKey<Level> dimension, int tier, int reachTier, float capacity, int towers, int topTowerTier,
                           int relays) {}

    public record LineInfo(int a, int b, int tier, long budget) {}

    public static final class TopologyView {

        public static final TopologyView EMPTY = new TopologyView(0, false, Collections.emptyList(), Collections.emptyList());

        private final int revision;
        private final boolean truncated;
        private final List<NodeInfo> nodes;
        private final List<LineInfo> lines;

        public TopologyView(int revision, boolean truncated, List<NodeInfo> nodes, List<LineInfo> lines) {
            this.revision = revision;
            this.truncated = truncated;
            this.nodes = nodes;
            this.lines = lines;
        }

        public int revision() {
            return revision;
        }

        public boolean isTruncated() {
            return truncated;
        }

        public List<NodeInfo> nodes() {
            return nodes;
        }

        public List<LineInfo> lines() {
            return lines;
        }

        public int indexOf(@Nullable ResourceKey<Level> dimension) {
            if (dimension == null) return -1;
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).dimension() == dimension) return i;
            }
            return -1;
        }

        private static TopologyView decoded(int revision, boolean truncated, List<NodeInfo> nodes, List<LineInfo> lines) {
            var valid = new ObjectArrayList<LineInfo>(lines.size());
            for (var line : lines) {
                if (line.a() >= 0 && line.b() >= 0 && line.a() < nodes.size() && line.b() < nodes.size() && line.a() != line.b()) valid.add(line);
            }
            return new TopologyView(revision, truncated, nodes, valid);
        }
    }

    public static final class LiveView {

        private static final float[] NONE = new float[0];
        public static final LiveView EMPTY = new LiveView(0, 0, NONE, NONE, NONE, NONE, 0);

        private final int revision;
        private final int topologyRevision;
        private final float[] storage;
        private final float[] storageDelta;
        private final float[] flowAB;
        private final float[] flowBA;
        private final float maxFlow;

        public LiveView(int revision, int topologyRevision, float[] storage, float[] storageDelta, float[] flowAB, float[] flowBA, float maxFlow) {
            this.revision = revision;
            this.topologyRevision = topologyRevision;
            this.storage = storage;
            this.storageDelta = storageDelta;
            this.flowAB = flowAB;
            this.flowBA = flowBA;
            this.maxFlow = maxFlow;
        }

        public int revision() {
            return revision;
        }

        public int topologyRevision() {
            return topologyRevision;
        }

        public float storage(int node) {
            return node >= 0 && node < storage.length ? storage[node] : 0;
        }

        public float storageDelta(int node) {
            return node >= 0 && node < storageDelta.length ? storageDelta[node] : 0;
        }

        public float flowAB(int line) {
            return line >= 0 && line < flowAB.length ? flowAB[line] : 0;
        }

        public float flowBA(int line) {
            return line >= 0 && line < flowBA.length ? flowBA[line] : 0;
        }

        public float maxFlow() {
            return maxFlow;
        }

        float[] storageArray() {
            return storage;
        }

        float[] storageDeltaArray() {
            return storageDelta;
        }

        float[] flowABArray() {
            return flowAB;
        }

        float[] flowBAArray() {
            return flowBA;
        }
    }

    public record Summary(int revision, double stored, double capacity, double storageDelta, int nodes, int lines, int activeLines,
                          boolean truncated, double[] input, double[] output, double[] loss) {

        public static final Summary EMPTY = new Summary(0, 0, 0, 0, 0, 0, 0, false, new double[WINDOWS], new double[WINDOWS], new double[WINDOWS]);

        public boolean isEmpty() {
            return nodes == 0;
        }

        public double input(int window) {
            return input[clampWindow(window)];
        }

        public double output(int window) {
            return output[clampWindow(window)];
        }

        public double loss(int window) {
            return loss[clampWindow(window)];
        }

        public double net(int window) {
            int w = clampWindow(window);
            return input[w] - output[w] - loss[w];
        }
    }

    public static final ByteStreamCodec<ResourceKey<Level>> DIMENSION = ByteStreamCodec.of((buf, key) -> {
        int ref = dimRef(key);
        ByteStreamCodec.INT_CODEC.encode(buf, ref);
        if (ref == 0) StreamCodecs.RESOURCE_LOCATION_CODEC.encode(buf, key.location());
    }, buf -> {
        int ref = ByteStreamCodec.INT_CODEC.decode(buf);
        if (ref != 0) {
            var key = dimension(ref);
            return key != null ? key : Level.OVERWORLD;
        }
        return ResourceKey.create(Registries.DIMENSION, StreamCodecs.RESOURCE_LOCATION_CODEC.decode(buf));
    });

    private static final ByteStreamCodec<Integer> TIER = ByteStreamCodec.convert(ByteStreamCodec.BYTE_CODEC, Integer::byteValue, Byte::intValue);
    private static final ByteStreamCodec<Integer> COUNT = ByteStreamCodec.convert(ByteStreamCodec.SHORT_CODEC, i -> (short) Math.min(Short.MAX_VALUE, i),
            s -> Math.max(0, (int) s));

    public static final ByteStreamCodec<NodeInfo> NODE = ByteStreamCodec.composite(
            DIMENSION, NodeInfo::dimension,
            TIER, NodeInfo::tier,
            TIER, NodeInfo::reachTier,
            ByteStreamCodec.FLOAT_CODEC, NodeInfo::capacity,
            COUNT, NodeInfo::towers,
            TIER, NodeInfo::topTowerTier,
            COUNT, NodeInfo::relays,
            (dimension, tier, reach, capacity, towers, top, relays) -> new NodeInfo(dimension, ProviderRegistry.clampTier(tier),
                    ProviderRegistry.clampTier(reach), Math.max(0, capacity), towers, ProviderRegistry.clampTier(top), relays));

    public static final ByteStreamCodec<LineInfo> LINE = ByteStreamCodec.composite(
            COUNT, LineInfo::a,
            COUNT, LineInfo::b,
            TIER, LineInfo::tier,
            ByteStreamCodec.LONG_CODEC, LineInfo::budget,
            (a, b, tier, budget) -> new LineInfo(a, b, ProviderRegistry.clampLineTier(tier), Math.max(0, budget)));

    public static final ByteStreamCodec<TopologyView> TOPOLOGY = ByteStreamCodec.composite(
            ByteStreamCodec.INT_CODEC, TopologyView::revision,
            ByteStreamCodec.BOOLEAN_CODEC, TopologyView::isTruncated,
            UICodecs.list(NODE, MAX_NODES), TopologyView::nodes,
            UICodecs.list(LINE, MAX_LINES), TopologyView::lines,
            TopologyView::decoded);

    public static final ByteStreamCodec<LiveView> LIVE = ByteStreamCodec.composite(
            ByteStreamCodec.INT_CODEC, LiveView::revision,
            ByteStreamCodec.INT_CODEC, LiveView::topologyRevision,
            UICodecs.floats(MAX_NODES), LiveView::storageArray,
            UICodecs.floats(MAX_NODES), LiveView::storageDeltaArray,
            UICodecs.floats(MAX_LINES), LiveView::flowABArray,
            UICodecs.floats(MAX_LINES), LiveView::flowBAArray,
            ByteStreamCodec.FLOAT_CODEC, LiveView::maxFlow,
            LiveView::new);

    private GridView() {}

    public static int clampWindow(int window) {
        return Math.max(0, Math.min(WINDOWS - 1, window));
    }

    public static int dimRef(@Nullable ResourceKey<Level> dimension) {
        if (dimension == null) return 0;
        var registered = Dimension.get(dimension);
        return registered == null ? 0 : registered.getId() + 1;
    }

    @Nullable
    public static ResourceKey<Level> dimension(int ref) {
        int id = ref - 1;
        if (id < 0 || id >= BY_ID.length || BY_ID[id] == null) return null;
        return BY_ID[id].getResourceKey();
    }

    private static Dimension[] byId() {
        int max = -1;
        for (var dimension : Dimension.all()) max = Math.max(max, dimension.getId());
        var table = new Dimension[max + 1];
        for (var dimension : Dimension.all()) table[dimension.getId()] = dimension;
        return table;
    }
}
