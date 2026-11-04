package com.gtocore.common.recipe.condition;

import com.gtolib.api.machine.feature.ISpaceWorkspaceMachine;
import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;

import net.minecraft.network.chat.Component;

public final class SpaceWorkspaceCondition extends RecipeCondition {

    private static final Component REQUIRES = Component.translatable("gtceu.recipe.info.requires");

    private final IWorkInSpaceMachine machine;

    public SpaceWorkspaceCondition(IWorkInSpaceMachine machine) {
        this.machine = machine;
    }

    @Override
    public Component getTooltips() {
        ISpaceWorkspaceMachine provider = machine.getWorkspaceProvider();
        if (provider != null) return provider.getWorkspaceNotReadyReason();
        return (machine.self() instanceof IMultiController ? IdleReason.SPACE_NO_WORKSPACE_MULTIBLOCK : IdleReason.SPACE_NO_WORKSPACE).reason();
    }

    @Override
    public void appendInfo(GTRecipeDefinition recipe, RecipeInfoBuilder info) {
        info.requirement(REQUIRES, () -> Component.translatable("gtocore.recipe.info.space_workspace"));
    }

    @Override
    protected boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (machine.canWorkInSpaceIndependently()) return true;
        ISpaceWorkspaceMachine provider = machine.getWorkspaceProvider();
        return provider != null && provider.isWorkspaceReady();
    }
}
