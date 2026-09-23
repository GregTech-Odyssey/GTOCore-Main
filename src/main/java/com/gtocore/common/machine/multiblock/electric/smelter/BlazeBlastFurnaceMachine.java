package com.gtocore.common.machine.multiblock.electric.smelter;

import com.gtocore.common.machine.trait.InternalSlotRecipeHandler;

import com.gtolib.api.machine.multiblock.CoilCustomParallelMultiblockMachine;
import com.gtolib.api.recipe.GTORecipeModifiers;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BlazeBlastFurnaceMachine extends CoilCustomParallelMultiblockMachine {

    private static final FluidStack BLAZE = GTMaterials.Blaze.getFluid(1);

    public BlazeBlastFurnaceMachine(MetaMachineBlockEntity holder) {
        super(holder, true, true, m -> 64);
    }

    /**
     * @param reserved 配方所在 unit 里需为配方原料预留的液态烈焰量，仅配方开始时非 0
     */
    private boolean inputFluid(@Nullable RecipeHandlerUnit recipeUnit, long reserved) {
        var fluid = BLAZE.getRawFluid();
        long amount = (1L << Math.max(0, getTier() - 2)) * 10L;
        // 与寒冰冷冻机（#1857）相同：被动消耗优先从普通输入仓取，避免先吃掉配方所在 unit 和样板总成槽位里的原料
        var units = getInputUnits();
        int size = units.size();
        for (int i = 0; i < size; i++) {
            var unit = units.get(i);
            if (unit != recipeUnit && !(unit instanceof InternalSlotRecipeHandler.AbstractRHL) && unit.inputFluid(fluid, amount)) return true;
        }
        // 回退时只需再试第一轮跳过的 unit，其余已确认不够量
        for (int i = 0; i < size; i++) {
            var unit = units.get(i);
            if (unit == recipeUnit) {
                // 从配方所在 unit 扣时须够"配方 + 被动"两份，否则扣完被动后配方扣料半途失败，已扣的物品会被吞
                if (reserved > 0 && unit.getFluidAmount(true, fluid)[0] < amount + reserved) continue;
            } else if (!(unit instanceof InternalSlotRecipeHandler.AbstractRHL)) {
                continue;
            }
            if (unit.inputFluid(fluid, amount)) return true;
        }
        setIdleReason(() -> ActionResult.failInsufficientIn(BLAZE.getDisplayName()).reason());
        return false;
    }

    private static long blazeInput(GTRecipe recipe) {
        long total = 0;
        for (var content : recipe.fluidInputs) {
            if (content.inner.testFluid(BLAZE.getRawFluid())) total += content.amount;
        }
        return total;
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        recipe.duration = recipe.duration / 2;
        recipe = ParallelLogic.accurateParallel(this, unit, recipe, getParallel());
        if (recipe == null) return null;
        return GTORecipeModifiers.UPGRADE_EBF_OVERCLOCK.applyModifier(this, unit, recipe);
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (getOffsetTimer() % 20 == 0 && !inputFluid(getRecipeLogic().getLastOriginUnit(), 0)) return false;
        return super.handleTickRecipe(recipe);
    }

    @Override
    public boolean handleRecipeInput(RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        return inputFluid(unit, blazeInput(recipe)) && super.handleRecipeInput(unit, recipe);
    }
}
