package com.gtocore.client.screen.starmap;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIPixels;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * 星图的像素绘制：所有图形落在界面像素网格上（整数坐标、硬边、无渐隐），星球是运行时按整数倍重采样并描边的方块精灵。
 */
@OnlyIn(Dist.CLIENT)
final class StarMapDraw {

    static final ResourceLocation MARKERS = GTCEu.id("textures/gui/uipro/starfield/markers.png");
    static final int MARKER_LOCK = 0;
    static final int MARKER_STATION = 1;
    static final int MARKER_HERE = 2;
    static final int OUTLINE = 0xFF05070F;
    private static final Object2BooleanOpenHashMap<ResourceLocation> TEXTURE_EXISTS = new Object2BooleanOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<String, Sprite> SPRITES = new Object2ObjectOpenHashMap<>();

    private StarMapDraw() {}

    record Sprite(ResourceLocation id, int size) {}

    static ResourceLocation texture(ResourceLocation wanted, ResourceLocation fallback) {
        if (!TEXTURE_EXISTS.containsKey(wanted)) {
            TEXTURE_EXISTS.put(wanted, Minecraft.getInstance().getResourceManager().getResource(wanted).isPresent());
        }
        return TEXTURE_EXISTS.getBoolean(wanted) ? wanted : fallback;
    }

    static int withAlpha(int rgb, int alpha) {
        return Math.max(0, Math.min(255, alpha)) << 24 | rgb & 0xFFFFFF;
    }

    static int mix(int from, int to, float t) {
        int r = (int) ((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
        int g = (int) ((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    static Sprite planet(ResourceLocation source, int texels) {
        return sprite(source, texels, true);
    }

    static Sprite star(ResourceLocation source, int texels) {
        return sprite(source, texels, false);
    }

    private static Sprite sprite(ResourceLocation source, int texels, boolean outlined) {
        String key = source + "|" + texels + "|" + outlined;
        var sprite = SPRITES.get(key);
        if (sprite == null) {
            sprite = bake(key, source, texels, outlined);
            SPRITES.put(key, sprite);
        }
        return sprite;
    }

    private static Sprite bake(String key, ResourceLocation source, int n, boolean outlined) {
        int pad = outlined ? 1 : 0, size = n + 2 * pad;
        var out = new NativeImage(size, size, true);
        try (var stream = Minecraft.getInstance().getResourceManager().open(source); var in = NativeImage.read(stream)) {
            int outline = 0xFF000000 | (OUTLINE & 0xFF) << 16 | (OUTLINE >> 8 & 0xFF) << 8 | OUTLINE >> 16 & 0xFF;
            var solid = new boolean[size * size];
            for (int y = 0; y < n; y++) {
                for (int x = 0; x < n; x++) {
                    int color = in.getPixelRGBA(x * in.getWidth() / n, y * in.getHeight() / n);
                    if ((color >>> 24) == 0) continue;
                    solid[(y + pad) * size + x + pad] = true;
                    out.setPixelRGBA(x + pad, y + pad, color);
                }
            }
            if (outlined) {
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        if (solid[y * size + x]) continue;
                        if ((x > 0 && solid[y * size + x - 1]) || (x < size - 1 && solid[y * size + x + 1]) ||
                                (y > 0 && solid[(y - 1) * size + x]) || (y < size - 1 && solid[(y + 1) * size + x])) {
                            out.setPixelRGBA(x, y, outline);
                        }
                    }
                }
            }
        } catch (Exception e) {
            GTOCore.LOGGER.warn("Failed to bake star map sprite {}", source, e);
        }
        var id = GTOCore.id("starmap/sprite_" + Integer.toHexString(key.hashCode()));
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(out));
        return new Sprite(id, size);
    }

    static void sprite(GuiGraphics graphics, Sprite sprite, int centerX, int centerY, int size, float tint) {
        if (tint < 1) RenderSystem.setShaderColor(tint, tint, Math.min(1, tint * 1.1f), 1);
        RenderSystem.enableBlend();
        graphics.blit(sprite.id(), centerX - size / 2, centerY - size / 2, size, size, 0, 0, sprite.size(), sprite.size(), sprite.size(),
                sprite.size());
        if (tint < 1) RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    static void marker(GuiGraphics graphics, int index, int centerX, int centerY) {
        RenderSystem.enableBlend();
        graphics.blit(MARKERS, centerX - 8, centerY - 8, index * 16, 0, 16, 16, 48, 16);
    }

    static void ellipse(GuiGraphics graphics, int cx, int cy, float radius, float tilt, int color, int dotEvery, Clip clip) {
        int rx = Math.round(radius), ry = Math.round(radius * tilt);
        if (rx <= 0 || !clip.touches(cx - rx, cy - ry, cx + rx, cy + ry)) return;
        int steps = Math.max(16, (int) Math.ceil(2 * Math.PI * rx));
        int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE, count = 0;
        for (int i = 0; i < steps; i++) {
            double angle = 2 * Math.PI * i / steps;
            int x = cx + (int) Math.round(Math.cos(angle) * rx), y = cy + (int) Math.round(Math.sin(angle) * ry);
            if (x == lastX && y == lastY) continue;
            lastX = x;
            lastY = y;
            if (count++ % dotEvery == 0) dot(graphics, x, y, color, clip);
        }
    }

    private static void dot(GuiGraphics graphics, int x, int y, int color, Clip clip) {
        if (clip.contains(x, y)) graphics.fill(x, y, x + 1, y + 1, color);
    }

    static void outline(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        UIDraw.strokeRect(graphics, left, top, right - left, bottom - top, color);
    }

    static void label(GuiGraphics graphics, Component text, int centerX, int top, int color) {
        var font = Minecraft.getInstance().font;
        int width = font.width(text), left = centerX - width / 2;
        graphics.fill(left - 2, top - 1, left + width + 1, top + 9, 0x90050816);
        graphics.drawString(font, text, left, top, color, true);
    }

    static void sideLabel(GuiGraphics graphics, Component text, int x, int top, boolean alignRight, int color) {
        var font = Minecraft.getInstance().font;
        int width = font.width(text), left = alignRight ? x - width : x;
        graphics.fill(left - 2, top - 1, left + width + 1, top + 9, 0x90050816);
        graphics.drawString(font, text, left, top, color, true);
    }

    static void text(GuiGraphics graphics, Component text, int centerX, int top, int color) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, text, centerX - font.width(text) / 2, top, color, true);
    }

    record Clip(int left, int top, int right, int bottom) {

        boolean contains(int x, int y) {
            return x >= left && x < right && y >= top && y < bottom;
        }

        boolean touches(int x0, int y0, int x1, int y1) {
            return x1 >= left && x0 < right && y1 >= top && y0 < bottom;
        }
    }

    static final class Pen {

        final GuiGraphics graphics;
        final float scale;
        final int unit;
        final Clip clip;
        private final float viewX, viewY, offsetX, offsetY;

        Pen(GuiGraphics graphics, StarMapHost host) {
            this.graphics = graphics;
            var canvas = host.canvas();
            this.scale = canvas.scale();
            this.viewX = canvas.viewportX();
            this.viewY = canvas.viewportY();
            this.offsetX = canvas.offsetX();
            this.offsetY = canvas.offsetY();
            this.unit = Math.max(1, Math.round(scale));
            this.clip = new Clip(canvas.viewportX(), canvas.viewportY(), canvas.viewportX() + canvas.viewportWidth(),
                    canvas.viewportY() + canvas.viewportHeight());
            var pose = graphics.pose();
            pose.pushPose();
            pose.scale(1 / scale, 1 / scale, 1);
            pose.translate(-UIPixels.snap(viewX - offsetX * scale), -UIPixels.snap(viewY - offsetY * scale), 0);
        }

        int x(float worldX) {
            return Math.round(viewX + (worldX - offsetX) * scale);
        }

        int y(float worldY) {
            return Math.round(viewY + (worldY - offsetY) * scale);
        }

        int length(float world) {
            return Math.round(world * scale);
        }

        void close() {
            graphics.pose().popPose();
        }
    }
}
