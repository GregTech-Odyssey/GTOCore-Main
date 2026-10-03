package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.api.wireless.energy.NodeDetail;

import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;

/**
 * 一张星球卡片的服务端取值缓存：按拓扑实例、详情实例与时间窗重建文字，同步值的 getter 每 tick 只做引用比较。
 */
final class GridCardData {

    final GridMapContext ctx;
    private final int argumentNode;
    private final int argumentDimRef;
    private final GridCardLines lines = new GridCardLines();
    private boolean resolved;
    @Nullable
    private ResourceKey<net.minecraft.world.level.Level> dimension;
    private Component title = GridFormat.NO_VALUE, location = GridFormat.NO_VALUE;
    @Nullable
    private GridView.TopologyView shownTopology;
    private int nodeIndex = -1;
    @Nullable
    private GridView.NodeInfo info;
    private Component state = GridFormat.NO_VALUE, tier = GridFormat.NO_VALUE, storageNote = GridFormat.NO_VALUE;
    private Level stateLevel = Level.NORMAL;
    @Nullable
    private NodeDetail shownDetail;
    private int shownWindow = -1;
    private ProgressBar.Progress progress = ProgressBar.Progress.EMPTY;
    private Component storedDetail = GridFormat.NO_VALUE, towers = GridFormat.NO_VALUE, loss = GridFormat.NO_VALUE,
            ports = GridFormat.NO_VALUE, delta = GridFormat.NO_VALUE, eta = GridFormat.NO_VALUE, input = GridFormat.NO_VALUE,
            output = GridFormat.NO_VALUE, lossRate = GridFormat.NO_VALUE, relayIn = GridFormat.NO_VALUE, relayOut = GridFormat.NO_VALUE;

    GridCardData(GridMapContext ctx, int argumentNode, int argumentDimRef) {
        this.ctx = ctx;
        this.argumentNode = argumentNode;
        this.argumentDimRef = argumentDimRef;
    }

    private void resolve() {
        if (resolved) return;
        resolved = true;
        if (argumentDimRef > 0) {
            dimension = GridView.dimension(argumentDimRef);
        } else {
            var nodes = ctx.topology().nodes();
            dimension = argumentNode >= 0 && argumentNode < nodes.size() ? nodes.get(argumentNode).dimension() : null;
        }
        title = GridFormat.bodyName(dimension);
        location = GridFormat.location(dimension);
    }

    GridView.TopologyView topology() {
        resolve();
        var topology = ctx.topology();
        if (topology != shownTopology) {
            shownTopology = topology;
            rebuildTopology(topology);
        }
        lines.refresh(topology, ctx.live(), nodeIndex);
        return topology;
    }

    GridView.LiveView live() {
        return ctx.live();
    }

    int nodeIndex() {
        topology();
        return nodeIndex;
    }

    private void rebuildTopology(GridView.TopologyView topology) {
        nodeIndex = topology.indexOf(dimension);
        info = nodeIndex < 0 ? null : topology.nodes().get(nodeIndex);
        if (info == null) {
            state = Component.translatable(GridMapLang.STATE_OUTSIDE);
            stateLevel = Level.NORMAL;
            tier = GridFormat.NO_VALUE;
            storageNote = Component.translatable(GridMapLang.NO_STORAGE);
        } else if (info.towers() > 0) {
            state = Component.translatable(GridMapLang.STATE_NODE, GridFormat.tierPlain(info.tier()));
            stateLevel = Level.GOOD;
            tier = Component.literal(GridFormat.tierPlain(info.tier()));
            storageNote = GridFormat.NO_VALUE;
        } else {
            state = Component.translatable(GridMapLang.STATE_LINE_ONLY, GridFormat.tierPlain(info.reachTier()));
            stateLevel = Level.WARNING;
            tier = GridFormat.NO_VALUE;
            storageNote = Component.translatable(GridMapLang.RELAY_ONLY);
        }
    }

    NodeDetail detail() {
        resolve();
        var detail = ctx.detail(dimension);
        int window = ctx.getWindow();
        if (detail != shownDetail || window != shownWindow) {
            shownDetail = detail;
            shownWindow = window;
            rebuildDetail(detail, window);
        }
        return detail;
    }

    private void rebuildDetail(NodeDetail detail, int window) {
        double stored = detail.stored(), capacity = detail.capacity();
        progress = new ProgressBar.Progress(GridFormat.ppm(stored, capacity), 1_000_000L, 0);
        storedDetail = GridFormat.storedDetail(stored, capacity);
        towers = detail.towers() > 0 ? Component.translatable(GridMapLang.TOWERS_VALUE, detail.towers(), GridFormat.tierPlain(detail.topTowerTier())) :
                Component.translatable(GridMapLang.NONE);
        loss = Component.translatable(GridMapLang.PERCENT, GridFormat.amount(detail.lossPercent()));
        ports = detail.portCount() > 0 ? Component.translatable(GridMapLang.PORTS_VALUE, detail.portCount()) : Component.translatable(GridMapLang.NONE);
        delta = GridFormat.netRate(detail.storageDelta());
        eta = capacity > 0 ? GridFormat.eta(stored, capacity, detail.storageDelta()) : GridFormat.NO_VALUE;
        input = GridFormat.rate(detail.input(window));
        output = GridFormat.rate(detail.output(window));
        lossRate = GridFormat.rate(detail.loss(window));
        relayIn = GridFormat.rate(detail.lineIn());
        relayOut = GridFormat.rate(detail.lineOut());
    }

    boolean isPortHere(int key) {
        var port = detail().port(key);
        return port != null && ctx.isInPlayerDimension(port.dimension());
    }

    boolean hasStorage() {
        topology();
        return info != null && info.towers() > 0;
    }

    boolean hasPorts() {
        return detail().portCount() > 0;
    }

    Component title() {
        resolve();
        return title;
    }

    Component location() {
        resolve();
        return location;
    }

    IntArrayList lineKeys() {
        topology();
        return lines.keys();
    }

    int lineVersion() {
        topology();
        return lines.version();
    }

    Component lineTitle() {
        topology();
        return lines.title();
    }

    Component state() {
        topology();
        return state;
    }

    Level stateLevel() {
        topology();
        return stateLevel;
    }

    Component tier() {
        topology();
        return tier;
    }

    Component storageNote() {
        topology();
        return storageNote;
    }

    ProgressBar.Progress progress() {
        detail();
        return progress;
    }

    Component storedDetail() {
        detail();
        return storedDetail;
    }

    Component towers() {
        detail();
        return towers;
    }

    Component loss() {
        detail();
        return loss;
    }

    Component ports() {
        detail();
        return ports;
    }

    Component delta() {
        detail();
        return delta;
    }

    Component eta() {
        detail();
        return eta;
    }

    Component input() {
        detail();
        return input;
    }

    Component output() {
        detail();
        return output;
    }

    Component lossRate() {
        detail();
        return lossRate;
    }

    Component relayIn() {
        detail();
        return relayIn;
    }

    Component relayOut() {
        detail();
        return relayOut;
    }
}
