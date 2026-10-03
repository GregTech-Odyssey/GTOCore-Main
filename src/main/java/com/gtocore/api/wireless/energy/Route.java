package com.gtocore.api.wireless.energy;

final class Route {

    final GridNode source;
    final Arc[] inbound;
    final Arc[] outbound;
    int tier;

    Route(GridNode source, Arc[] inbound, Arc[] outbound) {
        this.source = source;
        this.inbound = inbound;
        this.outbound = outbound;
        refreshTier();
    }

    void refreshTier() {
        int t = source.tier;
        for (var arc : inbound) t = Math.min(t, arc.tier);
        tier = t;
    }

    public GridNode source() {
        return source;
    }

    public int tier() {
        return tier;
    }
}
