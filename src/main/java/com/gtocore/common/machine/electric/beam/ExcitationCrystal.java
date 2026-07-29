package com.gtocore.common.machine.electric.beam;

import com.gtolib.api.beam.BeamNode;
import com.gtolib.api.beam.BeamPassContext;
import com.gtolib.api.beam.BeamPassKey;
import com.gtolib.api.beam.IBeamOperator;
import com.gtolib.api.machine.SimpleNoEnergyMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class ExcitationCrystal extends SimpleNoEnergyMachine implements IBeamOperator {

    private final Map<BeamPassKey, PassState> activePasses = new HashMap<>();
    private TickableSubscription tickSubscription;

    public ExcitationCrystal(MetaMachineBlockEntity holder) {
        super(holder, 0, t -> 0);
    }

    @Override
    public BeamNode operate(BeamPassContext context, BeamNode lastNode, Vec3 exactPos) {
        if (context.beamId() <= 0) return outputNode(lastNode, exactPos, tryExcite(lastNode.propertiesSnapshot().intensity), context.decay());
        long now = context.level().getGameTime();
        var state = activePasses.get(context.key());
        if (state == null) {
            state = new PassState(context, lastNode, exactPos);
            state.energized = tryExcite(lastNode.propertiesSnapshot().intensity);
            state.lastChargeTick = now;
            activePasses.put(context.key(), state);
        } else {
            long previousIntensity = state.incoming.propertiesSnapshot().intensity;
            state.context = context;
            state.incoming = lastNode;
            state.exactPos = exactPos;
            if (previousIntensity != lastNode.propertiesSnapshot().intensity) {
                state.energized = tryExcite(lastNode.propertiesSnapshot().intensity);
                state.lastChargeTick = now;
            }
        }
        return outputNode(lastNode, exactPos, state.energized, context.decay());
    }

    @Override
    public boolean hasAutoOutputFluid() {
        return false;
    }

    @Override
    public boolean hasAutoOutputItem() {
        return false;
    }

    @Override
    public void onRayBeamPassRemoved(BeamPassContext context) {
        activePasses.remove(context.key());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        var level = getLevel();
        if (!isRemote() && level != null) {
            tickSubscription = subscribeServerTick(tickSubscription, this::tick, 1);
            TaskHandler.enqueueTask(level, () -> requestRayBeamUpdate(level, getPos()), 1);
        }
    }

    @Override
    public void onUnload() {
        activePasses.clear();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        super.onUnload();
    }

    private void tick() {
        long now = getLevel().getGameTime();
        for (var state : activePasses.values()) {
            if (now - state.lastChargeTick < 10L) continue;
            state.lastChargeTick = now;
            boolean energized = tryExcite(state.incoming.propertiesSnapshot().intensity);
            if (energized != state.energized) {
                state.energized = energized;
                state.context.requestRebuild();
            }
        }
    }

    private boolean tryExcite(long intensity) {
        if (getLevel() == null || intensity <= 0) return false;
        for (Direction direction : Direction.values()) {
            if (MetaMachine.getMachine(getLevel(), getPos().relative(direction)) instanceof CrystalExciterMachine exciter &&
                    exciter.tryConsumeRayEnergy(intensity)) {
                return true;
            }
        }
        return false;
    }

    private static BeamNode outputNode(BeamNode incoming, Vec3 exactPos, boolean energized, double decay) {
        var properties = incoming.propertiesSnapshot().copy();
        if (energized) properties.intensity = BeamMachineUtils.doubled((long) (properties.intensity * decay));
        return new BeamNode(exactPos.x, exactPos.y, exactPos.z, properties);
    }

    private static final class PassState {

        private BeamPassContext context;
        private BeamNode incoming;
        private Vec3 exactPos;
        private boolean energized;
        private long lastChargeTick;

        private PassState(BeamPassContext context, BeamNode incoming, Vec3 exactPos) {
            this.context = context;
            this.incoming = incoming;
            this.exactPos = exactPos;
        }
    }
}
