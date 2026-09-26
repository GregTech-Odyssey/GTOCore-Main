package com.gtocore.data.recipe.classified;

import com.gtocore.common.data.GTOFluids;
import com.gtocore.common.data.GTOItems;

import com.gtolib.utils.RegistriesUtils;
import com.gtolib.utils.TagUtils;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gtocore.common.data.GTORecipeTypes.MANA_GARDEN_RECIPES;
import static net.minecraft.tags.ItemTags.LEAVES;

public final class ManaSimulator {

    public static final int BUFF_FACTOR = 8;

    public static void init() {
        MANA_GARDEN_RECIPES.builder("thermalily")
                .notConsumable(RegistriesUtils.getItem("botania", "thermalily"))
                .inputFluids(new FluidStack(Fluids.LAVA, 1000))
                .duration(900)
                .EUt(VA[MV])
                .MANAt(-20 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("rosa_arcana")
                .notConsumable(RegistriesUtils.getItem("botania", "rosa_arcana"))
                .inputFluids(new FluidStack(GTOFluids.XP_JUICE.get().getSource(), 1000))
                .EUt(VA[MV])
                .duration(1000)
                .MANAt(-50 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("munchdew")
                .notConsumable(RegistriesUtils.getItem("botania", "munchdew"))
                .inputItems(LEAVES, 4)
                .EUt(VA[MV])
                .duration(20)
                .MANAt(-32 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("wither_aconite_nether_star")
                .notConsumable(RegistriesUtils.getItem("mythicbotany", "wither_aconite"))
                .inputItems(Items.NETHER_STAR, 1)
                .EUt(VA[MV])
                .duration(500)
                .MANAt(-2500 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("wither_aconite_quantum_star")
                .notConsumable(RegistriesUtils.getItem("mythicbotany", "wither_aconite"))
                .inputItems(GTItems.QUANTUM_STAR, 1)
                .EUt(VA[MV])
                .duration(500)
                .MANAt(-5000 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("wither_aconite_gravi_star")
                .notConsumable(RegistriesUtils.getItem("mythicbotany", "wither_aconite"))
                .inputItems(GTItems.GRAVI_STAR, 1)
                .EUt(VA[MV])
                .duration(1000)
                .MANAt(-50000 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("wither_aconite_unstable_star")
                .notConsumable(RegistriesUtils.getItem("mythicbotany", "wither_aconite"))
                .inputItems(GTOItems.UNSTABLE_STAR, 1)
                .EUt(VA[MV])
                .duration(1500)
                .MANAt(-2500000 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("wither_aconite_nuclear_star")
                .notConsumable(RegistriesUtils.getItem("mythicbotany", "wither_aconite"))
                .inputItems(GTOItems.NUCLEAR_STAR, 1)
                .EUt(VA[MV])
                .duration(6000)
                .MANAt(-50000000 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("kekimurus_cake")
                .notConsumable(RegistriesUtils.getItem("botania", "kekimurus"))
                .inputItems(Items.CAKE, 1)
                .EUt(VA[MV])
                .duration(70)
                .MANAt(-100 * BUFF_FACTOR)
                .save();

        MANA_GARDEN_RECIPES.recipeBuilder("kekimurus_pies")
                .notConsumable(RegistriesUtils.getItem("botania", "kekimurus"))
                .inputItems(TagUtils.createItemTag("farmersdelight:pies"))
                .EUt(VA[MV])
                .duration(40)
                .MANAt(-100 * BUFF_FACTOR)
                .save();
    }
}
