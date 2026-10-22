package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.beam.BeamManager;
import com.gtolib.api.beam.IBeamEnergySource;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.Direction;

public class CrystalExciterMachine extends SimpleTieredMachine implements IBeamEnergySource {

    public CrystalExciterMachine(MetaMachineBlockEntity holder, int tier) {
        super(holder, tier, t -> 0);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        var level = getLevel();
        if (!isRemote() && level != null) {
            TaskHandler.enqueueTask(level, () -> {
                for (Direction direction : Direction.values()) {
                    var pos = getPos().relative(direction);
                    BeamManager.requestRebuildAt(level, pos);
                }
            }, 1);
        }
    }

    @Override
    public long availableBeamEnergy() {
        var energy = getEnergyContainer();
        double input = (double) energy.getInputAmperage() * energy.getInputVoltage() * BeamManager.STEP_TICKS;
        return Math.min(energy.getEnergyStored(), com.gtolib.api.beam.BeamSignal.toLong(input));
    }

    @Override
    public void consumeBeamEnergy(long amount) {
        if (getEnergyContainer().removeEnergy(amount) != amount) throw new IllegalStateException("Optical energy reservation changed during commit");
    }

    @Override
    public boolean hasAutoOutputItem() {
        return false;
    }

    @Override
    public boolean hasAutoOutputFluid() {
        return false;
    }
}
