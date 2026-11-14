package com.gtocore.common.wireless.energy.map;

import com.gtolib.api.scope.GTOScopes;
import com.gtolib.api.wireless.energy.EnergyAccount;
import com.gtolib.api.wireless.energy.GridClock;
import com.gtolib.api.wireless.energy.GridSampler;
import com.gtolib.api.wireless.energy.GridView;
import com.gtolib.api.wireless.energy.WirelessGrid;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.ButtonGroup;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.suried.gtoutils.schedule.Cadence;
import com.suried.gtoutils.schedule.OwnedScope;
import com.suried.gtoutils.schedule.Poll;
import com.suried.gtoutils.schedule.Scope;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 队伍电网汇总（存量、时间窗流量、节点与线路数）；星图 HUD、无线电网监视器与变电站主页共用。
 * 每个面板在服务端持有一份共享采样器句柄（按 20 tick 重新解析所有者的账户），界面关闭时释放。
 * 时间窗默认由面板自己保存（界面重建即回到默认）；机器托管的界面传入机器上持久化的时间窗，重开界面后沿用。
 */
public class GridSummaryPanel extends UIElement {

    private static final Component EMPTY_HINT = GridMapLang.EMPTY_TITLE;
    private static final float LOW_STORAGE = 0.1f;

    private final Supplier<UUID> owner;
    private final IntSupplier windowState;
    private final IntConsumer windowSelect;
    private final UIElement header;
    private final UIElement body;
    private final UIElement details;
    private final StatusPanel empty;
    @Nullable
    private GridMapToggles toggles;
    private boolean emptyHidden;
    private int localWindow;
    @Nullable
    private OwnedScope scope;
    private boolean opened;
    @Nullable
    private GridSampler sampler;
    private Component teamName = GridFormat.NO_VALUE;
    @Nullable
    private GridView.Summary shown;
    private int shownWindow = -1;
    private ProgressBar.Progress progress = ProgressBar.Progress.EMPTY;
    private Component storedDetail = GridFormat.NO_VALUE, net = GridFormat.NO_VALUE, input = GridFormat.NO_VALUE, output = GridFormat.NO_VALUE,
            loss = GridFormat.NO_VALUE, eta = GridFormat.NO_VALUE, scale = GridFormat.NO_VALUE, coverage = GridFormat.NO_VALUE;

    protected GridSummaryPanel(int width, Supplier<UUID> owner) {
        this(width, owner, null, null);
    }

    /**
     * @param window 时间窗（服务端读取）；为空时由面板自己保存，界面重建即回到默认
     * @param select 选中时间窗（服务端写入）；为空时写面板自己的字段
     */
    protected GridSummaryPanel(int width, Supplier<UUID> owner, @Nullable IntSupplier window, @Nullable IntConsumer select) {
        this.owner = owner;
        windowState = window != null ? window : () -> localWindow;
        windowSelect = select != null ? select : index -> localWindow = GridView.clampWindow(index);
        layout(l -> l.column().width(width).gapAll(UISizes.GAP));
        listenUIClose();
        var name = TextLine.of(0, this::teamName).bindClientColor(UITheme::panelText);
        header = UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChild(name.layout(l -> l.flex(1)));
        var bar = ProgressBar.of(LayoutStyle.AUTO, GridMapLang.STORED, 0, this::progress).percent();
        bar.bindClientColor(() -> barColor(bar));
        bar.bindDetail(this::storedDetail);
        details = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(windowGroup(windowState, windowSelect), status());
        empty = new StatusPanel(LayoutStyle.AUTO);
        empty.addChild(Label.of(width - 2 * UISizes.PANEL_PADDING, () -> EMPTY_HINT).bindClientColor(() -> UITheme.STATUS_TEXT_WARNING));
        body = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(details, empty);
        addChildren(header, bar, body);
        addSyncValue(SyncValue.ofBool(this::hasGrid, false).onChanged(this::showGrid));
        showGrid(false);
    }

    private StatusPanel status() {
        var status = new StatusPanel(LayoutStyle.AUTO);
        status.addLine(GridMapLang.NET, this::net);
        status.addLine(GridMapLang.NODE_INPUT, this::input);
        status.addLine(GridMapLang.NODE_OUTPUT, this::output);
        status.addLine(GridMapLang.LOSS, this::loss);
        status.addLine(GridMapLang.ETA, this::eta);
        status.addSentence(this::scale);
        var coverageLine = status.addLine(GridMapLang.COVERAGE, this::coverage).bindLevel(() -> Level.WARNING);
        status.addSyncValue(SyncValue.ofBool(this::isTruncated, false).onChanged(coverageLine::setDisplay));
        coverageLine.setDisplay(false);
        return status;
    }

    public static GridSummaryPanel of(int width, Supplier<UUID> owner) {
        return new GridSummaryPanel(width, owner);
    }

    /**
     * 时间窗选择由调用方持有（如机器保存的设置），关闭界面后重开仍按调用方的值显示。
     */
    public static GridSummaryPanel of(int width, Supplier<UUID> owner, IntSupplier window, IntConsumer select) {
        return new GridSummaryPanel(width, owner, window, select);
    }

    public GridSummaryPanel setCollapsible(GridMapToggles toggles) {
        this.toggles = toggles;
        var collapse = Button.of(UISizes.ICON_BUTTON_SIZE).bindClientText(() -> toggles.isHudCollapsed() ? "+" : "-")
                .setOnClientClick(() -> toggles.setHudCollapsed(!toggles.isHudCollapsed()));
        collapse.tooltips(GridMapLang.COLLAPSE);
        header.addChild(collapse);
        return this;
    }

    public GridSummaryPanel hideEmptyHint() {
        emptyHidden = true;
        empty.setDisplay(false);
        return this;
    }

    public GridSummaryPanel addSection(Widget section) {
        details.addChild(section);
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        if (toggles != null && body.isDisplayed() == toggles.isHudCollapsed()) body.setDisplay(!toggles.isHudCollapsed());
        super.updateScreen();
    }

    static ButtonGroup windowGroup(IntSupplier current, IntConsumer select) {
        return ButtonGroup.single(GridView.WINDOWS, i -> GridFormat.WINDOW_KEYS[i], current, select)
                .optionTooltips(i -> Collections.singletonList(GridFormat.WINDOW_TIPS[i])).horizontal().compact();
    }

    static int barColor(ProgressBar bar) {
        var value = bar.getProgress();
        return value.total() > 0 && value.ratio() < LOW_STORAGE ? UITheme.STATUS_OFFLINE : UITheme.barEnergy();
    }

    private void showGrid(boolean hasGrid) {
        details.setDisplay(hasGrid);
        empty.setDisplay(!hasGrid && !emptyHidden);
    }

    @Override
    public void detectAndSendChanges() {
        if (!isRemote()) tickServer();
        super.detectAndSendChanges();
    }

    @Override
    protected void onUIClosed() {
        if (isRemote()) return;
        opened = true;
        var held = scope;
        scope = null;
        if (held != null) held.dispose();
    }

    private void tickServer() {
        if (!opened) {
            opened = true;
            var host = gui != null && gui.entityPlayer instanceof ServerPlayer serverPlayer ? GTOScopes.PLAYER.get(serverPlayer) : null;
            if (host != null) {
                var child = host.child("grid_summary");
                scope = child;
                mount(child);
            }
        }
        if (sampler != null) sampler.sample(GridClock.tick());
    }

    private void mount(Scope scope) {
        scope.effect(this::account, this::borrow, this::giveBack);
        refreshName();
        scope.repeat(Cadence.every(GridSampler.INTERVAL), this::refreshName);
    }

    @Nullable
    private EnergyAccount account() {
        Poll.every(GridSampler.INTERVAL);
        var account = WirelessGrid.trackedAccount(owner.get());
        return account.isNone() ? null : account;
    }

    private void borrow(EnergyAccount account) {
        sampler = GridSampler.acquire(account);
    }

    private void giveBack(EnergyAccount account) {
        var held = sampler;
        sampler = null;
        if (held != null) held.release();
    }

    private void refreshName() {
        var uuid = owner.get();
        teamName = uuid == null || gui == null ? GridFormat.NO_VALUE : TeamUtil.getName(gui.entityPlayer.level(), uuid);
    }

    private void refresh() {
        int window = windowState.getAsInt();
        var summary = sampler == null ? GridView.Summary.EMPTY : sampler.summary();
        if (summary == shown && window == shownWindow) return;
        shown = summary;
        shownWindow = window;
        double stored = summary.stored(), capacity = summary.capacity();
        progress = new ProgressBar.Progress(GridFormat.ppm(stored, capacity), 1_000_000L, 0);
        storedDetail = GridFormat.storedDetail(stored, capacity);
        double netRate = summary.net(window);
        int tier = sampler == null ? -1 : sampler.account().unitTier(null);
        net = GridFormat.netRate(netRate, tier);
        input = GridFormat.rate(summary.input(window), tier);
        output = GridFormat.rate(summary.output(window), tier);
        loss = GridFormat.rate(summary.loss(window), tier);
        eta = capacity > 0 ? GridFormat.eta(stored, capacity, summary.storageDelta()) : GridFormat.NO_VALUE;
        scale = Component.translatable(GridMapLang.SCALE_VALUE, summary.nodes(), summary.lines(), summary.activeLines());
        coverage = summary.truncated() ? Component.translatable(GridMapLang.TRUNCATED, GridView.MAX_NODES, GridView.MAX_LINES) : GridFormat.NO_VALUE;
    }

    private boolean hasGrid() {
        return sampler != null && !sampler.summary().isEmpty();
    }

    private Component teamName() {
        return teamName;
    }

    private ProgressBar.Progress progress() {
        refresh();
        return progress;
    }

    private Component storedDetail() {
        refresh();
        return storedDetail;
    }

    private Component net() {
        refresh();
        return net;
    }

    private Component input() {
        refresh();
        return input;
    }

    private Component output() {
        refresh();
        return output;
    }

    private Component loss() {
        refresh();
        return loss;
    }

    private Component eta() {
        refresh();
        return eta;
    }

    private Component scale() {
        refresh();
        return scale;
    }

    private Component coverage() {
        refresh();
        return coverage;
    }

    private boolean isTruncated() {
        return sampler != null && sampler.summary().truncated();
    }
}
