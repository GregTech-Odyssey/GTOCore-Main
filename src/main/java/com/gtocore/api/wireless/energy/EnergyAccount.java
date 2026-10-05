package com.gtocore.api.wireless.energy;

import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/**
 * 一个队伍的电网：登记的能源塔与中继、由它们派生的维度节点与线路，以及速率、绑定与流量统计。
 * 存取、结算与拓扑重建分别在 GridTransfer、Settlement、GridTopology；total* 汇总只供展示，不用于任何判定。
 */
public final class EnergyAccount {

    static final EnergyAccount NONE = new EnergyAccount(new UUID(0, 0));

    public final UUID team;

    long rate;
    @Nullable
    GlobalPos bindPos;
    int version;
    boolean removed;
    boolean dirty;
    long pendingHi, pendingLo;
    int supplyGen, sinkGen;
    private int visitStamp;

    final FlowMeter meter = new FlowMeter(this);
    final O2OOpenCacheHashMap<GlobalPos, Provider.Tower> towers = new O2OOpenCacheHashMap<>();
    final O2OOpenCacheHashMap<GlobalPos, Provider.Relay> relays = new O2OOpenCacheHashMap<>();
    final O2OOpenCacheHashMap<GlobalPos, BigInteger> towerCapacities = new O2OOpenCacheHashMap<>();
    final Reference2ObjectOpenHashMap<ResourceKey<Level>, GridNode> nodes = new Reference2ObjectOpenHashMap<>();
    final ObjectArrayList<GridNode> nodeList = new ObjectArrayList<>();
    final ObjectArrayList<GridNode> touchedNodes = new ObjectArrayList<>();
    final ObjectArrayList<Arc> arcs = new ObjectArrayList<>();
    final Object2ObjectOpenHashMap<Arc.Key, Arc> retiredArcs = new Object2ObjectOpenHashMap<>();
    @Nullable
    GridTopology.Shape shape;
    @Nullable
    RouteJob routeJob;

    EnergyAccount(UUID team) {
        this.team = team;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public long rate() {
        return rate;
    }

    @Nullable
    public GlobalPos bindPos() {
        return bindPos;
    }

    @Nullable
    public EnergyStats stats() {
        return meter.stats();
    }

    public List<GridNode> nodes() {
        return ObjectLists.unmodifiable(nodeList);
    }

    public GridNode node(@Nullable ResourceKey<Level> dimension) {
        var node = dimension == null ? null : nodes.get(GridBody.of(dimension));
        return node == null ? GridNode.EMPTY : node;
    }

    GridNode nodeOrCreate(ResourceKey<Level> key) {
        var dimension = GridBody.of(key);
        var node = nodes.get(dimension);
        if (node == null) {
            node = new GridNode(dimension);
            nodes.put(dimension, node);
            nodeList.add(node);
        }
        return node;
    }

    void rollNodes() {
        for (int i = 0, n = touchedNodes.size(); i < n; i++) {
            var meter = touchedNodes.get(i).meter;
            if (meter != null) meter.roll();
        }
        touchedNodes.clear();
    }

    int nextVisit() {
        return ++visitStamp;
    }

    void supplyArrived(GridNode node) {
        supplyGen++;
        node.wakeDependents();
    }

    void sinkFreed(GridNode node) {
        sinkGen++;
        node.wakeDependents();
    }

    void roomFreed(GridNode node) {
        if (!node.awaitingRoom) return;
        node.awaitingRoom = false;
        sinkFreed(node);
    }

    void wakeAll() {
        for (int i = 0, n = nodeList.size(); i < n; i++) nodeList.get(i).wakeParked();
    }

    void addPending(long hi, long lo) {
        long s = pendingLo + lo;
        pendingHi = U126.saturatedAdd(U126.saturatedAdd(pendingHi, Math.max(0, hi)), s >>> 63);
        pendingLo = s & U126.MASK;
    }

    void returnToNode(GridNode node, long credit, long gross, int extraLoss) {
        if (credit <= 0 && gross <= 0) return;
        if (node.hasCapacity()) {
            int stocked = node.stocked;
            if (credit > 0) node.absorb(0, credit, null);
            if (gross > 0) node.store(gross, extraLoss);
            GridTransfer.arrived(this, node, stocked);
        } else {
            addPending(0, U126.saturatedAdd(Math.max(0, credit), gross));
            markDirty();
        }
    }

    void markDirty() {
        if (dirty) return;
        dirty = true;
        WirelessGrid.markDirty(this);
    }

    void rebuild() {
        rebuild(false);
    }

    void rebuild(boolean async) {
        dirty = false;
        GridTopology.rebuild(this, async);
        wakeAll();
        supplyGen++;
        sinkGen++;
    }

    void invalidatePorts() {
        version++;
    }

    public BigInteger totalStorage() {
        long h = Math.max(0, pendingHi), l = pendingLo;
        for (int i = 0, n = nodeList.size(); i < n; i++) {
            var node = nodeList.get(i);
            long s = l + node.lo;
            h = U126.saturatedAdd(U126.saturatedAdd(h, Math.max(0, node.hi)), s >>> 63);
            l = s & U126.MASK;
        }
        return U126.toBig(h, l);
    }

    public BigInteger towerCapacity(GlobalPos pos) {
        var capacity = towerCapacities.get(pos);
        return capacity == null ? BigInteger.ZERO : capacity;
    }

    public BigInteger totalCapacity() {
        long h = 0, l = 0;
        for (int i = 0, n = nodeList.size(); i < n; i++) {
            var node = nodeList.get(i);
            long s = l + node.capLo;
            h = U126.saturatedAdd(U126.saturatedAdd(h, node.capHi), s >>> 63);
            l = s & U126.MASK;
        }
        return U126.toBig(h, l);
    }

    public double totalStorageDouble() {
        double sum = U126.toDouble(Math.max(0, pendingHi), pendingLo);
        for (int i = 0, n = nodeList.size(); i < n; i++) sum += nodeList.get(i).storageDouble();
        return sum;
    }

    public double totalCapacityDouble() {
        double sum = 0;
        for (int i = 0, n = nodeList.size(); i < n; i++) sum += nodeList.get(i).capacityDouble();
        return sum;
    }

    public double averageLossPercent() {
        return averageLoss() / (double) Loss.PERCENT;
    }

    public int averageLoss() {
        double weighted = 0, total = 0;
        for (int i = 0, n = nodeList.size(); i < n; i++) {
            var node = nodeList.get(i);
            weighted += node.lossWeight();
            total += node.capacityDouble();
        }
        return total > 0 ? (int) Math.round(weighted / total) : 0;
    }
}
