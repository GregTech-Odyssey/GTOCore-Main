package com.gtocore.common.pipe.muffler;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

public final class MufflerPipeNet extends PipeNet<MufflerPipeProperties> {

    private final Long2ObjectOpenHashMap<MufflerRoutePath> netData = new Long2ObjectOpenHashMap<>();

    public MufflerPipeNet(LevelPipeNet<MufflerPipeProperties, ? extends PipeNet<MufflerPipeProperties>> world) {
        super(world);
    }

    @Nullable
    public MufflerRoutePath getNetData(long pipePos, BlockPos pos) {
        MufflerRoutePath cachedPath = netData.get(pipePos);
        if (cachedPath != null) return cachedPath;
        MufflerRoutePath path = MufflerNetWalker.createNetData(this, pos);
        if (path != null) {
            netData.put(pipePos, path);
        }
        return path;
    }

    @Nullable
    public IMufflerConduction getMufflerOutput(Level level, long pipePos, BlockPos pos) {
        MufflerRoutePath path = getNetData(pipePos, pos);
        IMufflerConduction conduction = getValidOutput(level, path);
        if (conduction != null) return conduction;
        if (path != null) {
            netData.remove(pipePos);
            return getValidOutput(level, getNetData(pipePos, pos));
        }
        return null;
    }

    @Nullable
    private static IMufflerConduction getValidOutput(Level level, @Nullable MufflerRoutePath path) {
        if (path == null) return null;
        IMufflerConduction conduction = path.getHandler(level);
        if (conduction == null || conduction.isMufflerSource() || !conduction.shouldWorkAsMufflerSource()) {
            return null;
        }
        return conduction;
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
    protected void transferNodeData(Long2ObjectOpenHashMap<Node<MufflerPipeProperties>> transferredNodes,
                                    PipeNet<MufflerPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        netData.clear();
        ((MufflerPipeNet) parentNet).netData.clear();
    }

    @Override
    protected void writeNodeData(MufflerPipeProperties nodeData, CompoundTag tagCompound) {}

    @Override
    protected MufflerPipeProperties readNodeData(CompoundTag tagCompound) {
        return MufflerPipeProperties.INSTANCE;
    }
}
