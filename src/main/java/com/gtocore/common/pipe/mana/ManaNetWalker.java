package com.gtocore.common.pipe.mana;

import com.gtocore.common.blockentity.ManaPipeBlockEntity;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.pipenet.PipeNetWalker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;

import java.util.ArrayList;
import java.util.List;

public final class ManaNetWalker extends PipeNetWalker<ManaPipeBlockEntity, ManaPipeProperties, ManaPipeNet> {

    public record Routes(ManaRoutePath[] mana, SourceRoutePath[] source) {}

    @Nullable
    public static Routes createNetData(ManaPipeNet pipeNet, BlockPos sourcePipe) {
        try {
            var walker = new ManaNetWalker(pipeNet, sourcePipe, 1, new ArrayList<>(), new ArrayList<>());
            walker.traversePipeNet();
            return new Routes(walker.manaRoutes.toArray(new ManaRoutePath[0]),
                    walker.sourceRoutes.toArray(new SourceRoutePath[0]));
        } catch (Exception e) {
            GTOCore.LOGGER.error("error while create net data for pipe net", e);
        }
        return null;
    }

    private final List<ManaRoutePath> manaRoutes;
    private final List<SourceRoutePath> sourceRoutes;
    private ManaPipeBlockEntity[] pipes = {};

    private ManaNetWalker(ManaPipeNet pipeNet, BlockPos sourcePipe, int walkedBlocks,
                          List<ManaRoutePath> manaRoutes, List<SourceRoutePath> sourceRoutes) {
        super(pipeNet, sourcePipe, walkedBlocks);
        this.manaRoutes = manaRoutes;
        this.sourceRoutes = sourceRoutes;
    }

    @NotNull
    @Override
    protected ManaNetWalker createSubWalker(ManaPipeNet pipeNet, Direction facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        var walker = new ManaNetWalker(pipeNet, nextPos, walkedBlocks, manaRoutes, sourceRoutes);
        walker.pipes = pipes;
        return walker;
    }

    @Override
    protected void checkPipe(ManaPipeBlockEntity pipeTile, BlockPos pos) {
        pipes = ArrayUtils.add(pipes, pipeTile);
    }

    @Override
    protected void checkNeighbour(ManaPipeBlockEntity pipeTile, BlockPos pipePos, Direction faceToNeighbour,
                                  @Nullable BlockEntity neighbourTile) {
        // assert that the last added pipe is the current pipe
        if (pipeTile != pipes[pipes.length - 1]) throw new IllegalStateException("The current pipe is not the last added pipe. Something went seriously wrong!");
        if (neighbourTile == null) return;
        var opposite = faceToNeighbour.getOpposite();
        if (GTCapabilityHelper.getBlockEntityCapability(BotaniaForgeCapabilities.MANA_RECEIVER, neighbourTile, opposite) != null) {
            manaRoutes.add(new ManaRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks()));
        }
        if (neighbourTile instanceof ISourceTile) {
            sourceRoutes.add(new SourceRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks()));
        }
    }

    @Override
    protected Class<ManaPipeBlockEntity> getBasePipeClass() {
        return ManaPipeBlockEntity.class;
    }
}
