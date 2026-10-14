package com.gtocore.api.wireless.energy;

public final class GridClock {

    private static int tick;
    private static int second;

    private GridClock() {}

    public static int tick() {
        return tick;
    }

    public static int second() {
        return second;
    }

    static boolean advance(int now) {
        tick = now;
        int s = now / 20;
        if (s == second) return false;
        second = s;
        return true;
    }
}
