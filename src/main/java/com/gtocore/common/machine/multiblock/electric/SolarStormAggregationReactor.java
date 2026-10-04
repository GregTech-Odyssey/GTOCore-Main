package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.integration.ae.SolarStormHandler;

import com.gtolib.api.machine.multiblock.CrossRecipeMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import static com.gtocore.data.IdleReason.SOLAR_STORM_REQUIRED;
import static com.gtocore.data.IdleReason.SOLAR_SURFACE_FLARE_INTERFACE;

public final class SolarStormAggregationReactor extends CrossRecipeMultiblockMachine {

    public SolarStormAggregationReactor(MetaMachineBlockEntity holder) {
        super(holder, false, true, MachineUtils::getHatchParallel);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote() && SolarStormHandler.isSolarSurface(getLevel())) {
            SolarStormHandler.track(this);
        }
    }

    @Override
    public void onUnload() {
        if (!isRemote() && SolarStormHandler.isSolarSurface(getLevel())) {
            SolarStormHandler.forget(this);
        }
        super.onUnload();
    }

    @Override
    public boolean checkConditions(RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (!SolarStormHandler.isStormActive(getLevel())) {
            if (!SolarStormHandler.isSolarSurface(getLevel())) {
                setIdleReason(SOLAR_SURFACE_FLARE_INTERFACE);
            } else {
                setIdleReason(SOLAR_STORM_REQUIRED);
            }
            return false;
        }
        return super.checkConditions(unit, recipe);
    }
}
