package com.gtocore.integration.jade;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import snownee.jade.JadeClient;
import snownee.jade.api.Accessor;
import snownee.jade.impl.ObjectDataCenter;

public final class JadeRecipeKeys {

    private JadeRecipeKeys() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(JadeRecipeKeys::onKey);
    }

    private static void onKey(InputEvent.Key event) {
        KeyMapping recipes = JadeClient.showRecipes;
        KeyMapping uses = JadeClient.showUses;
        if (recipes == null || uses == null) return;
        boolean showRecipes = recipes.consumeClick();
        boolean showUses = uses.consumeClick();
        if (!showRecipes && !showUses) return;
        Accessor<?> accessor = ObjectDataCenter.get();
        if (accessor == null) return;
        ItemStack stack = accessor.getPickedResult();
        if (stack.isEmpty()) return;
        if (showUses) {
            EmiApi.displayUses(EmiStack.of(stack));
        } else {
            EmiApi.displayRecipes(EmiStack.of(stack));
        }
    }
}
