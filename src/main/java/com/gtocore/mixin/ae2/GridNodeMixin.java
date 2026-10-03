package com.gtocore.mixin.ae2;

import com.gtocore.integration.ae.SolarStormConnections;

import appeng.api.networking.IGridNode;
import appeng.me.GridNode;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GridNode.class, remap = false)
public abstract class GridNodeMixin {

    @Inject(method = "destroy", at = @At("HEAD"))
    private void gto$forgetStormConnections(CallbackInfo ci) {
        SolarStormConnections.forgetNode((IGridNode) this);
    }
}
