package com.gtocore.integration.emi.rune;

import com.gtocore.common.data.machines.MythicBotanyMachines;

import com.gtolib.utils.RLUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.recipe.RuneRitualRecipe;
import vazkii.botania.common.rune.RuneRitualRecipes;

import java.util.ArrayList;
import java.util.List;

/**
 * 符文仪式的 EMI 分类：按仪式坐标摆出符文阵型，并列出投入材料与产出。
 */
public final class RuneRitualEmiCategory extends EmiRecipeCategory {

    public static final RuneRitualEmiCategory CATEGORY = new RuneRitualEmiCategory();

    private static final int CELL = 18;
    private static final int GRID = 11;
    private static final int ORIGIN = 4;

    private RuneRitualEmiCategory() {
        super(RLUtils.fromNamespaceAndPath("mythicbotany", "rune_ritual"), EmiStack.of(MythicBotanyMachines.CENTRAL_RUNE_HOLDER.asStack()));
    }

    @Override
    public Component getName() {
        return Component.translatable("emi.category.mythicbotany.rune_ritual");
    }

    public static void register(EmiRegistry registry) {
        // 分类先注册，配方要等客户端拿到配方表
        registry.addCategory(CATEGORY);
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        for (RuneRitualRecipe recipe : level.getRecipeManager().getAllRecipesFor(RuneRitualRecipes.RUNE_RITUAL_TYPE.get())) {
            registry.addRecipe(new RuneRitualEmiRecipe(recipe));
        }
    }

    private static final class RuneRitualEmiRecipe implements EmiRecipe {

        private final RuneRitualRecipe recipe;
        private final List<EmiIngredient> inputs = new ArrayList<>();
        private final List<EmiStack> outputs = new ArrayList<>();

        private RuneRitualEmiRecipe(RuneRitualRecipe recipe) {
            this.recipe = recipe;
            this.inputs.add(EmiIngredient.of(recipe.getCenterRune()));
            for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
                this.inputs.add(EmiIngredient.of(rune.getRune()));
            }
            for (Ingredient input : recipe.getInputs()) {
                this.inputs.add(EmiIngredient.of(input));
            }
            for (ItemStack output : recipe.getOutputs()) {
                this.outputs.add(EmiStack.of(output));
            }
            if (recipe.getSpecialOutput() != null) {
                for (ItemStack stack : recipe.getSpecialOutput().getJeiOutputItems()) {
                    this.outputs.add(EmiStack.of(stack));
                }
            }
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public @Nullable ResourceLocation getId() {
            return recipe.getId();
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return inputs;
        }

        @Override
        public List<EmiStack> getOutputs() {
            return outputs;
        }

        @Override
        public int getDisplayWidth() {
            return 320;
        }

        @Override
        public int getDisplayHeight() {
            return GRID * CELL + ORIGIN * 2;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            int center = GRID / 2;
            widgets.addSlot(EmiIngredient.of(recipe.getCenterRune()), ORIGIN + center * CELL, ORIGIN + center * CELL);
            for (RuneRitualRecipe.RunePosition rune : recipe.getRunes()) {
                widgets.addSlot(EmiIngredient.of(rune.getRune()), ORIGIN + (center + rune.getX()) * CELL,
                        ORIGIN + (center + rune.getZ()) * CELL);
            }

            int right = ORIGIN + GRID * CELL + 8;
            int y = ORIGIN;
            for (Ingredient input : recipe.getInputs()) {
                widgets.addSlot(EmiIngredient.of(input), right, y);
                y += CELL;
            }
            if (recipe.getSpecialInput() != null) {
                for (Ingredient input : recipe.getSpecialInput().getJeiInputItems()) {
                    widgets.addSlot(EmiIngredient.of(input), right, y);
                    y += CELL;
                }
            }
            y += 6;
            List<EmiStack> resultStacks = new ArrayList<>(outputs);
            for (EmiStack stack : resultStacks) {
                widgets.addSlot(stack, right, y).recipeContext(this);
                y += CELL;
            }

            widgets.addText(Component.translatable("emi.mythicbotany.rune_ritual.ticks", recipe.getTicks()), right, y + 4, 0x404040, false);
            widgets.addText(Component.translatable("emi.mythicbotany.rune_ritual.mana", recipe.getMana(), recipe.getManaPerTick()),
                    right, y + 16, 0x404040, false);
        }
    }
}
