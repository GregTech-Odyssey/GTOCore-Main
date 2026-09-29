package com.gtocore.common.data.machines;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.client.renderer.machine.AdvancedHyperRenderer;
import com.gtocore.client.renderer.machine.AnnihilateGeneratorRenderer;
import com.gtocore.client.renderer.machine.ArrayMachineRenderer;
import com.gtocore.common.block.BlockMap;
import com.gtocore.common.data.*;
import com.gtocore.common.data.translation.GTOMachineStories;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.common.data.translation.GTOMachineTooltipsA;
import com.gtocore.common.machine.multiblock.electric.space.DysonSphereLaunchSiloMachine;
import com.gtocore.common.machine.multiblock.electric.space.DysonSphereReceivingStationMcahine;
import com.gtocore.common.machine.multiblock.generator.*;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.NewDataAttributes;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.utils.MultiBlockFileReader;
import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Slot;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.*;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;

import com.gto.registrate.util.entry.BlockEntry;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;
import static com.gtocore.common.data.GTORecipeTypes.*;
import static com.gtocore.common.data.machines.GeneratorMultiblockRegisters.*;
import static com.gtocore.utils.register.MachineRegisterUtils.*;
import static com.gtolib.api.registries.GTORegistration.GTO;

public final class GeneratorMultiblock {

    public static void init() {}

    public static final MultiblockMachineDefinition PHOTOVOLTAIC_POWER_STATION_ENERGETIC = registerPhotovoltaicPowerStation("energetic", "充能", 1, GTBlocks.CASING_STEEL_SOLID, GTOBlocks.ENERGETIC_PHOTOVOLTAIC_BLOCK, GTCEu.id("block/casings/solid/machine_casing_solid_steel"));
    public static final MultiblockMachineDefinition PHOTOVOLTAIC_POWER_STATION_PULSATING = registerPhotovoltaicPowerStation("pulsating", "脉冲", 4, GTBlocks.CASING_TITANIUM_STABLE, GTOBlocks.PULSATING_PHOTOVOLTAIC_BLOCK, GTCEu.id("block/casings/solid/machine_casing_stable_titanium"));
    public static final MultiblockMachineDefinition PHOTOVOLTAIC_POWER_STATION_VIBRANT = registerPhotovoltaicPowerStation("vibrant", "振动", 16, GTBlocks.CASING_TUNGSTENSTEEL_ROBUST, GTOBlocks.VIBRANT_PHOTOVOLTAIC_BLOCK, GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"));

    public static final MultiblockMachineDefinition PHOTOVOLTAIC_SAIL_CONTROLLER_ENERGETIC = registerPhotovoltaicSailController("energetic", "充能", "Energetic", 1, GTBlocks.CASING_STEEL_SOLID, GTOBlocks.ENERGETIC_PHOTOVOLTAIC_BLOCK, GTCEu.id("block/casings/solid/machine_casing_solid_steel"));
    public static final MultiblockMachineDefinition PHOTOVOLTAIC_SAIL_CONTROLLER_PULSATING = registerPhotovoltaicSailController("pulsating", "脉冲", "Pulsating", 4, GTBlocks.CASING_TITANIUM_STABLE, GTOBlocks.PULSATING_PHOTOVOLTAIC_BLOCK, GTCEu.id("block/casings/solid/machine_casing_stable_titanium"));
    public static final MultiblockMachineDefinition PHOTOVOLTAIC_SAIL_CONTROLLER_VIBRANT = registerPhotovoltaicSailController("vibrant", "振动", "Vibrant", 16, GTBlocks.CASING_TUNGSTENSTEEL_ROBUST, GTOBlocks.VIBRANT_PHOTOVOLTAIC_BLOCK, GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"));

    private static String photovoltaicModel(int basicRate) {
        if (basicRate < 4) return "PG-11";
        if (basicRate < 16) return "PG-12";
        return "PG-13";
    }

    private static MultiblockMachineDefinition registerPhotovoltaicPowerStation(String name, String cn, int basicRate, Supplier<? extends Block> casing, BlockEntry<?> photovoltaicBlock, ResourceLocation texture) {
        return multiblock(name + "_photovoltaic_power_station", cn + "光伏电站", holder -> new PhotovoltaicPowerStationMachine(holder, basicRate))
                .allRotation()
                .tooltips(GTOMachineStories.PhotovoltaicPlantTooltips.invoke(photovoltaicModel(basicRate)))
                .tooltips(GTOMachineTooltips.PhotovoltaicPlantTooltips)
                .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
                .generator()
                .block(casing)
                .structure(definition -> PhotovoltaicPowerStationMachine.getStructureCommon(definition, casing, photovoltaicBlock))
                .workableCasingRenderer(texture, GTCEu.id("block/multiblock/generator/large_steam_turbine"))
                .register();
    }

    private static MultiblockMachineDefinition registerPhotovoltaicSailController(String name, String cn, String en, int basicRate, Supplier<? extends Block> casing, BlockEntry<?> photovoltaicBlock, ResourceLocation texture) {
        return multiblock(name + "_photovoltaic_sail_controller", "探索者号空间站" + cn + "光伏帆板控制器", holder -> new PhotovoltaicSailControllerMachine(holder, basicRate))
                .langValue("Explorer Space Station " + en + " Photovoltaic Sail Controller")
                .mountedOn(GTOMachineProtocols.PHOTOVOLTAIC_SAIL)
                .allRotation()
                .workableInSpace()
                .tooltips(GTOMachineStories.PhotovoltaicSailTooltips.invoke(photovoltaicModel(basicRate)))
                .tooltips(GTOMachineTooltips.PhotovoltaicSailTooltips)
                .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
                .generator()
                .block(casing)
                .structure(definition -> PhotovoltaicSailControllerMachine.getSailStructure(definition, casing, photovoltaicBlock))
                .workableCasingRenderer(texture, GTCEu.id("block/multiblock/generator/large_steam_turbine"))
                .register();
    }

    public static final MultiblockMachineDefinition MAGNETIC_FLUID_GENERATOR = multiblock("magnetic_fluid_generator", "磁流体发电机", MagneticFluidGeneratorMachine::new)
            .allRotation()
            .recipeTypes(GTRecipeTypes.PLASMA_GENERATOR_FUELS)
            .tooltipsText("等离子体洪流带着磅礴的能量奔涌", "A torrent of plasma rushes forward with majestic energy")
            .tooltips(GTOMachineTooltipsA.magneticFluidGeneratorTooltips)
            .generator()
            .moduleTooltips(new PartAbility[0])
            .block(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .structure(definition -> Structure
                    .root(Piece.start(FRONT, UP, RIGHT)
                            .aisle("AAAABBBBBBBBBAAAA", "CCCCBBBBBBBBBCCCC", "CDDCBEEEEEEEBCCCC", "CDDCBBBBBBBBBCCCC", "CDDCBEEEEEEEBCCCC", "CCCCBBBBBBBBBCCCC", "AAAABBBBBBBBBAAAA")
                            .aisle("CCCCBBBBBBBBBCCCC", "IFFFFGHGHGHGFFFFI", "IJJFFGHGHGHGFFFFI", "IJJFFGHGHGHGFFFFI", "IJJFFGHGHGHGFFFFI", "IFFFFGHGHGHGFFFFI", "CCCCBBBBBBBBBCCCC")
                            .aisle("CDDCBEEEEEEEBCCCC", "IJJFFGHGHGHGFFFFI", "KKKKKKKKKKKKKKKKI", "KKKKKKKKKKKKKKKKI", "KKKKKKKKKKKKKKKKI", "IJJFFGHGHGHGFFFFI", "CDDCBEEEEEEEBCCCC")
                            .aisle("CDDCBBBBBBBBBCCCC", "IJJFFGHGHGHGFFFFI", "KKKKKKKKKKKKKKKKI", "KMMMMMMMMMMMMMMAL", "KKKKKKKKKKKKKKKKI", "IJJFFGHGHGHGFFFFI", "CDDCBBBBBBBBBCCCC")
                            .aisle("CDDCBEEEEEEEBCCCC", "IJJFFGHGHGHGFFFFI", "KKKKKKKKKKKKKKKKI", "KKKKKKKKKKKKKKKKI", "KKKKKKKKKKKKKKKKI", "IJJFFGHGHGHGFFFFI", "CDDCBEEEEEEEBCCCC")
                            .aisle("CCCCBBBBBBBBBCCCC", "IFFFFGHGHGHGFFFFI", "IJJFFGHGHGHGFFFFI", "IJJFFGHGHGHGFFFFI", "IJJFFGHGHGHGFFFFI", "IFFFFGHGHGHGFFFFI", "CCCCBBBBBBBBBCCCC")
                            .aisle("AAAABBBBBBBBBAAAA", "CCCCBBBBBBBBBCCCC", "CDDCBEEEEEEEBCCCC", "CDDCBBBBBBBBBCCCC", "CDDCBEEEEEEEBCCCC", "CCCCBBBBBBBBBCCCC", "AAAABBBBBBBBBAAAA")
                            .port('L', StructureModuleKeys.EXT_OUT, BACK)
                            .build())
                    .symbols(Symbols.create()
                            .where('A', GTOPredicates.frame(GTMaterials.Tungsten))
                            .where('B', blocks(GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.get()))
                            .where('C', GTOPredicates.absBlocks())
                            .where('D', GTOPredicates.glass())
                            .where('E', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                            .where('F', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()))
                            .where('G', blocks(ChemicalHelper.getBlock(TagPrefix.block, GTMaterials.NeodymiumMagnetic)))
                            .where('H', blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
                            .where('J', blocks(GCYMBlocks.HEAT_VENT.get()))
                            .where('K', blocks(GTOBlocks.BORON_CARBIDE_CERAMIC_RADIATION_RESISTANT_MECHANICAL_CUBE.get()))
                            .where('L', controller(definition))
                            .wherePart('I', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                                    .or(abilities(OUTPUT_LASER).setExactLimit(1))
                                    .or(abilities(MAINTENANCE).setExactLimit(1))
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(1))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1)))
                            .where('M', GTOPredicates.hermeticCasing())
                            .where('a', blocks(GTOBlocks.HYPER_MECHANICAL_CASING.get()))
                            .where('c', blocks(GTOBlocks.MAGTECH_CASING.get()))
                            .where('d', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .where('e', blocks(GTOBlocks.IRIDIUM_CASING.get()))
                            .where('f', blocks(GTOBlocks.BORON_CARBIDE_CERAMIC_RADIATION_RESISTANT_MECHANICAL_CUBE.get()))
                            .where('g', GTOPredicates.frame(GTMaterials.Naquadria))
                            .where('h', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                            .where('i', blocks(GTBlocks.FUSION_CASING_MK3.get()))
                            .where('j', blocks(GTOBlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.get()))
                            .where('k', blocks(GTBlocks.FUSION_GLASS.get()))
                            .where('l', blocks(RegistriesUtils.getBlock("gtceu:lead_block")))
                            .where('@', any()))
                    .atPort(StructureModuleKeys.EXT_OUT, Slot.optional(Piece.start(LEFT, UP, FRONT)
                            .aisle("  eeeeeeeee  ", "   eeeeeee   ", "  aaafffaaa  ", "     fff     ", "  aaafffaaa  ", "     a a     ", "     a a     ", "     a a     ", "             ", "             ")
                            .aisle(" eeeeeeeeeee ", "  eeejjjeee  ", "  aejjljjea  ", "   ejlllje   ", "  aejjljjea  ", "   eejjjee   ", "    eeeee    ", "     a a     ", "             ", "             ")
                            .aisle(" eeeeeeeeeee ", "  eeejjjeee  ", "  aejjljjea  ", "   ejlllje   ", "  aejjljjea  ", "   eejjjee   ", "    eeeee    ", "     a a     ", "             ", "             ")
                            .aisle(" eeeeeeeeeee ", "  eeejjjeee  ", "  aejjljjea  ", "   ejlllje   ", "  aejjljjea  ", "   eejjjee   ", "    eeeee    ", "     a a     ", "             ", "             ")
                            .aisle("eeeeea aeeeee", " eeeefffeeee ", "aaaaffgffaaaa", "  a fgcgf a  ", "aaaaffgffaaaa", "     fff     ", "     a a     ", "     a a     ", "     a a     ", "     a a     ")
                            .aisle("eeeeheeeheeee", "ehhhheeehhhhe", "aeeeeegeeeeea", "eeeeegcgeeeee", "aeeeeegeeeeea", "ehhhheeehhhhe", " eeeheeeheee ", "  eeheeehee  ", "   eheeehe   ", "    eaeae    ")
                            .aisle("eeeeeeeeeeeee", "ee  ciiic  ee", "ac  iigii  ca", "ec  igcgi  ce", "ac  iigii  ca", "ee  ciiic  ee", "eee       eee", " eee     eee ", "  eeeccceee  ", "   eeaeaee   ")
                            .aisle("efeeeeeeeeefe", " e  ciiic  e ", "ae  iigii  ea", " e  igcgi  e ", "ae  iigii  ea", " e  ciiic  e ", "  e       e  ", "   e     e   ", "    eeeee    ", "     a a     ")
                            .aisle("efeeeeeeeeefe", " e  ciiic  e ", "ae  iigii  ea", " e  igcgi  e ", "ae  iigii  ea", " e  ciiic  e ", "  e       e  ", "   e     e   ", "    eeeee    ", "     a a     ")
                            .aisle("effeeeeeeeffe", "  eeciiicee  ", " ae iigii ea ", "  e igcgi e  ", " ae iigii ea ", "  eeciiicee  ", "   ee   ee   ", "    eeeee    ", "     a a     ", "             ")
                            .aisle("efffeeeeefffe", "   eciiice   ", "  aeiigiiea  ", "   eigcgie   ", "  aeiigiiea  ", "   eciiice   ", "    eeeee    ", "     a a     ", "             ", "             ")
                            .aisle("eeeeeeeeeeeee", "   eciiice   ", "  aeiigiiea  ", "   eigcgie   ", "  aeiigiiea  ", "   eciiice   ", "    eeeee    ", "     a a     ", "             ", "             ")
                            .aisle("ejffeeeeeffje", "  f cieic f  ", "  f iigii f  ", "  feegcgeef  ", "  f iigii f  ", "  f cieic f  ", "  ff  e  ff  ", "   fffffff   ", "             ", "             ")
                            .aisle("ejffeeeeeffje", "  f ciiic f  ", "  f iigii f  ", "  f igcgi f  ", "  f iigii f  ", "  f ciiic f  ", "  ff     ff  ", "   fffffff   ", "             ", "             ")
                            .aisle("ejffeeeeeffje", "  f ciiic f  ", "  k iigii k  ", "  k igcgi k  ", "  k iigii k  ", "  f ciiic f  ", "  ff     ff  ", "   ffkkkff   ", "             ", "             ")
                            .aisle("ejffeeeeeffje", "  f ciiic f  ", "  f iigii f  ", "  f igcgi f  ", "  f iigii f  ", "  f ciiic f  ", "  ff     ff  ", "   fffffff   ", "             ", "             ")
                            .aisle("ejffeeeeeffje", "  f cieic f  ", "  f iigii f  ", "  feegcgeef  ", "  f iigii f  ", "  f cieic f  ", "  ff  e  ff  ", "   fffffff   ", "             ", "             ")
                            .aisle("eeeeeeeeeeeee", "   eciiice   ", "  aeiigiiea  ", "   eigcgie   ", "  aeiigiiea  ", "   eciiice   ", "    eeeee    ", "     a a     ", "             ", "             ")
                            .aisle("efffeeeeefffe", "   eciiice   ", "  aeiigiiea  ", "   eigcgie   ", "  aeiigiiea  ", "   eciiice   ", "    eeeee    ", "     a a     ", "             ", "             ")
                            .aisle("effeeeeeeeffe", "  eeciiicee  ", " ae iigii ea ", "  e igcgi e  ", " ae iigii ea ", "  eeciiicee  ", "   ee   ee   ", "    eeeee    ", "     a a     ", "             ")
                            .aisle("efeeeeeeeeefe", " e  ciiic  e ", "ae  iigii  ea", " e  igcgi  e ", "ae  iigii  ea", " e  ciiic  e ", "  e       e  ", "   e     e   ", "    eeeee    ", "     a a     ")
                            .aisle("efeeeeeeeeefe", " e  ciiic  e ", "ae  iigii  ea", " e  igcgi  e ", "ae  iigii  ea", " e  ciiic  e ", "  e       e  ", "   e     e   ", "    eeeee    ", "     a a     ")
                            .aisle("eeeeeeeeeeeee", "ee  ciiic  ee", "ac  iigii  ca", "ec  igcgi  ce", "ac  iigii  ca", "ee  ciiic  ee", "eee       eee", " eee     eee ", "  eeeccceee  ", "   eeaeaee   ")
                            .aisle("eeeeheeeheeee", "ehhhheeehhhhe", "aeeeeegeeeeea", "eeeeegcgeeeee", "aeeeeegeeeeea", "ehhhheeehhhhe", " eeeheeeheee ", "  eeheeehee  ", "   eheeehe   ", "    eaeae    ")
                            .aisle("eeeeea aeeeee", "     fff     ", "aaaaffgffaaaa", "    fgcgf    ", "aaaaffgffaaaa", "     fff     ", "     a a     ", "     a a     ", "     a a     ", "     a a     ")
                            .aisle("aaa       aaa", "a a       a a", "a a       a a", "a a       a a", "a a       a a", "a a       a a", "aaa       aaa", "  aaaaaaaaa  ", "     a a     ", "             ")
                            .aisle("aca       aca", "             ", "             ", "             ", "             ", "a a       a a", "aca       aca", "  ddd   ddd  ", "     a a     ", "             ")
                            .aisle("aca       aca", "             ", "             ", "             ", "a a       a a", "             ", "aca       aca", "  ddd   ddd  ", "     a a     ", "             ")
                            .aisle("aca       aca", "             ", "             ", "a a       a a", "             ", "             ", "aca       aca", "  aaaaaaaaa  ", "     a a     ", "             ")
                            .aisle("aca       aca", "             ", "a a       a a", "             ", "             ", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aca       aca", "a a       a a", "             ", "             ", "             ", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aaa       aaa", "a a       a a", "a a       a a", "a a       a a", "a a       a a", "a a       a a", "aaa       aaa", "             ", "             ", "             ")
                            .aisle("aca       aca", "             ", "             ", "             ", "             ", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aca       aca", "             ", "             ", "             ", "             ", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aca       aca", "             ", "             ", "             ", "             ", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aaa       aaa", "a a       a a", "a a       a a", "a a       a a", "a a       a a", "a a       a a", "aaa       aaa", "             ", "             ", "             ")
                            .aisle("aca       aca", "a a       a a", "             ", "             ", "             ", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aca       aca", "             ", "a a       a a", "             ", "             ", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aca       aca", "             ", "             ", "a a       a a", "             ", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aca       aca", "             ", "             ", "             ", "a a       a a", "             ", "aca       aca", "             ", "             ", "             ")
                            .aisle("aca       aca", "             ", "             ", "      @      ", "             ", "a a       a a", "aca       aca", "             ", "             ", "             ")
                            .aisle("aaa       aaa", "a a       a a", "a a       a a", "a a       a a", "a a       a a", "a a       a a", "aaa       aaa", "             ", "             ", "             ")
                            .port('@', StructureModuleKeys.EXT_IN, FRONT)
                            .build(), StructureModuleKeys.EXT_IN).count(MagneticFluidGeneratorMachine.EXTENSION))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"), GTCEu.id("block/multiblock/generator/extreme_combustion_engine"))
            .register();

    public static final MultiblockMachineDefinition LARGE_SEMI_FLUID_GENERATOR = registerLargeCombustionEngine(GTO,
            "large_semi_fluid_generator", "大型半流质发电机", EV, GTORecipeTypes.SEMI_FLUID_GENERATOR_FUELS,
            GTBlocks.CASING_TITANIUM_STABLE, GTBlocks.CASING_STEEL_GEARBOX, GTBlocks.CASING_ENGINE_INTAKE,
            GTCEu.id("block/casings/solid/machine_casing_stable_titanium"),
            GTCEu.id("block/multiblock/generator/large_combustion_engine"), false);

    public static final MultiblockMachineDefinition CHEMICAL_ENERGY_DEVOURER = multiblock("chemical_energy_devourer", "化学能吞噬者", ChemicalEnergyDevourerMachine::new)
            .nonYAxisRotation()
            .recipeTypes(COMBUSTION_GENERATOR_FUELS, GAS_TURBINE_FUELS, ROCKET_ENGINE_FUELS)
            .generator()
            .tooltips(GTOMachineTooltips.ChemicalEnergyDevourerGenerateTooltips)
            .block(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("chemical_energy_devourer").build())
                    .symbols(Symbols.create()
                            .where('A', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()))
                            .where('B', blocks(GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.get()))
                            .where('C', blocks(GTBlocks.MACHINE_CASING_LuV.get()))
                            .where('D', GTOPredicates.frame(GTMaterials.TungstenSteel))
                            .where('E', blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
                            .where('F', blocks(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get()))
                            .where('G', blocks(GCYMBlocks.HEAT_VENT.get()))
                            .where('H', blocks(GTBlocks.CASING_TUNGSTENSTEEL_PIPE.get()))
                            .where('I', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                            .where('J', blocks(GTBlocks.FILTER_CASING.get()))
                            .where('K', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                            .where('L', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                            .where('M', blocks(GTBlocks.FIREBOX_TUNGSTENSTEEL.get()))
                            .where('N', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                                    .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(4, 4)))
                            .wherePart('n', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get())
                                    .or(abilities(MAINTENANCE).setExactLimit(1))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(4, 4)))
                            .where('O', blocks(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                            .where('P', blocks(GTBlocks.CASING_STEEL_PIPE.get()))
                            .where('Q', controller(definition))
                            .where('R', blocks(GTBlocks.CASING_STEEL_TURBINE.get()))
                            .where('r', blocks(GTBlocks.CASING_STEEL_TURBINE.get())
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(4, 4)))
                            .where('S', blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get()))
                            .where('T', GTOPredicates.frame(GTMaterials.StainlessSteel))
                            .where('U', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                            .where('V', blocks(GTOBlocks.MAGNESIUM_OXIDE_CERAMIC_HIGH_TEMPERATURE_INSULATION_MECHANICAL_BLOCK.get()))
                            .where('W', abilities(MUFFLER)))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    GTCEu.id("block/multiblock/generator/extreme_combustion_engine"), false)
            .register();

    public static final MultiblockMachineDefinition ROCKET_LARGE_TURBINE = registerLargeTurbine(GTO,
            "rocket_large_turbine", "大型火箭引擎涡轮", EV, true,
            ROCKET_ENGINE_FUELS,
            GTBlocks.CASING_TITANIUM_TURBINE, GTBlocks.CASING_TITANIUM_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_titanium"),
            GTCEu.id("block/multiblock/generator/large_gas_turbine"), false);

    public static final MultiblockMachineDefinition SUPERCRITICAL_STEAM_TURBINE = registerLargeTurbine(GTO,
            "supercritical_steam_turbine", "超临界蒸汽涡轮", IV, false,
            GTORecipeTypes.SUPERCRITICAL_STEAM_TURBINE_FUELS,
            GTOBlocks.SUPERCRITICAL_TURBINE_CASING, GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX,
            GTOCore.id("block/casings/supercritical_turbine_casing"),
            GTCEu.id("block/multiblock/generator/large_plasma_turbine"), false);

    public static final MultiblockMachineDefinition STEAM_MEGA_TURBINE = registerMegaTurbine("steam_mega_turbine", "特大蒸汽涡轮", EV, false, GTRecipeTypes.STEAM_TURBINE_FUELS, GTBlocks.CASING_STEEL_TURBINE, GTBlocks.CASING_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_steel"), GTCEu.id("block/multiblock/generator/large_steam_turbine"), s -> s
                    .where('a', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                    .where('b', blocks(GTBlocks.CASING_STAINLESS_TURBINE.get()))
                    .where('c', blocks(GTBlocks.CASING_STEEL_TURBINE.get())
                            .or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(4)))
                    .where('d', blocks(GTOBlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING.get()))
                    .where('e', blocks(GTBlocks.CASING_ENGINE_INTAKE.get()))
                    .where('f', GTOPredicates.frame(GTMaterials.StainlessSteel))
                    .where('g', GTOPredicates.frame(GTMaterials.BlackSteel))
                    .where('h', blocks(GTBlocks.FILTER_CASING.get()))
                    .where('i', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('j', blocks(GTBlocks.CASING_STAINLESS_STEEL_GEARBOX.get())));
    public static final MultiblockMachineDefinition GAS_MEGA_TURBINE = registerMegaTurbine("gas_mega_turbine", "特大燃气涡轮", IV, false, GTRecipeTypes.GAS_TURBINE_FUELS, GTBlocks.CASING_STAINLESS_TURBINE, GTBlocks.CASING_STAINLESS_STEEL_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_stainless_steel"), GTCEu.id("block/multiblock/generator/large_gas_turbine"), s -> s
                    .where('a', blocks(GTBlocks.CASING_TITANIUM_STABLE.get()))
                    .where('b', blocks(GTBlocks.CASING_TITANIUM_TURBINE.get()))
                    .where('c', blocks(GTBlocks.CASING_STAINLESS_TURBINE.get())
                            .or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(4)))
                    .where('d', blocks(GTOBlocks.HIGH_PRESSURE_PIPE_CASING.get()))
                    .where('e', blocks(GTBlocks.CASING_ENGINE_INTAKE.get()))
                    .where('f', GTOPredicates.frame(GTMaterials.Titanium))
                    .where('g', GTOPredicates.frame(GTMaterials.BlackSteel))
                    .where('h', blocks(GTBlocks.FILTER_CASING.get()))
                    .where('i', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('j', blocks(GTBlocks.CASING_TITANIUM_GEARBOX.get())));
    public static final MultiblockMachineDefinition ROCKET_MEGA_TURBINE = registerMegaTurbine("rocket_mega_turbine", "特大火箭引擎涡轮", IV, true, ROCKET_ENGINE_FUELS, GTBlocks.CASING_TITANIUM_TURBINE, GTBlocks.CASING_TITANIUM_GEARBOX,
            GTCEu.id("block/casings/mechanic/machine_casing_turbine_titanium"), GTCEu.id("block/multiblock/generator/large_gas_turbine"), s -> s
                    .where('a', blocks(GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get()))
                    .where('b', blocks(GTBlocks.CASING_TUNGSTENSTEEL_TURBINE.get()))
                    .where('c', blocks(GTBlocks.CASING_TITANIUM_TURBINE.get())
                            .or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(4)))
                    .where('d', blocks(GTOBlocks.HIGH_PRESSURE_PIPE_CASING.get()))
                    .where('e', blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                    .where('f', GTOPredicates.frame(GTMaterials.TungstenSteel))
                    .where('g', GTOPredicates.frame(GTMaterials.BlackSteel))
                    .where('h', blocks(GTBlocks.FILTER_CASING.get()))
                    .where('i', blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where('j', blocks(GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX.get())));
    public static final MultiblockMachineDefinition SUPERCRITICAL_MEGA_STEAM_TURBINE = registerMegaTurbine("supercritical_mega_steam_turbine", "特大超临界蒸汽涡轮", LuV, false, GTORecipeTypes.SUPERCRITICAL_STEAM_TURBINE_FUELS, GTOBlocks.SUPERCRITICAL_TURBINE_CASING, GTBlocks.CASING_TUNGSTENSTEEL_GEARBOX,
            GTOCore.id("block/casings/supercritical_turbine_casing"), GTCEu.id("block/multiblock/generator/large_plasma_turbine"), s -> s
                    .where('a', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                    .where('b', blocks(GTOBlocks.SUPERCRITICAL_TURBINE_CASING.get()))
                    .where('c', blocks(GTOBlocks.SUPERCRITICAL_TURBINE_CASING.get())
                            .or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(4)))
                    .where('d', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                    .where('e', blocks(GTBlocks.CASING_EXTREME_ENGINE_INTAKE.get()))
                    .where('f', GTOPredicates.frame(GTMaterials.Iridium))
                    .where('g', GTOPredicates.frame(GTMaterials.BlackSteel))
                    .where('h', blocks(GTBlocks.FILTER_CASING.get()))
                    .where('i', blocks(GTOBlocks.HSSS_BOROSILICATE_GLASS.get()))
                    .where('j', blocks(GTOBlocks.IRIDIUM_GEARBOX.get())));

    public static final MultiblockMachineDefinition DYSON_SPHERE_LAUNCH_SILO = multiblock("dyson_sphere_launch_silo", "戴森球发射井", DysonSphereLaunchSiloMachine::new)
            .nonYAxisRotation()
            .recipeTypes(GTORecipeTypes.DYSON_SPHERE_RECIPES)
            .block(GTOBlocks.SPACE_ELEVATOR_MECHANICAL_CASING)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("dyson_sphere_launch_silo").build())
                    .symbols(Symbols.create()
                            .where('~', controller(definition))
                            .where('A', blocks(GTOBlocks.HIGH_STRENGTH_CONCRETE.get()))
                            .where('B', blocks(GTOBlocks.SPACE_ELEVATOR_INTERNAL_SUPPORT.get()))
                            .where('C', blocks(GTOBlocks.MOLECULAR_CASING.get()))
                            .where('D', blocks(GTOBlocks.SPACE_ELEVATOR_SUPPORT.get()))
                            .where('E', blocks(GTOBlocks.DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                            .where('F', blocks(GTOBlocks.DYSON_DEPLOYMENT_MAGNET.get()))
                            .where('G', blocks(GTOBlocks.GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                            .where('H', blocks(GTOBlocks.DIMENSIONAL_BRIDGE_CASING.get()))
                            .where('I', blocks(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_5.get()))
                            .where('J', blocks(GTOBlocks.ENHANCE_HYPER_MECHANICAL_CASING.get()))
                            .where('K', blocks(GTOBlocks.DIMENSION_INJECTION_CASING.get()))
                            .where('L', blocks(GTOBlocks.DYSON_DEPLOYMENT_CASING.get()))
                            .where('M', GTOPredicates.frame(GTOMaterials.Quantanium))
                            .where('N', blocks(GTOBlocks.HOLLOW_CASING.get()))
                            .where('O', GTOPredicates.frame(GTOMaterials.Mithril))
                            .where('P', GTOPredicates.frame(GTMaterials.NaquadahAlloy))
                            .where('Q', blocks(GTOBlocks.CONTAINMENT_FIELD_GENERATOR.get()))
                            .where('R', blocks(GTOBlocks.FUSION_CASING_MK5.get()))
                            .where('S', blocks(GTOBlocks.DYSON_CONTROL_CASING.get()))
                            .where('T', blocks(GTOBlocks.DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                            .where('U', blocks(GTOBlocks.ACCELERATED_PIPELINE.get()))
                            .where('V', blocks(GTOBlocks.DYSON_CONTROL_TOROID.get()))
                            .where('W', blocks(GTOBlocks.DYSON_DEPLOYMENT_CORE.get()))
                            .where('X', blocks(GTOBlocks.SPACETIME_ASSEMBLY_LINE_UNIT.get()))
                            .where('Y', blocks(GTBlocks.CASING_PALLADIUM_SUBSTATION.get()))
                            .where('[', blocks(GTOBlocks.HIGH_ENERGY_ULTRAVIOLET_EMITTER_CASING.get()))
                            .wherePart('a', blocks(GTOBlocks.DYSON_CONTROL_CASING.get())
                                    .or(abilities(COMPUTATION_DATA_RECEPTION).setExactLimit(1))
                                    .or(autoAbilities(definition.getRecipeTypes()))))
                    .build())
            .workableCasingRenderer(GTOCore.id("block/casings/space_elevator_mechanical_casing"), GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    public static final MultiblockMachineDefinition DYSON_SPHERE_RECEIVING_STATION = multiblock("dyson_sphere_receiving_station", "戴森球接收站", DysonSphereReceivingStationMcahine::new)
            .nonYAxisRotation()
            .workableInSpace()
            .generator()
            .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(GTOMachineTooltipsA.dysonSphereReceivingStationTooltips)
            .block(GTBlocks.HIGH_POWER_CASING)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("dyson_sphere_receiving_station").build())
                    .symbols(Symbols.create()
                            .where('~', controller(definition))
                            .where('A', blocks(GTOBlocks.ENHANCE_HYPER_MECHANICAL_CASING.get()))
                            .where('B', blocks(GTOBlocks.DYSON_CONTROL_TOROID.get()))
                            .where('C', blocks(GTOBlocks.RADIATION_ABSORBENT_CASING.get()))
                            .where('D', blocks(GTOBlocks.HIGH_STRENGTH_CONCRETE.get()))
                            .where('E', blocks(GTOBlocks.SPACE_ELEVATOR_INTERNAL_SUPPORT.get()))
                            .where('F', GTOPredicates.frame(GTMaterials.NaquadahAlloy))
                            .where('G', blocks(GTOBlocks.HYPER_CORE.get()))
                            .where('H', blocks(GTOBlocks.MOLECULAR_CASING.get()))
                            .where('I', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .wherePart('a', blocks(GTBlocks.HIGH_POWER_CASING.get())
                                    .or(abilities(IMPORT_FLUIDS).setExactLimit(1))
                                    .or(abilities(COMPUTATION_DATA_RECEPTION).setExactLimit(1))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(abilities(OUTPUT_LASER).setExactLimit(1)))
                            .where('K', blocks(GTOBlocks.SPACE_ELEVATOR_SUPPORT.get()))
                            .where('L', blocks(GTOBlocks.MOLECULAR_COIL.get()))
                            .where('M', blocks(GTOBlocks.DYSON_RECEIVER_CASING.get()))
                            .where('N', blocks(GTOBlocks.DIMENSIONALLY_TRANSCENDENT_CASING.get())))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/hpca/high_power_casing"), GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    public static final MultiblockMachineDefinition LARGE_NAQUADAH_REACTOR = multiblock("large_naquadah_reactor", "大型硅岩反应堆", ElectricMultiblockMachine::new)
            .allRotation()
            .recipeTypes(GTORecipeTypes.LARGE_NAQUADAH_REACTOR_RECIPES)
            .generator()
            .block(GTOBlocks.HYPER_MECHANICAL_CASING)
            .recipeModifier(RecipeModifier.GENERATOR_OVERCLOCKING)
            .structure(definition -> Structure
                    .root(Piece.start(RIGHT, UP, BACK)
                            .aisle("                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "           a~a           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                            .aisle("                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "           AAA           ", "          aBBBa          ", "           AAA           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                            .aisle("                         ", "          CCCCC          ", "        CC     CC        ", "      CC         CC      ", "    CC             CC    ", "    C               C    ", "   C                 C   ", "   C                 C   ", "  C                   C  ", "  C                   C  ", " C         AAA         C ", " C        A   A        C ", " C       aB   Ba       C ", " C        A   A        C ", " C         AAA         C ", "  C                   C  ", "  C                   C  ", "   C                 C   ", "   C                 C   ", "    C               C    ", "    CC             CC    ", "      CC         CC      ", "        CC     CC        ", "          CCCCC          ", "                         ")
                            .aisle("          CCCCC          ", "        CC     CC        ", "      CC  CCCCC  CC      ", "    CC  DD  C  DD  CC    ", "   C  DD    C    DD  C   ", "   C D             D C   ", "  C D               D C  ", "  C D               D C  ", " C D        A        D C ", " C D        A        D C ", "C C       AAAAA       C C", "C C      A E E A      C C", "C CCC   aB E E Ba   CCC C", "C C      A E E A      C C", "C C       AAAAA       C C", " C D        A        D C ", " C D        A        D C ", "  C D               D C  ", "  C D               D C  ", "   C D             D C   ", "   C  DD    C    DD  C   ", "    CC  DD  C  DD  CC    ", "      CC  CCCCC  CC      ", "        CC     CC        ", "          CCCCC          ")
                            .aisle("          CCCCC          ", "        CC     CC        ", "      CC  DDFDD CCC      ", "    CC  DDGCFCGDD  CC    ", "   C  DD  GCFCG  DD  C   ", "   C D    G F G    D C   ", "  C D       F       D C  ", "  C D       F       D C  ", " C D       A A       D C ", " C D       A A       D C ", "C DGGG    AA AA    GGGD C", "C DCC    A     A    CCD C", "C DAAAAAAB     BAAAAAAD C", "C DCC    A     A    CCD C", "C DGGG    AA AA    GGGD C", " C D       A A       D C ", " C D       A A       D C ", "  C D       F       D C  ", "  C D       F       D C  ", "   C D    G F G    D C   ", "   CC DD  GCFCG  DD  C   ", "    CC  DDGCFCGDD  CC    ", "      CC  DDFDD  CC      ", "        CC     CC        ", "          CCCCC          ")
                            .aisle("          CCCCC          ", "        CC     CC        ", "      CC  CCCCC  CC      ", "    CC  DD  C  DD  CC    ", "   C  DD    C    DD  C   ", "   C D             D C   ", "  C D               D C  ", "  C D               D C  ", " C D        A        D C ", " C D        A        D C ", "C C       AAAAA       C C", "C C      A E E A      C C", "C CCC   aB E E Ba   CCC C", "C C      A E E A      C C", "C C       AAAAA       C C", " C D        A        D C ", " C D        A        D C ", "  C D               D C  ", "  C D               D C  ", "   C D             D C   ", "   CC DD    C    DD  C   ", "    CC  DD  C  DD  CC    ", "      CC  CCCCC  CC      ", "        CC     CC        ", "          CCCCC          ")
                            .aisle("                         ", "          CCCCC          ", "        CC     CC        ", "      CC         CC      ", "    CC             CC    ", "    C               C    ", "   C                 C   ", "   C                 C   ", "  C                   C  ", "  C                   C  ", " C         AAA         C ", " C        A   A        C ", " C       aB   Ba       C ", " C        A   A        C ", " C         AAA         C ", "  C                   C  ", "  C                   C  ", "   C                 C   ", "   C                 C   ", "    C               C    ", "    CC             CC    ", "      CC         CC      ", "        CC     CC        ", "          CCCCC          ", "                         ")
                            .aisle("                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "           AAA           ", "          aBBBa          ", "           AAA           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                            .aisle("                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "           aaa           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                            .build())
                    .symbols(Symbols.create()
                            .where('~', controller(definition))
                            .where('A', blocks(GTOBlocks.HYPER_MECHANICAL_CASING.get()))
                            .where('B', blocks(GTOBlocks.AMPROSIUM_GEARBOX.get()))
                            .where('C', blocks(GTOBlocks.EXTREME_STRENGTH_TRITANIUM_CASING.get()))
                            .where('D', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .where('E', GTOPredicates.frame(GTMaterials.Naquadria))
                            .where('F', blocks(GTOBlocks.AMPROSIUM_PIPE_CASING.get()))
                            .where('G', GTOPredicates.frame(GTMaterials.Trinium))
                            .wherePart('a', blocks(GTOBlocks.HYPER_MECHANICAL_CASING.get())
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                    .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(1))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(abilities(OUTPUT_LASER).setMaxGlobalLimited(1))))
                    .build())
            .workableCasingRenderer(GTOCore.id("block/casings/hyper_mechanical_casing"), GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    public static final MultiblockMachineDefinition ADVANCED_HYPER_REACTOR = multiblock("advanced_hyper_reactor", "进阶超能反应堆", ElectricMultiblockMachine::new)
            .nonYAxisRotation()
            .tooltipsText("提供不同等离子体获得不同并行", "Provides different plasmas to achieve different parallelism")
            .tooltipsText("星辉：8，致密中子：16", "Starmetal: 8, Dense Neutrons: 16")
            .recipeTypes(GTORecipeTypes.ADVANCED_HYPER_REACTOR_RECIPES)
            .generator()
            .recipeModifier((m, u, r) -> {
                int p = 1;
                if (u.inputFluid(GTOMaterials.DenseNeutron.getFluid(FluidStorageKeys.PLASMA), 1)) {
                    p = 16;
                } else if (u.inputFluid(GTOMaterials.Starmetal.getFluid(FluidStorageKeys.PLASMA), 1)) {
                    p = 8;
                }
                r = ParallelLogic.accurateParallel(m, u, r, p);
                if (r == null) return null;
                return RecipeModifier.generatorOverclocking(m, u, r);
            })
            .block(GTOBlocks.ENHANCE_HYPER_MECHANICAL_CASING)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("advanced_hyper_reactor").build())
                    .symbols(Symbols.create()
                            .where('~', controller(definition))
                            .where('A', blocks(GTOBlocks.DIMENSIONALLY_TRANSCENDENT_CASING.get()))
                            .where('B', blocks(GTOBlocks.ENHANCE_HYPER_MECHANICAL_CASING.get()))
                            .where('C', GTOPredicates.frame(GTMaterials.Naquadria))
                            .where('D', blocks(GTOBlocks.ECHO_CASING.get()))
                            .where('E', blocks(GTOBlocks.DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                            .where('F', blocks(GTOBlocks.DIMENSIONAL_BRIDGE_CASING.get()))
                            .where('G', blocks(GTOBlocks.CONTAINMENT_FIELD_GENERATOR.get()))
                            .where('H', blocks(GTOBlocks.HYPER_CORE.get()))
                            .wherePart('a', blocks(GTOBlocks.ENHANCE_HYPER_MECHANICAL_CASING.get())
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(1))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(abilities(MAINTENANCE).setExactLimit(1))
                                    .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(1))
                                    .or(abilities(OUTPUT_LASER).setMaxGlobalLimited(1))))
                    .build())
            .renderer(AdvancedHyperRenderer::new)
            .hasTESR(true)
            .register();

    public static final MultiblockMachineDefinition HYPER_REACTOR = multiblock("hyper_reactor", "超能反应堆", ElectricMultiblockMachine::new)
            .allRotation()
            .recipeTypes(GTORecipeTypes.HYPER_REACTOR_RECIPES)
            .tooltipsText("每次运行前提供额外的1mb等离子体将获得16的并行", "Providing additional 1mb plasma before each run will obtain 16 parallelism")
            .tooltipsText("不同燃料所需的等离子体不同", "Different fuels required different plasmas")
            .tooltipsText("从1-4顺序为: 山铜, 末影素, 魔金, 亚稳态\ud872\udf76", "The order from 1-4 is: Orichalcum, Enderium, Infuscolium, Metastable Hassium")
            .generator()
            .recipeModifier((m, u, r) -> {
                int p = 1;
                long outputEUt = r.getOutputEUt();
                if (outputEUt == V[UEV]) {
                    if (u.inputFluid(GTOMaterials.Orichalcum.getFluid(FluidStorageKeys.PLASMA), 1)) {
                        p = 16;
                    }
                } else if (outputEUt == V[UXV]) {
                    if (u.inputFluid(GTOMaterials.Enderium.getFluid(FluidStorageKeys.PLASMA), 1)) {
                        p = 16;
                    }
                } else if (outputEUt == V[OpV]) {
                    if (u.inputFluid(GTOMaterials.Infuscolium.getFluid(FluidStorageKeys.PLASMA), 1)) {
                        p = 16;
                    }
                } else if (outputEUt == V[MAX]) {
                    if (u.inputFluid(GTOMaterials.MetastableHassium.getFluid(FluidStorageKeys.PLASMA), 1)) {
                        p = 16;
                    }
                }
                r = ParallelLogic.accurateParallel(m, u, r, p);
                if (r == null) return null;
                return RecipeModifier.generatorOverclocking(m, u, r);
            })
            .block(GTOBlocks.ENHANCE_HYPER_MECHANICAL_CASING)
            .structure(definition -> Structure
                    .root(Piece.start(RIGHT, UP, BACK)
                            .aisle("                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "A                         A", "A                         A", "A                         A", "A                         A", "A                         A", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ")
                            .aisle("                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "A                         A", "A                         A", "ABB                     BBA", "ABB                     BBA", "ABB                     BBA", "ABB                     BBA", "ABB                     BBA", "A                         A", "A                         A", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ")
                            .aisle("                           ", "                           ", "                           ", "                           ", "A                         A", "A                         A", "ABB                     BBA", "ABB                     BBA", "A  BB                 BB  A", "A  BB                 BB  A", "A  BB                 BB  A", "A  BB                 BB  A", "A  BB                 BB  A", "ABB                     BBA", "ABB                     BBA", "A                         A", "A                         A", "                           ", "                           ", "                           ", "                           ")
                            .aisle("                           ", "                           ", "                           ", "A                         A", "ABB                     BBA", "ABB                     BBA", "A  BB                 BB  A", "A  BB                 BB  A", "A   CC               CC   A", "A   CC               CC   A", "A   CC               CC   A", "A   CC               CC   A", "A   CC               CC   A", "A  BB                 BB  A", "A  BB                 BB  A", "ABB                     BBA", "ABB                     BBA", "A                         A", "                           ", "                           ", "                           ")
                            .aisle("                           ", "                           ", "A                         A", "ABB                     BBA", "A  BB                 BB  A", "A  BB                 BB  A", "A   CC               CC   A", "A   CC               CC   A", "A     CC           CC     A", "A     CC           CC     A", "A     CC           CC     A", "A     CC           CC     A", "A     CC           CC     A", "A   CC               CC   A", "A   CC               CC   A", "A  BB                 BB  A", "A  BB                 BB  A", "ABB                     BBA", "A                         A", "                           ", "                           ")
                            .aisle("                           ", "                           ", "A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A", "                           ", "                           ")
                            .aisle("                           ", "A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      CDDDDDDDDDDDC      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      CDDDDDDDDDDDC      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A", "                           ")
                            .aisle("                           ", "A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      CAE       EAC      A", "A      C    aaa    C      A", "A      C    a~a    C      A", "A      C    aaa    C      A", "A      CAE       EAC      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A", "                           ")
                            .aisle("A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C   CCCCC   C      A", "A      C  aa   aa  C      A", "A      CFFFaC CaFFFC      A", "A      C  aa   aa  C      A", "A      C   CCCCC   C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A")
                            .aisle("A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C  CGGGGGC  C      A", "A      CCC       CCC      A", "A      CC    H    CC      A", "A      CCC       CCC      A", "A      C  CGGGGGC  C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A")
                            .aisle("A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      CFFFGGGGGFFFC      A", "A      CCC   H   CCC      A", "A      II   HHH   II      A", "A      CCC   H   CCC      A", "A      CFFFGGGGGFFFC      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A")
                            .aisle("A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C  CGGGGGC  C      A", "A      CCC       CCC      A", "A      CC    H    CC      A", "A      CCC       CCC      A", "A      C  CGGGGGC  C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A")
                            .aisle("A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C   CCCCCC  C      A", "A      C  aC   Ca  C      A", "A      CFFFaC CaFFFC      A", "A      C  aa   aa  C      A", "A      C   CCCCC   C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A")
                            .aisle("                           ", "A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      CAE       EAC      A", "A      C    aaa    C      A", "A      C    aaa    C      A", "A      C    aaa    C      A", "A      CAE       EAC      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A", "                           ")
                            .aisle("                           ", "A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A      CDDDDDDDDDDDC      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      CDDDDDDDDDDDC      A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A", "                           ")
                            .aisle("                           ", "                           ", "A                         A", "ABB                     BBA", "A  BB                 BB  A", "A   CC               CC   A", "A     CC           CC     A", "A     CC           CC     A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A      C           C      A", "A     CC           CC     A", "A     CC           CC     A", "A   CC               CC   A", "A  BB                 BB  A", "ABB                     BBA", "A                         A", "                           ", "                           ")
                            .aisle("                           ", "                           ", "A                         A", "ABB                     BBA", "A  BB                 BB  A", "A  BB                 BB  A", "A   CC               CC   A", "A   CC               CC   A", "A     CC           CC     A", "A     CC           CC     A", "A     CC           CC     A", "A     CC           CC     A", "A     CC           CC     A", "A   CC               CC   A", "A   CC               CC   A", "A  BB                 BB  A", "A  BB                 BB  A", "ABB                     BBA", "A                         A", "                           ", "                           ")
                            .aisle("                           ", "                           ", "                           ", "A                         A", "ABB                     BBA", "ABB                     BBA", "A  BB                 BB  A", "A  BB                 BB  A", "A   CC               CC   A", "A   CC               CC   A", "A   CC               CC   A", "A   CC               CC   A", "A   CC               CC   A", "A  BB                 BB  A", "A  BB                 BB  A", "ABB                     BBA", "ABB                     BBA", "A                         A", "                           ", "                           ", "                           ")
                            .aisle("                           ", "                           ", "                           ", "                           ", "A                         A", "A                         A", "ABB                     BBA", "ABB                     BBA", "A  BB                 BB  A", "A  BB                 BB  A", "A  BB                 BB  A", "A  BB                 BB  A", "A  BB                 BB  A", "ABB                     BBA", "ABB                     BBA", "A                         A", "A                         A", "                           ", "                           ", "                           ", "                           ")
                            .aisle("                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "A                         A", "A                         A", "ABB                     BBA", "ABB                     BBA", "ABB                     BBA", "ABB                     BBA", "ABB                     BBA", "A                         A", "A                         A", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ")
                            .aisle("                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "A                         A", "A                         A", "A                         A", "A                         A", "A                         A", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ", "                           ")
                            .build())
                    .symbols(Symbols.create()
                            .where('~', controller(definition))
                            .where('A', blocks(GTOBlocks.MOLECULAR_CASING.get()))
                            .where('B', blocks(GTOBlocks.PIKYONIUM_MACHINE_CASING.get()))
                            .where('C', blocks(GTOBlocks.ENHANCE_HYPER_MECHANICAL_CASING.get()))
                            .where('D', frames(GTMaterials.Neutronium))
                            .where('E', blocks(GTOBlocks.CONTAINMENT_FIELD_GENERATOR.get()))
                            .where('F', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .where('G', blocks(GTBlocks.FUSION_GLASS.get()))
                            .where('H', blocks(GTOBlocks.HYPER_CORE.get()))
                            .where('I', blocks(GTOBlocks.AMPROSIUM_PIPE_CASING.get()))
                            .wherePart('a', blocks(GTOBlocks.ENHANCE_HYPER_MECHANICAL_CASING.get())
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(abilities(MAINTENANCE).setExactLimit(1))
                                    .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(1))
                                    .or(abilities(OUTPUT_LASER).setMaxGlobalLimited(1))))
                    .build())
            .workableCasingRenderer(GTOCore.id("block/casings/enhance_hyper_mechanical_casing"), GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    public static final MultiblockMachineDefinition GENERATOR_ARRAY = multiblock("generator_array", "发电阵列", GeneratorArrayMachine::new)
            .nonYAxisRotation()
            .recipeTypes(GTRecipeTypes.DUMMY_RECIPES)
            .generator()
            .addTooltipsFromClass(GeneratorArrayMachine.class)
            .tooltips(NewDataAttributes.RECIPES_TYPE.create(
                    Component.empty().append(Component.translatable("gtceu.steam_turbine"))
                            .append(", ").append(Component.translatable("gtceu.combustion_generator"))
                            .append(", ").append(Component.translatable("gtceu.gas_turbine"))
                            .append(", ").append(Component.translatable("gtceu.semi_fluid_generator"))
                            .append(", ").append(Component.translatable("gtceu.rocket_engine"))
                            .append(", ").append(Component.translatable("gtceu.naquadah_reactor"))))
            .block(GTBlocks.CASING_STEEL_SOLID)
            .blockProp(p -> p.noOcclusion().isViewBlocking((state, level, pos) -> false))
            .shape(Shapes.box(0.001, 0.001, 0.001, 0.999, 0.999, 0.999))
            .structure(definition -> Structure
                    .root(Piece.start(LEFT, UP, FRONT)
                            .aisle("XXX", "CCC", "XXX")
                            .aisle("XXX", "C#C", "XXX")
                            .aisle("XSX", "CCC", "XXX")
                            .build())
                    .symbols(Symbols.create()
                            .where('S', controller(definition))
                            .wherePart('X', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                    .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(4))
                                    .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(1))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(abilities(MAINTENANCE).setExactLimit(1)))
                            .where('C', blocks(GTBlocks.CASING_TEMPERED_GLASS.get()))
                            .where('#', air()))
                    .build())
            .renderer(() -> new ArrayMachineRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/processing_array")))
            .register();

    public static final MultiblockMachineDefinition ANNIHILATE_GENERATOR = multiblock("annihilate_generator", "人造恒星", WirelessEnergyGeneratorMachine::new)
            .nonYAxisRotation()
            .langValue("Artificial Star")
            .recipeTypes(GTORecipeTypes.ANNIHILATE_GENERATOR_RECIPES)
            .generator()
            .block(GTBlocks.HIGH_POWER_CASING)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("annihilate_generator").build())
                    .symbols(Symbols.create()
                            .where('A', blocks(GTOBlocks.NAQUADAH_ALLOY_CASING.get()))
                            .where('B', blocks(GTOBlocks.GRAVITON_FIELD_CONSTRAINT_CASING.get()))
                            .where('C', blocks(GTOBlocks.ANNIHILATE_CORE.get()))
                            .where('D', blocks(GTOBlocks.ANTIMATTER_CONTAINMENT_CASING.get()))
                            .where('E', blocks(GTOBlocks.HYPER_MECHANICAL_CASING.get()))
                            .where('F', blocks(GTOBlocks.HOLLOW_CASING.get()))
                            .where('G', blocks(GTOBlocks.DYSON_CONTROL_TOROID.get()))
                            .where('H', blocks(GTOBlocks.RHENIUM_REINFORCED_ENERGY_GLASS.get()))
                            .where('I', blocks(GTOBlocks.DYSON_CONTROL_CASING.get()))
                            .where('J', blocks(GTOBlocks.DYSON_RECEIVER_CASING.get()))
                            .wherePart('K', blocks(GTBlocks.HIGH_POWER_CASING.get())
                                    .or(blocks(GTOMachines.WIRELESS_ENERGY_INTERFACE_HATCH.get()).setMaxGlobalLimited(1))
                                    .or(abilities(OUTPUT_LASER))
                                    .or(abilities(IMPORT_ITEMS))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(abilities(EXPORT_ITEMS)))
                            .where('L', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .where('M', blocks(GTOBlocks.DEGENERATE_RHENIUM_CONSTRAINED_CASING.get()))
                            .where('N', controller(definition)))
                    .build())
            .renderer(AnnihilateGeneratorRenderer::new)
            .hasTESR(true)
            .register();

    public static final MultiblockMachineDefinition FUEL_CELL_GENERATOR = multiblock("fuel_cell_generator", "燃料电池发电机", FullCellGenerator::new)
            .nonYAxisRotation()
            .recipeTypes(GTORecipeTypes.FUEL_CELL_ENERGY_ABSORPTION_RECIPES)
            .recipeTypes(GTORecipeTypes.FUEL_CELL_ENERGY_TRANSFER_RECIPES)
            .recipeTypes(GTORecipeTypes.FUEL_CELL_ENERGY_RELEASE_RECIPES)
            .generator()
            .addTooltipsFromClass(FullCellGenerator.class)
            .tooltipsSupplier(GTOMachineTooltips.FuelCellGeneratorTooltips)
            .block(GTOBlocks.IRIDIUM_CASING)
            .recipeModifier((m, u, r) -> {
                if (m instanceof FullCellGenerator f && f.isGenerator())
                    return RecipeModifier.GENERATOR_OVERCLOCKING.applyModifier(m, u, r);
                else return r;
            })
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("fuel_cell_generator").build())
                    .symbols(Symbols.create()
                            .wherePart('A', blocks(GTOBlocks.IRIDIUM_CASING.get())
                                    .or(abilities(OUTPUT_ENERGY).or(abilities(INPUT_ENERGY).or(abilities(OUTPUT_LASER))))
                                    .or(abilities(IMPORT_ITEMS))
                                    .or(abilities(IMPORT_FLUIDS))
                                    .or(abilities(EXPORT_ITEMS))
                                    .or(abilities(EXPORT_FLUIDS))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(blocks(GTOMachines.ION_ACTIVITY_SENSOR.get()).setMaxGlobalLimited(1)))
                            .where('B', blocks(GCYMBlocks.ELECTROLYTIC_CELL.get()))
                            .where('C', controller(definition))
                            .where('D', blocks(GTOBlocks.FLOCCULATION_CASING.get()))
                            .where('E', blocks(GTOBlocks.IRIDIUM_CASING.get()))
                            .where('F', blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                            .where('G', blocks(GTOBlocks.PRESSURE_CONTAINMENT_CASING.get()))
                            .where('H', GTOPredicates.frame(GTMaterials.HSSG))
                            .where('I', blocks(GTOBlocks.IRIDIUM_PIPE_CASING.get()))
                            .where('J', blocks(GTOBlocks.CALCIUM_OXIDE_CERAMIC_ANTI_METAL_CORROSION_MECHANICAL_BLOCK.get()))
                            .where('K', blocks(GTBlocks.FILTER_CASING.get()))
                            .where('L', blocks(GTBlocks.CASING_PALLADIUM_SUBSTATION.get()))
                            .where('M', blocks(GTOBlocks.CHEMICAL_CORROSION_RESISTANT_PIPE_CASING.get()))
                            .where('N', blocks(GTOBlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.get()))
                            .where('O', blocks(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                            .where('P', blocks(GTBlocks.CASING_PTFE_INERT.get()))
                            .where('Q', blocks(GTBlocks.BATTERY_EMPTY_TIER_II.get()))
                            .where('R', blocks(GTBlocks.FUSION_GLASS.get())))
                    .build())
            .renderer(() -> new ArrayMachineRenderer(GTOCore.id("block/casings/iridium_casing"), GTCEu.id("block/multiblock/processing_array")))
            .register();

    public static final MultiblockMachineDefinition BIO_OSCILLATION_ELECTRIC_STIMULATOR = multiblock("bio_oscillation_electric_stimulator", "生物振荡电刺激器", BioOscillationElectricStimulator::new)
            .mountedOn(GTOMachineProtocols.BIO_STIMULATOR)
            .nonYAxisRotation()
            .recipeTypes(DUMMY_RECIPES)
            .generator()
            .tooltipsSupplier(GTOMachineTooltipsA.BioOscillationElectricStimulatorTooltips)
            .block(GTOBlocks.BIOACTIVE_MECHANICAL_CASING)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("bio_oscillation_generator/module").build())
                    .symbols(Symbols.create()
                            .where('A', blocks(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                            .where('B', blocks(GTOBlocks.TITANIUM_ALLOY_PROTECTIVE_MECHANICAL_BLOCK.get()))
                            .where('C', blocks(GTOBlocks.BIOACTIVE_MECHANICAL_CASING.get()))
                            .where('D', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .where('E', GTOPredicates.frame(GTMaterials.Naquadria))
                            .wherePart('F', blocks(GTOBlocks.BIOACTIVE_MECHANICAL_CASING.get())
                                    .or(abilities(INPUT_ENERGY))
                                    .or(abilities(MAINTENANCE).setExactLimit(1)))
                            .where('G', blocks(GTOBlocks.AMPROSIUM_ACTIVE_CASING.get()))
                            .where('H', controller(definition))
                            .where('I', blocks(GTBlocks.FUSION_GLASS.get()))
                            .where('J', blocks(GTOBlocks.AMPROSIUM_CASING.get()))
                            .where('K', blocks(GTOBlocks.BIOLOGICAL_MECHANICAL_CASING.get()))
                            .where('L', blocks(GTOBlocks.LOAD_BEARING_STRUCTURAL_STEEL_MECHANICAL_BLOCK.get()))
                            .where('M', GTOPredicates.frame(GTOMaterials.StructuralSteel45)))
                    .build())
            .workableCasingRenderer(GTOCore.id("block/casings/bioactive_mechanical_casing"), GTCEu.id("block/multiblock/fusion_reactor"))
            .register();

    public static final MultiblockMachineDefinition BIO_OSCILLATION_GENERATOR = multiblock("bio_oscillation_generator", "生物振荡发电机", BioOscillationGenerator::new)
            .nonYAxisRotation()
            .recipeTypes(DUMMY_RECIPES)
            .generator()
            .tooltipsSupplier(GTOMachineTooltipsA.BioOscillationGeneratorTooltips)
            .block(GTOBlocks.ANTIFREEZE_HEATPROOF_MACHINE_CASING)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("bio_oscillation_generator/main").port('#', GTOMachineProtocols.MODULE_PORT).build())
                    .symbols(Symbols.create()
                            .where('A', blocks(GTOBlocks.OXIDATION_RESISTANT_HASTELLOY_N_MECHANICAL_CASING.get()))
                            .where('B', blocks(GTOBlocks.NAQUADAH_ALLOY_CASING.get()))
                            .where('C', blocks(GTOBlocks.ANTIFREEZE_HEATPROOF_MACHINE_CASING.get()))
                            .where('D', GTOPredicates.frame(GTMaterials.Naquadria))
                            .where('E', GTOPredicates.frame(GTOMaterials.StructuralSteel45))
                            .where('F', blocks(GTOBlocks.PRESSURE_CONTAINMENT_CASING.get()))
                            .where('G', blocks(GTOBlocks.HIGH_PRESSURE_RESISTANT_CASING.get()))
                            .where('H', GTOPredicates.frame(GTOMaterials.HastelloyN))
                            .where('I', blocks(GTOBlocks.BIOLOGICAL_MECHANICAL_CASING.get()))
                            .where('J', blocks(GTOBlocks.PRESSURE_RESISTANT_HOUSING_MECHANICAL_BLOCK.get()))
                            .wherePart('K', blocks(GTOBlocks.ANTIFREEZE_HEATPROOF_MACHINE_CASING.get())
                                    .or(abilities(OUTPUT_ENERGY).or(abilities(OUTPUT_LASER)))
                                    .or(abilities(IMPORT_ITEMS))
                                    .or(abilities(IMPORT_FLUIDS))
                                    .or(abilities(EXPORT_FLUIDS))
                                    .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                    .or(blocks(GTOMachines.TISSUE_SENSOR.get()).setMaxGlobalLimited(1))
                                    .or(blocks(GTOMachines.CONNECTING_ROD_HATCH.get()).setMaxGlobalLimited(1)))
                            .where('L', blocks(GTOBlocks.NAQUADAH_REINFORCED_PLANT_CASING.get()))
                            .where('M', GTOPredicates.tierBlock(BlockMap.MACHINING_CASING, GTORecipeDataKeys.MACHINING_CONTROL_MODULE_TIER))
                            .where('N', blocks(GTBlocks.CLEANROOM_GLASS.get()))
                            .where('O', blocks(GTOBlocks.INDUSTRIAL_FRAMELESS_GLASS.get()))
                            .where('P', blocks(GTOBlocks.LOAD_BEARING_STRUCTURAL_STEEL_MECHANICAL_BLOCK.get()))
                            .where('Q', blocks(GTOBlocks.COBALT_OXIDE_CERAMIC_STRONG_THERMALLY_CONDUCTIVE_MECHANICAL_BLOCK.get()))
                            .where('R', blocks(ChemicalHelper.getBlock(TagPrefix.block, GTOMaterials.CarbonFiberPolyetheretherketoneComposite)))
                            .where('S', GTOPredicates.frame(GTMaterials.Tritanium))
                            .where('T', blocks(GTBlocks.FILTER_CASING_STERILE.get()))
                            .where('U', blocks(GTBlocks.HIGH_POWER_CASING.get()))
                            .where('V', blocks(GTOBlocks.HIGH_PRESSURE_PIPE_CASING.get()))
                            .where('W', blocks(GTOBlocks.COMPRESSOR_CONTROLLER_CASING.get()))
                            .where('X', blocks(GTOBlocks.MAGTECH_CASING.get()))
                            .where('Y', GTOPredicates.tierBlock(BlockMap.ENERGY_CASING, GTORecipeDataKeys.ENERGY_CONTROL_MODULE_TIER))
                            .where('Z', blocks(GTBlocks.HERMETIC_CASING_UHV.get()))
                            .where('[', blocks(GTOBlocks.STAINLESS_STEEL_CORROSION_RESISTANT_CASING.get()))
                            .where('\\', blocks(ChemicalHelper.getBlock(TagPrefix.block, GTOMaterials.NanocrackRegulatedSelfHumidifyingCompositeMaterial)))
                            .where(']', blocks(GTOBlocks.COOLANT_PIPE_CASING.get()))
                            .where('^', blocks(GTOBlocks.AMPROSIUM_PIPE_CASING.get()))
                            .where('_', blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
                            .where('`', blocks(GTOBlocks.AMPROSIUM_GEARBOX.get()))
                            .where('a', controller(definition))
                            .where('b', blocks(GTOBlocks.EXTREME_STRENGTH_TRITANIUM_CASING.get()))
                            .where('c', blocks(ChemicalHelper.getBlock(TagPrefix.block, GTOMaterials.EnergeticNetherite)))
                            .where('d', GTOPredicates.frame(GTOMaterials.Amprosium))
                            .where('e', blocks(GTOBlocks.SUPERCRITICAL_TURBINE_CASING.get()))
                            .where('f', blocks(GTBlocks.FUSION_GLASS.get())))
                    .atPort(GTOMachineProtocols.MODULE_PORT, Slot.machines(GTOMachineProtocols.BIO_STIMULATOR))
                    .build())
            .workableCasingRenderer(GTOCore.id("block/casings/antifreeze_heatproof_machine_casing"), GTCEu.id("block/multiblock/fusion_reactor"))
            .register();
}
