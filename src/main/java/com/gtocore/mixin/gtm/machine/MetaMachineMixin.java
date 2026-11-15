package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.scope.MachineScope;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MetaMachine.class)
public abstract class MetaMachineMixin implements IIWirelessInteractor.IWirelessProvider {

    @Inject(method = "setOwnerUUID", at = @At("TAIL"), remap = false)
    private void gto$ownerChanged(CallbackInfo ci) {
        MachineScope.changed((MetaMachine) (Object) this);
    }

    @Inject(method = "loadCustomPersistedData", at = @At("TAIL"), remap = false)
    private void gto$reloaded(CallbackInfo ci) {
        MachineScope.changed((MetaMachine) (Object) this);
    }
}
