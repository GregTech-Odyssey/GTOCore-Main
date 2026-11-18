package com.gtocore.integration.emi.apotheosis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingRecipe.OutputData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 改写自 Apotheosis（Shadows_of_Fire，MIT）的 JEI 集成
// https://github.com/Shadows-of-Fire/Apotheosis
final class SalvagingEmiRecipe extends BasicEmiRecipe {

    private static final ResourceLocation TEX = new ResourceLocation(ApotheosisEmi.MODID, "textures/gui/salvage_jei.png");

    final int maxOut;
    private final String[] ranges;

    SalvagingEmiRecipe(EmiRecipeCategory category, SalvagingRecipe recipe) {
        super(category, recipe.getId(), 98, 74);
        this.inputs = Collections.singletonList(input(recipe.getInput()));
        List<OutputData> data = recipe.getOutputs();
        List<EmiStack> outs = new ArrayList<>(data.size());
        this.ranges = new String[data.size()];
        int max = 0;
        for (int i = 0; i < data.size(); i++) {
            OutputData d = data.get(i);
            outs.add(EmiStack.of(d.getStack()));
            ranges[i] = d.getMin() + "-" + d.getMax();
            max = Math.max(max, d.getMax());
        }
        this.outputs = outs;
        this.maxOut = max;
    }

    private static EmiIngredient input(Ingredient ingredient) {
        EmiIngredient converted = EmiIngredient.of(ingredient);
        if (!converted.isEmpty()) return converted;
        ItemStack[] items = ingredient.getItems();
        List<EmiStack> stacks = new ArrayList<>(items.length);
        for (ItemStack item : items) stacks.add(EmiStack.of(item));
        return EmiIngredient.of(stacks);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEX, 0, 0, 98, 74, 0, 0);
        widgets.addSlot(inputs.get(0), 4, 28).drawBack(false);
        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), 58 + 18 * (i % 2), 10 + 18 * (i / 2)).drawBack(false)
                    .appendTooltip(Component.literal(ranges[i])).recipeContext(this);
        }
        widgets.addDrawable(0, 0, 98, 74, (g, mx, my, d) -> {
            Font font = Minecraft.getInstance().font;
            PoseStack pose = g.pose();
            for (int i = 0; i < ranges.length; i++) {
                pose.pushPose();
                pose.translate(0, 0, 200);
                float x = 59 + 18 * (i % 2) + (16 - font.width(ranges[i]) * 0.5F);
                float y = 23F + 18 * (i / 2);
                pose.scale(0.5F, 0.5F, 1);
                g.drawString(font, ranges[i], (int) (x / 0.5F), (int) (y / 0.5F), 0xFFFFFF);
                pose.popPose();
            }
        });
    }
}
