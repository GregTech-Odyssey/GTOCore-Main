package com.gtocore.common.pipe.mana;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import com.gto.fastcollection.LoopIterator;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

public final class ManaPipeNet extends PipeNet<ManaPipeProperties> {

    private final Long2ObjectOpenHashMap<LoopIterator<ManaRoutePath>> manaNetData = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<LoopIterator<SourceRoutePath>> sourceNetData = new Long2ObjectOpenHashMap<>();

    public ManaPipeNet(LevelPipeNet<ManaPipeProperties, ? extends PipeNet<ManaPipeProperties>> world) {
        super(world);
    }

    @NotNull
    public LoopIterator<ManaRoutePath> getNetData(long pipePos, BlockPos pos) {
        var data = manaNetData.get(pipePos);
        if (data != null) return data;
        var routes = ManaNetWalker.createNetData(this, pos);
        if (routes == null) return LoopIterator.empty();
        data = new LoopIterator<>(routes.mana());
        manaNetData.put(pipePos, data);
        sourceNetData.put(pipePos, new LoopIterator<>(routes.source()));
        return data;
    }

    @NotNull
    public LoopIterator<SourceRoutePath> getSourceNetData(long pipePos, BlockPos pos) {
        var data = sourceNetData.get(pipePos);
        if (data == null) {
            getNetData(pipePos, pos);
            data = sourceNetData.get(pipePos);
        }
        return data == null ? LoopIterator.empty() : data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        clearNetData();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        clearNetData();
    }

    @Override
    protected void transferNodeData(Long2ObjectOpenHashMap<Node<ManaPipeProperties>> transferredNodes,
                                    PipeNet<ManaPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        clearNetData();
        ((ManaPipeNet) parentNet).clearNetData();
    }

    private void clearNetData() {
        manaNetData.clear();
        sourceNetData.clear();
    }

    @Override
    protected void writeNodeData(ManaPipeProperties nodeData, CompoundTag tagCompound) {
        tagCompound.putLong("ManaPerSecond", nodeData.manaPerSecond());
    }

    @Override
    protected ManaPipeProperties readNodeData(CompoundTag tagCompound) {
        return new ManaPipeProperties(tagCompound.getLong("ManaPerSecond"));
    }
}
