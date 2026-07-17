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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID;

public class TeamResearchSavedDtat extends FastSavedData {

    public static final String DATA_NAME = "team_research_data";
    public static final int DATA_VERSION = 3;
    public static TeamResearchSavedDtat INSTANCE = new TeamResearchSavedDtat();
    public static TeamResearchSavedDtat CLIENT_INSTANCE = new TeamResearchSavedDtat();

    private static boolean syncPending;

    private static final NetworkPack CLIENT_INSTANCE_SYNC = NetworkPack.registerS2C("teamResearchSavedDataSyncS2C", (player, buffer) -> {
        try (var stream = DataIOStream.of(new ByteArrayInputStream(buffer.readByteArray()))) {
            CLIENT_INSTANCE = load(stream, DATA_VERSION);
        } catch (IOException | RuntimeException exception) {
            GTOCore.LOGGER.error("Failed to synchronize team research data", exception);
        }
    });

    private final Map<UUID, TeamResearchContext> teamResearchContexts = new O2OOpenCacheHashMap<>();

    public static void init() {}

    public static TeamResearchSavedDtat get() {
        return GTCEu.isClientThread() ? CLIENT_INSTANCE : INSTANCE;
    }

    @Override
    public void save(DataIOStream dataIOStream) throws IOException {
        dataIOStream.writeInt(teamResearchContexts.size());
        for (Map.Entry<UUID, TeamResearchContext> entry : teamResearchContexts.entrySet()) {
            dataIOStream.writeUUID(entry.getKey());
            TeamResearchContext.writeContext(dataIOStream, entry.getValue());
        }
    }

    public static TeamResearchSavedDtat load(DataIOStream dataIOStream, int dataVersion) throws IOException {
        TeamResearchSavedDtat savedData = new TeamResearchSavedDtat();
        int teamCount = dataIOStream.readInt();
        for (int i = 0; i < teamCount; i++) {
            UUID teamUUID = dataIOStream.readUUID();
            savedData.teamResearchContexts.put(teamUUID, TeamResearchContext.readContext(dataIOStream, dataVersion));
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
        CLIENT_INSTANCE = new TeamResearchSavedDtat();
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
        if (dirty && this == INSTANCE) {
            syncPending = true;
        }
    }

    private static void sendSnapshot(Object recipient) {
        try {
            byte[] payload = encodeSnapshot();
            CLIENT_INSTANCE_SYNC.send(buffer -> buffer.writeByteArray(payload), recipient);
        } catch (IOException exception) {
            GTOCore.LOGGER.error("Failed to serialize team research data for synchronization", exception);
        }
    }

    private static byte[] encodeSnapshot() throws IOException {
        var output = new ByteArrayOutputStream();
        try (var stream = DataIOStream.of(output)) {
            INSTANCE.save(stream);
            stream.flush();
        }
        return output.toByteArray();
    }
}
