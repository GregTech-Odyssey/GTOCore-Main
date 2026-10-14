package com.gtocore.api.wireless.energy;

import org.jetbrains.annotations.Nullable;

final class FlowMeter {

    private final EnergyAccount account;
    private double secIn, secOut, secLoss;
    int second = -1;
    @Nullable
    private EnergyStats stats;

    FlowMeter(EnergyAccount account) {
        this.account = account;
    }

    @Nullable
    public EnergyStats stats() {
        return stats;
    }

    void in(double gross, double lost) {
        touch();
        secIn += gross;
        secLoss += lost;
    }

    void out(double amount) {
        touch();
        secOut += amount;
    }

    private void touch() {
        if (second != GridClock.second()) {
            second = GridClock.second();
            WirelessGrid.markActive(account);
        }
    }

    void roll() {
        if (stats == null) stats = new EnergyStats();
        stats.push(second, secIn, secOut, secLoss);
        secIn = 0;
        secOut = 0;
        secLoss = 0;
        account.rollNodes();
    }
}
