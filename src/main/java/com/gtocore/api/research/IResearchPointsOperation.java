package com.gtocore.api.research;

import com.gtocore.common.machine.multiblock.part.research.SimpleResearchTagPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import java.util.Arrays;

public interface IResearchPointsOperation {

    static void findHatchAndAddResearchData(IMultiController controller, ResearchTag tag, double data) {
        Arrays.stream(controller.getParts())
                .filter(p -> p instanceof SimpleResearchTagPartMachine sp && sp.getResearchTag() == tag)
                .map(p -> (SimpleResearchTagPartMachine) p)
                .forEach(p -> p.addData(data));
    }

    default void addResearchData(ResearchTag tag, double data) {
        if (this instanceof IMultiController controller) {
            findHatchAndAddResearchData(controller, tag, data);
        }
    }
}
