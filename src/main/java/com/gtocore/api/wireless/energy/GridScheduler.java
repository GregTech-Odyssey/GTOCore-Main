package com.gtocore.api.wireless.energy;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;

/**
 * 全服一个、每 tick 末运行一次的端点调度：按优先级从高到低服务到期端点，同优先级内每 tick 轮换起点，避免固定顺序饿死后者。
 * 机器在自己的 tick 里只碰端点缓冲，真正跨维度的存取都在这里按优先级发生。
 */
public final class GridScheduler {

    public static final int PRIORITIES = 5;
    public static final int DEFAULT_PRIORITY = 2;
    public static final String DEFAULT_PRIORITY_TEXT = "" + DEFAULT_PRIORITY;
    private static final int[] PERIODS = { 1, 2, 4, 5, 10, 20 };

    @SuppressWarnings("unchecked")
    private final ObjectArrayList<Handle>[][][] buckets = new ObjectArrayList[PRIORITIES][PERIODS.length][];
    private Handle[] snapshot = new Handle[64];

    GridScheduler() {
        for (int p = 0; p < PRIORITIES; p++) {
            for (int i = 0; i < PERIODS.length; i++) {
                buckets[p][i] = new ObjectArrayList[PERIODS[i]];
                for (int j = 0; j < PERIODS[i]; j++) buckets[p][i][j] = new ObjectArrayList<>();
            }
        }
    }

    static int periodIndex(int ticks) {
        for (int i = PERIODS.length - 1; i >= 0; i--) {
            if (PERIODS[i] <= ticks) return i;
        }
        return 0;
    }

    public static int clampPriority(int priority) {
        return Math.max(0, Math.min(PRIORITIES - 1, priority));
    }

    void run(int now) {
        for (int p = PRIORITIES - 1; p >= 0; p--) {
            for (int i = 0; i < PERIODS.length; i++) {
                int b = now % PERIODS[i];
                var list = buckets[p][i][b];
                int size = list.size();
                if (size == 0) continue;
                if (snapshot.length < size) snapshot = new Handle[Math.max(size, snapshot.length << 1)];
                list.toArray(snapshot);
                int start = Math.floorMod(now / PERIODS[i], size);
                for (int k = 0; k < size; k++) {
                    var handle = snapshot[(start + k) % size];
                    if (handle.scheduler == this && handle.priority == p && handle.period == i && handle.bucket == b && handle.lastRun != now) {
                        handle.lastRun = now;
                        handle.run();
                    }
                }
                Arrays.fill(snapshot, 0, size, null);
            }
        }
    }

    static final class Handle {

        private final Runnable task;
        private GridScheduler scheduler;
        private int priority = DEFAULT_PRIORITY;
        private int period = -1;
        private int bucket;
        private int slot = -1;
        private int lastRun = Integer.MIN_VALUE;

        Handle(Runnable task) {
            this.task = task;
        }

        boolean scheduled() {
            return slot >= 0;
        }

        void setPriority(int priority) {
            priority = clampPriority(priority);
            if (priority == this.priority) return;
            int current = period;
            cancel();
            this.priority = priority;
            if (current >= 0) schedule(current);
        }

        void schedule(int periodIndex) {
            var target = WirelessGrid.scheduler();
            if (target == null) return;
            if (slot >= 0 && period == periodIndex && scheduler == target) return;
            cancel();
            int p = PERIODS[periodIndex];
            int b = Math.floorMod(GridClock.tick(), p);
            var list = target.buckets[priority][periodIndex][b];
            scheduler = target;
            period = periodIndex;
            bucket = b;
            slot = list.size();
            list.add(this);
        }

        void cancel() {
            if (slot < 0) return;
            var list = scheduler.buckets[priority][period][bucket];
            if (slot < list.size() && list.get(slot) == this) {
                var tail = list.remove(list.size() - 1);
                if (tail != this) {
                    list.set(slot, tail);
                    tail.slot = slot;
                }
            }
            slot = -1;
            period = -1;
            scheduler = null;
        }

        private void run() {
            task.run();
        }
    }
}
