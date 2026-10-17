package com.gtocore.mixin.ae2;

import com.gtocore.integration.ae.SuperSpeedCard;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.parts.automation.IOBusPart;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IOBusPart.class)
public abstract class IOBusPartMixin {

    @Inject(method = "getOperationsPerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtocore$superSpeedCard(CallbackInfoReturnable<Integer> cir) {
        IUpgradeInventory upgrades = ((IOBusPart) (Object) this).getUpgrades();
        cir.setReturnValue(SuperSpeedCard.busOperations(upgrades));
    }
}
