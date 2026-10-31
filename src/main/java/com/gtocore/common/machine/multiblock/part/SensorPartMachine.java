package com.gtocore.common.machine.multiblock.part;

import com.gtocore.common.data.GTOMachines;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.uiwidgets.display.MachineDisplay;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@DataGeneratorScanned
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SensorPartMachine extends MultiblockPartMachine {

    private static final double RANGE = 1.0E9;
    @RegisterLanguage(cn = "反转红石输出", en = "Invert redstone output")
    private static final String INVERT = "gtocore.machine.sensor.invert";
    @RegisterLanguage(cn = "开启后，读数在范围内时输出减弱、范围外输出 15", en = "When on, the signal weakens inside the range and is 15 outside it")
    private static final String INVERT_TIP = "gtocore.machine.sensor.invert.tip";
    @RegisterLanguage(cn = "最小值", en = "Minimum")
    private static final String MIN = "gtocore.machine.sensor.min";
    @RegisterLanguage(cn = "最大值", en = "Maximum")
    private static final String MAX = "gtocore.machine.sensor.max";
    @RegisterLanguage(cn = "最小值（MeV）", en = "Minimum (MeV)")
    private static final String MIN_MEV = "gtocore.machine.sensor.min_mev";
    @RegisterLanguage(cn = "最大值（MeV）", en = "Maximum (MeV)")
    private static final String MAX_MEV = "gtocore.machine.sensor.max_mev";
    @RegisterLanguage(cn = "红石输出：%s", en = "Redstone output: %s")
    private static final String OUTPUT = "gtocore.machine.sensor.output";

    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private float min;
    @SaveToDisk(defaultValue = "0")
    @SyncToClient
    private float max;
    @SaveToDisk(defaultValue = "false")
    private boolean isInverted;
    @Getter
    @SaveToDisk(defaultValue = "0")
    private int redstoneSignalOutput;

    public SensorPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public Widget createUIWidget() {
        boolean neutron = getHolder().getBlockState().is(GTOMachines.NEUTRON_SENSOR.get());
        return MachineDisplay.page(this, this::addDisplayText, controls -> {
            controls.addToggle(INVERT, () -> isInverted, value -> isInverted = value, INVERT_TIP);
            controls.addDecimal(neutron ? MIN_MEV : MIN, () -> min, value -> min = (float) value, -RANGE, RANGE, 0.01);
            controls.addDecimal(neutron ? MAX_MEV : MAX, () -> max, value -> max = (float) value, -RANGE, RANGE, 0.01);
        });
    }

    private void addDisplayText(List<Component> textList) {
        textList.add(Component.translatable(OUTPUT, redstoneSignalOutput));
    }

    public void update(float a) {
        int output = computeRedstoneBetweenValues(a, max, min, isInverted);
        if (redstoneSignalOutput != output) {
            redstoneSignalOutput = output;
            updateSignal();
        }
    }

    private static int computeRedstoneBetweenValues(float value, float maxValue, float minValue, boolean isInverted) {
        float lower = Math.min(minValue, maxValue);
        float upper = Math.max(minValue, maxValue);
        if (value < lower) {
            return isInverted ? 15 : 0;
        }
        if (value > upper) {
            return isInverted ? 15 : 0;
        }
        if (Float.compare(lower, upper) == 0) {
            return isInverted ? 0 : 15;
        }
        float normalized = isInverted ? (upper - value) / (upper - lower) : (value - lower) / (upper - lower);
        return Math.clamp((int) Math.ceil(15 * normalized), 0, 15);
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        if (side == getFrontFacing().getOpposite()) {
            return redstoneSignalOutput;
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return side == getFrontFacing();
    }

    @Override
    public boolean canShared() {
        return false;
    }

    private float getMin() {
        return this.min;
    }

    private float getMax() {
        return this.max;
    }

    private boolean isInverted() {
        return this.isInverted;
    }

    private void setMin(final float min) {
        this.min = min;
    }

    private void setMax(final float max) {
        this.max = max;
    }

    private void setInverted(final boolean isInverted) {
        this.isInverted = isInverted;
    }
}
