package com.gtocore.api.wireless.energy;

import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;

/**
 * 所在维度没有电网节点的端点：退出调度，按退避间隔到点再试；账户电网重建时由账户直接唤醒。
 */
final class PortRetry {

    static final int FIRST_TICKS = 20;
    static final int MAX_TICKS = 1200;

    private record Entry(EnergyPort port, int at) {}

    private final ObjectHeapPriorityQueue<Entry> queue = new ObjectHeapPriorityQueue<>((a, b) -> Integer.compare(a.at, b.at));

    void add(EnergyPort port, int at) {
        queue.enqueue(new Entry(port, at));
    }

    void run(int now) {
        while (!queue.isEmpty()) {
            var entry = queue.first();
            if (entry.at > now) return;
            queue.dequeue();
            if (entry.port.retryAt == entry.at) entry.port.wakeRetry();
        }
    }

    static int next(int delay) {
        return delay <= 0 ? FIRST_TICKS : Math.min(delay << 1, MAX_TICKS);
    }
}
