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
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:non_linear_optical_lens"), ResearchPoints.of(ResearchTag.OPTICS, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:laser_diode"), ResearchPoints.of(ResearchTag.OPTICS, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:high_energy_laser_emitter"), ResearchPoints.of(ResearchTag.OPTICS, 3L, ResearchTag.ASSEMBLY, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:optical_wafer"), ResearchPoints.of(ResearchTag.OPTICS, 2L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:optical_circuit_board"), ResearchPoints.of(ResearchTag.OPTICS, 1L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("minecraft:bedrock"), ResearchPoints.of(ResearchTag.MATERIAL, 60L));

        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:advanced_sps_crafting"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:advanced_mass_fabricator"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:magnetic_confinement_dimensionality_shock_device"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:chemical_complex"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:mega_wiremill"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:superconducting_magnetic_presser"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:heavy_rolling"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:phase_change_cube"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:particle_stream_matrix_filling_machine"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:element_copying"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:molten_core"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:component_assembly_line"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:advanced_integrated_ore_processor"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:integrated_assembler"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:desulfurizer"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:mega_brewer"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:microorganism_master"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:lightning_rod"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:magnetic_energy_reaction_furnace"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:high_energy_laser_lathe"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:neutronium_wire_cutting"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:nano_phagocytosis_plant"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:mega_bath_tank"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:mega_vacuum_drying_furnace"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:molecular_oscillation_dehydrator"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:extreme_compressor"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:extreme_electric_furnace"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:high_temperature_reaction_hub"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:gravity_bending_device"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:hand_of_arachne"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:cracker_hub"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:biochemical_extraction"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:nanites_integrated_processing_center"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:nanites_circuit_assembly_factory"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:precision_assembly_center"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:hyperdimensional_plasma_fusion_core"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:nyarlathoteps_tentacle"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:dimensional_focus_engraving_array"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:star_ultimate_material_forge_factory"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:super_blast_smelter"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:compound_extreme_cooling_unit"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:compound_distillation_fractionator"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:superconducting_electromagnetism"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:crystal_builder"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:holy_separator"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:field_extruder_factory"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:rarity_forge"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:integrated_vapor_deposition_system"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:giant_flotation_tank"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:entropy_flux_engine"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:transliminal_oasis"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:neutron_forging_anvil"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:dissolution_core"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:life_forge"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:gemini_containment_system"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:mantle_crusher"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:smart_siftering_hub"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtocore:giant_electrochemical_workstation"), ResearchPoints.of(ResearchTag.MECHANICS, 16L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_maceration_tower"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_chemical_bath"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_centrifuge"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_mixer"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_electrolyzer"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_electromagnet"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_packer"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_assembler"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_circuit_assembler"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_arc_smelter"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_engraving_laser"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_sifting_funnel"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_autoclave"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_material_press"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_brewer"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_cutter"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_distillery"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_extractor"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_extruder"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_solidifier"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:large_wiremill"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:mega_blast_furnace"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
        DataScanningManager.registerDataScanning(RegistriesUtils.getItem("gtceu:mega_vacuum_freezer"), ResearchPoints.of(ResearchTag.MECHANICS, 4L));
    }
}
