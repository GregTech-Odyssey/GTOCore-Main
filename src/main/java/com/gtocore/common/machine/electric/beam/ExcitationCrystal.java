package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.beam.*;
import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Independent incident amplifiers sharing one pump budget, without coupling opposite directions. */
public class ExcitationCrystal extends SimpleNoEnergyMachine implements IBeamOperator {

    private static final Direction[] SIDES = Direction.values();
    private final CrystalExciterMachine[] exciters = new CrystalExciterMachine[SIDES.length];
    private final BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
    private long exciterScanTick = Long.MIN_VALUE;

    public ExcitationCrystal(MetaMachineBlockEntity holder) {
        super(holder, 0, t -> 0);
    }

    @Override
    public boolean activeBeamTick() {
        return true;
    }

    @Override
    public void route(BeamRay incoming, Vec3 hit, BeamRoutes outputs) {
        outputs.straight(incoming, hit);
    }

    @Override
    public void evaluate(BeamBatch batch) {
        long tick = getLevel().getGameTime();
        if (exciterScanTick != tick) {
            for (int i = 0; i < SIDES.length; i++) {
                neighbor.setWithOffset(getPos(), SIDES[i]);
                exciters[i] = MetaMachine.getMachine(getLevel(), neighbor) instanceof CrystalExciterMachine exciter ? exciter : null;
            }
            exciterScanTick = tick;
        }
        for (int i = 0; i < batch.size(); i++) {
            var pass = batch.pass(i);
            double required = Math.ceil(pass.input().power * 2048.0D);
            long cost = BeamSignal.toLong(required);
            boolean energized = false;
            if (cost > 0 && required < Long.MAX_VALUE) {
                for (var exciter : exciters) {
                    if (exciter != null && batch.budget().reserve(exciter, cost)) {
                        energized = true;
                        break;
                    }
                }
            }
            pass.forward(0, energized ? 2 : 1);
        }
    }

    @Override
    public boolean hasAutoOutputFluid() {
        return false;
    }

    @Override
    public boolean hasAutoOutputItem() {
        return false;
    }
}
