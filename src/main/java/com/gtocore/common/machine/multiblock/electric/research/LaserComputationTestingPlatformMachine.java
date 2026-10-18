package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.IResearchPointsOperation;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.machine.electric.beam.BeamAccessPartMachine;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

public class LaserComputationTestingPlatformMachine extends ElectricMultiblockMachine implements IResearchPointsOperation {

    private static final long MIN_BEAM_INTENSITY = 64L;
    private static final double LOG_4 = Math.log(4);
    private final O2OOpenCacheHashMap<BeamAccessPartMachine.BeamKey, long[]> intensitySamples = new O2OOpenCacheHashMap<>();

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
        for (var part : getParts(BeamAccessPartMachine.class)) {
            part.collectRecentIntensitySamples(intensitySamples);
        }
        long intensity = 0L;
        for (var iterator = intensitySamples.object2ObjectEntrySet().fastIterator(); iterator.hasNext();) {
            var entry = iterator.next();
            int wavelength = entry.getKey().waveLength();
            if (wavelength < 749 || wavelength > 751) continue;
            long quotient = 0L;
            long remainder = 0L;
            for (long sample : entry.getValue()) {
                quotient += sample / BeamAccessPartMachine.AVERAGE_WINDOW_TICKS;
                remainder += sample % BeamAccessPartMachine.AVERAGE_WINDOW_TICKS;
            }
            intensity = Math.max(intensity, quotient + remainder / BeamAccessPartMachine.AVERAGE_WINDOW_TICKS);
        }
        intensitySamples.clear();
        return intensity;
    }
}
