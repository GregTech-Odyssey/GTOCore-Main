package com.gtocore.common.machine.monitor;

public enum DisplayType {

    STYLED_TEXT(0b01),
    CUSTOM_RENDERER(0b10),
    BOTH(0b11);

    private final int value;

    DisplayType(int value) {
        this.value = value;
    }

    public boolean hasStyledText() {
        return (value & 0b01) != 0;
    }

    public boolean hasCustomRenderer() {
        return (value & 0b10) != 0;
    }
}
