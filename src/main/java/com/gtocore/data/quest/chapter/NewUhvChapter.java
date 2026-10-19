package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.SpaceMultiblock;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;

import com.gtolib.api.quest.AutoClaim;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewUhvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_uhv", c -> c
            .title("12. 极高压时代(UHV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.UHV]));

    public static final Quest GRAVITY_HATCH = CHAPTER.quest("gravity_hatch", q -> q
            .at(-2, 0)
            .title("重力控制维护仓")
            .task(Task.item(GTOMachines.GRAVITY_HATCH, 2))
            .scaledReward(GTItems.GRAVI_STAR, 2)
            .dependsOn(NewUvChapter.SPACE_ELEVATOR, NewUvChapter.UHV_HULL));

    public static final Quest BARNARDA_AIR = CHAPTER.quest("barnarda_air", q -> q
            .at(22, 18)
            .title("巴纳德C空气")
            .task(Task.item(GTOMaterials.BarnardaAir.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Radon.getBucket(), 4)
            .dependsOn(NewUvChapter.BARNARD_C));

    public static final Quest NETHERITE = CHAPTER.quest("netherite_ingot", q -> q
            .at(6, -2)
            .title("下界合金锭")
            .task(Task.item(Items.NETHERITE_INGOT, 72))
            .scaledReward(Items.NETHERITE_SCRAP, 16)
            .dependsOn(NewUvChapter.MUTATED_LIVING_SOLDER));

    public static final Quest ABYSSAL_ALLOY = CHAPTER.quest("abyssal_alloy", q -> q
            .at(22, 16)
            .title("渊狱合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AbyssalAlloy, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.TungstenCarbide, 16)
            .dependsOn(NewUvChapter.MUTATED_LIVING_SOLDER, BARNARDA_AIR));

    public static final Quest ABYSSAL_COIL = CHAPTER.quest("abyssalalloy_coil_block", q -> q
            .at(22, 14)
            .title("渊狱合金线圈")
            .task(Task.item(GTOBlocks.ABYSSALALLOY_COIL_BLOCK, 16))
            .scaledReward(GTOItems.MICA_INSULATOR_FOIL, 16)
            .dependsOn(ABYSSAL_ALLOY));

    public static final Quest LARGE_ELECTROMAGNET = CHAPTER.quest("large_electromagnet", q -> q
            .at(6, 0)
            .milestone()
            .title("大型电磁工厂")
            .task(Task.item(GCYMMachines.LARGE_ELECTROMAGNET))
            .scaledReward(Items.NETHERITE_INGOT, 9)
            .dependsOn(NETHERITE));

    public static final Quest ENERGETIC_NETHERITE = CHAPTER.quest("energetic_netherite_ingot", q -> q
            .at(18, 12)
            .title("充能下界合金锭")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.EnergeticNetherite, 8))
            .scaledReward(TagPrefix.dust, GTOMaterials.EnergeticNetherite, 2)
            .dependsOn(LARGE_ELECTROMAGNET));

    public static final Quest HDCS = CHAPTER.quest("high_durability_compound_steel", q -> q
            .at(22, 12)
            .title("高强度复合钢")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.HighDurabilityCompoundSteel, 33))
            .scaledReward(TagPrefix.dust, GTMaterials.HSSS, 9)
            .dependsOn(ABYSSAL_COIL));

    public static final Quest TECH_COMPONENT_IN_ASSEMBLY_LINE3 = CHAPTER.quest("tech_component_in_assembly_line3", q -> q
            .at(20, 10)
            .title("装配线进阶部件III研究")
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineuhv))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest UHV_MOTOR = CHAPTER.quest("uhv_electric_motor", q -> q
            .at(20, 12)
            .title("UHV 马达")
            .task(Task.item(GTItems.ELECTRIC_MOTOR_UHV, 8))
            .scaledReward(TagPrefix.rodLong, GTOMaterials.EnergeticNetherite, 2)
            .dependsOn(HDCS, ENERGETIC_NETHERITE, TECH_COMPONENT_IN_ASSEMBLY_LINE3));

    public static final Quest UHV_PUMP = CHAPTER.quest("uhv_electric_pump", q -> q
            .at(18, 10)
            .title("UHV 电泵")
            .task(Task.item(GTItems.ELECTRIC_PUMP_UHV, 4))
            .scaledReward(GTItems.ELECTRIC_MOTOR_UHV, 1)
            .dependsOn(UHV_MOTOR));

    public static final QuestLink UHV_PUMP_COPY_1 = CHAPTER.copy(UHV_PUMP, 6, 6);

    public static final QuestLink UHV_PUMP_COPY_2 = CHAPTER.copy(UHV_PUMP, 2, 12);

    public static final Quest RTAN_WIRE = CHAPTER.quest("ruthenium_trinium_americium_neutronate_wire", q -> q
            .at(8, 6)
            .title("1x 钌三钛镅中子素酸盐导线")
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.RutheniumTriniumAmericiumNeutronate, 64))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.RutheniumTriniumAmericiumNeutronate, 4)
            .dependsOn(UHV_PUMP_COPY_1));

    public static final Quest FLUXED_ELECTRUM = CHAPTER.quest("fluxed_electrum", q -> q
            .at(2, 10)
            .title("通流琥珀金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.FluxedElectrum, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.RoseGold, 8)
            .dependsOn(UHV_PUMP_COPY_2));

    public static final QuestLink FLUXED_ELECTRUM_COPY_1 = CHAPTER.copy(FLUXED_ELECTRUM, -4, 18);

    public static final Quest UHV_PARTS = CHAPTER.quest("uhv_parts", q -> q
            .at(4, 10)
            .title("UHV 部件")
            .icon(GTItems.ROBOT_ARM_UHV)
            .task(Task.item(GTItems.ELECTRIC_PISTON_UHV, 8), Task.item(GTItems.ROBOT_ARM_UHV, 8), Task.item(GTItems.CONVEYOR_MODULE_UHV, 2), Task.item(GTItems.EMITTER_UHV, 8), Task.item(GTItems.SENSOR_UHV, 8), Task.item(GTItems.FIELD_GENERATOR_UHV, 1))
            .scaledReward(GTItems.ELECTRIC_MOTOR_UHV, 2)
            .dependsOn(RTAN_WIRE, FLUXED_ELECTRUM));

    public static final QuestLink UHV_PARTS_COPY_1 = CHAPTER.copy(UHV_PARTS, 14, 16);

    public static final Quest TANTALLOY_61 = CHAPTER.quest("tantalloy_61", q -> q
            .at(20, 8)
            .title("钽钨合金-61")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Tantalloy61, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Tantalum, 8)
            .dependsOn(NewUvChapter.ROBOT_MK1));

    public static final QuestLink TANTALLOY_61_COPY_1 = CHAPTER.copy(TANTALLOY_61, 8, 10);

    public static final Quest HUI_CIRCUIT_4 = CHAPTER.quest("hui_circuit_4", q -> q
            .at(6, 10)
            .title("HUI 电路 4")
            .task(Task.item(GTOItems.HUI_CIRCUIT_4, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.GarnetRed, 8)
            .dependsOn(UHV_PARTS, TANTALLOY_61_COPY_1));

    public static final QuestLink HUI_CIRCUIT_4_COPY_1 = CHAPTER.copy(HUI_CIRCUIT_4, 4, 2);

    public static final Quest UHV_VOLTAGE_COIL = CHAPTER.quest("uhv_voltage_coil", q -> q
            .at(18, 8)
            .title("UHV 电压线圈")
            .task(Task.item(GTOItems.UHV_VOLTAGE_COIL, 28))
            .scaledReward(TagPrefix.rod, GTOMaterials.EnergeticNetherite, 4)
            .dependsOn(TANTALLOY_61));

    public static final QuestLink UHV_VOLTAGE_COIL_COPY_1 = CHAPTER.copy(UHV_VOLTAGE_COIL, 6, 4);

    public static final Quest ADVANCED_FUSION_COIL = CHAPTER.quest("advanced_fusion_coil", q -> q
            .at(8, 2)
            .title("先进聚变线圈")
            .task(Task.item(GTOBlocks.ADVANCED_FUSION_COIL, 6))
            .scaledReward(TagPrefix.plate, GTMaterials.Duranium, 4)
            .dependsOn(NewUvChapter.CIRCUIT_ASSEMBLY_LINE));

    public static final Quest FUSION_CASING_MK4 = CHAPTER.quest("fusion_casing_mk4", q -> q
            .at(6, 2)
            .title("聚变外壳 MK4")
            .task(Task.item(GTOBlocks.FUSION_CASING_MK4, 2))
            .scaledReward(TagPrefix.plate, GTOMaterials.Orichalcum, 6)
            .dependsOn(HUI_CIRCUIT_4_COPY_1, ADVANCED_FUSION_COIL, UHV_VOLTAGE_COIL_COPY_1));

    public static final Quest SPACE_COOLANT_30K = CHAPTER.quest("space_coolant_cell_30k", q -> q
            .at(8, 4)
            .title("30K 空间冷却单元")
            .task(Task.item(GTOItems.SPACE_COOLANT_CELL_30K, 8))
            .scaledReward(GTOItems.SPACE_COOLANT_CELL_10K, 3)
            .dependsOn(FUSION_CASING_MK4));

    public static final Quest RUTHERFORDIUM = CHAPTER.quest("rutherfordium", q -> q
            .at(20, 0)
            .title("𬬻")
            .task(Task.item(TagPrefix.dust, GTMaterials.Rutherfordium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Actinium, 2)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest BIOWARE_MAINFRAME = CHAPTER.quest("bioactivity_processor_controller", q -> q
            .at(10, -2)
            .title("生物活性处理器主机")
            .task(Task.item(GTOItems.BIOWARE_MAINFRAME, 16))
            .scaledReward(GTItems.GRAVI_STAR, 1)
            .dependsOn(NewUvChapter.BIOWARE_COMPUTER));

    public static final QuestLink BIOWARE_MAINFRAME_COPY_1 = CHAPTER.copy(BIOWARE_MAINFRAME, 10, 4);

    public static final QuestLink BIOWARE_MAINFRAME_COPY_2 = CHAPTER.copy(BIOWARE_MAINFRAME, 12, 18);

    public static final Quest GRISIUM_AND_TITANIUM_CARBIDES = CHAPTER.quest("grisium_and_titanium_carbides", q -> q
            .at(10, 2)
            .title("灰钛合金与碳化钛")
            .icon(TagPrefix.plate, GTOMaterials.Grisium)
            .task(Task.item(TagPrefix.plate, GTOMaterials.Grisium, 32), Task.item(TagPrefix.plateDouble, GTMaterials.TitaniumCarbide, 16), Task.item(TagPrefix.plateDouble, GTMaterials.TitaniumTungstenCarbide, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.TitaniumCarbide, 8)
            .dependsOn(NewUvChapter.BIOWARE_COMPUTER));

    public static final Quest RUTHERFORDIUM_BOULE = CHAPTER.quest("rutherfordium_amprosium_boule", q -> q
            .at(20, 2)
            .title("𬬻安普洛晶锭")
            .task(Task.item(GTOItems.RUTHERFORDIUM_AMPROSIUM_BOULE))
            .scaledReward(TagPrefix.dust, GTMaterials.Rutherfordium, 4)
            .dependsOn(RUTHERFORDIUM));

    public static final Quest LARGE_ENGRAVING_LASER = CHAPTER.quest("large_engraving_laser", q -> q
            .at(10, 0)
            .milestone()
            .title("大型激光蚀刻机")
            .task(Task.item(GCYMMachines.LARGE_ENGRAVING_LASER))
            .scaledReward(TagPrefix.plate, GTOMaterials.FluxedElectrum, 8)
            .dependsOn(BIOWARE_MAINFRAME, GRISIUM_AND_TITANIUM_CARBIDES));

    public static final Quest TECH_LASER_ENGRAVER = CHAPTER.quest("tech_laser_engraver", q -> q
            .at(14, 2)
            .title("极细尺度激光导向刻蚀研究")
            .task(Task.techNode(BaseNodes.LaserFoundations), Task.techNode(BaseNodes.LaserEngraver))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest ENGRAVING_LASER_PLANT = CHAPTER.quest("engraving_laser_plant", q -> q
            .at(14, 0)
            .milestone()
            .title("激光蚀刻工厂")
            .icon(MultiBlockA.ENGRAVING_LASER_PLANT)
            .task(Task.item(MultiBlockA.ENGRAVING_LASER_PLANT))
            .scaledReward(TagPrefix.plate, GTOMaterials.FluxedElectrum, 8)
            .dependsOn(LARGE_ENGRAVING_LASER, TECH_LASER_ENGRAVER));

    public static final Quest RUTHERFORDIUM_WAFER = CHAPTER.quest("rutherfordium_amprosium_wafer", q -> q
            .at(20, 4)
            .title("𬬻安普洛晶圆")
            .task(Task.item(GTOItems.RUTHERFORDIUM_AMPROSIUM_WAFER, 16))
            .scaledReward(GTOMaterials.FlocculentWater.getBucket(), 4)
            .dependsOn(RUTHERFORDIUM_BOULE));

    public static final QuestLink RUTHERFORDIUM_WAFER_COPY_1 = CHAPTER.copy(RUTHERFORDIUM_WAFER, 2, 4);

    public static final Quest MASK_AND_PHOTORESIST = CHAPTER.quest("lithography_mask_and_photoresist", q -> q
            .at(18, 2)
            .title("光刻掩模与光刻胶")
            .icon(GTOItems.LITHOGRAPHY_MASK)
            .task(Task.item(GTOItems.LITHOGRAPHY_MASK), Task.item(GTOMaterials.Photoresist.getBucket()))
            .scaledReward(GTOMaterials.Photoresist.getBucket(), 4)
            .dependsOn(ENGRAVING_LASER_PLANT));

    public static final QuestLink MASK_AND_PHOTORESIST_COPY_1 = CHAPTER.copy(MASK_AND_PHOTORESIST, 0, 4);

    public static final Quest NM_WAFER = CHAPTER.quest("nm_wafer", q -> q
            .at(18, 4)
            .title("NM 晶圆")
            .task(Task.item(GTOItems.NM_WAFER, 16))
            .scaledReward(GTOItems.RUTHERFORDIUM_AMPROSIUM_WAFER, 4)
            .dependsOn(MASK_AND_PHOTORESIST, RUTHERFORDIUM_WAFER));

    public static final Quest NM_CHIP = CHAPTER.quest("nm_chip", q -> q
            .at(18, 6)
            .title("NM 芯片")
            .task(Task.item(GTOItems.NM_CHIP, 64))
            .scaledReward(GTOMaterials.FlocculentWater.getBucket(), 8)
            .dependsOn(NM_WAFER));

    public static final Quest TECH_ENERGY_IO_UHV = CHAPTER.quest("tech_energy_io_uhv", q -> q
            .at(16, 10)
            .title("高压能量输入输出IV研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.UHV]))
            .scaledReward(GTItems.TOOL_DATA_MODULE, 1)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest UHV_ENERGY_HATCH = CHAPTER.quest("uhv_energy_input_hatch", q -> q
            .at(16, 8)
            .milestone()
            .title("UHV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.UHV], 12))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.Europium, 4)
            .dependsOn(NM_CHIP, UHV_PUMP, UHV_VOLTAGE_COIL, TECH_ENERGY_IO_UHV));

    public static final Quest TECH_TOKAMAK_FUSION_REACTOR4 = CHAPTER.quest("tech_tokamak_fusion_reactor4", q -> q
            .at(12, 4)
            .title("托卡马克聚变反应堆IV研究")
            .task(Task.techNode(BaseNodes.TokamakFusionReactor4))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest UHV_FUSION_REACTOR = CHAPTER.quest("uhv_fusion_reactor", q -> q
            .at(10, 8)
            .milestone()
            .title("UHV 聚变反应堆")
            .task(Task.item(MultiBlockD.FUSION_REACTOR[GTValues.UHV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadria, 8)
            .dependsOn(UHV_ENERGY_HATCH, SPACE_COOLANT_30K, RTAN_WIRE, BIOWARE_MAINFRAME_COPY_1, TECH_TOKAMAK_FUSION_REACTOR4));

    public static final Quest GADOLINIUM = CHAPTER.quest("gadolinium", q -> q
            .at(12, 10)
            .title("钆")
            .task(Task.item(TagPrefix.dust, GTMaterials.Gadolinium, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder, 6)
            .dependsOn(UHV_FUSION_REACTOR));

    public static final Quest LIVERMORIUM = CHAPTER.quest("livermorium", q -> q
            .at(4, 4)
            .title("鉝")
            .task(Task.item(GTMaterials.Livermorium.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Thorium.getBucket(), 2)
            .dependsOn(UHV_FUSION_REACTOR));

    public static final Quest FLUIX_EXQUISITE = CHAPTER.quest("exquisite_fluix", q -> q
            .at(12, 6)
            .title("精致福鲁伊克斯宝石")
            .task(Task.item(TagPrefix.gemExquisite, GTOMaterials.Fluix, 16))
            .scaledReward(TagPrefix.gem, GTOMaterials.Fluix, 16)
            .dependsOn(UHV_FUSION_REACTOR));

    public static final Quest LEMURITE = CHAPTER.quest("lemurite_bucket", q -> q
            .at(4, 6)
            .title("利莫里亚")
            .icon(GTOMaterials.Lemurite.getBucket())
            .task(Task.item(GTOMaterials.Lemurite.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadria, 8)
            .dependsOn(LIVERMORIUM));

    public static final Quest QUANTANIUM = CHAPTER.quest("quantanium_ingot", q -> q
            .at(4, 8)
            .milestone()
            .title("量子合金")
            .task(Task.item(TagPrefix.ingot, GTOMaterials.Quantanium, 64))
            .scaledReward(TagPrefix.dust, GTOMaterials.Mithril, 4)
            .dependsOn(LEMURITE));

    public static final Quest UEV_CASING = CHAPTER.quest("uev_casing", q -> q
            .at(-2, 14)
            .title("UEV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_UEV))
            .scaledReward(TagPrefix.plate, GTOMaterials.Quantanium, 2)
            .dependsOn(QUANTANIUM));

    public static final Quest MITHRIL_CABLE = CHAPTER.quest("mithril_cable", q -> q
            .at(-2, 12)
            .title("1x 秘银线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTOMaterials.Mithril, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTOMaterials.Mithril, 2)
            .dependsOn(QUANTANIUM));

    public static final Quest UEV_HULL = CHAPTER.quest("welcome_to_the_extreme_overpressure_stage", q -> q
            .at(0, 14)
            .milestone()
            .title("欢迎来到{极超压}阶段", DARK_GREEN)
            .task(Task.item(GTMachines.HULL[GTValues.UEV]))
            .reward(
                    Reward.item(GTMachines.HULL[GTValues.UEV], 1),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(UEV_CASING, MITHRIL_CABLE));

    public static final Quest ORICHALCUM_NANITES = CHAPTER.quest("orichalcum_nanites", q -> q
            .at(12, -2)
            .title("山铜纳米蜂群")
            .task(Task.item(GTOTagPrefix.NANITES, GTOMaterials.Orichalcum, 1))
            .scaledReward(TagPrefix.ingot, GTOMaterials.Orichalcum, 8)
            .dependsOn(NewUvChapter.UHV_HULL, NewUvChapter.NANO_FORGE));

    public static final Quest PCB_FACTORY = CHAPTER.quest("pcb_factory", q -> q
            .at(12, 0)
            .milestone()
            .title("PCB工厂")
            .task(Task.item(MultiBlockD.PCB_FACTORY))
            .scaledReward(TagPrefix.foil, GTMaterials.Ruthenium, 8)
            .dependsOn(ORICHALCUM_NANITES, NewUvChapter.TECH_CIRCUIT_ASSEMBLY_LINE));

    public static final Quest KEVLAR_PLATE = CHAPTER.quest("kevlar_plate", q -> q
            .at(14, 10)
            .title("凯芙拉板")
            .task(Task.item(TagPrefix.plate, GTOMaterials.Kevlar, 2))
            .scaledReward(GTOMaterials.PolyurethaneResin.getBucket(), 1)
            .dependsOn(UHV_ENERGY_HATCH));

    public static final Quest OPTICAL_PCB = CHAPTER.quest("optical_printed_circuit_board", q -> q
            .at(14, 8)
            .milestone()
            .title("光学印刷电路板")
            .task(Task.item(GTOItems.OPTICAL_PRINTED_CIRCUIT_BOARD, 2))
            .scaledReward(TagPrefix.foil, GTMaterials.Rhodium, 8)
            .dependsOn(PCB_FACTORY, KEVLAR_PLATE));

    public static final Quest DUBNIUM_AND_SEABORGIUM = CHAPTER.quest("dubnium_and_seaborgium", q -> q
            .at(10, 10)
            .title("𬭊与𬭳")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Dubnium, 4), Task.item(TagPrefix.ingot, GTMaterials.Seaborgium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Europium, 4)
            .dependsOn(UHV_FUSION_REACTOR));

    public static final QuestLink DUBNIUM_AND_SEABORGIUM_COPY_1 = CHAPTER.copy(DUBNIUM_AND_SEABORGIUM, -2, 4);

    public static final Quest OPTICAL_SMD = CHAPTER.quest("optical_smd", q -> q
            .at(8, 8)
            .milestone()
            .title("光学贴片元件")
            .icon(GTOItems.SMD_DIODE_OPTICAL)
            .task(Task.item(GTOItems.SMD_DIODE_OPTICAL, 16), Task.item(GTOItems.SMD_RESISTOR_OPTICAL, 16), Task.item(GTOItems.SMD_TRANSISTOR_OPTICAL, 16), Task.item(GTOItems.SMD_INDUCTOR_OPTICAL, 16))
            .scaledReward(GTOMaterials.Polyimide.getBucket(), 1)
            .dependsOn(DUBNIUM_AND_SEABORGIUM));

    public static final Quest ELECTRON_SOURCE = CHAPTER.quest("electron_source", q -> q
            .at(-4, 10)
            .title("电子源")
            .task(Task.item(GTOItems.ELECTRON_SOURCE))
            .scaledReward(TagPrefix.dust, GTMaterials.Niobium, 4)
            .dependsOn(UHV_ENERGY_HATCH));

    public static final Quest NON_LINEAR_OPTICAL_LENS = CHAPTER.quest("non_linear_optical_lens", q -> q
            .at(-4, 12)
            .title("非线性光学透镜")
            .task(Task.item(GTOItems.NON_LINEAR_OPTICAL_LENS, 3))
            .scaledReward(GTMaterials.Xenon.getBucket(), 1)
            .dependsOn(ELECTRON_SOURCE));

    public static final Quest FREQUENCY_LASERS = CHAPTER.quest("frequency_lasers", q -> q
            .at(-4, 14)
            .title("三频激光器")
            .icon(GTOItems.HIGH_FREQUENCY_LASER)
            .task(Task.item(GTOItems.LOW_FREQUENCY_LASER), Task.item(GTOItems.MEDIUM_FREQUENCY_LASER), Task.item(GTOItems.HIGH_FREQUENCY_LASER, 2))
            .scaledReward(GTOItems.SMD_DIODE_OPTICAL, 4)
            .dependsOn(NON_LINEAR_OPTICAL_LENS, OPTICAL_SMD));

    public static final Quest PHOTONIC_KRISTALLITE = CHAPTER.quest("photonic_kristallite", q -> q
            .at(0, 18)
            .title("光透域")
            .task(Task.item(TagPrefix.foil, GTOMaterials.PhotonicKristallite, 4))
            .scaledReward(GTOMaterials.Quantanium.getBucket(), 1)
            .dependsOn(FREQUENCY_LASERS, QUANTANIUM));

    public static final QuestLink PHOTONIC_KRISTALLITE_COPY_1 = CHAPTER.copy(PHOTONIC_KRISTALLITE, 10, 12);

    public static final Quest OPTICAL_SMD_CAPACITOR = CHAPTER.quest("optical_smd_capacitor", q -> q
            .at(2, 18)
            .title("光学贴片电容")
            .task(Task.item(GTOItems.SMD_CAPACITOR_OPTICAL, 16))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Dubnium, 8)
            .dependsOn(PHOTONIC_KRISTALLITE));

    public static final Quest SOLAR_LIGHT_SPLITTER = CHAPTER.quest("solar_light_splitter", q -> q
            .at(6, 12)
            .title("太阳光分光器")
            .task(Task.item(GTOItems.SOLAR_LIGHT_SPLITTER, 4))
            .scaledReward(TagPrefix.lens, GTMaterials.Ruby, 2)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest IRIDIUM_NANITES = CHAPTER.quest("iridium_nanites", q -> q
            .at(6, 18)
            .title("铱纳米蜂群")
            .task(Task.item(GTOTagPrefix.NANITES, GTMaterials.Iridium, 1))
            .scaledReward(TagPrefix.block, GTMaterials.Iridium, 2)
            .dependsOn(NewUvChapter.UHV_HULL, NewUvChapter.NANO_FORGE));

    public static final Quest BOSE_EINSTEIN_COOLING_CONTAINER = CHAPTER.quest("bose_einstein_cooling_container", q -> q
            .at(6, 16)
            .title("玻色-爱因斯坦冷却容器")
            .task(Task.item(GTOItems.BOSE_EINSTEIN_COOLING_CONTAINER))
            .scaledReward(TagPrefix.dust, GTMaterials.Rubidium, 2)
            .dependsOn(UHV_ENERGY_HATCH, NewUvChapter.LARGE_VOID_MINER, IRIDIUM_NANITES));

    public static final Quest INDUSTRIAL_SPACE_STATION = CHAPTER.quest("industrial_space_station_core_module", q -> q
            .at(16, -4)
            .title("工业空间站核心舱")
            .task(Task.item(SpaceMultiblock.LARGE_EXPANDABLE_SPACE_STATION_CORE_MODULE))
            .scaledReward(GTItems.SENSOR_IV, 2)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest STATION_DOCKING_MODULE = CHAPTER.quest("space_station_docking_module", q -> q
            .at(16, -2)
            .title("工业空间站六向衔接舱")
            .task(Task.item(SpaceMultiblock.SPACE_STATION_DOCKING_MODULE))
            .scaledReward(GTItems.ELECTRIC_PUMP_IV, 2)
            .dependsOn(INDUSTRIAL_SPACE_STATION));

    public static final Quest ORBITAL_FINE_MATERIALS_FACTORY = CHAPTER.quest("orbital_fine_materials_factory", q -> q
            .at(16, 0)
            .milestone()
            .title("轨道超材料锻造工厂舱")
            .task(Task.item(SpaceMultiblock.ORBITAL_FINE_MATERIALS_FACTORY))
            .scaledReward(GTMaterials.Methane.getBucket(), 4)
            .dependsOn(STATION_DOCKING_MODULE));

    public static final Quest RHENIUM = CHAPTER.quest("rhenium", q -> q
            .at(-2, 6)
            .title("铼")
            .task(Task.item(TagPrefix.plate, GTMaterials.Rhenium, 1))
            .scaledReward(TagPrefix.dust, GTMaterials.Rhenium, 2)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest CARBON_NANOTUBES = CHAPTER.quest("carbon_nanotubes", q -> q
            .at(0, 6)
            .title("碳纳米管与𬭳掺杂纳米管")
            .task(Task.item(TagPrefix.dust, GTOMaterials.CarbonNanotubes, 1), Task.item(GTOMaterials.SeaborgiumDopedNanotubes.getBucket()))
            .scaledReward(GTOMaterials.Cycloparaphenylene.getBucket(), 1)
            .dependsOn(ORBITAL_FINE_MATERIALS_FACTORY, RHENIUM, DUBNIUM_AND_SEABORGIUM_COPY_1));

    public static final QuestLink CARBON_NANOTUBES_COPY_1 = CHAPTER.copy(CARBON_NANOTUBES, 8, 18);

    public static final Quest FULLERENE = CHAPTER.quest("fullerene", q -> q
            .at(10, 16)
            .title("富勒烯")
            .task(Task.item(TagPrefix.dust, GTOMaterials.Fullerene, 1))
            .scaledReward(GTMaterials.Nitrogen.getBucket(), 16)
            .dependsOn(UHV_ENERGY_HATCH));

    public static final Quest FULLERENE_DOPED_NANOTUBES = CHAPTER.quest("fullerene_doped_nanotubes", q -> q
            .at(8, 16)
            .title("富勒烯掺杂纳米管")
            .icon(GTOMaterials.FullereneDopedNanotubes.getBucket())
            .task(Task.item(GTOMaterials.FullereneDopedNanotubes.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Methane.getBucket(), 8)
            .dependsOn(CARBON_NANOTUBES_COPY_1, FULLERENE));

    public static final Quest PHOTON_CARRYING_WAFER = CHAPTER.quest("photon_carrying_wafer", q -> q
            .at(2, 6)
            .title("光子承载晶圆")
            .task(Task.item(GTOItems.PHOTON_CARRYING_WAFER, 2))
            .scaledReward(GTOItems.RUTHERFORDIUM_AMPROSIUM_WAFER, 2)
            .dependsOn(MASK_AND_PHOTORESIST_COPY_1, RUTHERFORDIUM_WAFER_COPY_1));

    public static final QuestLink PHOTON_CARRYING_WAFER_COPY_1 = CHAPTER.copy(PHOTON_CARRYING_WAFER, -6, 12);

    public static final Quest HIGH_ENERGY_LASER_PURIFICATION_UNIT = CHAPTER.quest("high_energy_laser_purification_unit", q -> q
            .at(4, 0)
            .milestone()
            .title("高能激光净化装置")
            .task(Task.item(MultiBlockB.HIGH_ENERGY_LASER_PURIFICATION_UNIT))
            .scaledReward(GTOMaterials.ExtremeTemperatureWater.getBucket(), 4)
            .dependsOn(NewUvChapter.EXTREME_TEMPERATURE_WATER));

    public static final Quest ELECTRIC_EQUILIBRIUM_WATER = CHAPTER.quest("electric_equilibrium_water", q -> q
            .at(-2, 8)
            .title("电平衡水")
            .icon(GTOMaterials.ElectricEquilibriumWater.getBucket())
            .task(Task.item(GTOMaterials.ElectricEquilibriumWater.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.ExtremeTemperatureWater.getBucket(), 4)
            .dependsOn(HIGH_ENERGY_LASER_PURIFICATION_UNIT));

    public static final Quest OPTICAL_WAFER = CHAPTER.quest("optical_wafer", q -> q
            .at(2, 8)
            .milestone()
            .title("光学晶圆")
            .task(Task.item(GTOItems.OPTICAL_WAFER))
            .scaledReward(GTOItems.PHOTON_CARRYING_WAFER, 1)
            .dependsOn(PHOTON_CARRYING_WAFER, CARBON_NANOTUBES));

    public static final Quest OPTICAL_SLICE = CHAPTER.quest("optical_slice", q -> q
            .at(0, 8)
            .milestone()
            .title("光学裸片")
            .task(Task.item(GTOItems.OPTICAL_SLICE, 16))
            .scaledReward(GTOMaterials.ElectricEquilibriumWater.getBucket(), 1)
            .dependsOn(OPTICAL_WAFER, ELECTRIC_EQUILIBRIUM_WATER));

    public static final Quest OPTICAL_SOC_HOUSING = CHAPTER.quest("optical_soc_containment_housing", q -> q
            .at(4, 12)
            .title("光学 SoC 密封外壳")
            .task(Task.item(GTOItems.OPTICAL_SOC_CONTAINMENT_HOUSING, 2))
            .scaledReward(TagPrefix.bolt, GTMaterials.Darmstadtium, 4)
            .dependsOn(NewUvChapter.UHV_HULL));

    public static final Quest SIMPLE_OPTICAL_SOC = CHAPTER.quest("simple_optical_soc", q -> q
            .at(4, 14)
            .milestone()
            .title("简易光学 SoC")
            .task(Task.item(GTOItems.SIMPLE_OPTICAL_SOC, 8))
            .scaledReward(GTOItems.OPTICAL_SLICE, 2)
            .dependsOn(OPTICAL_SLICE, OPTICAL_SOC_HOUSING));

    public static final Quest OPTICAL_RAM_CHIP = CHAPTER.quest("optical_ram_chip", q -> q
            .at(-6, 14)
            .title("光学 RAM 芯片")
            .task(Task.item(GTOItems.OPTICAL_RAM_CHIP, 32))
            .scaledReward(GTOMaterials.PHNeutralWater.getBucket(), 1)
            .dependsOn(PHOTON_CARRYING_WAFER_COPY_1));

    public static final Quest SUNNARIUM = CHAPTER.quest("sunnarium", q -> q
            .at(-6, 16)
            .title("阳光化合物")
            .task(Task.item(TagPrefix.dustSmall, GTOMaterials.Sunnarium, 1))
            .scaledReward(TagPrefix.dust, GTMaterials.Glowstone, 16)
            .dependsOn(UHV_ENERGY_HATCH));

    public static final Quest OPTICAL_DATA_STICK = CHAPTER.quest("optical_data_stick", q -> q
            .at(-4, 16)
            .title("光学数据棒")
            .task(Task.item(GTOItems.OPTICAL_DATA_STICK, 4))
            .scaledReward(GTOItems.OPTICAL_RAM_CHIP, 4)
            .dependsOn(OPTICAL_PCB, OPTICAL_RAM_CHIP, SUNNARIUM, FLUXED_ELECTRUM_COPY_1));

    public static final Quest TECH_OPTICAL_TECH = CHAPTER.quest("tech_optical_tech", q -> q
            .at(8, 12)
            .title("光学计算技术")
            .task(Task.techNode(BaseNodes.OpticalTech))
            .scaledReward(GTOItems.NEURAL_MATRIX, 1)
            .dependsOn(NewUvChapter.UHV_HULL, NewUvChapter.TECH_BIOWARE_TECH));

    public static final Quest OPTICAL_PROCESSING_CORE = CHAPTER.quest("optical_processing_core", q -> q
            .at(6, 14)
            .milestone()
            .title("光学处理核心")
            .task(Task.item(GTOItems.OPTICAL_PROCESSING_CORE, 8))
            .scaledReward(GTOItems.OPTICAL_RAM_CHIP, 8)
            .dependsOn(OPTICAL_DATA_STICK, OPTICAL_SMD_CAPACITOR, SIMPLE_OPTICAL_SOC, SOLAR_LIGHT_SPLITTER, BOSE_EINSTEIN_COOLING_CONTAINER, FULLERENE_DOPED_NANOTUBES, TECH_OPTICAL_TECH));

    public static final Quest OPTICAL_PROCESSOR = CHAPTER.quest("optical_processor", q -> q
            .at(10, 14)
            .milestone()
            .title("光学处理器")
            .task(Task.item(GTOItems.OPTICAL_PROCESSOR, 4))
            .scaledReward(GTOItems.SMD_TRANSISTOR_OPTICAL, 8)
            .dependsOn(OPTICAL_PROCESSING_CORE));

    public static final Quest ROBOT_MK2 = CHAPTER.quest("precision_circuit_assembly_robot_mk2", q -> q
            .at(12, 16)
            .title("精密电路组装机器人 MK2")
            .task(Task.item(GTOItems.PRECISION_CIRCUIT_ASSEMBLY_ROBOT_MK2))
            .scaledReward(GTItems.ROBOT_ARM_UHV, 1)
            .dependsOn(BIOWARE_MAINFRAME_COPY_2, UHV_PARTS_COPY_1));

    public static final Quest OPTICAL_COMPUTING_CHANNEL = CHAPTER.quest("highly_concurrent_intensive_optical_computing_channel", q -> q
            .at(12, 12)
            .title("高并发光计算通道")
            .task(Task.item(GTOItems.HIGHLY_CONCURRENT_INTENSIVE_OPTICAL_COMPUTING_CHANNEL))
            .scaledReward(TagPrefix.foil, GTOMaterials.PhotonicKristallite, 2)
            .dependsOn(PHOTONIC_KRISTALLITE_COPY_1));

    public static final Quest OPTICAL_ASSEMBLY = CHAPTER.quest("optical_assembly", q -> q
            .at(12, 14)
            .milestone()
            .title("光学处理器集群")
            .task(Task.item(GTOItems.OPTICAL_ASSEMBLY, 3))
            .scaledReward(GTOItems.OPTICAL_RAM_CHIP, 16)
            .dependsOn(OPTICAL_PROCESSOR, ROBOT_MK2, OPTICAL_COMPUTING_CHANNEL));

    public static final Quest OPTICAL_COMPUTER = CHAPTER.quest("optical_computer", q -> q
            .at(16, 14)
            .milestone()
            .title("光学处理器超级计算机")
            .task(Task.item(GTOItems.OPTICAL_COMPUTER))
            .scaledReward(GTOItems.OPTICAL_PRINTED_CIRCUIT_BOARD, 1)
            .dependsOn(OPTICAL_ASSEMBLY));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewUvChapter.UHV_HULL, 0, 0))
                .mainLine(NewUvChapter.UHV_HULL, HIGH_ENERGY_LASER_PURIFICATION_UNIT, LARGE_ELECTROMAGNET, LARGE_ENGRAVING_LASER, PCB_FACTORY, ENGRAVING_LASER_PLANT, ORBITAL_FINE_MATERIALS_FACTORY, UHV_ENERGY_HATCH, OPTICAL_PCB, UHV_FUSION_REACTOR, OPTICAL_SMD, QUANTANIUM, OPTICAL_WAFER, OPTICAL_SLICE, UEV_HULL, SIMPLE_OPTICAL_SOC, OPTICAL_PROCESSING_CORE, OPTICAL_PROCESSOR, OPTICAL_ASSEMBLY, OPTICAL_COMPUTER));
    }
}
