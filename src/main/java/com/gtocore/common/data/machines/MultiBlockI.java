package com.gtocore.common.data.machines;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.client.renderer.machine.MultiFluidRenderer;
import com.gtocore.client.renderer.machine.SolarStormAggregationReactorRenderer;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOFluids;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.translation.GTOMachineStories;
import com.gtocore.common.data.translation.GTOMachineTooltipsA;
import com.gtocore.common.machine.multiblock.electric.LargeAlgaeFarm;
import com.gtocore.common.machine.multiblock.electric.PigmentMixer;
import com.gtocore.common.machine.multiblock.electric.SolarStormAggregationReactor;
import com.gtocore.common.machine.multiblock.electric.VirtualCoinMiner;

import com.gtolib.GTOCore;
import com.gtolib.utils.MultiBlockFileReader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.level.material.Fluids;

import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gtocore.api.pattern.GTOPredicates.glass;
import static com.gtocore.api.pattern.GTOPredicates.recordPosition;
import static com.gtocore.utils.register.MachineRegisterUtils.multiblock;

public class MultiBlockI {

    public static void init() {}

    // 颜料混合机
    public static final MultiblockMachineDefinition PIGMENT_MIXER = multiblock("pigment_mixer", "染料混合机", PigmentMixer::new)
            .nonYAxisRotation()
            .parallelizableTooltips()
            .perfectOCTooltips()
            .tooltips(GTOMachineStories.PigmentMixerTooltips)
            .recipeTypes(GTORecipeTypes.PIGMENT_MIXING_RECIPES)
            .recipeModifier(RecipeModifier.PERFECT_OVERCLOCKING)
            .block(GTOBlocks.PRESSURE_RESISTANT_HOUSING_MECHANICAL_BLOCK)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("pigment_mixer").build())
                    .symbols(Symbols.create()
                            .where('A', blocks(GTBlocks.CASING_PTFE_INERT.get()))
                            .where('B', blocks(GTOBlocks.STABLE_BASE_CASING.get()))
                            .where('C', blocks(GTOBlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING.get()))
                            .where('D', blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                            .where('E', blocks(GCYMBlocks.CASING_CORROSION_PROOF.get()))
                            .where('F', blocks(GTOBlocks.PRESSURE_RESISTANT_HOUSING_MECHANICAL_BLOCK.get()))
                            .where('G', blocks(GTOBlocks.BOROSILICATE_GLASS.get()))
                            .where('H', blocks(GTOBlocks.COMPRESSOR_CONTROLLER_CASING.get()))
                            .where('I', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .where('J', blocks(GTOBlocks.STAINLESS_STEEL_CORROSION_RESISTANT_CASING.get()))
                            .where('K', blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                            .where('L', GTOPredicates.frame(GTOMaterials.StructuralSteel45))
                            .wherePart('M', blocks(GTOBlocks.PRESSURE_RESISTANT_HOUSING_MECHANICAL_BLOCK.get())
                                    .or(autoAbilities(definition.getRecipeTypes()))
                                    .or(autoAbilities(true, false, true))
                                    .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2)))
                            .where('N', blocks(GTOBlocks.IRIDIUM_CASING.get()))
                            .where('O', blocks(GTOBlocks.HASTELLOY_N_75_CASING.get()))
                            .where('P', blocks(GTOBlocks.HASTELLOY_N_75_GEARBOX.get()))
                            .where('Q', blocks(GTOBlocks.NAQUADAH_REINFORCED_PLANT_CASING.get()))
                            .where('R', controller(definition))
                            .where('c', recordPosition(GTOPredicates.DataKeys.CYAN, air()))
                            .where('m', recordPosition(GTOPredicates.DataKeys.MAGENTA, air()))
                            .where('y', recordPosition(GTOPredicates.DataKeys.YELLOW, air()))
                            .where('k', recordPosition(GTOPredicates.DataKeys.BLACK, air()))
                            .where('w', recordPosition(GTOPredicates.DataKeys.WHITE, air())))
                    .build())
            .renderer(MultiFluidRenderer.create(GTOCore.id("block/casings/pressure_resistant_housing_mechanical_block"),
                    GTCEu.id("block/multiblock/gcym/large_chemical_bath")))
            .hasTESR(true)
            .register();

    // 大型藻类养殖中心
    public static final MultiblockMachineDefinition LARGE_ALGAE_FARM = multiblock("large_algae_farm", "大型藻类养殖中心", LargeAlgaeFarm::new)
            .nonYAxisRotation()
            .recipeTypes(GTORecipeTypes.DUMMY_RECIPES)
            .recipeModifier(RecipeModifier.PERFECT_OVERCLOCKING)
            .tooltipsSupplier(GTOMachineStories.LargeAlgaeFarmTooltips)
            .tooltipsSupplier(GTOMachineTooltipsA.LargeAlgaeFarmTooltips)
            .block(GTOBlocks.STAINLESS_STEEL_CORROSION_RESISTANT_CASING)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("large_algae_farm").build())
                    .symbols(Symbols.create()
                            .where('A', blocks(GTOBlocks.IRIDIUM_CASING.get()))
                            .where('a', fluids(GTOFluids.VAPOR_OF_LEVITY.getSource()))
                            .wherePart('B', blocks(GTOBlocks.STAINLESS_STEEL_CORROSION_RESISTANT_CASING.get())
                                    .or(autoAbilities(definition.getRecipeTypes()))
                                    .or(blocks(GTAEMachines.ALGAE_ACCESS_HATCH.get()).setExactLimit(1))
                                    .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                                    .or(abilities(IMPORT_FLUIDS))
                                    .or(abilities(MAINTENANCE).setExactLimit(1)))
                            .where('C', blocks(GTOBlocks.STAINLESS_STEEL_CORROSION_RESISTANT_CASING.get()))
                            .where('D', blocks(GTBlocks.CLEANROOM_GLASS.get()))
                            .where('E', controller(definition))
                            .where('F', blocks(GTBlocks.FILTER_CASING_STERILE.get()))
                            .where('G', blocks(GTOBlocks.PRESSURE_CONTAINMENT_CASING.get()))
                            .where('g', glass())
                            .where('H', GTOPredicates.frame(GTOMaterials.Inconel242))
                            .where('I', blocks(GTOBlocks.BIOLOGICAL_MECHANICAL_CASING.get()))
                            .where('J', GTOPredicates.frame(GTOMaterials.StainlessSteel316))
                            .where('K', blocks(GTOBlocks.IRIDIUM_PIPE_CASING.get()))
                            .where('L', blocks(GTOBlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING.get()))
                            .where('l', fluids(GTOFluids.NUTRIENT_DISTILLATION.getSource()))
                            .where('M', blocks(GTOBlocks.HIGH_PRESSURE_PIPE_CASING.get()))
                            .where('N', GTOPredicates.frame(GTMaterials.BlueSteel))
                            .where('w', fluids(Fluids.WATER.getSource())))
                    .build())
            .renderer(MultiFluidRenderer.create(GTOCore.id("block/casings/stainless_steel_corrosion_resistant_casing"),
                    GTCEu.id("block/multiblock/gcym/large_chemical_bath")))
            .register();

    // 虚拟挖币机
    public static final MultiblockMachineDefinition VIRTUAL_COIN_MINER = multiblock("virtual_coin_miner", "虚拟挖币机", VirtualCoinMiner::new)
            .nonYAxisRotation()
            .recipeTypes(GTORecipeTypes.DUMMY_RECIPES)
            .tooltipsSupplier(GTOMachineStories.virtualCoinMinerTooltips)
            .tooltipsSupplier(GTOMachineTooltipsA.virtualCoinMinerTooltips)
            .block(GTBlocks.ADVANCED_COMPUTER_CASING)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("virtual_coin_miner").build())
                    .symbols(Symbols.create()
                            .where('A', blocks(GTBlocks.COMPUTER_CASING.get()))
                            .where('B', blocks(GTOBlocks.INSULATION_TILE_MECHANICAL_BLOCK.get()))
                            .where('C', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                            .where('D', blocks(GTOBlocks.COOLANT_PIPE_CASING.get()))
                            .wherePart('E', blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get())
                                    .or(autoAbilities(definition.getRecipeTypes()))
                                    .or(abilities(COMPUTATION_DATA_RECEPTION).setExactLimit(1))
                                    .or(abilities(INPUT_ENERGY).setExactLimit(1))
                                    .or(abilities(MAINTENANCE).setExactLimit(1)))
                            .where('F', GTOPredicates.light())
                            .where('G', blocks(GTBlocks.FILTER_CASING.get()))
                            .where('H', blocks(GTBlocks.COMPUTER_HEAT_VENT.get()))
                            .where('I', blocks(GTOBlocks.IRIDIUM_CASING.get()))
                            .where('J', blocks(GTOBlocks.OPTICAL_DYNAMIC_COATING_INSTRUMENT_PROTECTIVE_SHIELD_GLASS.get()))
                            .where('K', blocks(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_LUV.get()))
                            .where('L', blocks(GTOBlocks.T3_ME_STORAGE_CORE.get()))
                            .where('M', blocks(GTOBlocks.COMPONENT_ASSEMBLY_LINE_CASING_ZPM.get()))
                            .where('N', blocks(GTBlocks.CLEANROOM_GLASS.get()))
                            .where('O', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .where('P', controller(definition)))
                    .build())
            .sidedWorkableCasingRenderer("block/casings/hpca/advanced_computer_casing",
                    GTCEu.id("block/multiblock/research_station"))
            .register();

    // 太阳风暴聚合反应器
    public static final MultiblockMachineDefinition SOLAR_STORM_AGGREGATION_REACTOR = multiblock("solar_storm_aggregation_reactor", "太阳风暴聚合反应器", SolarStormAggregationReactor::new)
            .nonYAxisRotation()
            .recipeTypes(GTORecipeTypes.MIXER_RECIPES)
            .block(GTOBlocks.SINGULARITY_REINFORCED_STELLAR_SHIELDING_CASING)
            .structure(definition -> Structure.root(MultiBlockFileReader.piece(definition.getName()).build()).symbols(Symbols.create()
                    .wherePart('A', blocks(GTOBlocks.SINGULARITY_REINFORCED_STELLAR_SHIELDING_CASING.get())
                            .or(GTOPredicates.autoLaserAbilities(definition.getRecipeTypes()))
                            .or(abilities(MAINTENANCE).setExactLimit(1)))
                    .where('B', blocks(GTOBlocks.HYPER_MECHANICAL_CASING.get()))
                    .where('C', blocks(GTOBlocks.STELLAR_ENERGY_SIPHON_CASING.get()))
                    .where('D', GTOPredicates.frame(GTMaterials.Naquadria))
                    .where('E', blocks(GTOBlocks.SINGULARITY_REINFORCED_STELLAR_SHIELDING_CASING.get()))
                    .where('F', blocks(GTOBlocks.EXTREME_DENSITY_CASING.get()))
                    .where('G', GTOPredicates.frame(GTOMaterials.RadiationShieldingHighDensityTungstenSteel))
                    .where('H', blocks(GTOBlocks.SUPERCRITICAL_TURBINE_CASING.get()))
                    .where('I', blocks(GTOBlocks.PLASMA_FIELD_GLASS.get()))
                    .where('J', blocks(GTOBlocks.NEUTRONIUM_STABLE_CASING.get()))
                    .where('K', blocks(GTOBlocks.FERMI_ENERGY_GAP_TRANSITION_GLASS.get()))
                    .where('L', blocks(GTOBlocks.TRANSCENDENTALLY_AMPLIFIED_MAGNETIC_CONFINEMENT_CASING.get()))
                    .where('M', GTOPredicates.frame(GTOMaterials.HexaphaseCopper))
                    .where('N', GTOPredicates.frame(GTMaterials.Neutronium))
                    .where('O', blocks(GTOBlocks.CELESTIAL_MATTER_GUIDANCE_CASING.get()))
                    .where('P', controller(definition))).build())
            .renderer(SolarStormAggregationReactorRenderer::new)
            .hasTESR(true)
            .register();
}
