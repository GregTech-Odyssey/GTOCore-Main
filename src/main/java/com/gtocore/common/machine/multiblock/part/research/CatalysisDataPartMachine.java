package com.gtocore.common.machine.multiblock.part.research;

import com.gtocore.api.research.ResearchTag;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CatalysisDataPartMachine extends SimpleResearchTagPartMachine {

    private static final int dataCapacity = 1024;

    public CatalysisDataPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public long getDataCapacity() {
        return dataCapacity;
    }

    @Override
    public ResearchTag getResearchTag() {
        return ResearchTag.CATALYSIS;
    }
}
