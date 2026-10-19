package com.gtocore.client.renderer;

import com.gtocore.common.block.HappyWaterGodStatue;
import com.gtocore.common.data.GTOBlocks;

import com.gtolib.GTOCore;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HappyWaterGodStatueRenderer implements BlockEntityRenderer<HappyWaterGodStatue.Entity> {

    private static final String NAME = "suriedxinxin";
    private static final ResourceLocation WHITE = new ResourceLocation("textures/misc/white.png");
    private static final float HALF_SQRT_3 = (float) (Math.sqrt(3.0) / 2.0);
    private static final float RAMP_SECONDS = 8.0F;
    private static final float DISCO_SECONDS = 23.0F;
    private static final float BALL_Y = 4.3F;
    private static final float BALL_RADIUS = 0.42F;
    private static final float BEAM_RANGE = 64.0F;
    private static final Vector3f[] BEAM_DIRS = sphere(64);
    private static final Component NAMEPLATE = Component.translatable(HappyWaterGodStatue.NAMEPLATE);
    private static final Map<String, Skin> SKINS = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> BUNDLED_SKINS = Map.of("suriedxinxin", true, "powsdssag", true, "hophenby", true, "alcox233", true,
            "maple197", true, "bnw_neurs", true, "qwerty54188", true, "ratonhagaton", false);
    private static final Component WARNING = Component.translatable(HappyWaterGodStatue.WARNING).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    private static final Component WARNING_DETAIL = Component.translatable(HappyWaterGodStatue.WARNING_DETAIL).withStyle(ChatFormatting.RED);
    private static final Component WARNING_HINT = Component.translatable(HappyWaterGodStatue.WARNING_HINT);
    private static long warnedAt = Long.MIN_VALUE;
    private static final Component[] LABELS = { Component.literal(HappyWaterGodStatue.LEAD), Component.literal(HappyWaterGodStatue.DEVS[0]), Component.literal(HappyWaterGodStatue.DEVS[1]) };
    private static final Component[] HELPER_LABELS = java.util.Arrays.stream(HappyWaterGodStatue.HELPERS)
            .map(name -> name.equals(HappyWaterGodStatue.OFFLINE_HELPER) ? Component.translatable(HappyWaterGodStatue.OFFLINE_HELPER_NAME) : Component.literal(name)).toArray(Component[]::new);
    private static String bubbleName;
    private static Component bubbleText = Component.empty();
    private static float bounce, yaw, roll, pitch;
    private static float squash;
    private static final int HX = 0, HY = 1, HZ = 2, RX = 3, RY = 4, RZ = 5, LX = 6, LY = 7, LZ = 8, HIP = 9, TWIST = 10, ROLL = 11, LIFT = 12, YAW = 13, RLEG = 14, LLEG = 15, SQUASH = 16, LEGOUT = 17, PITCH = 18, KEYS = 19;
    private static final float[] OMEGA = omega(5.5F, 5.5F, 5.5F, 4.2F, 4.2F, 4.2F, 4.2F, 4.2F, 4.2F, 4.0F, 4.0F, 3.5F, 7.0F, 4.5F, 6.0F, 6.0F, 6.5F, 6.0F, 4.0F);
    private static final float[] ZETA = { 0.38F, 0.38F, 0.38F, 0.36F, 0.36F, 0.36F, 0.36F, 0.36F, 0.36F, 0.42F, 0.42F, 0.5F, 0.45F, 0.55F, 0.4F, 0.4F, 0.22F, 0.4F, 0.5F };
    private static final float[] TARGET = new float[KEYS];
    private static final int GROOVE = 0, CLAP = 1, FRONT_WAVE = 2, HIP_BUMP = 3, CHEEK = 4, PUMP = 5, KICK = 6, RAISE_ROOF = 7, ARM_ROLL = 8, CHORUS_V = 9, HEART = 10, SWIM = 11, CHEST = 12, POINT = 13, SKY = 14, SOFT_WAVE = 15, SPIN = 16,
            HOLD = 17, IDLE = 18, CLAP_UP = 19, STEP_TOUCH = 20, LOOK = 21, DAB = 22, SHIMMY = 23, ROBOT = 24, JUMP_STAR = 25, BOW = 26, FINAL = 27;
    private static final int[] CHOREO = {
            GROOVE, GROOVE, CLAP_UP, CLAP_UP,
            FRONT_WAVE, FRONT_WAVE, STEP_TOUCH, LOOK, CLAP, CLAP, HIP_BUMP, DAB,
            CHEEK, CHEEK, SHIMMY, SHIMMY, ARM_ROLL, ARM_ROLL, STEP_TOUCH, JUMP_STAR,
            PUMP, PUMP, KICK, KICK, RAISE_ROOF, RAISE_ROOF, ROBOT, ROBOT,
            CHORUS_V, CHORUS_V, HEART, HEART, SWIM, SWIM, CHORUS_V, CHEST,
            POINT, POINT, ROBOT, ROBOT, POINT, POINT, SHIMMY, JUMP_STAR,
            HIP_BUMP, HIP_BUMP, FRONT_WAVE, LOOK, CLAP_UP, CLAP_UP, CHEEK, DAB,
            RAISE_ROOF, PUMP, KICK, JUMP_STAR, ARM_ROLL, ARM_ROLL, ROBOT, PUMP,
            CHORUS_V, CHORUS_V, SPIN, SPIN, HEART, SWIM, CHORUS_V, CHEST,
            SKY, SKY, SKY, SOFT_WAVE, CLAP_UP, CLAP_UP, RAISE_ROOF, JUMP_STAR,
            CHORUS_V, CHORUS_V, SPIN, SPIN, SWIM, HEART, SHIMMY, CHEST,
            SPIN, SPIN, CHORUS_V, CHORUS_V, JUMP_STAR, CHORUS_V, CLAP_UP, CLAP_UP,
            RAISE_ROOF, RAISE_ROOF, SPIN, SPIN, CHORUS_V, JUMP_STAR, BOW, BOW,
            HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD, HOLD };
    private static final int COUNT = 0, WIDTH = 1, RANGE = 2, SPEED = 3, SAT = 4, WARM = 5, STROBE = 6, LOOK_KEYS = 7;
    private static final float[][] LOOKS = {
            { 0.5F, 0.8F, 0.7F, 0.6F, 0.6F, 0, 0 },
            { 0.7F, 1.0F, 0.85F, 1.0F, 0.85F, 0, 0.15F },
            { 0.85F, 0.9F, 1.0F, 1.5F, 0.9F, 0, 0.35F },
            { 1.0F, 1.35F, 1.0F, 1.9F, 1.0F, 0, 0.55F },
            { 0.6F, 0.75F, 1.0F, 2.8F, 1.0F, 0, 0.8F },
            { 0.35F, 1.7F, 0.8F, 0.45F, 0.4F, 0.3F, 0 },
            { 1.0F, 1.55F, 1.0F, 2.5F, 1.0F, 0, 0.7F },
            { 0.45F, 1.3F, 0.75F, 0.3F, 0.35F, 1.0F, 0 } };
    private static final MultiBufferSource.BufferSource LATE = MultiBufferSource.immediateWithBuffers(
            Map.of(Types.RAYS, new BufferBuilder(Types.RAYS.bufferSize()), Types.BALL, new BufferBuilder(Types.BALL.bufferSize())), new BufferBuilder(256));

    private static float[] omega(float... hertz) {
        float[] out = new float[hertz.length];
        for (int i = 0; i < hertz.length; i++) out[i] = Mth.TWO_PI * hertz[i];
        return out;
    }

    private final PlayerModel<LivingEntity> wide;
    private final PlayerModel<LivingEntity> thin;

    public HappyWaterGodStatueRenderer(BlockEntityRendererProvider.Context context) {
        wide = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        thin = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        wide.young = false;
        thin.young = false;
    }

    private static Component bubbleText() {
        var player = Minecraft.getInstance().player;
        String name = player == null ? "" : player.getGameProfile().getName();
        if (!name.equals(bubbleName)) {
            bubbleName = name;
            bubbleText = Component.translatable(HappyWaterGodStatue.BUBBLE, name);
        }
        return bubbleText;
    }

    private static boolean thinArms(MinecraftProfileTexture texture) {
        try (InputStream in = new URL(texture.getUrl()).openStream(); NativeImage image = NativeImage.read(in)) {
            if (image.getWidth() != 64 || image.getHeight() != 64) return false;
            for (int y = 20; y < 32; y++) {
                if ((image.getPixelRGBA(54, y) >>> 24) != 0 || (image.getPixelRGBA(55, y) >>> 24) != 0) return false;
            }
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public boolean shouldRenderOffScreen(HappyWaterGodStatue.Entity entity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(HappyWaterGodStatue.Entity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var state = entity.getBlockState();
        var level = entity.getLevel();
        long now = level == null ? 0 : level.getGameTime();
        long start = entity.start();
        boolean shortcut = entity.shortcut();
        float elapsed = start == HappyWaterGodStatue.IDLE ? -1 : now - start + partialTick;
        boolean lit = elapsed >= 0 && elapsed < HappyWaterGodStatue.showLength(shortcut, entity.prelude());
        if (!lit) entity.dancer = null;
        float time = now + partialTick;
        float seconds = lit ? elapsed / 20.0F : 0;
        float glow = lit ? Mth.clamp(seconds / RAMP_SECONDS, 0, 1) : 0;
        float ease = glow * glow * (3 - 2 * glow);
        Skin lead = skin(HappyWaterGodStatue.LEAD);
        ResourceLocation texture = lead.texture();
        PlayerModel<LivingEntity> model = lead.slim() ? thin : wide;
        int light = lit ? LightTexture.FULL_BRIGHT : packedLight;
        if (lit && (shortcut || seconds >= DISCO_SECONDS)) {
            float song = HappyWaterGodStatue.songTime(seconds * 20.0F - (shortcut ? 0 : DISCO_SECONDS * 20.0F));
            stage(entity, state, poseStack, buffer, time, song, packedLight);
            return;
        }
        pose(model, ease, time, seconds);
        float lift = 0.3F * ease + 0.04F * ease * Mth.sin(seconds * 2.2F);

        float nameAlpha = 1 - Mth.clamp(glow * 2.5F, 0, 1);
        if (nameAlpha > 0.05F) nameplate(poseStack, buffer, 2.8F + lift, light, nameAlpha);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5 + lift, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.getValue(HorizontalDirectionalBlock.FACING).toYRot() + 25.0F * ease * seconds));
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F * ease));
        poseStack.scale(-0.9375F, -0.9375F, 0.9375F);
        poseStack.translate(0, -1.501, 0);
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        if (lit) {
            float pulse = 0.85F + 0.15F * Mth.sin(seconds * 6.0F);
            float b = 0.8F * glow * glow * pulse;
            model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.eyes(WHITE)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, b, b * 0.92F, b * 0.7F, 1.0F);
        }
        poseStack.popPose();
        if (lit) rays(poseStack, LATE, glow, seconds);
        poseStack.popPose();
    }

    private static void nameplate(PoseStack poseStack, MultiBufferSource buffer, float y, int light, float alpha) {
        nameplate(poseStack, buffer, NAMEPLATE, 0.5F, y, 0.5F, light, alpha);
    }

    private static void nameplate(PoseStack poseStack, MultiBufferSource buffer, Component text, float x, float y, float z, int light, float alpha) {
        var mc = Minecraft.getInstance();
        Font font = mc.font;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        int background = (int) (mc.options.getBackgroundOpacity(0.25F) * 255.0F * alpha) << 24;
        int color = (int) (255.0F * alpha) << 24 | 0xFFE08A;
        font.drawInBatch(text, -font.width(text) / 2.0F, 0, color, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, background, light);
        poseStack.popPose();
    }

    private static void pose(PlayerModel<LivingEntity> model, float ease, float time, float seconds) {
        model.setAllVisible(true);
        float idle = 1 - ease;
        float cycle = (time % 160.0F) / 160.0F;
        float wave = cycle < 0.3F ? Mth.sin(cycle / 0.3F * Mth.PI) : 0;
        float breath = Mth.sin(time * 0.08F);
        float headYaw = 0.35F * Mth.sin(time * 0.03F) * idle;
        model.head.setRotation(0.05F * Mth.sin(time * 0.05F) * idle - 0.6F * ease, headYaw, 0);
        model.body.setRotation(0, 0, 0);
        float waveSwing = 0.35F * wave * Mth.sin(time * 0.6F);
        float rightZ = (0.08F + 0.03F * breath) * idle + 2.5F * ease + 0.06F * ease * Mth.sin(seconds * 3.0F);
        float leftZ = -(0.08F + 0.03F * breath + 2.75F * wave + waveSwing) * idle - 2.5F * ease - 0.06F * ease * Mth.sin(seconds * 3.0F + 1.0F);
        model.rightArm.setRotation(0.04F * breath * idle - 0.35F * ease, 0, rightZ);
        model.leftArm.setRotation(-0.55F * wave * idle - 0.35F * ease, 0, leftZ);
        model.rightLeg.x = -1.9F;
        model.leftLeg.x = 1.9F;
        model.rightLeg.y = 12.0F;
        model.leftLeg.y = 12.0F;
        model.rightLeg.setRotation(0, 0, 0.03F * idle + 0.12F * ease);
        model.leftLeg.setRotation(0, 0, -0.03F * idle - 0.12F * ease);
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }

    private void stage(HappyWaterGodStatue.Entity entity, BlockState state, PoseStack poseStack, MultiBufferSource buffer, float time, float song, int light) {
        Stage stage = entity.dancer instanceof Stage existing ? existing : new Stage();
        entity.dancer = stage;
        stage.dt = Mth.clamp((song - stage.last) / 20.0F, 0, 0.5F);
        stage.last = song;
        int bar = HappyWaterGodStatue.SONG_BAR;
        float fade = 1 - Mth.clamp((song - 112 * bar) / (4 * bar + HappyWaterGodStatue.SONG_FADE), 0, 1);
        float dawn = Mth.clamp((song + HappyWaterGodStatue.SONG_LEAD) / HappyWaterGodStatue.SONG_LEAD, 0, 1);
        Direction front = state.getValue(HorizontalDirectionalBlock.FACING);
        float facing = 180.0F - front.toYRot();
        float fx = front.getStepX(), fz = front.getStepZ(), rx = -fz, rz = fx;
        int move = choreo(song);
        boolean farewell = move == HOLD || move == FINAL;
        perform(entity, poseStack, buffer, stage.lead, skin(HappyWaterGodStatue.LEAD), song, move, false, 1, facing, 0.5F, 0.5F, 0.5F, fade, 0, 0, 1, farewell ? LABELS[0] : null);
        float duoIn = Mth.clamp((song - 11 * bar) / bar, 0, 1);
        float duoOut = 1 - Mth.clamp((song - 108 * bar) / (2.0F * bar), 0, 1);
        if (duoIn > 0 && duoOut > 0) {
            float lift = -(1 - backOut(duoIn)) * 1.4F;
            float rise = 0.5F + lift + (1 - duoOut) * 1.2F;
            float plinth = lift - (1 - duoOut) * 1.0F;
            float turn = (1 - cubicOut(duoIn)) * 540.0F;
            float presence = Math.min(Math.min(1, duoIn * 2.5F), duoOut);
            float scale = 1 - 0.2F * (1 - duoOut);
            for (int i = 0; i < 2; i++) {
                float sx = HappyWaterGodStatue.seatX(1 + i), sz = HappyWaterGodStatue.seatZ(1 + i);
                float x = 0.5F + rx * sx + fx * sz, z = 0.5F + rz * sx + fz * sz;
                Dancer d = stage.duo[i];
                poseStack.pushPose();
                poseStack.translate(x - 0.5F, plinth, z - 0.5F);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
                if (!d.shown) burst(entity, x, 0.2F, z, ParticleTypes.END_ROD, 28, 0.12F);
                d.shown = true;
                perform(entity, poseStack, buffer, d, skin(HappyWaterGodStatue.DEVS[i]), song, move, false, 1, facing, x, rise, z, presence, i == 0 ? turn : -turn, 0, scale, farewell ? LABELS[i + 1] : null);
                if (duoOut < 0.02F && !d.gone) {
                    burst(entity, x, 1.6F, z, ParticleTypes.END_ROD, 16, 0.05F);
                    d.gone = true;
                }
            }
        }
        int helpers = HappyWaterGodStatue.HELPERS.length;
        for (int j = 0; j < helpers; j++) {
            float helperIn = Mth.clamp((song - (27 * bar - 8 + j * 6)) / 20.0F, 0, 1);
            float helperOut = 1 - Mth.clamp((song - (104 * bar + (helpers - 1 - j) * 8)) / bar, 0, 1);
            if (helperIn <= 0 || helperOut <= 0) continue;
            float sx = HappyWaterGodStatue.seatX(3 + j), sz = HappyWaterGodStatue.seatZ(3 + j);
            float x = 0.5F + rx * sx + fx * sz, z = 0.5F + rz * sx + fz * sz;
            Dancer d = stage.helpers[j];
            if (!d.shown) burst(entity, x, 0.4F, z, ParticleTypes.END_ROD, 28, 0.14F);
            d.shown = true;
            if (helperOut < 0.02F && !d.gone) {
                burst(entity, x, 1.6F, z, ParticleTypes.END_ROD, 16, 0.05F);
                d.gone = true;
            }
            float scale = Math.max(0.01F, backOut(helperIn)) * (1 - 0.2F * (1 - helperOut));
            float turn = (1 - cubicOut(helperIn)) * 720.0F;
            float presence = Math.min(Math.min(1, helperIn * 2), helperOut);
            perform(entity, poseStack, buffer, d, skin(HappyWaterGodStatue.HELPERS[j]), song, move, false, 1, facing, x, (1 - helperOut) * 1.2F, z, presence, turn, 0, scale, farewell ? HELPER_LABELS[j] : null);
        }
        List<String> crew = entity.crew();
        int count = crew.size();
        if (count > 0) {
            float enter = Math.min(8.0F, 56.0F / count), leave = Math.min(16.0F, 96.0F / count);
            float exitFrom = 96 * bar;
            for (int i = 0; i < count; i++) {
                String name = crew.get(i);
                float in = Mth.clamp((song - (43 * bar - 12 + i * enter)) / 18.0F, 0, 1);
                float out = 1 - Mth.clamp((song - (exitFrom + (count - 1 - i) * leave)) / bar, 0, 1);
                if (in <= 0 || out <= 0) continue;
                float lx = HappyWaterGodStatue.seatX(3 + helpers + i), lz = HappyWaterGodStatue.seatZ(3 + helpers + i);
                float x = 0.5F + rx * lx + fx * lz, z = 0.5F + rz * lx + fz * lz;
                Dancer d = stage.crew.computeIfAbsent(name, k -> new Dancer());
                if (in >= 1 && !d.shown) {
                    burst(entity, x, 0.1F, z, ParticleTypes.FIREWORK, 18, 0.15F);
                    d.shown = true;
                }
                if (out < 0.02F && !d.gone) {
                    burst(entity, x, 1.6F, z, ParticleTypes.END_ROD, 12, 0.05F);
                    d.gone = true;
                }
                float rise = (1 - bounceOut(in)) * 3.5F + (1 - out) * 1.2F;
                float presence = Math.min(Math.min(1, in * 3), out);
                float amp = 0.88F + 0.24F * ((name.hashCode() & 255) / 255.0F);
                int own = move == HOLD || move == SOFT_WAVE ? SOFT_WAVE : move;
                perform(entity, poseStack, buffer, d, skin(name), song, own, false, amp, facing, x, rise, z, presence, 0, (i % 3) * 0.6F, 1 - 0.2F * (1 - out), null);
            }
        }
        var recap = entity.recap();
        if (!recap.isEmpty()) {
            if (stage.recap == null || stage.recap.list != recap) stage.recap = new HappyWaterGodRecap(recap);
            stage.recap.render(entity, poseStack, buffer, song, fx, fz, rx, rz, facing, fade);
        }
        disco(entity, stage, poseStack, LATE, time, Math.min(fade, dawn * dawn), song);
    }

    public static void preview(int bar) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        HappyWaterGodStatue.Entity best = null;
        double nearest = 48 * 48;
        for (HappyWaterGodStatue.Entity show : HappyWaterGodStatue.CLIENT_SHOWS) {
            if (show.getLevel() != mc.level || show.isRemoved()) continue;
            double d = show.getBlockPos().distToCenterSqr(mc.player.position());
            if (d < nearest) {
                nearest = d;
                best = show;
            }
        }
        if (best == null) {
            mc.player.displayClientMessage(Component.translatable(HappyWaterGodStatue.RECAP_NO_STATUE).withStyle(ChatFormatting.RED), false);
            return;
        }
        long elapsed = bar <= 0 ? 0 : HappyWaterGodStatue.SONG_LEAD + (long) bar * HappyWaterGodStatue.SONG_BAR;
        best.preview(mc.level.getGameTime() - elapsed, HappyWaterGodStatue.fakeRecap(mc.player.getGameProfile().getName()));
    }

    private void perform(HappyWaterGodStatue.Entity entity, PoseStack poseStack, MultiBufferSource buffer, Dancer d, Skin skin, float song, int move, boolean mirror, float amp, float facing,
                         float x, float y, float z, float presence, float turn, float delay, float scale, @Nullable Component label) {
        if (presence <= 0) return;
        PlayerModel<LivingEntity> model = skin.slim() ? thin : wide;
        dance(d, model, song - delay, move, mirror, amp);
        poseStack.pushPose();
        body(poseStack, facing, x, y, z, turn, scale);
        RenderType type = presence < 0.999F ? RenderType.entityTranslucent(skin.texture()) : RenderType.entityCutoutNoCull(skin.texture());
        model.renderToBuffer(poseStack, buffer.getBuffer(type), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, presence);
        poseStack.popPose();
        if (label != null && presence > 0.05F) {
            float size = Math.min(1, presence * 1.5F);
            bubble(poseStack, buffer, model, facing, x, y, z, turn, scale, mirror, size);
            nameplate(poseStack, buffer, label, x, y + bounce + 2.45F * scale, z, LightTexture.FULL_BRIGHT, size);
        }
    }

    private static void burst(HappyWaterGodStatue.Entity entity, float x, float y, float z, SimpleParticleType type, int count, float speed) {
        var level = entity.getLevel();
        if (level == null) return;
        BlockPos pos = entity.getBlockPos();
        RandomSource random = level.random;
        for (int i = 0; i < count; i++) {
            float a = random.nextFloat() * Mth.TWO_PI;
            float up = random.nextFloat();
            level.addParticle(type, pos.getX() + x, pos.getY() + y + up * 1.8F, pos.getZ() + z, Mth.cos(a) * speed, speed * (0.5F + up), Mth.sin(a) * speed);
        }
    }

    private static float backOut(float t) {
        float u = t - 1;
        return 1 + 2.70158F * u * u * u + 1.70158F * u * u;
    }

    private static float cubicOut(float t) {
        float u = 1 - t;
        return 1 - u * u * u;
    }

    private static float bounceOut(float t) {
        if (t < 1 / 2.75F) return 7.5625F * t * t;
        if (t < 2 / 2.75F) {
            t -= 1.5F / 2.75F;
            return 7.5625F * t * t + 0.75F;
        }
        if (t < 2.5F / 2.75F) {
            t -= 2.25F / 2.75F;
            return 7.5625F * t * t + 0.9375F;
        }
        t -= 2.625F / 2.75F;
        return 7.5625F * t * t + 0.984375F;
    }

    private static int choreo(float song) {
        int bar = Math.floorDiv(Mth.floor(song / HappyWaterGodStatue.SONG_BEAT), 4);
        if (song < 0) return IDLE;
        return bar < CHOREO.length ? CHOREO[bar] : FINAL;
    }

    private static void dance(Dancer d, PlayerModel<LivingEntity> model, float song, int move, boolean mirror, float amp) {
        model.setAllVisible(true);
        float beat = song / HappyWaterGodStatue.SONG_BEAT;
        int k = Mth.floor(beat);
        boolean soft = move == SKY || move == SOFT_WAVE;
        target(move, beat, TARGET);
        if (amp != 1) {
            for (int i = 0; i < KEYS; i++) if (i != YAW && i != LIFT && i != SQUASH && i != PITCH) TARGET[i] *= amp;
        }
        if (mirror) mirror(TARGET);
        float accent = move == IDLE || move == FINAL ? 0 : soft ? 0.3F : move == HOLD ? 0.6F : 1;
        float dt = (song - d.last) / 20.0F;
        d.last = song;
        if (!d.ready || dt < 0 || dt > 0.5F) {
            System.arraycopy(TARGET, 0, d.x, 0, KEYS);
            java.util.Arrays.fill(d.v, 0);
            d.beat = k;
            d.ready = true;
            dt = 0;
        }
        if (k != d.beat) {
            d.beat = k;
            d.v[SQUASH] -= 2.2F * accent;
            d.v[HX] += 3.0F * accent;
            if (move == CHORUS_V || move == CHEEK || move == HEART || move == JUMP_STAR) d.v[LIFT] += 1.6F;
            if (move == HOLD && (Math.floorDiv(k, 4) & 3) == 1) d.v[LIFT] += 0.9F;
            if (move == SPIN && Math.floorMod(k, 8) == 6) {
                d.yawBase += 360.0F * d.spinDir;
                d.spinDir = -d.spinDir;
            }
        }
        TARGET[YAW] += mirror ? -d.yawBase : d.yawBase;
        int steps = Mth.ceil(dt / 0.008F);
        float h = steps == 0 ? 0 : dt / steps;
        for (int s = 0; s < steps; s++) {
            for (int i = 0; i < KEYS; i++) {
                float w = OMEGA[i];
                float a = w * w * (TARGET[i] - d.x[i]) - 2 * ZETA[i] * w * d.v[i];
                d.v[i] += a * h;
                d.x[i] += d.v[i] * h;
            }
        }
        float[] x = d.x;
        bounce = Math.max(-0.05F, x[LIFT]);
        yaw = x[YAW];
        roll = x[ROLL];
        pitch = x[PITCH];
        squash = Mth.clamp(x[SQUASH], -0.18F, 0.18F);
        float bodyZ = 0.075F * x[HIP];
        float shift = 12.0F * Mth.sin(bodyZ);
        float rx = Math.max(x[RX], -3.1F), lx = Math.max(x[LX], -3.1F);
        float rz = Mth.clamp(x[RZ], -0.9F, 2.45F), lz = Mth.clamp(x[LZ], -2.45F, 0.9F);
        float rc = Mth.cos(rx), lc = Mth.cos(lx);
        if (rc < -0.6F) rz = Math.min(rz, -(float) Math.asin(Math.min(1, 0.16F / -rc)));
        if (lc < -0.6F) lz = Math.max(lz, (float) Math.asin(Math.min(1, 0.16F / -lc)));
        float up = Math.max(-Mth.cos(rz) * rc, -Mth.cos(lz) * lc);
        float headLimit = Mth.lerp(Mth.clamp(up * 1.6F - 0.4F, 0, 1), 0.7F, 0.12F);
        model.head.setRotation(x[HX], Mth.clamp(x[HY], -headLimit, headLimit), Mth.clamp(x[HZ] - 0.5F * bodyZ, -headLimit, headLimit));
        model.body.setRotation(0, x[TWIST], bodyZ);
        model.rightArm.setRotation(rx, x[RY], rz);
        model.leftArm.setRotation(lx, x[LY], lz);
        model.rightLeg.setRotation(x[RLEG], 0, -bodyZ + x[LEGOUT]);
        model.leftLeg.setRotation(x[LLEG], 0, -bodyZ - x[LEGOUT]);
        model.rightLeg.x = -1.9F - shift;
        model.leftLeg.x = 1.9F - shift;
        model.rightLeg.y = 12.0F * Mth.cos(bodyZ);
        model.leftLeg.y = model.rightLeg.y;
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }

    private static void mirror(float[] o) {
        float rx = o[RX], ry = o[RY], rz = o[RZ];
        o[RX] = o[LX];
        o[RY] = -o[LY];
        o[RZ] = -o[LZ];
        o[LX] = rx;
        o[LY] = -ry;
        o[LZ] = -rz;
        float leg = o[RLEG];
        o[RLEG] = o[LLEG];
        o[LLEG] = leg;
        o[HY] = -o[HY];
        o[HZ] = -o[HZ];
        o[HIP] = -o[HIP];
        o[TWIST] = -o[TWIST];
        o[ROLL] = -o[ROLL];
        o[YAW] = -o[YAW];
    }

    private static void target(int move, float beat, float[] o) {
        int k = Mth.floor(beat);
        int b = Math.floorMod(k, 4);
        int bar = Math.floorDiv(k, 4);
        float side = (bar & 2) == 0 ? 1 : -1;
        float alt = (k & 1) == 0 ? 1 : -1;
        float wave = Mth.sin(beat * Mth.PI);
        float slow = Mth.sin(beat * Mth.HALF_PI);
        java.util.Arrays.fill(o, 0);
        o[RZ] = 0.15F;
        o[LZ] = -0.15F;
        o[YAW] = 8.0F * side;
        switch (move) {
            case CLAP -> {
                o[RX] = o[LX] = alt > 0 ? -1.35F : -1.65F;
                o[RZ] = alt > 0 ? 0.95F : -0.22F;
                o[LZ] = -o[RZ];
                o[HIP] = 0.8F * alt;
                o[HZ] = 0.12F * alt;
            }
            case FRONT_WAVE -> {
                o[RX] = o[LX] = -1.5F;
                o[RY] = o[LY] = 0.6F * alt;
                o[RZ] = 0.1F;
                o[LZ] = -0.1F;
                o[HIP] = -1.3F * alt;
                o[HZ] = 0.16F * alt;
            }
            case HIP_BUMP -> {
                o[RX] = o[LX] = 0.25F;
                o[RZ] = 0.75F;
                o[LZ] = -0.75F;
                o[HIP] = 1.4F * alt;
                o[HZ] = -0.2F * alt;
                o[TWIST] = 0.2F * alt;
                o[ROLL] = 3.0F * alt;
            }
            case CHEEK -> {
                o[RX] = o[LX] = -1.85F;
                o[RY] = -0.4F;
                o[LY] = 0.4F;
                o[RZ] = 0;
                o[LZ] = 0;
                o[HZ] = 0.32F * alt;
                o[HX] = 0.1F;
                o[HIP] = 0.7F * alt;
            }
            case PUMP -> {
                switch (b) {
                    case 0 -> {
                        o[RX] = -2.95F;
                        o[RZ] = 0.3F;
                        o[LX] = 0.35F;
                        o[HY] = 0.5F;
                        o[TWIST] = 0.3F;
                        o[HIP] = 1;
                    }
                    case 1 -> {
                        o[RX] = o[LX] = -0.7F;
                        o[HIP] = -1;
                    }
                    case 2 -> {
                        o[LX] = -2.95F;
                        o[LZ] = -0.3F;
                        o[RX] = 0.35F;
                        o[HY] = -0.5F;
                        o[TWIST] = -0.3F;
                        o[HIP] = 1;
                    }
                    default -> {
                        o[RX] = o[LX] = -3.0F;
                        o[RZ] = 0.25F;
                        o[LZ] = -0.25F;
                        o[HX] = -0.3F;
                        o[HIP] = -1;
                    }
                }
            }
            case KICK -> {
                o[RZ] = 1.35F;
                o[LZ] = -1.35F;
                o[RX] = o[LX] = -0.2F;
                if (alt > 0) o[RLEG] = -1.05F;
                else o[LLEG] = -1.05F;
                o[HX] = -0.15F;
                o[TWIST] = 0.22F * alt;
                o[HIP] = 0.5F * alt;
            }
            case RAISE_ROOF -> {
                o[RX] = o[LX] = alt > 0 ? -3.05F : -2.55F;
                o[RZ] = 0.35F;
                o[LZ] = -0.35F;
                o[HX] = -0.3F;
                o[HIP] = 0.9F * alt;
            }
            case ARM_ROLL -> {
                float ph = beat * Mth.PI;
                o[RX] = -1.3F + 0.55F * Mth.sin(ph);
                o[LX] = -1.3F - 0.55F * Mth.sin(ph);
                o[RZ] = 0.45F + 0.3F * Mth.cos(ph);
                o[LZ] = -0.45F + 0.3F * Mth.cos(ph);
                o[HIP] = 1.1F * Mth.sin(ph);
                o[HY] = 0.3F * slow;
            }
            case CHORUS_V -> {
                o[RZ] = 2.2F + 0.3F * alt;
                o[LZ] = -2.2F + 0.3F * alt;
                o[RX] = o[LX] = -0.2F;
                o[HIP] = 1.35F * alt;
                o[HZ] = 0.2F * alt;
            }
            case HEART -> {
                o[RX] = o[LX] = -2.75F;
                o[RZ] = 0.12F;
                o[LZ] = -0.12F;
                o[HZ] = 0.3F * alt;
                o[HIP] = 1.0F * alt;
                o[YAW] = 0;
            }
            case SWIM -> {
                o[RZ] = 2.0F + 0.5F * wave;
                o[LZ] = -2.0F + 0.5F * wave;
                o[RX] = o[LX] = -0.3F;
                o[HIP] = 1.3F * wave;
                o[HZ] = 0.2F * wave;
            }
            case CHEST -> {
                if (b < 2) {
                    o[RX] = -0.95F;
                    o[RY] = -0.7F;
                    o[RZ] = 0;
                    o[LX] = 0.1F;
                    o[LZ] = -0.1F;
                    o[HX] = 0.42F;
                    o[HZ] = 0.15F;
                } else {
                    o[RZ] = 1.75F;
                    o[LZ] = -1.75F;
                    o[RX] = o[LX] = -0.35F;
                    o[HX] = -0.35F;
                }
                o[YAW] = 0;
            }
            case POINT -> {
                float up = alt > 0 ? 1 : 0;
                float px = -0.4F - 0.35F * (1 - up), pz = -0.55F + 3.0F * up;
                if (side > 0) {
                    o[RX] = px;
                    o[RZ] = pz;
                    o[LX] = 0.25F;
                    o[LZ] = -0.6F;
                } else {
                    o[LX] = px;
                    o[LZ] = -pz;
                    o[RX] = 0.25F;
                    o[RZ] = 0.6F;
                }
                o[HX] = -0.35F * up + 0.2F * (1 - up);
                o[HY] = 0.35F * side;
                o[HIP] = 0.9F * alt;
                o[ROLL] = 4.0F * side * up;
            }
            case SKY -> {
                o[RZ] = 2.05F + 0.35F * slow;
                o[LZ] = -2.05F + 0.35F * slow;
                o[RX] = o[LX] = -0.3F;
                o[HX] = -0.42F;
                o[HZ] = 0.25F * slow;
                o[HIP] = 1.3F * slow;
                o[ROLL] = 4.0F * slow;
                o[YAW] = 0;
            }
            case SOFT_WAVE -> {
                o[RX] = -0.45F;
                o[RZ] = 2.05F + 0.4F * wave;
                o[LX] = -0.9F;
                o[LY] = 0.6F;
                o[LZ] = 0;
                o[HX] = 0.28F;
                o[HZ] = -0.18F;
                o[HIP] = 0.6F * slow;
                o[YAW] = 0;
            }
            case SPIN -> {
                int p8 = Math.floorMod(k, 8);
                if (p8 >= 6) {
                    o[RX] = o[LX] = -0.6F;
                    o[RZ] = 0.5F;
                    o[LZ] = -0.5F;
                    o[HX] = -0.2F;
                } else {
                    o[RZ] = 1.5F + 0.35F * alt;
                    o[LZ] = -1.5F + 0.35F * alt;
                    o[RX] = o[LX] = -0.1F;
                    o[HY] = 0.3F * alt;
                    o[HIP] = alt;
                    o[YAW] = 18.0F * Mth.sin(beat * Mth.PI * 0.25F);
                }
            }
            case CLAP_UP -> {
                if (alt < 0) {
                    o[RX] = o[LX] = -2.8F;
                    o[RZ] = 0.06F;
                    o[LZ] = -0.06F;
                    o[HX] = -0.25F;
                } else {
                    o[RZ] = 2.2F;
                    o[LZ] = -2.2F;
                    o[RX] = o[LX] = -0.3F;
                }
                o[HIP] = 0.8F * alt;
                o[YAW] = 0;
            }
            case STEP_TOUCH -> {
                o[HIP] = 1.0F * alt;
                if (alt > 0) o[RLEG] = -0.4F;
                else o[LLEG] = -0.4F;
                o[RX] = 0.5F * alt;
                o[LX] = -0.5F * alt;
                o[RZ] = 0.35F;
                o[LZ] = -0.35F;
                o[HZ] = 0.1F * alt;
                o[YAW] = 6.0F * alt;
            }
            case LOOK -> {
                float dir = (Math.floorDiv(k, 2) & 1) == 0 ? 1 : -1;
                o[RX] = -2.05F;
                o[RY] = -0.5F;
                o[RZ] = 0;
                o[LX] = 0.25F;
                o[LZ] = -0.75F;
                o[HY] = 0.6F * dir;
                o[TWIST] = 0.2F * dir;
                o[HIP] = 0.6F * dir;
                o[YAW] = 10.0F * dir;
            }
            case DAB -> {
                if (b < 2) {
                    o[RZ] = 2.2F;
                    o[RX] = -0.6F;
                    o[LX] = -1.7F;
                    o[LY] = 0.85F;
                    o[LZ] = 0;
                    o[HX] = 0.4F;
                    o[HY] = 0.4F;
                    o[PITCH] = 8.0F;
                } else {
                    o[RZ] = 0.9F;
                    o[LZ] = -0.9F;
                    o[HIP] = 1.2F * alt;
                    o[HZ] = 0.2F * alt;
                }
            }
            case SHIMMY -> {
                float fast = Mth.sin(beat * Mth.TWO_PI * 2);
                o[RZ] = 0.9F;
                o[LZ] = -0.9F;
                o[RX] = o[LX] = -0.4F;
                o[TWIST] = 0.28F * fast;
                o[HX] = -0.1F;
                o[HZ] = 0.08F * fast;
                o[HIP] = 0.5F * slow;
            }
            case ROBOT -> {
                switch (b) {
                    case 0 -> {
                        o[RX] = -1.57F;
                        o[RZ] = 0.05F;
                        o[HY] = 0.5F;
                    }
                    case 1 -> {
                        o[RX] = o[LX] = -1.57F;
                        o[RZ] = 0.05F;
                        o[LZ] = -0.05F;
                    }
                    case 2 -> {
                        o[LX] = -3.0F;
                        o[LZ] = -0.1F;
                        o[HY] = -0.5F;
                    }
                    default -> {
                        o[RZ] = 1.57F;
                        o[LZ] = -1.57F;
                        o[HX] = -0.3F;
                    }
                }
                o[YAW] = 15.0F * side;
            }
            case JUMP_STAR -> {
                if (alt > 0) {
                    o[RZ] = 2.4F;
                    o[LZ] = -2.4F;
                    o[RX] = o[LX] = -0.2F;
                    o[LEGOUT] = 0.35F;
                    o[HX] = -0.2F;
                } else {
                    o[RZ] = 0.1F;
                    o[LZ] = -0.1F;
                }
                o[YAW] = 0;
            }
            case BOW -> {
                if (b < 2) {
                    o[PITCH] = 25.0F;
                    o[HX] = 0.5F;
                    o[RX] = -0.95F;
                    o[RY] = -0.7F;
                    o[RZ] = 0;
                    o[LX] = 0.4F;
                    o[LZ] = -0.3F;
                } else {
                    o[RX] = -0.45F;
                    o[RZ] = 2.0F + 0.4F * wave;
                    o[LX] = -0.9F;
                    o[LY] = 0.6F;
                    o[LZ] = 0;
                    o[HX] = 0.2F;
                }
                o[YAW] = 0;
            }
            case FINAL -> {
                o[RX] = -1.95F;
                o[RZ] = 0.4F;
                o[LX] = -0.3F;
                o[LZ] = -2.0F - 0.35F * slow;
                o[HZ] = 0.15F;
                o[HX] = 0.1F;
                o[YAW] = 0;
            }
            case IDLE -> {
                float breath = Mth.sin(beat * 0.6F);
                o[RZ] = 0.1F + 0.03F * breath;
                o[LZ] = -0.1F - 0.03F * breath;
                o[HX] = 0.05F * breath;
                o[YAW] = 0;
            }
            case HOLD -> {
                o[RX] = -1.95F + 0.1F * slow;
                o[RZ] = 0.4F;
                o[YAW] = 0;
                switch (bar & 3) {
                    case 0 -> {
                        o[LX] = -0.3F;
                        o[LZ] = -2.0F - 0.4F * wave;
                        o[HZ] = 0.2F;
                        o[HIP] = 0.8F * slow;
                    }
                    case 1 -> {
                        o[LX] = -0.95F;
                        o[LY] = 0.7F;
                        o[LZ] = 0;
                        o[HX] = 0.15F;
                        o[HZ] = 0.15F * alt;
                        o[HIP] = 0.6F * slow;
                    }
                    case 2 -> {
                        o[LX] = 0.4F * alt;
                        o[LZ] = -0.3F;
                        o[HIP] = 1.3F * alt;
                        o[ROLL] = 3.0F * alt;
                        if (alt > 0) o[RLEG] = -0.35F;
                        else o[LLEG] = -0.35F;
                    }
                    default -> {
                        o[RX] = -2.15F;
                        o[HX] = 0.35F;
                        o[LX] = -0.2F;
                        o[LZ] = -0.9F - 0.35F * wave;
                        o[YAW] = 10.0F * side;
                    }
                }
            }
            default -> {
                o[RX] = 0.4F * alt;
                o[LX] = -0.4F * alt;
                o[RZ] = 0.3F;
                o[LZ] = -0.3F;
                o[HY] = 0.25F * side;
                o[HIP] = 0.95F * alt;
                if (alt > 0) o[RLEG] = -0.25F;
                else o[LLEG] = -0.25F;
            }
        }
    }

    private static final class Dancer {

        final float[] x = new float[KEYS];
        final float[] v = new float[KEYS];
        float last, yawBase, spinDir = 1;
        int beat;
        boolean ready, shown, gone;
    }

    private static final class Stage {

        final Dancer lead = new Dancer();
        final Dancer[] duo = { new Dancer(), new Dancer() };
        final Dancer[] helpers = java.util.stream.Stream.generate(Dancer::new).limit(HappyWaterGodStatue.HELPERS.length).toArray(Dancer[]::new);
        final Map<String, Dancer> crew = new HashMap<>();
        final float[] look = new float[LOOK_KEYS];
        float last, dt, spin, hue;
        boolean lookReady;
        @Nullable
        HappyWaterGodRecap recap;
    }

    private static final class Skin {

        final ResourceLocation fallback;
        final boolean fallbackSlim;
        volatile ResourceLocation texture;
        volatile boolean slim;

        Skin(String name) {
            UUID id = UUIDUtil.createOfflinePlayerUUID(name);
            fallback = DefaultPlayerSkin.getDefaultSkin(id);
            fallbackSlim = "slim".equals(DefaultPlayerSkin.getSkinModelName(id));
        }

        ResourceLocation texture() {
            ResourceLocation t = texture;
            return t != null ? t : fallback;
        }

        boolean slim() {
            return texture != null ? slim : fallbackSlim;
        }
    }

    private static Skin skin(String name) {
        return SKINS.computeIfAbsent(name.toLowerCase(Locale.ROOT), key -> {
            Skin skin = new Skin(name);
            Boolean bundledSlim = BUNDLED_SKINS.get(key);
            if (bundledSlim != null) {
                skin.slim = bundledSlim;
                skin.texture = GTOCore.id("textures/entity/happy_water_god/" + key + ".png");
            } else if (!name.equals(HappyWaterGodStatue.OFFLINE_HELPER)) fetch(name, skin);
            return skin;
        });
    }

    private static void fetch(String name, Skin skin) {
        Util.backgroundExecutor().execute(() -> {
            try {
                Minecraft mc = Minecraft.getInstance();
                GameProfile[] found = new GameProfile[1];
                new YggdrasilAuthenticationService(mc.getProxy()).createProfileRepository().findProfilesByNames(new String[] { name }, Agent.MINECRAFT, new ProfileLookupCallback() {

                    @Override
                    public void onProfileLookupSucceeded(GameProfile profile) {
                        found[0] = profile;
                    }

                    @Override
                    public void onProfileLookupFailed(GameProfile profile, Exception exception) {}
                });
                if (found[0] == null) return;
                GameProfile full = mc.getMinecraftSessionService().fillProfileProperties(found[0], false);
                MinecraftProfileTexture raw = mc.getMinecraftSessionService().getTextures(full, false).get(MinecraftProfileTexture.Type.SKIN);
                boolean thinArms = raw != null && thinArms(raw);
                mc.execute(() -> mc.getSkinManager().registerSkins(full, (type, location, texture) -> {
                    if (type != MinecraftProfileTexture.Type.SKIN) return;
                    skin.slim = thinArms || "slim".equals(texture.getMetadata("model"));
                    skin.texture = location;
                }, false));
            } catch (Throwable ignored) {}
        });
    }

    private static void body(PoseStack poseStack, float facing, float x, float y, float z, float turn, float scale) {
        poseStack.translate(x, y + bounce, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing + yaw + turn));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.scale(scale * (1 - 0.5F * squash), scale * (1 + squash), scale * (1 - 0.5F * squash));
        poseStack.scale(-0.9375F, -0.9375F, 0.9375F);
        poseStack.translate(0, -1.501, 0);
    }

    private static void bubble(PoseStack poseStack, MultiBufferSource buffer, PlayerModel<LivingEntity> model, float facing, float x, float y, float z, float turn, float scale, boolean left, float size) {
        PoseStack hand = new PoseStack();
        body(hand, facing, x, y, z, turn, scale);
        (left ? model.leftArm : model.rightArm).translateAndRotate(hand);
        hand.translate(left ? 0.0625F : -0.0625F, 0.72F, 0);
        Vector3f at = hand.last().pose().getTranslation(new Vector3f());
        var mc = Minecraft.getInstance();
        Font font = mc.font;
        Component text = bubbleText();
        int w = font.width(text);
        poseStack.pushPose();
        poseStack.translate(at.x, at.y, at.z);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        float s = 0.013F * size;
        poseStack.scale(-s, -s, s);
        Matrix4f m = poseStack.last().pose();
        VertexConsumer c = buffer.getBuffer(Types.BALL);
        float x0 = -w / 2.0F - 6, x1 = w / 2.0F + 6, y1 = -10, y0 = y1 - 21;
        int ink = 0xFF3A2E2A, paper = 0xFFFFFFFF;
        rect(c, m, x0, y0, x1, y1, paper);
        rect(c, m, x0, y0 - 1.5F, x1, y0, ink);
        rect(c, m, x0 - 1.5F, y0, x0, y1, ink);
        rect(c, m, x1, y0, x1 + 1.5F, y1, ink);
        rect(c, m, x0, y1, -5, y1 + 1.5F, ink);
        rect(c, m, 5, y1, x1, y1 + 1.5F, ink);
        quad(c, m, -3.5F, y1, -0.4F, y1 + 7, 0.4F, y1 + 7, 3.5F, y1, paper);
        quad(c, m, -5, y1, -1.2F, y1 + 8.5F, -0.4F, y1 + 7, -3.5F, y1, ink);
        quad(c, m, 3.5F, y1, 0.4F, y1 + 7, 1.2F, y1 + 8.5F, 5, y1, ink);
        quad(c, m, -1.2F, y1 + 8.5F, 1.2F, y1 + 8.5F, 0.4F, y1 + 7, -0.4F, y1 + 7, ink);
        font.drawInBatch(text, -w / 2.0F, y0 + 6, ink, false, m, buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void rect(VertexConsumer c, Matrix4f m, float x0, float y0, float x1, float y1, int argb) {
        quad(c, m, x0, y0, x0, y1, x1, y1, x1, y0, argb);
    }

    private static void quad(VertexConsumer c, Matrix4f m, float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy, int argb) {
        int a = argb >>> 24, r = argb >> 16 & 255, g = argb >> 8 & 255, b = argb & 255;
        c.vertex(m, ax, ay, 0).color(r, g, b, a).endVertex();
        c.vertex(m, bx, by, 0).color(r, g, b, a).endVertex();
        c.vertex(m, cx, cy, 0).color(r, g, b, a).endVertex();
        c.vertex(m, dx, dy, 0).color(r, g, b, a).endVertex();
    }

    public static final class Late {

        public static void init() {
            var bus = MinecraftForge.EVENT_BUS;
            bus.addListener(Late::onStage);
            bus.addListener(Late::onGui);
            bus.addListener(Late::onTick);
        }

        public static void onStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
            PoseStack view = RenderSystem.getModelViewStack();
            view.pushPose();
            view.setIdentity();
            RenderSystem.applyModelViewMatrix();
            LATE.endBatch();
            view.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        public static void onGui(RenderGuiEvent.Post event) {
            var mc = Minecraft.getInstance();
            if (mc.level == null || warnedAt == Long.MIN_VALUE) return;
            float t = mc.level.getGameTime() - warnedAt + event.getPartialTick();
            if (t < 0 || t > 40) return;
            float alpha = t < 5 ? t / 5 : t > 35 ? (40 - t) / 5 : 1;
            int a = (int) (alpha * 255);
            if (a <= 8) return;
            var graphics = event.getGuiGraphics();
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(graphics.guiWidth() / 2.0F, graphics.guiHeight() / 2.0F + 33, 0);
            pose.scale(1.5F, 1.5F, 1.5F);
            graphics.drawString(mc.font, WARNING_HINT, -mc.font.width(WARNING_HINT) / 2, 0, a << 24 | 0xFF5555, true);
            pose.popPose();
        }

        public static void onTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            var mc = Minecraft.getInstance();
            if (mc.level == null || mc.isPaused()) return;
            for (HappyWaterGodStatue.Entity show : HappyWaterGodStatue.CLIENT_SHOWS) {
                if (show.getLevel() == mc.level && !show.isRemoved()) cue(mc, show);
            }
        }

        private static void cue(Minecraft mc, HappyWaterGodStatue.Entity show) {
            long start = show.start();
            if (start == HappyWaterGodStatue.IDLE) {
                show.heard = Integer.MIN_VALUE;
                return;
            }
            if (show.heard == Integer.MIN_VALUE) {
                skin(HappyWaterGodStatue.LEAD);
                for (String dev : HappyWaterGodStatue.DEVS) skin(dev);
                for (String helper : HappyWaterGodStatue.HELPERS) skin(helper);
                for (String name : show.crew()) skin(name);
            }
            int now = (int) (mc.level.getGameTime() - start);
            int from = show.heard != Integer.MIN_VALUE ? Math.max(show.heard + 1, now - 4) : now < 0 ? Math.max(-HappyWaterGodStatue.COUNTDOWN_TICKS, now - 10) : now;
            for (int t = from; t <= now; t++) cue(mc, show, t);
            show.heard = Math.max(show.heard, now);
        }

        private static void cue(Minecraft mc, HappyWaterGodStatue.Entity show, int t) {
            BlockPos pos = show.getBlockPos();
            double x = pos.getX() + 0.5, y = pos.getY() + 1.5, z = pos.getZ() + 0.5;
            float gain = HappyWaterGodStatue.soundGain(mc.gameRenderer.getMainCamera().getPosition().distanceTo(new Vec3(x, y, z)));
            if (gain <= 0) return;
            if (t < 0) {
                int since = t + HappyWaterGodStatue.COUNTDOWN_TICKS;
                if (since < 0 || since % HappyWaterGodStatue.WARNING_EVERY != 0) return;
                int index = since / HappyWaterGodStatue.WARNING_EVERY;
                play(mc, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, gain, 0.5F + 0.12F * index, x, y, z);
                play(mc, SoundEvents.NOTE_BLOCK_BASEDRUM.value(), SoundSource.BLOCKS, gain, 0.6F, x, y, z);
                mc.gui.setTimes(5, 30, 5);
                mc.gui.setSubtitle(WARNING_DETAIL);
                mc.gui.setTitle(WARNING);
                warnedAt = mc.level.getGameTime();
                return;
            }
            int m = HappyWaterGodStatue.musicTick(t, show.shortcut());
            int n = HappyWaterGodStatue.notes(m);
            for (int i = 0; i < n; i++) {
                play(mc, HappyWaterGodStatue.noteSound(m, i), SoundSource.RECORDS, HappyWaterGodStatue.noteVolume(m, i) * gain, HappyWaterGodStatue.notePitch(m, i), x, y, z);
            }
        }

        private static void play(Minecraft mc, SoundEvent sound, SoundSource source, float volume, float pitch, double x, double y, double z) {
            mc.getSoundManager().play(new SimpleSoundInstance(sound.getLocation(), source, volume, pitch, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, x, y, z, false));
        }
    }

    private static Vector3f[] sphere(int count) {
        Vector3f[] dirs = new Vector3f[count];
        float golden = Mth.PI * (3 - Mth.sqrt(5));
        for (int i = 0; i < count; i++) {
            float y = 1 - 2 * (i + 0.5F) / count;
            float r = Mth.sqrt(1 - y * y);
            float theta = golden * i;
            dirs[i] = new Vector3f(Mth.cos(theta) * r, y, Mth.sin(theta) * r);
        }
        return dirs;
    }

    private static void disco(HappyWaterGodStatue.Entity entity, Stage d, PoseStack poseStack, MultiBufferSource buffer, float time, float fade, float song) {
        var level = entity.getLevel();
        if (level == null || fade <= 0) return;
        float[] want = LOOKS[HappyWaterGodStatue.songPart(song)];
        float[] look = d.look;
        if (!d.lookReady) {
            System.arraycopy(want, 0, look, 0, LOOK_KEYS);
            d.lookReady = true;
        } else {
            float k = 1 - (float) Math.exp(-d.dt * 1.2F);
            for (int i = 0; i < LOOK_KEYS; i++) look[i] += (want[i] - look[i]) * k;
        }
        d.spin += d.dt * 0.7F * look[SPEED];
        d.hue += d.dt * 0.08F * look[SPEED];
        float beat = song / HappyWaterGodStatue.SONG_BEAT;
        float frac = beat - Mth.floor(beat);
        float hit = (1 - frac) * (1 - frac) * (1 - frac);
        float flash = Mth.clamp(1 + look[STROBE] * (1.2F * hit - 0.35F), 0.3F, 1.6F) * fade;
        Quaternionf spin = new Quaternionf().rotationY(d.spin).rotateX(0.35F * Mth.sin(time * 0.013F));
        Matrix4f pose = poseStack.last().pose();
        BlockPos origin = entity.getBlockPos();
        float ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
        Vec3 centre = new Vec3(ox + 0.5, oy + BALL_Y, oz + 0.5);
        ball(buffer.getBuffer(Types.BALL), pose, spin, time, Math.min(1, 0.2F + 0.8F * flash));
        VertexConsumer rays = buffer.getBuffer(Types.RAYS);
        Vector3f dir = new Vector3f();
        int n = BEAM_DIRS.length;
        float active = look[COUNT] * n;
        float range = BEAM_RANGE * look[RANGE];
        for (int i = 0; i < n; i++) {
            float presence = Mth.clamp(active - i, 0, 1);
            if (presence <= 0) break;
            int idx = i * 37 % n;
            spin.transform(BEAM_DIRS[idx], dir);
            Vec3 from = centre.add(dir.x * BALL_RADIUS, dir.y * BALL_RADIUS, dir.z * BALL_RADIUS);
            Vec3 to = centre.add(dir.x * range, dir.y * range, dir.z * range);
            BlockHitResult hitResult = level.clip(new ClipContext(from, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null));
            boolean landed = hitResult.getType() != HitResult.Type.MISS;
            Vec3 end = landed ? hitResult.getLocation() : to;
            int vivid = Mth.hsvToRgb((idx / (float) n + d.hue) % 1.0F, 0.85F * look[SAT], 1.0F);
            int warm = Mth.hsvToRgb(0.03F + 0.09F * ((idx * 0.618F) % 1.0F), 0.55F, 1.0F);
            float w = look[WARM];
            int r = (int) Mth.lerp(w, vivid >> 16 & 255, warm >> 16 & 255);
            int g = (int) Mth.lerp(w, vivid >> 8 & 255, warm >> 8 & 255);
            int b = (int) Mth.lerp(w, vivid & 255, warm & 255);
            float a = presence * flash;
            int head = Math.min(255, (int) (110 * a)), tail = Math.min(255, (int) (25 * a));
            beam(rays, pose, (float) from.x - ox, (float) from.y - oy, (float) from.z - oz, (float) end.x - ox, (float) end.y - oy, (float) end.z - oz, r, g, b, head, tail, look[WIDTH]);
            if (landed) spot(rays, pose, hitResult, (float) from.distanceTo(end), ox, oy, oz, r, g, b, Math.min(255, (int) (230 * a)), look[WIDTH]);
        }
    }

    private static void ball(VertexConsumer consumer, Matrix4f pose, Quaternionf spin, float time, float bright) {
        int bands = 10, segments = 20;
        long flash = (long) (time / 3);
        Vector3f a = new Vector3f(), b = new Vector3f(), c = new Vector3f(), d = new Vector3f(), n = new Vector3f();
        for (int i = 0; i < bands; i++) {
            float p0 = Mth.PI * i / bands - Mth.HALF_PI, p1 = Mth.PI * (i + 1) / bands - Mth.HALF_PI;
            for (int j = 0; j < segments; j++) {
                float t0 = Mth.TWO_PI * j / segments, t1 = Mth.TWO_PI * (j + 1) / segments;
                onSphere(a, p0, t0, spin);
                onSphere(b, p1, t0, spin);
                onSphere(c, p1, t1, spin);
                onSphere(d, p0, t1, spin);
                onSphere(n, (p0 + p1) / 2, (t0 + t1) / 2, spin);
                float shade = 0.45F + 0.4F * Math.max(0, n.y * 0.6F + n.x * 0.5F + n.z * 0.3F);
                long h = (i * 73856093L) ^ (j * 19349663L) ^ (flash * 83492791L);
                h ^= h >>> 13;
                h *= 0x5DEECE66DL;
                h ^= h >>> 17;
                if ((h & 15) == 0) shade = 1.0F;
                int v = (int) (255 * shade * bright);
                quad(consumer, pose, a, b, c, d, v, v, Math.min(255, v + 20));
            }
        }
    }

    private static void onSphere(Vector3f out, float phi, float theta, Quaternionf spin) {
        out.set(Mth.cos(phi) * Mth.cos(theta), Mth.sin(phi), Mth.cos(phi) * Mth.sin(theta));
        spin.transform(out);
    }

    private static void quad(VertexConsumer consumer, Matrix4f pose, Vector3f a, Vector3f b, Vector3f c, Vector3f d, int r, int g, int bl) {
        corner(consumer, pose, a, r, g, bl);
        corner(consumer, pose, b, r, g, bl);
        corner(consumer, pose, c, r, g, bl);
        corner(consumer, pose, d, r, g, bl);
    }

    private static void corner(VertexConsumer consumer, Matrix4f pose, Vector3f v, int r, int g, int b) {
        consumer.vertex(pose, 0.5F + v.x * BALL_RADIUS, BALL_Y + v.y * BALL_RADIUS, 0.5F + v.z * BALL_RADIUS).color(r, g, b, 255).endVertex();
    }

    private static void beam(VertexConsumer consumer, Matrix4f pose, float x0, float y0, float z0, float x1, float y1, float z1, int r, int g, int b, int head, int tail, float width) {
        Vector3f d = new Vector3f(x1 - x0, y1 - y0, z1 - z0).normalize();
        Vector3f u = (Math.abs(d.y) < 0.9F ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0)).cross(d).normalize();
        Vector3f w = new Vector3f(d).cross(u).normalize();
        side(consumer, pose, u, x0, y0, z0, x1, y1, z1, r, g, b, head, tail, width);
        side(consumer, pose, w, x0, y0, z0, x1, y1, z1, r, g, b, head, tail, width);
    }

    private static void side(VertexConsumer consumer, Matrix4f pose, Vector3f s, float x0, float y0, float z0, float x1, float y1, float z1, int r, int g, int b, int head, int tail, float width) {
        float near = 0.02F * width, far = 0.09F * width;
        triangle(consumer, pose, x0 - s.x * near, y0 - s.y * near, z0 - s.z * near, x0 + s.x * near, y0 + s.y * near, z0 + s.z * near, x1 + s.x * far, y1 + s.y * far, z1 + s.z * far, r, g, b, head, head, tail);
        triangle(consumer, pose, x0 - s.x * near, y0 - s.y * near, z0 - s.z * near, x1 + s.x * far, y1 + s.y * far, z1 + s.z * far, x1 - s.x * far, y1 - s.y * far, z1 - s.z * far, r, g, b, head, tail, tail);
    }

    private static void spot(VertexConsumer consumer, Matrix4f pose, BlockHitResult hit, float distance, float ox, float oy, float oz, int r, int g, int b, int a, float width) {
        Direction face = hit.getDirection();
        Vector3f normal = new Vector3f(face.getStepX(), face.getStepY(), face.getStepZ());
        Vector3f t1 = face.getAxis() == Direction.Axis.Y ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
        Vector3f t2 = new Vector3f(normal).cross(t1);
        float size = (0.12F + distance * 0.018F) * width;
        Vec3 p = hit.getLocation();
        float cx = (float) p.x - ox + normal.x * 0.01F, cy = (float) p.y - oy + normal.y * 0.01F, cz = (float) p.z - oz + normal.z * 0.01F;
        float ax = (t1.x + t2.x) * size, ay = (t1.y + t2.y) * size, az = (t1.z + t2.z) * size;
        float bx = (t1.x - t2.x) * size, by = (t1.y - t2.y) * size, bz = (t1.z - t2.z) * size;
        triangle(consumer, pose, cx + ax, cy + ay, cz + az, cx + bx, cy + by, cz + bz, cx - ax, cy - ay, cz - az, r, g, b, a, a, a);
        triangle(consumer, pose, cx - ax, cy - ay, cz - az, cx - bx, cy - by, cz - bz, cx + ax, cy + ay, cz + az, r, g, b, a, a, a);
    }

    private static void triangle(VertexConsumer consumer, Matrix4f pose, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a0, int a1, int a2) {
        consumer.vertex(pose, x0, y0, z0).color(r, g, b, a0).endVertex();
        consumer.vertex(pose, x1, y1, z1).color(r, g, b, a1).endVertex();
        consumer.vertex(pose, x2, y2, z2).color(r, g, b, a2).endVertex();
    }

    private static void rays(PoseStack poseStack, MultiBufferSource buffer, float glow, float seconds) {
        VertexConsumer consumer = buffer.getBuffer(Types.RAYS);
        RandomSource random = RandomSource.create(20261018L);
        float overdrive = Mth.clamp((seconds - RAMP_SECONDS) / 12.0F, 0, 1);
        int count = (int) (8 + 80 * glow * glow + 260 * overdrive);
        int alpha = (int) (60 + 195 * glow);
        float reach = 0.5F + 4.5F * glow + 20.0F * overdrive;
        float thick = 0.6F + 1.4F * glow + 3.0F * overdrive;
        poseStack.pushPose();
        poseStack.translate(0, 1.1, 0);
        for (int i = 0; i < count; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + seconds * (12.0F + 30.0F * overdrive)));
            float length = (random.nextFloat() * 1.5F + 0.6F) * reach;
            float width = (random.nextFloat() * 0.25F + 0.1F) * thick;
            Matrix4f matrix = poseStack.last().pose();
            centre(consumer, matrix, alpha);
            edge(consumer, matrix, -HALF_SQRT_3 * width, length, -0.5F * width);
            edge(consumer, matrix, HALF_SQRT_3 * width, length, -0.5F * width);
            centre(consumer, matrix, alpha);
            edge(consumer, matrix, HALF_SQRT_3 * width, length, -0.5F * width);
            edge(consumer, matrix, 0, length, width);
            centre(consumer, matrix, alpha);
            edge(consumer, matrix, 0, length, width);
            edge(consumer, matrix, -HALF_SQRT_3 * width, length, -0.5F * width);
        }
        poseStack.popPose();
    }

    public static final class ItemRenderer extends BlockEntityWithoutLevelRenderer {

        private static ItemRenderer instance;
        private PlayerModel<LivingEntity> wide;
        private PlayerModel<LivingEntity> thin;

        private ItemRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        public static ItemRenderer get() {
            if (instance == null) instance = new ItemRenderer();
            return instance;
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
            var mc = Minecraft.getInstance();
            if (wide == null) {
                wide = new PlayerModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
                thin = new PlayerModel<>(mc.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
                wide.young = false;
                thin.young = false;
            }
            float time = mc.level == null ? 0 : mc.level.getGameTime() + mc.getFrameTime();
            Skin lead = skin(HappyWaterGodStatue.LEAD);
            PlayerModel<LivingEntity> model = lead.slim() ? thin : wide;
            ResourceLocation texture = lead.texture();
            pose(model, 0, time, 0);
            poseStack.pushPose();
            poseStack.translate(0.5, 0, 0.5);
            poseStack.scale(0.43F, 0.43F, 0.43F);
            poseStack.translate(-0.5, 0, -0.5);
            mc.getBlockRenderer().renderSingleBlock(GTOBlocks.HAPPY_WATER_GOD_STATUE.get().defaultBlockState(), poseStack, buffer, packedLight, packedOverlay);
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(-0.9375F, -0.9375F, 0.9375F);
            poseStack.translate(0, -1.501, 0);
            model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(texture)), packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            poseStack.popPose();
        }
    }

    private static final class Types extends RenderType {

        static final RenderType RAYS = create("gtocore_happy_water_god_rays", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 1536, false, true,
                CompositeState.builder()
                        .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                        .setWriteMaskState(COLOR_WRITE)
                        .setTransparencyState(LIGHTNING_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));

        static final RenderType BALL = create("gtocore_happy_water_god_disco_ball", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 4096, false, false,
                CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));

        private Types(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling, boolean sort, Runnable setup, Runnable clear) {
            super(name, format, mode, size, crumbling, sort, setup, clear);
        }
    }

    private static void centre(VertexConsumer consumer, Matrix4f matrix, int alpha) {
        consumer.vertex(matrix, 0, 0, 0).color(255, 255, 245, alpha).endVertex();
    }

    private static void edge(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z) {
        consumer.vertex(matrix, x, y, z).color(255, 190, 40, 0).endVertex();
    }
}
