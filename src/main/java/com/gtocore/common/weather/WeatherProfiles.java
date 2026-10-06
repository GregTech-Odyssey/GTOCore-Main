package com.gtocore.common.weather;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import static com.gtocore.common.weather.WeatherTypes.*;

public final class WeatherProfiles {

    public static final WeatherProfile DEFAULT = new WeatherProfile(true,
            entry(CLEAR, 70, 18000, 6000), entry(RAIN, 25, 12000, 4000), entry(THUNDER, 5, 6000, 2000));
    public static final WeatherProfile AIRLESS = new WeatherProfile(false, entry(CLEAR, 1, 24000, 0));
    public static final WeatherProfile SPACE = new WeatherProfile(false, entry(CALM, 1, 24000, 0));
    public static final WeatherProfile SOLAR = new WeatherProfile(false,
            entry(CALM, 85, 24000, 8000), entry(SOLAR_STORM, 15, 6000, 2000));
    public static final WeatherProfile ACID = new WeatherProfile(true,
            entry(CLEAR, 10, 6000, 2000), entry(ACID_RAIN, 90, 18000, 6000));
    public static final WeatherProfile MARS = new WeatherProfile(true,
            entry(CLEAR, 75, 18000, 6000), entry(DUST_STORM, 25, 12000, 4000));
    public static final WeatherProfile TITAN = new WeatherProfile(true,
            entry(CLEAR, 60, 18000, 6000), entry(METHANE_RAIN, 35, 12000, 4000), entry(DUST_STORM, 5, 6000, 2000));
    public static final WeatherProfile GLACIO = new WeatherProfile(true,
            entry(CLEAR, 35, 12000, 4000), entry(SNOW, 60, 18000, 6000), entry(THUNDER, 5, 6000, 2000));

    public static WeatherProfile get(ResourceKey<Level> key) {
        if (GTODimensions.isOrbit(key)) return SPACE;
        var dimension = Dimension.get(key);
        if (dimension != null) {
            return dimension.getWeatherProfile();
        }
        return DEFAULT;
    }

    public static boolean sameGalaxy(ResourceKey<Level> planet, ResourceKey<Level> star) {
        var galaxy = GTODimensions.getGalaxy(planet);
        if (galaxy == null) galaxy = Dimension.OVERWORLD.getGalaxy();
        return galaxy != null && galaxy == GTODimensions.getGalaxy(star);
    }

    private static WeatherProfile.Entry entry(WeatherType type, int weight, int duration, int variation) {
        return new WeatherProfile.Entry(type, weight, duration, variation);
    }

    private WeatherProfiles() {}
}
