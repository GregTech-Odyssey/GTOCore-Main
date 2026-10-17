package com.gtocore.client.screen.starmap.grid;

import com.gtocore.client.screen.starmap.base.PixelPen;

import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.render.UIDraw;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 信息卡层的色块，全部用标准绘制：卡片底板走 {@link UIDraw#bevel}，存量条走标准进度条轨道与填充，角括号与选中框走画布画笔。
 */
@OnlyIn(Dist.CLIENT)
final class GridTagPainter {

    private static final int SELECTION = 0xFFFFFFFF;
    private static final int CORNER_GAP = 4, CORNER_ARM = 3;

    private PixelPen pen;
    private CanvasPainter painter;

    void begin(PixelPen pen, CanvasPainter painter) {
        this.pen = pen;
        this.painter = painter;
    }

    void end() {
        painter.flush();
    }

    void plate(int left, int top, int right, int bottom, int edge) {
        UIDraw.bevel(pen.graphics(), left, top, right - left, bottom - top, edge, edge, GridStyle.plate());
    }

    void bar(int left, int top, int width, int height, float fill) {
        UIDraw.progressTrack(pen.graphics(), left, top, width, height);
        float ratio = fill > 0 ? Math.max(fill, 1f / Math.max(1, width - 2)) : 0;
        UIDraw.progressFill(pen.graphics(), left, top, width, height, 0, ratio, GridStyle.fillColor(fill));
    }

    void dot(int centerX, int top, int width, float fill) {
        bar(centerX - width / 2, top, width, GridTagLayout.DOT_HEIGHT, fill);
    }

    void corners(int centerX, int centerY, int size, int color) {
        int half = size / 2 + CORNER_GAP, l = centerX - half, t = centerY - half, r = centerX + half, b = centerY + half;
        corner(l, t, 1, 1, color);
        corner(r - 1, t, -1, 1, color);
        corner(l, b - 1, 1, -1, color);
        corner(r - 1, b - 1, -1, -1, color);
    }

    private void corner(int x, int y, int dx, int dy, int color) {
        int x2 = x + dx * CORNER_ARM, y2 = y + dy * CORNER_ARM;
        painter.fill(Math.min(x, x2 - dx), y, Math.max(x, x2 - dx) + 1, y + 1, color);
        painter.fill(x, Math.min(y, y2 - dy), x + 1, Math.max(y, y2 - dy) + 1, color);
    }

    void selection(int centerX, int centerY, int size) {
        int half = size / 2;
        pen.outline(centerX - half - 2, centerY - half - 2, centerX + half + 2, centerY + half + 2, SELECTION);
    }
}
