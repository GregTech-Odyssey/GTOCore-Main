package com.gtocore.common.pipe.steam;

import com.gtocore.common.blockentity.SteamPipeBlockEntity;

import com.gregtechceu.gtceu.api.pipenet.IRoutePath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import appeng.api.storage.MEStorage;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SteamRoutePath implements IRoutePath<MEStorage> {

    private final SteamPipeBlockEntity targetPipe;
    private final Direction targetFacing;
    @Getter
    private final int distance;

    public SteamRoutePath(SteamPipeBlockEntity targetPipe, Direction targetFacing, int distance) {
        this.targetPipe = targetPipe;
        this.targetFacing = targetFacing;
        this.distance = distance;
    }

    @Override
    @NotNull
    public BlockPos getTargetPipePos() {
        return targetPipe.getPipePos();
    }

    @Nullable
    @Override
    public MEStorage getHandler(Level world) {
        // a paused pipe must not push anything into its neighbours
        if (!targetPipe.isWorkingEnabled()) return null;
        return SteamStorages.getStorage(targetPipe.getNeighborBlockEntity(targetFacing), targetFacing.getOpposite());
    }

    public @NotNull Direction getTargetFacing() {
        return this.targetFacing;
    }
}
