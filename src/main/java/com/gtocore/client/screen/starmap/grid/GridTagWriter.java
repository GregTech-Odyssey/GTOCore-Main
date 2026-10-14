package com.gtocore.client.screen.starmap.grid;

import com.gtocore.client.screen.starmap.base.PixelPen;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarGeometry;

import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 信息卡的文字部分：标题（名字与折叠卫星角标）在左上，其余按左列（存量）、右列（电压、百分比、净流量）两列对齐，用画笔的标准文字绘制。
 */
@OnlyIn(Dist.CLIENT)
final class GridTagWriter {

    private static final int PAD = GridNodeTags.PAD, TOP = GridNodeTags.TOP, ROW = GridNodeTags.ROW, BADGE_GAP = 3;

    private final GridTagTexts texts;
    private final GridTagLayout layout;
    private final StarCatalog catalog;
    private final PixelPen pen;

    GridTagWriter(GridTagTexts texts, GridTagLayout layout, StarCatalog catalog, PixelPen pen) {
        this.texts = texts;
        this.layout = layout;
        this.catalog = catalog;
        this.pen = pen;
    }

    private boolean badged(int node) {
        return texts.badge[node] != null && pen.scale() < StarGeometry.FOLD_SCALE;
    }

    int titleWidth(int node) {
        return texts.nameWidth[node] + (badged(node) ? BADGE_GAP + texts.badgeWidth[node] : 0);
    }

    void draw(int[] nodes, int[] bodies, int[] modes) {
        for (int k = 0; k < layout.placed(); k++) {
            int node = nodes[k], mode = modes[k], left = layout.rect(k, 0) + PAD, top = layout.rect(k, 1) + TOP, right = layout.rect(k, 2) - PAD;
            if (mode == GridNodeTags.HIDDEN) continue;
            if (mode == GridNodeTags.PARENT) {
                text(texts.parentTitle(node), left, top, UITheme.MAP_LABEL_DIM);
                continue;
            }
            title(node, bodies[k], mode, left, top);
            if (mode == GridNodeTags.NAME) continue;
            if (texts.lineOnly[node]) relay(node, mode, left, top, right);
            else if (mode == GridNodeTags.FULL) full(node, top, left, right);
            else if (mode == GridNodeTags.COMPACT) compact(node, top, right);
            else text(texts.percent[node], right - texts.percentWidth[node], top, UITheme.MAP_LABEL_DIM);
        }
    }

    private void title(int node, int body, int mode, int left, int top) {
        int color = mode == GridNodeTags.NAME ? UITheme.MAP_LABEL_DIM : body == catalog.current() ? UITheme.MAP_CURRENT : UITheme.MAP_LABEL;
        text(texts.name[node], left, top, color);
        if (badged(node)) text(texts.badge[node], left + texts.nameWidth[node] + BADGE_GAP, top, UITheme.MAP_LABEL_DIM);
    }

    private void relay(int node, int mode, int left, int top, int right) {
        if (mode == GridNodeTags.FULL) text(texts.relay[node], left, top + ROW, UITheme.MAP_LABEL_DIM);
        else text(texts.relayShort[node], right - texts.relayShortWidth[node], top, UITheme.MAP_LABEL_DIM);
    }

    private void compact(int node, int top, int right) {
        int x = right - texts.summaryWidth[node];
        text(texts.summary[node], x, top, UITheme.MAP_LABEL_DIM);
        int trend = texts.trend[node];
        if (trend != 0) text(trend > 0 ? GridTagTexts.RISE : GridTagTexts.FALL, x - 2 - texts.glyphWidth(), top, trendColor(trend));
    }

    private void full(int node, int top, int left, int right) {
        int row = top + GridNodeTags.BAR_TOP + GridNodeTags.BAR_THICK + 2 - TOP;
        text(texts.tier[node], right - texts.tierWidth[node], top, UITheme.MAP_LABEL);
        text(texts.stored[node], left, row, UITheme.MAP_LABEL);
        text(texts.percent[node], right - texts.percentWidth[node], row, UITheme.MAP_LABEL_DIM);
        text(texts.delta[node], right - texts.deltaWidth[node], row + ROW, trendColor(texts.trend[node]));
    }

    static int trendColor(int trend) {
        return trend > 0 ? UITheme.MAP_NET_UP : trend < 0 ? UITheme.MAP_NET_DOWN : UITheme.MAP_LABEL_DIM;
    }

    private void text(Component text, int x, int y, int color) {
        pen.left(text, x, y, color);
    }
}
