package com.gtocore.data.recipe.gtm;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.config.GTORules;

import com.gtolib.GTOCore;
import com.gtolib.api.data.storage.CompartmentStorageRegistration;
import com.gtolib.api.data.storage.SuperStorageRegistration;
import com.gtolib.api.data.storage.SuperStorageReplacerRegistration;
import com.gtolib.api.data.storage.WoodenSuperStorageRegistration;
import com.gtolib.utils.TagUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.gto.registrate.util.entry.ItemEntry;

import static com.gregtechceu.gtceu.api.GTValues.LV;

public class SuperStorageAdditionalRecipe {

    private static final boolean easy = (GTORules.MOD_RECIPE_TIER.isEasy());

    public static void init() {
        for (int tier = GTValues.ULV; tier <= GTValues.MAX; tier++) {
            superChestReplacer(SuperStorageReplacerRegistration.CHESTS[tier], CompartmentStorageRegistration.NORMAL_CHESTS[tier]);
            superChestReplacer(SuperStorageReplacerRegistration.TANKS[tier], CompartmentStorageRegistration.NORMAL_TANKS[tier]);

        }

        for (int layout = 0; layout < 2; layout++) {
            for (int tier = 0; tier < GTValues.TIER_COUNT; tier++) {
                if (CompartmentStorageRegistration.CHESTS[layout][tier] != null) compartmentSuperChest(CompartmentStorageRegistration.CHESTS[layout][tier], layout == 0 ? CompartmentStorageRegistration.NORMAL_CHESTS[tier] : CompartmentStorageRegistration.CHESTS[0][tier], false);
                if (CompartmentStorageRegistration.TANKS[layout][tier] != null) compartmentSuperChest(CompartmentStorageRegistration.TANKS[layout][tier], layout == 0 ? CompartmentStorageRegistration.NORMAL_TANKS[tier] : CompartmentStorageRegistration.TANKS[0][tier], false);
            }
        }
        compartmentSuperChest(CompartmentStorageRegistration.WOODEN_CHEST_1X2, WoodenSuperStorageRegistration.CHEST, true);
        compartmentSuperChest(CompartmentStorageRegistration.WOODEN_TANK_1X2, WoodenSuperStorageRegistration.TANK, true);
        compartmentSuperChest(CompartmentStorageRegistration.WOODEN_CHEST_2X2, CompartmentStorageRegistration.WOODEN_CHEST_1X2, true);
        compartmentSuperChest(CompartmentStorageRegistration.WOODEN_TANK_2X2, CompartmentStorageRegistration.WOODEN_TANK_1X2, true);

        if (easy) {
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id("wod_super_chest"), WoodenSuperStorageRegistration.CHEST.asStack(),
                    "PPP", "PCP", "PPP", 'P', ItemTags.PLANKS, 'C', Items.CHEST);
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id("wod_super_tank"), WoodenSuperStorageRegistration.TANK.asStack(),
                    "PPP", "PGP", "PBP", 'P', ItemTags.PLANKS, 'G', Items.GLASS.asItem(), 'B', Items.BUCKET.asItem());
        } else {
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id("wd_super_chest"), WoodenSuperStorageRegistration.CHEST.asStack(),
                    "PIP", "ICI", "PIP", 'P', ItemTags.PLANKS, 'I', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron), 'C', GTMachines.WOODEN_CRATE.asItem());
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id("wd_super_tank"), WoodenSuperStorageRegistration.TANK.asStack(),
                    "PIP", "IBI", "PIG", 'P', ItemTags.PLANKS, 'I', new MaterialEntry(TagPrefix.plate, GTMaterials.Iron), 'B', GTMachines.WOODEN_DRUM.asItem(), 'G', new MaterialEntry(TagPrefix.pipeHugeFluid, GTMaterials.Lead));
        }
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("ulv_super_tank"), GTMachines.SUPER_TANK[GTValues.ULV].asItem(),
                "ABA",
                "BCB",
                "ADA",
                'A', CustomTags.ULV_CIRCUITS, 'B', new MaterialEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'C', GTOBlocks.HERMETIC_CASING_ULV.asItem(), 'D', GTOItems.ULV_ELECTRIC_PUMP.asItem());
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("ulv_super_chest"), GTMachines.SUPER_CHEST[GTValues.ULV].asItem(),
                "ABA",
                "BCB",
                "ABA",
                'A', CustomTags.ULV_CIRCUITS, 'B', new MaterialEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'C', GTMachines.BRONZE_CRATE.asItem());
        if (easy) {
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id("super_chest_controller"), SuperStorageRegistration.CONTROLLER.asItem(),
                    "ABA",
                    "C C",
                    "ABA",
                    'A', new MaterialEntry(TagPrefix.ingot, GTMaterials.Iron), 'B', new MaterialEntry(TagPrefix.block, GTMaterials.NetherQuartz), 'C', Items.COMPARATOR);

            VanillaRecipeHelper.addShapedRecipe(GTOCore.id("super_chest_controller_extension"), SuperStorageRegistration.EXTENSION.asItem(),
                    "ABA",
                    "C C",
                    "ABA",
                    'A', new MaterialEntry(TagPrefix.ingot, GTMaterials.Iron), 'B', new MaterialEntry(TagPrefix.block, GTMaterials.NetherQuartz), 'C', Items.REPEATER);
        } else {
            VanillaRecipeHelper.addShapedRecipe(true, "super_chest_controller", SuperStorageRegistration.CONTROLLER.asItem(),
                    "ABA",
                    "CDC",
                    "EBE",
                    'A', GTItems.CONVEYOR_MODULE_LV.asItem(), 'B', new MaterialEntry(TagPrefix.rod, GTOMaterials.PulsatingAlloy), 'C', GTItems.ROBOT_ARM_LV.asItem(), 'D', GTMachines.SUPER_CHEST[LV].asItem(), 'E', GTItems.FLUID_REGULATOR_LV.asItem());
            VanillaRecipeHelper.addShapedRecipe(true, "super_chest_controller_extension", SuperStorageRegistration.EXTENSION.asItem(),
                    "ABA",
                    "CDC",
                    "EFE",
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.EnderPearl), 'B', GTItems.FLUID_REGULATOR_MV.asItem(), 'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Aluminium), 'D', SuperStorageRegistration.CONTROLLER.asItem(), 'E', CustomTags.MV_CIRCUITS, 'F', GTItems.ROBOT_ARM_MV.asItem());
        }
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("quantum_chest_configurator"), SuperStorageRegistration.TOOL.asItem(),
                " AB",
                "AC ",
                "AA ",
                'A', new MaterialEntry(TagPrefix.plate, GTMaterials.WroughtIron), 'B', new MaterialEntry(TagPrefix.dust, GTMaterials.Redstone), 'C', easy ? Items.PAPER.asItem() : new MaterialEntry(TagPrefix.gem, GTMaterials.EnderPearl));

        VanillaRecipeHelper.addShapedRecipe(GTOCore.id("drawer_storage"), MultiBlockG.DRAWER_STORAGE.asItem(),
                "PDP",
                "DCD",
                "PDP",
                'P', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Steel), 'D', GTMachines.SUPER_CHEST[GTValues.ULV].asItem(), 'C', GTMachines.BRONZE_CRATE.asItem());
    }

    private static void compartmentSuperChest(MachineDefinition result, MachineDefinition source, boolean isWood) {
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id(result.getId().getPath()), result.asStack(), " G ", " C ", " G ", 'G', isWood ? Items.STICK : Items.GLASS_PANE, 'C', StrictNBTIngredient.of(source.asStack()));
    }

    private static void superChestReplacer(ItemEntry<ComponentItem> item, MachineDefinition machine) {
        if (item != null) VanillaRecipeHelper.addShapelessRecipe(item.getId(), item.asStack(), StrictNBTIngredient.of(machine.asStack()),
                TagUtils.createTGItemTag("ingots"));
    }
}
