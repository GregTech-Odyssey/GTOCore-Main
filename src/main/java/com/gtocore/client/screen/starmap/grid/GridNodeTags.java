package com.gtocore.client.screen.starmap.grid;

import com.gtocore.client.screen.starmap.base.PixelPen;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarGeometry;

import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * 天体之上的一层：选中框、当前位置角括号，以及带名称的储能信息卡（迷你 / 紧凑 / 完整三档，放不下逐档降级；关掉信息卡时只放名字；
 * 未入网但有卫星入网的母星放「名字 +N」）。被悬停浮卡盖住的卡片不画；卡片区域与折叠卫星的端点也参与命中测试。
 */
@OnlyIn(Dist.CLIENT)
final class GridNodeTags implements CanvasLayer {

    static final int HIDDEN = -1, PARENT = -2, NAME = 0, MINI = 1, COMPACT = 2, FULL = 3;
    static final float COMPACT_SCALE = 0.2f, FULL_SCALE = 0.6f;
    static final int PAD = 3, TOP = 2, ROW = 9, GAP = 4, BAR = 4, BAR_THICK = 5, BAR_TOP = 12;
    private static final int SMALL_HEIGHT = 12, COMPACT_HEIGHT = 18, FULL_HEIGHT = 38, RELAY_HEIGHT = 21;
    private static final int COMPACT_MIN = 44, FULL_MIN = 80, DOT_PICK = 4, BOUNDS_X = 90, BOUNDS_Y = 40;

    private final GridMapClient client;
    private final GridModel model;
    private final StarCatalog catalog;
    private final StarGeometry geometry;
    private final PixelPen pen;
    private final GridTagTexts texts;
    private final GridTagPainter painter = new GridTagPainter();
    private final GridTagLayout layout;
    private final GridTagWriter writer;
    private final int[] placedNode = new int[GridTagLayout.CAPACITY], placedBody = new int[GridTagLayout.CAPACITY];
    private final int[] placedMode = new int[GridTagLayout.CAPACITY];
    private int[] named = new int[0];
    private int frame;

    GridNodeTags(GridMapClient client) {
        this.client = client;
        this.model = client.model();
        this.catalog = client.catalog();
        this.geometry = client.geometry();
        this.pen = client.pen();
        this.texts = new GridTagTexts(model, catalog);
        this.layout = new GridTagLayout(client);
        this.writer = new GridTagWriter(texts, layout, catalog, pen);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter canvasPainter, @Nullable CanvasItem hovered) {
        frame++;
        if (named.length != catalog.size()) named = new int[catalog.size()];
        pen.begin(canvasPainter.graphics(), client.canvas());
        painter.begin(pen, canvasPainter);
        layout.begin();
        marks();
        int selected = client.selected(), current = catalog.current();
        boolean tags = client.view().toggles().isTags();
        forced(selected, tags);
        if (tags) forced(current, true);
        for (int rank = 0; rank < model.nodeCount(); rank++) {
            int node = model.byCapacity(rank), body = model.body(node);
            if (body < 0 || body == selected || tags && body == current || geometry.isFolded(body)) continue;
            boolean shown = place(node, body, tags ? desired(body) : NAME, false);
            if (tags && (layout.dot(body) || !shown && pen.scale() < StarGeometry.LABEL_MIN_SCALE && !model.isLineOnly(node))) dot(node, body);
        }
        if (pen.scale() < StarGeometry.FOLD_SCALE) parents();
        painter.end();
        if (layout.placed() > 0) writer.draw(placedNode, placedBody, placedMode);
        pen.end();
    }

    boolean isNamed(int body) {
        return body >= 0 && body < named.length && frame > 0 && named[body] == frame;
    }

    private void marks() {
        int selected = client.selected(), current = catalog.current();
        if (selected >= 0 && !geometry.isFolded(selected)) {
            painter.selection(pen.x(geometry.x(selected)), pen.y(geometry.y(selected)), geometry.pixels(selected));
        }
        if (current >= 0 && !geometry.isFolded(current)) {
            painter.corners(pen.x(geometry.x(current)), pen.y(geometry.y(current)), geometry.pixels(current), UITheme.MAP_CURRENT);
        }
    }

    private int desired(int body) {
        float scale = pen.scale();
        if (scale < COMPACT_SCALE) return MINI;
        if (scale < FULL_SCALE || catalog.isMoon(body) && scale < StarGeometry.MOON_LABEL_SCALE) return COMPACT;
        return FULL;
    }

    private void parents() {
        for (int body = 0; body < catalog.bodyCount(); body++) {
            if (catalog.isMoon(body) || model.node(body) >= 0 || !onScreen(body)) continue;
            var title = texts.parentTitle(body);
            if (title != null && layout.place(body, 2 * PAD + texts.parentWidth(body), SMALL_HEIGHT, false)) parent(body, body);
        }
        for (int anchor = 0; anchor < catalog.anchorCount(); anchor++) {
            int owner = catalog.size() + anchor;
            var title = texts.parentTitle(owner);
            if (title != null && layout.placeAnchor(anchor, 2 * PAD + texts.parentWidth(owner), SMALL_HEIGHT)) {
                parent(owner, GridTagLayout.anchorOwner(anchor));
            }
        }
    }

    private void parent(int owner, int body) {
        int k = layout.placed() - 1;
        placedNode[k] = owner;
        placedBody[k] = body;
        placedMode[k] = PARENT;
        if (body >= 0 && body < named.length) named[body] = frame;
        shapes(k, -1, body, PARENT);
    }

    boolean onScreen(int body) {
        var area = client.obstructions();
        int x = pen.x(model.worldX(body)), y = pen.y(model.worldY(body));
        return x >= area.left() && x < area.right() && y >= area.top() && y < area.bottom();
    }

    boolean overlapsSelected(int l, int t, int r, int b) {
        int selected = client.selected();
        for (int k = 0; k < layout.placed(); k++) {
            if (placedBody[k] == selected && l < layout.rect(k, 2) && r > layout.rect(k, 0) && t < layout.rect(k, 3) && b > layout.rect(k, 1)) return true;
        }
        return false;
    }

    boolean cardBox(int body, int[] out) {
        for (int k = 0; k < layout.placed(); k++) {
            if (placedBody[k] != body) continue;
            for (int edge = 0; edge < 4; edge++) out[edge] = layout.rect(k, edge);
            return true;
        }
        return false;
    }

    private void forced(int body, boolean tags) {
        int node = model.node(body);
        if (node < 0 || !onScreen(body)) return;
        for (int k = 0; k < layout.placed(); k++) {
            if (placedBody[k] == body) return;
        }
        place(node, body, tags ? FULL : NAME, true);
    }

    private void dot(int node, int body) {
        int x = pen.x(geometry.x(body)), y = pen.y(geometry.y(body)), size = geometry.pixels(body);
        painter.dot(x, y + size / 2 + 2, Math.max(6, size), model.fill(node));
    }

    private boolean place(int node, int body, int mode, boolean forced) {
        texts.ensure(node);
        int lowest = mode == NAME ? NAME : MINI;
        for (int m = mode; m >= lowest; m--) {
            if (!layout.place(body, width(node, m), height(node, m), forced)) continue;
            int k = layout.placed() - 1;
            placedNode[k] = node;
            placedBody[k] = body;
            placedMode[k] = m;
            if (body < named.length) named[body] = frame;
            shapes(k, node, body, m);
            return true;
        }
        return false;
    }

    private int width(int node, int mode) {
        int title = 2 * PAD + writer.titleWidth(node);
        if (mode == NAME) return title;
        if (texts.lineOnly[node]) return mode == FULL ? Math.max(title, 2 * PAD + texts.relayWidth[node]) : title + GAP + texts.relayShortWidth[node];
        if (mode == MINI) return title + GAP + texts.percentWidth[node];
        int glyph = texts.trend[node] != 0 ? texts.glyphWidth() + 2 : 0;
        if (mode == COMPACT) return Math.max(COMPACT_MIN, title + GAP + glyph + texts.summaryWidth[node]);
        int head = title + GAP + texts.tierWidth[node], middle = 2 * PAD + texts.storedWidth[node] + GAP + texts.percentWidth[node];
        return Math.max(FULL_MIN, Math.max(head, Math.max(middle, 2 * PAD + texts.deltaWidth[node])));
    }

    private int height(int node, int mode) {
        if (mode == NAME) return SMALL_HEIGHT;
        if (texts.lineOnly[node]) return mode == FULL ? RELAY_HEIGHT : SMALL_HEIGHT;
        return mode == FULL ? FULL_HEIGHT : mode == COMPACT ? COMPACT_HEIGHT : SMALL_HEIGHT;
    }

    private void shapes(int k, int node, int body, int mode) {
        int left = layout.rect(k, 0), top = layout.rect(k, 1), right = layout.rect(k, 2), bottom = layout.rect(k, 3);
        boolean own = body == client.hoveredBody() || body == client.selected() || body == GridTagLayout.anchorOwner(client.hoveredAnchor());
        if (!own && client.hover().covers(left, top, right, bottom)) {
            placedMode[k] = HIDDEN;
            return;
        }
        int edge = body == client.selected() ? UITheme.SELECTION_COLOR : own && body != client.selected() ? UITheme.MAP_HOVER_FRAME : UITheme.MAP_PLATE_EDGE;
        painter.plate(left, top, right, bottom, edge);
        if (mode < COMPACT || texts.lineOnly[node]) return;
        painter.bar(left + PAD, top + BAR_TOP, right - left - 2 * PAD, mode == FULL ? BAR_THICK : BAR, model.fill(node));
    }

    @Override
    @Nullable
    public CanvasItem pick(float x, float y) {
        var canvas = client.canvas();
        float scale = canvas.scale();
        float sx = canvas.viewportX() + (x - canvas.offsetX()) * scale, sy = canvas.viewportY() + (y - canvas.offsetY()) * scale;
        int k = layout.hit(sx, sy);
        if (k >= 0 && placedBody[k] >= 0) return client.bodies().item(placedBody[k]);
        if (k >= 0) {
            int anchor = -2 - placedBody[k];
            return client.bodies().pick(geometry.anchorX(anchor), geometry.anchorY(anchor));
        }
        if (scale >= StarGeometry.FOLD_SCALE) return null;
        for (int node = 0; node < model.nodeCount(); node++) {
            int body = model.body(node);
            if (body < 0 || !geometry.isFolded(body)) continue;
            if (Math.abs(model.worldX(body) - x) * scale <= DOT_PICK && Math.abs(model.worldY(body) - y) * scale <= DOT_PICK) return client.bodies().item(body);
        }
        return null;
    }

    @Override
    @Nullable
    public CanvasRect bounds() {
        if (model.nodeCount() == 0) return null;
        geometry.update(client.canvas().scale());
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int node = 0; node < model.nodeCount(); node++) {
            int body = model.body(node);
            if (body < 0) continue;
            minX = Math.min(minX, geometry.x(body));
            minY = Math.min(minY, geometry.y(body));
            maxX = Math.max(maxX, geometry.x(body));
            maxY = Math.max(maxY, geometry.y(body));
        }
        if (minX > maxX) return null;
        var canvas = client.canvas();
        float width = Math.max(1, maxX - minX), height = Math.max(1, maxY - minY);
        float estimate = Math.max(0.01f, Math.min(canvas.viewportWidth() / width, canvas.viewportHeight() / height));
        return CanvasRect.of(minX - BOUNDS_X / estimate, minY - BOUNDS_Y / estimate, width + 2 * BOUNDS_X / estimate, height + 2 * BOUNDS_Y / estimate);
    }
}
