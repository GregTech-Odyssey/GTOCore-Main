package com.gtocore.common.pipe.muffler;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public final class LevelMufflerPipeNet extends LevelPipeNet<MufflerPipeProperties, MufflerPipeNet> {

    private static final String DATA_ID = "gtocore_muffler_pipe_net";

    public static LevelMufflerPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(
                tag -> new LevelMufflerPipeNet(serverLevel, tag),
                () -> new LevelMufflerPipeNet(serverLevel),
                DATA_ID);
    }

    public LevelMufflerPipeNet(ServerLevel level) {
        super(level);
    }

    public LevelMufflerPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected MufflerPipeNet createNetInstance() {
        return new MufflerPipeNet(this);
    }
}
