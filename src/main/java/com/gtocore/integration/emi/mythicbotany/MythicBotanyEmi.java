package com.gtocore.integration.emi.mythicbotany;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mythicbotany.infuser.InfuserRecipe;
import mythicbotany.register.ModBlocks;
import mythicbotany.register.ModItems;
import mythicbotany.register.ModRecipes;
import mythicbotany.rune.RuneRitualRecipe;
import vazkii.botania.client.integration.emi.BlendTextureWidget;
import vazkii.botania.client.integration.emi.ManaWidget;
import vazkii.botania.common.block.BotaniaBlocks;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

// 改写自 MythicBotany（noeppi_noeppi，Apache-2.0）的 JEI 集成
// https://github.com/noeppi-noeppi/MythicBotany
public final class MythicBotanyEmi implements EmiPlugin {

    private static final ResourceLocation OVERLAY = new ResourceLocation("botania", "textures/gui/terrasteel_jei_overlay.png");
    private static final ResourceLocation RITUAL_BACKGROUND = new ResourceLocation("mythicbotany", "textures/gui/jei_ritual.png");

    public static final EmiRecipeCategory INFUSER = new EmiRecipeCategory(new ResourceLocation("mythicbotany", "infuser"), EmiStack.of(ModBlocks.manaInfuser)) {

        @Override
        public Component getName() {
            return Component.translatable("block.mythicbotany.mana_infuser");
        }
    };

    public static final EmiRecipeCategory RUNE_RITUAL = new EmiRecipeCategory(new ResourceLocation("mythicbotany", "rune_ritual"), EmiStack.of(ModItems.fimbultyrTablet)) {

        @Override
        public Component getName() {
            return Component.translatable("tooltip.mythicbotany.rune_ritual");
        }
    };

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(INFUSER);
        registry.addCategory(RUNE_RITUAL);
        registry.addWorkstation(INFUSER, EmiStack.of(ModBlocks.manaInfuser));
        registry.addWorkstation(RUNE_RITUAL, EmiStack.of(ModBlocks.centralRuneHolder));
        registry.addWorkstation(RUNE_RITUAL, EmiStack.of(ModBlocks.runeHolder));
        var manager = registry.getRecipeManager();
        for (InfuserRecipe recipe : manager.getAllRecipesFor(ModRecipes.infuser)) {
            registry.addRecipe(new InfuserEmiRecipe(recipe));
        }
        for (RuneRitualRecipe recipe : manager.getAllRecipesFor(ModRecipes.runeRitual)) {
            registry.addRecipe(new RuneRitualEmiRecipe(recipe));
        }
    }

    private static final class InfuserEmiRecipe extends BasicEmiRecipe {

        private final int mana;
        private final EmiStack infuser = EmiStack.of(ModBlocks.manaInfuser);

        private InfuserEmiRecipe(InfuserRecipe recipe) {
            super(INFUSER, recipe.getId(), 114, 141);
            this.mana = recipe.getManaUsage();
            var ingredients = recipe.getIngredients();
            inputs = new ObjectArrayList<>(ingredients.size());
            for (Ingredient ingredient : ingredients) {
                inputs.add(EmiIngredient.of(ingredient));
            }
            outputs.add(EmiStack.of(recipe.getResultItem()));
            catalysts.add(infuser);
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.add(new BlendTextureWidget(OVERLAY, 25, 14, 64, 64, 42, 29));
            widgets.add(new ManaWidget(6, 126, mana, 4_000_000));
            ItemStack shimmerrock = new ItemStack(BotaniaBlocks.shimmerrock);
            ItemStack gold = new ItemStack(Blocks.GOLD_BLOCK);
            widgets.addDrawable(35, 92, 43, 31, (graphics, mouseX, mouseY, delta) -> drawPlate(graphics, shimmerrock, shimmerrock, gold));
            int n = inputs.size();
            double angle = Math.toRadians(360.0 / n);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double px = 49;
            double py = 6;
            for (int i = 0; i < n; i++) {
                widgets.addSlot(inputs.get(i), (int) Math.round(px) - 1, (int) Math.round(py) - 1).drawBack(false);
                double dx = px - 49;
                double dy = py - 38;
                px = cos * dx - sin * dy + 49;
                py = sin * dx + cos * dy + 38;
            }
            widgets.addSlot(outputs.get(0), 47, 36).drawBack(false).recipeContext(this);
            widgets.addSlot(infuser, 48, 92).drawBack(false).catalyst(true);
            MutableComponent amount = Component.literal(BigDecimal.valueOf(mana / 1_000_000d).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()).withStyle(ChatFormatting.BLUE);
            widgets.addText(Component.translatable("tooltip.mythicbotany.cost_pools", amount), 57, 133, 0x000000, false).horizontalAlign(TextWidget.Alignment.CENTER);
        }

        private static void drawPlate(GuiGraphics graphics, ItemStack corner, ItemStack center, ItemStack middle) {
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(0, 0, -50);
            graphics.renderItem(corner, 13, 1);
            pose.translate(0, 0, 5);
            graphics.renderItem(middle, 20, 4);
            graphics.renderItem(middle, 7, 4);
            pose.translate(0, 0, 5);
            graphics.renderItem(corner, 13, 8);
            graphics.renderItem(center, 27, 8);
            graphics.renderItem(corner, 0, 8);
            pose.translate(0, 0, 5);
            graphics.renderItem(middle, 7, 12);
            graphics.renderItem(middle, 20, 12);
            pose.translate(0, 0, 5);
            graphics.renderItem(corner, 14, 15);
            pose.popPose();
        }
    }

    private static final class RuneRitualEmiRecipe extends BasicEmiRecipe {

        private final List<RuneSlot> runes;
        private final List<EmiIngredient> rowInputs;
        private final int mana;

        private RuneRitualEmiRecipe(RuneRitualRecipe recipe) {
            super(RUNE_RITUAL, recipe.getId(), 136, 196);
            this.mana = recipe.getMana();
            var positions = recipe.getRunes();
            runes = new ObjectArrayList<>(positions.size() + 1);
            runes.add(new RuneSlot(EmiIngredient.of(recipe.getCenterRune()), 0, 0, true));
            for (RuneRitualRecipe.RunePosition position : positions) {
                runes.add(new RuneSlot(EmiIngredient.of(position.getRune()), position.getX(), position.getZ(), position.isConsumed()));
            }
            var special = recipe.getSpecialInput();
            List<Ingredient> extraIn = special == null ? Collections.emptyList() : special.getJeiInputItems();
            rowInputs = new ObjectArrayList<>(recipe.getInputs().size() + extraIn.size());
            for (Ingredient ingredient : recipe.getInputs()) {
                rowInputs.add(EmiIngredient.of(ingredient));
            }
            for (Ingredient ingredient : extraIn) {
                rowInputs.add(EmiIngredient.of(ingredient));
            }
            for (RuneSlot rune : runes) {
                if (rune.consume) {
                    inputs.add(rune.stack);
                } else {
                    catalysts.add(rune.stack);
                }
            }
            inputs.addAll(rowInputs);
            for (ItemStack stack : recipe.getOutputs()) {
                outputs.add(EmiStack.of(stack));
            }
            var specialOut = recipe.getSpecialOutput();
            if (specialOut != null) {
                for (ItemStack stack : specialOut.getJeiOutputItems()) {
                    outputs.add(EmiStack.of(stack));
                }
            }
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addTexture(RITUAL_BACKGROUND, 0, 0, 136, 196, 0, 0);
            for (RuneSlot rune : runes) {
                SlotWidget slot = new SlotWidget(rune.stack, 2 + 12 * (rune.x + 5), 2 + 12 * (-rune.z + 5))
                        .customBackground(null, 0, 0, 12, 12)
                        .drawBack(false)
                        .catalyst(!rune.consume);
                if (rune.x == 0 && rune.z == 0) {
                    slot.appendTooltip(Component.translatable("tooltip.mythicbotany.rune_central").withStyle(ChatFormatting.GOLD));
                } else {
                    slot.appendTooltip(Component.translatable("tooltip.mythicbotany.rune_offset", rune.x, rune.z).withStyle(ChatFormatting.GOLD));
                }
                if (rune.consume) {
                    slot.appendTooltip(Component.translatable("tooltip.mythicbotany.rune_consume").withStyle(ChatFormatting.DARK_RED));
                } else {
                    slot.appendTooltip(Component.translatable("tooltip.mythicbotany.rune_keep").withStyle(ChatFormatting.DARK_GREEN));
                }
                widgets.add(slot);
            }
            int startIn = 69 - rowInputs.size() * 9;
            for (int i = 0; i < rowInputs.size(); i++) {
                widgets.addSlot(rowInputs.get(i), startIn + i * 18 - 1, 137);
            }
            int startOut = 69 - outputs.size() * 9;
            for (int i = 0; i < outputs.size(); i++) {
                widgets.addSlot(outputs.get(i), startOut + i * 18 - 1, 169).recipeContext(this);
            }
            if (mana > 0) {
                widgets.add(new ManaWidget(17, 189, mana, 1_000_000));
            }
        }

        private record RuneSlot(EmiIngredient stack, int x, int z, boolean consume) {

            private RuneSlot {
                consume = (x == 0 && z == 0) || consume;
            }
        }
    }
}
