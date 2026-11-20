package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestShape;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class BackpackChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("backpack", c -> c
            .title("摩登时代")
            .subtitle("Sophisticated Backpacks")
            .icon(QuestExternalRefs.SophisticatedBackpacksItems.BACKPACK));

    public static final Quest Q_1CE34BD4 = CHAPTER.quest("q_1ce34bd4", q -> q
            .at(2, -1)
            .desc(d -> d
                    .line("这里有一些存储方式")
                    .line()
                    .line("背包来装多方块的方块去部署多方块机器很方便")
                    .line()
                    .line("超级容器对于大量同种物品的存储很在行"))
            .task(Task.checkmark().title("欢迎来到摩登时代"))
            .dependsOn(StoneageChapter.BACKPACK_TIME));

    public static final Quest UPGRADE_BASE = CHAPTER.quest("upgrade_base", q -> q
            .at(2, -2.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.UPGRADE_BASE))
            .reward(Reward.item(GTOTagPrefix.COIN, GTMaterials.Copper, 16))
            .dependsOn(Q_1CE34BD4));

    public static final Quest RESTOCK_UPGRADE = CHAPTER.quest("restock_upgrade", q -> q
            .at(4, -3)
            .subtitle("打包带走")
            .desc(d -> d.line("安装好模块之后，右键容器即可将物品存入背包"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.RESTOCK_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest DEPOSIT_UPGRADE = CHAPTER.quest("deposit_upgrade", q -> q
            .at(4, -4)
            .subtitle("一键倾倒")
            .desc(d -> d.line("用于快速卸下物品，如矿石"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.DEPOSIT_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_RESTOCK_UPGRADE = CHAPTER.quest("advanced_restock_upgrade", q -> q
            .at(5.5, -3)
            .subtitle("过滤输入")
            .desc(d -> d
                    .line("可以过滤要拿取什么物品")
                    .line()
                    .line("但标签模式能设置标签的物品有点少"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_RESTOCK_UPGRADE))
            .dependsOn(RESTOCK_UPGRADE));

    public static final Quest ADVANCED_DEPOSIT_UPGRADE = CHAPTER.quest("advanced_deposit_upgrade", q -> q
            .at(5.5, -4)
            .subtitle("噢不不不 ，这东西我需要")
            .desc(d -> d.line("设置过滤输出的，可以在背包里预留一些需要的物品比如食物镐子等"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_DEPOSIT_UPGRADE))
            .dependsOn(DEPOSIT_UPGRADE));

    public static final Quest TANK_UPGRADE = CHAPTER.quest("tank_upgrade", q -> q
            .at(2, -4.5)
            .subtitle("油罐车")
            .desc(d -> d.line("临时接走一些液体存起来"))
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.TANK_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest MAGNET_UPGRADE = CHAPTER.quest("magnet_upgrade", q -> q
            .at(0, -3)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.MAGNET_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_MAGNET_UPGRADE = CHAPTER.quest("advanced_magnet_upgrade", q -> q
            .at(-1.5, -3)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_MAGNET_UPGRADE))
            .dependsOn(MAGNET_UPGRADE));

    public static final Quest FEEDING_UPGRADE = CHAPTER.quest("feeding_upgrade", q -> q
            .at(0, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.FEEDING_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_FEEDING_UPGRADE = CHAPTER.quest("advanced_feeding_upgrade", q -> q
            .at(-1.5, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_FEEDING_UPGRADE))
            .dependsOn(FEEDING_UPGRADE));

    public static final Quest VOID_UPGRADE = CHAPTER.quest("void_upgrade", q -> q
            .at(0, -4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.VOID_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_VOID_UPGRADE = CHAPTER.quest("advanced_void_upgrade", q -> q
            .at(-1.5, -4)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_VOID_UPGRADE))
            .dependsOn(VOID_UPGRADE));

    public static final Quest REFILL_UPGRADE = CHAPTER.quest("refill_upgrade", q -> q
            .at(4, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.REFILL_UPGRADE))
            .dependsOn(UPGRADE_BASE));

    public static final Quest ADVANCED_REFILL_UPGRADE = CHAPTER.quest("advanced_refill_upgrade", q -> q
            .at(5.5, -2)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.ADVANCED_REFILL_UPGRADE))
            .dependsOn(REFILL_UPGRADE));

    public static final Quest STACK_UPGRADE_STARTER_TIER = CHAPTER.quest("stack_upgrade_starter_tier", q -> q
            .at(-3, -5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_STARTER_TIER))
            .dependsOn(UPGRADE_BASE)
            .hideDependencyLines());

    public static final Quest STACK_UPGRADE_TIER_1 = CHAPTER.quest("stack_upgrade_tier_1", q -> q
            .at(-1, -5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_TIER_1))
            .dependsOn(STACK_UPGRADE_STARTER_TIER));

    public static final Quest STACK_UPGRADE_TIER_2 = CHAPTER.quest("stack_upgrade_tier_2", q -> q
            .at(1, -5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_TIER_2))
            .dependsOn(STACK_UPGRADE_TIER_1));

    public static final Quest STACK_UPGRADE_TIER_3 = CHAPTER.quest("stack_upgrade_tier_3", q -> q
            .at(3, -5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_TIER_3))
            .dependsOn(STACK_UPGRADE_TIER_2));

    public static final Quest STACK_UPGRADE_TIER_4 = CHAPTER.quest("stack_upgrade_tier_4", q -> q
            .at(5, -5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_TIER_4))
            .dependsOn(STACK_UPGRADE_TIER_3));

    public static final Quest STACK_UPGRADE_OMEGA_TIER = CHAPTER.quest("stack_upgrade_omega_tier", q -> q
            .at(7, -5.5)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.STACK_UPGRADE_OMEGA_TIER))
            .dependsOn(STACK_UPGRADE_TIER_4));

    public static final Quest COPPER_BACKPACK = CHAPTER.quest("copper_backpack", q -> q
            .at(-4, -7)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.COPPER_BACKPACK))
            .dependsOn(UPGRADE_BASE)
            .hideDependencyLines());

    public static final Quest IRON_BACKPACK = CHAPTER.quest("iron_backpack", q -> q
            .at(-1, -7)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.IRON_BACKPACK))
            .dependsOn(COPPER_BACKPACK)
            .requireAny());

    public static final Quest GOLD_BACKPACK = CHAPTER.quest("gold_backpack", q -> q
            .at(2, -7)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.GOLD_BACKPACK))
            .reward(Reward.item(Items.GOLD_INGOT, 8))
            .dependsOn(IRON_BACKPACK));

    public static final Quest DIAMOND_BACKPACK = CHAPTER.quest("diamond_backpack", q -> q
            .at(5, -7)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.DIAMOND_BACKPACK))
            .dependsOn(GOLD_BACKPACK));

    public static final Quest NETHERITE_BACKPACK = CHAPTER.quest("netherite_backpack", q -> q
            .at(8, -7)
            .task(Task.item(QuestExternalRefs.SophisticatedBackpacksItems.NETHERITE_BACKPACK))
            .dependsOn(DIAMOND_BACKPACK));

    public static final Quest DRAWER_STORAGE = CHAPTER.quest("drawer_storage", q -> q
            .at(4.5, -0.5)
            .shape(QuestShape.CIRCLE)
            .subtitle("“专精”型人才")
            .desc(d -> d
                    .line("可以在输入总线中放入{超级箱、超级缸}。", YELLOW)
                    .line()
                    .line("放的超级容器越多，设置的总类别数越少，使用的密封机械方块等级越高，容量就越大。")
                    .line()
                    .line("{1+1>2 }", DARK_GRAY.italic()))
            .task(Task.item(MultiBlockG.DRAWER_STORAGE).optional())
            .disableToast()
            .dependsOn(Q_1CE34BD4));

    public static final Quest LV_SUPER_CHEST = CHAPTER.quest("lv_super_chest", q -> q
            .at(1.5, 1.5)
            .title("超级箱")
            .subtitle("风味物品抽屉")
            .icon(GTMachines.SUPER_CHEST[GTValues.LV])
            .task(Task.anyOf(1, GTMachines.SUPER_CHEST[GTValues.ULV], GTMachines.SUPER_CHEST[GTValues.LV], GTMachines.SUPER_CHEST[GTValues.MV], GTMachines.SUPER_CHEST[GTValues.HV], GTMachines.SUPER_CHEST[GTValues.EV], BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "oak_super_chest")), GTMachines.QUANTUM_CHEST[GTValues.IV], GTMachines.QUANTUM_CHEST[GTValues.LuV], GTMachines.QUANTUM_CHEST[GTValues.ZPM], GTMachines.QUANTUM_CHEST[GTValues.UV], GTMachines.QUANTUM_CHEST[GTValues.UHV], GTMachines.QUANTUM_CHEST[GTValues.UEV], GTMachines.QUANTUM_CHEST[GTValues.UIV], GTMachines.QUANTUM_CHEST[GTValues.UXV], GTMachines.QUANTUM_CHEST[GTValues.OpV]).optional())
            .dependsOn(Q_1CE34BD4));

    public static final Quest LV_SUPER_TANK = CHAPTER.quest("lv_super_tank", q -> q
            .at(2.5, 1.5)
            .title("超级缸")
            .subtitle("风味流体抽屉")
            .icon(GTMachines.SUPER_TANK[GTValues.LV])
            .task(Task.anyOf(1, GTMachines.SUPER_TANK[GTValues.ULV], GTMachines.SUPER_TANK[GTValues.LV], GTMachines.SUPER_TANK[GTValues.MV], GTMachines.SUPER_TANK[GTValues.HV], GTMachines.SUPER_TANK[GTValues.EV], BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "oak_super_tank")), GTMachines.QUANTUM_TANK[GTValues.IV], GTMachines.QUANTUM_TANK[GTValues.LuV], GTMachines.QUANTUM_TANK[GTValues.ZPM], GTMachines.QUANTUM_TANK[GTValues.UV], GTMachines.QUANTUM_TANK[GTValues.UHV], GTMachines.QUANTUM_TANK[GTValues.UEV], GTMachines.QUANTUM_TANK[GTValues.UIV], GTMachines.QUANTUM_TANK[GTValues.UXV], GTMachines.QUANTUM_TANK[GTValues.OpV]).optional())
            .disableToast()
            .dependsOn(Q_1CE34BD4));

    public static final Quest BEDROCK = CHAPTER.quest("bedrock", q -> q
            .at(4, 1)
            .title("多格超级容器")
            .desc(d -> d
                    .line("通过使用我们最先进的{钠钙硅酸盐合金板分割技术}，我们得以将超级容器的存储格增加到最多4格。", YELLOW)
                    .line()
                    .line("{其实就是拿玻璃板隔开了}", DARK_GRAY.italic()))
            .task(Task.anyOf(1, BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "oak_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "oak_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "ulv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "lv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "mv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "hv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "ev_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "ulv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "lv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "mv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "hv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "ev_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "iv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "luv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "zpm_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uhv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uev_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uiv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uxv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "opv_super_tank_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "iv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "luv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "zpm_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uhv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uev_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uiv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uxv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "opv_super_tank_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "oak_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "oak_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "lv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "ulv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "mv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "hv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "ev_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "ulv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "lv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "mv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "hv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "ev_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "iv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "luv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "zpm_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uhv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uev_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uiv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uxv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "opv_super_chest_1x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "iv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "luv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "zpm_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uev_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uhv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uxv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "uiv_super_chest_2x2")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtceu", "opv_super_chest_2x2"))).optional())
            .dependsOn(Q_1CE34BD4)
            .disableToast());

    public static final Quest ULV_SUPER_CHEST_REPLACER = CHAPTER.quest("ulv_super_chest_replacer", q -> q
            .at(0, 1)
            .title("超级容器替换器")
            .subtitle("锭去哪了")
            .desc(d -> d
                    .line("直接把旧超级容器升级成新的，旧的会返还给你"))
            .task(Task.anyOf(1, BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "ulv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "lv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "mv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "hv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "ev_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "iv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "luv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "zpm_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uhv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uev_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uiv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "opv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uxv_super_chest_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "ulv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "lv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "mv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "hv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "ev_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "iv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "luv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "zpm_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uhv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uev_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uiv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "uxv_super_tank_replacer")), BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "opv_super_tank_replacer"))).optional())
            .disableToast()
            .dependsOn(Q_1CE34BD4));

    public static final Quest SUPER_CHEST_CONTROLLER = CHAPTER.quest("super_chest_controller", q -> q
            .at(-0.5, -0.5)
            .title("超级仓储链接器")
            .desc(d -> d
                    .line("专属于超级容器的超立方体。")
                    .line()
                    .line("将{超级容器}或{桥接器}链接到这个链接器上，对{桥接器}或{链接器}I/O等同于对{全体链接目标}I/O", YELLOW, GREEN, GREEN, GREEN, YELLOW))
            .task(Task.item(BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "super_chest_controller"))).optional(), Task.item(BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "super_chest_controller_extension"))).optional(), Task.item(BuiltInRegistries.ITEM.get(RLUtils.fromNamespaceAndPath("gtocore", "quantum_chest_configurator"))).optional())
            .disableToast()
            .dependsOn(Q_1CE34BD4));
}
