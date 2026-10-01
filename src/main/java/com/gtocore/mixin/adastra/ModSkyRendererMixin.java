package com.gtocore.mixin.adastra;

import com.gtocore.client.renderer.sky.CelestialSkyRenderer;

import net.minecraft.client.multiplayer.ClientLevel;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.client.dimension.ModSkyRenderer;
import earth.terrarium.adastra.client.dimension.SkyRenderable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModSkyRenderer.class)
public class ModSkyRendererMixin {

    @Inject(method = "lambda$render$0", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtocore$skipRenderable(ClientLevel level, float partialTick, BufferBuilder builder, PoseStack poseStack, SkyRenderable renderable, CallbackInfo ci) {
        if (CelestialSkyRenderer.replacesPlanetSky(level)) ci.cancel();
    }
}
