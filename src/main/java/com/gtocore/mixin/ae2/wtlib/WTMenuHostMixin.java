package com.gtocore.mixin.ae2.wtlib;

import com.gtocore.integration.ae.SolarStormConnections;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;

import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WTMenuHost.class, remap = false)
public abstract class WTMenuHostMixin {

    @Shadow
    private IActionHost quantumBridge;

    @Shadow
    @Final
    @Mutable
    private IGrid targetGrid;

    @Inject(method = "isQuantumLinked", at = @At("HEAD"))
    private void gto$refreshStormGrid(CallbackInfoReturnable<Boolean> cir) {
        targetGrid = ((SolarStormConnections.TerminalHost) this).gto$refreshStormGrid();
    }

    /** Quantum cards skip the access-point range check, including an already cached bridge. */
    @Inject(method = "isQuantumLinked", at = @At("RETURN"), cancellable = true)
    private void gto$blockStormQuantumLink(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || quantumBridge == null) return;
        var player = ((ItemMenuHost) (Object) this).getPlayer();
        if (SolarStormConnections.isBlocked(player.level(), quantumBridge.getActionableNode())) cir.setReturnValue(false);
    }

    @Inject(method = "getActionableNode", at = @At("RETURN"), cancellable = true)
    private void gto$blockStormNode(CallbackInfoReturnable<IGridNode> cir) {
        var player = ((ItemMenuHost) (Object) this).getPlayer();
        if (SolarStormConnections.isBlocked(player.level(), cir.getReturnValue())) cir.setReturnValue(null);
    }
}
