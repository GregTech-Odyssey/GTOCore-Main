package com.gtocore.mixin.gtm.machine;

import com.gtolib.api.capability.IHeatContainer;
import com.gtolib.api.machine.feature.IWorkInSpaceMachine;
import com.gtolib.api.machine.heat.SolarHeatHandler;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MetaMachineBlockEntity.class)
public abstract class SolarHeatCapabilityMixin {

    @Final
    @Shadow(remap = false)
    public MetaMachine metaMachine;

    @Inject(method = "getGTCapability", at = @At("HEAD"), cancellable = true, remap = false)
    private <T> void gto$getSolarHeat(Class<T> cap, @Nullable Direction side, CallbackInfoReturnable<Object> cir) {
        if ((cap == IHeatContainer.class || cap == SolarHeatHandler.class) && metaMachine instanceof IWorkInSpaceMachine machine && machine.isOnSolarSurface()) {
            var heat = machine.getHeatContainer();
            if (heat != null && (side == null || heat.heatIO(side))) cir.setReturnValue(heat);
        }
    }
}
