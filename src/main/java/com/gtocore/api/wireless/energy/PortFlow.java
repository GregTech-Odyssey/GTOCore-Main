package com.gtocore.api.wireless.energy;

final class PortFlow {

    private static final int WINDOW_TICKS = 20;

    private long in, out;
    private long observedIn, observedOut;
    private int observedTick = Integer.MIN_VALUE;
    private double inRate, outRate;

    void addIn(long amount) {
        in = U126.saturatedAdd(in, amount);
    }

    void addOut(long amount) {
        out = U126.saturatedAdd(out, amount);
    }

    void removeOut(long amount) {
        out -= Math.min(out, amount);
    }

    double inRate() {
        observe();
        return inRate;
    }

    double outRate() {
        observe();
        return outRate;
    }

    private void observe() {
        int now = GridClock.tick();
        if (observedTick == Integer.MIN_VALUE) {
            observedTick = now;
            observedIn = in;
            observedOut = out;
            return;
        }
        int elapsed = now - observedTick;
        if (elapsed < WINDOW_TICKS) return;
        inRate = (double) (in - observedIn) / elapsed;
        outRate = (double) (out - observedOut) / elapsed;
        observedTick = now;
        observedIn = in;
        observedOut = out;
    }
}
