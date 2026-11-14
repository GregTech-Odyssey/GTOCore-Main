package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.scope.MachineScope;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiblockControllerMachine.class)
public abstract class MultiblockControllerMachineMixin {

    @Shadow(remap = false)
    protected IMultiPart[] parts;

    @Inject(method = "onStructureFormed", at = @At("TAIL"), remap = false)
    private void gto$formed(CallbackInfo ci) {
        MachineScope.changed((MultiblockControllerMachine) (Object) this);
    }

    @Inject(method = "onStructureInvalid", at = @At("TAIL"), remap = false)
    private void gto$invalid(CallbackInfo ci) {
        MachineScope.changed((MultiblockControllerMachine) (Object) this);
    }

    @Inject(method = "onUnload", at = @At("TAIL"), remap = false)
    private void gto$partsLostController(CallbackInfo ci) {
        for (var part : parts) MachineScope.changed(part.self());
    }
}
