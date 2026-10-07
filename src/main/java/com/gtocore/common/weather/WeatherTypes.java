package com.gtocore.common.weather;

import com.gregtechceu.gtceu.api.registry.GTRegistry;

import net.minecraft.resources.ResourceLocation;

public final class WeatherTypes {

    public static final GTRegistry.Str<WeatherType> REGISTRY = new GTRegistry.Str<>(
            ResourceLocation.parse("gtocore:weather"), WeatherType::id, false);

    static {
        REGISTRY.unfreeze();
    }

    public static final WeatherType CLEAR = register("clear", "晴天", "Clear", false, false);
    public static final WeatherType RAIN = register("rain", "雨天", "Rain", true, false);
    public static final WeatherType THUNDER = register("thunder", "雷暴", "Thunderstorm", true, true);
    public static final WeatherType CALM = register("calm", "平静", "Calm", false, false);
    public static final WeatherType SOLAR_STORM = register("solar_storm", "太阳风暴", "Solar Storm", true, true);
    public static final WeatherType ACID_RAIN = register("acid_rain", "酸雨", "Acid Rain", true, false);
    public static final WeatherType METHANE_RAIN = register("methane_rain", "甲烷雨", "Methane Rain", true, false);
    public static final WeatherType DUST_STORM = register("dust_storm", "沙尘暴", "Dust Storm", false, false);
    public static final WeatherType SNOW = register("snow", "降雪", "Snow", true, false);

    static {
        REGISTRY.freeze();
    }

    private static WeatherType register(String id, String cn, String en, boolean rain, boolean thunder) {
        var weather = new WeatherType(id, cn, en, rain, thunder);
        REGISTRY.register(id, weather);
        return weather;
    }

    private WeatherTypes() {}
}
