package com.gtocore.mixin.mc;

import com.gtocore.common.weather.WeatherSystem;
import com.gtocore.common.weather.WeatherTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Level.class)
public abstract class LevelWeatherMixin {

    /**
     * @author GTO
     * @reason WeatherSystem owns initialization; vanilla LevelData weather is unused.
     */
    @Overwrite
    protected void prepareWeather() {}

    /**
     * @author GTO
     * @reason Evaluate WeatherSystem transitions without ticking vanilla intensity fields.
     */
    @Overwrite
    public float getRainLevel(float partialTick) {
        return WeatherSystem.rainLevel((Level) (Object) this, partialTick);
    }

    /**
     * @author GTO
     * @reason Evaluate WeatherSystem transitions without ticking vanilla intensity fields.
     */
    @Overwrite
    public float getThunderLevel(float partialTick) {
        return WeatherSystem.thunderLevel((Level) (Object) this, partialTick);
    }

    /**
     * @author GTO
     * @reason Route external server requests to WeatherSystem and ignore vanilla client packets.
     */
    @Overwrite
    public void setRainLevel(float strength) {
        WeatherSystem.setRainLevel((Level) (Object) this, strength);
    }

    /**
     * @author GTO
     * @reason Route external server requests to WeatherSystem and ignore vanilla client packets.
     */
    @Overwrite
    public void setThunderLevel(float strength) {
        WeatherSystem.setThunderLevel((Level) (Object) this, strength);
    }

    /**
     * @author GTO
     * @reason Read actual system weather independently of the visual fade.
     */
    @Overwrite
    public boolean isRaining() {
        var weather = WeatherSystem.current((Level) (Object) this);
        return weather != null && weather.raining();
    }

    /**
     * @author GTO
     * @reason Planetary thunder is defined by WeatherSystem rather than vanilla skylight flags.
     */
    @Overwrite
    public boolean isThundering() {
        var weather = WeatherSystem.current((Level) (Object) this);
        return weather != null && weather.thundering();
    }

    /**
     * @author GTO
     * @reason Query system precipitation, including dry planetary biomes and snow-only weather.
     */
    @Overwrite
    public boolean isRainingAt(BlockPos pos) {
        var level = (Level) (Object) this;
        var weather = WeatherSystem.current(level);
        if (weather == null || !weather.raining() || weather == WeatherTypes.SNOW) {
            return false;
        }
        if (weather == WeatherTypes.ACID_RAIN || weather == WeatherTypes.METHANE_RAIN ||
                (weather == WeatherTypes.THUNDER && level.dimension() != Level.OVERWORLD)) {
            return !level.dimensionType().hasCeiling() &&
                    level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() <= pos.getY();
        }
        return level.canSeeSky(pos) &&
                level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY() <= pos.getY() &&
                level.getBiome(pos).value().getPrecipitationAt(pos) == Biome.Precipitation.RAIN;
    }
}
