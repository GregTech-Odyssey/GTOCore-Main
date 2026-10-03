package com.gtocore.client.screen.starmap.grid;

import com.gtocore.client.screen.starmap.base.PixelPen;

import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 线路的流量分档、线宽、颜色与粒子参数；档位取「较忙方向流量 ÷ 全图最大线路流量」与「较忙方向负载」两者中较高的一档，跨档带回差。线宽与粒子按屏幕原生像素给出。
 */
@OnlyIn(Dist.CLIENT)
final class GridStyle {

    static final int IDLE = 0;
    static final int LOW = 1;
    static final int MID = 2;
    static final int HIGH = 3;
    static final int CORE = 4;
    static final int LEVELS = 5;
    static final float STEADY = 1;
    static final float HYSTERESIS = 0.85f;
    static final int MAX_PARTICLES = 600;
    static final int HEAD = 2;
    static final int TAIL = 2;
    static final int HOVER_CORE = 0xFFFFFFFF;
    static final int OPAQUE = 0xFF000000;
    static final float SPRITE_SCALE = 0.8f;
    private static final float[] THRESHOLDS = { 0.02f, 0.12f, 0.5f };
    private static final float[] LOAD_THRESHOLDS = { 0.25f, 0.6f, 0.9f };
    private static final int[] WIDTH = { 1, 1, 2, 2, 3 };
    private static final int[] SPACING = { 0, 0, 64, 40, 24 };
    private static final int[] SPEED = { 0, 0, 16, 28, 44 };

    private GridStyle() {}

    static int level(float flow, float maxFlow, float budget, int previous) {
        if (!(flow >= STEADY) || !(maxFlow > 0)) return IDLE;
        float ratio = flow / maxFlow, load = budget > 0 ? flow / budget : 0;
        int up = Math.max(rawLevel(ratio, THRESHOLDS), rawLevel(load, LOAD_THRESHOLDS));
        int down = Math.max(rawLevel(ratio / HYSTERESIS, THRESHOLDS), rawLevel(load / HYSTERESIS, LOAD_THRESHOLDS));
        if (previous < up) return up;
        return Math.min(previous, down);
    }

    private static int rawLevel(float ratio, float[] thresholds) {
        int level = LOW;
        for (float threshold : thresholds) {
            if (ratio >= threshold) level++;
        }
        return level;
    }

    static int width(int level) {
        return WIDTH[level];
    }

    static int color(int level) {
        return switch (level) {
            case IDLE -> PixelPen.mix(UITheme.MAP_LINK_IDLE, UITheme.MAP_LINK_DIM, 0.5f);
            case LOW -> PixelPen.mix(UITheme.MAP_LINK_LOW, UITheme.MAP_LINK_MID, 0.4f);
            case MID -> UITheme.MAP_LINK_MID;
            default -> UITheme.MAP_LINK_HIGH;
        };
    }

    static boolean hasParticles(int level) {
        return SPACING[level] > 0;
    }

    static int spacing(int level) {
        return SPACING[level];
    }

    static int speed(int level) {
        return SPEED[level];
    }

    static int tailColor(int level) {
        return level == MID ? UITheme.MAP_LINK_LOW : UITheme.MAP_LINK_MID;
    }

    static int headColor(int level) {
        return level == CORE ? PixelPen.mix(UITheme.MAP_SPARK, UITheme.MAP_SATURATED, 0.45f) : UITheme.MAP_SPARK;
    }

    static int fillColor(float ratio) {
        return ratio < 0.1f ? UITheme.STATUS_OFFLINE : UITheme.barEnergy();
    }

    static int plate() {
        return OPAQUE | UITheme.MAP_PLATE;
    }

    static int outline() {
        return PixelPen.mix(UITheme.MAP_ORBIT, UITheme.MAP_LABEL_DIM, 0.12f);
    }

    static int guide() {
        return PixelPen.mix(UITheme.MAP_ORBIT_FAR, UITheme.MAP_ORBIT, 0.5f);
    }
}
