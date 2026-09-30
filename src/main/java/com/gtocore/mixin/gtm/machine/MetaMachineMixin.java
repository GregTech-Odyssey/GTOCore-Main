package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.capability.IIWirelessInteractor;
import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.machine.heat.SolarHeatHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gto.datasynclib.annotations.Access;
import com.gto.datasynclib.annotations.Codec;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.datastream.data.ByteData;
import com.gto.datasynclib.datastream.data.Data;
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
    @SaveToDisk
    @Access(instanceAsValue = true)
    @Codec(writeToData = "gTOdyssey$writeToData", readFromData = "gTOdyssey$readFromData")
    private SolarHeatHandler gto$solarHeat;

    @Nullable
    public SolarHeatHandler getSolarHeatHandler() {
        return gto$solarHeat;
    }

    @Unique
    private Data gTOdyssey$writeToData(SolarHeatHandler handler) {
        return ByteData.FALSE;
    }

    @Unique
    private SolarHeatHandler gTOdyssey$readFromData(Data data) {
        gto$solarHeat = new SolarHeatHandler(getHolder());
        gto$solarHeat.setSideIOCondition(side -> true);
        return gto$solarHeat;
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
