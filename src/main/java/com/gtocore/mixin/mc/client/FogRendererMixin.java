package com.gtocore.mixin.mc.client;

import com.gtocore.client.renderer.fx.SolarStormFX;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @ModifyExpressionValue(method = "setupColor",
                           at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"),
                                   @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F") })
    private static float preserveSolarHorizonColor(float weather) {
        return SolarStormFX.isSolarSurface(Minecraft.getInstance().level) ? 0.0F : weather;
    }
}
