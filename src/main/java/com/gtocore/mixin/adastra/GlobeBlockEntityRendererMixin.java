package com.gtocore.mixin.adastra;

import com.gtocore.client.renderer.SolarGlobeRenderer;
import com.gtocore.common.data.GTOGlobes;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.adastra.client.renderers.blocks.GlobeBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlobeBlockEntityRenderer.class, remap = false)
public abstract class GlobeBlockEntityRendererMixin {

    @Inject(method = "render(Lnet/minecraft/world/level/block/state/BlockState;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void gto$renderSolarSurface(BlockState state, float rotation, PoseStack poseStack, MultiBufferSource buffer,
                                               int light, int overlay, CallbackInfo ci) {
        if (state.is(GTOGlobes.Blocks.SOLAR_SURFACE_GLOBE.get())) {
            SolarGlobeRenderer.render(state, rotation, poseStack, buffer, overlay);
            ci.cancel();
        }
    }
}
