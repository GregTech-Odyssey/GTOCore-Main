package com.gtocore.mixin.ae2.eae;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import appeng.api.stacks.AEItemKey;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.CraftingRecipeUtil;

import com.glodblock.github.extendedae.xmod.jei.transfer.ExCraftingHelper;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Comparator;

@Mixin(ExCraftingHelper.class)
public class ExCraftingHelperMixin {

    @Shadow(remap = false)
    @Final
    private static Comparator<GridInventoryEntry> ENTRY_COMPARATOR;

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    private static NonNullList<ItemStack> findGoodTemplateItems(Recipe<?> recipe, int recipeSize, MEStorageMenu menu) {
        var ingredientPriorities = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
        var templateItems = NonNullList.withSize(recipeSize, ItemStack.EMPTY);
        var ingredients = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);

        for (int i = 0; i < Math.min(ingredients.size(), recipeSize); ++i) {
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                var stack = ingredientPriorities.reference2IntEntrySet().stream().filter(e -> {
                    if (e.getKey() instanceof AEItemKey itemKey) {
                        return itemKey.matches(ingredient);
                    }
                    return false;
                }).max(Comparator.comparingInt(Reference2IntMap.Entry::getIntValue)).map((e) -> ((AEItemKey) e.getKey()).toStack()).orElse(ingredient.getItems()[0]);
                templateItems.set(i, stack);
            }
        }
        return templateItems;
    }
}
