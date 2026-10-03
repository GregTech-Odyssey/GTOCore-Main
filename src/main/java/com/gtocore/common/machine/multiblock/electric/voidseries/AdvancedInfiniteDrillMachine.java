package com.gtocore.common.machine.multiblock.electric.voidseries;

import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.machine.trait.AdvancedInfiniteDrillLogic;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.multiblock.StorageMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uiwidgets.multiblock.MultiblockPage;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEFluidKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import lombok.Getter;

import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class AdvancedInfiniteDrillMachine extends StorageMultiblockMachine {

    @RegisterLanguage(cn = "钻头", en = "Drill Head")
    private static final String SLOT_LABEL = "gtocore.machine.advanced_infinite_driller.slot";

    private static final AEFluidKey DISTILLED_WATER = AEFluidKey.of(GTMaterials.DistilledWater.getFluid());
    private static final AEFluidKey OXYGEN = AEFluidKey.of(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID));
    private static final AEFluidKey HELIUM = AEFluidKey.of(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID));
    private static final long COOLANT_AMOUNT = 20000;
    private static final Map<Material, Integer> HEAT_MAP = Map.of(GTOMaterials.Neutron, 1);

    public static final int RUNNING_HEAT = 2000;
    private static final int MAX_HEAT = 10000;

    @RegisterLanguage(cn = "工作范围", en = "Working Area")
    private static final String WORKING_AREA = "gtocore.machine.advanced_infinite_driller.working_area";
    @RegisterLanguage(cn = "当前温度", en = "Current Temperature")
    private static final String CURRENT_HEAT = "gtocore.machine.advanced_infinite_driller.current_heat";
    private static final Component WORKING_AREA_TEXT = Component.literal("5x5");
    @Getter
    @SaveToDisk(defaultValue = "300")
    private int currentHeat = 300;
    @SaveToDisk(defaultValue = "0")
    private int process;
    private final ConditionalSubscriptionHandler heatSubs;

    public AdvancedInfiniteDrillMachine(MetaMachineBlockEntity holder) {
        super(holder, 1, i -> ChemicalHelper.getPrefix(i.getItem()) == TagPrefix.toolHeadDrill);
        heatSubs = new ConditionalSubscriptionHandler(this, this::heatUpdate, 5, this::isFormed);
    }

    @Override
    public RecipeLogic createRecipeLogic(Object... args) {
        return new AdvancedInfiniteDrillLogic(this, 5);
    }

    private void heatUpdate() {
        heatSubs.updateSubscription();

        boolean isWorking = getRecipeLogic().isWorking();
        int playerWantsToHeat = !isEmpty() ? inputBlast() : 0;
        boolean heatedByPlayer = playerWantsToHeat > 0;

        if (heatedByPlayer && currentHeat < MAX_HEAT) {
            playerWantsToHeat = Math.min(playerWantsToHeat, MAX_HEAT - currentHeat);
            currentHeat += playerWantsToHeat;
        }

        if (isWorking && process <= 0) {
            currentHeat += (int) Math.floor(Math.abs(currentHeat - RUNNING_HEAT) / 2000.0);
        }

        if (isWorking) {
            if (inputFluid(DISTILLED_WATER, COOLANT_AMOUNT)) {
                currentHeat--;
            } else if (inputFluid(OXYGEN, COOLANT_AMOUNT)) {
                currentHeat -= 2;
            } else if (inputFluid(HELIUM, COOLANT_AMOUNT)) {
                currentHeat -= 4;
            }
        }

        if (!isWorking && !heatedByPlayer) {
            currentHeat = Math.max(300, currentHeat - 1);
        }

        currentHeat = Math.max(4, currentHeat);

        if (currentHeat > MAX_HEAT) {
            process++;
            if (process >= 200) {
                process = 0;
                currentHeat = 300;
                machineStorage.storage.setStackInSlot(0, ItemStack.EMPTY);
                getRecipeLogic().interruptRecipe(IdleReason.DRILL_HEAD_MISSING.reason());
            }
        } else if (process > 0) {
            process--;
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        heatSubs.initialize(getLevel());
    }

    @Override
    public AdvancedInfiniteDrillLogic getRecipeLogic() {
        return (AdvancedInfiniteDrillLogic) super.getRecipeLogic();
    }

    @Override
    public void customText(List<Component> textList) {
        super.customText(textList);
        if (isEmpty()) {
            textList.add(Component.translatable("gtocore.machine.advanced_infinite_driller.not_fluid_head").withStyle(ChatFormatting.RED));
        } else {
            boolean screen = MultiblockPage.isScreenText();
            if (!screen) textList.add(Component.translatable("gtceu.universal.tooltip.working_area", 5, 5));
            textList.add(Component.translatable("gtocore.machine.advanced_infinite_driller.heat", MAX_HEAT, RUNNING_HEAT));
            if (!screen) {
                textList.add(Component.translatable("gtocore.machine.current_temperature", currentHeat));
                textList.add(Component.translatable("gtocore.machine.fission_reactor.damaged", FormattingUtil.formatNumber2Places(process / 200.0F * 100)).append("%"));
            }
            var fluids = getRecipeLogic().getVeinFluids();
            if (!fluids.isEmpty()) {
                fluids.forEach((fluid, produced) -> {
                    Component fluidInfo = fluid.getFluidType().getDescription().copy().withStyle(ChatFormatting.GREEN);
                    Component amountInfo = Component.literal(FormattingUtil.formatNumbers(produced * getRate()) + " mB/s").withStyle(ChatFormatting.BLUE);
                    textList.add(Component.translatable("gtocore.machine.advanced_infinite_driller.drilled_fluid", fluidInfo, amountInfo));
                });
            } else {
                Component noFluid = Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area").withStyle(ChatFormatting.RED);
                textList.add(Component.translatable("gtceu.multiblock.fluid_rig.drilled_fluid", noFluid).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public void addScreenReadouts(MultiblockPage page) {
        super.addScreenReadouts(page);
        page.addLine(WORKING_AREA, () -> WORKING_AREA_TEXT);
        page.addLine(CURRENT_HEAT, MultiblockPage.numberText(() -> currentHeat, "K")).bindLevel(this::heatLevel);
        page.addReading("gtocore.machine.fission_reactor.damaged", MultiblockPage.cached(() -> process, value -> Component.literal(FormattingUtil.formatNumber2Places(value / 200.0F * 100) + "%")))
                .bindLevel(() -> process > 0 ? Level.WARNING : Level.NORMAL);
    }

    private Level heatLevel() {
        if (currentHeat > MAX_HEAT) return Level.ERROR;
        return currentHeat >= RUNNING_HEAT ? Level.GOOD : Level.WARNING;
    }

    public int getRate() {
        return (int) Math.max(1, (currentHeat - RUNNING_HEAT) * getDrillHeadTier() * 0.75);
    }

    private int getDrillHeadTier() {
        ItemStack itemStack = getStorageStack();
        if (!itemStack.isEmpty()) {
            MaterialStack ms = ChemicalHelper.getMaterialStack(itemStack);
            if (!ms.isEmpty()) {
                Material material = ms.material();
                Integer result = HEAT_MAP.get(material);
                if (result != null) return result;
            }
        }
        return 0;
    }

    private int inputBlast() {
        if (inputFluid(GTMaterials.Blaze.getFluid(), getFluidConsume())) return 1;
        if (inputFluid(GTOMaterials.BlazeCube.getFluid(), getFluidConsume())) return 1000;
        return 0;
    }

    private int getFluidConsume() {
        return (int) Math.pow(currentHeat, 1.3);
    }

    public boolean canRunnable() {
        return currentHeat >= RUNNING_HEAT;
    }

    @Override
    public String getStorageSlotLabel() {
        return SLOT_LABEL;
    }
}
