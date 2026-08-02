package com.gtocore.mixin.ae2.wtlib;

import de.mari_023.ae2wtlib.wut.WUTHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cap Universal Terminal energy-card registration to a fixed upgrade budget.
 * Vanilla ae2wtlib uses {@code terminalNames.size() * 2}, which grows with every
 * registered wireless terminal and inflates the upgrade UI.
 */
@Mixin(WUTHandler.class)
public abstract class WUTHandlerMixin {

    private static final int FIXED_UPGRADE_CARD_COUNT = 3;

    @Inject(method = "getUpgradeCardCount", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtocore$fixedUpgradeCardCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FIXED_UPGRADE_CARD_COUNT);
    }
}
