package com.gtocore.api.wireless.energy;

public enum SettleResult {

    OK,
    NO_ACCOUNT,
    NO_COVERAGE,
    NO_STORAGE,
    NO_TRANSFER,
    RESERVED;

    public boolean ok() {
        return this == OK;
    }
}
