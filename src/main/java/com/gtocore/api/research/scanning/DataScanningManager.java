package com.gtocore.api.research.scanning;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.client.Message;

import com.gtolib.GTOCore;
import com.gtolib.utils.AEChemicalHelper;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.gregtechceu.gtceu.common.data.GTMaterials.NULL;

public class DataScanningManager {

    private static final Reference2ObjectMap<AEKey, ResearchPoints> dataScanningMap = new Reference2ObjectOpenCustomHashMap<>(ResearchRequirements.AE_KEY_STRATEGY);
    private static final Reference2ReferenceMap<ResearchTag, Set<AEKey>> dataScanningSources = new Reference2ReferenceOpenHashMap<>();

    public static synchronized void registerDataScanning(AEKey key, ResearchPoints points) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(points, "points");

        var storedPoints = points.copy();
        var previous = dataScanningMap.put(key, storedPoints);
        if (previous != null) {
            for (var entry : previous.reference2LongEntrySet()) {
                var sources = dataScanningSources.get(entry.getKey());
                if (sources != null) {
                    sources.remove(key);
                    if (sources.isEmpty()) {
                        dataScanningSources.remove(entry.getKey());
                    }
                }
            }
        }
        for (var entry : storedPoints.reference2LongEntrySet()) {
            if (entry.getLongValue() <= 0L) {
                continue;
            }
            dataScanningSources.computeIfAbsent(entry.getKey(), ignored -> new ObjectOpenCustomHashSet<>(ResearchRequirements.AE_KEY_STRATEGY)).add(key);
        }
    }

    public static synchronized List<DataScanningEntry> getDataScanningEntries() {
        List<DataScanningEntry> entries = new ArrayList<>(dataScanningMap.size());
        for (var entry : dataScanningMap.reference2ObjectEntrySet()) {
            entries.add(new DataScanningEntry(entry.getKey(), entry.getValue().copy()));
        }
        entries.sort(Comparator.comparing((DataScanningEntry entry) -> entry.key().getType().getId().toString())
                .thenComparing(entry -> entry.key().getId().toString()));
        return List.copyOf(entries);
    }

    public static synchronized Set<AEKey> getDataScanningSources(ResearchTag tag) {
        var sources = dataScanningSources.get(tag);
        return sources == null ? Set.of() : Set.copyOf(sources);
    }

    private static ResearchPoints scanData(AEKey key, UUID team, boolean simulate) {
        var teamContext = TeamResearchSavedDtat.getOrCreateContext(team);
        var mat = AEChemicalHelper.getMaterial(key);
        boolean isMaterial = mat != NULL;
        boolean hasScanned = (isMaterial && teamContext.getScannedMaterials().contains(mat)) || teamContext.getScannedItems().contains(key);
        var penalty = hasScanned ? switch (GTOCore.difficulty) {
            case 1 -> 1 / 4f;
            case 2 -> 1 / 16f;
            default -> 1 / 64f;
        } : 1f;
        if (!simulate) {
            if (mat != NULL) {
                teamContext.addScannedMaterial(mat);
            }
            teamContext.addScannedItem(key);
            for (var node : ResearchRequirements.getEurekaRequirements(key)) {
                Message.sendResearchToast(team, node, false);
            }
        }
        var override = dataScanningMap.get(key);
        if (override != null) {
            return override.copyWithWeight(penalty);
        }
        var points = new ResearchPoints();
        if (isMaterial) {
            points.addTo(ResearchTag.MATERIAL, (long) (32 * penalty));
            if (mat.hasFlags(MaterialFlags.MAGICAL)) {
                points.addTo(ResearchTag.ALFHEIMY, (long) (8 * penalty));
            }
        }
        return points;
    }

    public static ResearchPoints scanData(ItemLike item, UUID team, boolean simulate) {
        return scanData(AEItemKey.of(item), team, simulate);
    }

    public static ResearchPoints scanData(Fluid fluid, UUID team, boolean simulate) {
        return scanData(AEFluidKey.of(fluid), team, simulate);
    }

    public static synchronized void registerDataScanning(ItemLike item, ResearchPoints points) {
        registerDataScanning(AEItemKey.of(item), points);
    }

    public static synchronized void registerDataScanning(Fluid fluid, ResearchPoints points) {
        registerDataScanning(AEFluidKey.of(fluid), points);
    }

    public record DataScanningEntry(AEKey key, ResearchPoints points) {}
}
