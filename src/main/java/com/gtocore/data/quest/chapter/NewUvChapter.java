package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOGlobes;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.ExResearchMachines;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockF;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;
import com.gtocore.data.techtree.MachinesNode;
import com.gtocore.data.techtree.NanitesNodes;
import com.gtocore.data.techtree.SpaceNodes;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.player.OrganTier;
import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewUvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_uv", c -> c
            .title("11. 极限压时代(UV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.UV]));

    public static final Quest LUTETIUM = CHAPTER.quest("lutetium", q -> q
            .at(12, 6)
            .title("镥")
            .task(Task.item(TagPrefix.dust, GTMaterials.Lutetium, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Lutetium, 4)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest AMERICIUM = CHAPTER.quest("americium_ingot", q -> q
            .at(14, 6)
            .title("镅")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Americium, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.Lutetium, 8)
            .dependsOn(LUTETIUM));

    public static final Quest TECH_COMPONENT_IN_ASSEMBLY_LINE2 = CHAPTER.quest("tech_component_in_assembly_line2", q -> q
            .at(18, 6)
            .title("装配线进阶部件II研究")
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuv))
            .scaledReward(GTItems.TOOL_DATA_ORB, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest UV_MOTOR = CHAPTER.quest("uv_electric_motor", q -> q
            .at(16, 6)
            .title("UV 马达")
            .task(Task.item(GTItems.ELECTRIC_MOTOR_UV, 8))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Americium, 32)
            .dependsOn(AMERICIUM, NewZpmChapter.TRITANIUM_INGOT, TECH_COMPONENT_IN_ASSEMBLY_LINE2));

    public static final Quest UV_PARTS = CHAPTER.quest("uv_parts", q -> q
            .at(16, 4)
            .title("UV 部件")
            .icon(GTItems.ROBOT_ARM_UV)
            .task(
                    Task.item(GTItems.ELECTRIC_PISTON_UV, 2),
                    Task.item(GTItems.ROBOT_ARM_UV, 2),
                    Task.item(GTItems.CONVEYOR_MODULE_UV, 2),
                    Task.item(GTItems.ELECTRIC_PUMP_UV, 4))
            .scaledReward(GTItems.ELECTRIC_MOTOR_UV, 1)
            .dependsOn(UV_MOTOR));

    public static final Quest UV_MIXER = CHAPTER.quest("uv_mixer", q -> q
            .at(14, 4)
            .title("UV 搅拌机")
            .task(Task.item(GTMachines.MIXER[GTValues.UV]))
            .scaledReward(TagPrefix.rotor, GTMaterials.Darmstadtium, 1)
            .dependsOn(UV_MOTOR));

    public static final Quest UV_ASSEMBLER = CHAPTER.quest("uv_assembler", q -> q
            .at(14, 2)
            .title("UV 组装机")
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.UV]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 2)
            .dependsOn(UV_PARTS));

    public static final Quest ENTED_DUST = CHAPTER.quest("enriched_naquadah_trinium_europium_duranide_dust", q -> q
            .at(12, 4)
            .title("富集硅岩凯金铕铿铀合金粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 20))
            .scaledReward(TagPrefix.dust, GTMaterials.Duranium, 2)
            .dependsOn(UV_MIXER));

    public static final Quest TRITANIUM_COIL_BLOCK = CHAPTER.quest("tritanium_coil_block", q -> q
            .at(12, 2)
            .title("三钛线圈")
            .task(Task.item(GTBlocks.COIL_TRITANIUM, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.Naquadria, 16)
            .dependsOn(UV_ASSEMBLER));

    public static final Quest UV_VOLTAGE_COIL = CHAPTER.quest("uv_voltage_coil", q -> q
            .at(16, 2)
            .title("UV 电压线圈")
            .task(Task.item(GTItems.VOLTAGE_COIL_UV, 4))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Tritanium, 16)
            .dependsOn(UV_ASSEMBLER));

    public static final Quest ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_INGOT = CHAPTER.quest("enriched_naquadah_trinium_europium_duranide_ingot", q -> q
            .at(10, 4)
            .title("富集硅岩凯金铕铿铀合金锭")
            .subtitle("UV超导")
            .task(Task.item(TagPrefix.ingot, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 10)
            .dependsOn(TRITANIUM_COIL_BLOCK, ENTED_DUST));

    public static final Quest ENTED_WIRE = CHAPTER.quest("enriched_naquadah_trinium_europium_duranide_wire", q -> q
            .at(10, 2)
            .title("1x 富集硅岩凯金铕铿铀合金导线")
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 64))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide, 8)
            .dependsOn(ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_INGOT));

    public static final Quest HPCA_BRIDGE = CHAPTER.quest("hpca_bridge_component", q -> q
            .at(8, 4)
            .title("HPCA 桥接组件")
            .task(Task.item(GTResearchMachines.HPCA_BRIDGE_COMPONENT, 2))
            .scaledReward(GTBlocks.OPTICAL_PIPES[0], 8)
            .dependsOn(ENRICHED_NAQUADAH_TRINIUM_EUROPIUM_DURANIDE_INGOT));

    public static final Quest NETWORK_SWITCH = CHAPTER.quest("network_switch", q -> q
            .at(8, 2)
            .title("网络交换机")
            .desc(d -> d
                    .line("做出它后你可以突破单台计算机可以获取的{算力上限}。是时候铺设计算机以获取{算力}了！", YELLOW, AQUA)
                    .line()
                    .line("在上面可以安装{算力数据仓}来实现算力分发，别忘了给提供算力的计算机安装{桥接组件}！", GOLD, GOLD))
            .task(Task.item(GTResearchMachines.NETWORK_SWITCH))
            .scaledReward(TagPrefix.foil, GTMaterials.Tritanium, 32)
            .dependsOn(ENTED_WIRE, HPCA_BRIDGE));

    public static final Quest DATA_MODULE = CHAPTER.quest("data_module", q -> q
            .at(14, -2)
            .title("数据模块")
            .desc(d -> d.line("三级数据载体"))
            .task(Task.item(GTItems.TOOL_DATA_MODULE, 4))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewZpmChapter.WETWARE_PRINTED_CIRCUIT_BOARD, NewZpmChapter.ZPM_CIRCUIT_ASSEMBLER));

    public static final QuestLink DATA_MODULE_COPY_1 = CHAPTER.copy(DATA_MODULE, 22, -6);

    public static final Quest TECH_ENERGY_IO_UV = CHAPTER.quest("tech_energy_io_uv", q -> q
            .at(16, -2)
            .title("高压能量输入输出III研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UV]))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest UV_ENERGY_INPUT_HATCH = CHAPTER.quest("uv_energy_input_hatch", q -> q
            .at(16, 0)
            .milestone()
            .title("UV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UV], 2))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.YttriumBariumCuprate, 4)
            .dependsOn(DATA_MODULE, UV_VOLTAGE_COIL, TECH_ENERGY_IO_UV));

    public static final Quest NEUTRON_IRRADIATION = CHAPTER.quest("neutron_irradiation_chamber", q -> q
            .at(2, 0)
            .milestone()
            .title("中子辐照室")
            .icon(MultiBlockH.NEUTRON_IRRADIATION_CHAMBER)
            .task(Task.item(MultiBlockH.NEUTRON_IRRADIATION_CHAMBER), Task.item(GTOMachines.NEUTRON_IRRADIATION_HATCH))
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium239, 4)
            .dependsOn(NewZpmChapter.WETWARE_PROCESSOR_SUPERCOMPUTER));

    public static final Quest NEUTRON_IRRADIATION_PRODUCT = CHAPTER.quest("neutron_irradiation_product", q -> q
            .at(26, -2)
            .title("钚铍粒子源")
            .task(Task.item(GTOTagPrefix.PARTICLE_SOURCE, GTOMaterials.PlutoniumBerylliumSource, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium239, 4)
            .dependsOn(NEUTRON_IRRADIATION));

    public static final Quest TITANIUM_PLASMA = CHAPTER.quest("titanium_plasma", q -> q
            .at(24, -2)
            .title("钛等离子体")
            .icon(GTMaterials.Titanium.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTMaterials.Titanium.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Fluorine.getBucket(), 4)
            .dependsOn(NEUTRON_IRRADIATION_PRODUCT));

    public static final Quest PHYSICAL_VAPOR_DEPOSITION = CHAPTER.quest("physical_vapor_deposition", q -> q
            .at(6, 0)
            .milestone()
            .title("物理气相沉积系统")
            .task(Task.item(MultiBlockC.PHYSICAL_VAPOR_DEPOSITION))
            .scaledReward(GTOTagPrefix.TARGET_BASE, GTMaterials.StainlessSteel, 2)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest AMERICIUM_TARGET = CHAPTER.quest("americium_stainless_steel_target", q -> q
            .at(28, -4)
            .title("镅不锈钢靶")
            .icon(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Americium)
            .task(Task.item(GTOTagPrefix.STAINLESS_STEEL_TARGET, GTMaterials.Americium, 8))
            .scaledReward(GTOTagPrefix.TARGET_BASE, GTMaterials.StainlessSteel, 2)
            .dependsOn(PHYSICAL_VAPOR_DEPOSITION));

    public static final Quest CURIUM = CHAPTER.quest("curium", q -> q
            .at(26, -4)
            .title("锔")
            .task(Task.item(TagPrefix.dust, GTMaterials.Curium, 4))
            .scaledReward(TagPrefix.dustSmall, GTMaterials.Curium, 2)
            .dependsOn(NEUTRON_IRRADIATION_PRODUCT, AMERICIUM_TARGET));

    public static final Quest XENON_PLASMA = CHAPTER.quest("xenon_plasma", q -> q
            .at(24, -4)
            .title("氙等离子体")
            .icon(GTMaterials.Xenon.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTMaterials.Xenon.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Curium, 1)
            .dependsOn(CURIUM));

    public static final Quest WETWARE_PROCESSOR_MAINFRAME = CHAPTER.quest("wetware_processor_mainframe", q -> q
            .at(22, -4)
            .title("湿件处理器主机")
            .subtitle("千载独步，惟公一人")
            .desc(d -> d
                    .line("先别急着庆祝，你还有一半的路没走呢")
                    .line()
                    .line("不过最难的路已经走完了"))
            .task(Task.item(GTItems.WETWARE_MAINFRAME_UHV, 16))
            .scaledReward(GTItems.WETWARE_SUPER_COMPUTER_UV, 1)
            .dependsOn(XENON_PLASMA, TITANIUM_PLASMA, NewZpmChapter.WETWARE_PROCESSOR_SUPERCOMPUTER, DATA_MODULE_COPY_1));

    public static final QuestLink WETWARE_PROCESSOR_MAINFRAME_COPY_1 = CHAPTER.copy(WETWARE_PROCESSOR_MAINFRAME, 16, 18);

    public static final QuestLink WETWARE_PROCESSOR_MAINFRAME_COPY_2 = CHAPTER.copy(WETWARE_PROCESSOR_MAINFRAME, 20, 20);

    public static final Quest SPACE_COOLANT_CELL = CHAPTER.quest("space_coolant_cell", q -> q
            .at(20, -4)
            .title("太空冷却单元")
            .task(Task.item(GTOItems.SPACE_COOLANT_CELL_10K, 8))
            .scaledReward(GTItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL, 2)
            .dependsOn(WETWARE_PROCESSOR_MAINFRAME));

    public static final Quest FUSION_CASING_MK3 = CHAPTER.quest("fusion_casing_mk3", q -> q
            .at(22, -2)
            .title("聚变机械方块 MK-III")
            .task(Task.item(GTBlocks.FUSION_CASING_MK3, 48))
            .scaledReward(TagPrefix.plate, GTMaterials.Americium, 6)
            .dependsOn(WETWARE_PROCESSOR_MAINFRAME));

    public static final Quest TECH_TOKAMAK_FUSION_REACTOR3 = CHAPTER.quest("tech_tokamak_fusion_reactor3", q -> q
            .at(18, 2)
            .title("托卡马克聚变反应堆III研究")
            .task(Task.techNode(BaseNodes.TokamakFusionReactor3))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest UV_FUSION_REACTOR = CHAPTER.quest("uv_fusion_reactor", q -> q
            .at(20, 0)
            .milestone()
            .title("UV 聚变反应堆")
            .subtitle("芥末甜甜圈")
            .icon(GTMultiMachines.FUSION_REACTOR[GTValues.UV])
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.UV]), Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UV], 16))
            .scaledReward(TagPrefix.ingot, GTMaterials.Americium, 4)
            .dependsOn(WETWARE_PROCESSOR_MAINFRAME, SPACE_COOLANT_CELL, FUSION_CASING_MK3, UV_ENERGY_INPUT_HATCH, TECH_TOKAMAK_FUSION_REACTOR3));

    public static final Quest AMERICIUM_PLASMA = CHAPTER.quest("americium_plasma", q -> q
            .at(20, 2)
            .title("镅等离子体")
            .icon(GTMaterials.Americium.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTMaterials.Americium.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium241, 4)
            .dependsOn(UV_FUSION_REACTOR));

    public static final Quest AMPROSIUM_INGOT = CHAPTER.quest("amprosium_ingot", q -> q
            .at(22, 0)
            .milestone()
            .title("安普洛锭")
            .subtitle("控制重力的力量")
            .desc(d -> d
                    .line("{UHV}阶段的主要材料", UHV)
                    .line()
                    .line("试试把它拿在手上？希望你一切安好。"))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Neutronium, 16))
            .scaledReward(TagPrefix.plate, GTMaterials.Neutronium, 2)
            .dependsOn(AMERICIUM_PLASMA));

    public static final Quest UHV_CASING = CHAPTER.quest("uhv_machine_casing", q -> q
            .at(28, 6)
            .title("UHV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_UHV))
            .scaledReward(TagPrefix.plate, GTMaterials.Neutronium, 4)
            .dependsOn(AMPROSIUM_INGOT));

    public static final QuestLink UHV_CASING_COPY_1 = CHAPTER.copy(UHV_CASING, 28, 20);

    public static final Quest GRAVI_STAR = CHAPTER.quest("gravi_star", q -> q
            .at(26, 20)
            .title("重力之星")
            .task(Task.item(GTItems.GRAVI_STAR, 8))
            .scaledReward(GTItems.QUANTUM_STAR, 2)
            .dependsOn(UHV_CASING_COPY_1));

    public static final QuestLink GRAVI_STAR_COPY_1 = CHAPTER.copy(GRAVI_STAR, 0, 2);

    public static final Quest UV_EMITTER_SENSOR_FIELD_GENERATOR = CHAPTER.quest("uv_emitter_sensor_and_field_generator", q -> q
            .at(24, 20)
            .title("UV 发射器、传感器与力场发生器")
            .icon(GTItems.FIELD_GENERATOR_UV)
            .task(
                    Task.item(GTItems.EMITTER_UV, 2),
                    Task.item(GTItems.SENSOR_UV, 2),
                    Task.item(GTItems.FIELD_GENERATOR_UV))
            .scaledReward(GTItems.GRAVI_STAR, 1)
            .dependsOn(GRAVI_STAR));

    public static final Quest ANILINE = CHAPTER.quest("aniline", q -> q
            .at(30, 6)
            .title("苯胺")
            .icon(GTOMaterials.Aniline.getBucket())
            .task(Task.item(GTOTagPrefix.CATALYST, GTMaterials.Zinc, 1),
                    Task.item(GTOMaterials.Aniline.getBucket()))
            .scaledReward(GTMaterials.Nitrobenzene.getBucket(), 2)
            .dependsOn(UHV_CASING));

    public static final Quest FLUOROTOLUENE = CHAPTER.quest("fluorotoluene", q -> q
            .at(32, 4)
            .title("氟甲苯")
            .icon(GTOMaterials.Fluorotoluene.getBucket())
            .task(Task.item(GTItems.BLACKLIGHT),
                    Task.item(GTOMaterials.Fluorotoluene.getBucket()))
            .scaledReward(GTMaterials.FluoroantimonicAcid.getBucket(), 1)
            .dependsOn(ANILINE));

    public static final Quest ZNFEALCL_CATALYST = CHAPTER.quest("znfealcl_catalyst", q -> q
            .at(28, 8)
            .title("锌-铁-铝-氯混合催化剂")
            .task(Task.item(GTOTagPrefix.CATALYST, GTOMaterials.ZnFeAlClCatalyst, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.ZnFeAlClCatalyst, 16)
            .dependsOn(ANILINE));

    public static final Quest POLYETHERETHERKETONE_BUCKET = CHAPTER.quest("polyetheretherketone_bucket", q -> q
            .at(26, 6)
            .title("聚醚醚酮")
            .desc(d -> d.line("特种工程塑料"))
            .icon(GTOMaterials.Polyetheretherketone.getBucket())
            .task(Task.item(GTOMaterials.Polyetheretherketone.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Polyetheretherketone.getBucket(), 1)
            .dependsOn(FLUOROTOLUENE, ZNFEALCL_CATALYST));

    public static final Quest EUROPIUM_CABLE = CHAPTER.quest("europium_cable", q -> q
            .at(28, 10)
            .title("1x 铕线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Europium, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Europium, 2)
            .dependsOn(FLUOROTOLUENE, ZNFEALCL_CATALYST));

    public static final Quest UHV_HULL = CHAPTER.quest("welcome_to_the_extreme_high_voltage", q -> q
            .at(24, 8)
            .milestone()
            .title("欢迎来到{极高压}阶段", DARK_RED)
            .subtitle("{你需要天马行空般的想象力}", DARK_RED)
            .desc(d -> d
                    .line("以安普洛为主，你可能需要注意自己的超能硅岩余量")
                    .line()
                    .line("来吧，既然体验过{生}{物}{线}，你应该也不会再对{光}{学}{线}产生畏惧了！", GREEN, DARK_GREEN, GREEN, GOLD, YELLOW, GOLD)
                    .line()
                    .line("简而言之，就是漫长的自动化样板过程..."))
            .task(Task.item(GTMachines.HULL[GTValues.UHV]))
            .reward(
                    Reward.item(GTMachines.HULL[GTValues.UHV], 1),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(POLYETHERETHERKETONE_BUCKET, EUROPIUM_CABLE));

    public static final Quest LAFIUM = CHAPTER.quest("lafium", q -> q
            .at(12, -4)
            .title("路菲恩")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Lafium, 32))
            .scaledReward(TagPrefix.dust, GTOMaterials.HastelloyN, 8)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest LAFIUM_CASING = CHAPTER.quest("lafium_mechanical_casing", q -> q
            .at(12, -2)
            .title("路菲恩机械外壳")
            .task(Task.item(GTOBlocks.LAFIUM_MECHANICAL_CASING, 16))
            .scaledReward(TagPrefix.plate, GTOMaterials.Lafium, 6)
            .dependsOn(LAFIUM));

    public static final Quest THORIUM_AND_INCOLOY = CHAPTER.quest("thorium_and_incoloy_ma956", q -> q
            .at(6, -6)
            .title("钍与 MA956 合金")
            .icon(TagPrefix.plateDouble, GTMaterials.Thorium)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.Thorium, 16), Task.item(TagPrefix.plateDouble, GTMaterials.IncoloyMA956, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.Thorium, 8)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest WORLD_ACCELERATOR_ZPM = CHAPTER.quest("world_accelerator_zpm", q -> q
            .at(8, -6)
            .title("ZPM 世界加速器")
            .task(Task.item(GTMachines.WORLD_ACCELERATOR[GTValues.ZPM]))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Thorium, 4)
            .dependsOn(THORIUM_AND_INCOLOY));

    public static final Quest TECH_PARTICLE_ACCELERATOR = CHAPTER.quest("tech_particle_accelerator", q -> q
            .at(6, -4)
            .title("高能粒子实验研究")
            .task(Task.techNode(BaseNodes.ParticleAccelerators))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest DECAY_HASTENER = CHAPTER.quest("decay_hastener", q -> q
            .at(8, -4)
            .title("衰变加速器")
            .icon(MultiBlockA.DECAY_HASTENER)
            .task(Task.item(MultiBlockA.DECAY_HASTENER))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Thorium, 4)
            .dependsOn(THORIUM_AND_INCOLOY, WORLD_ACCELERATOR_ZPM, TECH_PARTICLE_ACCELERATOR));

    public static final Quest COPPER_76 = CHAPTER.quest("copper76", q -> q
            .at(10, -4)
            .title("铜-76")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Copper76, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 16)
            .dependsOn(DECAY_HASTENER));

    public static final Quest ACCELERATED_PIPELINE = CHAPTER.quest("accelerated_pipeline", q -> q
            .at(10, -2)
            .title("加速管道")
            .task(Task.item(GTOBlocks.ACCELERATED_PIPELINE, 8))
            .scaledReward(TagPrefix.dust, GTOMaterials.Copper76, 2)
            .dependsOn(COPPER_76));

    public static final Quest SUPER_PARTICLE_COLLIDER = CHAPTER.quest("super_particle_collider", q -> q
            .at(12, 0)
            .milestone()
            .title("超级粒子对撞机")
            .task(Task.item(MultiBlockF.SUPER_PARTICLE_COLLIDER))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Lafium, 2)
            .dependsOn(ACCELERATED_PIPELINE, LAFIUM_CASING));

    public static final Quest HELIUM_PLASMA = CHAPTER.quest("helium_plasma", q -> q
            .at(26, 4)
            .title("氦等离子")
            .icon(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTMaterials.Helium.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Deuterium.getBucket(), 4)
            .dependsOn(SUPER_PARTICLE_COLLIDER));

    public static final QuestLink HELIUM_PLASMA_COPY_1 = CHAPTER.copy(HELIUM_PLASMA, 12, 18);

    public static final Quest BERKELIUM_AND_CURIUM = CHAPTER.quest("berkelium_and_curium", q -> q
            .at(26, 2)
            .title("锫与锔")
            .icon(TagPrefix.ingot, GTMaterials.Berkelium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Berkelium, 16), Task.item(TagPrefix.ingot, GTMaterials.Curium, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Americium, 8)
            .dependsOn(SUPER_PARTICLE_COLLIDER, HELIUM_PLASMA));

    public static final QuestLink BERKELIUM_AND_CURIUM_COPY_1 = CHAPTER.copy(BERKELIUM_AND_CURIUM, 18, 12);

    public static final QuestLink BERKELIUM_AND_CURIUM_COPY_2 = CHAPTER.copy(BERKELIUM_AND_CURIUM, 10, 14);

    public static final Quest MENDELEVIUM = CHAPTER.quest("mendelevium", q -> q
            .at(18, 10)
            .title("钔、锘与铹")
            .icon(GTMaterials.Mendelevium.getBucket())
            .task(Task.item(GTMaterials.Mendelevium.getBucket()).optional(), Task.item(GTMaterials.Nobelium.getBucket()).optional(), Task.item(GTMaterials.Lawrencium.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Einsteinium.getBucket(), 1)
            .dependsOn(BERKELIUM_AND_CURIUM_COPY_1));

    public static final Quest ORICHALCUM_PLASMA = CHAPTER.quest("orichalcum_plasma", q -> q
            .at(12, 14)
            .title("山铜等离子")
            .icon(GTOMaterials.Orichalcum.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.Orichalcum.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 16)
            .dependsOn(BERKELIUM_AND_CURIUM_COPY_2));

    public static final Quest ORICHALCUM_INGOT = CHAPTER.quest("orichalcum_ingot", q -> q
            .at(14, 12)
            .title("山铜锭")
            .desc(d -> d.line("{UHV}阶段部件的主要材料", UHV))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Orichalcum, 64))
            .scaledReward(TagPrefix.dust, GTOMaterials.Orichalcum, 8)
            .dependsOn(ORICHALCUM_PLASMA));

    public static final Quest MITHRIL_PLASMA = CHAPTER.quest("mithril_plasma", q -> q
            .at(26, 0)
            .title("秘银等离子")
            .icon(GTOMaterials.Mithril.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTOMaterials.Mithril.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Potassium, 16)
            .dependsOn(BERKELIUM_AND_CURIUM));

    public static final Quest HEAT_EXCHANGER = CHAPTER.quest("heat_exchanger", q -> q
            .at(24, 0)
            .milestone()
            .title("热交换机")
            .desc(d -> d.line("核电，搭配超临界蒸汽涡轮使用"))
            .task(Task.item(MultiBlockD.HEAT_EXCHANGER))
            .scaledReward(TagPrefix.dust, GTOMaterials.Mithril, 4)
            .dependsOn(MITHRIL_PLASMA));

    public static final Quest MITHRIL_INGOT = CHAPTER.quest("mithril_ingot", q -> q
            .at(18, 4)
            .title("秘银锭")
            .desc(d -> d.line("{等离子体}需要冷凝成{液体}之后才能固化成锭，目前推荐使用{热交换机}冷凝", GOLD, YELLOW, GOLD))
            .icon(TagPrefix.ingot, GTOMaterials.Mithril)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Mithril, 32))
            .scaledReward(TagPrefix.dust, GTOMaterials.Mithril, 4)
            .dependsOn(HEAT_EXCHANGER));

    public static final QuestLink MITHRIL_INGOT_COPY_1 = CHAPTER.copy(MITHRIL_INGOT, 8, 10);

    public static final QuestLink MITHRIL_INGOT_COPY_2 = CHAPTER.copy(MITHRIL_INGOT, 14, 10);

    public static final Quest TECHNETIUM = CHAPTER.quest("technetium", q -> q
            .at(20, 4)
            .title("锝")
            .task(Task.item(TagPrefix.dust, GTMaterials.Technetium, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.FissionedUranium235, 4)
            .dependsOn(MITHRIL_INGOT));

    public static final Quest URANIUM_SULFATE_WASTE = CHAPTER.quest("uranium_sulfate_waste_solution", q -> q
            .at(-4, 10)
            .title("硫酸铀废液")
            .task(Task.item(GTOMaterials.UraniumSulfateWasteSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 4)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest ACTINIUM = CHAPTER.quest("actinium", q -> q
            .at(-2, 10)
            .title("锕")
            .task(Task.item(TagPrefix.dust, GTMaterials.Actinium, 2))
            .scaledReward(TagPrefix.dustSmall, GTMaterials.Actinium, 4)
            .dependsOn(URANIUM_SULFATE_WASTE));

    public static final Quest MODULE_CONNECTOR = CHAPTER.quest("module_connector", q -> q
            .at(22, 4)
            .title("模块连接器")
            .task(Task.item(GTOBlocks.MODULE_CONNECTOR, 4))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Technetium, 16)
            .dependsOn(TECHNETIUM));

    public static final QuestLink MODULE_CONNECTOR_COPY_1 = CHAPTER.copy(MODULE_CONNECTOR, 20, 12);

    public static final QuestLink MODULE_CONNECTOR_COPY_2 = CHAPTER.copy(MODULE_CONNECTOR, 22, 14);

    public static final Quest LAPOTRONIC_ORB = CHAPTER.quest("lapotronic_energy_orb", q -> q
            .at(20, 14)
            .title("兰波顿能量球")
            .task(Task.item(GTItems.ENERGY_LAPOTRONIC_ORB, 16))
            .scaledReward(GTItems.ENGRAVED_LAPOTRON_CHIP, 24)
            .dependsOn(MODULE_CONNECTOR_COPY_1));

    public static final Quest GRAVITATION_ENGINE = CHAPTER.quest("gravitation_engine", q -> q
            .at(18, 14)
            .title("引力引擎")
            .task(Task.item(GTItems.GRAVITATION_ENGINE, 16))
            .scaledReward(GTItems.EMITTER_LuV, 4)
            .dependsOn(LAPOTRONIC_ORB));

    public static final Quest TECH_SPACE_ELEVATOR = CHAPTER.quest("tech_space_elevator", q -> q
            .at(14, 14)
            .title("太空电梯研究")
            .task(Task.techNode(SpaceNodes.SuperRocketTech), Task.techNode(SpaceNodes.SpaceElevator))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest POWER_MODULE = CHAPTER.quest("space_elevator_power_module", q -> q
            .at(16, 14)
            .title("太空电梯动力模块")
            .desc(d -> d
                    .line("更好的动力模块可以提供{并行}", AQUA)
                    .line()
                    .line("你也可以给{太空电梯模块}供电{升压}来获得配方耗时减免！", GOLD, YELLOW))
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_MODULE_1, 16))
            .scaledReward(GTItems.POWER_THRUSTER_ADVANCED, 4)
            .dependsOn(GRAVITATION_ENGINE, TECH_SPACE_ELEVATOR));

    public static final Quest POWER_CORE = CHAPTER.quest("space_elevator_power_core", q -> q
            .at(16, 12)
            .title("太空电梯动力核心")
            .task(Task.item(GTOBlocks.SPACE_ELEVATOR_POWER_CORE))
            .scaledReward(GTItems.GRAVITATION_ENGINE, 4)
            .dependsOn(POWER_MODULE));

    public static final Quest EGLIN_STEEL = CHAPTER.quest("eglin_steel", q -> q
            .at(24, 12)
            .title("埃格林钢")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.EglinSteel, 40))
            .scaledReward(TagPrefix.dust, GTMaterials.Kanthal, 8)
            .dependsOn(MODULE_CONNECTOR_COPY_2));

    public static final Quest PIKYONIUM = CHAPTER.quest("pikyonium", q -> q
            .at(22, 12)
            .title("皮卡优合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Pikyonium, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Antimony, 8)
            .dependsOn(EGLIN_STEEL));

    public static final QuestLink PIKYONIUM_COPY_1 = CHAPTER.copy(PIKYONIUM, 12, 12);

    public static final QuestLink PIKYONIUM_COPY_2 = CHAPTER.copy(PIKYONIUM, 20, 18);

    public static final Quest ELEVATOR_BLOCKS = CHAPTER.quest("space_elevator_structure_blocks", q -> q
            .at(20, 10)
            .title("太空电梯结构方块")
            .icon(GTOBlocks.SPACE_ELEVATOR_MECHANICAL_CASING)
            .task(Task.item(GTOBlocks.HIGH_STRENGTH_CONCRETE, 64), Task.item(GTOBlocks.SPACE_ELEVATOR_INTERNAL_SUPPORT, 64), Task.item(GTOBlocks.SPACE_ELEVATOR_SUPPORT, 64), Task.item(GTOBlocks.SPACE_ELEVATOR_MECHANICAL_CASING, 64), Task.item(GTOBlocks.SPACE_ELEVATOR_MODULE_BASE, 24))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Pikyonium, 6)
            .dependsOn(PIKYONIUM));

    public static final Quest CARBON_NANITES = CHAPTER.quest("carbon_nanites", q -> q
            .at(22, 6)
            .title("碳纳米蜂群")
            .desc(d -> d
                    .line("{装配线上，某位员工突发奇思妙想：将}{生物细胞}{与}{精密电路}{装配在一起会发生什么？}", ITALIC, YELLOW.italic(), ITALIC, YELLOW.italic(), ITALIC)
                    .line()
                    .line("{于是}{纳米蜂群}{就诞生了}", ITALIC, YELLOW.italic(), ITALIC)
                    .line()
                    .line("{前面几个必须用装配线，后面就可以换成纳米锻炉了}", ITALIC))
            .icon(GTOTagPrefix.NANITES, GTMaterials.Carbon)
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Carbon, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Carbon, 64)
            .dependsOn(MODULE_CONNECTOR));

    public static final Quest TECH_NANITES_TECH = CHAPTER.quest("tech_nanites_tech", q -> q
            .at(22, 10)
            .title("纳米蜂群技术")
            .task(Task.techNode(MachinesNode.ChemicalPlantEnvironmentControl), Task.techNode(NanitesNodes.NanitesTech))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final QuestLink TECH_NANITES_TECH_COPY_1 = CHAPTER.copy(TECH_NANITES_TECH, 8, 6);

    public static final Quest NANO_FORGE = CHAPTER.quest("nano_forge", q -> q
            .at(22, 8)
            .milestone()
            .title("纳米锻炉")
            .subtitle("蜂群，但不是养蜂")
            .desc(d -> d
                    .line("{...那名员工的发现被迅速上报至}{公司总部}{。数个科学家团队昼夜攻关，仿照蜂群结构研制出多种不同属性的纳米蜂群，并探索出一种基于复制的大规模量产方式}", ITALIC, DARK_AQUA.italic(), ITALIC)
                    .line()
                    .line("{以一个原始蜂群为基底，通过}{激光蚀刻}{不同属性的材料，就可以源源不断地制造出对应属性的蜂群}", ITALIC, YELLOW.italic(), ITALIC)
                    .line()
                    .line("{如果想用终端搭建，你需要先在主机里}{放一个蜂群}来指定等级，随后才可以开始搭建", ITALIC, GREEN.italic())
                    .line()
                    .line("机器每次{拿出或放入}蜂群都会重新触发结构检测", YELLOW))
            .task(Task.item(MultiBlockD.NANO_FORGE))
            .scaledReward(TagPrefix.block, GTMaterials.Carbon, 16)
            .dependsOn(CARBON_NANITES, TECH_NANITES_TECH));

    public static final Quest NANITES_BATCH = CHAPTER.quest("carbon_nanites_batch", q -> q
            .at(20, 6)
            .title("批量碳纳米蜂群")
            .icon(GTOTagPrefix.NANITES, GTMaterials.Carbon)
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Carbon, 128))
            .scaledReward(TagPrefix.block, GTMaterials.Carbon, 64)
            .dependsOn(NANO_FORGE));

    public static final Quest SPACE_ELEVATOR = CHAPTER.quest("human_cannon", q -> q
            .at(18, 8)
            .milestone()
            .title("人间大炮")
            .subtitle("我们的目标是星辰大海")
            .desc(d -> d
                    .line("这是T8火箭，解锁巴纳德C")
                    .line()
                    .line("{电梯的配方中有两组碳纳米蜂群的需求，如果还用装配线做显然太贵了，因此我将纳米锻炉设为了前置。}", GRAY.italic()))
            .task(Task.item(MultiBlockD.SPACE_ELEVATOR))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.Orichalcum, 4)
            .dependsOn(POWER_CORE, ELEVATOR_BLOCKS, NANITES_BATCH, ORICHALCUM_INGOT, MENDELEVIUM));

    public static final Quest STELLITE_AND_ARTHERIUM_TIN = CHAPTER.quest("stellite_and_artherium_tin", q -> q
            .at(4, 6)
            .title("铬钴锰钛合金与阿瑟锡")
            .icon(TagPrefix.ingot, GTOMaterials.ArtheriumTin)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Stellite, 64), Task.item(TagPrefix.ingot, GTOMaterials.ArtheriumTin, 40))
            .scaledReward(TagPrefix.dust, GTMaterials.Cobalt, 16)
            .dependsOn(SPACE_ELEVATOR));

    public static final QuestLink STELLITE_AND_ARTHERIUM_TIN_COPY_1 = CHAPTER.copy(STELLITE_AND_ARTHERIUM_TIN, -4, -2);

    public static final QuestLink STELLITE_AND_ARTHERIUM_TIN_COPY_2 = CHAPTER.copy(STELLITE_AND_ARTHERIUM_TIN, -6, 2);

    public static final Quest TECH_ADVANCED_ASSEMBLY_LINE_MACHINE = CHAPTER.quest("tech_advanced_assembly_line_machine", q -> q
            .at(-8, 0)
            .title("进阶装配线研究")
            .task(Task.techNode(MachinesNode.AdvancedAssemblyLineMachine))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest ADVANCED_ASSEMBLY_LINE = CHAPTER.quest("advanced_assembly_line", q -> q
            .at(-6, 0)
            .title("进阶装配线")
            .subtitle("真的进阶... 吗？")
            .desc(d -> d
                    .line("显然你已经受够了普通装配线的速度，是时候迭代更新了！")
                    .line()
                    .line("进阶装配线是装配线的并行版本，可以用{巨型输入总线}，而且可以上{激光仓}", AQUA, YELLOW)
                    .line()
                    .line("需要注意，盲目超频不可取，因为进阶装配线是{超损超频}的！换言之，{4倍}的功率只能让配方耗时变为{0.65}倍，但是并行可以正常结算", RED, GREEN, RED))
            .task(Task.item(MultiBlockD.ADVANCED_ASSEMBLY_LINE))
            .scaledReward(GTOBlocks.ADVANCED_ASSEMBLY_LINE_UNIT, 2)
            .dependsOn(STELLITE_AND_ARTHERIUM_TIN_COPY_2, TECH_ADVANCED_ASSEMBLY_LINE_MACHINE));

    public static final Quest ASSEMBLER_MODULE = CHAPTER.quest("assembler_module", q -> q
            .at(-4, 0)
            .title("太空电梯组装模块")
            .icon(MultiBlockD.ASSEMBLER_MODULE)
            .task(Task.item(MultiBlockD.ASSEMBLER_MODULE))
            .scaledReward(GTOBlocks.ADVANCED_ASSEMBLY_LINE_UNIT, 2)
            .dependsOn(STELLITE_AND_ARTHERIUM_TIN_COPY_1, ADVANCED_ASSEMBLY_LINE));

    public static final Quest TECH_VOID_MINER = CHAPTER.quest("tech_void_miner", q -> q
            .at(4, 4)
            .title("虚空矿脉采掘技术")
            .task(Task.techNode(BaseNodes.VoidMiner))
            .scaledReward(GTItems.TOOL_DATA_ORB, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest LARGE_VOID_MINER = CHAPTER.quest("large_void_miner", q -> q
            .at(2, 6)
            .title("大型虚空采矿厂")
            .subtitle("不仅仅是虚空")
            .desc(d -> d.line("大型虚空采矿能挖到一些你见不到的矿脉，用你的钻井液来换取它们吧！"))
            .task(Task.item(MultiBlockA.LARGE_VOID_MINER))
            .scaledReward(TagPrefix.gear, GTOMaterials.Stellite, 8)
            .dependsOn(STELLITE_AND_ARTHERIUM_TIN, TECH_VOID_MINER));

    public static final Quest VOID_FLUID_DRILLING_RIG = CHAPTER.quest("void_fluid_drilling_rig", q -> q
            .at(6, 6)
            .title("虚空流体钻机")
            .task(Task.item(MultiBlockA.VOID_FLUID_DRILLING_RIG))
            .scaledReward(TagPrefix.gear, GTOMaterials.Stellite, 8)
            .dependsOn(STELLITE_AND_ARTHERIUM_TIN, TECH_VOID_MINER));

    public static final Quest RESOURCE_COLLECTION_MODULE = CHAPTER.quest("resource_collection_module", q -> q
            .at(4, 8)
            .milestone()
            .title("太空电梯资源采集模块")
            .icon(MultiBlockD.RESOURCE_COLLECTION_MODULE)
            .task(Task.item(MultiBlockD.RESOURCE_COLLECTION_MODULE))
            .scaledReward(TagPrefix.gear, GTOMaterials.Stellite, 8)
            .dependsOn(STELLITE_AND_ARTHERIUM_TIN, LARGE_VOID_MINER, VOID_FLUID_DRILLING_RIG));

    public static final QuestLink RESOURCE_COLLECTION_MODULE_COPY_1 = CHAPTER.copy(RESOURCE_COLLECTION_MODULE, -4, 2);

    public static final Quest SPACE_DRONE_MK1 = CHAPTER.quest("space_drone_mk1", q -> q
            .at(-2, 0)
            .title("太空无人机 MK1")
            .desc(d -> d
                    .line("使用太空电梯采集资源必须要搭配无人机，部分高级资源必须在取得更高级的无人机之后才可以采集")
                    .line()
                    .line("每种资源都可以使用两种火箭燃料之一挖掘，更换更高等级的无人机并不会提升采集效率"))
            .task(Task.item(GTOItems.SPACE_DRONE_MK1, 16))
            .scaledReward(GTItems.POWER_THRUSTER, 4)
            .dependsOn(ASSEMBLER_MODULE));

    public static final Quest ALIEN_ALGAE = CHAPTER.quest("alien_algae_slag_ore", q -> q
            .at(-2, 2)
            .title("异星藻类渣")
            .task(Task.item(TagPrefix.dust, GTOMaterials.AlienAlgae, 32))
            .scaledReward(TagPrefix.dust, GTOMaterials.AlgaeExtract, 8)
            .dependsOn(SPACE_DRONE_MK1, RESOURCE_COLLECTION_MODULE_COPY_1));

    public static final Quest PROTOTYPE_ORGANS = CHAPTER.quest("prototype_organs", q -> q
            .at(6, 10)
            .title("原型级器官")
            .icon(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.PROTOTYPE))
            .task(Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.PROTOTYPE)), Task.item(GTOOrganItems.tierOrgan(OrganType.SPINE, OrganTier.PROTOTYPE)), Task.item(GTOOrganItems.tierOrgan(OrganType.LUNG, OrganTier.PROTOTYPE)), Task.item(GTOOrganItems.tierOrgan(OrganType.LIVER, OrganTier.PROTOTYPE)), Task.item(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.PROTOTYPE)), Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, OrganTier.PROTOTYPE)), Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, OrganTier.PROTOTYPE)), Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, OrganTier.PROTOTYPE)), Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, OrganTier.PROTOTYPE)))
            .scaledReward(GTItems.ROBOT_ARM_UV, 2)
            .dependsOn(SPACE_ELEVATOR));

    public static final Quest BARNARD_C = CHAPTER.quest("barnard_c", q -> q
            .at(6, 8)
            .milestone()
            .title("巴纳德C")
            .desc(d -> d
                    .line("点击太空电梯启程按钮前往巴纳德C")
                    .line()
                    .line("别忘了穿上{T4}器官", AQUA))
            .icon(GTOGlobes.Items.BARNARDA_C_GLOBE.get())
            .task(Task.dimension(Dimension.BARNARDA_C).title("造访巴纳德C").icon(GTOGlobes.Items.BARNARDA_C_GLOBE.get()))
            .scaledReward(GTItems.ROBOT_ARM_UV, 1)
            .dependsOn(PROTOTYPE_ORGANS));

    public static final Quest FLUID_DRILLING_RIG_EV = CHAPTER.quest("fluid_drilling_rig_ev", q -> q
            .at(-6, 4)
            .title("EV 流体钻机")
            .task(Task.item(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.EV]))
            .scaledReward(GTMaterials.PhthalicAcid.getBucket(), 4)
            .dependsOn(BARNARD_C));

    public static final Quest UNKNOWN_WATER = CHAPTER.quest("unknowwater_bucket", q -> q
            .at(-4, 4)
            .title("不明液体")
            .desc(d -> d.line("带上你的钻机"))
            .icon(GTOMaterials.UnknowWater.getBucket())
            .task(Task.item(GTOMaterials.UnknowWater.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.PhthalicAcid.getBucket(), 4)
            .dependsOn(FLUID_DRILLING_RIG_EV));

    public static final Quest VISIT_SOLAR_SURFACE = CHAPTER.quest("visit_solar_surface", q -> q
            .at(10, 8)
            .milestone()
            .title("登陆太阳表面")
            .task(Task.dimension(Dimension.SOLAR_SURFACE).title("造访太阳表面"))
            .scaledReward(GTItems.ROBOT_ARM_UV, 1)
            .dependsOn(SPACE_ELEVATOR));

    public static final Quest TCETIE_DANDELIONS = CHAPTER.quest("tcetie_dandelions", q -> q
            .at(-2, 4)
            .title("鲸鱼座T星E藻类")
            .task(Task.item(GTOItems.TCETIEDANDELIONS, 256))
            .scaledReward(Items.KELP, 64)
            .dependsOn(ALIEN_ALGAE, UNKNOWN_WATER, VISIT_SOLAR_SURFACE));

    public static final Quest BIOMEDIUM_STERILIZED = CHAPTER.quest("biohmediumsterilized_bucket", q -> q
            .at(0, 6)
            .title("灭菌生物培养基")
            .desc(d -> d
                    .line("是第一批生物细胞的制作原料，也是细胞增殖的原料")
                    .line()
                    .line("你可能要考虑设计处理循环产线"))
            .icon(GTOMaterials.BiohmediumSterilized.getBucket())
            .task(Task.item(GTOMaterials.BiohmediumSterilized.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.BiomediumRaw.getBucket(), 1)
            .dependsOn(TCETIE_DANDELIONS, NewEvChapter.EARTH_ORBIT));

    public static final QuestLink BIOMEDIUM_STERILIZED_COPY_1 = CHAPTER.copy(BIOMEDIUM_STERILIZED, -4, 18);

    public static final QuestLink BIOMEDIUM_STERILIZED_COPY_2 = CHAPTER.copy(BIOMEDIUM_STERILIZED, 2, 18);

    public static final Quest BIOLOGICAL_CELLS = CHAPTER.quest("biological_cells", q -> q
            .at(2, 8)
            .milestone()
            .title("生物细胞")
            .desc(d -> d.line("生化反应培养出后可使用培养缸繁殖"))
            .task(Task.item(GTOItems.BIOLOGICAL_CELLS, 32))
            .scaledReward(GTItems.STEM_CELLS, 16)
            .dependsOn(BIOMEDIUM_STERILIZED));

    public static final Quest OXYGEN_PLASMA = CHAPTER.quest("oxygen_plasma", q -> q
            .at(14, 20)
            .title("氧等离子")
            .icon(GTMaterials.Oxygen.getFluid(FluidStorageKeys.PLASMA).getBucket())
            .task(Task.item(GTMaterials.Oxygen.getFluid(FluidStorageKeys.PLASMA).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Helium3.getBucket(), 2)
            .dependsOn(UV_FUSION_REACTOR));

    public static final Quest MUTATED_LIVING_SOLDER = CHAPTER.quest("mutated_living_solder_bucket", q -> q
            .at(14, 18)
            .title("突变活体焊料")
            .icon(GTOMaterials.MutatedLivingSolder.getBucket())
            .task(Task.item(GTOMaterials.MutatedLivingSolder.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOItems.BIOLOGICAL_CELLS, 8)
            .dependsOn(BIOLOGICAL_CELLS, OXYGEN_PLASMA, HELIUM_PLASMA_COPY_1));

    public static final Quest VACUUM_HATCH = CHAPTER.quest("vacuum_hatch", q -> q
            .at(-6, 6)
            .title("真空维护仓")
            .task(Task.item(GTOMachines.VACUUM_HATCH, 3))
            .scaledReward(GTItems.ELECTRIC_PUMP_EV, 1)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest ULTRA_HIGH_PURITY_SILICON = CHAPTER.quest("ultra_high_purity_silicon", q -> q
            .at(-4, 6)
            .title("超高纯硅")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.UltraHighPuritySilicon, 288))
            .scaledReward(TagPrefix.dust, GTOMaterials.UltraHighPuritySilicon, 32)
            .dependsOn(VACUUM_HATCH));

    public static final Quest NEUTRONIUM_BOULE = CHAPTER.quest("neutronium_boule", q -> q
            .at(-4, 8)
            .title("中子素晶锭")
            .desc(d -> d.line("镭的来源是{硫酸铀废液}，你也可以通过{快中子增殖堆}获得", GOLD, GOLD))
            .task(Task.item(GTItems.NEUTRONIUM_BOULE))
            .scaledReward(TagPrefix.ingot, GTMaterials.Neutronium, 4)
            .dependsOn(ULTRA_HIGH_PURITY_SILICON, AMPROSIUM_INGOT));

    public static final Quest NEUTRONIUM_WAFER = CHAPTER.quest("neutronium_wafer", q -> q
            .at(-2, 8)
            .title("中子素晶圆")
            .task(Task.item(GTItems.NEUTRONIUM_WAFER, 16))
            .scaledReward(GTItems.NEUTRONIUM_WAFER, 4)
            .dependsOn(NEUTRONIUM_BOULE));

    public static final QuestLink NEUTRONIUM_WAFER_COPY_1 = CHAPTER.copy(NEUTRONIUM_WAFER, 16, 20);

    public static final Quest BIOWARE_BOULE = CHAPTER.quest("bioware_boule", q -> q
            .at(0, 8)
            .milestone()
            .title("生物活性晶锭")
            .task(Task.item(GTOItems.BIOWARE_BOULE, 2))
            .scaledReward(GTItems.NEUTRONIUM_WAFER, 2)
            .dependsOn(NEUTRONIUM_WAFER, BIOLOGICAL_CELLS, ACTINIUM));

    public static final Quest CLARIFIER_PURIFICATION_UNIT = CHAPTER.quest("clarifier_purification_unit", q -> q
            .at(8, -2)
            .title("澄清器净化装置")
            .desc(d -> d.line("净化水机器并行需要调至{1000}的整数倍才会运行，并行数决定了一次周期内制作多少水和耗电量", GREEN))
            .task(Task.item(MultiBlockB.CLARIFIER_PURIFICATION_UNIT))
            .scaledReward(GTMaterials.Water.getBucket(), 16)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest WATER_PURIFICATION_PLANT = CHAPTER.quest("water_purification_plant", q -> q
            .at(8, 0)
            .milestone()
            .title("水净化厂")
            .desc(d -> d
                    .line("随着芯片制作越发精密，普通的蒸馏水已经不能满足切割晶圆的需要。所以... 你已经解锁了GTO的一条新的支线——{净化水}！", AQUA)
                    .line()
                    .line("净化水厂是整个净化水网络的核心。它用于把所有水厂收纳为统一的网络，并为它们集中供电"))
            .icon(MultiBlockB.WATER_PURIFICATION_PLANT)
            .task(Task.item(MultiBlockB.WATER_PURIFICATION_PLANT))
            .scaledReward(GTMaterials.Water.getBucket(), 16)
            .dependsOn(CLARIFIER_PURIFICATION_UNIT));

    public static final Quest OZONATION_PURIFICATION_UNIT = CHAPTER.quest("ozonation_purification_unit", q -> q
            .at(4, 22)
            .title("臭氧净化装置")
            .task(Task.item(MultiBlockB.OZONATION_PURIFICATION_UNIT))
            .scaledReward(GTOMaterials.FilteredSater.getBucket(), 16)
            .dependsOn(WATER_PURIFICATION_PLANT));

    public static final Quest FLOCCULATION_PURIFICATION_UNIT = CHAPTER.quest("flocculation_purification_unit", q -> q
            .at(4, 20)
            .title("絮凝净化装置")
            .subtitle("3级“污染水”")
            .task(Task.item(MultiBlockB.FLOCCULATION_PURIFICATION_UNIT))
            .scaledReward(GTOMaterials.OzoneWater.getBucket(), 16)
            .dependsOn(OZONATION_PURIFICATION_UNIT));

    public static final Quest PH_NEUTRALIZATION_PURIFICATION_UNIT = CHAPTER.quest("ph_neutralization_purification_unit_machine", q -> q
            .at(6, 20)
            .title("pH中和净化装置")
            .task(Task.item(MultiBlockB.PH_NEUTRALIZATION_PURIFICATION_UNIT))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 32)
            .dependsOn(FLOCCULATION_PURIFICATION_UNIT));

    public static final Quest EXTREME_TEMPERATURE_FLUCTUATION_PURIFICATION_UNIT = CHAPTER.quest("extreme_temperature_fluctuation_purification_unit_machine", q -> q
            .at(8, 20)
            .title("极端温度波动净化装置")
            .task(Task.item(MultiBlockB.EXTREME_TEMPERATURE_FLUCTUATION_PURIFICATION_UNIT))
            .scaledReward(GTOMaterials.PHNeutralWater.getBucket(), 4)
            .dependsOn(PH_NEUTRALIZATION_PURIFICATION_UNIT));

    public static final Quest EXTREME_TEMPERATURE_WATER = CHAPTER.quest("extreme_temperature_fluctuation_purification_unit", q -> q
            .at(8, 18)
            .title("极端温度水")
            .subtitle("生物恒温环境")
            .icon(GTOMaterials.ExtremeTemperatureWater.getBucket())
            .task(Task.item(GTOMaterials.ExtremeTemperatureWater.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.PHNeutralWater.getBucket(), 4)
            .dependsOn(EXTREME_TEMPERATURE_FLUCTUATION_PURIFICATION_UNIT));

    public static final Quest BIOWARE_CHIP = CHAPTER.quest("bioware_chip", q -> q
            .at(8, 16)
            .milestone()
            .title("生物活性芯片")
            .task(Task.item(GTOItems.BIOWARE_CHIP, 32))
            .scaledReward(GTOItems.BIOLOGICAL_CELLS, 8)
            .dependsOn(BIOWARE_BOULE, EXTREME_TEMPERATURE_WATER));

    public static final Quest PEDOT = CHAPTER.quest("pedot", q -> q
            .at(-2, 16)
            .title("PEDOT")
            .icon(TagPrefix.foil, GTOMaterials.Pedot)
            .task(Task.item(TagPrefix.foil, GTOMaterials.Pedot, 64), Task.item(TagPrefix.plate, GTOMaterials.Pedot, 2))
            .scaledReward(TagPrefix.dust, GTOMaterials.Pedot, 4)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest POLYIMIDE = CHAPTER.quest("polyimide_bucket", q -> q
            .at(0, 18)
            .title("聚酰亚胺")
            .desc(d -> d.line("聚酰亚胺是一种特种工程材料，广泛应用在航空、航天、微电子、纳米、液晶、分离膜、激光等领域"))
            .icon(GTOMaterials.Polyimide.getBucket())
            .task(Task.item(GTOMaterials.Polyimide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Paa.getBucket(), 2)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest CEREBRUM = CHAPTER.quest("cerebrum", q -> q
            .at(-2, 20)
            .title("大脑")
            .desc(d -> d.line("还是一样，用捕获附魔获取蛋"))
            .task(Task.item(GTOItems.CEREBRUM, 32))
            .scaledReward(GTOMaterials.Perfluorobenzene.getBucket(), 4)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final QuestLink CEREBRUM_COPY_1 = CHAPTER.copy(CEREBRUM, 16, 22);

    public static final Quest POLYDOPAMINE = CHAPTER.quest("polydopamine", q -> q
            .at(-2, 18)
            .title("聚多巴胺")
            .task(Task.item(TagPrefix.foil, GTOMaterials.PolyDopamine, 32))
            .scaledReward(GTOMaterials.Dopamine.getBucket(), 4)
            .dependsOn(CEREBRUM, BIOMEDIUM_STERILIZED_COPY_1));

    public static final Quest BIOWARE_SMD = CHAPTER.quest("smd_capacitor_bioware", q -> q
            .at(0, 16)
            .milestone()
            .title("生物活性 SMD 元件")
            .desc(d -> d.line("后面的电路每种都有对应的贴片"))
            .icon(GTOItems.SMD_TRANSISTOR_BIOWARE)
            .task(Task.item(GTOItems.SMD_CAPACITOR_BIOWARE, 64), Task.item(GTOItems.SMD_DIODE_BIOWARE, 64), Task.item(GTOItems.SMD_RESISTOR_BIOWARE, 64), Task.item(GTOItems.SMD_TRANSISTOR_BIOWARE, 64), Task.item(GTOItems.SMD_INDUCTOR_BIOWARE, 64))
            .scaledReward(TagPrefix.foil, GTOMaterials.Pedot, 16)
            .dependsOn(PEDOT, POLYIMIDE, POLYDOPAMINE));

    public static final Quest TELLURATE_CERAMICS = CHAPTER.quest("tellurate_ceramics_flakes", q -> q
            .at(4, 14)
            .title("碲酸盐陶瓷片")
            .desc(d -> d
                    .line("还记得你的碲从哪里来吗？")
                    .line()
                    .line("不过这次你有太空电梯了"))
            .icon(GTOTagPrefix.FLAKES, GTOMaterials.TellurateCeramics)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.TellurateCeramics, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.TellurateCeramics, 4)
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest BIOWARE_CIRCUIT_BOARD = CHAPTER.quest("bioware_circuit_board", q -> q
            .at(4, 18)
            .title("生物活性电路基板")
            .task(Task.item(GTOItems.BIOWARE_CIRCUIT_BOARD, 32))
            .scaledReward(GTItems.WETWARE_BOARD, 8)
            .dependsOn(BIOMEDIUM_STERILIZED_COPY_2));

    public static final Quest BIOWARE_PCB = CHAPTER.quest("bioware_printed_circuit_board", q -> q
            .at(4, 16)
            .milestone()
            .title("生物活性印刷电路板")
            .task(Task.item(GTOItems.BIOWARE_PRINTED_CIRCUIT_BOARD, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.VanadiumGallium, 32)
            .dependsOn(TELLURATE_CERAMICS, BIOWARE_CIRCUIT_BOARD));

    public static final Quest HIGHLY_ADVANCED_SOC = CHAPTER.quest("highly_advanced_soc", q -> q
            .at(18, 18)
            .title("高度先进 SoC")
            .task(Task.item(GTItems.HIGHLY_ADVANCED_SOC, 16))
            .scaledReward(GTItems.HIGHLY_ADVANCED_SOC_WAFER, 2)
            .dependsOn(NEUTRONIUM_WAFER_COPY_1));

    public static final Quest TECH_BIOWARE_TECH = CHAPTER.quest("tech_bioware_tech", q -> q
            .at(10, 18)
            .title("生物件技术")
            .task(Task.techNode(BaseNodes.BiowareTech))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewZpmChapter.UV_HULL, NewZpmChapter.TECH_WETWARE_TECH));

    public static final Quest BIOWARE_CORE = CHAPTER.quest("bioware_processing_core", q -> q
            .at(12, 16)
            .milestone()
            .title("生物活性处理核心")
            .task(Task.item(GTOItems.BIOWARE_PROCESSING_CORE, 16))
            .scaledReward(GTOItems.BIOWARE_CHIP, 8)
            .dependsOn(BIOWARE_SMD, BIOWARE_PCB, TECH_BIOWARE_TECH));

    public static final Quest BIOWARE_PROCESSOR = CHAPTER.quest("bio_activity_processor", q -> q
            .at(16, 16)
            .milestone()
            .title("生物活性处理器")
            .task(Task.item(GTOItems.BIOWARE_PROCESSOR, 16))
            .scaledReward(GTItems.HIGHLY_ADVANCED_SOC, 4)
            .dependsOn(BIOWARE_CORE, HIGHLY_ADVANCED_SOC, MUTATED_LIVING_SOLDER, WETWARE_PROCESSOR_MAINFRAME_COPY_1));

    public static final Quest CAL_CASING = CHAPTER.quest("circuit_assembly_line_casing", q -> q
            .at(12, 10)
            .title("电路装配线外壳")
            .task(Task.item(GTOBlocks.MACHINE_CASING_CIRCUIT_ASSEMBLY_LINE, 8))
            .scaledReward(TagPrefix.plate, GTOMaterials.Pikyonium, 4)
            .dependsOn(PIKYONIUM_COPY_1));

    public static final Quest LARGE_CIRCUIT_ASSEMBLER = CHAPTER.quest("large_circuit_assembler", q -> q
            .at(10, 10)
            .title("大型电路组装机")
            .task(Task.item(GCYMMachines.LARGE_CIRCUIT_ASSEMBLER))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.Mithril, 2)
            .dependsOn(CAL_CASING, MITHRIL_INGOT_COPY_1));

    public static final Quest TECH_CIRCUIT_ASSEMBLY_LINE = CHAPTER.quest("tech_circuit_assembly_line", q -> q
            .at(10, 6)
            .title("蜂群电路装配研究")
            .task(Task.techNode(NanitesNodes.CircuitAssemblyLineTech))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(TECH_NANITES_TECH_COPY_1));

    public static final Quest CIRCUIT_ASSEMBLY_LINE = CHAPTER.quest("circuit_assembly_line", q -> q
            .at(12, 8)
            .milestone()
            .title("电路装配线")
            .desc(d -> d
                    .line("所有的电路装配线配方都需要一个机器人作为工作单位")
                    .line()
                    .line("使用更高等级的机器人电路配方可以为电路制作提供{无损超频}", YELLOW)
                    .line()
                    .line("往{主机}中塞入同配方的机器人可以提供并行（不可以替代配方中的机器人）", YELLOW))
            .icon(MultiBlockD.CIRCUIT_ASSEMBLY_LINE)
            .task(Task.item(MultiBlockD.CIRCUIT_ASSEMBLY_LINE))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.Mithril, 2)
            .dependsOn(CAL_CASING, MITHRIL_INGOT_COPY_2, LARGE_CIRCUIT_ASSEMBLER, TECH_CIRCUIT_ASSEMBLY_LINE));

    public static final Quest SILVER_NANITES = CHAPTER.quest("silver_nanites", q -> q
            .at(22, 20)
            .title("银纳米蜂群")
            .icon(GTOTagPrefix.NANITES, GTMaterials.Silver)
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Silver, 1))
            .scaledReward(TagPrefix.block, GTMaterials.Silver, 4)
            .dependsOn(NANO_FORGE));

    public static final Quest ROBOT_MK1 = CHAPTER.quest("precision_circuit_assembly_robot_mk1", q -> q
            .at(22, 18)
            .title("精密电路装配机器人 MK1")
            .task(Task.item(GTOItems.PRECISION_CIRCUIT_ASSEMBLY_ROBOT_MK1))
            .scaledReward(TagPrefix.plate, GTOMaterials.Pikyonium, 2)
            .dependsOn(SILVER_NANITES, PIKYONIUM_COPY_2, UV_EMITTER_SENSOR_FIELD_GENERATOR, WETWARE_PROCESSOR_MAINFRAME_COPY_2));

    public static final Quest BIOWARE_ASSEMBLY = CHAPTER.quest("bioactivity_processor_hatch_cluster", q -> q
            .at(20, 16)
            .milestone()
            .title("生物活性处理器集群")
            .task(Task.item(GTOItems.BIOWARE_ASSEMBLY, 4))
            .scaledReward(GTOItems.BIOWARE_PROCESSOR, 4)
            .dependsOn(BIOWARE_PROCESSOR, CIRCUIT_ASSEMBLY_LINE, ROBOT_MK1));

    public static final Quest BIOWARE_COMPUTER = CHAPTER.quest("bioactivityprocessorsupercomputer", q -> q
            .at(24, 16)
            .milestone()
            .title("生物活性处理器超级计算机")
            .task(Task.item(GTOItems.BIOWARE_COMPUTER, 2))
            .scaledReward(GTOItems.SMD_CAPACITOR_BIOWARE, 32)
            .dependsOn(BIOWARE_ASSEMBLY));

    public static final Quest PH_NEUTRAL_WATER_BUCKET = CHAPTER.quest("ph_neutral_water_bucket", q -> q
            .at(6, 18)
            .side()
            .icon(GTOMaterials.PHNeutralWater.getBucket())
            .subtitle("[H+]=1e-7")
            .task(Task.item(GTOMaterials.PHNeutralWater.getBucket()).optional(), Task.checkmark())
            .dependsOn(PH_NEUTRALIZATION_PURIFICATION_UNIT));

    public static final Quest BUFFERING_HATCH = CHAPTER.quest("buffering_hatch", q -> q
            .at(24, 22)
            .side()
            .icon(GTOMachines.OVERCLOCK_HATCH[GTValues.UV])
            .title("增益仓")
            .subtitle("{产能至上！}", DARK_AQUA)
            .desc(d -> d
                    .line("在巨构中，你将可以使用四种增益仓室提高机器效率。{首先是两位老朋友：}", YELLOW)
                    .line()
                    .line("{加速仓}：大部分机器可以使用。{无条件}加快机器速度，数字表示当加速仓等级{高于}配方等级时，配方时间将乘以这个百分比。当加速仓等级每{低于}配方一级，加速效果减少20%。", GREEN, AQUA, AQUA, AQUA)
                    .line()
                    .line("{并行控制仓}：可并行机器即可使用。允许机器一次性翻倍功率处理多个配方，某种意义下相当于提供无损超频。", GREEN)
                    .page()
                    .line("{然后是两位新朋友：}", YELLOW)
                    .line()
                    .line("{线程仓}：仅跨配方并行机器可以使用。允许机器尽量同时多运行数个{不同配方}来跑满并行和最高效率。", GREEN, AQUA)
                    .line()
                    .line("{超频仓}：几乎只有巨构可以使用。非常强大，能直接{修改机器超频的时间倍率系数}。UV级别是一般的有损超频，UEV级别即可达到无损超频，在这之后机器越超频越省电！（UEV以上机器会优先计算超频再计算并行）", GREEN, AQUA)
                    .line()
                    .line("此外，负荷调节维护仓也可以提供微弱的机器加速效果")
                    .line()
                    .line("{别忘了给你的狂飙也换上}", GRAY.italic()))
            .task(
                    Task.item(GCYMMachines.PARALLEL[GTValues.UV]).optional(),
                    Task.item(GTOMachines.ACCELERATE_HATCH[GTValues.UV]).optional(),
                    Task.item(GTOMachines.THREAD_HATCH[GTValues.UV]).optional(),
                    Task.item(GTOMachines.OVERCLOCK_HATCH[GTValues.UV]).optional(),
                    Task.checkmark())
            .dependsOn(UV_EMITTER_SENSOR_FIELD_GENERATOR));

    public static final Quest NANITES_INTEGRATED_PROCESSING_CENTER = CHAPTER.quest("nanites_integrated_processing_center", q -> q
            .at(26, 12)
            .side()
            .desc(d -> d
                    .line("{纳米蜂群}{的问世和量产推动了}{格雷科技有限公司}{的}{第一次技术革命}{。老旧复杂的生产线被彻底改造，取而代之的是一体化的蜂群生产车间——无数的纳米蜂群从原子层面上对物质进行拆分和组合，}{一步完成整个生产过程}", GOLD.italic(), ITALIC, DARK_AQUA.italic(), ITALIC, YELLOW.italic(), ITALIC, YELLOW.italic())
                    .line()
                    .line("你可以用它来大幅简化过去和未来的复杂产线，每个纳米加工中心可以放置{两个}模块", GREEN)
                    .line()
                    .line("放入{主机}的蜂群并不参与配方运行，而是减少{生产时}蜂群的污染概率。现在你可以放入{铱纳米蜂群}", YELLOW, YELLOW, AQUA)
                    .line()
                    .line("可以使用很多增益仓，不要忘记看看{这里}", link(NewUvChapter.BUFFERING_HATCH))
                    .line()
                    .line("{虽然模块本身也可以运行配方，但是只有主机可以安装增益仓，因此只需要在主机上安装各种仓室即可}", GRAY.italic()))
            .task(Task.item(MultiBlockC.NANITES_INTEGRATED_PROCESSING_CENTER))
            .dependsOn(NANO_FORGE));

    public static final Quest PIRANHA_SOLUTION_BUCKET = CHAPTER.quest("piranha_solution_bucket", q -> q
            .at(26, 10)
            .side()
            .icon(GTOMaterials.PiranhaSolution.getBucket())
            .subtitle("洗个澡澡！")
            .desc(d -> d
                    .line("{...就是蜂群的消耗有些快，所幸，科学家们发现了一种特殊的洗液可以}{重新激发蜂群的活力}", ITALIC, YELLOW.italic())
                    .line()
                    .line("蜂群们为你那么勤劳地工作，也许你可能需要给他们一点点{回报}...", DARK_PURPLE)
                    .line()
                    .line("那就给他们洗个澡吧！{过氧化氢}的强氧化性和{硫酸}的酸性环境能重新激发它们的活力！", AQUA, GOLD))
            .task(Task.item(GTOMaterials.PiranhaSolution.getBucket()).optional(), Task.checkmark())
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER));

    public static final Quest BIOENGINEERING_MODULE = CHAPTER.quest("bioengineering_module", q -> q
            .at(28, 12)
            .side()
            .subtitle("蜂群也是生物，很合理")
            .desc(d -> d.line("更方便地制作生物原料"))
            .task(Task.item(MultiBlockC.BIOENGINEERING_MODULE))
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER));

    public static final Quest ORE_EXTRACTION_MODULE = CHAPTER.quest("ore_extraction_module", q -> q
            .at(26, 14)
            .side()
            .subtitle("产线杀手")
            .desc(d -> d
                    .line("一步到位完成矿石处理产线！")
                    .line()
                    .line("厌倦了被动产线大量堆砌的机器？这个会满足你的"))
            .task(Task.item(MultiBlockC.ORE_EXTRACTION_MODULE))
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER));

    public static final Quest POLYMER_TWISTING_MODULE = CHAPTER.quest("polymer_twisting_module", q -> q
            .at(28, 14)
            .side()
            .subtitle("超级产线杀手")
            .desc(d -> d
                    .line("一步到位完成化工产线！")
                    .line()
                    .line("你最好随时注意解锁的新配方，来{替换}你之前的下单产线，别忘了处理蜂群的洗涤和回收", YELLOW))
            .task(Task.item(MultiBlockC.POLYMER_TWISTING_MODULE))
            .dependsOn(NANITES_INTEGRATED_PROCESSING_CENTER));

    public static final Quest INFINITY_FLUID_DRILLING_RIG = CHAPTER.quest("infinity_fluid_drilling_rig", q -> q
            .at(22, 2)
            .side()
            .subtitle("再也不用给钻机换位置了")
            .task(Task.item(MultiBlockD.INFINITY_FLUID_DRILLING_RIG))
            .dependsOn(AMPROSIUM_INGOT));

    public static final Quest INTEGRAL_FRAMEWORK_UV = CHAPTER.quest("integral_framework_uv", q -> q
            .at(2, -2)
            .side()
            .subtitle("升级你的大型机器#4")
            .desc(d -> d.line("不需要做单方块组装机也能做哦"))
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_UV))
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest WETWARE_SOC = CHAPTER.quest("wetware_soc", q -> q
            .at(18, 20)
            .side()
            .subtitle("布满了脑细胞")
            .desc(d -> d.line("更便宜的制作{湿件处理器}的原材料", GOLD))
            .task(Task.item(GTOItems.WETWARE_SOC))
            .dependsOn(HIGHLY_ADVANCED_SOC, CEREBRUM_COPY_1));

    public static final Quest SUPERCOMPUTING_CENTER = CHAPTER.quest("supercomputing_center", q -> q
            .at(18, 22)
            .side()
            .subtitle("天河一号")
            .desc(d -> d
                    .line("超级计算中心，可以放下足足{840}个组件。一台足够提供你整个基地的算力供应", AQUA)
                    .line()
                    .line("请使用{空组件}填充你用不到的组件槽位", GOLD)
                    .line()
                    .line("{导热剂}并非必需品，Tier2/3放入导热剂后会提升超级计算中心输出的算力", GOLD))
            .task(Task.item(ExResearchMachines.SUPERCOMPUTING_CENTER))
            .dependsOn(WETWARE_SOC));

    public static final Quest CYCLOPARAPHENYLENE_BUCKET = CHAPTER.quest("cycloparaphenylene_bucket", q -> q
            .at(0, -2)
            .side()
            .icon(GTOMaterials.Cycloparaphenylene.getBucket())
            .task(Task.item(GTOMaterials.Cycloparaphenylene.getBucket()).optional(), Task.checkmark())
            .dependsOn(NewZpmChapter.UV_HULL));

    public static final Quest NANOTUBE_SPOOL = CHAPTER.quest("nanotube_spool", q -> q
            .at(0, -4)
            .side()
            .desc(d -> d.line("太空电梯第一次启动的时候需要，放置在太空电梯的输入总线内自动消耗"))
            .task(Task.item(GTOItems.NANOTUBE_SPOOL, 256L))
            .dependsOn(CYCLOPARAPHENYLENE_BUCKET));

    public static final Quest TCETIESEAWEEDEXTRACT = CHAPTER.quest("tcetieseaweedextract", q -> q
            .at(0, 4)
            .side()
            .subtitle("天仓五 - 新的生命")
            .desc(d -> d.line("你需要使用重力控制仓达到强重力环境"))
            .task(Task.item(GTOItems.TCETIESEAWEEDEXTRACT))
            .dependsOn(TCETIE_DANDELIONS, GRAVI_STAR_COPY_1));

    public static final Quest SUPER_BLAST_SMELTER = CHAPTER.quest("super_blast_smelter", q -> q
            .at(26, 16)
            .side()
            .subtitle("超大杯")
            .desc(d -> d
                    .line("这台机器标志着你正式进入巨构时代，所有的{增益仓}都已经为你开放！", GOLD)
                    .line()
                    .line("其余大部分巨构将在你制造出{UIV}电路后解锁", UIV))
            .task(Task.item(MultiBlockF.SUPER_BLAST_SMELTER))
            .dependsOn(BIOWARE_COMPUTER));

    public static final Quest SUPERCONDUCTING_ELECTROMAGNETISM = CHAPTER.quest("superconducting_electromagnetism", q -> q
            .at(24, 18)
            .side()
            .task(Task.item(MultiBlockF.SUPERCONDUCTING_ELECTROMAGNETISM))
            .dependsOn(BIOWARE_COMPUTER));

    public static final Quest CHEMICAL_COMPLEX = CHAPTER.quest("chemical_complex", q -> q
            .at(24, 14)
            .side()
            .task(Task.item(MultiBlockA.CHEMICAL_COMPLEX))
            .dependsOn(BIOWARE_COMPUTER));

    public static final Quest GEMINI_CONTAINMENT_SYSTEM = CHAPTER.quest("gemini_containment_system", q -> q
            .at(26, 18)
            .side()
            .task(Task.item(MultiBlockH.GEMINI_CONTAINMENT_SYSTEM))
            .dependsOn(BIOWARE_COMPUTER));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewZpmChapter.UV_HULL, 0, 0))
                .mainLine(NewZpmChapter.UV_HULL, NEUTRON_IRRADIATION, PHYSICAL_VAPOR_DEPOSITION, WATER_PURIFICATION_PLANT, SUPER_PARTICLE_COLLIDER, UV_ENERGY_INPUT_HATCH, UV_FUSION_REACTOR, AMPROSIUM_INGOT, HEAT_EXCHANGER, UHV_HULL, NANO_FORGE, SPACE_ELEVATOR, CIRCUIT_ASSEMBLY_LINE, VISIT_SOLAR_SURFACE, BARNARD_C, RESOURCE_COLLECTION_MODULE, BIOLOGICAL_CELLS, BIOWARE_BOULE, BIOWARE_SMD, BIOWARE_PCB, BIOWARE_CHIP, BIOWARE_CORE, BIOWARE_PROCESSOR, BIOWARE_ASSEMBLY, BIOWARE_COMPUTER));
    }
}
