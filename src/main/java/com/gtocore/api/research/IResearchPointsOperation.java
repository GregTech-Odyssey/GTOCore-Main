package com.gtocore.api.research;

import com.gtocore.common.machine.multiblock.part.research.SimpleResearchTagPartMachine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

public interface IResearchPointsOperation {

    static void findHatchAndAddResearchData(IMultiController controller, ResearchTag tag, double data) {
        for (var p : controller.getParts(SimpleResearchTagPartMachine.class)) {
            if (p.getResearchTag() == tag) {
                p.addData(data);
            }
        }
    }

    default void addResearchData(ResearchTag tag, double data) {
        if (this instanceof IMultiController controller) {
            findHatchAndAddResearchData(controller, tag, data);
        }
    }
}
