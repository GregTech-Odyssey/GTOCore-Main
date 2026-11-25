package com.hepdd.gtmthings.common.cover;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;

public interface IWirelessTransferCover {

    @Nullable
    GlobalPos getBoundTarget();

    @Nullable
    Direction getBoundSide();

    @Nullable
    static GlobalPos resolveBoundTarget(@Nullable String dimensionId, @Nullable BlockPos targetPos) {
        if (dimensionId == null || dimensionId.isEmpty() || targetPos == null) return null;
        var dimension = ResourceLocation.tryParse(dimensionId);
        return dimension == null ? null : GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dimension), targetPos.immutable());
    }
}
