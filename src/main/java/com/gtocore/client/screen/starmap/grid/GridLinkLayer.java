package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.PixelPen;
import com.gtocore.client.screen.starmap.base.StarSprites;

import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 全部线路：按流量分档定粗细与颜色，满载画琥珀色标记，悬停或卡片里悬停的线加粗并加白芯，选中天体时无关线压暗；也负责线路的命中测试。
 */
@OnlyIn(Dist.CLIENT)
final class GridLinkLayer implements CanvasLayer {

    private static final int RANKS = GridStyle.LEVELS + 2;
    private static final int PICK_REACH = 5, TIE = 2;
    private static final int OUTLINE = 8;

    private final GridMapClient client;
    private final GridModel model;
    private final PixelPen pen;
    private CanvasPainter painter;
    private final int[] rank = new int[GridView.MAX_LINES];
    private final LineItem[] items = new LineItem[GridView.MAX_LINES];
    private List<Component> tip = Collections.emptyList();
    private int tipLine = -1, tipStamp = -1;

    GridLinkLayer(GridMapClient client) {
        this.client = client;
        this.model = client.model();
        this.pen = client.pen();
        for (int l = 0; l < items.length; l++) items[l] = new LineItem(l);
    }

    int lineOf(@Nullable CanvasItem item) {
        return item instanceof LineItem line && line.layer() == this ? line.index : -1;
    }

    boolean isStablyShown(int line) {
        if (!model.isPlaced(line)) return false;
        if (model.level(line) != GridStyle.IDLE || client.view().toggles().isIdleLines()) return true;
        int selected = client.selected();
        return selected >= 0 && model.touches(line, selected);
    }

    boolean isShown(int line) {
        if (!model.isPlaced(line)) return false;
        if (model.level(line) != GridStyle.IDLE || client.view().toggles().isIdleLines()) return true;
        int selected = client.selected();
        return selected >= 0 && model.touches(line, selected) || client.isEmphasized(line);
    }

    int width(int line) {
        if (client.isDimmed(line)) return 1;
        return GridStyle.width(model.level(line)) + (client.isEmphasized(line) ? 1 : 0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
        int count = model.lineCount();
        for (int l = 0; l < count; l++) {
            rank[l] = !isShown(l) ? -1 : client.isEmphasized(l) ? RANKS - 1 : client.isDimmed(l) ? 0 : model.level(l) + 1;
        }
        this.painter = painter;
        pen.begin(painter.graphics(), client.canvas());
        outlines();
        pen.beginFine();
        for (int r = 0; r < RANKS; r++) {
            for (int l = 0; l < count; l++) {
                if (rank[l] == r) drawLine(l);
            }
        }
        for (int l = 0; l < count; l++) {
            if (rank[l] > 0) ends(l);
        }
        painter.flush();
        pen.endFine();
        pen.end();
    }

    private void drawLine(int line) {
        int a = model.lineA(line), b = model.lineB(line);
        int x0 = pen.fineX(model.worldX(a)), y0 = pen.fineY(model.worldY(a)), x1 = pen.fineX(model.worldX(b)), y1 = pen.fineY(model.worldY(b));
        if (x0 == x1 && y0 == y1) return;
        boolean dimmed = client.isDimmed(line);
        int level = model.level(line);
        pen.fineLine(x0, y0, x1, y1, width(line), dimmed ? UITheme.MAP_LINK_DIM : GridStyle.color(level));
        if (dimmed) return;
        if (client.isEmphasized(line)) pen.fineLine(x0, y0, x1, y1, 1, GridStyle.HOVER_CORE);
        else if (level == GridStyle.CORE) pen.fineLine(x0, y0, x1, y1, 1, UITheme.MAP_LINK_CORE);
    }

    private void ends(int line) {
        int a = model.lineA(line), b = model.lineB(line), size = width(line) + 2;
        int color = client.isEmphasized(line) ? GridStyle.HOVER_CORE : GridStyle.color(model.level(line));
        int ax = pen.fineX(model.worldX(a)), ay = pen.fineY(model.worldY(a)), bx = pen.fineX(model.worldX(b)), by = pen.fineY(model.worldY(b));
        end(a, ax, ay, bx, by, size, color);
        end(b, bx, by, ax, ay, size, color);
    }

    private void end(int body, int x, int y, int toX, int toY, int size, int color) {
        int dx = toX - x, dy = toY - y, major = Math.max(Math.abs(dx), Math.abs(dy));
        if (major == 0) return;
        if (client.geometry().isFolded(body)) {
            block(x, y, size + 2, UITheme.MAP_SPRITE_OUTLINE);
            block(x, y, size, color);
            return;
        }
        int reach = reach(body, size);
        if (reach * 2 >= major) return;
        block(x + Math.round((float) dx * reach / major), y + Math.round((float) dy * reach / major), size, color);
    }

    private int reach(int body, int size) {
        return client.geometry().isFolded(body) ? size / 2 + 1 : (client.geometry().pixels(body) / 2 + 1) * pen.density() + size / 2 + 1;
    }

    private void block(int cx, int cy, int size, int color) {
        int half = size / 2;
        if (pen.fineVisible(cx - half, cy - half, cx - half + size, cy - half + size)) painter.fill(cx - half, cy - half, cx - half + size, cy - half + size, color);
    }

    private void outlines() {
        if (pen.scale() >= GridStyle.SPRITE_SCALE) return;
        var catalog = client.catalog();
        var geometry = client.geometry();
        var decorator = client.decorator();
        for (int body = 0; body < catalog.size(); body++) {
            if (geometry.isFolded(body) || decorator.lit(body)) continue;
            int x = pen.x(geometry.x(body)), y = pen.y(geometry.y(body)), half = OUTLINE / 2;
            pen.outline(x - half, y - half, x - half + OUTLINE, y - half + OUTLINE, GridStyle.outline());
        }
        for (int anchor = 0; anchor < catalog.anchorCount(); anchor++) {
            if (decorator.anchorTint(anchor) == StarSprites.NO_TINT) continue;
            int x = pen.x(geometry.anchorX(anchor)), y = pen.y(geometry.anchorY(anchor)), half = OUTLINE / 2;
            pen.outline(x - half, y - half, x - half + OUTLINE, y - half + OUTLINE, GridStyle.outline());
        }
    }

    @Override
    @Nullable
    public CanvasItem pick(float x, float y) {
        var canvas = client.canvas();
        float scale = canvas.scale();
        client.geometry().update(scale);
        float best = Float.MAX_VALUE;
        int found = -1, current = client.hoveredLine();
        for (int l = 0; l < model.lineCount(); l++) {
            if (!isShown(l)) continue;
            float distance = worldDistance(l, x, y) * scale;
            if (distance <= Math.max(width(l), PICK_REACH) && distance < best) {
                best = distance;
                found = l;
            }
        }
        if (current >= 0 && current != found && current < model.lineCount() && isShown(current) &&
                worldDistance(current, x, y) * scale <= Math.min(best + TIE, Math.max(width(current), PICK_REACH))) {
            found = current;
        }
        return found < 0 ? null : items[found];
    }

    float screenDistance(int line, float worldX, float worldY) {
        if (line < 0 || line >= model.lineCount() || !model.isPlaced(line)) return Float.MAX_VALUE;
        return worldDistance(line, worldX, worldY) * client.canvas().scale();
    }

    private float worldDistance(int line, float x, float y) {
        int a = model.lineA(line), b = model.lineB(line);
        return distance(x, y, model.worldX(a), model.worldY(a), model.worldX(b), model.worldY(b));
    }

    private static float distance(float px, float py, float ax, float ay, float bx, float by) {
        float dx = bx - ax, dy = by - ay, length = dx * dx + dy * dy;
        float t = length <= 0 ? 0 : Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / length));
        float ex = ax + t * dx - px, ey = ay + t * dy - py;
        return (float) Math.sqrt(ex * ex + ey * ey);
    }

    List<Component> hoverLines(int line) {
        if (line >= model.lineCount() || !model.isPlaced(line)) return Collections.emptyList();
        if (line != tipLine || tipStamp != model.stamp()) {
            tipLine = line;
            tipStamp = model.stamp();
            tip = GridTips.line(client.catalog(), model, line);
        }
        return tip;
    }

    private final class LineItem implements CanvasItem {

        private final int index;

        private LineItem(int index) {
            this.index = index;
        }

        private GridLinkLayer layer() {
            return GridLinkLayer.this;
        }

        @Override
        public CanvasRect bounds() {
            if (index >= model.lineCount() || !model.isPlaced(index)) return CanvasRect.of(0, 0, 0, 0);
            int a = model.lineA(index), b = model.lineB(index);
            float x0 = Math.min(model.worldX(a), model.worldX(b)), y0 = Math.min(model.worldY(a), model.worldY(b));
            return CanvasRect.of(x0, y0, Math.abs(model.worldX(a) - model.worldX(b)), Math.abs(model.worldY(a) - model.worldY(b)));
        }

        @Override
        public boolean hitTest(float x, float y) {
            return pick(x, y) == this;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawShape(CanvasPainter painter, boolean hovered) {}

        @Override
        public int blockColor() {
            return UITheme.MAP_LINK_MID;
        }
    }
}
