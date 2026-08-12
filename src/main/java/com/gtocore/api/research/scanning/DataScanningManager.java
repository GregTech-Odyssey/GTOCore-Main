package com.gtocore.api.research.scanning;

import com.gtocore.api.data.material.GTOMaterialFlags;
import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.client.Message;
import com.gtocore.common.data.GTORecipeTypes;

import com.gtolib.GTOCore;
import com.gtolib.api.machine.MultiblockDefinition;
import com.gtolib.api.recipe.RecipeType;
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
import appeng.hooks.IUnique;

import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.objects.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.gregtechceu.gtceu.common.data.GTMaterials.NULL;

public class DataScanningManager {

    private static final Reference2ObjectOpenCustomHashMap<AEKey, ResearchPoints> dataScanningMap = new Reference2ObjectOpenCustomHashMap<>(ResearchRequirements.AE_KEY_STRATEGY);
    private static final Reference2ReferenceMap<ResearchTag, Set<AEKey>> dataScanningSources = new Reference2ReferenceOpenHashMap<>();

    private static Reference2ObjectOpenCustomHashMap<AEKey, ResearchPoints> regMap = new Reference2ObjectOpenCustomHashMap<>(ResearchRequirements.AE_KEY_STRATEGY);

    private static final Reference2ObjectMap<UUID, Set<AEKey>> teamUnscannedItems = new Reference2ObjectOpenHashMap<>();
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

    public static List<DataScanningEntry> getDataScanningEntries() {
        List<DataScanningEntry> entries = new ArrayList<>(dataScanningMap.size());
        for (var it = dataScanningMap.reference2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            entries.add(new DataScanningEntry(entry.getKey(), entry.getValue()));
        }
        entries.sort(Comparator.comparing((DataScanningEntry entry) -> entry.key().getType().getId().toString())
                .thenComparing(entry -> entry.key().getId().toString()));
        return entries;
    }

    public static Set<AEKey> getDataScanningSources(ResearchTag tag) {
        var sources = dataScanningSources.get(tag);
        return sources == null ? Collections.emptySet() : sources;
    }

    public static boolean isUnscannable(AEKey key, UUID team) {
        var teamUnscanned = teamUnscannedItems.computeIfAbsent(team, ignored -> new ReferenceOpenHashSet<>());
        if (teamUnscanned.contains(key)) {
            return true;
        }
        long occupy = DataScanningManager.scanData(key, team, true).countBytes();
        if (occupy <= 0 && (ResearchRequirements.getEurekaRequirements(key).isEmpty() || hasScanned(key, team))) {
            teamUnscanned.add(key);
            return true;
        }
        return false;
    }

    public static ResearchPoints scanData(AEKey key, UUID team, boolean simulate) {
        var teamContext = TeamResearchSavedDtat.getOrCreateContext(team);
        var mat = AEChemicalHelper.getMaterial(key);
        boolean isMaterial = mat != NULL;
        boolean hasScanned = (isMaterial && teamContext.scannedMaterials().contains(mat)) || teamContext.scannedItems().contains(key);
        var penalty = hasScanned ? getRepeatedScanPenalty() : 1f;
        if (!simulate) {
            if (mat != NULL) {
                teamContext.addScannedMaterial(mat);
            }
            teamContext.addScannedItem(key);
            if (!hasScanned) {
                for (var node : ResearchRequirements.getEurekaRequirements(key)) {
                    Message.sendResearchToast(team, node, false);
                }
            }
        }
        return scanDataRaw(key, penalty);
    }

    public static ResearchPoints scanData(AEKey key, UUID team, long times, boolean simulate) {
        var teamContext = TeamResearchSavedDtat.getOrCreateContext(team);
        var mat = AEChemicalHelper.getMaterial(key);
        boolean isMaterial = mat != NULL;
        boolean hasScanned = (isMaterial && teamContext.scannedMaterials().contains(mat)) || teamContext.scannedItems().contains(key);
        var penalty = hasScanned ? getRepeatedScanPenalty() : 1f;
        var effectiveTimes = hasScanned ? times * penalty : 1f + (times - 1) * penalty;
        if (!simulate) {
            if (mat != NULL) {
                teamContext.addScannedMaterial(mat);
            }
            teamContext.addScannedItem(key);
            if (!hasScanned) {
                for (var node : ResearchRequirements.getEurekaRequirements(key)) {
                    TechTreeSavedData.unlock(team, node);
                    Message.sendResearchToast(team, node, false);
                }
            }
        }
        return scanDataRaw(key, effectiveTimes);
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

    public static boolean hasScanned(AEKey entry, UUID ownerId) {
        return TeamResearchSavedDtat.getOrCreateContext(TeamUtil.getTeamUUID(ownerId)).hasScanned(entry);
    }

    public record DataScanningEntry(AEKey key, ResearchPoints points) {}

    public static void freeze() {
        frozen = true;
        var prof = System.nanoTime();
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
        regMap = null;
        GTOCore.LOGGER.info("Data scanning freeze took {}ms", (System.nanoTime() - prof) / 1_000_000);
    }

    private static void putSearch(AEKey key, ResearchPoints points) {
        dataScanningMap.put(key, points);
        for (var it = points.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
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
            points.addTo(ResearchTag.MATERIAL, 32);
            if (mat.hasFlags(MaterialFlags.MAGICAL)) {
                points.addTo(ResearchTag.ALFHEIMY, 8);
            }
            if (mat.hasFlags(GTOMaterialFlags.GENERATE_CATALYST)) {
                points.addTo(ResearchTag.CATALYSIS, 3);
            }
        }
        if (key instanceof MetaMachineItem mmi && mmi.getDefinition() instanceof MultiblockDefinition d) {
            points.addTo(ResearchTag.MECHANICS, 1);
            if (d.canWorkInSpaceIndependently()) {
                points.addTo(ResearchTag.INTERSTELLAR_ENGINEERING, 3);
            }
        }
        for (var rt : new RecipeType[] { GTORecipeTypes.ASSEMBLER_RECIPES, GTORecipeTypes.ASSEMBLY_LINE_RECIPES, GTORecipeTypes.CIRCUIT_ASSEMBLER_RECIPES, GTORecipeTypes.ASSEMBLER_MODULE_RECIPES }) {
            if (rt.itemsCanBeProduced.contains(((IUnique) key).ae2$getUid())) {
                points.addTo(ResearchTag.ASSEMBLY, 4);
                break;
            }
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
