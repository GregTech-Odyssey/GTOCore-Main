package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import vazkii.botania.common.item.BotaniaItems;

import static com.gtocore.common.data.GTORecipeTypes.POLARIZER_RECIPES;

final class Polarizer {

    public static void init() {
        POLARIZER_RECIPES.recipeBuilder("attuned_tengam_dust")
                .inputItems(TagPrefix.dust, GTOMaterials.PurifiedTengam)
                .outputItems(TagPrefix.dust, GTOMaterials.AttunedTengam)
                .EUt(125829120)
                .duration(400)
                .save();

        POLARIZER_RECIPES.recipeBuilder("small_attuned_tengam_dust")
                .inputItems(TagPrefix.dustSmall, GTOMaterials.PurifiedTengam)
                .outputItems(TagPrefix.dustSmall, GTOMaterials.AttunedTengam)
                .EUt(31457280)
                .duration(400)
                .save();

        POLARIZER_RECIPES.recipeBuilder("triplet_neutronium_sphere")
                .inputItems(GTOItems.NEUTRONIUM_SPHERE)
                .outputItems(GTOItems.TRIPLET_NEUTRONIUM_SPHERE)
                .EUt(5000000)
                .duration(200)
                .save();

        POLARIZER_RECIPES.recipeBuilder("energetic_netherite")
                .inputItems(Items.NETHERITE_BLOCK.asItem())
                .outputItems(TagPrefix.dust, GTOMaterials.EnergeticNetherite)
                .EUt(524288)
                .duration(200)
                .save();

        POLARIZER_RECIPES.recipeBuilder("energetic_netherite_a")
                .inputItems(TagPrefix.ingot, GTMaterials.Netherite)
                .outputItems(TagPrefix.dust, GTOMaterials.EnergeticNetherite)
                .EUt(8388608)
                .duration(200)
                .save();

        POLARIZER_RECIPES.recipeBuilder("energetic_netherite_b")
                .inputItems(TagPrefix.dust, GTMaterials.Netherite)
                .outputItems(TagPrefix.dust, GTOMaterials.EnergeticNetherite)
                .EUt(33554432)
                .duration(20)
                .save();

        POLARIZER_RECIPES.recipeBuilder("magnetized_golden_carrot")
                .inputItems(Items.GOLDEN_CARROT)
                .outputItems(GTOItems.MAGNETIZED_GOLDEN_CARROT)
                .EUt(GTValues.VA[GTValues.ULV])
                .duration(100)
                .save();

        POLARIZER_RECIPES.recipeBuilder("magnetized_guanyin_soil")
                .inputItems(TagPrefix.dust, GTMaterials.Clay, 16)
                .outputItems(GTOItems.MAGNETIZED_GUANYIN_SOIL, 16)
                .EUt(GTValues.VA[GTValues.ULV])
                .duration(100)
                .save();

        POLARIZER_RECIPES.recipeBuilder("magnetized_mana_cookie")
                .inputItems(BotaniaItems.manaCookie)
                .outputItems(GTOItems.MAGNETIZED_MANA_COOKIE)
                .EUt(GTValues.VA[GTValues.ULV])
                .duration(100)
                .save();

        POLARIZER_RECIPES.recipeBuilder("magnetized_golden_apple")
                .inputItems(Items.GOLDEN_APPLE)
                .outputItems(GTOItems.MAGNETIZED_GOLDEN_APPLE)
                .EUt(GTValues.VA[GTValues.ULV])
                .duration(100)
                .save();

        POLARIZER_RECIPES.recipeBuilder("magnetized_enchanted_golden_apple")
                .inputItems(Items.ENCHANTED_GOLDEN_APPLE)
                .outputItems(GTOItems.MAGNETIZED_ENCHANTED_GOLDEN_APPLE)
                .EUt(GTValues.VA[GTValues.LV])
                .duration(100)
                .save();

        POLARIZER_RECIPES.recipeBuilder("magnetized_golden_melon_slice")
                .inputItems(Items.GLISTERING_MELON_SLICE)
                .outputItems(GTOItems.MAGNETIZED_GOLDEN_MELON_SLICE)
                .EUt(GTValues.VA[GTValues.ULV])
                .duration(100)
                .save();
    }
}
