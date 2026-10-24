package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.machine.electric.beam.BeamAccessPartMachine;

import com.gtolib.api.beam.BeamSignal;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

public class LaserComputationTestingPlatformMachine extends ElectricMultiblockMachine implements IResearchPointsOperation {

    private static final long MIN_BEAM_INTENSITY = 64L;
    private static final double LOG_4 = Math.log(4);

    public LaserComputationTestingPlatformMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        long intensity = getMatchingIntensity();
        if (intensity >= MIN_BEAM_INTENSITY) {
            addResearchData(ResearchTag.OPTICS, Math.log(intensity) / LOG_4 - 1);
        }
    }

    private long getMatchingIntensity() {
        double intensity = 0;
        for (var part : getParts(BeamAccessPartMachine.class)) {
            intensity += part.averageReceivedPower(749, 751, Double.NaN);
        }
        return BeamSignal.toLong(intensity);
    }
}
