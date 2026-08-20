package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOFluidStorageKey;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.machine.multiblock.part.SensorPartMachine;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.Scanned;
import com.gtolib.api.annotation.dynamic.DynamicInitialValue;
import com.gtolib.api.annotation.dynamic.DynamicInitialValueTypes;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.IdleReason;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import com.google.common.collect.ImmutableMap;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.util.holder.BooleanHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;

@Scanned
public class FullCellGenerator extends ElectricMultiblockMachine {

    private static final int MaxCanReleaseParallel = 50;
    private static final long WATER_RECOVERY_AMOUNT = 600;
    private static final double WATER_RECOVERY_RATE = 0.15d;
    private static final BigInteger BIG_INTEGER_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger BIG_INTEGER_MAX_PARALLEL = BigInteger.valueOf(ParallelLogic.MAX_PARALLEL);

    @DynamicInitialValue(key = "fuelcell.chance_consume", easyValue = "0.0d", normalValue = "0.035d", expertValue = "0.055d", typeKey = DynamicInitialValueTypes.KEY_PROBABILITY, cn = "放电时膜损坏概率", cnComment = """
            放电时使用的膜材料的损坏概率。
            """, en = "Fuel Cell Membrane Damage Chance on Discharge", enComment = """
            The chance of the membrane material used being damaged upon discharging.
            """)
    public static double chanceConsumeMembraneOnDischarge = 0.035d;

    private boolean isGenerator = false;
    @SaveToDisk(defaultValue = "1.0")
    private double bonusEfficiency = 1.0f;
    @SaveToDisk(defaultValue = "1.0")
    private double accumulatedEfficiencyDecay = 1.0f;
    @SaveToDisk(defaultValue = "-1")
    private int absorptionMembraneTier = -1;

    @Nullable
    private SensorPartMachine sensorPart;

    private TickableSubscription updateSubs;

    public FullCellGenerator(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateGeneratorState();
        updateSubs = subscribeServerTick(updateSubs, this::recoverEfficiency);
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        if (sensorPart == null && part instanceof SensorPartMachine sensor) {
            this.sensorPart = sensor;
            sensor.update((float) bonusEfficiency);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    private void updateGeneratorState() {
        var recipeType = getRecipeType();
        isGenerator = recipeType == GTORecipeTypes.FUEL_CELL_ENERGY_RELEASE_RECIPES;
        if (recipeType == GTORecipeTypes.FUEL_CELL_ENERGY_ABSORPTION_RECIPES) {
            var membraneInfo = getStoredAbsorptionMembraneInfo();
            if (membraneInfo != null) {
                updateAbsorptionEfficiency(membraneInfo, accumulatedEfficiencyDecay);
            }
        } else {
            updateDisplayedEfficiency(1.0d);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        bonusEfficiency = 1.0d;
        sensorPart = null;
    }

    @Override
    public boolean isGenerator() {
        return isGenerator;
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(@NotNull RecipeHandlerUnit unit, GTRecipe recipe) {
        var activeType = recipe.definition.recipeType;
        if (activeType == GTORecipeTypes.FUEL_CELL_ENERGY_RELEASE_RECIPES) {
            return getReleaseRecipe(unit, recipe);
        } else if (activeType == GTORecipeTypes.FUEL_CELL_ENERGY_ABSORPTION_RECIPES) {
            return getAbsorptionRecipe(unit, recipe);
        } else if (activeType == GTORecipeTypes.FUEL_CELL_ENERGY_TRANSFER_RECIPES) {
            return getElectrolyteTransferRecipe(unit, recipe);
        }
        return null;
    }

    private GTRecipe getAbsorptionRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        var fuelEnergyPerUnit = recipe.data.getLong(GTORecipeDataKeys.CONVERTED_ENERGY);
        // membrane bonus
        var membraneInfo = findMembraneInfo(unit);
        if (membraneInfo == null) {
            setIdleReason(IdleReason.INVALID_INPUT);
            return null;
        }
        updateAbsorptionEfficiency(membraneInfo, accumulatedEfficiencyDecay);
        fuelEnergyPerUnit = (long) (fuelEnergyPerUnit * bonusEfficiency);
        if (fuelEnergyPerUnit <= 0) return null;

        // find existing electrolytes
        Material electrolytesExisting = null;
        long amountExisting = 0;

        Material[] electrolyteMaterials = Wrapper.ELECTROLYTE_MATERIALS;
        long[] cElectrolytesAmounts = unit.getFluidAmount(true, Wrapper.ENERGY_RELEASE_CATHODE_FLUIDS);
        long[] aElectrolytesAmounts = unit.getFluidAmount(true, Wrapper.ENERGY_RELEASE_ANODE_FLUIDS);
        for (int i = 0; i < cElectrolytesAmounts.length; i++) {
            if (cElectrolytesAmounts[i] > 0 && aElectrolytesAmounts[i] > 0) {
                electrolytesExisting = electrolyteMaterials[i];
                amountExisting = Math.min(cElectrolytesAmounts[i], aElectrolytesAmounts[i]);
                break;
            }
        }
        if (electrolytesExisting == null) return null;

        // parallel calculation
        long euPermB = Wrapper.ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.get(electrolytesExisting);
        if (euPermB <= 0) return null;
        long maxCanAbsorbParallel = floorMultiplyDivideForParallel(amountExisting, euPermB, fuelEnergyPerUnit);
        if (maxCanAbsorbParallel <= 0) return null;
        var result = ParallelLogic.accurateParallel(this, unit, recipe, maxCanAbsorbParallel);
        if (result == null) return null;

        // electrolyte consumption adjustment
        long actuallyConsumedmB = ceilMultiplyDivide(result.parallels, fuelEnergyPerUnit, euPermB);
        if (actuallyConsumedmB <= 0 || actuallyConsumedmB > amountExisting) return null;
        var input = new ArrayList<>(result.fluidInputs);
        input.add(new Content<>(FluidIngredient.of(electrolytesExisting.getFluid(GTOFluidStorageKey.ENERGY_RELEASE_ANODE), actuallyConsumedmB), 10000, 0));
        input.add(new Content<>(FluidIngredient.of(electrolytesExisting.getFluid(GTOFluidStorageKey.ENERGY_RELEASE_CATHODE), actuallyConsumedmB), 10000, 0));
        var output = new ArrayList<Content<FluidIngredient>>(2);
        output.add(new Content<>(FluidIngredient.of(electrolytesExisting.getFluid(GTOFluidStorageKey.ENERGY_STORAGE_CATHODE), actuallyConsumedmB), 10000, 0));
        output.add(new Content<>(FluidIngredient.of(electrolytesExisting.getFluid(GTOFluidStorageKey.ENERGY_STORAGE_ANODE), actuallyConsumedmB), 10000, 0));
        result.fluidInputs = input;
        result.fluidOutputs = output;
        return result;
    }

    private GTRecipe getElectrolyteTransferRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (recipe.data.getFloat(GTORecipeDataKeys.EFFICIENCY) <= 0) {
            return null;
        }
        updateDisplayedEfficiency(recipe.data.getFloat(GTORecipeDataKeys.EFFICIENCY));
        return ParallelLogic.accurateParallel(this, unit, recipe, Long.MAX_VALUE);
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (!isGenerator) {
            textList.add(
                    Component.translatable(FUEL_EFFICIENCY, FormattingUtil.formatNumber2Places(bonusEfficiency * 100) + "%"));
            textList.add(
                    Component.translatable(EFFICIENCY_DECAY, DECIMAL_FORMAT_4F.format((1 - accumulatedEfficiencyDecay) * 100) + "%"));
        }
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        var completedRecipe = getRecipeLogic().getLastRecipe();
        if (completedRecipe != null && completedRecipe.definition.recipeType == GTORecipeTypes.FUEL_CELL_ENERGY_ABSORPTION_RECIPES) {
            var membraneInfo = getStoredAbsorptionMembraneInfo();
            if (membraneInfo != null) {
                updateAbsorptionEfficiency(membraneInfo, accumulatedEfficiencyDecay * getEfficiencyDecayFactor(membraneInfo));
                return;
            }
        }
        updateDisplayedEfficiency(1.0d);
    }

    private void recoverEfficiency() {
        if (GTOCore.isEasy() || getRecipeType() != GTORecipeTypes.FUEL_CELL_ENERGY_ABSORPTION_RECIPES ||
                !getRecipeLogic().isIdle() || accumulatedEfficiencyDecay >= 1.0d) {
            return;
        }
        var membraneInfo = findMembraneInfo();
        if (membraneInfo == null) membraneInfo = getStoredAbsorptionMembraneInfo();
        if (membraneInfo != null && inputFluid(GTMaterials.DistilledWater.getFluid(), WATER_RECOVERY_AMOUNT)) {
            double recoveredDecay = accumulatedEfficiencyDecay + (1.0d - accumulatedEfficiencyDecay) * WATER_RECOVERY_RATE;
            if (Double.compare(recoveredDecay, accumulatedEfficiencyDecay) == 0) recoveredDecay = 1.0d;
            updateAbsorptionEfficiency(membraneInfo, recoveredDecay);
        }
    }

    private void updateAbsorptionEfficiency(MembraneBonusInfo membraneInfo, double decay) {
        double normalizedDecay = GTOCore.isEasy() ? 1.0d : Math.clamp(decay, 0.0d, 1.0d);
        double efficiencyBonus = GTOCore.isExpert() ? membraneInfo.efficiencyBonusExpertMode : membraneInfo.efficiencyBonus;
        double newEfficiency = efficiencyBonus * normalizedDecay;
        accumulatedEfficiencyDecay = normalizedDecay;
        absorptionMembraneTier = membraneInfo.tier;
        bonusEfficiency = newEfficiency;
        if (sensorPart != null) sensorPart.update((float) newEfficiency);
    }

    private void updateDisplayedEfficiency(double efficiency) {
        bonusEfficiency = efficiency;
        if (sensorPart != null) sensorPart.update((float) efficiency);
    }

    private static double getEfficiencyDecayFactor(MembraneBonusInfo membraneInfo) {
        if (GTOCore.isEasy()) return 1.0d;
        return GTOCore.isExpert() ? membraneInfo.efficiencyBonusDecayFactorExpertMode : membraneInfo.efficiencyBonusDecayFactor;
    }

    @Nullable
    private MembraneBonusInfo findMembraneInfo() {
        for (int membraneTier = Wrapper.MEMBRANE_MATS.length - 1; membraneTier >= 0; membraneTier--) {
            var membrane = ChemicalHelper.get(GTOTagPrefix.MEMBRANE_ELECTRODE, Wrapper.MEMBRANE_MATS[membraneTier].membrane);
            for (var unit : getInputUnits()) {
                if (unit.matchItem(membrane)) return Wrapper.MEMBRANE_MATS[membraneTier];
            }
        }
        return null;
    }

    @Nullable
    private static MembraneBonusInfo findMembraneInfo(RecipeHandlerUnit unit) {
        for (int membraneTier = Wrapper.MEMBRANE_MATS.length - 1; membraneTier >= 0; membraneTier--) {
            if (unit.matchItem(ChemicalHelper.get(GTOTagPrefix.MEMBRANE_ELECTRODE, Wrapper.MEMBRANE_MATS[membraneTier].membrane))) {
                return Wrapper.MEMBRANE_MATS[membraneTier];
            }
        }
        return null;
    }

    @Nullable
    private MembraneBonusInfo getStoredAbsorptionMembraneInfo() {
        if (absorptionMembraneTier < 0 || absorptionMembraneTier >= Wrapper.MEMBRANE_MATS.length) return null;
        return Wrapper.MEMBRANE_MATS[absorptionMembraneTier];
    }

    private static long floorMultiplyDivideForParallel(long first, long second, long divisor) {
        if (first <= 0 || second <= 0 || divisor <= 0) return 0;
        if (first <= Long.MAX_VALUE / second) {
            return Math.min(first * second / divisor, ParallelLogic.MAX_PARALLEL);
        }
        var quotient = BigInteger.valueOf(first).multiply(BigInteger.valueOf(second)).divide(BigInteger.valueOf(divisor));
        return quotient.compareTo(BIG_INTEGER_MAX_PARALLEL) >= 0 ? ParallelLogic.MAX_PARALLEL : quotient.longValue();
    }

    private static long ceilMultiplyDivide(long first, long second, long divisor) {
        if (first <= 0 || second <= 0 || divisor <= 0) return 0;
        if (first <= Long.MAX_VALUE / second) {
            return Math.ceilDiv(first * second, divisor);
        }
        var bigDivisor = BigInteger.valueOf(divisor);
        var quotient = BigInteger.valueOf(first).multiply(BigInteger.valueOf(second)).add(bigDivisor).subtract(BigInteger.ONE).divide(bigDivisor);
        return quotient.compareTo(BIG_INTEGER_LONG_MAX) >= 0 ? Long.MAX_VALUE : quotient.longValue();
    }

    private GTRecipe getReleaseRecipe(RecipeHandlerUnit unit, GTRecipe recipe) {
        var input = new ArrayList<>(recipe.itemInputs);
        var content = input.getFirst();
        var ingredient = content.inner;
        var item = ingredient.getInnerItemStack().getItem();
        BooleanHolder hasMembrane = new BooleanHolder(false);
        unit.fastForEachItems(true, (i, a) -> {
            if (i.getItem() == item) {
                hasMembrane.value = true;
            }
        });
        if (!hasMembrane.value) {
            setIdleReason(IdleReason.INVALID_INPUT);
            return null;
        }
        if (GTValues.RNG.nextFloat() < chanceConsumeMembraneOnDischarge) {
            unit.inputItem(ingredient.getInnerItemStack().getItem(), content.amount);
        }
        return ParallelLogic.accurateParallel(this, unit, recipe, MaxCanReleaseParallel);
    }

    @Override
    public void setActiveRecipeType(int activeRecipeType) {
        super.setActiveRecipeType(activeRecipeType);
        updateGeneratorState();
    }

    public static class Wrapper {

        public static final ImmutableMap<Material, Long> ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET = ImmutableMap.<Material, Long>builder()
                .put(GTOMaterials.IronChromiumRedoxFlowBatteryElectrolyte, V[UEV] * 2 / 1000)
                .put(GTOMaterials.VanadiumRedoxFlowBatteryElectrolyte, V[UIV] * 3 / 1000)
                .put(GTOMaterials.ZincIodideFlowBatteryElectrolyte, V[UXV] * 6 / 1000)
                .put(GTOMaterials.OrganicMoleculeRedoxFlowBatteryElectrolyte, V[MAX] * 2 / 1000)
                .put(GTOMaterials.SuperconductingIonRedoxFlowBatteryElectrolyte, V[MAX] * 16 / 1000)
                .put(GTOMaterials.AntimatterRedoxFlowBatteryElectrolyte, V[MAX] * 160 / 1000)
                .build();
        private static final Material[] ELECTROLYTE_MATERIALS = ELECTROLYTES_PER_MATERIAL_PER_MILLIBUCKET.keySet().toArray(Material[]::new);
        private static final Fluid[] ENERGY_RELEASE_CATHODE_FLUIDS = new Fluid[ELECTROLYTE_MATERIALS.length];
        private static final Fluid[] ENERGY_RELEASE_ANODE_FLUIDS = new Fluid[ELECTROLYTE_MATERIALS.length];

        static {
            for (int i = 0; i < ELECTROLYTE_MATERIALS.length; i++) {
                ENERGY_RELEASE_CATHODE_FLUIDS[i] = ELECTROLYTE_MATERIALS[i].getFluid(GTOFluidStorageKey.ENERGY_RELEASE_CATHODE);
                ENERGY_RELEASE_ANODE_FLUIDS[i] = ELECTROLYTE_MATERIALS[i].getFluid(GTOFluidStorageKey.ENERGY_RELEASE_ANODE);
            }
        }

        public static final MembraneBonusInfo[] MEMBRANE_MATS = new MembraneBonusInfo[] {
                new MembraneBonusInfo(
                        0, GTMaterials.Polytetrafluoroethylene,
                        GTOMaterials.IronChromiumRedoxFlowBatteryElectrolyte,
                        1.05d, 0.992d, 1.1d, 0.99d),
                new MembraneBonusInfo(
                        1, GTMaterials.Graphene,
                        GTOMaterials.VanadiumRedoxFlowBatteryElectrolyte,
                        1.3d, 0.994d, 1.21d, 0.991d),
                new MembraneBonusInfo(
                        2, GTOMaterials.PolousPolyolefinSulfonate,
                        GTOMaterials.ZincIodideFlowBatteryElectrolyte,
                        1.69d, 0.996d, 1.44d, 0.993d),
                new MembraneBonusInfo(
                        3, GTOMaterials.PerfluorosulfonicAcidPolytetrafluoroethyleneCopolymer,
                        GTOMaterials.OrganicMoleculeRedoxFlowBatteryElectrolyte,
                        1.96d, 0.999d, 1.69d, 0.995d),
                new MembraneBonusInfo(
                        4, GTOMaterials.CeOxPolyDopamineReinforcedPolytetrafluoroethylene,
                        GTOMaterials.SuperconductingIonRedoxFlowBatteryElectrolyte,
                        2.25d, 0.9994d, 1.96d, 0.999d),
                new MembraneBonusInfo(
                        5, GTOMaterials.NanocrackRegulatedSelfHumidifyingCompositeMaterial,
                        GTOMaterials.AntimatterRedoxFlowBatteryElectrolyte,
                        2.5d, 1.0d, 2.5d, 0.9995d)
        };
        public static final ImmutableMap<Material, MembraneBonusInfo> MEMBRANE_MAT_TO_BONUS = Arrays.stream(MEMBRANE_MATS).collect(ImmutableMap.toImmutableMap(info -> info.membrane, info -> info));
    }

    public record MembraneBonusInfo(
                                    int tier,
                                    Material membrane,
                                    Material electrolyte,
                                    double efficiencyBonus,
                                    double efficiencyBonusDecayFactor,
                                    double efficiencyBonusExpertMode,
                                    double efficiencyBonusDecayFactorExpertMode) {

        public void getInfoComponents(List<Component> components) {
            components.add(Component.translatable(MEMBRANE_TIER, Component.literal(String.valueOf(tier)).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable(DISCHARGE_ELECTROLYTE, electrolyte.getLocalizedName().withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY));
            components.add(Component.translatable(ABSORPTION_EFFICIENCY, Component.literal(FormattingUtil.formatNumber2Places(GTOCore.isExpert() ? efficiencyBonusExpertMode * 100 : efficiencyBonus * 100) + "%").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
            if (!GTOCore.isEasy()) {
                if ((GTOCore.isExpert() ? efficiencyBonusDecayFactorExpertMode : efficiencyBonusDecayFactor) == 1.0d) {
                    components.add(Component.translatable(ABSORPTION_EFFICIENCY_NO_DECAY).withStyle(ChatFormatting.GRAY));
                } else {
                    components.add(Component.translatable(ABSORPTION_EFFICIENCY_DECAY, Component.literal("x" + DECIMAL_FORMAT_4F.format(GTOCore.isExpert() ? efficiencyBonusDecayFactorExpertMode : efficiencyBonusDecayFactor)).withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    private static final DecimalFormat DECIMAL_FORMAT_4F = new DecimalFormat("#,##0.####");

    @RegisterLanguage(cn = "燃料效率乘数：%s", en = "Fuel Efficiency Multiplier: %s")
    private static final String FUEL_EFFICIENCY = "gtocore.machine.fuelcell_efficiency";
    @RegisterLanguage(cn = "连续运行效率衰减：%s", en = "Continuous Operation Efficiency Decay: %s")
    private static final String EFFICIENCY_DECAY = "gtocore.machine.fuelcell_efficiency_decay";
    @RegisterLanguage(cn = "膜等级: %s", en = "Membrane Tier: %s")
    private static final String MEMBRANE_TIER = "gtocore.machine.fuelcell_membrane_tier";
    @RegisterLanguage(cn = "放电模式适用电解质: %s", en = "Discharge Mode Applicable Electrolyte: %s")
    private static final String DISCHARGE_ELECTROLYTE = "gtocore.machine.fuelcell_discharge_electrolyte";
    @RegisterLanguage(cn = "吸收模式效率乘数: %s", en = "Absorption Mode Efficiency Multiplier: %s")
    private static final String ABSORPTION_EFFICIENCY = "gtocore.machine.fuelcell_absorption_efficiency";
    @RegisterLanguage(cn = "吸收模式效率衰减: %s/运行次", en = "Absorption Mode Efficiency Decay: %s/op")
    private static final String ABSORPTION_EFFICIENCY_DECAY = "gtocore.machine.fuelcell_absorption_efficiency_decay";
    @RegisterLanguage(cn = "吸收模式效率衰减: §a无衰减§r", en = "Absorption Mode Efficiency Decay: §aNo Decay§r")
    private static final String ABSORPTION_EFFICIENCY_NO_DECAY = "gtocore.machine.fuelcell_absorption_efficiency_decay.no_decay";
}
