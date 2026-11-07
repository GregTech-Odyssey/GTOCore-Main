package vazkii.botania.common;

import com.gtocore.common.item.RewardBagItem;
import com.gtocore.data.lootTables.RewardBagLoot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import com.gto.registrate.util.entry.ItemEntry;

import vazkii.botania.common.handler.ExtraBotanySounds;
import vazkii.botania.common.item.BotaniaArmorMaterials;
import vazkii.botania.common.item.BotaniaTiers;
import vazkii.botania.common.item.relic.PandorasBoxItem;

/**
 * 额外植物学物品复刻。按内容类型分组。
 */
public final class ExtraBotanyItems {

    private ExtraBotanyItems() {}

    public static void init() {}

    /** 奖励袋：右键按战利品表开出奖励，音效沿用原额外植物学。 */
    private static ItemEntry<Item> rewardBag(String id, String cn, String en, ResourceLocation lootTable) {
        return BotaniaReplica.EXTRABOTANY.item(id, cn, en,
                p -> new RewardBagItem(p, lootTable, 64, () -> ExtraBotanySounds.REWARD_BAG_OPEN.get()));
    }

    // ==================== 材料与其它物品 ====================

    public static final ItemEntry<Item> MANASTEEL_SHIELD = BotaniaReplica.EXTRABOTANY.shield("manasteel_shield", "魔力钢盾牌", "Manasteel Shield");

    public static final ItemEntry<Item> ELEMENTIUM_SHIELD = BotaniaReplica.EXTRABOTANY.shield("elementium_shield", "源质钢盾牌", "Elementium Shield");

    public static final ItemEntry<Item> TERRASTEEL_SHIELD = BotaniaReplica.EXTRABOTANY.shield("terrasteel_shield", "泰拉钢盾牌", "Terrasteel Shield");

    public static final ItemEntry<Item> ACHILLES_SHIELD = BotaniaReplica.EXTRABOTANY.shield("achilles_shield", "勇士盾", "Achilles Shield");

    public static final ItemEntry<Item> MAGIC_FINGER = BotaniaReplica.EXTRABOTANY.item("magic_finger", "魔术手指", "Magic Finger");

    /** 原额外植物学「天空锭」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> AERIALITE_INGOT = BotaniaReplica.EXTRABOTANY.item("aerialite_ingot", "天空锭", "Aerialite Ingot");

    /** 原额外植物学「天空粒」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> AERIALITE_NUGGET = BotaniaReplica.EXTRABOTANY.item("aerialite_nugget", "天空粒", "Aerialite Nugget");

    /** 原额外植物学「源质石英」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> ELEMENTIUM_QUARTZ = BotaniaReplica.EXTRABOTANY.item("elementium_quartz", "源质石英", "Elementium Quartz");

    /** 原额外植物学「盖亚石英」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> GAIA_QUARTZ = BotaniaReplica.EXTRABOTANY.item("gaia_quartz", "盖亚石英", "Gaia Quartz");

    /** 原额外植物学「奥利哈钢」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> ORICHALCOS_INGOT = BotaniaReplica.EXTRABOTANY.item("orichalcos_ingot", "奥利哈钢", "Orichalcos Ingot");

    /** 原额外植物学「奥利哈钢粒」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> ORICHALCOS_NUGGET = BotaniaReplica.EXTRABOTANY.item("orichalcos_nugget", "奥利哈钢粒", "Orichalcos Nugget");

    /** 原额外植物学「光子锭」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> PHOTONIUM_INGOT = BotaniaReplica.EXTRABOTANY.item("photonium_ingot", "光子锭", "Photonium Ingot");

    /** 原额外植物学「光子粒」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> PHOTONIUM_NUGGET = BotaniaReplica.EXTRABOTANY.item("photonium_nugget", "光子粒", "Photonium Nugget");

    /** 原额外植物学「暗影锭」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> SHADOWIUM_INGOT = BotaniaReplica.EXTRABOTANY.item("shadowium_ingot", "暗影锭", "Shadowium Ingot");

    /** 原额外植物学「暗影粒」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> SHADOWIUM_NUGGET = BotaniaReplica.EXTRABOTANY.item("shadowium_nugget", "暗影粒", "Shadowium Nugget");

    /** 原额外植物学「%s圣水手雷」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> HOLY_WATER_GRENADE = BotaniaReplica.EXTRABOTANY.item("holy_water_grenade", "%s圣水手雷", "%s Holy Water Grenade");

    /** 原额外植物学「文文的相机」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> CAMERA = BotaniaReplica.EXTRABOTANY.item("camera", "文文的相机", "Shameimaru's Camera");

    /** 原额外植物学「空之律者核心」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> CORE_OF_THE_VOID = BotaniaReplica.EXTRABOTANY.item("core_of_the_void", "空之律者核心", "Core of The Void");

    /** 原额外植物学「莱茵河的黄金」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> DAS_RHEINGOLD = BotaniaReplica.EXTRABOTANY.item("das_rheingold", "莱茵河的黄金", "Das Rheingold");

    /** 原额外植物学「Drei奖励袋」：开出植物魔法材料。 */
    public static final ItemEntry<Item> DREI_REWARD_BAG = rewardBag("drei_reward_bag", "Drei奖励袋", "Drei Reward Bag", RewardBagLoot.DREI_REWARD_BAG_LOOT);

    /** 原额外植物学「Eins奖励袋」：开出随机一种颜色的 6 个花瓣。 */
    public static final ItemEntry<Item> EINS_REWARD_BAG = rewardBag("eins_reward_bag", "Eins奖励袋", "Eins Reward Bag", RewardBagLoot.EINS_REWARD_BAG_LOOT);

    /** 原额外植物学「九又四分之三奖励袋」：开出各档奖励袋与盖亚素材。 */
    public static final ItemEntry<Item> NINE_AND_THREE_QUARTERS_REWARD_BAG = rewardBag("nine_and_three_quarters_reward_bag", "九又四分之三奖励袋", "Nine and Three-Quarters Reward Bag", RewardBagLoot.NINE_AND_THREE_QUARTERS_REWARD_BAG_LOOT);

    /** 原额外植物学「潘多拉魔盒」：盖亚守护者 III 的遗物，开出各档奖励袋与英雄勋章。 */
    public static final ItemEntry<Item> PANDORAS_BOX = BotaniaReplica.EXTRABOTANY.item("pandoras_box", "潘多拉魔盒", "Pandora's Box",
            p -> new PandorasBoxItem(p, RewardBagLoot.PANDORAS_BOX_LOOT));

    /** 原额外植物学「Vier奖励袋」：开出原版材料与虚空万藏。 */
    public static final ItemEntry<Item> VIER_REWARD_BAG = rewardBag("vier_reward_bag", "Vier奖励袋", "Vier Reward Bag", RewardBagLoot.VIER_REWARD_BAG_LOOT);

    /** 原额外植物学「虚空万藏」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> VOID_ARCHIVES = BotaniaReplica.EXTRABOTANY.item("void_archives", "虚空万藏", "Void Archives");

    /** 原额外植物学「Zwei奖励袋」：开出符文。 */
    public static final ItemEntry<Item> ZWEI_REWARD_BAG = rewardBag("zwei_reward_bag", "Zwei奖励袋", "Zwei Reward Bag", RewardBagLoot.ZWEI_REWARD_BAG_LOOT);

    /** 原额外植物学「挑战券」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> CHALLENGE_TICKET = BotaniaReplica.EXTRABOTANY.item("challenge_ticket", "挑战券", "Challenge Ticket");

    /** 原额外植物学「精卫之羽」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> FEATHER_OF_JINGWEI = BotaniaReplica.EXTRABOTANY.item("feather_of_jingwei", "精卫之羽", "Feather of Jingwei");

    /** 原额外植物学「英雄勋章」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> HERO_MEDAL = BotaniaReplica.EXTRABOTANY.item("hero_medal", "英雄勋章", "Hero Medal");

    /** 原额外植物学「魔力读取器」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> MANA_READER = BotaniaReplica.EXTRABOTANY.item("mana_reader", "魔力读取器", "Mana Reader");

    /** 原额外植物学「自然蕴息宝珠」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> NATURE_ORB = BotaniaReplica.EXTRABOTANY.item("nature_orb", "自然蕴息宝珠", "Nature Orb");

    /** 原额外植物学「梦魇燃料」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> NIGHTMARE_FUEL = BotaniaReplica.EXTRABOTANY.item("nightmare_fuel", "梦魇燃料", "Nightmare Fuel");

    /** 原额外植物学「精神碎片」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> SPIRIT_FRAGMENT = BotaniaReplica.EXTRABOTANY.item("spirit_fragment", "精神碎片", "Spirit Fragment");

    /** 原额外植物学「精神燃料」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> SPIRIT_FUEL = BotaniaReplica.EXTRABOTANY.item("spirit_fuel", "精神燃料", "Spirit Fuel");

    /** 原额外植物学「混沌物质」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> THE_CHAOS = BotaniaReplica.EXTRABOTANY.item("the_chaos", "混沌物质", "The Chaos");

    /** 原额外植物学「终末物质」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> THE_END = BotaniaReplica.EXTRABOTANY.item("the_end", "终末物质", "The End");

    /** 原额外植物学「起源物质」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> THE_ORIGIN = BotaniaReplica.EXTRABOTANY.item("the_origin", "起源物质", "The Origin");

    /** 原额外植物学「宇宙之心」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> THE_UNIVERSE = BotaniaReplica.EXTRABOTANY.item("the_universe", "宇宙之心", "The Universe");

    /** 原额外植物学「步行手杖」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> WALKING_CANE = BotaniaReplica.EXTRABOTANY.item("walking_cane", "步行手杖", "Walking Cane");

    /** 原额外植物学「冰结傀儡」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> ZADKIEL = BotaniaReplica.EXTRABOTANY.item("zadkiel", "冰结傀儡", "Zadkiel");

    // ==================== 透镜 ====================

    /** 原额外植物学「魔力透镜：魔力」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> LENS_MANA = BotaniaReplica.EXTRABOTANY.item("lens_mana", "魔力透镜：魔力", "Mana Lens");

    /** 原额外植物学「魔力透镜：药水」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> LENS_POTION = BotaniaReplica.EXTRABOTANY.item("lens_potion", "魔力透镜：药水", "Potion Lens");

    /** 原额外植物学「魔力透镜：冲击」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> LENS_PUSH = BotaniaReplica.EXTRABOTANY.item("lens_push", "魔力透镜：冲击", "Push Lens");

    /** 原额外植物学「魔力透镜：冶炼」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> LENS_SMELT = BotaniaReplica.EXTRABOTANY.item("lens_smelt", "魔力透镜：冶炼", "Smelt Lens");

    /** 原额外植物学「魔力透镜：追踪」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> LENS_TRACE = BotaniaReplica.EXTRABOTANY.item("lens_trace", "魔力透镜：追踪", "Trace Lens");

    // ==================== 工具与法杖 ====================

    public static final ItemEntry<Item> MANASTEEL_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("manasteel_hammer", "魔力钢锤", "Manasteel Hammer", BotaniaTiers.MANASTEEL);

    public static final ItemEntry<Item> ELEMENTIUM_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("elementium_hammer", "源质钢锤", "Elementium Hammer", BotaniaTiers.ELEMENTIUM);

    public static final ItemEntry<Item> TERRASTEEL_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("terrasteel_hammer", "泰拉碾压者", "Terra Smasher", BotaniaTiers.TERRASTEEL);

    public static final ItemEntry<Item> GAIA_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("gaia_hammer", "盖亚魂钢锤", "Gaia Hammer", BotaniaTiers.GAIA);

    public static final ItemEntry<Item> PHOTONIUM_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("photonium_hammer", "光子锤", "Photonium Hammer", BotaniaTiers.PHOTONIUM);

    public static final ItemEntry<Item> SHADOWIUM_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("shadowium_hammer", "暗影锤", "Shadowium Hammer", BotaniaTiers.SHADOWIUM);

    public static final ItemEntry<Item> AERIALITE_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("aerialite_hammer", "天空锤", "Aerialite Hammer", BotaniaTiers.AERIALITE);

    public static final ItemEntry<Item> ORICHALCOS_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("orichalcos_hammer", "奥利哈钢锤", "Orichalcos Hammer", BotaniaTiers.ORICHALCOS);

    public static final ItemEntry<Item> RHEIN_HAMMER = BotaniaReplica.EXTRABOTANY.hammer("rhein_hammer", "莱茵锤", "Rhein Hammer", BotaniaTiers.RHEIN);

    // ==================== 武器 ====================

    public static final ItemEntry<Item> FAILNAUGHT = BotaniaReplica.EXTRABOTANY.bow("failnaught", "百中弓", "Failnaught");

    public static final ItemEntry<Item> EXCALIBUR = BotaniaReplica.EXTRABOTANY.sword("excalibur", "王者圣剑", "Excalibur", BotaniaTiers.RHEIN);

    // ==================== 盔甲 ====================

    public static final ItemEntry<Item> STARRY_IDOL_HEADGEAR = BotaniaReplica.EXTRABOTANY.armor("starry_idol_headgear", "星空歌姬头饰", "Starry Idol Headgear", BotaniaArmorMaterials.STARRY_IDOL, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> STARRY_IDOL_SUIT = BotaniaReplica.EXTRABOTANY.armor("starry_idol_suit", "星空歌姬服", "Starry Idol Suit", BotaniaArmorMaterials.STARRY_IDOL, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> STARRY_IDOL_SKIRT = BotaniaReplica.EXTRABOTANY.armor("starry_idol_skirt", "星空歌姬裙甲", "Starry Idol Skirt", BotaniaArmorMaterials.STARRY_IDOL, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> STARRY_IDOL_BOOTS = BotaniaReplica.EXTRABOTANY.armor("starry_idol_boots", "星空歌姬靴", "Starry Idol Boots", BotaniaArmorMaterials.STARRY_IDOL, ArmorItem.Type.BOOTS);

    public static final ItemEntry<Item> PLEIADES_COMBAT_MAID_HEADGEAR = BotaniaReplica.EXTRABOTANY.armor("pleiades_combat_maid_headgear", "昴宿星团战斗女仆头饰", "Pleiades Combat Maid Headgear", BotaniaArmorMaterials.PLEIADES, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> PLEIADES_COMBAT_MAID_SUIT = BotaniaReplica.EXTRABOTANY.armor("pleiades_combat_maid_suit", "昴宿星团战斗女仆服", "Pleiades Combat Maid Suit", BotaniaArmorMaterials.PLEIADES, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> PLEIADES_COMBAT_MAID_SKIRT = BotaniaReplica.EXTRABOTANY.armor("pleiades_combat_maid_skirt", "昴宿星团战斗女仆裙甲", "Pleiades Combat Maid Skirt", BotaniaArmorMaterials.PLEIADES, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> PLEIADES_COMBAT_MAID_BOOTS = BotaniaReplica.EXTRABOTANY.armor("pleiades_combat_maid_boots", "昴宿星团战斗女仆靴", "Pleiades Combat Maid Boots", BotaniaArmorMaterials.PLEIADES, ArmorItem.Type.BOOTS);

    public static final ItemEntry<Item> SANGUINE_PLEIADES_COMBAT_MAID_SUIT = BotaniaReplica.EXTRABOTANY.armor("sanguine_pleiades_combat_maid_suit", "血染昴宿星团战斗女仆服", "Sanguine Pleiades Combat Maid Suit", BotaniaArmorMaterials.PLEIADES, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> GOBLIN_SLAYER_HELMET = BotaniaReplica.EXTRABOTANY.armor("goblin_slayer_helmet", "哥布林猎人头盔", "Goblin Slayer Helmet", BotaniaArmorMaterials.GOBLIN_SLAYER, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> GOBLIN_SLAYER_CHESTPLATE = BotaniaReplica.EXTRABOTANY.armor("goblin_slayer_chestplate", "哥布林猎人胸甲", "Goblin Slayer Chestplate", BotaniaArmorMaterials.GOBLIN_SLAYER, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> GOBLIN_SLAYER_LEGGINGS = BotaniaReplica.EXTRABOTANY.armor("goblin_slayer_leggings", "哥布林猎人护腿", "Goblin Slayer Leggings", BotaniaArmorMaterials.GOBLIN_SLAYER, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> GOBLIN_SLAYER_BOOTS = BotaniaReplica.EXTRABOTANY.armor("goblin_slayer_boots", "哥布林猎人靴子", "Goblin Slayer Boots", BotaniaArmorMaterials.GOBLIN_SLAYER, ArmorItem.Type.BOOTS);

    public static final ItemEntry<Item> SHADOW_WARRIOR_HELMET = BotaniaReplica.EXTRABOTANY.armor("shadow_warrior_helmet", "暗影武士头盔", "Shadow Warrior Helmet", BotaniaArmorMaterials.SHADOW_WARRIOR, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> SHADOW_WARRIOR_CHESTPLATE = BotaniaReplica.EXTRABOTANY.armor("shadow_warrior_chestplate", "暗影武士胸甲", "Shadow Warrior Chestplate", BotaniaArmorMaterials.SHADOW_WARRIOR, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> SHADOW_WARRIOR_LEGGINGS = BotaniaReplica.EXTRABOTANY.armor("shadow_warrior_leggings", "暗影武士护腿", "Shadow Warrior Leggings", BotaniaArmorMaterials.SHADOW_WARRIOR, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> SHADOW_WARRIOR_BOOTS = BotaniaReplica.EXTRABOTANY.armor("shadow_warrior_boots", "暗影武士靴子", "Shadow Warrior Boots", BotaniaArmorMaterials.SHADOW_WARRIOR, ArmorItem.Type.BOOTS);

    // ==================== 饰品 ====================

    /** 原额外植物学「诅咒指环」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> CURSE_RING = BotaniaReplica.EXTRABOTANY.item("curse_ring", "诅咒指环", "Curse Ring");

    /** 原额外植物学「弥散指环」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> DISPERSIVE_RING = BotaniaReplica.EXTRABOTANY.item("dispersive_ring", "弥散指环", "Dispersive Ring");

    /** 原额外植物学「霜冻指环」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> FROST_RING = BotaniaReplica.EXTRABOTANY.item("frost_ring", "霜冻指环", "Frost Ring");

    /** 原额外植物学「成龙指环」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> JACKIE_CHAN_RING = BotaniaReplica.EXTRABOTANY.item("jackie_chan_ring", "成龙指环", "Jackie Chan Ring");

    /** 原额外植物学「大师魔力之戒」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> MANA_RING_MASTER = BotaniaReplica.EXTRABOTANY.item("mana_ring_master", "大师魔力之戒", "Master Band of Mana");

    /** 原额外植物学「跑酷指环」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> PARKOUR_RING = BotaniaReplica.EXTRABOTANY.item("parkour_ring", "跑酷指环", "Parkour Ring");

    /** 原额外植物学「白雏菊项链」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> PURE_DAISY_PENDANT = BotaniaReplica.EXTRABOTANY.item("pure_daisy_pendant", "白雏菊项链", "Pure Daisy Pendant");

    /** 原额外植物学「蛛行指环」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> SPIDER_RING = BotaniaReplica.EXTRABOTANY.item("spider_ring", "蛛行指环", "Spider Ring");

    // ==================== 酿造与食物 ====================

    /** 原额外植物学「无限%s酒 (%s)」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> INFINITE_WINE = BotaniaReplica.EXTRABOTANY.item("infinite_wine", "无限%s酒 (%s)", "Infinite %s Wine (%s)");

    /** 原额外植物学「秘制%s鸡尾酒 (%s)」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> MANA_COCKTAIL = BotaniaReplica.EXTRABOTANY.item("mana_cocktail", "秘制%s鸡尾酒 (%s)", "Secret %s Cocktail (%s)");

    /** 原额外植物学「香香鸡」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> FRIED_CHICKEN = BotaniaReplica.EXTRABOTANY.item("fried_chicken", "香香鸡", "Fried Chicken");

    /** 原额外植物学「镀金马铃薯」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> GILDED_POTATO = BotaniaReplica.EXTRABOTANY.item("gilded_potato", "镀金马铃薯", "Gilded Potato");

    /** 原额外植物学「镀金土豆泥」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> GILDED_POTATO_MASHED = BotaniaReplica.EXTRABOTANY.item("gilded_potato_mashed", "镀金土豆泥", "Gilded Mashed Potato");

    /** 原额外植物学「魔力玻璃瓶」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> MANA_GLASS_BOTTLE = BotaniaReplica.EXTRABOTANY.item("mana_glass_bottle", "魔力玻璃瓶", "Managlass Bottle");

    // ==================== 唱片与杂项 ====================

    /** 原额外植物学「破损的唱片」：原版为功能物品，此处仅注册基础物品。 */
    public static final ItemEntry<Item> MUSIC_DISC_GAIA3 = BotaniaReplica.EXTRABOTANY.item("music_disc_gaia3", "破损的唱片", "Scathed Music Disc");

    public static net.minecraft.world.item.Item getItem(String id) {
        return BotaniaReplica.EXTRABOTANY.getItem(id);
    }
}
