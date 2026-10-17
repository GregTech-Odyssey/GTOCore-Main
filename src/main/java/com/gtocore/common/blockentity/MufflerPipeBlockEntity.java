package com.gtocore.common.blockentity;

import com.gtocore.common.pipe.muffler.IMufflerConduction;
import com.gtocore.common.pipe.muffler.IMufflerPipeConnection;
import com.gtocore.common.pipe.muffler.LevelMufflerPipeNet;
import com.gtocore.common.pipe.muffler.MufflerPipeNet;
import com.gtocore.common.pipe.muffler.MufflerPipeProperties;
import com.gtocore.common.pipe.muffler.MufflerPipeType;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class MufflerPipeBlockEntity extends PipeBlockEntity<MufflerPipeType, MufflerPipeProperties>
                                          implements IMufflerPipeConnection {

    private WeakReference<MufflerPipeNet> currentPipeNet = new WeakReference<>(null);

    public MufflerPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    @Override
    public @Nullable <T> Object getGTCapability(@NotNull Class<T> cap, @Nullable Direction side) {
        if (cap == IMufflerPipeConnection.class) {
            if ((side == null || isConnected(side)) && !level.isClientSide && getMufflerPipeNet() != null) {
                return this;
            }
            return GTCapability.EMPTY;
        }
        return super.getGTCapability(cap, side);
    }

    @Override
    public boolean isConnectedToMufflerNet() {
        return getMufflerPipeNet() != null;
    }

    @Override
    public @Nullable IMufflerConduction getMufflerOutput() {
        MufflerPipeNet pipeNet = getMufflerPipeNet();
        return pipeNet == null || level == null ? null : pipeNet.getMufflerOutput(level, getPipeLongPos(), getPipePos());
    }

    @Nullable
    private MufflerPipeNet getMufflerPipeNet() {
        if (level == null || level.isClientSide) return null;
        MufflerPipeNet pipeNet = this.currentPipeNet.get();
        if (pipeNet != null && pipeNet.isValid() && pipeNet.containsNode(getPipeLongPos())) return pipeNet;
        LevelMufflerPipeNet worldNet = (LevelMufflerPipeNet) getPipeBlock().getWorldPipeNet((ServerLevel) getLevel());
        pipeNet = worldNet.getNetFromPos(getPipePos(), getPipeLongPos());
        if (pipeNet != null) {
            this.currentPipeNet = new WeakReference<>(pipeNet);
        }
        return pipeNet;
    }
}
