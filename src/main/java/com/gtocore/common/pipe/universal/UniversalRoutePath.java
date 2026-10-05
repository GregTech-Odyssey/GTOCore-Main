package com.gtocore.common.pipe.universal;

import com.gtocore.common.blockentity.UniversalPipeBlockEntity;

import com.gregtechceu.gtceu.api.pipenet.IRoutePath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.storage.MEStorage;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UniversalRoutePath implements IRoutePath<MEStorage> {

    @Getter
    private final UniversalPipeBlockEntity targetPipe;
    private final Direction targetFacing;
    @Getter
    private final int distance;

    public UniversalRoutePath(UniversalPipeBlockEntity targetPipe, Direction targetFacing, int distance) {
        this.targetPipe = targetPipe;
        this.targetFacing = targetFacing;
        this.distance = distance;
    }

    @Override
    public @NotNull BlockPos getTargetPipePos() {
        return targetPipe.getPipePos();
    }

    @Override
    public @NotNull Direction getTargetFacing() {
        return targetFacing;
    }

    @Override
    public @Nullable MEStorage getHandler(Level world) {
        return UniversalStorages.getStorage(targetPipe.getNeighborBlockEntity(targetFacing), targetFacing.getOpposite());
    }
}
