package com.gtocore.integration.emi;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.integration.Mods;
import com.gtocore.integration.biomeswevegone.BYGWoodTypes;

import com.gtolib.utils.RegistriesUtils;
import com.gtolib.utils.register.BlockRegisterUtils;

import net.minecraft.world.item.Item;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Set;

public class HiddenItems {

    private static final ReferenceOpenHashSet<Item> HIDDEN_ITEMS = new ReferenceOpenHashSet<>();
    private static final ReferenceOpenHashSet<Item> DEPRECATED_ITEMS = new ReferenceOpenHashSet<>();

    public static void registerHiddenItems(Set<Item> c) {
        hide(BlockRegisterUtils.REACTOR_CORE.asItem());
        deprecate(ModItems.WHEAT_DOUGH.get());
        deprecate(RegistriesUtils.getItem("morered:red_alloy_ingot"));
        deprecate(EPPItemAndBlock.CIRCUIT_CUTTER.asItem());
        deprecate(EPPItemAndBlock.SILICON_BLOCK.asItem());
        deprecate(RegistriesUtils.getItem("ad_astra:fuel_refinery"));
        deprecate(RegistriesUtils.getItem("ad_astra:cryo_freezer"));
        deprecate(RegistriesUtils.getItem("ad_astra:compressor"));
        deprecate(RegistriesUtils.getItem("ad_astra:etrionic_blast_furnace"));
        deprecate(RegistriesUtils.getItem("guideme:guide"));
        deprecate(RegistriesUtils.getItem("ars_nouveau:ritual_flight"));
        deprecate(GTOBlocks.LAURERIL_JAR.asItem());
        deprecate(RegistriesUtils.getItem("extrabotany:mana_reader"));

        if (Mods.EFFORTLESS.isLoaded()) {
            deprecate(RegistriesUtils.getItem("effortlessbuilding:randomizer_bag"));
            deprecate(RegistriesUtils.getItem("effortlessbuilding:golden_randomizer_bag"));
            deprecate(RegistriesUtils.getItem("effortlessbuilding:diamond_randomizer_bag"));
        }

        if (Mods.MYTHICBOTANY.isLoaded()) {
            deprecate(RegistriesUtils.getItem("mythicbotany:feysythia"));
            deprecate(RegistriesUtils.getItem("mythicbotany:feysythia_floating"));
            deprecate(RegistriesUtils.getItem("mythicbotany:raw_elementium"));
            deprecate(RegistriesUtils.getItem("mythicbotany:raw_elementium_block"));
            deprecate(RegistriesUtils.getItem("mythicbotany:elementium_ore"));
        }

        if (Mods.SOPHISTICATEDBACKPACKS.isLoaded()) {
            hide(RegistriesUtils.getItem("sophisticatedbackpacks:tool_swapper_upgrade"));
            hide(RegistriesUtils.getItem("sophisticatedbackpacks:advanced_tool_swapper_upgrade"));
        }

        if (Mods.BIOMESWEVEGONE.isLoaded()) {
            for (String woodName : BYGWoodTypes.WOOD_NAMES) {
                deprecate(RegistriesUtils.getItem("biomeswevegone:" + woodName + "_bookshelf"));
                deprecate(RegistriesUtils.getItem("biomeswevegone:" + woodName + "_crafting_table"));
            }
        }
        c.addAll(HIDDEN_ITEMS);
    }

    private static void hide(Item item) {
        HIDDEN_ITEMS.add(item);
    }

    private static void deprecate(Item item) {
        hide(item);
        DEPRECATED_ITEMS.add(item);
    }

    public static boolean isItemHidden(Item item) {
        return HIDDEN_ITEMS.contains(item);
    }

    public static boolean isItemDeprecated(Item item) {
        return DEPRECATED_ITEMS.contains(item);
    }
}
