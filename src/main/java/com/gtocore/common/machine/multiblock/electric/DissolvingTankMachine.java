package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.common.machine.multiblock.FluidRenderUtils;

import com.gtolib.api.machine.feature.multiblock.IFluidRendererMachine;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.GTORecipeModifiers;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.issue.GTIssues;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluid;

import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
        var fluidStack1 = fluidList.get(0);
        var fluidStack2 = fluidList.get(1);
        long[] a = unit.getFluidAmount(true, fluidStack1.inner.getFluid(), fluidStack2.inner.getFluid());
        if (a[1] > 0) {
            recipe = GTORecipeModifiers.UPGRADE_PARALLELIZABLE_OVERCLOCK.applyModifier(this, unit, recipe);
            if (recipe != null) {
                if ((double) a[0] / a[1] != ((double) fluidStack1.amount) / fluidStack2.amount) {
                    recipe.fluidOutputs = Collections.emptyList();
                    recipe.itemOutputs = Collections.emptyList();
                }
                return recipe;
            }
            return null;
        }
        reportIssue(GTIssues.INPUT_SHORT, null, IO.IN, FluidRecipeInfo.INSTANCE, 1, -1, -1, recipe.definition);
        return null;
    }
}
