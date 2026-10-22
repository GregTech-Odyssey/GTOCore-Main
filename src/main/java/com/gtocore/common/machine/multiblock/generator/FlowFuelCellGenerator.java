package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.api.electrochemistry.HalfReaction;
import com.gtocore.api.electrochemistry.HalfReactionRecipeExtension;
import com.gtocore.api.electrochemistry.IonMembrane;
import com.gtocore.api.electrochemistry.ReactionBalance;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.multiblock.electric.ElectrochemicalMachine;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiPredicate;

import static com.gregtechceu.gtceu.api.GTValues.*;

@DataGeneratorScanned
public final class FlowFuelCellGenerator extends ElectrochemicalMachine {

    @RegisterLanguage(cn = "燃料效率乘数：%s", en = "Fuel Efficiency Multiplier: %s")
    public static final String FUEL_EFFICIENCY = "gtocore.machine.fuelcell_efficiency";

    public FlowFuelCellGenerator(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean isGenerator() {
        return getRecipeType() == GTORecipeTypes.FUEL_CELL_ENERGY_RELEASE_RECIPES;
    }

    @Override
    public boolean findRecipe(GTRecipeType type, BiPredicate<RecipeHandlerUnit, GTRecipeDefinition> canHandle, GTRecipeDefinition lockedRecipe) {
        if (!isGenerator()) return super.findRecipe(type, canHandle, lockedRecipe);
        if (lockedRecipe != null) {
            for (var unit : getInputUnits()) {
                if (canHandle.test(unit, lockedRecipe)) return true;
            }
            return false;
        }
        for (var unit : getInputUnits()) {
            if (unit.findRecipe(type, canHandle)) return true;
        }
        return false;
    }

    @Override
    protected GTRecipeDefinition combine(GTRecipeType type, GTRecipeDefinition first, GTRecipeDefinition second, HalfReaction firstHalf, HalfReaction secondHalf) {
        if (firstHalf.fuel() == secondHalf.fuel()) {
            recordFailure(IdleReason.FUEL_CELL_PAIR_MISMATCH, 3);
            return null;
        }
        var membrane = IonMembrane.of(membraneHatch.membrane());
        long efficiencyPercent = Math.round(membrane.fuelEfficiency() * 100);
        var balance = ReactionBalance.fuel(firstHalf.charge(), secondHalf.charge(), firstHalf.fuel(), efficiencyPercent);
        if (balance == null) {
            recordFailure(IdleReason.ELECTROCHEMICAL_CHARGE_MISMATCH, 3);
            return null;
        }
        var fuel = firstHalf.fuel() ? first : second;
        var electrolyte = firstHalf.fuel() ? second : first;
        long fuelMultiplier = firstHalf.fuel() ? balance.firstMultiplier() : balance.secondMultiplier();
        long scale = ReactionBalance.minimumStorageScale(fuel.data.getLong(GTORecipeDataKeys.CONVERTED_ENERGY), fuelMultiplier, efficiencyPercent,
                electrolyte.data.getLong(GTORecipeDataKeys.CONVERTED_ENERGY));
        return combine(type, first, second, Math.multiplyExact(balance.firstMultiplier(), scale), Math.multiplyExact(balance.secondMultiplier(), scale));
    }

    @Override
    protected boolean preparePair(GTRecipe first, GTRecipe second) {
        var fuel = first.data.getData(HalfReactionRecipeExtension.INSTANCE).fuel() ? first : second;
        var electrolyte = fuel == first ? second : first;
        long efficiencyPercent = Math.round(IonMembrane.of(membraneHatch.membrane()).fuelEfficiency() * 100);
        long amount = ReactionBalance.storedAmount(fuel.data.getLong(GTORecipeDataKeys.CONVERTED_ENERGY), fuel.parallels, efficiencyPercent,
                electrolyte.data.getLong(GTORecipeDataKeys.CONVERTED_ENERGY));
        if (amount <= 0) {
            recordFailure(IdleReason.FUEL_CELL_STORAGE_AMOUNT, 6);
            return false;
        }
        electrolyte.fluidInputs = electrolyte.fluidInputs.withAmount(0, amount);
        electrolyte.fluidOutputs = electrolyte.fluidOutputs.withAmount(0, amount);
        return true;
    }

    @Override
    protected GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, GTRecipe recipe) {
        if (!isGenerator()) return ParallelLogic.accurateParallel(this, unit, recipe, ParallelLogic.MAX_PARALLEL);
        var membrane = IonMembrane.of(membraneStack());
        if (membrane == null) {
            setIdleReason(IdleReason.ELECTROCHEMICAL_MEMBRANE_MISSING);
            return null;
        }
        long parallel = Math.min(membrane.fuelcellParallel(), membraneUsesRemaining());
        if (parallel <= 0) {
            setIdleReason(IdleReason.ELECTROCHEMICAL_MEMBRANE_EXHAUSTED);
            return null;
        }
        return ParallelLogic.accurateParallel(this, unit, recipe, parallel);
    }

    @Override
    public void beforeWorking(RecipeHandlerUnit unit, GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        if (isGenerator()) consumeMembraneUses(recipe.parallels);
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (!isGenerator() && membraneHatch != null) {
            var membrane = IonMembrane.of(membraneHatch.membrane());
            if (membrane != null) textList.add(Component.translatable(FUEL_EFFICIENCY, FormattingUtil.formatNumber2Places(membrane.fuelEfficiency())));
        }
    }

    public static final class Wrapper {

        public static final Reference2LongOpenHashMap<Material> ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET = new Reference2LongOpenHashMap<>(6);

        static {
            ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.put(GTOMaterials.IronChromiumRedoxFlowBatteryElectrolyte, V[UEV] * 2 / 1000);
            ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.put(GTOMaterials.VanadiumRedoxFlowBatteryElectrolyte, V[UIV] * 3 / 1000);
            ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.put(GTOMaterials.ZincIodideFlowBatteryElectrolyte, V[UXV] * 6 / 1000);
            ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.put(GTOMaterials.OrganicMoleculeRedoxFlowBatteryElectrolyte, V[MAX] * 2 / 1000);
            ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.put(GTOMaterials.SuperconductingIonRedoxFlowBatteryElectrolyte, V[MAX] * 16 / 1000);
            ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.put(GTOMaterials.AntimatterRedoxFlowBatteryElectrolyte, V[MAX] * 160 / 1000);
        }
    }
}
