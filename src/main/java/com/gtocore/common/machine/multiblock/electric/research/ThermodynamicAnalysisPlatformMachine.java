package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.machine.multiblock.part.HeatHatchPartMachine;
import com.gtocore.common.machine.multiblock.part.research.SimpleResearchTagPartMachine;
import com.gtocore.data.IdleReason;

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
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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
        if (getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.LOW_TEMP_INTERFACE, Collections.emptySet()).contains(part.self().getPos())) {
            lowTempInterface = (HeatHatchPartMachine) part;
        }
        if (getMultiblockState().getMatchContext().getOrDefault(GTOPredicates.DataKeys.HIGH_TEMP_INTERFACE, Collections.emptySet()).contains(part.self().getPos())) {
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
    public void setWorkingEnabled(boolean isWorkingAllowed) {}

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (highTempInterface == null || lowTempInterface == null || dataHolder == null) {
            return null;
        }
        double difference = getAbsTempDifference();
        if (difference > 10) {
            IdleReason.THERMAL_ZONE_TEMP_DIFFERENCE.report(this, 10, (long) Math.ceil(difference));
            return null;
        }
        return RecipeBuilder.ofRaw().duration(600).EUt(VA[ZPM]).build();
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        if (MultiblockPage.isScreenText()) return;
        textList.add(Component.translatable(THERMAL_ZONE_MONITORING, highTempInterface == null ?
                Component.translatable(THERMAL_ZONE_NO_DATA) : FormattingUtil.formatNumber2Places(highTempInterface.getHeatContainer().getTemperature())));
        textList.add(Component.translatable(LOW_TEMP_THERMAL_ZONE_MONITORING, lowTempInterface == null ?
                Component.translatable(THERMAL_ZONE_NO_DATA) : FormattingUtil.formatNumber2Places(lowTempInterface.getHeatContainer().getTemperature())));
        textList.add(Component.translatable(THERMAL_ZONE_TEMP_DIFFERENCE, FormattingUtil.formatNumber2Places(getAbsTempDifference())));
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addLine(HIGH_TEMP_ZONE, MultiblockPage.cached(() -> temperatureBits(highTempInterface), ThermodynamicAnalysisPlatformMachine::temperatureText));
        page.addLine(LOW_TEMP_ZONE, MultiblockPage.cached(() -> temperatureBits(lowTempInterface), ThermodynamicAnalysisPlatformMachine::temperatureText));
        page.addLine(TEMP_DIFFERENCE, MultiblockPage.decimalText(this::getAbsTempDifference, "K"));
    }

    private static long temperatureBits(@Nullable HeatHatchPartMachine hatch) {
        return hatch == null ? NO_DATA_BITS : Double.doubleToLongBits(hatch.getHeatContainer().getTemperature());
    }

    private static Component temperatureText(long bits) {
        return bits == NO_DATA_BITS ? Component.translatable(THERMAL_ZONE_NO_DATA) : Component.literal(FormattingUtil.formatNumber2Places(Double.longBitsToDouble(bits)) + " K");
    }

    private static final long NO_DATA_BITS = Double.doubleToLongBits(Double.NaN);

    @RegisterLanguage(cn = "高温热区温度", en = "High-Temp Zone Temperature")
    private static final String HIGH_TEMP_ZONE = "gtocore.machine.thermodynamic_analysis_platform.high_temp_zone";
    @RegisterLanguage(cn = "低温热区温度", en = "Low-Temp Zone Temperature")
    private static final String LOW_TEMP_ZONE = "gtocore.machine.thermodynamic_analysis_platform.low_temp_zone";
    @RegisterLanguage(cn = "热区温差", en = "Zone Temperature Difference")
    private static final String TEMP_DIFFERENCE = "gtocore.machine.thermodynamic_analysis_platform.temp_difference";
    @RegisterLanguage(cn = "高温热区监测：%sK", en = "High-Temp Thermal Zone Monitoring: %sK")
    public static final String THERMAL_ZONE_MONITORING = "gtocore.machine.thermal_zone_monitoring";
    @RegisterLanguage(cn = "低温热区监测：%sK", en = "Low-Temp Thermal Zone Monitoring: %sK")
    public static final String LOW_TEMP_THERMAL_ZONE_MONITORING = "gtocore.machine.low_temp_thermal_zone_monitoring";
    @RegisterLanguage(cn = "热区温差：%sK", en = "Thermal Zone Temperature Difference: %sK")
    public static final String THERMAL_ZONE_TEMP_DIFFERENCE = "gtocore.machine.thermal_zone_temp_difference";
    @RegisterLanguage(cn = "（无数据）", en = "(No Data)")
    public static final String THERMAL_ZONE_NO_DATA = "gtocore.machine.thermal_zone_no_data";
}
