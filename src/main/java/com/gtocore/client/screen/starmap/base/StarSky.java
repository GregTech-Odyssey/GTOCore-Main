package com.gtocore.client.screen.starmap.base;

import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.view.PlanarView;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Random;

/**
 * 星图背景：竖向渐变底色、低透明度的末地天空纹理与按深度视差平移的方块星点，随画布平移累计偏移。
 */
@OnlyIn(Dist.CLIENT)
public final class StarSky {

    private static final int STAR_COUNT = 520;
    private static final int SKY_TEXEL = 2;
    private static final int STAR_NEAR = 0xFFFFFFFF;
    private static final int STAR_MID = 0xFF8890B0;
    private static final int STAR_FAR = 0xFF4A5070;
    private static final ResourceLocation END_SKY = ResourceLocation.parse("textures/environment/end_sky.png");

    private final float[] stars = new float[STAR_COUNT * 4];
    private float panX, panY, lastOffsetX = Float.NaN, lastOffsetY, lastScale;

    public StarSky() {
        var random = new Random(0x5EED);
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i * 4] = random.nextFloat();
            stars[i * 4 + 1] = random.nextFloat();
            stars[i * 4 + 2] = random.nextFloat();
            stars[i * 4 + 3] = random.nextFloat() * 6.2832f;
        }
    }

    public void draw(GuiGraphics graphics, int x, int y, int w, int h, PlanarView canvas) {
        graphics.fillGradient(x, y, x + w, y + h, UITheme.MAP_SKY_TOP, UITheme.MAP_SKY_BOTTOM);
        track(canvas);
        int shiftX = Math.round(panX * 0.03f / SKY_TEXEL), shiftY = Math.round(panY * 0.03f / SKY_TEXEL);
        int texelsX = (w + SKY_TEXEL - 1) / SKY_TEXEL, texelsY = (h + SKY_TEXEL - 1) / SKY_TEXEL;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(0.3f, 0.34f, 0.6f, 0.1f);
        graphics.blit(END_SKY, x, y, texelsX * SKY_TEXEL, texelsY * SKY_TEXEL, shiftX, shiftY, texelsX, texelsY, 128, 128);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        drawStars(graphics, x, y, w, h);
    }

    private void track(PlanarView canvas) {
        float scale = canvas.scale(), offsetX = canvas.offsetX(), offsetY = canvas.offsetY();
        if (!Float.isNaN(lastOffsetX) && scale == lastScale) {
            panX += (offsetX - lastOffsetX) * scale;
            panY += (offsetY - lastOffsetY) * scale;
        }
        lastOffsetX = offsetX;
        lastOffsetY = offsetY;
        lastScale = scale;
    }

    private void drawStars(GuiGraphics graphics, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        int count = Math.min(STAR_COUNT, Math.max(120, w * h / 900));
        var consumer = graphics.bufferSource().getBuffer(RenderType.gui());
        var matrix = graphics.pose().last().pose();
        for (int i = 0; i < count; i++) {
            float depth = stars[i * 4 + 2];
            float parallax = 0.02f + 0.1f * depth;
            int sx = Math.floorMod(Math.round(stars[i * 4] * w - panX * parallax), w);
            int sy = Math.floorMod(Math.round(stars[i * 4 + 1] * h - panY * parallax), h);
            int color = depth > 0.75f ? STAR_NEAR : depth > 0.4f ? STAR_MID : STAR_FAR;
            PixelEllipse.quad(consumer, matrix, x + sx, y + sy, x + sx + 1, y + sy + 1, color);
        }
        graphics.flush();
    }
}
