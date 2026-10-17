package com.gtocore.common.pipe.universal;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public final class LevelUniversalPipeNet extends LevelPipeNet<UniversalPipeProperties, UniversalPipeNet> {

    private static final String DATA_ID = "gtocore_universal_pipe_net";

    public static LevelUniversalPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelUniversalPipeNet(serverLevel, tag),
                () -> new LevelUniversalPipeNet(serverLevel), DATA_ID);
    }

    public LevelUniversalPipeNet(ServerLevel level) {
        super(level);
    }

    public LevelUniversalPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected UniversalPipeNet createNetInstance() {
        return new UniversalPipeNet(this);
    }
}
