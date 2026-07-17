package com.gtocore.integration.emi;

import com.gtocore.integration.Mods;
import com.gtocore.integration.biomeswevegone.BYGWoodTypes;

import com.gtolib.utils.RegistriesUtils;
import com.gtolib.utils.register.BlockRegisterUtils;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Item;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Set;

public class HiddenItems {

    static final Set<Item> HIDDEN_ITEMS = new ReferenceOpenHashSet<>();

    public static void registerHiddenItems(Set<Item> c) {
        HIDDEN_ITEMS.add(BlockRegisterUtils.REACTOR_CORE.asItem());
        HIDDEN_ITEMS.add(ModItems.WHEAT_DOUGH.get());
        HIDDEN_ITEMS.add(RegistriesUtils.getItem("morered:red_alloy_ingot"));
        HIDDEN_ITEMS.add(EPPItemAndBlock.CIRCUIT_CUTTER.asItem());
        HIDDEN_ITEMS.add(EPPItemAndBlock.SILICON_BLOCK.asItem());
        HIDDEN_ITEMS.add(RegistriesUtils.getItem("ad_astra:fuel_refinery"));
        HIDDEN_ITEMS.add(RegistriesUtils.getItem("ad_astra:cryo_freezer"));
        HIDDEN_ITEMS.add(RegistriesUtils.getItem("ad_astra:compressor"));
        HIDDEN_ITEMS.add(RegistriesUtils.getItem("ad_astra:etrionic_blast_furnace"));
        HIDDEN_ITEMS.add(GTMultiMachines.CHARCOAL_PILE_IGNITER.asItem());
        HIDDEN_ITEMS.add(GTBlocks.BRITTLE_CHARCOAL.asItem());

        if (Mods.EFFORTLESS.isLoaded()) {
            HIDDEN_ITEMS.add(RegistriesUtils.getItem("effortlessbuilding:randomizer_bag"));
            HIDDEN_ITEMS.add(RegistriesUtils.getItem("effortlessbuilding:golden_randomizer_bag"));
            HIDDEN_ITEMS.add(RegistriesUtils.getItem("effortlessbuilding:diamond_randomizer_bag"));
        }

        if (Mods.MYTHICBOTANY.isLoaded()) {
            HIDDEN_ITEMS.add(RegistriesUtils.getItem("mythicbotany:feysythia"));
            HIDDEN_ITEMS.add(RegistriesUtils.getItem("mythicbotany:feysythia_floating"));
            HIDDEN_ITEMS.add(RegistriesUtils.getItem("mythicbotany:raw_elementium"));
            HIDDEN_ITEMS.add(RegistriesUtils.getItem("mythicbotany:raw_elementium_block"));
            HIDDEN_ITEMS.add(RegistriesUtils.getItem("mythicbotany:elementium_ore"));
        }

        if (Mods.BIOMESWEVEGONE.isLoaded()) {
            for (String woodName : BYGWoodTypes.WOOD_NAMES) {
                HIDDEN_ITEMS.add(RegistriesUtils.getItem("biomeswevegone:" + woodName + "_bookshelf"));
                HIDDEN_ITEMS.add(RegistriesUtils.getItem("biomeswevegone:" + woodName + "_crafting_table"));
            }
        }
        c.addAll(HIDDEN_ITEMS);
    }

    public static boolean isItemHidden(Item item) {
        return HIDDEN_ITEMS.contains(item);
    }
}
