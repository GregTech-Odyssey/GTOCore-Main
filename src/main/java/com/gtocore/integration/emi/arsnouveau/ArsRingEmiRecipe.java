package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public abstract class ArsRingEmiRecipe extends BasicEmiRecipe {

    static final int TEXT_COLOR = 0x00000A;

    private final EmiIngredient center;
    private final EmiIngredient[] pedestals;

    protected ArsRingEmiRecipe(EmiRecipeCategory category, ResourceLocation id, EmiIngredient center, boolean centerIsInput, List<Ingredient> pedestalItems, EmiStack output) {
        super(category, id, 114, 108);
        this.center = center;
        int n = pedestalItems.size();
        pedestals = new EmiIngredient[n];
        inputs = new ObjectArrayList<>(n + 1);
        if (center != null && centerIsInput) inputs.add(center);
        for (int i = 0; i < n; i++) {
            pedestals[i] = EmiIngredient.of(pedestalItems.get(i));
            inputs.add(pedestals[i]);
        }
        outputs.add(output);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (center != null) widgets.addSlot(center, 47, 44);
        int n = pedestals.length;
        for (int i = 0; i < n; i++) {
            double t = Math.toRadians(i * 360.0 / n);
            int x = (int) (float) (48 + 32 * Math.sin(t)) - 1;
            int y = (int) (float) (45 - 32 * Math.cos(t)) - 1;
            widgets.addSlot(pedestals[i], x, y);
        }
        widgets.addSlot(outputs.get(0), 90, 9).recipeContext(this);
        addText(widgets);
    }

    protected abstract void addText(WidgetHolder widgets);

    static void addLine(WidgetHolder widgets, Component text, int y) {
        widgets.addText(text, 0, y, TEXT_COLOR, false);
    }
}
