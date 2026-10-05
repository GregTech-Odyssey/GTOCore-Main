package com.gtocore.common.machine.multiblock.electric.space.spacestaion.recipe;

import com.gtocore.common.machine.multiblock.electric.space.spacestaion.RecipeExtension;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.data.Galaxy;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyTypes;

import com.gto.datasynclib.util.holder.BooleanHolder;
import com.gto.datasynclib.util.holder.ObjHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;

@DataGeneratorScanned
public class SpaceDroneDock extends RecipeExtension {

    public SpaceDroneDock(MetaMachineBlockEntity metaMachineBlockEntity) {
        super(metaMachineBlockEntity);
    }

    @Override
    @Nullable
    public GTRecipe fullModifyRecipe(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipeDefinition definition) {
        long maxParallel;
        BooleanHolder hasInput = new BooleanHolder();
        ObjHolder<BigInteger> costEU = new ObjHolder<>();
        ObjHolder<ItemStack> outputHolder = new ObjHolder<>();
        ObjHolder<AEItemKey> inputHolder = new ObjHolder<>();
        KeyIngredient chargeable = definition.itemInputs.ingredient(0);
        unit.forEachKey(AEKeyTypes.ITEMS, true, (key, amount) -> {
            var itemKey = (AEItemKey) key;
            if (!KeyIngredient.accepts(chargeable, itemKey.uid, itemKey)) return false;
            ItemStack output = itemKey.toStack(1);
            if (GTCapabilityHelper.getElectricItem(output) instanceof ElectricItem electricItem) {
                var change = BigInteger.valueOf(electricItem.getCharge());
                if (change.compareTo(BigInteger.ZERO) > 0) {
                    costEU.value = change;
                    electricItem.setCharge(0);
                    inputHolder.value = itemKey;
                    outputHolder.value = output;
                    hasInput.set(true);
                    return true;
                }
            }
            return false;
        });
        if (!hasInput.get() || costEU.value == null || costEU.value.compareTo(BigInteger.ZERO) <= 0) {
            IdleReason.DRONE_NO_ENERGY.setReason(this);
            return null;
        }
        var recipe = definition.toRuntime();
        recipe.itemInputs = recipe.itemInputs.range(1, recipe.itemInputs.size());

        maxParallel = Math.max(1, costEU.value.divide(BigInteger.valueOf(600_000)).longValue());
        // "0.1 + 6.384 / (1.632 + (消耗的电量(单位：GEU))) ^ 4"
        double base = (1.632 + costEU.value.doubleValue() / 1_000_000_000);
        base = base * base;
        recipe.duration = (int) (recipe.duration * (0.1 + 6.384 / base / base));
        recipe = ParallelLogic.accurateParallel(this, unit, recipe, maxParallel);
        if (recipe == null) return null;
        unit.inputItem(inputHolder.value, 1);
        var outputKey = AEItemKey.of(outputHolder.value);
        if (outputKey != null) output(outputKey, 1);

        return recipe;
    }

    @Override
    public void customText(@NotNull List<Component> list) {
        super.customText(list);
        var level = getLevel();
        if (level == null) return;
        var galaxy = GTODimensions.getGalaxy(level.dimension());
        if (galaxy == null) {
            list.add(Component.translatable(NOT_IN_SPACETIME_DOMAIN));
            return;
        }
        if (!MultiblockPage.isScreenText()) list.add(Component.translatable(CURRENT_GALAXY, Component.translatable(galaxy.getTranslationKey())));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addReading(CURRENT_GALAXY, MultiblockPage.cachedRef(this::currentGalaxy, galaxy -> Component.translatable(galaxy.getTranslationKey()))).bindLevel(() -> currentGalaxy() == Galaxy.NONE ? Level.WARNING : Level.NORMAL);
    }

    private Galaxy currentGalaxy() {
        var level = getLevel();
        if (level == null) return Galaxy.NONE;
        var galaxy = GTODimensions.getGalaxy(level.dimension());
        return galaxy == null ? Galaxy.NONE : galaxy;
    }

    @RegisterLanguage(cn = "当前空间站所在星系：%s", en = "Current Space Station Galaxy: %s")
    public static final String CURRENT_GALAXY = "gtocore.machine.space_drone_dock.current_galaxy";
    @RegisterLanguage(cn = "当前空间站不在时空域中！", en = "The current space station is not in the spacetime domain!")
    public static final String NOT_IN_SPACETIME_DOMAIN = "gtocore.machine.space_drone_dock.not_in_spacetime_domain";
    @RegisterLanguage(cn = "无人机内没有电，无法出发！", en = "The drone has no power and cannot set off!")
    public static final String DRONE_NO_ENERGY = "gtocore.machine.space_drone_dock.drone_no_energy";
}
