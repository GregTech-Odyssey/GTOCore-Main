package com.gtocore.data.recipe.research;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.scanning.DataScanningManager;

import com.gtolib.utils.RegistriesUtils;

import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.GTValues.*;

public final class ScanningRecipes {

    public static void init() {
        /// 基元扫描
        DataScanningManager.registerDataScanning(Items.COMPASS, ResearchPoints.of(ResearchTag.MECHANICS, 1));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:catalyst_base"), ResearchPoints.of(ResearchTag.CATALYSIS, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:stem_cells"), ResearchPoints.of(ResearchTag.BIOLOGY, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:biological_cells"), ResearchPoints.of(ResearchTag.BIOLOGY, 6L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_1m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 10L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_4m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 15L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_16m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 30L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_64m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 40L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cell_component_256m"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 50L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:infinite_cell_component"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 120L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:bifidobacterium_breve_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 7L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cupriavidus_necator_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 7L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:hyperthermophilic_archaeon_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 7L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:eschericia_coli_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 7L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:clostridium_pasteurianum_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 7L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:shewanella_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 7L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:streptococcus_pyogenes_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 7L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:brevibacterium_flavium_dust"), ResearchPoints.of(ResearchTag.BIOLOGY, 7L, ResearchTag.MATERIAL, 32L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:bio_cardiomyocyte_cluster"), ResearchPoints.of(ResearchTag.BIOLOGY, 20L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:mutant_cardiomyocyte_cluster"), ResearchPoints.of(ResearchTag.BIOLOGY, 30L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:dragon_cardiomyocyte_cluster"), ResearchPoints.of(ResearchTag.BIOLOGY, 48L, ResearchTag.EXOTIC, 3L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:modified_dragon_heart"), ResearchPoints.of(ResearchTag.BIOLOGY, 60L, ResearchTag.EXOTIC, 3L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:awakened_dragon_heart"), ResearchPoints.of(ResearchTag.BIOLOGY, 60L, ResearchTag.EXOTIC, 3L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:blaze_casing"), ResearchPoints.of(ResearchTag.THERMODYNAMICS, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:rydberg_spinorial_assembly"), ResearchPoints.of(ResearchTag.EXOTIC, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:normal_heat_pipe"), ResearchPoints.of(ResearchTag.THERMODYNAMICS, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:electric_blast_furnace"), ResearchPoints.of(ResearchTag.THERMODYNAMICS, 1L, ResearchTag.MECHANICS, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:atomizing_condenser"), ResearchPoints.of(ResearchTag.THERMODYNAMICS, 1L, ResearchTag.MECHANICS, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:vacuum_freezer"), ResearchPoints.of(ResearchTag.THERMODYNAMICS, 1L, ResearchTag.MECHANICS, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cold_ice_casing"), ResearchPoints.of(ResearchTag.THERMODYNAMICS, 1L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:lapotronic_energy_orb"), ResearchPoints.of(ResearchTag.ENERGY, 10L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:lapotronic_energy_orb_cluster"), ResearchPoints.of(ResearchTag.ENERGY, 15L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:energy_module"), ResearchPoints.of(ResearchTag.ENERGY, 20L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:energy_cluster"), ResearchPoints.of(ResearchTag.ENERGY, 30L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:max_battery"), ResearchPoints.of(ResearchTag.ENERGY, 50L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:really_max_battery"), ResearchPoints.of(ResearchTag.ENERGY, 60L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:transcendent_max_battery"), ResearchPoints.of(ResearchTag.ENERGY, 80L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:extremely_max_battery"), ResearchPoints.of(ResearchTag.ENERGY, 120L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:insanely_max_battery"), ResearchPoints.of(ResearchTag.ENERGY, 200L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:mega_max_battery"), ResearchPoints.of(ResearchTag.ENERGY, 300L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:engraved_lapotron_crystal_chip"), ResearchPoints.of(ResearchTag.ENERGY, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:energy_crystal"), ResearchPoints.of(ResearchTag.ENERGY, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:lapotron_crystal"), ResearchPoints.of(ResearchTag.ENERGY, 2L, ResearchTag.ASSEMBLY, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:ev_lapotronic_battery"), ResearchPoints.of(ResearchTag.ENERGY, 5L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:iv_lapotronic_battery"), ResearchPoints.of(ResearchTag.ENERGY, 7L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:luv_lapotronic_battery"), ResearchPoints.of(ResearchTag.ENERGY, 9L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:zpm_lapotronic_battery"), ResearchPoints.of(ResearchTag.ENERGY, 15L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:uv_lapotronic_battery"), ResearchPoints.of(ResearchTag.ENERGY, 20L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:uhv_ultimate_battery"), ResearchPoints.of(ResearchTag.ENERGY, 40L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("ad_astra:tier_1_rocket"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("ad_astra:tier_2_rocket"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 6L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("ad_astra:tier_3_rocket"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 12L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("ad_astra:tier_4_rocket"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 20L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("ad_astra_rocketed:tier_5_rocket"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 30L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("ad_astra_rocketed:tier_6_rocket"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 42L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("ad_astra_rocketed:tier_7_rocket"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 56L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:space_elevator"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 60L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:space_elevator_power_module_1"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 50L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:space_elevator_power_module_2"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 100L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:space_elevator_power_module_3"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 150L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:space_elevator_power_module_4"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 200L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:space_elevator_power_module_5"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 250L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:data_stick"), ResearchPoints.of(ResearchTag.DATA_STORAGE, 4L, ResearchTag.ASSEMBLY, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:dimension_data"), ResearchPoints.of(ResearchTag.INTERSTELLAR_ENGINEERING, 4L, ResearchTag.DATA_STORAGE, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:data_orb"), ResearchPoints.of(ResearchTag.ASSEMBLY, 4L, ResearchTag.DATA_STORAGE, 8L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:data_module"), ResearchPoints.of(ResearchTag.ASSEMBLY, 4L, ResearchTag.DATA_STORAGE, 8L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:neural_matrix"), ResearchPoints.of(ResearchTag.ASSEMBLY, 4L, ResearchTag.DATA_STORAGE, 8L, ResearchTag.BIOLOGY, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:atomic_archives"), ResearchPoints.of(ResearchTag.ASSEMBLY, 4L, ResearchTag.DATA_STORAGE, 8L, ResearchTag.OPTICS, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:obsidian_matrix"), ResearchPoints.of(ResearchTag.ASSEMBLY, 4L, ResearchTag.DATA_STORAGE, 8L, ResearchTag.EXOTIC, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:closed_timelike_curve_guidance_unit"), ResearchPoints.of(ResearchTag.ASSEMBLY, 4L, ResearchTag.DATA_STORAGE, 8L, ResearchTag.SUPRACAUSAL, 1L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:microcosm"), ResearchPoints.of(ResearchTag.ASSEMBLY, 30L, ResearchTag.DATA_STORAGE, 60L, ResearchTag.SUPRACAUSAL, 1L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:highly_concurrent_intensive_optical_computing_channel"), ResearchPoints.of(ResearchTag.ASSEMBLY, 4L, ResearchTag.OPTICS, 6L));
    }
}
