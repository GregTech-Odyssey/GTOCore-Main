package com.gtocore.common.machine.multiblock.noenergy;

import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.NoEnergyMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.api.stacks.AEFluidKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HeatExchangerMachine extends NoEnergyMultiblockMachine implements IExplosionMachine {

    private static final Fluid Steam = GTMaterials.Steam.getFluid();
    private static final Fluid HighPressureSteam = GTOMaterials.HighPressureSteam.getFluid();
    private static final Fluid SupercriticalSteam = GTOMaterials.SupercriticalSteam.getFluid();
    private static final Fluid DistilledWater = GTMaterials.DistilledWater.getFluid();
    private static final int DURATION = 200;
    private static final int WARMUP_TIME = 800;

    public HeatExchangerMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @SaveToDisk(defaultValue = "0")
    private long hs;

    @SaveToDisk(defaultValue = "false")
    private boolean water;

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        water = recipe.fluidInputs.ingredient(1).displayKey() instanceof AEFluidKey fluidKey && fluidKey.getFluid() == Fluids.WATER;
        long eu = recipe.data.getLong(GTORecipeDataKeys.EU);
        boolean warmedUp = getRecipeLogic().getTotalContinuousRunningTime() + DURATION > WARMUP_TIME;
        var builder = getRecipeBuilder()
                .inputFluids(recipe.fluidInputs.ingredient(0), recipe.inputAmount(recipe.fluidInputs, 0))
                .outputFluids(recipe.fluidOutputs.ingredient(0), recipe.fluidOutputs.effective(0, recipe.scale))
                .duration(DURATION);
        if (water) {
            if (warmedUp) builder.outputFluids(HighPressureSteam, eu / 2);
            else builder.outputFluids(Steam, eu * 2);
        } else {
            if (warmedUp) builder.outputFluids(SupercriticalSteam, eu / 8);
            else builder.outputFluids(HighPressureSteam, eu / 2);
        }
        var result = ParallelLogic.accurateParallel(this, unit, builder.buildRawRecipe(), Integer.MAX_VALUE);
        if (result == null) return null;
        hs = result.parallels * eu / 2;
        return result;
    }

    @Override
    public boolean handleRecipeInput(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (super.handleRecipeInput(unit, recipe)) {
            if (!unit.inputFluid(water ? Fluids.WATER : DistilledWater, hs / 40)) {
                (water ? IdleReason.WATER_SHORT : IdleReason.DISTILLED_WATER_SHORT).setReason(this, hs / 40, -1);
                doExplosion(Math.min(10, hs / 10000));
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        hs = 0;
    }
}
