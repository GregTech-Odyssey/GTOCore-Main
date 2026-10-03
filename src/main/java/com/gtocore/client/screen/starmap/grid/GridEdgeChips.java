package com.gtocore.client.screen.starmap.grid;

import com.gtocore.client.screen.starmap.base.PixelPen;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.common.wireless.energy.map.GridFormat;
import com.gtocore.common.wireless.energy.map.GridMapLang;

import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * 选中与当前天体不在可视区时，贴在可视区边缘的小标签：箭头指向真实方向、名字与储能百分比，点击后镜头移过去。
 */
@OnlyIn(Dist.CLIENT)
final class GridEdgeChips implements CanvasLayer {

    private static final Component[] ARROWS = { Component.literal("◀"), Component.literal("▶"), Component.literal("▲"), Component.literal("▼") };
    private static final int SLOTS = 2, PAD = 3, GAP = 3, HEIGHT = 12, MARGIN = 6, PUSHES = 4, CLEARANCE = 4;

    private final GridMapClient client;
    private final GridModel model;
    private final StarCatalog catalog;
    private final PixelPen pen;
    private final Chip[] chips = { new Chip(), new Chip() };
    private final int[] arrowWidth = new int[ARROWS.length];
    private int arrowGeneration = Integer.MIN_VALUE;

    GridEdgeChips(GridMapClient client) {
        this.client = client;
        this.model = client.model();
        this.catalog = client.catalog();
        this.pen = client.pen();
    }

    int edgeOf(@Nullable CanvasItem item) {
        for (var chip : chips) {
            if (item == chip) return chip.body;
        }
        return -1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
        chips[0].body = client.selected();
        int current = catalog.current();
        chips[1].body = current != chips[0].body && model.node(current) >= 0 ? current : -1;
        if (arrowGeneration != UIText.generation()) {
            arrowGeneration = UIText.generation();
            for (int i = 0; i < ARROWS.length; i++) arrowWidth[i] = UIText.width(ARROWS[i]);
        }
        pen.begin(painter.graphics(), client.canvas());
        for (int i = 0; i < SLOTS; i++) layout(chips[i], i == 0 ? UITheme.SELECTION_COLOR : UITheme.MAP_CURRENT);
        for (var chip : chips) {
            if (chip.shown) write(chip);
        }
        pen.end();
    }

    private void layout(Chip chip, int edge) {
        chip.shown = false;
        if (chip.body < 0 || client.tags().onScreen(chip.body)) return;
        chip.refresh();
        var area = client.obstructions();
        int x = pen.x(model.worldX(chip.body)), y = pen.y(model.worldY(chip.body));
        int cx = Math.max(area.left() + MARGIN, Math.min(x, area.right() - MARGIN)), cy = Math.max(area.top() + MARGIN, Math.min(y, area.bottom() - MARGIN));
        int dx = x - cx, dy = y - cy;
        chip.arrow = Math.abs(dx) >= Math.abs(dy) ? dx < 0 ? 0 : 1 : dy < 0 ? 2 : 3;
        chip.width = 2 * PAD + arrowWidth[chip.arrow] + GAP + chip.textWidth;
        chip.left = chip.arrow == 0 ? cx : chip.arrow == 1 ? cx - chip.width : cx - chip.width / 2;
        chip.top = chip.arrow == 2 ? cy : chip.arrow == 3 ? cy - HEIGHT : cy - HEIGHT / 2;
        chip.left = Math.max(area.left() + MARGIN, Math.min(chip.left, area.right() - MARGIN - chip.width));
        chip.top = Math.max(area.top() + MARGIN, Math.min(chip.top, area.bottom() - MARGIN - HEIGHT));
        push(chip, area, chip.arrow < 2);
        chip.shown = true;
        UIDraw.bevel(pen.graphics(), chip.left, chip.top, chip.width, HEIGHT, edge, edge, GridStyle.plate());
    }

    private void push(Chip chip, GridObstructions area, boolean vertical) {
        for (int i = 0; i < PUSHES; i++) {
            int k = area.blocker(chip.left - CLEARANCE, chip.top - CLEARANCE, chip.left + chip.width + CLEARANCE, chip.top + HEIGHT + CLEARANCE);
            if (k < 0) return;
            if (vertical) chip.top = area.rect(k, 3) + CLEARANCE;
            else chip.left = area.rect(k, 2) + CLEARANCE;
        }
    }

    private void write(Chip chip) {
        int x = chip.left + PAD, y = chip.top + 2;
        pen.left(ARROWS[chip.arrow], x, y, UITheme.MAP_LABEL);
        pen.left(chip.text, x + arrowWidth[chip.arrow] + GAP, y, UITheme.MAP_LABEL);
    }

    @Override
    @Nullable
    public CanvasItem pick(float x, float y) {
        var canvas = client.canvas();
        float sx = canvas.viewportX() + (x - canvas.offsetX()) * canvas.scale(), sy = canvas.viewportY() + (y - canvas.offsetY()) * canvas.scale();
        for (var chip : chips) {
            if (chip.shown && sx >= chip.left && sx < chip.left + chip.width && sy >= chip.top && sy < chip.top + HEIGHT) return chip;
        }
        return null;
    }

    private final class Chip implements CanvasItem {

        private int body = -1, shownBody = -1, stamp = Integer.MIN_VALUE, arrow, left, top, width, textWidth;
        private boolean shown;
        private Component text = Component.empty();

        private void refresh() {
            int key = model.stamp() * 31 + UIText.generation();
            if (body == shownBody && key == stamp) return;
            shownBody = body;
            stamp = key;
            int node = model.node(body);
            var name = GridFormat.bodyName(catalog.dimension(body)).copy();
            if (node >= 0 && !model.isLineOnly(node)) name.append(" ").append(Component.translatable(GridMapLang.PERCENT, Math.round(model.fill(node) * 100)));
            text = name;
            textWidth = UIText.width(text);
        }

        @Override
        public CanvasRect bounds() {
            return CanvasRect.of(0, 0, 0, 0);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawShape(CanvasPainter painter, boolean hovered) {}
    }
}
