package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

public final class BackpackChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("backpack", c -> c
            .title("摩登时代")
            .subtitle("Sophisticated Backpacks")
            .icon(QuestExternalRefs.SophisticatedBackpacksItems.BACKPACK));

    public static final Quest Q_1CE34BD4 = CHAPTER.quest("q_1ce34bd4", q -> q
            .at(0, 0)
            .desc(d -> d
                    .line("你需要把你的背包慢慢升级")
                    .line()
                    .line("背包来装多方块的方块去部署多方块机器很方便"))
            .task(Task.checkmark().title("欢迎来到摩登时代"))
            .dependsOn(MainStoneageChapter.BACKPACK_TIME));

    public static final Quest UPGRADE_BASE = CHAPTER.quest("upgrade_base", q -> q
            .at(0, 1.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.UPGRADE_BASE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(Q_1CE34BD4));

    public static final Quest RESTOCK_UPGRADE = CHAPTER.quest("restock_upgrade", q -> q
            .at(2, 1)
            .subtitle("打包带走")
            .desc(d -> d.line("安装好模块之后，右键容器即可将物品存入背包"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.RESTOCK_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest DEPOSIT_UPGRADE = CHAPTER.quest("deposit_upgrade", q -> q
            .at(2, 0)
            .subtitle("一键倾倒")
            .desc(d -> d.line("用于快速卸下物品，如矿石"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.DEPOSIT_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_RESTOCK_UPGRADE = CHAPTER.quest("advanced_restock_upgrade", q -> q
            .at(3.5, 1)
            .subtitle("过滤输入")
            .desc(d -> d
                    .line("可以过滤要拿取什么物品")
                    .line()
                    .line("但标签模式能设置标签的物品有点少"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_RESTOCK_UPGRADE))
            .dependsOn(RESTOCK_UPGRADE));

    public static final Quest ADVANCED_DEPOSIT_UPGRADE = CHAPTER.quest("advanced_deposit_upgrade", q -> q
            .at(3.5, 0)
            .subtitle("噢不不不 ，这东西我需要")
            .desc(d -> d.line("设置过滤输出的，可以在背包里预留一些需要的物品比如食物镐子等"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_DEPOSIT_UPGRADE))
            .dependsOn(DEPOSIT_UPGRADE));

    public static final Quest TANK_UPGRADE = CHAPTER.quest("tank_upgrade", q -> q
            .at(0, 4)
            .subtitle("油罐车")
            .desc(d -> d.line("临时接走一些液体存起来"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.TANK_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest SMELTING_UPGRADE = CHAPTER.quest("smelting_upgrade", q -> q
            .at(1, 4)
            .subtitle("这东西背着不热吗")
            .desc(d -> d.line("省时省力"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.SMELTING_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest BLASTING_UPGRADE = CHAPTER.quest("blasting_upgrade", q -> q
            .at(2, 4)
            .subtitle("高速冶炼")
            .desc(d -> d.line("更快速地烧东西"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.BLASTING_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest MAGNET_UPGRADE = CHAPTER.quest("magnet_upgrade", q -> q
            .at(-2, 3)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.MAGNET_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_MAGNET_UPGRADE = CHAPTER.quest("advanced_magnet_upgrade", q -> q
            .at(-3.5, 3)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_MAGNET_UPGRADE))
            .dependsOn(MAGNET_UPGRADE));

    public static final Quest FEEDING_UPGRADE = CHAPTER.quest("feeding_upgrade", q -> q
            .at(2, 3)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.FEEDING_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_FEEDING_UPGRADE = CHAPTER.quest("advanced_feeding_upgrade", q -> q
            .at(3.5, 3)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_FEEDING_UPGRADE))
            .dependsOn(FEEDING_UPGRADE));

    public static final Quest VOID_UPGRADE = CHAPTER.quest("void_upgrade", q -> q
            .at(-2, 0)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.VOID_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_VOID_UPGRADE = CHAPTER.quest("advanced_void_upgrade", q -> q
            .at(-3.5, 0)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_VOID_UPGRADE))
            .dependsOn(VOID_UPGRADE));

    public static final Quest FILTER_UPGRADE = CHAPTER.quest("filter_upgrade", q -> q
            .at(-2, 1)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.FILTER_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_FILTER_UPGRADE = CHAPTER.quest("advanced_filter_upgrade", q -> q
            .at(-3.5, 1)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_FILTER_UPGRADE))
            .dependsOn(FILTER_UPGRADE));

    public static final Quest PICKUP_UPGRADE = CHAPTER.quest("pickup_upgrade", q -> q
            .at(-2, 2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.PICKUP_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_PICKUP_UPGRADE = CHAPTER.quest("advanced_pickup_upgrade", q -> q
            .at(-3.5, 2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_PICKUP_UPGRADE))
            .dependsOn(PICKUP_UPGRADE));

    public static final Quest REFILL_UPGRADE = CHAPTER.quest("refill_upgrade", q -> q
            .at(2, 2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.REFILL_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_REFILL_UPGRADE = CHAPTER.quest("advanced_refill_upgrade", q -> q
            .at(3.5, 2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_REFILL_UPGRADE))
            .dependsOn(REFILL_UPGRADE));

    public static final Quest SMOKING_UPGRADE = CHAPTER.quest("smoking_upgrade", q -> q
            .at(-2, 4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.SMOKING_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest AUTO_SMOKING_UPGRADE = CHAPTER.quest("auto_smoking_upgrade", q -> q
            .at(-2, 5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.AUTO_SMOKING_UPGRADE))
            .dependsOn(SMOKING_UPGRADE));

    public static final Quest AUTO_SMELTING_UPGRADE = CHAPTER.quest("auto_smelting_upgrade", q -> q
            .at(1, 5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.AUTO_SMELTING_UPGRADE))
            .dependsOn(SMELTING_UPGRADE));

    public static final Quest COMPACTING_UPGRADE = CHAPTER.quest("compacting_upgrade", q -> q
            .at(-1, 4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.COMPACTING_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_COMPACTING_UPGRADE = CHAPTER.quest("advanced_compacting_upgrade", q -> q
            .at(-1, 5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_COMPACTING_UPGRADE))
            .dependsOn(COMPACTING_UPGRADE));

    public static final Quest AUTO_BLASTING_UPGRADE = CHAPTER.quest("auto_blasting_upgrade", q -> q
            .at(2, 5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.AUTO_BLASTING_UPGRADE))
            .dependsOn(BLASTING_UPGRADE));

    public static final Quest STACK_UPGRADE_STARTER_TIER = CHAPTER.quest("stack_upgrade_starter_tier", q -> q
            .at(-5, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_STARTER_TIER))
            .dependsOn(UPGRADE_BASE)
            .hideDependencyLines());

    public static final Quest STACK_UPGRADE_TIER_1 = CHAPTER.quest("stack_upgrade_tier_1", q -> q
            .at(-3, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_TIER_1))
            .dependsOn(STACK_UPGRADE_STARTER_TIER));

    public static final Quest STACK_UPGRADE_TIER_2 = CHAPTER.quest("stack_upgrade_tier_2", q -> q
            .at(-1, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_TIER_2))
            .dependsOn(STACK_UPGRADE_TIER_1));

    public static final Quest STACK_UPGRADE_TIER_3 = CHAPTER.quest("stack_upgrade_tier_3", q -> q
            .at(1, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_TIER_3))
            .dependsOn(STACK_UPGRADE_TIER_2));

    public static final Quest STACK_UPGRADE_TIER_4 = CHAPTER.quest("stack_upgrade_tier_4", q -> q
            .at(3, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_TIER_4))
            .dependsOn(STACK_UPGRADE_TIER_3));

    public static final Quest STACK_UPGRADE_OMEGA_TIER = CHAPTER.quest("stack_upgrade_omega_tier", q -> q
            .at(5, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_OMEGA_TIER))
            .dependsOn(STACK_UPGRADE_TIER_4));

    public static final Quest COPPER_BACKPACK = CHAPTER.quest("copper_backpack", q -> q
            .at(-6, -4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.COPPER_BACKPACK))
            .dependsOn(UPGRADE_BASE)
            .hideDependencyLines());

    public static final Quest IRON_BACKPACK = CHAPTER.quest("iron_backpack", q -> q
            .at(-3, -4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.IRON_BACKPACK))
            .dependsOn(COPPER_BACKPACK, UPGRADE_BASE)
            .requireAny()
            .hideDependencyLines());

    public static final Quest GOLD_BACKPACK = CHAPTER.quest("gold_backpack", q -> q
            .at(0, -4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.GOLD_BACKPACK))
            .reward(Reward.item(Items.GOLD_INGOT, 8))
            .dependsOn(IRON_BACKPACK));

    public static final Quest DIAMOND_BACKPACK = CHAPTER.quest("diamond_backpack", q -> q
            .at(3, -4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.DIAMOND_BACKPACK))
            .dependsOn(GOLD_BACKPACK));

    public static final Quest NETHERITE_BACKPACK = CHAPTER.quest("netherite_backpack", q -> q
            .at(6, -4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.NETHERITE_BACKPACK))
            .dependsOn(DIAMOND_BACKPACK));
}
