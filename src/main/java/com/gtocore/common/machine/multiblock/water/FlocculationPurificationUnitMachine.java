package com.gtocore.common.machine.multiblock.water;

import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.material.Fluid;

import com.gto.datasynclib.annotations.SaveToDisk;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FlocculationPurificationUnitMachine extends WaterPurificationUnitMachine {

    private static final Fluid PolyAluminiumChloride = GTOMaterials.PolyAluminiumChloride.getFluid();
    private static final Fluid FlocculationWasteSolution = GTOMaterials.FlocculationWasteSolution.getFluid();

    private static final long CHEMICAL_PER_LEVEL = 100000;

    @SaveToDisk(defaultValue = "0")
    private long inputCount;

    /// 本轮累计消耗的聚合氯化铝，同时也是絮凝废液的产量
    @SaveToDisk(defaultValue = "0")
    private long outputCount;

    public FlocculationPurificationUnitMachine(MetaMachineBlockEntity holder) {
        super(holder, 4);
    }

    @Override
    public void onWorking() {
        super.onWorking();
        if (getOffsetTimer() % 20 == 0) {
            long amount = getFluidAmount(true, PolyAluminiumChloride)[0];
            if (inputFluid(PolyAluminiumChloride, amount)) {
                outputCount += amount;
            }
        }
    }

    /// 按本轮消耗总量计算：每满 100,000mB +10%（封顶 100%），总量有零头时再乘 2^(-10 × 零头/100,000)
    @Override
    double getSuccessChance() {
        double chance = Math.min(100, outputCount / CHEMICAL_PER_LEVEL * 10);
        long overflow = outputCount % CHEMICAL_PER_LEVEL;
        if (overflow > 0) chance *= Math.pow(2, -10.0 * overflow / CHEMICAL_PER_LEVEL);
        return chance;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        outputFluid(FlocculationWasteSolution, outputCount);
        long outputCount = inputCount * 9 / 10;
        if (Math.random() * 100 < getSuccessChance()) outputFluid(WaterPurificationPlantMachine.GradePurifiedWater3, outputCount);
        else outputFluid(WaterPurificationPlantMachine.GradePurifiedWater2, outputCount);
    }

    @Override
    long prepareRecipe(RecipeHandlerUnit unit) {
        eut = 0;
        outputCount = 0;
        inputCount = Math.min(parallel(), unit.getFluidAmount(true, WaterPurificationPlantMachine.GradePurifiedWater2)[0]);
        if (inputCount > 0) {
            recipe = getRecipeBuilder().duration(WaterPurificationPlantMachine.DURATION).inputFluids(WaterPurificationPlantMachine.GradePurifiedWater2, inputCount).buildRawRecipe();
            if (matchRecipe(unit, recipe)) {
                calculateVoltage(inputCount);
            }
        }
        return eut;
    }
}
