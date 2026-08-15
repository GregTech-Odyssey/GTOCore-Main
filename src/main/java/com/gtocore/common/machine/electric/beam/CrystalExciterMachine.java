package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.beam.BeamManager;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.Direction;

public class CrystalExciterMachine extends SimpleTieredMachine {

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

    public boolean tryConsumeRayEnergy(long intensity) {
        long cost = energyCost(intensity);
        if (cost <= 0) return true;
        NotifiableEnergyContainer energy = getEnergyContainer();
        return energy.getInputAmperage() * energy.getInputVoltage() >= cost && energy.getEnergyStored() >= cost && energy.removeEnergy(cost) == cost;
    }

    private static long energyCost(long intensity) {
        return intensity > (Long.MAX_VALUE >> 12) ? Long.MAX_VALUE : Math.max(0L, intensity << 12);
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
