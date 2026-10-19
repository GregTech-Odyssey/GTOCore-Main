package com.gtocore.common.pipe.steam;

import com.gtocore.common.blockentity.SteamPipeBlockEntity;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.pipenet.PipeNetWalker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class SteamNetWalker extends PipeNetWalker<SteamPipeBlockEntity, SteamPipeProperties, SteamPipeNet> {

    @Nullable
    public static List<SteamRoutePath> createNetData(SteamPipeNet pipeNet, BlockPos sourcePipe) {
        try {
            var walker = new SteamNetWalker(pipeNet, sourcePipe, 1, new ArrayList<>());
            walker.traversePipeNet();
            return walker.routes;
        } catch (Exception e) {
            GTOCore.LOGGER.error("error while create net data for pipe net", e);
        }
        return null;
    }

    private final List<SteamRoutePath> routes;
    private SteamPipeBlockEntity[] pipes = {};

    private SteamNetWalker(SteamPipeNet pipeNet, BlockPos sourcePipe, int walkedBlocks, List<SteamRoutePath> routes) {
        super(pipeNet, sourcePipe, walkedBlocks);
        this.routes = routes;
    }

    @NotNull
    @Override
    protected SteamNetWalker createSubWalker(SteamPipeNet pipeNet, Direction facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        var walker = new SteamNetWalker(pipeNet, nextPos, walkedBlocks, routes);
        walker.pipes = pipes;
        return walker;
    }

    @Override
    protected void checkPipe(SteamPipeBlockEntity pipeTile, BlockPos pos) {
        pipes = ArrayUtils.add(pipes, pipeTile);
    }

    @Override
    protected void checkNeighbour(SteamPipeBlockEntity pipeTile, BlockPos pipePos, Direction faceToNeighbour, @Nullable BlockEntity neighbourTile) {
        if (pipeTile != pipes[pipes.length - 1]) throw new IllegalStateException("The current pipe is not the last added pipe. Something went seriously wrong!");
        if (neighbourTile == null) return;
        if (SteamStorages.hasStorage(neighbourTile, faceToNeighbour.getOpposite())) {
            routes.add(new SteamRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks()));
        }
    }

    @Override
    protected Class<SteamPipeBlockEntity> getBasePipeClass() {
        return SteamPipeBlockEntity.class;
    }
}
