package com.gtocore.integration.emi.apotheosis;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.shadowsoffire.apotheosis.ench.Ench;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingRecipe;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 改写自 Apotheosis（Shadows_of_Fire，MIT）的 JEI 集成
// https://github.com/Shadows-of-Fire/Apotheosis
final class EnchantingEmiRecipe extends BasicEmiRecipe {

    private static final ResourceLocation TEX = new ResourceLocation(ApotheosisEmi.MODID, "textures/gui/enchanting_jei.png");

    final float eterna;
    private final Stats stats;
    private final Stats maxStats;
    private final List<EmiStack> charmIn;
    private final List<EmiStack> charmOut;

    EnchantingEmiRecipe(EmiRecipeCategory category, EnchantingRecipe recipe, List<EmiStack> charmIn, List<EmiStack> charmOut) {
        super(category, recipe.getId(), 170, 56);
        this.stats = recipe.getRequirements();
        this.maxStats = recipe.getMaxRequirements();
        this.eterna = stats.eterna();
        if (charmIn != null && !charmIn.isEmpty()) {
            this.charmIn = charmIn;
            this.charmOut = charmOut;
            this.inputs = Collections.singletonList(EmiIngredient.of(charmIn));
            this.outputs = charmOut;
        } else {
            this.charmIn = null;
            this.charmOut = null;
            this.inputs = Collections.singletonList(EmiIngredient.of(recipe.getInput()));
            this.outputs = Collections.singletonList(EmiStack.of(recipe.getOutput()));
        }
    }

    @Override
    public boolean supportsRecipeTree() {
        return charmIn == null;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEX, 0, 0, 170, 56, 0, 0);
        if (charmIn != null) {
            int n = charmIn.size();
            int unique = id.hashCode();
            widgets.addGeneratedSlot(r -> charmIn.get(r.nextInt(n)), unique, 5, 5).drawBack(false);
            widgets.addGeneratedSlot(r -> charmOut.get(r.nextInt(n)), unique, 36, 5).drawBack(false).recipeContext(this);
        } else {
            widgets.addSlot(inputs.get(0), 5, 5).drawBack(false);
            widgets.addSlot(outputs.get(0), 36, 5).drawBack(false).recipeContext(this);
        }
        widgets.addText(Component.translatable("gui.apotheosis.enchant.eterna"), 16, 26, 0x3DB53D, false);
        widgets.addText(Component.translatable("gui.apotheosis.enchant.quanta"), 16, 36, 0xFC5454, false);
        widgets.addText(Component.translatable("gui.apotheosis.enchant.arcana"), 16, 46, 0xA800A8, false);

        float absMax = EnchantingStatRegistry.getAbsoluteMaxEterna();
        float scale = absMax > 0 ? absMax : 1;
        widgets.addDrawable(0, 0, 170, 56, (g, mx, my, d) -> draw(g, mx, my, scale));

        widgets.addTooltipText(Collections.singletonList(Component.translatable("container.enchant.clue",
                Ench.Enchantments.INFUSION.get().getFullname(1).getString()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)), 57, 4, 108, 19);

        List<Component> eternaTip = new ArrayList<>(3);
        eternaTip.add(Component.translatable("gui.apotheosis.enchant.eterna").withStyle(ChatFormatting.GREEN));
        if (maxStats.eterna() == stats.eterna()) {
            eternaTip.add(Component.translatable("info.apotheosis.eterna_exact", stats.eterna(), absMax).withStyle(ChatFormatting.GRAY));
        } else {
            eternaTip.add(Component.translatable("info.apotheosis.eterna_at_least", stats.eterna(), absMax).withStyle(ChatFormatting.GRAY));
            if (maxStats.eterna() > -1) eternaTip.add(Component.translatable("info.apotheosis.eterna_at_most", maxStats.eterna(), absMax).withStyle(ChatFormatting.GRAY));
        }
        widgets.addTooltipText(eternaTip, 56, 27, 110, 5);
        widgets.addTooltipText(percentTip("gui.apotheosis.enchant.quanta", ChatFormatting.RED, stats.quanta(), maxStats.quanta()), 56, 37, 110, 5);
        widgets.addTooltipText(percentTip("gui.apotheosis.enchant.arcana", ChatFormatting.DARK_PURPLE, stats.arcana(), maxStats.arcana()), 56, 47, 110, 5);
    }

    private static List<Component> percentTip(String key, ChatFormatting color, float min, float max) {
        List<Component> list = new ArrayList<>(3);
        list.add(Component.translatable(key).withStyle(color));
        if (max == min) {
            list.add(Component.translatable("info.apotheosis.percent_exact", min).withStyle(ChatFormatting.GRAY));
        } else {
            list.add(Component.translatable("info.apotheosis.percent_at_least", min).withStyle(ChatFormatting.GRAY));
            if (max > -1) list.add(Component.translatable("info.apotheosis.percent_at_most", max).withStyle(ChatFormatting.GRAY));
        }
        return list;
    }

    private void draw(GuiGraphics g, int mouseX, int mouseY, float absMax) {
        boolean hover = mouseX > 57 && mouseX <= 57 + 108 && mouseY > 4 && mouseY <= 4 + 19;
        if (hover) g.blit(TEX, 57, 4, 0, 0, 71, 108, 19, 256, 256);

        Font font = Minecraft.getInstance().font;
        String s = String.valueOf((int) (stats.eterna() * 2));
        int width = 86 - font.width(s);
        EnchantmentNames.getInstance().initSeed(id.hashCode());
        FormattedText name = EnchantmentNames.getInstance().getRandomName(font, width);
        int color = hover ? 16777088 : 6839882;
        int y = 6;
        for (FormattedCharSequence line : font.split(name, width)) {
            g.drawString(font, line, 77, y, color, false);
            y += 9;
        }
        g.drawString(font, s, 77 + width, 13, 8453920);

        int p0 = (int) (stats.eterna() / absMax * 110);
        int p1 = (int) (stats.quanta() / 100 * 110);
        int p2 = (int) (stats.arcana() / 100 * 110);
        if (stats.eterna() > 0) g.blit(TEX, 56, 27, 0, 56, p0, 5, 256, 256);
        if (stats.quanta() > 0) g.blit(TEX, 56, 37, 0, 61, p1, 5, 256, 256);
        if (stats.arcana() > 0) g.blit(TEX, 56, 47, 0, 66, p2, 5, 256, 256);
        RenderSystem.enableBlend();
        if (maxStats.eterna() > 0) g.blit(TEX, 56 + p0, 27, p0, 90, (int) ((maxStats.eterna() - stats.eterna()) / absMax * 110), 5, 256, 256);
        if (maxStats.quanta() > 0) g.blit(TEX, 56 + p1, 37, p1, 95, (int) ((maxStats.quanta() - stats.quanta()) / 100 * 110), 5, 256, 256);
        if (maxStats.arcana() > 0) g.blit(TEX, 56 + p2, 47, p2, 100, (int) ((maxStats.arcana() - stats.arcana()) / 100 * 110), 5, 256, 256);
        RenderSystem.disableBlend();
    }
}
