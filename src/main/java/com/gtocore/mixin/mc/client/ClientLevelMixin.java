package com.gtocore.mixin.mc.client;

import com.gtocore.client.renderer.fx.SolarStormFX;

import net.minecraft.client.multiplayer.ClientLevel;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @ModifyExpressionValue(method = { "getSkyColor", "getSkyDarken", "getCloudColor" },
                           at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"),
                                   @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F") })
    private float preserveSolarSkyColor(float weather) {
        return SolarStormFX.isSolarSurface((ClientLevel) (Object) this) ? 0.0F : weather;
    }

    @ModifyExpressionValue(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getSkyFlashTime()I"))
    private int suppressSolarLightningFlash(int flash) {
        return SolarStormFX.isSolarSurface((ClientLevel) (Object) this) ? 0 : flash;
    }
}
