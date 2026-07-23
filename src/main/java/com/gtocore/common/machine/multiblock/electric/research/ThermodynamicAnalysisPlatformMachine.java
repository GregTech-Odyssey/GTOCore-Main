package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.machine.multiblock.part.HeatHatchPartMachine;
import com.gtocore.common.machine.multiblock.part.research.SimpleResearchTagPartMachine;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import com.gto.fastcollection.OpenCacheHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.VA;
import static com.gregtechceu.gtceu.api.GTValues.ZPM;

@DataGeneratorScanned
public class ThermodynamicAnalysisPlatformMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder {

    private SimpleResearchTagPartMachine dataHolder;
    private HeatHatchPartMachine lowTempInterface;
    private HeatHatchPartMachine highTempInterface;

    public ThermodynamicAnalysisPlatformMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        if (getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.LOW_TEMP_INTERFACE, new OpenCacheHashSet<>()).contains(part.self().getPos())) {
            lowTempInterface = (HeatHatchPartMachine) part;
        }
        if (getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.HIGH_TEMP_INTERFACE, new OpenCacheHashSet<>()).contains(part.self().getPos())) {
            highTempInterface = (HeatHatchPartMachine) part;
        }
        if (part instanceof SimpleResearchTagPartMachine) {
            dataHolder = (SimpleResearchTagPartMachine) part;
        }
        super.onPartScan(part);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        lowTempInterface = null;
        highTempInterface = null;
        dataHolder = null;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        lowTempInterface = null;
        highTempInterface = null;
        dataHolder = null;
    }

    @Override
    public boolean handleRecipeOutput(GTRecipe recipe) {
        var abs = getAbsTempDifference();
        if (abs > 10 && dataHolder != null) {
            dataHolder.addData(abs / 250d);
        }
        return super.handleRecipeOutput(recipe);
    }

    private double getAbsTempDifference() {
        if (highTempInterface == null || lowTempInterface == null) {
            return 0;
        }
        var highTemp = highTempInterface.getHeatContainer().getTemperature();
        var lowTemp = lowTempInterface.getHeatContainer().getTemperature();
        return Math.abs(highTemp - lowTemp);
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (highTempInterface == null || lowTempInterface == null || dataHolder == null) {
            return null;
        }
        if (getAbsTempDifference() > 10) {
            setIdleReason(Component.translatable(THERMAL_ZONE_TEMP_DIFFERENCE_TOO_LARGE));
            return null;
        }
        return RecipeBuilder.ofRaw().duration(600).EUt(VA[ZPM]).build();
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable(THERMAL_ZONE_MONITORING, highTempInterface == null ?
                Component.translatable(THERMAL_ZONE_NO_DATA) : FormattingUtil.formatNumber2Places(highTempInterface.getHeatContainer().getTemperature())));
        textList.add(Component.translatable(LOW_TEMP_THERMAL_ZONE_MONITORING, lowTempInterface == null ?
                Component.translatable(THERMAL_ZONE_NO_DATA) : FormattingUtil.formatNumber2Places(lowTempInterface.getHeatContainer().getTemperature())));
        textList.add(Component.translatable(THERMAL_ZONE_TEMP_DIFFERENCE, FormattingUtil.formatNumber2Places(getAbsTempDifference())));
    }

    @RegisterLanguage(cn = "高温热区监测：%sK", en = "High-Temp Thermal Zone Monitoring: %sK")
    public static final String THERMAL_ZONE_MONITORING = "gtocore.machine.thermal_zone_monitoring";
    @RegisterLanguage(cn = "低温热区监测：%sK", en = "Low-Temp Thermal Zone Monitoring: %sK")
    public static final String LOW_TEMP_THERMAL_ZONE_MONITORING = "gtocore.machine.low_temp_thermal_zone_monitoring";
    @RegisterLanguage(cn = "热区温差：%sK", en = "Thermal Zone Temperature Difference: %sK")
    public static final String THERMAL_ZONE_TEMP_DIFFERENCE = "gtocore.machine.thermal_zone_temp_difference";
    @RegisterLanguage(cn = "初始热区温差过大，无法进行分析", en = "Initial thermal zone temperature difference is too large to perform analysis")
    public static final String THERMAL_ZONE_TEMP_DIFFERENCE_TOO_LARGE = "gtocore.machine.thermal_zone_temp_difference_too_large";
    @RegisterLanguage(cn = "（无数据）", en = "(No Data)")
    public static final String THERMAL_ZONE_NO_DATA = "gtocore.machine.thermal_zone_no_data";
}
