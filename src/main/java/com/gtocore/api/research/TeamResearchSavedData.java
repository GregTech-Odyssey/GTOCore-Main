package com.gtocore.api.research;

import com.gtolib.GTOCore;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.api.network.NetworkPack;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.gto.datasynclib.util.VersionedFriendlyByteBuf;
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID;

public class TeamResearchSavedData extends FastSavedData {

    public static final String DATA_NAME = "team_research_data";
    public static final int DATA_VERSION = 4;
    public static final int VERSION = 1;
    public static TeamResearchSavedData INSTANCE = new TeamResearchSavedData();
    public static TeamResearchSavedData CLIENT_INSTANCE = new TeamResearchSavedData();

    private static boolean syncPending;

    private static final NetworkPack CLIENT_INSTANCE_SYNC = NetworkPack.registerS2C("teamResearchSavedDataSyncS2C",
            (objs, buf) -> INSTANCE.save(buf),
            (player, buf) -> CLIENT_INSTANCE = read(buf));

    private final O2OOpenCacheHashMap<UUID, TeamResearchContext> teamResearchContexts = new O2OOpenCacheHashMap<>();

    public static void init() {}

    public static TeamResearchSavedData get() {
        return GTCEu.isClientThread() ? CLIENT_INSTANCE : INSTANCE;
    }

    @Override
    public int version() {
        return VERSION;
    }

    @Override
    public void save(FriendlyByteBuf buf) {
        buf.writeInt(teamResearchContexts.size());
        for (var entry : Object2ObjectMaps.fastIterable(teamResearchContexts)) {
            buf.writeUUID(entry.getKey());
            TeamResearchContext.writeContext(buf, entry.getValue());
        }
    }

    public static TeamResearchSavedData load(VersionedFriendlyByteBuf stream) {
        return read(stream);
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "0.7.0")
    public static TeamResearchSavedData loadLegacy(FriendlyByteBuf buf) {
        TeamResearchSavedData savedData = new TeamResearchSavedData();
        int teamCount = buf.readInt();
        for (int i = 0; i < teamCount; i++) {
            UUID teamUUID = buf.readUUID();
            savedData.teamResearchContexts.put(teamUUID, TeamResearchContext.readLegacyContext(buf));
        }
        GTOCore.LOGGER.info("Migrated {} team research contexts from the pre-versioned save format", teamCount);
        return savedData;
    }

    private static TeamResearchSavedData read(FriendlyByteBuf buf) {
        TeamResearchSavedData savedData = new TeamResearchSavedData();
        int teamCount = buf.readInt();
        for (int i = 0; i < teamCount; i++) {
            UUID teamUUID = buf.readUUID();
            savedData.teamResearchContexts.put(teamUUID, TeamResearchContext.readContext(buf));
        }
        return savedData;
    }

    public static TeamResearchContext getOrCreateContext(Player player) {
        return getOrCreateContext(player.getUUID());
    }

    public static TeamResearchContext getOrCreateContext(UUID uuid) {
        return get().teamResearchContexts.computeIfAbsent(getTeamUUID(uuid), ignored -> new TeamResearchContext());
    }

    public static void sync(ServerPlayer player) {
        sendSnapshot(player);
    }

    public static void syncIfNeeded(MinecraftServer server) {
        if (!syncPending || server.getPlayerList().getPlayerCount() == 0) return;
        syncPending = false;
        sendSnapshot(server);
    }

    public static void clearClientInstance() {
        CLIENT_INSTANCE = new TeamResearchSavedData();
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        if (dirty && this == INSTANCE) {
            syncPending = true;
        }
    }

    private static void sendSnapshot(Object recipient) {
        CLIENT_INSTANCE_SYNC.send(recipient);
    }
}
