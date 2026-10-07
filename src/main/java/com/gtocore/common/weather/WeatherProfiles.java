package com.gtocore.common.weather;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.api.dimension.DimensionSync;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

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
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && server.isSameThread()) {
            var manager = DimensionManager.get(server);
            var descriptor = manager == null ? null : manager.descriptor(key);
            if (descriptor != null) {
                return instanceProfile(descriptor.template().environment().weatherProfile());
            }
        } else if (FMLEnvironment.dist.isClient()) {
            var environment = DimensionSync.clientEnvironment(key);
            if (environment != null) {
                return instanceProfile(environment.weatherProfile());
            }
        }
        if (GTODimensions.isOrbit(key)) {
            return SPACE;
        }
        var dimension = Dimension.get(key);
        if (dimension != null) {
            return dimension.getWeatherProfile();
        }
        return DEFAULT;
    }

    /**
     * 将模板环境中的持久化天气 ID 解析为现有天气配置。
     *
     * @param id 冻结的天气配置 ID
     * @return 对应天气规则
     * @throws IllegalArgumentException ID 未受支持
     */
    private static WeatherProfile instanceProfile(String id) {
        return switch (id) {
            case "calm" -> SPACE;
            case "airless" -> AIRLESS;
            case "solar" -> SOLAR;
            case "acid" -> ACID;
            case "mars" -> MARS;
            case "titan" -> TITAN;
            case "glacio" -> GLACIO;
            case "overworld" -> DEFAULT;
            default -> throw new IllegalArgumentException("Unknown instance weather profile: " + id);
        };
    }

    public static boolean sameGalaxy(ResourceKey<Level> planet, ResourceKey<Level> star) {
        var galaxy = GTODimensions.getGalaxy(planet);
        if (galaxy == null) {
            galaxy = Dimension.OVERWORLD.getGalaxy();
        }
        return galaxy != null && galaxy == GTODimensions.getGalaxy(star);
    }

    private static WeatherProfile.Entry entry(WeatherType type, int weight, int duration, int variation) {
        return new WeatherProfile.Entry(type, weight, duration, variation);
    }

    private WeatherProfiles() {}
}
