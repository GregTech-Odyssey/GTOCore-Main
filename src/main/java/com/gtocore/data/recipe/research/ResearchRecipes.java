package com.gtocore.data.recipe.research;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;

import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gtocore.common.data.GTORecipeDataKeys.DATA_TESTING_LEVEL;
import static com.gtocore.common.data.GTORecipeTypes.BIO_RESEARCH_RECIPES;
import static com.gtocore.common.data.GTORecipeTypes.DATA_TESTING_RECIPES;

public final class ResearchRecipes {

    public static void init() {
        ScanningRecipes.init();
        AnalyzeData.INSTANCE.init();

        DATA_TESTING_RECIPES.builder("tier1").inputItems(GTOItems.CELL_COMPONENT_1M).EUt(VA[LuV]).circuitMeta(1).duration(200).addData(DATA_TESTING_LEVEL, 1).save();
        DATA_TESTING_RECIPES.builder("tier2").inputItems(GTOItems.CELL_COMPONENT_4M).EUt(2L * VA[LuV]).circuitMeta(1).duration(200).addData(DATA_TESTING_LEVEL, 5).save();
        DATA_TESTING_RECIPES.builder("tier3").inputItems(GTOItems.CELL_COMPONENT_16M).EUt(VA[ZPM]).circuitMeta(1).duration(200).addData(DATA_TESTING_LEVEL, 9).save();
        DATA_TESTING_RECIPES.builder("tier4").inputItems(GTOItems.CELL_COMPONENT_64M).EUt(2L * VA[ZPM]).circuitMeta(1).duration(200).addData(DATA_TESTING_LEVEL, 13).save();
        DATA_TESTING_RECIPES.builder("tier5").inputItems(GTOItems.CELL_COMPONENT_256M).EUt(2L * VA[UV]).circuitMeta(1).duration(200).addData(DATA_TESTING_LEVEL, 17).save();
        DATA_TESTING_RECIPES.builder("tier6").inputItems(GTOItems.INFINITE_CELL_COMPONENT).EUt(2L * VA[UHV]).circuitMeta(1).duration(200).addData(DATA_TESTING_LEVEL, 25).save();

        BIO_RESEARCH_RECIPES.builder("contaminated_petri_dish1")
                .inputItems(GTOItems.ELECTRICALY_WIRED_PETRI_DISH)
                .inputItems(GTOItems.DRAGON_CELLS, 16)
                .inputItems(CustomTags.UIV_CIRCUITS, 4)
                .inputItems(GTItems.COVER_SOLAR_PANEL_IV, 4)
                .outputItems(GTOItems.CONTAMINATED_PETRI_DISH)
                .inputFluids(GTMaterials.Mutagen, 10000)
                .EUt(16777216)
                .duration(600)
                .researchPoints(ResearchTag.BIOLOGY, 4096)
                .addData(GTORecipeDataKeys.RADIOACTIVITY, 50)
                .addData(GTORecipeDataKeys.RADIOACTIVITY_END, 650)
                .save();
        BIO_RESEARCH_RECIPES.builder("contaminated_petri_dish2")
                .inputItems(GTOItems.ELECTRICALY_WIRED_PETRI_DISH)
                .inputItems(GTOItems.BIOLOGICAL_CELLS, 16)
                .inputItems(CustomTags.UHV_CIRCUITS, 4)
                .inputItems(GTOItems.HIGHLY_CONCURRENT_INTENSIVE_OPTICAL_COMPUTING_CHANNEL, 2)
                .outputItems(GTOItems.CONTAMINATED_PETRI_DISH)
                .inputFluids(GTMaterials.Mutagen, 1000)
                .EUt(2097152)
                .duration(600)
                .researchPoints(ResearchTag.BIOLOGY, 256)
                .addData(GTORecipeDataKeys.RADIOACTIVITY, 250)
                .addData(GTORecipeDataKeys.RADIOACTIVITY_END, 400)
                .save();
        BIO_RESEARCH_RECIPES.builder("contaminated_petri_dish3")
                .inputItems(GTOItems.ELECTRICALY_WIRED_PETRI_DISH)
                .inputItems(GTItems.STEM_CELLS, 16)
                .inputItems(CustomTags.ZPM_CIRCUITS, 4)
                .outputItems(GTOItems.CONTAMINATED_PETRI_DISH)
                .inputFluids(GTOMaterials.PluripotencyInductionGeneTherapyFluid, 2000)
                .inputFluids(GTMaterials.SterileGrowthMedium, 2250)
                .EUt(262144)
                .duration(600)
                .researchPoints(ResearchTag.BIOLOGY, 16)
                .addData(GTORecipeDataKeys.RADIOACTIVITY, 80)
                .addData(GTORecipeDataKeys.RADIOACTIVITY_END, 120)
                .save();
    }
}
