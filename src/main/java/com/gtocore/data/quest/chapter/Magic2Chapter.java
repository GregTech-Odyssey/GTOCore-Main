package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.ManaMultiBlock;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.quest.QuestTextures;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestImage;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;

import net.minecraft.world.item.Items;

import com.chaosthedude.naturescompass.NaturesCompass;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.item.BotaniaItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class Magic2Chapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("magic2", c -> c
            .title("英雄之路")
            .subtitle("当魔法遇上格雷")
            .icon(BotaniaItems.dice)
            .hideQuestsUntilDepsVisible()
            .autofocus(Magic2Chapter.Q_43F6C913));

    public static final Quest VARIOUS_RUNES = CHAPTER.quest("various_runes", q -> q
            .at(-0.3125, 5.625)
            .title("各种符文")
            .desc(d -> d.line("是的，你需要制作这里的所有符文……非常繁琐"))
            .task(
                    Task.item(BotaniaItems.runeWater),
                    Task.item(BotaniaItems.runeFire),
                    Task.item(BotaniaItems.runeEarth),
                    Task.item(BotaniaItems.runeAir),
                    Task.item(BotaniaItems.runeSpring),
                    Task.item(BotaniaItems.runeSummer),
                    Task.item(BotaniaItems.runeAutumn),
                    Task.item(BotaniaItems.runeWinter),
                    Task.item(BotaniaItems.runeMana),
                    Task.item(BotaniaItems.runeLust),
                    Task.item(BotaniaItems.runeGluttony),
                    Task.item(BotaniaItems.runeGreed),
                    Task.item(BotaniaItems.runeSloth),
                    Task.item(BotaniaItems.runeWrath),
                    Task.item(BotaniaItems.runeEnvy),
                    Task.item(BotaniaItems.runePride))
            .dependsOn(Magic1Chapter.RUNIC_ALTAR));

    public static final Quest AMPLIFY_MANA_SPREADER = CHAPTER.quest("amplify_mana_spreader", q -> q
            .at(-0.25, 10).minWidth(300)
            .title("强化{魔力发射器}", GREEN)
            .desc(d -> d
                    .line("通过在{魔力发射器}前端加装{魔力透镜}实现升级,基础透镜暂无特殊效果.", GREEN, GREEN)
                    .line()
                    .line("需升级透镜以赋予魔力脉冲特殊效果.可用染料或{魔力珍珠}{合成彩色/彩虹透镜.}", GREEN, WHITE)
                    .line()
                    .line("{结合符文与特定物品可制作进阶透镜,用}{粘液球}融合两枚透镜可制成{复合透镜},叠加强化效果.", WHITE, GREEN, BLUE))
            .task(Task.item(BotaniaItems.lensNormal))
            .dependsOn(VARIOUS_RUNES));

    public static final Quest AURA_RING = CHAPTER.quest("aura_ring", q -> q
            .at(0.9375, 7.5)
            .desc(d -> d.line("装备时,{光环之戒}会持续缓慢生成魔力,并将魔力注入背包内的储魔物品中.", GREEN))
            .task(Task.item(BotaniaItems.auraRing))
            .dependsOn(VARIOUS_RUNES)
            .hideDependencyLines(false));

    public static final Quest AURA_RING_GREATER = CHAPTER.quest("aura_ring_greater", q -> q
            .at(0.9375, 8.75)
            .desc(d -> d.line("用泰拉钢锭强化{光环之戒}{可大幅提升该戒指的魔力生成速率.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.auraRingGreater))
            .dependsOn(AURA_RING));

    public static final Quest BREWERY = CHAPTER.quest("brewery", q -> q
            .at(-1.5625, 7.5)
            .desc(d -> d.line("在{植物酿造}{台中使用药瓶、魔力和试剂,可酿造出与药水功效相似的}{魔法药酿}.", GREEN, WHITE, BLUE))
            .task(Task.item(BotaniaBlocks.brewery))
            .dependsOn(VARIOUS_RUNES)
            .optional());

    public static final Quest INCENSE_STICK = CHAPTER.quest("incense_stick", q -> q
            .at(-1.5625, 8.75)
            .desc(d -> d.line("{熏香}{可在}{植物酿造}{台中灌注药酿.将其置于}{香盘}{上用打火石点燃,能以香盘为中心30格半径范围释放药效,持续时间比液态药酿延长六十倍.}", DARK_GREEN, WHITE, GREEN, WHITE, BLUE, WHITE))
            .task(Task.item(BotaniaItems.incenseStick))
            .dependsOn(BREWERY)
            .optional());

    public static final Quest LENS_SPEED = CHAPTER.quest("lens_speed", q -> q
            .at(-1, 9.5)
            .desc(d -> d.line("以降低初始容量和加速魔力损耗为代价,大幅提升{魔力脉冲}{的移动速度.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensSpeed))
            .dependsOn(AMPLIFY_MANA_SPREADER));

    public static final Quest LENS_POWER = CHAPTER.quest("lens_power", q -> q
            .at(1.75, 9.75)
            .desc(d -> d.line("使{魔力脉冲}{的魔力承载量翻倍,但会降低传输速度并增加长距离传输时的魔力损耗.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensPower))
            .dependsOn(AMPLIFY_MANA_SPREADER));

    public static final Quest LENS_TIME = CHAPTER.quest("lens_time", q -> q
            .at(0.75, 9.75)
            .desc(d -> d.line("显著延长{魔力脉冲}{开始损耗前的持续时间,但同时会降低脉冲移动速度.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensTime))
            .dependsOn(AMPLIFY_MANA_SPREADER));

    public static final Quest LENS_EFFICIENCY = CHAPTER.quest("lens_efficiency", q -> q
            .at(-2.25, 9.5)
            .desc(d -> d.line("缩短{魔力脉冲}{开始损耗魔力的启动时间,但同时会减缓损耗速率.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensEfficiency))
            .dependsOn(AMPLIFY_MANA_SPREADER));

    public static final Quest LENS_BOUNCE = CHAPTER.quest("lens_bounce", q -> q
            .at(-0.25, 11)
            .desc(d -> d.line("允许{魔力脉冲}{在接触方块表面时产生反弹效果.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensBounce))
            .dependsOn(AMPLIFY_MANA_SPREADER));

    public static final Quest LENS_GRAVITY = CHAPTER.quest("lens_gravity", q -> q
            .at(0.25, 12.75)
            .desc(d -> d.line("使重力影响{魔力脉冲}{运动轨迹形成抛物线,同时略微延长脉冲开始损耗前的持续时间.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensGravity))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_MINE = CHAPTER.quest("lens_mine", q -> q
            .at(-0.75, 11.75)
            .desc(d -> d.line("允许{魔力脉冲}{消耗自身魔力击穿障碍方块.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensMine))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_DAMAGE = CHAPTER.quest("lens_damage", q -> q
            .at(-1.75, 11.25)
            .desc(d -> d.line("允许{魔力脉冲}{消耗自身魔力对击中的生物造成伤害.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensDamage))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_PHANTOM = CHAPTER.quest("lens_phantom", q -> q
            .at(-3, 11)
            .desc(d -> d.line("使{魔力脉冲}{具备穿透方块的能力,但会缩短脉冲存活时间.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensPhantom))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_MAGNET = CHAPTER.quest("lens_magnet", q -> q
            .at(0.25, 11.75)
            .desc(d -> d.line("使{魔力脉冲}{自动追踪附近可接收魔力的方块,同时略微降低脉冲移动速度.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensMagnet))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_EXPLOSIVE = CHAPTER.quest("lens_explosive", q -> q
            .at(1.25, 11.25)
            .desc(d -> d.line("为{魔力脉冲}{注入熵能——简而言之,当击中无法接收魔力的物体时会引发爆炸.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensExplosive))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_INFLUENCE = CHAPTER.quest("lens_influence", q -> q
            .at(2.25, 10.75)
            .desc(d -> d.line("使{魔力脉冲}{影响范围内的掉落物、经验球和下坠方块,使其运动轨迹与脉冲保持完全同步.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensInfluence))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_WEIGHT = CHAPTER.quest("lens_weight", q -> q
            .at(-1.75, 12.25)
            .desc(d -> d.line("当该透镜的{魔力脉冲}{击中方块时,会使该方块像沙砾般下坠.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensWeight))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_MESSENGER = CHAPTER.quest("lens_messenger", q -> q
            .at(1.25, 12.25)
            .desc(d -> d.line("大幅减少{魔力脉冲}{携带的魔力总量,同时极大提升脉冲速度和射程.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensMessenger))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_LIGHT = CHAPTER.quest("lens_light", q -> q
            .at(-2.75, 12)
            .desc(d -> d.line("使{魔力脉冲}{在命中方块时生成装饰性火焰.此火焰仅提供光照效果,可被其他}{魔力脉冲}{扑灭.}", GREEN, WHITE, GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensLight))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_PISTON = CHAPTER.quest("lens_piston", q -> q
            .at(2.25, 11.75)
            .desc(d -> d.line("允许{魔力脉冲}{如同活塞般推动方块.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensPiston))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest LENS_FIRE = CHAPTER.quest("lens_fire", q -> q
            .at(-0.75, 12.75)
            .desc(d -> d.line("使{魔力脉冲}{能够点燃方块,但对生物无效.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensFire))
            .dependsOn(AMPLIFY_MANA_SPREADER)
            .optional().hideDependencyLines());

    public static final Quest CRAFT_RUNEROCK = CHAPTER.quest("craft_runerock", q -> q
            .at(-0.3125, 3.75)
            .title("制作符文石")
            .desc(d -> d.line("希望你玩的开心"))
            .task(Task.item(TagPrefix.block, GTOMaterials.Runerock))
            .dependsOn(VARIOUS_RUNES));

    public static final Quest TERRA_PLATE = CHAPTER.quest("terra_plate", q -> q
            .at(-0.3125, 1.875)
            .desc(d -> d.line("以棋盘格形式摆放5块活石与4块青金石，将凝聚板置于中央活石上即可完成构建"))
            .task(Task.item(BotaniaBlocks.terraPlate))
            .dependsOn(CRAFT_RUNEROCK));

    public static final Quest CRAFT_TERRASTEEL_THAUMIUM = CHAPTER.quest("craft_terrasteel_thaumium", q -> q
            .at(2.1875, 1.875)
            .title("{合成} {泰拉钢 神秘}", GREEN, LIGHT_PURPLE)
            .desc(d -> d
                    .line("泰拉钢合成需海量魔力，须使用{火花}系统。在{魔力池}{上方布置若干火花，再于凝聚板上方安置火花引导魔力}", BLUE, GREEN, WHITE)
                    .line()
                    .line("{平台就绪后，向凝聚板投入输入物品即可开始合成}", WHITE))
            .task(Task.item(BotaniaItems.terrasteel), Task.item(TagPrefix.ingot, GTOMaterials.Thaumium))
            .dependsOn(TERRA_PLATE));

    public static final Quest OPEN_WARP_GATE = CHAPTER.quest("open_warp_gate", q -> q
            .at(2.1875, 5.625)
            .title("开启传送门")
            .task(
                    Task.item(BotaniaBlocks.alfPortal),
                    Task.item(BotaniaBlocks.naturaPylon, 2),
                    Task.item(BotaniaBlocks.livingwoodLogGlimmering, 3),
                    Task.item(BotaniaBlocks.livingwood, 8))
            .dependsOn(CRAFT_TERRASTEEL_THAUMIUM));

    public static final Quest ELVEN_TRADE = CHAPTER.quest("elven_trade", q -> q
            .at(5.625, 5.625).minWidth(300)
            .icon(BotaniaItems.dragonstone)
            .title("精灵交易")
            .subtitle("不要向里面投喂面包！")
            .desc(d -> d
                    .line("虽然无法进入{精灵传送门}，但可通过向门内投掷特定物品进行{精灵交易}{，每次交易均会消耗魔力!}", LIGHT_PURPLE, DARK_GREEN, WHITE)
                    .line()
                    .line("{材料可用于升级装备，例如制作}{精灵魔力发射器}", WHITE, DARK_GREEN)
                    .line()
                    .line("{将}{植物魔法辞典}{投入传送门可升级为附有}{精灵学}{的版本，获取更多}{植物魔法}{进阶知识}", WHITE, GREEN, WHITE, YELLOW, WHITE, LIGHT_PURPLE, WHITE))
            .task(
                    Task.item(BotaniaItems.lexicon).nbt("{\"botania:elven_unlock\":1b}").matchNbt(true),
                    Task.item(BotaniaItems.elementium),
                    Task.item(BotaniaItems.dragonstone),
                    Task.item(BotaniaItems.pixieDust),
                    Task.item(BotaniaBlocks.dreamwood),
                    Task.item(BotaniaBlocks.elfGlass))
            .dependsOn(OPEN_WARP_GATE));

    public static final Quest CONJURATION_CATALYST = CHAPTER.quest("conjuration_catalyst", q -> q
            .at(4.375, 7.5)
            .desc(d -> d.line("与{炼金催化器}{类似,}{炼造催化器}置于{魔力池}{下方时,可解锁循环转化配方.}", GREEN, WHITE, GREEN, GREEN, WHITE))
            .task(Task.item(BotaniaBlocks.conjurationCatalyst))
            .dependsOn(ELVEN_TRADE));

    public static final Quest SPARK_ENHANCEMENT = CHAPTER.quest("spark_enhancement", q -> q
            .at(3.125, 7.5).minWidth(300)
            .title("火花增强")
            .desc(d -> d
                    .line("借助精灵材料,可使用{增强核心}升级火花.手持增强核心{右键点击}{火花即可升级.潜行状态下}{右键点击}{火花可}{清除}{增强效果.}", LIGHT_PURPLE, GREEN, WHITE, GREEN, WHITE, GREEN, WHITE)
                    .line()
                    .line("{扩散增强:将自身储罐魔力分配给附近非增强型或逸散型火花.}", WHITE)
                    .line()
                    .line("{聚集增强:强制吸收非增强型火花储罐中的魔力.}", WHITE)
                    .line()
                    .line("{弥散增强:使火花从其储罐抽取魔力,为附近玩家充能魔力容器.}", WHITE)
                    .line()
                    .line("{隔绝增强:阻止该火花与支配/隐性增强型火花交互.}", WHITE))
            .task(
                    Task.anyOf(1, BotaniaItems.sparkUpgradeDispersive, BotaniaItems.sparkUpgradeDominant, BotaniaItems.sparkUpgradeRecessive, BotaniaItems.sparkUpgradeIsolated).title("火花增幅器"))
            .dependsOn(ELVEN_TRADE));

    public static final Quest MULTIMEDIA_NETWORK = CHAPTER.quest("multimedia_network", q -> q
            .at(5, 9.375).minWidth(300)
            .title("{多媒体}{网络}", GREEN, WHITE)
            .desc(d -> d
                    .line("通过{植物魔法}{,您可使用}{多媒体火花}在容器上方构建{多媒体网络}.", LIGHT_PURPLE, WHITE, GREEN, LIGHT_PURPLE)
                    .line()
                    .line("网络需至少一个{主节点} {多媒体火花}作为核心,随后可通过任意数量{多媒体火花}{扩展.同色火花会在8格范围内自动连接形成物品网络.}", AQUA, GREEN, GREEN, WHITE)
                    .line()
                    .line("{火花仅能识别其正下方容器的物品,且仅能通过顶部存取.所有网络内物品均可被漏斗、索引等多媒体组件访问.}", WHITE))
            .task(Task.item(BotaniaItems.corporeaSpark), Task.item(BotaniaItems.corporeaSparkMaster))
            .dependsOn(ELVEN_TRADE));

    public static final Quest CORPOREA_FUNNEL = CHAPTER.quest("corporea_funnel", q -> q
            .at(4.375, 10.625)
            .desc(d -> d
                    .line("{多媒体漏斗}是物品索引系统的简化版,可通过红石信号向网络请求物品.", GREEN)
                    .line()
                    .line("配置请求物品时,需在方块上的物品展示框内放置对应物品.若存在多个展示框,漏斗将随机选择其一.旋转展示框内物品可调整请求数量.")
                    .line()
                    .line("更多详细信息请查阅{辞典} {植物魔法}.", GREEN, LIGHT_PURPLE))
            .task(Task.item(BotaniaBlocks.corporeaFunnel))
            .dependsOn(MULTIMEDIA_NETWORK));

    public static final Quest CORPOREA_INDEX = CHAPTER.quest("corporea_index", q -> q
            .at(3.125, 9.375)
            .desc(d -> d
                    .line("{多媒体索引}是访问{多媒体网络}的交互界面,可调用其上{多媒体火花}{构建的网络资源.}", GREEN, BLUE, GREEN, WHITE)
                    .line()
                    .line("{使用时靠近索引,它将截获附近玩家的}{聊天栏指令}{.玩家可通过输入指令请求物品,例如输入}{10 }{铁锭},若网络内存有该物品则会自动发放.", WHITE, GREEN, WHITE, AQUA, GREEN)
                    .line()
                    .line("更多操作指南请查阅{辞典} {植物魔法}.", GREEN, LIGHT_PURPLE))
            .task(Task.item(BotaniaBlocks.corporeaIndex))
            .dependsOn(MULTIMEDIA_NETWORK));

    public static final Quest CORPOREA_CRYSTAL_CUBE = CHAPTER.quest("corporea_crystal_cube", q -> q
            .at(5.625, 10.625)
            .desc(d -> d.line("{多媒体水晶魔方}用于显示上方火花所属网络中指定物品的总量,使用方式为手持该物品{右击}{魔方.}", GREEN, GREEN, WHITE))
            .task(Task.item(BotaniaBlocks.corporeaCrystalCube))
            .dependsOn(MULTIMEDIA_NETWORK)
            .optional());

    public static final Quest LENS_PAINT = CHAPTER.quest("lens_paint", q -> q
            .at(1.25, 13.25)
            .desc(d -> d
                    .line("需先进行染色处理.")
                    .line()
                    .line("允许{魔力脉冲}{为击中的可染色方块(包括连接的方块)及绵羊进行染色.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensPaint))
            .dependsOn(AMPLIFY_MANA_SPREADER, ELVEN_TRADE)
            .optional().hideDependencyLines());

    public static final Quest LENS_FIREWORK = CHAPTER.quest("lens_firework", q -> q
            .at(-0.75, 13.75)
            .desc(d -> d.line("使{魔力脉冲}{在击中方块时发射节日烟花.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensFirework))
            .dependsOn(AMPLIFY_MANA_SPREADER, ELVEN_TRADE)
            .optional().hideDependencyLines());

    public static final Quest LENS_FLARE = CHAPTER.quest("lens_flare", q -> q
            .at(-1.75, 13.25)
            .desc(d -> d.line("该组件可将{魔力发射器}{的脉冲模式转换为仅发射连续粒子流,而非短促脉冲.不消耗魔力.非常适合装饰用途.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensFlare))
            .dependsOn(AMPLIFY_MANA_SPREADER, ELVEN_TRADE)
            .optional().hideDependencyLines());

    public static final Quest LENS_TRIPWIRE = CHAPTER.quest("lens_tripwire", q -> q
            .at(-2.75, 13)
            .desc(d -> d.line("在魔力扩散器上使用此组件时,仅当{魔力脉冲}{能命中生物或玩家时才会发射.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensTripwire))
            .dependsOn(AMPLIFY_MANA_SPREADER, ELVEN_TRADE)
            .optional().hideDependencyLines());

    public static final Quest LENS_REDIRECT = CHAPTER.quest("lens_redirect", q -> q
            .at(0.25, 13.75)
            .desc(d -> d.line("该棱镜会重定向任何与其碰撞的{魔力发射器}{或实体,使其朝向发射脉冲的方块或实体.}", GREEN, WHITE))
            .task(Task.item(BotaniaItems.lensRedirect))
            .dependsOn(AMPLIFY_MANA_SPREADER, ELVEN_TRADE)
            .optional().hideDependencyLines());

    public static final Quest LENS_WARP = CHAPTER.quest("lens_warp", q -> q
            .at(2.25, 12.75)
            .desc(d -> d.line("请查阅{辞典} {植物魔法}了解此棱镜的用法.", GREEN, LIGHT_PURPLE))
            .task(Task.item(BotaniaItems.lensWarp))
            .dependsOn(AMPLIFY_MANA_SPREADER, ELVEN_TRADE)
            .optional().hideDependencyLines());

    public static final Quest MYTHIC_BOTANY = CHAPTER.quest("mythic_botany", q -> q
            .at(7.5, 5.625)
            .title("神话植物学")
            .desc(d -> d
                    .line("探索{猪灵堡垒}和{地牢}（如锻造厂）有概率获取下界合金锭", YELLOW, YELLOW)
                    .line()
                    .line("使用{陨星之板}可以稳定获取下界合金锭", GOLD)
                    .line()
                    .line("亦或者到达LuV使用{中子活化器}制作下界合金粉", GOLD))
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.ASGARD_RUNE))
            .dependsOn(ELVEN_TRADE));

    public static final Quest MYTHIC_COALESCENCE_PLATE = CHAPTER.quest("mythic_coalescence_plate", q -> q
            .at(9.375, 5.625)
            .title("神话凝聚板")
            .desc(d -> d
                    .line("以5块微光石构成棋盘格基底,其余4格放置金块,最后在中央微光石上安置精灵凝聚板.")
                    .line()
                    .line("类似泰拉钢聚合,需用{火花}{传输魔力.火花需装备支配增强方可运作灌注器.}", GREEN, WHITE)
                    .line()
                    .line("{现在能生产精灵钢!}", WHITE))
            .task(
                    Task.item(QuestExternalRefs.MythicBotanyItems.MANA_INFUSER),
                    Task.item(BotaniaBlocks.shimmerrock, 5),
                    Task.item(Items.GOLD_BLOCK, 4))
            .dependsOn(MYTHIC_BOTANY));

    public static final Quest ALFSTEEL = CHAPTER.quest("alfsteel", q -> q
            .at(11.25, 5.625)
            .title("精灵钢")
            .desc(d -> d
                    .line("将龙晶、{精灵尘}{与}{源质钢锭}{结合,并消耗1.5池魔力,即可获得精灵钢!}", GREEN, WHITE, GREEN, WHITE)
                    .line()
                    .line("{获取}{锻造模板}{需用}{源质钢剑}{等植魔武器击杀女巫或唤魔者.}", WHITE, GREEN, WHITE, GREEN, WHITE))
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.ALFSTEEL_INGOT))
            .dependsOn(MYTHIC_COALESCENCE_PLATE));

    public static final Quest CYCLE_OF_BLOSSOMS_SOLVENT_BUCKET = CHAPTER.quest("cycle_of_blossoms_solvent_bucket", q -> q
            .at(7.5, 3.75)
            .icon(GTOMaterials.CycleofBlossomsSolvent.getBucket())
            .task(Task.item(GTOMaterials.CycleofBlossomsSolvent.getBucket()).optional(), Task.checkmark())
            .dependsOn(ELVEN_TRADE, Magic1Chapter.FRACTAL_PETAL_SOLVENT_BUCKET));

    public static final Quest GAIASTEEL_INGOT = CHAPTER.quest("gaiasteel_ingot", q -> q
            .at(13.125, 5.625)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Gaiasteel))
            .dependsOn(ALFSTEEL));

    public static final Quest WHITE_WAX_INGOT = CHAPTER.quest("white_wax_ingot", q -> q
            .at(11.25, 0)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.WhiteWax))
            .dependsOn(Magic1Chapter.LIVINGROCK)
            .hideDependencyLines());

    public static final Quest TIME_TWISTER = CHAPTER.quest("time_twister", q -> q
            .at(-2.8125, 2.5)
            .subtitle("比床更加黑科技的时间机器")
            .desc(d -> d
                    .line("GTO集团汲取盖亚的能量，研发出了跳跃时间的技术，就汇聚在这一个小小的瓶子中。")
                    .line()
                    .line("时间扭曲者可以快速地加速机器的加工进程，但你需要保证电网足够，你也不希望看到机器全面跳电罢工吧！"))
            .task(Task.item(GTOItems.TIME_TWISTER))
            .dependsOn(GAIASTEEL_INGOT, EvChapter.HV_FIELD_GENERATOR)
            .hideDependencyLines());

    public static final Quest BASE_MANA_DISTRIBUTOR = CHAPTER.quest("base_mana_distributor", q -> q
            .at(0.9375, 3.75)
            .desc(d -> d
                    .line("火花的替代品")
                    .line()
                    .line("它的进阶版本范围更大，且能开启无线魔力模式。无线魔力网络储量无限！"))
            .task(Task.item(ManaMultiBlock.BASE_MANA_DISTRIBUTOR))
            .dependsOn(VARIOUS_RUNES));

    public static final Quest ELEMENTIUM_CASING = CHAPTER.quest("elementium_casing", q -> q
            .at(7.5, 7.5)
            .task(Task.item(GTOBlocks.ELEMENTIUM_CASING))
            .dependsOn(ELVEN_TRADE));

    public static final Quest HV_MANA_MACHINE_HULL = CHAPTER.quest("hv_mana_machine_hull", q -> q
            .at(7.5, 9.375)
            .subtitle("{你需要天马行空般的想象力}", GOLD)
            .task(Task.item(ManaMachine.MANA_HULL[GTValues.HV]))
            .dependsOn(ELEMENTIUM_CASING));

    public static final Quest ELF_EXCHANGE = CHAPTER.quest("elf_exchange", q -> q
            .at(9.375, 7.5)
            .task(Task.item(ManaMultiBlock.ELF_EXCHANGE))
            .dependsOn(HV_MANA_MACHINE_HULL, MYTHIC_BOTANY)
            .requireAny());

    public static final Quest INDUSTRIAL_ALTAR = CHAPTER.quest("industrial_altar", q -> q
            .at(9.375, 9.375)
            .task(Task.item(ManaMultiBlock.INDUSTRIAL_ALTAR))
            .dependsOn(HV_MANA_MACHINE_HULL));

    public static final Quest ALFSTEEL_CASING = CHAPTER.quest("alfsteel_casing", q -> q
            .at(11.25, 7.5)
            .task(Task.item(GTOBlocks.ALFSTEEL_CASING))
            .dependsOn(ALFSTEEL));

    public static final Quest EV_MANA_MACHINE_HULL = CHAPTER.quest("ev_mana_machine_hull", q -> q
            .at(11.25, 9.375)
            .subtitle("{你需要天马行空般的想象力}", DARK_PURPLE)
            .task(Task.item(ManaMachine.MANA_HULL[GTValues.EV]))
            .dependsOn(ALFSTEEL_CASING));

    public static final Quest GAIASTEEL_CASING = CHAPTER.quest("gaiasteel_casing", q -> q
            .at(13.125, 7.5)
            .task(Task.item(GTOBlocks.GAIASTEEL_CASING))
            .dependsOn(GAIASTEEL_INGOT));

    public static final Quest IV_MANA_MACHINE_HULL = CHAPTER.quest("iv_mana_machine_hull", q -> q
            .at(13.125, 9.375)
            .subtitle("{你需要天马行空般的想象力}", BLUE)
            .task(Task.item(ManaMachine.MANA_HULL[GTValues.IV]))
            .dependsOn(GAIASTEEL_CASING));

    public static final Quest MANA_CONDENSER = CHAPTER.quest("mana_condenser", q -> q
            .at(13.125, 11.25)
            .task(Task.item(ManaMultiBlock.MANA_CONDENSER))
            .dependsOn(IV_MANA_MACHINE_HULL));

    public static final Quest MANA_ALLOY_BLAST_SMELTER = CHAPTER.quest("mana_alloy_blast_smelter", q -> q
            .at(11.875, 11.25)
            .task(Task.item(ManaMultiBlock.MANA_ALLOY_BLAST_SMELTER))
            .dependsOn(EV_MANA_MACHINE_HULL));

    public static final Quest MANA_GREENHOUSE = CHAPTER.quest("mana_greenhouse", q -> q
            .at(10.625, 11.25)
            .task(Task.item(ManaMultiBlock.MANA_GREENHOUSE))
            .dependsOn(EV_MANA_MACHINE_HULL));

    public static final Quest SPIRIT_FUEL = CHAPTER.quest("spirit_fuel", q -> q
            .at(3.125, 12.5)
            .task(Task.item(ExtraBotanyItems.spiritFuel)));

    public static final Quest PEDESTAL_LIVINGROCK = CHAPTER.quest("pedestal_livingrock", q -> q
            .at(3.125, 11.25)
            .task(Task.item(ExtraBotanyBlocks.livingrockPedestal))
            .dependsOn(Magic1Chapter.LIVINGROCK));

    public static final Quest SPIRIT_FRAGMENT = CHAPTER.quest("spirit_fragment", q -> q
            .at(5, 11.875)
            .task(Task.item(ExtraBotanyItems.spiritFragment))
            .dependsOn(SPIRIT_FUEL, PEDESTAL_LIVINGROCK));

    public static final Quest LIGHT_AND_DARKNESS = CHAPTER.quest("light_and_darkness", q -> q
            .at(6.875, 11.25)
            .title("{光}  与  {暗}", WHITE, BLACK)
            .task(Task.item(ExtraBotanyItems.photonium), Task.item(ExtraBotanyItems.shadowium))
            .dependsOn(ELVEN_TRADE, SPIRIT_FRAGMENT));

    public static final Quest DIMENSION_CATALYST = CHAPTER.quest("dimension_catalyst", q -> q
            .at(8.75, 11.25)
            .task(Task.item(ExtraBotanyBlocks.dimensionCatalyst))
            .dependsOn(LIGHT_AND_DARKNESS));

    public static final Quest TERRASTEEL_CASING = CHAPTER.quest("terrasteel_casing", q -> q
            .at(3.75, 1.875)
            .task(Task.item(GTOBlocks.TERRASTEEL_CASING))
            .dependsOn(CRAFT_TERRASTEEL_THAUMIUM));

    public static final Quest MV_MANA_MACHINE_HULL = CHAPTER.quest("mv_mana_machine_hull", q -> q
            .at(5.625, 1.875)
            .subtitle("{你需要天马行空般的想象力}", AQUA)
            .task(Task.item(ManaMachine.MANA_HULL[GTValues.MV]))
            .dependsOn(TERRASTEEL_CASING));

    public static final Quest MANA_HEATER = CHAPTER.quest("mana_heater", q -> q
            .at(7.5, 1.875)
            .desc(d -> d.line("魔法使得它能够加热到2400度"))
            .task(Task.item(ManaMachine.MANA_HEATER))
            .dependsOn(MV_MANA_MACHINE_HULL));

    public static final Quest HERBS_INGOT = CHAPTER.quest("herbs_ingot", q -> q
            .at(9.375, 2.8125)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Herbs))
            .dependsOn(CYCLE_OF_BLOSSOMS_SOLVENT_BUCKET, MANA_HEATER));

    public static final Quest LAURERIL_INGOT = CHAPTER.quest("laureril_ingot", q -> q
            .at(11.25, 1.875)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Laureril))
            .dependsOn(HERBS_INGOT, WHITE_WAX_INGOT));

    public static final Quest ALFSTEEL_PYLON = CHAPTER.quest("alfsteel_pylon", q -> q
            .at(11.25, 3.75)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.ALFSTEEL_PYLON))
            .dependsOn(ALFSTEEL, HERBS_INGOT));

    public static final Quest SUMMON_GUARDIAN_OF_GAIA = CHAPTER.quest("summon_guardian_of_gaia", q -> q
            .at(13.125, 1.875).minWidth(300)
            .title("{召唤} {盖亚守护者}", GREEN, GREEN)
            .desc(d -> d
                    .line("要继续旅程,你需要获取{盖亚魂}.为此需进行{盖亚魔法仪式}.", GREEN, GREEN)
                    .line()
                    .line("需在{激活的信标}四周放置4枚{盖亚水晶}{,并准备一块泰拉钢锭.结构建成后,持泰拉钢锭在潜行状态下}{右击}{信标,准备迎接生死之战.}", GREEN, GREEN, WHITE, GREEN, WHITE))
            .task(Task.item(BotaniaBlocks.gaiaPylon, 4), Task.item(Items.BEACON))
            .dependsOn(GAIASTEEL_INGOT, LAURERIL_INGOT, ALFSTEEL_PYLON));

    public static final Quest GAIA = CHAPTER.quest("gaia", q -> q
            .at(15.625, 1.875)
            .icon(BotaniaBlocks.gaiaHead)
            .title("{盖亚魂}", GREEN)
            .desc(d -> d
                    .line("击败{盖亚守护者}后，你将获得{盖亚魂}作为战利品", GREEN, GREEN)
                    .line()
                    .line("游戏难度与参与仪式的人数将共同影响战利品的数量"))
            .task(Task.item(BotaniaItems.lifeEssence), Task.kill(BotaniaEntities.DOPPLEGANGER, 1).icon(BotaniaBlocks.gaiaHead))
            .dependsOn(SUMMON_GUARDIAN_OF_GAIA));

    public static final Quest GUARDIAN_OF_GAIA_2_0 = CHAPTER.quest("guardian_of_gaia_2_0", q -> q
            .at(15.625, 5.625)
            .title("{盖亚守护者}{ 2.0}", GREEN, WHITE)
            .subtitle("你需要更多{盖亚魂}！！", GREEN)
            .desc(d -> d
                    .line("将4个{盖亚魂}{与泰拉钢锭结合，用于激活盖亚的魔法仪式吧 :)}", GREEN, WHITE)
                    .line()
                    .line("{盖亚2.0将比1.0难度更高，但也会带来更加丰富的奖励}", WHITE))
            .task(Task.item(BotaniaItems.gaiaIngot))
            .dependsOn(GAIA));

    public static final Quest DICE_OF_FATE = CHAPTER.quest("dice_of_fate", q -> q
            .at(15.625, 9.375).iconScale(2)
            .title("{命运骰子}", GREEN)
            .desc(d -> d.line("使用盖亚锭召唤强化版{盖亚守护者}可获取更多{盖亚魂}，同时还有概率掉落{命运骰子}", GREEN, GREEN, GREEN))
            .task(Task.item(BotaniaItems.dice))
            .dependsOn(GUARDIAN_OF_GAIA_2_0)
            .hideDependentLines());

    public static final Quest THOR_RING = CHAPTER.quest("thor_ring", q -> q
            .at(26.7143, 4.1726).iconScale(2)
            .task(Task.item(BotaniaItems.thorRing))
            .dependsOn(DICE_OF_FATE));

    public static final Quest ODIN_RING = CHAPTER.quest("odin_ring", q -> q
            .at(26.25, 5.625).iconScale(2)
            .task(Task.item(BotaniaItems.odinRing))
            .dependsOn(DICE_OF_FATE));

    public static final Quest LOKI_RING = CHAPTER.quest("loki_ring", q -> q
            .at(27.7857, 3.0655).iconScale(2)
            .task(Task.item(BotaniaItems.lokiRing))
            .dependsOn(DICE_OF_FATE));

    public static final Quest INFINITE_FRUIT = CHAPTER.quest("infinite_fruit", q -> q
            .at(29.375, 8.75).iconScale(2)
            .task(Task.item(BotaniaItems.infiniteFruit))
            .dependsOn(DICE_OF_FATE));

    public static final Quest KING_KEY = CHAPTER.quest("king_key", q -> q
            .at(27.8125, 8.3631).iconScale(2)
            .task(Task.item(BotaniaItems.kingKey))
            .dependsOn(DICE_OF_FATE));

    public static final Quest FLUGEL_EYE = CHAPTER.quest("flugel_eye", q -> q
            .at(26.7054, 7.2917).iconScale(2)
            .task(Task.item(BotaniaItems.flugelEye))
            .dependsOn(DICE_OF_FATE));

    public static final Quest GAIA_EQUIPMENT_AND_ACCESSORIES = CHAPTER.quest("gaia_equipment_and_accessories", q -> q
            .at(14.375, 0)
            .title("{盖亚装备与饰品}", GREEN)
            .desc(d -> d
                    .line("诸多蕴含{盖亚魂}之力的饰品等待探索!记得查看详情!", GREEN)
                    .line()
                    .line("所有描述均可于{辞典} {植物魔法}中找到.", GREEN, LIGHT_PURPLE))
            .task(
                    Task.anyOf(1, BotaniaItems.blackHoleTalisman, BotaniaItems.spawnerMover, BotaniaItems.astrolabe, BotaniaItems.holyCloak, BotaniaItems.divaCharm, BotaniaItems.superLavaPendant, BotaniaItems.unholyCloak, BotaniaItems.laputaShard, BotaniaItems.superCloudPendant, BotaniaItems.flightTiara, BotaniaBlocks.manaBomb, BotaniaItems.missileRod, BotaniaItems.balanceCloak, BotaniaItems.superTravelBelt).title("盖亚饰品"))
            .dependsOn(GAIA)
            .optional());

    public static final Quest GAIA_SOLVENT_BUCKET = CHAPTER.quest("gaia_solvent_bucket", q -> q
            .at(18.125, 9.375)
            .icon(GTOMaterials.GaiaSolvent.getBucket())
            .task(Task.item(GTOMaterials.GaiaSolvent.getBucket()).optional(), Task.checkmark())
            .dependsOn(Magic1Chapter.FRACTAL_PETAL_SOLVENT_BUCKET, GUARDIAN_OF_GAIA_2_0));

    public static final Quest GAIA_SPIRIT_CYCLE = CHAPTER.quest("gaia_spirit_cycle", q -> q
            .at(20, 9.375)
            .title("盖亚魂循环")
            .desc(d -> d.line("通过击杀盖亚累计获取4个{盖亚之魂}以开启盖亚魂循环。", GOLD))
            .task(Task.item(GTOItems.UNSTABLE_GAIA_SOUL), Task.item(GTOItems.GAIA_CORE), Task.item(BotaniaItems.lifeEssence, 64))
            .dependsOn(GAIA_SOLVENT_BUCKET));

    public static final Quest MANA_GARDEN = CHAPTER.quest("mana_garden", q -> q
            .at(15.625, 0)
            .desc(d -> d.line("魔力花园能够提供大量的魔力"))
            .task(Task.item(ManaMultiBlock.MANA_GARDEN))
            .dependsOn(GAIA));

    public static final Quest ADVANCED_MANA_DISTRIBUTOR = CHAPTER.quest("advanced_mana_distributor", q -> q
            .at(16.875, 0)
            .desc(d -> d.line("火花的更强替代品，拥有无线魔力模式。"))
            .task(Task.item(ManaMultiBlock.ADVANCED_MANA_DISTRIBUTOR))
            .dependsOn(GAIA));

    public static final Quest LARGE_ALCHEMICAL_DEVICE = CHAPTER.quest("large_alchemical_device", q -> q
            .at(10, 0)
            .task(Task.item(ManaMultiBlock.LARGE_ALCHEMICAL_DEVICE))
            .dependsOn(HERBS_INGOT, MassiveManufacturingChapter.LARGE_BREWER));

    public static final Quest CHALLENGE_TICKET = CHAPTER.quest("challenge_ticket", q -> q
            .at(18.125, 5.625)
            .task(Task.item(ExtraBotanyItems.challengeTicket))
            .dependsOn(GUARDIAN_OF_GAIA_2_0));

    public static final Quest GUARDIAN_OF_GAIA_3_0 = CHAPTER.quest("guardian_of_gaia_3_0", q -> q
            .at(18.125, 3.75)
            .title("{盖亚守护者}  3.0", GREEN)
            .subtitle("你需要 {英雄般的力量}", YELLOW)
            .task(Task.item(ExtraBotanyItems.pandorasBox))
            .dependsOn(CHALLENGE_TICKET));

    public static final Quest QUICKSILVER_INGOT = CHAPTER.quest("quicksilver_ingot", q -> q
            .at(18.125, 7.5)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Quicksilver))
            .dependsOn(GUARDIAN_OF_GAIA_2_0));

    public static final Quest LIQUID_ELEMENTS = CHAPTER.quest("liquid_elements", q -> q
            .at(7.5, 0)
            .title("液态元素们")
            .task(
                    Task.item(GTOMaterials.Aether.getFluid(FluidStorageKeys.LIQUID).getBucket()),
                    Task.item(GTOMaterials.Salamander.getFluid(FluidStorageKeys.LIQUID).getBucket()),
                    Task.item(GTOMaterials.Undine.getFluid(FluidStorageKeys.LIQUID).getBucket()),
                    Task.item(GTOMaterials.Sylph.getFluid(FluidStorageKeys.LIQUID).getBucket()),
                    Task.item(GTOMaterials.Gnome.getFluid(FluidStorageKeys.LIQUID).getBucket()))
            .dependsOn(Magic1Chapter.ELEMENTAL_STEAMS, MANA_HEATER));

    public static final Quest AERIALITE_INGOT = CHAPTER.quest("aerialite_ingot", q -> q
            .at(-0.3125, 0)
            .task(Task.item(ExtraBotanyItems.aerialite))
            .dependsOn(Magic1Chapter.ENDER_AIR_BOTTLE, ELVEN_TRADE)
            .hideDependencyLines());

    public static final Quest CELESTIAL_CONDENSER = CHAPTER.quest("celestial_condenser", q -> q
            .at(2.1875, 0)
            .task(Task.item(ManaMachine.CELESTIAL_CONDENSER))
            .dependsOn(AERIALITE_INGOT));

    public static final Quest ASTRAL_SILVER_INGOT = CHAPTER.quest("astral_silver_ingot", q -> q
            .at(4.6875, 0)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AstralSilver), Task.item(GTOItems.HELIO_COAL))
            .dependsOn(CELESTIAL_CONDENSER));

    public static final Quest YGGDRASIL_BRANCH = CHAPTER.quest("yggdrasil_branch", q -> q
            .at(3.75, 3.75)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.YGGDRASIL_BRANCH))
            .dependsOn(CRAFT_TERRASTEEL_THAUMIUM));

    public static final Quest GJALLAR_HORN_FULL = CHAPTER.quest("gjallar_horn_full", q -> q
            .at(5.625, 3.75)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.GJALLAR_HORN_FULL))
            .dependsOn(YGGDRASIL_BRANCH)
            .hideDependentLines());

    public static final Quest ANIMIUM_BUCKET = CHAPTER.quest("animium_bucket", q -> q
            .at(6.875, 12.5)
            .icon(GTOMaterials.Animium.getBucket())
            .task(Task.item(GTOMaterials.Animium.getBucket()).optional(), Task.checkmark())
            .dependsOn(SPIRIT_FRAGMENT, GJALLAR_HORN_FULL));

    public static final Quest FLOWING_CIPHERS_BUCKET = CHAPTER.quest("flowing_ciphers_bucket", q -> q
            .at(8.75, 12.5)
            .icon(GTOMaterials.FlowingCiphers.getBucket())
            .task(Task.item(GTOMaterials.FlowingCiphers.getBucket()).optional(), Task.checkmark())
            .dependsOn(ANIMIUM_BUCKET));

    public static final Quest FIMBULTYR_TABLET = CHAPTER.quest("fimbultyr_tablet", q -> q
            .at(18.125, 11.25).iconScale(2)
            .desc(d -> d.line("饮下泉水后击杀盖亚2.0获取"))
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.FIMBULTYR_TABLET))
            .dependsOn(GUARDIAN_OF_GAIA_2_0, GJALLAR_HORN_FULL));

    public static final Quest FAIRY_WING = CHAPTER.quest("fairy_wing", q -> q
            .at(8.75, 0)
            .task(Task.item(GTOOrganItems.FAIRY_WING))
            .dependsOn(HERBS_INGOT));

    public static final Quest HERO_MEDAL = CHAPTER.quest("hero_medal", q -> q
            .at(20, 2.8125).iconScale(2)
            .subtitle("荣光的印记 无畏的象征")
            .task(Task.item(ExtraBotanyItems.heroMedal))
            .dependsOn(GUARDIAN_OF_GAIA_3_0));

    public static final Quest ORICHALCOS_INGOT = CHAPTER.quest("orichalcos_ingot", q -> q
            .at(21.875, 1.875)
            .subtitle("凝聚了盖亚的心血")
            .task(Task.item(ExtraBotanyItems.orichalcos))
            .dependsOn(HERO_MEDAL));

    public static final Quest ORICHALCOS_CASING = CHAPTER.quest("orichalcos_casing", q -> q
            .at(21.875, 3.75)
            .task(Task.item(GTOBlocks.ORICHALCOS_CASING))
            .dependsOn(ORICHALCOS_INGOT));

    public static final Quest LUV_MANA_MACHINE_HULL = CHAPTER.quest("luv_mana_machine_hull", q -> q
            .at(21.875, 5.625)
            .subtitle("{你需要天马行空般的想象力}", LIGHT_PURPLE)
            .task(Task.item(ManaMachine.MANA_HULL[GTValues.LuV]))
            .dependsOn(ORICHALCOS_CASING));

    public static final Quest AREA_DESTRUCTION_TOOLS = CHAPTER.quest("area_destruction_tools", q -> q
            .at(21.875, 7.5)
            .subtitle("人畜无害")
            .desc(d -> d
                    .line("或许你要...备份一下？")
                    .line()
                    .line("区域破坏者并不会产生爆炸，而是清空方块，也许能作为平地的好帮手"))
            .task(Task.item(ManaMachine.AREA_DESTRUCTION_TOOLS))
            .dependsOn(LUV_MANA_MACHINE_HULL));

    public static final Quest MANA_AMPLIFIER_HATCH = CHAPTER.quest("mana_amplifier_hatch", q -> q
            .at(23.75, 5.625)
            .subtitle("反哺科技")
            .desc(d -> d
                    .line("{格雷科技有限公司}{的}{神秘学家们}{在}{魔法}{发展到极高水平后研究出的跨界魔法成果。只要给机器提供足够的魔力，机器就无需担忧超频带来的能源损耗。}", DARK_AQUA.italic(), ITALIC, GOLD.italic(), ITALIC, AQUA.italic(), ITALIC)
                    .line()
                    .line("{魔力增幅仓内的UI可调整其缓存大小。开启批处理时，仍然只需要输入一份机器最大功率的魔力即可完成批处理。}", ITALIC))
            .task(Task.item(ManaMachine.MANA_AMPLIFIER_HATCH).optional(), Task.item(ManaMachine.ME_MANA_AMPLIFIER_HATCH).optional())
            .dependsOn(LUV_MANA_MACHINE_HULL, LuvChapter.INDIUM_TIN_BARIUM_TITANIUM_CUPRATE_INGOT)
            .optional());

    public static final Quest RUNE_ENGRAVING_CHAMBER = CHAPTER.quest("rune_engraving_chamber", q -> q
            .at(23.75, 3.75)
            .task(Task.item(ManaMultiBlock.RUNE_ENGRAVING_CHAMBER))
            .dependsOn(ORICHALCOS_CASING));

    public static final Quest HEROS_SOUL = CHAPTER.quest("heros_soul", q -> q
            .at(20, 5.625)
            .subtitle("不灭英灵")
            .task(Task.item(GTOItems.HEROS_SOUL))
            .dependsOn(HERO_MEDAL));

    public static final Quest HEROIC_CYCLE = CHAPTER.quest("heroic_cycle", q -> q
            .at(20, 7.5)
            .title("英雄循环")
            .desc(d -> d.line("刻印精粹需要用到神化中的{源初重构仪}", GOLD))
            .task(
                    Task.item(GTOItems.GOLD_MEDAL),
                    Task.item(GTOMaterials.HerosBrawlers.getBucket()),
                    Task.item(GTOMaterials.HerosBreach.getBucket()),
                    Task.item(GTOMaterials.HerosSplendor.getBucket()))
            .dependsOn(HEROS_SOUL, QUICKSILVER_INGOT, ApotheosisChapter.THE_PRIMORDIAL_RECONSTRUCTOR));

    public static final Quest PROSPECTOR_MANA_LV = CHAPTER.quest("prospector_mana_lv", q -> q
            .at(-2.1875, 0)
            .task(Task.item(GTOItems.PROSPECTOR_MANA_LV))
            .dependsOn(CRAFT_TERRASTEEL_THAUMIUM, LvChapter.LV_SENSOR, LvChapter.LV_EMITTER)
            .hideDependencyLines());

    public static final Quest PROSPECTOR_MANA_HV = CHAPTER.quest("prospector_mana_hv", q -> q
            .at(-3.75, 0)
            .task(Task.item(GTOItems.PROSPECTOR_MANA_HV))
            .dependsOn(ALFSTEEL, HvChapter.HV_LAUNCHER_AND_HV_SENSOR)
            .hideDependencyLines());

    public static final Quest REWARD_BAG = CHAPTER.quest("reward_bag", q -> q
            .at(18.125, 1.875)
            .title("奖励袋")
            .desc(d -> d.line("多余的命运骰子可以换成奖励袋"))
            .task(
                    Task.anyOf(1, ExtraBotanyItems.nineAndThreeQuartersRewardBag, ExtraBotanyItems.vierRewardBag, ExtraBotanyItems.dreiRewardBag, ExtraBotanyItems.zweiRewardBag, ExtraBotanyItems.einsRewardBag).title("任意奖励袋"))
            .dependsOn(GUARDIAN_OF_GAIA_3_0, DICE_OF_FATE)
            .requireAny()
            .hideDependentLines());

    public static final Quest VOID_ARCHIVES = CHAPTER.quest("void_archives", q -> q
            .at(29.375, 2.5).iconScale(2)
            .task(Task.item(ExtraBotanyItems.voidArchives))
            .dependsOn(REWARD_BAG));

    public static final Quest ME_MANA_INTERFACE = CHAPTER.quest("me_mana_interface", q -> q
            .at(23.75, 7.5)
            .task(Task.item(ManaMachine.AE_MANA_INTERFACE))
            .dependsOn(LUV_MANA_MACHINE_HULL));

    public static final Quest EXCALIBUR = CHAPTER.quest("excalibur", q -> q
            .at(31.0536, 3.1399).iconScale(2)
            .task(Task.item(ExtraBotanyItems.excalibur))
            .dependsOn(HERO_MEDAL)
            .hideDependencyLines());

    public static final Quest RHEIN_HAMMER = CHAPTER.quest("rhein_hammer", q -> q
            .at(32.0536, 4.1399).iconScale(2)
            .task(Task.item(ExtraBotanyItems.rheinHammer))
            .dependsOn(ORICHALCOS_INGOT)
            .hideDependencyLines());

    public static final Quest ACHILLES_SHIELD = CHAPTER.quest("achilles_shield", q -> q
            .at(32.125, 7.1399).iconScale(2)
            .task(Task.item(ExtraBotanyItems.achillesShield))
            .dependsOn(ORICHALCOS_INGOT)
            .hideDependencyLines());

    public static final Quest FAILNAUGHT = CHAPTER.quest("failnaught", q -> q
            .at(31.1161, 8.3854).iconScale(2)
            .task(Task.item(ExtraBotanyItems.failnaught))
            .dependsOn(ORICHALCOS_INGOT)
            .hideDependencyLines());

    public static final Quest RUNE_RITUALS = CHAPTER.quest("rune_rituals", q -> q
            .at(20, 11.25)
            .title("符文仪式")
            .desc(d -> d.line("将若干符文支托围绕1个大师符文支托摆成特定形状，然后在符文支托和大师符文支托上放上对应的物品，将合成物品丢进符文仪式的范围内即可"))
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.CENTRAL_RUNE_HOLDER), Task.item(QuestExternalRefs.MythicBotanyItems.RUNE_HOLDER))
            .dependsOn(FIMBULTYR_TABLET)
            .hideDependentLines());

    public static final Quest MJOELLNIR = CHAPTER.quest("mjoellnir", q -> q
            .at(32.5, 5.625).iconScale(2)
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.MJOELLNIR))
            .dependsOn(RUNE_RITUALS));

    public static final Quest RUNE_SUBSIDY = CHAPTER.quest("rune_subsidy", q -> q
            .at(-2.8125, 8.125)
            .icon(BotaniaItems.runeWater)
            .title("符文补助")
            .desc(d -> d.line("符文制作太繁琐了？这里免费送你一些"))
            .task(Task.checkmark())
            .easySubsidy(
                    Reward.item(BotaniaItems.runeWater, 2),
                    Reward.item(BotaniaItems.runeFire, 2),
                    Reward.item(BotaniaItems.runeEarth, 2),
                    Reward.item(BotaniaItems.runeAir, 2),
                    Reward.item(BotaniaItems.runeSpring, 2),
                    Reward.item(BotaniaItems.runeSummer, 2),
                    Reward.item(BotaniaItems.runeAutumn, 2),
                    Reward.item(BotaniaItems.runeWinter, 2),
                    Reward.item(BotaniaItems.runeMana, 2),
                    Reward.item(BotaniaItems.runeLust, 2),
                    Reward.item(BotaniaItems.runeGluttony, 2),
                    Reward.item(BotaniaItems.runeGreed, 2),
                    Reward.item(BotaniaItems.runeSloth, 2),
                    Reward.item(BotaniaItems.runeWrath, 2),
                    Reward.item(BotaniaItems.runeEnvy, 2),
                    Reward.item(BotaniaItems.runePride, 2))
            .dependsOn(Magic1Chapter.RUNIC_ALTAR)
            .optional().hideUntilDepsVisible());

    public static final Quest Q_43F6C913 = CHAPTER.quest("q_43f6c913", q -> q
            .at(8.125, 6.25)
            .task(Task.checkmark())
            .optional().invisible().disableToast());

    public static final Quest PHILOSOPHERS_STONE = CHAPTER.quest("philosophers_stone", q -> q
            .at(20, 0)
            .subtitle("先祖知识凝聚而成的结晶")
            .desc(d -> d.line("注意配方消耗的魔力，必须使用{导师}以上等级的魔力输入仓才可以提供足够的魔力", LIGHT_PURPLE))
            .task(Task.item(GTOItems.PHILOSOPHERS_STONE))
            .dependsOn(HERO_MEDAL)
            .hideDependentLines());

    public static final Quest KVASIR_MEAD = CHAPTER.quest("kvasir_mead", q -> q
            .at(23.75, 11.25).iconScale(2)
            .subtitle("精灵诗人的不传之秘")
            .desc(d -> d.line("喝下它，身体似乎会发生奇妙的变化..."))
            .task(Task.item(QuestExternalRefs.MythicBotanyItems.KVASIR_MEAD))
            .dependsOn(RUNE_RITUALS, PHILOSOPHERS_STONE)
            .hideDependentLines());

    public static final Quest VISIT_ALFHEIM = CHAPTER.quest("visit_alfheim", q -> q
            .at(29.375, 5.625)
            .subtitle("魔法线毕业")
            .desc(d -> d
                    .line("{迷雾笼罩的}{亚尔夫海姆}{即将开放准入 —— 唯有饮下}{「诗之蜜酒」}{、集齐 12 件}{远古遗物}{的行者，方能踏足这片秘境。任何规避准入规则的偷渡操作均不被允许，空间法则将自动击杀未满足条件的闯入者。}", ITALIC, AQUA.italic(), ITALIC, GOLD.italic(), ITALIC, GOLD.italic(), ITALIC)
                    .line()
                    .line("{满足以上条件，踏入放置在}{主维度}的{精灵门}，你可以进入到精灵维度。在精灵维度中，你可以挖掘魔法线的专属矿石", ITALIC, AQUA.italic(), GOLD))
            .task(Task.dimension(Dimension.ALFHEIM).title("{访问亚尔夫海姆}", LIGHT_PURPLE).icon(NaturesCompass.naturesCompass))
            .dependsOn(
                    LOKI_RING,
                    ODIN_RING,
                    KING_KEY,
                    INFINITE_FRUIT,
                    VOID_ARCHIVES,
                    FAILNAUGHT,
                    EXCALIBUR,
                    RHEIN_HAMMER,
                    MJOELLNIR,
                    ACHILLES_SHIELD,
                    THOR_RING,
                    FLUGEL_EYE,
                    KVASIR_MEAD));

    public static final Quest RESONANCE_FLOWER = CHAPTER.quest("resonance_flower", q -> q
            .at(23.75, 1.875)
            .subtitle("诞生于虚空之中的共鸣之花")
            .desc(d -> d.line("利用元素共鸣，处理{亚尔夫海姆}的珍贵矿物", AQUA))
            .task(Task.item(ManaMultiBlock.RESONANCE_FLOWER))
            .dependsOn(ORICHALCOS_INGOT, PHILOSOPHERS_STONE));

    public static final Quest COSMIC_CELESTIAL_SPIRE_OF_CONVERGENCE = CHAPTER.quest("cosmic_celestial_spire_of_convergence", q -> q
            .at(23.75, 0)
            .subtitle("凝聚群星 倒转虚空")
            .task(Task.item(ManaMultiBlock.COSMIC_CELESTIAL_SPIRE_OF_CONVERGENCE))
            .dependsOn(RESONANCE_FLOWER, UvChapter.GRAVI_STAR));

    public static final Quest SLOT_ENHANCER = CHAPTER.quest("slot_enhancer", q -> q
            .at(18.125, 0)
            .desc(d -> d.line("可以用来扩展饰品槽"))
            .task(Task.item(GTOItems.SLOT_ENHANCER))
            .dependsOn(PHILOSOPHERS_STONE));

    public static final Quest TRANSMUTATION_CATALYST = CHAPTER.quest("transmutation_catalyst", q -> q
            .at(21.875, 0)
            .task(Task.item(GTOBlocks.TRANSMUTATION_CATALYST))
            .dependsOn(PHILOSOPHERS_STONE));

    public static final Quest PULSE_MACHINE_MAINTENANCE_PEDESTAL = CHAPTER.quest("pulse_machine_maintenance_pedestal", q -> q
            .at(-3, 10)
            .subtitle("魔法的维护和清灰")
            .desc(d -> d
                    .line("如果你的前期发展了魔法，不妨考虑一下用它来自动化维护和清灰")
                    .line()
                    .line("HV阶段你可以解锁科技线提供的自动维护和掏灰机器，也就用不上它了"))
            .task(Task.item(ManaMultiBlock.PULSE_MACHINE_MAINTENANCE_PEDESTAL))
            .dependsOn(CRAFT_TERRASTEEL_THAUMIUM)
            .hideDependencyLines());

    static {
        CHAPTER.decorate(d -> d
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, 23.125, 11.25, 1.25, 1.25))
                .image(QuestImage.of(QuestTextures.CIRCUIT_SUPRACHRONAL_LUV, 19.375, -0.25, 1.25, 1.25))
                .link(QuestLink.of(Magic1Chapter.RUNIC_ALTAR, -2.8125, 5.625))
                .link(QuestLink.of(ApotheosisChapter.THE_PRIMORDIAL_RECONSTRUCTOR, 21.875, 9.375)));
    }
}
