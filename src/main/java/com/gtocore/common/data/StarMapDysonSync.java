package com.gtocore.common.data;

import com.gtocore.common.saved.DysonSphereSavaedData;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.api.scope.GTOScopes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.util.ByteBufCodecExtends;

import com.suried.gtoutils.level.LevelStore;
import com.suried.gtoutils.schedule.Cadence;

/** 当前星系的模块数在登录、换维度和进度变化时同步；快照随 Level 一起释放。 */
public final class StarMapDysonSync {

    public static final int COMPLETE = 100_000;
    private static final LevelStore<Integer> LAUNCHED = LevelStore.create(() -> null);
    private static final NetworkPack PROGRESS = NetworkPack.registerS2C("gtocoreStarMapDysonProgress", (player, buf) -> {
        var dimension = ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.decode(buf);
        int launched = buf.readVarInt();
        if (player.level().dimension().location().equals(dimension)) {
            LAUNCHED.set(player.level(), launched);
            player.level().updateSkyBrightness();
        }
    });

    private StarMapDysonSync() {}

    public static void install() {
        GTOScopes.PRESENCE.install("dyson_sky", (scope, player) -> sync(player));
        GTOScopes.LEVEL.install("dyson_sky", (scope, level) -> {
            if (GTODimensions.getGalaxy(level.dimension()) != null) scope.repeat(Cadence.every(20), () -> poll(level));
        });
    }

    private static void sync(ServerPlayer player) {
        var level = player.serverLevel();
        send(level, Math.clamp(DysonSphereSavaedData.getDimensionLaunchData(level.dimension()), 0, COMPLETE), player);
    }

    private static void poll(ServerLevel level) {
        int launched = Math.clamp(DysonSphereSavaedData.getDimensionLaunchData(level.dimension()), 0, COMPLETE);
        var previous = LAUNCHED.getIfPresent(level);
        if (previous != null && previous == launched) return;
        LAUNCHED.set(level, launched);
        if (!level.players().isEmpty()) send(level, launched, level.players());
    }

    private static void send(ServerLevel level, int launched, Object recipients) {
        PROGRESS.send(out -> {
            ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.encode(out, level.dimension().location());
            out.writeVarInt(launched);
        }, recipients);
    }

    public static int launched(Level level) {
        var launched = LAUNCHED.getIfPresent(level);
        return launched == null ? 0 : launched;
    }
}
