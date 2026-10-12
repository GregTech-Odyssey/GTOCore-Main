package com.gtocore.common.data;

import com.gtolib.GTOCore;
import com.gtolib.api.lang.CNEN;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import static com.gtocore.data.lang.LangHandler.addCNEN;

public class GTOFluidStorageKey

{

    public static final O2OOpenCacheHashMap<String, CNEN> LANG = GTCEu.isDataGen() ? new O2OOpenCacheHashMap<>() : null;

    public static void initLang() {
        LANG.forEach((k, v) -> addCNEN("gtocore.fluid." + k, v.cn(), v.en()));
    }

    public static final FluidStorageKey ENERGY_STORAGE = key("energy_storage", "%s储能电解液", "%s Charged Electrolyte");
    public static final FluidStorageKey ENERGY_RELEASE = key("energy_release", "%s释能电解液", "%s Discharged Electrolyte");

    public static final FluidStorageKey HIGH_PRESSURE_GAS = key(("high_pressure_gas"), "高压%s", "High Pressure %s");

    private static FluidStorageKey key(String id, String cn, String en) {
        if (LANG != null) {
            LANG.put(id, new CNEN(cn, en));
        }
        return new FluidStorageKey(GTOCore.id(id),
                MaterialIconType.liquid,
                m -> m.getName() + "_" + id,
                m -> "gtocore.fluid." + id,
                FluidState.LIQUID, 0);
    }
}
