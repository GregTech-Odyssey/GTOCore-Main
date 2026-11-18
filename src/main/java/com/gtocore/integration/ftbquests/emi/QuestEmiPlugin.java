package com.gtocore.integration.ftbquests.emi;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import dev.ftb.mods.ftbxmodcompat.ftbquests.recipemod_common.WrappedQuest;
import dev.ftb.mods.ftbxmodcompat.ftbquests.recipemod_common.WrappedQuestCache;

public final class QuestEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory QUEST = new EmiRecipeCategory(new ResourceLocation("ftbquests", "quest"), EmiStack.of(FTBQuestsItems.BOOK.get())) {

        @Override
        public Component getName() {
            return Component.translatable("ftbquests.quests");
        }
    };

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(QUEST);
        registry.addWorkstation(QUEST, EmiStack.of(FTBQuestsItems.BOOK.get()));
        registry.addDeferredRecipes(consumer -> {
            for (WrappedQuest quest : new WrappedQuestCache().getCachedItems()) {
                consumer.accept(new QuestEmiRecipe(quest));
            }
        });
    }
}
