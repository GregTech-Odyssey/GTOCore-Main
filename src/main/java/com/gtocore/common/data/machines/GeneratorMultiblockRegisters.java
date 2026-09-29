package com.gtocore.common.data.machines;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.api.pattern.StructureModuleKeys;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.common.machine.multiblock.generator.CombustionEngineMachine;
import com.gtocore.common.machine.multiblock.generator.TurbineMachine;
import com.gtocore.config.GTORules;

import com.gtolib.GTOCore;
import com.gtolib.api.registries.GTORegistration;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Piece;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Slot;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gtocore.utils.register.MachineRegisterUtils.multiblock;
import static com.gtolib.utils.register.BlockRegisterUtils.addLang;

public final class GeneratorMultiblockRegisters {

    private static final MultiblockMachineDefinition DUMMY_MULTIBLOCK = MultiblockMachineDefinition.createDefinition(GTOCore.id("dummy"));

    private GeneratorMultiblockRegisters() {}

    public static MultiblockMachineDefinition registerLargeCombustionEngine(GTORegistration registrate, String name, String cn,
                                                                            int tier, GTRecipeType recipeType,
                                                                            Supplier<? extends Block> casing,
                                                                            Supplier<? extends Block> gear,
                                                                            Supplier<? extends Block> intake,
                                                                            ResourceLocation casingTexture,
                                                                            ResourceLocation overlayModel, boolean isGTM) {
        if (!isGTM) addLang(name, cn);
        boolean titanium = tier == EV;
        Supplier<? extends Block> extTurbine = titanium ? GTBlocks.CASING_TITANIUM_TURBINE : GTBlocks.CASING_TUNGSTENSTEEL_TURBINE;
        Supplier<? extends Block> extCasing = titanium ? GTBlocks.CASING_TITANIUM_STABLE : GTBlocks.CASING_TUNGSTENSTEEL_ROBUST;
        Supplier<? extends Block> extFirebox = titanium ? GTBlocks.FIREBOX_TITANIUM : GTBlocks.FIREBOX_TUNGSTENSTEEL;
        Supplier<? extends Block> extIntake = titanium ? GTBlocks.CASING_ENGINE_INTAKE : GTBlocks.CASING_EXTREME_ENGINE_INTAKE;
        Material extFrame = titanium ? recipeType == GTORecipeTypes.SEMI_FLUID_GENERATOR_FUELS ? GTMaterials.StainlessSteel : GTMaterials.BlueSteel : GTMaterials.BlackSteel;
        return registrate.multiblock(name, holder -> new CombustionEngineMachine(holder, tier))
                .nonYAxisRotation()
                .recipeTypes(recipeType)
                .tooltips(GTOMachineTooltips.LargeCombustionTooltips
                        .invoke(V[tier] << 1, V[tier] * 6, tier > EV, V[tier] << 3))
                .moduleTooltips(new PartAbility[0])
                .generator()
                .block(casing)
                .structure(definition -> Structure
                        .root(Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                                .aisle("XXX", "XDX", "XXX")
                                .aisle("XCX", "CGC", "XCX")
                                .aisle("XCX", "CGC", "XCX")
                                .aisle("AAA", "AYA", "AAA")
                                .port('Y', StructureModuleKeys.EXT_OUT, RelativeDirection.BACK)
                                .build())
                        .symbols(Symbols.create()
                                .where('X', blocks(casing.get()))
                                .where('G', blocks(gear.get()))
                                .wherePart('C', blocks(casing.get()).setMinGlobalLimited(3)
                                        .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                        .or(autoAbilities(true, true, false)))
                                .where('D', ability(PartAbility.OUTPUT_ENERGY,
                                        tier == EV ? Stream.of(HV, EV, IV, LuV, ZPM, UV, UHV).mapToInt(Integer::intValue).toArray() : Stream.of(EV, IV, LuV, ZPM, UV, UHV).filter(t -> t >= tier).mapToInt(Integer::intValue).toArray())
                                        .addTooltips(Component.translatable("gtceu.machine.large_combustion_engine.tooltip.boost_regular", V[tier] * 6)))
                                .where('A', blocks(intake.get()).addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_1")))
                                .where('Y', controller(definition))
                                .where('a', blocks(extTurbine.get()))
                                .where('b', GTOPredicates.frame(extFrame))
                                .where('c', blocks(extCasing.get()))
                                .where('d', blocks(extFirebox.get()))
                                .where('e', abilities(MUFFLER))
                                .where('f', blocks(extCasing.get())
                                        .or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3)))
                                .where('g', blocks(extIntake.get()))
                                .where('@', any()))
                        .atPort(StructureModuleKeys.EXT_OUT, Slot.optional(Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                                .aisle("addda", "bf fb", "bfffb", "bbbbb")
                                .aisle("addda", "ac ca", "accca", " aaa ")
                                .aisle("addda", " c c ", " ccc ", " geg ")
                                .aisle("a   a", "a   a", "a   a", " aaa ")
                                .aisle("a   a", "     ", "     ", " geg ")
                                .aisle("a   a", "a @ a", "a   a", " aaa ")
                                .aisle("a   a", "     ", "     ", "     ")
                                .port('@', StructureModuleKeys.EXT_IN, RelativeDirection.FRONT)
                                .build(), StructureModuleKeys.EXT_IN).count(CombustionEngineMachine.EXTENSION))
                        .build())
                .workableCasingRenderer(casingTexture, overlayModel)
                .register();
    }

    private record TurbineExtension(Supplier<? extends Block> shell, Supplier<? extends Block> core, Supplier<? extends Block> turbine, Material frame) {

        static TurbineExtension of(GTRecipeType recipeType) {
            if (recipeType == GTORecipeTypes.STEAM_TURBINE_FUELS) {
                return new TurbineExtension(GTBlocks.CASING_STEEL_SOLID, GTBlocks.CASING_STEEL_PIPE, GTBlocks.CASING_STEEL_TURBINE, GTMaterials.StainlessSteel);
            } else if (recipeType == GTORecipeTypes.GAS_TURBINE_FUELS) {
                return new TurbineExtension(GTBlocks.CASING_STAINLESS_CLEAN, GTBlocks.CASING_ENGINE_INTAKE, GTBlocks.CASING_STAINLESS_TURBINE, GTMaterials.BlackSteel);
            } else if (recipeType == GTORecipeTypes.ROCKET_ENGINE_FUELS) {
                return new TurbineExtension(GTBlocks.CASING_TITANIUM_STABLE, GTBlocks.CASING_ENGINE_INTAKE, GTBlocks.CASING_TITANIUM_TURBINE, GTMaterials.BlueSteel);
            } else if (recipeType == GTORecipeTypes.SUPERCRITICAL_STEAM_TURBINE_FUELS) {
                return new TurbineExtension(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING, GCYMBlocks.ELECTROLYTIC_CELL, GTOBlocks.SUPERCRITICAL_TURBINE_CASING, GTMaterials.TungstenSteel);
            }
            throw new IllegalArgumentException("no large turbine extension for " + recipeType);
        }
    }

    public static MultiblockMachineDefinition registerLargeTurbine(GTORegistration registrate, String name, String cn, int tier, boolean special, GTRecipeType recipeType, Supplier<? extends Block> casing, Supplier<? extends Block> gear, ResourceLocation casingTexture, ResourceLocation overlayModel, boolean isGTM) {
        if (Objects.equals(name, "plasma_large_turbine")) {
            DUMMY_MULTIBLOCK.setItemSupplier(MultiBlockA.VOID_MINER::asItem);
            return DUMMY_MULTIBLOCK;
        }
        if (!isGTM) addLang(name, cn);
        var extension = TurbineExtension.of(recipeType);
        return registrate.multiblock(name, holder -> new TurbineMachine(holder, tier, special, false))
                .ruleTooltips(GTORules.MEGA_TURBINE_OUTPUT, GTORules.MEGA_TURBINE_ROTOR_DAMAGE, GTORules.MEGA_TURBINE_FAULT)
                .tooltips(GTOMachineTooltips.LargeTurbineTooltips.invoke((long) (V[tier] * (special ? 2.5 : 2)), tier))
                .tooltips(GTOMachineTooltips.TurbineHighSpeedTooltips)
                .moduleTooltips(new PartAbility[0])
                .nonYAxisRotation()
                .recipeTypes(recipeType)
                .generator()
                .block(casing)
                .structure(definition -> Structure
                        .root(Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                                .aisle("CCCC", "CHHC", "CCCC")
                                .aisle("CHHC", "RGGR", "CHHC")
                                .aisle("CCCC", "CSHC", "CCCC")
                                .port('S', StructureModuleKeys.EXT_OUT, RelativeDirection.BACK)
                                .build())
                        .symbols(Symbols.create()
                                .where('S', controller(definition))
                                .where('G', blocks(gear.get()))
                                .where('C', blocks(casing.get()))
                                .where('R', GTOPredicates.RotorBlockFacingOutwards(tier).setExactLimit(1)
                                        .or(abilities(PartAbility.OUTPUT_ENERGY)).setExactLimit(1))
                                .wherePart('H', blocks(casing.get()).or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true).or(autoAbilities(true, true, false))))
                                .where('a', blocks(extension.shell().get()))
                                .where('b', blocks(extension.core().get()))
                                .where('c', blocks(extension.turbine().get()))
                                .where('d', blocks(extension.turbine().get())
                                        .or(abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3)))
                                .where('f', GTOPredicates.frame(extension.frame()))
                                .where('@', any()))
                        .atPort(StructureModuleKeys.EXT_OUT, Slot.optional(Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                                .aisle("aaaaaaa", "a   aba", "a   aba", "aaaaaaa")
                                .aisle("    ccd", "    ccd", "    ccd", "a   aba")
                                .aisle("    ccd", " @  fff", "    ccd", "a   aba")
                                .aisle("    ccd", "    ccd", "    ccd", "a   aba")
                                .aisle("aaaaaaa", "a   aba", "a   aba", "aaaaaaa")
                                .port('@', StructureModuleKeys.EXT_IN, RelativeDirection.FRONT)
                                .build(), StructureModuleKeys.EXT_IN).count(TurbineMachine.EXTENSION))
                        .build())
                .workableCasingRenderer(casingTexture, overlayModel)
                .register();
    }

    public static MultiblockMachineDefinition registerMegaTurbine(String name, String cn, int tier, boolean special, GTRecipeType recipeType,
                                                                  Supplier<Block> casing, Supplier<Block> gear, ResourceLocation baseCasing,
                                                                  ResourceLocation overlayModel, UnaryOperator<Symbols> extensionSymbols) {
        return multiblock(name, cn, holder -> new TurbineMachine.MegaTurbine(holder, tier, special))
                .nonYAxisRotation()
                .recipeTypes(recipeType)
                .generator()
                .tooltips(GTOMachineTooltips.MegaTurbineGenerateTooltips
                        .invoke(V[tier] * (special ? 12 : 8), tier))
                .tooltips(GTOMachineTooltips.TurbineHighSpeedTooltips)
                .moduleTooltips(new PartAbility[0])
                .ruleTooltips(GTORules.MEGA_TURBINE_OUTPUT, GTORules.MEGA_TURBINE_ROTOR_DAMAGE, GTORules.MEGA_TURBINE_FAULT)
                .block(casing)
                .structure(definition -> Structure
                        .root(Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                                .aisle("   AAAAA   ", "  A  A  A  ", " AA  A  AA ", "A  A A A  A", "A   A A   A", "AAAA A AAAA", "A   A A   A", "A  A A A  A", " AA  A  AA ", "  A  A  A  ", "   AAAAA   ")
                                .aisle("   ABABA   ", "  BBBBBBB  ", " BBBBBBBBB ", "ABBBBBBBBBA", "BBBBBBBBBBB", "ABBBBBBBBBA", "BBBBBBBBBBB", "ABBBBBBBBBA", " BBBBBBBBB ", "  BBBBBBB  ", "   ABABA   ")
                                .aisle("   BBBBB   ", "  BBEEEBB  ", " B   E   B ", "BB   E   BB", "BE   E   EB", "BEEEEJEEEEB", "BE   E   EB", "BB   E   BB", " B   E   B ", "  BBEEEBB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BB   BB  ", " B       B ", "BB       BB", "B         B", "B         B", "B         B", "BB       BB", " B       B ", "  BB   BB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BBEEEBB  ", " B   E   B ", "BB   E   BB", "BE   E   EB", "BEEEEIEEEEB", "BE   E   EB", "BB   E   BB", " B   E   B ", "  BBEEEBB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BBEEEBB  ", " B   E   B ", "BB   E   BB", "BE   E   EB", "BEEEEJEEEEB", "BE   E   EB", "BB   E   BB", " B   E   B ", "  BBEEEBB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BB   BB  ", " B       B ", "BB       BB", "B         B", "B         B", "B         B", "BB       BB", " B       B ", "  BB   BB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BBEEEBB  ", " B   E   B ", "BB   E   BB", "BE   E   EB", "BEEEEIEEEEB", "BE   E   EB", "BB   E   BB", " B   E   B ", "  BBEEEBB  ", "   BBBBB   ")
                                .aisle("   ABABA   ", "  BBBBBBB  ", " BBBBBBBBB ", "ABBBBBBBBBA", "BBBBBBBBBBB", "ABBBBEBBBBA", "BBBBBBBBBBB", "ABBBBBBBBBA", " BBBBBBBBB ", "  BBBBBBB  ", "   ABABA   ")
                                .aisle("   AAAAA   ", "  A  A  A  ", " AA  A  AA ", "A  A A A  A", "A   AAA   A", "AAAAAEAAAAA", "A   AAA   A", "A  A A A  A", " AA  A  AA ", "  A  A  A  ", "   AAAAA   ")
                                .aisle("           ", "           ", "           ", "           ", "    AAA    ", "    AEA    ", "    AAA    ", "           ", "           ", "           ", "           ")
                                .aisle("           ", "           ", "           ", "           ", "    AAA    ", "    AEA    ", "    AAA    ", "           ", "           ", "           ", "           ")
                                .aisle("   AAAAA   ", "  A  A  A  ", " AA  A  AA ", "A  A A A  A", "A   A A   A", "AAAA A AAAA", "A   A A   A", "A  A A A  A", " AA  A  AA ", "  A  A  A  ", "   AAAAA   ")
                                .aisle("   ABABA   ", "  BBBBBBB  ", " BBBBBBBBB ", "ABBBBBBBBBA", "BBBBBBBBBBB", "ABBBBBBBBBA", "BBBBBBBBBBB", "ABBBBBBBBBA", " BBBBBBBBB ", "  BBBBBBB  ", "   ABABA   ")
                                .aisle("   BBBBB   ", "  BBEEEBB  ", " B   E   B ", "BB   E   BB", "BE   E   EB", "BEEEEJEEEEB", "BE   E   EB", "BB   E   BB", " B   E   B ", "  BBEEEBB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BB   BB  ", " B       B ", "BB       BB", "B         B", "B         B", "B         B", "BB       BB", " B       B ", "  BB   BB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BBEEEBB  ", " B   E   B ", "BB   E   BB", "BE   E   EB", "BEEEEIEEEEB", "BE   E   EB", "BB   E   BB", " B   E   B ", "  BBEEEBB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BBEEEBB  ", " B   E   B ", "BB   E   BB", "BE   E   EB", "BEEEEJEEEEB", "BE   E   EB", "BB   E   BB", " B   E   B ", "  BBEEEBB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BB   BB  ", " B       B ", "BB       BB", "B         B", "B         B", "B         B", "BB       BB", " B       B ", "  BB   BB  ", "   BBBBB   ")
                                .aisle("   BBBBB   ", "  BBEEEBB  ", " B   E   B ", "BB   E   BB", "BE   E   EB", "BEEEEIEEEEB", "BE   E   EB", "BB   E   BB", " B   E   B ", "  BBEEEBB  ", "   BBBBB   ")
                                .aisle("   ABABA   ", "  BBBBBBB  ", " BBBBBBBBB ", "ABBBBBBBBBA", "BBBBBBBBBBB", "ABBBBEBBBBA", "BBBBBBBBBBB", "ABBBBBBBBBA", " BBBBBBBBB ", "  BBBBBBB  ", "   ABABA   ")
                                .aisle("   AAAAA   ", "  A  A  A  ", " AA  A  AA ", "A  A A A  A", "A   AAA   A", "AAAAAEAAAAA", "A   AAA   A", "A  A A A  A", " AA  A  AA ", "  A  A  A  ", "   AAAAA   ")
                                .aisle("           ", "           ", "           ", "           ", "    AAA    ", "    AEA    ", "    AAA    ", "           ", "           ", "           ", "           ")
                                .aisle("           ", "           ", "           ", "           ", "    AAA    ", "    AEA    ", "    AAA    ", "           ", "           ", "           ", "           ")
                                .aisle("           ", "           ", "    AAA    ", "   A A A   ", "  A AAA A  ", "  AAAEAAA  ", "  A AAA A  ", "   A A A   ", "    AAA    ", "           ", "           ")
                                .aisle("           ", "           ", "    ABA    ", "   BBBBB   ", "  ABBBBBA  ", "  BBBEBBB  ", "  ABBBBBA  ", "   BBBBB   ", "    ABA    ", "           ", "           ")
                                .aisle("           ", "           ", "    BBB    ", "   B   B   ", "  B     B  ", "  B  E  B  ", "  B     B  ", "   B   B   ", "    BBB    ", "           ", "           ")
                                .aisle("           ", "           ", "    BBB    ", "   BGGGB   ", "  BGAEAGB  ", "  HGEJEGH  ", "  BGAEAGB  ", "   BGGGB   ", "    BHB    ", "           ", "           ")
                                .aisle("           ", "           ", "    BBB    ", "   B   B   ", "  H     H  ", "  H     H  ", "  H     H  ", "   B   B   ", "    HHH    ", "           ", "           ")
                                .aisle("           ", "           ", "    BBB    ", "   BGGGB   ", "  HGAEAGH  ", "  HGEIEGH  ", "  HGAEAGH  ", "   BGGGB   ", "    HHH    ", "           ", "           ")
                                .aisle("           ", "           ", "    BBB    ", "   BGGGB   ", "  HGAEAGH  ", "  HGEJEGH  ", "  HGAEAGH  ", "   BGGGB   ", "    HHH    ", "           ", "           ")
                                .aisle("           ", "           ", "    BBB    ", "   B   B   ", "  H     H  ", "  H     H  ", "  H     H  ", "   B   B   ", "    HHH    ", "           ", "           ")
                                .aisle("           ", "           ", "    BBB    ", "   BGGGB   ", "  BGAEAGB  ", "  HGEIEGH  ", "  BGAEAGB  ", "   BGGGB   ", "    BHB    ", "           ", "           ")
                                .aisle("           ", "           ", "    BBB    ", "   B   B   ", "  B     B  ", "  B  E  B  ", "  B     B  ", "   B   B   ", "    BBB    ", "           ", "           ")
                                .aisle("           ", "           ", "    ABA    ", "   BBBBB   ", "  ABBBBBA  ", "  BBBEBBB  ", "  ABBBBBA  ", "   BBBBB   ", "    ABA    ", "           ", "           ")
                                .aisle("           ", "           ", "    AAA    ", "   A A A   ", "  A AAA A  ", "  AAAEAAA  ", "  A AAA A  ", "   A A A   ", "    AAA    ", "           ", "           ")
                                .aisle("           ", "           ", "           ", "           ", "     A     ", "    AEA    ", "     A     ", "           ", "           ", "           ", "           ")
                                .aisle("           ", "           ", "           ", "           ", "     A     ", "    AEA    ", "     A     ", "           ", "           ", "           ", "           ")
                                .aisle("           ", "           ", "   AAAAA   ", "   ABBBA   ", "   BBBBB   ", "   BBEBB   ", "   BBBBB   ", "   ABBBA   ", "   AAAAA   ", "           ", "           ")
                                .aisle("           ", "           ", "   ABBBA   ", "   B   B   ", "   C   C   ", "   C E C   ", "   C   C   ", "   B   B   ", "   ABBBA   ", "           ", "           ")
                                .aisle("           ", "           ", "   ABBBA   ", "   B   B   ", "   C   C   ", "   C E C   ", "   C   C   ", "   B   B   ", "   ABFBA   ", "           ", "           ")
                                .aisle("           ", "           ", "   ABBBA   ", "   B   B   ", "   C   C   ", "   C E C   ", "   C   C   ", "   B   B   ", "   ABBBA   ", "           ", "           ")
                                .aisle("           ", "           ", "   AAAAA   ", "   ABBBA   ", "   BCCCB   ", "   BCDCB   ", "   BCCCB   ", "   ABBBA   ", "   AAAAA   ", "           ", "           ")
                                .port('D', StructureModuleKeys.EXT_OUT, RelativeDirection.BACK)
                                .build())
                        .symbols(extensionSymbols.apply(Symbols.create()
                                .where('A', blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                                .where('B', blocks(casing.get()))
                                .wherePart('C', blocks(casing.get())
                                        .or(abilities(MAINTENANCE).setExactLimit(1))
                                        .or(abilities(IMPORT_FLUIDS).setMaxGlobalLimited(8))
                                        .or(abilities(EXPORT_FLUIDS).setMaxGlobalLimited(2))
                                        .or(abilities(OUTPUT_ENERGY).setMaxGlobalLimited(4))
                                        .or(blocks(ExResearchMachines.ENERGY_DATA_HOLDER.get()).setMaxGlobalLimited(1))
                                        .or(blocks(GTOMachines.ROTOR_HATCH.get()).setMaxGlobalLimited(1)))
                                .where('D', controller(definition))
                                .where('E', blocks(gear.get()))
                                .where('F', abilities(MUFFLER))
                                .where('G', heatingCoils())
                                .where('H', GTOPredicates.glass())
                                .where('I', GTOPredicates.RotorBlock(tier, RelativeDirection.BACK))
                                .where('J', GTOPredicates.RotorBlock(tier, RelativeDirection.FRONT))
                                .where('@', any())))
                        .atPort(StructureModuleKeys.EXT_OUT, Slot.optional(Piece.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                                .aisle("ccccccccccccc", "c           c", "ccccccccccccc", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                                .aisle("bbbbbbbbbbbbb", "bhhhhhhhhhhhb", "bbbbbbbbbbbbb", "bgggggggggggb", "bgggggggggggb", "bbbbbbbbbbbbb", "             ", "             ", "             ", "             ", "             ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "             ", "             ", "             ", "             ", "             ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "f           f", " f         f ", "  f       f  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "a           a", " a         a ", "  a       a  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "a           a", " a         a ", "  a       a  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "             ", "             ", "             ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "             ", "             ", "             ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "a           a", " a         a ", "  a       a  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "a           a", " a         a ", "  a       a  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "f           f", " f         f ", "  f       f  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "bbb       bbb", "bb         bb", "b           b", "b           b", "b           b", "             ", "             ", "             ", "             ", "             ")
                                .aisle("   ggggggg   ", "   ggggggg   ", "  g  jjj  g  ", "  g jjjjj g  ", "  gjj   jjg  ", "  gjj   jjg  ", "  gjj   jjg  ", "  g jjjjj g  ", "  g  jjj  g  ", "   ggggggg   ", "             ")
                                .aisle("   ggggggg   ", "   ggggggg   ", "  g  jjj  g  ", "  g jjjjj g  ", "  gjj   jjg  ", "  gjj   jjg  ", "  gjj   jjg  ", "  g jjjjj g  ", "  g  jjj  g  ", "   ggggggg   ", "             ")
                                .aisle("bbbb     bbbb", "bbb       bbb", "bb         bb", "b           b", "b           b", "b           b", "             ", "             ", "             ", "             ", "             ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "f           f", " f         f ", "  f       f  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "a           a", " a         a ", "  a       a  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "a           a", " a         a ", "  a       a  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "             ", "             ", "             ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "             ", "             ", "             ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "a           a", " a         a ", "  a       a  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "a           a", " a         a ", "  a       a  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "ebb       bbe", "bb         bb", "e           e", "b           b", "b           b", "a           a", "f           f", " f         f ", "  f       f  ", "   a     a   ")
                                .aisle("bbbb     bbbb", "bbb       bbb", "bb         bb", "b           b", "b           b", "b           b", "             ", "             ", "             ", "             ", "             ")
                                .aisle("     ggg     ", "     ggg     ", "     ggg     ", "    gjjjg    ", "   gj   jg   ", "   gj   jg   ", "   gj   jg   ", "    gjjjg    ", "     ggg     ", "             ", "             ")
                                .aisle(" ggggggggggg ", " g   ggg   g ", " g   ggg   g ", " g  gjjjg  g ", "   gj   jg   ", "   gj   jg   ", "   gj   jg   ", "    gjjjg    ", "     ggg     ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "abbbbbbbbbbba", "abbbb   bbbba", " gbb     bbg ", "  b       b  ", "             ", "             ", "             ", "             ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "             ", "             ", "             ", "             ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "  b       b  ", "  b       b  ", "   b     b   ", "    b   b    ", "     bbb     ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "  b       b  ", "  b       b  ", "  bb     bb  ", "    b   b    ", "     bbb     ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "  i       i  ", "  i       i  ", "  bb     bb  ", "   bb   bb   ", "    bbbbb    ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "  i       i  ", "  i       i  ", "  bb     bb  ", "    b   b    ", "     bbb     ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "  i       i  ", "  i       i  ", "  bb     bb  ", "    b   b    ", "     bbb     ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "  i       i  ", "  i       i  ", "  bb     bb  ", "   bb   bb   ", "    bbbbb    ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "  b       b  ", "  b       b  ", "  bb     bb  ", "    b   b    ", "     bbb     ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "  b       b  ", "  b       b  ", "   b     b   ", "    b   b    ", "     bbb     ", "             ")
                                .aisle("abbbbbbbbbbba", "db         bd", "ab         ba", " gbb     bbg ", "  b       b  ", "             ", "             ", "             ", "             ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "abbbbbbbbbbba", "abbbb   bbbba", " gbb     bbg ", "  b       b  ", "             ", "             ", "             ", "             ", "             ", "             ")
                                .aisle(" ggggggggggg ", " g   ggg   g ", " g   ggg   g ", " g   jjj   g ", "    j   j    ", "    j   j    ", "    j   j    ", "     jjj     ", "             ", "             ", "             ")
                                .aisle(" ggggggggggg ", " g   ggg   g ", " g   ggg   g ", " g   jjj   g ", "    j   j    ", "    j   j    ", "    j   j    ", "     jjj     ", "             ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "abbbbbbbbbbba", "abbb     bbba", " gbb     bbg ", "  bb     bb  ", "  bb     bb  ", "   b     b   ", "   b     b   ", "             ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "dbbbbbbbbbbbd", "abbb     bbba", " gbb     bbg ", "  bb     bb  ", "  bb     bb  ", "   b     b   ", "   b     b   ", "             ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "dbbbbbbbbbbbd", "abbb     bbba", " gbb     bbg ", "  bb     bb  ", "  bb     bb  ", "   b     b   ", "   b     b   ", "             ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "dbbbbbbbbbbbd", "abbb     bbba", " gbb     bbg ", "  bb     bb  ", "  bb  @  bb  ", "   b     b   ", "   b     b   ", "             ", "             ", "             ")
                                .aisle("abbbbbbbbbbba", "abbbbbbbbbbba", "abbb     bbba", " gbb     bbg ", "  bb     bb  ", "  bb     bb  ", "   b     b   ", "   b     b   ", "             ", "             ", "             ")
                                .aisle(" ggggggggggg ", " gaaaaaaaaag ", " gaaaaaaaaag ", " gbbbb bbbbg ", "  bb     bb  ", "   b     b   ", "             ", "             ", "             ", "             ", "             ")
                                .port('@', StructureModuleKeys.EXT_IN, RelativeDirection.FRONT)
                                .build(), StructureModuleKeys.EXT_IN).count(TurbineMachine.EXTENSION))
                        .build())
                .workableCasingRenderer(baseCasing, overlayModel)
                .register();
    }
}
