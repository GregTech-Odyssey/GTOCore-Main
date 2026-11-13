package com.gtocore.mixin.jade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.util.SmoothChasingValue;

@Mixin(SmoothChasingValue.class)
public abstract class SmoothChasingValueMixin {

    @Shadow(remap = false)
    float speed;

    @Shadow(remap = false)
    float target;

    @Shadow(remap = false)
    public float value;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void gto$stableTick(float pTicks, CallbackInfo ci) {
        ci.cancel();
        float diff = target - value;
        if (!Float.isFinite(diff)) {
            value = Float.isFinite(target) ? target : 0F;
        } else if (Math.abs(diff) >= 1 / 4096F) {
            value += diff * Math.min(1F, speed * pTicks);
        }
    }
}
