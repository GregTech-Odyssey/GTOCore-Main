package com.gtocore.mixin.gtmthings;

import com.gtolib.api.wireless.ReceiverTransmitterHandler;
import com.gtolib.api.wireless.ReceiverTransmitterHandler.ConnectionType;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableMultiblockPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.common.block.machine.multiblock.part.computation.WirelessOpticalComputationHatchMachine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WirelessOpticalComputationHatchMachine.class, remap = false)
public abstract class WirelessOpticalComputationHatchMachineMixin extends WorkableMultiblockPartMachine implements IMachineLife {

    protected WirelessOpticalComputationHatchMachineMixin(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Invoker("setTransmitterPos")
    protected abstract void gto$setTransmitterPos(@Nullable BlockPos pos);

    @Invoker("setReceiverPos")
    protected abstract void gto$setReceiverPos(@Nullable BlockPos pos);

    @Override
    public void onLoad() {
        super.onLoad();
        gto$registerConnection();
    }

    @Override
    public void onUnload() {
        ReceiverTransmitterHandler.unregister(getLevel(), ConnectionType.COMPUTATION, getPos());
        super.onUnload();
    }

    @Override
    public void onMachineRemoved() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;
        WirelessOpticalComputationHatchMachine self = (WirelessOpticalComputationHatchMachine) (Object) this;
        if (self.isTransmitter()) gto$setReceiverPos(null);
        else gto$setTransmitterPos(null);
        onChanged();
        ReceiverTransmitterHandler.unregister(level, ConnectionType.COMPUTATION, getPos());
    }

    @Inject(method = "bindWith", at = @At("RETURN"))
    private void gto$bindWith(BlockPos otherPos, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) gto$registerConnection();
    }

    @Unique
    private void gto$registerConnection() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;
        WirelessOpticalComputationHatchMachine self = (WirelessOpticalComputationHatchMachine) (Object) this;
        boolean transmitter = self.isTransmitter();
        BlockPos ownPos = getPos();
        BlockPos otherPos = transmitter ? self.getReceiverPos() : self.getTransmitterPos();
        ReceiverTransmitterHandler.register(level, ConnectionType.COMPUTATION,
                transmitter ? ownPos : otherPos, transmitter ? otherPos : ownPos);
    }
}
