package com.gtocore.mixin.jade;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.util.ClientProxy;

@Mixin(value = ClientProxy.class, remap = false)
public class ClientProxyRecipeKeysMixin {

    @Inject(method = "shouldRegisterRecipeViewerKeys", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void gtocore$registerRecipeKeys(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
