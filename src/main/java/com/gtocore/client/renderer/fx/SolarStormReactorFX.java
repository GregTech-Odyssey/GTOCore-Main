package com.gtocore.client.renderer.fx;

import com.gtocore.client.renderer.GTORenderTypes;
import com.gtocore.common.machine.multiblock.electric.SolarStormAggregationReactor;
import com.gtocore.config.GTOConfig;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

/** Working reactor visuals. Static particle seeds are animated on the GPU, as in GTM's RingInstancer. */
@OnlyIn(Dist.CLIENT)
public final class SolarStormReactorFX extends AbstractFX {

    // MBS axes LEFT/UP/BACK: controller (7,4,2), bottom face (7,-0.5,7), top face y=64.5.
    private static final float BASE_BACK = 5;
    private static final float BASE_UP = -4.5F;
    private static final float TOP_ABOVE_BASE = 65;
    private static final float BEAM_HEIGHT = 256.5F;
    private static final float WAVE_RADIUS = 48;
    private static final float CONE_HEIGHT = 72;
    private static final float CONE_RADIUS = 60;
    // Particle half-size/variation and ring drift amplitudes are in blocks.
    private static final float RING_PARTICLE_SIZE = 0.40F;
    private static final float RING_PARTICLE_SIZE_VARIATION = 0.20F;
    private static final float RING_RADIAL_THICKNESS = 3.23F;
    private static final float RING_VERTICAL_THICKNESS = 3.22F;
    // Positive travel time in seconds; a smaller value speeds up both rise and radius expansion.
    private static final float RING_RISE_DURATION_SECONDS = 72;
    private static final int RING_COUNT = 8;
    private static final int PARTICLES_PER_RING = 384;
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static VertexBuffer screenQuad;
    private static VertexBuffer ringParticles;

    private final SolarStormAggregationReactor machine;
    private final Matrix4f modelView = new Matrix4f();
    private final Matrix4f inverseViewProjection = new Matrix4f();
    private Direction front;
    private Direction upwards;
    private boolean flipped;
    private Direction up;
    private double x, y, z;
    private AABB bounds;
    private int lastRefreshAge;

    public SolarStormReactorFX(SolarStormAggregationReactor machine) {
        this.machine = machine;
        refresh();
    }

    public void refresh() {
        lastRefreshAge = age;
        if (front == machine.getFrontFacing() && upwards == machine.getUpwardsFacing() && flipped == machine.isFlipped()) return;
        front = machine.getFrontFacing();
        upwards = machine.getUpwardsFacing();
        flipped = machine.isFlipped();
        var back = RelativeDirection.BACK.getRelative(front, upwards, flipped);
        up = RelativeDirection.UP.getRelative(front, upwards, flipped);
        var pos = machine.getPos();
        x = pos.getX() + 0.5 + back.getStepX() * BASE_BACK + up.getStepX() * BASE_UP;
        y = pos.getY() + 0.5 + back.getStepY() * BASE_BACK + up.getStepY() * BASE_UP;
        z = pos.getZ() + 0.5 + back.getStepZ() * BASE_BACK + up.getStepZ() * BASE_UP;
        double endX = x + up.getStepX() * BEAM_HEIGHT;
        double endY = y + up.getStepY() * BEAM_HEIGHT;
        double endZ = z + up.getStepZ() * BEAM_HEIGHT;
        bounds = new AABB(Math.min(x, endX) - WAVE_RADIUS, Math.min(y, endY) - WAVE_RADIUS, Math.min(z, endZ) - WAVE_RADIUS,
                Math.max(x, endX) + WAVE_RADIUS, Math.max(y, endY) + WAVE_RADIUS, Math.max(z, endZ) + WAVE_RADIUS);
    }

    @Override
    public boolean shouldDiscard() {
        return machine.getLevel() != Minecraft.getInstance().level || machine.holder.isRemoved() ||
                !machine.isFormed() || !machine.recipeLogic.isWorking() || age - lastRefreshAge > 12;
    }

    @Override
    public void render(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix,
                       float partialTick, Camera camera, Frustum frustum) {
        if (stage != RenderLevelStageEvent.Stage.AFTER_LEVEL || shouldDiscard() ||
                (frustum != null && !frustum.isVisible(bounds)))
            return;
        Vec3 cameraPos = camera.getPosition();
        double dx = cameraPos.x - x, dy = cameraPos.y - y, dz = cameraPos.z - z;
        double along = Mth.clamp(dx * up.getStepX() + dy * up.getStepY() + dz * up.getStepZ(), 0, BEAM_HEIGHT);
        double sideX = dx - along * up.getStepX(), sideY = dy - along * up.getStepY(), sideZ = dz - along * up.getStepZ();
        if (sideX * sideX + sideY * sideY + sideZ * sideZ > 256.0 * 256.0) return;
        // AFTER_LEVEL supplies a projection stack; recover the camera's view rotation instead.
        modelView.set(RenderSystem.getInverseViewRotationMatrix()).transpose();
        inverseViewProjection.set(projectionMatrix).mul(modelView).invert();
        modelView.translate((float) -dx, (float) -dy, (float) -dz);
        float time = (age + partialTick) / 20.0F;
        float strength = Mth.clamp((age + partialTick) / 20.0F, 0, 1);
        if (GTOConfig.INSTANCE.client.renderingConfig.enableLargeRangeShaderEffects) {
            renderBeam(dx, dy, dz, time, strength);
        }
        renderRings(projectionMatrix, time, strength);
    }

    private void renderBeam(double dx, double dy, double dz, float time, float strength) {
        var shader = GTORenderTypes.getShader(GTORenderTypes.SOLAR_STORM_REACTOR_SHADER_LOCATION);
        if (shader == null) return;
        var scene = ScreenSpaceSceneCapture.captureMainTarget();
        if (scene == null) return;
        shader.setSampler("DepthSampler", scene.getDepthTextureId());
        shader.safeGetUniform("InverseViewProjection").set(inverseViewProjection);
        shader.safeGetUniform("CameraRelative").set((float) dx, (float) dy, (float) dz);
        shader.safeGetUniform("Axis").set((float) up.getStepX(), (float) up.getStepY(), (float) up.getStepZ());
        shader.safeGetUniform("BeamHeight").set(BEAM_HEIGHT);
        shader.safeGetUniform("WaveRadius").set(WAVE_RADIUS);
        shader.safeGetUniform("Time").set(time);
        shader.safeGetUniform("Strength").set(strength);
        if (screenQuad == null) {
            var builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(-1, -1, 0).uv(0, 0).endVertex();
            builder.vertex(1, -1, 0).uv(1, 0).endVertex();
            builder.vertex(1, 1, 0).uv(1, 1).endVertex();
            builder.vertex(-1, 1, 0).uv(0, 1).endVertex();
            screenQuad = upload(builder);
        }
        // The quad is already in clip space; scene rays use InverseViewProjection above.
        ScreenSpaceMeshRenderer.render(IDENTITY, IDENTITY, screenQuad, GTORenderTypes.SOLAR_STORM_REACTOR, shader, 1, 1, 1, 1);
    }

    private void renderRings(Matrix4f projection, float time, float strength) {
        var shader = GTORenderTypes.getShader(GTORenderTypes.SOLAR_STORM_REACTOR_RINGS_SHADER_LOCATION);
        if (shader == null) return;
        var quality = Minecraft.getInstance().options.particles().get();
        shader.safeGetUniform("ParticleStride").set(quality == ParticleStatus.MINIMAL ? 4.0F : quality == ParticleStatus.DECREASED ? 2.0F : 1.0F);
        shader.safeGetUniform("Axis").set((float) up.getStepX(), (float) up.getStepY(), (float) up.getStepZ());
        shader.safeGetUniform("TopHeight").set(TOP_ABOVE_BASE);
        shader.safeGetUniform("ConeHeight").set(CONE_HEIGHT);
        shader.safeGetUniform("ConeRadius").set(CONE_RADIUS);
        shader.safeGetUniform("ParticleSize").set(RING_PARTICLE_SIZE);
        shader.safeGetUniform("ParticleSizeVariation").set(RING_PARTICLE_SIZE_VARIATION);
        shader.safeGetUniform("RingRadialThickness").set(RING_RADIAL_THICKNESS);
        shader.safeGetUniform("RingVerticalThickness").set(RING_VERTICAL_THICKNESS);
        shader.safeGetUniform("RiseDuration").set(RING_RISE_DURATION_SECONDS);
        shader.safeGetUniform("Time").set(time);
        shader.safeGetUniform("Strength").set(strength);
        if (ringParticles == null) {
            var builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            for (int ring = 0; ring < RING_COUNT; ring++) {
                for (int particle = 0; particle < PARTICLES_PER_RING; particle++) {
                    // Position.xy is the sprite corner, z the seed; UV0 holds ring phase and angular phase.
                    float seed = ring * PARTICLES_PER_RING + particle;
                    float phase = ring / (float) RING_COUNT;
                    float angle = particle / (float) PARTICLES_PER_RING;
                    builder.vertex(-1, -1, seed).uv(phase, angle).endVertex();
                    builder.vertex(1, -1, seed).uv(phase, angle).endVertex();
                    builder.vertex(1, 1, seed).uv(phase, angle).endVertex();
                    builder.vertex(-1, 1, seed).uv(phase, angle).endVertex();
                }
            }
            ringParticles = upload(builder);
        }
        ScreenSpaceMeshRenderer.render(modelView, projection, ringParticles, GTORenderTypes.SOLAR_STORM_REACTOR_RINGS, shader, 1, 1, 1, 1);
    }

    private static VertexBuffer upload(BufferBuilder builder) {
        var buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(builder.end());
        VertexBuffer.unbind();
        return buffer;
    }

    public static void release() {
        if (screenQuad != null) {
            screenQuad.close();
            screenQuad = null;
        }
        if (ringParticles != null) {
            ringParticles.close();
            ringParticles = null;
        }
    }
}
