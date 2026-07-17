package com.gtocore.api.research;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.data.recipe.research.AnalyzeData;

import com.gtolib.utils.iostream.DataIOStream;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import appeng.api.stacks.AEKey;

import com.gto.datasynclib.datastream.data.Data;
import com.gto.fastcollection.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Getter
public class TeamResearchContext {

    private final ResearchPoints researchPoints;
    private final Set<AEKey> scannedItems;
    private final Set<Material> scannedMaterials;
    private final Map<TechNode, Long> techNodeAccCWU;

    public TeamResearchContext() {
        this(new ResearchPoints(), new ReferenceOpenHashSet<>(), new ReferenceOpenHashSet<>(), new O2OOpenCacheHashMap<>());
    }

    public TeamResearchContext(
                               ResearchPoints researchPoints,
                               Set<AEKey> scannedItems,
                               Set<Material> scannedMaterials,
                               Map<TechNode, Long> techNodeAccCWU) {
        this.researchPoints = researchPoints;
        this.scannedItems = scannedItems;
        this.scannedMaterials = scannedMaterials;
        this.techNodeAccCWU = techNodeAccCWU;
    }

    static void writeContext(DataIOStream dataIOStream, TeamResearchContext context) throws IOException {
        writeResearchPoints(dataIOStream, context.getResearchPoints());
        writeScannedItems(dataIOStream, context.getScannedItems());
        writeScannedMaterials(dataIOStream, context.getScannedMaterials());
        writeTechNodeAccCWU(dataIOStream, context.getTechNodeAccCWU());
    }

    static TeamResearchContext readContext(DataIOStream dataIOStream, int dataVersion) throws IOException {
        try {
            if (dataVersion == 2) return new TeamResearchContext(
                    readResearchPoints(dataIOStream),
                    readScannedItems(dataIOStream),
                    new ReferenceOpenHashSet<>(),
                    readTechNodeAccCWU(dataIOStream));
            return new TeamResearchContext(
                    readResearchPoints(dataIOStream),
                    readScannedItems(dataIOStream),
                    readScannedMaterials(dataIOStream),
                    readTechNodeAccCWU(dataIOStream));
        } catch (Exception e) {
            // return new TeamResearchContext();
            throw new IllegalStateException("Failed to read TeamResearchContext", e);
        }
    }

    static void writeResearchPoints(DataIOStream dataIOStream, Map<ResearchTag, Long> researchPoints) throws IOException {
        dataIOStream.writeInt(researchPoints.size());
        for (Map.Entry<ResearchTag, Long> researchEntry : researchPoints.entrySet()) {
            dataIOStream.writeUTF(researchEntry.getKey().getName());
            dataIOStream.writeLong(researchEntry.getValue());
        }
    }

    static ResearchPoints readResearchPoints(DataIOStream dataIOStream) throws IOException {
        int researchCount = dataIOStream.readInt();
        ResearchPoints researchPoints = new ResearchPoints();
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

    static void writeScannedItems(DataIOStream dataIOStream, Set<AEKey> scannedItems) throws IOException {
        dataIOStream.writeInt(scannedItems.size());
        for (AEKey item : scannedItems) {
            dataIOStream.writeByteArray(GTOCodecs.AE_KEY_DATA_CODEC.encode(item).writeToBytes());
        }
    }

    static Set<AEKey> readScannedItems(DataIOStream dataIOStream) throws IOException {
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

    static void writeTechNodeAccCWU(DataIOStream dataIOStream, Map<TechNode, Long> techNodeAccCWU) throws IOException {
        dataIOStream.writeInt(techNodeAccCWU.size());
        for (Map.Entry<TechNode, Long> techNodeEntry : techNodeAccCWU.entrySet()) {
            dataIOStream.writeUTF(techNodeEntry.getKey().name);
            dataIOStream.writeLong(techNodeEntry.getValue());
        }
    }

    static Map<TechNode, Long> readTechNodeAccCWU(DataIOStream dataIOStream) throws IOException {
        int techNodeCount = dataIOStream.readInt();
        Map<TechNode, Long> techNodeAccCWU = new Reference2LongOpenHashMap<>();
        for (int i = 0; i < techNodeCount; i++) {
            String nodeName = dataIOStream.readUTF();
            long accCWU = dataIOStream.readLong();
            TechNode node = AnalyzeData.TechTree.getNode(nodeName);
            if (node != null) {
                techNodeAccCWU.put(node, accCWU);
            }
        }
        return techNodeAccCWU;
    }

    static void writeScannedMaterials(DataIOStream dataIOStream, Set<Material> scannedMaterials) throws IOException {
        dataIOStream.writeInt(scannedMaterials.size());
        for (Material material : scannedMaterials) {
            dataIOStream.writeUTF(material.getResourceLocation().toString());
        }
    }

    static Set<Material> readScannedMaterials(DataIOStream dataIOStream) throws IOException {
        int scannedMaterialCount = dataIOStream.readInt();
        Set<Material> scannedMaterials = new ReferenceOpenHashSet<>();
        for (int i = 0; i < scannedMaterialCount; i++) {
            String materialName = dataIOStream.readUTF();
            Material material = GTCEuAPI.materialManager.getMaterial(materialName);
            if (material != null) {
                scannedMaterials.add(material);
            }
        }
        return scannedMaterials;
    }

    public boolean isEmpty() {
        return researchPoints.isEmpty() && scannedItems.isEmpty() && techNodeAccCWU.isEmpty();
    }

    public void addTechNodeAccCWU(TechNode selectedNode, long cwuBuffer) {
        techNodeAccCWU.merge(selectedNode, cwuBuffer, Long::sum);
        TeamResearchSavedDtat.INSTANCE.setDirty(true);
    }

    public void addResearchPoints(ResearchPoints researchPoints) {
        for (var entry : researchPoints.reference2LongEntrySet()) {
            this.researchPoints.addTo(entry.getKey(), entry.getLongValue());
        }
        TeamResearchSavedDtat.INSTANCE.setDirty(true);
    }

    public void addScannedItem(AEKey item) {
        scannedItems.add(item);
        TeamResearchSavedDtat.INSTANCE.setDirty(true);
    }

    public void addScannedMaterial(Material material) {
        scannedMaterials.add(material);
        TeamResearchSavedDtat.INSTANCE.setDirty(true);
    }
}
