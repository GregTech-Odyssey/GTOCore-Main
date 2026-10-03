package com.gtocore.common.machine.multiblock.electric.space;

import com.gtocore.common.saved.DysonSphereSavaedData;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.utils.MachineUtils;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.transfer.key.Keys;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.annotations.SaveToDisk;
import earth.terrarium.adastra.api.planets.PlanetApi;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class SpaceProbeSurfaceReceptionMachine extends ElectricMultiblockMachine {

    private ResourceKey<Level> dimension;

    @SaveToDisk(defaultValue = "false")
    private boolean use;

    public SpaceProbeSurfaceReceptionMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    private ResourceKey<Level> getDimension() {
        if (dimension == null) dimension = Objects.requireNonNull(getLevel()).dimension();
        return dimension;
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (use) DysonSphereSavaedData.setDysonUse(getDimension(), true);
        super.beforeWorking(unit, recipe);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        if (use) {
            DysonSphereSavaedData.setDysonUse(getDimension(), false);
            use = false;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (use) {
            DysonSphereSavaedData.setDysonUse(getDimension(), false);
            use = false;
        }
    }

    @Override
    public GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        if (!PlanetApi.API.isSpace(getLevel())) {
            IdleReason.ONLY_IN_SPACE.setReason(this);
            return null;
        }
        recipe = RecipeModifier.perfectOverclocking(this, unit, recipe);
        if (recipe == null) return null;
        if (!DysonSphereSavaedData.getDimensionUse(getDimension())) {
            double number = (double) DysonSphereSavaedData.getDimensionData(getDimension()).leftInt() / 100;
            if (number > 1) {
                use = true;
                var outputs = recipe.fluidOutputs;
                if (!outputs.isEmpty()) {
                    var first = outputs.range(0, 1);
                    recipe.fluidOutputs = first.chance(0) == 0 ? first : first.withAmount(0, Keys.multiply(first.amount(0), (long) number));
                }
                return recipe;
            }
        }
        return recipe;
    }

    @Override
    public boolean handleTickRecipe(GTRecipe recipe) {
        if (super.handleTickRecipe(recipe)) {
            Level level = getLevel();
            if (level == null) return false;
            if (getOffsetTimer() % 20 == 0) {
                BlockPos pos = MachineUtils.getOffsetPos(8, 28, getFrontFacing(), getPos());
                for (int i = -4; i < 5; i++) {
                    for (int j = -4; j < 5; j++) {
                        if (!level.canSeeSky(pos.offset(i, 0, j))) {
                            IdleReason.SKY_OBSTRUCTED.setReason(this);
                            return false;
                        }
                    }
                }
                return true;
            }
            return true;
        }
        return false;
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (!MultiblockPage.isScreenText()) textList.add(Component.translatable("gtocore.machine.dyson_sphere.amount", DysonSphereSavaedData.getDimensionData(getDimension()).leftInt()));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading("gtocore.machine.dyson_sphere.amount", MultiblockPage.numberText(() -> DysonSphereSavaedData.getDimensionLaunchData(getDimension()), ""));
    }
}
