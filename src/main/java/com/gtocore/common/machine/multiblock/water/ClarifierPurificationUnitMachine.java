package com.gtocore.common.machine.multiblock.water;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.machine.multiblock.FluidRenderUtils;

import com.gtolib.api.machine.feature.multiblock.IFluidRendererMachine;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;

import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ClarifierPurificationUnitMachine extends WaterPurificationUnitMachine implements IFluidRendererMachine {

    private static final Fluid AIR = GTMaterials.Air.getFluid();
    private static final Fluid[] CATALYSTS = { WaterPurificationPlantMachine.GradePurifiedWater4, WaterPurificationPlantMachine.GradePurifiedWater3, WaterPurificationPlantMachine.GradePurifiedWater2, WaterPurificationPlantMachine.GradePurifiedWater1 };
    private static final int[] CATALYST_DIVISORS = { 16, 4, 2, 1 };
    private static final int[] CATALYST_CHANCES = { 100, 95, 90, 85 };

    @SaveToDisk(defaultValue = "0")
    private int count;
    @SaveToDisk(defaultValue = "0")
    private int chance;
    @Getter
    @SyncToClient(scheduleUpdate = true, autoDetect = false)
    private final Set<BlockPos> fluidBlockOffsets = FluidRenderUtils.emptyFluidBlockOffsets();
    @Getter
    @SyncToClient
    private Fluid cachedFluid;

    public ClarifierPurificationUnitMachine(MetaMachineBlockEntity holder) {
        super(holder, 1);
    }

    @Override
    public void beforeWorking(RecipeHandlerUnit unit, GTRecipe recipe) {
        cachedFluid = IFluidRendererMachine.getFluid(recipe);
        super.beforeWorking(unit, recipe);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        FluidRenderUtils.loadFluidBlockOffsets(this);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        FluidRenderUtils.clearFluidBlockOffsets(this);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (count > 100) {
            textList.add(Component.translatable("gtceu.top.maintenance_broken").withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    long prepareRecipe(RecipeHandlerUnit unit) {
        eut = 0;
        if (count > 100) {
            if (!simulateOutputItem(GTOItems.SCRAP.asItem(), count / 20)) return 0;
            long air = count * 10000L;
            long water = (200L + GTValues.RNG.nextInt(100)) * 1000;
            // 空气和水都够才开始扣，否则扣完空气后水不够会白白损失空气，且每次搜索配方都会再扣一次
            if (matchFluid(AIR, air) && matchFluid(Fluids.WATER, water) && inputFluid(AIR, air) && inputFluid(Fluids.WATER, water) && outputItem(GTOItems.SCRAP.asItem(), count / 20)) {
                count = 0;
            } else {
                return 0;
            }
        }
        long inputCount = Math.min(parallel(), unit.getFluidAmount(true, Fluids.WATER)[0]);
        if (inputCount > 0) {
            long outputCount = inputCount * 9 / 10;
            RecipeBuilder builder = getRecipeBuilder();
            builder.duration(WaterPurificationPlantMachine.DURATION).inputFluids(Fluids.WATER, inputCount);
            chance = addCatalyst(unit, builder, outputCount / 10);
            if (GTValues.RNG.nextInt(100) < chance) {
                builder.outputFluids(WaterPurificationPlantMachine.GradePurifiedWater1, outputCount);
            } else {
                builder.outputFluids(Fluids.WATER, outputCount);
            }
            recipe = builder.buildRawRecipe();
            if (matchRecipe(unit, recipe)) {
                count += NumberUtils.chanceOccurrences((int) Math.min(10000, inputCount / 1000), 3, 800);
                calculateVoltage(inputCount);
            }
        }
        return eut;
    }

    @Override
    double getSuccessChance() {
        return chance;
    }

    /// 在当前输入单元里选够量的最高等级净化水作催化，写进配方随水一起扣除，配方未启动时不消耗
    private static int addCatalyst(RecipeHandlerUnit unit, RecipeBuilder builder, long count) {
        long[] amounts = unit.getFluidAmount(true, CATALYSTS);
        for (int i = 0; i < CATALYSTS.length; i++) {
            long need = Math.max(1, count / CATALYST_DIVISORS[i]);
            if (amounts[i] >= need) {
                builder.inputFluids(CATALYSTS[i], need);
                return CATALYST_CHANCES[i];
            }
        }
        return 70;
    }
}
