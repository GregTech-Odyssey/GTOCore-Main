package com.gtocore.common.pipe.steam;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;

public final class SteamFluidFilter {

    private static AEFluidKey[] steamKeys;

    private SteamFluidFilter() {}

    public static boolean isSteam(AEKey key) {
        return key instanceof AEFluidKey fluid && isSteam(fluid.getFluid());
    }

    public static boolean isSteam(Fluid fluid) {
        return fluid == GTMaterials.Steam.getFluid();
    }

    public static AEFluidKey[] steamKeys() {
        var keys = steamKeys;
        if (keys == null) {
            steamKeys = keys = new AEFluidKey[] { AEFluidKey.of(GTMaterials.Steam.getFluid()) };
        }
        return keys;
    }
}
