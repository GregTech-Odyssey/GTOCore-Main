package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.hollingsworth.arsnouveau.common.crafting.recipes.DyeRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.IDyeable;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.EnumSet;
import java.util.List;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class ArsDyeEmiRecipe extends EmiPatternCraftingRecipe {

    private final int dyeSlot;
    private final EmiStack[] dyes;
    private final EmiStack[] dyed;

    private ArsDyeEmiRecipe(DyeRecipe recipe, List<EmiIngredient> input, ItemStack result, int dyeSlot, EnumSet<DyeColor> colors) {
        super(input, EmiStack.of(result), recipe.getId(), true);
        this.dyeSlot = dyeSlot;
        IDyeable dyeable = (IDyeable) result.getItem();
        dyes = new EmiStack[colors.size()];
        dyed = new EmiStack[colors.size()];
        int i = 0;
        for (DyeColor color : colors) {
            ItemStack copy = result.copy();
            dyeable.onDye(copy, color);
            dyes[i] = EmiStack.of(DyeItem.byColor(color));
            dyed[i] = EmiStack.of(copy);
            i++;
        }
    }

    public static ArsDyeEmiRecipe of(DyeRecipe recipe, ItemStack result) {
        if (!(result.getItem() instanceof IDyeable)) return null;
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int dyeSlot = -1;
        EnumSet<DyeColor> colors = EnumSet.noneOf(DyeColor.class);
        ObjectArrayList<EmiIngredient> input = new ObjectArrayList<>(ingredients.size());
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            input.add(EmiIngredient.of(ingredient));
            if (dyeSlot >= 0) continue;
            for (ItemStack stack : ingredient.getItems()) {
                DyeColor color = DyeColor.getColor(stack);
                if (color != null) colors.add(color);
            }
            if (!colors.isEmpty()) dyeSlot = i;
        }
        if (dyeSlot < 0 || input.size() > 9) return null;
        return new ArsDyeEmiRecipe(recipe, input, result, dyeSlot, colors);
    }

    @Override
    public SlotWidget getInputWidget(int slot, int x, int y) {
        if (slot == dyeSlot) return new GeneratedSlotWidget(r -> dyes[r.nextInt(dyes.length)], unique, x, y);
        if (slot < input.size()) return new SlotWidget(input.get(slot), x, y);
        return new SlotWidget(EmiStack.EMPTY, x, y);
    }

    @Override
    public SlotWidget getOutputWidget(int x, int y) {
        return new GeneratedSlotWidget(r -> dyed[r.nextInt(dyed.length)], unique, x, y);
    }
}
