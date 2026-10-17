package com.gtocore.common.pipe.universal;

import com.gtocore.common.blockentity.UniversalPipeBlockEntity;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.pipenet.PipeNetWalker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class UniversalNetWalker extends PipeNetWalker<UniversalPipeBlockEntity, UniversalPipeProperties, UniversalPipeNet> {

    public static @Nullable List<UniversalRoutePath> createNetData(UniversalPipeNet pipeNet, BlockPos sourcePipe, Direction sourceFacing) {
        if (!(pipeNet.getLevel().getBlockEntity(sourcePipe) instanceof UniversalPipeBlockEntity)) {
            return null;
        }
        try {
            var walker = new UniversalNetWalker(pipeNet, sourcePipe, 1, new ArrayList<>());
            walker.sourcePipe = sourcePipe;
            walker.facingToHandler = sourceFacing;
            walker.traversePipeNet();
            return walker.routes;
        } catch (Exception e) {
            GTOCore.LOGGER.error("error while create net data for universal pipe net", e);
        }
        return null;
    }

    private final List<UniversalRoutePath> routes;
    private BlockPos sourcePipe;
    private Direction facingToHandler;

    private UniversalNetWalker(UniversalPipeNet pipeNet, BlockPos sourcePipe, int walkedBlocks, List<UniversalRoutePath> routes) {
        super(pipeNet, sourcePipe, walkedBlocks);
        this.routes = routes;
    }

    @NotNull
    @Override
    protected UniversalNetWalker createSubWalker(UniversalPipeNet pipeNet, Direction facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        var walker = new UniversalNetWalker(pipeNet, nextPos, walkedBlocks, routes);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = facingToHandler;
        return walker;
    }

    @Override
    protected void checkPipe(UniversalPipeBlockEntity pipeTile, BlockPos pos) {}

    @Override
    protected void checkNeighbour(UniversalPipeBlockEntity pipeTile, BlockPos pipePos, Direction faceToNeighbour,
                                  @Nullable BlockEntity neighbourTile) {
        if (pipePos.equals(sourcePipe) && faceToNeighbour == facingToHandler) return;
        if (UniversalStorages.getStorage(neighbourTile, faceToNeighbour.getOpposite()) != null) {
            routes.add(new UniversalRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks()));
        }
    }

    @Override
    protected Class<UniversalPipeBlockEntity> getBasePipeClass() {
        return UniversalPipeBlockEntity.class;
    }
}
