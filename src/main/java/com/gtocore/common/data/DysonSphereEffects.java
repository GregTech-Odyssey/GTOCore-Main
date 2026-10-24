package com.gtocore.common.data;

import com.gtocore.common.saved.DysonSphereSavaedData;

import com.gtolib.api.data.Dimension;

import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/** 戴森球只遮蔽行星及其轨道的日光，夜间与方块光源不受影响。 */
public final class DysonSphereEffects {

    private DysonSphereEffects() {}

    public static float transmission(Level level, float partialTick) {
        var dimension = Dimension.getIncludingOrbits(level.dimension());
        var body = dimension == null ? null : dimension.getBody();
        if (body == null || body.isStar()) return 1;
        int launched = level.isClientSide ? StarMapDysonSync.launched(level) : DysonSphereSavaedData.getDimensionLaunchData(level.dimension());
        if (launched <= 0) return 1;
        float daylight = Mth.clamp(Mth.cos(level.getSunAngle(partialTick)) * 2 + 0.5f, 0, 1);
        return transmission(launched, daylight);
    }

    static float transmission(int launched, float daylight) {
        return 1 - Math.clamp(launched / (float) StarMapDysonSync.COMPLETE, 0, 1) * daylight;
    }

    public static int skyDarken(Level level, int original) {
        return skyDarken(original, transmission(level, 1));
    }

    static int skyDarken(int original, float transmission) {
        return original + Math.round((15 - original) * (1 - transmission));
    }
}
