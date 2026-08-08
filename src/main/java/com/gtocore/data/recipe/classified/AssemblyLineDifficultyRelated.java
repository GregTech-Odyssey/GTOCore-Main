package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOAEParts;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import appeng.core.definitions.AEItems;

import static com.gtocore.common.data.GTORecipeTypes.ASSEMBLY_LINE_RECIPES;
import static com.gtocore.data.recipe.research.AnalyzeData.MESmartGatingClustering;

class AssemblyLineDifficultyRelated {

    static void init() {
        if (GTOCore.isExpert() || GTOCore.isNormal()) {
            ASSEMBLY_LINE_RECIPES.builder("me_wildcard_pattern_buffer_uhv")
                    .inputItems(GTOMachines.HUGE_ITEM_IMPORT_BUS.asItem())
                    .inputItems("gtocore:me_extend_pattern_buffer_ultra")
                    .inputItems(AEItems.FUZZY_CARD.asItem(), 4)
                    .inputItems(AEItems.EQUAL_DISTRIBUTION_CARD.asItem(), 2)
                    .inputItems(GTOAEParts.INSTANCE.getPattern_Content_Access_Terminal().get().asItem(), 4)
                    .inputItems(GTItems.SMART_ITEM_FILTER, 4)
                    .inputItems(GTItems.FLUID_FILTER, 4)
                    .inputItems(GTItems.TAG_FILTER, 4)
                    .inputItems(GTItems.TAG_FLUID_FILTER, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.HighEntropyShapeMemoryAlloy, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.StarliteSteel, 4)
                    .inputItems(TagPrefix.foil, GTOMaterials.Quicksilver, 64)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .outputItems("gtocore:me_wildcard_pattern_buffer")
                    .inputFluids(GTOMaterials.EnergySolidifier, 2000)
                    .inputFluids(GTMaterials.Lubricant, 1000)
                    .EUt(152000)
                    .duration(200)
                    .researchStation(b -> b.researchStack(GTOAEParts.INSTANCE.getPattern_Content_Access_Terminal().get().asItem())
                            .duration(1200)
                            .EUt(GTValues.VA[GTValues.UV])
                            .CWUt(256))
                    .researchNode(MESmartGatingClustering)
                    .save();
        }
        if (GTOCore.isNormal()) {
            ASSEMBLY_LINE_RECIPES.builder("me_wildcard_pattern_buffer_orichalcos")
                    .inputItems(GTOMachines.HUGE_ITEM_IMPORT_BUS.asItem())
                    .inputItems(CustomTags.UEV_CIRCUITS, 4)
                    .inputItems("gtocore:me_extend_pattern_buffer")
                    .inputItems(GTOAEParts.INSTANCE.getPattern_Content_Access_Terminal().get().asItem(), 4)
                    .inputItems(AEItems.FUZZY_CARD.asItem(), 4)
                    .inputItems(AEItems.EQUAL_DISTRIBUTION_CARD.asItem(), 2)
                    .inputItems(GTItems.TAG_FILTER, 4)
                    .inputItems(GTItems.FLUID_FILTER, 4)
                    .inputItems(GTItems.SMART_ITEM_FILTER, 4)
                    .inputItems(GTItems.TAG_FLUID_FILTER, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.TitaniumTi53311S, 4)
                    .inputItems(TagPrefix.gear, GTOMaterials.Orichalcos, 4)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .inputItems(TagPrefix.wireFine, GTMaterials.Europium, 64)
                    .inputItems(TagPrefix.foil, GTOMaterials.Quicksilver, 64)
                    .outputItems("gtocore:me_wildcard_pattern_buffer")
                    .inputFluids(GTOMaterials.EnergySolidifier, 2000)
                    .inputFluids(GTMaterials.Lubricant, 1000)
                    .EUt(152000)
                    .duration(200)
                    .researchStation(b -> b.researchStack(GTOAEParts.INSTANCE.getPattern_Content_Access_Terminal().get().asItem())
                            .duration(1200)
                            .EUt(GTValues.VA[GTValues.UV])
                            .CWUt(256))
                    .researchNode(MESmartGatingClustering)
                    .save();
        }
        if (GTOCore.isEasy()) {

            ASSEMBLY_LINE_RECIPES.builder("me_wildcard_pattern_buffer_easy")
                    .inputItems(GTMachines.ITEM_IMPORT_BUS[GTValues.LuV].asItem())
                    .inputItems("gtceu:me_pattern_buffer")
                    .inputItems(GTOAEParts.INSTANCE.getPattern_Content_Access_Terminal().get().asItem(), 4)
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
                    .outputItems("gtocore:me_wildcard_pattern_buffer")
                    .inputFluids(GTMaterials.SolderingAlloy, 2000)
                    .inputFluids(GTMaterials.Lubricant, 1000)
                    .EUt(152000 / 16)
                    .researchStation(b -> b.researchStack(GTOAEParts.INSTANCE.getPattern_Content_Access_Terminal().get().asItem())
                            .duration(1200)
                            .EUt(GTValues.VA[GTValues.LuV])
                            .CWUt(32))
                    .researchNode(MESmartGatingClustering)
                    .duration(200)
                    .save();
        }
    }
}
