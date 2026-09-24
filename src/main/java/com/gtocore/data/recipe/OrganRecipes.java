package com.gtocore.data.recipe;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.common.item.misc.TierOrganItem;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class OrganRecipes {

    private OrganRecipes() {}

    public static void init() {
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("organ_modifier"), GTOOrganItems.ORGAN_MODIFIER.asStack(),
                " B ", "CDC", "EFF",
                'B', GTItems.VOLTAGE_COIL_MV.asStack(),
                'C', GTItems.ROBOT_ARM_MV.asStack(),
                'D', new ItemStack(Items.CRAFTING_TABLE),
                'E', new ItemStack(Items.SLIME_BALL),
                'F', CustomTags.MV_CIRCUITS);
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("mana_steel_wing"), GTOOrganItems.MANA_STEEL_WING.asStack(),
                "ABA", "CDC", "ABA",
                'A', new MaterialEntry(TagPrefix.plateDouble, GTOMaterials.Manasteel),
                'B', new MaterialEntry(TagPrefix.foil, GTOMaterials.Manasteel),
                'C', new MaterialEntry(GTOTagPrefix.FIELD_GENERATOR_CASING, GTMaterials.Steel),
                'D', GTOItems.COLORFUL_MYSTICAL_FLOWER.asItem());
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("fairy_wing"), GTOOrganItems.FAIRY_WING.asStack(),
                "ABA", "ACA", "DDD",
                'A', new MaterialEntry(TagPrefix.foil, GTOMaterials.Herbs),
                'B', new MaterialEntry(TagPrefix.plateDouble, GTOMaterials.Herbs),
                'C', GTItems.FIELD_GENERATOR_MV.asStack(),
                'D', GTOItems.COLORFUL_MYSTICAL_FLOWER.asItem());
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("mechanical_wing"), GTOOrganItems.MECHANICAL_WING.asStack(),
                "ABA", "CBC", "DED",
                'A', GTItems.BATTERY_EV_VANADIUM.asStack(),
                'B', new MaterialEntry(TagPrefix.foil, GTMaterials.Titanium),
                'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Titanium),
                'D', GTItems.FIELD_GENERATOR_EV.asStack(),
                'E', new MaterialEntry(TagPrefix.ingot, GTMaterials.Titanium));

        // 1~4 级身体器官：器官等级 n 用电压 2n（MV / EV / LuV / UHV）的部件，场发生器低一级
        for (int organTier = 1; organTier <= TierOrganItem.MAX_TIER; organTier++) {
            int tier = organTier << 1;
            // 1 级是 MV，还没有自动化，用量减半
            int shift = organTier == 1 ? GTOCore.difficulty - 1 : GTOCore.difficulty;
            var motor = (Item) GTCraftingComponents.MOTOR.get(tier);
            var conveyor = (Item) GTCraftingComponents.CONVEYOR.get(tier);
            var pump = (Item) GTCraftingComponents.PUMP.get(tier);
            var piston = (Item) GTCraftingComponents.PISTON.get(tier);
            var robotArm = (Item) GTCraftingComponents.ROBOT_ARM.get(tier);
            var emitter = (Item) GTCraftingComponents.EMITTER.get(tier);
            var sensor = (Item) GTCraftingComponents.SENSOR.get(tier);
            var fieldGenerator = (Item) GTCraftingComponents.FIELD_GENERATOR.get(tier - 1);
            @SuppressWarnings("unchecked")
            var circuit = (TagKey<Item>) GTCraftingComponents.CIRCUIT.get(tier);
            long eut = GTValues.VA[tier];

            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_right_arm_tier_" + tier)
                    .inputItems(motor, 2 << shift)
                    .inputItems(robotArm, 4 << shift)
                    .inputItems(sensor, 2 << shift)
                    .inputItems(fieldGenerator, 1 << shift)
                    .inputItems(circuit, 4 << shift)
                    .inputItems(piston, 1 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(1).save();
            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_left_arm_tier_" + tier)
                    .inputItems(motor, 2 << shift)
                    .inputItems(robotArm, 4 << shift)
                    .inputItems(sensor, 2 << shift)
                    .inputItems(fieldGenerator, 1 << shift)
                    .inputItems(circuit, 4 << shift)
                    .inputItems(piston, 1 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(2).save();
            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_right_leg_tier_" + tier)
                    .inputItems(motor, 4 << shift)
                    .inputItems(conveyor, 2 << shift)
                    .inputItems(robotArm, 2 << shift)
                    .inputItems(sensor, 2 << shift)
                    .inputItems(fieldGenerator, 1 << shift)
                    .inputItems(circuit, 4 << shift)
                    .inputItems(piston, 1 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(3).save();
            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_left_leg_tier_" + tier)
                    .inputItems(motor, 4 << shift)
                    .inputItems(conveyor, 2 << shift)
                    .inputItems(robotArm, 2 << shift)
                    .inputItems(sensor, 2 << shift)
                    .inputItems(fieldGenerator, 1 << shift)
                    .inputItems(circuit, 4 << shift)
                    .inputItems(piston, 1 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(4).save();
            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_heart_tier_" + tier)
                    .inputItems(motor, 2 << shift)
                    .inputItems(emitter, 2 << shift)
                    .inputItems(sensor, 2 << shift)
                    .inputItems(fieldGenerator, 1 << shift)
                    .inputItems(circuit, 4 << shift)
                    .inputItems(piston, 2 << shift)
                    .inputItems(pump, 2 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.HEART, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(5).save();
            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_eyes_tier_" + tier)
                    .inputItems(motor, 1 << shift)
                    .inputItems(emitter, 1 << shift)
                    .inputItems(sensor, 2 << shift)
                    .inputItems(circuit, 4 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.EYE, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(6).save();
            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_lungs_tier_" + tier)
                    .inputItems(motor, 2 << shift)
                    .inputItems(robotArm, 4 << shift)
                    .inputItems(emitter, 1 << shift)
                    .inputItems(sensor, 1 << shift)
                    .inputItems(piston, 2 << shift)
                    .inputItems(pump, 2 << shift)
                    .inputItems(fieldGenerator, 1 << shift)
                    .inputItems(circuit, 4 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.LUNG, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(7).save();
            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_liver_tier_" + tier)
                    .inputItems(emitter, 1 << shift)
                    .inputItems(sensor, 2 << shift)
                    .inputItems(fieldGenerator, 2 << shift)
                    .inputItems(pump, 2 << shift)
                    .inputItems(circuit, 4 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.LIVER, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(8).save();
            GTORecipeTypes.ASSEMBLER_RECIPES.builder("organ_spine_tier_" + tier)
                    .inputItems(robotArm, 4 << shift)
                    .inputItems(emitter, 1 << shift)
                    .inputItems(circuit, 1 << shift)
                    .outputItems(GTOOrganItems.tierOrgan(OrganType.SPINE, organTier).asStack())
                    .EUt(eut).duration(1200).circuitMeta(9).save();
        }
    }
}
