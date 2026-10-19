package com.gtocore.client.renderer;

import com.gtocore.common.block.HappyWaterGodStatue;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestBook;
import com.gtolib.quest.QuestLedger;
import com.gtolib.quest.QuestMilestones;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.List;

/**
 * 雕像片尾的里程碑回顾：逐个在迪斯科球两侧弹出并附说明，飞上高处攒成一圈，随后拼成 GTO 字样，
 * 终章重拍时炸开成绕球旋转的椭圆大环，最后重新拼回字样一直留到谢幕。
 */
final class HappyWaterGodRecap {

    private static final int BAR = HappyWaterGodStatue.SONG_BAR, BEAT = HappyWaterGodStatue.SONG_BEAT;
    static final int APPEAR_FROM = 60, APPEAR_BARS = 24, LOGO_FROM = 84, BURST_AT = 92, REFORM_AT = 98;
    private static final float POP = 8, HOLD = 40, FLY = 18, GATHER = 40, SCATTER = 36, REFORM = 56;
    private static final float[][] SLOTS = { { -3.6F, 7.4F }, { 3.6F, 7.4F }, { -6.4F, 5.2F }, { 6.4F, 5.2F } };
    private static final float SLOT_Z = 1.5F, SLOT_SCALE = 2.4F, TEXT_SCALE = 0.036F;
    private static final float LOGO_Y = 10.2F, LOGO_Z = -0.8F, CELL = 0.7F, CELL_SCALE = 1.0F, TILE = 0.76F;
    private static final float COLLECT_R = 3.0F, COLLECT_SCALE = 1.1F;
    private static final float RING_Y = 6.3F, RING_RX = 5.4F, RING_RY = 2.6F, RING_SCALE = 1.05F;
    private static final String[][] GLYPHS = {
            { ".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".###." },
            { "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#.." },
            { ".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###." } };
    private static final int[] TINTS = { 0xF2B233, 0x3C8CFF, 0xD8E2EA };
    private static final int[] TEXT_COLORS = { 0xFFD860, 0xFFFFFF, 0xC8C8C8 };
    private static final ResourceLocation FLASH = new ResourceLocation("textures/particle/flash.png");
    private static final ResourceLocation WHITE = new ResourceLocation("textures/misc/white.png");
    private static final float[] CELL_X, CELL_Y;
    private static final int[] CELL_TINT;
    private static final int CELLS;

    static {
        int count = 0;
        for (String[] glyph : GLYPHS) for (String row : glyph) for (int c = 0; c < row.length(); c++) if (row.charAt(c) == '#') count++;
        CELLS = count;
        CELL_X = new float[count];
        CELL_Y = new float[count];
        CELL_TINT = new int[count];
        int i = 0;
        int columns = GLYPHS.length * 6 - 1;
        for (int letter = 0; letter < GLYPHS.length; letter++) {
            for (int r = 0; r < 7; r++) {
                String row = GLYPHS[letter][r];
                for (int c = 0; c < row.length(); c++) {
                    if (row.charAt(c) != '#') continue;
                    CELL_X[i] = ((columns - 1) / 2.0F - letter * 6 - c) * CELL;
                    CELL_Y[i] = LOGO_Y + (3 - r) * CELL;
                    CELL_TINT[i] = TINTS[letter];
                    i++;
                }
            }
        }
    }

    final List<QuestMilestones.Shown> list;
    private final int count;
    private final ItemStack[] stacks;
    private final ResourceLocation[] textures;
    private final Component[][] text;
    private final int[] itemOfCell;
    private int popped = -1;
    private boolean gathered, scattered, reformed;
    private final float[] p = new float[3], q = new float[3], rr = new float[3], ll = new float[3];

    HappyWaterGodRecap(List<QuestMilestones.Shown> list) {
        this.list = list;
        count = Math.min(list.size(), CELLS);
        stacks = new ItemStack[count];
        textures = new ResourceLocation[count];
        text = new Component[count][];
        itemOfCell = new int[CELLS];
        Arrays.fill(itemOfCell, -1);
        for (int k = 0; k < count; k++) {
            build(k, list.get(k));
            itemOfCell[Math.min(CELLS - 1, (int) ((k + 0.5F) * CELLS / count))] = k;
        }
    }

    private void build(int k, QuestMilestones.Shown s) {
        stacks[k] = ItemStack.EMPTY;
        Component title;
        switch (s.type()) {
            case QuestMilestones.ERA -> {
                stacks[k] = stack(s.icon());
                int tier = Mth.clamp(parse(s.arg()), 0, GTValues.VNF.length - 1);
                title = Component.translatable(HappyWaterGodStatue.RECAP_ERA, Component.literal(GTValues.VNF[tier]));
            }
            case QuestMilestones.LAND, QuestMilestones.ARRIVE -> {
                var location = ResourceLocation.tryParse(s.arg());
                var dim = location == null ? null : Dimension.get(ResourceKey.create(Registries.DIMENSION, location));
                Component name = dim == null ? Component.literal(s.arg()) : Component.translatable(dim.getKey());
                if (dim != null) textures[k] = dim.getIcon();
                title = Component.translatable(s.type() == QuestMilestones.LAND ? HappyWaterGodStatue.RECAP_LAND : HappyWaterGodStatue.RECAP_ARRIVE, name);
            }
            case QuestMilestones.TREE -> {
                stacks[k] = stack(s.icon());
                title = Component.translatable(HappyWaterGodStatue.RECAP_TREE);
            }
            case QuestMilestones.ORE -> {
                stacks[k] = stack(s.icon());
                title = Component.translatable(HappyWaterGodStatue.RECAP_ORE);
            }
            case QuestMilestones.QUEST -> {
                if (QuestBook.byKey(s.arg()) instanceof Quest quest) {
                    stacks[k] = quest.icon().stack();
                    title = quest.title();
                } else {
                    title = Component.literal(s.arg());
                }
            }
            default -> {
                stacks[k] = stack(s.icon());
                title = stacks[k].getHoverName();
            }
        }
        long seconds = s.playtime() / 20;
        Component time = Component.translatable(HappyWaterGodStatue.RECAP_TIME, s.day(), seconds / 3600, seconds % 3600 / 60, seconds % 60);
        text[k] = new Component[] { title, time, who(s) };
    }

    @Nullable
    private static Component who(QuestMilestones.Shown s) {
        if (!s.who().isEmpty()) return Component.translatable(HappyWaterGodStatue.RECAP_BY, s.who());
        return switch (QuestLedger.Source.byId(s.source())) {
            case AE_DETECTOR -> Component.translatable(HappyWaterGodStatue.RECAP_AE);
            case UNLOCK -> Component.translatable(HappyWaterGodStatue.RECAP_UNLOCK);
            case TEAM_SYNC -> Component.translatable(HappyWaterGodStatue.RECAP_TEAM);
            case COMMAND -> Component.translatable(HappyWaterGodStatue.RECAP_OP);
            default -> null;
        };
    }

    private static ItemStack stack(String id) {
        var location = ResourceLocation.tryParse(id);
        return location == null ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(location));
    }

    private static int parse(String text) {
        int value = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') return 0;
            value = value * 10 + (c - '0');
        }
        return value;
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static float backOut(float t) {
        t = Mth.clamp(t, 0, 1) - 1;
        return 1 + t * t * (2.70158F * t + 1.70158F);
    }

    private int spacing() {
        return Mth.clamp(APPEAR_BARS * BAR / count / BEAT * BEAT, 2 * BEAT, 6 * BEAT);
    }

    private static void set(float[] out, float x, float y, float z) {
        out[0] = x;
        out[1] = y;
        out[2] = z;
    }

    private static void mix(float[] a, float[] b, float t) {
        a[0] += (b[0] - a[0]) * t;
        a[1] += (b[1] - a[1]) * t;
        a[2] += (b[2] - a[2]) * t;
    }

    private void collect(int k, float song, float[] out) {
        float a = Mth.TWO_PI * k / count + song * 0.018F;
        set(out, COLLECT_R * Mth.cos(a), LOGO_Y + COLLECT_R * Mth.sin(a), LOGO_Z);
    }

    private static void ring(int cell, float song, float[] out) {
        float a = Mth.TWO_PI * cell / CELLS + song * Mth.TWO_PI / (2 * BAR);
        float beat = (song % BEAT) / BEAT;
        float pulse = 1 + 0.07F * (float) Math.exp(-6 * beat);
        float tilt = 0.3F * Mth.sin(song * 0.025F);
        float sx = RING_RX * pulse * Mth.cos(a), sy = RING_RY * pulse * Mth.sin(a);
        set(out, sx, RING_Y + sy * Mth.cos(tilt), sy * Mth.sin(tilt));
    }

    private static void logo(int cell, float[] out) {
        set(out, CELL_X[cell], CELL_Y[cell], LOGO_Z);
    }

    private float formation(int cell, float song, float[] out) {
        logo(cell, out);
        if (song < BURST_AT * BAR) return CELL_SCALE;
        var r = rr;
        ring(cell, song, r);
        if (song < REFORM_AT * BAR) {
            float t = (song - BURST_AT * BAR) / SCATTER;
            float b = smooth(t);
            float push = Mth.sin(Mth.PI * Mth.clamp(t, 0, 1)) * 2.2F;
            float ox = out[0], oy = out[1] - LOGO_Y;
            float len = Math.max(0.3F, Mth.sqrt(ox * ox + oy * oy));
            mix(out, r, b);
            out[0] += ox / len * push;
            out[1] += oy / len * push;
            out[2] += push * 0.6F;
            return Mth.lerp(b, CELL_SCALE, RING_SCALE);
        }
        float back = smooth((song - REFORM_AT * BAR - cell * 0.7F) / REFORM);
        var l = ll;
        set(l, out[0], out[1], out[2]);
        set(out, r[0], r[1], r[2]);
        mix(out, l, back);
        return Mth.lerp(back, RING_SCALE, CELL_SCALE);
    }

    void render(HappyWaterGodStatue.Entity entity, PoseStack poseStack, MultiBufferSource buffer, float song, float fx, float fz, float rx, float rz, float facing, float fade) {
        if (count == 0 || fade <= 0.01F) return;
        float start = APPEAR_FROM * BAR;
        if (song < start) return;
        int spacing = spacing();
        float logoAt = LOGO_FROM * BAR;
        float pulse = 0.75F + 0.25F * (float) Math.exp(-5 * ((song % BEAT) / BEAT));
        for (int k = 0; k < count; k++) {
            float age = song - (start + k * spacing);
            if (age < 0) continue;
            float scale, textAlpha = 0;
            var slot = SLOTS[k % SLOTS.length];
            if (age < POP + HOLD) {
                set(p, slot[0], slot[1], SLOT_Z);
                scale = backOut(age / POP) * SLOT_SCALE;
                textAlpha = Math.min(1, age / 4);
                if (k > popped) {
                    popped = k;
                    burst(entity, world(fx, rx, p), p[1], worldZ(fz, rz, p), ParticleTypes.END_ROD, 14, 0.07F);
                }
            } else if (age < POP + HOLD + FLY) {
                float t = (age - POP - HOLD) / FLY;
                set(p, slot[0], slot[1], SLOT_Z);
                collect(k, song, q);
                float e = smooth(t);
                mix(p, q, e);
                p[1] += Mth.sin(Mth.PI * t) * 1.3F;
                scale = Mth.lerp(e, SLOT_SCALE, COLLECT_SCALE);
                textAlpha = Math.max(0, 1 - t * 3);
            } else {
                collect(k, song, p);
                scale = COLLECT_SCALE;
            }
            int cell = Math.min(CELLS - 1, (int) ((k + 0.5F) * CELLS / count));
            float orb = 0.35F, tile = 0;
            int tint = 0xFFFFFF;
            if (song >= logoAt) {
                float m = smooth((song - logoAt - k * 1.2F) / GATHER);
                scale = Mth.lerp(m, scale, formation(cell, song, q));
                mix(p, q, m);
                orb = Mth.lerp(m, 0.35F, 0.3F);
                tile = m * pulse;
                tint = mixColor(0xFFFFFF, CELL_TINT[cell], m);
            }
            if (tile > 0) tile(poseStack, buffer, world(fx, rx, p), p[1], worldZ(fz, rz, p), TILE * scale / CELL_SCALE * fade, tint, tile * fade);
            draw(entity, poseStack, buffer, k, p, scale * fade, orb * fade, tint, song, fx, fz, rx, rz, facing, textAlpha * fade);
        }
        if (song >= logoAt) {
            for (int cell = 0; cell < CELLS; cell++) {
                if (itemOfCell[cell] >= 0) continue;
                float in = smooth((song - logoAt - 20 - cell * 0.8F) / 24);
                if (in <= 0) continue;
                float s = formation(cell, song, p) / CELL_SCALE;
                float x = world(fx, rx, p), z = worldZ(fz, rz, p);
                tile(poseStack, buffer, x, p[1], z, TILE * s * in * fade, CELL_TINT[cell], pulse * in * fade);
                orb(poseStack, buffer, x, p[1], z, 0.35F * s * in * fade, CELL_TINT[cell], 0.8F);
            }
            if (!gathered && song >= logoAt + GATHER + count * 1.2F) {
                gathered = true;
                burstAround(entity, fx, fz, rx, rz, ParticleTypes.FIREWORK, 3);
            }
            if (!scattered && song >= BURST_AT * BAR) {
                scattered = true;
                burstAround(entity, fx, fz, rx, rz, ParticleTypes.FIREWORK, 6);
            }
            if (!reformed && song >= REFORM_AT * BAR + REFORM + CELLS * 0.7F) {
                reformed = true;
                burstAround(entity, fx, fz, rx, rz, ParticleTypes.END_ROD, 4);
            }
        }
    }

    private void burstAround(HappyWaterGodStatue.Entity entity, float fx, float fz, float rx, float rz, SimpleParticleType type, int perCell) {
        for (int cell = 0; cell < CELLS; cell += 2) {
            logo(cell, p);
            burst(entity, world(fx, rx, p), p[1], worldZ(fz, rz, p), type, perCell, 0.05F);
        }
    }

    private void draw(HappyWaterGodStatue.Entity entity, PoseStack poseStack, MultiBufferSource buffer, int k, float[] at, float scale, float orb, int tint, float song,
                      float fx, float fz, float rx, float rz, float facing, float textAlpha) {
        if (scale <= 0.01F) return;
        float x = world(fx, rx, at), y = at[1], z = worldZ(fz, rz, at);
        orb(poseStack, buffer, x, y, z, orb * scale, tint, 0.85F);
        var mc = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        ResourceLocation texture = textures[k];
        if (texture != null) {
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            float s = 0.62F * scale;
            poseStack.scale(-s, s, s);
            quad(poseStack, buffer.getBuffer(RenderType.entityTranslucent(texture)), 0xFFFFFF, 255);
        } else {
            boolean spinning = textAlpha > 0;
            float yaw = spinning ? song * 4.0F + k * 37.0F : facing + 18.0F * Mth.sin(song * 0.06F + k);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.scale(scale, scale, scale);
            mc.getItemRenderer().renderStatic(stacks[k], ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.getLevel(), k);
        }
        poseStack.popPose();
        if (textAlpha > 0.03F) text(poseStack, buffer, text[k], x, y - 0.32F * scale - 0.3F, z, textAlpha);
    }

    private static void tile(PoseStack poseStack, MultiBufferSource buffer, float x, float y, float z, float size, int tint, float alpha) {
        if (size <= 0.02F || alpha <= 0.02F) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-size, size, size);
        quad(poseStack, buffer.getBuffer(RenderType.eyes(WHITE)), tint, (int) (Math.min(1, alpha) * 185));
        poseStack.popPose();
    }

    private static void orb(PoseStack poseStack, MultiBufferSource buffer, float x, float y, float z, float size, int tint, float alpha) {
        if (size <= 0.02F) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-size * 1.6F, size * 1.6F, size * 1.6F);
        quad(poseStack, buffer.getBuffer(RenderType.eyes(FLASH)), tint, (int) (alpha * 255));
        poseStack.popPose();
    }

    private static void quad(PoseStack poseStack, VertexConsumer c, int rgb, int a) {
        var pose = poseStack.last();
        Matrix4f m = pose.pose();
        Matrix3f n = pose.normal();
        int r = rgb >> 16 & 255, g = rgb >> 8 & 255, b = rgb & 255;
        c.vertex(m, -0.5F, -0.5F, 0).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 0, 1).endVertex();
        c.vertex(m, 0.5F, -0.5F, 0).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 0, 1).endVertex();
        c.vertex(m, 0.5F, 0.5F, 0).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 0, 1).endVertex();
        c.vertex(m, -0.5F, 0.5F, 0).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(n, 0, 0, 1).endVertex();
    }

    private static void text(PoseStack poseStack, MultiBufferSource buffer, Component[] lines, float x, float y, float z, float alpha) {
        var mc = Minecraft.getInstance();
        Font font = mc.font;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        Matrix4f m = poseStack.last().pose();
        int a = (int) (alpha * 255);
        int background = (int) (alpha * 110) << 24;
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            if (line == null) continue;
            font.drawInBatch(line, -font.width(line) / 2.0F, i * 11, a << 24 | TEXT_COLORS[i], false, m, buffer, Font.DisplayMode.NORMAL, background, LightTexture.FULL_BRIGHT);
        }
        poseStack.popPose();
    }

    private static int mixColor(int a, int b, float t) {
        int r = (int) Mth.lerp(t, a >> 16 & 255, b >> 16 & 255), g = (int) Mth.lerp(t, a >> 8 & 255, b >> 8 & 255), bl = (int) Mth.lerp(t, a & 255, b & 255);
        return r << 16 | g << 8 | bl;
    }

    private static float world(float fx, float rx, float[] at) {
        return 0.5F + rx * at[0] + fx * at[2];
    }

    private static float worldZ(float fz, float rz, float[] at) {
        return 0.5F + rz * at[0] + fz * at[2];
    }

    private static void burst(HappyWaterGodStatue.Entity entity, float x, float y, float z, SimpleParticleType type, int count, float speed) {
        var level = entity.getLevel();
        if (level == null) return;
        BlockPos pos = entity.getBlockPos();
        var random = level.random;
        for (int i = 0; i < count; i++) {
            float a = random.nextFloat() * Mth.TWO_PI, up = random.nextFloat() * 2 - 1;
            level.addParticle(type, pos.getX() + x, pos.getY() + y, pos.getZ() + z, Mth.cos(a) * speed, up * speed, Mth.sin(a) * speed);
        }
    }
}
