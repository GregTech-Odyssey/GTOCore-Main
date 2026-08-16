package com.gtocore.mixin.jade;

import com.gtolib.api.machine.dynamic.DynamicVisualManager;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.overlay.RayTracing;

@Mixin(RayTracing.class)
public final class RayTracingMixin {

    @Shadow(remap = false)
    private HitResult target;

    @Inject(method = "fire", at = @At("RETURN"), remap = false)
    private void gto$dynamicTarget(CallbackInfo ci) {
        var hit = DynamicVisualManager.findDynamicHit();
        if (hit == null) return;
        if (target != null && target.getType() != HitResult.Type.MISS && target.getLocation().distanceTo(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()) <= hit.distance() + 1.0E-4) return;
        target = hit.target();
    }
}
