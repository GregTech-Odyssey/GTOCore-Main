package com.gtocore.integration.xaero;

import com.gtolib.monitor.MonitorHudEditorFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Captures Xaero's complete native draw pass without leaving its original HUD on the editor's game background. */
final class XaeroMinimapPreview {

    @Nullable
    private static Screen owner;
    @Nullable
    private static TextureTarget background;
    @Nullable
    private static TextureTarget preview;
    private static boolean capturing;
    private static boolean ready;
    private static float left, top, right, bottom;

    private XaeroMinimapPreview() {}

    static void install() {
        MinecraftForge.EVENT_BUS.addListener(XaeroMinimapPreview::closed);
    }

    static boolean editing() {
        return Minecraft.getInstance().screen instanceof ModularUIGuiContainer screen && screen.modularUI.holder instanceof MonitorHudEditorFactory.Target;
    }

    static boolean capturing() {
        return capturing;
    }

    static void begin(GuiGraphics graphics, Rect2i rect, int width, int height) {
        graphics.flush();
        var source = Minecraft.getInstance().getMainRenderTarget();
        if (background == null) {
            background = new TextureTarget(source.width, source.height, false, Minecraft.ON_OSX);
            preview = new TextureTarget(source.width, source.height, false, Minecraft.ON_OSX);
        } else if (background.width != source.width || background.height != source.height) {
            background.resize(source.width, source.height, Minecraft.ON_OSX);
            preview.resize(source.width, source.height, Minecraft.ON_OSX);
        }
        owner = Minecraft.getInstance().screen;
        left = rect.getX() / (float) width;
        right = (rect.getX() + rect.getWidth()) / (float) width;
        top = 1 - rect.getY() / (float) height;
        bottom = 1 - (rect.getY() + rect.getHeight()) / (float) height;
        copy(source, background);
        source.bindWrite(true);
        GlStateManager._clearColor(0, 0, 0, 0);
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
        capturing = true;
    }

    static void end(GuiGraphics graphics) {
        if (!capturing) return;
        graphics.flush();
        var source = Minecraft.getInstance().getMainRenderTarget();
        copy(source, preview);
        copy(background, source);
        source.bindWrite(true);
        capturing = false;
        ready = true;
    }

    private static void copy(RenderTarget source, RenderTarget destination) {
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, source.frameBufferId);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, destination.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, source.width, source.height, 0, 0, destination.width, destination.height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
    }

    static boolean draw(GuiGraphics graphics, Rect2i rect) {
        if (!ready || owner != Minecraft.getInstance().screen) return false;
        graphics.flush();
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        var color = RenderSystem.getShaderColor();
        float red = color[0], green = color[1], blue = color[2], alpha = color[3];
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, preview.getColorTextureId());
        var vertices = Tesselator.getInstance().getBuilder();
        vertices.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var matrix = graphics.pose().last().pose();
        int x = rect.getX(), y = rect.getY(), r = x + rect.getWidth(), b = y + rect.getHeight();
        vertices.vertex(matrix, x, y, 0).uv(left, top).endVertex();
        vertices.vertex(matrix, x, b, 0).uv(left, bottom).endVertex();
        vertices.vertex(matrix, r, b, 0).uv(right, bottom).endVertex();
        vertices.vertex(matrix, r, y, 0).uv(right, top).endVertex();
        BufferUploader.drawWithShader(vertices.end());
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.defaultBlendFunc();
        if (!blend) RenderSystem.disableBlend();
        if (depth) RenderSystem.enableDepthTest();
        return true;
    }

    private static void closed(ScreenEvent.Closing event) {
        if (event.getScreen() != owner) return;
        owner = null;
        ready = false;
        capturing = false;
        if (background != null) {
            background.destroyBuffers();
            preview.destroyBuffers();
            background = null;
            preview = null;
        }
    }
}
