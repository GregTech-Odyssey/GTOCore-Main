package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestRewardTables;
import com.gtocore.data.quest.QuestTextures;
import com.gtocore.integration.Mods;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;

import dev.shadowsoffire.apotheosis.adventure.Adventure;
import earth.terrarium.adastra.common.registry.ModFluids;
import earth.terrarium.adastra.common.registry.ModItems;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class ShopChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("shop", c -> c
            .title("格雷科技™员工会员店")
            .subtitle("Welcome to Greg's Club")
            .icon(GTOTagPrefix.COIN, GTOMaterials.Infinity)
            .defaultRepeatable()
            .consumeItems(true));

    public static final Quest PROSPECTOR_MANA_ULV = CHAPTER.quest("prospector_mana_ulv", q -> q
            .at(-11.5, 3.5)
            .iconStack(() -> QuestStacks.withNbt(new ItemStack(GTOItems.PROSPECTOR_MANA_ULV), "{mana:500000}"))
            .title("魔力钢探矿仪")
            .desc(d -> d.line("主线送了一个，如果你丢了可以在这里买"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .reward(Reward.stack(() -> QuestStacks.withNbt(new ItemStack(GTOItems.PROSPECTOR_MANA_ULV), "{mana:500000}"), 1))
            .repeatable());

    public static final Quest Q_16K_PORTABLE_ITEM_CELL = CHAPTER.quest("q_16k_portable_item_cell", q -> q
            .at(-9.5, 3.5)
            .iconStack(() -> QuestStacks.withNbt(new ItemStack(AEItems.PORTABLE_ITEM_CELL16K), "{internalCurrentPower:20000.0d}"))
            .title("16k便携物品元件")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 128L))
            .reward(
                    Reward.stack(() -> QuestStacks.withNbt(new ItemStack(AEItems.PORTABLE_ITEM_CELL16K), "{internalCurrentPower:20000.0d}"), 1).personal()));

    public static final Quest SUPER_TANK_I = CHAPTER.quest("super_tank_i", q -> q
            .at(-9.5, 7.5)
            .icon(GTMachines.SUPER_TANK[GTValues.LV])
            .title("超级缸 I")
            .desc(d -> d
                    .line("完成{LV组装机任务}后解锁", link(LvChapter.ASSEMBLE_EVERYTHING))
                    .line()
                    .line("只能装一种流体"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 24))
            .reward(Reward.item(GTMachines.SUPER_TANK[GTValues.LV]))
            .dependsOn(LvChapter.ASSEMBLE_EVERYTHING));

    public static final Quest CHARGING_PROSPECTOR_MANA_ULV = CHAPTER.quest("charging_prospector_mana_ulv", q -> q
            .at(-11.5, 2)
            .icon(ModItems.ETRIUM_NUGGET.get())
            .title("充电-魔力钢探矿仪")
            .subtitle("概不退换")
            .desc(d -> d.line("建议完全用完了魔力再来这里兑换，当然如果你有自己的植物魔法产线就最好了"))
            .task(Task.item(GTOItems.PROSPECTOR_MANA_ULV), Task.item(TagPrefix.ingot, GTMaterials.Bronze, 32))
            .reward(Reward.stack(() -> QuestStacks.withNbt(new ItemStack(GTOItems.PROSPECTOR_MANA_ULV), "{mana:500000}"), 1)));

    public static final Quest COLA_BOX_I = CHAPTER.quest("cola_box_i", q -> q
            .at(-1.5, 5.5)
            .icon(GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.ULV])
            .title("可乐箱 I")
            .desc(d -> d
                    .line("喝了可乐之后在小机器上跳来跳去可以加速小机器工作")
                    .line()
                    .line("可乐等级/机械之神附身的等级代表其最高能加速的小机器的等级"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .reward(Reward.random(QuestRewardTables.ULV).excludeFromClaimAll()));

    public static final Quest WELCOME_TO_THE_GREGTECH_MEMBER_EXCLUSIVE = CHAPTER.quest("welcome_to_the_gregtech_member_exclusive", q -> q
            .at(-0.5, 0)
            .icon(BotaniaItems.manaCookie)
            .title("欢迎来到格雷科技™会员专卖店")
            .subtitle("希望你喜欢")
            .desc(d -> d
                    .line("以后会添加更多购买项以及钱币获取途径")
                    .line()
                    .line("{这个饼（我画的）就送给你啦}", GRAY.italic()))
            .task(Task.checkmark().title("欢迎光临！"))
            .reward(Reward.item(BotaniaItems.manaCookie))
            .repeatable(false));

    public static final Quest Q_16K_PORTABLE_FLUID_CELL = CHAPTER.quest("q_16k_portable_fluid_cell", q -> q
            .at(-7.5, 3.5)
            .iconStack(() -> QuestStacks.withNbt(new ItemStack(AEItems.PORTABLE_FLUID_CELL16K), "{internalCurrentPower:20000.0d}"))
            .title("16k便携流体元件")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 128L))
            .reward(
                    Reward.stack(() -> QuestStacks.withNbt(new ItemStack(AEItems.PORTABLE_FLUID_CELL16K), "{internalCurrentPower:20000.0d}"), 1).personal()));

    public static final Quest ENDER_PEARL = CHAPTER.quest("ender_pearl", q -> q
            .at(-9.5, 5.5)
            .icon(Items.ENDER_PEARL)
            .title("末影珍珠")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 8))
            .reward(Reward.item(Items.ENDER_PEARL, 4)));

    public static final Quest FLOWER_BUNDLE = CHAPTER.quest("flower_bundle", q -> q
            .at(-5.5, 5.5)
            .icon(BotaniaBlocks.greenFlower)
            .title("花朵组合包")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .reward(Reward.choice(QuestRewardTables.FLOWERS).excludeFromClaimAll()));

    public static final Quest HOW_S_YOUR_LUCK_TODAY = CHAPTER.quest("how_s_your_luck_today", q -> q
            .at(-13.5, 3.5)
            .title("今日手气如何？")
            .subtitle("切勿沉迷")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 32))
            .reward(Reward.random(QuestRewardTables.ULV_2).title("一念之间").icon(BotaniaItems.dice)));

    public static final Quest BRONZE_BLOCK = CHAPTER.quest("bronze_block", q -> q
            .at(4.5, 3.5)
            .icon(TagPrefix.block, GTMaterials.Bronze)
            .title("青铜块")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 10))
            .reward(Reward.item(TagPrefix.block, GTMaterials.Bronze, 8)));

    public static final Quest PRECISION_STEAM_MECHANISM = CHAPTER.quest("precision_steam_mechanism", q -> q
            .at(-11.5, 5.5)
            .icon(GTOItems.PRECISION_STEAM_MECHANISM)
            .title("精密蒸汽构件")
            .desc(d -> d.line("提前造出大蒸汽机器"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 128L))
            .reward(Reward.item(GTOItems.PRECISION_STEAM_MECHANISM)));

    public static final Quest ULV_ORE_SUPPLY_PACKAGE = CHAPTER.quest("ulv_ore_supply_package", q -> q
            .at(12.5, 3.5)
            .icon(TagPrefix.ingot, GTMaterials.WroughtIron)
            .title("ULV矿石补给包")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 24))
            .reward(Reward.choice(QuestRewardTables.ULV_3).excludeFromClaimAll()));

    public static final Quest DUNGEON_HUNTER_ARMOR_SET = CHAPTER.quest("dungeon_hunter_armor_set", q -> q
            .at(6.5, 3.5)
            .iconStack(
                    () -> QuestStacks.enchanted(QuestStacks.enchanted(QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.NETHERITE_SWORD), Enchantments.SMITE, 7), Enchantments.MOB_LOOTING, 3), Enchantments.MENDING, 1), Enchantments.SHARPNESS, 7))
            .title("地牢猎手套装")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .reward(Reward.allOf(QuestRewardTables.DUNGEON).personal().excludeFromClaimAll().title("地牢猎手套装").icon(Items.NETHERITE_SWORD)));

    public static final Quest ALCHEMIST_S_MASTER_SET = CHAPTER.quest("alchemist_s_master_set", q -> q
            .at(-3.5, 3.5)
            .icon(Items.FURNACE)
            .title("炼金大师套装")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 24))
            .reward(Reward.allOf(QuestRewardTables.FURNACE).excludeFromClaimAll().title("炼金大师套装")));

    public static final Quest A_STACK_OF_CLAY = CHAPTER.quest("a_stack_of_clay", q -> q
            .at(2.5, 3.5)
            .icon(Items.CLAY)
            .title("黏土一组")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .reward(Reward.item(Items.CLAY, 64)));

    public static final Quest STICKY_RESIN = CHAPTER.quest("sticky_resin", q -> q
            .at(0.5, 3.5)
            .icon(GTItems.STICKY_RESIN)
            .title("黏性树脂")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .reward(Reward.item(GTItems.STICKY_RESIN, 128L)));

    public static final Quest APOTHEOSIS_PACK = CHAPTER.quest("apotheosis_pack", q -> q
            .at(-3.5, 5.5)
            .icon(Adventure.Items.GEM_DUST.get())
            .title("神化组合包")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 32))
            .reward(Reward.choice(QuestRewardTables.APOTHEOSIS).excludeFromClaimAll()));

    public static final Quest ENCHANTED_BOOK_MENDING = CHAPTER.quest("enchanted_book_mending", q -> q
            .at(-7.5, 5.5)
            .iconStack(() -> QuestStacks.enchantedBook(Enchantments.MENDING, 1))
            .title("附魔书——经验修补")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 128L))
            .reward(Reward.stack(() -> QuestStacks.enchantedBook(Enchantments.MENDING, 1), 1))
            .repeatable());

    public static final Quest EMERGENCY_OXYGEN_TANK = CHAPTER.quest("emergency_oxygen_tank", q -> q
            .at(-13.5, 9.5)
            .icon(ModItems.LARGE_GAS_TANK.get())
            .title("急救氧气罐")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .reward(Reward.stack(() -> QuestStacks.filled(ModItems.LARGE_GAS_TANK.get(), new FluidStack(ModFluids.OXYGEN.get(), 3000)), 1))
            .dependsOn(HvChapter.HEAD_TO_THE_MOON)
            .hideUntilDepsComplete());

    public static final Quest DAMASCUS_STEEL_MINING_HAMMER = CHAPTER.quest("damascus_steel_mining_hammer", q -> q
            .at(-5.5, 3.5)
            .iconStack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.MINING_HAMMER, GTMaterials.DamascusSteel), Enchantments.UNBREAKING, 3))
            .title("大马士革钢采矿锤")
            .desc(d -> d.line("自带耐久3"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 48))
            .reward(
                    Reward.stack(() -> QuestStacks.enchanted(ToolHelper.get(GTToolType.MINING_HAMMER, GTMaterials.DamascusSteel), Enchantments.UNBREAKING, 3), 1).personal()));

    public static final Quest HALF_A_STACK_OF_STEEL_INGOTS = CHAPTER.quest("half_a_stack_of_steel_ingots", q -> q
            .at(8.5, 3.5)
            .icon(ModItems.STEEL_INGOT.get())
            .title("钢锭半组")
            .desc(d -> d.line("提前获取一些钢"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .reward(Reward.item(ModItems.STEEL_INGOT.get(), 32)));

    public static final Quest COLA_BOX_II = CHAPTER.quest("cola_box_ii", q -> q
            .at(12.5, 7.5)
            .icon(GTOItems.MYSTERIOUS_BOOST_DRINK[GTValues.LV])
            .title("可乐箱 II")
            .desc(d -> d.line("喝了可乐之后在小机器上跳来跳去可以加速小机器工作"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Cupronickel, 8))
            .invisible());

    public static final Quest AE2_SUPPLY_PACKAGE_EASY_NORMAL = CHAPTER.quest("ae2_supply_package_easy_normal", q -> q
            .at(-11.5, 9.5)
            .icon(AEItems.BLANK_PATTERN)
            .title("AE2物资补给包（{简单} & {普通}）", GREEN, YELLOW)
            .subtitle("{仅可兑换一次}", YELLOW)
            .desc(d -> d
                    .line("进入HV之后再来兑换吧")
                    .line()
                    .line("能让你快速起步AE"))
            .task(
                    Task.item(GTItems.NAND_MEMORY_CHIP, 32),
                    Task.item(GTOItems.UNIVERSAL_CIRCUIT[GTValues.HV], 4),
                    Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 128L))
            .reward(
                    Reward.item(AEBlocks.CRAFTING_STORAGE_1K, 2),
                    Reward.item(AEBlocks.PATTERN_PROVIDER, 4),
                    Reward.item(AEBlocks.INTERFACE, 8),
                    Reward.item(AEItems.BLANK_PATTERN, 24),
                    Reward.item(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT), 64),
                    Reward.item(AEItems.ITEM_CELL_16K),
                    Reward.item(AEItems.FLUID_CELL_16K))
            .dependsOn(UtilChapter.EASY_NORMAL)
            .repeatable(false).hideUntilDepsVisible());

    public static final Quest TITANIUM_INGOT_EASY_NORMAL = CHAPTER.quest("titanium_ingot_easy_normal", q -> q
            .at(-9.5, 9.5)
            .icon(TagPrefix.ingot, GTMaterials.Titanium)
            .title("钛锭（{简单} & {普通}）", GREEN, YELLOW)
            .desc(d -> d
                    .line("进入HV之后再来兑换吧")
                    .line()
                    .line("能让你提前做出少量的HV整体框架"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Nichrome, 32), Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .reward(Reward.item(TagPrefix.ingot, GTMaterials.Titanium, 3))
            .dependsOn(UtilChapter.EASY_NORMAL)
            .hideUntilDepsVisible());

    public static final Quest HV_FORCE_FIELD_GENERATOR_EASY_NORMAL = CHAPTER.quest("hv_force_field_generator_easy_normal", q -> q
            .at(-7.5, 9.5)
            .icon(GTItems.FIELD_GENERATOR_HV)
            .title("{HV}力场发生器（{简单} & {普通}）", HV, GREEN, YELLOW)
            .desc(d -> d
                    .line("进入HV之后再来兑换吧")
                    .line()
                    .line("能让你提前做出金刚杵和普通难度下的HV输入输出总成"))
            .task(Task.item(GTItems.FIELD_GENERATOR_MV, 8), Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .reward(Reward.item(GTItems.FIELD_GENERATOR_HV))
            .dependsOn(UtilChapter.EASY_NORMAL)
            .hideUntilDepsVisible());

    public static final Quest SUPER_CHEST_I = CHAPTER.quest("super_chest_i", q -> q
            .at(-11.5, 7.5)
            .icon(GTMachines.SUPER_CHEST[GTValues.LV])
            .title("超级箱 I")
            .desc(d -> d
                    .line("完成{LV电路板任务}后解锁", link(UlvChapter.BASIC_ELECTRONIC_CIRCUIT))
                    .line()
                    .line("只能装一种物品"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .reward(Reward.item(GTMachines.SUPER_CHEST[GTValues.LV]))
            .dependsOn(UlvChapter.BASIC_ELECTRONIC_CIRCUIT));

    public static final Quest BISCUIT_OF_TOTALITY = CHAPTER.quest("biscuit_of_totality", q -> q
            .at(-1.5, 3.5)
            .icon(BotaniaItems.manaCookie)
            .title("魔力曲奇")
            .subtitle("{仅可兑换一次}", YELLOW)
            .desc(d -> d
                    .line("给你一组曲奇，愿你熬过这个冬天")
                    .line()
                    .line("它有神奇的魔力，能瞬间补满你的饱食度"))
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 32))
            .reward(Reward.item(BotaniaItems.manaCookie, 64).personal())
            .repeatable(false));

    public static final Quest COPPER_COIN_SUBSIDY = CHAPTER.quest("copper_coin_subsidy", q -> q
            .at(2, 0)
            .icon(GTOTagPrefix.COIN, GTMaterials.Copper)
            .title("铜币补助")
            .task(Task.checkmark())
            .easySubsidy(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .normalSubsidy(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .dependsOn(WELCOME_TO_THE_GREGTECH_MEMBER_EXCLUSIVE)
            .optional()
            .repeatable(false)
            .hideUntilDepsVisible());

    public static final Quest ADMISSION_TICKET_EXPERT = CHAPTER.quest("admission_ticket_expert", q -> q
            .at(3, 0)
            .icon(GTOTagPrefix.COIN, GTMaterials.Copper)
            .title("准入门票{（专家）}", LIGHT_PURPLE)
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 64))
            .dependsOn(WELCOME_TO_THE_GREGTECH_MEMBER_EXCLUSIVE, UtilChapter.Q_1FB1EC5B)
            .optional().repeatable(false).hideUntilDepsVisible().hideDependentLines());

    public static final Quest BUILDER_S_MASTER_SET = CHAPTER.quest("builder_s_master_set", q -> q
            .at(10.5, 3.5)
            .icon(QuestExternalRefs.ConstructionWandItems.INFINITY_WAND)
            .title("建筑大师套装")
            .subtitle("成为建筑大师的必经之路")
            .task(Task.item(GTOTagPrefix.COIN, GTMaterials.Copper, 128L))
            .reward(
                    Reward.item(QuestExternalRefs.ConstructionWandItems.INFINITY_WAND).personal().onlyOne(),
                    Reward.item(QuestExternalRefs.ConstructionWandItems.CORE_ANGEL).personal())
            .dependsOn(ADMISSION_TICKET_EXPERT, UtilChapter.EASY_NORMAL)
            .requireAny());

    public static final Quest BUILDING_MATERIALS = CHAPTER.quest("building_materials", q -> q
            .at(11.5, 2)
            .icon(Items.SHULKER_BOX)
            .title("建筑材料")
            .subtitle("免费续杯")
            .desc(d -> d.line("不想手动点也可以制作混凝土的无限AE元件"))
            .task(Task.checkmark().title("再来十组！"))
            .reward(Reward.choice(QuestRewardTables.CONCRETE).excludeFromClaimAll())
            .dependsOn(UtilChapter.INFINITY_WAND, BUILDER_S_MASTER_SET)
            .requireAny());

    public static final Quest FACTORY_BLOCK = CHAPTER.quest("factory_block", q -> q
            .at(9.5, 2)
            .icon(QuestExternalRefs.FactoryBlocksItems.FACTORY)
            .title("工厂方块")
            .subtitle("建筑师必备")
            .desc(d -> d.line("不想手动点也可以制作建筑方块的无限AE元件"))
            .task(Task.modLoaded(Mods.FACTORY_BLOCKS), Task.checkmark().title("再来一组"))
            .reward(Reward.item(QuestExternalRefs.FactoryBlocksItems.FACTORY, 640L))
            .dependsOn(BUILDER_S_MASTER_SET, UtilChapter.INFINITY_WAND)
            .requireAny()
            .hideUntilDepsVisible());

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -12, 9.5, 1, 1).after(UtilChapter.EASY_NORMAL))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -10, 9.5, 1, 1).after(UtilChapter.EASY_NORMAL))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_HV, -8, 9.5, 1, 1).after(UtilChapter.EASY_NORMAL))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_ULV, -12.2, 7.5, 1, 1))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LV, -10, 7.5, 1, 1)));
    }
}
