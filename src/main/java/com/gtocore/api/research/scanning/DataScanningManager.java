package com.gtocore.api.research.scanning;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.client.Message;

import com.gtolib.GTOCore;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.utils.AEChemicalHelper;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
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

    private static Reference2ObjectMap<AEKey, ResearchPoints> regMap = new Reference2ObjectOpenCustomHashMap<>(ResearchRequirements.AE_KEY_STRATEGY);

    private static boolean frozen = false;

    public static synchronized void registerDataScanning(AEKey key, ResearchPoints points) {
        if (frozen) {
            throw new IllegalStateException("Data scanning registration is frozen");
        }
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(points, "points");

        var previous = regMap.put(key, points);
        if (previous != null) {
            throw new IllegalStateException("Data scanning for key " + key + " is already registered with points: " + previous);
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
        var penalty = hasScanned ? getRepeatedScanPenalty() : 1f;
        if (!simulate) {
            if (mat != NULL) {
                teamContext.addScannedMaterial(mat);
            }
            teamContext.addScannedItem(key);
            for (var node : ResearchRequirements.getEurekaRequirements(key)) {
                Message.sendResearchToast(team, node, false);
            }
        }
        return scanDataRaw(key, penalty);
    }

    public static ResearchPoints scanDataRaw(AEKey key, float penalty) {
        var override = dataScanningMap.get(key);
        if (override != null) {
            return override.copyWithWeight(penalty);
        }
        return new ResearchPoints();
    }

    public static float getRepeatedScanPenalty() {
        return switch (GTOCore.difficulty) {
            case 1 -> 1 / 4f;
            case 2 -> 1 / 16f;
            default -> 1 / 64f;
        };
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

    public static void freeze() {
        frozen = true;
        dataScanningMap.putAll(regMap);
        BuiltInRegistries.ITEM.stream().forEach(key -> {
            var aeKey = AEItemKey.of(key);
            if (!dataScanningMap.containsKey(aeKey)) {
                var points = scanDataRaw(key);
                if (points.isEmpty()) {
                    return;
                }
                putSearch(aeKey, points);
            }
        });
        BuiltInRegistries.FLUID.stream().forEach(key -> {
            var aeKey = AEFluidKey.of(key);
            if (!dataScanningMap.containsKey(aeKey)) {
                var points = scanDataRaw(key);
                if (points.isEmpty()) {
                    return;
                }
                putSearch(aeKey, points);
            }
        });
    }

    private static void putSearch(AEKey key, ResearchPoints points) {
        dataScanningMap.put(key, points);
        for (var entry : points.reference2LongEntrySet()) {
            if (entry.getLongValue() <= 0L) {
                continue;
            }
            dataScanningSources.computeIfAbsent(entry.getKey(), ignored -> new ObjectOpenCustomHashSet<>(ResearchRequirements.AE_KEY_STRATEGY)).add(key);
        }
    }

    private static ResearchPoints scanDataRaw(Item key) {
        var mat = ChemicalHelper.getMaterialEntry(key).material();
        boolean isMaterial = mat != NULL;
        var points = new ResearchPoints();
        if (isMaterial) {
            points.addTo(ResearchTag.MATERIAL, (long) (32 * (float) 1.0));
            if (mat.hasFlags(MaterialFlags.MAGICAL)) {
                points.addTo(ResearchTag.ALFHEIMY, (long) (8 * (float) 1.0));
            }
        }
        if (key instanceof MetaMachineItem mmi && mmi.getDefinition() instanceof MultiblockDefinition) {
            points.addTo(ResearchTag.MECHANICS, (long) ((float) 1.0));
        }
        return points;
    }

    private static ResearchPoints scanDataRaw(Fluid key) {
        var mat = ChemicalHelper.getMaterial(key);
        boolean isMaterial = mat != NULL;
        var points = new ResearchPoints();
        if (isMaterial) {
            points.addTo(ResearchTag.MATERIAL, (long) (32 * (float) 1.0));
            if (mat.hasFlags(MaterialFlags.MAGICAL)) {
                points.addTo(ResearchTag.ALFHEIMY, (long) (8 * (float) 1.0));
            }
        }
        return points;
    }
}
