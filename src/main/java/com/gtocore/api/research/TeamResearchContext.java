package com.gtocore.api.research;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.data.techtree.BaseNodes;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.utils.AEChemicalHelper;
import com.gtolib.utils.iostream.DataIOStream;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;

import com.gto.datasynclib.datastream.data.Data;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.*;

import java.io.IOException;
import java.util.Set;

public record TeamResearchContext(ResearchPoints researchPoints, Set<AEKey> scannedItems,
                                  Set<Material> scannedMaterials, Reference2LongOpenHashMap<TechNode> techNodeAccCWU, IntOpenHashSet unlockedDimensions) {

    private static final int TECH_NODE_MANAGER_ID_FORMAT_MARKER = -1;

    public TeamResearchContext() {
        this(new ResearchPoints(), new ObjectOpenCustomHashSet<>(ResearchRequirements.AE_KEY_STRATEGY), new ReferenceOpenHashSet<>(), new Reference2LongOpenHashMap<>(), new IntOpenHashSet());
    }

    static void writeContext(DataIOStream dataIOStream, TeamResearchContext context) throws IOException {
        writeResearchPoints(dataIOStream, context.researchPoints());
        writeScannedItems(dataIOStream, context.scannedItems());
        writeScannedMaterials(dataIOStream, context.scannedMaterials());
        writeTechNodeAccCWU(dataIOStream, context.techNodeAccCWU());
        writeUnlockedDimensions(dataIOStream, context.unlockedDimensions());
    }

    @SuppressWarnings("unused")
    static TeamResearchContext readContext(DataIOStream dataIOStream, int dataVersion) {
        try {
            return new TeamResearchContext(
                    readResearchPoints(dataIOStream),
                    readScannedItems(dataIOStream),
                    readScannedMaterials(dataIOStream),
                    readTechNodeAccCWU(dataIOStream),
                    readUnlockedDimensions(dataIOStream));
        } catch (Exception e) {
            return new TeamResearchContext();
            // throw new IllegalStateException("Failed to read TeamResearchContext", e);
        }
    }

    static void writeResearchPoints(DataIOStream dataIOStream, Reference2LongOpenHashMap<ResearchTag> researchPoints) throws IOException {
        dataIOStream.writeInt(researchPoints.size());
        for (ObjectIterator<Reference2LongMap.Entry<ResearchTag>> it = researchPoints.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var researchEntry = it.next();
            dataIOStream.writeUTF(researchEntry.getKey().getName());
            dataIOStream.writeLong(researchEntry.getLongValue());
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
        Set<AEKey> scannedItems = new ObjectOpenCustomHashSet<>(ResearchRequirements.AE_KEY_STRATEGY);
        for (int i = 0; i < scannedItemCount; i++) {
            byte[] itemData = dataIOStream.readByteArray();
            AEKey item = GTOCodecs.AE_KEY_DATA_CODEC.decode(Data.readData(itemData));
            if (item != null) {
                scannedItems.add(item);
            }
        }
        return scannedItems;
    }

    static void writeTechNodeAccCWU(DataIOStream dataIOStream, Reference2LongOpenHashMap<TechNode> techNodeAccCWU) throws IOException {
        dataIOStream.writeInt(TECH_NODE_MANAGER_ID_FORMAT_MARKER);
        dataIOStream.writeInt(techNodeAccCWU.size());
        for (ObjectIterator<Reference2LongMap.Entry<TechNode>> it = techNodeAccCWU.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var techNodeEntry = it.next();
            dataIOStream.writeUTF(techNodeEntry.getKey().getManager().getId());
            dataIOStream.writeUTF(techNodeEntry.getKey().name);
            dataIOStream.writeLong(techNodeEntry.getLongValue());
        }
    }

    static Reference2LongOpenHashMap<TechNode> readTechNodeAccCWU(DataIOStream dataIOStream) throws IOException {
        int techNodeCount = dataIOStream.readInt();
        boolean hasManagerIds = techNodeCount == TECH_NODE_MANAGER_ID_FORMAT_MARKER;
        if (hasManagerIds) {
            techNodeCount = dataIOStream.readInt();
        }
        Reference2LongOpenHashMap<TechNode> techNodeAccCWU = new Reference2LongOpenHashMap<>();
        for (int i = 0; i < techNodeCount; i++) {
            TechTreeManager manager = hasManagerIds ? TechTreeManager.getManager(dataIOStream.readUTF()) : BaseNodes.MainTree;
            // todo remove datafix in future
            String nodeName = dataIOStream.readUTF();
            long accCWU = dataIOStream.readLong();
            TechNode node = manager == null ? null : manager.getNode(nodeName);
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

    static void writeUnlockedDimensions(DataIOStream dataIOStream, IntOpenHashSet unlockedDimensions) throws IOException {
        dataIOStream.writeInt(unlockedDimensions.size());
        for (int dimensionId : unlockedDimensions) {
            dataIOStream.writeInt(dimensionId);
        }
    }

    static IntOpenHashSet readUnlockedDimensions(DataIOStream dataIOStream) throws IOException {
        int unlockedDimensionCount = dataIOStream.readInt();
        IntOpenHashSet unlockedDimensions = new IntOpenHashSet();
        for (int i = 0; i < unlockedDimensionCount; i++) {
            int dimensionId = dataIOStream.readInt();
            unlockedDimensions.add(dimensionId);
        }
        return unlockedDimensions;
    }

    public boolean isEmpty() {
        return researchPoints.isEmpty() && scannedItems.isEmpty() && techNodeAccCWU.isEmpty();
    }

    public void addTechNodeAccCWU(TechNode selectedNode, long cwuBuffer) {
        techNodeAccCWU.merge(selectedNode, cwuBuffer, Long::sum);
        TeamResearchSavedData.INSTANCE.setDirty(true);
    }

    public void addResearchPoints(ResearchPoints researchPoints) {
        for (var it = researchPoints.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            this.researchPoints.addTo(entry.getKey(), entry.getLongValue());
        }
        TeamResearchSavedData.INSTANCE.setDirty(true);
    }

    public void addResearchPoints(ResearchTag tag, long points) {
        this.researchPoints.addTo(tag, points);
        TeamResearchSavedData.INSTANCE.setDirty(true);
    }

    public void addScannedItem(AEKey item) {
        scannedItems.add(item);
        TeamResearchSavedData.INSTANCE.setDirty(true);
    }

    public void addScannedMaterial(Material material) {
        scannedMaterials.add(material);
        TeamResearchSavedData.INSTANCE.setDirty(true);
    }

    public boolean hasScanned(AEKey key) {
        return scannedItems.contains(key) || scannedMaterials.contains(AEChemicalHelper.getMaterial(key));
    }

    public boolean addUnlockedDimension(ResourceKey<Level> dimensionId) {
        var r = unlockedDimensions.add(GTODimensions.getDimensionIncludingOrbits(dimensionId).ordinal());
        if (r) TeamResearchSavedData.INSTANCE.setDirty(true);
        return r;
    }

    public boolean hasUnlockedDimension(ResourceKey<Level> dimensionId) {
        return unlockedDimensions.contains(GTODimensions.getDimensionIncludingOrbits(dimensionId).ordinal());
    }
}
