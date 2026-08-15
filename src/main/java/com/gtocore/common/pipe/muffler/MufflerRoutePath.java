package com.gtocore.common.pipe.muffler;

import com.gtocore.common.blockentity.MufflerPipeBlockEntity;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.pipenet.IRoutePath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MufflerRoutePath implements IRoutePath<IMufflerConduction> {

    private final MufflerPipeBlockEntity targetPipe;
    private final Direction targetFacing;
    @Getter
    private final int distance;

    public MufflerRoutePath(MufflerPipeBlockEntity targetPipe, Direction targetFacing, int distance) {
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

    @Nullable
    @Override
    public IMufflerConduction getHandler(Level world) {
        return GTCapabilityHelper.getBlockEntityGTCapability(
                IMufflerConduction.class,
                targetPipe.getNeighborBlockEntity(targetFacing),
                targetFacing.getOpposite());
    }
}
