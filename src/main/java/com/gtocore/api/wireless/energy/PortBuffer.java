package com.gtocore.api.wireless.energy;

/**
 * 端点在机器 tick 与调度服务之间的缓冲：credit 是已从电网取出待交付的电量，demand 是本轮请求量，pending 是待送入电网的毛电量。
 */
final class PortBuffer {

    long credit, demand;
    int creditTier = -1;
    long pending;
    int pendingTier = -1;

    long withdraw(long want, int voltageTier) {
        if (voltageTier > creditTier) creditTier = voltageTier;
        long take = Math.min(want, credit);
        credit -= take;
        demand = U126.saturatedAdd(demand, want);
        return take;
    }

    void returnUnused(long amount) {
        if (amount > 0) credit = U126.saturatedAdd(credit, amount);
    }

    long roomFor(long gross) {
        return Math.max(0, Math.min(gross, U126.saturatedAdd(gross, gross) - pending));
    }

    boolean acceptsAll(long gross) {
        return pending <= gross;
    }

    void addPending(long amount, int voltageTier) {
        pending = U126.saturatedAdd(pending, amount);
        if (voltageTier > pendingTier) pendingTier = voltageTier;
    }

    int servedTier(int baseTier) {
        return Math.max(baseTier, Math.max(creditTier, pendingTier));
    }

    void pushed(long amount) {
        pending -= amount;
        if (pending <= 0) pendingTier = -1;
    }

    long missingCredit() {
        return demand - credit;
    }

    boolean demandMet() {
        boolean met = credit >= demand;
        creditTier = -1;
        demand = 0;
        return met;
    }

    void clear() {
        credit = 0;
        demand = 0;
        pending = 0;
        creditTier = -1;
        pendingTier = -1;
    }
}
