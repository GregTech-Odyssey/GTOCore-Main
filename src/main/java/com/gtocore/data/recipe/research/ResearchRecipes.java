package com.gtocore.data.recipe.research;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTORecipeDataKeys;
import com.gtocore.common.recipe.condition.BeamCondition;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import appeng.core.definitions.AEItems;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gtocore.common.data.GTORecipeDataKeys.DATA_TESTING_CAPACITY;
import static com.gtocore.common.data.GTORecipeTypes.*;

public final class ResearchRecipes {

    public static void init() {
        ScanningRecipes.init();

        DATA_TESTING_RECIPES.builder("tier1").inputItems(AEItems.CELL_COMPONENT_1K).EUt(VA[LuV]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 10).save();
        DATA_TESTING_RECIPES.builder("tier2").inputItems(AEItems.CELL_COMPONENT_4K).EUt(2L * VA[LuV]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 12).save();
        DATA_TESTING_RECIPES.builder("tier3").inputItems(AEItems.CELL_COMPONENT_16K).EUt(3L * VA[LuV]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 14).save();
        DATA_TESTING_RECIPES.builder("tier4").inputItems(AEItems.CELL_COMPONENT_64K).EUt(VA[ZPM]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 16).save();
        DATA_TESTING_RECIPES.builder("tier5").inputItems(AEItems.CELL_COMPONENT_256K).EUt(2L * VA[ZPM]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 18).save();
        DATA_TESTING_RECIPES.builder("tier6").inputItems(GTOItems.CELL_COMPONENT_1M).EUt(VA[LuV]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 20).save();
        DATA_TESTING_RECIPES.builder("tier7").inputItems(GTOItems.CELL_COMPONENT_4M).EUt(2L * VA[LuV]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 22).save();
        DATA_TESTING_RECIPES.builder("tier8").inputItems(GTOItems.CELL_COMPONENT_16M).EUt(3L * VA[ZPM]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 24).save();
        DATA_TESTING_RECIPES.builder("tier9").inputItems(GTOItems.CELL_COMPONENT_64M).EUt(VA[UV]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 26).save();
        DATA_TESTING_RECIPES.builder("tier10").inputItems(GTOItems.CELL_COMPONENT_256M).EUt(2L * VA[UV]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 28).save();
        DATA_TESTING_RECIPES.builder("tier11").inputItems(GTOItems.INFINITE_CELL_COMPONENT).EUt(2L * VA[UHV]).circuitMeta(1).duration(200).addData(DATA_TESTING_CAPACITY, 1 << 31 - 1).save();

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
        BEAM_GUIDED_COMPUTATION_TESTING_RECIPES.builder("optical_computing_channel")
                .inputItems(TagPrefix.rod, GTOMaterials.PhotonicKristallite, 4)
                .inputItems(GTOItems.LITHOGRAPHY_MASK)
                .inputFluids(GTMaterials.Silver, FluidStorageKeys.PLASMA, 2000)
                .EUt(1074000)
                .duration(2400)
                .vacuum(4)
                .addCondition(new BeamCondition(6400, 749, 751))
                .researchPoints(ResearchTag.OPTICS, 12)
                .save();
    }
}
