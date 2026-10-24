package com.gtocore.common.pipe.mana;

import com.gtocore.common.blockentity.ManaPipeBlockEntity;

import com.gregtechceu.gtceu.api.pipenet.IRoutePath;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.xplat.XplatAbstractions;

public final class ManaRoutePath implements IRoutePath<ManaReceiver> {

    @Getter
    private final ManaPipeBlockEntity targetPipe;
    private final Direction targetFacing;
    @Getter
    private final int distance;

    public ManaRoutePath(ManaPipeBlockEntity targetPipe, Direction targetFacing, int distance) {
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
    public ManaReceiver getHandler(Level world) {
        // a paused pipe must not push mana into its neighbours
        if (!targetPipe.isWorkingEnabled()) return null;
        var blockEntity = targetPipe.getNeighborBlockEntity(targetFacing);
        return receiverOf(blockEntity, targetFacing.getOpposite());
    }

    @Nullable
    public static ManaReceiver receiverOf(@Nullable BlockEntity blockEntity, Direction side) {
        if (blockEntity == null || blockEntity.getLevel() == null) return null;
        return XplatAbstractions.INSTANCE.findManaReceiver(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, side);
    }

    public @NotNull Direction getTargetFacing() {
        return this.targetFacing;
    }
}
