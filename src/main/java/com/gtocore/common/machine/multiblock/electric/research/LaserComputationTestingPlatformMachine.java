package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.recipe.condition.BeamCondition;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

public class LaserComputationTestingPlatformMachine extends ElectricMultiblockMachine implements IResearchPointsOperation {

    private static final long MIN_BEAM_INTENSITY = 64L;
    private static final double LOG_4 = Math.log(4);
    public static final BeamCondition BEAM_CONDITION = new BeamCondition(MIN_BEAM_INTENSITY, 749, 751);

    public LaserComputationTestingPlatformMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void addResearchData(ResearchTag tag, double data) {
        if (tag == ResearchTag.OPTICS) {
            long intensity = BEAM_CONDITION.getMatchingIntensity(this);
            if (intensity < MIN_BEAM_INTENSITY) return;
            data = Math.log(intensity) / LOG_4 - 1;
        }
        IResearchPointsOperation.super.addResearchData(tag, data);
    }
}
