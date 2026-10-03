package com.gtocore.mixin.ae2;

import com.gtocore.integration.ae.SolarStormConnections;

import net.minecraft.core.Direction;

import appeng.api.networking.IGridNode;
import appeng.me.GridConnection;
import appeng.me.GridNode;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GridConnection.class, remap = false)
public abstract class GridConnectionMixin implements SolarStormConnections.Suspendable {

    @Shadow
    private GridNode sideA;
    @Shadow
    private GridNode sideB;
    @Shadow
    int usedChannels;
    @Shadow
    private int lastUsedChannels;
    @Shadow
    private Object visitorIterationNumber;

    @Shadow
    private static void mergeGrids(GridNode a, GridNode b) {
        throw new AssertionError();
    }

    @Shadow
    public abstract void destroy();

    @Unique
    private boolean gto$suspended;
    @Unique
    private boolean gto$detaching;
    @Unique
    private boolean gto$retired;

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void gto$reusePending(IGridNode a, IGridNode b, Direction direction, CallbackInfoReturnable<GridConnection> cir) {
        var pending = SolarStormConnections.findPending(a, b);
        if (pending != null) cir.setReturnValue(pending);
    }

    @Inject(method = "create", at = @At(value = "INVOKE", target = "Lappeng/me/GridConnection;mergeGrids(Lappeng/me/GridNode;Lappeng/me/GridNode;)V"), cancellable = true)
    private static void gto$deferStormConnection(IGridNode a, IGridNode b, Direction direction, CallbackInfoReturnable<GridConnection> cir,
                                                 @Local GridConnection connection) {
        if (SolarStormConnections.isBlocked(a.getLevel(), b)) {
            ((GridConnectionMixin) (Object) connection).gto$suspended = true;
            SolarStormConnections.track(connection);
            cir.setReturnValue(connection);
        }
    }

    @Inject(method = "create", at = @At("RETURN"))
    private static void gto$trackConnection(IGridNode a, IGridNode b, Direction direction, CallbackInfoReturnable<GridConnection> cir) {
        SolarStormConnections.track(cir.getReturnValue());
    }

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void gto$removeConnection(CallbackInfo ci) {
        if (gto$detaching) {
            // Consume the internal call marker; a listener cancelling the edge during detach is external.
            gto$detaching = false;
            return;
        }
        SolarStormConnections.forget((GridConnection) (Object) this);
        if (gto$suspended || gto$retired) ci.cancel();
        gto$retired = true;
        gto$suspended = false;
    }

    @Override
    public boolean gto$isStormSuspended() {
        return gto$suspended;
    }

    @Override
    public void gto$retireStormConnection() {
        gto$retired = true;
        gto$suspended = false;
    }

    @Override
    public void gto$setStormSuspended(boolean suspended) {
        if (gto$retired || gto$suspended == suspended) return;
        if (suspended) {
            gto$suspended = true;
            gto$detaching = true;
            destroy();
            gto$detaching = false;
            usedChannels = 0;
            lastUsedChannels = 0;
        } else {
            // Restore the SAME object, so QNB ConnectionWrapper and addon handles remain valid.
            mergeGrids(sideA, sideB);
            if (gto$retired) return;
            usedChannels = 0;
            lastUsedChannels = 0;
            visitorIterationNumber = null;
            sideA.getGrid().getPathingService().repath();
            ((GridNodeAccessor) sideA).gto$addConnection((GridConnection) (Object) this);
            ((GridNodeAccessor) sideB).gto$addConnection((GridConnection) (Object) this);
            gto$suspended = false;
        }
    }
}
