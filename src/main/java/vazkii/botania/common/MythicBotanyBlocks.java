package vazkii.botania.common;

import net.minecraft.world.level.block.Block;

import com.gto.registrate.util.entry.BlockEntry;

import vazkii.botania.common.block.PylonBlock;

/**
 * 神话植物学方块复刻。按内容类型分组。
 */
public final class MythicBotanyBlocks {

    private MythicBotanyBlocks() {}

    public static void init() {}

    // ==================== 基础材料方块 ====================

    /** 原神话植物学「精灵钢块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_ALFSTEEL_BLOCK = BotaniaReplica.MYTHICBOTANY.block("alfsteel_block", "精灵钢块", "Block of Alfsteel");

    /** 原神话植物学「Block of Raw Elementium」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_RAW_ELEMENTIUM_BLOCK = BotaniaReplica.MYTHICBOTANY.block("raw_elementium_block", "Block of Raw Elementium", "Block of Raw Elementium");

    // ==================== 木材与构件 ====================

    /** 原神话植物学「梦之木树叶」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_DREAMWOOD_LEAVES = BotaniaReplica.MYTHICBOTANY.block("dreamwood_leaves", "梦之木树叶", "Dreamwood Leaves");

    // ==================== 矿石 ====================

    /** 原神话植物学「龙石矿石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_DRAGONSTONE_ORE = BotaniaReplica.MYTHICBOTANY.block("dragonstone_ore", "龙石矿石", "Dragonstone Ore");

    /** 原神话植物学「源质钢矿石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_ELEMENTIUM_ORE = BotaniaReplica.MYTHICBOTANY.block("elementium_ore", "源质钢矿石", "Elementium Ore");

    /** 原神话植物学「金矿石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_GOLD_ORE = BotaniaReplica.MYTHICBOTANY.block("gold_ore", "金矿石", "Gold Ore");

    // ==================== 功能花 ====================

    /** 原神话植物学「百水莲」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_AQUAPANTHUS = BotaniaReplica.MYTHICBOTANY.flower("aquapanthus", "百水莲", "Aquapanthus");

    /** 原神话植物学「悬浮百水莲」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_AQUAPANTHUS_FLOATING = BotaniaReplica.MYTHICBOTANY.floatingFlower("aquapanthus_floating", "悬浮百水莲", "Floating Aquapanthus");

    /** 原神话植物学「烈焰花」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_EXOBLAZE = BotaniaReplica.MYTHICBOTANY.flower("exoblaze", "烈焰花", "Exoblaze");

    /** 原神话植物学「悬浮烈焰花」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_EXOBLAZE_FLOATING = BotaniaReplica.MYTHICBOTANY.floatingFlower("exoblaze_floating", "悬浮烈焰花", "Floating Exoblaze");

    /** 原神话植物学「Feysythia」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_FEYSYTHIA = BotaniaReplica.MYTHICBOTANY.flower("feysythia", "Feysythia", "Feysythia");

    /** 原神话植物学「Floating Feysythia」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_FEYSYTHIA_FLOATING = BotaniaReplica.MYTHICBOTANY.floatingFlower("feysythia_floating", "Floating Feysythia", "Floating Feysythia");

    /** 原神话植物学「鹿食草」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_HELLEBORE = BotaniaReplica.MYTHICBOTANY.flower("hellebore", "鹿食草", "Hellebore");

    /** 原神话植物学「悬浮鹿食草」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_HELLEBORE_FLOATING = BotaniaReplica.MYTHICBOTANY.floatingFlower("hellebore_floating", "悬浮鹿食草", "Floating Hellebore");

    /** 原神话植物学「Petrunia」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_PETRUNIA = BotaniaReplica.MYTHICBOTANY.flower("petrunia", "Petrunia", "Petrunia");

    /** 原神话植物学「Floating Petrunia」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_PETRUNIA_FLOATING = BotaniaReplica.MYTHICBOTANY.floatingFlower("petrunia_floating", "Floating Petrunia", "Floating Petrunia");

    /** 原神话植物学「雨德花」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_RAINDELETIA = BotaniaReplica.MYTHICBOTANY.flower("raindeletia", "雨德花", "Raindeletia");

    /** 原神话植物学「悬浮雨德花」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_RAINDELETIA_FLOATING = BotaniaReplica.MYTHICBOTANY.floatingFlower("raindeletia_floating", "悬浮雨德花", "Floating Raindeletia");

    /** 原神话植物学「凋零菟葵」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_WITHER_ACONITE = BotaniaReplica.MYTHICBOTANY.flower("wither_aconite", "凋零菟葵", "Wither Aconite");

    /** 原神话植物学「悬浮凋零菟葵」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_WITHER_ACONITE_FLOATING = BotaniaReplica.MYTHICBOTANY.floatingFlower("wither_aconite_floating", "悬浮凋零菟葵", "Floating Wither Aconite");

    // ==================== 功能装置 ====================

    /** 原神话植物学「精灵水晶」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_ALFSTEEL_PYLON = BotaniaReplica.MYTHICBOTANY.pylon("alfsteel_pylon", "精灵水晶", "Alfsteel Pylon", PylonBlock.Variant.ALFSTEEL);

    /** 原神话植物学「魔力收集器」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_MANA_COLLECTOR = BotaniaReplica.MYTHICBOTANY.block("mana_collector", "魔力收集器", "Mana Collector");

    /** 原神话植物学「精灵凝聚板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_MANA_INFUSER = BotaniaReplica.MYTHICBOTANY.noOcclusion("mana_infuser", "精灵凝聚板", "Mana-Infuser");

    /** 原神话植物学「妙尔尼尔」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_MJOELLNIR = BotaniaReplica.MYTHICBOTANY.noOcclusion("mjoellnir", "妙尔尼尔", "Mjöllnir");

    /** 原神话植物学「Alfheim Return Portal」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_RETURN_PORTAL = BotaniaReplica.MYTHICBOTANY.returnPortal("return_portal", "Alfheim Return Portal", "Alfheim Return Portal");

    /** 原神话植物学「世界树的枝干」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MB_YGGDRASIL_BRANCH = BotaniaReplica.MYTHICBOTANY.facingNoOcclusion("yggdrasil_branch", "世界树的枝干", "Branch of Yggdrasil");

    public static net.minecraft.world.level.block.Block getBlock(String id) {
        return BotaniaReplica.MYTHICBOTANY.getBlock(id);
    }
}
