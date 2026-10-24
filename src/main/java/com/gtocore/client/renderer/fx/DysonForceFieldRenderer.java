package com.gtocore.client.renderer.fx;

import com.gtocore.client.renderer.GTORenderTypes;
import com.gtocore.common.data.StarMapDysonSync;

import com.gtolib.api.data.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

/** World-anchored, four-block-side hexagonal prisms below the Sun's station. */
public final class DysonForceFieldRenderer {

    private static final double HEIGHT = -72;
    private static final double SIDE_LENGTH = 4;
    private static final double CELL_PITCH = SIDE_LENGTH * 1.0125;
    // Whole lattice periods keep shader coordinates small without changing cell identities.
    private static final double PERIOD_X = 384 * CELL_PITCH;
    private static final double PERIOD_Z = Math.sqrt(3) * 256 * CELL_PITCH;
    private static final Matrix4f VIEW = new Matrix4f();
    private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
    private static final Matrix4f INVERSE_VIEW_PROJECTION = new Matrix4f();
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static VertexBuffer plane;

    private DysonForceFieldRenderer() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(DysonForceFieldRenderer::render);
        MinecraftForge.EVENT_BUS.addListener(DysonForceFieldRenderer::onLoggingOut);
    }

    private static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null || level.dimension() != Dimension.SOLAR_SURFACE.getOrbit()) return;
        int launched = StarMapDysonSync.launched(level);
        if (launched <= 0) return;
        var shader = GTORenderTypes.getShader(GTORenderTypes.DYSON_FORCE_FIELD_SHADER_LOCATION);
        if (shader == null) return;

        var camera = event.getCamera().getPosition();
        double seconds = (level.getGameTime() + (double) event.getPartialTick()) / 20;
        float radius = minecraft.options.getEffectiveRenderDistance() * 16.0F;
        shader.safeGetUniform("ViewDistance").set(radius);
        shader.safeGetUniform("GridOrigin").set(wrap(camera.x - seconds * 0.025, PERIOD_X), wrap(camera.z - seconds * 0.0125, PERIOD_Z));
        shader.safeGetUniform("Completion").set(Math.clamp(launched / (float) StarMapDysonSync.COMPLETE, 0, 1));
        // AFTER_LEVEL no longer carries the world-view stack; use the camera rotation, as solar weather does.
        VIEW.set(RenderSystem.getInverseViewRotationMatrix()).transpose();
        VIEW_PROJECTION.set(event.getProjectionMatrix()).mul(VIEW);
        INVERSE_VIEW_PROJECTION.set(VIEW_PROJECTION).invert();
        shader.safeGetUniform("ViewProjection").set(VIEW_PROJECTION);
        shader.safeGetUniform("InverseViewProjection").set(INVERSE_VIEW_PROJECTION);
        shader.safeGetUniform("CameraHeight").set((float) (camera.y - HEIGHT));

        if (plane == null) {
            var builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(-1, -1, 0).uv(0, 0).endVertex();
            builder.vertex(1, -1, 0).uv(1, 0).endVertex();
            builder.vertex(1, 1, 0).uv(1, 1).endVertex();
            builder.vertex(-1, -1, 0).uv(0, 0).endVertex();
            builder.vertex(1, 1, 0).uv(1, 1).endVertex();
            builder.vertex(-1, 1, 0).uv(0, 1).endVertex();
            plane = new VertexBuffer(VertexBuffer.Usage.STATIC);
            plane.bind();
            plane.upload(builder.end());
            VertexBuffer.unbind();
        }
        ScreenSpaceMeshRenderer.render(IDENTITY, IDENTITY, plane, GTORenderTypes.DYSON_FORCE_FIELD, shader, 1, 1, 1, 1);
    }

    private static float wrap(double coordinate, double period) {
        return (float) (coordinate - Math.floor(coordinate / period) * period);
    }

    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (plane != null) {
            plane.close();
            plane = null;
        }
    }
}
