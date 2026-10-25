package com.gtocore.common.data;

import com.gtocore.common.saved.DysonSphereSavaedData;

import com.gtolib.api.data.Dimension;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/** 戴森球只遮蔽行星及其轨道的日光，夜间与方块光源不受影响。 */
public final class DysonSphereEffects {

    private DysonSphereEffects() {}

    public static float transmission(Level level, float partialTick) {
        float sunlight = sunlightTransmission(level);
        if (sunlight == 1) return 1;
        float daylight = Mth.clamp(Mth.cos(level.getSunAngle(partialTick)) * 2 + 0.5f, 0, 1);
        return 1 - (1 - sunlight) * daylight;
    }

    /** 晨昏散射来自太阳，遮蔽比例不随日光亮度衰退而恢复。 */
    public static float sunlightTransmission(Level level) {
        var dimension = Dimension.getIncludingOrbits(level.dimension());
        var body = dimension == null ? null : dimension.getBody();
        if (body == null || body.isStar()) return 1;
        int launched = level.isClientSide ? StarMapDysonSync.launched(level) : DysonSphereSavaedData.getDimensionLaunchData(level.dimension());
        return sunlightTransmission(launched);
    }

    static float sunlightTransmission(int launched) {
        return 1 - Math.clamp(launched / (float) StarMapDysonSync.COMPLETE, 0, 1);
    }

    static float transmission(int launched, float daylight) {
        return 1 - (1 - sunlightTransmission(launched)) * daylight;
    }

    public static int skyDarken(Level level, int original) {
        return skyDarken(original, transmission(level, 1));
    }

    static int skyDarken(int original, float transmission) {
        return original + Math.round((15 - original) * (1 - transmission));
    }
}
