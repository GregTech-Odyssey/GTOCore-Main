package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.machines.GTAEMachines;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.reward.QuestStacks;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.enchantment.Enchantments;

import appeng.core.definitions.AEBlocks;

import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.shadowsoffire.apotheosis.ench.Ench;
import earth.terrarium.adastra.common.registry.ModItems;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class UtilChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("util", c -> c
            .title("辅助章节")
            .icon(ItemsRegistry.BLANK_PARCHMENT)
            .alwaysInvisible()
            .disableToast()
            .autofocus(UtilChapter.ANY_BEDS));

    public static final Quest ANY_BEDS = CHAPTER.quest("any_beds", q -> q
            .at(0.5, 0)
            .icon(Items.WHITE_BED)
            .desc(d -> d.line("完成此任务跳过木软锤"))
            .task(Task.tag(ItemTags.BEDS, 1).title("床"))
            .requireAnyStarted()
            .invisible().invisibleUntilTasks(9999).disableToast());

    public static final Quest INFINITY_WAND = CHAPTER.quest("infinity_wand", q -> q
            .at(2.5, 0)
            .task(Task.item(QuestExternalRefs.ConstructionWandItems.INFINITY_WAND).consume(false))
            .requireAnyStarted()
            .repeatable(false).hideUntilDepsVisible().hideUntilDepsComplete().hideDetailsUntilStartable().hideDependencyLines().invisible().hideTextUntilComplete().hideDependentLines().hideLockIcon().disableToast().flexible());

    public static final Quest PRECISION_STEAM_MECHANISM = CHAPTER.quest("precision_steam_mechanism", q -> q
            .at(4.5, 0)
            .task(Task.item(GTOItems.PRECISION_STEAM_MECHANISM))
            .invisible().disableToast());

    public static final Quest LABOR_PROTECTION_SUIT_SET = CHAPTER.quest("labor_protection_suit_set", q -> q
            .at(0.5, 2)
            .icon(GTItems.NANO_HELMET)
            .title("劳保服套装")
            .task(Task.stat(Stats.DEATHS, 1))
            .reward(
                    Reward.stack(() -> QuestStacks.trimmed(QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.GOLDEN_HELMET), Enchantments.ALL_DAMAGE_PROTECTION, 3), Enchantments.PROJECTILE_PROTECTION, 5), TrimMaterials.REDSTONE, TrimPatterns.HOST), 1).personal().name("员工头盔").autoClaim(AutoClaim.ENABLED),
                    Reward.stack(() -> QuestStacks.trimmed(QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.LEATHER_CHESTPLATE), Enchantments.THORNS, 3), Enchantments.BLAST_PROTECTION, 2), TrimMaterials.REDSTONE, TrimPatterns.HOST), 1).personal().name("员工劳保服").autoClaim(AutoClaim.ENABLED),
                    Reward.stack(() -> QuestStacks.trimmed(QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.CHAINMAIL_LEGGINGS), Ench.Enchantments.REBOUNDING.get(), 1), Enchantments.UNBREAKING, 2), TrimMaterials.REDSTONE, TrimPatterns.HOST), 1).personal().name("员工劳动裤").autoClaim(AutoClaim.ENABLED),
                    Reward.stack(() -> QuestStacks.trimmed(QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.IRON_BOOTS), Enchantments.FIRE_PROTECTION, 5), Enchantments.FALL_PROTECTION, 1), TrimMaterials.REDSTONE, TrimPatterns.HOST), 1).personal().name("员工劳保靴").autoClaim(AutoClaim.ENABLED))
            .invisible());

    public static final Quest OPEN_PATH_AMULET = CHAPTER.quest("open_path_amulet", q -> q
            .at(2.5, 2)
            .icon(ItemsRegistry.BLANK_PARCHMENT)
            .title("开路护符")
            .task(Task.stat(Stats.DEATHS, 5))
            .reward(Reward.item(Items.TOTEM_OF_UNDYING).name("员工生活质量保障证(一次性)").autoClaim(AutoClaim.ENABLED))
            .invisible());

    public static final Quest CHARGING_SWORD = CHAPTER.quest("charging_sword", q -> q
            .at(4.5, 2)
            .title("冲锋之剑")
            .task(Task.stat(Stats.DEATHS, 10))
            .reward(
                    Reward.stack(() -> QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.NETHERITE_SWORD), Enchantments.SHARPNESS, 5), Enchantments.SMITE, 6), 1).personal().name("员工基础生活保障剑").autoClaim(AutoClaim.ENABLED))
            .invisible());

    public static final Quest ORDER_BOW = CHAPTER.quest("order_bow", q -> q
            .at(6.5, 2)
            .icon(ExtraBotanyItems.failnaught)
            .title("秩序之弓")
            .task(Task.stat(Stats.DEATHS, 15))
            .reward(
                    Reward.stack(() -> QuestStacks.enchanted(QuestStacks.enchanted(QuestStacks.enchanted(QuestStacks.enchanted(new ItemStack(Items.BOW), Enchantments.INFINITY_ARROWS, 1), Enchantments.FLAMING_ARROWS, 1), Enchantments.PUNCH_ARROWS, 3), Enchantments.POWER_ARROWS, 2), 1).personal().name("员工清扫弓").autoClaim(AutoClaim.ENABLED),
                    Reward.item(Items.ARROW).personal().autoClaim(AutoClaim.ENABLED))
            .invisible());

    public static final Quest PATCH_ASSIST = CHAPTER.quest("patch_assist", q -> q
            .at(6.5, 0)
            .title("贴片辅助")
            .task(Task.stat(Stats.JUMP, 1))
            .dependsOn(LvChapter.LV_POWER_AMPLIFIERS, ApotheosisChapter.RARITY_FORGE)
            .requireAny()
            .invisible().disableToast());

    public static final Quest Q_59430E02 = CHAPTER.quest("q_59430e02", q -> q
            .at(-1.5, 2)
            .desc(d -> d
                    .line("{锡石矿脉：}{锡}—{锡石}", rgb(0x89847E), rgb(0xFAFEFF), rgb(0x89847E))
                    .line("{铝土矿脉：铝土矿}—{钛铁矿}—{铝}", rgb(0xCFB853), rgb(0x2B2A24), rgb(0x7DB9D8))
                    .line("{磁铁矿脉：磁铁矿}—{钒磁铁矿}—{金}", rgb(0x9D9D9D), rgb(0x505D70), rgb(0xFDF55F))
                    .line("{沥青铀矿脉：沥青铀矿}—{晶质铀矿}", rgb(0xFFD647), rgb(0xFFD52E))
                    .line("{独居石矿脉：氟碳镧铈矿}—{独居石}—{钕}", rgb(0xCAAB60), rgb(0xD0EE98), rgb(0x6C5863))
                    .line("{皂滑矿脉：皂石}—{滑石}—{海绿石砂}—{镍黄铁矿}", rgb(0x5A7261), rgb(0xEBFFE9), rgb(0x1DA351), rgb(0xE3CF13))
                    .line("{锡石榴石矿脉：}{锡石矿砂}—{石榴石砂}—{石棉}—{硅藻土}", rgb(0xCC4C25), rgb(0x89847E), rgb(0xCC4C25), rgb(0xE6E6E6), rgb(0xFFFAFA))
                    .line("{白钨矿脉：白钨矿}—{钨酸锂}—{锂}", rgb(0xD7E8B3), rgb(0x3B3A32), rgb(0xD7E7EE))
                    .line("{云母矿脉：}{蓝晶石}—{云母}—{铯榴石}", rgb(0xECFEFF), rgb(0xD5FFFF), rgb(0xECFEFF), rgb(0xEED9E1))
                    .line("{磷灰石矿脉：磷灰石}—{磷酸三钙}—{烧绿石}", rgb(0x06CDF1), rgb(0xFFFDDB), rgb(0x5B4838))
                    .line("{铜矿脉：}{黄铜矿}—{铁}—{黄铁矿}—{铜}", rgb(0xE77C56), rgb(0x96C185), rgb(0xEEEEEE), rgb(0xFFFEE6), rgb(0xE77C56))
                    .line("{硝石矿脉：硝石}—{硅藻土}—{蓝石}—{明矾石}", rgb(0xE6E6E6), rgb(0xFFFAFA), rgb(0x83CBF5), rgb(0xFBD677))
                    .line("{铁矿脉：针铁矿}—{黄褐铁矿}—{赤铁矿}—{孔雀石}", rgb(0x97873A), rgb(0xF5E315), rgb(0xFF7161), rgb(0x00F1B0))
                    .line("{石榴石矿脉：红石榴石}—{黄石榴石}—{紫水晶}—{猫眼石}", rgb(0x950C15), rgb(0xF6FF09), rgb(0xCFA0F3), rgb(0xD8BFD8))
                    .line("{矿砂矿脉：玄武岩矿砂}—{花岗岩矿砂}—{漂白土}—{石膏}", rgb(0x5C5C5C), rgb(0xD69077), rgb(0xF3EFBB), rgb(0xFFFAEC))
                    .line("{谢尔顿矿脉：}{斑铜矿}—{谢尔顿矿}—{铂}—{钯}", rgb(0xFFF4BA), rgb(0xFFE05A), rgb(0x665F2F), rgb(0xFFF4BA), rgb(0xBD92B5))
                    .line("{黝铜矿脉：黝铜矿}—{铜}—{辉锑矿}", rgb(0xA3A09B), rgb(0xE77C56), rgb(0x656565))
                    .line("{铬铁矿脉：磁铁矿}—{钒磁铁矿}—{铬铁矿}", rgb(0x9D9D9D), rgb(0x2B2A24), rgb(0xC5C1A8))
                    .line("{带状铁矿脉：针铁矿}—{黄褐铁矿}—{赤铁矿}—{金}", rgb(0x97873A), rgb(0xF5E315), rgb(0xFF7161), rgb(0xFDF55F))
                    .line("{钼矿脉：}{钼铅矿}—{辉钼矿}—{钼}—{钼钙矿}", rgb(0xE3DDC3), rgb(0xFF9000), rgb(0xE3DDC3), rgb(0x404068), rgb(0xD8CFAC))
                    .line("{硫矿脉：硫}—{黄铁矿}—{闪锌矿}", rgb(0xFDFF31), rgb(0xFFFEE6), rgb(0xFFDC88))
                    .line("{方铅矿脉：方铅矿}—{银}—{铅}", rgb(0xF3E8FA), rgb(0xDCDCFF), rgb(0x7E6F82))
                    .line("{钻石矿脉：}{石墨}—{钻石}—{煤炭}", rgb(0xC8FFFF), rgb(0xA8A89E), rgb(0xC8FFFF), rgb(0x393E41))
                    .line("{橄榄石矿脉：}{膨润土}—{磁铁矿}—{橄榄石}—{海绿石砂}", rgb(0xA7E404), rgb(0xEDE8A3), rgb(0x9D9D9D), rgb(0xA7E404), rgb(0x1DA351))
                    .line("{戴斯矿脉：戴斯}—{菱镁矿}", rgb(0xF2A057), rgb(0xFBFBF6))
                    .line("{铜锡矿脉：黄铜矿}—{沸石}—{锡石}—{雄黄}", rgb(0x96C185), rgb(0xF2E3E0), rgb(0x89847E), rgb(0xFF3D33))
                    .line("{镍矿脉：}{硅镁镍矿}—{镍}—{辉钴矿}—{镍黄铁矿}", rgb(0xCCDFF5), rgb(0x32C880), rgb(0xCCDFF5), rgb(0x1975FF), rgb(0xE3CF13))
                    .line("{锰矿脉：钙铝榴石}—{锰铝榴石}—{软锰矿}—{钽铁矿}", rgb(0xFFB777), rgb(0xFFA81E), rgb(0xC7B5AB), rgb(0x4E6B94))
                    .line("{黄玉矿脉：}{蓝黄玉}—{黄玉}—{辉铜矿}—{斑铜矿}", rgb(0xE8D73A), rgb(0xDBFEFF), rgb(0xE8D73A), rgb(0x657882), rgb(0xFFE05A))
                    .line("{耐热矿脉：}{耐热金属}—{钴}—{烧绿石}", rgb(0xC8FFFF), rgb(0xE65757), rgb(0x5050FA), rgb(0x5B4838))
                    .line("{紫金矿脉：}{紫金}—{金}", rgb(0xFDF55F), rgb(0xE5939B), rgb(0xFDF55F))
                    .line("{硅岩矿脉：硅岩}—{钚}", rgb(0x323232), rgb(0xBA2727))
                    .line("{铍矿脉：铍}—{绿宝石}", rgb(0x73D73D), rgb(0x17FF6C))
                    .line("{钛矿脉：钛铁矿}—{钛}", rgb(0x2B2A24), rgb(0xED8EEA))
                    .line("{红石矿脉：红石}—{红宝石}—{朱砂}", rgb(0xFF0000), rgb(0xD72310), rgb(0xFF335F))
                    .line("{蓝宝石矿脉：}{铁铝榴石}—{镁铝榴石}—{蓝宝石}—{绿色蓝宝石}", rgb(0x3235E3), rgb(0xA21717), rgb(0xE81958), rgb(0x3235E3), rgb(0x9AE6B0))
                    .line("{天青石矿脉：}{天青石}—{谢尔顿矿}—{天然碱}", rgb(0xE6E6A5), rgb(0xDE953C), rgb(0x665F2F), rgb(0xE6E6A5))
                    .line("{锆石矿脉：锆石}—{钨}—{软锰矿}—{钽铁矿}", rgb(0xB99B7E), rgb(0x3B3A32), rgb(0xC7B5AB), rgb(0x4E6B94)))
            .task(Task.checkmark())
            .hideUntilDepsComplete().invisible());

    public static final Quest Q_18E057F3 = CHAPTER.quest("q_18e057f3", q -> q
            .at(2.5, 4)
            .task(Task.difficulty(2))
            .invisible());

    public static final Quest Q_1FB1EC5B = CHAPTER.quest("q_1fb1ec5b", q -> q
            .at(4.5, 4)
            .task(Task.difficulty(3))
            .invisible());

    public static final Quest Q_056EC4E6 = CHAPTER.quest("q_056ec4e6", q -> q
            .at(0.5, 4)
            .task(Task.difficulty(1))
            .invisible());

    public static final Quest NORMAL_EXPERT = CHAPTER.quest("normal_expert", q -> q
            .at(6.5, 4)
            .title("普通＋专家")
            .task(Task.difficulty(2).optional(), Task.difficulty(3).optional())
            .invisible());

    public static final Quest INSCRIBER_EASY = CHAPTER.quest("inscriber_easy", q -> q
            .at(-5.5, 2)
            .title("压印器{（简单）}", GREEN)
            .desc(d -> d.line("使用压印器压印AE电路板"))
            .task(Task.item(AEBlocks.INSCRIBER))
            .dependsOn(Q_056EC4E6)
            .hideUntilDepsVisible());

    public static final Quest ME_WILDCARD_PATTERN_BUFFER = CHAPTER.quest("me_wildcard_pattern_buffer", q -> q
            .at(-5.5, 4)
            .desc(d -> d.line("直接在EMI里搜索\"任何\"就能看到通配符，拖进样板即可。不要漏了虚拟物品！"))
            .task(Task.item(GTAEMachines.ME_WILDCARD_PATTERN_BUFFER))
            .dependsOn(AeChapter.ME_PATTERN_BUFFER, Q_056EC4E6, LuvChapter.HPCA_ADVANCED_COMPUTATION_COMPONENT)
            .hideUntilDepsVisible().hideDependencyLines());

    public static final Quest ME_WILDCARD_PATTERN_BUFFER_2 = CHAPTER.quest("me_wildcard_pattern_buffer_2", q -> q
            .at(-5.5, 6)
            .desc(d -> d.line("直接在EMI里搜索\"任何\"就能看到通配符，拖进样板即可。不要漏了虚拟物品！"))
            .task(Task.item(GTAEMachines.ME_WILDCARD_PATTERN_BUFFER))
            .dependsOn(AeChapter.ME_PATTERN_BUFFER, NORMAL_EXPERT, UhvChapter.QUICKSILVER_BUCKET, Magic2Chapter.ORICHALCOS_INGOT)
            .hideUntilDepsVisible());

    public static final Quest EXPERT_MAINLINE_LOCK = CHAPTER.quest("expert_mainline_lock", q -> q
            .at(6.5, 6)
            .title("专家主线锁")
            .task(Task.difficulty(1).optional(), Task.difficulty(2).optional(), Task.item(Items.PAPER).optional())
            .invisible());

    public static final Quest EASY_NORMAL = CHAPTER.quest("easy_normal", q -> q
            .at(4.5, 6)
            .title("简单＋普通")
            .task(Task.difficulty(1).optional(), Task.difficulty(2).optional())
            .invisible());

    public static final Quest ENDER_EYE = CHAPTER.quest("ender_eye", q -> q
            .at(-1.5, 0)
            .task(Task.item(Items.ENDER_EYE))
            .invisible().disableToast());

    public static final Quest RAW_DESH = CHAPTER.quest("raw_desh", q -> q
            .at(8.5, 0)
            .task(Task.item(ModItems.RAW_DESH.get(), 20))
            .invisible().disableToast());
}
