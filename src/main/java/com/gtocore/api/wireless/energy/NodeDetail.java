package com.gtocore.api.wireless.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.jetbrains.annotations.Nullable;

/**
 * 单个电网节点的详情（只为打开着的星球卡片计算）：能源塔、中继、存量、各时间窗流量与流量最大的端点。
 * 流量按端点所在节点归属；经线路的流入流出取采样器最近一次的线路均值，均为 EU/t。
 */
public final class NodeDetail {

    public static final int MAX_PORTS = 16;
    private static final double[] ZERO = new double[GridView.WINDOWS];
    private static final Port[] NO_PORTS = new Port[0];
    private static final EnergyPort[] TOP_PORTS = new EnergyPort[MAX_PORTS];
    private static final ServerLevel[] TOP_LEVELS = new ServerLevel[MAX_PORTS];
    private static final double[] TOP_RATES = new double[MAX_PORTS];
    private static int revisions;
    public static final NodeDetail EMPTY = new NodeDetail(0);

    public record Port(int key, Component name, ResourceKey<Level> dimension, BlockPos pos, double rate) {}

    final int tick;
    int requested;
    private int revision;
    private int towers, topTowerTier = -1, relays, portCount;
    private double stored, capacity, storageDelta, lossPercent, lineIn, lineOut;
    private double[] input = ZERO, output = ZERO, loss = ZERO;
    private Port[] ports = NO_PORTS;
    private IntList keys = IntLists.emptyList();

    private NodeDetail(int tick) {
        this.tick = tick;
    }

    public static NodeDetail compute(EnergyAccount account, ResourceKey<Level> dimension) {
        return compute(account, GridSampler.peek(account), GridBody.of(dimension), null);
    }

    static NodeDetail compute(EnergyAccount account, @Nullable GridSampler sampler, ResourceKey<Level> key, @Nullable NodeDetail previous) {
        var detail = new NodeDetail(GridClock.tick());
        detail.countProviders(account, key);
        var node = account.nodes.get(key);
        if (node != null) detail.readNode(node, sampler);
        detail.collectPorts(account, key);
        detail.revision = previous != null && previous.keys.equals(detail.keys) ? previous.revision : ++revisions;
        return detail;
    }

    private void countProviders(EnergyAccount account, ResourceKey<Level> key) {
        for (var tower : account.towers.values()) {
            if (GridBody.of(tower.pos().dimension()) != key) continue;
            towers++;
            topTowerTier = Math.max(topTowerTier, tower.tier());
        }
        for (var relay : account.relays.values()) {
            if (GridBody.of(relay.pos().dimension()) == key) relays++;
        }
    }

    private void readNode(GridNode node, @Nullable GridSampler sampler) {
        stored = node.storageDouble();
        capacity = node.capacityDouble();
        lossPercent = node.loss / (double) Loss.PERCENT;
        var stats = node.stats();
        int second = GridClock.second();
        input = NodeMeter.windows(stats, NodeMeter.IN, second);
        output = NodeMeter.windows(stats, NodeMeter.OUT, second);
        loss = NodeMeter.windows(stats, NodeMeter.LOSS, second);
        if (sampler == null) return;
        int sampled = sampler.lastTick();
        if (node.sampledTick == sampled) storageDelta = node.sampledDelta;
        for (var arc : node.out) {
            if (arc.sampledTick == sampled) lineOut += arc.sampledFlow;
            var back = arc.reverse;
            if (back != null && back.sampledTick == sampled) lineIn += back.sampledFlow;
        }
    }

    private void collectPorts(EnergyAccount account, ResourceKey<Level> key) {
        var server = WirelessGrid.server();
        if (server == null) return;
        int kept = 0;
        for (var level : server.getAllLevels()) {
            if (GridBody.of(level.dimension()) != key) continue;
            var list = PortIndex.ports(level, account);
            for (int i = 0, n = list.size(); i < n; i++) {
                var port = list.get(i);
                var machine = port.machine();
                if (machine == null || machine.isRemoved()) continue;
                portCount++;
                kept = offer(kept, port, level, port.inRate() - port.outRate());
            }
        }
        buildPorts(kept);
    }

    private static int offer(int kept, EnergyPort port, ServerLevel level, double rate) {
        double weight = Math.abs(rate);
        if (kept == MAX_PORTS && weight <= Math.abs(TOP_RATES[MAX_PORTS - 1])) return kept;
        int i = Math.min(kept, MAX_PORTS - 1);
        while (i > 0 && Math.abs(TOP_RATES[i - 1]) < weight) {
            TOP_PORTS[i] = TOP_PORTS[i - 1];
            TOP_LEVELS[i] = TOP_LEVELS[i - 1];
            TOP_RATES[i] = TOP_RATES[i - 1];
            i--;
        }
        TOP_PORTS[i] = port;
        TOP_LEVELS[i] = level;
        TOP_RATES[i] = rate;
        return Math.min(kept + 1, MAX_PORTS);
    }

    private void buildPorts(int kept) {
        if (kept == 0) return;
        ports = new Port[kept];
        var list = new IntArrayList(kept);
        for (int i = 0; i < kept; i++) {
            var machine = TOP_PORTS[i].machine();
            var dimension = TOP_LEVELS[i].dimension();
            var pos = machine.getPos();
            int key = (int) HashCommon.mix(pos.asLong() * 31 + dimension.location().hashCode());
            while (list.contains(key)) key++;
            ports[i] = new Port(key, machine.getBlockState().getBlock().getName(), dimension, pos, TOP_RATES[i]);
            list.add(key);
            TOP_PORTS[i] = null;
            TOP_LEVELS[i] = null;
        }
        keys = IntLists.unmodifiable(list);
    }

    public int revision() {
        return revision;
    }

    public int towers() {
        return towers;
    }

    public int topTowerTier() {
        return topTowerTier;
    }

    public int relays() {
        return relays;
    }

    public double stored() {
        return stored;
    }

    public double capacity() {
        return capacity;
    }

    public double storageDelta() {
        return storageDelta;
    }

    public double lossPercent() {
        return lossPercent;
    }

    public int portCount() {
        return portCount;
    }

    public double input(int window) {
        return input[GridView.clampWindow(window)];
    }

    public double output(int window) {
        return output[GridView.clampWindow(window)];
    }

    public double loss(int window) {
        return loss[GridView.clampWindow(window)];
    }

    public double lineIn() {
        return lineIn;
    }

    public double lineOut() {
        return lineOut;
    }

    public IntList portKeys() {
        return keys;
    }

    @Nullable
    public Port port(int key) {
        for (var port : ports) {
            if (port.key == key) return port;
        }
        return null;
    }
}
