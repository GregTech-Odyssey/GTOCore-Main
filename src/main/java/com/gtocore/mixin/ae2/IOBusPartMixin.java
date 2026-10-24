package com.gtocore.mixin.ae2;

import com.gtocore.integration.ae.SpeedCards;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.parts.automation.IOBusPart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IOBusPart.class)
public abstract class IOBusPartMixin {

    @Inject(method = "getSpeedCardOperations", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtocore$tieredSpeedCards(CallbackInfoReturnable<Integer> cir) {
        IUpgradeInventory upgrades = ((IOBusPart) (Object) this).getUpgrades();
        cir.setReturnValue(SpeedCards.busOperations(upgrades));
    }
}
