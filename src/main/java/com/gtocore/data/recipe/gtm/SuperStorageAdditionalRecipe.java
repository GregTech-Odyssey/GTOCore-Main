package com.gtocore.data.recipe.gtm;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.SuperStorageUpgrades;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.config.GTORules;

import com.gtolib.GTOCore;
import com.gtolib.api.data.storage.CompartmentStorageRegistration;
import com.gtolib.api.data.storage.SuperStorageRegistration;
import com.gtolib.api.data.storage.SuperStorageReplacerRegistration;
import com.gtolib.api.data.storage.WoodenSuperStorageRegistration;
import com.gtolib.utils.RegistriesUtils;
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
                if (CompartmentStorageRegistration.CHESTS[layout][tier] != null) compartmentSuperChest(CompartmentStorageRegistration.CHESTS[layout][tier], layout == 0 ? CompartmentStorageRegistration.NORMAL_CHESTS[tier] : CompartmentStorageRegistration.CHESTS[0][tier]);
                if (CompartmentStorageRegistration.TANKS[layout][tier] != null) compartmentSuperChest(CompartmentStorageRegistration.TANKS[layout][tier], layout == 0 ? CompartmentStorageRegistration.NORMAL_TANKS[tier] : CompartmentStorageRegistration.TANKS[0][tier]);
            }
            for (int wood = 0; wood < CompartmentStorageRegistration.WOODEN_CHESTS[layout].length; wood++) {
                compartmentSuperChest(CompartmentStorageRegistration.WOODEN_CHESTS[layout][wood], layout == 0 ? WoodenSuperStorageRegistration.CHESTS[wood] : CompartmentStorageRegistration.WOODEN_CHESTS[0][wood]);
                compartmentSuperChest(CompartmentStorageRegistration.WOODEN_TANKS[layout][wood], layout == 0 ? WoodenSuperStorageRegistration.TANKS[wood] : CompartmentStorageRegistration.WOODEN_TANKS[0][wood]);
            }
        }
        for (var wood : WoodenSuperStorageRegistration.Wood.values()) {
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id(wood.getId() + "_super_chest"), WoodenSuperStorageRegistration.CHESTS[wood.ordinal()].asStack(),
                    "PPP", "PCP", "PPP", 'P', wood.getPlanks(), 'C', Items.CHEST);
            VanillaRecipeHelper.addShapedRecipe(GTOCore.id(wood.getId() + "_super_tank"), WoodenSuperStorageRegistration.TANKS[wood.ordinal()].asStack(),
                    "PPP", "PGP", "PBP", 'P', wood.getPlanks(), 'G', easy ? Items.GLASS.asItem() : GTMachines.STEEL_DRUM.asItem(), 'B', easy ? Items.BUCKET.asItem() : new MaterialEntry(TagPrefix.pipeLargeFluid, GTMaterials.Steel));
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

            VanillaRecipeHelper.addShapedRecipe(GTOCore.id("super_chest_controller"), SuperStorageRegistration.EXTENSION.asItem(),
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
                    'A', new MaterialEntry(TagPrefix.plate, GTMaterials.EnderPearl), 'B', GTItems.FLUID_REGULATOR_MV.asItem(), 'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Aluminium), 'D', RegistriesUtils.getItem("gtocore:super_chest_controller"), 'E', CustomTags.MV_CIRCUITS, 'F', GTItems.ROBOT_ARM_MV.asItem());
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

        switch (GTORules.MOD_RECIPE_TIER.get()) {
            case EXPERT -> {
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("bronze_super_storage_upgrade"), SuperStorageUpgrades.BRONZE.asItem(),
                        "ABC",
                        "ADC",
                        "EEE",
                        'A', GTMachines.BRONZE_CRATE.asItem(), 'B', CustomTags.ULV_CIRCUITS, 'C', GTMachines.BRONZE_DRUM.asItem(), 'D', new MaterialEntry(TagPrefix.gear, GTMaterials.Bronze), 'E', new MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Bronze));

                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("bronzee_super_storage_upgrade"), SuperStorageUpgrades.STEEL.asItem(),
                        "ABC",
                        "ADC",
                        "EEE",
                        'A', GTMachines.STEEL_CRATE.asItem(), 'B', CustomTags.LV_CIRCUITS, 'C', GTMachines.STEEL_DRUM.asItem(), 'D', new MaterialEntry(TagPrefix.gear, GTMaterials.Steel), 'E', new MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Steel));

                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("steel_super_storage_upgrade"), SuperStorageUpgrades.ALUMINIUM.asItem(),
                        "ABC",
                        "ADC",
                        "EEE",
                        'A', GTMachines.ALUMINIUM_CRATE.asItem(), 'B', CustomTags.MV_CIRCUITS, 'C', GTMachines.ALUMINIUM_DRUM.asItem(), 'D', new MaterialEntry(TagPrefix.gear, GTMaterials.Aluminium), 'E', new MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Aluminium));

                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("steel_super_storage_upgrade"), SuperStorageUpgrades.TITANIUM.asItem(),
                        "ABC",
                        "ADC",
                        "EEE",
                        'A', GTMachines.TITANIUM_CRATE.asItem(), 'B', CustomTags.HV_CIRCUITS, 'C', GTMachines.TITANIUM_DRUM.asItem(), 'D', new MaterialEntry(TagPrefix.gear, GTMaterials.Titanium), 'E', new MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.Titanium));

                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("steel_super_storage_upgrade"), SuperStorageUpgrades.TUNGSTEN_STEEL.asItem(),
                        "ABC",
                        "ADC",
                        "EEE",
                        'A', GTMachines.TUNGSTENSTEEL_CRATE.asItem(), 'B', CustomTags.EV_CIRCUITS, 'C', GTMachines.TUNGSTENSTEEL_DRUM.asItem(), 'D', new MaterialEntry(TagPrefix.gear, GTMaterials.TungstenSteel), 'E', new MaterialEntry(TagPrefix.pipeNonupleFluid, GTMaterials.TungstenSteel));
            }
            case NORMAL -> {

                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("bronze_super_storage_upgrade"), SuperStorageUpgrades.BRONZE.asItem(),
                        "ABA",
                        "A A",
                        "CDC",
                        'A', new MaterialEntry(TagPrefix.foil, GTMaterials.Bronze), 'B', GTMachines.BRONZE_CRATE.asItem(), 'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Bronze), 'D', GTMachines.BRONZE_DRUM.asItem());
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("bronzee_super_storage_upgrade"), SuperStorageUpgrades.STEEL.asItem(),
                        "ABA",
                        "A A",
                        "CDC",
                        'A', new MaterialEntry(TagPrefix.foil, GTMaterials.Steel), 'B', GTMachines.STEEL_CRATE.asItem(), 'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Steel), 'D', GTMachines.STEEL_DRUM.asItem());
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("steel_super_storage_upgrade"), SuperStorageUpgrades.ALUMINIUM.asItem(),
                        "ABA",
                        "A A",
                        "CDC",
                        'A', new MaterialEntry(TagPrefix.foil, GTMaterials.Aluminium), 'B', GTMachines.ALUMINIUM_CRATE.asItem(), 'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Aluminium), 'D', GTMachines.ALUMINIUM_DRUM.asItem());
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("titanium_super_storage_upgrade"), SuperStorageUpgrades.TITANIUM.asItem(),
                        "ABA",
                        "A A",
                        "CDC",
                        'A', new MaterialEntry(TagPrefix.foil, GTMaterials.Titanium), 'B', GTMachines.TITANIUM_CRATE.asItem(), 'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.Titanium), 'D', GTMachines.TITANIUM_DRUM.asItem());
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("tungstensteel_super_storage_upgrade"), SuperStorageUpgrades.TUNGSTEN_STEEL.asItem(),
                        "ABA",
                        "A A",
                        "CDC",
                        'A', new MaterialEntry(TagPrefix.foil, GTMaterials.TungstenSteel), 'B', GTMachines.TUNGSTENSTEEL_CRATE.asItem(), 'C', new MaterialEntry(TagPrefix.plateDouble, GTMaterials.TungstenSteel), 'D', GTMachines.TUNGSTENSTEEL_DRUM.asItem());
            }
            case EASY -> {

                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("bronze_super_storage_upgrade"), RegistriesUtils.getItem("gtocore:bronze_super_storage_upgrade"),
                        "A A",
                        " B ",
                        "A A",
                        'A', new MaterialEntry(TagPrefix.plate, GTMaterials.BismuthBronze), 'B', new MaterialEntry(TagPrefix.gear, GTMaterials.Bronze));
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("bronzee_super_storage_upgrade"), RegistriesUtils.getItem("gtocore:bronzee_super_storage_upgrade"),
                        "A A",
                        " B ",
                        "A A",
                        'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Steel), 'B', new MaterialEntry(TagPrefix.gear, GTMaterials.Steel));
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("steel_super_storage_upgrade"), RegistriesUtils.getItem("gtocore:steel_super_storage_upgrade"),
                        "A A",
                        " B ",
                        "A A",
                        'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Aluminium), 'B', new MaterialEntry(TagPrefix.gear, GTMaterials.Aluminium));
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("titanium_super_storage_upgrade"), RegistriesUtils.getItem("gtocore:titanium_super_storage_upgrade"),
                        "A A",
                        " B ",
                        "A A",
                        'A', new MaterialEntry(TagPrefix.plate, GTMaterials.Titanium), 'B', new MaterialEntry(TagPrefix.gear, GTMaterials.Titanium));
                VanillaRecipeHelper.addShapedRecipe(GTOCore.id("tungstensteel_super_storage_upgrade"), RegistriesUtils.getItem("gtocore:tungstensteel_super_storage_upgrade"),
                        "A A",
                        " B ",
                        "A A",
                        'A', new MaterialEntry(TagPrefix.plate, GTMaterials.TungstenSteel), 'B', new MaterialEntry(TagPrefix.gear, GTMaterials.TungstenSteel));
            }
        }
    }

    private static void compartmentSuperChest(MachineDefinition result, MachineDefinition source) {
        VanillaRecipeHelper.addShapedRecipe(GTOCore.id(result.getId().getPath()), result.asStack(), " G ", " C ", " G ", 'G', Items.GLASS_PANE, 'C', StrictNBTIngredient.of(source.asStack()));
    }

    private static void superChestReplacer(ItemEntry<ComponentItem> item, MachineDefinition machine) {
        if (item != null) VanillaRecipeHelper.addShapelessRecipe(item.getId(), item.asStack(), StrictNBTIngredient.of(machine.asStack()),
                TagUtils.createTGItemTag("ingots"));
    }
}
