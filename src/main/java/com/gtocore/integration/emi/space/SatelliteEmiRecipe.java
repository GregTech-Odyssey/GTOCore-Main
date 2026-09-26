package com.gtocore.integration.emi.space;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.integration.emi.GTEMIRecipe;

import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

final class SatelliteEmiRecipe extends GTEMIRecipe {

    private static final int TIER_COLOR = 0xFFFFFFFF;
    private static final int TIER_Z = 200;

    private final ResourceLocation id;
    private final DimensionMarker marker;

    private SatelliteEmiRecipe(ResourceLocation id, RecipeBuilder recipe, DimensionMarker marker) {
        super(recipe.build(), SatelliteEmiCategory.CATEGORY);
        this.id = id;
        this.marker = marker;
    }

    static SatelliteEmiRecipe fromInputOutput(ResourceLocation id, DimensionMarker marker, Consumer<RecipeBuilder> builder) {
        var recipe = GTORecipeTypes.SATELLITE_LAUNCH_RECIPES.recipeBuilder(id);
        recipe.EUt(GTValues.V[GTValues.HV])
                .duration(20 * 300)
                .inputItems(GTOItems.PLANET_DATA_CHIP)
                .inputItems(GTOItems.PLANET_SCAN_SATELLITE);
        builder.accept(recipe);
        recipe.outputItems(marker.getIcon());
        return new SatelliteEmiRecipe(id, recipe, marker);
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean supportsTransfer() {
        return false;
    }

    @Override
    protected SlotWidget createItemSlot(EmiIngredient ingredient, int x, int y) {
        if (ConfigHolder.INSTANCE.compat.showDimensionTier && !ingredient.isEmpty() &&
                ingredient.getEmiStacks().getFirst().getItemStack().is(marker.getIcon().getItem())) {
            return new TierSlotWidget(ingredient, x, y, "T" + (marker.tier >= DimensionMarker.MAX_TIER ? "?" : marker.tier));
        }
        return super.createItemSlot(ingredient, x, y);
    }

    private static final class TierSlotWidget extends SlotWidget {

        private final String tier;

        private TierSlotWidget(EmiIngredient ingredient, int x, int y, String tier) {
            super(ingredient, x, y);
            this.tier = tier;
        }

        @Override
        public void drawOverlay(GuiGraphics draw, int mouseX, int mouseY, float delta) {
            super.drawOverlay(draw, mouseX, mouseY, delta);
            var font = Minecraft.getInstance().font;
            float scale = UISizes.SMALL_TEXT_SCALE;
            var pose = draw.pose();
            pose.pushPose();
            pose.translate(x + UISizes.SLOT - 1 - font.width(tier) * scale, y + UISizes.SLOT - 1 - UISizes.SMALL_TEXT_HEIGHT, TIER_Z);
            pose.scale(scale, scale, 1);
            draw.drawString(font, tier, 0, 0, TIER_COLOR, true);
            pose.popPose();
        }
    }
}
