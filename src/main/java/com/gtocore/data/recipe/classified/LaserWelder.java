package com.gtocore.data.recipe.classified;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.recipe.condition.RestrictedMachineCondition;
import com.gtocore.common.recipe.condition.VacuumCondition;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rodLong;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Steel;
import static com.gregtechceu.gtceu.common.data.GTMaterials.TungstenCarbide;
import static com.gtocore.common.data.GTORecipeTypes.LASER_WELDER_RECIPES;

final class LaserWelder {

    public static void init() {
        LASER_WELDER_RECIPES.builder("reactor_thorium_dual")
                .inputItems(GTOItems.REACTOR_THORIUM_SIMPLE, 2)
                .inputItems(rodLong, Steel, 4)
                .outputItems(GTOItems.REACTOR_THORIUM_DUAL)
                .EUt(VA[HV])
                .duration(2400)
                .addCondition(new VacuumCondition(4))
                .save();

        LASER_WELDER_RECIPES.builder("reactor_thorium_quad")
                .inputItems(GTOItems.REACTOR_THORIUM_DUAL, 2)
                .inputItems(rodLong, Steel, 4)
                .outputItems(GTOItems.REACTOR_THORIUM_QUAD)
                .EUt(VA[HV])
                .duration(2400)
                .addCondition(new VacuumCondition(4))
                .save();

        LASER_WELDER_RECIPES.builder("reactor_uranium_dual")
                .inputItems(GTOItems.REACTOR_URANIUM_SIMPLE, 2)
                .inputItems(rodLong, Steel, 4)
                .outputItems(GTOItems.REACTOR_URANIUM_DUAL)
                .EUt(VA[EV])
                .duration(2400)
                .addCondition(new VacuumCondition(4))
                .save();

        LASER_WELDER_RECIPES.builder("reactor_uranium_quad")
                .inputItems(GTOItems.REACTOR_URANIUM_DUAL, 2)
                .inputItems(rodLong, Steel, 4)
                .outputItems(GTOItems.REACTOR_URANIUM_QUAD)
                .EUt(VA[EV])
                .duration(2400)
                .addCondition(new VacuumCondition(4))
                .save();

        LASER_WELDER_RECIPES.builder("reactor_mox_dual")
                .inputItems(GTOItems.REACTOR_MOX_SIMPLE, 2)
                .inputItems(rodLong, Steel, 4)
                .outputItems(GTOItems.REACTOR_MOX_DUAL)
                .EUt(VA[IV])
                .duration(2400)
                .addCondition(new VacuumCondition(4))
                .save();

        LASER_WELDER_RECIPES.builder("reactor_mox_quad")
                .inputItems(GTOItems.REACTOR_MOX_DUAL, 2)
                .inputItems(rodLong, Steel, 4)
                .outputItems(GTOItems.REACTOR_MOX_QUAD)
                .EUt(VA[IV])
                .duration(2400)
                .addCondition(new VacuumCondition(4))
                .save();

        LASER_WELDER_RECIPES.builder("reactor_naquadah_dual")
                .inputItems(GTOItems.REACTOR_NAQUADAH_SIMPLE, 2)
                .outputItems(GTOItems.REACTOR_NAQUADAH_DUAL)
                .EUt(VA[LuV])
                .duration(2400)
                .addCondition(new VacuumCondition(4))
                .save();

        LASER_WELDER_RECIPES.builder("reactor_naquadah_quad")
                .inputItems(GTOItems.REACTOR_NAQUADAH_DUAL, 2)
                .inputItems(rodLong, TungstenCarbide, 4)
                .outputItems(GTOItems.REACTOR_NAQUADAH_QUAD)
                .EUt(VA[LuV])
                .duration(2400)
                .addCondition(RestrictedMachineCondition.multiblock())
                .save();

        LASER_WELDER_RECIPES.builder("magnetic_samarium_connecting_rod")
                .inputItems(TagPrefix.block, GTMaterials.SamariumMagnetic, 16)
                .inputItems(GTOTagPrefix.NANITES, GTMaterials.Neutronium)
                .outputItems(GTOTagPrefix.CONNECTING_ROD, GTMaterials.SamariumMagnetic)
                .circuitMeta(6)
                .EUt(524299)
                .duration(900)
                .save();
        LASER_WELDER_RECIPES.builder("magnetic_samarium_c3onnecting_rod")
                .inputItems(TagPrefix.block, GTOMaterials.EnergeticNetherite, 16)
                .inputItems(GTOTagPrefix.NANITES, GTOMaterials.PhotonicKristallite)
                .outputItems(GTOTagPrefix.CONNECTING_ROD, GTOMaterials.EnergeticNetherite)
                .circuitMeta(6)
                .EUt(1124299)
                .duration(900)
                .save();
        LASER_WELDER_RECIPES.builder("attuned_tengam_connecting_rod")
                .inputItems(TagPrefix.block, GTOMaterials.AttunedTengam, 16)
                .inputItems(GTOTagPrefix.NANITES, GTOMaterials.Infuscolium)
                .outputItems(GTOTagPrefix.CONNECTING_ROD, GTOMaterials.AttunedTengam)
                .circuitMeta(6)
                .EUt(4444299)
                .duration(900)
                .save();
        LASER_WELDER_RECIPES.builder("magmatter_connecting_rod")
                .inputItems(TagPrefix.block, GTOMaterials.Magmatter, 16)
                .inputItems(GTOTagPrefix.NANITES, GTOMaterials.Eternity)
                .outputItems(GTOTagPrefix.CONNECTING_ROD, GTOMaterials.Magmatter)
                .circuitMeta(6)
                .EUt(4444299777L)
                .duration(900)
                .save();
    }
}
