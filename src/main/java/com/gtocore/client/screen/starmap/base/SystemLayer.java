package com.gtocore.client.screen.starmap.base;

import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.view.PlanarView;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * 星系层：轨道点线椭圆、恒星、星系名与副标题；悬停或选中星球时点亮它所在的轨道。
 */
@OnlyIn(Dist.CLIENT)
public final class SystemLayer implements CanvasLayer {

    private static final float CULL_MARGIN = 60;
    private static final float BOUNDS_MARGIN = 30;
    private static final float MINIMAP_HALF = 3;

    private final StarGeometry geometry;
    private final StarCatalog catalog;
    private final BodyDecorator decorator;
    private final BodyLayer bodies;
    private final PixelPen pen;
    private final PlanarView canvas;
    private final float[] box = new float[4];

    public SystemLayer(StarGeometry geometry, BodyDecorator decorator, BodyLayer bodies, PixelPen pen, PlanarView canvas) {
        this.geometry = geometry;
        this.catalog = geometry.catalog();
        this.decorator = decorator;
        this.bodies = bodies;
        this.pen = pen;
        this.canvas = canvas;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
        painter.flush();
        geometry.update(painter.scale());
        pen.begin(painter.graphics(), canvas);
        float scale = pen.scale();
        boolean lights = decorator.litOrbits();
        int hoveredBody = lights ? bodies.bodyOf(hovered) : -1, selectedBody = lights ? bodies.selectedBody() : -1;
        float margin = CULL_MARGIN / scale;
        for (int s = 0; s < catalog.systemCount(); s++) {
            catalog.systemBounds(s, box);
            if (!painter.isVisible(box[0] - margin, box[1] - margin, box[2] + 2 * margin, box[3] + 2 * margin)) continue;
            int color = catalog.systemColor(s), cx = pen.x(catalog.systemX(s)), cy = pen.y(catalog.systemY(s));
            for (int order = 0; order < catalog.ringCount(s); order++) {
                boolean selected = litRing(selectedBody, s, order), lit = selected || litRing(hoveredBody, s, order);
                int orbit = selected ? color : lit ? PixelPen.mix(UITheme.MAP_ORBIT, color, 0.5f) :
                        decorator.orbitColor(s, order, scale < StarGeometry.FOLD_SCALE ? UITheme.MAP_ORBIT_FAR : UITheme.MAP_ORBIT);
                pen.ellipse(cx, cy, catalog.ringRadius(s, order) * scale, StarGeometry.TILT, orbit, lit ? 2 : 3);
            }
            pen.sprite(StarSprites.system(catalog, s), cx, cy, geometry.starPixels(), StarSprites.NO_TINT);
            int top = cy - Math.round(catalog.systemRadius(s) * StarGeometry.TILT * scale) - 22;
            pen.text(catalog.systemName(s), cx, top, color);
            var subtitle = scale >= StarGeometry.FOLD_SCALE ? decorator.systemSubtitle(s) : null;
            if (subtitle != null) pen.text(subtitle, cx, top + 11, UITheme.MAP_LABEL_DIM);
        }
        pen.end();
    }

    private boolean litRing(int body, int system, int order) {
        return body >= 0 && body < catalog.bodyCount() && !catalog.isMoon(body) && catalog.system(body) == system && catalog.order(body) == order;
    }

    @Override
    @Nullable
    public CanvasRect bounds() {
        CanvasRect bounds = null;
        for (int s = 0; s < catalog.systemCount(); s++) {
            catalog.systemBounds(s, box);
            bounds = CanvasRect.union(bounds, CanvasRect.of(box[0], box[1], box[2], box[3]).inflate(BOUNDS_MARGIN));
        }
        if (catalog.realmBounds(box)) bounds = CanvasRect.union(bounds, CanvasRect.of(box[0], box[1], box[2], box[3]).inflate(BOUNDS_MARGIN));
        return bounds;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawMinimap(CanvasPainter painter) {
        float half = painter.px(MINIMAP_HALF);
        for (int s = 0; s < catalog.systemCount(); s++) {
            float x = catalog.systemX(s), y = catalog.systemY(s);
            painter.fill(x - half, y - half, x + half, y + half, catalog.systemColor(s));
        }
    }
}
