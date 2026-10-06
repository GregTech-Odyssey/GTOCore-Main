package com.gtocore.common.weather;

/** A duration is mean +/- variation, in ticks; weights are relative occurrence probabilities. */
public record WeatherProfile(boolean stellarInfluence, Entry... entries) {

    public record Entry(WeatherType weather, int weight, int duration, int variation) {

        public Entry {
            if (weight <= 0 || duration <= variation || variation < 0 || duration > Integer.MAX_VALUE - variation) {
                throw new IllegalArgumentException("Invalid weather weight or duration");
            }
        }
    }

    public WeatherType clearWeather() {
        return entries[0].weather();
    }

    public Entry find(WeatherType weather) {
        for (var entry : entries) {
            if (entry.weather() == weather) return entry;
        }
        return null;
    }

    public WeatherType vanillaWeather(boolean rain, boolean thunder) {
        if (!rain) return clearWeather();
        if (find(WeatherTypes.SOLAR_STORM) != null) return WeatherTypes.SOLAR_STORM;
        if (thunder) return find(WeatherTypes.THUNDER) == null ? null : WeatherTypes.THUNDER;
        for (var entry : entries) {
            if (entry.weather().raining() && !entry.weather().thundering()) return entry.weather();
        }
        return null;
    }
}
