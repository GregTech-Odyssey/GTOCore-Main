package com.gtocore.api.research;

import com.gtocore.api.techtree.TechNode;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.data.recipe.research.AnalyzeData;

import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.world.entity.player.Player;

import appeng.api.stacks.AEKey;

import com.gto.datasynclib.datasream.data.Data;
import com.gto.fastcollection.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.hepdd.gtmthings.utils.TeamUtil.getTeamUUID;

public class TeamResearchSavedDtat extends FastSavedData {

    public static final String DATA_NAME = "team_research_data";
    public static final int DATA_VERSION = 1;
    public static TeamResearchSavedDtat INSTANCE = new TeamResearchSavedDtat();

    private final Map<UUID, TeamResearchContext> teamResearchContexts = new O2OOpenCacheHashMap<>();

    @Override
    public void save(DataIOStream dataIOStream) throws IOException {
        dataIOStream.writeInt(teamResearchContexts.size());
        for (Map.Entry<UUID, TeamResearchContext> entry : teamResearchContexts.entrySet()) {
            dataIOStream.writeUUID(entry.getKey());
            writeContext(dataIOStream, entry.getValue());
        }
    }

    public static TeamResearchSavedDtat load(DataIOStream dataIOStream, int dataVersion) throws IOException {
        TeamResearchSavedDtat savedData = new TeamResearchSavedDtat();
        int teamCount = dataIOStream.readInt();
        for (int i = 0; i < teamCount; i++) {
            UUID teamUUID = dataIOStream.readUUID();
            savedData.teamResearchContexts.put(teamUUID, readContext(dataIOStream));
        }
        return savedData;
    }

    public static TeamResearchContext getOrCreateContext(Player player) {
        return getOrCreateContext(player.getUUID());
    }

    public static TeamResearchContext getOrCreateContext(UUID uuid) {
        return INSTANCE.teamResearchContexts.computeIfAbsent(getTeamUUID(uuid), ignored -> new TeamResearchContext());
    }

    private static void writeContext(DataIOStream dataIOStream, TeamResearchContext context) throws IOException {
        writeResearchPoints(dataIOStream, context.getResearchPoints());
        writeScannedItems(dataIOStream, context.getScannedItems());
        writeTechNodeAccCWU(dataIOStream, context.getTechNodeAccCWU());
    }

    private static TeamResearchContext readContext(DataIOStream dataIOStream) throws IOException {
        return new TeamResearchContext(
                readResearchPoints(dataIOStream),
                readScannedItems(dataIOStream),
                readTechNodeAccCWU(dataIOStream));
    }

    private static void writeResearchPoints(DataIOStream dataIOStream, Map<ResearchTag, Long> researchPoints) throws IOException {
        dataIOStream.writeInt(researchPoints.size());
        for (Map.Entry<ResearchTag, Long> researchEntry : researchPoints.entrySet()) {
            dataIOStream.writeUTF(researchEntry.getKey().name());
            dataIOStream.writeLong(researchEntry.getValue());
        }
    }

    private static Map<ResearchTag, Long> readResearchPoints(DataIOStream dataIOStream) throws IOException {
        int researchCount = dataIOStream.readInt();
        Map<ResearchTag, Long> researchPoints = new O2OOpenCacheHashMap<>();
        for (int i = 0; i < researchCount; i++) {
            String tagName = dataIOStream.readUTF();
            long points = dataIOStream.readLong();
            ResearchTag tag = ResearchTag.TAGS.get(tagName);
            if (tag != null) {
                researchPoints.put(tag, points);
            }
        }
        return researchPoints;
    }

    private static void writeScannedItems(DataIOStream dataIOStream, Set<AEKey> scannedItems) throws IOException {
        dataIOStream.writeInt(scannedItems.size());
        for (AEKey item : scannedItems) {
            dataIOStream.writeByteArray(GTOCodecs.AE_KEY_DATA_CODEC.encode(item).writeToBytes());
        }
    }

    private static Set<AEKey> readScannedItems(DataIOStream dataIOStream) throws IOException {
        int scannedItemCount = dataIOStream.readInt();
        Set<AEKey> scannedItems = new ReferenceOpenHashSet<>();
        for (int i = 0; i < scannedItemCount; i++) {
            byte[] itemData = dataIOStream.readByteArray();
            AEKey item = GTOCodecs.AE_KEY_DATA_CODEC.decode(Data.readData(itemData));
            if (item != null) {
                scannedItems.add(item);
            }
        }
        return scannedItems;
    }

    private static void writeTechNodeAccCWU(DataIOStream dataIOStream, Map<TechNode<?>, Long> techNodeAccCWU) throws IOException {
        dataIOStream.writeInt(techNodeAccCWU.size());
        for (Map.Entry<TechNode<?>, Long> techNodeEntry : techNodeAccCWU.entrySet()) {
            dataIOStream.writeUTF(techNodeEntry.getKey().name);
            dataIOStream.writeLong(techNodeEntry.getValue());
        }
    }

    private static Map<TechNode<?>, Long> readTechNodeAccCWU(DataIOStream dataIOStream) throws IOException {
        int techNodeCount = dataIOStream.readInt();
        Map<TechNode<?>, Long> techNodeAccCWU = new O2OOpenCacheHashMap<>();
        for (int i = 0; i < techNodeCount; i++) {
            String nodeName = dataIOStream.readUTF();
            long accCWU = dataIOStream.readLong();
            TechNode<?> node = AnalyzeData.INSTANCE.getTechTree().getNode(nodeName);
            if (node != null) {
                techNodeAccCWU.put(node, accCWU);
            }
        }
        return techNodeAccCWU;
    }
}
