package com.gtocore.mixin.mc.client;

import com.gtocore.common.data.DysonSphereEffects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {

    @Unique
    private float gtocore$dysonTransmission = 1;

    @Inject(method = "updateLightTexture", at = @At("HEAD"))
    private void cacheDysonTransmission(float partialTick, CallbackInfo ci) {
        var level = Minecraft.getInstance().level;
        gtocore$dysonTransmission = level == null ? 1 : DysonSphereEffects.transmission(level, partialTick);
    }

    // 仅衰减天空光通道，保留火把等方块光照、夜视以及原版的光照后处理。
    @ModifyExpressionValue(method = "updateLightTexture",
                           at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LightTexture;getBrightness(Lnet/minecraft/world/level/dimension/DimensionType;I)F", ordinal = 0))
    private float dimDysonSunlight(float brightness) {
        return brightness * gtocore$dysonTransmission;
    }
}
