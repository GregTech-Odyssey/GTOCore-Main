package com.gtocore.common.pipe.steam;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import com.gto.fastcollection.LoopIterator;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

public final class SteamPipeNet extends PipeNet<SteamPipeProperties> {

    private final Long2ObjectOpenHashMap<LoopIterator<SteamRoutePath>> netData = new Long2ObjectOpenHashMap<>();

    public SteamPipeNet(LevelPipeNet<SteamPipeProperties, ? extends PipeNet<SteamPipeProperties>> world) {
        super(world);
    }

    @NotNull
    public LoopIterator<SteamRoutePath> getNetData(long pipePos, BlockPos pos) {
        var path = netData.get(pipePos);
        if (path != null) return path;
        var paths = SteamNetWalker.createNetData(this, pos);
        if (paths == null) return LoopIterator.empty();
        path = new LoopIterator<>(paths.toArray(new SteamRoutePath[0]));
        netData.put(pipePos, path);
        return path;
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
    protected void transferNodeData(Long2ObjectOpenHashMap<Node<SteamPipeProperties>> transferredNodes,
                                    PipeNet<SteamPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        netData.clear();
        ((SteamPipeNet) parentNet).netData.clear();
    }

    @Override
    protected void writeNodeData(SteamPipeProperties nodeData, CompoundTag tagCompound) {}

    @Override
    protected SteamPipeProperties readNodeData(CompoundTag tagCompound) {
        return SteamPipeProperties.INSTANCE;
    }
}
