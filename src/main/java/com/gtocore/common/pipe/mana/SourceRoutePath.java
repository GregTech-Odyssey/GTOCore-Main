package com.gtocore.common.pipe.mana;

import com.gtocore.common.blockentity.ManaPipeBlockEntity;

import com.gregtechceu.gtceu.api.pipenet.IRoutePath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SourceRoutePath implements IRoutePath<ISourceTile> {

    @Getter
    private final ManaPipeBlockEntity targetPipe;
    private final Direction targetFacing;
    @Getter
    private final int distance;

    public SourceRoutePath(ManaPipeBlockEntity targetPipe, Direction targetFacing, int distance) {
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
    public ISourceTile getHandler(Level world) {
        // a paused pipe must not push source into its neighbours
        if (!targetPipe.isWorkingEnabled()) return null;
        return targetPipe.getNeighborBlockEntity(targetFacing) instanceof ISourceTile sourceTile ? sourceTile : null;
    }

    public @NotNull Direction getTargetFacing() {
        return this.targetFacing;
    }
}
