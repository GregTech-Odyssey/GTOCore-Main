package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.EnergyPort;
import com.gtocore.api.wireless.energy.SettleResult;
import com.gtocore.api.wireless.energy.WirelessText;
import com.gtocore.data.IdleReason;

import com.gtolib.utils.NumberUtils;

public final class WirelessIdle {

    private WirelessIdle() {}

    public static void reportDeposit(Object machine, EnergyPort port, int tier) {
        if (port.account().isNone()) IdleReason.NO_OWNER.setReason(machine);
        else if (!port.isOpen(tier)) IdleReason.WIRELESS_NO_COVERAGE.setReason(machine, WirelessText.voltage(tier), WirelessText.voltage(port.node().reachTier()));
        else IdleReason.WIRELESS_GRID_FULL.setReason(machine);
    }

    public static void report(Object machine, SettleResult result, EnergyPort port, int tier, double amount) {
        switch (result) {
            case OK -> {}
            case NO_ACCOUNT -> IdleReason.NO_OWNER.setReason(machine);
            case NO_COVERAGE -> IdleReason.WIRELESS_NO_COVERAGE.setReason(machine, WirelessText.voltage(tier), WirelessText.voltage(port.node().reachTier()));
            case NO_STORAGE -> IdleReason.WIRELESS_EU_SHORT.setReason(machine, NumberUtils.formatDouble(amount), NumberUtils.formatDouble(port.shortfallStorage()));
            case NO_TRANSFER -> IdleReason.WIRELESS_RELAY_SHORT.setReason(machine, NumberUtils.formatDouble(amount));
            case RESERVED -> IdleReason.WIRELESS_PRIORITY_RESERVED.setReason(machine, NumberUtils.formatDouble(amount));
        }
    }
}
