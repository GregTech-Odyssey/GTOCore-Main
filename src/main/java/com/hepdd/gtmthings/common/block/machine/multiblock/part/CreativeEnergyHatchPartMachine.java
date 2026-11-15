package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.tooltip.ElectricText;
import com.gregtechceu.gtceu.common.data.GTTickTimeMonitors;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.Stepper;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.suried.gtoutils.schedule.Task;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CreativeEnergyHatchPartMachine extends WorkableTieredIOPartMachine implements IDataInfoProvider, IFancyUIMachine {

    @SaveToDisk
    public final NotifiableEnergyContainer energyContainer;
    @Nullable
    protected ISubscription energyListener;
    protected Task energySubs;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor energyMonitor = holder.monitorTick(GTTickTimeMonitors.ENERGY_TRANSFER, this::addEng);
    @SaveToDisk
    private long maxEnergy;
    @SaveToDisk
    private long voltage = 0;
    @SaveToDisk
    @Getter
    private int amps = 1;
    @SaveToDisk
    private int setTier = 0;

    public CreativeEnergyHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        this.energyContainer = createEnergyContainer();
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected NotifiableEnergyContainer createEnergyContainer() {
        NotifiableEnergyContainer container;
        this.voltage = GTValues.VEX[setTier];
        this.maxEnergy = this.voltage * this.amps;
        container = new InfinityEnergyContainer(this, this.maxEnergy, this.voltage, this.amps, 0L, 0L);
        return container;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (energyListener != null) {
            energyListener.unsubscribe();
            energyListener = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateEnergyContainer();
        energyListener = energyContainer.addChangedListener(this::addEnergySubscription);
        addEnergySubscription();
    }

    protected void addEnergySubscription() {
        energySubs = subscribeServerTick(energySubs, energyMonitor);
    }

    protected void addEng() {
        if (energyContainer.getInputVoltage() != voltage || energyContainer.getInputAmperage() != amps) {
            updateEnergyContainer();
        }
        if (energyContainer.getEnergyStored() < this.maxEnergy) {
            energyContainer.setEnergyStored(this.maxEnergy);
        }
    }

    @Override
    public Widget createUIWidget() {
        return Form.page().addChildren(
                Form.controlRow(Component.translatable("gtceu.creative.energy.tier"),
                        Stepper.of(50, () -> setTier, tier -> {
                            setTier = tier;
                            setVoltage(GTValues.VEX[setTier]);
                        }, 0, GTValues.VNF.length - 1).setFormatter(idx -> GTValues.VNF[idx])),
                Form.numberRow(Component.translatable("gtceu.creative.energy.voltage"),
                        NumberField.ofLong(UISizes.CONTENT_WIDTH, () -> voltage, v -> {
                            setVoltage(v);
                            setTier = GTUtil.getTierByVoltage(this.voltage);
                        }, 8L, Long.MAX_VALUE)),
                Form.numberRow(Component.translatable("gtceu.creative.energy.amperage"),
                        NumberField.ofInt(UISizes.CONTENT_WIDTH, () -> amps, this::setAmps, 1, 67108864)));
    }

    private void setVoltage(long voltage) {
        this.voltage = voltage;
        this.maxEnergy = this.voltage * this.amps;
        updateMachine();
    }

    private void setAmps(int amps) {
        this.amps = amps;
        this.maxEnergy = this.voltage * this.amps;
        updateMachine();
    }

    private void updateEnergyContainer() {
        this.energyContainer.resetBasicInfo(this.maxEnergy, this.voltage, this.amps, 0, 0);
        this.energyContainer.setEnergyStored(this.maxEnergy);
    }

    private void updateMachine() {
        updateEnergyContainer();
        getControllers().forEach(IMultiController::requestCheck);
    }

    //////////////////////////////////////
    // ********** Misc **********//
    //////////////////////////////////////

    @Override
    public int tintColor(int index) {
        if (index == 2) {
            return GTValues.VC[getTier()];
        }
        return super.tintColor(index);
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return Collections.singletonList(ElectricText.chargeExact(energyContainer.getEnergyStored(), energyContainer.getEnergyCapacity()));
        }
        return new ArrayList<>();
    }

    private static class InfinityEnergyContainer extends NotifiableEnergyContainer {

        public InfinityEnergyContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
            super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
        }

        @Override
        public long changeEnergy(long energyToAdd) {
            long oldEnergyStored = getEnergyStored();
            long newEnergyStored = (getEnergyCapacity() - oldEnergyStored < energyToAdd) ? getEnergyCapacity() : (oldEnergyStored + energyToAdd);
            if (newEnergyStored < 0) newEnergyStored = 0;
            return newEnergyStored - oldEnergyStored;
        }

        @Override
        public void checkOutputSubscription() {}

        @Override
        public void serverTick() {}

        @Override
        public long acceptEnergyFromNetwork(Object o, Direction side, long voltage, long amperage) {
            return 0;
        }

        @Override
        public boolean outputsEnergy(Direction side) {
            return false;
        }

        @Override
        public boolean inputsEnergy(Direction side) {
            return false;
        }
    }
}
