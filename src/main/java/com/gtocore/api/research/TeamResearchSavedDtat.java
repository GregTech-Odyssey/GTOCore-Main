package com.gtocore.api.research;

import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.world.entity.player.Player;

import com.gto.fastcollection.O2OOpenCacheHashMap;
import com.hepdd.gtmthings.utils.TeamUtil;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class TeamResearchSavedDtat extends FastSavedData {

    public static final String DATA_NAME = "team_research_data";
    public static final int DATA_VERSION = 1;
    public static TeamResearchSavedDtat INSTANCE = new TeamResearchSavedDtat();

    private final Map<UUID, Map<ResearchTag, Long>> teamResearchPoints = new O2OOpenCacheHashMap<>();

    @Override
    public void save(DataIOStream dataIOStream) throws IOException {
        dataIOStream.writeInt(teamResearchPoints.size());
        for (Map.Entry<UUID, Map<ResearchTag, Long>> entry : teamResearchPoints.entrySet()) {
            dataIOStream.writeUUID(entry.getKey());
            Map<ResearchTag, Long> researchPoints = entry.getValue();
            dataIOStream.writeInt(researchPoints.size());
            for (Map.Entry<ResearchTag, Long> researchEntry : researchPoints.entrySet()) {
                dataIOStream.writeUTF(researchEntry.getKey().name());
                dataIOStream.writeLong(researchEntry.getValue());
            }
        }
    }

    public static TeamResearchSavedDtat load(DataIOStream dataIOStream, int dataVersion) throws IOException {
        TeamResearchSavedDtat savedData = new TeamResearchSavedDtat();
        int teamCount = dataIOStream.readInt();
        for (int i = 0; i < teamCount; i++) {
            UUID teamUUID = dataIOStream.readUUID();
            int researchCount = dataIOStream.readInt();
            Map<ResearchTag, Long> researchPoints = new O2OOpenCacheHashMap<>();
            for (int j = 0; j < researchCount; j++) {
                String tagName = dataIOStream.readUTF();
                long points = dataIOStream.readLong();
                ResearchTag tag = ResearchTag.TAGS.get(tagName);
                if (tag != null) {
                    researchPoints.put(tag, points);
                }
            }
            savedData.teamResearchPoints.put(teamUUID, researchPoints);
        }
        return savedData;
    }

    public static Map<ResearchTag, Long> getForPlayer(Player player) {
        return INSTANCE.teamResearchPoints.getOrDefault(TeamUtil.getTeamUUID(player.getUUID()), new O2OOpenCacheHashMap<>());
    }
}
