package com.gtocore.api.electrochemistry;

import java.util.Locale;

public enum ElectrolyteType {

    WATER,
    ORGANIC,
    MOLTEN_SALT,
    SUPERCRITICAL_FLUID,
    IONIC_LIQUID;

    private final String translationKey = "gtocore.electrolyte_type." + name().toLowerCase(Locale.ROOT);

    public String translationKey() {
        return translationKey;
    }
}
