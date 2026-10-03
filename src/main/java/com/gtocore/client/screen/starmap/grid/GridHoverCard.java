package com.gtocore.client.screen.starmap.grid;

import com.gtocore.client.screen.starmap.base.PixelPen;

import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UISegments;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 画布最上层的悬停浮卡：不透明底与 1 像素边，第一行为标题；每帧先定位置（光标旁，避开界面块、最大输送路径与选中天体的卡片，
 * 放不下就翻到另一侧），信息卡层据此隐去被盖住的卡片。
 */
@OnlyIn(Dist.CLIENT)
final class GridHoverCard implements CanvasLayer {

    private static final int PAD = 4, ROW = 10, RULE = 3, OFFSET = 12, GAP = 4, CURSOR = 2, CURSOR_ROOM = 12, CANDIDATES = 8;

    private final GridMapClient client;
    private final PixelPen pen;
    private List<Component> shown = Collections.emptyList();
    private int maxWidth, generation, left, top, width, height, pathLeft, pathTop, pathRight, pathBottom;
    private final int[] box = new int[4], card = new int[4];
    private final float[] range = new float[2];
    private int mouseX, mouseY;
    private boolean visible, hasBox;

    GridHoverCard(GridMapClient client) {
        this.client = client;
        this.pen = client.pen();
    }

    private List<Component> lines() {
        int body = client.hoveredBody();
        if (body >= 0) return client.decorator().hoverLines(body);
        int anchor = client.hoveredAnchor();
        if (anchor >= 0) return client.decorator().anchorLines(anchor);
        int line = client.hoveredLine();
        return line >= 0 ? client.links().hoverLines(line) : Collections.emptyList();
    }

    private void measure(List<Component> lines) {
        shown = lines;
        generation = UIText.generation();
        maxWidth = 0;
        for (int i = 0; i < lines.size(); i++) maxWidth = Math.max(maxWidth, UIText.width(lines.get(i)));
    }

    void prepare(CanvasPainter painter) {
        visible = false;
        hasBox = false;
        var lines = lines();
        if (lines.isEmpty() || Float.isNaN(painter.mouseX())) return;
        if (lines != shown || generation != UIText.generation()) measure(lines);
        mouseX = screenX(painter.mouseX());
        mouseY = screenY(painter.mouseY());
        width = maxWidth + 2 * PAD;
        height = 2 * PAD + lines.size() * ROW + (lines.size() > 1 ? RULE : 0) - 2;
        pathBox();
        objectBox();
        for (int strict = 2; strict >= 0 && !visible; strict--) {
            for (int k = 0; k < CANDIDATES && !visible; k++) {
                spot(k);
                visible = allowed(left, top, left + width, top + height, strict);
            }
        }
        if (visible) return;
        var area = client.obstructions();
        spot(0);
        left = Math.max(area.left(), Math.min(left, area.right() - width));
        top = Math.max(area.top(), Math.min(top, area.bottom() - height));
        visible = !contains(left, top, left + width, top + height, mouseX, mouseY);
    }

    private void spot(int k) {
        switch (k) {
            case 0 -> set(mouseX + OFFSET, mouseY + OFFSET);
            case 1 -> set(mouseX - OFFSET - width, mouseY + OFFSET);
            case 2 -> set(mouseX + OFFSET, mouseY - OFFSET - height);
            case 3 -> set(mouseX - OFFSET - width, mouseY - OFFSET - height);
            case 4 -> set(box[2] + GAP, mouseY - height / 2);
            case 5 -> set(box[0] - GAP - width, mouseY - height / 2);
            case 6 -> set(mouseX - width / 2, box[3] + GAP);
            default -> set(mouseX - width / 2, box[1] - GAP - height);
        }
    }

    private void set(int l, int t) {
        left = l;
        top = t;
    }

    private boolean allowed(int l, int t, int r, int b, int strict) {
        if (contains(l, t, r, b, mouseX, mouseY) || hasBox && l < box[2] && r > box[0] && t < box[3] && b > box[1]) return false;
        var area = client.obstructions();
        if (strict >= 1 ? !area.fits(l, t, r, b) : l < area.left() || t < area.top() || r > area.right() || b > area.bottom()) return false;
        if (strict >= 1 && crossesLine(l, t, r, b)) return false;
        return strict < 2 || !hitsPath(l, t, r, b) && !client.tags().overlapsSelected(l, t, r, b);
    }

    private static boolean contains(int l, int t, int r, int b, int x, int y) {
        return x >= l - CURSOR && x < r + CURSOR && y >= t - CURSOR && y < b + CURSOR;
    }

    private void objectBox() {
        hasBox = false;
        int body = client.hoveredBody(), anchor = client.hoveredAnchor();
        var geometry = client.geometry();
        if (body >= 0) {
            int x = screenX(client.model().worldX(body)), y = screenY(client.model().worldY(body)), h = geometry.pixels(body) / 2 + 2;
            box(x - h, y - h, x + h, y + h);
            if (client.tags().cardBox(body, card)) box(card[0], card[1], card[2], card[3]);
        } else if (anchor >= 0) {
            int x = screenX(geometry.anchorX(anchor)), y = screenY(geometry.anchorY(anchor)), h = geometry.anchorPixels() / 2 + 2;
            box(x - h, y - h, x + h, y + h);
            if (client.tags().cardBox(GridTagLayout.anchorOwner(anchor), card)) box(card[0], card[1], card[2], card[3]);
        } else {
            box(mouseX - CURSOR_ROOM, mouseY - CURSOR_ROOM, mouseX + CURSOR_ROOM, mouseY + CURSOR_ROOM);
        }
    }

    private boolean crossesLine(int l, int t, int r, int b) {
        int line = client.hoveredLine();
        if (line < 0 || client.hoveredBody() >= 0 || client.hoveredAnchor() >= 0 || !client.model().isPlaced(line)) return false;
        var model = client.model();
        int a = model.lineA(line), e = model.lineB(line);
        return UISegments.clip(screenX(model.worldX(a)), screenY(model.worldY(a)), screenX(model.worldX(e)), screenY(model.worldY(e)), l, t, r, b, range);
    }

    private void box(int l, int t, int r, int b) {
        if (!hasBox) {
            box[0] = l;
            box[1] = t;
            box[2] = r;
            box[3] = b;
            hasBox = true;
            return;
        }
        box[0] = Math.min(box[0], l);
        box[1] = Math.min(box[1], t);
        box[2] = Math.max(box[2], r);
        box[3] = Math.max(box[3], b);
    }

    boolean holds(int x, int y) {
        return hasBox && x >= box[0] - CURSOR && x < box[2] + CURSOR && y >= box[1] - CURSOR && y < box[3] + CURSOR;
    }

    boolean covers(int l, int t, int r, int b) {
        return visible && l < left + width && r > left && t < top + height && b > top;
    }

    private void pathBox() {
        var flow = client.pairFlow();
        var model = client.model();
        pathLeft = Integer.MAX_VALUE;
        pathTop = Integer.MAX_VALUE;
        pathRight = Integer.MIN_VALUE;
        pathBottom = Integer.MIN_VALUE;
        if (!flow.isActive()) return;
        for (int l = 0; l < model.lineCount(); l++) {
            if (!flow.carries(l) || !model.isPlaced(l)) continue;
            include(model.lineA(l));
            include(model.lineB(l));
        }
    }

    private void include(int body) {
        var model = client.model();
        int x = screenX(model.worldX(body)), y = screenY(model.worldY(body));
        pathLeft = Math.min(pathLeft, x);
        pathTop = Math.min(pathTop, y);
        pathRight = Math.max(pathRight, x);
        pathBottom = Math.max(pathBottom, y);
    }

    private boolean hitsPath(int l, int t, int r, int b) {
        return l < pathRight && r > pathLeft && t < pathBottom && b > pathTop;
    }

    private int screenX(float worldX) {
        var canvas = client.canvas();
        return Math.round(canvas.viewportX() + (worldX - canvas.offsetX()) * canvas.scale());
    }

    private int screenY(float worldY) {
        var canvas = client.canvas();
        return Math.round(canvas.viewportY() + (worldY - canvas.offsetY()) * canvas.scale());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
        if (!visible) return;
        painter.flush();
        pen.begin(painter.graphics(), client.canvas());
        render(shown, left, top, width, height);
        pen.end();
    }

    private void render(List<Component> lines, int left, int top, int width, int height) {
        var graphics = pen.graphics();
        UIDraw.bevel(graphics, left, top, width, height, UITheme.MAP_PLATE_EDGE, UITheme.MAP_PLATE_EDGE, GridStyle.plate());
        if (lines.size() > 1) UIDraw.fillRect(graphics, left + PAD, top + PAD + ROW, width - 2 * PAD, 1, UITheme.MAP_PLATE_EDGE);
        for (int i = 0; i < lines.size(); i++) {
            int y = top + PAD + i * ROW + (i > 0 ? RULE : 0);
            pen.left(lines.get(i), left + PAD, y, i == 0 ? 0xFFFFFFFF : UITheme.MAP_LABEL);
        }
    }
}
