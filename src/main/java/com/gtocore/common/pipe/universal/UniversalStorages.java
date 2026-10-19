package com.gtocore.common.pipe.universal;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import appeng.me.storage.CompositeStorage;
import appeng.me.storage.ExternalStorageFacade;
import appeng.me.storage.NetworkStorage;

import org.jetbrains.annotations.Nullable;

public final class UniversalStorages {

    private UniversalStorages() {}

    public static @Nullable MEStorage getStorage(@Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity == null) return null;
        var storage = blockEntity.getCapability(Capabilities.STORAGE, side).orElse(null);
        if (storage != null) return storage instanceof NetworkStorage ? null : storage;
        var items = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        var fluids = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
        if (items == null) return fluids == null ? null : ExternalStorageFacade.of(fluids);
        if (fluids == null) return ExternalStorageFacade.of(items);
        return new CompositeStorage(ExternalStorageFacade.of(items), ExternalStorageFacade.of(fluids));
    }

    public static boolean hasStorage(@Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity == null) return false;
        var storage = blockEntity.getCapability(Capabilities.STORAGE, side).orElse(null);
        if (storage != null) return !(storage instanceof NetworkStorage);
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).isPresent() ||
                blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side).isPresent();
    }

    public static long transferKey(MEStorage from, MEStorage to, AEKey key, long amount) {
        if (amount <= 0) return 0;
        long accept = to.insert(key, amount, Actionable.SIMULATE, IActionSource.empty());
        if (accept <= 0) return 0;
        long want = from.extract(key, accept, Actionable.SIMULATE, IActionSource.empty());
        if (want <= 0) return 0;
        long extracted = from.extract(key, want, Actionable.MODULATE, IActionSource.empty());
        if (extracted <= 0) return 0;
        return to.insert(key, extracted, Actionable.MODULATE, IActionSource.empty());
    }
}
