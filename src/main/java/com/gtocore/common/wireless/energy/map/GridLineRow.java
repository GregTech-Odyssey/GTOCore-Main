package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.GridView;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.ProgressBar;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import dev.vfyjxf.taffy.style.AlignContent;
import org.jetbrains.annotations.Nullable;

final class GridLineRow extends UIElement {

    static final int HEIGHT = 2 * TextLine.HEIGHT + ProgressBar.HEIGHT + 2 * UISizes.GAP + 2;
    private static final int TIER_WIDTH = 64;
    private static final float SATURATED = 0.95f;
    private static final ProgressBar.Progress EMPTY_USAGE = new ProgressBar.Progress(0, 1000, 0);

    private final GridCardData data;
    private final int line;
    @Nullable
    private GridView.TopologyView shownTopology;
    @Nullable
    private GridView.LiveView shownLive;
    private Component peer = GridFormat.NO_VALUE, tier = GridFormat.NO_VALUE, flow = GridFormat.NO_VALUE;
    private ProgressBar.Progress usage = EMPTY_USAGE;

    GridLineRow(GridCardData data, int line) {
        this.data = data;
        this.line = line;
        layout(l -> l.column().height(HEIGHT).gapAll(UISizes.GAP).justifyContent(AlignContent.CENTER));
        var peerText = TextLine.of(0, this::peer).bindClientColor(UITheme::panelText);
        var tierText = TextLine.of(TIER_WIDTH, this::tier).styled().setTextAlign(Horizontal.RIGHT);
        var flowText = TextLine.of(LayoutStyle.AUTO, this::flow).bindClientColor(UITheme::textSecondary);
        var bar = ProgressBar.of(LayoutStyle.AUTO, Component.translatable(GridMapLang.LINE_USAGE), 0, this::usage).percent();
        bar.bindClientColor(() -> bar.getProgress().ratio() >= SATURATED ? UITheme.MAP_SATURATED : UITheme.MAP_LINK_HIGH);
        addChildren(UIElement.row(TextLine.HEIGHT).addChildren(peerText.layout(l -> l.flex(1)), tierText), flowText,
                bar);
        tooltips(GridMapLang.LINE_CLICK);
    }

    private Component peer() {
        refresh();
        return peer;
    }

    private Component tier() {
        refresh();
        return tier;
    }

    private ProgressBar.Progress usage() {
        refresh();
        return usage;
    }

    private Component flow() {
        refresh();
        return flow;
    }

    private void refresh() {
        var topology = data.topology();
        var live = data.live();
        if (topology == shownTopology && live == shownLive) return;
        boolean structural = topology != shownTopology;
        shownTopology = topology;
        shownLive = live;
        var lines = topology.lines();
        int node = data.nodeIndex();
        if (line >= lines.size() || node < 0) {
            peer = tier = flow = GridFormat.NO_VALUE;
            usage = EMPTY_USAGE;
            return;
        }
        var info = lines.get(line);
        boolean forward = info.a() == node;
        if (structural) {
            peer = GridFormat.bodyName(topology.nodes().get(forward ? info.b() : info.a()).dimension());
            tier = Component.translatable(GridMapLang.LINE_TIER, GridFormat.tierText(info.tier()), GridFormat.compact(amps(info)));
        }
        boolean current = live.topologyRevision() == topology.revision();
        float ab = current ? live.flowAB(line) : 0, ba = current ? live.flowBA(line) : 0;
        flow = Component.translatable(GridMapLang.LINE_FLOW, GridFormat.amount(forward ? ab : ba), GridFormat.amount(forward ? ba : ab));
        usage = new ProgressBar.Progress(Math.round(GridCardLines.usage(topology, live, line) * 1000), 1000, 0);
    }

    private static long amps(GridView.LineInfo info) {
        int tier = info.tier();
        return tier >= 0 && tier < GTValues.V.length && GTValues.V[tier] > 0 ? info.budget() / GTValues.V[tier] : 0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (isPointerOver(mouseX, mouseY)) {
            data.ctx.hoverLine(line);
            UIDraw.hoverOverlay(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button != 0 || !isPointerOver(mouseX, mouseY)) return false;
        var navigator = data.ctx.navigator();
        if (navigator != null) navigator.focusLine(line);
        playButtonClickSound();
        return true;
    }
}
