package com.gtocore.common.machine.multiblock.water;

import com.gtocore.common.data.GTOMaterials;
import com.gtocore.data.IdleReason;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.utils.ClientUtil;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.issue.IssueLines;
import com.gregtechceu.gtceu.api.machine.issue.IssueStage;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.uiwidgets.multiblock.ControlPanel;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.material.Fluid;

import it.unimi.dsi.fastutil.objects.Object2BooleanRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanSortedMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class WaterPurificationPlantMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder, IDataInfoProvider {

    @RegisterLanguage(cn = "链接范围", en = "Link Range")
    private static final String LINK_RANGE = "gtocore.machine.water_purification_plant.link_range";
    @RegisterLanguage(cn = "在世界中显示可自动链接净化单元控制器的范围（半径 32 格）", en = "Shows in the world the range within which purification unit controllers are linked automatically (radius 32 blocks)")
    private static final String LINK_RANGE_TOOLTIP = "gtocore.machine.water_purification_plant.link_range.tooltip";
    @RegisterLanguage(cn = "成功率", en = "Success Chance")
    static final String SUCCESS_CHANCE = "gtocore.machine.water_purification_unit.success_chance.label";

    static final int DURATION = 2400;

    static final Fluid GradePurifiedWater1 = GTOMaterials.FilteredSater.getFluid();
    static final Fluid GradePurifiedWater2 = GTOMaterials.OzoneWater.getFluid();
    static final Fluid GradePurifiedWater3 = GTOMaterials.FlocculentWater.getFluid();
    static final Fluid GradePurifiedWater4 = GTOMaterials.PHNeutralWater.getFluid();
    static final Fluid GradePurifiedWater5 = GTOMaterials.ExtremeTemperatureWater.getFluid();
    static final Fluid GradePurifiedWater6 = GTOMaterials.ElectricEquilibriumWater.getFluid();
    static final Fluid GradePurifiedWater7 = GTOMaterials.DegassedWater.getFluid();
    static final Fluid GradePurifiedWater8 = GTOMaterials.BaryonicPerfectionWater.getFluid();
    public static final Fluid[] GradePurifiedWater = { GradePurifiedWater1, GradePurifiedWater2, GradePurifiedWater3, GradePurifiedWater4, GradePurifiedWater5, GradePurifiedWater6, GradePurifiedWater7, GradePurifiedWater8 };

    long availableEu;

    final Object2BooleanSortedMap<WaterPurificationUnitMachine> waterPurificationUnitMachineMap = new Object2BooleanRBTreeMap<>(Comparator.comparingLong(a -> -a.multiple));

    public WaterPurificationPlantMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean supportLockRecipe() {
        return false;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        IIWirelessInteractor.addToNet(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        IIWirelessInteractor.removeFromNet(this);
    }

    @Override
    public void onStructureInvalid() {
        IIWirelessInteractor.removeFromNet(this);
        for (var entry : waterPurificationUnitMachineMap.object2BooleanEntrySet()) {
            if (entry.getBooleanValue()) {
                entry.getKey().getRecipeLogic().resetRecipeLogic();
                entry.setValue(false);
            }
        }
        super.onStructureInvalid();
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        for (var entry : waterPurificationUnitMachineMap.object2BooleanEntrySet()) {
            if (entry.getBooleanValue() && entry.getKey().getRecipeLogic().getLastRecipe() != null) {
                entry.getKey().getRecipeLogic().onRecipeFinish();
                entry.setValue(false);
            }
        }
    }

    @Override
    public void onWorking() {
        for (var entry : waterPurificationUnitMachineMap.object2BooleanEntrySet()) {
            if (entry.getBooleanValue()) {
                entry.getKey().onWorking();
                entry.getKey().getRecipeLogic().setProgress(getRecipeLogic().getProgress());
            }
        }
        super.onWorking();
    }

    @Override
    public void onWaiting() {
        for (var entry : waterPurificationUnitMachineMap.object2BooleanEntrySet()) {
            if (entry.getBooleanValue()) {
                entry.getKey().getRecipeLogic().setWaiting(IdleReason.PLANT_WAITING.type(), 0, 0);
            }
        }
        super.onWaiting();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        for (var entry : waterPurificationUnitMachineMap.object2BooleanEntrySet()) {
            var machine = entry.getKey();
            machine.setWorking(isWorkingAllowed);
            entry.setValue(machine.getRecipeLogic().isWorking());
        }
        super.setWorkingEnabled(isWorkingAllowed);
    }

    @Override
    public void beforeWorking(RecipeHandlerUnit unit, GTRecipe recipe) {
        for (var entry : waterPurificationUnitMachineMap.object2BooleanEntrySet()) {
            var m = entry.getKey();
            if (entry.getBooleanValue() && m.recipe != null && m.unit != null) {
                var l = m.getRecipeLogic();
                l.resetRecipeLogic();
                if (!l.isSuspend() && m.isRecipeLogicAvailable()) l.setupRecipe(m.unit, m.recipe);
            }
        }
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        if (getRecipeLogic().getProgress() == 0) return 0;
        return 15 * getRecipeLogic().getProgress() / DURATION;
    }

    @Override
    public void addControls(ControlPanel controls) {
        super.addControls(controls);
        controls.addClientButton(LINK_RANGE, "gtocore.digital_miner.show_range", () -> ClientUtil.highlighting(getPos(), 32), LINK_RANGE_TOOLTIP);
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed()) return;
        textList.add(Component.translatable("gtocore.machine.water_purification_plant.bind"));
        for (var entry : waterPurificationUnitMachineMap.object2BooleanEntrySet()) {
            MutableComponent component = Component.translatable(entry.getKey().getBlockState().getBlock().getDescriptionId()).append(" ");
            if (entry.getBooleanValue()) {
                component.append(Component.translatable("gtceu.multiblock.running").append("\n").append(Component.translatable("gtceu.multiblock.energy_consumption", FormattingUtil.formatNumbers(entry.getKey().eut), Component.literal(GTValues.VNF[GTUtil.getTierByVoltage(entry.getKey().eut)]))));
            } else {
                component.append(IssueLines.headline(entry.getKey().getRecipeLogic().getIssueSnapshot()));
            }
            textList.add(component);
        }
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (!isFormed() || waterPurificationUnitMachineMap.isEmpty() || (mode != PortableScannerBehavior.DisplayMode.SHOW_ALL && mode != PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO)) {
            return Collections.emptyList();
        }
        List<Component> list = new ArrayList<>(waterPurificationUnitMachineMap.size() + 1);
        list.add(Component.translatable("gtocore.machine.water_purification_plant.bind"));
        for (var entry : waterPurificationUnitMachineMap.object2BooleanEntrySet()) {
            var machine = entry.getKey();
            MutableComponent component = Component.translatable(machine.getBlockState().getBlock().getDescriptionId()).append(" ");
            if (entry.getBooleanValue()) {
                component.append(WaterPurificationUnitMachine.successChanceText(machine.getSuccessChance()));
            } else {
                component.append(Component.translatable("gtceu.multiblock.idling"));
            }
            list.add(component);
        }
        return list;
    }

    @Override
    @Nullable
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        long eut = 0;
        long stored = getEnergyContainer().getEnergyStored();
        if (stored < 1000) {
            IdleReason.INSUFFICIENT_ENERGY_BUFFER.report(this, 1000, stored);
            return null;
        }
        availableEu = getOverclockVoltage();
        for (var it = waterPurificationUnitMachineMap.object2BooleanEntrySet().iterator(); it.hasNext();) {
            var entry = it.next();
            entry.setValue(false);
            var machine = entry.getKey();
            if (machine.isFormed() && !machine.isRemoved()) {
                var logic = machine.getRecipeLogic();
                if (logic.isIdle()) {
                    logic.beginIssueRound(IssueStage.SEARCH);
                    try {
                        for (var u : machine.getInputUnits()) {
                            long eu = machine.prepareRecipe(u);
                            if (eu > 0) {
                                entry.setValue(true);
                                machine.unit = u;
                                availableEu -= eu;
                                eut += eu;
                                break;
                            }
                        }
                    } finally {
                        logic.endIssueRound();
                    }
                }
            } else {
                it.remove();
            }
        }
        if (eut > 0) {
            return getRecipeBuilder().duration(DURATION).EUt(eut).build();
        }
        return null;
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }
}
