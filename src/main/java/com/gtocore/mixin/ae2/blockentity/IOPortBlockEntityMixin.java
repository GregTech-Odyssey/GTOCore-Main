package com.gtocore.mixin.ae2.blockentity;

import com.gtocore.integration.ae.SpeedCards;

import appeng.blockentity.storage.IOPortBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IOPortBlockEntity.class)
public abstract class IOPortBlockEntityMixin {

    @Inject(method = "getOperationsPerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtocore$tieredSpeedCards(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(SpeedCards.ioPortOperations(((IOPortBlockEntity) (Object) this).getUpgrades()));
    }
}
