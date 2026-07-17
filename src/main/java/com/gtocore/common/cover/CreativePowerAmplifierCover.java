package com.gtocore.common.cover;

import com.gtolib.api.machine.feature.IConfigurablePowerAmplifierMachine;
import com.gtolib.api.machine.feature.IPowerAmplifierMachine;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.widget.FloatInputWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public final class CreativePowerAmplifierCover extends CoverBehavior implements IUICover {

    public static final double DEFAULT_DURATION_MULTIPLIER = 0.01D;
    public static final double DEFAULT_ENERGY_MULTIPLIER = 1D;
    public static final double MIN_DURATION_MULTIPLIER = 0.0001D;
    public static final double MIN_ENERGY_MULTIPLIER = 0.01D;
    public static final double MAX_MULTIPLIER = 100D;

    @SaveToDisk
    private double durationMultiplier = DEFAULT_DURATION_MULTIPLIER;
    @SaveToDisk
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
        WidgetGroup group = new WidgetGroup(0, 0, 176, 75);
        group.addWidget(new LabelWidget(10, 5, "gtocore.cover.creative_power_amplifier.title"));
        group.addWidget(new LabelWidget(10, 22, "gtocore.cover.creative_power_amplifier.duration"));
        group.addWidget(new FloatInputWidget(76, 18, 90, 20, this::getDurationMultiplierInput, this::setDurationMultiplier)
                .setMin((float) MIN_DURATION_MULTIPLIER)
                .setMax((float) MAX_MULTIPLIER));
        group.addWidget(new LabelWidget(10, 50, "gtocore.cover.creative_power_amplifier.energy"));
        group.addWidget(new FloatInputWidget(76, 46, 90, 20, this::getEnergyMultiplierInput, this::setEnergyMultiplier)
                .setMin((float) MIN_ENERGY_MULTIPLIER)
                .setMax((float) MAX_MULTIPLIER));
        return group;
    }

    private float getDurationMultiplierInput() {
        return (float) durationMultiplier;
    }

    private float getEnergyMultiplierInput() {
        return (float) energyMultiplier;
    }

    private void setDurationMultiplier(float durationMultiplier) {
        this.durationMultiplier = clamp(durationMultiplier, MIN_DURATION_MULTIPLIER, MAX_MULTIPLIER);
        updatePowerAmplifier();
    }

    private void setEnergyMultiplier(float energyMultiplier) {
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
