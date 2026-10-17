package com.gtocore.api.electrochemistry;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

/** Registry aliases retain membranes and both old electrolyte variants in existing saves. */
public final class ElectrochemistryMigration {

    private ElectrochemistryMigration() {}

    public static void remap(MissingMappingsEvent event) {
        for (var namespace : new String[] { GTOCore.MOD_ID, GTCEu.MOD_ID }) {
            for (var mapping : event.getMappings(Registries.ITEM, namespace)) {
                var original = mapping.getKey();
                var replacement = original.withPath(rename(original.getPath()));
                if (original.equals(replacement)) continue;
                var item = ForgeRegistries.ITEMS.getValue(replacement);
                if (item != null) mapping.remap(item);
            }
            for (var mapping : event.getMappings(Registries.FLUID, namespace)) {
                var original = mapping.getKey();
                var replacement = original.withPath(rename(original.getPath()));
                if (original.equals(replacement)) continue;
                var fluid = ForgeRegistries.FLUIDS.getValue(replacement);
                if (fluid != null) mapping.remap(fluid);
            }
            for (var mapping : event.getMappings(Registries.BLOCK, namespace)) {
                var original = mapping.getKey();
                var replacement = original.withPath(rename(original.getPath()));
                if (original.equals(replacement)) continue;
                var block = ForgeRegistries.BLOCKS.getValue(replacement);
                if (block != null) mapping.remap(block);
            }
        }
    }

    private static String rename(String path) {
        return path.replace("_membrane_electrode", "_ion_membrane")
                .replace("_energy_storage_anode", "_energy_storage")
                .replace("_energy_storage_cathode", "_energy_storage")
                .replace("_energy_release_anode", "_energy_release")
                .replace("_energy_release_cathode", "_energy_release");
    }
}
