package com.gtocore.client.renderer.fx;

import com.gtocore.client.renderer.GTORenderTypes;
import com.gtocore.config.GTOConfig;

import com.gtolib.GTOCore;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
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
import com.mojang.math.Axis;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public final class SolarSurfaceFX extends AbstractFX {

    private static final ResourceLocation DIMENSION = GTOCore.id("solar_surface");
    // animateTick samples many surface blocks: keep eruptions rare even over a plasma ocean.
    private static final int ERUPTION_CHANCE = 180;
    private static final int VORTEX_CHANCE = 2400;
    private static final int MAX_VORTICES = 4;
    private static final int FADE_IN_TICKS = 20;
    private static final int FADE_OUT_TICKS = 30;
    private static boolean eruptedThisTick;
    private static boolean spawnedVortexThisTick;
    private static final double MAX_DISTANCE_SQUARED = 256.0D * 256.0D;
    private static final double MIN_DISTANCE_BETWEEN_VORTICES_SQUARED = 16.0D * 16.0D;
    private static VertexBuffer quad;

    private final Level level;
    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final float rotation;
    private final int lifetime;
    private final AABB bounds;
    private final Matrix4f modelView = new Matrix4f();

    private SolarSurfaceFX(Level level, double x, double y, double z, RandomSource random) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        radius = 2.5F + random.nextFloat() * 16.0F;
        rotation = random.nextBoolean() ? 1.0F : -1.0F;
        lifetime = 20 + random.nextInt(30);
        bounds = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
    }

    public static void beginTick() {
        eruptedThisTick = false;
        spawnedVortexThisTick = false;
    }

    public static void animateTick(Level level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide || !level.dimension().location().equals(DIMENSION)) {
            return;
        }
        boolean erupt = !eruptedThisTick && random.nextInt(ERUPTION_CHANCE) == 0;
        boolean vortex = !spawnedVortexThisTick && random.nextInt(VORTEX_CHANCE) == 0;
        if ((!erupt && !vortex) || !level.isEmptyBlock(pos.above())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + level.getFluidState(pos).getHeight(level, pos) + 0.05D;
        double z = pos.getZ() + 0.5D;
        if (cameraPos.distanceToSqr(x, y, z) > MAX_DISTANCE_SQUARED) {
            return;
        }
        ParticleStatus particles = minecraft.options.particles().get();
        if (erupt && particles != ParticleStatus.MINIMAL) {
            eruptedThisTick = true;
            int count = particles == ParticleStatus.DECREASED ? 32 : 80;
            for (int i = 0; i < count; i++) {
                double spreadX = (random.nextDouble() - 0.5D) * 0.7D;
                double spreadZ = (random.nextDouble() - 0.5D) * 0.7D;
                level.addParticle(ParticleTypes.FLAME, x + spreadX, y, z + spreadZ,
                        spreadX * 0.3D, 0.45D + random.nextDouble() * 0.9D, spreadZ * 0.3D);
            }
        }
        if (vortex && GTOConfig.INSTANCE.client.renderingConfig.enableLargeRangeShaderEffects) {
            int active = 0;
            for (int i = 0; i < FXManager.FX_LIST.size(); i++) {
                if (FXManager.FX_LIST.get(i) instanceof SolarSurfaceFX fx && fx.level == level && !fx.isDiscarded()) {
                    double dx = x - fx.x;
                    double dz = z - fx.z;
                    if (++active >= MAX_VORTICES || dx * dx + dz * dz < MIN_DISTANCE_BETWEEN_VORTICES_SQUARED) {
                        return;
                    }
                }
            }
            FXManager.addFX(new SolarSurfaceFX(level, x, y + 0.4D, z, random));
            spawnedVortexThisTick = true;
        }
    }

    @Override
    public boolean shouldDiscard() {
        return age >= lifetime || Minecraft.getInstance().level != level;
    }

    @Override
    public void render(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f projectionMatrix,
                       float partialTick, Camera camera, Frustum frustum) {
        if (stage != RenderLevelStageEvent.Stage.AFTER_LEVEL || shouldDiscard() || camera.getPosition().distanceToSqr(x, y, z) > MAX_DISTANCE_SQUARED || (frustum != null && !frustum.isVisible(bounds))) {
            return;
        }
        ShaderInstance shader = GTORenderTypes.getSolarSurfaceVortexShader();
        if (shader == null) {
            return;
        }
        var scene = ScreenSpaceSceneCapture.captureMainTarget();
        if (scene == null) {
            return;
        }
        float time = age + partialTick;
        float appear = smoothStep(time / FADE_IN_TICKS);
        float disappear = smoothStep((lifetime - time) / FADE_OUT_TICKS);
        float strength = appear * disappear;
        float scale = radius * (0.25F + 0.75F * appear) * (0.55F + 0.45F * disappear);
        shader.setSampler("DiffuseSampler", scene.getColorTextureId());
        shader.safeGetUniform("ScreenSize").set((float) scene.viewWidth, (float) scene.viewHeight);
        shader.safeGetUniform("Time").set(time / 200.0F * rotation);
        shader.safeGetUniform("Strength").set(strength);

        Vec3 cameraPos = camera.getPosition();
        // Forge 1.20.1 passes the projection stack at AFTER_LEVEL, not the world view stack.
        // Recover the world view rotation (including camera roll) before placing the billboard.
        modelView.set(RenderSystem.getInverseViewRotationMatrix()).transpose()
                .translate((float) (x - cameraPos.x), (float) (y - cameraPos.y), (float) (z - cameraPos.z))
                // .rotate(camera.rotation())
                .rotate(Axis.XP.rotationDegrees(90.0F))
                .scale(scale);
        ScreenSpaceMeshRenderer.render(modelView, projectionMatrix, getQuad(), GTORenderTypes.SOLAR_SURFACE_VORTEX, shader, 1, 1, 1, 1);
    }

    private static float smoothStep(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static VertexBuffer getQuad() {
        if (quad == null) {
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(-1, -1, 0).uv(0, 0).endVertex();
            builder.vertex(1, -1, 0).uv(1, 0).endVertex();
            builder.vertex(1, 1, 0).uv(1, 1).endVertex();
            builder.vertex(-1, -1, 0).uv(0, 0).endVertex();
            builder.vertex(1, 1, 0).uv(1, 1).endVertex();
            builder.vertex(-1, 1, 0).uv(0, 1).endVertex();
            quad = new VertexBuffer(VertexBuffer.Usage.STATIC);
            quad.bind();
            quad.upload(builder.end());
            VertexBuffer.unbind();
        }
        return quad;
    }
}
