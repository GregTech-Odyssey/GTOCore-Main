package com.gtocore.common.data;

import com.gtolib.api.data.CelestialBody;

import it.unimi.dsi.fastutil.HashCommon;

/**
 * 天体轨道：初始相位与升交点由存档种子决定，之后按存档经过的 tick 匀速公转；坐标以恒星为原点、黄道面为 xy 平面（距离比，地球为 1）。
 */
public final class CelestialOrbits {

    private static final double TAU = Math.PI * 2;
    private static final long PHASE_SALT = 0x9E3779B97F4A7C15L;
    private static final long NODE_SALT = 0xC2B2AE3D27D4EB4FL;

    private static volatile long clientSeed;
    private static volatile boolean clientReady;

    private CelestialOrbits() {}

    public static void setClientSeed(long seed) {
        clientSeed = seed;
        clientReady = true;
    }

    public static void clearClientSeed() {
        clientReady = false;
    }

    public static boolean hasClientSeed() {
        return clientReady;
    }

    public static long clientSeed() {
        return clientSeed;
    }

    private static double random(long seed, int index, long salt) {
        return (HashCommon.mix(seed + (index + 1) * salt) >>> 11) * 0x1.0p-53 * TAU;
    }

    public static double angle(long seed, CelestialBody body, double ticks) {
        return random(seed, body.getIndex(), PHASE_SALT) + TAU * (ticks % body.getRevolutionTicks()) / body.getRevolutionTicks();
    }

    private static double node(long seed, CelestialBody body) {
        var primary = body.getPrimary();
        return primary != null ? random(seed, primary.getIndex(), NODE_SALT) : random(seed, body.getIndex(), NODE_SALT);
    }

    public static void position(long seed, CelestialBody body, double ticks, double[] out) {
        var primary = body.getPrimary();
        double radius;
        if (primary != null) {
            position(seed, primary, ticks, out);
            radius = body.getMoonDistanceKm() / CelestialBody.AU_KM;
        } else {
            out[0] = out[1] = out[2] = 0;
            radius = body.getDistanceRatio();
        }
        double u = angle(seed, body, ticks), node = node(seed, body), inclination = body.getInclination();
        double cosU = Math.cos(u), sinU = Math.sin(u), cosNode = Math.cos(node), sinNode = Math.sin(node), cosI = Math.cos(inclination);
        out[0] += radius * (cosNode * cosU - sinNode * sinU * cosI);
        out[1] += radius * (sinNode * cosU + cosNode * sinU * cosI);
        out[2] += radius * sinU * Math.sin(inclination);
    }
}
