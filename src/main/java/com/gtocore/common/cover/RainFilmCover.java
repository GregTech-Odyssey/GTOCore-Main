package com.gtocore.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IExplosionMachine;

import net.minecraft.core.Direction;

public final class RainFilmCover extends CoverBehavior {

    public RainFilmCover(CoverDefinition definition, ICoverable holder, Direction side) {
        super(definition, holder, side);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && attachedSide == Direction.UP && MetaMachine.getMachine(coverHolder.holder()) instanceof IExplosionMachine;
    }
}
