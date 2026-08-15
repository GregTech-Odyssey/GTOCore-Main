package com.gtocore.common.machine.multiblock.electric.space;

import com.gtocore.common.saved.DysonSphereSavaedData;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class DysonSphereLaunchSiloMachine extends ElectricMultiblockMachine {

    private ResourceKey<Level> dimension;

    public DysonSphereLaunchSiloMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    private ResourceKey<Level> getDimension() {
        if (dimension == null) {
            var currentDimension = Objects.requireNonNull(getLevel()).dimension();
            dimension = GTODimensions.isOverworld(currentDimension) ? Level.OVERWORLD : currentDimension;
        }
        return dimension;
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (!GTODimensions.isPlanet(getDimension())) return null;
        if (DysonSphereSavaedData.getDimensionData(getDimension()).leftInt() >= 100000) return null;
        int integer = GTODimensions.getPlanetDistances(getDimension());
        if (integer > 0) recipe.duration = recipe.duration * integer / 4;
        return RecipeModifier.overclocking(this, unit, recipe, false, 1, 1, 0.85);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        var recipe = getRecipeLogic().getLastRecipe();
        if (recipe == null) return;
        IntIntImmutablePair pair = DysonSphereSavaedData.getDimensionData(getDimension());
        if (pair.leftInt() >= 100000) return;
        long launches = recipe.parallels;
        int damage = pair.rightInt();
        if (damage > 60) {
            damage = 0;
            launches--;
        }
        int count = pair.leftInt();
        if (launches > 0) count = (int) Math.min(100000L, count + (long) launches);
        DysonSphereSavaedData.setDysonData(getDimension(), count, damage);
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (DysonSphereSavaedData.getDimensionUse(getDimension())) textList.add(Component.translatable("gtceu.multiblock.large_miner.working"));
    }
}
