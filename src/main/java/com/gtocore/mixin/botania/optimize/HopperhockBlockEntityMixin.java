package com.gtocore.mixin.botania.optimize;

import appeng.hooks.ticking.TickHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.common.block.flower.functional.HopperhockBlockEntity;

@Mixin(HopperhockBlockEntity.class)
public abstract class HopperhockBlockEntityMixin {

    @Inject(method = "tickFlower", at = @At(value = "INVOKE", target = "Lvazkii/botania/common/block/flower/functional/HopperhockBlockEntity;getLevel()Lnet/minecraft/world/level/Level;", ordinal = 0, remap = true), remap = false, cancellable = true)
    private void tickFlower(CallbackInfo ci) {
        if (TickHandler.INSTANCE.tickCounter % 40 != 7) ci.cancel();
    }
}
