package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.scope.MachineScope;

import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiblockPartMachine.class)
public abstract class MultiblockPartMachineMixin {

    @Inject(method = "addedToController", at = @At("TAIL"), remap = false)
    private void gto$added(CallbackInfo ci) {
        MachineScope.changed((MultiblockPartMachine) (Object) this);
    }

    @Inject(method = "removedFromController", at = @At("TAIL"), remap = false)
    private void gto$removed(CallbackInfo ci) {
        MachineScope.changed((MultiblockPartMachine) (Object) this);
    }
}
