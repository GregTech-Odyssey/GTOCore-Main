package com.gtocore.data.lootTables;

import com.gtocore.common.data.GTOItems;
import com.gtocore.data.lootTables.tool.CustomLogicFunction;
import com.gtocore.data.lootTables.tool.CustomLogicNumberProvider;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.util.Lazy;

import org.jetbrains.annotations.NotNull;
import vazkii.botania.common.BotaniaItems;
import vazkii.botania.common.ExtraBotanyItems;
import vazkii.botania.common.MythicBotanyItems;

import static com.gregtechceu.gtceu.api.GTValues.LV;
import static com.gregtechceu.gtceu.api.GTValues.ULV;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ingot;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTOLoots.addToot;
import static com.gtocore.common.data.GTOMaterials.RedstoneAlloy;
import static com.gtocore.data.lootTables.tool.LootRegistrationTool.getLootItem;
import static net.minecraft.world.level.storage.loot.providers.number.ConstantValue.exactly;
import static net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.between;

public final class RewardBagLoot {

    public static void init() {
        addToot(LV_REWARD_BAG_LOOT, create_LV_REWARD_BAG_LOOT());
        addToot(RUNE_REWARD_BAG1_LOOT, create_RUNE_REWARD_BAG_LOOT(rune1.get()));
        addToot(RUNE_REWARD_BAG2_LOOT, create_RUNE_REWARD_BAG_LOOT(rune2.get()));
        addToot(RUNE_REWARD_BAG3_LOOT, create_RUNE_REWARD_BAG_LOOT(rune3.get()));
        addToot(RUNE_REWARD_BAG4_LOOT, create_RUNE_REWARD_BAG_LOOT(rune4.get()));
        addToot(EINS_REWARD_BAG_LOOT, create_EINS_REWARD_BAG_LOOT());
        addToot(ZWEI_REWARD_BAG_LOOT, create_ZWEI_REWARD_BAG_LOOT());
        addToot(DREI_REWARD_BAG_LOOT, create_DREI_REWARD_BAG_LOOT());
        addToot(VIER_REWARD_BAG_LOOT, create_VIER_REWARD_BAG_LOOT());
        addToot(NINE_AND_THREE_QUARTERS_REWARD_BAG_LOOT, create_NINE_AND_THREE_QUARTERS_REWARD_BAG_LOOT());
        addToot(PANDORAS_BOX_LOOT, create_PANDORAS_BOX_LOOT());

        getEfficiencyLuckLevel = null;
    }

    // 获取物品的效率等级
    public static CustomLogicNumberProvider getEfficiencyLuckLevel = new CustomLogicNumberProvider.Builder(
            (thisEntity, lastDamagePlayer, damageSource, killerEntity, directKiller,
             origin, blockState, blockEntity, tool, explosionRadius, level) -> {
                int result = tool.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
                return between(Mth.clamp(result - 2, 1, 18), Mth.clamp(result + 4, 1, 18));
            }).build();

    // 根据时运等级获得稀有物品
    public static @NotNull LootItem.Builder<?> getLootItemRelyFortune(Item item, int weight, int count) {
        return LootItem.lootTableItem(Items.AIR)
                .setWeight(weight)
                .apply(
                        new CustomLogicFunction.Builder((
                                                         level, thisEntity, lastDamagePlayer, damageSource, killerEntity, directKiller,
                                                         origin, blockState, blockEntity, tool, explosionRadius, stack) -> {
                            if (origin != null) {
                                float probability = 0.2f * (1 + tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE));
                                if (probability >= 1 || level.random.nextFloat() < probability) {
                                    ItemEntity itemEntity = new ItemEntity(level, origin.x(), origin.y(), origin.z(), new ItemStack(item, count));
                                    itemEntity.setNoPickUpDelay();
                                    level.addFreshEntity(itemEntity);
                                }
                            }
                        }));
    }

    // LV 战利品袋子
    public static final ResourceLocation LV_REWARD_BAG_LOOT = GTOCore.id("reward_bags/lv_reward_bag_loot");
    // 额外植物学 Eins 奖励袋（花瓣）
    public static final ResourceLocation EINS_REWARD_BAG_LOOT = GTOCore.id("reward_bags/eins_reward_bag_loot");
    // 额外植物学 Zwei 奖励袋（符文）
    public static final ResourceLocation ZWEI_REWARD_BAG_LOOT = GTOCore.id("reward_bags/zwei_reward_bag_loot");
    // 额外植物学 Drei 奖励袋（植物魔法材料）
    public static final ResourceLocation DREI_REWARD_BAG_LOOT = GTOCore.id("reward_bags/drei_reward_bag_loot");
    // 额外植物学 Vier 奖励袋（原版材料）
    public static final ResourceLocation VIER_REWARD_BAG_LOOT = GTOCore.id("reward_bags/vier_reward_bag_loot");
    // 额外植物学 九又四分之三奖励袋
    public static final ResourceLocation NINE_AND_THREE_QUARTERS_REWARD_BAG_LOOT = GTOCore.id("reward_bags/nine_and_three_quarters_reward_bag_loot");
    // 额外植物学 潘多拉魔盒
    public static final ResourceLocation PANDORAS_BOX_LOOT = GTOCore.id("reward_bags/pandoras_box_loot");
    // 植物魔法 符文1 袋子
    public static final ResourceLocation RUNE_REWARD_BAG1_LOOT = GTOCore.id("reward_bags/rune_reward_bag1_loot");
    // 植物魔法 符文2 袋子
    public static final ResourceLocation RUNE_REWARD_BAG2_LOOT = GTOCore.id("reward_bags/rune_reward_bag2_loot");
    // 植物魔法 符文3 袋子
    public static final ResourceLocation RUNE_REWARD_BAG3_LOOT = GTOCore.id("reward_bags/rune_reward_bag3_loot");
    // 植物魔法 符文4 袋子
    public static final ResourceLocation RUNE_REWARD_BAG4_LOOT = GTOCore.id("reward_bags/rune_reward_bag4_loot");

    private static LootTable create_LV_REWARD_BAG_LOOT() {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(getEfficiencyLuckLevel)
                .setBonusRolls(between(0, 3))
                .add(getLootItem("farmersrespite:black_cod", 60, between(8, 16)))
                .add(getLootItem("farmersrespite:tea_curry", 60, between(8, 16)))
                .add(getLootItem("farmersrespite:blazing_chili", 60, between(8, 16)))
                .add(getLootItem("farmersrespite:coffee_cake", 60, between(8, 16)))
                .add(getLootItem("farmersrespite:rose_hip_pie", 60, between(8, 16)))
                .add(getLootItem("farmersdelight:beef_patty", 60, between(8, 16)))
                .add(getLootItem("farmersdelight:hamburger", 60, between(8, 16)))
                .add(getLootItem("farmersdelight:roasted_mutton_chops", 60, between(8, 16)))
                .add(getLootItem("farmersdelight:hot_cocoa", 60, between(8, 16)))

                .add(getLootItem(ChemicalHelper.getItem(ingot, Iron), 80, exactly(1)))
                .add(getLootItem(ChemicalHelper.getItem(ingot, Copper), 80, exactly(1)))
                .add(getLootItem(ChemicalHelper.getItem(ingot, BismuthBronze), 80, exactly(1)))
                .add(getLootItem(ChemicalHelper.getItem(ingot, RedAlloy), 80, exactly(1)))
                .add(getLootItem(ChemicalHelper.getItem(ingot, RedstoneAlloy), 80, exactly(1)))
                .add(getLootItem(ChemicalHelper.getItem(ingot, Steel), 80, exactly(1)))
                .add(getLootItem(ChemicalHelper.getItem(ingot, Aluminium), 80, exactly(1)))
                .add(getLootItem(Items.REDSTONE, 80, exactly(1)))
                .add(getLootItem(Items.COAL, 80, exactly(1)))
                .add(getLootItem(Items.ENDER_PEARL, 80, exactly(1)))
                .add(getLootItem("functionalstorage:copper_upgrade", 60, exactly(1)))
                .add(getLootItem(GTItems.FLUID_CELL_LARGE_STEEL.asItem(), 60, between(8, 16)))

                .add(getLootItem("gtceu:steel_wrench", 80, exactly(1)))
                .add(getLootItem("gtceu:steel_wire_cutter", 80, exactly(1)))
                .add(getLootItem("gtceu:steel_hammer", 80, exactly(1)))
                .add(getLootItem("gtceu:steel_screwdriver", 80, exactly(1)))
                .add(getLootItem("gtceu:steel_file", 80, exactly(1)))
                .add(getLootItem("gtceu:silicone_rubber_mallet", 80, exactly(1)))

                .add(getLootItem(GTItems.ELECTRIC_MOTOR_LV.asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.CONVEYOR_MODULE_LV.asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.ELECTRIC_PUMP_LV.asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.FLUID_REGULATOR_LV.asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.ELECTRIC_PISTON_LV.asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.ROBOT_ARM_LV.asItem(), 80, exactly(1)))
                .add(getLootItemRelyFortune(GTItems.EMITTER_LV.asItem(), 80, 1))
                .add(getLootItemRelyFortune(GTItems.SENSOR_LV.asItem(), 80, 1))
                .add(getLootItemRelyFortune(GTItems.FIELD_GENERATOR_LV.asItem(), 5, 1))

                .add(getLootItem(GTItems.VOLTAGE_COIL_LV.asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.RESISTOR.asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.DIODE.asItem(), 80, exactly(1)))
                .add(getLootItem(GTOItems.UNIVERSAL_CIRCUIT[ULV].asItem(), 80, exactly(1)))
                .add(getLootItem(GTOItems.UNIVERSAL_CIRCUIT[LV].asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.GLASS_TUBE.asItem(), 80, exactly(1)))
                .add(getLootItem(GTItems.COATED_BOARD.asItem(), 80, exactly(1)))
                .add(getLootItem("sophisticatedbackpacks:upgrade_base", 80, exactly(1)))
                .add(getLootItem("sophisticatedbackpacks:stack_upgrade_tier_1", 80, exactly(1)));

        return LootTable.lootTable()
                .withPool(pool)
                .setParamSet(LootContextParamSets.FISHING)
                .build();
    }

    private static final Lazy<Item[]> rune1 = Lazy.of(() -> new Item[] {
            BotaniaItems.getItem("rune_mana"),
            BotaniaItems.getItem("rune_water"),
            BotaniaItems.getItem("rune_fire"),
            BotaniaItems.getItem("rune_air"),
            BotaniaItems.getItem("rune_earth")
    });

    private static final Lazy<Item[]> rune2 = Lazy.of(() -> new Item[] {
            BotaniaItems.getItem("rune_spring"),
            BotaniaItems.getItem("rune_summer"),
            BotaniaItems.getItem("rune_autumn"),
            BotaniaItems.getItem("rune_winter")
    });

    private static final Lazy<Item[]> rune3 = Lazy.of(() -> new Item[] {
            BotaniaItems.getItem("rune_lust"),
            BotaniaItems.getItem("rune_gluttony"),
            BotaniaItems.getItem("rune_greed"),
            BotaniaItems.getItem("rune_sloth"),
            BotaniaItems.getItem("rune_wrath"),
            BotaniaItems.getItem("rune_envy"),
            BotaniaItems.getItem("rune_pride")
    });

    private static final Lazy<Item[]> rune4 = Lazy.of(() -> new Item[] {
            MythicBotanyItems.getItem("asgard_rune"),
            MythicBotanyItems.getItem("vanaheim_rune"),
            MythicBotanyItems.getItem("alfheim_rune"),
            MythicBotanyItems.getItem("joetunheim_rune"),
            MythicBotanyItems.getItem("muspelheim_rune"),
            MythicBotanyItems.getItem("niflheim_rune"),
            MythicBotanyItems.getItem("nidavellir_rune"),
            MythicBotanyItems.getItem("helheim_rune"),
            MythicBotanyItems.getItem("midgard_rune")
    });

    private static LootTable create_RUNE_REWARD_BAG_LOOT(Item[] runes) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(getEfficiencyLuckLevel)
                .setBonusRolls(between(0, 2));

        for (Item rune : runes) {
            pool.add(getLootItem(rune, 100, between(4, 8)));
        }

        return LootTable.lootTable()
                .withPool(pool)
                .setParamSet(LootContextParamSets.FISHING)
                .build();
    }

    private static final String[] PETAL_IDS = {
            "white_petal", "orange_petal", "magenta_petal", "light_blue_petal",
            "yellow_petal", "lime_petal", "pink_petal", "gray_petal",
            "light_gray_petal", "cyan_petal", "purple_petal", "blue_petal",
            "brown_petal", "green_petal", "red_petal", "black_petal"
    };

    // 额外植物学 Eins 奖励袋：随机 6 个同色花瓣
    private static LootTable create_EINS_REWARD_BAG_LOOT() {
        LootPool.Builder pool = LootPool.lootPool().setRolls(exactly(1));
        for (String petal : PETAL_IDS) {
            pool.add(getLootItem(BotaniaItems.getItem(petal), 1, exactly(6)));
        }
        return LootTable.lootTable()
                .withPool(pool)
                .setParamSet(LootContextParamSets.FISHING)
                .build();
    }

    // 额外植物学 Zwei 奖励袋：随机符文
    private static LootTable create_ZWEI_REWARD_BAG_LOOT() {
        LootPool.Builder pool = LootPool.lootPool().setRolls(exactly(1));
        for (String rune : new String[] { "rune_water", "rune_fire", "rune_earth", "rune_air" }) {
            pool.add(getLootItem(BotaniaItems.getItem(rune), 50, exactly(2)));
        }
        for (String rune : new String[] { "rune_spring", "rune_summer", "rune_autumn", "rune_winter" }) {
            pool.add(getLootItem(BotaniaItems.getItem(rune), 30, exactly(1)));
        }
        for (String rune : new String[] { "rune_mana", "rune_lust", "rune_gluttony", "rune_greed",
                "rune_sloth", "rune_wrath", "rune_envy", "rune_pride" }) {
            pool.add(getLootItem(BotaniaItems.getItem(rune), 10, exactly(1)));
        }
        return LootTable.lootTable()
                .withPool(pool)
                .setParamSet(LootContextParamSets.FISHING)
                .build();
    }

    // 额外植物学 Drei 奖励袋：植物魔法材料
    private static LootTable create_DREI_REWARD_BAG_LOOT() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(exactly(1))
                        .add(getLootItem(BotaniaItems.getItem("manasteel_ingot"), 15, exactly(4)))
                        .add(getLootItem(BotaniaItems.getItem("mana_pearl"), 15, exactly(4)))
                        .add(getLootItem(BotaniaItems.getItem("mana_diamond"), 15, exactly(4)))
                        .add(getLootItem(BotaniaItems.getItem("elementium_ingot"), 11, exactly(3)))
                        .add(getLootItem(BotaniaItems.getItem("pixie_dust"), 11, exactly(3)))
                        .add(getLootItem(BotaniaItems.getItem("dragonstone"), 11, exactly(3)))
                        .add(getLootItem(BotaniaItems.getItem("mana_powder"), 10, exactly(8)))
                        .add(getLootItem(BotaniaItems.getItem("terrasteel_ingot"), 9, exactly(1)))
                        .add(getLootItem(BotaniaItems.getItem("life_essence"), 8, exactly(4)))
                        .add(getLootItem(BotaniaItems.getItem("gaia_ingot"), 7, exactly(1)))
                        .add(getLootItem(ExtraBotanyItems.HERO_MEDAL.get(), 1, exactly(1))))
                .setParamSet(LootContextParamSets.FISHING)
                .build();
    }

    // 额外植物学 Vier 奖励袋：原版材料
    private static LootTable create_VIER_REWARD_BAG_LOOT() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(exactly(1))
                        .add(getLootItem(Items.COAL, 40, exactly(6)))
                        .add(getLootItem(Items.IRON_INGOT, 36, exactly(4)))
                        .add(getLootItem(Items.GOLD_INGOT, 24, exactly(4)))
                        .add(getLootItem(Items.REDSTONE, 22, exactly(8)))
                        .add(getLootItem(Items.ENDER_PEARL, 20, exactly(4)))
                        .add(getLootItem(Items.DIAMOND, 18, exactly(1)))
                        .add(getLootItem(BotaniaItems.getItem("blacker_lotus"), 16, exactly(2)))
                        .add(getLootItem(BotaniaItems.getItem("overgrowth_seed"), 12, exactly(1)))
                        .add(getLootItem(ExtraBotanyItems.VOID_ARCHIVES.get(), 1, exactly(1))))
                .setParamSet(LootContextParamSets.FISHING)
                .build();
    }

    // 额外植物学 九又四分之三奖励袋
    private static LootTable create_NINE_AND_THREE_QUARTERS_REWARD_BAG_LOOT() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(exactly(1))
                        .add(getLootItem(ExtraBotanyItems.HERO_MEDAL.get(), 1, exactly(1)))
                        .add(getLootItem(ExtraBotanyItems.EINS_REWARD_BAG.get(), 30, exactly(16)))
                        .add(getLootItem(ExtraBotanyItems.ZWEI_REWARD_BAG.get(), 20, exactly(10)))
                        .add(getLootItem(ExtraBotanyItems.DREI_REWARD_BAG.get(), 10, exactly(6)))
                        .add(getLootItem(ExtraBotanyItems.VIER_REWARD_BAG.get(), 10, exactly(6)))
                        .add(getLootItem(BotaniaItems.getItem("gaia_ingot"), 14, exactly(1)))
                        .add(getLootItem(BotaniaItems.getItem("life_essence"), 20, exactly(4)))
                        .add(getLootItem(ExtraBotanyItems.CHALLENGE_TICKET.get(), 45, exactly(1))))
                .setParamSet(LootContextParamSets.FISHING)
                .build();
    }

    // 额外植物学 潘多拉魔盒：各奖励袋与英雄勋章
    private static LootTable create_PANDORAS_BOX_LOOT() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(exactly(1))
                        .add(getLootItem(ExtraBotanyItems.EINS_REWARD_BAG.get(), 1, exactly(32))))
                .withPool(LootPool.lootPool().setRolls(exactly(1))
                        .add(getLootItem(ExtraBotanyItems.ZWEI_REWARD_BAG.get(), 1, exactly(16))))
                .withPool(LootPool.lootPool().setRolls(exactly(1))
                        .add(getLootItem(ExtraBotanyItems.DREI_REWARD_BAG.get(), 1, exactly(10))))
                .withPool(LootPool.lootPool().setRolls(exactly(1))
                        .add(getLootItem(ExtraBotanyItems.VIER_REWARD_BAG.get(), 1, exactly(10))))
                .withPool(LootPool.lootPool().setRolls(exactly(1))
                        .add(getLootItem(ExtraBotanyItems.HERO_MEDAL.get(), 1, exactly(1))))
                .setParamSet(LootContextParamSets.FISHING)
                .build();
    }
}
