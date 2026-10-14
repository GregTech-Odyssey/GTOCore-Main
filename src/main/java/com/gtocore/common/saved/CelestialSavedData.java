package com.gtocore.common.saved;

import com.gtocore.client.Message;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;

/**
 * 存档首次加载时随机的天体相位种子；玩家登录时同步到客户端。
 */
public final class CelestialSavedData extends SavedData {

    private static final String DATA_NAME = "gtocore_celestial";
    private static final String SEED = "seed";

    private final long seed;

    private CelestialSavedData(long seed) {
        this.seed = seed;
    }

    public long seed() {
        return seed;
    }

    public static CelestialSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(tag -> new CelestialSavedData(tag.getLong(SEED)), () -> {
            var data = new CelestialSavedData(RandomSource.create().nextLong());
            data.setDirty();
            return data;
        }, DATA_NAME);
    }

    public static void sync(ServerPlayer player) {
        long seed = get(player.server).seed;
        Message.CELESTIAL_SEED_S2C.send(buf -> buf.writeLong(seed), player);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putLong(SEED, seed);
        return tag;
    }
}
