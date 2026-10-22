package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;

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

public final class NewLvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_lv", c -> c
            .title("4. 低压时代(LV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.LV]));

    public static final Quest POLARIZER = CHAPTER.quest("polarizer", q -> q
            .at(0, -2)
            .title("LV 磁化机")
            .task(Task.item(GTMachines.POLARIZER[GTValues.LV]))
            .scaledReward(TagPrefix.rod, GTMaterials.Steel, 2)
            .dependsOn(NewUlvChapter.LV_HULL));

    public static final Quest MOTOR = CHAPTER.quest("motor", q -> q
            .at(2, -2)
            .title("LV 马达")
            .task(Task.item(GTItems.ELECTRIC_MOTOR_LV, 4))
            .scaledReward(GTItems.ELECTRIC_MOTOR_LV, 1)
            .dependsOn(POLARIZER));

    public static final QuestLink MOTOR_COPY_1 = CHAPTER.copy(MOTOR, 10, 6);

    public static final QuestLink MOTOR_COPY_2 = CHAPTER.copy(MOTOR, 6, 4);

    public static final QuestLink MOTOR_COPY_3 = CHAPTER.copy(MOTOR, 6, -2);

    public static final QuestLink MOTOR_COPY_4 = CHAPTER.copy(MOTOR, 12, -4);

    public static final QuestLink MOTOR_COPY_5 = CHAPTER.copy(MOTOR, 18, -4);

    public static final QuestLink MOTOR_COPY_6 = CHAPTER.copy(MOTOR, 18, -2);

    public static final QuestLink MOTOR_COPY_7 = CHAPTER.copy(MOTOR, 14, 12);

    public static final QuestLink MOTOR_COPY_8 = CHAPTER.copy(MOTOR, -6, 2);

    public static final Quest STEAM_TURBINE = CHAPTER.quest("steam_turbine", q -> q
            .at(2, -4)
            .title("LV 蒸汽轮机")
            .task(Task.item(GTMachines.STEAM_TURBINE[GTValues.LV], 2))
            .scaledReward(Items.COAL, 16)
            .dependsOn(MOTOR));

    public static final Quest PISTON = CHAPTER.quest("piston", q -> q
            .at(10, 4)
            .title("LV 活塞")
            .task(Task.item(GTItems.ELECTRIC_PISTON_LV, 2))
            .scaledReward(GTItems.ELECTRIC_PISTON_LV, 1)
            .dependsOn(MOTOR_COPY_1));

    public static final QuestLink PISTON_COPY_1 = CHAPTER.copy(PISTON, 20, 4);

    public static final QuestLink PISTON_COPY_2 = CHAPTER.copy(PISTON, -6, 4);

    public static final Quest ROBOT_ARM = CHAPTER.quest("robot_arm", q -> q
            .at(10, 2)
            .title("LV 机械臂")
            .task(Task.item(GTItems.ROBOT_ARM_LV, 2))
            .scaledReward(GTItems.ROBOT_ARM_LV, 1)
            .dependsOn(PISTON));

    public static final QuestLink ROBOT_ARM_COPY_1 = CHAPTER.copy(ROBOT_ARM, 8, -4);

    public static final Quest CHEMICAL_REACTOR = CHAPTER.quest("chemical_reactor", q -> q
            .at(4, -2)
            .title("LV 化学反应釜")
            .task(Task.item(GTMachines.CHEMICAL_REACTOR[GTValues.LV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 2)
            .dependsOn(MOTOR));

    public static final QuestLink CHEMICAL_REACTOR_COPY_1 = CHAPTER.copy(CHEMICAL_REACTOR, 18, 6);

    public static final QuestLink CHEMICAL_REACTOR_COPY_2 = CHAPTER.copy(CHEMICAL_REACTOR, 10, 10);

    public static final QuestLink CHEMICAL_REACTOR_COPY_3 = CHAPTER.copy(CHEMICAL_REACTOR, 6, 10);

    public static final QuestLink CHEMICAL_REACTOR_COPY_4 = CHAPTER.copy(CHEMICAL_REACTOR, 12, 16);

    public static final Quest LIQUID_RUBBER = CHAPTER.quest("liquid_rubber", q -> q
            .at(14, 16)
            .title("液态橡胶")
            .task(Task.item(GTMaterials.Rubber.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTItems.STICKY_RESIN, 4)
            .dependsOn(CHEMICAL_REACTOR_COPY_4));

    public static final Quest CONVEYOR = CHAPTER.quest("conveyor", q -> q
            .at(6, 2)
            .title("LV 传送带")
            .task(Task.item(GTItems.CONVEYOR_MODULE_LV, 2))
            .scaledReward(GTItems.CONVEYOR_MODULE_LV, 1)
            .dependsOn(MOTOR_COPY_2));

    public static final QuestLink CONVEYOR_COPY_1 = CHAPTER.copy(CONVEYOR, 10, -4);

    public static final QuestLink CONVEYOR_COPY_2 = CHAPTER.copy(CONVEYOR, -2, 6);

    public static final QuestLink CONVEYOR_COPY_3 = CHAPTER.copy(CONVEYOR, 14, 18);

    public static final Quest PUMP = CHAPTER.quest("pump", q -> q
            .at(14, 10)
            .title("LV 泵")
            .task(Task.item(GTItems.ELECTRIC_PUMP_LV))
            .scaledReward(GTItems.ELECTRIC_PUMP_LV, 1)
            .dependsOn(MOTOR_COPY_7));

    public static final QuestLink PUMP_COPY_1 = CHAPTER.copy(PUMP, -4, 6);

    public static final QuestLink PUMP_COPY_2 = CHAPTER.copy(PUMP, -2, 8);

    public static final QuestLink PUMP_COPY_3 = CHAPTER.copy(PUMP, 18, 18);

    public static final Quest ASSEMBLER = CHAPTER.quest("assembler", q -> q
            .at(8, 2)
            .title("LV 组装机")
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.LV]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.Tin, 4)
            .dependsOn(ROBOT_ARM, CONVEYOR));

    public static final QuestLink ASSEMBLER_COPY_1 = CHAPTER.copy(ASSEMBLER, 12, 2);

    public static final QuestLink ASSEMBLER_COPY_2 = CHAPTER.copy(ASSEMBLER, 4, 6);

    public static final Quest WIREMILL = CHAPTER.quest("wiremill", q -> q
            .at(8, -2)
            .title("LV 线材轧机")
            .task(Task.item(GTMachines.WIREMILL[GTValues.LV]))
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 4)
            .dependsOn(MOTOR_COPY_3));

    public static final Quest LOOM = CHAPTER.quest("loom", q -> q
            .at(-4, 2)
            .title("LV 织布机")
            .task(Task.item(GTOMachines.LOOM[GTValues.LV]))
            .scaledReward(TagPrefix.ingot, GTMaterials.Nickel, 4)
            .dependsOn(MOTOR_COPY_8));

    public static final Quest ARC_FURNACE = CHAPTER.quest("arc_furnace", q -> q
            .at(-2, 0)
            .title("LV 电弧炉")
            .task(Task.item(GTMachines.ARC_FURNACE[GTValues.LV]))
            .scaledReward(TagPrefix.ingot, GTMaterials.Copper, 4)
            .dependsOn(NewUlvChapter.LV_HULL));

    public static final Quest ELECTROLYZER = CHAPTER.quest("electrolyzer", q -> q
            .at(2, 2)
            .title("LV 电解机")
            .task(Task.item(GTMachines.ELECTROLYZER[GTValues.LV]))
            .scaledReward(TagPrefix.dust, GTMaterials.SiliconDioxide, 3)
            .dependsOn(NewUlvChapter.LV_HULL));

    public static final QuestLink ELECTROLYZER_COPY_1 = CHAPTER.copy(ELECTROLYZER, 20, 6);

    public static final QuestLink ELECTROLYZER_COPY_2 = CHAPTER.copy(ELECTROLYZER, -4, 0);

    public static final Quest ELECTRIC_HEATER = CHAPTER.quest("electric_heater", q -> q
            .at(-2, 2)
            .title("电力加热器")
            .task(Task.item(GTOMachines.ELECTRIC_HEATER))
            .scaledReward(TagPrefix.dust, GTMaterials.Tin, 4)
            .dependsOn(LOOM, ARC_FURNACE, ELECTROLYZER_COPY_2));

    public static final Quest MIXER = CHAPTER.quest("mixer", q -> q
            .at(14, -4)
            .title("LV 搅拌机")
            .task(Task.item(GTMachines.MIXER[GTValues.LV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Mica, 6)
            .dependsOn(MOTOR_COPY_4));

    public static final QuestLink MIXER_COPY_1 = CHAPTER.copy(MIXER, 20, -4);

    public static final QuestLink MIXER_COPY_2 = CHAPTER.copy(MIXER, 20, 12);

    public static final QuestLink MIXER_COPY_3 = CHAPTER.copy(MIXER, 0, 2);

    public static final Quest EXTRACTOR = CHAPTER.quest("extractor", q -> q
            .at(-4, 4)
            .title("LV 提取机")
            .task(Task.item(GTMachines.EXTRACTOR[GTValues.LV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Tin, 4)
            .dependsOn(PISTON_COPY_2, PUMP_COPY_1));

    public static final Quest LIQUID_TIN = CHAPTER.quest("liquid_tin", q -> q
            .at(-2, 4)
            .title("液态锡")
            .task(Task.item(GTMaterials.Tin.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Iron, 4)
            .dependsOn(EXTRACTOR));

    public static final Quest TIN_ALLOY = CHAPTER.quest("tin_alloy", q -> q
            .at(0, 4)
            .title("液态锡铁合金")
            .task(Task.item(GTMaterials.TinAlloy.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.wireGtDouble, GTMaterials.Cupronickel, 2)
            .dependsOn(MIXER_COPY_3, LIQUID_TIN, ELECTRIC_HEATER));

    public static final Quest MICA_PULP = CHAPTER.quest("mica_pulp", q -> q
            .at(20, -2)
            .title("云母浆")
            .task(Task.item(GTOItems.MICA_BASED_PULP, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Asbestos, 2)
            .dependsOn(MIXER_COPY_1));

    public static final Quest FORMING_PRESS_LV = CHAPTER.quest("forming_press_lv", q -> q
            .at(20, 2)
            .title("LV 冲压机床")
            .task(Task.item(GTMachines.FORMING_PRESS[GTValues.LV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Asbestos, 4)
            .dependsOn(PISTON_COPY_1));

    public static final Quest MICA_SHEET = CHAPTER.quest("mica_sheet", q -> q
            .at(20, 0)
            .title("云母基板")
            .icon(GTOItems.MICA_BASED_SHEET)
            .task(Task.item(GTOItems.MICA_BASED_SHEET, 5))
            .scaledReward(TagPrefix.dust, GTMaterials.Asbestos, 4)
            .dependsOn(FORMING_PRESS_LV, MICA_PULP));

    public static final Quest ALLOY_SMELTER_LV = CHAPTER.quest("alloy_smelter_lv", q -> q
            .at(18, 4)
            .title("LV 合金炉")
            .task(Task.item(GTMachines.ALLOY_SMELTER[GTValues.LV]))
            .scaledReward(TagPrefix.dust, GTMaterials.SiliconDioxide, 4)
            .dependsOn(NewUlvChapter.LV_HULL));

    public static final Quest MICA_INSULATOR_SHEET = CHAPTER.quest("mica_insulator_sheet", q -> q
            .at(18, 2)
            .title("云母绝缘板")
            .icon(GTOItems.MICA_INSULATOR_SHEET)
            .task(Task.item(GTOItems.MICA_INSULATOR_SHEET, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.SiliconDioxide, 4)
            .dependsOn(ALLOY_SMELTER_LV, MICA_SHEET));

    public static final Quest CLUSTER_LV = CHAPTER.quest("cluster_lv", q -> q
            .at(18, 0)
            .title("LV 多辊式轧机")
            .task(Task.item(GTOMachines.CLUSTER[GTValues.LV]))
            .scaledReward(TagPrefix.foil, GTMaterials.Bronze, 8)
            .dependsOn(MOTOR_COPY_6));

    public static final Quest MICA_FOIL = CHAPTER.quest("mica_foil", q -> q
            .at(16, 0)
            .milestone()
            .title("云母绝缘箔")
            .icon(GTOItems.MICA_INSULATOR_FOIL)
            .task(Task.item(GTOItems.MICA_INSULATOR_FOIL, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.Bronze, 8)
            .dependsOn(CLUSTER_LV, MICA_INSULATOR_SHEET));

    public static final Quest COIL = CHAPTER.quest("coil", q -> q
            .at(4, 4)
            .title("白铜线圈")
            .task(Task.item(GTBlocks.COIL_CUPRONICKEL, 16))
            .scaledReward(TagPrefix.ingot, GTMaterials.Invar, 6)
            .dependsOn(ASSEMBLER_COPY_2, TIN_ALLOY, MICA_FOIL));

    public static final Quest ELECTRIC_BLAST_FURNACE = CHAPTER.quest("electric_blast_furnace", q -> q
            .at(8, 8)
            .milestone()
            .title("电力高炉")
            .task(
                    Task.item(GTMultiMachines.ELECTRIC_BLAST_FURNACE),
                    Task.item(GTBlocks.CASING_INVAR_HEATPROOF, 9),
                    Task.item(GTMachines.ITEM_IMPORT_BUS[GTValues.LV]),
                    Task.item(GTMachines.ITEM_EXPORT_BUS[GTValues.LV]),
                    Task.item(GTMachines.MAINTENANCE_HATCH),
                    Task.item(GTMachines.MUFFLER_HATCH[GTValues.LV]))
            .scaledReward(TagPrefix.gem, GTMaterials.Sapphire, 4)
            .dependsOn(COIL));

    public static final Quest ENERGY_HATCH = CHAPTER.quest("energy_hatch", q -> q
            .at(8, 0)
            .milestone()
            .title("LV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LV], 2))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.Tin, 4)
            .dependsOn(ASSEMBLER, WIREMILL));

    public static final Quest ALUMINIUM = CHAPTER.quest("aluminium", q -> q
            .at(2, 8)
            .milestone()
            .title("铝锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Aluminium, 9))
            .scaledReward(TagPrefix.ingot, GTMaterials.Aluminium, 2)
            .dependsOn(ELECTRIC_BLAST_FURNACE, ENERGY_HATCH));

    public static final Quest MV_CASING = CHAPTER.quest("mv_casing", q -> q
            .at(16, 12)
            .title("MV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_MV))
            .scaledReward(TagPrefix.plate, GTMaterials.Aluminium, 1)
            .dependsOn(ALUMINIUM));

    public static final Quest LAMINATOR = CHAPTER.quest("laminator", q -> q
            .at(16, 18)
            .title("LV 过胶机")
            .task(Task.item(GTOMachines.LAMINATOR[GTValues.LV]))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Copper, 2)
            .dependsOn(CONVEYOR_COPY_3, PUMP_COPY_3));

    public static final Quest COPPER_CABLE = CHAPTER.quest("copper_cable", q -> q
            .at(16, 16)
            .title("铜线缆")
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Copper, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.Polyethylene, 2)
            .dependsOn(LAMINATOR, LIQUID_RUBBER));

    public static final Quest MV_HULL = CHAPTER.quest("mv_hull", q -> q
            .at(16, 14)
            .milestone()
            .title("MV 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.MV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.MV], 1))
            .dependsOn(MV_CASING, COPPER_CABLE));

    public static final Quest CARBON_MONOXIDE = CHAPTER.quest("carbon_monoxide", q -> q
            .at(10, 12)
            .title("一氧化碳")
            .icon(GTMaterials.CarbonMonoxide.getBucket())
            .task(Task.item(GTMaterials.CarbonMonoxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Carbon, 4)
            .dependsOn(CHEMICAL_REACTOR_COPY_2));

    public static final Quest METHANOL = CHAPTER.quest("methanol", q -> q
            .at(12, 12)
            .title("甲醇")
            .task(Task.item(GTMaterials.Methanol.getBucket()).optional(), Task.checkmark())
            .scaledReward(ModItems.HYDROGEN_BUCKET.get(), 4)
            .dependsOn(CARBON_MONOXIDE));

    public static final Quest FORMALDEHYDE = CHAPTER.quest("formaldehyde", q -> q
            .at(12, 10)
            .title("甲醛")
            .task(Task.item(GTMaterials.Formaldehyde.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Silver, 1)
            .dependsOn(METHANOL));

    public static final Quest BRINE = CHAPTER.quest("brine", q -> q
            .at(20, 10)
            .title("盐水")
            .task(Task.item(GTMaterials.SaltWater.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Salt, 8)
            .dependsOn(MIXER_COPY_2));

    public static final Quest CHLORINE = CHAPTER.quest("chlorine", q -> q
            .at(20, 8)
            .title("氯")
            .task(Task.item(GTMaterials.Chlorine.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 6)
            .dependsOn(BRINE, ELECTROLYZER_COPY_1));

    public static final Quest SULFURIC_HEAVY_FUEL = CHAPTER.quest("sulfuric_heavy_fuel", q -> q
            .at(4, 2)
            .title("含硫重燃油")
            .task(Task.item(GTMaterials.SulfuricHeavyFuel.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.RawOil.getBucket(), 2)
            .dependsOn(NewUlvChapter.RAW_OIL));

    public static final Quest HEAVY_FUEL = CHAPTER.quest("heavy_fuel", q -> q
            .at(4, 0)
            .milestone()
            .title("重燃油")
            .task(Task.item(GTMaterials.HeavyFuel.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Hydrogen.getBucket(), 1)
            .dependsOn(SULFURIC_HEAVY_FUEL, CHEMICAL_REACTOR, ELECTROLYZER));

    public static final Quest LV_DISTILLERY = CHAPTER.quest("lv_distillery", q -> q
            .at(16, 10)
            .title("LV 蒸馏室")
            .task(Task.item(GTMachines.DISTILLERY[GTValues.LV]))
            .scaledReward(GTMaterials.HeavyFuel.getBucket(), 1)
            .dependsOn(PUMP));

    public static final Quest BENZENE = CHAPTER.quest("benzene", q -> q
            .at(18, 10)
            .title("苯")
            .task(Task.item(GTMaterials.Benzene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SeverelySteamCrackedNaphtha.getBucket(), 2)
            .dependsOn(HEAVY_FUEL, LV_DISTILLERY));

    public static final Quest DICHLOROBENZENE = CHAPTER.quest("dichlorobenzene", q -> q
            .at(18, 8)
            .title("二氯苯")
            .task(Task.item(GTMaterials.Dichlorobenzene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(CHLORINE, BENZENE, CHEMICAL_REACTOR_COPY_1));

    public static final QuestLink DICHLOROBENZENE_COPY_1 = CHAPTER.copy(DICHLOROBENZENE, -2, 16);

    public static final Quest PHENOL = CHAPTER.quest("phenol", q -> q
            .at(16, 8)
            .milestone()
            .title("苯酚")
            .task(Task.item(GTMaterials.Phenol.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 6)
            .dependsOn(DICHLOROBENZENE));

    public static final Quest HYDROCHLORIC_ACID = CHAPTER.quest("hydrochloric_acid", q -> q
            .at(-2, 14)
            .title("盐酸")
            .task(Task.item(GTMaterials.HydrochloricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Iron, 4)
            .dependsOn(DICHLOROBENZENE_COPY_1));

    public static final Quest IRON_CHLORIDE = CHAPTER.quest("iron_chloride", q -> q
            .at(0, 14)
            .title("氯化铁")
            .task(Task.item(GTMaterials.Iron3Chloride.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.foil, GTMaterials.Silver, 4)
            .dependsOn(HYDROCHLORIC_ACID));

    public static final Quest PHENOLIC_RESIN = CHAPTER.quest("phenolic_resin", q -> q
            .at(12, 8)
            .milestone()
            .title("酚醛树脂")
            .task(Task.item(GTOMaterials.PhenolicResin.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.foil, GTMaterials.Lead, 4)
            .dependsOn(PHENOL, FORMALDEHYDE));

    public static final Quest SULFUR_DIOXIDE = CHAPTER.quest("sulfur_dioxide", q -> q
            .at(6, 12)
            .title("二氧化硫")
            .icon(GTMaterials.SulfurDioxide.getBucket())
            .task(Task.item(GTMaterials.SulfurDioxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 4)
            .dependsOn(CHEMICAL_REACTOR_COPY_3));

    public static final Quest SULFUR_TRIOXIDE = CHAPTER.quest("sulfur_trioxide", q -> q
            .at(4, 12)
            .title("三氧化硫")
            .icon(GTMaterials.SulfurTrioxide.getBucket())
            .task(Task.item(GTMaterials.SulfurTrioxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 4)
            .dependsOn(SULFUR_DIOXIDE));

    public static final Quest SULFURIC_ACID = CHAPTER.quest("sulfuric_acid", q -> q
            .at(4, 10)
            .title("硫酸")
            .task(Task.item(GTMaterials.SulfuricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Sulfur, 4)
            .dependsOn(SULFUR_TRIOXIDE));

    public static final Quest COKE_OVEN = CHAPTER.quest("coke_oven", q -> q
            .at(0, 10)
            .title("焦炉")
            .task(Task.item(GTMultiMachines.COKE_OVEN))
            .scaledReward(Items.COAL, 16)
            .dependsOn(NewUlvChapter.LV_HULL));

    public static final Quest CREOSOTE = CHAPTER.quest("creosote", q -> q
            .at(0, 8)
            .title("杂酚油")
            .task(Task.item(GTMaterials.Creosote.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.COAL, 16)
            .dependsOn(COKE_OVEN));

    public static final Quest CHEMICAL_BATH_LV = CHAPTER.quest("chemical_bath_lv", q -> q
            .at(0, 6)
            .title("LV 化学浸洗机")
            .task(Task.item(GTMachines.CHEMICAL_BATH[GTValues.LV]))
            .scaledReward(GTBlocks.TREATED_WOOD_PLANK, 8)
            .dependsOn(CONVEYOR_COPY_2, PUMP_COPY_2));

    public static final Quest TREATED_WOOD = CHAPTER.quest("treated_wood", q -> q
            .at(2, 6)
            .title("防腐木板")
            .icon(TagPrefix.plate, GTMaterials.TreatedWood)
            .task(Task.item(TagPrefix.plate, GTMaterials.TreatedWood, 2))
            .scaledReward(GTBlocks.TREATED_WOOD_PLANK, 8)
            .dependsOn(CHEMICAL_BATH_LV, CREOSOTE));

    public static final Quest PHENOLIC_BOARD = CHAPTER.quest("phenolic_board", q -> q
            .at(4, 8)
            .milestone()
            .title("酚醛基板")
            .task(Task.item(GTItems.PHENOLIC_BOARD, 2))
            .scaledReward(TagPrefix.foil, GTMaterials.Lead, 8)
            .dependsOn(PHENOLIC_RESIN, SULFURIC_ACID, TREATED_WOOD));

    public static final Quest GOOD_CIRCUIT_BOARD = CHAPTER.quest("good_circuit_board", q -> q
            .at(2, 14)
            .milestone()
            .title("好的电路板")
            .task(Task.item(GTItems.GOOD_CIRCUIT_BOARD, 2))
            .scaledReward(GTItems.GOOD_CIRCUIT_BOARD, 1)
            .dependsOn(PHENOLIC_BOARD, IRON_CHLORIDE));

    public static final Quest GALLIUM = CHAPTER.quest("gallium", q -> q
            .at(12, -2)
            .title("镓")
            .task(Task.item(TagPrefix.dust, GTMaterials.Gallium))
            .scaledReward(TagPrefix.dust, GTMaterials.Realgar, 4)
            .dependsOn(NewSteamChapter.ANCIENT_WORLD));

    public static final Quest LV_CENTRIFUGE = CHAPTER.quest("lv_centrifuge", q -> q
            .at(16, -4)
            .title("LV 离心机")
            .task(Task.item(GTMachines.CENTRIFUGE[GTValues.LV]))
            .scaledReward(TagPrefix.dust, GTMaterials.Realgar, 4)
            .dependsOn(MOTOR_COPY_5));

    public static final Quest ARSENIC = CHAPTER.quest("arsenic", q -> q
            .at(16, -2)
            .title("砷")
            .task(Task.item(TagPrefix.dust, GTMaterials.Arsenic))
            .scaledReward(TagPrefix.dust, GTMaterials.Realgar, 4)
            .dependsOn(LV_CENTRIFUGE));

    public static final Quest GALLIUM_ARSENIDE = CHAPTER.quest("gallium_arsenide", q -> q
            .at(14, -2)
            .title("砷化镓")
            .task(Task.item(TagPrefix.dust, GTMaterials.GalliumArsenide, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.GalliumArsenide, 1)
            .dependsOn(GALLIUM, ARSENIC, MIXER));

    public static final Quest UNPACKER_LV = CHAPTER.quest("unpacker_lv", q -> q
            .at(10, -2)
            .title("LV 解包机")
            .task(Task.item(GTOMachines.UNPACKER[GTValues.LV]))
            .scaledReward(GTItems.DIODE, 2)
            .dependsOn(ROBOT_ARM_COPY_1, CONVEYOR_COPY_1));

    public static final Quest DIODE = CHAPTER.quest("diode", q -> q
            .at(12, 0)
            .milestone()
            .title("二极管")
            .icon(GTItems.DIODE)
            .task(Task.item(GTItems.DIODE, 4))
            .scaledReward(GTItems.DIODE, 2)
            .dependsOn(UNPACKER_LV, GALLIUM_ARSENIDE, ASSEMBLER_COPY_1));

    public static final Quest MV_CIRCUIT = CHAPTER.quest("mv_circuit", q -> q
            .at(10, 14)
            .milestone()
            .title("好的电子电路")
            .task(Task.item(GTItems.ELECTRONIC_CIRCUIT_MV, 4))
            .scaledReward(GTItems.ELECTRONIC_CIRCUIT_MV, 1)
            .dependsOn(GOOD_CIRCUIT_BOARD, DIODE));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewUlvChapter.LV_HULL, 0, 0))
                .mainLine(NewUlvChapter.LV_HULL, HEAVY_FUEL, ENERGY_HATCH, DIODE, MICA_FOIL, PHENOL, PHENOLIC_RESIN, ELECTRIC_BLAST_FURNACE, PHENOLIC_BOARD, ALUMINIUM, GOOD_CIRCUIT_BOARD, MV_CIRCUIT, MV_HULL));
    }
}
