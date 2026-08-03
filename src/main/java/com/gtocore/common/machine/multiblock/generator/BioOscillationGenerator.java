package com.gtocore.common.machine.multiblock.generator;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.machine.multiblock.part.ConnectingRodHatch;
import com.gtocore.common.machine.multiblock.part.SensorPartMachine;

import com.gtolib.api.machine.feature.multiblock.ITierCasingMachine;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.machine.trait.TierCasingTrait;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.api.recipe.TierDataKey;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

import com.google.common.collect.ImmutableMap;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Biomass;
import static com.gregtechceu.gtceu.common.data.GTMaterials.SamariumMagnetic;
import static com.gtocore.common.data.GTOItems.*;
import static com.gtocore.common.data.GTOMaterials.*;
import static com.gtocore.common.data.GTORecipeDataKeys.ENERGY_CONTROL_MODULE_TIER;
import static com.gtocore.common.data.GTORecipeDataKeys.MACHINING_CONTROL_MODULE_TIER;

@Getter
@Setter
public class BioOscillationGenerator extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder, ITierCasingMachine {

    public static final ImmutableMap<Item, TissueData> TISSUE_MATERIALS_TIER = ImmutableMap.<Item, TissueData>builder()
            .put(BIO_CARDIOMYOCYTE_CLUSTER.get(),
                    new TissueData(new int[] { 100, 200, 300, 100 }, 1, 0.01f,
                            new float[] { 1.5f, 4.1f, 3.2f, 1.5f }, 1L << 23, 1, UV))
            .put(MUTANT_CARDIOMYOCYTE_CLUSTER.get(),
                    new TissueData(new int[] { 150, 300, 450, 150 }, 2, 0.2f,
                            new float[] { 1.5f, 5.4f, 5.2f, 1.6f }, 1L << 25, 2, UHV))
            .put(DRAGON_CARDIOMYOCYTE_CLUSTER.get(),
                    new TissueData(new int[] { 250, 500, 750, 250 }, 4, 0.5f,
                            new float[] { 1.5f, 7.2f, 10.4f, 1.3f }, 1L << 27, 3, UEV))
            .put(MODIFIED_DRAGON_HEART.get(),
                    new TissueData(new int[] { 400, 800, 1200, 400 }, 8, 1f,
                            new float[] { 1.8f, 10.25f, 16.5f, 1.34f }, 1L << 29, 4, UIV))
            .put(AWAKENED_DRAGON_HEART.get(),
                    new TissueData(new int[] { 600, 1200, 1800, 600 }, 16, 0.5f,
                            new float[] { 2.25f, 10.25f, 22.6f, 1.35f }, 1L << 31, 5, UXV))
            .build();
    private static final int MAX_BOOST_TICKS = 20 * 60;
    private static final int MAX_MEDIUM_AMOUNT = 1_000_000_000;
    @SaveToDisk(saveNull = true)
    private Material mediumMaterial;
    @SaveToDisk
    private int mediumAmount;
    @SaveToDisk
    @SyncToClient
    private short mediumUsage;
    @SaveToDisk
    private int remainingBoostTicks;
    @SaveToDisk
    private float remainingBoostFactor;
    @SaveToDisk
    private short mediumUsageThreshold;
    @SaveToDisk(saveNull = true)
    private Item tissue;
    @SaveToDisk
    private float tissuePoints;
    @SaveToDisk
    private int tissueAmount;

    private SensorPartMachine sensorPartMachine;
    private ConnectingRodHatch connectingRodHatch;
    BioOscillationElectricStimulator stimulator;
    private final TierCasingTrait tierCasingTrait;
    private TickableSubscription tickableSubscription;

    public BioOscillationGenerator(MetaMachineBlockEntity holder) {
        super(holder);
        this.tierCasingTrait = new TierCasingTrait(this, MACHINING_CONTROL_MODULE_TIER, ENERGY_CONTROL_MODULE_TIER);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.tickableSubscription = subscribeServerTick(tickableSubscription, this::tick);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        if (part instanceof SensorPartMachine sensor) {
            sensorPartMachine = sensor;
        }
        if (part instanceof ConnectingRodHatch connectingRod) {
            connectingRodHatch = connectingRod;
        }
        super.onPartScan(part);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        connectingRodHatch = null;
        sensorPartMachine = null;
        stimulator = null;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        connectingRodHatch = null;
        sensorPartMachine = null;
        stimulator = null;
        if (tickableSubscription != null) {
            tickableSubscription.unsubscribe();
            tickableSubscription = null;
        }
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    void stimulateTissue(int pointsCost) {
        if (tissue == null || tissuePoints <= 0) {
            return;
        }
        var tissueData = TISSUE_MATERIALS_TIER.get(tissue);
        if (tissueData == null) {
            return;
        }
        tissuePoints -= pointsCost;
        if (tissuePoints < 0) {
            clearTissue();
            return;
        }
        var stage = getTissueStage();
        if (stage == null) {
            return;
        }
        var stimulationFactor = tissueData.electrodeStimulationFactors()[stage.ordinal()];
        remainingBoostTicks = Math.min(remainingBoostTicks + pointsCost * MAX_BOOST_TICKS / 1000, MAX_BOOST_TICKS);
        remainingBoostFactor = stimulationFactor;
    }

    public long getGeneratorPower() {
        if (tissue == null || tissuePoints < 0 || mediumMaterial == null || mediumAmount <= 0) {
            return 0;
        }
        var tissueData = TISSUE_MATERIALS_TIER.get(tissue);
        if (tissueData == null) {
            return 0;
        }
        if (connectingRodHatch == null) {
            return 0;
        }
        var tier = BioOscillationGeneratorData.CONNECTING_ROD_TIER.get(connectingRodHatch.getRodMaterial());
        if (tier == null) {
            return 0;
        }
        var power = (Math.pow(tissueData.tier(), tier) * tissueAmount * mediumAmount * 8);
        if (remainingBoostTicks > 0 && remainingBoostFactor > 0) {
            power *= remainingBoostFactor;
        }
        switch (getCasingTier(ENERGY_CONTROL_MODULE_TIER)) {
            case 3 -> power *= 1.5;
            case 2 -> power *= 1.2;
            default -> {}
        }
        return (long) power;
    }

    private Stage getTissueStage() {
        if (tissue == null || tissuePoints <= 0) {
            return null;
        }
        var tissueData = TISSUE_MATERIALS_TIER.get(tissue);
        if (tissueData == null) {
            return null;
        }
        var points = tissuePoints;
        if (points <= tissueData.stage1Points()) {
            return Stage.JUVENILE;
        } else if (points <= tissueData.stage2Points()) {
            return Stage.ADULT;
        } else if (points <= tissueData.stage3Points()) {
            return Stage.MATURE;
        } else {
            return Stage.ELDERLY;
        }
    }

    private float getTissueGrowthFactor() {
        if (tissue == null || tissuePoints < 0) {
            return 0;
        }
        var tissueData = TISSUE_MATERIALS_TIER.get(tissue);
        if (tissueData == null) {
            return 0;
        }
        var medium = mediumUsage / 1000f;
        return tissueData.maxGrowRate() * medium / (tissueData.saturationFactor() + medium);
    }

    private void tick() {
        if (!isFormed()) return;
        if (remainingBoostTicks > 0) {
            remainingBoostTicks--;
        }
        if (remainingBoostTicks == 0) {
            remainingBoostFactor = 0;
        }
        if (getOffsetTimer() % 5 == 0) {
            if (tissue != null && mediumMaterial != null) {
                var growthFactor = getTissueGrowthFactor();
                tissuePoints += growthFactor;
                // noinspection DataFlowIssue
                if (tissuePoints > TISSUE_MATERIALS_TIER.get(tissue).stage4Points() || tissuePoints < 0) {
                    clearTissue();
                }
                if (sensorPartMachine != null) {
                    sensorPartMachine.update(tissuePoints);
                }
            } else {
                if (sensorPartMachine != null) {
                    sensorPartMachine.update(0);
                }
            }
            if (stimulator != null) {
                stimulator.recipeLogic.updateTickSubscription();
            }
        }
        if (getOffsetTimer() % 20 == 0) {
            mediumUsage -= 1;
            if (mediumUsage < mediumUsageThreshold) {
                clearMedium();
            }
        }
    }

    private void clearTissue() {
        tissue = null;
        tissuePoints = 0;
        tissueAmount = 0;
        remainingBoostTicks = 0;
        remainingBoostFactor = 0;
    }

    private void clearMedium() {
        if (mediumMaterial == null || mediumAmount <= 0) {
            return;
        }
        @SuppressWarnings("DataFlowIssue")
        var wasteMaterial = BioOscillationGeneratorData.MEDIUM_MATERIALS_TIER.get(mediumMaterial).wasteMaterial();
        outputFluid(wasteMaterial.getFluid(), mediumAmount);
        mediumMaterial = null;
        mediumAmount = 0;
        mediumUsage = 0;
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        if (tissue == null) {
            forEachItems(true, (stack, amount) -> {
                var data = TISSUE_MATERIALS_TIER.get(stack.getItem());
                if (data != null) {
                    var casingTier = getCasingTier(MACHINING_CONTROL_MODULE_TIER);
                    if (casingTier < data.tier() && casingTier < 3) {
                        setIdleReason(Component.translatable(BioOscillationElectricStimulator.IDLE_REASON_TISSUE_TIER, Math.min(3, data.tier()), casingTier));
                        return false;
                    }
                    int amount1 = (int) Math.min(64, amount);
                    if (inputItem(stack.getItem(), amount1)) {
                        tissue = stack.getItem();
                        tissuePoints = 0;
                        tissueAmount = amount1;
                        return true;
                    }
                }
                return false;
            });
        }
        if (mediumMaterial == null) {
            forEachFluids(true, (fluid, amount) -> {
                var material = ChemicalHelper.getMaterial(fluid.getFluid());
                if (BioOscillationGeneratorData.MEDIUM_MATERIALS_TIER.containsKey(material)) {
                    int amount1 = (int) Math.min(MAX_MEDIUM_AMOUNT, amount);
                    if (inputFluid(fluid.getFluid(), amount1)) {
                        mediumMaterial = material;
                        mediumAmount = amount1;
                        mediumUsage = 1000;
                        return true;
                    }
                }
                return false;
            });
        }
        var tissueData = TISSUE_MATERIALS_TIER.get(tissue);
        if (tissueData == null) {
            return null;
        }
        var mediumTier = BioOscillationGeneratorData.MEDIUM_MATERIALS_TIER.get(mediumMaterial);
        if (mediumTier == null) {
            return null;
        }
        if (mediumTier.tier < tissueData.tier) {
            setIdleReason(Component.translatable(BioOscillationElectricStimulator.IDLE_REASON_MEDIUM_TIER, tissueData.tier(), mediumTier));
            return null;
        }
        var builder = RecipeBuilder.ofRaw().duration(20);
        var power = getGeneratorPower();
        if (power != 0) {
            builder.EUt(-power);
        }
        var rod = connectingRodHatch != null ? connectingRodHatch.getRodMaterial() : null;
        if (rod != null) {
            builder.inputItems(ItemIngredient.of(ChemicalHelper.get(GTOTagPrefix.CONNECTING_ROD, rod).getItem()), 1);
        }
        return builder.build();
    }

    @Override
    public Reference2IntMap<TierDataKey> getCasingTiers() {
        return tierCasingTrait.getCasingTiers();
    }

    @Override
    public boolean isGenerator() {
        return true;
    }

    @Override
    public void customText(@NotNull List<Component> textList) {
        super.customText(textList);
        st:
        if (tissue != null) {
            textList.add(Component.translatable(BioOscillationElectricStimulator.CURRENT_TISSUE, tissue.getDescription()));
            var nextStagePoints = 0;
            var tissueData = TISSUE_MATERIALS_TIER.get(tissue);
            if (tissueData != null) {
                switch (getTissueStage()) {
                    case JUVENILE -> nextStagePoints = tissueData.stage1Points();
                    case ADULT -> nextStagePoints = tissueData.stage2Points();
                    case MATURE -> nextStagePoints = tissueData.stage3Points();
                    case ELDERLY -> nextStagePoints = tissueData.stage4Points();
                    case null -> {
                        break st;
                    }
                }
            }
            textList.add(Component.translatable(BioOscillationElectricStimulator.CURRENT_TISSUE_STAGE, getTissueStage().getStageComponent(), FormattingUtil.formatNumbers((int) tissuePoints), FormattingUtil.formatNumbers(nextStagePoints)));
        }
        if (mediumMaterial != null) {
            textList.add(Component.translatable(BioOscillationElectricStimulator.CURRENT_MEDIUM_NUTRIENT_AVAILABILITY, (FormattingUtil.formatNumber2Places(mediumUsage / 10f))));
        }
        textList.add(Component.translatable(BioOscillationElectricStimulator.MEDIUM_NUTRIENT_AVAILABILITY_THRESHOLD, mediumUsageThreshold / 10f).append(ComponentPanelWidget.withButton(Component.literal(" [-]"), "Sub")).append(ComponentPanelWidget.withButton(Component.literal(" [+]"), "Add")));
        if (remainingBoostTicks > 0 && remainingBoostFactor > 0) {
            textList.add(Component.translatable(BioOscillationElectricStimulator.ELECTRICAL_STIMULATION, remainingBoostFactor, remainingBoostTicks));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            var amount = clickData.isCtrlClick ? 100 : (clickData.isShiftClick ? 10 : 1);
            mediumUsageThreshold = (short) Mth.clamp(mediumUsageThreshold + ("Add".equals(componentData) ? amount : -amount), 0, 1000);
        }
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(GuiTextures.BUTTON_VOID, GuiTextures.BUTTON_VOID, () -> true, (clickData, pressed) -> {
            if (!clickData.isRemote && getRecipeLogic().isWorking() && configuratorPanel.getGui() != null && configuratorPanel.getGui().entityPlayer instanceof ServerPlayer) {
                clearMedium();
                clearTissue();
            }
        }).setTooltipsSupplier(pressed -> List.of(Component.translatable(BioOscillationElectricStimulator.BUTTON_CLEAR_ALL))));
    }

    @UtilityClass
    public static class BioOscillationGeneratorData {

        public final ImmutableMap<Material, Integer> CONNECTING_ROD_DURABILITY = ImmutableMap.<Material, Integer>builder()
                .put(SamariumMagnetic, 1350)
                .put(EnergeticNetherite, 5400)
                .put(AttunedTengam, 21600)
                .put(Magmatter, 86400)
                .build();
        public final ImmutableMap<Material, Integer> CONNECTING_ROD_TIER = ImmutableMap.<Material, Integer>builder()
                .put(SamariumMagnetic, 1)
                .put(EnergeticNetherite, 2)
                .put(AttunedTengam, 3)
                .put(Magmatter, 4)
                .build();
        public static final ImmutableMap<Material, MediumData> MEDIUM_MATERIALS_TIER = ImmutableMap.<Material, MediumData>builder()
                .put(BiomediumRaw, new MediumData(1, Biomass))
                .put(EssenceMediumRaw, new MediumData(2, BiomediumRaw))
                .put(DragonSoulMediumRaw, new MediumData(3, EnrichedDragonBreath))
                .put(WyvernInfusionSolvent, new MediumData(4, DragonElement))
                .put(DraconicInfusionSolvent, new MediumData(5, WyvernInfusionSolvent))
                .build();
    }

    public record TissueData(int[] pointStages, int maxGrowRate, float saturationFactor, float[] electrodeStimulationFactors,
                             long energyConsumption, int tier, int vTier) {

        public int stage1Points() {
            return pointStages[0];
        }

        public int stage2Points() {
            return pointStages[0] + pointStages[1];
        }

        public int stage3Points() {
            return pointStages[0] + pointStages[1] + pointStages[2];
        }

        public int stage4Points() {
            return pointStages[0] + pointStages[1] + pointStages[2] + pointStages[3];
        }
    }

    public record MediumData(int tier, Material wasteMaterial) {}

    enum Stage {

        JUVENILE(BioOscillationElectricStimulator.TISSUE_STAGE_JUVENILE),
        ADULT(BioOscillationElectricStimulator.TISSUE_STAGE_ADULT),
        MATURE(BioOscillationElectricStimulator.TISSUE_STAGE_MATURE),
        ELDERLY(BioOscillationElectricStimulator.TISSUE_STAGE_ELDERLY),
        ;

        private final String tissueStage;

        Stage(String tissueStageElderly) {
            this.tissueStage = tissueStageElderly;
        }

        Component getStageComponent() {
            return Component.translatable(tissueStage);
        }
    }
}
