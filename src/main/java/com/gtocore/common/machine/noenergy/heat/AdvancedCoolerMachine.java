package com.gtocore.common.machine.noenergy.heat;

import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.api.machine.SimpleNoEnergyMachine;
import com.gtolib.api.machine.heat.HeatHandler;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluid;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public final class AdvancedCoolerMachine extends SimpleNoEnergyMachine implements IHeatContainerMachine, ICustomRecipeLogicHolder {

    @Getter
    @SaveToDisk
    @SyncToClient
    private final HeatHandler heatContainer;

    @SaveToDisk
    private int coolantEfficiency = 0;

    public AdvancedCoolerMachine(MetaMachineBlockEntity holder) {
        super(holder, 2, i -> 16000);
        heatContainer = new HeatHandler(holder, 3000, 2, 4, 0.01);
        heatContainer.setSideIOCondition(s -> s != Direction.DOWN && s != Direction.UP);
        heatContainer.addChangedListener(getRecipeLogic()::updateTickSubscription);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        heatContainer.onLoad();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        heatContainer.onUnLoad();
    }

    @Override
    @NotNull
    public GTRecipeType getRecipeType() {
        return GTORecipeTypes.F1A1B;
    }

    @Override
    public GTRecipe fullModifyRecipe(RecipeHandlerUnit unit, GTRecipeDefinition definition) {
        return definition.toRuntime();
    }

    @Override
    public void onWorking() {
        super.onWorking();
        if (getOffsetTimer() % 20 == 0) {
            if (heatContainer.getCurrentHeat() < coolantEfficiency) {
                getRecipeLogic().markLastRecipeDirty();
            } else {
                heatContainer.removeHeatUnrestricted(coolantEfficiency, false);
            }
        }
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (heatContainer.getCurrentHeat() < 8) {
            setIdleReason(IdleReason.INSUFFICIENT_TEMPERATURE);
            return null;
        }
        AtomicReference<Fluid> coolantFluid = new AtomicReference<>();
        unit.forEachFluids(true, (f, a) -> {
            if (f.isEmpty() || a < 1000) {
                return false;
            }
            var type = f.getFluid().getFluidType();
            var fluidTemp = type.getTemperature();
            var tempDiff = heatContainer.getTemperature() - fluidTemp;
            if (fluidTemp < 320 && tempDiff > 0) {
                coolantEfficiency = Math.min(200, (int) (tempDiff / 4));
            } else {
                coolantEfficiency = 0;
                setIdleReason(IdleReason.INSUFFICIENT_TEMPERATURE);
                return false;
            }
            coolantFluid.set(f.getFluid());
            return true;
        });
        if (coolantFluid.get() == null) {
            return null;
        }
        return getRecipeBuilder().duration(20).inputFluids(coolantFluid.get(), 1000)
                .outputFluids(coolantFluid.get(), 990).build();
    }
}
