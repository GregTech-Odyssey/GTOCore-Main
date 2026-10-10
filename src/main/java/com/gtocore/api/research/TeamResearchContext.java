package com.gtocore.api.research;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.data.techtree.BaseNodes;

import com.gtolib.api.data.GTODimensions;
import com.gtolib.utils.AEChemicalHelper;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.transfer.key.KeyCodecs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import appeng.api.stacks.AEKey;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.*;

import java.util.Set;

public record TeamResearchContext(ResearchPoints researchPoints, Set<AEKey> scannedItems,
                                  Set<Material> scannedMaterials, Reference2LongOpenHashMap<TechNode> techNodeAccCWU, IntOpenHashSet unlockedDimensions) {

    private static final int TECH_NODE_MANAGER_ID_FORMAT_MARKER = -1;

    /**
     * An {@link AEKey} as the self-describing tag it writes — the disk half of the codec
     * {@code KeyCodecs} registers for {@link AEKey}, taken from the single registry instead of being
     * rebuilt here (it used to be a {@code Data} encoded to a {@code byte[]}, which copied the payload
     * twice). A registry-keyed codec would be the alternative, but AE2's own tag carries the key type
     * and the item/fluid key, and it is what {@code AEKey.fromTagGeneric} reads back.
     */
    private static final StreamCodec<FriendlyByteBuf, AEKey> AE_KEY_WRITE_CODEC = ByteBufCodecs.fromValueCodec(0, KeyCodecs.AE_KEY_DATA_CODEC).cast();

    public TeamResearchContext() {
        this(new ResearchPoints(), new ObjectOpenCustomHashSet<>(ResearchRequirements.AE_KEY_STRATEGY), new ReferenceOpenHashSet<>(), new Reference2LongOpenHashMap<>(), new IntOpenHashSet());
    }

    static void writeContext(FriendlyByteBuf buf, TeamResearchContext context) {
        writeResearchPoints(buf, context.researchPoints());
        writeScannedItems(buf, context.scannedItems());
        writeScannedMaterials(buf, context.scannedMaterials());
        writeTechNodeAccCWU(buf, context.techNodeAccCWU());
        writeUnlockedDimensions(buf, context.unlockedDimensions());
    }

    @SuppressWarnings("unused")
    static TeamResearchContext readContext(FriendlyByteBuf buf) {
        try {
            return new TeamResearchContext(
                    readResearchPoints(buf),
                    readScannedItems(buf),
                    readScannedMaterials(buf),
                    readTechNodeAccCWU(buf),
                    readUnlockedDimensions(buf));
        } catch (Exception e) {
            return new TeamResearchContext();
            // throw new IllegalStateException("Failed to read TeamResearchContext", e);
        }
    }

    static void writeResearchPoints(FriendlyByteBuf buf, Reference2LongOpenHashMap<ResearchTag> researchPoints) {
        buf.writeInt(researchPoints.size());
        for (ObjectIterator<Reference2LongMap.Entry<ResearchTag>> it = researchPoints.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var researchEntry = it.next();
            buf.writeUtf(researchEntry.getKey().getName());
            buf.writeLong(researchEntry.getLongValue());
        }
    }

    static ResearchPoints readResearchPoints(FriendlyByteBuf buf) {
        int researchCount = buf.readInt();
        ResearchPoints researchPoints = new ResearchPoints();
        for (int i = 0; i < researchCount; i++) {
            String tagName = buf.readUtf();
            long points = buf.readLong();
            ResearchTag tag = ResearchTag.TAGS.get(tagName);
            if (tag != null) {
                researchPoints.put(tag, points);
            }
        }
        return researchPoints;
    }

    static void writeScannedItems(FriendlyByteBuf buf, Set<AEKey> scannedItems) {
        buf.writeInt(scannedItems.size());
        for (AEKey item : scannedItems) {
            AE_KEY_WRITE_CODEC.encode(buf, item);
        }
    }

    static Set<AEKey> readScannedItems(FriendlyByteBuf buf) {
        int scannedItemCount = buf.readInt();
        Set<AEKey> scannedItems = new ObjectOpenCustomHashSet<>(ResearchRequirements.AE_KEY_STRATEGY);
        for (int i = 0; i < scannedItemCount; i++) {
            AEKey item = AE_KEY_WRITE_CODEC.decode(buf);
            if (item != null) {
                scannedItems.add(item);
            }
        }
        return scannedItems;
    }

    static void writeTechNodeAccCWU(FriendlyByteBuf buf, Reference2LongOpenHashMap<TechNode> techNodeAccCWU) {
        buf.writeInt(TECH_NODE_MANAGER_ID_FORMAT_MARKER);
        buf.writeInt(techNodeAccCWU.size());
        for (ObjectIterator<Reference2LongMap.Entry<TechNode>> it = techNodeAccCWU.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var techNodeEntry = it.next();
            buf.writeUtf(techNodeEntry.getKey().getManager().getId());
            buf.writeUtf(techNodeEntry.getKey().name);
            buf.writeLong(techNodeEntry.getLongValue());
        }
    }

    static Reference2LongOpenHashMap<TechNode> readTechNodeAccCWU(FriendlyByteBuf buf) {
        int techNodeCount = buf.readInt();
        boolean hasManagerIds = techNodeCount == TECH_NODE_MANAGER_ID_FORMAT_MARKER;
        if (hasManagerIds) {
            techNodeCount = buf.readInt();
        }
        Reference2LongOpenHashMap<TechNode> techNodeAccCWU = new Reference2LongOpenHashMap<>();
        for (int i = 0; i < techNodeCount; i++) {
            TechTreeManager manager = hasManagerIds ? TechTreeManager.getManager(buf.readUtf()) : BaseNodes.MainTree;
            // todo remove datafix in future
            String nodeName = buf.readUtf();
            long accCWU = buf.readLong();
            TechNode node = manager == null ? null : manager.getNode(nodeName);
            if (node != null) {
                techNodeAccCWU.put(node, accCWU);
            }
        }
        return techNodeAccCWU;
    }

    static void writeScannedMaterials(FriendlyByteBuf buf, Set<Material> scannedMaterials) {
        buf.writeInt(scannedMaterials.size());
        for (Material material : scannedMaterials) {
            buf.writeUtf(material.getResourceLocation().toString());
        }
    }

    static Set<Material> readScannedMaterials(FriendlyByteBuf buf) {
        int scannedMaterialCount = buf.readInt();
        Set<Material> scannedMaterials = new ReferenceOpenHashSet<>();
        for (int i = 0; i < scannedMaterialCount; i++) {
            String materialName = buf.readUtf();
            Material material = GTCEuAPI.materialManager.getMaterial(materialName);
            if (material != null) {
                scannedMaterials.add(material);
            }
        }
        return scannedMaterials;
    }

    static void writeUnlockedDimensions(FriendlyByteBuf buf, IntOpenHashSet unlockedDimensions) {
        ByteBufCodecs.INT_SET.encode(buf, unlockedDimensions);
    }

    static IntOpenHashSet readUnlockedDimensions(FriendlyByteBuf buf) {
        return new IntOpenHashSet(ByteBufCodecs.INT_SET.decode(buf));
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
        var dim = GTODimensions.getDimensionIncludingOrbits(dimensionId);
        if (dim == null) return false;
        var r = unlockedDimensions.add(dim.getId());
        if (r) TeamResearchSavedData.INSTANCE.setDirty(true);
        return r;
    }

    public boolean hasUnlockedDimension(ResourceKey<Level> dimensionId) {
        var dim = GTODimensions.getDimensionIncludingOrbits(dimensionId);
        if (dim == null) return false;
        return unlockedDimensions.contains(dim.getId());
    }
}
