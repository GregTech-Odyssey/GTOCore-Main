package com.gtocore.common.wireless.energy.map;

public enum GridMapMode {

    VIEW,
    PICK;

    private static final GridMapMode[] VALUES = values();

    public static GridMapMode of(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : VIEW;
    }
}
