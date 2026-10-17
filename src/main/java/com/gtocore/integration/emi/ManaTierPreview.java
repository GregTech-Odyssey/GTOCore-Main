package com.gtocore.integration.emi;

import com.gtolib.api.GTOValues;
import com.gtolib.api.recipe.extension.MANATRecipeExtension;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.extension.RecipeExtension;
import com.gregtechceu.gtceu.api.recipe.ui.EnergyTierPreview;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeTierPreview;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ManaTierPreview implements RecipeTierPreview {

    private static final String MANA_USAGE = "gtocore.recipe.info.mana_usage";
    private static final String MANA_GENERATION = "gtocore.recipe.info.mana_generation";
    private static final String TOTAL_MANA = "gtocore.recipe.info.total_mana";
    private static final String MANA_OVERCLOCK = "gtocore.recipe.info.mana_overclock";
    private static final String MANA_TIER = "gtocore.recipe.info.mana_tier.";

    private final GTRecipeDefinition recipe;
    private final long mana;
    private final int minTier;

    private ManaTierPreview(GTRecipeDefinition recipe, long mana) {
        this.recipe = recipe;
        this.mana = mana;
        int tier = 0;
        while (tier < GTOValues.MANA.length - 1 && GTOValues.MANA[tier] < Math.abs(mana)) tier++;
        this.minTier = tier;
    }

    @Nullable
    public static ManaTierPreview of(GTRecipeDefinition recipe) {
        long mana = MANATRecipeExtension.getMANAt(recipe);
        return mana == 0 ? null : new ManaTierPreview(recipe, mana);
    }

    @Override
    public int minTier() {
        return minTier;
    }

    @Override
    public int maxTier() {
        return mana > 0 ? GTOValues.MANA.length - 1 : minTier;
    }

    @Override
    public Component tierName(int tier) {
        return Component.translatable(MANA_TIER + tier).withStyle(EnergyTierPreview.tierFormats(tier));
    }

    @Override
    public List<Component> tooltip() {
        return List.of(Component.translatable(MANA_OVERCLOCK, tierName(minTier)));
    }

    @Override
    public boolean hasStepper() {
        return mana > 0;
    }

    @Override
    public boolean supportsPerfect() {
        return false;
    }

    private int overclocks(int tier) {
        if (mana < 0) return 0;
        long current = mana;
        int overclocks = 0;
        while (current * 4 <= GTOValues.MANA[tier]) {
            current *= 4;
            overclocks++;
        }
        return overclocks;
    }

    @Override
    public int duration(int tier, boolean perfect) {
        return Math.max(1, (int) (recipe.duration / Math.pow(4, overclocks(tier))));
    }

    @Override
    public List<String> rowLabels() {
        return List.of(mana > 0 ? MANA_USAGE : MANA_GENERATION, TOTAL_MANA);
    }

    @Override
    public Component rowValue(int row, int tier, boolean perfect) {
        long manat = Math.abs(mana) * (long) Math.pow(4, overclocks(tier));
        if (row == 0) return Component.literal(FormattingUtil.formatNumbers(manat) + " /t");
        return Component.literal(FormattingUtil.formatNumbers(manat * duration(tier, perfect)));
    }

    @Override
    public boolean replaces(RecipeExtension<?> extension) {
        return extension == MANATRecipeExtension.INSTANCE;
    }
}
