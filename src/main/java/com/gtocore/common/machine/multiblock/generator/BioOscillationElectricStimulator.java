package com.gtocore.common.machine.multiblock.generator;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiModule;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
@DataGeneratorScanned
public class BioOscillationElectricStimulator extends ElectricMultiblockMachine implements IMultiModule<BioOscillationGenerator>, ICustomRecipeLogicHolder {

    private BioOscillationGenerator controller;
    @SaveToDisk
    private int stimulationLevel;
    @SaveToDisk
    private int currentStimulation;

    public BioOscillationElectricStimulator(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (controller != null && controller.getTissue() != null) {
            currentStimulation = stimulationLevel;
            return RecipeBuilder.ofRaw()
                    .duration(120 * GTOCore.difficulty)
                    .EUt(BioOscillationGenerator.TISSUE_MATERIALS_TIER.get(controller.getTissue()).energyConsumption() * stimulationLevel / 1000 / 200)
                    .build();
        } else {
            return null;
        }
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        if (controller != null && controller.getTissue() != null) {
            controller.stimulateTissue(currentStimulation);
            currentStimulation = 0;
        }
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        textList.add(Component.translatable(BUTTON_SET_ELECTRICAL_STIMULATION, FormattingUtil.formatNumber2Places(stimulationLevel / 10f)).append(ComponentPanelWidget.withButton(Component.literal(" [-]"), "Sub")).append(ComponentPanelWidget.withButton(Component.literal(" [+]"), "Add")));
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            var amount = clickData.isCtrlClick ? 100 : (clickData.isShiftClick ? 10 : 1);
            stimulationLevel = Mth.clamp(stimulationLevel + ("Add".equals(componentData) ? amount : -amount), 0, 1000);
            getRecipeLogic().updateTickSubscription();
        }
    }

    @RegisterLanguage(cn = "组织等级：§b%s§r", en = "Tissue tier: §b%s§r")
    public static final String TISSUE_TIER = "gtocore.biooscillation.tissue.tier";
    @RegisterLanguage(cn = "生长阶段对应点数：§b0§r/§a%s§r/§e%s§r/§6%s§r/§8%s§r", en = "Growth stage corresponding points: §b0§r/§a%s§r/§e%s§r/§6%s§r/§8%s§r")
    public static final String TISSUE_GROWTH_STAGE_POINTS = "gtocore.biooscillation.tissue.growth.stage.points";
    @RegisterLanguage(cn = "（§b幼年§r/§a成年§r/§e壮年§r/§6老年§r/§8死亡§r）", en = "(§bJuvenile§r/§aAdult§r/§eMature§r/§6Elderly§r/§8Death§r)")
    public static final String TISSUE_GROWTH_STAGE_POINTS_DESC = "gtocore.biooscillation.tissue.growth.stage.points.desc";
    @RegisterLanguage(cn = "最大生长速率：§b%s§r/5ticks", en = "Maximum growth rate: §b%s§r/5ticks")
    public static final String TISSUE_MAX_GROW_RATE = "gtocore.biooscillation.tissue.max.grow.rate";
    @RegisterLanguage(cn = "饱和因子：§b%s§r", en = "Saturation factor: §b%s§r")
    public static final String TISSUE_SATURATION_FACTOR = "gtocore.biooscillation.tissue.saturation.factor";
    @RegisterLanguage(cn = "电刺激增幅系数：§b%s§r/§a%s§r/§e%s§r/§6%s§r", en = "Electrical stimulation amplification factor: §b%s§r/§a%s§r/§e%s§r/§6%s§r")
    public static final String TISSUE_ELECTRODE_STIMULATION_FACTORS = "gtocore.biooscillation.tissue.electrode.stimulation.factors";
    @RegisterLanguage(cn = "（§b幼年§r/§a成年§r/§e壮年§r/§6老年§r）", en = "(§bJuvenile§r/§aAdult§r/§eMature§r/§6Elderly§r)")
    public static final String TISSUE_ELECTRODE_STIMULATION_FACTORS_DESC = "gtocore.biooscillation.tissue.electrode.stimulation.factors.desc";
    @RegisterLanguage(cn = "电刺激需求：§b%s§r EU（电压等级：§a%s§r）", en = "Electrical stimulation demand: §b%s§r EU (Voltage level: §a%s§r)")
    public static final String TISSUE_ELECTRODE_ENERGY_CONSUMPTION = "gtocore.biooscillation.tissue.electrode.energy.consumption";
    @RegisterLanguage(cn = "培养液与等级需求：§b%s§r及以上", en = "Culture medium demand: §b%s§r and above")
    public static final String TISSUE_MEDIUM_REQUIREMENT = "gtocore.biooscillation.tissue.medium.requirement";
    @RegisterLanguage(cn = "运行控制方块等级：§b%s§r及以上", en = "Running control block tier: §b%s§r and above")
    public static final String TISSUE_RUNNING_CONTROL_BLOCK_TIER = "gtocore.biooscillation.tissue.running.control.block.tier";
    @RegisterLanguage(cn = "运行控制方块等级不足！需求：§b%s§r，当前：§a%s§r", en = "Insufficient running control block level! Required: §b%s§r, Current: §a%s§r")
    public static final String IDLE_REASON_TISSUE_TIER = "gtocore.biooscillation.idle.reason.tissue.tier";
    @RegisterLanguage(cn = "培养液等级不足！需求：§b%s§r，当前：§a%s§r", en = "Insufficient culture medium level! Required: §b%s§r, Current: §a%s§r")
    public static final String IDLE_REASON_MEDIUM_TIER = "gtocore.biooscillation.idle.reason.medium.tier";
    @RegisterLanguage(cn = "当前生物组织：§b%s§r", en = "Current biological tissue: §b%s§r")
    public static final String CURRENT_TISSUE = "gtocore.biooscillation.current.tissue";
    @RegisterLanguage(cn = "生长阶段：§b%s§r(%s/%s)", en = "Growth stage: §b%s§r(%s/%s)")
    public static final String CURRENT_TISSUE_STAGE = "gtocore.biooscillation.current.tissue.stage";
    @RegisterLanguage(cn = "当前培养液营养可用率：§b%s%%§r", en = "Current culture medium nutrient availability: §b%s%%§r")
    public static final String CURRENT_MEDIUM_NUTRIENT_AVAILABILITY = "gtocore.biooscillation.current.medium.nutrient.availability";
    @RegisterLanguage(cn = "营养可用率低于%s%%时，培养液将被清空", en = "When nutrient availability is below %s%%, the culture medium will be cleared")
    public static final String MEDIUM_NUTRIENT_AVAILABILITY_THRESHOLD = "gtocore.biooscillation.medium.nutrient.availability.threshold";
    @RegisterLanguage(cn = "电刺激中... （x%s发电增幅，剩余时间：%s ticks）", en = "Electrical stimulation... (x%s power boost, remaining time: %s ticks)")
    public static final String ELECTRICAL_STIMULATION = "gtocore.biooscillation.electrical.stimulation";
    @RegisterLanguage(cn = "幼年", en = "Juvenile")
    public static final String TISSUE_STAGE_JUVENILE = "gtocore.biooscillation.tissue.stage.juvenile";
    @RegisterLanguage(cn = "成年", en = "Adult")
    public static final String TISSUE_STAGE_ADULT = "gtocore.biooscillation.tissue.stage.adult";
    @RegisterLanguage(cn = "壮年", en = "Mature")
    public static final String TISSUE_STAGE_MATURE = "gtocore.biooscillation.tissue.stage.mature";
    @RegisterLanguage(cn = "老年", en = "Elderly")
    public static final String TISSUE_STAGE_ELDERLY = "gtocore.biooscillation.tissue.stage.elderly";
    @RegisterLanguage(cn = "清空所有培养液和组织", en = "Clear all culture medium and tissue")
    public static final String BUTTON_CLEAR_ALL = "gtocore.biooscillation.button.clear.all";
    @RegisterLanguage(cn = "设置电刺激强度：%s%%", en = "Set electrical stimulation intensity: %s%%")
    public static final String BUTTON_SET_ELECTRICAL_STIMULATION = "gtocore.biooscillation.button.set.electrical.stimulation";
    @RegisterLanguage(cn = "通过界面调整电刺激强度，范围为0%至100%；每次完整的电刺激过程持续%s秒", en = "Adjust the electrical stimulation intensity through the interface, ranging from 0% to 100%; each complete electrical stimulation process lasts for %s seconds")
    public static final String BUTTON_SET_ELECTRICAL_STIMULATION_DESC = "gtocore.biooscillation.button.set.electrical.stimulation.desc";

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        IMultiModule.super.addedToController(controller);
        if (controller instanceof BioOscillationGenerator c) {
            c.stimulator = this;
        }
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        IMultiModule.super.removedFromController(controller);
        if (controller instanceof BioOscillationGenerator c) {
            c.stimulator = null;
        }
    }
}
