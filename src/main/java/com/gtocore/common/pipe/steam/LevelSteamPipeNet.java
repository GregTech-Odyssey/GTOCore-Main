package com.gtocore.common.pipe.steam;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public final class LevelSteamPipeNet extends LevelPipeNet<SteamPipeProperties, SteamPipeNet> {

    private static final String DATA_ID = "gtocore_steam_pipe_net";

    public static LevelSteamPipeNet getOrCreate(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(tag -> new LevelSteamPipeNet(serverLevel, tag),
                () -> new LevelSteamPipeNet(serverLevel), DATA_ID);
    }

    public LevelSteamPipeNet(ServerLevel level) {
        super(level);
    }

    public LevelSteamPipeNet(ServerLevel serverLevel, CompoundTag tag) {
        super(serverLevel, tag);
    }

    @Override
    protected SteamPipeNet createNetInstance() {
        return new SteamPipeNet(this);
    }
}
