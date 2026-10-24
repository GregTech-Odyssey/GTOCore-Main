package com.gtocore.mixin.mc.client;

import com.gtocore.client.renderer.fx.SolarStormFX;
import com.gtocore.common.data.DysonSphereEffects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import earth.terrarium.adastra.api.planets.PlanetApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @ModifyExpressionValue(method = "setupColor",
                           at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getSunriseColor(FF)[F"))
    private static float[] disableSpaceSunriseFog(float[] color, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true, ordinal = 0) float partialTick) {
        // Directional sunrise fog can turn a black space background yellow, especially with night vision.
        if (PlanetApi.API.isSpace(level)) return null;
        if (color != null) color[3] *= DysonSphereEffects.transmission(level, partialTick);
        return color;
    }

    @ModifyExpressionValue(method = "setupColor",
                           at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 dimDysonHorizon(Vec3 color, @Local(argsOnly = true) ClientLevel level, @Local(argsOnly = true, ordinal = 0) float partialTick) {
        float transmission = DysonSphereEffects.transmission(level, partialTick);
        return transmission == 1 ? color : color.scale(transmission);
    }

    @ModifyExpressionValue(method = "setupColor",
                           at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"),
                                   @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getThunderLevel(F)F") })
    private static float preserveSolarHorizonColor(float weather) {
        return SolarStormFX.isSolarSurface(Minecraft.getInstance().level) ? 0.0F : weather;
    }
}
