package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockH;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.Items;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewIvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_iv", c -> c
            .title("8. 强导压时代(IV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.IV]));

    public static final Quest BASTNASITE = CHAPTER.quest("bastnasite", q -> q
            .at(2, -2)
            .title("氟碳铈矿")
            .icon(TagPrefix.rawOre, GTMaterials.Bastnasite)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Bastnasite, 64))
            .scaledReward(TagPrefix.rawOre, GTMaterials.Bastnasite, 16)
            .dependsOn(NewEvChapter.IV_HULL));

    public static final Quest DIGESTION_TANK = CHAPTER.quest("digestion_tank", q -> q
            .at(4, 0)
            .milestone()
            .title("煮解池")
            .task(Task.item(MultiBlockB.DIGESTION_TANK))
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 2)
            .dependsOn(BASTNASITE));

    public static final Quest FCLC_SOLUTION = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_solution", q -> q
            .at(4, -2)
            .title("氟碳镧铈溶液")
            .icon(GTOMaterials.FluoroCarbonLanthanideCeriumSolution.getBucket())
            .task(Task.item(GTOMaterials.FluoroCarbonLanthanideCeriumSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 2)
            .dependsOn(DIGESTION_TANK));

    public static final Quest CRACKER = CHAPTER.quest("cracker", q -> q
            .at(6, 0)
            .milestone()
            .title("裂化机")
            .task(Task.item(GTMultiMachines.CRACKER))
            .scaledReward(GTBlocks.COIL_CUPRONICKEL, 4)
            .dependsOn(FCLC_SOLUTION));

    public static final Quest STEAM_CRACKED_SLURRY = CHAPTER.quest("steam_cracked_fluoro_carbon_lanthanide_slurry", q -> q
            .at(14, -4)
            .title("蒸汽裂化氟碳镧铈浆")
            .icon(GTOMaterials.SteamCrackedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.SteamCrackedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTBlocks.COIL_CUPRONICKEL, 4)
            .dependsOn(CRACKER));

    public static final Quest MODULATED_SLURRY = CHAPTER.quest("modulated_fluoro_carbon_lanthanide_slurry", q -> q
            .at(14, -2)
            .title("改性氟碳镧铈浆")
            .icon(GTOMaterials.ModulatedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.ModulatedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 2)
            .dependsOn(STEAM_CRACKED_SLURRY));

    public static final Quest DISSOLVING_TANK = CHAPTER.quest("dissolving_tank", q -> q
            .at(16, 0)
            .milestone()
            .title("溶解罐")
            .task(Task.item(MultiBlockD.DISSOLVING_TANK))
            .scaledReward(TagPrefix.dust, GTMaterials.Saltpeter, 8)
            .dependsOn(MODULATED_SLURRY));

    public static final Quest DILUTED_SLURRY = CHAPTER.quest("diluted_fluoro_carbon_lanthanide_slurry", q -> q
            .at(16, -2)
            .title("稀释氟碳镧铈浆")
            .icon(GTOMaterials.DilutedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.DilutedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Saltpeter, 8)
            .dependsOn(DISSOLVING_TANK));

    public static final Quest FCLC_OXIDE = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_oxide", q -> q
            .at(18, -2)
            .title("氟碳镧铈氧化物粉")
            .icon(TagPrefix.dust, GTOMaterials.FluoroCarbonLanthanideCeriumOxidePowder)
            .task(Task.item(GTMachines.SIFTER[GTValues.HV]), Task.item(TagPrefix.dust, GTOMaterials.FluoroCarbonLanthanideCeriumOxidePowder, 18))
            .scaledReward(TagPrefix.dust, GTOMaterials.FluoroCarbonLanthanideCeriumOxidePowder, 4)
            .dependsOn(DILUTED_SLURRY));

    public static final Quest SUSPENSION = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_rare_earth_suspension", q -> q
            .at(20, -2)
            .title("氟碳镧铈稀土悬浊液")
            .icon(GTOMaterials.FluoroCarbonLanthanideCeriumRareEarthSuspension.getBucket())
            .task(Task.item(GTOMaterials.FluoroCarbonLanthanideCeriumRareEarthSuspension.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Acetone.getBucket(), 4)
            .dependsOn(FCLC_OXIDE, NewEvChapter.ACETONE));

    public static final QuestLink SUSPENSION_COPY_1 = CHAPTER.copy(SUSPENSION, 16, 6);

    public static final Quest SAMARIUM_CONCENTRATE = CHAPTER.quest("samarium_rare_earth_concentrate", q -> q
            .at(18, 6)
            .title("钐稀土精粉")
            .icon(TagPrefix.dust, GTOMaterials.SamariumRareEarthConcentratePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumRareEarthConcentratePowder, 9))
            .scaledReward(TagPrefix.dust, GTOMaterials.SamariumRareEarthConcentratePowder, 2)
            .dependsOn(SUSPENSION_COPY_1));

    public static final Quest PHOSPHORIC_ACID = CHAPTER.quest("phosphoric_acid", q -> q
            .at(16, 4)
            .title("磷酸")
            .icon(GTMaterials.PhosphoricAcid.getBucket())
            .task(Task.item(GTMaterials.PhosphoricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Apatite, 9)
            .dependsOn(SAMARIUM_CONCENTRATE));

    public static final Quest RARE_EARTH = CHAPTER.quest("rare_earth", q -> q
            .at(20, 6)
            .title("稀土")
            .icon(TagPrefix.dust, GTMaterials.RareEarth)
            .task(Task.item(TagPrefix.dust, GTMaterials.RareEarth, 32))
            .scaledReward(GTMaterials.HydrofluoricAcid.getBucket(), 4)
            .dependsOn(SAMARIUM_CONCENTRATE));

    public static final Quest RARE_EARTH_HYDROXIDES = CHAPTER.quest("rare_earth_hydroxides", q -> q
            .at(18, 4)
            .title("氢氧化稀土溶液")
            .icon(GTOMaterials.RareEarthHydroxides.getBucket())
            .task(Task.item(GTOMaterials.RareEarthHydroxides.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 15)
            .dependsOn(RARE_EARTH, PHOSPHORIC_ACID));

    public static final Quest RARE_EARTH_CHLORIDES = CHAPTER.quest("rare_earth_chlorides", q -> q
            .at(20, 4)
            .title("稀土氯化物")
            .icon(GTOMaterials.RareEarthChlorides.getBucket())
            .task(Task.item(GTOMaterials.RareEarthChlorides.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 4)
            .dependsOn(RARE_EARTH_HYDROXIDES));

    public static final Quest TBHO_SOLUTION = CHAPTER.quest("tb_ho_oxides_solution", q -> q
            .at(22, 4)
            .title("铽钬氧化物溶液")
            .icon(GTOMaterials.TbHoOxidesSolution.getBucket())
            .task(Task.item(GTItems.ITEM_MAGNET_HV), Task.item(GTOMaterials.TbHoOxidesSolution.getBucket()))
            .scaledReward(GTOMaterials.RareEarthChlorides.getBucket(), 4)
            .dependsOn(RARE_EARTH_CHLORIDES));

    public static final Quest DEHYDRATOR_HV = CHAPTER.quest("dehydrator_hv", q -> q
            .at(22, 2)
            .title("HV 脱水机")
            .task(Task.item(GTOMachines.DEHYDRATOR[GTValues.HV]))
            .scaledReward(GTOMaterials.TbHoOxidesSolution.getBucket(), 2)
            .dependsOn(TBHO_SOLUTION));

    public static final Quest YTTRIUM_OXIDE = CHAPTER.quest("yttrium_oxide", q -> q
            .at(20, 2)
            .title("氧化钇")
            .icon(TagPrefix.dust, GTOMaterials.YttriumOxide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.YttriumOxide, 5))
            .scaledReward(GTOMaterials.TbHoOxidesSolution.getBucket(), 2)
            .dependsOn(DEHYDRATOR_HV));

    public static final Quest YTTRIUM_NITRATE = CHAPTER.quest("yttrium_nitrate", q -> q
            .at(20, 0)
            .milestone()
            .title("硝酸钇")
            .icon(TagPrefix.dust, GTOMaterials.YttriumNitrate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.YttriumNitrate, 13))
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 4)
            .dependsOn(YTTRIUM_OXIDE));

    public static final Quest BIOMASS = CHAPTER.quest("biomass", q -> q
            .at(28, -6)
            .title("生物质")
            .icon(GTMaterials.Biomass.getBucket())
            .task(Task.item(GTMaterials.Biomass.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTItems.PLANT_BALL, 8)
            .dependsOn(NewEvChapter.LARGE_CHEMICAL_REACTOR));

    public static final Quest ETHANOL = CHAPTER.quest("ethanol", q -> q
            .at(30, -6)
            .title("乙醇")
            .icon(GTMaterials.Ethanol.getBucket())
            .task(Task.item(GTMaterials.Ethanol.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Biomass.getBucket(), 2)
            .dependsOn(BIOMASS));

    public static final Quest GLYCEROL = CHAPTER.quest("glycerol", q -> q
            .at(30, -4)
            .title("甘油")
            .icon(GTMaterials.Glycerol.getBucket())
            .task(Task.item(GTMaterials.Glycerol.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SeedOil.getBucket(), 4)
            .dependsOn(ETHANOL));

    public static final Quest CHROMIUM_TRIOXIDE = CHAPTER.quest("chromium_trioxide", q -> q
            .at(28, 0)
            .title("三氧化铬")
            .icon(TagPrefix.dust, GTMaterials.ChromiumTrioxide)
            .task(Task.item(TagPrefix.dust, GTMaterials.ChromiumTrioxide, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Chromium, 2)
            .dependsOn(NewEvChapter.LARGE_CHEMICAL_REACTOR));

    public static final Quest POTASSIUM_DICHROMATE = CHAPTER.quest("potassium_dichromate", q -> q
            .at(28, -2)
            .title("重铬酸钾")
            .icon(TagPrefix.dust, GTMaterials.PotassiumDichromate)
            .task(Task.item(TagPrefix.dust, GTMaterials.PotassiumDichromate, 11))
            .scaledReward(TagPrefix.dust, GTMaterials.Saltpeter, 10)
            .dependsOn(CHROMIUM_TRIOXIDE));

    public static final Quest CITRIC_ACID = CHAPTER.quest("citric_acid", q -> q
            .at(28, -4)
            .title("柠檬酸")
            .icon(GTOMaterials.CitricAcid.getBucket())
            .task(Task.item(GTOMaterials.CitricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Glycerol.getBucket(), 1)
            .dependsOn(GLYCEROL, POTASSIUM_DICHROMATE));

    public static final Quest BARIUM_SULFIDE = CHAPTER.quest("barium_sulfide", q -> q
            .at(26, -4)
            .title("硫化钡")
            .icon(TagPrefix.dust, GTMaterials.BariumSulfide)
            .task(Task.item(TagPrefix.dust, GTMaterials.BariumSulfide, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Barite, 12)
            .dependsOn(CITRIC_ACID));

    public static final Quest BARIUM_COPPER_NITRATE = CHAPTER.quest("barium_and_copper_nitrate", q -> q
            .at(24, -4)
            .title("硝酸钡与硝酸铜")
            .icon(TagPrefix.dust, GTOMaterials.BariumNitrate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.BariumNitrate, 18), Task.item(TagPrefix.dust, GTOMaterials.CopperNitrate, 27))
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 4)
            .dependsOn(BARIUM_SULFIDE));

    public static final Quest WELL_MIXED_YBC_OXIDES = CHAPTER.quest("well_mixed_ybc_oxides", q -> q
            .at(22, -2)
            .title("混匀钇钡铜氧化物")
            .icon(TagPrefix.dust, GTOMaterials.WellMixedYbcOxides)
            .task(Task.item(TagPrefix.dust, GTOMaterials.WellMixedYbcOxides, 12))
            .scaledReward(GTOMaterials.CitricAcid.getBucket(), 1)
            .dependsOn(YTTRIUM_NITRATE, BARIUM_COPPER_NITRATE));

    public static final Quest YBCO = CHAPTER.quest("yttrium_barium_cuprate", q -> q
            .at(24, -2)
            .title("钇钡铜氧")
            .icon(TagPrefix.ingot, GTMaterials.YttriumBariumCuprate)
            .task(Task.item(GTMachines.ARC_FURNACE[GTValues.EV]), Task.item(TagPrefix.ingot, GTMaterials.YttriumBariumCuprate))
            .scaledReward(TagPrefix.dust, GTOMaterials.WellMixedYbcOxides, 3)
            .dependsOn(WELL_MIXED_YBC_OXIDES));

    public static final Quest YBCO_ROD = CHAPTER.quest("yttrium_barium_cuprate_long_rod", q -> q
            .at(26, -2)
            .title("钇钡铜氧长棒")
            .icon(TagPrefix.rodLong, GTMaterials.YttriumBariumCuprate)
            .task(Task.item(TagPrefix.rodLong, GTMaterials.YttriumBariumCuprate))
            .scaledReward(TagPrefix.ingot, GTMaterials.YttriumBariumCuprate, 1)
            .dependsOn(YBCO));

    public static final Quest SUGAR_GEM = CHAPTER.quest("sugar_gem", q -> q
            .at(26, 0)
            .title("糖宝石")
            .icon(TagPrefix.gem, GTMaterials.Sugar)
            .task(Task.item(TagPrefix.gem, GTMaterials.Sugar, 12))
            .scaledReward(Items.SUGAR, 16)
            .dependsOn(YBCO_ROD));

    public static final Quest GLUCOSE = CHAPTER.quest("glucose", q -> q
            .at(24, 0)
            .milestone()
            .title("葡萄糖")
            .icon(TagPrefix.dust, GTOMaterials.Glucose)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Glucose, 72))
            .scaledReward(TagPrefix.gem, GTMaterials.Sugar, 3)
            .dependsOn(SUGAR_GEM));

    public static final Quest GLUCOSE_IRON_SOLUTION = CHAPTER.quest("glucose_iron_solution", q -> q
            .at(26, 2)
            .title("葡萄糖铁溶液")
            .icon(GTOMaterials.GlucoseIronSolution.getBucket())
            .task(Task.item(GTOMaterials.GlucoseIronSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.Glucose, 24)
            .dependsOn(GLUCOSE));

    public static final Quest GRAPHENE_IRON_PLATE = CHAPTER.quest("graphene_iron_plate", q -> q
            .at(28, 2)
            .title("石墨烯铁板")
            .icon(GTOItems.GRAPHENE_IRON_PLATE)
            .task(Task.item(GTOItems.GRAPHENE_IRON_PLATE, 3))
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 9)
            .dependsOn(GLUCOSE_IRON_SOLUTION));

    public static final Quest ELECTROMAGNETIC_SEPARATOR_LV = CHAPTER.quest("electromagnetic_separator_lv", q -> q
            .at(30, 2)
            .title("LV 电磁选矿机")
            .task(Task.item(GTMachines.ELECTROMAGNETIC_SEPARATOR[GTValues.LV]))
            .scaledReward(GTOItems.GRAPHENE_IRON_PLATE, 1)
            .dependsOn(GRAPHENE_IRON_PLATE));

    public static final Quest GRAPHENE_OXIDE = CHAPTER.quest("graphene_oxide", q -> q
            .at(30, 4)
            .title("氧化石墨烯")
            .icon(TagPrefix.dust, GTOMaterials.GrapheneOxide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.GrapheneOxide, 27))
            .scaledReward(GTOItems.GRAPHENE_IRON_PLATE, 1)
            .dependsOn(ELECTROMAGNETIC_SEPARATOR_LV));

    public static final Quest TUNGSTEN_INGOT = CHAPTER.quest("tungsten_ingot", q -> q
            .at(28, 4)
            .title("钨锭")
            .icon(TagPrefix.ingot, GTMaterials.Tungsten)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Tungsten, 24))
            .scaledReward(TagPrefix.dust, GTMaterials.Tungsten, 6)
            .dependsOn(GRAPHENE_OXIDE));

    public static final Quest TUNGSTEN_CABLE = CHAPTER.quest("tungsten_cable", q -> q
            .at(28, 6)
            .title("钨线缆")
            .icon(TagPrefix.cableGtSingle, GTMaterials.Tungsten)
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Tungsten, 40))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Tungsten, 10)
            .dependsOn(TUNGSTEN_INGOT));

    public static final Quest GRAPHENE_FOIL = CHAPTER.quest("graphene_foil", q -> q
            .at(30, 6)
            .title("石墨烯箔")
            .icon(TagPrefix.foil, GTMaterials.Graphene)
            .task(Task.item(TagPrefix.foil, GTMaterials.Graphene, 64))
            .scaledReward(TagPrefix.dust, GTOMaterials.GrapheneOxide, 6)
            .dependsOn(GRAPHENE_OXIDE, NewEvChapter.CHEMICAL_VAPOR_DEPOSITION));

    public static final Quest GRAPHENE = CHAPTER.quest("graphene", q -> q
            .at(30, 8)
            .title("石墨烯")
            .icon(TagPrefix.ingot, GTMaterials.Graphene)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Graphene, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.Graphene, 16)
            .dependsOn(GRAPHENE_FOIL));

    public static final Quest IV_MOTOR = CHAPTER.quest("iv_motor", q -> q
            .at(28, 8)
            .title("IV 马达")
            .icon(GTItems.ELECTRIC_MOTOR_IV)
            .task(Task.item(GTItems.ELECTRIC_MOTOR_IV, 4))
            .scaledReward(TagPrefix.wireGtDouble, GTMaterials.Graphene, 4)
            .dependsOn(GRAPHENE, TUNGSTEN_CABLE));

    public static final Quest IV_COMPONENTS = CHAPTER.quest("iv_components", q -> q
            .at(26, 8)
            .title("IV 泵与传送带")
            .icon(GTItems.ELECTRIC_PUMP_IV)
            .task(Task.item(GTItems.ELECTRIC_PUMP_IV, 2), Task.item(GTItems.CONVEYOR_MODULE_IV, 3))
            .scaledReward(GTItems.ELECTRIC_MOTOR_IV, 1)
            .dependsOn(IV_MOTOR));

    public static final Quest IV_ROBOT_ARM = CHAPTER.quest("iv_robot_arm", q -> q
            .at(22, 6)
            .title("IV 机械臂")
            .icon(GTItems.ROBOT_ARM_IV)
            .task(Task.item(GTItems.ROBOT_ARM_IV, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.TungstenSteel, 4)
            .dependsOn(NewEvChapter.MAINFRAME));

    public static final Quest IV_ASSEMBLER = CHAPTER.quest("iv_assembler", q -> q
            .at(24, 8)
            .milestone()
            .title("IV 组装机")
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.IV]))
            .scaledReward(TagPrefix.plate, GTMaterials.TungstenSteel, 6)
            .dependsOn(IV_ROBOT_ARM, IV_COMPONENTS));

    public static final Quest BUTYRALDEHYDE = CHAPTER.quest("butyraldehyde", q -> q
            .at(28, 12)
            .title("丁醛")
            .icon(GTMaterials.Butyraldehyde.getBucket())
            .task(Task.item(GTMaterials.Butyraldehyde.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Propene.getBucket(), 2)
            .dependsOn(NewEvChapter.PROPENE));

    public static final Quest VINYL_ACETATE = CHAPTER.quest("vinyl_acetate", q -> q
            .at(28, 18)
            .title("醋酸乙烯酯")
            .icon(GTMaterials.VinylAcetate.getBucket())
            .task(Task.item(GTMaterials.VinylAcetate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.AceticAcid.getBucket(), 2)
            .dependsOn(NewEvChapter.PROPENE));

    public static final Quest POLYVINYL_ACETATE = CHAPTER.quest("polyvinyl_acetate", q -> q
            .at(28, 16)
            .title("聚醋酸乙烯酯")
            .icon(GTMaterials.PolyvinylAcetate.getBucket())
            .task(Task.item(GTMaterials.PolyvinylAcetate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.VinylAcetate.getBucket(), 2)
            .dependsOn(VINYL_ACETATE));

    public static final Quest POLYVINYL_BUTYRAL = CHAPTER.quest("polyvinyl_butyral", q -> q
            .at(28, 14)
            .title("聚乙烯醇缩丁醛")
            .icon(GTMaterials.PolyvinylButyral.getBucket())
            .task(Task.item(GTMaterials.PolyvinylButyral.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Butyraldehyde.getBucket(), 2)
            .dependsOn(BUTYRALDEHYDE, POLYVINYL_ACETATE));

    public static final Quest POLYVINYL_BUTYRAL_PLATE = CHAPTER.quest("polyvinyl_butyral_plate", q -> q
            .at(26, 12)
            .title("聚乙烯醇缩丁醛板")
            .icon(TagPrefix.plate, GTMaterials.PolyvinylButyral)
            .task(Task.item(TagPrefix.plate, GTMaterials.PolyvinylButyral, 5))
            .scaledReward(GTMaterials.PolyvinylButyral.getBucket(), 1)
            .dependsOn(POLYVINYL_BUTYRAL));

    public static final Quest LAMINATED_GLASS = CHAPTER.quest("laminated_glass", q -> q
            .at(26, 10)
            .title("夹层玻璃")
            .task(Task.item(GTBlocks.CASING_LAMINATED_GLASS, 5))
            .scaledReward(GTBlocks.CASING_TEMPERED_GLASS, 4)
            .dependsOn(POLYVINYL_BUTYRAL_PLATE));

    public static final Quest IV_CUTTER = CHAPTER.quest("iv_cutter", q -> q
            .at(24, 10)
            .title("IV 切割机")
            .task(Task.item(GTMachines.CUTTER[GTValues.IV]))
            .scaledReward(GTBlocks.CASING_LAMINATED_GLASS, 2)
            .dependsOn(IV_ASSEMBLER, LAMINATED_GLASS));

    public static final Quest MOLYBDENUM_AND_VANADIUM = CHAPTER.quest("molybdenum_and_vanadium", q -> q
            .at(24, 12)
            .title("钼与钒")
            .icon(TagPrefix.dust, GTMaterials.Molybdenum)
            .task(Task.item(TagPrefix.dust, GTMaterials.Molybdenum, 16), Task.item(TagPrefix.dust, GTMaterials.Vanadium, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.VanadiumMagnetite, 8)
            .dependsOn(IV_CUTTER));

    public static final QuestLink MOLYBDENUM_AND_VANADIUM_COPY_1 = CHAPTER.copy(MOLYBDENUM_AND_VANADIUM, 8, 10);

    public static final Quest POTASSIUM_PYROSULFATE = CHAPTER.quest("potassium_pyrosulfate", q -> q
            .at(8, 12)
            .title("焦硫酸钾")
            .icon(TagPrefix.dust, GTOMaterials.PotassiumPyrosulfate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.PotassiumPyrosulfate, 88))
            .scaledReward(TagPrefix.dust, GTMaterials.Potassium, 8)
            .dependsOn(MOLYBDENUM_AND_VANADIUM_COPY_1));

    public static final Quest RHODIUM_SULFATE_GAS = CHAPTER.quest("rhodium_sulfate_gas", q -> q
            .at(8, 14)
            .title("硫酸铑")
            .icon(GTOMaterials.RhodiumSulfateGas.getBucket())
            .task(Task.item(GTOMaterials.RhodiumSulfateGas.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.PlatinumSlag, 12)
            .dependsOn(POTASSIUM_PYROSULFATE));

    public static final Quest RHODIUM_SULFATE = CHAPTER.quest("rhodium_sulfate", q -> q
            .at(10, 14)
            .title("硫酸铑溶液")
            .icon(GTMaterials.RhodiumSulfate.getBucket())
            .task(Task.item(GTMaterials.RhodiumSulfate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RhodiumSulfateGas.getBucket(), 1)
            .dependsOn(RHODIUM_SULFATE_GAS));

    public static final Quest ROUGHLY_RHODIUM = CHAPTER.quest("roughly_rhodium_metal", q -> q
            .at(10, 12)
            .title("粗铑金属粉")
            .icon(TagPrefix.dust, GTOMaterials.RoughlyRhodiumMetal)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RoughlyRhodiumMetal, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.Zinc, 6)
            .dependsOn(RHODIUM_SULFATE));

    public static final Quest RHODIUM_SALT = CHAPTER.quest("rhodium_salt", q -> q
            .at(12, 12)
            .title("铑盐")
            .icon(TagPrefix.dust, GTOMaterials.RhodiumSalt)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RhodiumSalt, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Salt, 12)
            .dependsOn(ROUGHLY_RHODIUM));

    public static final Quest RHODIUM_SALT_SOLUTION = CHAPTER.quest("rhodium_salt_solution", q -> q
            .at(14, 10)
            .title("铑盐溶液")
            .icon(GTOMaterials.RhodiumSaltSolution.getBucket())
            .task(Task.item(GTOMaterials.RhodiumSaltSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.RhodiumSalt, 4)
            .dependsOn(RHODIUM_SALT));

    public static final Quest SODIUM_NITRATE = CHAPTER.quest("sodium_nitrate", q -> q
            .at(12, 10)
            .title("硝酸钠")
            .icon(TagPrefix.dust, GTOMaterials.SodiumNitrate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SodiumNitrate, 20))
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 2)
            .dependsOn(RHODIUM_SALT));

    public static final Quest RHODIUM_NITRATE = CHAPTER.quest("rhodium_nitrate", q -> q
            .at(14, 8)
            .milestone()
            .title("硝酸铑")
            .icon(TagPrefix.dust, GTOMaterials.RhodiumNitrate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RhodiumNitrate, 52))
            .scaledReward(TagPrefix.dust, GTOMaterials.SodiumNitrate, 5)
            .dependsOn(RHODIUM_SALT_SOLUTION, SODIUM_NITRATE));

    public static final Quest RHODIUM_FILTER_CAKE = CHAPTER.quest("rhodium_filter_cake", q -> q
            .at(14, 4)
            .title("铑滤饼")
            .icon(TagPrefix.dust, GTOMaterials.RhodiumFilterCake)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RhodiumFilterCake, 12))
            .scaledReward(TagPrefix.dust, GTOMaterials.RhodiumNitrate, 13)
            .dependsOn(RHODIUM_NITRATE));

    public static final Quest RHODIUM_FILTER_CAKE_SOLUTION = CHAPTER.quest("rhodium_filter_cake_solution", q -> q
            .at(12, 2)
            .title("铑滤饼溶液")
            .icon(GTOMaterials.RhodiumFilterCakeSolution.getBucket())
            .task(Task.item(GTOMaterials.RhodiumFilterCakeSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.RhodiumFilterCake, 6)
            .dependsOn(RHODIUM_FILTER_CAKE));

    public static final Quest REPRECIPITATED_RHODIUM = CHAPTER.quest("reprecipitated_rhodium", q -> q
            .at(10, 4)
            .title("再沉淀铑")
            .icon(TagPrefix.dust, GTOMaterials.ReprecipitatedRhodium)
            .task(Task.item(TagPrefix.dust, GTOMaterials.ReprecipitatedRhodium, 14))
            .scaledReward(GTOMaterials.RhodiumFilterCakeSolution.getBucket(), 1)
            .dependsOn(RHODIUM_FILTER_CAKE_SOLUTION));

    public static final Quest RHODIUM = CHAPTER.quest("rhodium", q -> q
            .at(8, 6)
            .title("铑")
            .icon(TagPrefix.plateDouble, GTMaterials.Rhodium)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.Rhodium))
            .scaledReward(TagPrefix.dust, GTOMaterials.ReprecipitatedRhodium, 7)
            .dependsOn(REPRECIPITATED_RHODIUM));

    public static final Quest HSLA_STEEL = CHAPTER.quest("hsla_steel", q -> q
            .at(22, 14)
            .title("HSLA 钢")
            .icon(TagPrefix.ingot, GTMaterials.HSLASteel)
            .task(Task.item(TagPrefix.ingot, GTMaterials.HSLASteel, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Invar, 16)
            .dependsOn(MOLYBDENUM_AND_VANADIUM));

    public static final Quest NONCONDUCTING_CASING = CHAPTER.quest("nonconducting_casing", q -> q
            .at(20, 12)
            .title("不导电外壳")
            .task(Task.item(GCYMBlocks.CASING_NONCONDUCTING, 62))
            .scaledReward(TagPrefix.plate, GTMaterials.HSLASteel, 24)
            .dependsOn(HSLA_STEEL));

    public static final Quest ELECTROLYTIC_CELL = CHAPTER.quest("electrolytic_cell", q -> q
            .at(20, 10)
            .title("电解池")
            .task(Task.item(GCYMBlocks.ELECTROLYTIC_CELL, 10))
            .scaledReward(TagPrefix.wireGtDouble, GTMaterials.Platinum, 8)
            .dependsOn(NONCONDUCTING_CASING));

    public static final Quest WHITE_DYE = CHAPTER.quest("white_dye", q -> q
            .at(22, 12)
            .title("白色染料")
            .icon(GTMaterials.DyeWhite.getBucket())
            .task(Task.item(GTMaterials.DyeWhite.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.WHITE_DYE, 8)
            .dependsOn(NONCONDUCTING_CASING));

    public static final Quest CHEMICAL_GRADE_GLASS = CHAPTER.quest("chemical_grade_glass", q -> q
            .at(22, 10)
            .title("化学级玻璃")
            .task(Task.item(GTOBlocks.CHEMICAL_GRADE_GLASS, 10))
            .scaledReward(GTBlocks.CASING_TEMPERED_GLASS, 6)
            .dependsOn(WHITE_DYE));

    public static final Quest ELECTROPLATING_BATH = CHAPTER.quest("electroplating_bath", q -> q
            .at(20, 8)
            .milestone()
            .title("电镀池")
            .task(Task.item(MultiBlockH.ELECTROPLATING_BATH), Task.item(TagPrefix.frameGt, GTMaterials.Polytetrafluoroethylene, 4))
            .scaledReward(GTBlocks.CASING_PTFE_INERT, 8)
            .dependsOn(ELECTROLYTIC_CELL, CHEMICAL_GRADE_GLASS));

    public static final Quest SODIUM_FORMATE = CHAPTER.quest("sodium_formate", q -> q
            .at(12, 4)
            .title("甲酸钠")
            .icon(GTOMaterials.SodiumFormate.getBucket())
            .task(Task.item(GTOMaterials.SodiumFormate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.CarbonMonoxide.getBucket(), 4)
            .dependsOn(ELECTROPLATING_BATH));

    public static final Quest FORMIC_ACID = CHAPTER.quest("formic_acid", q -> q
            .at(12, 6)
            .title("甲酸")
            .icon(GTMaterials.FormicAcid.getBucket())
            .task(Task.item(GTMaterials.FormicAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.SodiumFormate.getBucket(), 2)
            .dependsOn(SODIUM_FORMATE));

    public static final Quest PALLADIUM = CHAPTER.quest("palladium", q -> q
            .at(10, 6)
            .title("钯")
            .icon(TagPrefix.plate, GTMaterials.Palladium)
            .task(Task.item(TagPrefix.plate, GTMaterials.Palladium, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.PalladiumRaw, 10)
            .dependsOn(FORMIC_ACID));

    public static final Quest RHODIUM_PLATED_PALLADIUM = CHAPTER.quest("rhodium_plated_palladium", q -> q
            .at(8, 8)
            .milestone()
            .title("镀铑钯")
            .icon(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium, 2)
            .dependsOn(RHODIUM, PALLADIUM));

    public static final Quest SODA_ASH = CHAPTER.quest("soda_ash", q -> q
            .at(6, 6)
            .title("苏打灰")
            .icon(TagPrefix.dust, GTMaterials.SodaAsh)
            .task(Task.item(TagPrefix.dust, GTMaterials.SodaAsh, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 12)
            .dependsOn(RHODIUM_PLATED_PALLADIUM));

    public static final QuestLink SODA_ASH_COPY_1 = CHAPTER.copy(SODA_ASH, -6, 10);

    public static final Quest RAREST_METAL_MIXTURE = CHAPTER.quest("rarest_metal_mixture", q -> q
            .at(6, 4)
            .title("稀有金属混合物")
            .icon(TagPrefix.dust, GTMaterials.RarestMetalMixture)
            .task(Task.item(TagPrefix.dust, GTMaterials.RarestMetalMixture, 20))
            .scaledReward(TagPrefix.dust, GTOMaterials.LeachResidue, 10)
            .dependsOn(SODA_ASH));

    public static final QuestLink RAREST_METAL_MIXTURE_COPY_1 = CHAPTER.copy(RAREST_METAL_MIXTURE, -6, 20);

    public static final Quest SODIUM_HYPOCHLORITE = CHAPTER.quest("sodium_hypochlorite", q -> q
            .at(-6, 12)
            .title("次氯酸钠")
            .icon(TagPrefix.dust, GTOMaterials.SodiumHypochlorite)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SodiumHypochlorite, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 12)
            .dependsOn(SODA_ASH_COPY_1));

    public static final Quest SODIUM_CHLORATE = CHAPTER.quest("sodium_chlorate", q -> q
            .at(-6, 14)
            .title("氯酸钠")
            .icon(TagPrefix.dust, GTOMaterials.SodiumChlorate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SodiumChlorate, 10))
            .scaledReward(TagPrefix.dust, GTOMaterials.SodiumHypochlorite, 9)
            .dependsOn(SODIUM_HYPOCHLORITE));

    public static final Quest IRIDIUM_METAL_RESIDUE = CHAPTER.quest("iridium_metal_residue", q -> q
            .at(-6, 18)
            .title("铱金属残渣")
            .icon(TagPrefix.dust, GTMaterials.IridiumMetalResidue)
            .task(Task.item(TagPrefix.dust, GTMaterials.IridiumMetalResidue, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.RarestMetalMixture, 7)
            .dependsOn(RAREST_METAL_MIXTURE_COPY_1));

    public static final Quest IRIDIUM_DIOXIDE = CHAPTER.quest("iridium_dioxide", q -> q
            .at(-6, 16)
            .title("二氧化铱")
            .icon(TagPrefix.dust, GTOMaterials.IridiumDioxide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.IridiumDioxide, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.IridiumMetalResidue, 6)
            .dependsOn(IRIDIUM_METAL_RESIDUE, SODIUM_CHLORATE));

    public static final Quest ACIDIC_IRIDIUM = CHAPTER.quest("acidic_iridium", q -> q
            .at(-4, 16)
            .title("酸性铱溶液")
            .icon(GTOMaterials.AcidicIridium.getBucket())
            .task(Task.item(GTOMaterials.AcidicIridium.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.IridiumDioxide, 3)
            .dependsOn(IRIDIUM_DIOXIDE));

    public static final Quest IRIDIUM_CHLORIDE = CHAPTER.quest("iridium_chloride", q -> q
            .at(-4, 18)
            .title("氯化铱")
            .icon(TagPrefix.dust, GTMaterials.IridiumChloride)
            .task(Task.item(TagPrefix.dust, GTMaterials.IridiumChloride, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.AmmoniumChloride, 18)
            .dependsOn(ACIDIC_IRIDIUM));

    public static final Quest RUTHENIUM_TETROXIDE = CHAPTER.quest("ruthenium_tetroxide", q -> q
            .at(6, 2)
            .title("四氧化钌")
            .icon(GTMaterials.RutheniumTetroxide.getBucket())
            .task(Task.item(GTMaterials.RutheniumTetroxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.SodiumRutheniate, 7)
            .dependsOn(RAREST_METAL_MIXTURE));

    public static final Quest HOT_RUTHENIUM_TETROXIDE = CHAPTER.quest("hot_ruthenium_tetroxide", q -> q
            .at(4, 2)
            .title("热四氧化钌")
            .icon(GTOMaterials.RutheniumTetroxideHot.getBucket())
            .task(Task.item(GTOMaterials.RutheniumTetroxideHot.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.RutheniumTetroxide.getBucket(), 2)
            .dependsOn(RUTHENIUM_TETROXIDE));

    public static final Quest LIQUID_RUTHENIUM_TETROXIDE = CHAPTER.quest("liquid_ruthenium_tetroxide", q -> q
            .at(4, 4)
            .title("液态四氧化钌")
            .icon(GTOMaterials.RutheniumTetroxideLQ.getBucket())
            .task(Task.item(GTOMaterials.RutheniumTetroxideLQ.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RutheniumTetroxideHot.getBucket(), 2)
            .dependsOn(HOT_RUTHENIUM_TETROXIDE));

    public static final Quest RUTHENIUM_TETROXIDE_DUST = CHAPTER.quest("ruthenium_tetroxide_dust", q -> q
            .at(4, 6)
            .title("四氧化钌粉")
            .icon(TagPrefix.dust, GTMaterials.RutheniumTetroxide)
            .task(Task.item(TagPrefix.dust, GTMaterials.RutheniumTetroxide, 20))
            .scaledReward(GTOMaterials.RutheniumTetroxideLQ.getBucket(), 1)
            .dependsOn(LIQUID_RUTHENIUM_TETROXIDE));

    public static final Quest RUTHENIUM = CHAPTER.quest("ruthenium", q -> q
            .at(4, 8)
            .milestone()
            .title("钌")
            .icon(TagPrefix.dust, GTMaterials.Ruthenium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Ruthenium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.RutheniumTetroxide, 5)
            .dependsOn(RUTHENIUM_TETROXIDE_DUST));

    public static final Quest VANADIUM_GALLIUM_DUST = CHAPTER.quest("vanadium_gallium_dust", q -> q
            .at(4, 10)
            .title("钒镓粉")
            .icon(TagPrefix.dust, GTMaterials.VanadiumGallium)
            .task(Task.item(TagPrefix.dust, GTMaterials.VanadiumGallium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Vanadium, 3)
            .dependsOn(RUTHENIUM));

    public static final QuestLink VANADIUM_GALLIUM_DUST_COPY_1 = CHAPTER.copy(VANADIUM_GALLIUM_DUST, -4, 2);

    public static final Quest RTM_ALLOY_DUST = CHAPTER.quest("rtm_alloy_dust", q -> q
            .at(2, 6)
            .title("RTM 合金粉")
            .icon(TagPrefix.dust, GTMaterials.RTMAlloy)
            .task(Task.item(TagPrefix.dust, GTMaterials.RTMAlloy, 7))
            .scaledReward(TagPrefix.dust, GTMaterials.Molybdenum, 2)
            .dependsOn(RUTHENIUM));

    public static final Quest RTM_ALLOY = CHAPTER.quest("rtm_alloy", q -> q
            .at(2, 4)
            .title("RTM 合金锭")
            .icon(TagPrefix.ingot, GTMaterials.RTMAlloy)
            .task(Task.item(TagPrefix.ingot, GTMaterials.RTMAlloy, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.RTMAlloy, 7)
            .dependsOn(RTM_ALLOY_DUST));

    public static final Quest RTM_COIL = CHAPTER.quest("rtm_alloy_coil", q -> q
            .at(0, 2)
            .title("RTM 合金线圈")
            .icon(GTBlocks.COIL_RTMALLOY)
            .task(Task.item(GTBlocks.COIL_RTMALLOY, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.VanadiumSteel, 32)
            .dependsOn(RTM_ALLOY));

    public static final QuestLink RTM_COIL_COPY_1 = CHAPTER.copy(RTM_COIL, -2, 20);

    public static final Quest VANADIUM_GALLIUM = CHAPTER.quest("vanadium_gallium", q -> q
            .at(-2, 2)
            .title("钒镓")
            .icon(TagPrefix.ingot, GTMaterials.VanadiumGallium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.VanadiumGallium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.VanadiumGallium, 4)
            .dependsOn(RTM_COIL, VANADIUM_GALLIUM_DUST_COPY_1));

    public static final QuestLink VANADIUM_GALLIUM_COPY_1 = CHAPTER.copy(VANADIUM_GALLIUM, 2, 10);

    public static final Quest IRIDIUM = CHAPTER.quest("iridium", q -> q
            .at(-2, 18)
            .title("铱")
            .icon(TagPrefix.ingot, GTMaterials.Iridium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Iridium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.IridiumChloride, 4)
            .dependsOn(RTM_COIL_COPY_1, IRIDIUM_CHLORIDE));

    public static final Quest IV_VOLTAGE_COIL = CHAPTER.quest("iv_voltage_coil", q -> q
            .at(0, 18)
            .title("IV 电压线圈")
            .icon(GTItems.VOLTAGE_COIL_IV)
            .task(Task.item(GTItems.VOLTAGE_COIL_IV, 2))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Iridium, 8)
            .dependsOn(IRIDIUM));

    public static final Quest LIQUID_VANADIUM_GALLIUM = CHAPTER.quest("liquid_vanadium_gallium", q -> q
            .at(2, 12)
            .title("液态钒镓")
            .icon(GTMaterials.VanadiumGallium.getBucket())
            .task(Task.item(GTMaterials.VanadiumGallium.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.ingot, GTMaterials.VanadiumGallium, 1)
            .dependsOn(VANADIUM_GALLIUM_COPY_1));

    public static final Quest INDIUM_CONCENTRATE = CHAPTER.quest("indium_concentrate", q -> q
            .at(-2, 4)
            .title("铟浓缩液")
            .icon(GTMaterials.IndiumConcentrate.getBucket())
            .task(Task.item(GTMaterials.IndiumConcentrate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 4)
            .dependsOn(VANADIUM_GALLIUM));

    public static final Quest INDIUM = CHAPTER.quest("indium", q -> q
            .at(0, 4)
            .title("铟")
            .icon(TagPrefix.dust, GTMaterials.Indium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Indium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Aluminium, 16)
            .dependsOn(INDIUM_CONCENTRATE));

    public static final Quest INDIUM_GALLIUM_PHOSPHIDE = CHAPTER.quest("indium_gallium_phosphide", q -> q
            .at(0, 6)
            .title("铟镓磷")
            .icon(TagPrefix.dust, GTMaterials.IndiumGalliumPhosphide)
            .task(Task.item(TagPrefix.dust, GTMaterials.IndiumGalliumPhosphide, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Gallium, 2)
            .dependsOn(INDIUM));

    public static final QuestLink INDIUM_GALLIUM_PHOSPHIDE_COPY_1 = CHAPTER.copy(INDIUM_GALLIUM_PHOSPHIDE, 4, 14);

    public static final Quest HPIC_WAFER = CHAPTER.quest("hpic_wafer", q -> q
            .at(4, 12)
            .title("高功率集成电路晶圆")
            .icon(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
            .task(Task.item(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 2))
            .scaledReward(GTItems.POWER_INTEGRATED_CIRCUIT_WAFER, 1)
            .dependsOn(LIQUID_VANADIUM_GALLIUM, INDIUM_GALLIUM_PHOSPHIDE_COPY_1));

    public static final Quest HPIC = CHAPTER.quest("hpic", q -> q
            .at(2, 14)
            .title("高功率集成电路")
            .icon(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT)
            .task(Task.item(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT, 4))
            .scaledReward(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT, 1)
            .dependsOn(HPIC_WAFER));

    public static final Quest IV_ENERGY_HATCH = CHAPTER.quest("iv_energy_hatch", q -> q
            .at(0, 16)
            .milestone()
            .title("IV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.IV], 2))
            .scaledReward(TagPrefix.cableGtDouble, GTMaterials.Tungsten, 4)
            .dependsOn(IV_VOLTAGE_COIL, HPIC));

    public static final Quest THERMAL_CENTRIFUGE_LV = CHAPTER.quest("thermal_centrifuge_lv", q -> q
            .at(16, 20)
            .title("LV 热力离心机")
            .task(Task.item(GTMachines.THERMAL_CENTRIFUGE[GTValues.LV]))
            .scaledReward(TagPrefix.crushed, GTMaterials.Tantalite, 8)
            .dependsOn(IV_ENERGY_HATCH));

    public static final Quest NIOBIUM = CHAPTER.quest("niobium", q -> q
            .at(18, 20)
            .title("铌")
            .icon(TagPrefix.dust, GTMaterials.Niobium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Niobium, 2))
            .scaledReward(TagPrefix.crushed, GTMaterials.Tantalite, 8)
            .dependsOn(THERMAL_CENTRIFUGE_LV));

    public static final Quest NIOBIUM_TITANIUM_DUST = CHAPTER.quest("niobium_titanium_dust", q -> q
            .at(18, 18)
            .title("铌钛粉")
            .icon(TagPrefix.dust, GTMaterials.NiobiumTitanium)
            .task(Task.item(GTMachines.MIXER[GTValues.IV]), Task.item(TagPrefix.dust, GTMaterials.NiobiumTitanium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Titanium, 2)
            .dependsOn(NIOBIUM));

    public static final Quest NIOBIUM_TITANIUM = CHAPTER.quest("niobium_titanium", q -> q
            .at(20, 18)
            .title("铌钛锭")
            .icon(TagPrefix.ingot, GTMaterials.NiobiumTitanium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.NiobiumTitanium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.NiobiumTitanium, 2)
            .dependsOn(NIOBIUM_TITANIUM_DUST));

    public static final Quest SODIUM_SULFIDE = CHAPTER.quest("sodium_sulfide", q -> q
            .at(20, 22)
            .title("硫化钠")
            .icon(TagPrefix.dust, GTMaterials.SodiumSulfide)
            .task(Task.item(TagPrefix.dust, GTMaterials.SodiumSulfide, 3))
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 2)
            .dependsOn(NIOBIUM));

    public static final Quest POLYPHENYLENE_SULFIDE = CHAPTER.quest("polyphenylene_sulfide", q -> q
            .at(22, 22)
            .title("聚苯硫醚")
            .icon(GTMaterials.PolyphenyleneSulfide.getBucket())
            .task(Task.item(GTMaterials.PolyphenyleneSulfide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Dichlorobenzene.getBucket(), 1)
            .dependsOn(SODIUM_SULFIDE));

    public static final Quest PPS_FOIL = CHAPTER.quest("polyphenylene_sulfide_foil", q -> q
            .at(22, 20)
            .title("聚苯硫醚箔")
            .icon(TagPrefix.foil, GTMaterials.PolyphenyleneSulfide)
            .task(Task.item(TagPrefix.foil, GTMaterials.PolyphenyleneSulfide, 2))
            .scaledReward(TagPrefix.foil, GTMaterials.PolyvinylChloride, 4)
            .dependsOn(POLYPHENYLENE_SULFIDE));

    public static final Quest NIOBIUM_TITANIUM_CABLE = CHAPTER.quest("niobium_titanium_cable", q -> q
            .at(22, 18)
            .title("铌钛线缆")
            .icon(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium)
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.NiobiumTitanium, 2)
            .dependsOn(NIOBIUM_TITANIUM, PPS_FOIL));

    public static final Quest LUV_CASING = CHAPTER.quest("luv_casing", q -> q
            .at(24, 18)
            .title("LuV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_LuV))
            .scaledReward(TagPrefix.plate, GTMaterials.RhodiumPlatedPalladium, 1)
            .dependsOn(NIOBIUM_TITANIUM_CABLE));

    public static final Quest LUV_HULL = CHAPTER.quest("luv_hull", q -> q
            .at(24, 16)
            .milestone()
            .title("LuV 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.LuV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.LuV], 1))
            .dependsOn(NIOBIUM_TITANIUM_CABLE, LUV_CASING));

    public static final Quest MOLECULAR_TRANSFORMER = CHAPTER.quest("molecular_transformer", q -> q
            .at(4, 22)
            .title("分子重组仪")
            .task(Task.item(MultiBlockC.MOLECULAR_TRANSFORMER))
            .scaledReward(Items.WITHER_SKELETON_SKULL, 2)
            .dependsOn(NewEvChapter.IV_HULL));

    public static final Quest NETHER_STAR = CHAPTER.quest("nether_star", q -> q
            .at(4, 20)
            .title("下界之星")
            .icon(TagPrefix.gem, GTMaterials.NetherStar)
            .task(Task.item(TagPrefix.gem, GTMaterials.NetherStar, 4))
            .scaledReward(Items.WITHER_SKELETON_SKULL, 2)
            .dependsOn(MOLECULAR_TRANSFORMER));

    public static final Quest NANO_MAINFRAME = CHAPTER.quest("nano_processor_mainframe", q -> q
            .at(6, 14)
            .title("纳米处理器主机")
            .icon(GTItems.NANO_MAINFRAME_LuV)
            .task(Task.item(GTItems.NANO_MAINFRAME_LuV, 4))
            .scaledReward(GTItems.RANDOM_ACCESS_MEMORY, 16)
            .dependsOn(NewEvChapter.NANO_COMPUTER));

    public static final Quest REINFORCED_EPOXY_RESIN = CHAPTER.quest("reinforced_epoxy_resin_plate", q -> q
            .at(10, -4)
            .title("增强环氧树脂板")
            .task(Task.item(TagPrefix.plate, GTMaterials.ReinforcedEpoxyResin, 8))
            .scaledReward(GTItems.CARBON_FIBERS, 8)
            .dependsOn(NewEvChapter.EPOXY));

    public static final Quest FIBER_BOARD = CHAPTER.quest("fiber_reinforced_circuit_board", q -> q
            .at(10, -2)
            .title("纤维强化电路基板")
            .task(Task.item(GTItems.FIBER_BOARD, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.AnnealedCopper, 16)
            .dependsOn(REINFORCED_EPOXY_RESIN));

    public static final Quest BORON = CHAPTER.quest("boron", q -> q
            .at(-4, -4)
            .title("硼")
            .task(Task.item(TagPrefix.dust, GTMaterials.Boron, 36))
            .scaledReward(TagPrefix.dust, GTMaterials.Borax, 16)
            .dependsOn(NewEvChapter.IV_HULL));

    public static final Quest TUNGSTEN_BORON_MIXTURE = CHAPTER.quest("tungsten_boron_mixture", q -> q
            .at(-2, -4)
            .title("钨硼混合物")
            .task(Task.item(TagPrefix.dust, GTOMaterials.TungstenBoronMixture, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Tungsten, 4)
            .dependsOn(BORON));

    public static final Quest TUNGSTEN_TETRABORIDE_DUST = CHAPTER.quest("tungsten_tetraboride_ceramics_dust", q -> q
            .at(2, -4)
            .title("四硼化钨陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.TungstenTetraborideCeramics, 9))
            .scaledReward(TagPrefix.dust, GTOMaterials.TungstenBoronMixture, 3)
            .dependsOn(TUNGSTEN_BORON_MIXTURE));

    public static final Quest TUNGSTEN_TETRABORIDE_ROUGH_BLANK = CHAPTER.quest("tungsten_tetraboride_ceramics_rough_blank", q -> q
            .at(6, -4)
            .title("四硼化钨陶瓷粗胚")
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.TungstenTetraborideCeramics, 1))
            .scaledReward(GTMaterials.Nickel.getBucket(), 1)
            .dependsOn(TUNGSTEN_TETRABORIDE_DUST));

    public static final Quest TUNGSTEN_TETRABORIDE_FLAKE = CHAPTER.quest("tungsten_tetraboride_ceramics_flake", q -> q
            .at(8, -2)
            .title("四硼化钨陶瓷薄片")
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.TungstenTetraborideCeramics, 16))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.TungstenTetraborideCeramics, 4)
            .dependsOn(TUNGSTEN_TETRABORIDE_ROUGH_BLANK));

    public static final Quest EXTREME_CIRCUIT_BOARD = CHAPTER.quest("extreme_circuit_board", q -> q
            .at(10, 0)
            .milestone()
            .title("极致电路板")
            .icon(GTItems.EXTREME_CIRCUIT_BOARD)
            .task(Task.item(GTItems.EXTREME_CIRCUIT_BOARD, 4))
            .scaledReward(TagPrefix.foil, GTMaterials.AnnealedCopper, 40)
            .dependsOn(FIBER_BOARD, TUNGSTEN_TETRABORIDE_FLAKE));

    public static final Quest QUANTUM_STAR = CHAPTER.quest("quantum_star", q -> q
            .at(4, 18)
            .title("量子之星")
            .task(Task.item(GTItems.QUANTUM_STAR, 4))
            .scaledReward(GTMaterials.Radon.getBucket(), 2)
            .dependsOn(NETHER_STAR, NewEvChapter.EARTH_ORBIT, NewEvChapter.MARS_RADON));

    public static final Quest IV_EMITTER_SENSOR = CHAPTER.quest("iv_emitter_and_sensor", q -> q
            .at(6, 18)
            .title("IV 发射器与传感器")
            .icon(GTItems.EMITTER_IV)
            .task(Task.item(GTItems.EMITTER_IV, 2), Task.item(GTItems.SENSOR_IV, 2))
            .scaledReward(GTItems.QUANTUM_STAR, 1)
            .dependsOn(QUANTUM_STAR));

    public static final Quest QBIT_WAFER = CHAPTER.quest("qbit_cpu_wafer", q -> q
            .at(2, 8)
            .milestone()
            .title("量子位 CPU 晶圆")
            .task(Task.item(GTItems.QUBIT_CENTRAL_PROCESSING_UNIT_WAFER, 2))
            .scaledReward(GTItems.NANO_CENTRAL_PROCESSING_UNIT_WAFER, 1)
            .dependsOn(NewEvChapter.NANO_CPU_WAFER, INDIUM_GALLIUM_PHOSPHIDE, NewEvChapter.MARS_RADON));

    public static final Quest QBIT_CPU = CHAPTER.quest("qbit_cpu_chip", q -> q
            .at(0, 8)
            .milestone()
            .title("量子位 CPU 芯片")
            .task(Task.item(GTItems.QUBIT_CENTRAL_PROCESSING_UNIT, 6))
            .scaledReward(GTItems.QUBIT_CENTRAL_PROCESSING_UNIT, 2)
            .dependsOn(QBIT_WAFER));

    public static final Quest QUANTUM_PROCESSOR = CHAPTER.quest("quantum_processor", q -> q
            .at(6, 16)
            .milestone()
            .title("量子处理器")
            .icon(GTItems.QUANTUM_PROCESSOR_EV)
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.IV]),
                    Task.item(GTItems.QUANTUM_PROCESSOR_EV, 6))
            .scaledReward(GTItems.SMD_CAPACITOR, 16)
            .dependsOn(QBIT_CPU, EXTREME_CIRCUIT_BOARD, NANO_MAINFRAME, IV_EMITTER_SENSOR));

    public static final Quest QUANTUM_ASSEMBLY = CHAPTER.quest("quantum_processor_assembly", q -> q
            .at(12, 16)
            .milestone()
            .title("量子处理器集群")
            .task(Task.item(GTItems.QUANTUM_ASSEMBLY_IV, 6))
            .scaledReward(GTItems.SMD_INDUCTOR, 8)
            .dependsOn(QUANTUM_PROCESSOR));

    public static final Quest QUANTUM_COMPUTER = CHAPTER.quest("quantum_processor_supercomputer", q -> q
            .at(18, 16)
            .milestone()
            .title("量子处理器超级计算机")
            .task(Task.item(GTItems.QUANTUM_COMPUTER_LuV, 2))
            .scaledReward(GTItems.RANDOM_ACCESS_MEMORY, 16)
            .dependsOn(QUANTUM_ASSEMBLY));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewEvChapter.IV_HULL, 0, 0))
                .mainLine(NewEvChapter.IV_HULL, DIGESTION_TANK, CRACKER, EXTREME_CIRCUIT_BOARD, DISSOLVING_TANK, YTTRIUM_NITRATE, GLUCOSE, IV_ASSEMBLER, ELECTROPLATING_BATH, RHODIUM_NITRATE, RHODIUM_PLATED_PALLADIUM, RUTHENIUM, QBIT_WAFER, QBIT_CPU, IV_ENERGY_HATCH, QUANTUM_PROCESSOR, QUANTUM_ASSEMBLY, QUANTUM_COMPUTER, LUV_HULL));
    }
}
