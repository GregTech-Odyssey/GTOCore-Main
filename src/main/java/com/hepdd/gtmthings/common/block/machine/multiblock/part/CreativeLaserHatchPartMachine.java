package com.hepdd.gtmthings.common.block.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableTieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableLaserContainer;
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
public class CreativeLaserHatchPartMachine extends WorkableTieredIOPartMachine implements IDataInfoProvider, IFancyUIMachine {

    @SaveToDisk
    private final NotifiableLaserContainer buffer;
    @Nullable
    protected ISubscription LaserListener;
    protected Task explosionSubs;

    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private TickTimeMonitor energyMonitor = holder.monitorTick(GTTickTimeMonitors.ENERGY_TRANSFER, this::addEng);
    private Long maxEnergy;
    @SaveToDisk
    private long voltage = 0;
    @SaveToDisk
    @Getter
    private int amps = 256;
    @SaveToDisk
    private int setTier = GTValues.IV;

    public CreativeLaserHatchPartMachine(MetaMachineBlockEntity holder) {
        super(holder, GTValues.MAX, IO.IN);
        this.voltage = GTValues.VEX[setTier];
        this.maxEnergy = voltage * 64L * amps;
        this.buffer = NotifiableLaserContainer.receiverContainer(this, this.maxEnergy, voltage, amps);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (LaserListener != null) {
            LaserListener.unsubscribe();
            LaserListener = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateLaserContainer();
        LaserListener = buffer.addChangedListener(this::AddEngerySubscription);
        AddEngerySubscription();
    }

    protected void AddEngerySubscription() {
        explosionSubs = subscribeServerTick(explosionSubs, energyMonitor);
    }

    protected void addEng() {
        if (buffer.getInputVoltage() != voltage || buffer.getInputAmperage() != amps) {
            maxEnergy = voltage * 64L * amps;
            buffer.resetBasicInfo(maxEnergy, voltage, amps, 0, 0);
            buffer.setEnergyStored(0);
        }
        if (buffer.getEnergyStored() < this.maxEnergy) {
            buffer.setEnergyStored(this.maxEnergy);
        }
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public Widget createUIWidget() {
        return Form.page().addChildren(
                Form.controlRow(Component.translatable("gtceu.creative.energy.tier"),
                        Stepper.of(50, () -> Math.max(0, setTier - GTValues.IV), idx -> {
                            setTier = idx + GTValues.IV;
                            setVoltage(GTValues.VEX[setTier]);
                        }, 0, GTValues.VNF.length - 1 - GTValues.IV).setFormatter(idx -> GTValues.VNF[idx + GTValues.IV])),
                Form.numberRow(Component.translatable("gtceu.creative.energy.voltage"),
                        NumberField.ofLong(UISizes.CONTENT_WIDTH, () -> voltage, v -> {
                            setVoltage(v);
                            setTier = GTUtil.getTierByVoltage(this.voltage);
                        }, GTValues.VEX[GTValues.IV], Long.MAX_VALUE)),
                Form.numberRow(Component.translatable("gtceu.creative.energy.amperage"),
                        NumberField.ofInt(UISizes.CONTENT_WIDTH, () -> amps, this::setAmps, 256, 67108864)));
    }

    private void setVoltage(long voltage) {
        this.voltage = voltage;
        this.maxEnergy = this.voltage * 64L * this.amps;
        updateMachine();
    }

    private void setAmps(int amps) {
        this.amps = amps;
        this.maxEnergy = this.voltage * 64L * this.amps;
        updateMachine();
    }

    private void updateLaserContainer() {
        this.buffer.resetBasicInfo(this.maxEnergy, this.voltage, this.amps, 0, 0);
        this.buffer.setEnergyStored(this.maxEnergy);
    }

    private void updateMachine() {
        updateLaserContainer();
        getControllers().forEach(IMultiController::requestCheck);
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL || mode == PortableScannerBehavior.DisplayMode.SHOW_ELECTRICAL_INFO) {
            return Collections.singletonList(ElectricText.chargeExact(buffer.getEnergyStored(), buffer.getEnergyCapacity()));
        }
        return new ArrayList<>();
    }
}
