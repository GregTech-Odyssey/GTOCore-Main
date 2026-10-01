package com.gtocore.common.data.machines;

import com.gtocore.api.pattern.GTOPredicates;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.common.machine.multiblock.electric.ChiselMachine;
import com.gtocore.common.machine.multiblock.part.ae.PatternBufferType;
import com.gtocore.config.GTORules;
import com.gtocore.integration.Mods;

import com.gtolib.api.recipe.GTORecipeModifiers;
import com.gtolib.utils.MultiBlockFileReader;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Structure;
import com.gregtechceu.gtceu.api.machine.multiblockpro.Symbols;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.level.block.Blocks;

import static com.gregtechceu.gtceu.api.GTValues.MV;
import static com.gregtechceu.gtceu.api.machine.multiblock.PartAbility.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES;
import static com.gtocore.utils.register.MachineRegisterUtils.multiblock;
import static com.gtolib.api.registries.GTORegistration.GTO;

public final class OptionalMachine {

    public static void init() {}

    public static final MachineDefinition ME_SIMPLE_PATTERN_BUFFER = GTCEu.isDev() || GTORules.SIMPLE_PATTERN_BUFFER_SLOTS.get() > 0 ?

            PatternBufferType.builder(GTO, "me_simple_pattern_buffer")
                    .name("ME简单样板总成", "ME Simple Pattern Buffer")
                    .slots(GTORules.SIMPLE_PATTERN_BUFFER_SLOTS::get)
                    .tier(MV)
                    .noWireless()
                    .upgradable()
                    .register() :
            null;

    public static final MultiblockMachineDefinition CARVING_CENTER = GTCEu.isDev() || Mods.CHISEL.isLoaded() ? multiblock("carving_center", "雕刻中心", ChiselMachine::new)
            .allRotation()
            .tooltips(GTOMachineTooltips.CarvingCenterTooltips)
            .recipeTypes(DUMMY_RECIPES)
            .recipeModifier(GTORecipeModifiers.PARALLEL)
            .block(GTBlocks.CASING_STEEL_SOLID)
            .structure(definition -> Structure
                    .root(MultiBlockFileReader.piece("carving_center").build())
                    .symbols(Symbols.create()
                            .wherePart('A', blocks(GTBlocks.CASING_STEEL_SOLID.get())
                                    .or(abilities(IMPORT_ITEMS).setMaxGlobalLimited(4))
                                    .or(abilities(EXPORT_ITEMS).setMaxGlobalLimited(1))
                                    .or(abilities(INPUT_ENERGY).setMaxGlobalLimited(2))
                                    .or(abilities(MAINTENANCE).setExactLimit(1)))
                            .where('B', GTOPredicates.frame(GTMaterials.Steel))
                            .where('C', blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                            .where('D', blocks(GTBlocks.STEEL_HULL.get()))
                            .where('E', blocks(GTBlocks.CASING_STEEL_GEARBOX.get()))
                            .where('F', blocks(Blocks.IRON_BARS))
                            .where('G', controller(definition))
                            .where('H', blocks(GTBlocks.CASING_TEMPERED_GLASS.get())))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_solid_steel"), GTCEu.id("block/multiblock/fusion_reactor"))
            .register() : null;
}
