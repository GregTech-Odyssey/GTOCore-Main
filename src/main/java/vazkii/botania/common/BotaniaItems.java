package vazkii.botania.common;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import com.gto.registrate.util.entry.ItemEntry;

import vazkii.botania.common.item.BotaniaArmorMaterials;
import vazkii.botania.common.item.BotaniaTiers;
import vazkii.botania.common.item.LexicaBotaniaItem;

/**
 * 植物魔法物品复刻。按内容类型分组。
 */
public final class BotaniaItems {

    private BotaniaItems() {}

    public static void init() {}

    // ==================== 材料与其它物品 ====================

    public static final ItemEntry<Item> RED_STRING = BotaniaReplica.BOTANIA.item("red_string", "红线", "Red String");

    public static final ItemEntry<Item> MANASTEEL_AXE = BotaniaReplica.BOTANIA.axe("manasteel_axe", "魔钢斧", "Manasteel Axe", BotaniaTiers.MANASTEEL);

    public static final ItemEntry<Item> ELEMENTIUM_AXE = BotaniaReplica.BOTANIA.axe("elementium_axe", "源质钢斧", "Elementium Axe", BotaniaTiers.ELEMENTIUM);

    public static final ItemEntry<Item> TERRA_AXE = BotaniaReplica.BOTANIA.axe("terra_axe", "泰拉断裂者", "Terra Truncator", BotaniaTiers.TERRASTEEL);

    public static final ItemEntry<Item> CRAFTING_HALO = BotaniaReplica.BOTANIA.item("crafting_halo", "合成环", "Assembly Halo");

    public static final ItemEntry<Item> AUTO_CRAFTING_HALO = BotaniaReplica.BOTANIA.item("auto_crafting_halo", "工厂环", "Manufactory Halo");

    public static final ItemEntry<Item> CORPOREA_SPARK = BotaniaReplica.BOTANIA.item("corporea_spark", "多媒体火花", "Corporea Spark");

    public static final ItemEntry<Item> CORPOREA_SPARK_MASTER = BotaniaReplica.BOTANIA.item("corporea_spark_master", "主媒体火花", "Master Corporea Spark");

    public static final ItemEntry<Item> CORPOREA_SPARK_CREATIVE = BotaniaReplica.BOTANIA.item("corporea_spark_creative", "创造多媒体火花", "Creative Corporea Spark");

    public static final ItemEntry<Item> KEEP_IVY = BotaniaReplica.BOTANIA.item("keep_ivy", "坚毅之藤", "Resolute Ivy");

    public static final ItemEntry<Item> MANA_CELL_HOUSING = BotaniaReplica.BOTANIA.item("mana_cell_housing", "ME魔力元件外壳", "ME Mana Cell Housing");

    public static final ItemEntry<Item> MANA_STORAGE_CELL_1K = BotaniaReplica.BOTANIA.item("mana_storage_cell_1k", "1k ME魔力存储元件", "1k ME Mana Storage Cell");

    public static final ItemEntry<Item> MANA_STORAGE_CELL_4K = BotaniaReplica.BOTANIA.item("mana_storage_cell_4k", "4k ME魔力存储元件", "4k ME Mana Storage Cell");

    public static final ItemEntry<Item> MANA_STORAGE_CELL_16K = BotaniaReplica.BOTANIA.item("mana_storage_cell_16k", "16k ME魔力存储元件", "16k ME Mana Storage Cell");

    public static final ItemEntry<Item> MANA_STORAGE_CELL_64K = BotaniaReplica.BOTANIA.item("mana_storage_cell_64k", "64k ME魔力存储元件", "64k ME Mana Storage Cell");

    public static final ItemEntry<Item> MANA_STORAGE_CELL_256K = BotaniaReplica.BOTANIA.item("mana_storage_cell_256k", "256k ME魔力存储元件", "256k ME Mana Storage Cell");

    public static final ItemEntry<Item> CREATIVE_MANA_CELL = BotaniaReplica.BOTANIA.item("creative_mana_cell", "创造型ME魔力存储元件", "Creative ME Mana Cell");

    public static final ItemEntry<Item> MANA_P2P_TUNNEL = BotaniaReplica.BOTANIA.item("mana_p2p_tunnel", "魔力P2P通道", "Mana P2P Tunnel");

    public static final ItemEntry<Item> FERTILIZER = BotaniaReplica.BOTANIA.item("fertilizer", "花肥", "Floral Fertilizer", vazkii.botania.common.item.FloralFertilizerItem::new);

    public static final ItemEntry<Item> DRAGONSTONE = BotaniaReplica.BOTANIA.item("dragonstone", "龙石", "Dragonstone");

    public static final ItemEntry<Item> DREAMWOOD_TWIG = BotaniaReplica.BOTANIA.item("dreamwood_twig", "梦之木棍", "Dreamwood Twig");

    public static final ItemEntry<Item> ELEMENTIUM_INGOT = BotaniaReplica.BOTANIA.item("elementium_ingot", "源质钢锭", "Elementium Ingot");

    public static final ItemEntry<Item> ELEMENTIUM_NUGGET = BotaniaReplica.BOTANIA.item("elementium_nugget", "源质钢粒", "Elementium Nugget");

    public static final ItemEntry<Item> GAIA_INGOT = BotaniaReplica.BOTANIA.item("gaia_ingot", "盖亚魂锭", "Gaia Spirit Ingot");

    public static final ItemEntry<Item> LIFE_ESSENCE = BotaniaReplica.BOTANIA.item("life_essence", "盖亚之魂", "Gaia Spirit");

    public static final ItemEntry<Item> LIVING_ROOT = BotaniaReplica.BOTANIA.item("living_root", "活根", "Living Root");

    public static final ItemEntry<Item> LIVINGWOOD_TWIG = BotaniaReplica.BOTANIA.item("livingwood_twig", "活木枝", "Livingwood Twig");

    public static final ItemEntry<Item> MANA_DIAMOND = BotaniaReplica.BOTANIA.item("mana_diamond", "魔力钻石", "Mana Diamond");

    public static final ItemEntry<Item> MANA_PEARL = BotaniaReplica.BOTANIA.item("mana_pearl", "魔力珍珠", "Mana Pearl");

    public static final ItemEntry<Item> MANA_POWDER = BotaniaReplica.BOTANIA.item("mana_powder", "魔力尘", "Mana Powder");

    public static final ItemEntry<Item> MANASTEEL_INGOT = BotaniaReplica.BOTANIA.item("manasteel_ingot", "魔力钢锭", "Manasteel Ingot");

    public static final ItemEntry<Item> MANASTEEL_NUGGET = BotaniaReplica.BOTANIA.item("manasteel_nugget", "魔钢粒", "Manasteel Nugget");

    public static final ItemEntry<Item> MANAWEAVE_CLOTH = BotaniaReplica.BOTANIA.item("manaweave_cloth", "魔源布匹", "Manaweave Cloth");

    public static final ItemEntry<Item> OVERGROWTH_SEED = BotaniaReplica.BOTANIA.item("overgrowth_seed", "增生之种", "Overgrowth Seed");

    public static final ItemEntry<Item> PEBBLE = BotaniaReplica.BOTANIA.item("pebble", "卵石", "Pebble");

    public static final ItemEntry<Item> PIXIE_DUST = BotaniaReplica.BOTANIA.item("pixie_dust", "精灵尘", "Pixie Dust");

    public static final ItemEntry<Item> REDSTONE_ROOT = BotaniaReplica.BOTANIA.item("redstone_root", "红石根", "Redstone Root");

    public static final ItemEntry<Item> TERRASTEEL_INGOT = BotaniaReplica.BOTANIA.item("terrasteel_ingot", "泰拉钢锭", "Terrasteel Ingot");

    public static final ItemEntry<Item> TERRASTEEL_NUGGET = BotaniaReplica.BOTANIA.item("terrasteel_nugget", "泰拉钢粒", "Terrasteel Nugget");

    public static final ItemEntry<Item> WORLD_SEED = BotaniaReplica.BOTANIA.item("world_seed", "世界之种", "World Seed");

    public static final ItemEntry<Item> BAUBLE_BOX = BotaniaReplica.BOTANIA.item("bauble_box", "饰品盒子", "Trinket Case");

    public static final ItemEntry<Item> ITEMFINDER = BotaniaReplica.BOTANIA.item("itemfinder", "寻物者", "The Spectator");

    public static final ItemEntry<Item> MANA_STRING = BotaniaReplica.BOTANIA.item("mana_string", "蕴魔丝线", "Mana Infused String");

    public static final ItemEntry<Item> THIRD_EYE = BotaniaReplica.BOTANIA.item("third_eye", "第三只眼", "Third Eye");

    public static final ItemEntry<Item> TINY_PLANET = BotaniaReplica.BOTANIA.item("tiny_planet", "行星项链", "Tiny Planet");

    public static final ItemEntry<Item> INCENSE_STICK = BotaniaReplica.BOTANIA.item("incense_stick", "熏香", "Incense Stick");

    public static final ItemEntry<Item> ANCIENT_WILL_AHRIM = BotaniaReplica.BOTANIA.item("ancient_will_ahrim", "艾琳的意志", "Will of Ahrim");

    public static final ItemEntry<Item> ANCIENT_WILL_DHAROK = BotaniaReplica.BOTANIA.item("ancient_will_dharok", "达洛克的意志", "Will of Dharok");

    public static final ItemEntry<Item> ANCIENT_WILL_GUTHAN = BotaniaReplica.BOTANIA.item("ancient_will_guthan", "古赞的意志", "Will of Guthan");

    public static final ItemEntry<Item> ANCIENT_WILL_KARIL = BotaniaReplica.BOTANIA.item("ancient_will_karil", "卡瑞的意志", "Will of Karil");

    public static final ItemEntry<Item> ANCIENT_WILL_TORAG = BotaniaReplica.BOTANIA.item("ancient_will_torag", "托拉格的意志", "Will of Torag");

    public static final ItemEntry<Item> ANCIENT_WILL_VERAC = BotaniaReplica.BOTANIA.item("ancient_will_verac", "威拉克的意志", "Will of Verac");

    public static final ItemEntry<Item> CACOPHONIUM = BotaniaReplica.BOTANIA.item("cacophonium", "不和谐之音", "Cacophonium");

    public static final ItemEntry<Item> ENDER_HAND = BotaniaReplica.BOTANIA.item("ender_hand", "末影之手", "Hand of Ender");

    public static final ItemEntry<Item> FLOWER_BAG = BotaniaReplica.BOTANIA.unstackableItem("flower_bag", "采花袋", "Flower Pouch",
            vazkii.botania.common.item.FlowerPouchItem::new);

    public static final ItemEntry<Item> HORN_GRASS = BotaniaReplica.BOTANIA.item("horn_grass", "狂野号角", "Horn of the Wild");

    public static final ItemEntry<Item> HORN_LEAVES = BotaniaReplica.BOTANIA.item("horn_leaves", "林冠号角", "Horn of the Canopy");

    public static final ItemEntry<Item> HORN_SNOW = BotaniaReplica.BOTANIA.item("horn_snow", "雪原号角", "Horn of the Covering");

    public static final ItemEntry<Item> MANA_GUN = BotaniaReplica.BOTANIA.item("mana_gun", "魔力脉冲枪", "Mana Blaster");

    public static final ItemEntry<Item> MANA_MIRROR = BotaniaReplica.BOTANIA.item("mana_mirror", "魔力手镜", "Mana Mirror");

    public static final ItemEntry<Item> MANA_TABLET = BotaniaReplica.BOTANIA.item("mana_tablet", "魔力石板", "Mana Tablet");

    public static final ItemEntry<Item> OBEDIENCE_STICK = BotaniaReplica.BOTANIA.item("obedience_stick", "植物遵从法杖", "Floral Obedience Stick");

    public static final ItemEntry<Item> OPEN_BUCKET = BotaniaReplica.BOTANIA.item("open_bucket", "无底桶", "Extrapolated Bucket");

    public static final ItemEntry<Item> PHANTOM_INK = BotaniaReplica.BOTANIA.item("phantom_ink", "幻影墨水", "Phantom Ink");

    public static final ItemEntry<Item> POOL_MINECART = BotaniaReplica.BOTANIA.item("pool_minecart", "魔力池矿车", "Minecart with Mana Pool");

    public static final ItemEntry<Item> QUARTZ_BLAZE = BotaniaReplica.BOTANIA.item("quartz_blaze", "烈焰石英", "Blaze Quartz");

    public static final ItemEntry<Item> QUARTZ_DARK = BotaniaReplica.BOTANIA.item("quartz_dark", "烟熏石英", "Smokey Quartz");

    public static final ItemEntry<Item> QUARTZ_ELVEN = BotaniaReplica.BOTANIA.item("quartz_elven", "精灵石英", "Elven Quartz");

    public static final ItemEntry<Item> QUARTZ_LAVENDER = BotaniaReplica.BOTANIA.item("quartz_lavender", "熏香石英", "Lavender Quartz");

    public static final ItemEntry<Item> QUARTZ_MANA = BotaniaReplica.BOTANIA.item("quartz_mana", "魔力石英", "Mana Quartz");

    public static final ItemEntry<Item> QUARTZ_RED = BotaniaReplica.BOTANIA.item("quartz_red", "红色石英", "Redquartz");

    public static final ItemEntry<Item> QUARTZ_SUNNY = BotaniaReplica.BOTANIA.item("quartz_sunny", "金黄石英", "Sunny Quartz");

    public static final ItemEntry<Item> SPAWNER_MOVER = BotaniaReplica.BOTANIA.item("spawner_mover", "生命聚合器", "Life Aggregator");

    public static final ItemEntry<Item> SPELL_CLOTH = BotaniaReplica.BOTANIA.item("spell_cloth", "封印之布", "Spellbinding Cloth");

    public static final ItemEntry<Item> TEMPERANCE_STONE = BotaniaReplica.BOTANIA.item("temperance_stone", "节制之石", "Stone of Temperance");

    public static final ItemEntry<Item> VINE_BALL = BotaniaReplica.BOTANIA.item("vine_ball", "藤蔓球", "Vine Ball",
            vazkii.botania.common.item.VineBallItem::new);

    public static final ItemEntry<Item> WATER_BOWL = BotaniaReplica.BOTANIA.item("water_bowl", "一碗水", "Bowl of Water");

    // ==================== 花瓣与种子 ====================

    public static final ItemEntry<Item> BLACK_PETAL = BotaniaReplica.BOTANIA.petal("black_petal", "黑色神秘花瓣", "Mystical Black Petal");

    public static final ItemEntry<Item> BLUE_PETAL = BotaniaReplica.BOTANIA.petal("blue_petal", "蓝色神秘花瓣", "Mystical Blue Petal");

    public static final ItemEntry<Item> BROWN_PETAL = BotaniaReplica.BOTANIA.petal("brown_petal", "棕色神秘花瓣", "Mystical Brown Petal");

    public static final ItemEntry<Item> CYAN_PETAL = BotaniaReplica.BOTANIA.petal("cyan_petal", "青色神秘花瓣", "Mystical Cyan Petal");

    public static final ItemEntry<Item> DRY_SEEDS = BotaniaReplica.BOTANIA.item("dry_seeds", "干草之种", "Dry Pasture Seeds");

    public static final ItemEntry<Item> GOLDEN_SEEDS = BotaniaReplica.BOTANIA.item("golden_seeds", "黄金草种", "Golden Pasture Seeds");

    public static final ItemEntry<Item> GRASS_SEEDS = BotaniaReplica.BOTANIA.item("grass_seeds", "草地之种", "Pasture Seeds");

    public static final ItemEntry<Item> GRAY_PETAL = BotaniaReplica.BOTANIA.petal("gray_petal", "灰色神秘花瓣", "Mystical Gray Petal");

    public static final ItemEntry<Item> GREEN_PETAL = BotaniaReplica.BOTANIA.petal("green_petal", "绿色神秘花瓣", "Mystical Green Petal");

    public static final ItemEntry<Item> INFUSED_SEEDS = BotaniaReplica.BOTANIA.item("infused_seeds", "注魔草种", "Infused Pasture Seeds");

    public static final ItemEntry<Item> LIGHT_BLUE_PETAL = BotaniaReplica.BOTANIA.petal("light_blue_petal", "淡蓝色神秘花瓣", "Mystical Light Blue Petal");

    public static final ItemEntry<Item> LIGHT_GRAY_PETAL = BotaniaReplica.BOTANIA.petal("light_gray_petal", "淡灰色神秘花瓣", "Mystical Light Gray Petal");

    public static final ItemEntry<Item> LIME_PETAL = BotaniaReplica.BOTANIA.petal("lime_petal", "黄绿色神秘花瓣", "Mystical Lime Petal");

    public static final ItemEntry<Item> MAGENTA_PETAL = BotaniaReplica.BOTANIA.petal("magenta_petal", "品红色神秘花瓣", "Mystical Magenta Petal");

    public static final ItemEntry<Item> MUTATED_SEEDS = BotaniaReplica.BOTANIA.item("mutated_seeds", "异变草种", "Mutated Pasture Seeds");

    public static final ItemEntry<Item> MYCELIUM_SEEDS = BotaniaReplica.BOTANIA.item("mycelium_seeds", "菌丝孢子", "Infestation Spores");

    public static final ItemEntry<Item> ORANGE_PETAL = BotaniaReplica.BOTANIA.petal("orange_petal", "橙色神秘花瓣", "Mystical Orange Petal");

    public static final ItemEntry<Item> PINK_PETAL = BotaniaReplica.BOTANIA.petal("pink_petal", "粉色神秘花瓣", "Mystical Pink Petal");

    public static final ItemEntry<Item> PODZOL_SEEDS = BotaniaReplica.BOTANIA.item("podzol_seeds", "寒地之种", "Boreal Seeds");

    public static final ItemEntry<Item> PURPLE_PETAL = BotaniaReplica.BOTANIA.petal("purple_petal", "紫色神秘花瓣", "Mystical Purple Petal");

    public static final ItemEntry<Item> RED_PETAL = BotaniaReplica.BOTANIA.petal("red_petal", "红色神秘花瓣", "Mystical Red Petal");

    public static final ItemEntry<Item> SCORCHED_SEEDS = BotaniaReplica.BOTANIA.item("scorched_seeds", "焦土草种", "Scorched Pasture Seeds");

    public static final ItemEntry<Item> VIVID_SEEDS = BotaniaReplica.BOTANIA.item("vivid_seeds", "鲜草之种", "Vivid Pasture Seeds");

    public static final ItemEntry<Item> WHITE_PETAL = BotaniaReplica.BOTANIA.petal("white_petal", "白色神秘花瓣", "Mystical White Petal");

    public static final ItemEntry<Item> YELLOW_PETAL = BotaniaReplica.BOTANIA.petal("yellow_petal", "黄色神秘花瓣", "Mystical Yellow Petal");

    // ==================== 符文 ====================

    public static final ItemEntry<Item> RUNE_AIR = BotaniaReplica.BOTANIA.item("rune_air", "风之符文", "Rune of Air");

    public static final ItemEntry<Item> RUNE_AUTUMN = BotaniaReplica.BOTANIA.item("rune_autumn", "秋之符文", "Rune of Autumn");

    public static final ItemEntry<Item> RUNE_EARTH = BotaniaReplica.BOTANIA.item("rune_earth", "地之符文", "Rune of Earth");

    public static final ItemEntry<Item> RUNE_ENVY = BotaniaReplica.BOTANIA.item("rune_envy", "嫉妒符文", "Rune of Envy");

    public static final ItemEntry<Item> RUNE_FIRE = BotaniaReplica.BOTANIA.item("rune_fire", "火之符文", "Rune of Fire");

    public static final ItemEntry<Item> RUNE_GLUTTONY = BotaniaReplica.BOTANIA.item("rune_gluttony", "暴食符文", "Rune of Gluttony");

    public static final ItemEntry<Item> RUNE_GREED = BotaniaReplica.BOTANIA.item("rune_greed", "贪婪符文", "Rune of Greed");

    public static final ItemEntry<Item> RUNE_LUST = BotaniaReplica.BOTANIA.item("rune_lust", "欲望符文", "Rune of Lust");

    public static final ItemEntry<Item> RUNE_MANA = BotaniaReplica.BOTANIA.item("rune_mana", "魔力符文", "Rune of Mana");

    public static final ItemEntry<Item> RUNE_PRIDE = BotaniaReplica.BOTANIA.item("rune_pride", "傲慢符文", "Rune of Pride");

    public static final ItemEntry<Item> RUNE_SLOTH = BotaniaReplica.BOTANIA.item("rune_sloth", "懒惰符文", "Rune of Sloth");

    public static final ItemEntry<Item> RUNE_SPRING = BotaniaReplica.BOTANIA.item("rune_spring", "春之符文", "Rune of Spring");

    public static final ItemEntry<Item> RUNE_SUMMER = BotaniaReplica.BOTANIA.item("rune_summer", "夏之符文", "Rune of Summer");

    public static final ItemEntry<Item> RUNE_WATER = BotaniaReplica.BOTANIA.item("rune_water", "水之符文", "Rune of Water");

    public static final ItemEntry<Item> RUNE_WINTER = BotaniaReplica.BOTANIA.item("rune_winter", "冬之符文", "Rune of Winter");

    public static final ItemEntry<Item> RUNE_WRATH = BotaniaReplica.BOTANIA.item("rune_wrath", "暴怒符文", "Rune of Wrath");

    // ==================== 透镜 ====================

    public static final ItemEntry<Item> LENS_BOUNCE = BotaniaReplica.BOTANIA.item("lens_bounce", "魔力透镜：反射", "Bounce Lens");

    public static final ItemEntry<Item> LENS_DAMAGE = BotaniaReplica.BOTANIA.item("lens_damage", "魔力透镜：伤害", "Damaging Lens");

    public static final ItemEntry<Item> LENS_EFFICIENCY = BotaniaReplica.BOTANIA.item("lens_efficiency", "魔力透镜：效率", "Efficiency Lens");

    public static final ItemEntry<Item> LENS_EXPLOSIVE = BotaniaReplica.BOTANIA.item("lens_explosive", "魔力透镜：爆炸", "Entropic Lens");

    public static final ItemEntry<Item> LENS_FIRE = BotaniaReplica.BOTANIA.item("lens_fire", "魔力透镜：引燃", "Kindle Lens");

    public static final ItemEntry<Item> LENS_FIREWORK = BotaniaReplica.BOTANIA.item("lens_firework", "魔力透镜：喜庆", "Celebratory Lens");

    public static final ItemEntry<Item> LENS_FLARE = BotaniaReplica.BOTANIA.item("lens_flare", "魔力透镜：闪耀", "Flare Lens");

    public static final ItemEntry<Item> LENS_GRAVITY = BotaniaReplica.BOTANIA.item("lens_gravity", "魔力透镜：引力", "Gravity Lens");

    public static final ItemEntry<Item> LENS_INFLUENCE = BotaniaReplica.BOTANIA.item("lens_influence", "魔力透镜：推力", "Influence Lens");

    public static final ItemEntry<Item> LENS_LIGHT = BotaniaReplica.BOTANIA.item("lens_light", "魔力透镜：闪光", "Flash Lens");

    public static final ItemEntry<Item> LENS_MAGNET = BotaniaReplica.BOTANIA.item("lens_magnet", "魔力透镜：磁化", "Magnetizing Lens");

    public static final ItemEntry<Item> LENS_MESSENGER = BotaniaReplica.BOTANIA.item("lens_messenger", "魔力透镜：信使", "Messenger Lens");

    public static final ItemEntry<Item> LENS_MINE = BotaniaReplica.BOTANIA.item("lens_mine", "魔力透镜：挖掘", "Bore Lens");

    public static final ItemEntry<Item> LENS_NORMAL = BotaniaReplica.BOTANIA.item("lens_normal", "魔力透镜", "Mana Lens");

    public static final ItemEntry<Item> LENS_PAINT = BotaniaReplica.BOTANIA.item("lens_paint", "魔力透镜：涂色", "Paintslinger Lens");

    public static final ItemEntry<Item> LENS_PHANTOM = BotaniaReplica.BOTANIA.item("lens_phantom", "魔力透镜：虚化", "Phantom Lens");

    public static final ItemEntry<Item> LENS_PISTON = BotaniaReplica.BOTANIA.item("lens_piston", "魔力透镜：力量", "Force Lens");

    public static final ItemEntry<Item> LENS_POWER = BotaniaReplica.BOTANIA.item("lens_power", "魔力透镜：强度", "Potency Lens");

    public static final ItemEntry<Item> LENS_REDIRECT = BotaniaReplica.BOTANIA.item("lens_redirect", "魔力透镜：变向", "Redirective Lens");

    public static final ItemEntry<Item> LENS_SPEED = BotaniaReplica.BOTANIA.item("lens_speed", "魔力透镜：速度", "Velocity Lens");

    public static final ItemEntry<Item> LENS_STORM = BotaniaReplica.BOTANIA.item("lens_storm", "魔力透镜：风暴", "Storm Lens");

    public static final ItemEntry<Item> LENS_TIME = BotaniaReplica.BOTANIA.item("lens_time", "魔力透镜：阻力", "Resistance Lens");

    public static final ItemEntry<Item> LENS_TRIPWIRE = BotaniaReplica.BOTANIA.item("lens_tripwire", "魔力透镜：绊线", "Tripwire Lens");

    public static final ItemEntry<Item> LENS_WARP = BotaniaReplica.BOTANIA.item("lens_warp", "魔力透镜：传送", "Warp Lens");

    public static final ItemEntry<Item> LENS_WEIGHT = BotaniaReplica.BOTANIA.item("lens_weight", "魔力透镜：重力", "Weight Lens");

    // ==================== 工具与法杖 ====================

    public static final ItemEntry<Item> MANASTEEL_PICK = BotaniaReplica.BOTANIA.pickaxe("manasteel_pick", "魔钢镐", "Manasteel Pickaxe", BotaniaTiers.MANASTEEL);

    public static final ItemEntry<Item> MANASTEEL_SHOVEL = BotaniaReplica.BOTANIA.shovel("manasteel_shovel", "魔钢铲", "Manasteel Shovel", BotaniaTiers.MANASTEEL);

    public static final ItemEntry<Item> MANASTEEL_HOE = BotaniaReplica.BOTANIA.hoe("manasteel_hoe", "魔钢锄", "Manasteel Hoe", BotaniaTiers.MANASTEEL);

    public static final ItemEntry<Item> MANASTEEL_SHEARS = BotaniaReplica.BOTANIA.shears("manasteel_shears", "魔钢剪刀", "Manasteel Shears");

    public static final ItemEntry<Item> ELEMENTIUM_PICKAXE = BotaniaReplica.BOTANIA.pickaxe("elementium_pickaxe", "源质钢镐", "Elementium Pickaxe", BotaniaTiers.ELEMENTIUM);

    public static final ItemEntry<Item> ELEMENTIUM_SHOVEL = BotaniaReplica.BOTANIA.shovel("elementium_shovel", "源质钢铲", "Elementium Shovel", BotaniaTiers.ELEMENTIUM);

    public static final ItemEntry<Item> ELEMENTIUM_HOE = BotaniaReplica.BOTANIA.hoe("elementium_hoe", "源质钢锄", "Elementium Hoe", BotaniaTiers.ELEMENTIUM);

    public static final ItemEntry<Item> ELEMENTIUM_SHEARS = BotaniaReplica.BOTANIA.shears("elementium_shears", "源质钢剪", "Elementium Shears");

    public static final ItemEntry<Item> TERRA_PICK = BotaniaReplica.BOTANIA.pickaxe("terra_pick", "泰拉粉碎者", "Terra Shatterer", BotaniaTiers.TERRASTEEL);

    public static final ItemEntry<Item> GLASS_PICKAXE = BotaniaReplica.BOTANIA.pickaxe("glass_pickaxe", "玻璃镐", "Vitreous Pickaxe", BotaniaTiers.TERRASTEEL);

    public static final ItemEntry<Item> COBBLE_ROD = BotaniaReplica.BOTANIA.item("cobble_rod", "深渊法杖", "Rod of the Depths");

    public static final ItemEntry<Item> DIRT_ROD = BotaniaReplica.BOTANIA.item("dirt_rod", "大地法杖", "Rod of the Lands");

    public static final ItemEntry<Item> DIVINING_ROD = BotaniaReplica.BOTANIA.item("divining_rod", "占矿法杖", "Rod of the Plentiful Mantle");

    public static final ItemEntry<Item> DREAMWOOD_WAND = BotaniaReplica.BOTANIA.item("dreamwood_wand", "精灵森林法杖", "Wand of the Elven Forest");

    public static final ItemEntry<Item> EXCHANGE_ROD = BotaniaReplica.BOTANIA.item("exchange_rod", "移壳杆", "Rod of the Shifting Crust");

    public static final ItemEntry<Item> FIRE_ROD = BotaniaReplica.BOTANIA.item("fire_rod", "地狱法杖", "Rod of the Hells");

    public static final ItemEntry<Item> GRAVITY_ROD = BotaniaReplica.BOTANIA.item("gravity_rod", "黑山法杖", "Rod of the Shaded Mesa");

    public static final ItemEntry<Item> MISSILE_ROD = BotaniaReplica.BOTANIA.item("missile_rod", "弹幕法杖", "Rod of the Unstable Reservoir");

    public static final ItemEntry<Item> RAINBOW_ROD = BotaniaReplica.BOTANIA.item("rainbow_rod", "彩虹桥法杖", "Rod of the Bifrost");

    public static final ItemEntry<Item> SKYDIRT_ROD = BotaniaReplica.BOTANIA.item("skydirt_rod", "高原法杖", "Rod of the Highlands");

    public static final ItemEntry<Item> SMELT_ROD = BotaniaReplica.BOTANIA.item("smelt_rod", "炽心法杖", "Rod of the Molten Core");

    public static final ItemEntry<Item> TERRAFORM_ROD = BotaniaReplica.BOTANIA.item("terraform_rod", "大地权杖", "Rod of the Terra Firma");

    public static final ItemEntry<Item> TORNADO_ROD = BotaniaReplica.BOTANIA.item("tornado_rod", "天空法杖", "Rod of the Skies");

    public static final ItemEntry<Item> TWIG_WAND = BotaniaReplica.BOTANIA.item("twig_wand", "森林法杖", "Wand of the Forest");

    public static final ItemEntry<Item> WATER_ROD = BotaniaReplica.BOTANIA.item("water_rod", "海洋法杖", "Rod of the Seas");

    // ==================== 武器 ====================

    public static final ItemEntry<Item> MANASTEEL_SWORD = BotaniaReplica.BOTANIA.sword("manasteel_sword", "魔钢剑", "Manasteel Sword", BotaniaTiers.MANASTEEL);

    public static final ItemEntry<Item> ELEMENTIUM_SWORD = BotaniaReplica.BOTANIA.sword("elementium_sword", "源质钢剑", "Elementium Sword", BotaniaTiers.ELEMENTIUM);

    public static final ItemEntry<Item> TERRA_SWORD = BotaniaReplica.BOTANIA.sword("terra_sword", "泰拉之刃", "Terra Blade", BotaniaTiers.TERRASTEEL);

    public static final ItemEntry<Item> STAR_SWORD = BotaniaReplica.BOTANIA.sword("star_sword", "召星者", "Starcaller", BotaniaTiers.TERRASTEEL);

    public static final ItemEntry<Item> THUNDER_SWORD = BotaniaReplica.BOTANIA.sword("thunder_sword", "召雷者", "Thundercaller", BotaniaTiers.TERRASTEEL);

    public static final ItemEntry<Item> ENDER_DAGGER = BotaniaReplica.BOTANIA.sword("ender_dagger", "灵魂匕首", "Soulscribe", BotaniaTiers.TERRASTEEL);

    public static final ItemEntry<Item> LIVINGWOOD_BOW = BotaniaReplica.BOTANIA.bow("livingwood_bow", "活木弓", "Livingwood Bow");

    public static final ItemEntry<Item> CRYSTAL_BOW = BotaniaReplica.BOTANIA.bow("crystal_bow", "水晶弓", "Crystal Bow");

    public static final ItemEntry<Item> THORN_CHAKRAM = BotaniaReplica.BOTANIA.item("thorn_chakram", "荆棘飞轮", "Thorn Chakram",
            vazkii.botania.common.item.ThornChakramItem::new);

    public static final ItemEntry<Item> FLARE_CHAKRAM = BotaniaReplica.BOTANIA.item("flare_chakram", "闪耀飞轮", "Flare Chakram",
            vazkii.botania.common.item.ThornChakramItem::new);

    public static final ItemEntry<Item> SLINGSHOT = BotaniaReplica.BOTANIA.item("slingshot", "活木弹弓", "Livingwood Slingshot");

    // ==================== 盔甲 ====================

    public static final ItemEntry<Item> MANASTEEL_HELMET = BotaniaReplica.BOTANIA.armor("manasteel_helmet", "魔钢头盔", "Manasteel Helmet", BotaniaArmorMaterials.MANASTEEL, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> MANASTEEL_CHESTPLATE = BotaniaReplica.BOTANIA.armor("manasteel_chestplate", "魔钢胸甲", "Manasteel Chestplate", BotaniaArmorMaterials.MANASTEEL, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> MANASTEEL_LEGGINGS = BotaniaReplica.BOTANIA.armor("manasteel_leggings", "魔钢护腿", "Manasteel Leggings", BotaniaArmorMaterials.MANASTEEL, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> MANASTEEL_BOOTS = BotaniaReplica.BOTANIA.armor("manasteel_boots", "魔钢靴子", "Manasteel Boots", BotaniaArmorMaterials.MANASTEEL, ArmorItem.Type.BOOTS);

    public static final ItemEntry<Item> ELEMENTIUM_HELMET = BotaniaReplica.BOTANIA.armor("elementium_helmet", "源质钢头盔", "Elementium Helmet", BotaniaArmorMaterials.ELEMENTIUM, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> ELEMENTIUM_CHESTPLATE = BotaniaReplica.BOTANIA.armor("elementium_chestplate", "源质钢胸甲", "Elementium Chestplate", BotaniaArmorMaterials.ELEMENTIUM, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> ELEMENTIUM_LEGGINGS = BotaniaReplica.BOTANIA.armor("elementium_leggings", "源质钢护腿", "Elementium Leggings", BotaniaArmorMaterials.ELEMENTIUM, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> ELEMENTIUM_BOOTS = BotaniaReplica.BOTANIA.armor("elementium_boots", "源质钢靴子", "Elementium Boots", BotaniaArmorMaterials.ELEMENTIUM, ArmorItem.Type.BOOTS);

    public static final ItemEntry<Item> TERRASTEEL_HELMET = BotaniaReplica.BOTANIA.armor("terrasteel_helmet", "泰拉钢头盔", "Terrasteel Helmet", BotaniaArmorMaterials.TERRASTEEL, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> TERRASTEEL_CHESTPLATE = BotaniaReplica.BOTANIA.armor("terrasteel_chestplate", "泰拉钢胸甲", "Terrasteel Chestplate", BotaniaArmorMaterials.TERRASTEEL, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> TERRASTEEL_LEGGINGS = BotaniaReplica.BOTANIA.armor("terrasteel_leggings", "泰拉钢护腿", "Terrasteel Leggings", BotaniaArmorMaterials.TERRASTEEL, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> TERRASTEEL_BOOTS = BotaniaReplica.BOTANIA.armor("terrasteel_boots", "泰拉钢靴子", "Terrasteel Boots", BotaniaArmorMaterials.TERRASTEEL, ArmorItem.Type.BOOTS);

    public static final ItemEntry<Item> MANAWEAVE_HELMET = BotaniaReplica.BOTANIA.armor("manaweave_helmet", "魔源头蓬", "Manaweave Cowl", BotaniaArmorMaterials.MANAWEAVE, ArmorItem.Type.HELMET);

    public static final ItemEntry<Item> MANAWEAVE_CHESTPLATE = BotaniaReplica.BOTANIA.armor("manaweave_chestplate", "魔源长袍", "Manaweave Robe Top", BotaniaArmorMaterials.MANAWEAVE, ArmorItem.Type.CHESTPLATE);

    public static final ItemEntry<Item> MANAWEAVE_LEGGINGS = BotaniaReplica.BOTANIA.armor("manaweave_leggings", "魔源长袍（下半身）", "Manaweave Robe Bottom", BotaniaArmorMaterials.MANAWEAVE, ArmorItem.Type.LEGGINGS);

    public static final ItemEntry<Item> MANAWEAVE_BOOTS = BotaniaReplica.BOTANIA.armor("manaweave_boots", "魔源长靴", "Manaweave Boots", BotaniaArmorMaterials.MANAWEAVE, ArmorItem.Type.BOOTS);

    // ==================== 饰品 ====================

    public static final ItemEntry<Item> AURA_RING = BotaniaReplica.BOTANIA.item("aura_ring", "光环之戒", "Band of Aura");

    public static final ItemEntry<Item> BALANCE_CLOAK = BotaniaReplica.BOTANIA.item("balance_cloak", "平衡斗篷", "Cloak of Balance");

    public static final ItemEntry<Item> BLOOD_PENDANT = BotaniaReplica.BOTANIA.item("blood_pendant", "污血吊坠", "Tainted Blood Pendant");

    public static final ItemEntry<Item> CLOUD_PENDANT = BotaniaReplica.BOTANIA.item("cloud_pendant", "卷云护符", "Cirrus Amulet");

    public static final ItemEntry<Item> COSMETIC_ALIEN_ANTENNA = BotaniaReplica.BOTANIA.item("cosmetic_alien_antenna", "外星人天线", "Alien Antenna");

    public static final ItemEntry<Item> COSMETIC_ANAGLYPH_GLASSES = BotaniaReplica.BOTANIA.item("cosmetic_anaglyph_glasses", "红蓝眼镜", "Anaglyph Glasses");

    public static final ItemEntry<Item> COSMETIC_ANCIENT_MASK = BotaniaReplica.BOTANIA.item("cosmetic_ancient_mask", "远古面具", "Ancient Mask");

    public static final ItemEntry<Item> COSMETIC_BLACK_BOWTIE = BotaniaReplica.BOTANIA.item("cosmetic_black_bowtie", "黑领结", "Black Bowtie");

    public static final ItemEntry<Item> COSMETIC_BLACK_TIE = BotaniaReplica.BOTANIA.item("cosmetic_black_tie", "黑领带", "Black Tie");

    public static final ItemEntry<Item> COSMETIC_BLUE_BUTTERFLY = BotaniaReplica.BOTANIA.item("cosmetic_blue_butterfly", "蓝蝴蝶", "Blue Butterfly");

    public static final ItemEntry<Item> COSMETIC_BOTANIST_EMBLEM = BotaniaReplica.BOTANIA.item("cosmetic_botanist_emblem", "植物学家徽章", "Botanist Emblem");

    public static final ItemEntry<Item> COSMETIC_CAT_EARS = BotaniaReplica.BOTANIA.item("cosmetic_cat_ears", "猫耳", "Cat Ears");

    public static final ItemEntry<Item> COSMETIC_CLOCK_EYE = BotaniaReplica.BOTANIA.item("cosmetic_clock_eye", "时钟之眼", "Clock Eye");

    public static final ItemEntry<Item> COSMETIC_DEVIL_HORNS = BotaniaReplica.BOTANIA.item("cosmetic_devil_horns", "恶魔尖角", "Devil Horns");

    public static final ItemEntry<Item> COSMETIC_DEVIL_TAIL = BotaniaReplica.BOTANIA.item("cosmetic_devil_tail", "恶魔尾巴", "Devil Tail");

    public static final ItemEntry<Item> COSMETIC_EERIE_MASK = BotaniaReplica.BOTANIA.item("cosmetic_eerie_mask", "怪异面具", "Eerie Mask");

    public static final ItemEntry<Item> COSMETIC_ENGINEER_GOGGLES = BotaniaReplica.BOTANIA.item("cosmetic_engineer_goggles", "工程师护目镜", "Engineer Goggles");

    public static final ItemEntry<Item> COSMETIC_EYEPATCH = BotaniaReplica.BOTANIA.item("cosmetic_eyepatch", "眼罩", "Eyepatch");

    public static final ItemEntry<Item> COSMETIC_FOUR_LEAF_CLOVER = BotaniaReplica.BOTANIA.item("cosmetic_four_leaf_clover", "四叶草", "Four-Leafed Clover");

    public static final ItemEntry<Item> COSMETIC_GOOGLY_EYES = BotaniaReplica.BOTANIA.item("cosmetic_googly_eyes", "青蛙眼", "Googly Eyes");

    public static final ItemEntry<Item> COSMETIC_GROUCHO_GLASSES = BotaniaReplica.BOTANIA.item("cosmetic_groucho_glasses", "大鼻子面具", "Groucho Glasses");

    public static final ItemEntry<Item> COSMETIC_HYPER_PLUS = BotaniaReplica.BOTANIA.item("cosmetic_hyper_plus", "十字头饰", "Hyper Plus");

    public static final ItemEntry<Item> COSMETIC_KAMUI_EYE = BotaniaReplica.BOTANIA.item("cosmetic_kamui_eye", "神威之眼", "Kamui Eye");

    public static final ItemEntry<Item> COSMETIC_LUSITANIC_SHIELD = BotaniaReplica.BOTANIA.item("cosmetic_lusitanic_shield", "盾牌", "Lusitanic Shield");

    public static final ItemEntry<Item> COSMETIC_ORANGE_SHADES = BotaniaReplica.BOTANIA.item("cosmetic_orange_shades", "橙色墨镜", "Orange Shades");

    public static final ItemEntry<Item> COSMETIC_PINK_FLOWER_BUD = BotaniaReplica.BOTANIA.item("cosmetic_pink_flower_bud", "粉花蕾", "Pink Flower Bud");

    public static final ItemEntry<Item> COSMETIC_POLKA_DOTTED_BOWS = BotaniaReplica.BOTANIA.item("cosmetic_polka_dotted_bows", "斑点蝴蝶结", "Polka-Dotted Bows");

    public static final ItemEntry<Item> COSMETIC_PUFFY_SCARF = BotaniaReplica.BOTANIA.item("cosmetic_puffy_scarf", "蓬松围巾", "Puffy Scarf");

    public static final ItemEntry<Item> COSMETIC_QUESTGIVER_MARK = BotaniaReplica.BOTANIA.item("cosmetic_questgiver_mark", "任务人标志", "Questgiver Mark");

    public static final ItemEntry<Item> COSMETIC_RED_GLASSES = BotaniaReplica.BOTANIA.item("cosmetic_red_glasses", "红眼镜", "Red Glasses");

    public static final ItemEntry<Item> COSMETIC_RED_RIBBONS = BotaniaReplica.BOTANIA.item("cosmetic_red_ribbons", "红丝带", "Red Ribbons");

    public static final ItemEntry<Item> COSMETIC_THICK_EYEBROWS = BotaniaReplica.BOTANIA.item("cosmetic_thick_eyebrows", "浓眉毛", "Thick Eyebrows");

    public static final ItemEntry<Item> COSMETIC_THINKING_HAND = BotaniaReplica.BOTANIA.item("cosmetic_thinking_hand", "思考姿态", "Thinking Hand");

    public static final ItemEntry<Item> COSMETIC_TINY_POTATO_MASK = BotaniaReplica.BOTANIA.item("cosmetic_tiny_potato_mask", "小土豆面具", "Tiny Potato Mask");

    public static final ItemEntry<Item> COSMETIC_UNICORN_HORN = BotaniaReplica.BOTANIA.item("cosmetic_unicorn_horn", "独角兽之角", "Unicorn Horn");

    public static final ItemEntry<Item> COSMETIC_WICKED_EYEPATCH = BotaniaReplica.BOTANIA.item("cosmetic_wicked_eyepatch", "中二眼罩", "Wicked Eyepatch");

    public static final ItemEntry<Item> COSMETIC_WITCH_PIN = BotaniaReplica.BOTANIA.item("cosmetic_witch_pin", "女巫别针", "Witch Pin");

    public static final ItemEntry<Item> DIVA_CHARM = BotaniaReplica.BOTANIA.item("diva_charm", "女王之魅", "Charm of the Diva");

    public static final ItemEntry<Item> DODGE_RING = BotaniaReplica.BOTANIA.item("dodge_ring", "闪避指环", "Ring of Dexterous Motion");

    public static final ItemEntry<Item> FLIGHT_TIARA = BotaniaReplica.BOTANIA.item("flight_tiara", "天翼族之冠", "Flügel Tiara");

    public static final ItemEntry<Item> GODDESS_CHARM = BotaniaReplica.BOTANIA.item("goddess_charm", "仁慈女神的链饰", "Benevolent Goddess' Charm");

    public static final ItemEntry<Item> HOLY_CLOAK = BotaniaReplica.BOTANIA.item("holy_cloak", "神圣斗篷", "Cloak of Virtue");

    public static final ItemEntry<Item> ICE_PENDANT = BotaniaReplica.BOTANIA.item("ice_pendant", "雪花项链", "Snowflake Pendant");

    public static final ItemEntry<Item> INVISIBILITY_CLOAK = BotaniaReplica.BOTANIA.item("invisibility_cloak", "隐身斗篷", "Invisibility Cloak");

    public static final ItemEntry<Item> KNOCKBACK_BELT = BotaniaReplica.BOTANIA.item("knockback_belt", "抗击腰带", "Tectonic Girdle");

    public static final ItemEntry<Item> LAVA_PENDANT = BotaniaReplica.BOTANIA.item("lava_pendant", "火石项链", "Pyroclast Pendant");

    public static final ItemEntry<Item> LOKI_RING = BotaniaReplica.BOTANIA.item("loki_ring", "洛基之戒", "Ring of Loki");

    public static final ItemEntry<Item> MAGNET_RING = BotaniaReplica.BOTANIA.item("magnet_ring", "磁化指环", "Ring of Magnetization");

    public static final ItemEntry<Item> MANA_RING = BotaniaReplica.BOTANIA.item("mana_ring", "魔力之戒", "Band of Mana");

    public static final ItemEntry<Item> MINING_RING = BotaniaReplica.BOTANIA.item("mining_ring", "地幔指环", "Ring of the Mantle");

    public static final ItemEntry<Item> MONOCLE = BotaniaReplica.BOTANIA.item("monocle", "窥魔之镜", "Manaseer Monocle");

    public static final ItemEntry<Item> ODIN_RING = BotaniaReplica.BOTANIA.item("odin_ring", "奥丁之戒", "Ring of Odin");

    public static final ItemEntry<Item> PIXIE_RING = BotaniaReplica.BOTANIA.item("pixie_ring", "精灵指环", "Great Fairy Ring");

    public static final ItemEntry<Item> REACH_RING = BotaniaReplica.BOTANIA.item("reach_ring", "触物指环", "Ring of Far Reach");

    public static final ItemEntry<Item> SPEED_UP_BELT = BotaniaReplica.BOTANIA.item("speed_up_belt", "平原旅带", "Planestrider's Sash");

    public static final ItemEntry<Item> SUPER_CLOUD_PENDANT = BotaniaReplica.BOTANIA.item("super_cloud_pendant", "筋斗云护符", "Nimbus Amulet");

    public static final ItemEntry<Item> SUPER_LAVA_PENDANT = BotaniaReplica.BOTANIA.item("super_lava_pendant", "绯红项链", "Crimson Pendant");

    public static final ItemEntry<Item> SUPER_TRAVEL_BELT = BotaniaReplica.BOTANIA.item("super_travel_belt", "环游者腰带", "Globetrotter's Sash");

    public static final ItemEntry<Item> SWAP_RING = BotaniaReplica.BOTANIA.item("swap_ring", "修正指环", "Ring of Correction");

    public static final ItemEntry<Item> THOR_RING = BotaniaReplica.BOTANIA.item("thor_ring", "托尔之戒", "Ring of Thor");

    public static final ItemEntry<Item> TRAVEL_BELT = BotaniaReplica.BOTANIA.item("travel_belt", "旅居者腰带", "Sojourner's Sash");

    public static final ItemEntry<Item> UNHOLY_CLOAK = BotaniaReplica.BOTANIA.item("unholy_cloak", "罪恶斗篷", "Cloak of Sin");

    public static final ItemEntry<Item> WATER_RING = BotaniaReplica.BOTANIA.item("water_ring", "鱼泳指环", "Ring of Chordata");

    public static final ItemEntry<Item> AURA_RING_GREATER = BotaniaReplica.BOTANIA.item("aura_ring_greater", "高级光环之戒", "Greater Band of Aura");

    public static final ItemEntry<Item> BLACK_HOLE_TALISMAN = BotaniaReplica.BOTANIA.item("black_hole_talisman", "黑洞护符", "Black Hole Talisman");

    public static final ItemEntry<Item> MAGNET_RING_GREATER = BotaniaReplica.BOTANIA.item("magnet_ring_greater", "高级磁化指环", "Greater Ring of Magnetization");

    public static final ItemEntry<Item> MANA_RING_GREATER = BotaniaReplica.BOTANIA.item("mana_ring_greater", "高级魔力之戒", "Greater Band of Mana");

    // ==================== 酿造与食物 ====================

    public static final ItemEntry<Item> BREW_FLASK = BotaniaReplica.BOTANIA.item("brew_flask", "大瓶%s精酿 (%s)", "Flask of %s (%s)");

    public static final ItemEntry<Item> BREW_VIAL = BotaniaReplica.BOTANIA.item("brew_vial", "小瓶%s精酿 (%s)", "Vial of %s (%s)");

    public static final ItemEntry<Item> FLASK = BotaniaReplica.BOTANIA.item("flask", "精灵玻璃烧瓶", "Alfglass Flask");

    public static final ItemEntry<Item> VIAL = BotaniaReplica.BOTANIA.item("vial", "魔法玻璃小瓶", "Managlass Vial");

    public static final ItemEntry<Item> INFINITE_FRUIT = BotaniaReplica.BOTANIA.item("infinite_fruit", "禁忌之果", "The Fruit of Grisaia");

    public static final ItemEntry<Item> ENDER_AIR_BOTTLE = BotaniaReplica.BOTANIA.item("ender_air_bottle", "瓶装末地空气", "Ender Air Bottle",
            vazkii.botania.common.item.material.EnderAirItem::new);

    public static final ItemEntry<Item> MANA_BOTTLE = BotaniaReplica.BOTANIA.item("mana_bottle", "瓶装魔力", "Mana in a Bottle");

    public static final ItemEntry<Item> MANA_COOKIE = BotaniaReplica.BOTANIA.item("mana_cookie", "魔力曲奇", "Biscuit of Totality");

    public static final ItemEntry<Item> SLIME_BOTTLE = BotaniaReplica.BOTANIA.item("slime_bottle", "史莱姆罐子", "Slime in a Bottle");

    // ==================== 火花与升级 ====================

    public static final ItemEntry<Item> SPARK = BotaniaReplica.BOTANIA.item("spark", "火花", "Spark");

    public static final ItemEntry<Item> SPARK_UPGRADE_DISPERSIVE = BotaniaReplica.BOTANIA.item("spark_upgrade_dispersive", "火花升级：弥散", "Spark Augment: Dispersive");

    public static final ItemEntry<Item> SPARK_UPGRADE_DOMINANT = BotaniaReplica.BOTANIA.item("spark_upgrade_dominant", "火花升级：聚集", "Spark Augment: Dominant");

    public static final ItemEntry<Item> SPARK_UPGRADE_ISOLATED = BotaniaReplica.BOTANIA.item("spark_upgrade_isolated", "火花升级：隔绝", "Spark Augment: Isolated");

    public static final ItemEntry<Item> SPARK_UPGRADE_RECESSIVE = BotaniaReplica.BOTANIA.item("spark_upgrade_recessive", "火花升级：扩散", "Spark Augment: Recessive");

    // ==================== 图案 ====================

    public static final ItemEntry<Item> PATTERN_1_1 = BotaniaReplica.BOTANIA.item("pattern_1_1", "合成模板#1：1x1", "Crafting Pattern #1: 1x1");

    public static final ItemEntry<Item> PATTERN_1_2 = BotaniaReplica.BOTANIA.item("pattern_1_2", "合成模板#3：1x2", "Crafting Pattern #3: 1x2");

    public static final ItemEntry<Item> PATTERN_1_3 = BotaniaReplica.BOTANIA.item("pattern_1_3", "合成模板#5：1x3", "Crafting Pattern #5: 1x3");

    public static final ItemEntry<Item> PATTERN_2_1 = BotaniaReplica.BOTANIA.item("pattern_2_1", "合成模板#4：2x1", "Crafting Pattern #4: 2x1");

    public static final ItemEntry<Item> PATTERN_2_2 = BotaniaReplica.BOTANIA.item("pattern_2_2", "合成模板#2：2x2", "Crafting Pattern #2: 2x2");

    public static final ItemEntry<Item> PATTERN_2_3 = BotaniaReplica.BOTANIA.item("pattern_2_3", "合成模板#7：2x3", "Crafting Pattern #7: 2x3");

    public static final ItemEntry<Item> PATTERN_3_1 = BotaniaReplica.BOTANIA.item("pattern_3_1", "合成模板#6：3x1", "Crafting Pattern #6: 3x1");

    public static final ItemEntry<Item> PATTERN_3_2 = BotaniaReplica.BOTANIA.item("pattern_3_2", "合成模板#8：3x2", "Crafting Pattern #8: 3x2");

    public static final ItemEntry<Item> PATTERN_DONUT = BotaniaReplica.BOTANIA.item("pattern_donut", "合成模板#9：圈形", "Crafting Pattern #9: Donut");

    // ==================== 唱片与杂项 ====================

    public static final ItemEntry<Item> LEXICON = BotaniaReplica.BOTANIA.item("lexicon", "植物魔法辞典", "Lexica Botania", LexicaBotaniaItem::new);

    public static final ItemEntry<Item> BLACK_LOTUS = BotaniaReplica.BOTANIA.item("black_lotus", "黑莲花", "Black Lotus");

    public static final ItemEntry<Item> BLACKER_LOTUS = BotaniaReplica.BOTANIA.item("blacker_lotus", "暗黑莲花", "Blacker Lotus");

    public static final ItemEntry<Item> DICE = BotaniaReplica.BOTANIA.item("dice", "命运骰子", "Dice of Fate");

    public static final ItemEntry<Item> FLUGEL_EYE = BotaniaReplica.BOTANIA.item("flugel_eye", "天翼族之眼", "Eye of the Flügel");

    public static final ItemEntry<Item> KING_KEY = BotaniaReplica.BOTANIA.item("king_key", "王之宝钥", "Key of the King's Law",
            vazkii.botania.common.item.relic.KeyOfTheKingsLawItem::new);

    public static final ItemEntry<Item> PINKINATOR = BotaniaReplica.BOTANIA.item("pinkinator", "粉色手炮", "The Pinkinator");

    public static final ItemEntry<Item> RECORD_GAIA_1 = BotaniaReplica.BOTANIA.item("record_gaia_1", "破损的唱片", "Scathed Music Disc");

    public static final ItemEntry<Item> RECORD_GAIA_2 = BotaniaReplica.BOTANIA.item("record_gaia_2", "破损的唱片", "Scathed Music Disc");

    public static final ItemEntry<Item> ASTROLABE = BotaniaReplica.BOTANIA.item("astrolabe", "世界塑造者之星盘", "Worldshaper's Astrolabe");

    public static final ItemEntry<Item> CLIP = BotaniaReplica.BOTANIA.item("clip", "透镜夹", "Lens Clip");

    public static final ItemEntry<Item> LAPUTA_SHARD = BotaniaReplica.BOTANIA.item("laputa_shard", "拉普达碎片", "Shard of Laputa");

    public static final ItemEntry<Item> PLACEHOLDER = BotaniaReplica.BOTANIA.item("placeholder", "合成占位符", "Crafting Placeholder");

    public static final ItemEntry<Item> SEXTANT = BotaniaReplica.BOTANIA.item("sextant", "盖世六分仪", "Worldshaper's Sextant");

    public static final ItemEntry<Item> VIRUS_NECRODERMAL = BotaniaReplica.BOTANIA.item("virus_necrodermal", "僵尸病毒", "Necrodermal Virus");

    public static final ItemEntry<Item> VIRUS_NULLODERMAL = BotaniaReplica.BOTANIA.item("virus_nullodermal", "骷髅病毒", "Nullodermal Virus");

    public static net.minecraft.world.item.Item getItem(String id) {
        return BotaniaReplica.BOTANIA.getItem(id);
    }
}
