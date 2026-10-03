package com.gtocore.api.wireless.energy;

import org.jetbrains.annotations.Nullable;

/**
 * 端点一次服务的结论：是否搬动了电量、是否因存量（空间）不足或电压不可达而饥饿、是否只受线路或速率限制；据此决定挂起或本 tick 免服。
 */
final class PortCycle {

    boolean moved, starved, rateLimited, drew, pushed;
    private boolean drawBlocked, pushBlocked;
    private int blockedTier = Integer.MIN_VALUE;

    @Nullable
    GridNode parkedAt;
    int parkedTier;
    int parkSlot = -1;

    void begin() {
        moved = starved = rateLimited = drew = pushed = false;
    }

    void record(long amount, long allowed, boolean lineLimited) {
        moved |= amount > 0;
        starved |= amount == 0 && allowed > 0 && !lineLimited;
    }

    boolean shouldPark(int next) {
        return next == 1 && starved && !moved && !rateLimited;
    }

    void finish(int next, int servedTier) {
        boolean lineBound = next == 1 && !moved && !starved && !rateLimited;
        drawBlocked = lineBound && drew && !pushed;
        pushBlocked = lineBound && pushed && !drew;
        blockedTier = servedTier;
    }

    boolean stillBlocked(EnergyAccount account, GridNode node, int servedTier, boolean demandPending) {
        if (servedTier != blockedTier) return false;
        int now = GridClock.tick();
        if (drawBlocked) return !demandPending && node.dry.limited && node.dry.hit(now, account.supplyGen, servedTier);
        if (pushBlocked) return node.full.limited && node.full.hit(now, account.sinkGen, servedTier);
        return false;
    }
}
