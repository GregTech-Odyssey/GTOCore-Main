package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.MultiBlockI;
import com.gtocore.data.quest.QuestExternalRefs;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class LuvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("luv", c -> c
            .title("9. 剧差压(LuV)")
            .subtitle("Tier6-LuV")
            .icon(GTMultiMachines.FUSION_REACTOR[GTValues.LuV])
            .hideQuestsUntilDepsVisible());

    public static final Quest NAQUADAH_INGOT = CHAPTER.quest("naquadah_ingot", q -> q
            .at(3.5, -3.5)
            .desc(d -> d.line("搅拌低纯硅岩乳液"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Naquadah))
            .dependsOn(IvChapter.QUAKE_ORE_VEIN));

    public static final Quest ENRICHED_NAQUADAH_INGOT = CHAPTER.quest("enriched_naquadah_ingot", q -> q
            .at(5, -1)
            .desc(d -> d.line("电磁选矿洁净硅岩粉"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.NaquadahEnriched))
            .dependsOn(IvChapter.QUAKE_ORE_VEIN)
            .repeatable(false).hideDependencyLines());

    public static final Quest NAQUADAH_COIL_BLOCK = CHAPTER.quest("naquadah_coil_block", q -> q
            .at(3.5, 0.5)
            .task(Task.item(GTBlocks.COIL_NAQUADAH))
            .dependsOn(NAQUADAH_INGOT));

    public static final Quest INDIUM_TIN_BARIUM_TITANIUM_CUPRATE_INGOT = CHAPTER.quest("indium_tin_barium_titanium_cuprate_ingot", q -> q
            .at(3.5, 2.5)
            .subtitle("LuV超导")
            .task(Task.item(TagPrefix.ingot, GTMaterials.IndiumTinBariumTitaniumCuprate))
            .dependsOn(NAQUADAH_COIL_BLOCK));

    public static final Quest FLOCCULATION_PURIFICATION_UNIT = CHAPTER.quest("flocculation_purification_unit", q -> q
            .at(5.5, -3.5)
            .subtitle("3级“污染水”")
            .task(Task.item(MultiBlockB.FLOCCULATION_PURIFICATION_UNIT))
            .dependsOn(NAQUADAH_INGOT));

    public static final Quest FLOCCULENT_WATER_BUCKET = CHAPTER.quest("flocculent_water_bucket", q -> q
            .at(7.5, -3.5)
            .icon(GTOMaterials.FlocculentWater.getBucket())
            .task(Task.item(GTOMaterials.FlocculentWater.getBucket()).optional(), Task.checkmark())
            .dependsOn(FLOCCULATION_PURIFICATION_UNIT));

    public static final Quest UHPIC_CHIP = CHAPTER.quest("uhpic_chip", q -> q
            .at(9.5, -3.5)
            .task(Task.item(GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT))
            .dependsOn(FLOCCULENT_WATER_BUCKET));

    public static final Quest NAQUADAH_BOULE = CHAPTER.quest("naquadah_boule", q -> q
            .at(4.5, -5)
            .desc(d -> d.line("硅岩掺杂的单晶硅，能产出更多的晶圆的同时也解锁了ASoC芯片，其能够简化{纳米处理器}和{量子处理器}的制作", AQUA, GREEN))
            .task(Task.item(GTItems.NAQUADAH_BOULE))
            .dependsOn(NAQUADAH_INGOT));

    public static final Quest SILICA_CERAMIC_FLAKE = CHAPTER.quest("silica_ceramic_flake", q -> q
            .at(20.5, -5)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.SilicaCeramic))
            .dependsOn(ENRICHED_NAQUADAH_INGOT)
            .hideDependencyLines());

    public static final Quest TINY_POLONIUM_DUST = CHAPTER.quest("tiny_polonium_dust", q -> q
            .at(6, -2)
            .desc(d -> d.line("你需要搭建{裂变反应堆}，用离心{核废料}来获得一点点的钋", GOLD, GOLD))
            .task(Task.item(TagPrefix.dustTiny, GTMaterials.Polonium))
            .dependsOn(IvChapter.FISSION_REACTOR));

    public static final Quest BLOCK_CONVERSION_ROOM = CHAPTER.quest("block_conversion_room", q -> q
            .at(22, -1.5)
            .desc(d -> d
                    .line("你需要想办法在这个机器内部设计一套自动放置和挖掘方块的自动化")
                    .line()
                    .line("如果不知道从何下手，不妨在AE章节找找思路")
                    .line()
                    .line("{在UEV阶段你可以使用方块总线和转换模拟卡模拟这个流程}", GRAY.italic()))
            .task(Task.item(MultiBlockD.BLOCK_CONVERSION_ROOM))
            .dependsOn(EvChapter.QUANTUM_EYE));

    public static final Quest WELCOME_TO_THE_DRAMA_PRESSURE_STAGE = CHAPTER.quest("welcome_to_the_drama_pressure_stage", q -> q
            .at(1.5, -6.5)
            .title("欢迎来到{剧差压}阶段", LIGHT_PURPLE)
            .subtitle("{你需要天马行空般的想象力}", LIGHT_PURPLE)
            .desc(d -> d
                    .line("IV阶段解锁的大部分{大批量加工机器}都可以安装{8个}能源仓，这意味着你可以通过{升压}来直接加工本阶段的材料，而无需制造这个阶段的单方块机器——除了{组装机}，因为你需要一台{LuV级别的组装机}来制造{至少一个}{整体框架(LuV)}安装到你的大型组装厂", YELLOW, GREEN, YELLOW, GOLD, GOLD, AQUA, GOLD)
                    .line()
                    .line("还需要注意的是，你的{部件组装机}在这个阶段必须进行升级才能制作更高级的部件", GOLD)
                    .line()
                    .line("你的首要目标是制作{核聚变}", GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.LuV]))
            .reward(
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(IvChapter.RHODIUM_PLATED_PALLADIUM_INGOT, IvChapter.POLYPHENYLENE_SULFIDE_FOIL));

    public static final Quest POLYURETHANERESIN_BUCKET = CHAPTER.quest("polyurethaneresin_bucket", q -> q
            .at(25.5, -5.5)
            .icon(GTOMaterials.PolyurethaneResin.getBucket())
            .task(Task.item(GTOMaterials.PolyurethaneResin.getBucket()).optional(), Task.checkmark())
            .dependsOn(WELCOME_TO_THE_DRAMA_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest SILICON_NITRIDE_CERAMIC_FLAKE = CHAPTER.quest("silicon_nitride_ceramic_flake", q -> q
            .at(27.5, -5.5)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.SiliconNitrideCeramic))
            .dependsOn(POLYURETHANERESIN_BUCKET));

    public static final Quest DRAWING_TOWER = CHAPTER.quest("drawing_tower", q -> q
            .at(5.25, 4.5)
            .desc(d -> d.line("我强烈推荐你一上来就建造最高层的拉丝塔，它能极大加速拉丝速度"))
            .task(Task.item(MultiBlockG.DRAWING_TOWER))
            .dependsOn(WELCOME_TO_THE_DRAMA_PRESSURE_STAGE)
            .hideDependencyLines());

    public static final Quest SIMPLE_FIBER_OPTIC_ROUGH = CHAPTER.quest("simple_fiber_optic_rough", q -> q
            .at(7.5, 4.5)
            .subtitle("什么？这里也有序列组装？")
            .desc(d -> d.line("灰烬我相信你肯定有，而丹宁，你需要下界疣"))
            .task(Task.item(GTOItems.SIMPLE_FIBER_OPTIC_ROUGH))
            .dependsOn(DRAWING_TOWER));

    public static final Quest NORMAL_OPTICAL_PIPE = CHAPTER.quest("normal_optical_pipe", q -> q
            .at(9.5, 4.5)
            .subtitle("正如生活所见")
            .desc(d -> d.line("用于传输算力和数据"))
            .task(Task.item(GTBlocks.OPTICAL_PIPES[0]))
            .dependsOn(SIMPLE_FIBER_OPTIC_ROUGH));

    public static final Quest DATA_BANK = CHAPTER.quest("data_bank", q -> q
            .at(9.5, 2.5)
            .subtitle("并不是基于SQL")
            .desc(d -> d
                    .line("装配线上直接放一个数据访问仓显然不能满足这么多配方的需要，因此我们提供了{数据库}...", GOLD)
                    .line()
                    .line("数据库可以放很多{数据访问仓}，并通过{光学数据仓}给装配线提供数据！", GOLD, GOLD))
            .task(Task.item(GTResearchMachines.DATA_BANK))
            .dependsOn(NORMAL_OPTICAL_PIPE, EvChapter.DATA_ORB, INDIUM_TIN_BARIUM_TITANIUM_CUPRATE_INGOT));

    public static final Quest OPTICAL_DATA_HATCH = CHAPTER.quest("optical_data_hatch", q -> q
            .at(11, 3.5)
            .title("光学数据仓")
            .desc(d -> d
                    .line("使用光缆一对一连接源仓与靶仓")
                    .line()
                    .line("后续还有其无线版本"))
            .task(Task.item(GTResearchMachines.DATA_HATCH_TRANSMITTER), Task.item(GTResearchMachines.DATA_HATCH_RECEIVER))
            .dependsOn(NORMAL_OPTICAL_PIPE, DATA_BANK)
            .hideDependentLines());

    public static final Quest FALL_KING_INGOT = CHAPTER.quest("fall_king_ingot", q -> q
            .at(8, -2)
            .subtitle("没什么，我只是想提一下")
            .desc(d -> d
                    .line("Man! What can I say")
                    .line()
                    .line("{元素周期表真是让人着迷}", GRAY.italic()))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.FallKing))
            .dependsOn(IvChapter.YTTRIUM_OXIDE_DUST));

    public static final Quest NEUTRON_ACTIVATOR = CHAPTER.quest("neutron_activator", q -> q
            .at(7, -1)
            .task(Task.item(MultiBlockD.NEUTRON_ACTIVATOR))
            .dependsOn(TINY_POLONIUM_DUST, FALL_KING_INGOT));

    public static final Quest TRINIUM_INGOT = CHAPTER.quest("trinium_ingot", q -> q
            .at(6, 0.5)
            .desc(d -> d.line("硫化凯金粉高温反应"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Trinium))
            .dependsOn(NEUTRON_ACTIVATOR, NAQUADAH_COIL_BLOCK, ENRICHED_NAQUADAH_INGOT));

    public static final Quest NAQUADAH_ALLOY_INGOT = CHAPTER.quest("naquadah_alloy_ingot", q -> q
            .at(13.25, 0.5)
            .task(Task.item(TagPrefix.ingot, GTMaterials.NaquadahAlloy))
            .dependsOn(TRINIUM_INGOT)
            .hideDependencyLines());

    public static final Quest SUPERCONDUCTING_COIL = CHAPTER.quest("superconducting_coil", q -> q
            .at(9.5, 0.5)
            .desc(d -> d.line("很贵，在你有更高的电压等级后有更加节省凯金的配方"))
            .task(Task.item(GTBlocks.SUPERCONDUCTING_COIL))
            .dependsOn(TRINIUM_INGOT, INDIUM_TIN_BARIUM_TITANIUM_CUPRATE_INGOT));

    public static final Quest ENERGY_INJECTOR = CHAPTER.quest("energy_injector", q -> q
            .at(9.5, -1)
            .task(Task.item(MultiBlockC.ENERGY_INJECTOR))
            .dependsOn(SUPERCONDUCTING_COIL)
            .optional());

    public static final Quest FUSION_CASING = CHAPTER.quest("fusion_casing", q -> q
            .at(11.5, 0.5)
            .task(Task.item(GTBlocks.FUSION_CASING))
            .dependsOn(SUPERCONDUCTING_COIL));

    public static final Quest SUPERCRITICAL_STEAM_TURBINE = CHAPTER.quest("supercritical_steam_turbine", q -> q
            .at(1.5, -8)
            .task(Task.item(GeneratorMultiblock.SUPERCRITICAL_STEAM_TURBINE))
            .dependsOn(WELCOME_TO_THE_DRAMA_PRESSURE_STAGE)
            .optional());

    public static final Quest LUV_ELECTRIC_MOTOR = CHAPTER.quest("luv_electric_motor", q -> q
            .at(4.5, -6.5)
            .desc(d -> d
                    .line("这个阶段开始的部件需要装配线制作，并且需要解锁对应的科技节点")
                    .line()
                    .line("{前往}{科技树}{章节组来查看全新的研究线！}", YELLOW, link(TechtreeChapter.IV_SCANNER), YELLOW))
            .task(Task.item(GTItems.ELECTRIC_MOTOR_LuV))
            .dependsOn(WELCOME_TO_THE_DRAMA_PRESSURE_STAGE, IvChapter.ASSEMBLY_LINE));

    public static final Quest LUV_ASSEMBLER = CHAPTER.quest("luv_assembler", q -> q
            .at(7.5, -6.5)
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.LuV]))
            .dependsOn(LUV_ELECTRIC_MOTOR));

    public static final Quest LUV_ENERGY_INPUT_HATCH = CHAPTER.quest("luv_energy_input_hatch", q -> q
            .at(11.5, -6.5)
            .desc(d -> d
                    .line("这个阶段的升压很容易")
                    .line()
                    .line("{不过这也意味着有些事情变难了……}", GRAY))
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV]))
            .dependsOn(IvChapter.ASSEMBLY_LINE, LUV_ASSEMBLER));

    public static final Quest LUV_FUSION_REACTOR = CHAPTER.quest("luv_fusion_reactor", q -> q
            .at(11.5, -3.5)
            .subtitle("最初的甜甜圈")
            .desc(d -> d
                    .line("{聚变机械方块}对于现在来说实在是太贵了！最节省的方案是：1个{主机}，48个{聚变机械方块}，31个{聚变玻璃}，4个{超导线圈方块}，16个{输入仓}，16个{输出仓}，16个{能源仓}，{上下堆叠}可以节省大量机械方块！（在EMI多方块预览界面中点击P即可切换带有玻璃的形态）", GOLD, AQUA, AQUA, AQUA, AQUA, AQUA, AQUA, AQUA, GREEN)
                    .line()
                    .line("需要攒够一定的能量才能开机，如果停止运行会慢慢冷却，需要下一次攒够能量才会运行")
                    .line()
                    .line("{温馨小提示：你可以通过标记输出仓的过滤来保证只有一个仓会输出流体！}", GRAY.italic()))
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.LuV]))
            .dependsOn(UHPIC_CHIP, FUSION_CASING, LUV_ENERGY_INPUT_HATCH)
            .hideDependencyLines(false));

    public static final Quest EUROPIUM_INGOT = CHAPTER.quest("europium_ingot", q -> q
            .at(14, -3.5)
            .subtitle("第一个聚变金属")
            .desc(d -> d
                    .line("速度很慢，你可能需要挂机来获得你的第一批{铕}", GREEN)
                    .line()
                    .line("获得{三钛线圈}后你可以用{铕粉}直接烧制成{铕锭}，不要忘记更新你的产线", GOLD, YELLOW, YELLOW))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Europium))
            .dependsOn(LUV_FUSION_REACTOR));

    public static final Quest TRINIUM_COIL_BLOCK = CHAPTER.quest("trinium_coil_block", q -> q
            .at(15.5, -1.5)
            .desc(d -> d
                    .line("你应该已经发现了制作它需要{ZPM}电压，而这章还只是{LuV}...", ZPM, LUV)
                    .line()
                    .line("是的！不同章节之间并{不是严格的线性关系}，你完全可以同时进行当前阶段和下个阶段的任务！", YELLOW)
                    .line()
                    .line("想要完成这个任务，你必须先上到{ZPM}章节解锁{精英组装机 III}", ZPM, GOLD))
            .task(Task.item(GTBlocks.COIL_TRINIUM))
            .dependsOn(ZpmChapter.ZPM_ASSEMBLER, ZpmChapter.WELCOME_TO_THE_ZERO_PRESSURE_PHASE, EUROPIUM_INGOT));

    public static final Quest URANIUM_RHODIUM_DINAQUADIDE_INGOT = CHAPTER.quest("uranium_rhodium_dinaquadide_ingot", q -> q
            .at(15.5, 0.5)
            .subtitle("ZPM超导")
            .task(Task.item(TagPrefix.ingot, GTMaterials.UraniumRhodiumDinaquadide))
            .dependsOn(TRINIUM_COIL_BLOCK));

    public static final Quest CRYSTAL_CIRCUIT = CHAPTER.quest("crystal_circuit", q -> q
            .at(16.5, -3.5)
            .title("晶体电路")
            .desc(d -> d.line("{魔法}还是来了——如果你没有将魔法线进度推进到{精灵门}，你无法制作接下来的{晶体CPU}", LIGHT_PURPLE, AQUA, GOLD))
            .task(Task.item(GTItems.RAW_CRYSTAL_CHIP))
            .dependsOn(EUROPIUM_INGOT));

    public static final Quest NAQUADRIA_INGOT = CHAPTER.quest("naquadria_ingot", q -> q
            .at(17.5, -1.5)
            .desc(d -> d.line("做凯金粉的时候你应该顺手搞到了一点"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Naquadria))
            .dependsOn(TRINIUM_COIL_BLOCK));

    public static final Quest INTEGRAL_FRAMEWORK_LUV = CHAPTER.quest("integral_framework_luv", q -> q
            .at(7.5, -5)
            .subtitle("升级你的大型机器#2")
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_LUV))
            .dependsOn(LUV_ASSEMBLER));

    public static final Quest RAW_CRYSTAL_CHIP_PARTS = CHAPTER.quest("raw_crystal_chip_parts", q -> q
            .at(16.5, -5)
            .desc(d -> d.line("你需要自循环来实现晶体芯片的增殖"))
            .task(Task.item(GTItems.RAW_CRYSTAL_CHIP_PART))
            .dependsOn(CRYSTAL_CIRCUIT));

    public static final Quest HIGH_PERFORMANCE_COMPUTATION_ARRAY = CHAPTER.quest("high_performance_computation_array", q -> q
            .at(15.5, 4.5)
            .icon(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY)
            .subtitle("内置八张RTX 4090")
            .desc(d -> d
                    .line("研究站需要{算力供应}，因此你需要一个能够产生算力的{计算机}", YELLOW, GOLD)
                    .line()
                    .line("你需要替换空组件为{计算组件}和{冷却组件}才能让计算机开始工作", GOLD, GOLD)
                    .line()
                    .line("需要注意，{冷却组件}必须足够，否则HPCA{过热}就会{报废}！", AQUA, YELLOW, RED)
                    .line()
                    .line("我建议你直接使用{主动冷却组件}（需要使用{多氯联苯冷却液}）进行主动冷却，目前你能达到的最大算力为{16CWU}", GOLD, YELLOW, GREEN))
            .task(Task.item(GTResearchMachines.HIGH_PERFORMANCE_COMPUTING_ARRAY), Task.item(GTResearchMachines.HPCA_COMPUTATION_COMPONENT))
            .reward(Reward.item(GTMaterials.PCBCoolant.getBucket()))
            .dependsOn(URANIUM_RHODIUM_DINAQUADIDE_INGOT, NORMAL_OPTICAL_PIPE));

    public static final Quest RESEARCH_STATION = CHAPTER.quest("research_station", q -> q
            .at(15.5, 2.5)
            .icon(GTResearchMachines.RESEARCH_STATION)
            .subtitle("比学校机房更耐用")
            .desc(d -> d
                    .line("仅仅使用{扫描仪}已经不足以完成{更高级物品}的研究", GOLD, YELLOW)
                    .line()
                    .line("因此你需要一个{多方块研究站}！", GOLD)
                    .line()
                    .line("把待研究物品放入{物品支架}即可开始研究", GOLD)
                    .line()
                    .line("你需要注意研究所需{电压}和{算力}！", YELLOW, YELLOW)
                    .line()
                    .line("{如果不成型看看支架的方向}", GRAY.italic()))
            .task(Task.item(GTResearchMachines.RESEARCH_STATION), Task.item(GTResearchMachines.OBJECT_HOLDER))
            .dependsOn(HIGH_PERFORMANCE_COMPUTATION_ARRAY, DATA_BANK, URANIUM_RHODIUM_DINAQUADIDE_INGOT));

    public static final Quest COMPUTATIONAL_DATA_HATCH = CHAPTER.quest("computational_data_hatch", q -> q
            .at(17, 3.5)
            .title("算力数据仓")
            .desc(d -> d
                    .line("同样使用光缆一对一连接源仓与靶仓")
                    .line()
                    .line("后续还有其无线版本"))
            .task(Task.item(GTResearchMachines.COMPUTATION_HATCH_TRANSMITTER), Task.item(GTResearchMachines.COMPUTATION_HATCH_RECEIVER))
            .dependsOn(OPTICAL_DATA_HATCH, HIGH_PERFORMANCE_COMPUTATION_ARRAY, RESEARCH_STATION)
            .hideDependentLines());

    public static final Quest CRYSTAL_CPU = CHAPTER.quest("crystal_cpu", q -> q
            .at(18.5, -3.5)
            .desc(d -> d
                    .line("制作{刻蚀水晶芯片}需要{无重力环境}，这意味着你需要第三次前往空间站", GOLD, AQUA)
                    .line()
                    .line("但小空间站不够高，电力高炉放下后普通消声仓将无法工作，你需要{ME消声仓}来绕过小空间站的高度限制", GOLD)
                    .line()
                    .line("或者，建造{工业空间站}获取足够高的空间", GOLD))
            .task(Task.item(GTItems.CRYSTAL_CENTRAL_PROCESSING_UNIT))
            .dependsOn(CRYSTAL_CIRCUIT, Magic2Chapter.ELVEN_TRADE));

    public static final Quest CRYSTAL_PROCESSOR = CHAPTER.quest("crystal_processor", q -> q
            .at(20.5, -3.5)
            .title("{晶体处理器}", BLUE)
            .task(Task.item(GTItems.CRYSTAL_PROCESSOR_IV))
            .dependsOn(SILICA_CERAMIC_FLAKE, CRYSTAL_CPU));

    public static final Quest CRYSTAL_PROCESSOR_ASSEMBLY = CHAPTER.quest("crystal_processor_assembly", q -> q
            .at(20.5, -2)
            .title("{晶体处理器集群}", BLUE)
            .task(Task.item(GTItems.CRYSTAL_ASSEMBLY_LuV))
            .dependsOn(CRYSTAL_PROCESSOR));

    public static final Quest CRYSTAL_PROCESSOR_SUPERCOMPUTER = CHAPTER.quest("crystal_processor_supercomputer", q -> q
            .at(20.5, 0)
            .title("{晶体处理器超级计算机}", BLUE)
            .subtitle("更便宜的ZPM级电路")
            .task(Task.item(GTItems.CRYSTAL_COMPUTER_ZPM))
            .dependsOn(CRYSTAL_PROCESSOR_ASSEMBLY));

    public static final Quest CRYSTAL_PROCESSOR_MAINFRAME = CHAPTER.quest("crystal_processor_mainframe", q -> q
            .at(20.5, 2.5)
            .title("{晶体处理器主机}", BLUE)
            .subtitle("第一个UV级电路")
            .desc(d -> d.line("晶体系列是你至今最便宜的电路之一"))
            .task(Task.item(GTItems.CRYSTAL_MAINFRAME_UV))
            .dependsOn(CRYSTAL_PROCESSOR_SUPERCOMPUTER, RESEARCH_STATION));

    public static final Quest LARGE_CRYSTALLIZATION_CHAMBER = CHAPTER.quest("large_crystallization_chamber", q -> q
            .at(22, 1)
            .task(Task.item(MultiBlockG.LARGE_CRYSTALLIZATION_CHAMBER))
            .dependsOn(CRYSTAL_PROCESSOR_MAINFRAME));

    public static final Quest DURANIUM_INGOT = CHAPTER.quest("duranium_ingot", q -> q
            .at(10, -5)
            .desc(d -> d.line("下一个阶段要用到的重要金属，制作三钛也要大量用到它，早早开始准备"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Duranium))
            .dependsOn(LUV_FUSION_REACTOR));

    public static final Quest IV_DRONE = CHAPTER.quest("iv_drone", q -> q
            .at(2.5, -5)
            .subtitle("全频域覆盖")
            .task(Task.item(GTOItems.IV_DRONE))
            .dependsOn(NAQUADAH_INGOT)
            .hideDependentLines());

    public static final Quest SUPERCRITICAL_MEGA_STEAM_TURBINE = CHAPTER.quest("supercritical_mega_steam_turbine", q -> q
            .at(19, 1)
            .task(Task.item(GeneratorMultiblock.SUPERCRITICAL_MEGA_STEAM_TURBINE))
            .dependsOn(CRYSTAL_PROCESSOR_MAINFRAME)
            .optional());

    public static final Quest SUPERCRITICAL_STEAM_INPUT_HATCH = CHAPTER.quest("supercritical_steam_input_hatch", q -> q
            .at(19, 0)
            .subtitle("真的还有用吗")
            .desc(d -> d.line("继续提升蒸汽机器的配方电压一级，解锁更多次数的超频"))
            .task(Task.item(GTOMachines.SUPERCRITICAL_STEAM_INPUT_HATCH))
            .dependsOn(SUPERCRITICAL_MEGA_STEAM_TURBINE)
            .optional());

    public static final Quest CALORITE_INGOT = CHAPTER.quest("calorite_ingot", q -> q
            .at(1.5, 0.5)
            .desc(d -> d.line("从同为t4星球的{水星}上获得", GOLD))
            .task(Task.item(ModItems.CALORITE_INGOT.get()))
            .dependsOn(EvChapter.TIER_4_ROCKET, IvChapter.QUAKE_ORE_VEIN));

    public static final Quest TIER_6_ROCKET = CHAPTER.quest("tier_6_rocket", q -> q
            .at(1.5, 2.5)
            .icon(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_6_ROCKET.get())
            .desc(d -> d
                    .line("这个阶段的火箭不再解锁一些矿石，转而提升你的游玩体验：")
                    .line()
                    .line("你可以在解锁的三个星球上，用流体钻机钻取大量需求的{气态氟}和{气态氯}等流体，一劳永逸！", GOLD, GOLD))
            .task(Task.item(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_6_ROCKET.get()), Task.item(ModItems.CRYO_FUEL_BUCKET.get()))
            .dependsOn(INDIUM_TIN_BARIUM_TITANIUM_CUPRATE_INGOT, CALORITE_INGOT));

    public static final Quest HPCA_ADVANCED_COMPUTATION_COMPONENT = CHAPTER.quest("hpca_advanced_computation_component", q -> q
            .at(23.5, 2.5)
            .desc(d -> d
                    .line("解锁{高级计算组件}之后，你能达到的最高算力来到了{48CWU}", GOLD, GREEN)
                    .line()
                    .line("在下个阶段你就可以制作突破算力上限的设施"))
            .task(Task.item(GTResearchMachines.HPCA_ADVANCED_COMPUTATION_COMPONENT))
            .dependsOn(CRYSTAL_PROCESSOR_MAINFRAME));

    public static final Quest PRECISION_ASSEMBLER = CHAPTER.quest("precision_assembler", q -> q
            .at(25.5, 4.5)
            .desc(d -> d.line("别忘了{精密组装机}也可以执行{组装机配方}，而且不需要{整体框架}", GOLD, YELLOW, GOLD))
            .task(Task.item(MultiBlockA.PRECISION_ASSEMBLER))
            .dependsOn(HPCA_ADVANCED_COMPUTATION_COMPONENT));

    public static final Quest MAGNETIC_FLUID_GENERATOR = CHAPTER.quest("magnetic_fluid_generator", q -> q
            .at(23.5, 4.5)
            .desc(d -> d.line("可持续性发电机，在MK I阶段你可以使用氖等离子体作为原料"))
            .task(Task.item(GeneratorMultiblock.MAGNETIC_FLUID_GENERATOR))
            .dependsOn(PRECISION_ASSEMBLER)
            .optional());

    public static final Quest LUV_KUANGBIAO_ONE_GIANT_NUCLEAR_FUSION_REACTOR = CHAPTER.quest("luv_kuangbiao_one_giant_nuclear_fusion_reactor", q -> q
            .at(28, 4.5)
            .desc(d -> d
                    .line("并行版本的超强聚变堆！")
                    .line()
                    .line("有史以来你做过最贵的东西，但是这值得。它能够{极大幅}提升你聚变的{速度}。", RED, YELLOW)
                    .line()
                    .line("{你是不是在机器的超频模块描述中看到了几个新的仓室？它们在UV阶段才能够制造}", GRAY.italic()))
            .task(Task.item(MultiBlockD.KUANGBIAO_ONE_GIANT_NUCLEAR_FUSION_REACTOR))
            .dependsOn(PRECISION_ASSEMBLER)
            .requireAny());

    public static final Quest ISA_LINE = CHAPTER.quest("isa_line", q -> q
            .at(21.5, 4.5)
            .title("艾萨线")
            .desc(d -> d
                    .line("{艾}{萨}{线}，这是一条新的矿物处理线，你需要大量的松油和乙醇来搭建这条产线", GREEN, YELLOW, RED)
                    .line()
                    .line("可以跳过繁杂的产线直接产出铂处（没有钌），稀土，硅岩等一些稀有的粉")
                    .line()
                    .line("现在你有了两条矿物处理线，也许你需要想办法改进你的自动化来自动分流你的矿物！"))
            .task(Task.item(MultiBlockD.ISA_MILL), Task.item(MultiBlockA.INDUSTRIAL_FLOTATION_CELL), Task.item(MultiBlockA.VACUUM_DRYING_FURNACE))
            .dependsOn(HPCA_ADVANCED_COMPUTATION_COMPONENT));

    public static final Quest LARGE_VOID_MINER = CHAPTER.quest("large_void_miner", q -> q
            .at(23.5, 0.5)
            .subtitle("不仅仅是虚空")
            .desc(d -> d.line("大型虚空采矿能挖到一些你见不到的矿脉，用你的钻井液来换取它们吧！"))
            .task(Task.item(MultiBlockA.LARGE_VOID_MINER))
            .dependsOn(HPCA_ADVANCED_COMPUTATION_COMPONENT));

    public static final Quest ESSENCE = CHAPTER.quest("essence", q -> q
            .at(23.5, -1.5)
            .title("精华")
            .desc(d -> d.line("精准虚空采矿模式能挖到一些特殊的矿，但同时你也需要准备{精华}", GOLD))
            .task(Task.tag(QuestExternalRefs.TAG_GTOCORE_VEIN_ESSENCE, 1).title("任意精华"))
            .dependsOn(BLOCK_CONVERSION_ROOM, LARGE_VOID_MINER));

    public static final Quest TELLURIUM_DUST = CHAPTER.quest("tellurium_dust", q -> q
            .at(25.5, -1.5)
            .desc(d -> d
                    .line("碲是一种半金属元素，它们在世界分布松散，需要用虚空采矿才能找到")
                    .line()
                    .line("碲和若干碲化物是优良的磁共振半导体材料，你未来的共振紫晶会用到它的"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Tellurium))
            .dependsOn(ESSENCE));

    public static final Quest MAGNETO_RESONATIC_DUST = CHAPTER.quest("magneto_resonatic_dust", q -> q
            .at(27.5, -1.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.MagnetoResonatic))
            .dependsOn(TELLURIUM_DUST));

    public static final Quest CIRCUIT_COMPOUND_DUST = CHAPTER.quest("circuit_compound_dust", q -> q
            .at(25.5, -3.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.CircuitCompound))
            .dependsOn(TELLURIUM_DUST));

    public static final Quest IMPRINTED_RESONATIC_CIRCUIT_BOARD = CHAPTER.quest("imprinted_resonatic_circuit_board", q -> q
            .at(27.5, -3.5)
            .task(Task.item(GTOItems.IMPRINTED_RESONATIC_CIRCUIT_BOARD))
            .dependsOn(MAGNETO_RESONATIC_DUST, SILICON_NITRIDE_CERAMIC_FLAKE, CIRCUIT_COMPOUND_DUST));

    public static final Quest MAGNETIC_RESONANCE_CIRCUIT_SERIES = CHAPTER.quest("magnetic_resonance_circuit_series", q -> q
            .at(29.75, -3.5)
            .title("磁共振电路系列")
            .subtitle("性价比之王！")
            .desc(d -> d
                    .line("第一次见可能觉得有些贵，但随着不断升级电路板，磁共振的造价优势会越发显著！")
                    .line()
                    .line("你现在就可以做出最高LuV级别的磁共振电路"))
            .task(
                    Task.anyOf(1, GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.ULV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.LV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.MV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.HV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.EV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.IV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.LuV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UHV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UEV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UIV]).title("磁共振电路系列"))
            .dependsOn(IMPRINTED_RESONATIC_CIRCUIT_BOARD));

    public static final Quest LUV_FIELD_GENERATOR = CHAPTER.quest("luv_field_generator", q -> q
            .at(3.5, 4)
            .task(Task.item(GTItems.FIELD_GENERATOR_LuV))
            .dependsOn(INDIUM_TIN_BARIUM_TITANIUM_CUPRATE_INGOT));

    public static final Quest ADVANCED_DATA_ACCESS_HATCH = CHAPTER.quest("advanced_data_access_hatch", q -> q
            .at(16.5, 1.5)
            .task(Task.item(GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH))
            .dependsOn(RESEARCH_STATION));

    public static final Quest LARGE_CRACKER = CHAPTER.quest("large_cracker", q -> q
            .at(19, 4)
            .task(Task.item(MultiBlockA.LARGE_CRACKER))
            .dependsOn(CRYSTAL_PROCESSOR_MAINFRAME));

    public static final Quest PROSPECTOR_LUV = CHAPTER.quest("prospector_luv", q -> q
            .at(4.5, -8)
            .desc(d -> d.line("最顶级的探矿仪，极大增加了勘探的范围"))
            .task(Task.item(GTItems.PROSPECTOR_LuV))
            .dependsOn(LUV_ELECTRIC_MOTOR));

    public static final Quest VIRTUAL_COIN_MINER = CHAPTER.quest("virtual_coin_miner", q -> q
            .at(14.5, 1.5)
            .task(Task.item(MultiBlockI.VIRTUAL_COIN_MINER))
            .dependsOn(RESEARCH_STATION)
            .optional());

    public static final Quest CALL_FOR_DESIGNS = CHAPTER.quest("call_for_designs", q -> q
            .at(23.5, -7)
            .title("征集设计")
            .desc(d -> d
                    .line("欢迎玩家给LuV章节设计更好看的布线！")
                    .line()
                    .line("如您希望帮助我们设计布线，请将您设计好的LuV和其他涉及到的任务书文件一并投稿至我们的github issue#89")
                    .line()
                    .line("如采纳，我们将会把您一并加入co-author"))
            .task(Task.checkmark()));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(AeChapter.ME_PATTERN_BUFFER, 15, -5))
                .link(QuestLink.of(AeChapter.ME_STORAGE, 13, -5))
                .link(QuestLink.of(AeChapter.ME_PATTERN_BUFFER_PROXY, 25.5, 2.5))
                .link(QuestLink.of(AeChapter.ME_EXPORT_BUFFER, 7.5, -8))
                .link(QuestLink.of(AeChapter.SPRAY_CAN, 14, -6))
                .link(QuestLink.of(Magic2Chapter.MANA_AMPLIFIER_HATCH, 2, 4))
                .link(QuestLink.of(ZpmChapter.WELCOME_TO_THE_ZERO_PRESSURE_PHASE, 13.25, -1.5))
                .link(QuestLink.of(IvChapter.QUAKE_ORE_VEIN, 1.5, -3.5))
                .link(QuestLink.of(ZpmChapter.ASTATINE_DUST, 23.5, -3.5))
                .link(QuestLink.of(Magic2Chapter.ELVEN_TRADE, 18.5, -5))
                .link(QuestLink.of(IvChapter.FISSION_REACTOR, 7, -2.5))
                .link(QuestLink.of(AeChapter.ME_MUFFLER_HATCH, 3.5, 5.5))
                .link(QuestLink.of(UtilChapter.ME_WILDCARD_PATTERN_BUFFER, 25.5, 0.5)));
    }
}
