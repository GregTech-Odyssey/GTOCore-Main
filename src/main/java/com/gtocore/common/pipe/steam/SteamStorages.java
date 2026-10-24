package com.gtocore.common.pipe.steam;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.KeyTypedStorage;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;

import org.jetbrains.annotations.Nullable;

public final class SteamStorages {

    private SteamStorages() {}

    public static @Nullable MEStorage getStorage(@Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity == null) return null;
        var storage = blockEntity.getCapability(Capabilities.STORAGE, side).orElse(null);
        if (storage == null) return null;
        if (storage instanceof KeyTypedStorage typed) {
            return typed.forKeyType(AEKeyTypes.FLUIDS);
        }
        return storage;
    }

    public static boolean hasStorage(@Nullable BlockEntity blockEntity, @Nullable Direction side) {
        return getStorage(blockEntity, side) != null;
    }

    public static long transferKey(MEStorage from, MEStorage to, AEKey key, long amount) {
        if (amount <= 0) return 0;
        var source = IActionSource.empty();
        var want = from.extract(key, amount, Actionable.SIMULATE, source);
        if (want <= 0) return 0;
        var accept = to.insert(key, want, Actionable.SIMULATE, source);
        if (accept <= 0) return 0;
        var extracted = from.extract(key, want, Actionable.MODULATE, source);
        if (extracted <= 0) return 0;
        var inserted = to.insert(key, extracted, Actionable.MODULATE, source);
        if (inserted < extracted) {
            from.insert(key, extracted - inserted, Actionable.MODULATE, source);
        }
        return inserted;
    }
}
