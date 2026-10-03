package com.gtocore.api.wireless.energy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 按队伍共享的电网采样器（引用计数）：只在有人打开电网界面时存在，每 {@link #INTERVAL} tick 采样一次。
 * 拓扑在账户版本、节点数、弧数或电压变化时才重建；线路流量取弧累计搬运量之差加当前预约，做指数平均。
 */
public final class GridSampler {

    public static final int INTERVAL = 20;
    private static final double ALPHA = 0.5;
    private static final int DETAIL_KEEP_TICKS = INTERVAL * 3;
    private static final float[] NO_FLOATS = new float[0];
    private static final Reference2ObjectOpenHashMap<EnergyAccount, GridSampler> SAMPLERS = new Reference2ObjectOpenHashMap<>();
    private static int revisions;

    private final EnergyAccount account;
    private final Reference2ObjectOpenHashMap<ResourceKey<Level>, NodeDetail> details = new Reference2ObjectOpenHashMap<>();
    private int refs;
    private int lastTick = Integer.MIN_VALUE;
    private int version = -1, nodeCount = -1, arcCount = -1, reachHash;
    private GridNode[] nodes = GridNode.NO_NODES;
    private Arc[] lineAB = GridNode.NO_ARCS, lineBA = GridNode.NO_ARCS;
    private float[] storage = NO_FLOATS, storageDelta = NO_FLOATS, flowAB = NO_FLOATS, flowBA = NO_FLOATS;
    private float maxFlow;
    private int activeLines;
    private double lastStored, storedDelta;
    private GridView.TopologyView topology = GridView.TopologyView.EMPTY;
    private GridView.LiveView live = GridView.LiveView.EMPTY;
    private GridView.Summary summary = GridView.Summary.EMPTY;

    private GridSampler(EnergyAccount account) {
        this.account = account;
    }

    public static GridSampler acquire(EnergyAccount account) {
        var sampler = SAMPLERS.get(account);
        if (sampler == null) {
            sampler = new GridSampler(account);
            SAMPLERS.put(account, sampler);
        }
        sampler.refs++;
        return sampler;
    }

    public void release() {
        if (refs <= 0) return;
        if (--refs == 0) SAMPLERS.remove(account, this);
    }

    @Nullable
    static GridSampler peek(EnergyAccount account) {
        return SAMPLERS.get(account);
    }

    static void clearAll() {
        SAMPLERS.clear();
    }

    public void sample(long tick) {
        sample();
    }

    public void sample() {
        int now = GridClock.tick();
        boolean first = lastTick == Integer.MIN_VALUE || now < lastTick;
        if (!first && now - lastTick < INTERVAL) return;
        int dt = first ? INTERVAL : now - lastTick;
        if (topologyChanged()) rebuildTopology();
        sampleLines(now, dt, first);
        sampleNodes(now, dt, first);
        publishLive();
        summarize(dt, first);
        lastTick = now;
        pruneDetails(now);
    }

    public EnergyAccount account() {
        return account;
    }

    public GridView.TopologyView topology() {
        return topology;
    }

    public GridView.LiveView live() {
        return live;
    }

    public GridView.Summary summary() {
        return summary;
    }

    public NodeDetail detail(ResourceKey<Level> dimension) {
        var key = GridBody.of(dimension);
        int now = GridClock.tick();
        var cached = details.get(key);
        if (cached != null && now >= cached.tick && now - cached.tick < INTERVAL) {
            cached.requested = now;
            return cached;
        }
        var fresh = NodeDetail.compute(account, this, key, cached);
        fresh.requested = now;
        details.put(key, fresh);
        return fresh;
    }

    int lastTick() {
        return lastTick;
    }

    private void pruneDetails(int now) {
        if (details.isEmpty()) return;
        for (var it = details.values().iterator(); it.hasNext();) {
            int idle = now - it.next().requested;
            if (idle < 0 || idle > DETAIL_KEEP_TICKS) it.remove();
        }
    }

    private boolean topologyChanged() {
        return account.version != version || account.nodeList.size() != nodeCount || account.arcs.size() != arcCount || reachHash() != reachHash;
    }

    private int reachHash() {
        int hash = 1;
        var list = account.nodeList;
        for (int i = 0, n = list.size(); i < n; i++) {
            var node = list.get(i);
            hash = hash * 31 + (node.reachTier << 8 | node.tier & 0xFF);
        }
        return hash;
    }

    private void rebuildTopology() {
        var list = account.nodeList;
        int n = Math.min(list.size(), GridView.MAX_NODES);
        boolean truncated = list.size() > n;
        nodes = new GridNode[n];
        var index = new Reference2IntOpenHashMap<GridNode>(n);
        index.defaultReturnValue(-1);
        for (int i = 0; i < n; i++) {
            nodes[i] = list.get(i);
            index.put(nodes[i], i);
        }
        truncated |= collectLines(index);
        var lines = new ObjectArrayList<GridView.LineInfo>(lineAB.length);
        for (var arc : lineAB) lines.add(new GridView.LineInfo(index.getInt(arc.from), index.getInt(arc.to), arc.tier, arc.budget));
        topology = new GridView.TopologyView(++revisions, truncated, nodeInfos(index), lines);
        storage = new float[n];
        storageDelta = new float[n];
        flowAB = new float[lineAB.length];
        flowBA = new float[lineAB.length];
        version = account.version;
        nodeCount = list.size();
        arcCount = account.arcs.size();
        reachHash = reachHash();
    }

    private boolean collectLines(Reference2IntOpenHashMap<GridNode> index) {
        var picked = new ObjectArrayList<Arc>();
        boolean truncated = false;
        for (var arc : account.arcs) {
            if (arc.reverse == null) continue;
            int a = index.getInt(arc.from), b = index.getInt(arc.to);
            if (a < 0 || b < 0 || a > b) {
                truncated |= a < 0 || b < 0;
                continue;
            }
            if (picked.size() >= GridView.MAX_LINES) {
                truncated = true;
                continue;
            }
            picked.add(arc);
        }
        lineAB = picked.toArray(GridNode.NO_ARCS);
        lineBA = new Arc[lineAB.length];
        for (int l = 0; l < lineAB.length; l++) lineBA[l] = lineAB[l].reverse;
        return truncated;
    }

    private List<GridView.NodeInfo> nodeInfos(Reference2IntOpenHashMap<GridNode> index) {
        int n = nodes.length;
        var towers = new int[n];
        var top = new int[n];
        var relays = new int[n];
        Arrays.fill(top, -1);
        for (var tower : account.towers.values()) {
            int i = indexOf(index, tower.pos().dimension());
            if (i < 0) continue;
            towers[i]++;
            top[i] = Math.max(top[i], tower.tier());
        }
        for (var relay : account.relays.values()) {
            int i = indexOf(index, relay.pos().dimension());
            if (i >= 0) relays[i]++;
        }
        var infos = new ObjectArrayList<GridView.NodeInfo>(n);
        for (int i = 0; i < n; i++) {
            var node = nodes[i];
            infos.add(new GridView.NodeInfo(node.dimension, node.tier, node.reachTier, (float) node.capacityDouble(), towers[i], top[i], relays[i]));
        }
        return infos;
    }

    private int indexOf(Reference2IntOpenHashMap<GridNode> index, ResourceKey<Level> dimension) {
        var node = account.nodes.get(GridBody.of(dimension));
        return node == null ? -1 : index.getInt(node);
    }

    private void sampleLines(int now, int dt, boolean first) {
        float max = 0;
        int active = 0;
        for (int l = 0; l < lineAB.length; l++) {
            float ab = sampleArc(lineAB[l], now, dt, first);
            float ba = sampleArc(lineBA[l], now, dt, first);
            flowAB[l] = ab;
            flowBA[l] = ba;
            float top = Math.max(ab, ba);
            if (top > 0) active++;
            max = Math.max(max, top);
        }
        maxFlow = max;
        activeLines = active;
    }

    private float sampleArc(Arc arc, int now, int dt, boolean first) {
        double instant = arc.reserved(now);
        if (!first && arc.sampledTick == lastTick) instant += U126.difference(arc.carriedHi, arc.carriedLo, arc.sampledHi, arc.sampledLo) / dt;
        double ema = first ? instant : ALPHA * instant + (1 - ALPHA) * arc.sampledFlow;
        float flow = ema < 1 ? 0 : (float) ema;
        arc.sampledHi = arc.carriedHi;
        arc.sampledLo = arc.carriedLo;
        arc.sampledTick = now;
        arc.sampledFlow = flow;
        return flow;
    }

    private void sampleNodes(int now, int dt, boolean first) {
        for (int i = 0; i < nodes.length; i++) {
            var node = nodes[i];
            long hi = Math.max(0, node.hi), lo = node.hi < 0 ? 0 : node.lo;
            double delta = 0;
            if (!first && node.sampledTick == lastTick) {
                double instant = U126.signedDifference(hi, lo, node.sampledHi, node.sampledLo) / dt;
                delta = ALPHA * instant + (1 - ALPHA) * node.sampledDelta;
            }
            float value = Math.abs(delta) < 1 ? 0 : (float) delta;
            node.sampledHi = hi;
            node.sampledLo = lo;
            node.sampledTick = now;
            node.sampledDelta = value;
            storage[i] = (float) U126.toDouble(hi, lo);
            storageDelta[i] = value;
        }
    }

    private void publishLive() {
        var old = live;
        if (old.topologyRevision() == topology.revision() && old.maxFlow() == maxFlow && Arrays.equals(storage, old.storageArray()) &&
                Arrays.equals(storageDelta, old.storageDeltaArray()) && Arrays.equals(flowAB, old.flowABArray()) && Arrays.equals(flowBA, old.flowBAArray()))
            return;
        live = new GridView.LiveView(++revisions, topology.revision(), storage.clone(), storageDelta.clone(), flowAB.clone(), flowBA.clone(), maxFlow);
    }

    private void summarize(int dt, boolean first) {
        double stored = account.totalStorageDouble();
        double capacity = account.totalCapacityDouble();
        double delta = first ? 0 : ALPHA * ((stored - lastStored) / dt) + (1 - ALPHA) * storedDelta;
        storedDelta = Math.abs(delta) < 1 ? 0 : delta;
        lastStored = stored;
        var stats = account.stats();
        int second = GridClock.second();
        var input = NodeMeter.windows(stats, NodeMeter.IN, second);
        var output = NodeMeter.windows(stats, NodeMeter.OUT, second);
        var loss = NodeMeter.windows(stats, NodeMeter.LOSS, second);
        int lines = account.arcs.size() >> 1;
        var old = summary;
        if (old != GridView.Summary.EMPTY && old.stored() == stored && old.capacity() == capacity && old.storageDelta() == storedDelta &&
                old.nodes() == account.nodeList.size() && old.lines() == lines && old.activeLines() == activeLines && old.truncated() == topology.isTruncated() &&
                Arrays.equals(old.input(), input) && Arrays.equals(old.output(), output) && Arrays.equals(old.loss(), loss))
            return;
        summary = new GridView.Summary(++revisions, stored, capacity, storedDelta, account.nodeList.size(), lines, activeLines, topology.isTruncated(), input, output, loss);
    }
}
