package com.gtocore.common.recipe.condition;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.data.Galaxy;

import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.IRecipeHandlerHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ui.RecipeInfoBuilder;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GalaxyCondition extends DimensionCondition {

    private final Galaxy galaxy;
    private DimensionMarker[] dimensions;

    public GalaxyCondition(Galaxy galaxy) {
        super(false, null);
        this.galaxy = galaxy;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtocore.condition.within_galaxy", Component.translatable("gtolib.galaxy.name." + galaxy.name()));
    }

    /** 配方页：一句说明，并以展示槽轮流显示该星系各维度的标志物品。 */
    @Override
    public void appendInfo(GTRecipeDefinition recipe, RecipeInfoBuilder info) {
        info.sentence(this::getTooltips);
        info.slot(this::createDimensionSlot);
    }

    @Override
    public Widget createDimensionSlot() {
        if (getDimensions().length == 0) return super.createDimensionSlot();
        Supplier<DimensionMarker> dimSupplier = () -> getDimensions()[Math.toIntExact((System.currentTimeMillis() / 1000) % getDimensions().length)];
        CustomItemStackHandler handler = new CustomItemStackHandler(1);
        var slot = new ItemSlot(handler, 0, false, false) {

            @Override
            public void drawOverlay(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                if (ConfigHolder.INSTANCE.compat.showDimensionTier) {
                    overlay = new TextTexture("T" + (dimSupplier.get().tier >= DimensionMarker.MAX_TIER ? "?" : dimSupplier.get().tier)).scale(0.75F).transform(-3.0F, 5.0F);
                }
                super.drawOverlay(graphics, mouseX, mouseY, partialTicks);
            }

            @Override
            public ItemStack getRealStack(ItemStack itemStack) {
                handler.setStackInSlot(0, dimSupplier.get().getIcon());
                return dimSupplier.get().getIcon();
            }
        };
        slot.setIngredientIO(IngredientIO.INPUT);
        return slot;
    }

    public DimensionMarker[] getDimensions() {
        if (dimensions != null && dimensions.length > 0) {
            return dimensions;
        }
        return dimensions = Stream.of(Dimension.values()).filter(d -> d.getGalaxy() == galaxy)
                .map(Dimension::getLocation)
                .map(GTRegistries.DIMENSION_MARKERS::get)
                .filter(Objects::nonNull)
                .toArray(DimensionMarker[]::new);
    }

    @Override
    public boolean testCondition(IRecipeHandlerHolder holder, RecipeHandlerUnit unit, GTRecipeDefinition recipe) {
        Level level = holder.self().getLevel();
        return level != null && GTODimensions.getGalaxy(level.dimension()) == galaxy;
    }
}
