package com.gtocore.api.research;

import com.gtolib.GTOCore;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.api.network.NetworkPack;
import com.gtolib.utils.iostream.DataIOStream;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.gto.fastcollection.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;

import java.io.IOException;
import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID;

public class TeamResearchSavedData extends FastSavedData {

    public static final String DATA_NAME = "team_research_data";
    public static final int DATA_VERSION = 4;
    public static TeamResearchSavedData INSTANCE = new TeamResearchSavedData();
    public static TeamResearchSavedData CLIENT_INSTANCE = new TeamResearchSavedData();

    private static boolean syncPending;

    private static final NetworkPack CLIENT_INSTANCE_SYNC = NetworkPack.registerS2C("teamResearchSavedDataSyncS2C",
            (objs, buf) -> {
                try {
                    INSTANCE.save(DataIOStream.of(buf));
                } catch (IOException exception) {
                    GTOCore.LOGGER.error("Failed to serialize team research data for synchronization", exception);
                }
            },
            (player, buffer) -> {
                try {
                    CLIENT_INSTANCE = load(DataIOStream.of(buffer));
                } catch (IOException | RuntimeException exception) {
                    GTOCore.LOGGER.error("Failed to synchronize team research data", exception);
                }
            });

    private final O2OOpenCacheHashMap<UUID, TeamResearchContext> teamResearchContexts = new O2OOpenCacheHashMap<>();

    public static void init() {}

    public static TeamResearchSavedData get() {
        return GTCEu.isClientThread() ? CLIENT_INSTANCE : INSTANCE;
    }

    @Override
    public void save(DataIOStream dataIOStream) throws IOException {
        dataIOStream.writeInt(teamResearchContexts.size());
        for (var entry : Object2ObjectMaps.fastIterable(teamResearchContexts)) {
            dataIOStream.writeUUID(entry.getKey());
            TeamResearchContext.writeContext(dataIOStream, entry.getValue());
        }
    }

    public static TeamResearchSavedData load(DataIOStream dataIOStream) throws IOException {
        TeamResearchSavedData savedData = new TeamResearchSavedData();
        int teamCount = dataIOStream.readInt();
        for (int i = 0; i < teamCount; i++) {
            UUID teamUUID = dataIOStream.readUUID();
            savedData.teamResearchContexts.put(teamUUID, TeamResearchContext.readContext(dataIOStream));
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
