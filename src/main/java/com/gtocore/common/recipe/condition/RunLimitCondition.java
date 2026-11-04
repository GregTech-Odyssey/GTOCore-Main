package com.gtocore.common.recipe.condition;

import com.gtocore.common.saved.RecipeRunLimitSavaedData;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;

import net.minecraft.network.chat.Component;

import java.util.UUID;

public final class RunLimitCondition extends RecipeCondition {

    private static final Component LABEL = Component.translatable("gtocore.recipe.info.run_limit");

    private final int count;

    public RunLimitCondition(int count) {
        this.count = count;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtocore.recipe.runlimit.count", count);
    }

    @Override
    public void appendInfo(GTRecipeDefinition recipe, RecipeInfoBuilder info) {
        info.requirement(LABEL, () -> Component.literal(String.valueOf(count)));
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
