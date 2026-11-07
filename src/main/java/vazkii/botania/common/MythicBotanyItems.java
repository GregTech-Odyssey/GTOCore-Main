package vazkii.botania.common;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import com.gto.registrate.util.entry.ItemEntry;

import vazkii.botania.common.item.BotaniaArmorMaterials;
import vazkii.botania.common.item.BotaniaTiers;
import vazkii.botania.common.item.FullGjallarHornItem;
import vazkii.botania.common.item.KvasirMeadItem;

/**
 * 神话植物学物品复刻。按内容类型分组。
 */
public final class MythicBotanyItems {

    private MythicBotanyItems() {}

    public static void init() {}

    // ==================== 材料与其它物品 ====================

    public static final ItemEntry<Item> ALFSTEEL_AXE = BotaniaReplica.MYTHICBOTANY.axe("alfsteel_axe", "精灵断裂者", "Alf Truncator", BotaniaTiers.ALFSTEEL);

    public static final ItemEntry<Item> MB_ALFHEIM_RUNE = BotaniaReplica.MYTHICBOTANY.item("alfheim_rune", "精灵符文", "Rune of Alfheim");

    public static final ItemEntry<Item> MB_ASGARD_RUNE = BotaniaReplica.MYTHICBOTANY.item("asgard_rune", "阿萨神族符文", "Rune of Asgard");

    public static final ItemEntry<Item> MB_HELHEIM_RUNE = BotaniaReplica.MYTHICBOTANY.item("helheim_rune", "亡灵符文", "Rune of Helheim");

    public static final ItemEntry<Item> MB_JOETUNHEIM_RUNE = BotaniaReplica.MYTHICBOTANY.item("joetunheim_rune", "巨人符文", "Rune of Jötunheim");

    public static final ItemEntry<Item> MB_MIDGARD_RUNE = BotaniaReplica.MYTHICBOTANY.item("midgard_rune", "人类符文", "Rune of Midgard");

    public static final ItemEntry<Item> MB_MUSPELHEIM_RUNE = BotaniaReplica.MYTHICBOTANY.item("muspelheim_rune", "烈焰符文", "Rune of Muspelheim");

    public static final ItemEntry<Item> MB_NIDAVELLIR_RUNE = BotaniaReplica.MYTHICBOTANY.item("nidavellir_rune", "矮人符文", "Rune of Nidavellir");

    public static final ItemEntry<Item> MB_NIFLHEIM_RUNE = BotaniaReplica.MYTHICBOTANY.item("niflheim_rune", "冰雪符文", "Rune of Niflheim");

    public static final ItemEntry<Item> MB_VANAHEIM_RUNE = BotaniaReplica.MYTHICBOTANY.item("vanaheim_rune", "华纳神族符文", "Rune of Vanaheim");

    public static final ItemEntry<Item> MB_ALFSTEEL_INGOT = BotaniaReplica.MYTHICBOTANY.item("alfsteel_ingot", "精灵钢", "Alfsteel Ingot");

    public static final ItemEntry<Item> MB_ALFSTEEL_NUGGET = BotaniaReplica.MYTHICBOTANY.item("alfsteel_nugget", "精灵钢粒", "Alfsteel Nugget");

    public static final ItemEntry<Item> MB_RAW_ELEMENTIUM = BotaniaReplica.MYTHICBOTANY.item("raw_elementium", "粗精灵钢", "Raw Elementium");

    public static final ItemEntry<Item> MB_ALF_PIXIE_SPAWN_EGG = BotaniaReplica.MYTHICBOTANY.item("alf_pixie_spawn_egg", "精灵皮克希刷怪蛋", "Alfheim Pixie Spawn Egg");

    public static final ItemEntry<Item> MB_FADED_NETHER_STAR = BotaniaReplica.MYTHICBOTANY.item("faded_nether_star", "凋零的下界之星", "Faded Nether Star");

    public static final ItemEntry<Item> MB_FIMBULTYR_TABLET = BotaniaReplica.MYTHICBOTANY.item("fimbultyr_tablet", "菲姆布尔的符文牌", "Rune Tablet of Fimbultyr");

    public static final ItemEntry<Item> MB_GJALLAR_HORN_EMPTY = BotaniaReplica.MYTHICBOTANY.item("gjallar_horn_empty", "海姆达尔号角", "Gjallarhorn");

    public static final ItemEntry<Item> MB_GJALLAR_HORN_FULL = BotaniaReplica.MYTHICBOTANY.item("gjallar_horn_full", "装有智慧之泉水的海姆达尔号角", "Water from the Well of Mimir", FullGjallarHornItem::new);

    // ==================== 工具与法杖 ====================

    public static final ItemEntry<Item> ALFSTEEL_PICK = BotaniaReplica.MYTHICBOTANY.pickaxe("alfsteel_pick", "精灵粉碎者", "Alf Shatterer", BotaniaTiers.ALFSTEEL);

    // ==================== 武器 ====================

    public static final ItemEntry<Item> ALFSTEEL_SWORD = BotaniaReplica.MYTHICBOTANY.sword("alfsteel_sword", "精灵之刃", "Alf Blade", BotaniaTiers.ALFSTEEL);

    // ==================== 盔甲 ====================

    public static final ItemEntry<Item> ALFSTEEL_HELMET = BotaniaReplica.MYTHICBOTANY.armor("alfsteel_helmet", "精灵盔", "Alfsteel Helmet", BotaniaArmorMaterials.ALFSTEEL, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> ALFSTEEL_CHESTPLATE = BotaniaReplica.MYTHICBOTANY.armor("alfsteel_chestplate", "精灵战甲", "Alfsteel Chestplate", BotaniaArmorMaterials.ALFSTEEL, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> ALFSTEEL_LEGGINGS = BotaniaReplica.MYTHICBOTANY.armor("alfsteel_leggings", "精灵护腿", "Alfsteel Leggings", BotaniaArmorMaterials.ALFSTEEL, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> ALFSTEEL_BOOTS = BotaniaReplica.MYTHICBOTANY.armor("alfsteel_boots", "精灵靴", "Alfsteel Boots", BotaniaArmorMaterials.ALFSTEEL, ArmorItem.Type.BOOTS);

    // ==================== 饰品 ====================

    public static final ItemEntry<Item> MB_ANDWARI_RING = BotaniaReplica.MYTHICBOTANY.item("andwari_ring", "安德瓦利之戒", "Ring of Andwari");

    public static final ItemEntry<Item> MB_AURA_RING_GREATEST = BotaniaReplica.MYTHICBOTANY.item("aura_ring_greatest", "精灵光环戒", "Greatest Band of Aura");

    public static final ItemEntry<Item> MB_CURSED_ANDWARI_RING = BotaniaReplica.MYTHICBOTANY.item("cursed_andwari_ring", "被诅咒的安德瓦利之戒", "Cursed Ring of Andwari");

    public static final ItemEntry<Item> MB_FIRE_RING = BotaniaReplica.MYTHICBOTANY.item("fire_ring", "烈焰之戒", "Ring of Muspelheim");

    public static final ItemEntry<Item> MB_ICE_RING = BotaniaReplica.MYTHICBOTANY.item("ice_ring", "冰雪之戒", "Ring of Niflheim");

    public static final ItemEntry<Item> MB_MANA_RING_GREATEST = BotaniaReplica.MYTHICBOTANY.item("mana_ring_greatest", "精灵魔力戒", "Greatest Band of Mana");

    // ==================== 酿造与食物 ====================

    public static final ItemEntry<Item> MB_KVASIR_BLOOD = BotaniaReplica.MYTHICBOTANY.item("kvasir_blood", "克瓦希尔之血", "Blood of Kvasir");

    public static final ItemEntry<Item> MB_DREAM_CHERRY = BotaniaReplica.MYTHICBOTANY.item("dream_cherry", "梦之樱桃", "Dreamcherry");

    public static final ItemEntry<Item> MB_KVASIR_MEAD = BotaniaReplica.MYTHICBOTANY.item("kvasir_mead", "诗之蜜酒", "Mead of Kvasir", KvasirMeadItem::new);

    // ==================== 唱片与杂项 ====================

    public static final ItemEntry<Item> MB_ALFSTEEL_TEMPLATE = BotaniaReplica.MYTHICBOTANY.item("alfsteel_template", "精灵钢升级模板", "Alfsteel Upgrade Smithing Template");

    public static net.minecraft.world.item.Item getItem(String id) {
        return BotaniaReplica.MYTHICBOTANY.getItem(id);
    }
}
