package com.gtocore.client.screen.starmap.grid;

import com.gtocore.client.screen.starmap.base.PixelPen;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarGeometry;

import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.render.UISegments;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * 选中天体时的引导：到其他所有天体的 1 像素灰线（已有线路相连的不画，由线路层画真实线路），悬停目标提亮；
 * 另一层在线路之上用虚线标出两地最大输送所经的线路。
 */
@OnlyIn(Dist.CLIENT)
final class GridGuideLayer implements CanvasLayer {

    private static final int DASH_ON = 3;
    private static final int DASH_OFF = 2;
    private static final float CLEAR_RADIUS = 16;

    private final GridMapClient client;
    private final GridModel model;
    private final StarCatalog catalog;
    private final StarGeometry geometry;
    private final PixelPen pen;
    private final float[] range = new float[2];
    private CanvasPainter painter;
    private final CanvasLayer path = new PathLayer();

    GridGuideLayer(GridMapClient client) {
        this.client = client;
        this.model = client.model();
        this.catalog = client.catalog();
        this.geometry = client.geometry();
        this.pen = client.pen();
    }

    CanvasLayer path() {
        return path;
    }

    private boolean guides(int selected, int body) {
        return body != selected && body >= 0 && body < catalog.size() && !geometry.isFolded(body) && !model.isNeighbor(selected, body);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
        int selected = client.selected();
        if (selected < 0 || selected >= catalog.size()) return;
        this.painter = painter;
        pen.begin(painter.graphics(), client.canvas());
        pen.beginFine();
        int sx = pen.fineX(model.worldX(selected)), sy = pen.fineY(model.worldY(selected)), target = client.hoveredBody();
        int color = GridStyle.guide();
        for (int body = 0; body < catalog.size(); body++) {
            if (body != target && guides(selected, body)) guide(sx, sy, body, color, false);
        }
        if (guides(selected, target)) guide(sx, sy, target, UITheme.MAP_GUIDE_HOVER, true);
        painter.flush();
        pen.endFine();
        pen.end();
    }

    private void guide(int sx, int sy, int body, int color, boolean solid) {
        int x = pen.fineX(model.worldX(body)), y = pen.fineY(model.worldY(body)), d = pen.density();
        float dx = x - sx, dy = y - sy, length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= CLEAR_RADIUS * d) return;
        float t = CLEAR_RADIUS * d / length;
        int fromX = sx + Math.round(dx * t), fromY = sy + Math.round(dy * t);
        if (solid) pen.fineLine(fromX, fromY, x, y, 1, color);
        else dotted(fromX, fromY, x, y, 1, 2 * d - 1, color);
    }

    private void dotted(int x0, int y0, int x1, int y1, int on, int off, int color) {
        int dx = x1 - x0, dy = y1 - y0, length = Math.max(Math.abs(dx), Math.abs(dy)), period = on + off;
        int d = pen.density();
        if (length == 0 || !UISegments.clip(x0, y0, x1, y1, pen.clipLeft() * d, pen.clipTop() * d, pen.clipRight() * d, pen.clipBottom() * d, range)) return;
        int from = (int) Math.floor(range[0] * length), to = (int) Math.ceil(range[1] * length);
        for (int step = from; step <= to; step++) {
            if (step % period >= on) continue;
            int x = x0 + Math.round((float) dx * step / length), y = y0 + Math.round((float) dy * step / length);
            painter.fill(x, y, x + 1, y + 1, color);
        }
    }

    private final class PathLayer implements CanvasLayer {

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
            var flow = client.pairFlow();
            if (!flow.isActive()) return;
            GridGuideLayer.this.painter = painter;
            pen.begin(painter.graphics(), client.canvas());
            pen.beginFine();
            int color = UITheme.MAP_PATH, d = pen.density();
            for (int l = 0; l < model.lineCount(); l++) {
                if (!flow.carries(l) || !model.isPlaced(l)) continue;
                int a = model.lineA(l), b = model.lineB(l);
                dotted(pen.fineX(model.worldX(a)), pen.fineY(model.worldY(a)), pen.fineX(model.worldX(b)), pen.fineY(model.worldY(b)), DASH_ON * d, DASH_OFF * d, color);
            }
            painter.flush();
            pen.endFine();
            pen.end();
        }
    }
}
