package com.gtocore.common.pipe.muffler;

import com.gtocore.common.blockentity.MufflerPipeBlockEntity;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.pipenet.PipeNetWalker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MufflerNetWalker extends PipeNetWalker<MufflerPipeBlockEntity, MufflerPipeProperties, MufflerPipeNet> {

    @Nullable
    public static MufflerRoutePath createNetData(MufflerPipeNet pipeNet, BlockPos sourcePipe) {
        try {
            MufflerNetWalker walker = new MufflerNetWalker(pipeNet, sourcePipe, 1);
            walker.traversePipeNet();
            return walker.routePath;
        } catch (Exception e) {
            GTOCore.LOGGER.error("error while checking muffler pipe net output", e);
        }
        return null;
    }

    private MufflerRoutePath routePath;

    private MufflerNetWalker(MufflerPipeNet pipeNet, BlockPos sourcePipe, int walkedBlocks) {
        super(pipeNet, sourcePipe, walkedBlocks);
    }

    @Override
    protected @NotNull MufflerNetWalker createSubWalker(MufflerPipeNet pipeNet, Direction facingToNextPos,
                                                        BlockPos nextPos, int walkedBlocks) {
        return new MufflerNetWalker(pipeNet, nextPos, walkedBlocks);
    }

    @Override
    protected void checkPipe(MufflerPipeBlockEntity pipeTile, BlockPos pos) {}

    @Override
    protected void checkNeighbour(MufflerPipeBlockEntity pipeTile, BlockPos pipePos, Direction faceToNeighbour,
                                  @Nullable BlockEntity neighbourTile) {
        IMufflerConduction conduction = GTCapabilityHelper.getBlockEntityGTCapability(
                IMufflerConduction.class,
                neighbourTile,
                faceToNeighbour.getOpposite());
        if (((MufflerNetWalker) root).routePath == null &&
                conduction != null &&
                !conduction.isMufflerSource() &&
                conduction.shouldWorkAsMufflerSource()) {
            ((MufflerNetWalker) root).routePath = new MufflerRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks());
            stop();
        }
    }

    @Override
    protected Class<MufflerPipeBlockEntity> getBasePipeClass() {
        return MufflerPipeBlockEntity.class;
    }
}
