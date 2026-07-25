package com.gtocore.data.recipe.research;

import com.gtocore.common.data.GTOItems;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gtocore.common.data.GTORecipeDataKeys.DATA_TESTING_LEVEL;
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
    }
}
