package com.gtocore.mixin.ae2.wtlib;

import com.gtocore.integration.ae.SolarStormHandler;

import net.minecraft.world.entity.player.Player;

import appeng.api.networking.IGrid;

import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wct.CraftingTerminalHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingTerminalHandler.class, remap = false)
public abstract class CraftingTerminalHandlerMixin {

    @Shadow
    @Final
    private Player player;
    @Shadow
    private WTMenuHost menuHost;

    @Inject(method = "inRange", at = @At("RETURN"), cancellable = true)
    private void gto$blockStormRange(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || menuHost == null || player.isCreative()) return;
        var node = menuHost.getActionableNode();
        if (node == null || SolarStormHandler.isBlocked(player.level(), node)) cir.setReturnValue(false);
    }

    /** GTO tools and player integrations also request the grid directly, without calling inRange. */
    @Inject(method = "getTargetGrid", at = @At("RETURN"), cancellable = true)
    private void gto$blockCachedStormGrid(CallbackInfoReturnable<IGrid> cir) {
        if (player.isCreative() || SolarStormHandler.isGridBlocked(player.level(), cir.getReturnValue())) cir.setReturnValue(null);
    }
}
