package com.gtocore.common.cover;

import com.gtolib.api.machine.feature.IConfigurablePowerAmplifierMachine;
import com.gtolib.api.machine.feature.IPowerAmplifierMachine;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.PercentField;
import com.gregtechceu.gtceu.uiwidgets.cover.CoverUIs;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public final class CreativePowerAmplifierCover extends CoverBehavior implements IUICover {

    public static final double DEFAULT_DURATION_MULTIPLIER = 0.01D;
    public static final double DEFAULT_ENERGY_MULTIPLIER = 1D;
    public static final double MIN_DURATION_MULTIPLIER = 0.0001D;
    public static final double MIN_ENERGY_MULTIPLIER = 0.01D;
    public static final double MAX_MULTIPLIER = 100D;

    @SaveToDisk(defaultValue = "0.01")
    private double durationMultiplier = DEFAULT_DURATION_MULTIPLIER;
    @SaveToDisk(defaultValue = "1.0")
    private double energyMultiplier = DEFAULT_ENERGY_MULTIPLIER;

    public CreativePowerAmplifierCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && getMachine() instanceof IConfigurablePowerAmplifierMachine powerAmplifierMachine && powerAmplifierMachine.gtolib$noPowerAmplifier();
    }

    @Override
    public void onAttached(@NotNull ItemStack itemStack, @NotNull ServerPlayer player) {
        super.onAttached(itemStack, player);
        updateCoverSub();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateCoverSub();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        MetaMachine machine = getMachine();
        if (machine instanceof IPowerAmplifierMachine amplifierMachine) {
            amplifierMachine.gtolib$setHasPowerAmplifier(false);
            amplifierMachine.gtolib$setPowerAmplifier(1);
        }
    }

    @Override
    public Widget createUIWidget() {
        var duration = new PercentField(LayoutStyle.AUTO, () -> durationMultiplier, this::setDurationMultiplier,
                () -> MIN_DURATION_MULTIPLIER, () -> MAX_MULTIPLIER, MIN_DURATION_MULTIPLIER, 100, 1000, 10000);
        var energy = new PercentField(LayoutStyle.AUTO, () -> energyMultiplier, this::setEnergyMultiplier,
                () -> MIN_ENERGY_MULTIPLIER, () -> MAX_MULTIPLIER, PercentField.DEFAULT_STEP, 1, 10, 100);
        return CoverUIs.page().addChild(UIElement.section().addChildren(
                CoverUIs.numberRow("gtocore.cover.creative_power_amplifier.duration", duration),
                CoverUIs.numberRow("gtocore.cover.creative_power_amplifier.energy", energy)));
    }

    private void setDurationMultiplier(double durationMultiplier) {
        this.durationMultiplier = clamp(durationMultiplier, MIN_DURATION_MULTIPLIER, MAX_MULTIPLIER);
        updatePowerAmplifier();
    }

    private void setEnergyMultiplier(double energyMultiplier) {
        this.energyMultiplier = clamp(energyMultiplier, MIN_ENERGY_MULTIPLIER, MAX_MULTIPLIER);
        updatePowerAmplifier();
    }

    private void updateCoverSub() {
        if (coverHolder.getLevel() instanceof ServerLevel level) {
            TaskHandler.enqueueTask(level, () -> {
                clampMultipliers();
                MetaMachine machine = getMachine();
                if (machine instanceof IConfigurablePowerAmplifierMachine amplifierMachine && amplifierMachine.gtolib$noPowerAmplifier()) {
                    amplifierMachine.gtolib$setHasPowerAmplifier(true);
                    amplifierMachine.gtolib$setPowerAmplifier(durationMultiplier, energyMultiplier);
                }
            });
        }
    }

    private void updatePowerAmplifier() {
        clampMultipliers();
        MetaMachine machine = getMachine();
        if (machine instanceof IConfigurablePowerAmplifierMachine amplifierMachine) {
            amplifierMachine.gtolib$setHasPowerAmplifier(true);
            amplifierMachine.gtolib$setPowerAmplifier(durationMultiplier, energyMultiplier);
        }
        markAsDirty();
    }

    private void clampMultipliers() {
        durationMultiplier = clamp(durationMultiplier, MIN_DURATION_MULTIPLIER, MAX_MULTIPLIER);
        energyMultiplier = clamp(energyMultiplier, MIN_ENERGY_MULTIPLIER, MAX_MULTIPLIER);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Nullable
    private MetaMachine getMachine() {
        return MetaMachine.getMachine(coverHolder.holder());
    }
}
