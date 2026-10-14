package com.gtocore.client.screen.starmap.grid;

import com.gtocore.common.wireless.energy.map.GridMapView;

import com.gregtechceu.gtceu.uipro.canvas.CanvasView;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

/**
 * 每帧收集盖在画布上的界面块（汇总、图例、提示条、悬浮栏、星球弹框）的屏幕矩形，信息卡与悬停浮卡据此避让。
 */
@OnlyIn(Dist.CLIENT)
final class GridObstructions {

    private static final int MAX = 16;

    private final GridMapView view;
    private final CanvasView canvas;
    private final int[] rects = new int[MAX * 4];
    private int count;
    private int left, top, right, bottom;

    GridObstructions(GridMapView view, CanvasView canvas) {
        this.view = view;
        this.canvas = canvas;
    }

    void refresh() {
        count = 0;
        left = canvas.viewportX();
        top = canvas.viewportY();
        right = left + canvas.viewportWidth();
        bottom = top + canvas.viewportHeight();
        if (view instanceof WidgetGroup root) collect(root, canvas);
        collect(canvas, null);
    }

    private void collect(WidgetGroup group, Widget skip) {
        var widgets = group.widgets;
        for (int i = 0; i < widgets.size() && count < MAX; i++) {
            var widget = widgets.get(i);
            int w = widget.getSizeWidth(), h = widget.getSizeHeight();
            if (widget == skip || !widget.isVisible() || w <= 0 || h <= 0) continue;
            int k = count++;
            rects[k * 4] = widget.getPositionX();
            rects[k * 4 + 1] = widget.getPositionY();
            rects[k * 4 + 2] = widget.getPositionX() + w;
            rects[k * 4 + 3] = widget.getPositionY() + h;
        }
    }

    boolean fits(int l, int t, int r, int b) {
        return l >= left && t >= top && r <= right && b <= bottom && blocker(l, t, r, b) < 0;
    }

    int blocker(int l, int t, int r, int b) {
        for (int k = 0; k < count; k++) {
            if (l < rects[k * 4 + 2] && r > rects[k * 4] && t < rects[k * 4 + 3] && b > rects[k * 4 + 1]) return k;
        }
        return -1;
    }

    int rect(int index, int edge) {
        return rects[index * 4 + edge];
    }

    int left() {
        return left;
    }

    int top() {
        return top;
    }

    int right() {
        return right;
    }

    int bottom() {
        return bottom;
    }
}
