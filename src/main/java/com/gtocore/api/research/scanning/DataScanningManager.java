package com.gtocore.api.research.scanning;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchSavedDtat;

import com.gtolib.GTOCore;
import com.gtolib.utils.AEChemicalHelper;

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenCustomHashMap;

import java.util.Objects;
import java.util.UUID;

import static com.gregtechceu.gtceu.common.data.GTMaterials.NULL;

public class DataScanningManager {

    private static final Reference2ObjectMap<AEKey, ResearchPoints> dataScanningMap = new Reference2ObjectOpenCustomHashMap<>(new Hash.Strategy<>() {

        @Override
        public int hashCode(AEKey aeKey) {
            if (aeKey == null || aeKey.getPrimaryKey() == null) {
                return 0;
            }
            return aeKey.getPrimaryKey().hashCode();
        }

        @Override
        public boolean equals(AEKey aeKey, AEKey k1) {
            return aeKey == null ? k1 == null : (k1 != null &&
                    Objects.equals(aeKey.getPrimaryKey(), k1.getPrimaryKey()));
        }
    });

    public static void registerDataScanning(AEKey key, ResearchPoints points) {
        dataScanningMap.put(key, points);
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
                teamContext.getScannedMaterials().add(mat);
            }
            teamContext.getScannedItems().add(key);
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
}
