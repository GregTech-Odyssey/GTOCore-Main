package com.gtocore.common.data;

import com.gtocore.common.saved.DysonSphereSavaedData;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.network.NetworkPack;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import com.gto.datasynclib.datastream.DataComponentKey;
import com.gto.datasynclib.util.ByteBufCodecExtends;

/** 当前星系的模块数在登录、换维度和进度变化时同步；快照随 Level 一起释放。 */
public final class StarMapDysonSync {

    public static final int COMPLETE = 100_000;
    private static final DataComponentKey<Integer> LAUNCHED = DataComponentKey.createNoCodec("starmap_dyson_launched");
    private static final NetworkPack PROGRESS = NetworkPack.registerS2C("gtocoreStarMapDysonProgress", (player, buf) -> {
        var dimension = ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.decode(buf);
        int launched = buf.readVarInt();
        if (player.level().dimension().location().equals(dimension)) {
            ILevel.setCapability(player.level(), LAUNCHED, launched);
            player.level().updateSkyBrightness();
        }
    });

    private StarMapDysonSync() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(StarMapDysonSync::onLogin);
        MinecraftForge.EVENT_BUS.addListener(StarMapDysonSync::onChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(StarMapDysonSync::onRespawn);
        MinecraftForge.EVENT_BUS.addListener(StarMapDysonSync::onLevelTick);
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    private static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    private static void sync(ServerPlayer player) {
        var level = player.serverLevel();
        send(level, Math.clamp(DysonSphereSavaedData.getDimensionLaunchData(level.dimension()), 0, COMPLETE), player);
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || level.getGameTime() % 20 != 0 ||
                GTODimensions.getGalaxy(level.dimension()) == null)
            return;
        int launched = Math.clamp(DysonSphereSavaedData.getDimensionLaunchData(level.dimension()), 0, COMPLETE);
        var previous = ILevel.getCapability(level, LAUNCHED);
        if (previous != null && previous == launched) return;
        ILevel.setCapability(level, LAUNCHED, launched);
        if (!level.players().isEmpty()) send(level, launched, level.players());
    }

    private static void send(ServerLevel level, int launched, Object recipients) {
        PROGRESS.send(out -> {
            ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.encode(out, level.dimension().location());
            out.writeVarInt(launched);
        }, recipients);
    }

    public static int launched(Level level) {
        var launched = ILevel.getCapability(level, LAUNCHED);
        return launched == null ? 0 : launched;
    }
}
