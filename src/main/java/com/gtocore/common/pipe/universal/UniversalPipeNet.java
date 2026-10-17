package com.gtocore.common.pipe.universal;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import com.gto.fastcollection.LoopIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

public final class UniversalPipeNet extends PipeNet<UniversalPipeProperties> {

    private final Long2ObjectOpenHashMap<LoopIterator<UniversalRoutePath>> netData = new Long2ObjectOpenHashMap<>();

    public UniversalPipeNet(LevelPipeNet<UniversalPipeProperties, ? extends PipeNet<UniversalPipeProperties>> world) {
        super(world);
    }

    @NotNull
    public LoopIterator<UniversalRoutePath> getNetData(long pipePos, BlockPos pos, Direction facing) {
        long key = pipePos * 8 + facing.ordinal();
        var data = netData.get(key);
        if (data != null) return data;
        var routes = UniversalNetWalker.createNetData(this, pos, facing);
        if (routes == null) {
            return LoopIterator.empty();
        }
        data = new LoopIterator<>(routes.toArray(new UniversalRoutePath[0]));
        netData.put(key, data);
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        netData.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        netData.clear();
    }

    @Override
    protected void transferNodeData(Long2ObjectOpenHashMap<Node<UniversalPipeProperties>> transferredNodes,
                                    PipeNet<UniversalPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        netData.clear();
        ((UniversalPipeNet) parentNet).netData.clear();
    }

    @Override
    protected void writeNodeData(UniversalPipeProperties nodeData, CompoundTag tagCompound) {
        tagCompound.putLong("ItemThroughput", nodeData.itemThroughput());
        tagCompound.putLong("FluidThroughput", nodeData.fluidThroughput());
    }

    @Override
    protected UniversalPipeProperties readNodeData(CompoundTag tagCompound) {
        return new UniversalPipeProperties(tagCompound.getLong("ItemThroughput"), tagCompound.getLong("FluidThroughput"));
    }
}
