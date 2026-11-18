package com.gtocore.integration.ftbquests.emi;

import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.integration.RecipeModHelper;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;

public final class EmiQuestRecipeHelper implements RecipeModHelper {

    public static void install() {
        if (FTBQuests.getRecipeModHelper() instanceof RecipeModHelper.NoOp) {
            FTBQuests.setRecipeModHelper(new EmiQuestRecipeHelper());
        }
    }

    @Override
    public void refreshAll(Components component) {}

    @Override
    public void refreshRecipes(QuestObjectBase object) {}

    @Override
    public void showRecipes(ItemStack stack) {
        if (!stack.isEmpty()) EmiApi.displayRecipes(EmiStack.of(stack));
    }

    @Override
    public boolean isRecipeModAvailable() {
        return true;
    }

    @Override
    public String getHelperName() {
        return "EMI";
    }
}
