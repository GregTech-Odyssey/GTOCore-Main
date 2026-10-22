package com.gtocore.mixin.ae2.eae;

import com.gtocore.integration.ae.SpeedCards;

import com.glodblock.github.extendedae.common.tileentities.TileExIOPort;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TileExIOPort.class)
public abstract class TileExIOPortMixin {

    @ModifyVariable(method = "tickingRequest", at = @At(value = "INVOKE", target = "Lappeng/api/networking/IManagedGridNode;getGrid()Lappeng/api/networking/IGrid;", ordinal = 0), name = "itemsToMove", remap = false)
    private long gtocore$operations(long itemsToMove) {
        return SpeedCards.exIoPortOperations(((TileExIOPort) (Object) this).getUpgrades());
    }
}
