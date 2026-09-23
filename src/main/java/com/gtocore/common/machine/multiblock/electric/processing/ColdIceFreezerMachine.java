package com.gtocore.common.machine.multiblock.electric.processing;

import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.trait.InternalSlotRecipeHandler;

import com.gtolib.api.machine.multiblock.CustomParallelMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ColdIceFreezerMachine extends CustomParallelMultiblockMachine {

    private static final FluidStack ICE = GTMaterials.Ice.getFluid(1);

    public ColdIceFreezerMachine(MetaMachineBlockEntity holder) {
        super(holder, m -> 64);
    }

    /**
     * @param reserved 配方所在 unit 里需为配方原料预留的液态冰量，仅配方开始时非 0
     */
    private boolean inputFluid(@Nullable RecipeHandlerUnit recipeUnit, long reserved) {
        var fluid = ICE.getRawFluid();
        long amount = (1L << Math.max(0, getTier() - 2)) * 10L;
        // 被动消耗优先从普通输入仓取：配方所在 unit 和样板总成槽位里的液态冰是配方原料，
        // 且样板槽位 priority 恒为 HIGH、总排在最前，按默认顺序扣会先吃掉原料
        for (var unit : getInputUnits()) {
            if (unit != recipeUnit && !(unit instanceof InternalSlotRecipeHandler.AbstractRHL) && unit.inputFluid(fluid, amount)) return true;
        }
        for (var unit : getInputUnits()) {
            // 从配方所在 unit 扣时须够"配方 + 被动"两份，否则扣完被动后配方扣料半途失败，已扣的物品会被吞
            if (unit == recipeUnit && reserved > 0 && unit.getFluidAmount(true, fluid)[0] < amount + reserved) continue;
            if (unit.inputFluid(fluid, amount)) return true;
        }
        setIdleReason(() -> ActionResult.failInsufficientIn(ICE.getDisplayName()).reason());
        return false;
    }

    private static long iceInput(GTRecipe recipe) {
        long total = 0;
        for (var content : recipe.fluidInputs) {
            if (content.inner.testFluid(ICE.getRawFluid())) total += content.amount;
        }
        return total;
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (getOffsetTimer() % 20 == 0 && !inputFluid(getRecipeLogic().getLastOriginUnit(), 0)) return false;
        return super.handleTickRecipe(recipe);
    }

    @Override
    public boolean handleRecipeInput(RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        return inputFluid(unit, iceInput(recipe)) && super.handleRecipeInput(unit, recipe);
    }

    @Override
    public boolean recipeTypeAvailable(GTRecipeType type) {
        if (type == GTORecipeTypes.ATOMIZATION_CONDENSATION_RECIPES) {
            return formedAmount > 0;
        }
        return true;
    }
}
