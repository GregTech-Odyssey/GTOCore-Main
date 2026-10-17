package com.gtocore.client.screen.starmap.base;

import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLod;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.view.PlanarView;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.ints.IntArrays;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 星球、卫星、锚点与异界维度合在一层：位置读 {@link StarGeometry} 的数组，项对象预先建好，绘制与命中测试不分配对象。
 */
@OnlyIn(Dist.CLIENT)
public final class BodyLayer implements CanvasLayer {

    private static final int WHITE = 0xFFFFFFFF;

    private final StarGeometry geometry;
    private final StarCatalog catalog;
    private final BodyDecorator decorator;
    private final PixelPen pen;
    private final PlanarView canvas;
    private final float[] box = new float[4];
    private Handle[] bodies = new Handle[0];
    private Handle[] anchors = new Handle[0];
    private int[] order = new int[0];
    private int revision = -1;

    public BodyLayer(StarGeometry geometry, BodyDecorator decorator, PixelPen pen, PlanarView canvas) {
        this.geometry = geometry;
        this.catalog = geometry.catalog();
        this.decorator = decorator;
        this.pen = pen;
        this.canvas = canvas;
    }

    public int bodyOf(@Nullable CanvasItem item) {
        return item instanceof Handle handle && handle.layer() == this && !handle.anchor ? handle.index : -1;
    }

    public int anchorOf(@Nullable CanvasItem item) {
        return item instanceof Handle handle && handle.layer() == this && handle.anchor ? handle.index : -1;
    }

    @Nullable
    public CanvasItem item(int body) {
        ensure();
        return body < 0 || body >= bodies.length ? null : bodies[body];
    }

    public int selectedBody() {
        for (int i = 0; i < catalog.size(); i++) {
            if (decorator.isSelected(i)) return i;
        }
        return -1;
    }

    private void ensure() {
        if (revision == catalog.revision() && bodies.length == catalog.size()) return;
        revision = catalog.revision();
        int old = bodies.length;
        bodies = Arrays.copyOf(bodies, catalog.size());
        for (int i = old; i < bodies.length; i++) bodies[i] = new Handle(i, false);
        if (anchors.length != catalog.anchorCount()) {
            anchors = new Handle[catalog.anchorCount()];
            for (int a = 0; a < anchors.length; a++) anchors[a] = new Handle(a, true);
        }
        if (order.length != catalog.bodyCount()) {
            geometry.update(1);
            var sorted = new int[catalog.bodyCount()];
            for (int i = 0; i < sorted.length; i++) sorted[i] = i;
            IntArrays.mergeSort(sorted, (a, b) -> Float.compare(geometry.y(a), geometry.y(b)));
            order = sorted;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
        ensure();
        geometry.update(painter.scale());
        if (painter.lod() == CanvasLod.BLOCK) {
            drawMinimap(painter);
            return;
        }
        painter.flush();
        pen.begin(painter.graphics(), canvas);
        int selected = selectedBody();
        int focusedParent = catalog.isMoon(selected) ? catalog.parent(selected) : -1;
        for (int i : order) {
            geometry.bounds(i, box);
            if (painter.isVisible(box[0], box[1], box[2], box[3])) drawBody(i, hovered == bodies[i], selected == i, focusedParent);
        }
        for (int a = 0; a < anchors.length; a++) {
            geometry.anchorBounds(a, box);
            if (painter.isVisible(box[0], box[1], box[2], box[3])) drawAnchor(a, hovered == anchors[a]);
        }
        for (int i = catalog.bodyCount(); i < bodies.length; i++) {
            geometry.bounds(i, box);
            if (painter.isVisible(box[0], box[1], box[2], box[3])) drawRealm(i, hovered == bodies[i], selected == i);
        }
        pen.end();
    }

    private void drawBody(int i, boolean hovered, boolean selected, int focusedParent) {
        if (geometry.isFolded(i)) return;
        int x = pen.x(geometry.x(i)), y = pen.y(geometry.y(i)), size = geometry.pixels(i), half = size / 2;
        boolean current = catalog.current() == i;
        drawSprite(i, x, y, size, hovered, selected, current);
        float scale = pen.scale();
        boolean moon = catalog.isMoon(i);
        boolean named = moon ? hovered || selected || current || scale >= StarGeometry.MOON_LABEL_SCALE :
                current || (scale >= StarGeometry.LABEL_MIN_SCALE || hovered || selected) && (hovered || i != focusedParent);
        if (!named || !decorator.showsLabel(i)) return;
        int color = labelColor(i, current, hovered || selected);
        var name = catalog.name(i);
        if (moon) {
            boolean left = geometry.x(i) < geometry.parentX(i);
            pen.sideLabel(name, left ? x - half - 3 : x + half + 3, y - 4, left, color);
            return;
        }
        int below = y + half + 4, system = catalog.system(i);
        int starHalf = geometry.starPixels() / 2 + 1;
        int starX = pen.x(catalog.systemX(system)), starY = pen.y(catalog.systemY(system));
        int labelHalf = pen.width(name) / 2 + 2;
        boolean blocked = below - 1 < starY + starHalf && below + 9 > starY - starHalf && x - labelHalf < starX + starHalf && x + labelHalf > starX - starHalf;
        pen.label(name, x, blocked ? y - half - 12 : below, color);
    }

    private void drawRealm(int i, boolean hovered, boolean selected) {
        int x = pen.x(geometry.x(i)), y = pen.y(geometry.y(i)), size = geometry.pixels(i), half = size / 2;
        boolean current = catalog.current() == i;
        drawSprite(i, x, y, size, hovered, selected, current);
        if ((pen.scale() >= StarGeometry.LABEL_MIN_SCALE || hovered || selected || current) && decorator.showsLabel(i)) {
            pen.label(catalog.name(i), x, y + half + 4, labelColor(i, current, hovered || selected));
        }
    }

    private void drawSprite(int i, int x, int y, int size, boolean hovered, boolean selected, boolean current) {
        int half = size / 2;
        if (!catalog.isStellar(i)) pen.sprite(StarSprites.body(catalog, i), x, y, size, decorator.tint(i));
        if (selected || hovered) pen.outline(x - half - 2, y - half - 2, x + half + 2, y + half + 2, selected ? WHITE : UITheme.MAP_HOVER_FRAME);
        decorator.drawAfter(pen.graphics(), i, x, y, size, hovered);
        if (current && decorator.showsHereMarker(i)) pen.marker(StarSprites.MARKER_HERE, x, y - half - 8);
    }

    private void drawAnchor(int a, boolean hovered) {
        int x = pen.x(geometry.anchorX(a)), y = pen.y(geometry.anchorY(a)), size = geometry.anchorPixels(), half = size / 2;
        pen.sprite(StarSprites.anchor(catalog, a), x, y, size, decorator.anchorTint(a));
        if (hovered) pen.outline(x - half - 2, y - half - 2, x + half + 2, y + half + 2, UITheme.MAP_HOVER_FRAME);
        if (pen.scale() >= StarGeometry.LABEL_MIN_SCALE || hovered) pen.label(catalog.anchorName(a), x, y + half + 4, decorator.anchorLabelColor(a, UITheme.MAP_LABEL));
    }

    private int labelColor(int i, boolean current, boolean lit) {
        return current ? UITheme.MAP_CURRENT : lit ? WHITE : decorator.labelColor(i, UITheme.MAP_LABEL);
    }

    @Override
    @Nullable
    public CanvasItem pick(float x, float y) {
        ensure();
        geometry.update(canvas.scale());
        for (int i = bodies.length - 1; i >= catalog.bodyCount(); i--) {
            geometry.bounds(i, box);
            if (hit(x, y)) return bodies[i];
        }
        for (int a = anchors.length - 1; a >= 0; a--) {
            geometry.anchorBounds(a, box);
            if (hit(x, y)) return anchors[a];
        }
        for (int k = order.length - 1; k >= 0; k--) {
            geometry.bounds(order[k], box);
            if (hit(x, y)) return bodies[order[k]];
        }
        return null;
    }

    private boolean hit(float x, float y) {
        return x >= box[0] && x < box[0] + box[2] && y >= box[1] && y < box[1] + box[3];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawMinimap(CanvasPainter painter) {
        ensure();
        for (int i = 0; i < bodies.length; i++) {
            geometry.bounds(i, box);
            painter.fill(box[0], box[1], box[0] + box[2], box[1] + box[3], bodies[i].blockColor());
        }
        for (int a = 0; a < anchors.length; a++) {
            geometry.anchorBounds(a, box);
            painter.fill(box[0], box[1], box[0] + box[2], box[1] + box[3], anchors[a].blockColor());
        }
    }

    private final class Handle implements CanvasItem {

        private final int index;
        private final boolean anchor;

        private Handle(int index, boolean anchor) {
            this.index = index;
            this.anchor = anchor;
        }

        private BodyLayer layer() {
            return BodyLayer.this;
        }

        @Override
        public CanvasRect bounds() {
            return anchor ? geometry.anchorRect(index) : geometry.rect(index);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawShape(CanvasPainter painter, boolean hovered) {}

        @Override
        public int blockColor() {
            int base = anchor ? catalog.systemColor(catalog.anchorSystem(index)) :
                    catalog.isRealm(index) ? UITheme.MAP_REALM : catalog.systemColor(catalog.system(index));
            return PixelPen.mix(base, WHITE, 0.4f);
        }

        @Override
        public List<Component> tooltip() {
            return anchor ? decorator.anchorTooltip(index) : decorator.tooltip(index);
        }
    }
}
