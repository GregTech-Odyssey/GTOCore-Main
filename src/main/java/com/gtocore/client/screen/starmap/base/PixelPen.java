package com.gtocore.client.screen.starmap.base;

import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIPixels;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.view.PlanarView;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 画布层里复用的屏幕像素画笔：begin 时把 pose 换回界面像素并记下视口，之后所有图形落在整数像素上；每层每帧 begin/end 一次，不分配对象。
 */
@OnlyIn(Dist.CLIENT)
public final class PixelPen {

    private static final int PLATE_ALPHA = 0x90;

    private final StarLabels labels = new StarLabels();
    private GuiGraphics graphics;
    private float scale = 1, viewX, viewY, offsetX, offsetY;
    private int unit = 1, density = 1, clipLeft, clipTop, clipRight, clipBottom;

    public PixelPen begin(GuiGraphics graphics, PlanarView canvas) {
        this.graphics = graphics;
        this.scale = canvas.scale();
        this.unit = StarGeometry.unit(scale);
        this.viewX = canvas.viewportX();
        this.viewY = canvas.viewportY();
        this.offsetX = canvas.offsetX();
        this.offsetY = canvas.offsetY();
        this.clipLeft = canvas.viewportX();
        this.clipTop = canvas.viewportY();
        this.clipRight = clipLeft + canvas.viewportWidth();
        this.clipBottom = clipTop + canvas.viewportHeight();
        this.density = Math.max(1, (int) Math.round(Minecraft.getInstance().getWindow().getGuiScale()));
        var pose = graphics.pose();
        pose.pushPose();
        pose.scale(1 / scale, 1 / scale, 1);
        pose.translate(-UIPixels.snap(viewX - offsetX * scale), -UIPixels.snap(viewY - offsetY * scale), 0);
        return this;
    }

    public void end() {
        graphics.pose().popPose();
    }

    public int density() {
        return density;
    }

    public void beginFine() {
        graphics.pose().pushPose();
        graphics.pose().scale(1f / density, 1f / density, 1);
    }

    public void endFine() {
        graphics.pose().popPose();
    }

    public int fineX(float worldX) {
        return Math.round((viewX + (worldX - offsetX) * scale) * density);
    }

    public int fineY(float worldY) {
        return Math.round((viewY + (worldY - offsetY) * scale) * density);
    }

    public void fineLine(int x0, int y0, int x1, int y1, int thickness, int color) {
        UIDraw.pixelLine(graphics, x0, y0, x1, y1, thickness, color, clipLeft * density, clipTop * density, clipRight * density, clipBottom * density);
    }

    public boolean fineVisible(int left, int top, int right, int bottom) {
        return right > clipLeft * density && left < clipRight * density && bottom > clipTop * density && top < clipBottom * density;
    }

    public GuiGraphics graphics() {
        return graphics;
    }

    public float scale() {
        return scale;
    }

    public int unit() {
        return unit;
    }

    public StarLabels labels() {
        return labels;
    }

    public int x(float worldX) {
        return Math.round(viewX + (worldX - offsetX) * scale);
    }

    public int y(float worldY) {
        return Math.round(viewY + (worldY - offsetY) * scale);
    }

    public int length(float world) {
        return Math.round(world * scale);
    }

    public int clipLeft() {
        return clipLeft;
    }

    public int clipTop() {
        return clipTop;
    }

    public int clipRight() {
        return clipRight;
    }

    public int clipBottom() {
        return clipBottom;
    }

    public boolean touches(int left, int top, int right, int bottom) {
        return right >= clipLeft && left < clipRight && bottom >= clipTop && top < clipBottom;
    }

    public void sprite(StarSprites.Sprite sprite, int centerX, int centerY, int size, int tint) {
        StarSprites.draw(graphics, sprite, centerX, centerY, size, tint);
    }

    public void marker(int index, int centerX, int centerY) {
        StarSprites.marker(graphics, index, centerX, centerY);
    }

    public void outline(int left, int top, int right, int bottom, int color) {
        UIDraw.strokeRect(graphics, left, top, right - left, bottom - top, color);
    }

    public void ellipse(int centerX, int centerY, float radius, float tilt, int color, int dotEvery) {
        PixelEllipse.draw(graphics, centerX, centerY, radius, tilt, color, dotEvery, clipLeft, clipTop, clipRight, clipBottom);
    }

    public void line(int x0, int y0, int x1, int y1, int thickness, int color) {
        UIDraw.pixelLine(graphics, x0, y0, x1, y1, thickness, color, clipLeft, clipTop, clipRight, clipBottom);
    }

    public int width(Component text) {
        return labels.width(text);
    }

    public void label(Component text, int centerX, int top, int color) {
        int width = labels.width(text), left = centerX - width / 2;
        graphics.fill(left - 2, top - 1, left + width + 1, top + 9, plate());
        graphics.drawString(UIText.font(), text, left, top, color, true);
    }

    public void sideLabel(Component text, int x, int top, boolean alignRight, int color) {
        int width = labels.width(text), left = alignRight ? x - width : x;
        graphics.fill(left - 2, top - 1, left + width + 1, top + 9, plate());
        graphics.drawString(UIText.font(), text, left, top, color, true);
    }

    public void left(Component text, int x, int top, int color) {
        graphics.drawString(UIText.font(), text, x, top, color, true);
    }

    public void text(Component text, int centerX, int top, int color) {
        graphics.drawString(UIText.font(), text, centerX - labels.width(text) / 2, top, color, true);
    }

    public static int plate() {
        return PLATE_ALPHA << 24 | UITheme.MAP_PLATE & 0xFFFFFF;
    }

    public static int mix(int from, int to, float t) {
        int r = (int) ((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
        int g = (int) ((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
        int b = (int) ((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
