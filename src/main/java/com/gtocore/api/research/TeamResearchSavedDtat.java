package com.gtocore.api.research;

import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.world.entity.player.Player;

import com.gto.fastcollection.O2OOpenCacheHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID;

public class TeamResearchSavedDtat extends FastSavedData {

    public static final String DATA_NAME = "team_research_data";
    public static final int DATA_VERSION = 3;
    public static TeamResearchSavedDtat INSTANCE = new TeamResearchSavedDtat();

    private final Map<UUID, TeamResearchContext> teamResearchContexts = new O2OOpenCacheHashMap<>();

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
        return INSTANCE.teamResearchContexts.computeIfAbsent(getTeamUUID(uuid), ignored -> new TeamResearchContext());
    }
}
