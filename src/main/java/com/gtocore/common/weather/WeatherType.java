package com.gtocore.common.weather;

import net.minecraft.network.chat.Component;

/** Registered identity; probabilities and durations belong to the dimension's profile. */
public record WeatherType(String id, String cn, String en, boolean raining, boolean thundering) {

    public String translationKey() {
        return "gtocore.weather." + id;
    }

    public Component displayName() {
        return Component.translatable(translationKey());
    }
}
