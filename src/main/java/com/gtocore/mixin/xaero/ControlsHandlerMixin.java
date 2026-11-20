package com.gtocore.mixin.xaero;

import com.gtolib.api.wireless.energy.client.XaeroDimensionScreen;

import net.minecraft.client.KeyMapping;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.MapProcessor;
import xaero.map.controls.ControlsHandler;
import xaero.map.controls.ControlsRegister;

@Mixin(value = ControlsHandler.class, remap = false)
public abstract class ControlsHandlerMixin {

    @Shadow
    private MapProcessor mapProcessor;

    @Inject(method = "keyDown", at = @At("HEAD"), cancellable = true)
    private void gtocore$openDimensionChart(KeyMapping key, boolean tickEnd, boolean repeat, CallbackInfo ci) {
        if (tickEnd || key != ControlsRegister.keyToggleDimension) return;
        ci.cancel();
        XaeroDimensionScreen.openFromWorld(mapProcessor);
    }
}
