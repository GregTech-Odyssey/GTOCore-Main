package com.gtocore.api.wireless.energy;

final class TokenBucket {

    public static final int BURST_TICKS = 20;

    private long tokens;
    private int last;

    public long allowance(long rate, int now) {
        if (now != last) {
            tokens = refilled(rate, now);
            last = now;
        }
        return tokens;
    }

    void copyFrom(TokenBucket other) {
        tokens = other.tokens;
        last = other.last;
    }

    public void take(long amount) {
        tokens -= amount;
    }

    public void giveBack(long amount, long rate) {
        tokens = Math.min(capacity(rate), U126.saturatedAdd(tokens, amount));
    }

    public void clampTo(long rate) {
        long cap = capacity(rate);
        if (tokens > cap) tokens = cap;
    }

    private long refilled(long rate, int now) {
        if (rate <= 0) return 0;
        int elapsed = now - last;
        long gained = U126.saturatedMul(rate, elapsed < 0 || elapsed > BURST_TICKS ? BURST_TICKS : elapsed);
        return Math.min(capacity(rate), U126.saturatedAdd(Math.max(0, tokens), gained));
    }

    private static long capacity(long rate) {
        return rate <= 0 ? 0 : U126.saturatedMul(rate, BURST_TICKS);
    }
}
