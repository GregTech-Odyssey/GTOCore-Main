package com.gtocore.mixin.mc.client;

import com.gtocore.common.data.DysonSphereEffects;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @ModifyReturnValue(method = "getSkyColor", at = @At("RETURN"))
    private Vec3 dimDysonSky(Vec3 color, Vec3 pos, float partialTick) {
        return dimDysonColor(color, partialTick);
    }

    @ModifyReturnValue(method = "getCloudColor", at = @At("RETURN"))
    private Vec3 dimDysonClouds(Vec3 color, float partialTick) {
        return dimDysonColor(color, partialTick);
    }

    @Unique
    private Vec3 dimDysonColor(Vec3 color, float partialTick) {
        float transmission = DysonSphereEffects.transmission((ClientLevel) (Object) this, partialTick);
        return transmission == 1 ? color : color.scale(transmission);
    }

    @ModifyReturnValue(method = "getStarBrightness", at = @At("RETURN"))
    private float revealDysonStars(float brightness, float partialTick) {
        float transmission = DysonSphereEffects.transmission((ClientLevel) (Object) this, partialTick);
        return transmission == 1 ? brightness : 0.5f - (0.5f - brightness) * transmission;
    }

}
