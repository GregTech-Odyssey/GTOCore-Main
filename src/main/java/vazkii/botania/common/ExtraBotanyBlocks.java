package vazkii.botania.common;

import net.minecraft.world.level.block.Block;

import com.gto.registrate.util.entry.BlockEntry;

/**
 * 额外植物学方块复刻。按内容类型分组。
 */
public final class ExtraBotanyBlocks {

    private ExtraBotanyBlocks() {}

    public static void init() {}

    // ==================== 基础材料方块 ====================

    /** 原额外植物学「天空块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> AERIALITE_BLOCK = BotaniaReplica.EXTRABOTANY.block("aerialite_block", "天空块", "Block of Aerialite");

    /** 原额外植物学「奥利哈钢块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ORICHALCOS_BLOCK = BotaniaReplica.EXTRABOTANY.block("orichalcos_block", "奥利哈钢块", "Block of Orichalcos");

    /** 原额外植物学「光子块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> PHOTONIUM_BLOCK = BotaniaReplica.EXTRABOTANY.block("photonium_block", "光子块", "Block of Photonium");

    /** 原额外植物学「暗影块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SHADOWIUM_BLOCK = BotaniaReplica.EXTRABOTANY.block("shadowium_block", "暗影块", "Block of Shadowium");

    // ==================== 石材与构件 ====================

    /** 原额外植物学「活石祭坛」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> PEDESTAL_LIVINGROCK = BotaniaReplica.EXTRABOTANY.block("pedestal_livingrock", "活石祭坛", "Livingrock Pedestal");

    // ==================== 石英 ====================

    /** 原额外植物学「雕纹源质石英块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_ELEMENTIUM_QUARTZ_BLOCK = BotaniaReplica.EXTRABOTANY.block("chiseled_elementium_quartz_block", "雕纹源质石英块", "Chiseled Elementium Quartz Block");

    /** 原额外植物学「雕纹盖亚石英块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> CHISELED_GAIA_QUARTZ_BLOCK = BotaniaReplica.EXTRABOTANY.block("chiseled_gaia_quartz_block", "雕纹盖亚石英块", "Chiseled Gaia Quartz Block");

    /** 原额外植物学「源质石英块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELEMENTIUM_QUARTZ_BLOCK = BotaniaReplica.EXTRABOTANY.block("elementium_quartz_block", "源质石英块", "Elementium Quartz Block");

    /** 原额外植物学「源质石英砖」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELEMENTIUM_QUARTZ_BRICKS = BotaniaReplica.EXTRABOTANY.block("elementium_quartz_bricks", "源质石英砖", "Elementium Quartz Bricks");

    /** 原额外植物学「源质石英柱」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELEMENTIUM_QUARTZ_PILLAR = BotaniaReplica.EXTRABOTANY.axis("elementium_quartz_pillar", "源质石英柱", "Elementium Quartz Pillar");

    /** 原额外植物学「盖亚石英块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_QUARTZ_BLOCK = BotaniaReplica.EXTRABOTANY.block("gaia_quartz_block", "盖亚石英块", "Gaia Quartz Block");

    /** 原额外植物学「盖亚石英砖」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_QUARTZ_BRICKS = BotaniaReplica.EXTRABOTANY.block("gaia_quartz_bricks", "盖亚石英砖", "Gaia Quartz Bricks");

    /** 原额外植物学「盖亚石英柱」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_QUARTZ_PILLAR = BotaniaReplica.EXTRABOTANY.axis("gaia_quartz_pillar", "盖亚石英柱", "Gaia Quartz Pillar");

    /** 原额外植物学「平滑源质石英块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SMOOTH_ELEMENTIUM_QUARTZ = BotaniaReplica.EXTRABOTANY.block("smooth_elementium_quartz", "平滑源质石英块", "Smooth Elementium Quartz");

    /** 原额外植物学「平滑盖亚石英块」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SMOOTH_GAIA_QUARTZ = BotaniaReplica.EXTRABOTANY.block("smooth_gaia_quartz", "平滑盖亚石英块", "Smooth Gaia Quartz");

    // ==================== 台阶楼梯墙栅栏与玻璃板 ====================

    /** 原额外植物学「源质石英台阶」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELEMENTIUM_QUARTZ_SLAB = BotaniaReplica.EXTRABOTANY.slab("elementium_quartz_slab", "源质石英台阶", "Elementium Quartz Slab");

    /** 原额外植物学「源质石英楼梯」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ELEMENTIUM_QUARTZ_STAIRS = BotaniaReplica.EXTRABOTANY.stairs("elementium_quartz_stairs", "源质石英楼梯", "Elementium Quartz Stairs");

    /** 原额外植物学「盖亚石英台阶」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_QUARTZ_SLAB = BotaniaReplica.EXTRABOTANY.slab("gaia_quartz_slab", "盖亚石英台阶", "Gaia Quartz Slab");

    /** 原额外植物学「盖亚石英楼梯」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> GAIA_QUARTZ_STAIRS = BotaniaReplica.EXTRABOTANY.stairs("gaia_quartz_stairs", "盖亚石英楼梯", "Gaia Quartz Stairs");

    /** 原额外植物学「平滑源质石英台阶」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SMOOTH_ELEMENTIUM_QUARTZ_SLAB = BotaniaReplica.EXTRABOTANY.slab("smooth_elementium_quartz_slab", "平滑源质石英台阶", "Smooth Elementium Quartz Slab");

    /** 原额外植物学「平滑源质石英楼梯」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SMOOTH_ELEMENTIUM_QUARTZ_STAIRS = BotaniaReplica.EXTRABOTANY.stairs("smooth_elementium_quartz_stairs", "平滑源质石英楼梯", "Smooth Elementium Quartz Stairs");

    /** 原额外植物学「平滑盖亚石英台阶」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SMOOTH_GAIA_QUARTZ_SLAB = BotaniaReplica.EXTRABOTANY.slab("smooth_gaia_quartz_slab", "平滑盖亚石英台阶", "Smooth Gaia Quartz Slab");

    /** 原额外植物学「平滑盖亚石英楼梯」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SMOOTH_GAIA_QUARTZ_STAIRS = BotaniaReplica.EXTRABOTANY.stairs("smooth_gaia_quartz_stairs", "平滑盖亚石英楼梯", "Smooth Gaia Quartz Stairs");

    // ==================== 功能花 ====================

    /** 原额外植物学「神烦花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ANNOYINGFLOWER = BotaniaReplica.EXTRABOTANY.flower("annoyingflower", "神烦花", "Annoyingflower");

    /** 原额外植物学「风铃草」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BELLFLOWER = BotaniaReplica.EXTRABOTANY.flower("bellflower", "风铃草", "Bellflower");

    /** 原额外植物学「鲜血妖姬」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> BLOOD_ENCHANTRESS = BotaniaReplica.EXTRABOTANY.flower("blood_enchantress", "鲜血妖姬", "Blood Enchantress");

    /** 原额外植物学「雪绒花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> EDELWEISS = BotaniaReplica.EXTRABOTANY.flower("edelweiss", "雪绒花", "Edelweiss");

    /** 原额外植物学「浮空神烦花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_ANNOYINGFLOWER = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_annoyingflower", "浮空神烦花", "Floating Annoyingflower");

    /** 原额外植物学「浮空风铃草」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_BELLFLOWER = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_bellflower", "浮空风铃草", "Floating Bellflower");

    /** 原额外植物学「浮空鲜血妖姬」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_BLOOD_ENCHANTRESS = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_blood_enchantress", "浮空鲜血妖姬", "Floating Blood Enchantress");

    /** 原额外植物学「浮空雪绒花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_EDELWEISS = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_edelweiss", "浮空雪绒花", "Floating Edelweiss");

    /** 原额外植物学「浮空魔链星」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_MANALINK = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_manalink", "浮空魔链星", "Floating Manalink");

    /** 原额外植物学「浮空镜姬」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_MIRROWTUNIA = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_mirrowtunia", "浮空镜姬", "Floating Mirrowtunia");

    /** 原额外植物学「浮空月耀百合」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_MOONLIGHT_LILY = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_moonlight_lily", "浮空月耀百合", "Floating Moonlight Lily");

    /** 原额外植物学「浮空死之花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_NECROFLEUR = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_necrofleur", "浮空死之花", "Floating Necrofleur");

    /** 原额外植物学「浮空小死之花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_NECROFLEUR_CHIBI = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_necrofleur_chibi", "浮空小死之花", "Floating Necrofleur Petite");

    /** 原额外植物学「浮空全知瑾」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_OMNIVIOLET = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_omniviolet", "浮空全知瑾", "Floating Omniviolet");

    /** 原额外植物学「浮空雷卡兰」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_REIKARLILY = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_reikarlily", "浮空雷卡兰", "Floating Reikarlily");

    /** 原额外植物学「浮空回音花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_RESONCUND = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_resoncund", "浮空回音花", "Floating Resoncund");

    /** 原额外植物学「浮空永寂龙胆」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_SERENITIAN = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_serenitian", "浮空永寂龙胆", "Floating Serenitian");

    /** 原额外植物学「浮空石中姬」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_STONESIA = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_stonesia", "浮空石中姬", "Floating Stonesia");

    /** 原额外植物学「浮空日曜百合」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_SUNSHINE_LILY = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_sunshine_lily", "浮空日曜百合", "Floating Sunshine Lily");

    /** 原额外植物学「浮空叮当舞花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_TINKLE = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_tinkle", "浮空叮当舞花", "Floating Tinkle");

    /** 原额外植物学「浮空商友兰」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_TRADE_ORCHID = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_trade_orchid", "浮空商友兰", "Floating Trade Orchid");

    /** 原额外植物学「浮空双子兰」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_TWINSTAR = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_twinstar", "浮空双子兰", "Floating Twinstar");

    /** 原额外植物学「浮空伐木花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_WOODIENIA = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_woodienia", "浮空伐木花", "Floating Woodienia");

    /** 原额外植物学「魔链星」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANALINK = BotaniaReplica.EXTRABOTANY.flower("manalink", "魔链星", "Manalink");

    /** 原额外植物学「镜姬」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MIRROWTUNIA = BotaniaReplica.EXTRABOTANY.flower("mirrowtunia", "镜姬", "Mirrowtunia");

    /** 原额外植物学「月耀百合」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MOONLIGHT_LILY = BotaniaReplica.EXTRABOTANY.flower("moonlight_lily", "月耀百合", "Moonlight Lily");

    /** 原额外植物学「死之花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> NECROFLEUR = BotaniaReplica.EXTRABOTANY.flower("necrofleur", "死之花", "Necrofleur");

    /** 原额外植物学「小死之花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> NECROFLEUR_CHIBI = BotaniaReplica.EXTRABOTANY.flower("necrofleur_chibi", "小死之花", "Necrofleur Petite");

    /** 原额外植物学「全知瑾」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> OMNIVIOLET = BotaniaReplica.EXTRABOTANY.flower("omniviolet", "全知瑾", "Omniviolet");

    /** 原额外植物学「雷卡兰」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> REIKARLILY = BotaniaReplica.EXTRABOTANY.flower("reikarlily", "雷卡兰", "Reikarlily");

    /** 原额外植物学「回音花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> RESONCUND = BotaniaReplica.EXTRABOTANY.flower("resoncund", "回音花", "Resoncund");

    /** 原额外植物学「永寂龙胆」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SERENITIAN = BotaniaReplica.EXTRABOTANY.flower("serenitian", "永寂龙胆", "Serenitian");

    /** 原额外植物学「石中姬」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> STONESIA = BotaniaReplica.EXTRABOTANY.flower("stonesia", "石中姬", "Stonesia");

    /** 原额外植物学「日曜百合」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> SUNSHINE_LILY = BotaniaReplica.EXTRABOTANY.flower("sunshine_lily", "日曜百合", "Sunshine Lily");

    /** 原额外植物学「叮当舞花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TINKLE = BotaniaReplica.EXTRABOTANY.flower("tinkle", "叮当舞花", "Tinkle");

    /** 原额外植物学「商友兰」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TRADE_ORCHID = BotaniaReplica.EXTRABOTANY.flower("trade_orchid", "商友兰", "Trade Orchid");

    /** 原额外植物学「双子兰」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> TWINSTAR = BotaniaReplica.EXTRABOTANY.flower("twinstar", "双子兰", "Twinstar");

    /** 原额外植物学「伐木花」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> WOODIENIA = BotaniaReplica.EXTRABOTANY.flower("woodienia", "伐木花", "Woodienia");

    // ==================== 盆栽 ====================

    /** 原额外植物学「神烦花盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_ANNOYINGFLOWER = BotaniaReplica.EXTRABOTANY.potted("potted_annoyingflower", "神烦花盆栽", "Potted Annoyingflower");

    /** 原额外植物学「风铃草盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_BELLFLOWER = BotaniaReplica.EXTRABOTANY.potted("potted_bellflower", "风铃草盆栽", "Potted Bellflower");

    /** 原额外植物学「鲜血妖姬盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_BLOOD_ENCHANTRESS = BotaniaReplica.EXTRABOTANY.potted("potted_blood_enchantress", "鲜血妖姬盆栽", "Potted Blood Enchantress");

    /** 原额外植物学「雪绒花盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_EDELWEISS = BotaniaReplica.EXTRABOTANY.potted("potted_edelweiss", "雪绒花盆栽", "Potted Edelweiss");

    /** 原额外植物学「蕴魔瑾盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_ENCHANTER = BotaniaReplica.EXTRABOTANY.potted("potted_enchanter", "蕴魔瑾盆栽", "Potted Enchanter");

    /** 原额外植物学「魔链星盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_MANALINK = BotaniaReplica.EXTRABOTANY.potted("potted_manalink", "魔链星盆栽", "Potted Manalink");

    /** 原额外植物学「镜姬盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_MIRROWTUNIA = BotaniaReplica.EXTRABOTANY.potted("potted_mirrowtunia", "镜姬盆栽", "Potted Mirrowtunia");

    /** 原额外植物学「月耀百合盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_MOONLIGHT_LILY = BotaniaReplica.EXTRABOTANY.potted("potted_moonlight_lily", "月耀百合盆栽", "Potted Moonlight Lily");

    /** 原额外植物学「死之花盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_NECROFLEUR = BotaniaReplica.EXTRABOTANY.potted("potted_necrofleur", "死之花盆栽", "Potted Necrofleur");

    /** 原额外植物学「小死之花盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_NECROFLEUR_CHIBI = BotaniaReplica.EXTRABOTANY.potted("potted_necrofleur_chibi", "小死之花盆栽", "Potted Necrofleur Petite");

    /** 原额外植物学「全知瑾盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_OMNIVIOLET = BotaniaReplica.EXTRABOTANY.potted("potted_omniviolet", "全知瑾盆栽", "Potted Omniviolet");

    /** 原额外植物学「雷卡兰盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_REIKARLILY = BotaniaReplica.EXTRABOTANY.potted("potted_reikarlily", "雷卡兰盆栽", "Potted Reikarlily");

    /** 原额外植物学「回音花盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_RESONCUND = BotaniaReplica.EXTRABOTANY.potted("potted_resoncund", "回音花盆栽", "Potted Resoncund");

    /** 原额外植物学「永寂龙胆盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_SERENITIAN = BotaniaReplica.EXTRABOTANY.potted("potted_serenitian", "永寂龙胆盆栽", "Potted Serenitian");

    /** 原额外植物学「石中姬盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_STONESIA = BotaniaReplica.EXTRABOTANY.potted("potted_stonesia", "石中姬盆栽", "Potted Stonesia");

    /** 原额外植物学「日曜百合盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_SUNSHINE_LILY = BotaniaReplica.EXTRABOTANY.potted("potted_sunshine_lily", "日曜百合盆栽", "Potted Sunshine Lily");

    /** 原额外植物学「叮当舞花盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_TINKLE = BotaniaReplica.EXTRABOTANY.potted("potted_tinkle", "叮当舞花盆栽", "Potted Tinkle");

    /** 原额外植物学「商友兰盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_TRADE_ORCHID = BotaniaReplica.EXTRABOTANY.potted("potted_trade_orchid", "商友兰盆栽", "Potted Trade Orchid");

    /** 原额外植物学「双子兰盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_TWINSTAR = BotaniaReplica.EXTRABOTANY.potted("potted_twinstar", "双子兰盆栽", "Potted Twinstar");

    /** 原额外植物学「伐木花盆栽」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POTTED_WOODIENIA = BotaniaReplica.EXTRABOTANY.potted("potted_woodienia", "伐木花盆栽", "Potted Woodienia");

    // ==================== 功能装置 ====================

    /** 原额外植物学「蕴魔瑾」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> ENCHANTER = BotaniaReplica.EXTRABOTANY.block("enchanter", "蕴魔瑾", "Enchanter");

    /** 原额外植物学「浮空蕴魔瑾」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> FLOATING_ENCHANTER = BotaniaReplica.EXTRABOTANY.floatingFlower("floating_enchanter", "浮空蕴魔瑾", "Floating Enchanter");

    /** 原额外植物学「次元催化器」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> DIMENSION_CATALYST = BotaniaReplica.EXTRABOTANY.block("dimension_catalyst", "次元催化器", "Dimension Catalyst");

    /** 原额外植物学「魔力充能基座」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> MANA_CHARGER = BotaniaReplica.EXTRABOTANY.noOcclusion("mana_charger", "魔力充能基座", "Mana Charger");

    /** 原额外植物学「方解石祭坛」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> PEDESTAL_CALCITE = BotaniaReplica.EXTRABOTANY.block("pedestal_calcite", "方解石祭坛", "Calcite Pedestal");

    /** 原额外植物学「力量框架」（原版为功能方块或带方块实体），此处仅注册基础方块。 */
    public static final BlockEntry<Block> POWER_FRAME = BotaniaReplica.EXTRABOTANY.block("power_frame", "力量框架", "Power Frame");

    public static net.minecraft.world.level.block.Block getBlock(String id) {
        return BotaniaReplica.EXTRABOTANY.getBlock(id);
    }
}
