package com.gtocore.common.recipe.condition;

import com.gtocore.common.saved.RecipeRunLimitSavaedData;
import com.gtocore.data.IdleReason;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.machine.issue.IssueType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.network.chat.Component;

import java.util.UUID;

public final class RunLimitCondition extends RecipeCondition {

    private final int count;

    public RunLimitCondition(int count) {
        this.count = count;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtocore.recipe.runlimit.count", count);
    }

    @Override
    public IssueType getIssueType() {
        return IdleReason.RUN_LIMIT.type();
    }

    @Override
    public void reportFailure(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe, int index) {
        UUID owner = holder.self().getOwnerUUID();
        long used = owner == null ? -1 : RecipeRunLimitSavaedData.get(owner, recipe.id);
        holder.reportIssue(IdleReason.RUN_LIMIT.type(), IssueStage.CONDITION, IO.NONE, null, index, count, used, recipe);
    }

    @Override
    public boolean isDiagnosable() {
        return false;
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        MetaMachine machine = holder.self();
        UUID owner = machine.getOwnerUUID();
        if (owner == null) return false;
        int runLimit = RecipeRunLimitSavaedData.get(owner, recipe.id);
        if (runLimit < count) {
            RecipeRunLimitSavaedData.set(owner, recipe.id, runLimit + 1);
            return true;
        }
        return false;
    }
}
