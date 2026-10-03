package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.GridClock;
import com.gtocore.api.wireless.energy.GridSampler;
import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.api.wireless.energy.WirelessGrid;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * 队伍电网汇总（存量、时间窗流量、节点与线路数）；星图 HUD、无线电网监视器与变电站主页共用。
 * 每个面板在服务端持有一份共享采样器句柄（按 20 tick 重新解析所有者的账户），界面关闭时释放。
 */
public class GridSummaryPanel extends UIElement {

    private static final Component EMPTY_HINT = Component.translatable(GridMapLang.EMPTY_TITLE);
    private static final float LOW_STORAGE = 0.1f;

    private final Supplier<UUID> owner;
    private final UIElement header;
    private final UIElement body;
    private final UIElement details;
    private final StatusPanel empty;
    @Nullable
    private GridMapToggles toggles;
    private boolean emptyHidden;
    private int window;
    private boolean resolved;
    private int resolvedAt;
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
        this.owner = owner;
        layout(l -> l.column().width(width).gapAll(UISizes.GAP));
        listenUIClose();
        var name = TextLine.of(0, this::teamName).bindClientColor(UITheme::panelText);
        header = UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChild(name.layout(l -> l.flex(1)));
        var bar = ProgressBar.of(LayoutStyle.AUTO, Component.translatable(GridMapLang.STORED), 0, this::progress).percent();
        bar.bindClientColor(() -> barColor(bar));
        bar.bindDetail(this::storedDetail);
        details = UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP))
                .addChildren(windowGroup(() -> window, index -> window = GridView.clampWindow(index)), status());
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
        return ButtonGroup.single(GridView.WINDOWS, i -> Component.translatable(GridFormat.WINDOW_KEYS[i]), current, select)
                .optionTooltips(i -> Collections.singletonList(Component.translatable(GridFormat.WINDOW_TIPS[i]))).horizontal().compact();
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
        if (!isRemote()) release();
    }

    private void tickServer() {
        int now = GridClock.tick();
        if (!resolved || now < resolvedAt || now - resolvedAt >= GridSampler.INTERVAL) {
            resolved = true;
            resolvedAt = now;
            resolve();
        }
        if (sampler != null) sampler.sample(now);
    }

    private void resolve() {
        var uuid = owner.get();
        var account = WirelessGrid.accountIfPresent(uuid);
        if (sampler == null || sampler.account() != account) {
            release();
            sampler = account.isNone() ? null : GridSampler.acquire(account);
        }
        teamName = uuid == null || gui == null ? GridFormat.NO_VALUE : TeamUtil.getName(gui.entityPlayer.level(), uuid);
    }

    private void release() {
        if (sampler == null) return;
        sampler.release();
        sampler = null;
    }

    private void refresh() {
        var summary = sampler == null ? GridView.Summary.EMPTY : sampler.summary();
        if (summary == shown && window == shownWindow) return;
        shown = summary;
        shownWindow = window;
        double stored = summary.stored(), capacity = summary.capacity();
        progress = new ProgressBar.Progress(GridFormat.ppm(stored, capacity), 1_000_000L, 0);
        storedDetail = GridFormat.storedDetail(stored, capacity);
        double netRate = summary.net(window);
        net = GridFormat.netRate(netRate);
        input = GridFormat.rate(summary.input(window));
        output = GridFormat.rate(summary.output(window));
        loss = GridFormat.rate(summary.loss(window));
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
