package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.ManaMachine;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockC;

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
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewUlvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_ulv", c -> c
            .title("3. 超低压时代(ULV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.ULV]));

    public static final Quest MOTOR = CHAPTER.quest("motor", q -> q
            .at(8, 4)
            .title("ULV 马达")
            .task(Task.item(GTOItems.ULV_ELECTRIC_MOTOR, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final QuestLink MOTOR_COPY_1 = CHAPTER.copy(MOTOR, 16, 0);

    public static final QuestLink MOTOR_COPY_2 = CHAPTER.copy(MOTOR, 12, 10);

    public static final QuestLink MOTOR_COPY_3 = CHAPTER.copy(MOTOR, -2, 12);

    public static final Quest VACUUM_PUMP = CHAPTER.quest("vacuum_pump", q -> q
            .at(6, -4)
            .title("低压蒸汽真空泵")
            .task(Task.item(GTOMachines.STEAM_VACUUM_PUMP.first()))
            .scaledReward(GTOItems.RAW_VACUUM_TUBE, 1)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final QuestLink VACUUM_PUMP_COPY_1 = CHAPTER.copy(VACUUM_PUMP, 14, -4);

    public static final Quest VACUUM_PUMP_PRODUCT = CHAPTER.quest("vacuum_pump_product", q -> q
            .at(6, -2)
            .title("巨型青铜管道")
            .task(Task.item(TagPrefix.pipeHugeFluid, GTMaterials.Bronze))
            .scaledReward(GTOItems.RAW_VACUUM_TUBE, 1)
            .dependsOn(VACUUM_PUMP));

    public static final Quest VACUUM_TUBE = CHAPTER.quest("vacuum_tube", q -> q
            .at(6, 0)
            .milestone()
            .title("真空管")
            .task(Task.item(GTItems.VACUUM_TUBE, 4))
            .scaledReward(GTItems.VACUUM_TUBE, 1)
            .dependsOn(VACUUM_PUMP_PRODUCT));

    public static final Quest THERMAL_GENERATOR = CHAPTER.quest("thermal_generator", q -> q
            .at(6, 2)
            .title("ULV 热力发电机")
            .task(Task.item(GTOMachines.THERMAL_GENERATOR[GTValues.ULV]))
            .scaledReward(Items.COAL, 8)
            .dependsOn(MOTOR, VACUUM_TUBE));

    public static final Quest PISTON = CHAPTER.quest("piston", q -> q
            .at(10, 6)
            .title("ULV 活塞")
            .task(Task.item(GTOItems.ULV_ELECTRIC_PISTON, 2))
            .scaledReward(GTOItems.ULV_ELECTRIC_PISTON, 2)
            .dependsOn(MOTOR));

    public static final QuestLink PISTON_COPY_1 = CHAPTER.copy(PISTON, 2, 12);

    public static final Quest ROBOT_ARM = CHAPTER.quest("robot_arm", q -> q
            .at(10, 4)
            .title("ULV 机械臂")
            .task(Task.item(GTOItems.ULV_ROBOT_ARM, 2))
            .scaledReward(GTOItems.ULV_ROBOT_ARM, 2)
            .dependsOn(PISTON));

    public static final QuestLink ROBOT_ARM_COPY_1 = CHAPTER.copy(ROBOT_ARM, 8, 12);

    public static final Quest CONVEYOR = CHAPTER.quest("conveyor", q -> q
            .at(8, 2)
            .title("ULV 传送带")
            .task(Task.item(GTOItems.ULV_CONVEYOR_MODULE, 2))
            .scaledReward(GTOItems.ULV_CONVEYOR_MODULE, 2)
            .dependsOn(MOTOR));

    public static final QuestLink CONVEYOR_COPY_1 = CHAPTER.copy(CONVEYOR, 12, 12);

    public static final Quest PUMP = CHAPTER.quest("pump", q -> q
            .at(14, 10)
            .title("ULV 泵")
            .task(Task.item(GTOItems.ULV_ELECTRIC_PUMP, 3))
            .scaledReward(GTOItems.ULV_ELECTRIC_PUMP, 2)
            .dependsOn(MOTOR_COPY_2));

    public static final QuestLink PUMP_COPY_1 = CHAPTER.copy(PUMP, -2, 4);

    public static final Quest FLUID_REGULATOR = CHAPTER.quest("fluid_regulator", q -> q
            .at(14, 8)
            .title("ULV 流体校准器")
            .task(Task.item(GTOItems.ULV_FLUID_REGULATOR, 2))
            .scaledReward(GTOItems.ULV_FLUID_REGULATOR, 2)
            .dependsOn(PUMP, VACUUM_TUBE));

    public static final Quest ASSEMBLER = CHAPTER.quest("assembler", q -> q
            .at(10, 2)
            .title("ULV 组装机")
            .task(Task.item(GTOMachines.ULV_ASSEMBLER[GTValues.ULV]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.RedAlloy, 2)
            .dependsOn(ROBOT_ARM, CONVEYOR));

    public static final QuestLink ASSEMBLER_COPY_1 = CHAPTER.copy(ASSEMBLER, 12, -2);

    public static final QuestLink ASSEMBLER_COPY_2 = CHAPTER.copy(ASSEMBLER, 0, 4);

    public static final Quest STEAM_SEPARATOR = CHAPTER.quest("steam_separator", q -> q
            .at(2, 0)
            .milestone()
            .title("蒸汽分离机")
            .task(
                    Task.item(MultiBlockA.STEAM_SEPARATOR),
                    Task.item(GTMachines.STEAM_HATCH),
                    Task.item(GTMachines.STEAM_IMPORT_BUS),
                    Task.item(GTMachines.STEAM_EXPORT_BUS),
                    Task.item(GTOMachines.STEAM_FLUID_INPUT_HATCH),
                    Task.item(GTOMachines.STEAM_FLUID_OUTPUT_HATCH, 3),
                    Task.item(GTOMachines.STEAM_VENT_HATCH))
            .scaledReward(GTItems.STICKY_RESIN, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest GLUE = CHAPTER.quest("glue", q -> q
            .at(10, -2)
            .title("胶水")
            .task(Task.item(GTMaterials.Glue.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTItems.STICKY_RESIN, 8)
            .dependsOn(STEAM_SEPARATOR));

    public static final QuestLink GLUE_COPY_1 = CHAPTER.copy(GLUE, 14, 0);

    public static final Quest CARBON = CHAPTER.quest("carbon", q -> q
            .at(8, -2)
            .title("碳粉")
            .task(Task.item(TagPrefix.dust, GTMaterials.Carbon, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.DarkAsh, 4)
            .dependsOn(STEAM_SEPARATOR));

    public static final Quest RESISTOR = CHAPTER.quest("resistor", q -> q
            .at(10, 0)
            .milestone()
            .title("电阻")
            .task(Task.item(GTItems.RESISTOR, 3))
            .scaledReward(GTItems.RESISTOR, 1)
            .dependsOn(GLUE, CARBON, ASSEMBLER));

    public static final Quest PACKER = CHAPTER.quest("packer", q -> q
            .at(10, 12)
            .title("ULV 打包机")
            .task(Task.item(GTOMachines.ULV_PACKER[GTValues.ULV]))
            .scaledReward(TagPrefix.ingot, GTMaterials.Tin, 4)
            .dependsOn(RESISTOR, ROBOT_ARM_COPY_1, CONVEYOR_COPY_1));

    public static final Quest TIN_CABLE = CHAPTER.quest("tin_cable", q -> q
            .at(10, 10)
            .title("锡线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Tin, 6))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.Tin, 1)
            .dependsOn(PACKER));

    public static final QuestLink TIN_CABLE_COPY_1 = CHAPTER.copy(TIN_CABLE, 8, 10);

    public static final QuestLink TIN_CABLE_COPY_2 = CHAPTER.copy(TIN_CABLE, -2, 8);

    public static final Quest OXYGEN = CHAPTER.quest("oxygen", q -> q
            .at(14, 4)
            .title("氧")
            .icon(GTMaterials.Oxygen.getBucket())
            .task(Task.item(GTOItems.AIR_VENT), Task.item(GTMaterials.Oxygen.getBucket()))
            .scaledReward(Items.IRON_INGOT, 4)
            .dependsOn(STEAM_SEPARATOR));

    public static final Quest CHEMICAL_REACTOR = CHAPTER.quest("chemical_reactor", q -> q
            .at(16, 2)
            .title("ULV 化学反应釜")
            .task(Task.item(GTOMachines.ULV_CHEMICAL_REACTOR[GTValues.ULV]))
            .scaledReward(Items.OAK_PLANKS, 4)
            .dependsOn(MOTOR_COPY_1));

    public static final QuestLink CHEMICAL_REACTOR_COPY_1 = CHAPTER.copy(CHEMICAL_REACTOR, 6, 10);

    public static final Quest COATED_BOARD = CHAPTER.quest("coated_board", q -> q
            .at(14, 2)
            .title("覆膜板")
            .task(Task.item(GTItems.COATED_BOARD))
            .scaledReward(GTItems.COATED_BOARD, 1)
            .dependsOn(CHEMICAL_REACTOR, OXYGEN));

    public static final Quest HP_VACUUM_PUMP = CHAPTER.quest("hp_vacuum_pump", q -> q
            .at(14, -2)
            .title("高压蒸汽真空泵")
            .task(Task.item(GTOMachines.STEAM_VACUUM_PUMP.second()))
            .scaledReward(TagPrefix.foil, GTMaterials.Copper, 4)
            .dependsOn(VACUUM_PUMP_COPY_1));

    public static final Quest CIRCUIT_BOARD = CHAPTER.quest("circuit_board", q -> q
            .at(12, 0)
            .milestone()
            .title("覆膜印刷电路基板")
            .task(Task.item(GTItems.BASIC_CIRCUIT_BOARD))
            .scaledReward(GTItems.VACUUM_TUBE, 2)
            .dependsOn(COATED_BOARD, GLUE_COPY_1, ASSEMBLER_COPY_1, HP_VACUUM_PUMP));

    public static final Quest LV_CIRCUIT = CHAPTER.quest("lv_circuit", q -> q
            .at(10, 8)
            .milestone()
            .title("基础电子电路")
            .task(Task.item(GTItems.ELECTRONIC_CIRCUIT_LV, 4))
            .scaledReward(GTItems.ELECTRONIC_CIRCUIT_LV, 1)
            .dependsOn(TIN_CABLE, CIRCUIT_BOARD, VACUUM_TUBE, RESISTOR));

    public static final Quest HEATER = CHAPTER.quest("heater", q -> q
            .at(-2, 0)
            .title("加热器")
            .task(Task.item(GTOMachines.HEATER, 2))
            .scaledReward(Items.COAL, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final QuestLink HEATER_COPY_1 = CHAPTER.copy(HEATER, 2, 10);

    public static final Quest LARGE_BRONZE_BOILER = CHAPTER.quest("large_bronze_boiler", q -> q
            .at(8, 8)
            .milestone()
            .title("大型青铜锅炉")
            .task(Task.item(GTMultiMachines.LARGE_BOILER_BRONZE))
            .scaledReward(Items.COAL, 8)
            .dependsOn(TIN_CABLE_COPY_1, LV_CIRCUIT));

    public static final Quest ALCHEMY_CAULDRON = CHAPTER.quest("alchemy_cauldron", q -> q
            .at(-2, 2)
            .title("炼金锅")
            .task(Task.item(ManaMachine.ALCHEMY_CAULDRON))
            .scaledReward(TagPrefix.dust, GTMaterials.Coal, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest COAL_SLURRY = CHAPTER.quest("coal_slurry", q -> q
            .at(0, 2)
            .title("煤浆")
            .task(Task.item(GTOMaterials.CoalSlurry.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Coal, 4)
            .dependsOn(ALCHEMY_CAULDRON, HEATER));

    public static final Quest WATER_GAS = CHAPTER.quest("water_gas", q -> q
            .at(6, 4)
            .title("水煤气")
            .task(Task.item(GTOMaterials.WaterGas.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.COAL, 4)
            .dependsOn(LARGE_BRONZE_BOILER, COAL_SLURRY));

    public static final Quest HYDROGEN = CHAPTER.quest("hydrogen", q -> q
            .at(6, 6)
            .title("氢")
            .task(Task.item(ModItems.HYDROGEN_BUCKET.get()).optional(), Task.checkmark())
            .scaledReward(ModItems.STEEL_PLATE.get(), 4)
            .dependsOn(WATER_GAS, STEAM_SEPARATOR));

    public static final Quest HEAT_HATCH = CHAPTER.quest("heat_hatch", q -> q
            .at(2, 4)
            .title("导热仓")
            .task(Task.item(GTOMachines.HEAT_HATCH, 2))
            .scaledReward(ModItems.STEEL_PLATE.get(), 4)
            .dependsOn(ASSEMBLER_COPY_2));

    public static final Quest ULV_FLUID_HATCH = CHAPTER.quest("ulv_fluid_hatch", q -> q
            .at(4, -2)
            .title("ULV 流体仓")
            .task(Task.item(GTMachines.FLUID_IMPORT_HATCH[GTValues.ULV]), Task.item(GTMachines.FLUID_EXPORT_HATCH[GTValues.ULV], 3))
            .scaledReward(GTItems.STICKY_RESIN, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest DISTILLATION_TOWER = CHAPTER.quest("distillation_tower", q -> q
            .at(4, 0)
            .milestone()
            .title("原始蒸馏塔")
            .task(Task.item(MultiBlockC.PRIMITIVE_DISTILLATION_TOWER))
            .scaledReward(ModItems.STEEL_PLATE.get(), 8)
            .dependsOn(ULV_FLUID_HATCH));

    public static final Quest RAW_OIL = CHAPTER.quest("raw_oil", q -> q
            .at(4, 12)
            .title("原油")
            .task(Task.item(GTMaterials.RawOil.getBucket()))
            .scaledReward(Items.BUCKET, 4)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest SULFURIC_NAPHTHA = CHAPTER.quest("sulfuric_naphtha", q -> q
            .at(4, 10)
            .title("含硫石脑油")
            .task(Task.item(GTMaterials.SulfuricNaphtha.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.RawOil.getBucket(), 1)
            .dependsOn(RAW_OIL, DISTILLATION_TOWER));

    public static final Quest NAPHTHA = CHAPTER.quest("naphtha", q -> q
            .at(6, 8)
            .milestone()
            .title("石脑油")
            .task(Task.item(GTMaterials.Naphtha.getBucket()).optional(), Task.checkmark())
            .scaledReward(ModItems.HYDROGEN_BUCKET.get(), 1)
            .dependsOn(SULFURIC_NAPHTHA, HYDROGEN, CHEMICAL_REACTOR_COPY_1));

    public static final Quest STEAM_CRACKER = CHAPTER.quest("steam_cracker", q -> q
            .at(12, 8)
            .milestone()
            .title("蒸汽裂化机")
            .task(Task.item(MultiBlockC.STEAM_CRACKER))
            .scaledReward(Items.COAL, 8)
            .dependsOn(FLUID_REGULATOR));

    public static final Quest CRACKED_NAPHTHA = CHAPTER.quest("cracked_naphtha", q -> q
            .at(4, 6)
            .title("重度蒸汽裂化石脑油")
            .task(Task.item(GTMaterials.SeverelySteamCrackedNaphtha.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Naphtha.getBucket(), 1)
            .dependsOn(STEAM_CRACKER, NAPHTHA));

    public static final Quest ETHYLENE = CHAPTER.quest("ethylene", q -> q
            .at(4, 4)
            .title("乙烯")
            .task(Task.item(GTMaterials.Ethylene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ethylene.getBucket(), 1)
            .dependsOn(CRACKED_NAPHTHA, DISTILLATION_TOWER));

    public static final Quest LATHE = CHAPTER.quest("lathe", q -> q
            .at(0, 12)
            .title("ULV 车床")
            .task(Task.item(GTOMachines.ULV_LATHE[GTValues.ULV]))
            .scaledReward(TagPrefix.gem, GTMaterials.Ruby, 1)
            .dependsOn(MOTOR_COPY_3, PISTON_COPY_1));

    public static final Quest RUBY_ROD = CHAPTER.quest("ruby_rod", q -> q
            .at(0, 10)
            .title("红宝石杆")
            .task(Task.item(TagPrefix.rod, GTMaterials.Ruby))
            .scaledReward(TagPrefix.rod, GTMaterials.Steel, 2)
            .dependsOn(LATHE));

    public static final Quest POLYETHYLENE = CHAPTER.quest("polyethylene", q -> q
            .at(2, 8)
            .milestone()
            .title("聚乙烯")
            .task(Task.item(GTOItems.HEAT_INTERFACE), Task.item(GTMaterials.Polyethylene.getBucket()))
            .scaledReward(ModItems.STEEL_PLATE.get(), 2)
            .dependsOn(ETHYLENE, RUBY_ROD, HEAT_HATCH, HEATER_COPY_1));

    public static final Quest FLUID_SOLIDIFIER = CHAPTER.quest("fluid_solidifier", q -> q
            .at(-2, 6)
            .title("ULV 流体固化器")
            .task(Task.item(GTOMachines.ULV_FLUID_SOLIDIFIER[GTValues.ULV]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.Tin, 1)
            .dependsOn(PUMP_COPY_1));

    public static final Quest POLYETHYLENE_PLATE = CHAPTER.quest("polyethylene_plate", q -> q
            .at(0, 6)
            .title("聚乙烯板")
            .task(Task.item(TagPrefix.plate, GTMaterials.Polyethylene, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.Polyethylene, 2)
            .dependsOn(FLUID_SOLIDIFIER, POLYETHYLENE));

    public static final Quest LV_CASING = CHAPTER.quest("lv_casing", q -> q
            .at(-2, 10)
            .title("LV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_LV))
            .scaledReward(ModItems.STEEL_PLATE.get(), 2)
            .dependsOn(NewSteamChapter.ULV_HULL));

    public static final Quest LV_HULL = CHAPTER.quest("lv_hull", q -> q
            .at(0, 8)
            .milestone()
            .title("LV 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.LV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.LV], 1))
            .dependsOn(LV_CASING, POLYETHYLENE_PLATE, TIN_CABLE_COPY_2));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewSteamChapter.ULV_HULL, 0, 0))
                .mainLine(NewSteamChapter.ULV_HULL, STEAM_SEPARATOR, DISTILLATION_TOWER, VACUUM_TUBE, RESISTOR, CIRCUIT_BOARD, STEAM_CRACKER, LV_CIRCUIT, LARGE_BRONZE_BOILER, NAPHTHA, POLYETHYLENE, LV_HULL));
    }
}
