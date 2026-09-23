package com.gtocore.common.machine.multiblock.water;

import com.gtocore.common.data.GTOMaterials;

import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.material.Fluid;

import com.gto.datasynclib.annotations.SaveToDisk;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OzonationPurificationUnitMachine extends WaterPurificationUnitMachine implements IExplosionMachine {

    private static final Fluid Ozone = GTOMaterials.Ozone.getFluid();
    private static final Fluid[] CATALYSTS = { WaterPurificationPlantMachine.GradePurifiedWater3, WaterPurificationPlantMachine.GradePurifiedWater2 };

    @SaveToDisk(defaultValue = "0")
    private int chance;

    public OzonationPurificationUnitMachine(MetaMachineBlockEntity holder) {
        super(holder, 2);
    }

    @Override
    double getSuccessChance() {
        return chance;
    }

    @Override
    long prepareRecipe(RecipeHandlerUnit unit) {
        eut = 0;
        long[] a = unit.getFluidAmount(true, WaterPurificationPlantMachine.GradePurifiedWater1, Ozone);
        long ozoneCount = a[1];
        if (ozoneCount > 1024000) {
            inputFluid(Ozone, ozoneCount);
            doExplosion(10);
        } else {
            long inputCount = Math.min(parallel(), Math.min(a[0], ozoneCount * 10000));
            if (inputCount > 0) {
                long outputCount = inputCount * 9 / 10;
                RecipeBuilder builder = getRecipeBuilder();
                builder.duration(WaterPurificationPlantMachine.DURATION).inputFluids(Ozone, inputCount / 10000).inputFluids(WaterPurificationPlantMachine.GradePurifiedWater1, inputCount);
                chance = getChance(unit, builder, outputCount / 10, ozoneCount);
                if (Math.random() * 100 <= chance) {
                    builder.outputFluids(WaterPurificationPlantMachine.GradePurifiedWater2, outputCount);
                } else {
                    builder.outputFluids(WaterPurificationPlantMachine.GradePurifiedWater1, outputCount);
                }
                recipe = builder.buildRawRecipe();
                if (matchRecipe(unit, recipe)) {
                    calculateVoltage(inputCount);
                }
            }
        }
        return eut;
    }

    /// 催化用的净化水取自当前输入单元，写进配方随原料一起扣除，配方未启动时不消耗
    private static int getChance(RecipeHandlerUnit unit, RecipeBuilder builder, long count, long ozoneCount) {
        int a = (int) (80 * Math.log(1 + ozoneCount / 10000.0) / Math.log(103.0));
        long[] amounts = unit.getFluidAmount(true, CATALYSTS);
        long need3 = Math.max(1, count / 4);
        if (amounts[0] >= need3) {
            builder.inputFluids(WaterPurificationPlantMachine.GradePurifiedWater3, need3);
            return a + 20;
        }
        long need2 = Math.max(1, count);
        if (amounts[1] >= need2) {
            builder.inputFluids(WaterPurificationPlantMachine.GradePurifiedWater2, need2);
            return a + 15;
        }
        return a;
    }
}
