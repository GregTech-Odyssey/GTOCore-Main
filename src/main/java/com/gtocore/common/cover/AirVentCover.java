package com.gtocore.common.cover;

import com.gtocore.common.machine.multiblock.part.InfiniteIntakeHatchPartMachine;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.stacks.AEFluidKey;

import com.suried.gtoutils.schedule.Schedulers;
import com.suried.gtoutils.schedule.Task;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AirVentCover extends CoverBehavior {

    private Task subscription;
    private MetaMachine machine;

    public AirVentCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && (machine = MetaMachine.getMachine(coverHolder.holder())) != null && machine.getFluidHandlerCap(attachedSide, false) != null;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (coverHolder.getLevel() instanceof ServerLevel serverLevel) {
            Schedulers.of(serverLevel).schedule(() -> {
                machine = MetaMachine.getMachine(coverHolder.holder());
                subscription = coverHolder.subscribeServerTick(subscription, this::update, 20);
            });
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        if (subscription != null) {
            subscription.cancel();
            subscription = null;
        }
    }

    private void update() {
        if (machine != null && machine.getNeighborBlockState(attachedSide).isAir()) {
            var fluid = InfiniteIntakeHatchPartMachine.AIR_MAP.get(coverHolder.getLevel().dimension());
            if (fluid == null) {
                subscription.cancel();
                return;
            }
            var handler = machine.getFluidHandlerCap(attachedSide, false);
            if (handler == null) return;
            handler.insert(AEFluidKey.of(fluid), 200, false);
        }
    }
}
