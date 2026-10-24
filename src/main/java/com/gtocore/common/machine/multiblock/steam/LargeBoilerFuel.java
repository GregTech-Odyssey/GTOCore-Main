package com.gtocore.common.machine.multiblock.steam;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeTierPreview;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.LargeBoilerSpec;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 大型锅炉的燃料：锅炉等级（{@link LargeBoilerSpec}）每高一级，同一份燃料产出的蒸汽总量多 25%（燃烧时间相应延长）；
 * 配方页可切换锅炉等级，查看各级锅炉的燃烧时间与蒸汽量。
 */
@DataGeneratorScanned
public final class LargeBoilerFuel {

    public static final double STEAM_BONUS_PER_TIER = 0.25;
    private static final int BASE_TEMPERATURE = 1600;
    private static final int MAX_BATCH = 64;
    private static final double BATCH_TOLERANCE = 0.01;

    @RegisterLanguage(cn = "锅炉等级", en = "Boiler tier")
    private static final String TIER = "gtocore.recipe.info.large_boiler.tier";
    @RegisterLanguage(cn = "蒸汽产量", en = "Steam output")
    private static final String STEAM_RATE = "gtocore.recipe.info.large_boiler.steam_rate";
    @RegisterLanguage(cn = "蒸汽总量", en = "Total steam")
    private static final String TOTAL_STEAM = "gtocore.recipe.info.large_boiler.total_steam";
    @RegisterLanguage(cn = "蒸汽总量倍率", en = "Total steam multiplier")
    private static final String MULTIPLIER = "gtocore.recipe.info.large_boiler.multiplier";
    @RegisterLanguage(cn = "锅炉每比%s高一级，同一份燃料产出的蒸汽总量多 25%%", en = "Each boiler tier above the %s makes 25%% more total steam from the same fuel")
    private static final String BONUS_HINT = "gtocore.recipe.info.large_boiler.bonus_hint";
    @RegisterLanguage(cn = "同一份燃料产出的蒸汽总量：%s（%s为 ×1）", en = "Total steam from the same fuel: %s (%s: ×1)")
    private static final String TOOLTIP = "gtocore.machine.large_boiler.fuel_multiplier";

    private LargeBoilerFuel() {}

    public static void init() {
        for (var spec : LargeBoilerSpec.all()) {
            var multiplier = multiplierText(spec.tier());
            spec.definition().setTooltipBuilder((stack, tooltips) -> tooltips.add(Tips.stat(Component.translatable(TOOLTIP, multiplier, baseName()))));
        }
        RecipeTierPreview.register(LargeBoilerFuel::preview);
    }

    public static double steamMultiplier(int tier) {
        return 1 + STEAM_BONUS_PER_TIER * tier;
    }

    public static double durationScale(int tier, int maxTemperature) {
        return BASE_TEMPERATURE * steamMultiplier(tier) / maxTemperature;
    }

    public static double durationScale(LargeBoilerMachine machine) {
        var spec = LargeBoilerSpec.of(machine.getDefinition());
        return durationScale(spec == null ? 0 : spec.tier(), machine.maxTemperature);
    }

    public static long batch(double duration) {
        for (int batch = 1; batch <= MAX_BATCH; batch++) {
            double total = duration * batch;
            if (total >= 1 && Math.abs(Math.round(total) - total) <= total * BATCH_TOLERANCE) return batch;
        }
        return Math.max(1, (long) Math.ceil(1 / duration));
    }

    private static Component baseName() {
        return Component.translatable(LargeBoilerSpec.all().getFirst().definition().getDescriptionId());
    }

    private static Component multiplierText(int tier) {
        return Component.literal("×" + FormattingUtil.formatNumbers(steamMultiplier(tier))).withStyle(ChatFormatting.AQUA);
    }

    @Nullable
    private static RecipeTierPreview preview(GTRecipeDefinition recipe) {
        return recipe.recipeType == GTRecipeTypes.LARGE_BOILER_RECIPES && !LargeBoilerSpec.all().isEmpty() ? new Preview(recipe) : null;
    }

    private record Preview(GTRecipeDefinition recipe) implements RecipeTierPreview {

        @Override
        public int minTier() {
            return 0;
        }

        @Override
        public int maxTier() {
            return LargeBoilerSpec.all().size() - 1;
        }

        @Override
        public Component tierName(int tier) {
            return Component.translatable(LargeBoilerSpec.all().get(tier).definition().getDescriptionId());
        }

        @Override
        public List<Component> tooltip() {
            return List.of(Component.translatable(BONUS_HINT, baseName()));
        }

        @Override
        public boolean supportsPerfect() {
            return false;
        }

        private double exactDuration(int tier) {
            return recipe.duration * durationScale(tier, LargeBoilerSpec.all().get(tier).maxTemperature());
        }

        @Override
        public int duration(int tier, boolean perfect) {
            return Math.max(1, (int) Math.round(exactDuration(tier)));
        }

        @Override
        public List<String> rowLabels() {
            return List.of(STEAM_RATE, TOTAL_STEAM, MULTIPLIER);
        }

        @Override
        public Component rowValue(int row, int tier, boolean perfect) {
            long rate = LargeBoilerSpec.all().get(tier).maxTemperature();
            return switch (row) {
                case 0 -> Component.literal(FormattingUtil.formatNumbers(rate) + " mB/t");
                case 1 -> Component.literal(FormattingUtil.formatNumbers(Math.round(rate * exactDuration(tier))) + " mB");
                default -> multiplierText(tier);
            };
        }

        @Override
        public boolean hasStepper() {
            return true;
        }

        @Override
        public Component stepperLabel() {
            return Component.translatable(TIER);
        }
    }
}
