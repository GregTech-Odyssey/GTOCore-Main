package com.gtocore.api.research;

import com.gtolib.utils.AEChemicalHelper;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.fluids.FluidConstants;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;

import com.gto.fastcollection.O2OOpenCacheHashMap;

import java.util.function.ToLongFunction;

public record ResearchTag(String name, ToLongFunction<AEKey> valueSupplier) {

    public static final O2OOpenCacheHashMap<String, ResearchTag> TAGS = new O2OOpenCacheHashMap<>();

    public ResearchTag {
        TAGS.put(name, this);
    }

    public static final ResearchTag ID = new ResearchTag("id", id -> id.getId().getPath().length());
    public static final ResearchTag ELEMENT = new ResearchTag("element", key -> {
        Element element = AEChemicalHelper.getMaterial(key).getElement();
        if (element != null) {
            if (element.protons() > 118) return 1;
            return element.neutrons() + element.protons();
        }
        return 0;
    });
    public static final ResearchTag LOW_TEMP_FLUID = new ResearchTag("low_temp_fluid", key -> {
        if (key instanceof AEFluidKey fluidKey) {
            var fluid = fluidKey.getFluid();
            var fluidType = fluid.getFluidType();
            return Math.max(0, FluidConstants.CRYOGENIC_FLUID_THRESHOLD - fluidType.getTemperature());
        }
        return 0;
    });
    public static final ResearchTag PLASMA = new ResearchTag("plasma", key -> {
        if (key instanceof AEFluidKey fluidKey) {
            var fluid = fluidKey.getFluid();
            var fluidType = fluid.getFluidType();
            return fluid instanceof GTFluid gtFluid && gtFluid.getState() == FluidState.PLASMA ? (long) Math.log10(fluidType.getTemperature()) : 0;
        }
        return 0;
    });
}
