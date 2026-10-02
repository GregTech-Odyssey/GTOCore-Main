package com.gtocore.mixin.gtm.machine;

import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.data.IdleReason;

import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.heat.HeatHandler;
import com.gtolib.api.machine.heat.feature.IHeatContainerMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.checkerframework.common.aliasing.qual.Unique;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LargeBoilerMachine.class)
public abstract class LargeBoilerMachineMixin extends WorkableMultiblockMachine implements IHeatContainerMachine {

    @Shadow(remap = false)
    @Final
    public int maxTemperature;
    @Shadow(remap = false)
    @Final
    public int heatSpeed;
    @org.spongepowered.asm.mixin.Unique
    @SaveToDisk
    private HeatHandler gto$heatContainer;

    public LargeBoilerMachineMixin(MetaMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Inject(method = "<init>", at = @org.spongepowered.asm.mixin.injection.At("RETURN"), remap = false)
    private void init(MetaMachineBlockEntity holder, int maxTemperature, int heatSpeed, Object[] args, CallbackInfo ci) {
        gto$heatContainer = new HeatHandler(holder, maxTemperature, 2, heatSpeed / 4f, 0);
        gto$heatContainer.setAllowExplosion(false);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        gto$heatContainer.onLoad();
    }

    @Override
    public boolean usePrioritySearch() {
        return true;
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public static @Nullable GTRecipe recipeModifier(IRecipeHandlerHolder machine, RecipeHandlerUnit unit, GTRecipe recipe) {
        if (machine instanceof LargeBoilerMachine largeBoilerMachine) {
            if (recipe.data.getInt(GTORecipeDataKeys.TEMPERATURE) > largeBoilerMachine.getCurrentTemperature() + 274) {
                IdleReason.INSUFFICIENT_TEMPERATURE.setReason(machine, recipe.data.getInt(GTORecipeDataKeys.TEMPERATURE), largeBoilerMachine.getCurrentTemperature() + 274);
                return null;
            }
            double duration = recipe.duration * 1600.0D / largeBoilerMachine.maxTemperature;
            if (duration < 1) {
                recipe = ParallelLogic.accurateParallel(machine, unit, recipe, (long) (1 / duration));
                if (recipe == null) return null;
            }
            if (largeBoilerMachine.getThrottle() < 100) {
                duration = duration * 100 / largeBoilerMachine.getThrottle();
            }
            recipe.duration = (int) duration;
            return recipe;
        }
        IdleReason.NOT_APPLICABLE.setReason(machine);
        return null;
    }

    @Override
    public IHeatContainer getHeatContainer() {
        return gto$heatContainer;
    }
}
