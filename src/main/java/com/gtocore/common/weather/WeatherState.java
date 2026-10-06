package com.gtocore.common.weather;

import net.minecraft.util.Mth;

/** A transition is evaluated on demand; no per-tick intensity updates or packets are needed. */
public record WeatherState(WeatherType weather, float rain, float thunder, long started) {

    public float rainLevel(long gameTime, float partialTick) {
        return intensity(rain, weather.raining(), gameTime, partialTick);
    }

    public float thunderLevel(long gameTime, float partialTick) {
        return rawThunderLevel(gameTime, partialTick) * rainLevel(gameTime, partialTick);
    }

    public float rawThunderLevel(long gameTime, float partialTick) {
        return intensity(thunder, weather.thundering(), gameTime, partialTick);
    }

    private float intensity(float initial, boolean active, long gameTime, float partialTick) {
        float elapsed = Math.max(0, gameTime - started + partialTick);
        return Mth.clamp(initial + (active ? elapsed : -elapsed) * 0.01F, 0, 1);
    }
}
