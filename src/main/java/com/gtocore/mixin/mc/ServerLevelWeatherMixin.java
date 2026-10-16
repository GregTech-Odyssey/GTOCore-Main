package com.gtocore.mixin.mc;

import com.gtocore.common.weather.WeatherSystem;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerLevel.class)
public abstract class ServerLevelWeatherMixin {

    /**
     * @author GTO
     * @reason WeatherSystem alone advances weather and sends transition packets.
     */
    @Overwrite
    private void advanceWeatherCycle() {}

    /**
     * @author GTO
     * @reason External mods retain the overworld-only contract; commands use the local entry point.
     */
    @Overwrite
    public void setWeatherParameters(int clearTime, int weatherTime, boolean rain, boolean thunder) {
        var level = (ServerLevel) (Object) this;
        if (level.dimension() == Level.OVERWORLD) {
            WeatherSystem.get(level.getServer()).change(level,
                    WeatherSystem.profile(level.dimension()).vanillaWeather(rain, thunder),
                    Math.max(1, rain || clearTime <= 0 ? weatherTime : clearTime));
        }
    }

    /**
     * @author GTO
     * @reason Sleep clears only this dimension's natural WeatherSystem schedule.
     */
    @Overwrite
    private void resetWeatherCycle() {
        var level = (ServerLevel) (Object) this;
        var profile = WeatherSystem.profile(level.dimension());
        WeatherSystem.get(level.getServer()).change(level, profile.clearWeather(), profile.entries()[0].duration());
    }
}
