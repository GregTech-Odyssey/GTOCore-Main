package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.grid.GridMapClient;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Dock;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UIStyleManager;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.CardHost;
import com.gregtechceu.gtceu.uipro.window.Popup;
import com.gregtechceu.gtceu.uipro.window.ScreenHost;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.vfyjxf.taffy.style.TaffyPosition;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * 电网星图根元素：全屏画布、左上汇总、右上星球卡片、底部悬浮栏、左下图例与选目标提示条；拓扑与实时数据的同步、选目标与返回请求。
 */
public final class GridMapRoot extends ScreenHost implements GridMapView {

    static final int HUD_WIDTH = UISizes.POPUP_CONTENT_WIDTH;
    private static final int EMPTY_WIDTH = HUD_WIDTH, STEP_NUMBER_WIDTH = 12, LEGEND_GAP = 8;
    private static final String[] EMPTY_STEPS = { GridMapLang.EMPTY_STEP_1, GridMapLang.EMPTY_STEP_2, GridMapLang.EMPTY_STEP_3 };

    private final GridMapContext ctx;
    private final GridMapToggles toggles = new GridMapToggles();
    private final SyncValue<GridView.TopologyView> topology;
    private final SyncValue<GridView.LiveView> live;
    private final RPC<Integer> pick;
    private final RPC<Unit> back;
    private final CanvasView canvas;
    private final CardHost card;
    private final Dock dock;
    private final Dock hud;
    private final Dock legend;
    private final Dock emptyCard;
    private final GridRankPanel rank;
    private int legendTop = -1, emptyTop = -1, rankHeight;

    GridMapRoot(GridMapContext ctx) {
        this.ctx = ctx;
        setStyleOverride(UIStyleManager.STARFIELD_ID, UIStyleManager.STARFIELD_ID);
        layout(l -> l.column().alignCenter().paddingTop(UISizes.DOCK_MARGIN));
        topology = addSyncValue(SyncValue.of(ctx::topology, GridView.TOPOLOGY, GridView.TopologyView.EMPTY));
        live = addSyncValue(SyncValue.of(ctx::live, GridView.LIVE, GridView.LiveView.EMPTY));
        pick = addRPC(ByteBufCodecs.VAR_INT, ctx::handlePick).validate(ref -> GridView.dimension(ref) != null).limit(1);
        back = addRPC(ctx::handleBack).limit(1);
        card = new CardHost("gridmap.card", this::createCard);
        canvas = new CanvasView("gridmap", SERVER_WIDTH, SERVER_HEIGHT);
        canvas.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(0).top(0));
        canvas.setFrame(false, 0);
        canvas.setResizable(false);
        canvas.setRememberView(false);
        canvas.setGrid(null);
        canvas.addFloatingCard(card);
        dock = GridMapDock.build(this);
        canvas.addOverlay(dock);
        rank = new GridRankPanel(ctx);
        hud = Dock.vertical().addGroup(GridSummaryPanel.of(HUD_WIDTH, ctx.player()::getUUID).setCollapsible(toggles).hideEmptyHint().addSection(rank));
        hud.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(UISizes.DOCK_MARGIN).top(UISizes.DOCK_MARGIN));
        canvas.setObstructedLeft(() -> hud.isVisible() && hud.getSizeWidth() > 0 ? hud.getSizeWidth() + 2 * UISizes.DOCK_MARGIN - 2 : 0);
        legend = Dock.vertical().addGroup(new GridLegendPanel());
        legend.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(UISizes.DOCK_MARGIN).top(UISizes.DOCK_MARGIN));
        legend.setDisplay(toggles.isLegend());
        emptyCard = emptyCard();
        addChildren(canvas, hud, legend, emptyCard, pickBar());
        addSyncValue(SyncValue.ofBool(() -> !ctx.summary().isEmpty(), true).onChanged(hasGrid -> emptyCard.setDisplay(!hasGrid)));
    }

    private Dock emptyCard() {
        var column = UIElement.column(EMPTY_WIDTH).layout(l -> l.gapAll(UISizes.GAP))
                .addChild(TextLine.translatable(LayoutStyle.AUTO, GridMapLang.EMPTY_TITLE).bindClientColor(() -> UITheme.STATUS_TEXT_WARNING));
        for (int i = 0; i < EMPTY_STEPS.length; i++) {
            var number = TextLine.constant(STEP_NUMBER_WIDTH, Component.literal((i + 1) + ".")).bindClientColor(UITheme::panelText);
            number.layout(l -> l.marginTop((Label.LINE_HEIGHT - TextLine.HEIGHT) / 2f));
            var body = Label.translatable(EMPTY_WIDTH - STEP_NUMBER_WIDTH - UISizes.GAP, EMPTY_STEPS[i]).bindClientColor(UITheme::panelText);
            column.addChild(UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP).alignStart()).addChildren(number, body));
        }
        var card = Dock.vertical().addGroup(column);
        card.layout(l -> l.positionType(TaffyPosition.ABSOLUTE).left(UISizes.DOCK_MARGIN).top(UISizes.DOCK_MARGIN));
        card.setDisplay(false);
        return card;
    }

    private Dock pickBar() {
        var text = Component.translatable(GridMapLang.PICK_HINT);
        var hint = TextLine.constant(UISizes.widthFor(text.getString()), text).bindClientColor(UITheme::panelText);
        var cancel = Button.translatable(UISizes.BUTTON_WIDTH, GridMapLang.PICK_CANCEL).setOnClientClick(this::sendBack);
        cancel.disabled(() -> ctx.machine() == null, GridMapLang.FROM_TERMINAL);
        var bar = new Dock().addGroup(UIElement.centeredRow(UISizes.CONTROL_HEIGHT).addChildren(hint, cancel));
        bar.setDisplay(ctx.mode() == GridMapMode.PICK);
        return bar;
    }

    @Nullable
    private Popup createCard(int argument) {
        int node = GridMapView.cardNode(argument), dimRef = GridMapView.cardDimRef(argument);
        if (node < 0 && (dimRef <= 0 || GridView.dimension(dimRef) == null)) return null;
        if (node >= 0 && !isRemote() && node >= ctx.topology().nodes().size()) return null;
        return GridNodeCard.popup(new GridCardData(ctx, node, dimRef));
    }

    GridMapContext context() {
        return ctx;
    }

    void locateCurrent() {
        var navigator = ctx.navigator();
        if (navigator != null) navigator.locateCurrent();
    }

    void toggleLegend() {
        toggles.setLegend(!toggles.isLegend());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void clientInit() {
        super.clientInit();
        GridMapClient.install(this);
    }

    @Override
    protected void onScreenResized(int width, int height) {
        canvas.setPreferredSize(width, height);
        dock.layout(l -> l.maxWidth(Math.max(UISizes.DOCK_BUTTON_SIZE, width - 2 * UISizes.DOCK_MARGIN)));
    }

    @Override
    public void detectAndSendChanges() {
        if (!isRemote()) ctx.tick();
        super.detectAndSendChanges();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        ctx.beginFrame();
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        boolean legendShown = toggles.isLegend();
        if (legend.isDisplayed() != legendShown) legend.setDisplay(legendShown);
        int hudBottom = hud.getPositionY() - getPositionY() + hud.getSizeHeight();
        placeLegend(hudBottom, legendShown);
        if (!emptyCard.isDisplayed()) return;
        int top = hudBottom + UISizes.DOCK_MARGIN;
        if (top == emptyTop) return;
        emptyTop = top;
        emptyCard.layout(l -> l.top(top));
    }

    @OnlyIn(Dist.CLIENT)
    private void placeLegend(int hudBottom, boolean legendShown) {
        boolean shown = isShownInHud(rank);
        if (shown && rank.getSizeHeight() > 0) rankHeight = rank.getSizeHeight() + UISizes.GAP;
        int withoutRank = hudBottom - (shown ? rankHeight : 0);
        int limit = dock.getPositionY() - getPositionY() - UISizes.DOCK_MARGIN;
        boolean roomForRank = !legendShown || withoutRank + rankHeight + LEGEND_GAP + legend.getSizeHeight() <= limit;
        if (rank.isDisplayed() != roomForRank) rank.setDisplay(roomForRank);
        int top = (roomForRank ? withoutRank + rankHeight : withoutRank) + LEGEND_GAP;
        if (top == legendTop) return;
        legendTop = top;
        legend.layout(l -> l.top(top));
    }

    private boolean isShownInHud(Widget widget) {
        for (Widget current = widget; current != null && current != hud; current = current.getParent()) {
            if (!current.isVisible()) return false;
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode != GLFW.GLFW_KEY_ESCAPE || !card.isOpenOrRequested()) return false;
        card.close();
        return true;
    }

    @Override
    protected void onUIClosed() {
        super.onUIClosed();
        if (!isRemote()) ctx.close();
    }

    @Override
    public GridView.TopologyView topology() {
        return topology.getValue();
    }

    @Override
    public GridView.LiveView live() {
        return live.getValue();
    }

    @Override
    public GridMapToggles toggles() {
        return toggles;
    }

    @Override
    public CanvasView canvas() {
        return canvas;
    }

    @Override
    public CardHost card() {
        return card;
    }

    @Override
    public GridMapMode mode() {
        return ctx.mode();
    }

    @Override
    public int focusDimRef() {
        return ctx.focusDimRef();
    }

    @Override
    public void sendPick(int dimRef) {
        pick.send(dimRef);
    }

    @Override
    public void sendBack() {
        back.send(Unit.INSTANCE);
    }

    @Override
    public void setNavigator(@Nullable GridMapNavigator navigator) {
        ctx.setNavigator(navigator);
    }

    @Override
    public int hoveredCardLine() {
        return ctx.hoveredLine();
    }
}
