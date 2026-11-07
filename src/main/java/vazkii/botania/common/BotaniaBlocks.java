package vazkii.botania.common;

import net.minecraft.world.level.block.Block;

import com.gto.registrate.util.entry.BlockEntry;

import vazkii.botania.common.block.PylonBlock;
import vazkii.botania.common.block.mana.ManaPoolBlock;

/**
 * 植物魔法方块复刻。按内容类型分组。
 */
public final class BotaniaBlocks {

    private BotaniaBlocks() {}

    public static void init() {}

    // ==================== 基础材料方块 ====================

    /** 原植物魔法「精灵玻璃」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELF_GLASS = BotaniaReplica.BOTANIA.block("elf_glass", "精灵玻璃", "Alfglass");

    /** 原植物魔法「魔力玻璃」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_GLASS = BotaniaReplica.BOTANIA.block("mana_glass", "魔力玻璃", "Managlass");

    /** 原植物魔法「源质钢块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELEMENTIUM_BLOCK = BotaniaReplica.BOTANIA.block("elementium_block", "源质钢块", "Block of Elementium");

    /** 原植物魔法「魔力钻石块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_DIAMOND_BLOCK = BotaniaReplica.BOTANIA.block("mana_diamond_block", "魔力钻石块", "Block of Mana Diamond");

    /** 原植物魔法「魔力钢块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANASTEEL_BLOCK = BotaniaReplica.BOTANIA.block("manasteel_block", "魔力钢块", "Block of Manasteel");

    /** 原植物魔法「泰拉钢块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TERRASTEEL_BLOCK = BotaniaReplica.BOTANIA.block("terrasteel_block", "泰拉钢块", "Block of Terrasteel");

    // ==================== 木材与构件 ====================

    public static final BlockEntry<Block> DREAMWOOD = BotaniaReplica.BOTANIA.axis("dreamwood", "梦之木", "Dreamwood");

    public static final BlockEntry<Block> DREAMWOOD_LOG = BotaniaReplica.BOTANIA.axis("dreamwood_log", "梦之木原木", "Dreamwood Log");

    /** 原植物魔法「梦之木板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DREAMWOOD_PLANKS = BotaniaReplica.BOTANIA.block("dreamwood_planks", "梦之木板", "Dreamwood Planks");

    public static final BlockEntry<Block> FRAMED_DREAMWOOD = BotaniaReplica.BOTANIA.axis("framed_dreamwood", "框边梦之木板", "Framed Dreamwood Planks");

    public static final BlockEntry<Block> FRAMED_LIVINGWOOD = BotaniaReplica.BOTANIA.axis("framed_livingwood", "框边活木板", "Framed Livingwood Planks");

    public static final BlockEntry<Block> GLIMMERING_DREAMWOOD = BotaniaReplica.BOTANIA.axis("glimmering_dreamwood", "微光梦之木", "Glimmering Dreamwood");

    public static final BlockEntry<Block> GLIMMERING_DREAMWOOD_LOG = BotaniaReplica.BOTANIA.axis("glimmering_dreamwood_log", "微光梦之原木", "Glimmering Dreamwood Log");

    public static final BlockEntry<Block> GLIMMERING_LIVINGWOOD = BotaniaReplica.BOTANIA.axis("glimmering_livingwood", "微光活木", "Glimmering Livingwood");

    public static final BlockEntry<Block> GLIMMERING_LIVINGWOOD_LOG = BotaniaReplica.BOTANIA.axis("glimmering_livingwood_log", "微光活木原木", "Glimmering Livingwood Log");

    public static final BlockEntry<Block> GLIMMERING_STRIPPED_DREAMWOOD = BotaniaReplica.BOTANIA.axis("glimmering_stripped_dreamwood", "去皮微光梦之木", "Glimmering Stripped Dreamwood");

    public static final BlockEntry<Block> GLIMMERING_STRIPPED_DREAMWOOD_LOG = BotaniaReplica.BOTANIA.axis("glimmering_stripped_dreamwood_log", "去皮微光梦之木原木", "Glimmering Stripped Dreamwood Log");

    public static final BlockEntry<Block> GLIMMERING_STRIPPED_LIVINGWOOD = BotaniaReplica.BOTANIA.axis("glimmering_stripped_livingwood", "去皮微光活木", "Glimmering Stripped Livingwood");

    public static final BlockEntry<Block> GLIMMERING_STRIPPED_LIVINGWOOD_LOG = BotaniaReplica.BOTANIA.axis("glimmering_stripped_livingwood_log", "去皮微光活木原木", "Glimmering Stripped Livingwood Log");

    public static final BlockEntry<Block> LIVINGWOOD = BotaniaReplica.BOTANIA.axis("livingwood", "活木", "Livingwood");

    public static final BlockEntry<Block> LIVINGWOOD_LOG = BotaniaReplica.BOTANIA.axis("livingwood_log", "活木原木", "Livingwood Log");

    /** 原植物魔法「活木板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LIVINGWOOD_PLANKS = BotaniaReplica.BOTANIA.block("livingwood_planks", "活木板", "Livingwood Planks");

    /** 原植物魔法「苔梦之木板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MOSSY_DREAMWOOD_PLANKS = BotaniaReplica.BOTANIA.block("mossy_dreamwood_planks", "苔梦之木板", "Mossy Dreamwood Planks");

    /** 原植物魔法「苔活木板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MOSSY_LIVINGWOOD_PLANKS = BotaniaReplica.BOTANIA.block("mossy_livingwood_planks", "苔活木板", "Mossy Livingwood Planks");

    /** 原植物魔法「框纹梦之木板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> PATTERN_FRAMED_DREAMWOOD = BotaniaReplica.BOTANIA.block("pattern_framed_dreamwood", "框纹梦之木板", "Pattern Framed Dreamwood Planks");

    /** 原植物魔法「框纹活木板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> PATTERN_FRAMED_LIVINGWOOD = BotaniaReplica.BOTANIA.block("pattern_framed_livingwood", "框纹活木板", "Pattern Framed Livingwood Planks");

    /** 原植物魔法「微光木板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SHIMMERWOOD_PLANKS = BotaniaReplica.BOTANIA.block("shimmerwood_planks", "微光木板", "Shimmerwood Planks");

    public static final BlockEntry<Block> STRIPPED_DREAMWOOD = BotaniaReplica.BOTANIA.axis("stripped_dreamwood", "去皮梦之木", "Stripped Dreamwood");

    public static final BlockEntry<Block> STRIPPED_DREAMWOOD_LOG = BotaniaReplica.BOTANIA.axis("stripped_dreamwood_log", "去皮梦之木原木", "Stripped Dreamwood Log");

    public static final BlockEntry<Block> STRIPPED_LIVINGWOOD = BotaniaReplica.BOTANIA.axis("stripped_livingwood", "去皮活木", "Stripped Livingwood");

    public static final BlockEntry<Block> STRIPPED_LIVINGWOOD_LOG = BotaniaReplica.BOTANIA.axis("stripped_livingwood_log", "去皮活木原木", "Stripped Livingwood Log");

    // ==================== 石材与构件 ====================

    /** 原植物魔法「框边活石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_LIVINGROCK_BRICKS = BotaniaReplica.BOTANIA.block("chiseled_livingrock_bricks", "框边活石砖", "Chiseled Livingrock Bricks");

    /** 原植物魔法「錾制变质沙漠石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_METAMORPHIC_DESERT_BRICKS = BotaniaReplica.BOTANIA.block("chiseled_metamorphic_desert_bricks", "錾制变质沙漠石砖", "Chiseled Solite Bricks");

    /** 原植物魔法「錾制变质森林石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_METAMORPHIC_FOREST_BRICKS = BotaniaReplica.BOTANIA.block("chiseled_metamorphic_forest_bricks", "錾制变质森林石砖", "Chiseled Fuchsite Bricks");

    /** 原植物魔法「錾制变质菌丝石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_METAMORPHIC_FUNGAL_BRICKS = BotaniaReplica.BOTANIA.block("chiseled_metamorphic_fungal_bricks", "錾制变质菌丝石砖", "Chiseled Mycelite Bricks");

    public static final BlockEntry<Block> CHISELED_METAMORPHIC_MESA_BRICKS = BotaniaReplica.BOTANIA.axis("chiseled_metamorphic_mesa_bricks", "錾制变质高原石砖", "Chiseled Rosy Talc Bricks");

    /** 原植物魔法「錾制变质高山石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_METAMORPHIC_MOUNTAIN_BRICKS = BotaniaReplica.BOTANIA.block("chiseled_metamorphic_mountain_bricks", "錾制变质高山石砖", "Chiseled Gneiss Bricks");

    /** 原植物魔法「錾制变质平原石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_METAMORPHIC_PLAINS_BRICKS = BotaniaReplica.BOTANIA.block("chiseled_metamorphic_plains_bricks", "錾制变质平原石砖", "Chiseled Talc Bricks");

    /** 原植物魔法「錾制变质沼泽石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_METAMORPHIC_SWAMP_BRICKS = BotaniaReplica.BOTANIA.facingAny("chiseled_metamorphic_swamp_bricks", "錾制变质沼泽石砖", "Chiseled Cataclasite Bricks");

    /** 原植物魔法「錾制变质雪原石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_METAMORPHIC_TAIGA_BRICKS = BotaniaReplica.BOTANIA.block("chiseled_metamorphic_taiga_bricks", "錾制变质雪原石砖", "Chiseled Lunite Bricks");

    /** 原植物魔法「裂活石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CRACKED_LIVINGROCK_BRICKS = BotaniaReplica.BOTANIA.block("cracked_livingrock_bricks", "裂活石砖", "Cracked Livingrock Bricks");

    /** 原植物魔法「活石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LIVINGROCK = BotaniaReplica.BOTANIA.block("livingrock", "活石", "Livingrock");

    /** 原植物魔法「活石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LIVINGROCK_BRICKS = BotaniaReplica.BOTANIA.block("livingrock_bricks", "活石砖", "Livingrock Bricks");

    /** 原植物魔法「活石板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LIVINGROCK_SLATE = BotaniaReplica.BOTANIA.block("livingrock_slate", "活石板", "Livingrock Slate");

    /** 原植物魔法「变质沙漠石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_DESERT_BRICKS = BotaniaReplica.BOTANIA.block("metamorphic_desert_bricks", "变质沙漠石砖", "Solite Bricks");

    /** 原植物魔法「变质沙漠圆石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_DESERT_COBBLESTONE = BotaniaReplica.BOTANIA.block("metamorphic_desert_cobblestone", "变质沙漠圆石", "Cobbled Solite");

    /** 原植物魔法「变质沙漠石头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_DESERT_STONE = BotaniaReplica.BOTANIA.block("metamorphic_desert_stone", "变质沙漠石头", "Solite");

    /** 原植物魔法「变质森林石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FOREST_BRICKS = BotaniaReplica.BOTANIA.block("metamorphic_forest_bricks", "变质森林石砖", "Fuchsite Bricks");

    /** 原植物魔法「变质森林圆石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FOREST_COBBLESTONE = BotaniaReplica.BOTANIA.block("metamorphic_forest_cobblestone", "变质森林圆石", "Cobbled Fuchsite");

    /** 原植物魔法「变质森林石头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FOREST_STONE = BotaniaReplica.BOTANIA.block("metamorphic_forest_stone", "变质森林石头", "Fuchsite");

    /** 原植物魔法「变质菌丝石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_BRICKS = BotaniaReplica.BOTANIA.block("metamorphic_fungal_bricks", "变质菌丝石砖", "Mycelite Bricks");

    /** 原植物魔法「变质菌丝圆石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_COBBLESTONE = BotaniaReplica.BOTANIA.block("metamorphic_fungal_cobblestone", "变质菌丝圆石", "Cobbled Mycelite");

    /** 原植物魔法「变质菌丝石头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_STONE = BotaniaReplica.BOTANIA.block("metamorphic_fungal_stone", "变质菌丝石头", "Mycelite");

    /** 原植物魔法「变质高原石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MESA_BRICKS = BotaniaReplica.BOTANIA.block("metamorphic_mesa_bricks", "变质高原石砖", "Rosy Talc Bricks");

    /** 原植物魔法「变质高原圆石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MESA_COBBLESTONE = BotaniaReplica.BOTANIA.block("metamorphic_mesa_cobblestone", "变质高原圆石", "Cobbled Rosy Talc");

    /** 原植物魔法「变质高原石头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MESA_STONE = BotaniaReplica.BOTANIA.block("metamorphic_mesa_stone", "变质高原石头", "Rosy Talc");

    /** 原植物魔法「变质高山石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_BRICKS = BotaniaReplica.BOTANIA.block("metamorphic_mountain_bricks", "变质高山石砖", "Gneiss Bricks");

    /** 原植物魔法「变质高山圆石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_COBBLESTONE = BotaniaReplica.BOTANIA.block("metamorphic_mountain_cobblestone", "变质高山圆石", "Cobbled Gneiss");

    /** 原植物魔法「变质高山石头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_STONE = BotaniaReplica.BOTANIA.block("metamorphic_mountain_stone", "变质高山石头", "Gneiss");

    public static final BlockEntry<Block> METAMORPHIC_PLAINS_BRICKS = BotaniaReplica.BOTANIA.axis("metamorphic_plains_bricks", "变质平原石砖", "Talc Bricks");

    /** 原植物魔法「变质平原圆石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_PLAINS_COBBLESTONE = BotaniaReplica.BOTANIA.block("metamorphic_plains_cobblestone", "变质平原圆石", "Cobbled Talc");

    /** 原植物魔法「变质平原石头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_PLAINS_STONE = BotaniaReplica.BOTANIA.block("metamorphic_plains_stone", "变质平原石头", "Talc");

    /** 原植物魔法「变质沼泽石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_SWAMP_BRICKS = BotaniaReplica.BOTANIA.facingAny("metamorphic_swamp_bricks", "变质沼泽石砖", "Cataclasite Bricks");

    /** 原植物魔法「变质沼泽圆石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_SWAMP_COBBLESTONE = BotaniaReplica.BOTANIA.block("metamorphic_swamp_cobblestone", "变质沼泽圆石", "Cobbled Cataclasite");

    /** 原植物魔法「变质沼泽石头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_SWAMP_STONE = BotaniaReplica.BOTANIA.block("metamorphic_swamp_stone", "变质沼泽石头", "Cataclasite");

    /** 原植物魔法「变质雪原石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_TAIGA_BRICKS = BotaniaReplica.BOTANIA.block("metamorphic_taiga_bricks", "变质雪原石砖", "Lunite Bricks");

    /** 原植物魔法「变质雪原圆石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_TAIGA_COBBLESTONE = BotaniaReplica.BOTANIA.block("metamorphic_taiga_cobblestone", "变质雪原圆石", "Cobbled Lunite");

    /** 原植物魔法「变质雪原石头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_TAIGA_STONE = BotaniaReplica.BOTANIA.block("metamorphic_taiga_stone", "变质雪原石头", "Lunite");

    /** 原植物魔法「苔活石砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MOSSY_LIVINGROCK_BRICKS = BotaniaReplica.BOTANIA.block("mossy_livingrock_bricks", "苔活石砖", "Mossy Livingrock Bricks");

    /** 原植物魔法「磨制活石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POLISHED_LIVINGROCK = BotaniaReplica.BOTANIA.block("polished_livingrock", "磨制活石", "Polished Livingrock");

    /** 原植物魔法「微光石」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SHIMMERROCK = BotaniaReplica.BOTANIA.block("shimmerrock", "微光石", "Shimmerrock");

    /** 原植物魔法「活石花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_LIVINGROCK = BotaniaReplica.BOTANIA.apothecary("apothecary_livingrock", "活石花药台", "Livingrock Petal Apothecary");

    // ==================== 石英 ====================

    /** 原植物魔法「烈焰石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BLAZE_QUARTZ = BotaniaReplica.BOTANIA.block("blaze_quartz", "烈焰石英块", "Block of Blaze Quartz");

    public static final BlockEntry<Block> BLAZE_QUARTZ_PILLAR = BotaniaReplica.BOTANIA.axis("blaze_quartz_pillar", "竖纹烈焰石英块", "Pillar Blaze Quartz Block");

    /** 原植物魔法「錾制烈焰石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_BLAZE_QUARTZ = BotaniaReplica.BOTANIA.block("chiseled_blaze_quartz", "錾制烈焰石英块", "Chiseled Blaze Quartz Block");

    /** 原植物魔法「錾制烟熏石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_DARK_QUARTZ = BotaniaReplica.BOTANIA.block("chiseled_dark_quartz", "錾制烟熏石英块", "Chiseled Smokey Quartz Block");

    /** 原植物魔法「錾制精灵石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_ELF_QUARTZ = BotaniaReplica.BOTANIA.block("chiseled_elf_quartz", "錾制精灵石英块", "Chiseled Elven Quartz Block");

    /** 原植物魔法「錾制熏香石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_LAVENDER_QUARTZ = BotaniaReplica.BOTANIA.block("chiseled_lavender_quartz", "錾制熏香石英块", "Chiseled Lavender Quartz Block");

    /** 原植物魔法「錾制魔力石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_MANA_QUARTZ = BotaniaReplica.BOTANIA.block("chiseled_mana_quartz", "錾制魔力石英块", "Chiseled Mana Quartz Block");

    /** 原植物魔法「錾制红色石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_RED_QUARTZ = BotaniaReplica.BOTANIA.block("chiseled_red_quartz", "錾制红色石英块", "Chiseled Redquartz Block");

    /** 原植物魔法「錾制金黄石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_SUNNY_QUARTZ = BotaniaReplica.BOTANIA.block("chiseled_sunny_quartz", "錾制金黄石英块", "Chiseled Sunny Quartz Block");

    /** 原植物魔法「烟熏石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DARK_QUARTZ = BotaniaReplica.BOTANIA.block("dark_quartz", "烟熏石英块", "Block of Smokey Quartz");

    public static final BlockEntry<Block> DARK_QUARTZ_PILLAR = BotaniaReplica.BOTANIA.axis("dark_quartz_pillar", "竖纹烟熏石英块", "Pillar Smokey Quartz Block");

    /** 原植物魔法「精灵石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELF_QUARTZ = BotaniaReplica.BOTANIA.block("elf_quartz", "精灵石英块", "Block of Elven Quartz");

    public static final BlockEntry<Block> ELF_QUARTZ_PILLAR = BotaniaReplica.BOTANIA.axis("elf_quartz_pillar", "竖纹精灵石英块", "Pillar Elven Quartz Block");

    /** 原植物魔法「熏香石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LAVENDER_QUARTZ = BotaniaReplica.BOTANIA.block("lavender_quartz", "熏香石英块", "Block of Lavender Quartz");

    public static final BlockEntry<Block> LAVENDER_QUARTZ_PILLAR = BotaniaReplica.BOTANIA.axis("lavender_quartz_pillar", "竖纹熏香石英块", "Pillar Lavender Quartz Block");

    /** 原植物魔法「魔力石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_QUARTZ = BotaniaReplica.BOTANIA.block("mana_quartz", "魔力石英块", "Block of Mana Quartz");

    public static final BlockEntry<Block> MANA_QUARTZ_PILLAR = BotaniaReplica.BOTANIA.axis("mana_quartz_pillar", "竖纹魔力石英块", "Pillar Mana Quartz Block");

    /** 原植物魔法「红色石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RED_QUARTZ = BotaniaReplica.BOTANIA.block("red_quartz", "红色石英块", "Block of Redquartz");

    public static final BlockEntry<Block> RED_QUARTZ_PILLAR = BotaniaReplica.BOTANIA.axis("red_quartz_pillar", "竖纹红色石英块", "Pillar Redquartz Block");

    /** 原植物魔法「金黄石英块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SUNNY_QUARTZ = BotaniaReplica.BOTANIA.block("sunny_quartz", "金黄石英块", "Block of Sunny Quartz");

    public static final BlockEntry<Block> SUNNY_QUARTZ_PILLAR = BotaniaReplica.BOTANIA.axis("sunny_quartz_pillar", "竖纹金黄石英块", "Pillar Sunny Quartz Block");

    // ==================== 路面 ====================

    /** 原植物魔法「黑色葡萄牙路面」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BLACK_PAVEMENT = BotaniaReplica.BOTANIA.block("black_pavement", "黑色葡萄牙路面", "Black Portuguese Pavement");

    /** 原植物魔法「蓝色葡萄牙路面」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BLUE_PAVEMENT = BotaniaReplica.BOTANIA.block("blue_pavement", "蓝色葡萄牙路面", "Blue Portuguese Pavement");

    /** 原植物魔法「绿色葡萄牙路面」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GREEN_PAVEMENT = BotaniaReplica.BOTANIA.block("green_pavement", "绿色葡萄牙路面", "Green Portuguese Pavement");

    /** 原植物魔法「红色葡萄牙路面」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RED_PAVEMENT = BotaniaReplica.BOTANIA.block("red_pavement", "红色葡萄牙路面", "Red Portuguese Pavement");

    /** 原植物魔法「白色葡萄牙路面」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> WHITE_PAVEMENT = BotaniaReplica.BOTANIA.block("white_pavement", "白色葡萄牙路面", "White Portuguese Pavement");

    /** 原植物魔法「黄色葡萄牙路面」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> YELLOW_PAVEMENT = BotaniaReplica.BOTANIA.block("yellow_pavement", "黄色葡萄牙路面", "Yellow Portuguese Pavement");

    // ==================== 台阶楼梯墙栅栏与玻璃板 ====================

    public static final BlockEntry<Block> CORPOREA_BRICK_SLAB = BotaniaReplica.BOTANIA.slab("corporea_brick_slab", "多媒体砖台阶", "Corporea Brick Slab");

    public static final BlockEntry<Block> CORPOREA_BRICK_STAIRS = BotaniaReplica.BOTANIA.stairs("corporea_brick_stairs", "多媒体砖楼梯", "Corporea Brick Stairs");

    public static final BlockEntry<Block> CORPOREA_BRICK_WALL = BotaniaReplica.BOTANIA.wall("corporea_brick_wall", "多媒体砖墙", "Corporea Brick Wall");

    public static final BlockEntry<Block> CORPOREA_SLAB = BotaniaReplica.BOTANIA.slab("corporea_slab", "多媒体台阶", "Corporea Slab");

    public static final BlockEntry<Block> CORPOREA_STAIRS = BotaniaReplica.BOTANIA.stairs("corporea_stairs", "多媒体楼梯", "Corporea Stairs");

    public static final BlockEntry<Block> DREAMWOOD_FENCE = BotaniaReplica.BOTANIA.fence("dreamwood_fence", "梦之木栅栏", "Dreamwood Fence");

    public static final BlockEntry<Block> DREAMWOOD_FENCE_GATE = BotaniaReplica.BOTANIA.fenceGate("dreamwood_fence_gate", "梦之木栅栏门", "Dreamwood Fence Gate");

    public static final BlockEntry<Block> DREAMWOOD_PLANKS_SLAB = BotaniaReplica.BOTANIA.slab("dreamwood_planks_slab", "梦之木板台阶", "Dreamwood Plank Slab");

    /** 原植物魔法「梦之木板楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DREAMWOOD_PLANKS_STAIRS = BotaniaReplica.BOTANIA.stairs("dreamwood_planks_stairs", "梦之木板楼梯", "Dreamwood Plank Stairs");

    public static final BlockEntry<Block> DREAMWOOD_SLAB = BotaniaReplica.BOTANIA.slab("dreamwood_slab", "梦之木台阶", "Dreamwood Slab");

    /** 原植物魔法「梦之木楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DREAMWOOD_STAIRS = BotaniaReplica.BOTANIA.stairs("dreamwood_stairs", "梦之木楼梯", "Dreamwood Stairs");

    public static final BlockEntry<Block> DREAMWOOD_WALL = BotaniaReplica.BOTANIA.wall("dreamwood_wall", "梦之木墙", "Dreamwood Wall");

    public static final BlockEntry<Block> LIVINGWOOD_FENCE = BotaniaReplica.BOTANIA.fence("livingwood_fence", "活木栅栏", "Livingwood Fence");

    public static final BlockEntry<Block> LIVINGWOOD_FENCE_GATE = BotaniaReplica.BOTANIA.fenceGate("livingwood_fence_gate", "活木栅栏门", "Livingwood Fence Gate");

    public static final BlockEntry<Block> LIVINGWOOD_PLANKS_SLAB = BotaniaReplica.BOTANIA.slab("livingwood_planks_slab", "活木板台阶", "Livingwood Plank Slab");

    /** 原植物魔法「活木板楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LIVINGWOOD_PLANKS_STAIRS = BotaniaReplica.BOTANIA.stairs("livingwood_planks_stairs", "活木板楼梯", "Livingwood Plank Stairs");

    public static final BlockEntry<Block> LIVINGWOOD_SLAB = BotaniaReplica.BOTANIA.slab("livingwood_slab", "活木台阶", "Livingwood Slab");

    /** 原植物魔法「活木楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LIVINGWOOD_STAIRS = BotaniaReplica.BOTANIA.stairs("livingwood_stairs", "活木楼梯", "Livingwood Stairs");

    public static final BlockEntry<Block> LIVINGWOOD_WALL = BotaniaReplica.BOTANIA.wall("livingwood_wall", "活木墙", "Livingwood Wall");

    public static final BlockEntry<Block> SHIMMERWOOD_PLANKS_SLAB = BotaniaReplica.BOTANIA.slab("shimmerwood_planks_slab", "微光木板台阶", "Shimmerwood Plank Slab");

    /** 原植物魔法「微光木板楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SHIMMERWOOD_PLANKS_STAIRS = BotaniaReplica.BOTANIA.stairs("shimmerwood_planks_stairs", "微光木板楼梯", "Shimmerwood Plank Stairs");

    public static final BlockEntry<Block> STRIPPED_DREAMWOOD_SLAB = BotaniaReplica.BOTANIA.slab("stripped_dreamwood_slab", "去皮梦之木台阶", "Stripped Dreamwood Slab");

    /** 原植物魔法「去皮梦之木楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> STRIPPED_DREAMWOOD_STAIRS = BotaniaReplica.BOTANIA.stairs("stripped_dreamwood_stairs", "去皮梦之木楼梯", "Stripped Dreamwood Stairs");

    public static final BlockEntry<Block> STRIPPED_DREAMWOOD_WALL = BotaniaReplica.BOTANIA.wall("stripped_dreamwood_wall", "去皮梦之木墙", "Stripped Dreamwood Wall");

    public static final BlockEntry<Block> STRIPPED_LIVINGWOOD_SLAB = BotaniaReplica.BOTANIA.slab("stripped_livingwood_slab", "去皮活木台阶", "Stripped Livingwood Slab");

    /** 原植物魔法「去皮活木楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> STRIPPED_LIVINGWOOD_STAIRS = BotaniaReplica.BOTANIA.stairs("stripped_livingwood_stairs", "去皮活木楼梯", "Stripped Livingwood Stairs");

    public static final BlockEntry<Block> STRIPPED_LIVINGWOOD_WALL = BotaniaReplica.BOTANIA.wall("stripped_livingwood_wall", "去皮活木墙", "Stripped Livingwood Wall");

    /** 原植物魔法「彩虹玻璃板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BIFROST_PANE = BotaniaReplica.BOTANIA.pane("bifrost_pane", "彩虹玻璃板", "Bifrost Pane");

    public static final BlockEntry<Block> BLACK_PAVEMENT_SLAB = BotaniaReplica.BOTANIA.slab("black_pavement_slab", "黑色葡萄牙台阶", "Black Portuguese Pavement Slab");

    /** 原植物魔法「黑色葡萄牙楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BLACK_PAVEMENT_STAIRS = BotaniaReplica.BOTANIA.stairs("black_pavement_stairs", "黑色葡萄牙楼梯", "Black Portuguese Pavement Stairs");

    public static final BlockEntry<Block> BLAZE_QUARTZ_SLAB = BotaniaReplica.BOTANIA.slab("blaze_quartz_slab", "烈焰石英台阶", "Blaze Quartz Slab");

    /** 原植物魔法「烈焰石英楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BLAZE_QUARTZ_STAIRS = BotaniaReplica.BOTANIA.stairs("blaze_quartz_stairs", "烈焰石英楼梯", "Blaze Quartz Stairs");

    public static final BlockEntry<Block> BLUE_PAVEMENT_SLAB = BotaniaReplica.BOTANIA.slab("blue_pavement_slab", "蓝色葡萄牙台阶", "Blue Portuguese Pavement Slab");

    /** 原植物魔法「蓝色葡萄牙楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BLUE_PAVEMENT_STAIRS = BotaniaReplica.BOTANIA.stairs("blue_pavement_stairs", "蓝色葡萄牙楼梯", "Blue Portuguese Pavement Stairs");

    public static final BlockEntry<Block> DARK_QUARTZ_SLAB = BotaniaReplica.BOTANIA.slab("dark_quartz_slab", "烟熏石英台阶", "Smokey Quartz Slab");

    /** 原植物魔法「烟熏石英楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DARK_QUARTZ_STAIRS = BotaniaReplica.BOTANIA.stairs("dark_quartz_stairs", "烟熏石英楼梯", "Smokey Quartz Stairs");

    /** 原植物魔法「精灵玻璃板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELF_GLASS_PANE = BotaniaReplica.BOTANIA.pane("elf_glass_pane", "精灵玻璃板", "Alfglass Pane");

    public static final BlockEntry<Block> ELF_QUARTZ_SLAB = BotaniaReplica.BOTANIA.slab("elf_quartz_slab", "精灵石英台阶", "Elven Quartz Slab");

    /** 原植物魔法「精灵石英楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELF_QUARTZ_STAIRS = BotaniaReplica.BOTANIA.stairs("elf_quartz_stairs", "精灵石英楼梯", "Elven Quartz Stairs");

    public static final BlockEntry<Block> GREEN_PAVEMENT_SLAB = BotaniaReplica.BOTANIA.slab("green_pavement_slab", "绿色葡萄牙台阶", "Green Portuguese Pavement Slab");

    /** 原植物魔法「绿色葡萄牙楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GREEN_PAVEMENT_STAIRS = BotaniaReplica.BOTANIA.stairs("green_pavement_stairs", "绿色葡萄牙楼梯", "Green Portuguese Pavement Stairs");

    public static final BlockEntry<Block> LAVENDER_QUARTZ_SLAB = BotaniaReplica.BOTANIA.slab("lavender_quartz_slab", "熏香石英台阶", "Lavender Quartz Slab");

    /** 原植物魔法「熏香石英楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LAVENDER_QUARTZ_STAIRS = BotaniaReplica.BOTANIA.stairs("lavender_quartz_stairs", "熏香石英楼梯", "Lavender Quartz Stairs");

    public static final BlockEntry<Block> LIVINGROCK_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("livingrock_bricks_slab", "活石砖台阶", "Livingrock Brick Slab");

    /** 原植物魔法「活石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LIVINGROCK_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("livingrock_bricks_stairs", "活石砖楼梯", "Livingrock Brick Stairs");

    public static final BlockEntry<Block> LIVINGROCK_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("livingrock_bricks_wall", "活石砖墙", "Livingrock Brick Wall");

    public static final BlockEntry<Block> LIVINGROCK_SLAB = BotaniaReplica.BOTANIA.slab("livingrock_slab", "活石台阶", "Livingrock Slab");

    /** 原植物魔法「活石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LIVINGROCK_STAIRS = BotaniaReplica.BOTANIA.stairs("livingrock_stairs", "活石楼梯", "Livingrock Stairs");

    public static final BlockEntry<Block> LIVINGROCK_WALL = BotaniaReplica.BOTANIA.wall("livingrock_wall", "活石墙", "Livingrock Wall");

    /** 原植物魔法「魔力玻璃板」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_GLASS_PANE = BotaniaReplica.BOTANIA.pane("mana_glass_pane", "魔力玻璃板", "Managlass Pane");

    public static final BlockEntry<Block> MANA_QUARTZ_SLAB = BotaniaReplica.BOTANIA.slab("mana_quartz_slab", "魔力石英台阶", "Mana Quartz Slab");

    /** 原植物魔法「魔力石英楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_QUARTZ_STAIRS = BotaniaReplica.BOTANIA.stairs("mana_quartz_stairs", "魔力石英楼梯", "Mana Quartz Stairs");

    public static final BlockEntry<Block> METAMORPHIC_DESERT_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_desert_bricks_slab", "变质沙漠石砖台阶", "Solite Brick Slab");

    /** 原植物魔法「变质沙漠石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_DESERT_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_desert_bricks_stairs", "变质沙漠石砖楼梯", "Solite Brick Stairs");

    public static final BlockEntry<Block> METAMORPHIC_DESERT_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_desert_bricks_wall", "变质沙漠石砖墙", "Solite Brick Wall");

    public static final BlockEntry<Block> METAMORPHIC_DESERT_COBBLESTONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_desert_cobblestone_slab", "变质沙漠圆石台阶", "Cobbled Solite Slab");

    /** 原植物魔法「变质沙漠圆石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_DESERT_COBBLESTONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_desert_cobblestone_stairs", "变质沙漠圆石楼梯", "Cobbled Solite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_DESERT_COBBLESTONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_desert_cobblestone_wall", "变质沙漠圆石墙", "Cobbled Solite Wall");

    public static final BlockEntry<Block> METAMORPHIC_DESERT_STONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_desert_stone_slab", "变质沙漠石头台阶", "Solite Slab");

    /** 原植物魔法「变质沙漠石头楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_DESERT_STONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_desert_stone_stairs", "变质沙漠石头楼梯", "Solite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_DESERT_STONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_desert_stone_wall", "变质沙漠石墙", "Solite Wall");

    public static final BlockEntry<Block> METAMORPHIC_FOREST_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_forest_bricks_slab", "变质森林石砖台阶", "Fuchsite Brick Slab");

    /** 原植物魔法「变质森林石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FOREST_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_forest_bricks_stairs", "变质森林石砖楼梯", "Fuchsite Brick Stairs");

    public static final BlockEntry<Block> METAMORPHIC_FOREST_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_forest_bricks_wall", "变质森林石砖墙", "Fuchsite Brick Wall");

    public static final BlockEntry<Block> METAMORPHIC_FOREST_COBBLESTONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_forest_cobblestone_slab", "变质森林圆石台阶", "Cobbled Fuchsite Slab");

    /** 原植物魔法「变质森林圆石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FOREST_COBBLESTONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_forest_cobblestone_stairs", "变质森林圆石楼梯", "Cobbled Fuchsite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_FOREST_COBBLESTONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_forest_cobblestone_wall", "变质森林圆石墙", "Cobbled Fuchsite Wall");

    public static final BlockEntry<Block> METAMORPHIC_FOREST_STONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_forest_stone_slab", "变质森林石头台阶", "Fuchsite Slab");

    /** 原植物魔法「变质森林石头楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FOREST_STONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_forest_stone_stairs", "变质森林石头楼梯", "Fuchsite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_FOREST_STONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_forest_stone_wall", "变质森林石墙", "Fuchsite Wall");

    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_fungal_bricks_slab", "变质菌丝石砖台阶", "Mycelite Brick Slab");

    /** 原植物魔法「变质菌丝石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_fungal_bricks_stairs", "变质菌丝石砖楼梯", "Mycelite Brick Stairs");

    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_fungal_bricks_wall", "变质菌丝石砖墙", "Mycelite Brick Wall");

    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_COBBLESTONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_fungal_cobblestone_slab", "变质菌丝圆石台阶", "Cobbled Mycelite Slab");

    /** 原植物魔法「变质菌丝圆石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_COBBLESTONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_fungal_cobblestone_stairs", "变质菌丝圆石楼梯", "Cobbled Mycelite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_COBBLESTONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_fungal_cobblestone_wall", "变质菌丝圆石墙", "Cobbled Mycelite Wall");

    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_STONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_fungal_stone_slab", "变质菌丝石头台阶", "Mycelite Slab");

    /** 原植物魔法「变质菌丝石头楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_STONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_fungal_stone_stairs", "变质菌丝石头楼梯", "Mycelite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_FUNGAL_STONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_fungal_stone_wall", "变质菌丝石墙", "Mycelite Wall");

    public static final BlockEntry<Block> METAMORPHIC_MESA_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_mesa_bricks_slab", "变质高原石砖台阶", "Rosy Talc Brick Slab");

    /** 原植物魔法「变质高原石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MESA_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_mesa_bricks_stairs", "变质高原石砖楼梯", "Rosy Talc Brick Stairs");

    public static final BlockEntry<Block> METAMORPHIC_MESA_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_mesa_bricks_wall", "变质沼泽石砖墙", "Rosy Talc Brick Wall");

    public static final BlockEntry<Block> METAMORPHIC_MESA_COBBLESTONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_mesa_cobblestone_slab", "变质高原圆石台阶", "Cobbled Rosy Talc Slab");

    /** 原植物魔法「变质高原圆石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MESA_COBBLESTONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_mesa_cobblestone_stairs", "变质高原圆石楼梯", "Cobbled Rosy Talc Stairs");

    public static final BlockEntry<Block> METAMORPHIC_MESA_COBBLESTONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_mesa_cobblestone_wall", "变质高原圆石墙", "Cobbled Rosy Talc Wall");

    public static final BlockEntry<Block> METAMORPHIC_MESA_STONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_mesa_stone_slab", "变质高原石头台阶", "Rosy Talc Slab");

    /** 原植物魔法「变质高原石头楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MESA_STONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_mesa_stone_stairs", "变质高原石头楼梯", "Rosy Talc Stairs");

    public static final BlockEntry<Block> METAMORPHIC_MESA_STONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_mesa_stone_wall", "变质高原石墙", "Rosy Talc Wall");

    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_mountain_bricks_slab", "变质高山石砖台阶", "Gneiss Brick Slab");

    /** 原植物魔法「变质高山石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_mountain_bricks_stairs", "变质高山石砖楼梯", "Gneiss Brick Stairs");

    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_mountain_bricks_wall", "变质高山石砖墙", "Gneiss Brick Wall");

    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_COBBLESTONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_mountain_cobblestone_slab", "变质高山圆石台阶", "Cobbled Gneiss Slab");

    /** 原植物魔法「变质高山圆石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_COBBLESTONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_mountain_cobblestone_stairs", "变质高山圆石楼梯", "Cobbled Gneiss Stairs");

    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_COBBLESTONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_mountain_cobblestone_wall", "变质高山圆石墙", "Cobbled Gneiss Wall");

    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_STONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_mountain_stone_slab", "变质高山石头台阶", "Gneiss Slab");

    /** 原植物魔法「变质高山石头楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_STONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_mountain_stone_stairs", "变质高山石头楼梯", "Gneiss Stairs");

    public static final BlockEntry<Block> METAMORPHIC_MOUNTAIN_STONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_mountain_stone_wall", "变质高山石墙", "Gneiss Wall");

    public static final BlockEntry<Block> METAMORPHIC_PLAINS_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_plains_bricks_slab", "变质平原石砖台阶", "Talc Brick Slab");

    /** 原植物魔法「变质平原石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_PLAINS_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_plains_bricks_stairs", "变质平原石砖楼梯", "Talc Brick Stairs");

    public static final BlockEntry<Block> METAMORPHIC_PLAINS_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_plains_bricks_wall", "变质平原石砖墙", "Talc Brick Wall");

    public static final BlockEntry<Block> METAMORPHIC_PLAINS_COBBLESTONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_plains_cobblestone_slab", "变质平原圆石台阶", "Cobbled Talc Slab");

    /** 原植物魔法「变质平原圆石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_PLAINS_COBBLESTONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_plains_cobblestone_stairs", "变质平原圆石楼梯", "Cobbled Talc Stairs");

    public static final BlockEntry<Block> METAMORPHIC_PLAINS_COBBLESTONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_plains_cobblestone_wall", "变质平原圆石墙", "Cobbled Talc Wall");

    public static final BlockEntry<Block> METAMORPHIC_PLAINS_STONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_plains_stone_slab", "变质平原石头台阶", "Talc Slab");

    /** 原植物魔法「变质平原石头楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_PLAINS_STONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_plains_stone_stairs", "变质平原石头楼梯", "Talc Stairs");

    public static final BlockEntry<Block> METAMORPHIC_PLAINS_STONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_plains_stone_wall", "变质平原石墙", "Talc Wall");

    public static final BlockEntry<Block> METAMORPHIC_SWAMP_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_swamp_bricks_slab", "变质沼泽石砖台阶", "Cataclasite Brick Slab");

    /** 原植物魔法「变质沼泽石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_SWAMP_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_swamp_bricks_stairs", "变质沼泽石砖楼梯", "Cataclasite Brick Stairs");

    public static final BlockEntry<Block> METAMORPHIC_SWAMP_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_swamp_bricks_wall", "变质沼泽石砖墙", "Cataclasite Brick Wall");

    public static final BlockEntry<Block> METAMORPHIC_SWAMP_COBBLESTONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_swamp_cobblestone_slab", "变质沼泽圆石台阶", "Cobbled Cataclasite Slab");

    /** 原植物魔法「变质沼泽圆石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_SWAMP_COBBLESTONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_swamp_cobblestone_stairs", "变质沼泽圆石楼梯", "Cobbled Cataclasite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_SWAMP_COBBLESTONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_swamp_cobblestone_wall", "变质沼泽圆石墙", "Cobbled Cataclasite Wall");

    public static final BlockEntry<Block> METAMORPHIC_SWAMP_STONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_swamp_stone_slab", "变质沼泽石头台阶", "Cataclasite Slab");

    /** 原植物魔法「变质沼泽石头楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_SWAMP_STONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_swamp_stone_stairs", "变质沼泽石头楼梯", "Cataclasite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_SWAMP_STONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_swamp_stone_wall", "变质沼泽石墙", "Cataclasite Wall");

    public static final BlockEntry<Block> METAMORPHIC_TAIGA_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_taiga_bricks_slab", "变质雪原石砖台阶", "Lunite Brick Slab");

    /** 原植物魔法「变质雪原石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_TAIGA_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_taiga_bricks_stairs", "变质雪原石砖楼梯", "Lunite Brick Stairs");

    public static final BlockEntry<Block> METAMORPHIC_TAIGA_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_taiga_bricks_wall", "变质雪原石砖墙", "Lunite Brick Wall");

    public static final BlockEntry<Block> METAMORPHIC_TAIGA_COBBLESTONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_taiga_cobblestone_slab", "变质雪原圆石台阶", "Cobbled Lunite Slab");

    /** 原植物魔法「变质雪原圆石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_TAIGA_COBBLESTONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_taiga_cobblestone_stairs", "变质雪原圆石楼梯", "Cobbled Lunite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_TAIGA_COBBLESTONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_taiga_cobblestone_wall", "变质雪原圆石墙", "Cobbled Lunite Wall");

    public static final BlockEntry<Block> METAMORPHIC_TAIGA_STONE_SLAB = BotaniaReplica.BOTANIA.slab("metamorphic_taiga_stone_slab", "变质雪原石头台阶", "Lunite Slab");

    /** 原植物魔法「变质雪原石头楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> METAMORPHIC_TAIGA_STONE_STAIRS = BotaniaReplica.BOTANIA.stairs("metamorphic_taiga_stone_stairs", "变质雪原石头楼梯", "Lunite Stairs");

    public static final BlockEntry<Block> METAMORPHIC_TAIGA_STONE_WALL = BotaniaReplica.BOTANIA.wall("metamorphic_taiga_stone_wall", "变质雪原石墙", "Lunite Wall");

    public static final BlockEntry<Block> MOSSY_LIVINGROCK_BRICKS_SLAB = BotaniaReplica.BOTANIA.slab("mossy_livingrock_bricks_slab", "覆苔活石砖台阶", "Mossy Livingrock Brick Slab");

    /** 原植物魔法「覆苔活石砖楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MOSSY_LIVINGROCK_BRICKS_STAIRS = BotaniaReplica.BOTANIA.stairs("mossy_livingrock_bricks_stairs", "覆苔活石砖楼梯", "Mossy Livingrock Brick Stairs");

    public static final BlockEntry<Block> MOSSY_LIVINGROCK_BRICKS_WALL = BotaniaReplica.BOTANIA.wall("mossy_livingrock_bricks_wall", "覆苔活石砖墙", "Mossy Livingrock Brick Wall");

    public static final BlockEntry<Block> POLISHED_LIVINGROCK_SLAB = BotaniaReplica.BOTANIA.slab("polished_livingrock_slab", "磨制活石台阶", "Polished Livingrock Slab");

    /** 原植物魔法「磨制活石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POLISHED_LIVINGROCK_STAIRS = BotaniaReplica.BOTANIA.stairs("polished_livingrock_stairs", "磨制活石楼梯", "Polished Livingrock Stairs");

    public static final BlockEntry<Block> POLISHED_LIVINGROCK_WALL = BotaniaReplica.BOTANIA.wall("polished_livingrock_wall", "磨制活石墙", "Polished Livingrock Wall");

    public static final BlockEntry<Block> RED_PAVEMENT_SLAB = BotaniaReplica.BOTANIA.slab("red_pavement_slab", "红色葡萄牙台阶", "Red Portuguese Pavement Slab");

    /** 原植物魔法「红色葡萄牙楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RED_PAVEMENT_STAIRS = BotaniaReplica.BOTANIA.stairs("red_pavement_stairs", "红色葡萄牙楼梯", "Red Portuguese Pavement Stairs");

    public static final BlockEntry<Block> RED_QUARTZ_SLAB = BotaniaReplica.BOTANIA.slab("red_quartz_slab", "红色石英台阶", "Redquartz Slab");

    /** 原植物魔法「红色石英楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RED_QUARTZ_STAIRS = BotaniaReplica.BOTANIA.stairs("red_quartz_stairs", "红色石英楼梯", "Redquartz Stairs");

    public static final BlockEntry<Block> SHIMMERROCK_SLAB = BotaniaReplica.BOTANIA.slab("shimmerrock_slab", "微光石台阶", "Shimmerrock Slab");

    /** 原植物魔法「微光石楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SHIMMERROCK_STAIRS = BotaniaReplica.BOTANIA.stairs("shimmerrock_stairs", "微光石楼梯", "Shimmerrock Stairs");

    public static final BlockEntry<Block> SUNNY_QUARTZ_SLAB = BotaniaReplica.BOTANIA.slab("sunny_quartz_slab", "金黄石英台阶", "Sunny Quartz Slab");

    /** 原植物魔法「金黄石英楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SUNNY_QUARTZ_STAIRS = BotaniaReplica.BOTANIA.stairs("sunny_quartz_stairs", "金黄石英楼梯", "Sunny Quartz Stairs");

    public static final BlockEntry<Block> WHITE_PAVEMENT_SLAB = BotaniaReplica.BOTANIA.slab("white_pavement_slab", "白色葡萄牙台阶", "White Portuguese Pavement Slab");

    /** 原植物魔法「白色葡萄牙楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> WHITE_PAVEMENT_STAIRS = BotaniaReplica.BOTANIA.stairs("white_pavement_stairs", "白色葡萄牙楼梯", "White Portuguese Pavement Stairs");

    public static final BlockEntry<Block> YELLOW_PAVEMENT_SLAB = BotaniaReplica.BOTANIA.slab("yellow_pavement_slab", "黄色葡萄牙台阶", "Yellow Portuguese Pavement Slab");

    /** 原植物魔法「黄色葡萄牙楼梯」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> YELLOW_PAVEMENT_STAIRS = BotaniaReplica.BOTANIA.stairs("yellow_pavement_stairs", "黄色葡萄牙楼梯", "Yellow Portuguese Pavement Stairs");

    // ==================== 花与蘑菇 ====================

    public static final BlockEntry<Block> BLACK_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("black_double_flower", "黑色高神秘花", "Tall Mystical Black Flower");

    public static final BlockEntry<Block> BLACK_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("black_floating_flower", "黑色浮空花", "Floating Black Flower");

    public static final BlockEntry<Block> BLACK_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("black_mushroom", "黑色微光蘑菇", "Black Shimmering Mushroom");

    public static final BlockEntry<Block> BLACK_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("black_mystical_flower", "黑色神秘花", "Mystical Black Flower");

    public static final BlockEntry<Block> BLACK_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("black_shiny_flower", "黑色荧光花", "Glimmering Black Flower");

    public static final BlockEntry<Block> BLUE_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("blue_double_flower", "蓝色高神秘花", "Tall Mystical Blue Flower");

    public static final BlockEntry<Block> BLUE_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("blue_floating_flower", "蓝色浮空花", "Floating Blue Flower");

    public static final BlockEntry<Block> BLUE_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("blue_mushroom", "蓝色微光蘑菇", "Blue Shimmering Mushroom");

    public static final BlockEntry<Block> BLUE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("blue_mystical_flower", "蓝色神秘花", "Mystical Blue Flower");

    public static final BlockEntry<Block> BLUE_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("blue_shiny_flower", "蓝色荧光花", "Glimmering Blue Flower");

    public static final BlockEntry<Block> BROWN_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("brown_double_flower", "褐色高神秘花", "Tall Mystical Brown Flower");

    public static final BlockEntry<Block> BROWN_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("brown_floating_flower", "棕色浮空花", "Floating Brown Flower");

    public static final BlockEntry<Block> BROWN_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("brown_mushroom", "棕色微光蘑菇", "Brown Shimmering Mushroom");

    public static final BlockEntry<Block> BROWN_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("brown_mystical_flower", "棕色神秘花", "Mystical Brown Flower");

    public static final BlockEntry<Block> BROWN_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("brown_shiny_flower", "棕色荧光花", "Glimmering Brown Flower");

    public static final BlockEntry<Block> CYAN_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("cyan_double_flower", "青色高神秘花", "Tall Mystical Cyan Flower");

    public static final BlockEntry<Block> CYAN_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("cyan_floating_flower", "青色浮空花", "Floating Cyan Flower");

    public static final BlockEntry<Block> CYAN_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("cyan_mushroom", "青色微光蘑菇", "Cyan Shimmering Mushroom");

    public static final BlockEntry<Block> CYAN_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("cyan_mystical_flower", "青色神秘花", "Mystical Cyan Flower");

    public static final BlockEntry<Block> CYAN_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("cyan_shiny_flower", "青色荧光花", "Glimmering Cyan Flower");

    public static final BlockEntry<Block> GRAY_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("gray_double_flower", "灰色高神秘花", "Tall Mystical Gray Flower");

    public static final BlockEntry<Block> GRAY_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("gray_floating_flower", "灰色浮空花", "Floating Gray Flower");

    public static final BlockEntry<Block> GRAY_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("gray_mushroom", "灰色微光蘑菇", "Gray Shimmering Mushroom");

    public static final BlockEntry<Block> GRAY_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("gray_mystical_flower", "灰色神秘花", "Mystical Gray Flower");

    public static final BlockEntry<Block> GRAY_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("gray_shiny_flower", "灰色荧光花", "Glimmering Gray Flower");

    public static final BlockEntry<Block> GREEN_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("green_double_flower", "绿色高神秘花", "Tall Mystical Green Flower");

    public static final BlockEntry<Block> GREEN_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("green_floating_flower", "绿色浮空花", "Floating Green Flower");

    public static final BlockEntry<Block> GREEN_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("green_mushroom", "绿色微光蘑菇", "Green Shimmering Mushroom");

    public static final BlockEntry<Block> GREEN_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("green_mystical_flower", "绿色神秘花", "Mystical Green Flower");

    public static final BlockEntry<Block> GREEN_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("green_shiny_flower", "绿色荧光花", "Glimmering Green Flower");

    public static final BlockEntry<Block> LIGHT_BLUE_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("light_blue_double_flower", "淡蓝色高神秘花", "Tall Mystical Light Blue Flower");

    public static final BlockEntry<Block> LIGHT_BLUE_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("light_blue_floating_flower", "淡蓝色浮空花", "Floating Light Blue Flower");

    public static final BlockEntry<Block> LIGHT_BLUE_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("light_blue_mushroom", "淡蓝色微光蘑菇", "Light Blue Shimmering Mushroom");

    public static final BlockEntry<Block> LIGHT_BLUE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("light_blue_mystical_flower", "淡蓝色神秘花", "Mystical Light Blue Flower");

    public static final BlockEntry<Block> LIGHT_BLUE_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("light_blue_shiny_flower", "淡蓝色荧光花", "Glimmering Light Blue Flower");

    public static final BlockEntry<Block> LIGHT_GRAY_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("light_gray_double_flower", "淡灰色高神秘花", "Tall Mystical Light Gray Flower");

    public static final BlockEntry<Block> LIGHT_GRAY_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("light_gray_floating_flower", "淡灰色浮空花", "Floating Light Gray Flower");

    public static final BlockEntry<Block> LIGHT_GRAY_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("light_gray_mushroom", "淡灰色微光蘑菇", "Light Gray Shimmering Mushroom");

    public static final BlockEntry<Block> LIGHT_GRAY_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("light_gray_mystical_flower", "淡灰色神秘花", "Mystical Light Gray Flower");

    public static final BlockEntry<Block> LIGHT_GRAY_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("light_gray_shiny_flower", "淡灰色荧光花", "Glimmering Light Gray Flower");

    public static final BlockEntry<Block> LIME_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("lime_double_flower", "黄绿色高神秘花", "Tall Mystical Lime Flower");

    public static final BlockEntry<Block> LIME_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("lime_floating_flower", "黄绿色浮空花", "Floating Lime Flower");

    public static final BlockEntry<Block> LIME_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("lime_mushroom", "黄绿色微光蘑菇", "Lime Shimmering Mushroom");

    public static final BlockEntry<Block> LIME_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("lime_mystical_flower", "黄绿色神秘花", "Mystical Lime Flower");

    public static final BlockEntry<Block> LIME_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("lime_shiny_flower", "黄绿色荧光花", "Glimmering Lime Flower");

    public static final BlockEntry<Block> MAGENTA_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("magenta_double_flower", "品红色高神秘花", "Tall Mystical Magenta Flower");

    public static final BlockEntry<Block> MAGENTA_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("magenta_floating_flower", "品红色浮空花", "Floating Magenta Flower");

    public static final BlockEntry<Block> MAGENTA_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("magenta_mushroom", "品红色微光蘑菇", "Magenta Shimmering Mushroom");

    public static final BlockEntry<Block> MAGENTA_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("magenta_mystical_flower", "品红色神秘花", "Mystical Magenta Flower");

    public static final BlockEntry<Block> MAGENTA_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("magenta_shiny_flower", "品红色荧光花", "Glimmering Magenta Flower");

    public static final BlockEntry<Block> ORANGE_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("orange_double_flower", "橙色高神秘花", "Tall Mystical Orange Flower");

    public static final BlockEntry<Block> ORANGE_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("orange_floating_flower", "橙色浮空花", "Floating Orange Flower");

    public static final BlockEntry<Block> ORANGE_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("orange_mushroom", "橙色微光蘑菇", "Orange Shimmering Mushroom");

    public static final BlockEntry<Block> ORANGE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("orange_mystical_flower", "橙色神秘花", "Mystical Orange Flower");

    public static final BlockEntry<Block> ORANGE_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("orange_shiny_flower", "橙色荧光花", "Glimmering Orange Flower");

    public static final BlockEntry<Block> PINK_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("pink_double_flower", "粉色高神秘花", "Tall Mystical Pink Flower");

    public static final BlockEntry<Block> PINK_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("pink_floating_flower", "粉色浮空花", "Floating Pink Flower");

    public static final BlockEntry<Block> PINK_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("pink_mushroom", "粉色微光蘑菇", "Pink Shimmering Mushroom");

    public static final BlockEntry<Block> PINK_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("pink_mystical_flower", "粉色神秘花", "Mystical Pink Flower");

    public static final BlockEntry<Block> PINK_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("pink_shiny_flower", "粉色荧光花", "Glimmering Pink Flower");

    public static final BlockEntry<Block> PURPLE_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("purple_double_flower", "紫色高神秘花", "Tall Mystical Purple Flower");

    public static final BlockEntry<Block> PURPLE_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("purple_floating_flower", "紫色浮空花", "Floating Purple Flower");

    public static final BlockEntry<Block> PURPLE_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("purple_mushroom", "紫色微光蘑菇", "Purple Shimmering Mushroom");

    public static final BlockEntry<Block> PURPLE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("purple_mystical_flower", "紫色神秘花", "Mystical Purple Flower");

    public static final BlockEntry<Block> PURPLE_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("purple_shiny_flower", "紫色荧光花", "Glimmering Purple Flower");

    public static final BlockEntry<Block> RED_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("red_double_flower", "红色高神秘花", "Tall Mystical Red Flower");

    public static final BlockEntry<Block> RED_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("red_floating_flower", "红色浮空花", "Floating Red Flower");

    public static final BlockEntry<Block> RED_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("red_mushroom", "红色微光蘑菇", "Red Shimmering Mushroom");

    public static final BlockEntry<Block> RED_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("red_mystical_flower", "红色神秘花", "Mystical Red Flower");

    public static final BlockEntry<Block> RED_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("red_shiny_flower", "红色荧光花", "Glimmering Red Flower");

    public static final BlockEntry<Block> WHITE_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("white_double_flower", "白色高神秘花", "Tall Mystical White Flower");

    public static final BlockEntry<Block> WHITE_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("white_floating_flower", "白色浮空花", "Floating White Flower");

    public static final BlockEntry<Block> WHITE_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("white_mushroom", "白色微光蘑菇", "White Shimmering Mushroom");

    public static final BlockEntry<Block> WHITE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("white_mystical_flower", "白色神秘花", "Mystical White Flower");

    public static final BlockEntry<Block> WHITE_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("white_shiny_flower", "白色荧光花", "Glimmering White Flower");

    public static final BlockEntry<Block> YELLOW_DOUBLE_FLOWER = BotaniaReplica.BOTANIA.doublePlant("yellow_double_flower", "黄色高神秘花", "Tall Mystical Yellow Flower");

    public static final BlockEntry<Block> YELLOW_FLOATING_FLOWER = BotaniaReplica.BOTANIA.floatingFlower("yellow_floating_flower", "黄色浮空花", "Floating Yellow Flower");

    public static final BlockEntry<Block> YELLOW_MUSHROOM = BotaniaReplica.BOTANIA.mushroom("yellow_mushroom", "黄色微光蘑菇", "Yellow Shimmering Mushroom");

    public static final BlockEntry<Block> YELLOW_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.flower("yellow_mystical_flower", "黄色神秘花", "Mystical Yellow Flower");

    public static final BlockEntry<Block> YELLOW_SHINY_FLOWER = BotaniaReplica.BOTANIA.flower("yellow_shiny_flower", "黄色荧光花", "Glimmering Yellow Flower");

    // ==================== 功能花 ====================

    /** 原植物魔法「田园康乃馨」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AGRICARNATION = BotaniaReplica.BOTANIA.flower("agricarnation", "田园康乃馨", "Agricarnation");

    /** 原植物魔法「小田园康乃馨」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AGRICARNATION_CHIBI = BotaniaReplica.BOTANIA.flower("agricarnation_chibi", "小田园康乃馨", "Agricarnation Petite");

    /** 原植物魔法「娇媚之刺」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BELLETHORN = BotaniaReplica.BOTANIA.flower("bellethorn", "娇媚之刺", "Bellethorne");

    /** 原植物魔法「小娇媚之刺」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BELLETHORN_CHIBI = BotaniaReplica.BOTANIA.flower("bellethorn_chibi", "小娇媚之刺", "Bellethorne Petite");

    /** 原植物魔法「消音柑」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BERGAMUTE = BotaniaReplica.BOTANIA.flower("bergamute", "消音柑", "Bergamute");

    /** 原植物魔法「气泡铃」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BUBBELL = BotaniaReplica.BOTANIA.flower("bubbell", "气泡铃", "Bubbell");

    /** 原植物魔法「小气泡铃」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BUBBELL_CHIBI = BotaniaReplica.BOTANIA.flower("bubbell_chibi", "小气泡铃", "Bubbell Petite");

    /** 原植物魔法「粘土花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CLAYCONIA = BotaniaReplica.BOTANIA.flower("clayconia", "粘土花", "Clayconia");

    /** 原植物魔法「小粘土花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CLAYCONIA_CHIBI = BotaniaReplica.BOTANIA.flower("clayconia_chibi", "小粘土花", "Clayconia Petite");

    /** 原植物魔法「扇水仙」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DAFFOMILL = BotaniaReplica.BOTANIA.flower("daffomill", "扇水仙", "Daffomill");

    /** 原植物魔法「启命英」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DANDELIFEON = BotaniaReplica.BOTANIA.flower("dandelifeon", "启命英", "Dandelifeon");

    /** 原植物魔法「恐惧之刺」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DREADTHORN = BotaniaReplica.BOTANIA.flower("dreadthorn", "恐惧之刺", "Dreadthorne");

    /** 原植物魔法「火红莲」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ENDOFLAME = BotaniaReplica.BOTANIA.flower("endoflame", "火红莲", "Endoflame");

    /** 原植物魔法「热爆花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ENTROPINNYUM = BotaniaReplica.BOTANIA.flower("entropinnyum", "热爆花", "Entropinnyum");

    /** 原植物魔法「冶炼火」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> EXOFLAME = BotaniaReplica.BOTANIA.flower("exoflame", "冶炼火", "Exoflame");

    /** 原植物魔法「天降之奏」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FALLEN_KANADE = BotaniaReplica.BOTANIA.flower("fallen_kanade", "天降之奏", "Fallen Kanade");

    /** 原植物魔法「浮空田园康乃馨」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_AGRICARNATION = BotaniaReplica.BOTANIA.floatingFlower("floating_agricarnation", "浮空田园康乃馨", "Floating Agricarnation");

    /** 原植物魔法「浮空小田园康乃馨」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_AGRICARNATION_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_agricarnation_chibi", "浮空小田园康乃馨", "Floating Agricarnation Petite");

    /** 原植物魔法「浮空娇媚之刺」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_BELLETHORN = BotaniaReplica.BOTANIA.floatingFlower("floating_bellethorn", "浮空娇媚之刺", "Floating Bellethorne");

    /** 原植物魔法「浮空小娇媚之刺」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_BELLETHORN_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_bellethorn_chibi", "浮空小娇媚之刺", "Floating Bellethorne Petite");

    /** 原植物魔法「浮空消音柑」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_BERGAMUTE = BotaniaReplica.BOTANIA.floatingFlower("floating_bergamute", "浮空消音柑", "Floating Bergamute");

    /** 原植物魔法「浮空气泡铃」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_BUBBELL = BotaniaReplica.BOTANIA.floatingFlower("floating_bubbell", "浮空气泡铃", "Floating Bubbell");

    /** 原植物魔法「浮空小气泡铃」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_BUBBELL_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_bubbell_chibi", "浮空小气泡铃", "Floating Bubbell Petite");

    /** 原植物魔法「浮空粘土花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_CLAYCONIA = BotaniaReplica.BOTANIA.floatingFlower("floating_clayconia", "浮空粘土花", "Floating Clayconia");

    /** 原植物魔法「浮空小粘土花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_CLAYCONIA_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_clayconia_chibi", "浮空小粘土花", "Floating Clayconia Petite");

    /** 原植物魔法「浮空扇水仙」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_DAFFOMILL = BotaniaReplica.BOTANIA.floatingFlower("floating_daffomill", "浮空扇水仙", "Floating Daffomill");

    /** 原植物魔法「浮空启命英」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_DANDELIFEON = BotaniaReplica.BOTANIA.floatingFlower("floating_dandelifeon", "浮空启命英", "Floating Dandelifeon");

    /** 原植物魔法「浮空恐惧之刺」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_DREADTHORN = BotaniaReplica.BOTANIA.floatingFlower("floating_dreadthorn", "浮空恐惧之刺", "Floating Dreadthorne");

    /** 原植物魔法「浮空火红莲」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_ENDOFLAME = BotaniaReplica.BOTANIA.floatingFlower("floating_endoflame", "浮空火红莲", "Floating Endoflame");

    /** 原植物魔法「浮空热爆花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_ENTROPINNYUM = BotaniaReplica.BOTANIA.floatingFlower("floating_entropinnyum", "浮空热爆花", "Floating Entropinnyum");

    /** 原植物魔法「浮空冶炼火」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_EXOFLAME = BotaniaReplica.BOTANIA.floatingFlower("floating_exoflame", "浮空冶炼火", "Floating Exoflame");

    /** 原植物魔法「浮空天降之奏」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_FALLEN_KANADE = BotaniaReplica.BOTANIA.floatingFlower("floating_fallen_kanade", "浮空天降之奏", "Floating Fallen Kanade");

    /** 原植物魔法「浮空彼方兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_GOURMARYLLIS = BotaniaReplica.BOTANIA.floatingFlower("floating_gourmaryllis", "浮空彼方兰", "Floating Gourmaryllis");

    /** 原植物魔法「浮空平成之梦」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_HEISEI_DREAM = BotaniaReplica.BOTANIA.floatingFlower("floating_heisei_dream", "浮空平成之梦", "Floating Heisei Dream");

    /** 原植物魔法「浮空漏斗花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_HOPPERHOCK = BotaniaReplica.BOTANIA.floatingFlower("floating_hopperhock", "浮空漏斗花", "Floating Hopperhock");

    /** 原植物魔法「浮空小漏斗花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_HOPPERHOCK_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_hopperhock_chibi", "浮空小漏斗花", "Floating Hopperhock Petite");

    /** 原植物魔法「浮空毒风信」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_HYACIDUS = BotaniaReplica.BOTANIA.floatingFlower("floating_hyacidus", "浮空毒风信", "Floating Hyacidus");

    /** 原植物魔法「浮空水绣球」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_HYDROANGEAS = BotaniaReplica.BOTANIA.floatingFlower("floating_hydroangeas", "浮空水绣球", "Floating Hydroangeas");

    /** 原植物魔法「浮空翡翠苋」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_JADED_AMARANTHUS = BotaniaReplica.BOTANIA.floatingFlower("floating_jaded_amaranthus", "浮空翡翠苋", "Floating Jaded Amaranthus");

    /** 原植物魔法「浮空自由之翼」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_JIYUULIA = BotaniaReplica.BOTANIA.floatingFlower("floating_jiyuulia", "浮空自由之翼", "Floating Jiyuulia");

    /** 原植物魔法「浮空小自由之翼」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_JIYUULIA_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_jiyuulia_chibi", "浮空小自由之翼", "Floating Jiyuulia Petite");

    /** 原植物魔法「浮空贪食花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_KEKIMURUS = BotaniaReplica.BOTANIA.floatingFlower("floating_kekimurus", "浮空贪食花", "Floating Kekimurus");

    /** 原植物魔法「浮空铭刻莲」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_LABELLIA = BotaniaReplica.BOTANIA.floatingFlower("floating_labellia", "浮空铭刻莲", "Floating Labellia");

    /** 原植物魔法「浮空聚宝花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_LOONIUM = BotaniaReplica.BOTANIA.floatingFlower("floating_loonium", "浮空聚宝花", "Floating Loonium");

    /** 原植物魔法「浮空魔力星」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_MANASTAR = BotaniaReplica.BOTANIA.floatingFlower("floating_manastar", "浮空魔力星", "Floating Manastar");

    /** 原植物魔法「浮空异构花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_MARIMORPHOSIS = BotaniaReplica.BOTANIA.floatingFlower("floating_marimorphosis", "浮空异构花", "Floating Marimorphosis");

    /** 原植物魔法「浮空小异构花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_MARIMORPHOSIS_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_marimorphosis_chibi", "浮空小异构花", "Floating Marimorphosis Petite");

    /** 原植物魔法「浮空石化草」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_MEDUMONE = BotaniaReplica.BOTANIA.floatingFlower("floating_medumone", "浮空石化草", "Floating Medumone");

    /** 原植物魔法「浮空咀叶花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_MUNCHDEW = BotaniaReplica.BOTANIA.floatingFlower("floating_munchdew", "浮空咀叶花", "Floating Munchdew");

    /** 原植物魔法「浮空粘球草」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_NARSLIMMUS = BotaniaReplica.BOTANIA.floatingFlower("floating_narslimmus", "浮空粘球草", "Floating Narslimmus");

    /** 原植物魔法「浮空凝矿兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_ORECHID = BotaniaReplica.BOTANIA.floatingFlower("floating_orechid", "浮空凝矿兰", "Floating Orechid");

    /** 原植物魔法「浮空畜牧花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_POLLIDISIAC = BotaniaReplica.BOTANIA.floatingFlower("floating_pollidisiac", "浮空畜牧花", "Floating Pollidisiac");

    /** 原植物魔法「浮空白雏菊」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_PURE_DAISY = BotaniaReplica.BOTANIA.floatingFlower("floating_pure_daisy", "浮空白雏菊", "Floating Pure Daisy");

    /** 原植物魔法「浮空噬草花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_RAFFLOWSIA = BotaniaReplica.BOTANIA.floatingFlower("floating_rafflowsia", "浮空噬草花", "Floating Rafflowsia");

    /** 原植物魔法「浮空手掌花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_RANNUNCARPUS = BotaniaReplica.BOTANIA.floatingFlower("floating_rannuncarpus", "浮空手掌花", "Floating Rannuncarpus");

    /** 原植物魔法「浮空小手掌花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_RANNUNCARPUS_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_rannuncarpus_chibi", "浮空小手掌花", "Floating Rannuncarpus Petite");

    /** 原植物魔法「浮空阿卡纳蔷薇」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_ROSA_ARCANA = BotaniaReplica.BOTANIA.floatingFlower("floating_rosa_arcana", "浮空阿卡纳蔷薇", "Floating Rosa Arcana");

    /** 原植物魔法「浮空勿落草」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_SHULK_ME_NOT = BotaniaReplica.BOTANIA.floatingFlower("floating_shulk_me_not", "浮空勿落草", "Floating Shulk Me Not");

    /** 原植物魔法「浮空弱磁兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_SOLEGNOLIA = BotaniaReplica.BOTANIA.floatingFlower("floating_solegnolia", "浮空弱磁兰", "Floating Solegnolia");

    /** 原植物魔法「浮空小弱磁兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_SOLEGNOLIA_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_solegnolia_chibi", "浮空小弱磁兰", "Floating Solegnolia Petite");

    /** 原植物魔法「浮空凌空蓟」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_SPECTRANTHEMUM = BotaniaReplica.BOTANIA.floatingFlower("floating_spectranthemum", "浮空凌空蓟", "Floating Spectranthemum");

    /** 原植物魔法「浮空斑斓花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_SPECTROLUS = BotaniaReplica.BOTANIA.floatingFlower("floating_spectrolus", "浮空斑斓花", "Floating Spectrolus");

    /** 原植物魔法「浮空束缚莓」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_TANGLEBERRIE = BotaniaReplica.BOTANIA.floatingFlower("floating_tangleberrie", "浮空束缚莓", "Floating Tangleberrie");

    /** 原植物魔法「浮空小束缚莓」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_TANGLEBERRIE_CHIBI = BotaniaReplica.BOTANIA.floatingFlower("floating_tangleberrie_chibi", "浮空小束缚莓", "Floating Tangleberrie Petite");

    /** 原植物魔法「浮空炽玫瑰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_THERMALILY = BotaniaReplica.BOTANIA.floatingFlower("floating_thermalily", "浮空炽玫瑰", "Floating Thermalily");

    /** 原植物魔法「浮空虎之眼」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_TIGERSEYE = BotaniaReplica.BOTANIA.floatingFlower("floating_tigerseye", "浮空虎之眼", "Floating Tigerseye");

    /** 原植物魔法「浮空诱引莲」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_VINCULOTUS = BotaniaReplica.BOTANIA.floatingFlower("floating_vinculotus", "浮空诱引莲", "Floating Vinculotus");

    /** 原植物魔法「彼方兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GOURMARYLLIS = BotaniaReplica.BOTANIA.flower("gourmaryllis", "彼方兰", "Gourmaryllis");

    /** 原植物魔法「平成之梦」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> HEISEI_DREAM = BotaniaReplica.BOTANIA.flower("heisei_dream", "平成之梦", "Heisei Dream");

    /** 原植物魔法「漏斗花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> HOPPERHOCK = BotaniaReplica.BOTANIA.flower("hopperhock", "漏斗花", "Hopperhock");

    /** 原植物魔法「小漏斗花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> HOPPERHOCK_CHIBI = BotaniaReplica.BOTANIA.flower("hopperhock_chibi", "小漏斗花", "Hopperhock Petite");

    /** 原植物魔法「毒风信」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> HYACIDUS = BotaniaReplica.BOTANIA.flower("hyacidus", "毒风信", "Hyacidus");

    /** 原植物魔法「水绣球」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> HYDROANGEAS = BotaniaReplica.BOTANIA.flower("hydroangeas", "水绣球", "Hydroangeas");

    /** 原植物魔法「翡翠苋」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> JADED_AMARANTHUS = BotaniaReplica.BOTANIA.flower("jaded_amaranthus", "翡翠苋", "Jaded Amaranthus");

    /** 原植物魔法「自由之翼」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> JIYUULIA = BotaniaReplica.BOTANIA.flower("jiyuulia", "自由之翼", "Jiyuulia");

    /** 原植物魔法「小自由之翼」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> JIYUULIA_CHIBI = BotaniaReplica.BOTANIA.flower("jiyuulia_chibi", "小自由之翼", "Jiyuulia Petite");

    /** 原植物魔法「贪食花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> KEKIMURUS = BotaniaReplica.BOTANIA.flower("kekimurus", "贪食花", "Kekimurus");

    /** 原植物魔法「铭刻莲」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LABELLIA = BotaniaReplica.BOTANIA.flower("labellia", "铭刻莲", "Labellia");

    /** 原植物魔法「聚宝花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LOONIUM = BotaniaReplica.BOTANIA.flower("loonium", "聚宝花", "Loonium");

    /** 原植物魔法「魔力星」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANASTAR = BotaniaReplica.BOTANIA.flower("manastar", "魔力星", "Manastar");

    /** 原植物魔法「异构花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MARIMORPHOSIS = BotaniaReplica.BOTANIA.flower("marimorphosis", "异构花", "Marimorphosis");

    /** 原植物魔法「小异构花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MARIMORPHOSIS_CHIBI = BotaniaReplica.BOTANIA.flower("marimorphosis_chibi", "小异构花", "Marimorphosis Petite");

    /** 原植物魔法「石化草」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MEDUMONE = BotaniaReplica.BOTANIA.flower("medumone", "石化草", "Medumone");

    /** 原植物魔法「咀叶花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MUNCHDEW = BotaniaReplica.BOTANIA.flower("munchdew", "咀叶花", "Munchdew");

    /** 原植物魔法「粘球草」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> NARSLIMMUS = BotaniaReplica.BOTANIA.flower("narslimmus", "粘球草", "Narslimmus");

    /** 原植物魔法「凝矿兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ORECHID = BotaniaReplica.BOTANIA.flower("orechid", "凝矿兰", "Orechid");

    /** 原植物魔法「畜牧花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POLLIDISIAC = BotaniaReplica.BOTANIA.flower("pollidisiac", "畜牧花", "Pollidisiac");

    /** 原植物魔法「白雏菊」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> PURE_DAISY = BotaniaReplica.BOTANIA.flower("pure_daisy", "白雏菊", "Pure Daisy");

    /** 原植物魔法「噬草花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RAFFLOWSIA = BotaniaReplica.BOTANIA.flower("rafflowsia", "噬草花", "Rafflowsia");

    /** 原植物魔法「手掌花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RANNUNCARPUS = BotaniaReplica.BOTANIA.flower("rannuncarpus", "手掌花", "Rannuncarpus");

    /** 原植物魔法「小手掌花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RANNUNCARPUS_CHIBI = BotaniaReplica.BOTANIA.flower("rannuncarpus_chibi", "小手掌花", "Rannuncarpus Petite");

    /** 原植物魔法「阿卡纳蔷薇」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ROSA_ARCANA = BotaniaReplica.BOTANIA.flower("rosa_arcana", "阿卡纳蔷薇", "Rosa Arcana");

    /** 原植物魔法「勿落草」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SHULK_ME_NOT = BotaniaReplica.BOTANIA.flower("shulk_me_not", "勿落草", "Shulk Me Not");

    /** 原植物魔法「弱磁兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SOLEGNOLIA = BotaniaReplica.BOTANIA.flower("solegnolia", "弱磁兰", "Solegnolia");

    /** 原植物魔法「小弱磁兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SOLEGNOLIA_CHIBI = BotaniaReplica.BOTANIA.flower("solegnolia_chibi", "小弱磁兰", "Solegnolia Petite");

    /** 原植物魔法「凌空蓟」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SPECTRANTHEMUM = BotaniaReplica.BOTANIA.flower("spectranthemum", "凌空蓟", "Spectranthemum");

    /** 原植物魔法「斑斓花」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SPECTROLUS = BotaniaReplica.BOTANIA.flower("spectrolus", "斑斓花", "Spectrolus");

    /** 原植物魔法「束缚莓」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TANGLEBERRIE = BotaniaReplica.BOTANIA.flower("tangleberrie", "束缚莓", "Tangleberrie");

    /** 原植物魔法「小束缚莓」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TANGLEBERRIE_CHIBI = BotaniaReplica.BOTANIA.flower("tangleberrie_chibi", "小束缚莓", "Tangleberrie Petite");

    /** 原植物魔法「炽玫瑰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> THERMALILY = BotaniaReplica.BOTANIA.flower("thermalily", "炽玫瑰", "Thermalily");

    /** 原植物魔法「虎之眼」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TIGERSEYE = BotaniaReplica.BOTANIA.flower("tigerseye", "虎之眼", "Tigerseye");

    /** 原植物魔法「诱引莲」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> VINCULOTUS = BotaniaReplica.BOTANIA.flower("vinculotus", "诱引莲", "Vinculotus");

    /** 原植物魔法「浮空炎矿兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_ORECHID_IGNEM = BotaniaReplica.BOTANIA.floatingFlower("floating_orechid_ignem", "浮空炎矿兰", "Floating Orechid Ignem");

    /** 原植物魔法「炎矿兰」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ORECHID_IGNEM = BotaniaReplica.BOTANIA.flower("orechid_ignem", "炎矿兰", "Orechid Ignem");

    // ==================== 花瓣方块 ====================

    public static final BlockEntry<Block> BLACK_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("black_buried_petals", "埋藏黑色花瓣", "Buried Black Petal");

    public static final BlockEntry<Block> BLACK_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("black_petal_block", "黑色花瓣块", "Black Petal Block");

    public static final BlockEntry<Block> BLUE_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("blue_buried_petals", "埋藏蓝色花瓣", "Buried Blue Petal");

    public static final BlockEntry<Block> BLUE_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("blue_petal_block", "蓝色花瓣块", "Blue Petal Block");

    public static final BlockEntry<Block> BROWN_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("brown_buried_petals", "埋藏棕色花瓣", "Buried Brown Petal");

    public static final BlockEntry<Block> BROWN_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("brown_petal_block", "棕色花瓣块", "Brown Petal Block");

    public static final BlockEntry<Block> CYAN_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("cyan_buried_petals", "埋藏青色花瓣", "Buried Cyan Petal");

    public static final BlockEntry<Block> CYAN_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("cyan_petal_block", "青色花瓣块", "Cyan Petal Block");

    public static final BlockEntry<Block> GRAY_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("gray_buried_petals", "埋藏灰色花瓣", "Buried Gray Petal");

    public static final BlockEntry<Block> GRAY_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("gray_petal_block", "灰色花瓣块", "Gray Petal Block");

    public static final BlockEntry<Block> GREEN_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("green_buried_petals", "埋藏绿色花瓣", "Buried Green Petal");

    public static final BlockEntry<Block> GREEN_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("green_petal_block", "绿色花瓣块", "Green Petal Block");

    public static final BlockEntry<Block> LIGHT_BLUE_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("light_blue_buried_petals", "埋藏淡蓝色花瓣", "Buried Light Blue Petal");

    public static final BlockEntry<Block> LIGHT_BLUE_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("light_blue_petal_block", "淡蓝色花瓣块", "Light Blue Petal Block");

    public static final BlockEntry<Block> LIGHT_GRAY_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("light_gray_buried_petals", "埋藏淡灰色花瓣", "Buried Light Gray Petal");

    public static final BlockEntry<Block> LIGHT_GRAY_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("light_gray_petal_block", "淡灰色花瓣块", "Light Gray Petal Block");

    public static final BlockEntry<Block> LIME_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("lime_buried_petals", "埋藏黄绿色花瓣", "Buried Lime Petal");

    public static final BlockEntry<Block> LIME_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("lime_petal_block", "黄绿色花瓣块", "Lime Petal Block");

    public static final BlockEntry<Block> MAGENTA_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("magenta_buried_petals", "埋藏品红色花瓣", "Buried Magenta Petal");

    public static final BlockEntry<Block> MAGENTA_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("magenta_petal_block", "品红色花瓣块", "Magenta Petal Block");

    public static final BlockEntry<Block> ORANGE_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("orange_buried_petals", "埋藏橙色花瓣", "Buried Orange Petal");

    public static final BlockEntry<Block> ORANGE_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("orange_petal_block", "橙色花瓣块", "Orange Petal Block");

    public static final BlockEntry<Block> PINK_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("pink_buried_petals", "埋藏粉色花瓣", "Buried Pink Petal");

    public static final BlockEntry<Block> PINK_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("pink_petal_block", "粉色花瓣块", "Pink Petal Block");

    public static final BlockEntry<Block> PURPLE_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("purple_buried_petals", "埋藏紫色花瓣", "Buried Purple Petal");

    public static final BlockEntry<Block> PURPLE_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("purple_petal_block", "紫色花瓣块", "Purple Petal Block");

    public static final BlockEntry<Block> RED_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("red_buried_petals", "埋藏红色花瓣", "Buried Red Petal");

    public static final BlockEntry<Block> RED_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("red_petal_block", "红色花瓣块", "Red Petal Block");

    public static final BlockEntry<Block> WHITE_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("white_buried_petals", "埋藏白色花瓣", "Buried White Petal");

    public static final BlockEntry<Block> WHITE_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("white_petal_block", "白色花瓣块", "White Petal Block");

    public static final BlockEntry<Block> YELLOW_BURIED_PETALS = BotaniaReplica.BOTANIA.buriedPetal("yellow_buried_petals", "埋藏黄色花瓣", "Buried Yellow Petal");

    public static final BlockEntry<Block> YELLOW_PETAL_BLOCK = BotaniaReplica.BOTANIA.block("yellow_petal_block", "黄色花瓣块", "Yellow Petal Block");

    // ==================== 盆栽 ====================

    public static final BlockEntry<Block> POTTED_BLACK_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_black_mushroom", "黑色微光蘑菇盆栽", "Potted Black Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_BLACK_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_black_mystical_flower", "黑色神秘花盆栽", "Potted Mystical Black Flower");

    public static final BlockEntry<Block> POTTED_BLACK_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_black_shiny_flower", "黑色荧光花盆栽", "Potted Glimmering Black Flower");

    public static final BlockEntry<Block> POTTED_BLUE_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_blue_mushroom", "蓝色微光蘑菇盆栽", "Potted Blue Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_BLUE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_blue_mystical_flower", "蓝色神秘花盆栽", "Potted Mystical Blue Flower");

    public static final BlockEntry<Block> POTTED_BLUE_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_blue_shiny_flower", "蓝色荧光花盆栽", "Potted Glimmering Blue Flower");

    public static final BlockEntry<Block> POTTED_BROWN_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_brown_mushroom", "棕色微光蘑菇盆栽", "Potted Brown Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_BROWN_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_brown_mystical_flower", "棕色神秘花盆栽", "Potted Mystical Brown Flower");

    public static final BlockEntry<Block> POTTED_BROWN_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_brown_shiny_flower", "棕色荧光花盆栽", "Potted Glimmering Brown Flower");

    public static final BlockEntry<Block> POTTED_CYAN_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_cyan_mushroom", "青色微光蘑菇盆栽", "Potted Cyan Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_CYAN_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_cyan_mystical_flower", "青色神秘花盆栽", "Potted Mystical Cyan Flower");

    public static final BlockEntry<Block> POTTED_CYAN_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_cyan_shiny_flower", "青色荧光花盆栽", "Potted Glimmering Cyan Flower");

    public static final BlockEntry<Block> POTTED_DAYBLOOM_MOTIF = BotaniaReplica.BOTANIA.potted("potted_daybloom_motif", "太阳花装饰盆栽", "Potted Daybloom Motif");

    public static final BlockEntry<Block> POTTED_GRAY_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_gray_mushroom", "灰色微光蘑菇盆栽", "Potted Gray Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_GRAY_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_gray_mystical_flower", "灰色神秘花盆栽", "Potted Mystical Gray Flower");

    public static final BlockEntry<Block> POTTED_GRAY_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_gray_shiny_flower", "灰色荧光花盆栽", "Potted Glimmering Gray Flower");

    public static final BlockEntry<Block> POTTED_GREEN_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_green_mushroom", "绿色微光蘑菇盆栽", "Potted Green Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_GREEN_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_green_mystical_flower", "绿色神秘花盆栽", "Potted Mystical Green Flower");

    public static final BlockEntry<Block> POTTED_GREEN_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_green_shiny_flower", "绿色荧光花盆栽", "Potted Glimmering Green Flower");

    public static final BlockEntry<Block> POTTED_HYDROANGEAS_MOTIF = BotaniaReplica.BOTANIA.potted("potted_hydroangeas_motif", "水绣球装饰盆栽", "Potted Hydroangeas Motif");

    public static final BlockEntry<Block> POTTED_LIGHT_BLUE_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_light_blue_mushroom", "淡蓝色微光蘑菇盆栽", "Potted Light Blue Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_LIGHT_BLUE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_light_blue_mystical_flower", "淡蓝色神秘花盆栽", "Potted Mystical Light Blue Flower");

    public static final BlockEntry<Block> POTTED_LIGHT_BLUE_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_light_blue_shiny_flower", "淡蓝色荧光花盆栽", "Potted Glimmering Light Blue Flower");

    public static final BlockEntry<Block> POTTED_LIGHT_GRAY_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_light_gray_mushroom", "淡灰色微光蘑菇盆栽", "Potted Light Gray Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_LIGHT_GRAY_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_light_gray_mystical_flower", "淡灰色神秘花盆栽", "Potted Mystical Light Gray Flower");

    public static final BlockEntry<Block> POTTED_LIGHT_GRAY_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_light_gray_shiny_flower", "淡灰色荧光花盆栽", "Potted Glimmering Light Gray Flower");

    public static final BlockEntry<Block> POTTED_LIME_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_lime_mushroom", "黄绿色微光蘑菇盆栽", "Potted Lime Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_LIME_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_lime_mystical_flower", "黄绿色神秘花盆栽", "Potted Mystical Lime Flower");

    public static final BlockEntry<Block> POTTED_LIME_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_lime_shiny_flower", "黄绿色荧光花盆栽", "Potted Glimmering Lime Flower");

    public static final BlockEntry<Block> POTTED_MAGENTA_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_magenta_mushroom", "品红色微光蘑菇盆栽", "Potted Magenta Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_MAGENTA_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_magenta_mystical_flower", "品红色神秘花盆栽", "Potted Mystical Magenta Flower");

    public static final BlockEntry<Block> POTTED_MAGENTA_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_magenta_shiny_flower", "品红色荧光花盆栽", "Potted Glimmering Magenta Flower");

    public static final BlockEntry<Block> POTTED_NIGHTSHADE_MOTIF = BotaniaReplica.BOTANIA.potted("potted_nightshade_motif", "夜颠茄装饰盆栽", "Potted Nightshade Motif");

    public static final BlockEntry<Block> POTTED_ORANGE_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_orange_mushroom", "橙色微光蘑菇盆栽", "Potted Orange Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_ORANGE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_orange_mystical_flower", "橙色神秘花盆栽", "Potted Mystical Orange Flower");

    public static final BlockEntry<Block> POTTED_ORANGE_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_orange_shiny_flower", "橙色荧光花盆栽", "Potted Glimmering Orange Flower");

    public static final BlockEntry<Block> POTTED_PINK_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_pink_mushroom", "粉色微光蘑菇盆栽", "Potted Pink Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_PINK_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_pink_mystical_flower", "粉色神秘花盆栽", "Potted Mystical Pink Flower");

    public static final BlockEntry<Block> POTTED_PINK_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_pink_shiny_flower", "粉色荧光花盆栽", "Potted Glimmering Pink Flower");

    public static final BlockEntry<Block> POTTED_PURPLE_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_purple_mushroom", "紫色微光蘑菇盆栽", "Potted Purple Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_PURPLE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_purple_mystical_flower", "紫色神秘花盆栽", "Potted Mystical Purple Flower");

    public static final BlockEntry<Block> POTTED_PURPLE_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_purple_shiny_flower", "紫色荧光花盆栽", "Potted Glimmering Purple Flower");

    public static final BlockEntry<Block> POTTED_RED_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_red_mushroom", "红色微光蘑菇盆栽", "Potted Red Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_RED_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_red_mystical_flower", "红色神秘花盆栽", "Potted Mystical Red Flower");

    public static final BlockEntry<Block> POTTED_RED_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_red_shiny_flower", "红色荧光花盆栽", "Potted Glimmering Red Flower");

    public static final BlockEntry<Block> POTTED_WHITE_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_white_mushroom", "白色微光蘑菇盆栽", "Potted White Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_WHITE_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_white_mystical_flower", "白色神秘花盆栽", "Potted Mystical White Flower");

    public static final BlockEntry<Block> POTTED_WHITE_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_white_shiny_flower", "白色荧光花盆栽", "Potted Glimmering White Flower");

    public static final BlockEntry<Block> POTTED_YELLOW_MUSHROOM = BotaniaReplica.BOTANIA.potted("potted_yellow_mushroom", "黄色微光蘑菇盆栽", "Potted Yellow Shimmering Mushroom");

    public static final BlockEntry<Block> POTTED_YELLOW_MYSTICAL_FLOWER = BotaniaReplica.BOTANIA.potted("potted_yellow_mystical_flower", "黄色神秘花盆栽", "Potted Mystical Yellow Flower");

    public static final BlockEntry<Block> POTTED_YELLOW_SHINY_FLOWER = BotaniaReplica.BOTANIA.potted("potted_yellow_shiny_flower", "黄色荧光花盆栽", "Potted Glimmering Yellow Flower");

    public static final BlockEntry<Block> POTTED_AGRICARNATION = BotaniaReplica.BOTANIA.potted("potted_agricarnation", "田园康乃馨盆栽", "Potted Agricarnation");

    public static final BlockEntry<Block> POTTED_AGRICARNATION_CHIBI = BotaniaReplica.BOTANIA.potted("potted_agricarnation_chibi", "小田园康乃馨盆栽", "Potted Agricarnation Petite");

    public static final BlockEntry<Block> POTTED_BELLETHORN = BotaniaReplica.BOTANIA.potted("potted_bellethorn", "娇媚之刺盆栽", "Potted Bellethorne");

    public static final BlockEntry<Block> POTTED_BELLETHORN_CHIBI = BotaniaReplica.BOTANIA.potted("potted_bellethorn_chibi", "小娇媚之刺盆栽", "Potted Bellethorne Petite");

    public static final BlockEntry<Block> POTTED_BERGAMUTE = BotaniaReplica.BOTANIA.potted("potted_bergamute", "消音柑盆栽", "Potted Bergamute");

    public static final BlockEntry<Block> POTTED_BUBBELL = BotaniaReplica.BOTANIA.potted("potted_bubbell", "气泡铃盆栽", "Potted Bubbell");

    public static final BlockEntry<Block> POTTED_BUBBELL_CHIBI = BotaniaReplica.BOTANIA.potted("potted_bubbell_chibi", "小气泡铃盆栽", "Potted Bubbell Petite");

    public static final BlockEntry<Block> POTTED_CLAYCONIA = BotaniaReplica.BOTANIA.potted("potted_clayconia", "粘土花盆栽", "Potted Clayconia");

    public static final BlockEntry<Block> POTTED_CLAYCONIA_CHIBI = BotaniaReplica.BOTANIA.potted("potted_clayconia_chibi", "小粘土花盆栽", "Potted Clayconia Petite");

    public static final BlockEntry<Block> POTTED_DAFFOMILL = BotaniaReplica.BOTANIA.potted("potted_daffomill", "扇水仙盆栽", "Potted Daffomill");

    public static final BlockEntry<Block> POTTED_DANDELIFEON = BotaniaReplica.BOTANIA.potted("potted_dandelifeon", "启命英盆栽", "Potted Dandelifeon");

    public static final BlockEntry<Block> POTTED_DREADTHORN = BotaniaReplica.BOTANIA.potted("potted_dreadthorn", "恐惧之刺盆栽", "Potted Dreadthorne");

    public static final BlockEntry<Block> POTTED_ENDOFLAME = BotaniaReplica.BOTANIA.potted("potted_endoflame", "火红莲盆栽", "Potted Endoflame");

    public static final BlockEntry<Block> POTTED_ENTROPINNYUM = BotaniaReplica.BOTANIA.potted("potted_entropinnyum", "热爆花盆栽", "Potted Entropinnyum");

    public static final BlockEntry<Block> POTTED_EXOFLAME = BotaniaReplica.BOTANIA.potted("potted_exoflame", "冶炼火盆栽", "Potted Exoflame");

    public static final BlockEntry<Block> POTTED_FALLEN_KANADE = BotaniaReplica.BOTANIA.potted("potted_fallen_kanade", "天降之奏盆栽", "Potted Fallen Kanade");

    public static final BlockEntry<Block> POTTED_GOURMARYLLIS = BotaniaReplica.BOTANIA.potted("potted_gourmaryllis", "彼方兰盆栽", "Potted Gourmaryllis");

    public static final BlockEntry<Block> POTTED_HEISEI_DREAM = BotaniaReplica.BOTANIA.potted("potted_heisei_dream", "平成之梦盆栽", "Potted Heisei Dream");

    public static final BlockEntry<Block> POTTED_HOPPERHOCK = BotaniaReplica.BOTANIA.potted("potted_hopperhock", "漏斗花盆栽", "Potted Hopperhock");

    public static final BlockEntry<Block> POTTED_HOPPERHOCK_CHIBI = BotaniaReplica.BOTANIA.potted("potted_hopperhock_chibi", "小漏斗花盆栽", "Potted Hopperhock Petite");

    public static final BlockEntry<Block> POTTED_HYACIDUS = BotaniaReplica.BOTANIA.potted("potted_hyacidus", "毒风信盆栽", "Potted Hyacidus");

    public static final BlockEntry<Block> POTTED_HYDROANGEAS = BotaniaReplica.BOTANIA.potted("potted_hydroangeas", "水绣球盆栽", "Potted Hydroangeas");

    public static final BlockEntry<Block> POTTED_JADED_AMARANTHUS = BotaniaReplica.BOTANIA.potted("potted_jaded_amaranthus", "翡翠苋盆栽", "Potted Jaded Amaranthus");

    public static final BlockEntry<Block> POTTED_JIYUULIA = BotaniaReplica.BOTANIA.potted("potted_jiyuulia", "自由之翼盆栽", "Potted Jiyuulia");

    public static final BlockEntry<Block> POTTED_JIYUULIA_CHIBI = BotaniaReplica.BOTANIA.potted("potted_jiyuulia_chibi", "小自由之翼盆栽", "Potted Jiyuulia Petite");

    public static final BlockEntry<Block> POTTED_KEKIMURUS = BotaniaReplica.BOTANIA.potted("potted_kekimurus", "贪食花盆栽", "Potted Kekimurus");

    public static final BlockEntry<Block> POTTED_LABELLIA = BotaniaReplica.BOTANIA.potted("potted_labellia", "铭刻莲盆栽", "Potted Labellia");

    public static final BlockEntry<Block> POTTED_LOONIUM = BotaniaReplica.BOTANIA.potted("potted_loonium", "聚宝花盆栽", "Potted Loonium");

    public static final BlockEntry<Block> POTTED_MANASTAR = BotaniaReplica.BOTANIA.potted("potted_manastar", "魔力星盆栽", "Potted Manastar");

    public static final BlockEntry<Block> POTTED_MARIMORPHOSIS = BotaniaReplica.BOTANIA.potted("potted_marimorphosis", "异构花盆栽", "Potted Marimorphosis");

    public static final BlockEntry<Block> POTTED_MARIMORPHOSIS_CHIBI = BotaniaReplica.BOTANIA.potted("potted_marimorphosis_chibi", "小异构花盆栽", "Potted Marimorphosis Petite");

    public static final BlockEntry<Block> POTTED_MEDUMONE = BotaniaReplica.BOTANIA.potted("potted_medumone", "石化草盆栽", "Potted Medumone");

    public static final BlockEntry<Block> POTTED_MUNCHDEW = BotaniaReplica.BOTANIA.potted("potted_munchdew", "咀叶花盆栽", "Potted Munchdew");

    public static final BlockEntry<Block> POTTED_NARSLIMMUS = BotaniaReplica.BOTANIA.potted("potted_narslimmus", "粘球草盆栽", "Potted Narslimmus");

    public static final BlockEntry<Block> POTTED_ORECHID = BotaniaReplica.BOTANIA.potted("potted_orechid", "凝矿兰盆栽", "Potted Orechid");

    public static final BlockEntry<Block> POTTED_ORECHID_IGNEM = BotaniaReplica.BOTANIA.potted("potted_orechid_ignem", "炎矿兰盆栽", "Potted Orechid Ignem");

    public static final BlockEntry<Block> POTTED_POLLIDISIAC = BotaniaReplica.BOTANIA.potted("potted_pollidisiac", "畜牧花盆栽", "Potted Pollidisiac");

    public static final BlockEntry<Block> POTTED_PURE_DAISY = BotaniaReplica.BOTANIA.potted("potted_pure_daisy", "白雏菊盆栽", "Potted Pure Daisy");

    public static final BlockEntry<Block> POTTED_RAFFLOWSIA = BotaniaReplica.BOTANIA.potted("potted_rafflowsia", "噬草花盆栽", "Potted Rafflowsia");

    public static final BlockEntry<Block> POTTED_RANNUNCARPUS = BotaniaReplica.BOTANIA.potted("potted_rannuncarpus", "手掌花盆栽", "Potted Rannuncarpus");

    public static final BlockEntry<Block> POTTED_RANNUNCARPUS_CHIBI = BotaniaReplica.BOTANIA.potted("potted_rannuncarpus_chibi", "小手掌花盆栽", "Potted Rannuncarpus Petite");

    public static final BlockEntry<Block> POTTED_ROSA_ARCANA = BotaniaReplica.BOTANIA.potted("potted_rosa_arcana", "阿卡纳蔷薇盆栽", "Potted Rosa Arcana");

    public static final BlockEntry<Block> POTTED_SHULK_ME_NOT = BotaniaReplica.BOTANIA.potted("potted_shulk_me_not", "勿落草盆栽", "Potted Shulk Me Not");

    public static final BlockEntry<Block> POTTED_SOLEGNOLIA = BotaniaReplica.BOTANIA.potted("potted_solegnolia", "弱磁兰盆栽", "Potted Solegnolia");

    public static final BlockEntry<Block> POTTED_SOLEGNOLIA_CHIBI = BotaniaReplica.BOTANIA.potted("potted_solegnolia_chibi", "小弱磁兰盆栽", "Potted Solegnolia Petite");

    public static final BlockEntry<Block> POTTED_SPECTRANTHEMUM = BotaniaReplica.BOTANIA.potted("potted_spectranthemum", "凌空蓟盆栽", "Potted Spectranthemum");

    public static final BlockEntry<Block> POTTED_SPECTROLUS = BotaniaReplica.BOTANIA.potted("potted_spectrolus", "斑斓花盆栽", "Potted Spectrolus");

    public static final BlockEntry<Block> POTTED_TANGLEBERRIE = BotaniaReplica.BOTANIA.potted("potted_tangleberrie", "束缚莓盆栽", "Potted Tangleberrie");

    public static final BlockEntry<Block> POTTED_TANGLEBERRIE_CHIBI = BotaniaReplica.BOTANIA.potted("potted_tangleberrie_chibi", "小束缚莓盆栽", "Potted Tangleberrie Petite");

    public static final BlockEntry<Block> POTTED_THERMALILY = BotaniaReplica.BOTANIA.potted("potted_thermalily", "炽玫瑰盆栽", "Potted Thermalily");

    public static final BlockEntry<Block> POTTED_TIGERSEYE = BotaniaReplica.BOTANIA.potted("potted_tigerseye", "虎之眼盆栽", "Potted Tigerseye");

    public static final BlockEntry<Block> POTTED_VINCULOTUS = BotaniaReplica.BOTANIA.potted("potted_vinculotus", "诱引莲盆栽", "Potted Vinculotus");

    // ==================== 功能装置 ====================

    /** 原植物魔法「永恒魔力池」：容量无限，可随意抽取。 */
    public static final BlockEntry<Block> CREATIVE_POOL = BotaniaReplica.BOTANIA.manaPool("creative_pool", "永恒魔力池", "The Everlasting Guilty Pool", ManaPoolBlock.Variant.CREATIVE);

    /** 原植物魔法「稀释魔力池」：容量 1 万。 */
    public static final BlockEntry<Block> DILUTED_POOL = BotaniaReplica.BOTANIA.manaPool("diluted_pool", "稀释魔力池", "Diluted Mana Pool", ManaPoolBlock.Variant.DILUTED);

    /** 原植物魔法「精灵魔力发射器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELVEN_SPREADER = BotaniaReplica.BOTANIA.spreader("elven_spreader", "精灵魔力发射器", "Elven Mana Spreader");

    /** 原植物魔法「炫彩魔力池」。 */
    public static final BlockEntry<Block> FABULOUS_POOL = BotaniaReplica.BOTANIA.manaPool("fabulous_pool", "炫彩魔力池", "Fabulous Mana Pool", ManaPoolBlock.Variant.FABULOUS);

    public static final BlockEntry<Block> FLUIX_MANA_POOL = BotaniaReplica.BOTANIA.fluixManaPool("fluix_mana_pool", "福鲁伊克斯魔力池", "Fluix Mana Pool");

    /** 原植物魔法「盖亚水晶」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_PYLON = BotaniaReplica.BOTANIA.pylon("gaia_pylon", "盖亚水晶", "Gaia Pylon", PylonBlock.Variant.GAIA);

    /** 原植物魔法「盖亚魔力发射器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_SPREADER = BotaniaReplica.BOTANIA.spreader("gaia_spreader", "盖亚魔力发射器", "Gaia Mana Spreader");

    /** 原植物魔法「大型魔力池」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> LARGE_MANA_POOL = BotaniaReplica.BOTANIA.largePool("large_mana_pool", "大型魔力池", "Large Mana Pool");

    /** 原植物魔法「魔力检测器」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_DETECTOR = BotaniaReplica.BOTANIA.powered("mana_detector", "魔力检测器", "Mana Detector");

    /** 原植物魔法「魔力分配器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_DISTRIBUTOR = BotaniaReplica.BOTANIA.block("mana_distributor", "魔力分配器", "Mana Splitter");

    /** 原植物魔法「魔力转换器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_FLUXFIELD = BotaniaReplica.BOTANIA.block("mana_fluxfield", "魔力转换器", "Mana Fluxfield");

    /** 原植物魔法「魔力池」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_POOL = BotaniaReplica.BOTANIA.manaPool("mana_pool", "魔力池", "Mana Pool", ManaPoolBlock.Variant.DEFAULT);

    /** 原植物魔法「魔法水晶」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_PYLON = BotaniaReplica.BOTANIA.pylon("mana_pylon", "魔法水晶", "Mana Pylon", PylonBlock.Variant.MANA);

    /** 原植物魔法「魔力发射器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_SPREADER = BotaniaReplica.BOTANIA.spreader("mana_spreader", "魔力发射器", "Mana Spreader");

    /** 原植物魔法「魔力销毁器」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_VOID = BotaniaReplica.BOTANIA.block("mana_void", "魔力销毁器", "Mana Void");

    /** 原植物魔法「自然水晶」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> NATURA_PYLON = BotaniaReplica.BOTANIA.pylon("natura_pylon", "自然水晶", "Natura Pylon", PylonBlock.Variant.NATURA);

    /** 原植物魔法「魔力泵」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> PUMP = BotaniaReplica.BOTANIA.pump("pump", "魔力泵", "Mana Pump");

    /** 原植物魔法「红石魔力发射器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> REDSTONE_SPREADER = BotaniaReplica.BOTANIA.spreader("redstone_spreader", "红石魔力发射器", "Pulse Mana Spreader");

    /** 原植物魔法「火花工匠台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SPARK_CHANGER = BotaniaReplica.BOTANIA.noOcclusion("spark_changer", "火花工匠台", "Spark Tinkerer");

    /** 原植物魔法「炼金催化器」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ALCHEMY_CATALYST = BotaniaReplica.BOTANIA.block("alchemy_catalyst", "炼金催化器", "Alchemy Catalyst");

    /** 原植物魔法「精灵门核心」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ALFHEIM_PORTAL = BotaniaReplica.BOTANIA.alfheimPortal("alfheim_portal", "精灵门核心", "Elven Gateway Core");

    /** 原植物魔法「深板岩花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_DEEPSLATE = BotaniaReplica.BOTANIA.apothecary("apothecary_deepslate", "深板岩花药台", "Deepslate Petal Apothecary");

    /** 原植物魔法「花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_DEFAULT = BotaniaReplica.BOTANIA.apothecary("apothecary_default", "花药台", "Petal Apothecary");

    /** 原植物魔法「沙漠花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_DESERT = BotaniaReplica.BOTANIA.apothecary("apothecary_desert", "沙漠花药台", "Solite Petal Apothecary");

    /** 原植物魔法「森林花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_FOREST = BotaniaReplica.BOTANIA.apothecary("apothecary_forest", "森林花药台", "Fuchsite Petal Apothecary");

    /** 原植物魔法「菌丝花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_FUNGAL = BotaniaReplica.BOTANIA.apothecary("apothecary_fungal", "菌丝花药台", "Mycelite Petal Apothecary");

    /** 原植物魔法「高原花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_MESA = BotaniaReplica.BOTANIA.apothecary("apothecary_mesa", "高原花药台", "Rosy Talc Petal Apothecary");

    /** 原植物魔法「苔石花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_MOSSY = BotaniaReplica.BOTANIA.apothecary("apothecary_mossy", "苔石花药台", "Mossy Petal Apothecary");

    /** 原植物魔法「高山花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_MOUNTAIN = BotaniaReplica.BOTANIA.apothecary("apothecary_mountain", "高山花药台", "Gneiss Petal Apothecary");

    /** 原植物魔法「平原花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_PLAINS = BotaniaReplica.BOTANIA.apothecary("apothecary_plains", "平原花药台", "Talc Petal Apothecary");

    /** 原植物魔法「沼泽花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_SWAMP = BotaniaReplica.BOTANIA.apothecary("apothecary_swamp", "沼泽花药台", "Cataclasite Petal Apothecary");

    /** 原植物魔法「雪原花药台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> APOTHECARY_TAIGA = BotaniaReplica.BOTANIA.apothecary("apothecary_taiga", "雪原花药台", "Lunite Petal Apothecary");

    /** 原植物魔法「植物酿造台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BREWERY = BotaniaReplica.BOTANIA.block("brewery", "植物酿造台", "Botanical Brewery");

    /** 原植物魔法「炼造催化器」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CONJURATION_CATALYST = BotaniaReplica.BOTANIA.block("conjuration_catalyst", "炼造催化器", "Conjuration Catalyst");

    /** 原植物魔法「魔力附魔台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ENCHANTER = BotaniaReplica.BOTANIA.block("enchanter", "魔力附魔台", "Mana Enchanter");

    /** 原植物魔法「符文祭坛」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RUNIC_ALTAR = BotaniaReplica.BOTANIA.runicAltar("runic_altar", "符文祭坛", "Runic Altar");

    /** 原植物魔法「泰拉凝聚板」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TERRA_PLATE = BotaniaReplica.BOTANIA.noOcclusion("terra_plate", "泰拉凝聚板", "Terrestrial Agglomeration Plate");

    /** 原植物魔法「奥秘平台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ABSTRUSE_PLATFORM = BotaniaReplica.BOTANIA.block("abstruse_platform", "奥秘平台", "Abstruse Platform");

    /** 原植物魔法「活化红石火把」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ANIMATED_TORCH = BotaniaReplica.BOTANIA.block("animated_torch", "活化红石火把", "Animated Torch");

    /** 原植物魔法「活木化身」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AVATAR = BotaniaReplica.BOTANIA.block("avatar", "活木化身", "Livingwood Avatar");

    /** 原植物魔法「魔源风箱」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BELLOWS = BotaniaReplica.BOTANIA.block("bellows", "魔源风箱", "Manatide Bellows");

    /** 原植物魔法「不和谐之音方块」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CACOPHONIUM_BLOCK = BotaniaReplica.BOTANIA.block("cacophonium_block", "不和谐之音方块", "Cacophonium Block");

    /** 原植物魔法「细胞方块」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CELL_BLOCK = BotaniaReplica.BOTANIA.block("cell_block", "细胞方块", "Cellular Block");

    /** 原植物魔法「随想之茧」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> COCOON = BotaniaReplica.BOTANIA.noOcclusion("cocoon", "随想之茧", "Cocoon of Caprice");

    /** 原植物魔法「机巧箱」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CRAFTY_CRATE = BotaniaReplica.BOTANIA.cratePattern("crafty_crate", "机巧箱", "Crafty Crate");

    /** 原植物魔法「林冠之鼓」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DRUM_CANOPY = BotaniaReplica.BOTANIA.noOcclusion("drum_canopy", "林冠之鼓", "Drum of the Canopy");

    /** 原植物魔法「收集之鼓」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DRUM_GATHERING = BotaniaReplica.BOTANIA.noOcclusion("drum_gathering", "收集之鼓", "Drum of the Gathering");

    /** 原植物魔法「狂野之鼓」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DRUM_WILD = BotaniaReplica.BOTANIA.noOcclusion("drum_wild", "狂野之鼓", "Drum of the Wild");

    /** 原植物魔法「恶魔南瓜头」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FEL_PUMPKIN = BotaniaReplica.BOTANIA.facing("fel_pumpkin", "恶魔南瓜头", "Fel Pumpkin");

    /** 原植物魔法「远古之眼」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FOREST_EYE = BotaniaReplica.BOTANIA.noOcclusion("forest_eye", "远古之眼", "Eye of the Ancients");

    /** 原植物魔法「悬浮沙漏」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> HOURGLASS = BotaniaReplica.BOTANIA.block("hourglass", "悬浮沙漏", "Hovering Hourglass");

    /** 原植物魔法「香盘」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> INCENSE_PLATE = BotaniaReplica.BOTANIA.facingNoOcclusion("incense_plate", "香盘", "Incense Plate");

    /** 原植物魔法「不破平台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> INFRANGIBLE_PLATFORM = BotaniaReplica.BOTANIA.block("infrangible_platform", "不破平台", "Infrangible Platform");

    /** 原植物魔法「花格箱」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> OPEN_CRATE = BotaniaReplica.BOTANIA.block("open_crate", "花格箱", "Open Crate");

    /** 原植物魔法「魔力棱镜」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> PRISM = BotaniaReplica.BOTANIA.noOcclusion("prism", "魔力棱镜", "Mana Prism");

    /** 原植物魔法「生命灌注器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SPAWNER_CLAW = BotaniaReplica.BOTANIA.noOcclusion("spawner_claw", "生命灌注器", "Life Imbuer");

    /** 原植物魔法「幻影平台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SPECTRAL_PLATFORM = BotaniaReplica.BOTANIA.block("spectral_platform", "幻影平台", "Spectral Platform");

    /** 原植物魔法「星空制造器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> STARFIELD = BotaniaReplica.BOTANIA.noOcclusion("starfield", "星空制造器", "Starfield Creator");

    /** 原植物魔法「晴天娃娃」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TERU_TERU_BOZU = BotaniaReplica.BOTANIA.noOcclusion("teru_teru_bozu", "晴天娃娃", "Teru Teru Bozu");

    /** 原植物魔法「小土豆」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TINY_POTATO = BotaniaReplica.BOTANIA.facingNoOcclusion("tiny_potato", "小土豆", "Tiny Potato");

    /** 原植物魔法「发射器旋转台」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TURNTABLE = BotaniaReplica.BOTANIA.block("turntable", "发射器旋转台", "Spreader Turntable");

    /** 原植物魔法「魔力风暴」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_BOMB = BotaniaReplica.BOTANIA.block("mana_bomb", "魔力风暴", "Manastorm Charge");

    // ==================== corporea 与红石 ====================

    public static final BlockEntry<Block> CORPOREA_BLOCK = BotaniaReplica.BOTANIA.block("corporea_block", "多媒体块", "Corporea Block");

    public static final BlockEntry<Block> CORPOREA_BRICK = BotaniaReplica.BOTANIA.block("corporea_brick", "多媒体砖", "Corporea Brick");

    public static final BlockEntry<Block> CORPOREA_CRYSTAL_CUBE = BotaniaReplica.BOTANIA.noOcclusion("corporea_crystal_cube", "多媒体水晶魔方", "Corporea Crystal Cube");

    public static final BlockEntry<Block> CORPOREA_FUNNEL = BotaniaReplica.BOTANIA.block("corporea_funnel", "多媒体漏斗", "Corporea Funnel");

    public static final BlockEntry<Block> CORPOREA_INDEX = BotaniaReplica.BOTANIA.corporeaIndex("corporea_index", "多媒体索引", "Corporea Index");

    public static final BlockEntry<Block> CORPOREA_INTERCEPTOR = BotaniaReplica.BOTANIA.block("corporea_interceptor", "多媒体拦截器", "Corporea Interceptor");

    public static final BlockEntry<Block> CORPOREA_RETAINER = BotaniaReplica.BOTANIA.block("corporea_retainer", "多媒体固定器", "Corporea Retainer");

    public static final BlockEntry<Block> DETECTOR_LIGHT_RELAY = BotaniaReplica.BOTANIA.block("detector_light_relay", "探测微光", "Detector Luminizer");

    public static final BlockEntry<Block> FORK_LIGHT_RELAY = BotaniaReplica.BOTANIA.block("fork_light_relay", "岔路微光", "Fork Luminizer");

    public static final BlockEntry<Block> TOGGLE_LIGHT_RELAY = BotaniaReplica.BOTANIA.block("toggle_light_relay", "激活微光", "Toggle Luminizer");

    public static final BlockEntry<Block> LIGHT_LAUNCHER = BotaniaReplica.BOTANIA.noOcclusion("light_launcher", "微光发射器", "Luminizer Launcher");

    public static final BlockEntry<Block> LIGHT_RELAY = BotaniaReplica.BOTANIA.block("light_relay", "微光", "Luminizer");

    public static final BlockEntry<Block> PISTON_RELAY = BotaniaReplica.BOTANIA.block("piston_relay", "力量传递器", "Force Relay");

    public static final BlockEntry<Block> RED_STRING_COMPARATOR = BotaniaReplica.BOTANIA.facing("red_string_comparator", "红线比较器", "Red Stringed Comparator");

    public static final BlockEntry<Block> RED_STRING_CONTAINER = BotaniaReplica.BOTANIA.facing("red_string_container", "红线容器", "Red Stringed Container");

    public static final BlockEntry<Block> RED_STRING_DISPENSER = BotaniaReplica.BOTANIA.facing("red_string_dispenser", "红线发射器", "Red Stringed Dispenser");

    public static final BlockEntry<Block> RED_STRING_FERTILIZER = BotaniaReplica.BOTANIA.facing("red_string_fertilizer", "红线施肥器", "Red Stringed Nutrifier");

    public static final BlockEntry<Block> RED_STRING_INTERCEPTOR = BotaniaReplica.BOTANIA.facing("red_string_interceptor", "红线拦截器", "Red Stringed Interceptor");

    public static final BlockEntry<Block> RED_STRING_RELAY = BotaniaReplica.BOTANIA.facing("red_string_relay", "红线继电器", "Red Stringed Spoofer");

    // ==================== 装饰与特殊方块 ====================

    public static final BlockEntry<Block> FAKE_AIR = BotaniaReplica.BOTANIA.blockNoItem("fake_air", "虚假空气", "Fake Air");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_0 = BotaniaReplica.BOTANIA.block("azulejo_0", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_1 = BotaniaReplica.BOTANIA.block("azulejo_1", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_10 = BotaniaReplica.BOTANIA.block("azulejo_10", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_11 = BotaniaReplica.BOTANIA.block("azulejo_11", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_12 = BotaniaReplica.BOTANIA.block("azulejo_12", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_13 = BotaniaReplica.BOTANIA.block("azulejo_13", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_14 = BotaniaReplica.BOTANIA.block("azulejo_14", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_15 = BotaniaReplica.BOTANIA.block("azulejo_15", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_2 = BotaniaReplica.BOTANIA.block("azulejo_2", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_3 = BotaniaReplica.BOTANIA.block("azulejo_3", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_4 = BotaniaReplica.BOTANIA.block("azulejo_4", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_5 = BotaniaReplica.BOTANIA.block("azulejo_5", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_6 = BotaniaReplica.BOTANIA.block("azulejo_6", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_7 = BotaniaReplica.BOTANIA.block("azulejo_7", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_8 = BotaniaReplica.BOTANIA.block("azulejo_8", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「阿兹勒赫瓷砖」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AZULEJO_9 = BotaniaReplica.BOTANIA.block("azulejo_9", "阿兹勒赫瓷砖", "Azulejo");

    /** 原植物魔法「魔力闪光」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_FLAME = BotaniaReplica.BOTANIA.block("mana_flame", "魔力闪光", "Mana Flash");

    /** 原植物魔法「盖亚守护者的头」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_HEAD = BotaniaReplica.BOTANIA.block("gaia_head", "盖亚守护者的头", "Gaia Guardian's Head");

    /** 原植物魔法「gaia_wall_head」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_WALL_HEAD = BotaniaReplica.BOTANIA.block("gaia_wall_head", "盖亚守护者的头", "Gaia Guardian's Head");

    /** 原植物魔法「烈焰网」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BLAZE_BLOCK = BotaniaReplica.BOTANIA.block("blaze_block", "烈焰网", "Blaze Mesh");

    /** 原植物魔法「龙石块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DRAGONSTONE_BLOCK = BotaniaReplica.BOTANIA.block("dragonstone_block", "龙石块", "Block of Dragonstone");

    /** 原植物魔法「干草地」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DRY_GRASS = BotaniaReplica.BOTANIA.block("dry_grass", "干草地", "Dry Grass");

    /** 原植物魔法「蕴魔土」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ENCHANTED_SOIL = BotaniaReplica.BOTANIA.block("enchanted_soil", "蕴魔土", "Enchanted Soil");

    /** 原植物魔法「末影监视者」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ENDER_EYE_BLOCK = BotaniaReplica.BOTANIA.powered("ender_eye_block", "末影监视者", "Ender Overseer");

    /** 原植物魔法「黄金草地」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GOLDEN_GRASS = BotaniaReplica.BOTANIA.block("golden_grass", "黄金草地", "Golden Grass");

    /** 原植物魔法「注魔草地」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> INFUSED_GRASS = BotaniaReplica.BOTANIA.block("infused_grass", "注魔草地", "Infused Grass");

    /** 原植物魔法「异变草地」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MUTATED_GRASS = BotaniaReplica.BOTANIA.block("mutated_grass", "异变草地", "Mutated Grass");

    /** 原植物魔法「活根」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ROOT = BotaniaReplica.BOTANIA.block("root", "活根", "Living Root");

    /** 原植物魔法「焦草草地」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SCORCHED_GRASS = BotaniaReplica.BOTANIA.block("scorched_grass", "焦草草地", "Scorched Grass");

    /** 原植物魔法「solid_vine」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SOLID_VINE = BotaniaReplica.BOTANIA.solidVine("solid_vine", "实心藤", "Solid Vine");

    /** 原植物魔法「魔力环绕器」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TINY_PLANET_BLOCK = BotaniaReplica.BOTANIA.noOcclusion("tiny_planet_block", "魔力环绕器", "Tiny Planet");

    /** 原植物魔法「鲜草草地」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> VIVID_GRASS = BotaniaReplica.BOTANIA.block("vivid_grass", "鲜草草地", "Vivid Grass");

    /** 原植物魔法「临时彩虹桥方块」（原版带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BIFROST = BotaniaReplica.BOTANIA.block("bifrost", "临时彩虹桥方块", "Temporary Bifrost Block");

    /** 原植物魔法「彩虹桥方块」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BIFROST_PERM = BotaniaReplica.BOTANIA.block("bifrost_perm", "彩虹桥方块", "Bifrost Block");

    /** 原植物魔法「太阳花装饰」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DAYBLOOM_MOTIF = BotaniaReplica.BOTANIA.flower("daybloom_motif", "太阳花装饰", "Daybloom Motif");

    /** 原植物魔法「幻影轨道」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GHOST_RAIL = BotaniaReplica.BOTANIA.railShape("ghost_rail", "幻影轨道", "Spectral Rail");

    /** 原植物魔法「水绣球装饰」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> HYDROANGEAS_MOTIF = BotaniaReplica.BOTANIA.flower("hydroangeas_motif", "水绣球装饰", "Hydroangeas Motif");

    /** 原植物魔法「夜颠茄装饰」（原版有特殊行为），此处仅注册基础方块。 */
    public static final BlockEntry<Block> NIGHTSHADE_MOTIF = BotaniaReplica.BOTANIA.flower("nightshade_motif", "夜颠茄装饰", "Nightshade Motif");

    public static net.minecraft.world.level.block.Block getBlock(String id) {
        return BotaniaReplica.BOTANIA.getBlock(id);
    }
}
