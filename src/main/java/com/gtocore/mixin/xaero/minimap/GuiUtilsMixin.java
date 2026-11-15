package com.gtocore.mixin.xaero.minimap;

import com.gtocore.integration.xaero.XaeroMinimapHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.hud.gui.util.GuiUtils;
import xaero.lib.client.config.ClientConfigManager;

@Mixin(value = GuiUtils.class, remap = false)
public abstract class GuiUtilsMixin {

    @Inject(method = "getMinimapScale", at = @At("HEAD"), cancellable = true)
    private static void gtocore$hudScale(ClientConfigManager config, CallbackInfoReturnable<Float> cir) {
        if (XaeroMinimapHud.rendering()) cir.setReturnValue(XaeroMinimapHud.scale());
    }
}
