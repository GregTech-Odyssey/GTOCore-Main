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
    private static float[] disableSpaceSunriseFog(float[] color, @Local(argsOnly = true) ClientLevel level) {
        // Directional sunrise fog can turn a black space background yellow, especially with night vision.
        if (PlanetApi.API.isSpace(level)) return null;
        return color;
    }

    // 衰减朝向太阳的雾色混合权重，不修改 DimensionSpecialEffects 复用的颜色数组。
    @ModifyExpressionValue(method = "setupColor",
                           at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;dot(Lorg/joml/Vector3fc;)F", remap = false))
    private static float dimDysonSunriseFog(float alignment, @Local(argsOnly = true) ClientLevel level) {
        return alignment * DysonSphereEffects.sunlightTransmission(level);
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
