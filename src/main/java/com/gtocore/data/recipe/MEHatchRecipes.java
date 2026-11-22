package com.gtocore.data.recipe;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOAEParts;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.common.data.machines.OptionalMachine;
import com.gtocore.config.GTORules;
import com.gtocore.integration.ae.TieredInterfaces;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.world.item.Item;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;

import com.almostreliable.merequester.Registration;
import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.hepdd.gtmthings.data.CustomMachines;
import lombok.experimental.UtilityClass;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTItems.SENSOR_LuV;
import static com.gregtechceu.gtceu.common.data.GTMachines.FLUID_EXPORT_HATCH;
import static com.gregtechceu.gtceu.common.data.GTMachines.FLUID_IMPORT_HATCH;
import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_EXPORT_BUS;
import static com.gregtechceu.gtceu.common.data.GTMachines.ITEM_IMPORT_BUS;
import static com.gtocore.common.data.GTORecipeTypes.*;
import static com.gtocore.common.data.machines.GTAEMachines.MUFFLER_HATCH_ME;
import static com.gtocore.data.techtree.AENodes.BaseMEMachines;
import static com.gtocore.data.techtree.AENodes.MEAdvancedAssembly;
import static com.gtocore.data.techtree.AENodes.MECapacityExpansion;
import static com.gtocore.data.techtree.AENodes.MECatalystSupplying;
import static com.gtocore.data.techtree.AENodes.MESmartGatingClustering;
import static com.gtocore.data.techtree.AENodes.MEWasteRecycling;
import static com.gtocore.data.techtree.BaseNodes.ScannerInnovation;

@UtilityClass
public final class MEHatchRecipes {

    Item meInterface = AEParts.INTERFACE.asItem();
    Item accelerationCard = AEItems.SPEED_CARD.asItem();
    int basicMEHullTier = GTORules.RECIPE_TIER.isExpert() ? EV : MV;
    int advancedMEHullTier = GTORules.RECIPE_TIER.isExpert() ? LuV : HV;

    public void registerInputOutputAndMuffler() {
        ASSEMBLER_RECIPES.recipeBuilder("me_import_bus")
                .inputItems(ITEM_IMPORT_BUS[basicMEHullTier])
                .inputItems(meInterface)
                .inputItems(accelerationCard)
                .inputItems(GTCraftingComponents.FIELD_GENERATOR.get(basicMEHullTier))
                .outputItems(GTAEMachines.ITEM_IMPORT_BUS_ME)
                .duration(600).EUt(VA[basicMEHullTier])
                .save();

        ASSEMBLER_RECIPES.recipeBuilder("me_import_hatch")
                .inputItems(FLUID_IMPORT_HATCH[basicMEHullTier])
                .inputItems(meInterface)
                .inputItems(accelerationCard)
                .inputItems(GTCraftingComponents.FIELD_GENERATOR.get(basicMEHullTier))
                .outputItems(GTAEMachines.FLUID_IMPORT_HATCH_ME)
                .duration(600).EUt(VA[basicMEHullTier])
                .save();

        ASSEMBLER_RECIPES.recipeBuilder("me_export_bus")
                .inputItems(ITEM_EXPORT_BUS[basicMEHullTier])
                .inputItems(meInterface)
                .inputItems(accelerationCard)
                .inputItems(GTCraftingComponents.FIELD_GENERATOR.get(basicMEHullTier))
                .outputItems(GTAEMachines.ITEM_EXPORT_BUS_ME)
                .duration(600).EUt(VA[basicMEHullTier])
                .save();

        ASSEMBLER_RECIPES.recipeBuilder("me_export_hatch")
                .inputItems(FLUID_EXPORT_HATCH[basicMEHullTier])
                .inputItems(meInterface)
                .inputItems(accelerationCard)
                .inputItems(GTCraftingComponents.FIELD_GENERATOR.get(basicMEHullTier))
                .outputItems(GTAEMachines.FLUID_EXPORT_HATCH_ME)
                .duration(600).EUt(VA[basicMEHullTier])
                .save();

        ASSEMBLER_RECIPES.recipeBuilder("me_stocking_import_bus")
                .inputItems(ITEM_IMPORT_BUS[advancedMEHullTier])
                .inputItems(GTAEMachines.ITEM_IMPORT_BUS_ME)
                .inputItems(meInterface, 4)
                .inputItems(GTCraftingComponents.CONVEYOR.get(advancedMEHullTier))
                .inputItems(GTCraftingComponents.SENSOR.get(advancedMEHullTier))
                .inputItems(accelerationCard, 4)
                .outputItems(GTAEMachines.STOCKING_IMPORT_BUS_ME)
                .duration(300).EUt(VA[advancedMEHullTier])
                .save();

        ASSEMBLER_RECIPES.recipeBuilder("me_stocking_import_hatch")
                .inputItems(FLUID_IMPORT_HATCH[advancedMEHullTier])
                .inputItems(GTAEMachines.FLUID_IMPORT_HATCH_ME)
                .inputItems(meInterface, 4)
                .inputItems(GTCraftingComponents.PUMP.get(advancedMEHullTier))
                .inputItems(GTCraftingComponents.SENSOR.get(advancedMEHullTier))
                .inputItems(accelerationCard, 4)
                .outputItems(GTAEMachines.STOCKING_IMPORT_HATCH_ME)
                .duration(300).EUt(VA[advancedMEHullTier])
                .save();

        ASSEMBLY_LINE_RECIPES.builder("me_muffler_hatch")
                .inputItems(GTMachines.MUFFLER_HATCH[GTValues.LuV].asItem())
                .inputItems(GTOBlocks.INTEGRAL_FRAMEWORK_LUV.asItem())
                .inputItems(GTAEMachines.ITEM_EXPORT_BUS_ME.asItem())
                .inputItems(GTItems.FIELD_GENERATOR_LuV, 16)
                .inputItems(GTItems.ROBOT_ARM_LuV, 16)
                .inputItems(AEParts.ANNIHILATION_PLANE.asItem(), 8)
                .inputItems(GTOItems.IV_DRONE)
                .outputItems(MUFFLER_HATCH_ME)
                .inputFluids(GTMaterials.SolderingAlloy, 9216)
                .EUt(GTValues.VA[LuV])
                .researchNode(MEWasteRecycling)
                .duration(400)
                .save();

        ASSEMBLER_RECIPES.builder("huge_item_import_bus")
                .inputItems(GTMachines.ITEM_IMPORT_BUS[basicMEHullTier].asItem())
                .inputItems(GTCraftingComponents.ROBOT_ARM.get(basicMEHullTier), (GTORules.RECIPE_TIER.isExpert() ? 16 : 4))
                .inputItems(GTMachines.SUPER_CHEST[basicMEHullTier].asItem(), (GTORules.RECIPE_TIER.isExpert() ? 16 : 4))
                .inputFluids((GTORules.RECIPE_TIER.isExpert() ? GTMaterials.RhodiumPlatedPalladium : GTOMaterials.StainlessSteel155Ph), 144 * (GTORules.RECIPE_TIER.isExpert() ? 15 : 2))
                .outputItems(GTOMachines.HUGE_ITEM_IMPORT_BUS.asItem())
                .EUt(VA[basicMEHullTier])
                .duration(600)
                .save();
    }

    public void registerInputOutputConversions() {
        VanillaRecipeHelper.addShapedRecipe("me_fluid_hatch_output_to_input",
                GTAEMachines.FLUID_IMPORT_HATCH_ME.asItem(), "d", "B", 'B',
                GTAEMachines.FLUID_EXPORT_HATCH_ME.asItem());
        VanillaRecipeHelper.addShapedRecipe("me_fluid_hatch_input_to_output",
                GTAEMachines.FLUID_EXPORT_HATCH_ME.asItem(), "d", "B", 'B',
                GTAEMachines.FLUID_IMPORT_HATCH_ME.asItem());
        VanillaRecipeHelper.addShapedRecipe("me_item_bus_output_to_input",
                GTAEMachines.ITEM_IMPORT_BUS_ME.asItem(), "d", "B", 'B',
                GTAEMachines.ITEM_EXPORT_BUS_ME.asItem());
        VanillaRecipeHelper.addShapedRecipe("me_item_bus_input_to_output",
                GTAEMachines.ITEM_EXPORT_BUS_ME.asItem(), "d", "B", 'B',
                GTAEMachines.ITEM_IMPORT_BUS_ME.asItem());
    }

    public void registerExportBuffer() {
        ASSEMBLER_RECIPES.recipeBuilder("me_export_buffer")
                .inputItems(GTMachines.BUFFER[advancedMEHullTier].asItem())
                .inputItems(GTAEMachines.ITEM_EXPORT_BUS_ME.asItem())
                .inputItems(GTAEMachines.FLUID_EXPORT_HATCH_ME.asItem())
                .inputFluids(GTMaterials.SolderingAlloy, 576)
                .inputItems(GTCraftingComponents.FIELD_GENERATOR.get(basicMEHullTier))
                .outputItems(CustomMachines.ME_EXPORT_BUFFER.asItem())
                .duration(400)
                .EUt(GTValues.VA[advancedMEHullTier])
                .save();
    }

    public void registerTagFilterParts() {
        ASSEMBLER_RECIPES.builder("me_tag_filter_stock_bus_part_machine")
                .inputItems(GTAEMachines.STOCKING_IMPORT_BUS_ME.asItem())
                .inputItems(GTItems.TAG_FILTER, 16)
                .inputItems(GTItems.SMART_ITEM_FILTER, 16)
                .inputItems(GTCraftingComponents.ROBOT_ARM.get(basicMEHullTier))
                .outputItems(GTAEMachines.ME_TAG_FILTER_STOCK_BUS.asItem())
                .inputFluids(GTMaterials.SolderingAlloy, 576)
                .EUt(480)
                .duration(400)
                .save();

        ASSEMBLER_RECIPES.builder("me_tag_filter_stock_hatch_part_machine")
                .inputItems(GTAEMachines.STOCKING_IMPORT_HATCH_ME.asItem())
                .inputItems(GTItems.FLUID_FILTER, 16)
                .inputItems(GTCraftingComponents.ROBOT_ARM.get(basicMEHullTier))
                .inputItems(GTItems.SMART_ITEM_FILTER, 16)
                .outputItems(GTAEMachines.ME_TAG_FILTER_STOCK_HATCH.asItem())
                .inputFluids(GTMaterials.SolderingAlloy, 576)
                .EUt(480)
                .duration(400)
                .save();

        ASSEMBLER_RECIPES.builder("me_tag_filter_input_bus_part_machine")
                .inputItems(GTAEMachines.ITEM_IMPORT_BUS_ME.asItem())
                .inputItems(GTItems.TAG_FILTER, 16)
                .inputItems(GTItems.SMART_ITEM_FILTER, 16)
                .inputItems(GTCraftingComponents.ROBOT_ARM.get(basicMEHullTier))
                .outputItems(GTAEMachines.ME_TAG_FILTER_INPUT_BUS.asItem())
                .inputFluids(GTMaterials.SolderingAlloy, 576)
                .EUt(480)
                .duration(400)
                .save();

        ASSEMBLER_RECIPES.builder("me_tag_filter_input_hatch_part_machine")
                .inputItems(GTAEMachines.FLUID_IMPORT_HATCH_ME.asItem())
                .inputItems(GTItems.FLUID_FILTER, 16)
                .inputItems(GTCraftingComponents.ROBOT_ARM.get(basicMEHullTier))
                .inputItems(GTItems.SMART_ITEM_FILTER, 16)
                .outputItems(GTAEMachines.ME_TAG_FILTER_INPUT_HATCH.asItem())
                .inputFluids(GTMaterials.SolderingAlloy, 576)
                .EUt(480)
                .duration(400)
                .save();
    }

    public void registerRequestableInputsAndInputBuffer() {
        ASSEMBLY_LINE_RECIPES.builder("me_requestable_input_bus_machine")
                .inputItems(GTAEMachines.ITEM_IMPORT_BUS_ME.asItem())
                .inputItems(AEItems.CRAFTING_CARD.asItem())
                .inputItems(TagPrefix.plateDouble, GTMaterials.RhodiumPlatedPalladium, 4)
                .inputItems(GTItems.ROBOT_ARM_IV, 2)
                .outputItems(GTAEMachines.ME_REQUESTABLE_INPUT_BUS_MACHINE)
                .inputFluids(GTMaterials.SolderingAlloy, 1296)
                .EUt(480)
                .duration(1440)
                .researchNode(BaseMEMachines)
                .scanner(GTAEMachines.ITEM_IMPORT_BUS_ME.asItem())
                .save();

        ASSEMBLY_LINE_RECIPES.builder("me_requestable_input_hatch_machine")
                .inputItems(GTAEMachines.FLUID_IMPORT_HATCH_ME.asItem())
                .inputItems(AEItems.CRAFTING_CARD.asItem())
                .inputItems(TagPrefix.plateDouble, GTMaterials.RhodiumPlatedPalladium, 4)
                .inputItems(GTItems.ROBOT_ARM_IV, 2)
                .outputItems(GTAEMachines.ME_REQUESTABLE_INPUT_HATCH_MACHINE)
                .inputFluids(GTMaterials.SolderingAlloy, 1296)
                .EUt(480)
                .duration(1440)
                .researchNode(BaseMEMachines)
                .scanner(GTAEMachines.FLUID_IMPORT_HATCH_ME.asItem())
                .save();
        ASSEMBLY_LINE_RECIPES.builder("me_input_buffer_part_machine")
                .inputItems(GTAEMachines.ME_REQUESTABLE_INPUT_BUS_MACHINE, 2)
                .inputItems(GTAEMachines.ME_REQUESTABLE_INPUT_HATCH_MACHINE, 2)
                .inputItems(GTAEMachines.ME_PATTERN_BUFFER.asItem())
                .inputItems(GTItems.FIELD_GENERATOR_LuV, 4)
                .inputItems(GTMachines.BUFFER[GTValues.LuV].asItem(), 2)
                .inputItems(TagPrefix.pipeNonupleFluid, GTMaterials.NiobiumTitanium, 2)
                .inputItems(GTBlocks.CASING_LAMINATED_GLASS.asItem(), 2)
                .inputItems(AEItems.SPEED_CARD.asItem(), 8)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .outputItems(GTAEMachines.ME_INPUT_BUFFER_PART_MACHINE)
                .inputFluids(GTMaterials.SolderingAlloy, 1296)
                .inputFluids(GTMaterials.Lubricant, 500)
                .scanner(Registration.REQUESTER.asItem())
                .EUt(480)
                .duration(1440)
                .researchNode(MESmartGatingClustering)
                .save();
    }

    public void registerIntelligentScanningProxy() {
        ASSEMBLY_LINE_RECIPES.builder("intelligent_scanning_me_proxy")
                .inputItems(GTAEMachines.ME_PATTERN_BUFFER_PROXY.asItem())
                .inputItems(GTOTagPrefix.NANITES, GTMaterials.Naquadah)
                .inputItems(GTItems.EMITTER_UHV)
                .inputItems(GTItems.SENSOR_UHV)
                .outputItems(ExResearchMachines.INTELLIGENT_SCANNING_ME_PROXY)
                .inputFluids(GTMaterials.SolderingAlloy, 2880)
                .EUt(24576)
                .researchNode(ScannerInnovation)
                .duration(700)
                .save();
    }

    public void registerPatternBuffers() {
        ASSEMBLY_LINE_RECIPES.builder("me_pattern_buffer")
                .inputItems(GTMachines.DUAL_IMPORT_HATCH[LuV], 1)
                .inputItems(GTItems.EMITTER_LuV, 2)
                .inputItems(CustomTags.LuV_CIRCUITS, 4)
                .inputItems(GTOItems.CELL_COMPONENT_1M)
                .inputItems(EPPItemAndBlock.EX_PATTERN_PROVIDER.asItem(), 3)
                .inputItems(TieredInterfaces.block(1).asItem(), 3)
                .inputItems(AEItems.SPEED_CARD.asItem(), 4)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 2)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputFluids(GTMaterials.SolderingAlloy, L << 2)
                .inputFluids(GTMaterials.Lubricant, 500)
                .outputItems(GTAEMachines.ME_PATTERN_BUFFER)
                .scanner(b -> b.researchStack(GTMachines.DUAL_IMPORT_HATCH[LuV].asItem())
                        .duration(1200)
                        .EUt(VA[LuV]))
                .duration(600).EUt(VA[LuV])
                .researchNode(BaseMEMachines)
                .save();

        ASSEMBLY_LINE_RECIPES.builder("me_pattern_buffer_proxy")
                .inputItems(GTMachines.HULL[LuV], 1)
                .inputItems(GTOMachines.ME_WIRELESS_CONNECTION_MACHINE, 1)
                .inputItems(GTItems.SENSOR_LuV, 16)
                .inputItems(GTItems.FIELD_GENERATOR_LuV, 16)
                .inputItems(CustomTags.UV_CIRCUITS, 16)
                .inputItems(AEBlocks.QUANTUM_LINK.asItem(), 1)
                .inputItems(AEBlocks.QUANTUM_RING.asItem(), 8)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputFluids(GTMaterials.SolderingAlloy, L << 2)
                .inputFluids(GTMaterials.Lubricant, 500)
                .outputItems(GTAEMachines.ME_PATTERN_BUFFER_PROXY)
                .researchStation(b -> b.researchStack(GTAEMachines.ME_PATTERN_BUFFER.asItem())
                        .CWUt(32))
                .researchNode(BaseMEMachines)
                .duration(600).EUt(VA[ZPM])
                .save();

        ASSEMBLY_LINE_RECIPES.builder("me_catalyst_pattern_buffer")
                .inputItems(GTMachines.DUAL_IMPORT_HATCH[ZPM], 1)
                .inputItems(GTOMachines.ADVANCED_CATALYST_HATCH, 1)
                .inputItems(GTOItems.INTEGRATED_CONTROL_CORE_UV, 8)
                .inputItems(CustomTags.UHV_CIRCUITS, 16)
                .inputItems(GTOItems.CELL_COMPONENT_4M)
                .inputItems(EPPItemAndBlock.EX_PATTERN_PROVIDER.asItem(), 3)
                .inputItems(TieredInterfaces.block(1).asItem(), 3)
                .inputItems(AEItems.SPEED_CARD.asItem(), 4)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 2)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputFluids(GTMaterials.SolderingAlloy, L << 2)
                .inputFluids(GTMaterials.Lubricant, 1000)
                .outputItems(GTAEMachines.ME_CATALYST_ME_PATTERN_BUFFER)
                .researchStation(b -> b.researchStack(GTOMachines.ADVANCED_CATALYST_HATCH.asItem())
                        .duration(1200)
                        .CWUt(128))
                .duration(600).EUt(VA[UV])
                .researchNode(MECatalystSupplying)
                .save();

        ASSEMBLY_LINE_RECIPES.builder("me_extend_pattern_buffer")
                .inputItems(GTMachines.DUAL_IMPORT_HATCH[UV], 1)
                .inputItems(GTAEMachines.ME_PATTERN_BUFFER, 1)
                .inputItems(GTOItems.CELL_COMPONENT_16M, 4)
                .inputItems(EPPItemAndBlock.EX_PATTERN_PROVIDER.asItem(), 4)
                .inputItems(TieredInterfaces.block(1).asItem(), 4)
                .inputItems(AEItems.SPEED_CARD.asItem(), 6)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputFluids(GTMaterials.SolderingAlloy, L << 2)
                .outputItems(GTAEMachines.ME_EXTEND_PATTERN_BUFFER)
                .researchStation(b -> b.researchStack(GTAEMachines.ME_PATTERN_BUFFER_PROXY.asItem())
                        .duration(1200)
                        .CWUt(128))
                .duration(600).EUt(VA[UV])
                .researchNode(MECapacityExpansion)
                .save();

        ASSEMBLY_LINE_RECIPES.builder("me_extend_pattern_buffer_ultra")
                .inputItems(GTMachines.DUAL_IMPORT_HATCH[UHV], 1)
                .inputItems(GTAEMachines.ME_EXTEND_PATTERN_BUFFER, 4)
                .inputItems(GTOItems.CELL_COMPONENT_256M, 16)
                .inputItems(EPPItemAndBlock.EX_PATTERN_PROVIDER.asItem(), 16)
                .inputItems(TieredInterfaces.block(1).asItem(), 16)
                .inputItems(AEItems.SPEED_CARD.asItem(), 6)
                .inputItems(AEItems.CAPACITY_CARD.asItem(), 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                .inputFluids(GTMaterials.SolderingAlloy, L << 2)
                .outputItems(GTAEMachines.ME_EXTEND_PATTERN_BUFFER_ULTRA)
                .researchStation(b -> b.researchStack(GTAEMachines.ME_EXTEND_PATTERN_BUFFER.asItem())
                        .duration(1200)
                        .CWUt(256))
                .duration(2400).EUt(VA[UHV])
                .researchNode(MECapacityExpansion)
                .save();
    }

    public void registerCraftPatternHatch() {
        ASSEMBLY_LINE_RECIPES.builder("me_craft_pattern_part_machine")
                .inputItems(GTMachines.DUAL_IMPORT_HATCH[GTValues.ZPM].asItem())
                .inputItems(EPPItemAndBlock.ASSEMBLER_MATRIX_PATTERN.asItem())
                .inputItems(EPPItemAndBlock.ASSEMBLER_MATRIX_CRAFTER.asItem())
                .inputItems(EPPItemAndBlock.ASSEMBLER_MATRIX_SPEED.asItem())
                .inputItems(GTItems.EMITTER_ZPM, 2)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(TagPrefix.wireFine, GTMaterials.YttriumBariumCuprate, 64)
                .inputItems(TagPrefix.wireFine, GTMaterials.YttriumBariumCuprate, 64)
                .outputItems(GTAEMachines.ME_CRAFT_PATTERN_PART_MACHINE.asItem())
                .inputFluids(GTOMaterials.Indalloy140, 288)
                .inputFluids(GTMaterials.Polybenzimidazole, 576)
                .EUt(122880)
                .duration(200)
                .researchStation(b -> b.researchStack(AEItems.CRAFTING_PATTERN)
                        .CWUt(32)
                        .EUt(122880))
                .researchNode(MEAdvancedAssembly)
                .save();
    }

    public void registerWildcardPatternBuffer() {
        if (GTORules.RECIPE_TIER.isExpert()) {
            ASSEMBLY_LINE_RECIPES.builder("me_wildcard_pattern_buffer_uhv")
                    .inputItems(GTOMachines.HUGE_ITEM_IMPORT_BUS.asItem())
                    .inputItems(GTAEMachines.ME_EXTEND_PATTERN_BUFFER_ULTRA)
                    .inputItems(AEItems.FUZZY_CARD.asItem(), 4)
                    .inputItems(AEItems.EQUAL_DISTRIBUTION_CARD.asItem(), 2)
                    .inputItems(GTOAEParts.Pattern_Content_Access_Terminal.get().asItem(), 4)
                    .inputItems(GTItems.SMART_ITEM_FILTER, 4)
                    .inputItems(GTItems.FLUID_FILTER, 4)
                    .inputItems(GTItems.TAG_FILTER, 4)
                    .inputItems(GTItems.TAG_FLUID_FILTER, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.HighEntropyShapeMemoryAlloy, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.StarliteSteel, 4)
                    .inputItems(TagPrefix.foil, GTOMaterials.Quicksilver, 64)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .outputItems(GTAEMachines.ME_WILDCARD_PATTERN_BUFFER)
                    .inputFluids(GTOMaterials.EnergySolidifier, 2000)
                    .inputFluids(GTMaterials.Lubricant, 1000)
                    .EUt(152000)
                    .duration(600)
                    .researchNode(MESmartGatingClustering)
                    .save();
        }
        if (GTORules.RECIPE_TIER.isNormal()) {
            ASSEMBLY_LINE_RECIPES.builder("me_wildcard_pattern_buffer_zpm")
                    .inputItems(GTOMachines.HUGE_ITEM_IMPORT_BUS.asItem())
                    .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                    .inputItems(GTAEMachines.ME_PATTERN_BUFFER)
                    .inputItems(GTOAEParts.Pattern_Content_Access_Terminal.get().asItem(), 4)
                    .inputItems(AEItems.FUZZY_CARD.asItem(), 4)
                    .inputItems(AEItems.EQUAL_DISTRIBUTION_CARD.asItem(), 2)
                    .inputItems(GTItems.TAG_FILTER, 4)
                    .inputItems(GTItems.FLUID_FILTER, 4)
                    .inputItems(GTItems.SMART_ITEM_FILTER, 4)
                    .inputItems(GTItems.TAG_FLUID_FILTER, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.TitaniumTi53311S, 4)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .inputItems(TagPrefix.foil, GTOMaterials.TitaniumTC21, 64)
                    .outputItems(GTAEMachines.ME_WILDCARD_PATTERN_BUFFER)
                    .inputFluids(GTMaterials.Lubricant, 1000)
                    .EUt(67000)
                    .duration(600)
                    .researchNode(MESmartGatingClustering)
                    .save();
        }
        if (GTORules.RECIPE_TIER.isEasy()) {

            ASSEMBLY_LINE_RECIPES.builder("me_wildcard_pattern_buffer_easy")
                    .inputItems(GTMachines.ITEM_IMPORT_BUS[GTValues.LuV].asItem())
                    .inputItems(GTAEMachines.ME_PATTERN_BUFFER.asItem())
                    .inputItems(GTOAEParts.Pattern_Content_Access_Terminal.get().asItem(), 4)
                    .inputItems(AEItems.FUZZY_CARD.asItem(), 4)
                    .inputItems(AEItems.EQUAL_DISTRIBUTION_CARD.asItem(), 2)
                    .inputItems(GTItems.TAG_FILTER, 4)
                    .inputItems(GTItems.FLUID_FILTER, 4)
                    .inputItems(GTItems.SMART_ITEM_FILTER, 4)
                    .inputItems(GTItems.TAG_FLUID_FILTER, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.PlatinumRhodiumAlloy, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.Grcop84, 4)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .inputItems(TagPrefix.foil, GTOMaterials.Aerialite, 64)
                    .outputItems(GTAEMachines.ME_WILDCARD_PATTERN_BUFFER)
                    .inputFluids(GTMaterials.SolderingAlloy, 2000)
                    .inputFluids(GTMaterials.Lubricant, 1000)
                    .EUt(10000)
                    .researchNode(MESmartGatingClustering)
                    .duration(600)
                    .save();
        }
    }

    public void registerMolecularAssemblyPatternHatches() {
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("molecular_assembly_factory_pattern_hatch"), GTAEMachines.MOLECULAR_ASSEMBLY_FACTORY_PATTERN_HATCH.asItem(),
                "ABA",
                "CDE",
                "ABA",
                'A', new MaterialEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'B', AEBlocks.PATTERN_PROVIDER.asItem(), 'C', AEItems.CAPACITY_CARD.asItem(), 'D', GTMachines.DUAL_IMPORT_HATCH[MV].asItem(), 'E', AEItems.SPEED_CARD.asItem());
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("molecular_assembly_factory_pull_pattern_hatch"), GTAEMachines.MOLECULAR_ASSEMBLY_FACTORY_PULL_PATTERN_HATCH.asItem(),
                "ABA",
                "CDE",
                "ABA",
                'A', new MaterialEntry(TagPrefix.plate, GTMaterials.StainlessSteel), 'B', AEBlocks.PATTERN_PROVIDER.asItem(), 'C', AEItems.CRAFTING_CARD.asItem(), 'D', GTMachines.DUAL_IMPORT_HATCH[MV].asItem(), 'E', AEItems.SPEED_CARD.asItem());
    }

    public void registerSimplePatternBuffer() {
        ASSEMBLER_RECIPES.builder("me_simple_pattern_buffer")
                .inputItems(GTMachines.DUAL_IMPORT_HATCH[MV].asItem())
                .inputItems(AEBlocks.PATTERN_PROVIDER.block().asItem())
                .inputItems(AEItems.CAPACITY_CARD.asItem())
                .inputItems(AEItems.SPEED_CARD.asItem())
                .outputItems(OptionalMachine.ME_SIMPLE_PATTERN_BUFFER.asItem())
                .inputFluids(GTMaterials.SolderingAlloy, 72)
                .EUt(120)
                .duration(200)
                .save();
    }
}
