package com.gtocore.mixin.ae2.eae;

import appeng.core.definitions.AEItems;
import appeng.parts.automation.IOBusPart;

import com.glodblock.github.extendedae.common.parts.PartActiveFormationPlane;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PartActiveFormationPlane.class)
public abstract class PartActiveFormationPlaneMixin {

    @Inject(method = "getDropMultiplier", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtocore$dropMultiplier(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue((long) IOBusPart.getOperationsForSpeedCards(((PartActiveFormationPlane) (Object) this).getInstalledUpgrades(AEItems.SPEED_CARD)));
    }
}
