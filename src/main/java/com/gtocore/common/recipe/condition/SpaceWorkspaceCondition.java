package com.gtocore.common.recipe.condition;

import com.gtolib.api.machine.feature.ISpaceWorkspaceMachine;
import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.issue.IssueType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.network.chat.Component;

public final class SpaceWorkspaceCondition extends RecipeCondition {

    private final IWorkInSpaceMachine machine;

    public SpaceWorkspaceCondition(IWorkInSpaceMachine machine) {
        this.machine = machine;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtocore.issue.space_no_workspace");
    }

    @Override
    public IssueType getIssueType() {
        return noWorkspaceReason().type();
    }

    @Override
    public void reportFailure(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe, int index) {
        ISpaceWorkspaceMachine provider = machine.getWorkspaceProvider();
        if (provider != null) provider.reportWorkspaceNotReady(holder);
        else noWorkspaceReason().report(holder, IssueStage.CONDITION, recipe);
    }

    @Override
    protected boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        if (machine.canWorkInSpaceIndependently()) return true;
        ISpaceWorkspaceMachine provider = machine.getWorkspaceProvider();
        return provider != null && provider.isWorkspaceReady();
    }

    private IdleReason noWorkspaceReason() {
        return machine.self() instanceof IMultiController ? IdleReason.SPACE_NO_WORKSPACE_MULTIBLOCK : IdleReason.SPACE_NO_WORKSPACE;
    }
}
