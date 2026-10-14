package com.gtocore.api.wireless.energy;

import com.gtocore.config.GTORules;

import java.util.function.IntSupplier;

/**
 * 端点种类：所有出入电共用同一套规则，种类只声明该种设备额外承担的入账损耗（千分比）。
 */
public enum PortKind {

    HATCH(() -> 0),
    RECEIVE_COVER(() -> 0),
    INTERFACE(() -> 100),
    GENERATOR_ARRAY(() -> GTORules.GENERATOR_ARRAY_LOSS.get() * Loss.PERCENT),
    INTERFACE_HATCH(() -> 0),
    CHARGER(() -> 0),
    HARMONY(() -> 0),
    TRADE(() -> 0),
    TIME_TWISTER(() -> 0);

    private final IntSupplier extraLoss;

    PortKind(IntSupplier extraLoss) {
        this.extraLoss = extraLoss;
    }

    int extraLoss() {
        return extraLoss.getAsInt();
    }
}
