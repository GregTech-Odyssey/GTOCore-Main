package com.gtocore.client.renderer.fx;

import com.gtocore.client.renderer.GTORenderTypes;
import com.gtocore.config.GTOConfig;

import com.gtolib.GTOCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Client-only weather visuals; trajectories are anchored to world columns, without particle allocations. */
public final class SolarStormFX {

    private static final ResourceLocation DIMENSION = GTOCore.id("solar_surface");
    private static final Matrix4f VIEW = new Matrix4f();
    private static final Matrix4f INVERSE_VIEW_PROJECTION = new Matrix4f();
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final Vector3f RIGHT = new Vector3f();
    private static VertexBuffer particles;
    private static VertexBuffer screenQuad;

    private SolarStormFX() {}

    public static boolean isSolarSurface(Level level) {
        return level != null && level.dimension().location().equals(DIMENSION);
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (!isSolarSurface(level)) return;
        float strength = level.getRainLevel(event.getPartialTick());
        if (strength <= 0.001F) return;

        // AFTER_LEVEL supplies a projection stack. Recover the camera's world-view rotation.
        VIEW.set(RenderSystem.getInverseViewRotationMatrix()).transpose();
        Vec3 camera = event.getCamera().getPosition();
        double time = level.getGameTime() + (double) event.getPartialTick();
        renderHeat(event, level, camera, time, strength);
        renderParticles(event, minecraft.options.particles().get(), level, camera, time, strength);
    }

    private static void renderHeat(RenderLevelStageEvent event, Level level, Vec3 camera, double time, float strength) {
        if (!GTOConfig.INSTANCE.client.renderingConfig.enableLargeRangeShaderEffects) return;
        ShaderInstance shader = GTORenderTypes.getShader(GTORenderTypes.SOLAR_STORM_HEAT_SHADER_LOCATION);
        if (shader == null) return;
        var scene = ScreenSpaceSceneCapture.captureMainTarget();
        if (scene == null) return;
        INVERSE_VIEW_PROJECTION.set(event.getProjectionMatrix()).mul(VIEW).invert();
        shader.setSampler("DiffuseSampler", scene.getColorTextureId());
        shader.setSampler("DepthSampler", scene.getDepthTextureId());
        shader.safeGetUniform("InverseViewProjection").set(INVERSE_VIEW_PROJECTION);
        shader.safeGetUniform("ScreenSize").set((float) scene.viewWidth, (float) scene.viewHeight);
        shader.safeGetUniform("CameraHeight").set((float) camera.y);
        shader.safeGetUniform("GroundHeight").set((float) level.getSeaLevel());
        shader.safeGetUniform("ViewDistance").set(Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F);
        shader.safeGetUniform("Time").set((float) (time % 24000.0D) / 20.0F);
        shader.safeGetUniform("Strength").set(strength);
        if (screenQuad == null) {
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(-1, -1, 0).uv(0, 0).endVertex();
            builder.vertex(1, -1, 0).uv(1, 0).endVertex();
            builder.vertex(1, 1, 0).uv(1, 1).endVertex();
            builder.vertex(-1, -1, 0).uv(0, 0).endVertex();
            builder.vertex(1, 1, 0).uv(1, 1).endVertex();
            builder.vertex(-1, 1, 0).uv(0, 1).endVertex();
            screenQuad = new VertexBuffer(VertexBuffer.Usage.STATIC);
            screenQuad.bind();
            screenQuad.upload(builder.end());
            VertexBuffer.unbind();
        }
        ScreenSpaceMeshRenderer.render(IDENTITY, IDENTITY, screenQuad, GTORenderTypes.SOLAR_STORM_HEAT, shader, 1, 1, 1, 1);
    }

    private static void renderParticles(RenderLevelStageEvent event, ParticleStatus quality, Level level, Vec3 camera,
                                        double time, float strength) {
        int radius = quality == ParticleStatus.MINIMAL ? 12 : 24;
        int step = quality == ParticleStatus.ALL ? 1 : 2;
        int centerX = Mth.floor(camera.x);
        int centerZ = Mth.floor(camera.z);
        // Include the upwind emission columns whose particles can drift into the visible volume.
        int startX = Math.floorDiv(centerX - radius - 16, step) * step;
        int startZ = Math.floorDiv(centerZ - radius - 7, step) * step;
        RIGHT.set(1, 0, 0).rotate(event.getCamera().rotation()).mul(0.055F);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int groundHeight = level.getSeaLevel();
        for (int z = startZ; z <= centerZ + radius; z += step) {
            for (int x = startX; x <= centerX + radius; x += step) {
                int seed = hash(x * 73428767 ^ z * 912931);
                float speed = 0.65F + unit(seed) * 0.7F;
                double travel = time * speed + unit(hash(seed)) * 64.0D;
                float height = (float) (travel % 64.0D);
                // Emission begins below the plasma surface. No heightmap, sky visibility or collision clipping.
                float px = (float) (x + unit(hash(seed + 1)) - camera.x) + height * 0.24F;
                float py = (float) (groundHeight - 12.0D - camera.y) + height;
                float pz = (float) (z + unit(hash(seed + 2)) - camera.z) + height * 0.10F;
                float distance = Mth.sqrt(px * px + pz * pz);
                float fade = Mth.clamp((radius - distance) / 6.0F, 0, 1) * Mth.clamp(height / 4.0F, 0, 1) * Mth.clamp((64.0F - height) / 10.0F, 0, 1);
                if (fade <= 0) continue;
                float alpha = fade * strength * 0.85F;
                float length = 0.35F + speed * 0.3F;
                // Four soft triangles form a glowing, wind-leaning plasma streak.
                triangle(builder, px, py, pz, px + length * 0.24F, py + length, pz + length * 0.10F,
                        px + RIGHT.x, py + RIGHT.y, pz + RIGHT.z, alpha);
                triangle(builder, px, py, pz, px + RIGHT.x, py + RIGHT.y, pz + RIGHT.z,
                        px - length * 0.24F, py - length, pz - length * 0.10F, alpha);
                triangle(builder, px, py, pz, px - length * 0.24F, py - length, pz - length * 0.10F,
                        px - RIGHT.x, py - RIGHT.y, pz - RIGHT.z, alpha);
                triangle(builder, px, py, pz, px - RIGHT.x, py - RIGHT.y, pz - RIGHT.z,
                        px + length * 0.24F, py + length, pz + length * 0.10F, alpha);
            }
        }
        if (particles == null) particles = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        particles.bind();
        particles.upload(builder.end());
        VertexBuffer.unbind();
        ScreenSpaceMeshRenderer.render(VIEW, event.getProjectionMatrix(), particles, GTORenderTypes.SOLAR_STORM_PARTICLES,
                net.minecraft.client.renderer.GameRenderer.getPositionColorShader(), 1, 1, 1, 1);
    }

    private static void triangle(BufferBuilder builder, float x, float y, float z,
                                 float ax, float ay, float az, float bx, float by, float bz, float alpha) {
        builder.vertex(x, y, z).color(1.0F, 0.85F, 0.35F, alpha).endVertex();
        builder.vertex(ax, ay, az).color(1.0F, 0.25F, 0.02F, 0.0F).endVertex();
        builder.vertex(bx, by, bz).color(1.0F, 0.25F, 0.02F, 0.0F).endVertex();
    }

    private static int hash(int value) {
        value = (value ^ value >>> 16) * 0x7feb352d;
        value = (value ^ value >>> 15) * 0x846ca68b;
        return value ^ value >>> 16;
    }

    private static float unit(int value) {
        return (value & 0xFFFFFF) / 16777216.0F;
    }

    public static void release() {
        if (particles != null) {
            particles.close();
            particles = null;
        }
        if (screenQuad != null) {
            screenQuad.close();
            screenQuad = null;
        }
    }
}
