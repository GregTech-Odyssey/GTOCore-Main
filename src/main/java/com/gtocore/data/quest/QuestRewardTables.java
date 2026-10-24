package com.gtocore.data.quest;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;

import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.reward.RewardTable;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import commoble.jumbofurnace.JumboFurnace;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.ench.Ench;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

public final class QuestRewardTables {

    public static final RewardTable BUILDING = RewardTable.of("building", t -> t
            .title("建筑材料")
            .lootSize(64)
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.YELLOW_SHULKER_BOX, Items.YELLOW_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.ORANGE_SHULKER_BOX, Items.ORANGE_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.RED_SHULKER_BOX, Items.RED_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.BROWN_SHULKER_BOX, Items.BROWN_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.BLACK_SHULKER_BOX, Items.BLACK_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.GRAY_SHULKER_BOX, Items.GRAY_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.LIGHT_GRAY_SHULKER_BOX, Items.LIGHT_GRAY_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.WHITE_SHULKER_BOX, Items.WHITE_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.PINK_SHULKER_BOX, Items.PINK_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.MAGENTA_SHULKER_BOX, Items.MAGENTA_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.BLUE_SHULKER_BOX, Items.BLUE_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.PURPLE_SHULKER_BOX, Items.PURPLE_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.LIGHT_BLUE_SHULKER_BOX, Items.LIGHT_BLUE_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.CYAN_SHULKER_BOX, Items.CYAN_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.GREEN_SHULKER_BOX, Items.GREEN_CONCRETE, 27), 1))
            .entry(Reward.stack(() -> QuestStacks.shulkerOf(Items.LIME_SHULKER_BOX, Items.LIME_CONCRETE, 27), 1)));

    public static final RewardTable EVIV = RewardTable.of("eviv", t -> t
            .title("EV-IV线缆包")
            .entry(Reward.item(TagPrefix.cableGtSingle, GTMaterials.Aluminium, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtDouble, GTMaterials.Aluminium, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtQuadruple, GTMaterials.Aluminium, 1))
            .entry(Reward.item(TagPrefix.cableGtOctal, GTMaterials.Aluminium, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtSingle, GTMaterials.Platinum, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtDouble, GTMaterials.Platinum, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtQuadruple, GTMaterials.Platinum, 1))
            .entry(Reward.item(TagPrefix.cableGtOctal, GTMaterials.Platinum, 1), 0));

    public static final RewardTable MVHV = RewardTable.of("mvhv", t -> t
            .title("MV-HV线缆包")
            .entry(Reward.item(TagPrefix.cableGtSingle, GTMaterials.AnnealedCopper, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtDouble, GTMaterials.AnnealedCopper, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtQuadruple, GTMaterials.AnnealedCopper, 1))
            .entry(Reward.item(TagPrefix.cableGtOctal, GTMaterials.AnnealedCopper, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtSingle, GTMaterials.Silver, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtDouble, GTMaterials.Silver, 1), 0)
            .entry(Reward.item(TagPrefix.cableGtQuadruple, GTMaterials.Silver, 1))
            .entry(Reward.item(TagPrefix.cableGtOctal, GTMaterials.Silver, 1), 0));

    public static final RewardTable PLANKS = RewardTable.of("planks", t -> t
            .title("木板组合包")
            .entry(Reward.item(Items.OAK_PLANKS, 64))
            .entry(Reward.item(Items.SPRUCE_PLANKS, 64))
            .entry(Reward.item(Items.BIRCH_PLANKS, 64))
            .entry(Reward.item(Items.JUNGLE_PLANKS, 64))
            .entry(Reward.item(Items.ACACIA_PLANKS, 64))
            .entry(Reward.item(Items.DARK_OAK_PLANKS, 64))
            .entry(Reward.item(Items.MANGROVE_PLANKS, 64))
            .entry(Reward.item(Items.CHERRY_PLANKS, 64))
            .entry(Reward.item(Items.CRIMSON_PLANKS, 64))
            .entry(Reward.item(Items.WARPED_PLANKS, 64)));

    public static final RewardTable ULV = RewardTable.of("ulv", t -> t
            .title("ULV可乐")
            .entry(Reward.item(GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.ULV]), 9)
            .entry(Reward.item(GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.LV]), 0)
            .entry(Reward.item(GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.MV]), 0));

    public static final RewardTable FLOWERS = RewardTable.of("flowers", t -> t
            .title("花朵组合包")
            .entry(Reward.item(BotaniaBlocks.whiteFlower, 16))
            .entry(Reward.item(BotaniaBlocks.lightGrayFlower, 16))
            .entry(Reward.item(BotaniaBlocks.grayFlower, 16))
            .entry(Reward.item(BotaniaBlocks.blackFlower, 16))
            .entry(Reward.item(BotaniaBlocks.brownFlower, 16))
            .entry(Reward.item(BotaniaBlocks.redFlower, 16))
            .entry(Reward.item(BotaniaBlocks.orangeFlower, 16))
            .entry(Reward.item(BotaniaBlocks.yellowFlower, 16))
            .entry(Reward.item(BotaniaBlocks.limeFlower, 16))
            .entry(Reward.item(BotaniaBlocks.greenFlower, 16))
            .entry(Reward.item(BotaniaBlocks.cyanFlower, 16))
            .entry(Reward.item(BotaniaBlocks.lightBlueFlower, 16))
            .entry(Reward.item(BotaniaBlocks.blueFlower, 16))
            .entry(Reward.item(BotaniaBlocks.purpleFlower, 16))
            .entry(Reward.item(BotaniaBlocks.magentaFlower, 16))
            .entry(Reward.item(BotaniaBlocks.pinkFlower, 16)));

    public static final RewardTable ULV_2 = RewardTable.of("ulv_2", t -> t
            .title("ULV摇奖")
            .icon(BotaniaItems.dice)
            .entry(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16), 60)
            .entry(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 32), 34)
            .entry(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64), 5)
            .entry(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 640L), 0)
            .entry(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 6400L), 0));

    public static final RewardTable ULV_3 = RewardTable.of("ulv_3", t -> t
            .title("ULV矿石补给包")
            .icon(ChemicalHelper.getItem(TagPrefix.ingot, GTMaterials.WroughtIron))
            .entry(Reward.item(TagPrefix.ore, GTMaterials.Iron, 64))
            .entry(Reward.item(TagPrefix.ore, GTMaterials.Copper, 64))
            .entry(Reward.item(TagPrefix.ore, GTMaterials.Tin, 64))
            .entry(Reward.item(TagPrefix.ore, GTMaterials.Coal, 64)));

    public static final RewardTable DUNGEON = RewardTable.of("dungeon", t -> t
            .title("地牢猎手套装")
            .icon(Items.NETHERITE_SWORD)
            .entry(
                    Reward.stack(() -> QuestStacks.enchanted(QuestStacks.enchanted(QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.NETHERITE_SWORD), Enchantments.SMITE, 7), Enchantments.MOB_LOOTING, 3), Enchantments.MENDING, 1), Enchantments.SHARPNESS, 7), 1).name("勇气之剑"))
            .entry(
                    Reward.stack(() -> QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.SHIELD), Enchantments.UNBREAKING, 5), Ench.Enchantments.REFLECTIVE.get(), 7), 1).name("豪迈之盾"))
            .entry(
                    Reward.stack(() -> QuestStacks.enchanted(QuestStacks.enchanted(QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.BOW), Enchantments.POWER_ARROWS, 8), Enchantments.INFINITY_ARROWS, 1), Enchantments.FLAMING_ARROWS, 1), Enchantments.PUNCH_ARROWS, 3), 1).name("无畏之弓"))
            .entry(Reward.item(Items.ARROW)));

    public static final RewardTable FURNACE = RewardTable.of("furnace", t -> t
            .title("炼金大师套装")
            .icon(Items.FURNACE)
            .entry(Reward.item(Items.BLAST_FURNACE, 4))
            .entry(Reward.item(JumboFurnace.get().jumboFurnaceItem.get(), 8))
            .entry(Reward.item(Items.COAL, 128L)));

    public static final RewardTable APOTHEOSIS = RewardTable.of("apotheosis", t -> t
            .title("神化组合包")
            .entry(Reward.item(Items.HONEYCOMB, 16))
            .entry(Reward.item(Items.CHORUS_FRUIT))
            .entry(Reward.item(Items.RABBIT_FOOT, 16))
            .entry(Reward.item(Adventure.Items.GEM_DUST.get(), 64))
            .entry(Reward.item(Items.GLOW_INK_SAC, 8))
            .entry(Reward.item(Adventure.Items.MYTHIC_MATERIAL.get()), 0));

    public static final RewardTable CONCRETE = RewardTable.of("concrete", t -> t
            .title("混凝土组合包")
            .entry(Reward.item(Items.WHITE_CONCRETE, 640L))
            .entry(Reward.item(Items.MAGENTA_CONCRETE, 640L))
            .entry(Reward.item(Items.LIGHT_BLUE_CONCRETE, 640L))
            .entry(Reward.item(Items.YELLOW_CONCRETE, 640L))
            .entry(Reward.item(Items.LIME_CONCRETE, 640L))
            .entry(Reward.item(Items.PINK_CONCRETE, 640L))
            .entry(Reward.item(Items.CYAN_CONCRETE, 640L))
            .entry(Reward.item(Items.PURPLE_CONCRETE, 640L))
            .entry(Reward.item(Items.BLUE_CONCRETE, 640L))
            .entry(Reward.item(Items.BROWN_CONCRETE, 640L))
            .entry(Reward.item(Items.GREEN_CONCRETE, 640L))
            .entry(Reward.item(Items.RED_CONCRETE, 640L))
            .entry(Reward.item(Items.BLACK_CONCRETE, 640L))
            .entry(Reward.item(Items.ORANGE_CONCRETE, 640L))
            .entry(Reward.item(Items.LIGHT_GRAY_CONCRETE, 640L))
            .entry(Reward.item(Items.GRAY_CONCRETE, 640L)));

    private QuestRewardTables() {}

    static void init() {}
}
