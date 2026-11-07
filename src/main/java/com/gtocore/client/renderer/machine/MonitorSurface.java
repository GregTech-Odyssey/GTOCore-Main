package com.gtocore.client.renderer.machine;

import com.gtocore.common.machine.monitor.Manager;
import com.gtocore.common.machine.monitor.MonitorEditor;
import com.gtocore.common.machine.monitor.MonitorLayout;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.LinkedHashMap;

/** Draws every card into one color texture before mapping it onto the monitor face. */
@OnlyIn(Dist.CLIENT)
final class MonitorSurface {

    private static final LinkedHashMap<Manager.GridNetwork, Cache> CACHED = new LinkedHashMap<>(12, 0.75f, true);
    private static final MultiBufferSource.BufferSource GUI_BUFFER = MultiBufferSource.immediate(new BufferBuilder(256));
    private static final Component NO_INFORMATION = Component.translatable("gtocore.machine.monitor.no_information");
    private static final int MAX_SURFACE_PIXELS = 4 * 1024 * 1024;
    private static final int MAX_CACHED_PIXELS = 16 * 1024 * 1024;
    private static final int[] VIEWPORT = new int[4];
    private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
    private static final Vector4f CORNER = new Vector4f();
    private static int cachedPixels;

    private static final class Cache {

        private final TextureTarget surface;
        private final float pixelScale;
        private long tick = Long.MIN_VALUE;

        private Cache(int width, int height, float pixelScale) {
            surface = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            surface.setFilterMode(GL11.GL_NEAREST);
            this.pixelScale = pixelScale;
        }
    }

    private MonitorSurface() {}

    static void render(Manager.GridNetwork network, net.minecraft.world.level.Level level, PoseStack worldPose, float width, float height) {
        int logicalWidth = network.width3D() * MonitorLayout.PIXELS_PER_BLOCK;
        int logicalHeight = network.height3D() * MonitorLayout.PIXELS_PER_BLOCK;
        float pixelScale = pixelScale(worldPose, width, height, logicalWidth, logicalHeight);
        Cache cache = CACHED.get(network);
        // Grow immediately, but leave room before shrinking so camera motion does not rebuild the target every frame.
        if (cache == null || pixelScale > cache.pixelScale || pixelScale <= cache.pixelScale / 4) {
            int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            boolean depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            if (cache != null) {
                cachedPixels -= cache.surface.width * cache.surface.height;
                cache.surface.destroyBuffers();
            }
            int textureWidth = Math.max(1, Math.round(logicalWidth * pixelScale));
            int textureHeight = Math.max(1, Math.round(logicalHeight * pixelScale));
            cache = new Cache(textureWidth, textureHeight, pixelScale);
            CACHED.put(network, cache);
            cachedPixels += textureWidth * textureHeight;
            while (CACHED.size() > 8 || cachedPixels > MAX_CACHED_PIXELS) {
                var oldest = CACHED.entrySet().iterator();
                var surface = oldest.next().getValue().surface;
                cachedPixels -= surface.width * surface.height;
                surface.destroyBuffers();
                oldest.remove();
            }
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            RenderSystem.viewport(VIEWPORT[0], VIEWPORT[1], VIEWPORT[2], VIEWPORT[3]);
            RenderSystem.depthMask(depthMask);
            if (!depthEnabled) RenderSystem.disableDepthTest();
        }

        if (cache.tick != level.getGameTime() / 10) {
            paint(cache.surface, network, level, logicalWidth, logicalHeight,
                    cache.surface.width, cache.surface.height, cache.pixelScale);
            cache.tick = level.getGameTime() / 10;
        }

        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean cullEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, cache.surface.getColorTextureId());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        var vertices = Tesselator.getInstance().getBuilder();
        vertices.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        var pose = worldPose.last().pose();
        vertices.vertex(pose, 0, 0, 0).uv(0, 1).endVertex();
        vertices.vertex(pose, 0, height, 0).uv(0, 0).endVertex();
        vertices.vertex(pose, width, height, 0).uv(1, 0).endVertex();
        vertices.vertex(pose, width, 0, 0).uv(1, 1).endVertex();
        BufferUploader.drawWithShader(vertices.end());
        if (cullEnabled) RenderSystem.enableCull();
        if (!blendEnabled) RenderSystem.disableBlend();
    }

    static void clear() {
        for (Cache cache : CACHED.values()) cache.surface.destroyBuffers();
        CACHED.clear();
        cachedPixels = 0;
    }

    private static float pixelScale(PoseStack worldPose, float width, float height, int logicalWidth, int logicalHeight) {
        int maxSize = Math.min(4096, RenderSystem.maxSupportedTextureSize());
        float maxScale = Math.min((float) maxSize / Math.max(logicalWidth, logicalHeight),
                (float) Math.sqrt(MAX_SURFACE_PIXELS / ((double) logicalWidth * logicalHeight)));
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, VIEWPORT);
        VIEW_PROJECTION.set(RenderSystem.getProjectionMatrix()).mul(RenderSystem.getModelViewMatrix()).mul(worldPose.last().pose());
        float requiredScale = 1;
        for (int i = 0; i < 4; i++) {
            CORNER.set((i & 1) == 0 ? 0 : width, (i & 2) == 0 ? 0 : height, 0, 1).mul(VIEW_PROJECTION);
            if (CORNER.w <= 0.05f) return maxScale;
            float inverseW = 1 / CORNER.w;
            float ndcX = CORNER.x * inverseW;
            float ndcY = CORNER.y * inverseW;
            // The projection derivative includes magnification at the near edge of an obliquely viewed screen.
            float dx = (VIEW_PROJECTION.m00() - ndcX * VIEW_PROJECTION.m03()) * inverseW * VIEWPORT[2] / 2;
            float dy = (VIEW_PROJECTION.m01() - ndcY * VIEW_PROJECTION.m03()) * inverseW * VIEWPORT[3] / 2;
            float ux = (VIEW_PROJECTION.m10() - ndcX * VIEW_PROJECTION.m13()) * inverseW * VIEWPORT[2] / 2;
            float uy = (VIEW_PROJECTION.m11() - ndcY * VIEW_PROJECTION.m13()) * inverseW * VIEWPORT[3] / 2;
            // Two texture samples per framebuffer pixel keep glyph edges sharp without magnifying texels.
            requiredScale = Math.max(requiredScale, 2 * (float) Math.hypot(dx, dy) * width / logicalWidth);
            requiredScale = Math.max(requiredScale, 2 * (float) Math.hypot(ux, uy) * height / logicalHeight);
        }
        return Math.min(maxScale, Mth.smallestEncompassingPowerOfTwo(Mth.ceil(Math.min(requiredScale, maxScale))));
    }

    private static void paint(TextureTarget surface, Manager.GridNetwork network, net.minecraft.world.level.Level level,
                              int logicalWidth, int logicalHeight, int textureWidth, int textureHeight, float pixelScale) {
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        boolean depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        float fogStart = RenderSystem.getShaderFogStart();
        float fogEnd = RenderSystem.getShaderFogEnd();
        VertexSorting vertexSorting = RenderSystem.getVertexSorting();
        Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        try {
            surface.setClearColor(16f / 255f, 21f / 255f, 28f / 255f, 1);
            surface.clear(Minecraft.ON_OSX);
            surface.bindWrite(false);
            RenderSystem.viewport(0, 0, textureWidth, textureHeight);
            RenderSystem.disableDepthTest();
            // Font shaders use the GUI Z offset as fog distance unless this pass disables world fog.
            RenderSystem.setShaderFogStart(1.0e9f);
            RenderSystem.setShaderFogEnd(2.0e9f);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, textureWidth, textureHeight, 0, 1000, 21000), VertexSorting.ORTHOGRAPHIC_Z);
            modelView.setIdentity();
            modelView.translate(0, 0, -11000);
            RenderSystem.applyModelViewMatrix();
            GuiGraphics graphics = new GuiGraphics(Minecraft.getInstance(), GUI_BUFFER);
            graphics.pose().scale(pixelScale, pixelScale, 1);
            var entries = MonitorEditor.effectiveLayout(network, level);
            for (MonitorLayout.Entry entry : entries) {
                if (entry.x() < 0 || entry.y() < 0 || entry.x() >= logicalWidth || entry.y() >= logicalHeight) continue;
                MonitorEditor.drawCard(graphics, entry, MonitorEditor.component(network, entry));
            }
            if (entries.isEmpty()) {
                var font = Minecraft.getInstance().font;
                graphics.drawString(font, NO_INFORMATION, (logicalWidth - font.width(NO_INFORMATION)) / 2,
                        logicalHeight / 2, 0xFFFFFFFF, false);
            }
            graphics.flush();
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection, vertexSorting);
            RenderSystem.setShaderFogStart(fogStart);
            RenderSystem.setShaderFogEnd(fogEnd);
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            RenderSystem.depthMask(depthMask);
            if (depthEnabled) RenderSystem.enableDepthTest();
            else RenderSystem.disableDepthTest();
        }
    }
}
