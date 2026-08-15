package com.gtocore.common.machine.noenergy;

import com.gtocore.common.pipe.muffler.IMufflerConduction;

import com.gtolib.api.machine.feature.ISpaceWorkspaceMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import earth.terrarium.adastra.api.planets.PlanetApi;
import org.jetbrains.annotations.Nullable;

public class ExhaustFanMachine extends MultiblockPartMachine implements IMufflerConduction {

    public ExhaustFanMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean isMufflerSource() {
        return false;
    }

    @Override
    public boolean shouldWorkAsMufflerSource() {
        if (PlanetApi.API.isSpace(getLevel())) {
            return getController() instanceof ISpaceWorkspaceMachine;
        }
        BlockPos pos = self().getPos();
        for (int i = 0; i < 3; i++) {
            pos = pos.relative(this.self().getFrontFacing());
            if (!self().getLevel().getBlockState(pos).isAir()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean testCapability(@Nullable Direction side) {
        return side == null || side != self().getFrontFacing();
    }
}
