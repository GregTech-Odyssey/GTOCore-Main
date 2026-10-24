package com.gtocore.data.loot;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;

import com.gtolib.api.annotation.Register;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.loot.ChestGenHooks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;

@Register
public final class DungeonLoot {

    private static final String WDA_LOOT_POOL = "gtocore_dungeon";

    public static void init() {
        if (ConfigHolder.INSTANCE.worldgen.addLoot || ConfigHolder.INSTANCE.worldgen.increaseDungeonLoot) {
            MinecraftForge.EVENT_BUS.addListener(DungeonLoot::onLootTableLoad);
        }
        ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_TOOLSMITH, GTItems.FLUID_CELL_GLASS_VIAL.asStack(), 1, 2, 1);
        ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_TOOLSMITH, GTItems.TOOL_LIGHTER_INVAR.asStack(), 1, 1, 4);
        ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_TOOLSMITH, GTBlocks.BRONZE_BRICKS_HULL.asStack(), 1, 4, 2);
        ChestGenHooks.addItem(BuiltInLootTables.VILLAGE_MASON, GTBlocks.STEEL_BRICKS_HULL.asStack(), 1, 2, 2);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.ULV].asStack(), 1, 4, 5);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.LV].asStack(), 1, 3, 4);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.MV].asStack(), 1, 2, 3);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.LARGE_MYSTERIOUS_BOOST_DRINK[GTValues.ULV].asStack(), 1, 4, 3);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.LARGE_MYSTERIOUS_BOOST_DRINK[GTValues.LV].asStack(), 1, 3, 2);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.LARGE_MYSTERIOUS_BOOST_DRINK[GTValues.MV].asStack(), 1, 2, 1);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.RUNE1_REWARD_BAG.asStack(), 1, 1, 3);

        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.RANDOM_POSITIVE_FOOD_1.asStack(), 1, 2, 6);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.RANDOM_POSITIVE_FOOD_2.asStack(), 1, 2, 5);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.RANDOM_POSITIVE_FOOD_3.asStack(), 1, 1, 4);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.RANDOM_POSITIVE_FOOD_4.asStack(), 1, 1, 3);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.CHOPPER_POPPER_FISH_HEAD_1.asStack(), 1, 1, 3);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.CHOPPER_POPPER_FISH_HEAD_2.asStack(), 1, 1, 3);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.CHOPPER_POPPER_FISH_HEAD_3.asStack(), 1, 1, 3);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.CHOPPER_POPPER_FISH_HEAD_4.asStack(), 1, 1, 3);
        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, GTOItems.CHOPPER_POPPER_FISH_HEAD_5.asStack(), 1, 1, 3);

        ChestGenHooks.addItem(BuiltInLootTables.SIMPLE_DUNGEON, ChemicalHelper.get(GTOTagPrefix.COIN, GTMaterials.Copper), 1, 12, 6);
        ChestGenHooks.addItem(BuiltInLootTables.IGLOO_CHEST, ChemicalHelper.get(GTOTagPrefix.COIN, GTMaterials.Copper), 1, 8, 3);
        ChestGenHooks.addItem(BuiltInLootTables.SHIPWRECK_TREASURE, ChemicalHelper.get(GTOTagPrefix.COIN, GTMaterials.Copper), 1, 8, 3);

        ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE, GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.ULV].asStack(), 1, 4, 4);
        ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE, GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.LV].asStack(), 1, 3, 3);
        ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE, GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.MV].asStack(), 1, 2, 2);
        ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE, GTOItems.LARGE_MYSTERIOUS_BOOST_DRINK[GTValues.ULV].asStack(), 1, 4, 2);
        ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE, GTOItems.LARGE_MYSTERIOUS_BOOST_DRINK[GTValues.LV].asStack(), 1, 3, 2);
        ChestGenHooks.addItem(BuiltInLootTables.JUNGLE_TEMPLE, GTOItems.LARGE_MYSTERIOUS_BOOST_DRINK[GTValues.MV].asStack(), 1, 2, 1);
    }

    private static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation id = event.getName();
        if (!"dungeons_arise".equals(id.getNamespace()) || !id.getPath().startsWith("chests/")) {
            return;
        }
        LootTable table = event.getTable();
        if (table.getPool(WDA_LOOT_POOL) != null) {
            return;
        }
        boolean treasure = id.getPath().contains("treasure") || id.getPath().contains("elite") || id.getPath().contains("top");
        LootPool.Builder pool = LootPool.lootPool()
                .name(WDA_LOOT_POOL)
                .setRolls(ConstantValue.exactly(1))
                .add(EmptyLootItem.emptyItem().setWeight(treasure ? 80 : 92))
                .add(LootItem.lootTableItem(GTOItems.MYSTERIOUS_BOOST_DRINK[treasure ? GTValues.LV : GTValues.ULV].get())
                        .setWeight(treasure ? 10 : 4)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, treasure ? 3 : 2))))
                .add(LootItem.lootTableItem(ChemicalHelper.get(GTOTagPrefix.COIN, GTMaterials.Copper).getItem())
                        .setWeight(8)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, treasure ? 12 : 6))));
        table.addPool(pool.build());
    }
}
