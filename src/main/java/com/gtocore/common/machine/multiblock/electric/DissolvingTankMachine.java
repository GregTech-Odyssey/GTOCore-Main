package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.common.machine.multiblock.FluidRenderUtils;

import com.gtolib.api.machine.feature.multiblock.IFluidRendererMachine;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.GTORecipeModifiers;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentList;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;

import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class DissolvingTankMachine extends ElectricMultiblockMachine implements IFluidRendererMachine {

    @Getter
    @SyncToClient(scheduleUpdate = true, autoDetect = false)
    private final Set<BlockPos> fluidBlockOffsets = FluidRenderUtils.emptyFluidBlockOffsets();
    @Getter
    @SyncToClient
    private Fluid cachedFluid;

    public DissolvingTankMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
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

    @Nullable
    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (hasStructurePart(StructureModuleKeys.DISSOLVING_TANK_EXTENSION)) {
            return GTORecipeModifiers.UPGRADE_PARALLELIZABLE_OVERCLOCK.applyModifier(this, unit, recipe);
        }
        var fluidList = recipe.fluidInputs;
        long amount1 = fluidList.amount(0);
        long amount2 = fluidList.amount(1);
        long a0 = unit.count(fluidList.ingredient(0), true);
        long a1 = unit.count(fluidList.ingredient(1), true);
        if (a1 > 0) {
            recipe = GTORecipeModifiers.UPGRADE_PARALLELIZABLE_OVERCLOCK.applyModifier(this, unit, recipe);
            if (recipe != null) {
                if ((double) a0 / a1 != ((double) amount1) / amount2) {
                    recipe.fluidOutputs = ContentList.EMPTY;
                    recipe.itemOutputs = ContentList.EMPTY;
                }
                return recipe;
            }
        } else {
            setIdleReason(ActionResult.failInsufficientIn(FluidRecipeInfo.INSTANCE.getName()));
        }
        return null;
    }
}
