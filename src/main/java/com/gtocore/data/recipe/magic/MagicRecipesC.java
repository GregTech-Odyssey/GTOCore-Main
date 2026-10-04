package com.gtocore.data.recipe.magic;

import com.gtolib.utils.RegistriesUtils;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.crafting.Ingredient;

import static com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys.GAS;
import static com.gtocore.common.data.GTOMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.*;

public class MagicRecipesC {

    public static void init() {
        // 魔法原木的集成木质生物质热解
        {
            archwoodDistillationRecipe("blue_archwood", getArchwoodIngredient("blue_archwood"), Undine);
            archwoodDistillationRecipe("red_archwood", getArchwoodIngredient("red_archwood"), Salamander);
            archwoodDistillationRecipe("green_archwood", getArchwoodIngredient("green_archwood"), Sylph);
            archwoodDistillationRecipe("purple_archwood", getArchwoodIngredient("purple_archwood"), Aether);
        }
    }

    private static Ingredient getArchwoodIngredient(String name) {
        return Ingredient.of(
                RegistriesUtils.getItem("ars_nouveau:" + name + "_log"),
                RegistriesUtils.getItem("ars_nouveau:" + name + "_wood"),
                RegistriesUtils.getItem("ars_nouveau:stripped_" + name + "_log"),
                RegistriesUtils.getItem("ars_nouveau:stripped_" + name + "_wood"));
    }

    private static void archwoodDistillationRecipe(String name, Ingredient ingredient, Material material) {
        WOOD_DISTILLATION_RECIPES.builder(name + "_distillation_with_nitrogen")
                .inputItems(ingredient, 16)
                .outputItems(TagPrefix.dust, GTMaterials.Ash, 2)
                .inputFluids(GTMaterials.Nitrogen, 1000)
                .outputFluids(GTMaterials.Benzene, 1050)
                .outputFluids(GTMaterials.Dimethylbenzene, 1000)
                .outputFluids(GTMaterials.Creosote, 900)
                .outputFluids(GTMaterials.CarbonDioxide, 490)
                .outputFluids(GTMaterials.Methanol, 480)
                .outputFluids(GTMaterials.CarbonMonoxide, 340)
                .outputFluids(GTMaterials.Phenol, 225)
                .outputFluids(GTMaterials.Toluene, 225)
                .outputFluids(GTMaterials.AceticAcid, 160)
                .outputFluids(GTMaterials.Methane, 130)
                .outputFluids(GTMaterials.Acetone, 80)
                .outputFluids(GTMaterials.Ethylene, 20)
                .outputFluids(GTMaterials.Hydrogen, 20)
                .outputFluids(GTMaterials.MethylAcetate, 16)
                .outputFluids(GTMaterials.Ethanol, 16)
                .outputFluids(Mana.getFluid(GAS, 64))
                .outputFluids(material.getFluid(GAS, 32))
                .outputFluids(Gnome.getFluid(GAS, 16))
                .circuitMeta(4)
                .EUt(120)
                .duration(300)
                .save();

        WOOD_DISTILLATION_RECIPES.builder(name + "_distillation_with_steam")
                .inputItems(ingredient, 16)
                .outputItems(TagPrefix.dust, GTMaterials.Ash, 2)
                .inputFluids(GTMaterials.Steam, 1000)
                .outputFluids(GTMaterials.Ammonia, 600)
                .outputFluids(GTMaterials.CarbonDioxide, 500)
                .outputFluids(GTMaterials.Ethylbenzene, 500)
                .outputFluids(GTMaterials.Naphthalene, 410)
                .outputFluids(GTMaterials.HydrogenSulfide, 307)
                .outputFluids(GTMaterials.Creosote, 205)
                .outputFluids(GTMaterials.Phenol, 102)
                .outputFluids(Mana.getFluid(GAS, 64))
                .outputFluids(material.getFluid(GAS, 32))
                .outputFluids(Gnome.getFluid(GAS, 16))
                .circuitMeta(5)
                .EUt(120)
                .duration(300)
                .save();

        WOOD_DISTILLATION_RECIPES.builder(name + "_distillation_with_water")
                .inputItems(ingredient, 16)
                .outputItems(GTItems.FERTILIZER, 6)
                .inputFluids(GTMaterials.Water, 6000)
                .outputFluids(GTMaterials.Methane, 3300)
                .outputFluids(GTMaterials.CarbonDioxide, 2200)
                .outputFluids(GTMaterials.Methanol, 825)
                .outputFluids(GTMaterials.Ethanol, 825)
                .outputFluids(GTMaterials.Creosote, 560)
                .outputFluids(GTMaterials.Ammonia, 550)
                .outputFluids(GTMaterials.AceticAcid, 137)
                .outputFluids(Mana.getFluid(GAS, 64))
                .outputFluids(material.getFluid(GAS, 32))
                .outputFluids(Gnome.getFluid(GAS, 16))
                .circuitMeta(6)
                .EUt(120)
                .duration(300)
                .save();
    }
}
