package com.gtocore.common.machine.tesseract;

import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMultiTesseract extends TesseractCapCache.Holder, ITesseractMarkerInteractable {

    @Override
    @Nullable
    default <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (isCalled()) return null;
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            var caps = getItemCaps();
            var handler = caps.collect(this, side);
            if (handler != null) return caps.optional(side, handler);
        } else if (cap == ForgeCapabilities.FLUID_HANDLER) {
            var caps = getFluidCaps();
            var handler = caps.collect(this, side);
            if (handler != null) return caps.optional(side, handler);
        }
        return null;
    }

    @Nullable
    default IKeyHandler<AEItemKey> collectItemHandler(@Nullable Direction side) {
        return getItemCaps().collect(this, side);
    }

    @Nullable
    default IKeyHandler<AEFluidKey> collectFluidHandler(@Nullable Direction side) {
        return getFluidCaps().collect(this, side);
    }
}
