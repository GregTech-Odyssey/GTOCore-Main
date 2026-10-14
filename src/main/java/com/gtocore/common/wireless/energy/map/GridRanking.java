package com.gtocore.common.wireless.energy.map;

import java.util.Arrays;

/**
 * 带滞后的前 N 名：新名次与当前名次的度量差达到阈值时立即换位，否则同一新顺序要连续出现若干次采样才换位，避免读数接近时来回跳。
 */
final class GridRanking {

    static final int SIZE = 3;
    private static final int CONFIRM_SAMPLES = 3;

    private final boolean descending;
    private final boolean relative;
    private final double threshold;
    private final int[] shown = new int[SIZE];
    private final int[] candidate = new int[SIZE];
    private final int[] pendingOrder = new int[SIZE];
    private int pending;

    GridRanking(boolean descending, boolean relative, double threshold) {
        this.descending = descending;
        this.relative = relative;
        this.threshold = threshold;
        Arrays.fill(shown, -1);
        Arrays.fill(pendingOrder, -1);
    }

    int get(int rank) {
        return shown[rank];
    }

    void reset() {
        Arrays.fill(shown, -1);
        pending = 0;
    }

    void update(double[] metric, int count) {
        pick(metric, count);
        if (Arrays.equals(candidate, shown)) {
            pending = 0;
            return;
        }
        if (urgent(metric, count)) {
            accept();
            return;
        }
        if (Arrays.equals(candidate, pendingOrder)) {
            if (++pending >= CONFIRM_SAMPLES) accept();
        } else {
            System.arraycopy(candidate, 0, pendingOrder, 0, SIZE);
            pending = 1;
        }
    }

    private void accept() {
        System.arraycopy(candidate, 0, shown, 0, SIZE);
        pending = 0;
    }

    private void pick(double[] metric, int count) {
        Arrays.fill(candidate, -1);
        for (int i = 0; i < count; i++) {
            if (Double.isNaN(metric[i])) continue;
            int at = SIZE;
            while (at > 0 && (candidate[at - 1] < 0 || better(metric[i], metric[candidate[at - 1]]))) at--;
            if (at >= SIZE) continue;
            System.arraycopy(candidate, at, candidate, at + 1, SIZE - 1 - at);
            candidate[at] = i;
        }
    }

    private boolean better(double a, double b) {
        return descending ? a > b : a < b;
    }

    private boolean urgent(double[] metric, int count) {
        for (int r = 0; r < SIZE; r++) {
            int now = shown[r], next = candidate[r];
            if (now == next) continue;
            if (now < 0 || next < 0 || now >= count || Double.isNaN(metric[now])) return true;
            double a = metric[now], b = metric[next];
            double gap = Math.abs(a - b), limit = relative ? threshold * Math.max(Math.abs(a), Math.abs(b)) : threshold;
            if (gap >= limit) return true;
        }
        return false;
    }
}
