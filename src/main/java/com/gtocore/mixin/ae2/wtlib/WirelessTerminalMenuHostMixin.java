package com.gtocore.mixin.ae2.wtlib;

import com.gtocore.integration.ae.SolarStormConnections;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.MEStorage;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.items.tools.powered.WirelessTerminalItem;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WirelessTerminalMenuHost.class)
public abstract class WirelessTerminalMenuHostMixin implements SolarStormConnections.TerminalHost {

    @Shadow(remap = false)
    @Final
    @Mutable
    private IGrid targetGrid;

    @Shadow(remap = false)
    @Final
    private WirelessTerminalItem terminal;

    @Shadow(remap = false)
    private IStorageService sg;

    @Unique
    private long gto$stormRevision = Long.MIN_VALUE;

    @Shadow(remap = false)
    public abstract boolean rangeCheck();

    @Override
    public IGrid gto$refreshStormGrid() {
        var host = (ItemMenuHost) (Object) this;
        long revision = SolarStormConnections.revision(host.getPlayer().level());
        if (!host.isClientSide() && (gto$stormRevision != revision || targetGrid == null || targetGrid.isEmpty())) {
            gto$stormRevision = revision;
            targetGrid = terminal.getLinkedGrid(host.getItemStack(), host.getPlayer().level(), null);
            sg = targetGrid == null ? null : targetGrid.getStorageService();
        }
        return targetGrid;
    }

    @Inject(method = "rangeCheck", at = @At("HEAD"), remap = false)
    private void gto$refreshStormRange(CallbackInfoReturnable<Boolean> cir) {
        gto$refreshStormGrid();
    }

    @Inject(method = "getWapSqDistance", at = @At("HEAD"), cancellable = true, remap = false)
    private void gto$blockStormAccessPoint(IWirelessAccessPoint accessPoint, CallbackInfoReturnable<Double> cir) {
        var player = ((ItemMenuHost) (Object) this).getPlayer();
        if (SolarStormConnections.isBlocked(player.level(), accessPoint.getActionableNode())) cir.setReturnValue(Double.MAX_VALUE);
    }

    @Inject(method = "getActionableNode", at = @At("RETURN"), cancellable = true, remap = false)
    private void gto$blockStormNode(CallbackInfoReturnable<IGridNode> cir) {
        var player = ((ItemMenuHost) (Object) this).getPlayer();
        if (SolarStormConnections.isBlocked(player.level(), cir.getReturnValue())) cir.setReturnValue(null);
    }

    @Inject(method = "getInventory", at = @At("HEAD"), cancellable = true, remap = false)
    private void gto$blockCachedStormInventory(CallbackInfoReturnable<MEStorage> cir) {
        gto$refreshStormGrid();
        var player = ((ItemMenuHost) (Object) this).getPlayer();
        if (SolarStormConnections.isGridBlocked(player.level(), targetGrid)) cir.setReturnValue(null);
    }

    @Inject(method = "extractAEPower", at = @At("HEAD"), cancellable = true, remap = false)
    private void extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier, CallbackInfoReturnable<Double> cir) {
        if (!rangeCheck()) {
            cir.setReturnValue(0.0);
        }
    }

    @Inject(method = "onBroadcastChanges", at = @At("RETURN"), remap = false, cancellable = true)
    private void onBroadcastChanges(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
