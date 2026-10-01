package com.gtocore.client.screen.starmap;

import com.gtolib.api.misc.PlanetManagement;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.canvas.ItemLayer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
final class StarMapScene {

    static final int LABEL = 0xFFE0E0E0;
    static final int LABEL_DIM = 0xFFA0A0A0;
    static final int CURRENT = 0xFF55FFFF;
    static final int ORBIT = 0xFF2C3354;
    static final int ORBIT_FAR = 0xFF1E2238;
    static final float FOLD_SCALE = 0.65f;
    static final int PLANET_TEXELS = 16;
    static final int SATELLITE_TEXELS = 8;
    static final int ANCHOR_TEXELS = 16;
    static final int STAR_TEXELS = 24;
    private static final float LABEL_MIN_SCALE = 0.6f;
    private static final float MOON_LABEL_SCALE = 1.4f;
    private static final ResourceLocation FALLBACK_STAR = RLUtils.ad("textures/environment/red_sun.png");

    private StarMapScene() {}

    static void build(CanvasView view, StarMapHost host) {
        var model = host.model();
        view.addLayer(new SystemLayer(model, host));
        var bodies = new ItemLayer<BodyItem>();
        var ordered = new ArrayList<>(model.bodies);
        ordered.sort((a, b) -> Float.compare(y(a, 1), y(b, 1)));
        for (var body : ordered) bodies.add(new BodyItem(body, host));
        view.addLayer(bodies);
        var anchors = new ItemLayer<AnchorItem>();
        for (var system : model.systems) {
            for (var anchor : system.anchors) anchors.add(new AnchorItem(system, anchor, host));
        }
        view.addLayer(anchors);
    }

    static boolean unlocked(StarMapModel.Body body) {
        return GTCEu.isDev() || PlanetManagement.isClientUnlocked(body.dimensionKey());
    }

    static int color(StarMapModel.SystemInfo system) {
        return system.galaxy != null ? system.galaxy.getColor() : 0xFFFFD27A;
    }

    static int unit(float scale) {
        return Math.max(1, (int) Math.floor(scale + 0.1f));
    }

    static int texels(StarMapModel.Body body) {
        return body.satellite() ? SATELLITE_TEXELS : PLANET_TEXELS;
    }

    static StarMapDraw.Sprite sprite(StarMapModel.Body body) {
        return StarMapDraw.planet(StarMapDraw.texture(body.icon(), StarMapModel.fallbackIcon()), texels(body));
    }

    static float ringRadius(StarMapModel.SystemInfo system, int order) {
        return system.ringRadius(system.rings.getInt(order));
    }

    static int pixels(StarMapDraw.Sprite sprite, float scale) {
        return scale < FOLD_SCALE ? (sprite.size() + 1) / 2 : sprite.size() * unit(scale);
    }

    static boolean folded(StarMapModel.Body body, float scale) {
        return body.satellite() && scale < FOLD_SCALE;
    }

    private static boolean hasAnchor(StarMapModel.SystemInfo system, int order) {
        for (var anchor : system.anchors) {
            if (anchor.order() == order) return true;
        }
        return false;
    }

    static float moonDistance(StarMapModel.Body body, float scale) {
        int u = unit(scale);
        int parent = ((hasAnchor(body.system(), body.order()) ? ANCHOR_TEXELS : PLANET_TEXELS) + 2) * u;
        int moon = (SATELLITE_TEXELS + 2) * u;
        float spacing = parent / 2f + moon / 2f + 2 + body.moon() * (moon + 2);
        float axis = Math.max(Math.abs((float) Math.cos(body.angle())), Math.abs((float) Math.sin(body.angle())) * StarMapModel.TILT);
        return Math.max(body.orbitRadius(), spacing / Math.max(0.2f, axis) / scale);
    }

    static float x(StarMapModel.Body body, float scale) {
        if (!body.satellite()) return body.system().cx + (float) Math.cos(body.angle()) * ringRadius(body.system(), body.order());
        return parentX(body, scale) + (float) Math.cos(body.angle()) * moonDistance(body, scale);
    }

    static float y(StarMapModel.Body body, float scale) {
        if (!body.satellite()) return body.system().cy + (float) Math.sin(body.angle()) * ringRadius(body.system(), body.order()) * StarMapModel.TILT;
        return parentY(body, scale) + (float) Math.sin(body.angle()) * moonDistance(body, scale) * StarMapModel.TILT;
    }

    static float parentX(StarMapModel.Body body, float scale) {
        return body.system().cx + (float) Math.cos(body.parentAngle()) * ringRadius(body.system(), body.order());
    }

    static float parentY(StarMapModel.Body body, float scale) {
        return body.system().cy + (float) Math.sin(body.parentAngle()) * ringRadius(body.system(), body.order()) * StarMapModel.TILT;
    }

    static CanvasRect bounds(StarMapModel.Body body, float scale) {
        if (folded(body, scale)) return CanvasRect.of(x(body, scale), y(body, scale), 0, 0);
        float size = (pixels(sprite(body), scale) + 6) / scale;
        return CanvasRect.of(x(body, scale) - size / 2, y(body, scale) - size / 2, size, size);
    }

    private record SystemLayer(StarMapModel model, StarMapHost host) implements CanvasLayer {

        @Override
        public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
            painter.flush();
            var graphics = painter.graphics();
            var pen = new StarMapDraw.Pen(graphics, host);
            float scale = pen.scale;
            var hoveredBody = hovered instanceof BodyItem item ? item.body : null;
            var selectedBody = host.selected();
            var font = Minecraft.getInstance().font;
            for (var system : model.systems) {
                if (!painter.isVisible(system.bounds().inflate(60 / scale))) continue;
                int color = color(system), cx = pen.x(system.cx), cy = pen.y(system.cy);
                for (int order = 0; order < system.rings.size(); order++) {
                    boolean selected = litRing(selectedBody, system, order), lit = selected || litRing(hoveredBody, system, order);
                    int orbit = selected ? color : lit ? StarMapDraw.mix(ORBIT, color, 0.5f) : scale < FOLD_SCALE ? ORBIT_FAR : ORBIT;
                    StarMapDraw.ellipse(graphics, cx, cy, ringRadius(system, order) * scale, StarMapModel.TILT, orbit, lit ? 2 : 3, pen.clip);
                }
                var star = StarMapDraw.star(StarMapDraw.texture(system.star, FALLBACK_STAR), STAR_TEXELS);
                StarMapDraw.sprite(graphics, star, cx, cy, pixels(star, scale), 1);
                int top = cy - Math.round(system.radius * StarMapModel.TILT * scale) - 22;
                StarMapDraw.text(graphics, system.name, cx, top, color);
                if (scale >= FOLD_SCALE) {
                    StarMapDraw.text(graphics, Component.translatable(StarMapLang.REACHABLE, model.reachableCount(system), system.bodies.size()), cx, top + 11,
                            LABEL_DIM);
                }
            }
            pen.close();
        }

        private static boolean litRing(@Nullable StarMapModel.Body body, StarMapModel.SystemInfo system, int order) {
            return body != null && !body.satellite() && body.system() == system && body.order() == order;
        }

        @Override
        public CanvasRect bounds() {
            CanvasRect bounds = null;
            for (var system : model.systems) bounds = CanvasRect.union(bounds, system.bounds().inflate(30));
            return bounds;
        }

        @Override
        public void drawMinimap(CanvasPainter painter) {
            for (var system : model.systems) {
                float half = painter.px(3);
                painter.fill(CanvasRect.of(system.cx - half, system.cy - half, 2 * half, 2 * half), color(system));
            }
        }
    }

    static float anchorX(StarMapModel.SystemInfo system, StarMapModel.Anchor anchor) {
        return system.cx + (float) Math.cos(anchor.angle()) * ringRadius(system, anchor.order());
    }

    static float anchorY(StarMapModel.SystemInfo system, StarMapModel.Anchor anchor) {
        return system.cy + (float) Math.sin(anchor.angle()) * ringRadius(system, anchor.order()) * StarMapModel.TILT;
    }

    static StarMapDraw.Sprite anchorSprite(StarMapModel.Anchor anchor) {
        return StarMapDraw.planet(StarMapDraw.texture(anchor.icon(), StarMapModel.fallbackIcon()), ANCHOR_TEXELS);
    }

    static final class AnchorItem implements CanvasItem {

        private final StarMapModel.SystemInfo system;
        private final StarMapModel.Anchor anchor;
        private final StarMapHost host;

        AnchorItem(StarMapModel.SystemInfo system, StarMapModel.Anchor anchor, StarMapHost host) {
            this.system = system;
            this.anchor = anchor;
            this.host = host;
        }

        @Override
        public CanvasRect bounds() {
            float scale = Math.max(0.01f, host.viewScale());
            float size = (pixels(anchorSprite(anchor), scale) + 6) / scale;
            return CanvasRect.of(anchorX(system, anchor) - size / 2, anchorY(system, anchor) - size / 2, size, size);
        }

        @Override
        public void drawShape(CanvasPainter painter, boolean hovered) {}

        @Override
        public void drawContent(CanvasPainter painter, boolean hovered) {
            var pen = new StarMapDraw.Pen(painter.graphics(), host);
            var sprite = anchorSprite(anchor);
            int x = pen.x(anchorX(system, anchor)), y = pen.y(anchorY(system, anchor)), size = pixels(sprite, pen.scale);
            StarMapDraw.sprite(pen.graphics, sprite, x, y, size, 1);
            if (hovered) StarMapDraw.outline(pen.graphics, x - size / 2 - 2, y - size / 2 - 2, x + size / 2 + 2, y + size / 2 + 2, 0xA0FFFFFF);
            if (pen.scale >= LABEL_MIN_SCALE || hovered) StarMapDraw.label(pen.graphics, anchor.name(), x, y + size / 2 + 4, LABEL_DIM);
            pen.close();
        }

        @Override
        public int blockColor() {
            return StarMapDraw.mix(color(system), 0xFFFFFFFF, 0.4f);
        }

        @Override
        public List<Component> tooltip() {
            return List.of(anchor.name().copy().withStyle(ChatFormatting.WHITE),
                    Component.translatable(StarMapLang.MOONS_ONLY).withStyle(ChatFormatting.GRAY));
        }
    }

    static final class BodyItem implements CanvasItem {

        private final StarMapModel.Body body;
        private final StarMapHost host;

        BodyItem(StarMapModel.Body body, StarMapHost host) {
            this.body = body;
            this.host = host;
        }

        StarMapModel.Body body() {
            return body;
        }

        @Override
        public CanvasRect bounds() {
            return StarMapScene.bounds(body, Math.max(0.01f, host.viewScale()));
        }

        @Override
        public void drawShape(CanvasPainter painter, boolean hovered) {}

        @Override
        public void drawContent(CanvasPainter painter, boolean hovered) {
            var graphics = painter.graphics();
            var pen = new StarMapDraw.Pen(graphics, host);
            boolean selected = host.selected() == body;
            boolean current = host.model().current == body;
            boolean unlocked = unlocked(body);
            boolean reachable = host.model().reachable(body);
            if (folded(body, pen.scale)) {
                pen.close();
                return;
            }
            int x = pen.x(x(body, pen.scale)), y = pen.y(y(body, pen.scale));
            var sprite = sprite(body);
            int size = pixels(sprite, pen.scale), half = size / 2;
            StarMapDraw.sprite(graphics, sprite, x, y, size, unlocked && reachable ? 1 : 0.45f);
            if (selected || hovered) {
                StarMapDraw.outline(graphics, x - half - 2, y - half - 2, x + half + 2, y + half + 2, selected ? 0xFFFFFFFF : 0xA0FFFFFF);
            }
            if (!unlocked) StarMapDraw.marker(graphics, StarMapDraw.MARKER_LOCK, x + half, y - half);
            if (body.stations() > 0) StarMapDraw.marker(graphics, StarMapDraw.MARKER_STATION, x - half - 2, y - half);
            if (current) StarMapDraw.marker(graphics, StarMapDraw.MARKER_HERE, x, y - half - 8);
            boolean named = body.satellite() ? hovered || selected || current || pen.scale >= MOON_LABEL_SCALE :
                    current || (pen.scale >= LABEL_MIN_SCALE || hovered || selected) && !satelliteFocused(hovered);
            if (named) {
                int labelColor = current ? CURRENT : hovered || selected ? 0xFFFFFFFF : unlocked && reachable ? LABEL : LABEL_DIM;
                if (body.satellite()) {
                    boolean left = x(body, pen.scale) < parentX(body, pen.scale);
                    StarMapDraw.sideLabel(graphics, body.name(), left ? x - half - 3 : x + half + 3, y - 4, left, labelColor);
                } else {
                    int below = y + half + 4;
                    int starHalf = pixels(StarMapDraw.star(StarMapDraw.texture(body.system().star, FALLBACK_STAR), STAR_TEXELS), pen.scale) / 2 + 1;
                    int starX = pen.x(body.system().cx), starY = pen.y(body.system().cy);
                    int labelHalf = Minecraft.getInstance().font.width(body.name()) / 2 + 2;
                    boolean blocked = below - 1 < starY + starHalf && below + 9 > starY - starHalf && x - labelHalf < starX + starHalf &&
                            x + labelHalf > starX - starHalf;
                    StarMapDraw.label(graphics, body.name(), x, blocked ? y - half - 12 : below, labelColor);
                }
            }
            pen.close();
        }

        private boolean satelliteFocused(boolean hovered) {
            if (hovered) return false;
            var selected = host.selected();
            return selected != null && selected.satellite() && selected.system() == body.system() && selected.order() == body.order() &&
                    selected.parentAngle() == body.angle();
        }

        @Override
        public int blockColor() {
            return StarMapDraw.mix(color(body.system()), 0xFFFFFFFF, 0.4f);
        }

        @Override
        public List<Component> tooltip() {
            if (host.selected() == body) return Collections.emptyList();
            var lines = new ArrayList<Component>(5);
            lines.add(body.name().copy().withStyle(ChatFormatting.WHITE));
            if (host.model().current == body) lines.add(Component.translatable(StarMapLang.CURRENT).withStyle(ChatFormatting.GREEN));
            boolean reachable = host.model().reachable(body);
            lines.add(Component.translatable(StarMapLang.REQUIRED_ROCKET, body.requiredTier())
                    .withStyle(reachable ? ChatFormatting.GRAY : ChatFormatting.RED));
            if (!reachable) lines.add(Component.translatable(StarMapLang.CURRENT_ROCKET, host.model().rocketTier).withStyle(ChatFormatting.RED));
            lines.add(Component.translatable(unlocked(body) ? StarMapLang.UNLOCKED : StarMapLang.LOCKED)
                    .withStyle(unlocked(body) ? ChatFormatting.GREEN : ChatFormatting.RED));
            if (host.selected() == null) lines.add(Component.translatable(StarMapLang.CLICK_FOR_DETAILS).withStyle(ChatFormatting.GRAY));
            return lines;
        }
    }
}
