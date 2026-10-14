package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.GridClock;
import com.gtocore.api.wireless.energy.GridSampler;
import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.api.wireless.energy.NodeDetail;
import com.gtocore.api.wireless.energy.WirelessGrid;
import com.gtocore.common.machine.multiblock.storage.WirelessDimensionRepeaterMachine;

import com.gregtechceu.gtceu.api.gui.factory.MachineSubWindowFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * 每次打开电网星图建一份的界面上下文：服务端持有账户、共享采样器、时间窗与卡片详情缓存；客户端持有导航回调与卡片悬停线路。
 */
public final class GridMapContext {

    private final Player player;
    @Nullable
    private final MetaMachine machine;
    private final int focusDimRef;
    private final GridMapMode mode;
    private int window;
    private boolean resolved;
    private int resolvedAt;
    @Nullable
    private GridSampler sampler;
    @Nullable
    private ResourceKey<Level> detailDimension;
    private NodeDetail detail = NodeDetail.EMPTY;
    private int detailAt;
    @Nullable
    private GridMapNavigator navigator;
    private int hoveredLine = -1, pendingHoveredLine = -1;

    public GridMapContext(Player player, @Nullable MetaMachine machine, int focusDimRef, GridMapMode mode) {
        this.player = player;
        this.machine = machine;
        this.focusDimRef = focusDimRef;
        this.mode = mode;
    }

    public Player player() {
        return player;
    }

    @Nullable
    public MetaMachine machine() {
        return machine;
    }

    public int focusDimRef() {
        return focusDimRef;
    }

    public GridMapMode mode() {
        return mode;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = GridView.clampWindow(window);
    }

    public void tick() {
        int now = GridClock.tick();
        if (!resolved || now < resolvedAt || now - resolvedAt >= GridSampler.INTERVAL) {
            resolved = true;
            resolvedAt = now;
            refreshSampler();
        }
        if (sampler != null) sampler.sample(now);
    }

    private void refreshSampler() {
        var account = WirelessGrid.accountIfPresent(player.getUUID());
        if (sampler != null && sampler.account() == account) return;
        close();
        sampler = account.isNone() ? null : GridSampler.acquire(account);
    }

    public void close() {
        detail = NodeDetail.EMPTY;
        detailDimension = null;
        if (sampler == null) return;
        sampler.release();
        sampler = null;
    }

    public GridView.TopologyView topology() {
        return sampler == null ? GridView.TopologyView.EMPTY : sampler.topology();
    }

    public GridView.LiveView live() {
        return sampler == null ? GridView.LiveView.EMPTY : sampler.live();
    }

    public GridView.Summary summary() {
        return sampler == null ? GridView.Summary.EMPTY : sampler.summary();
    }

    public NodeDetail detail(@Nullable ResourceKey<Level> dimension) {
        if (dimension == null || sampler == null) return NodeDetail.EMPTY;
        int now = GridClock.tick();
        if (dimension != detailDimension || now != detailAt) {
            detailDimension = dimension;
            detailAt = now;
            detail = sampler.detail(dimension);
        }
        return detail;
    }

    public boolean isInPlayerDimension(ResourceKey<Level> dimension) {
        return player.level().dimension() == dimension;
    }

    void handlePick(Player sender, int dimRef) {
        if (mode != GridMapMode.PICK || sender != player || !(sender instanceof ServerPlayer serverPlayer)) return;
        if (!(machine instanceof WirelessDimensionRepeaterMachine repeater) || repeater.isRemoved()) return;
        var target = GridView.dimension(dimRef);
        if (target != null && repeater.pickTarget(serverPlayer, target)) {
            MachineSubWindowFactory.openMachine(serverPlayer, repeater);
            return;
        }
        serverPlayer.displayClientMessage(Component.translatable(GridMapLang.PICK_REJECTED), true);
    }

    void handleBack(Player sender) {
        if (sender != player || machine == null || machine.isRemoved() || !(sender instanceof ServerPlayer serverPlayer)) return;
        MachineSubWindowFactory.openMachine(serverPlayer, machine);
    }

    @Nullable
    GridMapNavigator navigator() {
        return navigator;
    }

    void setNavigator(@Nullable GridMapNavigator navigator) {
        this.navigator = navigator;
    }

    int hoveredLine() {
        return hoveredLine;
    }

    void hoverLine(int line) {
        pendingHoveredLine = line;
    }

    void beginFrame() {
        hoveredLine = pendingHoveredLine;
        pendingHoveredLine = -1;
    }
}
