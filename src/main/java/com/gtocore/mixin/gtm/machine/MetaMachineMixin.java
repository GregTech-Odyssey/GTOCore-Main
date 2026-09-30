package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.machine.heat.SolarHeatHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gto.datasynclib.annotations.AdditionalHolder;
import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MetaMachine.class)
public abstract class MetaMachineMixin implements IIWirelessInteractor.IWirelessProvider {

    @Shadow(remap = false)
    public abstract MetaMachineBlockEntity getHolder();

    @Unique
    @AdditionalHolder(childManager = true)
    @SaveToDisk
    private SolarHeatHandler gto$solarHeat;

    @Nullable
    public SolarHeatHandler getSolarHeatHandler() {
        return gto$solarHeat;
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void gto$createSolarHeat(CallbackInfo ci) {
        if (this instanceof IWorkInSpaceMachine) {
            gto$solarHeat = new SolarHeatHandler(getHolder());
            gto$solarHeat.setSideIOCondition(side -> true);
        }
    }

    @Inject(method = "onLoad", at = @At("TAIL"), remap = false)
    private void gto$loadSolarHeat(CallbackInfo ci) {
        if (this instanceof IWorkInSpaceMachine machine && machine.isOnSolarSurface()) {
            if (gto$solarHeat == null) {
                gto$solarHeat = new SolarHeatHandler(getHolder());
                gto$solarHeat.setSideIOCondition(side -> true);
            }
            gto$solarHeat.onLoad();
        }
    }

    @Inject(method = "onUnload", at = @At("HEAD"), remap = false)
    private void gto$unloadSolarHeat(CallbackInfo ci) {
        if (gto$solarHeat != null) gto$solarHeat.onUnLoad();
    }
}
