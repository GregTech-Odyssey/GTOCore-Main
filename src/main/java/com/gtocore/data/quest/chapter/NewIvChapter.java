package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GCYMMachines;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockB;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.MultiBlockH;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gtolib.api.item.tool.GTOToolType;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
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
            .at(2, 2)
            .title("氟碳铈矿")
            .desc(d -> d.line("一路做到钐稀土精粉即可产出稀土"))
            .icon(TagPrefix.rawOre, GTMaterials.Bastnasite)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Bastnasite, 64))
            .scaledReward(TagPrefix.rawOre, GTMaterials.Bastnasite, 16)
            .dependsOn(NewEvChapter.IV_HULL));

    public static final Quest DIGESTION_TANK = CHAPTER.quest("digestion_tank", q -> q
            .at(2, 0)
            .milestone()
            .title("煮解池")
            .task(Task.item(MultiBlockB.DIGESTION_TANK))
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 2)
            .dependsOn(BASTNASITE));

    public static final Quest FCLC_SOLUTION = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_solution", q -> q
            .at(4, 2)
            .title("氟碳镧铈溶液")
            .icon(GTOMaterials.FluoroCarbonLanthanideCeriumSolution.getBucket())
            .task(Task.item(GTOMaterials.FluoroCarbonLanthanideCeriumSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 2)
            .dependsOn(DIGESTION_TANK));

    public static final Quest CRACKER = CHAPTER.quest("cracker", q -> q
            .at(6, 0)
            .milestone()
            .title("裂化机")
            .desc(d -> d
                    .line("不同于蒸汽裂化，你现在可以将任意的初级油品{加氢/蒸汽裂化}为任何你想要的裂化油，以获得你需要的石油化工材料", YELLOW)
                    .line()
                    .line("{相比在化学反应釜执行裂化，它能获得双倍的产出}", YELLOW))
            .task(Task.item(GTMultiMachines.CRACKER))
            .scaledReward(GTBlocks.COIL_CUPRONICKEL, 4)
            .dependsOn(FCLC_SOLUTION));

    public static final Quest STEAM_CRACKED_SLURRY = CHAPTER.quest("steam_cracked_fluoro_carbon_lanthanide_slurry", q -> q
            .at(26, -2)
            .title("蒸汽裂化氟碳镧铈浆")
            .icon(GTOMaterials.SteamCrackedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.SteamCrackedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTBlocks.COIL_CUPRONICKEL, 4)
            .dependsOn(CRACKER));

    public static final Quest MODULATED_SLURRY = CHAPTER.quest("modulated_fluoro_carbon_lanthanide_slurry", q -> q
            .at(24, -2)
            .title("改性氟碳镧铈浆")
            .icon(GTOMaterials.ModulatedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.ModulatedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 2)
            .dependsOn(STEAM_CRACKED_SLURRY));

    public static final Quest DISSOLVING_TANK = CHAPTER.quest("dissolving_tank", q -> q
            .at(22, 0)
            .milestone()
            .title("溶解罐")
            .desc(d -> d.line("稀土两件套"))
            .task(Task.item(MultiBlockD.DISSOLVING_TANK))
            .scaledReward(TagPrefix.dust, GTMaterials.Saltpeter, 8)
            .dependsOn(MODULATED_SLURRY));

    public static final Quest DILUTED_SLURRY = CHAPTER.quest("diluted_fluoro_carbon_lanthanide_slurry", q -> q
            .at(24, 0)
            .title("稀释氟碳镧铈浆")
            .icon(GTOMaterials.DilutedFluoroCarbonLanthanideSlurry.getBucket())
            .task(Task.item(GTOMaterials.DilutedFluoroCarbonLanthanideSlurry.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Saltpeter, 8)
            .dependsOn(DISSOLVING_TANK));

    public static final Quest FCLC_OXIDE = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_oxide", q -> q
            .at(24, 2)
            .title("氟碳镧铈氧化物粉")
            .icon(TagPrefix.dust, GTOMaterials.FluoroCarbonLanthanideCeriumOxidePowder)
            .task(Task.item(GTMachines.SIFTER[GTValues.HV]), Task.item(TagPrefix.dust, GTOMaterials.FluoroCarbonLanthanideCeriumOxidePowder, 18))
            .scaledReward(TagPrefix.dust, GTOMaterials.FluoroCarbonLanthanideCeriumOxidePowder, 4)
            .dependsOn(DILUTED_SLURRY));

    public static final Quest SUSPENSION = CHAPTER.quest("fluoro_carbon_lanthanide_cerium_rare_earth_suspension", q -> q
            .at(26, 2)
            .title("氟碳镧铈稀土悬浊液")
            .icon(GTOMaterials.FluoroCarbonLanthanideCeriumRareEarthSuspension.getBucket())
            .task(Task.item(GTOMaterials.FluoroCarbonLanthanideCeriumRareEarthSuspension.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Acetone.getBucket(), 4)
            .dependsOn(FCLC_OXIDE, NewEvChapter.ACETONE));

    public static final Quest SAMARIUM_CONCENTRATE = CHAPTER.quest("samarium_rare_earth_concentrate", q -> q
            .at(24, 4)
            .title("钐稀土精粉")
            .icon(TagPrefix.dust, GTOMaterials.SamariumRareEarthConcentratePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumRareEarthConcentratePowder, 9))
            .scaledReward(TagPrefix.dust, GTOMaterials.SamariumRareEarthConcentratePowder, 2)
            .dependsOn(SUSPENSION));

    public static final QuestLink SAMARIUM_CONCENTRATE_COPY_1 = CHAPTER.copy(SAMARIUM_CONCENTRATE, 26, 4);

    public static final Quest PHOSPHORIC_ACID = CHAPTER.quest("phosphoric_acid", q -> q
            .at(22, 6)
            .milestone()
            .title("磷酸")
            .icon(GTMaterials.PhosphoricAcid.getBucket())
            .task(Task.item(GTMaterials.PhosphoricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Apatite, 9)
            .dependsOn(SAMARIUM_CONCENTRATE));

    public static final Quest RARE_EARTH = CHAPTER.quest("rare_earth", q -> q
            .at(26, 6)
            .title("稀土")
            .desc(d -> d
                    .line("和之前所有阶段不太一样，制造出IV马达对目前的你来说是一个巨大的挑战")
                    .line()
                    .line("你需要从{稀土}中获取生产{石墨烯}需要的{钇}元素", GOLD, GOLD, GOLD))
            .icon(TagPrefix.dust, GTMaterials.RareEarth)
            .task(Task.item(TagPrefix.dust, GTMaterials.RareEarth, 32))
            .scaledReward(GTMaterials.HydrofluoricAcid.getBucket(), 4)
            .dependsOn(SAMARIUM_CONCENTRATE_COPY_1));

    public static final Quest RARE_EARTH_HYDROXIDES = CHAPTER.quest("rare_earth_hydroxides", q -> q
            .at(24, 6)
            .title("氢氧化稀土溶液")
            .icon(GTOMaterials.RareEarthHydroxides.getBucket())
            .task(Task.item(GTOMaterials.RareEarthHydroxides.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 15)
            .dependsOn(RARE_EARTH, PHOSPHORIC_ACID));

    public static final Quest RARE_EARTH_CHLORIDES = CHAPTER.quest("rare_earth_chlorides", q -> q
            .at(24, 8)
            .title("稀土氯化物")
            .icon(GTOMaterials.RareEarthChlorides.getBucket())
            .task(Task.item(GTOMaterials.RareEarthChlorides.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 4)
            .dependsOn(RARE_EARTH_HYDROXIDES));

    public static final Quest TBHO_SOLUTION = CHAPTER.quest("tb_ho_oxides_solution", q -> q
            .at(22, 10)
            .title("铽钬氧化物溶液")
            .icon(GTOMaterials.TbHoOxidesSolution.getBucket())
            .task(Task.item(GTItems.ITEM_MAGNET_HV), Task.item(GTOMaterials.TbHoOxidesSolution.getBucket()))
            .scaledReward(GTOMaterials.RareEarthChlorides.getBucket(), 4)
            .dependsOn(RARE_EARTH_CHLORIDES));

    public static final Quest DEHYDRATOR_HV = CHAPTER.quest("dehydrator_hv", q -> q
            .at(20, 10)
            .title("HV 脱水机")
            .task(Task.item(GTOMachines.DEHYDRATOR[GTValues.HV]))
            .scaledReward(GTOMaterials.TbHoOxidesSolution.getBucket(), 2)
            .dependsOn(TBHO_SOLUTION));

    public static final Quest YTTRIUM_OXIDE = CHAPTER.quest("yttrium_oxide", q -> q
            .at(20, 8)
            .title("氧化钇")
            .desc(d -> d
                    .line("与铂处类似，你无需此刻完成所有金属粉的产出")
                    .line()
                    .line("不过它们的处理方法都一模一样，为什么不一起做完呢"))
            .icon(TagPrefix.dust, GTOMaterials.YttriumOxide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.YttriumOxide, 5))
            .scaledReward(GTOMaterials.TbHoOxidesSolution.getBucket(), 2)
            .dependsOn(DEHYDRATOR_HV));

    public static final Quest YTTRIUM_NITRATE = CHAPTER.quest("yttrium_nitrate", q -> q
            .at(18, 6)
            .milestone()
            .title("硝酸钇")
            .icon(TagPrefix.dust, GTOMaterials.YttriumNitrate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.YttriumNitrate, 13))
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 4)
            .dependsOn(YTTRIUM_OXIDE));

    public static final Quest BIOMASS = CHAPTER.quest("biomass", q -> q
            .at(10, -6)
            .title("生物质")
            .icon(GTMaterials.Biomass.getBucket())
            .task(Task.item(GTMaterials.Biomass.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTItems.PLANT_BALL, 8)
            .dependsOn(NewEvChapter.LARGE_CHEMICAL_REACTOR));

    public static final Quest ETHANOL = CHAPTER.quest("ethanol", q -> q
            .at(10, -4)
            .title("乙醇")
            .icon(GTMaterials.Ethanol.getBucket())
            .task(Task.item(GTMaterials.Ethanol.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Biomass.getBucket(), 2)
            .dependsOn(BIOMASS));

    public static final Quest GLYCEROL = CHAPTER.quest("glycerol", q -> q
            .at(10, -2)
            .title("甘油")
            .icon(GTMaterials.Glycerol.getBucket())
            .task(Task.item(GTMaterials.Glycerol.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SeedOil.getBucket(), 4)
            .dependsOn(ETHANOL));

    public static final Quest CHROMIUM_TRIOXIDE = CHAPTER.quest("chromium_trioxide", q -> q
            .at(6, -2)
            .title("三氧化铬")
            .icon(TagPrefix.dust, GTMaterials.ChromiumTrioxide)
            .task(Task.item(TagPrefix.dust, GTMaterials.ChromiumTrioxide, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Chromium, 2)
            .dependsOn(NewEvChapter.LARGE_CHEMICAL_REACTOR));

    public static final Quest POTASSIUM_DICHROMATE = CHAPTER.quest("potassium_dichromate", q -> q
            .at(8, -2)
            .title("重铬酸钾")
            .icon(TagPrefix.dust, GTMaterials.PotassiumDichromate)
            .task(Task.item(TagPrefix.dust, GTMaterials.PotassiumDichromate, 11))
            .scaledReward(TagPrefix.dust, GTMaterials.Saltpeter, 10)
            .dependsOn(CHROMIUM_TRIOXIDE));

    public static final Quest CITRIC_ACID = CHAPTER.quest("citric_acid", q -> q
            .at(10, 0)
            .milestone()
            .title("柠檬酸")
            .icon(GTOMaterials.CitricAcid.getBucket())
            .task(Task.item(GTOMaterials.CitricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Glycerol.getBucket(), 1)
            .dependsOn(GLYCEROL, POTASSIUM_DICHROMATE));

    public static final Quest BARIUM_SULFIDE = CHAPTER.quest("barium_sulfide", q -> q
            .at(16, 10)
            .title("硫化钡")
            .icon(TagPrefix.dust, GTMaterials.BariumSulfide)
            .task(Task.item(TagPrefix.dust, GTMaterials.BariumSulfide, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Barite, 12)
            .dependsOn(CITRIC_ACID));

    public static final Quest BARIUM_COPPER_NITRATE = CHAPTER.quest("barium_and_copper_nitrate", q -> q
            .at(18, 10)
            .title("硝酸钡与硝酸铜")
            .icon(TagPrefix.dust, GTOMaterials.BariumNitrate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.BariumNitrate, 18), Task.item(TagPrefix.dust, GTOMaterials.CopperNitrate, 27))
            .scaledReward(GTMaterials.NitricAcid.getBucket(), 4)
            .dependsOn(BARIUM_SULFIDE));

    public static final Quest WELL_MIXED_YBC_OXIDES = CHAPTER.quest("well_mixed_ybc_oxides", q -> q
            .at(18, 8)
            .title("混匀钇钡铜氧化物")
            .icon(TagPrefix.dust, GTOMaterials.WellMixedYbcOxides)
            .task(Task.item(TagPrefix.dust, GTOMaterials.WellMixedYbcOxides, 12))
            .scaledReward(GTOMaterials.CitricAcid.getBucket(), 1)
            .dependsOn(YTTRIUM_NITRATE, BARIUM_COPPER_NITRATE));

    public static final QuestLink WELL_MIXED_YBC_OXIDES_COPY_1 = CHAPTER.copy(WELL_MIXED_YBC_OXIDES, 10, 8);

    public static final Quest YBCO = CHAPTER.quest("yttrium_barium_cuprate", q -> q
            .at(12, 8)
            .title("钇钡铜氧")
            .desc(d -> d
                    .line("为什么它的{合金冶炼}电压需求这么夸张？！或许我们要考虑{电弧炉}...", GOLD, GOLD)
                    .line()
                    .line("{本质上还是化工产线}", GRAY.italic()))
            .icon(TagPrefix.ingot, GTMaterials.YttriumBariumCuprate)
            .task(Task.item(GTMachines.ARC_FURNACE[GTValues.EV]), Task.item(TagPrefix.ingot, GTMaterials.YttriumBariumCuprate))
            .scaledReward(TagPrefix.dust, GTOMaterials.WellMixedYbcOxides, 3)
            .dependsOn(WELL_MIXED_YBC_OXIDES_COPY_1));

    public static final Quest YBCO_ROD = CHAPTER.quest("yttrium_barium_cuprate_long_rod", q -> q
            .at(12, 10)
            .title("钇钡铜氧长棒")
            .icon(TagPrefix.rodLong, GTMaterials.YttriumBariumCuprate)
            .task(Task.item(TagPrefix.rodLong, GTMaterials.YttriumBariumCuprate))
            .scaledReward(TagPrefix.ingot, GTMaterials.YttriumBariumCuprate, 1)
            .dependsOn(YBCO));

    public static final Quest SUGAR_GEM = CHAPTER.quest("sugar_gem", q -> q
            .at(14, 10)
            .title("糖宝石")
            .icon(TagPrefix.gem, GTMaterials.Sugar)
            .task(Task.item(TagPrefix.gem, GTMaterials.Sugar, 12))
            .scaledReward(Items.SUGAR, 16)
            .dependsOn(YBCO_ROD));

    public static final Quest GLUCOSE = CHAPTER.quest("glucose", q -> q
            .at(14, 8)
            .title("葡萄糖")
            .icon(TagPrefix.dust, GTOMaterials.Glucose)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Glucose, 72))
            .scaledReward(TagPrefix.gem, GTMaterials.Sugar, 3)
            .dependsOn(SUGAR_GEM));

    public static final Quest GLUCOSE_IRON_SOLUTION = CHAPTER.quest("glucose_iron_solution", q -> q
            .at(14, 6)
            .milestone()
            .title("葡萄糖铁溶液")
            .icon(GTOMaterials.GlucoseIronSolution.getBucket())
            .task(Task.item(GTOMaterials.GlucoseIronSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.Glucose, 24)
            .dependsOn(GLUCOSE));

    public static final Quest GRAPHENE_IRON_PLATE = CHAPTER.quest("graphene_iron_plate", q -> q
            .at(14, 4)
            .title("石墨烯铁板")
            .icon(GTOItems.GRAPHENE_IRON_PLATE)
            .task(Task.item(GTOItems.GRAPHENE_IRON_PLATE, 3))
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 9)
            .dependsOn(GLUCOSE_IRON_SOLUTION));

    public static final Quest ELECTROMAGNETIC_SEPARATOR_LV = CHAPTER.quest("electromagnetic_separator_lv", q -> q
            .at(12, 4)
            .title("LV 电磁选矿机")
            .task(Task.item(GTMachines.ELECTROMAGNETIC_SEPARATOR[GTValues.LV]))
            .scaledReward(GTOItems.GRAPHENE_IRON_PLATE, 1)
            .dependsOn(GRAPHENE_IRON_PLATE));

    public static final Quest GRAPHENE_OXIDE = CHAPTER.quest("graphene_oxide", q -> q
            .at(10, 4)
            .title("氧化石墨烯")
            .icon(TagPrefix.dust, GTOMaterials.GrapheneOxide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.GrapheneOxide, 27))
            .scaledReward(GTOItems.GRAPHENE_IRON_PLATE, 1)
            .dependsOn(ELECTROMAGNETIC_SEPARATOR_LV));

    public static final QuestLink GRAPHENE_OXIDE_COPY_1 = CHAPTER.copy(GRAPHENE_OXIDE, 0, 8);

    public static final Quest TUNGSTEN_INGOT = CHAPTER.quest("tungsten_ingot", q -> q
            .at(8, 4)
            .title("钨锭")
            .icon(TagPrefix.ingot, GTMaterials.Tungsten)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Tungsten, 24))
            .scaledReward(TagPrefix.dust, GTMaterials.Tungsten, 6)
            .dependsOn(GRAPHENE_OXIDE));

    public static final Quest TUNGSTEN_CABLE = CHAPTER.quest("tungsten_cable", q -> q
            .at(8, 6)
            .milestone()
            .title("钨线缆")
            .icon(TagPrefix.cableGtSingle, GTMaterials.Tungsten)
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Tungsten, 40))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Tungsten, 10)
            .dependsOn(TUNGSTEN_INGOT));

    public static final Quest GRAPHENE_FOIL = CHAPTER.quest("graphene_foil", q -> q
            .at(0, 10)
            .title("石墨烯箔")
            .desc(d -> d.line("你必须研磨 化学气相沉积产出的{石墨烯箔} 来获得第一批石墨烯粉，此后你可以使用分子重组仪", GOLD))
            .icon(TagPrefix.foil, GTMaterials.Graphene)
            .task(Task.item(TagPrefix.foil, GTMaterials.Graphene, 64))
            .scaledReward(TagPrefix.dust, GTOMaterials.GrapheneOxide, 6)
            .dependsOn(GRAPHENE_OXIDE_COPY_1, NewEvChapter.CHEMICAL_VAPOR_DEPOSITION));

    public static final Quest GRAPHENE = CHAPTER.quest("graphene", q -> q
            .at(2, 10)
            .title("石墨烯")
            .icon(TagPrefix.ingot, GTMaterials.Graphene)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Graphene, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.Graphene, 16)
            .dependsOn(GRAPHENE_FOIL));

    public static final Quest IV_MOTOR = CHAPTER.quest("iv_motor", q -> q
            .at(4, 10)
            .title("IV 马达")
            .subtitle("产能大爆发")
            .desc(d -> d
                    .line("强导压时代解锁了多方块版本的加工机器，它们具有更高的处理速度和更低的能耗！")
                    .line()
                    .line("现在你的目标是努力向{IV能源仓}和{量子电路}进发", GOLD, GOLD))
            .icon(GTItems.ELECTRIC_MOTOR_IV)
            .task(Task.item(GTItems.ELECTRIC_MOTOR_IV, 4))
            .scaledReward(TagPrefix.wireGtDouble, GTMaterials.Graphene, 4)
            .dependsOn(GRAPHENE, TUNGSTEN_CABLE));

    public static final Quest IV_COMPONENTS = CHAPTER.quest("iv_components", q -> q
            .at(4, 8)
            .title("IV 泵与传送带")
            .icon(GTItems.ELECTRIC_PUMP_IV)
            .task(Task.item(GTItems.ELECTRIC_PUMP_IV, 2), Task.item(GTItems.CONVEYOR_MODULE_IV, 3))
            .scaledReward(GTItems.ELECTRIC_MOTOR_IV, 1)
            .dependsOn(IV_MOTOR));

    public static final Quest IV_ROBOT_ARM = CHAPTER.quest("iv_robot_arm", q -> q
            .at(4, 4)
            .title("IV 机械臂")
            .icon(GTItems.ROBOT_ARM_IV)
            .task(Task.item(GTItems.ROBOT_ARM_IV, 2))
            .scaledReward(TagPrefix.plate, GTMaterials.TungstenSteel, 4)
            .dependsOn(NewEvChapter.MAINFRAME));

    public static final Quest IV_ASSEMBLER = CHAPTER.quest("iv_assembler", q -> q
            .at(4, 6)
            .milestone()
            .title("IV 组装机")
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.IV]))
            .scaledReward(TagPrefix.plate, GTMaterials.TungstenSteel, 6)
            .dependsOn(IV_ROBOT_ARM, IV_COMPONENTS));

    public static final Quest BUTYRALDEHYDE = CHAPTER.quest("butyraldehyde", q -> q
            .at(16, 4)
            .title("丁醛")
            .icon(GTMaterials.Butyraldehyde.getBucket())
            .task(Task.item(GTMaterials.Butyraldehyde.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Propene.getBucket(), 2)
            .dependsOn(NewEvChapter.PROPENE));

    public static final Quest VINYL_ACETATE = CHAPTER.quest("vinyl_acetate", q -> q
            .at(18, 4)
            .title("醋酸乙烯酯")
            .icon(GTMaterials.VinylAcetate.getBucket())
            .task(Task.item(GTMaterials.VinylAcetate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.AceticAcid.getBucket(), 2)
            .dependsOn(NewEvChapter.PROPENE));

    public static final Quest POLYVINYL_ACETATE = CHAPTER.quest("polyvinyl_acetate", q -> q
            .at(18, 2)
            .title("聚醋酸乙烯酯")
            .icon(GTMaterials.PolyvinylAcetate.getBucket())
            .task(Task.item(GTMaterials.PolyvinylAcetate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.VinylAcetate.getBucket(), 2)
            .dependsOn(VINYL_ACETATE));

    public static final Quest POLYVINYL_BUTYRAL = CHAPTER.quest("polyvinyl_butyral", q -> q
            .at(16, 2)
            .title("聚乙烯醇缩丁醛")
            .desc(d -> d.line("夹层玻璃所需"))
            .icon(GTMaterials.PolyvinylButyral.getBucket())
            .task(Task.item(GTMaterials.PolyvinylButyral.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Butyraldehyde.getBucket(), 2)
            .dependsOn(BUTYRALDEHYDE, POLYVINYL_ACETATE));

    public static final Quest POLYVINYL_BUTYRAL_PLATE = CHAPTER.quest("polyvinyl_butyral_plate", q -> q
            .at(14, 2)
            .title("聚乙烯醇缩丁醛板")
            .icon(TagPrefix.plate, GTMaterials.PolyvinylButyral)
            .task(Task.item(TagPrefix.plate, GTMaterials.PolyvinylButyral, 5))
            .scaledReward(GTMaterials.PolyvinylButyral.getBucket(), 1)
            .dependsOn(POLYVINYL_BUTYRAL));

    public static final Quest LAMINATED_GLASS = CHAPTER.quest("laminated_glass", q -> q
            .at(14, 0)
            .milestone()
            .title("夹层玻璃")
            .task(Task.item(GTBlocks.CASING_LAMINATED_GLASS, 5))
            .scaledReward(GTBlocks.CASING_TEMPERED_GLASS, 4)
            .dependsOn(POLYVINYL_BUTYRAL_PLATE));

    public static final Quest IV_CUTTER = CHAPTER.quest("iv_cutter", q -> q
            .at(2, 4)
            .title("IV 切割机")
            .task(Task.item(GTMachines.CUTTER[GTValues.IV]))
            .scaledReward(GTBlocks.CASING_LAMINATED_GLASS, 2)
            .dependsOn(IV_ASSEMBLER, LAMINATED_GLASS));

    public static final Quest MOLYBDENUM_AND_VANADIUM = CHAPTER.quest("molybdenum_and_vanadium", q -> q
            .at(0, 2)
            .title("钼与钒")
            .icon(TagPrefix.dust, GTMaterials.Molybdenum)
            .task(Task.item(TagPrefix.dust, GTMaterials.Molybdenum, 16), Task.item(TagPrefix.dust, GTMaterials.Vanadium, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.VanadiumMagnetite, 8)
            .dependsOn(IV_CUTTER));

    public static final QuestLink MOLYBDENUM_AND_VANADIUM_COPY_1 = CHAPTER.copy(MOLYBDENUM_AND_VANADIUM, -10, -2);

    public static final Quest POTASSIUM_PYROSULFATE = CHAPTER.quest("potassium_pyrosulfate", q -> q
            .at(-10, 0)
            .title("焦硫酸钾")
            .icon(TagPrefix.dust, GTOMaterials.PotassiumPyrosulfate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.PotassiumPyrosulfate, 88))
            .scaledReward(TagPrefix.dust, GTMaterials.Potassium, 8)
            .dependsOn(MOLYBDENUM_AND_VANADIUM_COPY_1));

    public static final Quest RHODIUM_SULFATE_GAS = CHAPTER.quest("rhodium_sulfate_gas", q -> q
            .at(-8, 0)
            .title("硫酸铑")
            .icon(GTOMaterials.RhodiumSulfateGas.getBucket())
            .task(Task.item(GTOMaterials.RhodiumSulfateGas.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.PlatinumSlag, 12)
            .dependsOn(POTASSIUM_PYROSULFATE));

    public static final Quest RHODIUM_SULFATE = CHAPTER.quest("rhodium_sulfate", q -> q
            .at(-8, 2)
            .title("硫酸铑溶液")
            .icon(GTMaterials.RhodiumSulfate.getBucket())
            .task(Task.item(GTMaterials.RhodiumSulfate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RhodiumSulfateGas.getBucket(), 1)
            .dependsOn(RHODIUM_SULFATE_GAS));

    public static final Quest ROUGHLY_RHODIUM = CHAPTER.quest("roughly_rhodium_metal", q -> q
            .at(-8, 4)
            .title("粗铑金属粉")
            .icon(TagPrefix.dust, GTOMaterials.RoughlyRhodiumMetal)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RoughlyRhodiumMetal, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.Zinc, 6)
            .dependsOn(RHODIUM_SULFATE));

    public static final Quest RHODIUM_SALT = CHAPTER.quest("rhodium_salt", q -> q
            .at(-6, 4)
            .title("铑盐")
            .icon(TagPrefix.dust, GTOMaterials.RhodiumSalt)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RhodiumSalt, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Salt, 12)
            .dependsOn(ROUGHLY_RHODIUM));

    public static final Quest RHODIUM_SALT_SOLUTION = CHAPTER.quest("rhodium_salt_solution", q -> q
            .at(-4, 4)
            .title("铑盐溶液")
            .icon(GTOMaterials.RhodiumSaltSolution.getBucket())
            .task(Task.item(GTOMaterials.RhodiumSaltSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.RhodiumSalt, 4)
            .dependsOn(RHODIUM_SALT));

    public static final Quest SODIUM_NITRATE = CHAPTER.quest("sodium_nitrate", q -> q
            .at(-4, 6)
            .title("硝酸钠")
            .icon(TagPrefix.dust, GTOMaterials.SodiumNitrate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SodiumNitrate, 20))
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 2)
            .dependsOn(RHODIUM_SALT));

    public static final Quest RHODIUM_NITRATE = CHAPTER.quest("rhodium_nitrate", q -> q
            .at(-2, 6)
            .milestone()
            .title("硝酸铑")
            .icon(TagPrefix.dust, GTOMaterials.RhodiumNitrate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RhodiumNitrate, 52))
            .scaledReward(TagPrefix.dust, GTOMaterials.SodiumNitrate, 5)
            .dependsOn(RHODIUM_SALT_SOLUTION, SODIUM_NITRATE));

    public static final Quest RHODIUM_FILTER_CAKE = CHAPTER.quest("rhodium_filter_cake", q -> q
            .at(-4, 8)
            .title("铑滤饼")
            .icon(TagPrefix.dust, GTOMaterials.RhodiumFilterCake)
            .task(Task.item(TagPrefix.dust, GTOMaterials.RhodiumFilterCake, 12))
            .scaledReward(TagPrefix.dust, GTOMaterials.RhodiumNitrate, 13)
            .dependsOn(RHODIUM_NITRATE));

    public static final Quest RHODIUM_FILTER_CAKE_SOLUTION = CHAPTER.quest("rhodium_filter_cake_solution", q -> q
            .at(-6, 10)
            .title("铑滤饼溶液")
            .icon(GTOMaterials.RhodiumFilterCakeSolution.getBucket())
            .task(Task.item(GTOMaterials.RhodiumFilterCakeSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.RhodiumFilterCake, 6)
            .dependsOn(RHODIUM_FILTER_CAKE));

    public static final Quest REPRECIPITATED_RHODIUM = CHAPTER.quest("reprecipitated_rhodium", q -> q
            .at(-6, 12)
            .title("再沉淀铑")
            .icon(TagPrefix.dust, GTOMaterials.ReprecipitatedRhodium)
            .task(Task.item(TagPrefix.dust, GTOMaterials.ReprecipitatedRhodium, 14))
            .scaledReward(GTOMaterials.RhodiumFilterCakeSolution.getBucket(), 1)
            .dependsOn(RHODIUM_FILTER_CAKE_SOLUTION));

    public static final Quest RHODIUM = CHAPTER.quest("rhodium", q -> q
            .at(-4, 12)
            .title("铑")
            .icon(TagPrefix.plateDouble, GTMaterials.Rhodium)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.Rhodium))
            .scaledReward(TagPrefix.dust, GTOMaterials.ReprecipitatedRhodium, 7)
            .dependsOn(REPRECIPITATED_RHODIUM));

    public static final Quest HSLA_STEEL = CHAPTER.quest("hsla_steel", q -> q
            .at(-2, 0)
            .title("HSLA 钢")
            .icon(TagPrefix.ingot, GTMaterials.HSLASteel)
            .task(Task.item(TagPrefix.ingot, GTMaterials.HSLASteel, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Invar, 16)
            .dependsOn(MOLYBDENUM_AND_VANADIUM));

    public static final Quest NONCONDUCTING_CASING = CHAPTER.quest("nonconducting_casing", q -> q
            .at(-4, 2)
            .title("不导电外壳")
            .task(Task.item(GCYMBlocks.CASING_NONCONDUCTING, 62))
            .scaledReward(TagPrefix.plate, GTMaterials.HSLASteel, 24)
            .dependsOn(HSLA_STEEL));

    public static final Quest ELECTROLYTIC_CELL = CHAPTER.quest("electrolytic_cell", q -> q
            .at(-2, 4)
            .title("电解池")
            .task(Task.item(GCYMBlocks.ELECTROLYTIC_CELL, 10))
            .scaledReward(TagPrefix.wireGtDouble, GTMaterials.Platinum, 8)
            .dependsOn(NONCONDUCTING_CASING));

    public static final Quest WHITE_DYE = CHAPTER.quest("white_dye", q -> q
            .at(-2, 2)
            .title("白色染料")
            .icon(GTMaterials.DyeWhite.getBucket())
            .task(Task.item(GTMaterials.DyeWhite.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.WHITE_DYE, 8)
            .dependsOn(NONCONDUCTING_CASING));

    public static final Quest CHEMICAL_GRADE_GLASS = CHAPTER.quest("chemical_grade_glass", q -> q
            .at(0, 4)
            .title("化学级玻璃")
            .task(Task.item(GTOBlocks.CHEMICAL_GRADE_GLASS, 10))
            .scaledReward(GTBlocks.CASING_TEMPERED_GLASS, 6)
            .dependsOn(WHITE_DYE));

    public static final Quest ELECTROPLATING_BATH = CHAPTER.quest("electroplating_bath", q -> q
            .at(0, 6)
            .milestone()
            .title("电镀池")
            .desc(d -> d.line("来自GTO的新机器，用于{电镀}和{给转子镀膜}", YELLOW, YELLOW))
            .task(Task.item(MultiBlockH.ELECTROPLATING_BATH), Task.item(TagPrefix.frameGt, GTMaterials.Polytetrafluoroethylene, 4))
            .scaledReward(GTBlocks.CASING_PTFE_INERT, 8)
            .dependsOn(ELECTROLYTIC_CELL, CHEMICAL_GRADE_GLASS));

    public static final Quest SODIUM_FORMATE = CHAPTER.quest("sodium_formate", q -> q
            .at(-4, 16)
            .title("甲酸钠")
            .icon(GTOMaterials.SodiumFormate.getBucket())
            .task(Task.item(GTOMaterials.SodiumFormate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.CarbonMonoxide.getBucket(), 4)
            .dependsOn(ELECTROPLATING_BATH));

    public static final Quest FORMIC_ACID = CHAPTER.quest("formic_acid", q -> q
            .at(-2, 16)
            .title("甲酸")
            .icon(GTMaterials.FormicAcid.getBucket())
            .task(Task.item(GTMaterials.FormicAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.SodiumFormate.getBucket(), 2)
            .dependsOn(SODIUM_FORMATE));

    public static final Quest PALLADIUM = CHAPTER.quest("palladium", q -> q
            .at(-2, 14)
            .title("钯")
            .desc(d -> d.line("无需经过复杂的矿泥处理也可以获取"))
            .icon(TagPrefix.plate, GTMaterials.Palladium)
            .task(Task.item(TagPrefix.plate, GTMaterials.Palladium, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.PalladiumRaw, 10)
            .dependsOn(FORMIC_ACID));

    public static final Quest RHODIUM_PLATED_PALLADIUM = CHAPTER.quest("rhodium_plated_palladium", q -> q
            .at(-2, 12)
            .milestone()
            .title("镀铑钯")
            .icon(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.RhodiumPlatedPalladium, 2)
            .dependsOn(RHODIUM, PALLADIUM));

    public static final Quest SODA_ASH = CHAPTER.quest("soda_ash", q -> q
            .at(-4, 14)
            .title("苏打灰")
            .icon(TagPrefix.dust, GTMaterials.SodaAsh)
            .task(Task.item(TagPrefix.dust, GTMaterials.SodaAsh, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 12)
            .dependsOn(RHODIUM_PLATED_PALLADIUM));

    public static final QuestLink SODA_ASH_COPY_1 = CHAPTER.copy(SODA_ASH, -2, 20);

    public static final Quest RAREST_METAL_MIXTURE = CHAPTER.quest("rarest_metal_mixture", q -> q
            .at(-6, 16)
            .title("稀有金属混合物")
            .icon(TagPrefix.dust, GTMaterials.RarestMetalMixture)
            .task(Task.item(TagPrefix.dust, GTMaterials.RarestMetalMixture, 20))
            .scaledReward(TagPrefix.dust, GTOMaterials.LeachResidue, 10)
            .dependsOn(SODA_ASH));

    public static final QuestLink RAREST_METAL_MIXTURE_COPY_1 = CHAPTER.copy(RAREST_METAL_MIXTURE, 2, 24);

    public static final Quest SODIUM_HYPOCHLORITE = CHAPTER.quest("sodium_hypochlorite", q -> q
            .at(0, 20)
            .title("次氯酸钠")
            .icon(TagPrefix.dust, GTOMaterials.SodiumHypochlorite)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SodiumHypochlorite, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 12)
            .dependsOn(SODA_ASH_COPY_1));

    public static final Quest SODIUM_CHLORATE = CHAPTER.quest("sodium_chlorate", q -> q
            .at(2, 20)
            .title("氯酸钠")
            .icon(TagPrefix.dust, GTOMaterials.SodiumChlorate)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SodiumChlorate, 10))
            .scaledReward(TagPrefix.dust, GTOMaterials.SodiumHypochlorite, 9)
            .dependsOn(SODIUM_HYPOCHLORITE));

    public static final Quest IRIDIUM_METAL_RESIDUE = CHAPTER.quest("iridium_metal_residue", q -> q
            .at(2, 22)
            .title("铱金属残渣")
            .icon(TagPrefix.dust, GTMaterials.IridiumMetalResidue)
            .task(Task.item(TagPrefix.dust, GTMaterials.IridiumMetalResidue, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.RarestMetalMixture, 7)
            .dependsOn(RAREST_METAL_MIXTURE_COPY_1));

    public static final Quest IRIDIUM_DIOXIDE = CHAPTER.quest("iridium_dioxide", q -> q
            .at(4, 20)
            .title("二氧化铱")
            .icon(TagPrefix.dust, GTOMaterials.IridiumDioxide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.IridiumDioxide, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.IridiumMetalResidue, 6)
            .dependsOn(IRIDIUM_METAL_RESIDUE, SODIUM_CHLORATE));

    public static final Quest ACIDIC_IRIDIUM = CHAPTER.quest("acidic_iridium", q -> q
            .at(6, 18)
            .title("酸性铱溶液")
            .icon(GTOMaterials.AcidicIridium.getBucket())
            .task(Task.item(GTOMaterials.AcidicIridium.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.IridiumDioxide, 3)
            .dependsOn(IRIDIUM_DIOXIDE));

    public static final Quest IRIDIUM_CHLORIDE = CHAPTER.quest("iridium_chloride", q -> q
            .at(8, 16)
            .title("氯化铱")
            .icon(TagPrefix.dust, GTMaterials.IridiumChloride)
            .task(Task.item(TagPrefix.dust, GTMaterials.IridiumChloride, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.AmmoniumChloride, 18)
            .dependsOn(ACIDIC_IRIDIUM));

    public static final Quest RUTHENIUM_TETROXIDE = CHAPTER.quest("ruthenium_tetroxide", q -> q
            .at(-6, 18)
            .title("四氧化钌")
            .icon(GTMaterials.RutheniumTetroxide.getBucket())
            .task(Task.item(GTMaterials.RutheniumTetroxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.SodiumRutheniate, 7)
            .dependsOn(RAREST_METAL_MIXTURE));

    public static final Quest HOT_RUTHENIUM_TETROXIDE = CHAPTER.quest("hot_ruthenium_tetroxide", q -> q
            .at(-6, 20)
            .title("热四氧化钌")
            .icon(GTOMaterials.RutheniumTetroxideHot.getBucket())
            .task(Task.item(GTOMaterials.RutheniumTetroxideHot.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.RutheniumTetroxide.getBucket(), 2)
            .dependsOn(RUTHENIUM_TETROXIDE));

    public static final Quest LIQUID_RUTHENIUM_TETROXIDE = CHAPTER.quest("liquid_ruthenium_tetroxide", q -> q
            .at(-4, 18)
            .title("液态四氧化钌")
            .icon(GTOMaterials.RutheniumTetroxideLQ.getBucket())
            .task(Task.item(GTOMaterials.RutheniumTetroxideLQ.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RutheniumTetroxideHot.getBucket(), 2)
            .dependsOn(HOT_RUTHENIUM_TETROXIDE));

    public static final Quest RUTHENIUM_TETROXIDE_DUST = CHAPTER.quest("ruthenium_tetroxide_dust", q -> q
            .at(-2, 18)
            .title("四氧化钌粉")
            .icon(TagPrefix.dust, GTMaterials.RutheniumTetroxide)
            .task(Task.item(TagPrefix.dust, GTMaterials.RutheniumTetroxide, 20))
            .scaledReward(GTOMaterials.RutheniumTetroxideLQ.getBucket(), 1)
            .dependsOn(LIQUID_RUTHENIUM_TETROXIDE));

    public static final Quest RUTHENIUM = CHAPTER.quest("ruthenium", q -> q
            .at(0, 16)
            .title("钌")
            .icon(TagPrefix.dust, GTMaterials.Ruthenium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Ruthenium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.RutheniumTetroxide, 5)
            .dependsOn(RUTHENIUM_TETROXIDE_DUST));

    public static final QuestLink RUTHENIUM_COPY_1 = CHAPTER.copy(RUTHENIUM, 14, 16);

    public static final Quest VANADIUM_GALLIUM_DUST = CHAPTER.quest("vanadium_gallium_dust", q -> q
            .at(16, 16)
            .title("钒镓粉")
            .icon(TagPrefix.dust, GTMaterials.VanadiumGallium)
            .task(Task.item(TagPrefix.dust, GTMaterials.VanadiumGallium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Vanadium, 3)
            .dependsOn(RUTHENIUM_COPY_1));

    public static final Quest RTM_ALLOY_DUST = CHAPTER.quest("rtm_alloy_dust", q -> q
            .at(0, 14)
            .title("RTM 合金粉")
            .icon(TagPrefix.dust, GTMaterials.RTMAlloy)
            .task(Task.item(TagPrefix.dust, GTMaterials.RTMAlloy, 7))
            .scaledReward(TagPrefix.dust, GTMaterials.Molybdenum, 2)
            .dependsOn(RUTHENIUM));

    public static final Quest RTM_ALLOY = CHAPTER.quest("rtm_alloy", q -> q
            .at(2, 12)
            .milestone()
            .title("RTM 合金锭")
            .icon(TagPrefix.ingot, GTMaterials.RTMAlloy)
            .task(Task.item(TagPrefix.ingot, GTMaterials.RTMAlloy, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.RTMAlloy, 7)
            .dependsOn(RTM_ALLOY_DUST));

    public static final Quest RTM_COIL = CHAPTER.quest("rtm_alloy_coil", q -> q
            .at(8, 12)
            .milestone()
            .title("RTM 合金线圈")
            .desc(d -> d.line("T4级别的线圈方块，集合了你至今为止所有的矿处产线成果"))
            .icon(GTBlocks.COIL_RTMALLOY)
            .task(Task.item(GTBlocks.COIL_RTMALLOY, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.VanadiumSteel, 32)
            .dependsOn(RTM_ALLOY));

    public static final Quest VANADIUM_GALLIUM = CHAPTER.quest("vanadium_gallium", q -> q
            .at(18, 16)
            .title("钒镓")
            .icon(TagPrefix.ingot, GTMaterials.VanadiumGallium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.VanadiumGallium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.VanadiumGallium, 4)
            .dependsOn(RTM_COIL, VANADIUM_GALLIUM_DUST));

    public static final QuestLink VANADIUM_GALLIUM_COPY_1 = CHAPTER.copy(VANADIUM_GALLIUM, 28, 6);

    public static final Quest IRIDIUM = CHAPTER.quest("iridium", q -> q
            .at(10, 14)
            .title("铱")
            .desc(d -> d.line("下个阶段的重要材料"))
            .icon(TagPrefix.ingot, GTMaterials.Iridium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Iridium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.IridiumChloride, 4)
            .dependsOn(RTM_COIL, IRIDIUM_CHLORIDE));

    public static final Quest IV_VOLTAGE_COIL = CHAPTER.quest("iv_voltage_coil", q -> q
            .at(10, 12)
            .milestone()
            .title("IV 电压线圈")
            .icon(GTItems.VOLTAGE_COIL_IV)
            .task(Task.item(GTItems.VOLTAGE_COIL_IV, 2))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Iridium, 8)
            .dependsOn(IRIDIUM));

    public static final Quest LIQUID_VANADIUM_GALLIUM = CHAPTER.quest("liquid_vanadium_gallium", q -> q
            .at(28, 8)
            .title("液态钒镓")
            .icon(GTMaterials.VanadiumGallium.getBucket())
            .task(Task.item(GTMaterials.VanadiumGallium.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.ingot, GTMaterials.VanadiumGallium, 1)
            .dependsOn(VANADIUM_GALLIUM_COPY_1));

    public static final Quest INDIUM_CONCENTRATE = CHAPTER.quest("indium_concentrate", q -> q
            .at(18, 14)
            .title("铟浓缩液")
            .icon(GTMaterials.IndiumConcentrate.getBucket())
            .task(Task.item(GTMaterials.IndiumConcentrate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 4)
            .dependsOn(VANADIUM_GALLIUM));

    public static final Quest INDIUM = CHAPTER.quest("indium", q -> q
            .at(16, 14)
            .title("铟")
            .icon(TagPrefix.dust, GTMaterials.Indium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Indium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Aluminium, 16)
            .dependsOn(INDIUM_CONCENTRATE));

    public static final Quest INDIUM_GALLIUM_PHOSPHIDE = CHAPTER.quest("indium_gallium_phosphide", q -> q
            .at(14, 14)
            .title("铟镓磷")
            .icon(TagPrefix.dust, GTMaterials.IndiumGalliumPhosphide)
            .task(Task.item(TagPrefix.dust, GTMaterials.IndiumGalliumPhosphide, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Gallium, 2)
            .dependsOn(INDIUM));

    public static final QuestLink INDIUM_GALLIUM_PHOSPHIDE_COPY_1 = CHAPTER.copy(INDIUM_GALLIUM_PHOSPHIDE, 30, 10);

    public static final Quest HPIC_WAFER = CHAPTER.quest("hpic_wafer", q -> q
            .at(28, 10)
            .title("高功率集成电路晶圆")
            .icon(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT_WAFER)
            .task(Task.item(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 2))
            .scaledReward(GTItems.POWER_INTEGRATED_CIRCUIT_WAFER, 1)
            .dependsOn(LIQUID_VANADIUM_GALLIUM, INDIUM_GALLIUM_PHOSPHIDE_COPY_1));

    public static final Quest HPIC = CHAPTER.quest("hpic", q -> q
            .at(26, 12)
            .title("高功率集成电路")
            .icon(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT)
            .task(Task.item(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT, 4))
            .scaledReward(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT, 1)
            .dependsOn(HPIC_WAFER));

    public static final Quest IV_ENERGY_HATCH = CHAPTER.quest("iv_energy_hatch", q -> q
            .at(22, 12)
            .milestone()
            .title("IV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.IV], 2))
            .scaledReward(TagPrefix.cableGtDouble, GTMaterials.Tungsten, 4)
            .dependsOn(IV_VOLTAGE_COIL, HPIC));

    public static final QuestLink IV_ENERGY_HATCH_COPY_1 = CHAPTER.copy(IV_ENERGY_HATCH, 24, 24);

    public static final Quest THERMAL_CENTRIFUGE_LV = CHAPTER.quest("thermal_centrifuge_lv", q -> q
            .at(4, 24)
            .title("LV 热力离心机")
            .task(Task.item(GTMachines.THERMAL_CENTRIFUGE[GTValues.LV]))
            .scaledReward(TagPrefix.crushed, GTMaterials.Tantalite, 8)
            .dependsOn(IV_ENERGY_HATCH));

    public static final Quest NIOBIUM = CHAPTER.quest("niobium", q -> q
            .at(6, 24)
            .title("铌")
            .icon(TagPrefix.dust, GTMaterials.Niobium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Niobium, 2))
            .scaledReward(TagPrefix.crushed, GTMaterials.Tantalite, 8)
            .dependsOn(THERMAL_CENTRIFUGE_LV));

    public static final Quest NIOBIUM_TITANIUM_DUST = CHAPTER.quest("niobium_titanium_dust", q -> q
            .at(8, 24)
            .title("铌钛粉")
            .icon(TagPrefix.dust, GTMaterials.NiobiumTitanium)
            .task(Task.item(GTMachines.MIXER[GTValues.IV]), Task.item(TagPrefix.dust, GTMaterials.NiobiumTitanium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Titanium, 2)
            .dependsOn(NIOBIUM));

    public static final Quest NIOBIUM_TITANIUM = CHAPTER.quest("niobium_titanium", q -> q
            .at(8, 22)
            .title("铌钛锭")
            .icon(TagPrefix.ingot, GTMaterials.NiobiumTitanium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.NiobiumTitanium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.NiobiumTitanium, 2)
            .dependsOn(NIOBIUM_TITANIUM_DUST));

    public static final Quest SODIUM_SULFIDE = CHAPTER.quest("sodium_sulfide", q -> q
            .at(6, 22)
            .title("硫化钠")
            .icon(TagPrefix.dust, GTMaterials.SodiumSulfide)
            .task(Task.item(TagPrefix.dust, GTMaterials.SodiumSulfide, 3))
            .scaledReward(TagPrefix.dust, GTMaterials.Sodium, 2)
            .dependsOn(NIOBIUM));

    public static final Quest POLYPHENYLENE_SULFIDE = CHAPTER.quest("polyphenylene_sulfide", q -> q
            .at(4, 22)
            .title("聚苯硫醚")
            .icon(GTMaterials.PolyphenyleneSulfide.getBucket())
            .task(Task.item(GTMaterials.PolyphenyleneSulfide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Dichlorobenzene.getBucket(), 1)
            .dependsOn(SODIUM_SULFIDE));

    public static final Quest PPS_FOIL = CHAPTER.quest("polyphenylene_sulfide_foil", q -> q
            .at(6, 20)
            .title("聚苯硫醚箔")
            .desc(d -> d.line("PPS特种工程塑料，你需要它来制作线缆"))
            .icon(TagPrefix.foil, GTMaterials.PolyphenyleneSulfide)
            .task(Task.item(TagPrefix.foil, GTMaterials.PolyphenyleneSulfide, 2))
            .scaledReward(TagPrefix.foil, GTMaterials.PolyvinylChloride, 4)
            .dependsOn(POLYPHENYLENE_SULFIDE));

    public static final Quest NIOBIUM_TITANIUM_CABLE = CHAPTER.quest("niobium_titanium_cable", q -> q
            .at(8, 20)
            .title("铌钛线缆")
            .icon(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium)
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.NiobiumTitanium, 2)
            .dependsOn(NIOBIUM_TITANIUM, PPS_FOIL));

    public static final Quest LUV_CASING = CHAPTER.quest("luv_casing", q -> q
            .at(10, 20)
            .title("LuV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_LuV))
            .scaledReward(TagPrefix.plate, GTMaterials.RhodiumPlatedPalladium, 1)
            .dependsOn(NIOBIUM_TITANIUM_CABLE));

    public static final Quest LUV_HULL = CHAPTER.quest("luv_hull", q -> q
            .at(8, 18)
            .milestone()
            .title("LuV 机器外壳")
            .subtitle("{你需要天马行空般的想象力}", LIGHT_PURPLE)
            .desc(d -> d
                    .line("IV阶段解锁的大部分{大批量加工机器}都可以安装{8个}能源仓，这意味着你可以通过{升压}来直接加工本阶段的材料，而无需制造这个阶段的单方块机器——除了{组装机}，因为你需要一台{LuV级别的组装机}来制造{至少一个}{整体框架(LuV)}安装到你的大型组装厂", YELLOW, GREEN, YELLOW, GOLD, GOLD, AQUA, GOLD)
                    .line()
                    .line("还需要注意的是，你的{部件组装机}在这个阶段必须进行升级才能制作更高级的部件", GOLD)
                    .line()
                    .line("你的首要目标是制作{核聚变}", GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.LuV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.LuV], 1))
            .dependsOn(NIOBIUM_TITANIUM_CABLE, LUV_CASING));

    public static final Quest MOLECULAR_TRANSFORMER = CHAPTER.quest("molecular_transformer", q -> q
            .at(22, 24)
            .title("分子重组仪")
            .subtitle("分子料理")
            .desc(d -> d.line("YummYummmm……你想来一点吗？"))
            .task(Task.item(MultiBlockC.MOLECULAR_TRANSFORMER))
            .scaledReward(Items.WITHER_SKELETON_SKULL, 2)
            .dependsOn(NewEvChapter.IV_HULL));

    public static final Quest NETHER_STAR = CHAPTER.quest("nether_star", q -> q
            .at(22, 22)
            .title("下界之星")
            .desc(d -> d
                    .line("电弧发生器合成它需要{无重力环境}，这是你第二次前往空间站", BLUE)
                    .line()
                    .line("凋零骷髅头...看看工业屠宰场吧")
                    .line()
                    .line("亦或者通过{魔法}获取", LIGHT_PURPLE))
            .icon(TagPrefix.gem, GTMaterials.NetherStar)
            .task(Task.item(TagPrefix.gem, GTMaterials.NetherStar, 4))
            .scaledReward(Items.WITHER_SKELETON_SKULL, 2)
            .dependsOn(MOLECULAR_TRANSFORMER));

    public static final QuestLink NETHER_STAR_COPY_1 = CHAPTER.copy(NETHER_STAR, 8, 8);

    public static final Quest NANO_MAINFRAME = CHAPTER.quest("nano_processor_mainframe", q -> q
            .at(20, 20)
            .title("纳米处理器主机")
            .subtitle("第一个LuV级电路")
            .desc(d -> d.line("觉得太贵？别担心，你最终会找到更便宜的路径"))
            .icon(GTItems.NANO_MAINFRAME_LuV)
            .task(Task.item(GTItems.NANO_MAINFRAME_LuV, 4))
            .scaledReward(GTItems.RANDOM_ACCESS_MEMORY, 16)
            .dependsOn(NewEvChapter.NANO_COMPUTER));

    public static final Quest REINFORCED_EPOXY_RESIN = CHAPTER.quest("reinforced_epoxy_resin_plate", q -> q
            .at(16, -2)
            .title("增强环氧树脂板")
            .task(Task.item(TagPrefix.plate, GTMaterials.ReinforcedEpoxyResin, 8))
            .scaledReward(GTItems.CARBON_FIBERS, 8)
            .dependsOn(NewEvChapter.EPOXY));

    public static final Quest FIBER_BOARD = CHAPTER.quest("fiber_reinforced_circuit_board", q -> q
            .at(18, -2)
            .title("纤维强化电路基板")
            .task(Task.item(GTItems.FIBER_BOARD, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.AnnealedCopper, 16)
            .dependsOn(REINFORCED_EPOXY_RESIN));

    public static final Quest BORON = CHAPTER.quest("boron", q -> q
            .at(18, -4)
            .title("硼")
            .task(Task.item(TagPrefix.dust, GTMaterials.Boron, 36))
            .scaledReward(TagPrefix.dust, GTMaterials.Borax, 16)
            .dependsOn(NewEvChapter.IV_HULL));

    public static final Quest TUNGSTEN_BORON_MIXTURE = CHAPTER.quest("tungsten_boron_mixture", q -> q
            .at(18, -6)
            .title("钨硼混合物")
            .task(Task.item(TagPrefix.dust, GTOMaterials.TungstenBoronMixture, 9))
            .scaledReward(TagPrefix.dust, GTMaterials.Tungsten, 4)
            .dependsOn(BORON));

    public static final Quest TUNGSTEN_TETRABORIDE_DUST = CHAPTER.quest("tungsten_tetraboride_ceramics_dust", q -> q
            .at(20, -6)
            .title("四硼化钨陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.TungstenTetraborideCeramics, 9))
            .scaledReward(TagPrefix.dust, GTOMaterials.TungstenBoronMixture, 3)
            .dependsOn(TUNGSTEN_BORON_MIXTURE));

    public static final Quest TUNGSTEN_TETRABORIDE_ROUGH_BLANK = CHAPTER.quest("tungsten_tetraboride_ceramics_rough_blank", q -> q
            .at(20, -4)
            .title("四硼化钨陶瓷粗胚")
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.TungstenTetraborideCeramics, 1))
            .scaledReward(GTMaterials.Nickel.getBucket(), 1)
            .dependsOn(TUNGSTEN_TETRABORIDE_DUST));

    public static final Quest TUNGSTEN_TETRABORIDE_FLAKE = CHAPTER.quest("tungsten_tetraboride_ceramics_flake", q -> q
            .at(20, -2)
            .title("四硼化钨陶瓷薄片")
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.TungstenTetraborideCeramics, 16))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.TungstenTetraborideCeramics, 4)
            .dependsOn(TUNGSTEN_TETRABORIDE_ROUGH_BLANK));

    public static final Quest EXTREME_CIRCUIT_BOARD = CHAPTER.quest("extreme_circuit_board", q -> q
            .at(18, 0)
            .milestone()
            .title("极致电路板")
            .icon(GTItems.EXTREME_CIRCUIT_BOARD)
            .task(Task.item(GTItems.EXTREME_CIRCUIT_BOARD, 4))
            .scaledReward(TagPrefix.foil, GTMaterials.AnnealedCopper, 40)
            .dependsOn(FIBER_BOARD, TUNGSTEN_TETRABORIDE_FLAKE));

    public static final Quest QUANTUM_STAR = CHAPTER.quest("quantum_star", q -> q
            .at(22, 20)
            .title("量子之星")
            .task(Task.item(GTItems.QUANTUM_STAR, 4))
            .scaledReward(GTMaterials.Radon.getBucket(), 2)
            .dependsOn(NETHER_STAR, NewEvChapter.EARTH_ORBIT, NewEvChapter.MARS_RADON));

    public static final Quest IV_EMITTER_SENSOR = CHAPTER.quest("iv_emitter_and_sensor", q -> q
            .at(24, 20)
            .title("IV 发射器与传感器")
            .icon(GTItems.EMITTER_IV)
            .task(Task.item(GTItems.EMITTER_IV, 2), Task.item(GTItems.SENSOR_IV, 2))
            .scaledReward(GTItems.QUANTUM_STAR, 1)
            .dependsOn(QUANTUM_STAR));

    public static final QuestLink IV_EMITTER_SENSOR_COPY_1 = CHAPTER.copy(IV_EMITTER_SENSOR, 2, 16);

    public static final QuestLink IV_EMITTER_SENSOR_COPY_2 = CHAPTER.copy(IV_EMITTER_SENSOR, 2, 14);

    public static final QuestLink IV_EMITTER_SENSOR_COPY_3 = CHAPTER.copy(IV_EMITTER_SENSOR, 4, 18);

    public static final QuestLink IV_EMITTER_SENSOR_COPY_4 = CHAPTER.copy(IV_EMITTER_SENSOR, 28, 16);

    public static final QuestLink IV_EMITTER_SENSOR_COPY_5 = CHAPTER.copy(IV_EMITTER_SENSOR, 24, 10);

    public static final QuestLink IV_EMITTER_SENSOR_COPY_6 = CHAPTER.copy(IV_EMITTER_SENSOR, 26, 16);

    public static final QuestLink IV_EMITTER_SENSOR_COPY_7 = CHAPTER.copy(IV_EMITTER_SENSOR, 24, 16);

    public static final QuestLink IV_EMITTER_SENSOR_COPY_8 = CHAPTER.copy(IV_EMITTER_SENSOR, 30, 12);

    public static final Quest QBIT_WAFER = CHAPTER.quest("qbit_cpu_wafer", q -> q
            .at(14, 12)
            .milestone()
            .title("量子位 CPU 晶圆")
            .task(Task.item(GTItems.QUBIT_CENTRAL_PROCESSING_UNIT_WAFER, 2))
            .scaledReward(GTItems.NANO_CENTRAL_PROCESSING_UNIT_WAFER, 1)
            .dependsOn(NewEvChapter.NANO_CPU_WAFER, INDIUM_GALLIUM_PHOSPHIDE, NewEvChapter.MARS_RADON));

    public static final Quest QBIT_CPU = CHAPTER.quest("qbit_cpu_chip", q -> q
            .at(18, 12)
            .milestone()
            .title("量子位 CPU 芯片")
            .subtitle("更便宜的EV电路")
            .desc(d -> d
                    .line("有了它你就能够生产{量子系列电路}", GOLD)
                    .line()
                    .line("我更推荐你使用带有{铟}的配方", GOLD))
            .task(Task.item(GTItems.QUBIT_CENTRAL_PROCESSING_UNIT, 6))
            .scaledReward(GTItems.QUBIT_CENTRAL_PROCESSING_UNIT, 2)
            .dependsOn(QBIT_WAFER));

    public static final Quest QUANTUM_PROCESSOR = CHAPTER.quest("quantum_processor", q -> q
            .at(22, 18)
            .milestone()
            .title("量子处理器")
            .subtitle("更便宜的EV级电路")
            .desc(d -> d.line("老规矩，制造{IV电路组装机}需要消耗两个昂贵的{LuV级电路}", GOLD, GOLD))
            .icon(GTItems.QUANTUM_PROCESSOR_EV)
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.IV]),
                    Task.item(GTItems.QUANTUM_PROCESSOR_EV, 6))
            .scaledReward(GTItems.SMD_CAPACITOR, 16)
            .dependsOn(QBIT_CPU, EXTREME_CIRCUIT_BOARD, NANO_MAINFRAME, IV_EMITTER_SENSOR));

    public static final Quest QUANTUM_ASSEMBLY = CHAPTER.quest("quantum_processor_assembly", q -> q
            .at(18, 18)
            .milestone()
            .title("量子处理器集群")
            .subtitle("更便宜的IV级电路")
            .task(Task.item(GTItems.QUANTUM_ASSEMBLY_IV, 6))
            .scaledReward(GTItems.SMD_INDUCTOR, 8)
            .dependsOn(QUANTUM_PROCESSOR));

    public static final Quest QUANTUM_COMPUTER = CHAPTER.quest("quantum_processor_supercomputer", q -> q
            .at(12, 18)
            .milestone()
            .title("量子处理器超级计算机")
            .subtitle("更便宜的LuV级电路")
            .task(Task.item(GTItems.QUANTUM_COMPUTER_LuV, 2))
            .scaledReward(GTItems.RANDOM_ACCESS_MEMORY, 16)
            .dependsOn(QUANTUM_ASSEMBLY));

    public static final Quest IV_PARALLEL_HATCH = CHAPTER.quest("iv_parallel_hatch", q -> q
            .at(26, 22)
            .side()
            .desc(d -> d
                    .line("为你的大机器提供并行！")
                    .line()
                    .line("允许机器一次性以多倍功率处理多个配方，某种意义下相当于提供无损超频。"))
            .task(Task.item(GCYMMachines.PARALLEL[GTValues.IV]))
            .dependsOn(IV_EMITTER_SENSOR));

    public static final Quest IV_PROCESSING_ARRAY = CHAPTER.quest("iv_processing_array", q -> q
            .at(26, 20)
            .side()
            .desc(d -> d
                    .line("如果你遇到了很慢的配方，可以试试这个")
                    .line()
                    .line("处理阵列可以安装{激光仓}来进行更多次数的{超频}，但处理阵列每次超频获得的{耗时减免}{较少}。你可以用{高能耗}作为代价来获得比大型机器更快的{处理速度}", GOLD, YELLOW, YELLOW, AQUA, RED, YELLOW))
            .task(Task.item(MultiBlockG.PROCESSING_ARRAY[GTValues.IV]))
            .dependsOn(IV_EMITTER_SENSOR));

    public static final Quest COLD_ICE_FREEZER = CHAPTER.quest("cold_ice_freezer", q -> q
            .at(24, 18)
            .side()
            .desc(d -> d.line("更高效的真空冷冻机，具有最高64并行，同时对你的能源供应也提出了更高的要求"))
            .task(Task.item(MultiBlockD.COLD_ICE_FREEZER))
            .dependsOn(IV_EMITTER_SENSOR));

    public static final Quest Q_02C29988 = CHAPTER.quest("q_02c29988", q -> q
            .at(2, -2)
            .side()
            .icon(TagPrefix.dustSmall, GTMaterials.RareEarth)
            .desc(d -> d
                    .line("你有两条路线来制作稀土，选择你想要的一条，它们都是8步产出稀土")
                    .line()
                    .line("产出稀土的路上有比较多的配方和副产，为了避免你头痛，我已经帮你整理好了")
                    .line()
                    .line("完成本任务后解锁{稀土}章节，查看详细流程", link(RareDustChapter.Q_75569B55))
                    .line()
                    .line("{如果想挑战自己，也可以选择不看}", GRAY.italic()))
            .task(Task.checkmark().title("稀土元素"))
            .dependsOn(NewEvChapter.IV_HULL));

    public static final Quest MONAZITE_DUST = CHAPTER.quest("monazite_dust", q -> q
            .at(4, -2)
            .side()
            .desc(d -> d
                    .line("相比左边中间多了两个增产配方，分别使用富铈混合物粉和钐精粉进行单步100%和50%的增产")
                    .line()
                    .line("一路做到饱和独居石稀土粉即可产出稀土"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Monazite))
            .dependsOn(Q_02C29988));

    public static final Quest RED_MUD_BUCKET = CHAPTER.quest("red_mud_bucket", q -> q
            .at(0, -2)
            .side()
            .icon(GTOMaterials.RedMud.getBucket())
            .subtitle("不推荐走这条路")
            .desc(d -> d
                    .line("这是什么邪道？！好吧，如果你只是急需少量的稀土产物（比如在制作石墨烯中作为催化剂长杆的一部分），而又不想马上处理漫长的稀土产线，可以试一试")
                    .line()
                    .line("但我仍然推荐你正常完成整个稀土产线，毕竟石头粉的产率肯定是远不如常规稀土矿物")
                    .line()
                    .line("{不知道你有没有发现赤泥甚至可以在HV不上天的情况下产出钛。二周目的时候你可以试一试...}", GRAY.italic()))
            .task(Task.item(GTOMaterials.RedMud.getBucket()).optional(), Task.checkmark())
            .optional()
            .dependsOn(NewEvChapter.IV_HULL));

    public static final Quest BLAZE_BLAST_FURNACE = CHAPTER.quest("blaze_blast_furnace", q -> q
            .at(4, 16)
            .side()
            .desc(d -> d.line("更高效的电力高炉，具有最高64并行，同时对你的能源供应也提出了更高的要求"))
            .task(Task.item(MultiBlockD.BLAZE_BLAST_FURNACE))
            .dependsOn(RTM_COIL, IV_EMITTER_SENSOR_COPY_1));

    public static final Quest DRILLING_CONTROL_CENTER = CHAPTER.quest("drilling_control_center", q -> q
            .at(4, 14)
            .side()
            .subtitle("流体钻机的好帮手")
            .task(Task.item(MultiBlockG.DRILLING_CONTROL_CENTER))
            .dependsOn(RTM_COIL, IV_EMITTER_SENSOR_COPY_2));

    public static final Quest ROCKET_MEGA_TURBINE = CHAPTER.quest("rocket_mega_turbine", q -> q
            .at(12, 16)
            .side()
            .task(Task.item(GeneratorMultiblock.ROCKET_MEGA_TURBINE))
            .optional()
            .dependsOn(NewEvChapter.ROCKET_LARGE_TURBINE, QUANTUM_COMPUTER));

    public static final Quest HASTELLOY_N_75_INGOT = CHAPTER.quest("hastelloy_n_75_ingot", q -> q
            .at(8, 14)
            .side()
            .desc(d -> d.line("该用合金冶炼炉了"))
            .task(Task.item(TagPrefix.ingot, GTOMaterials.HastelloyN75))
            .dependsOn(RTM_COIL));

    public static final Quest AUTO_CONFIGURATION_MAINTENANCE_HATCH = CHAPTER.quest("auto_configuration_maintenance_hatch", q -> q
            .at(28, 14)
            .side()
            .desc(d -> d
                    .line("根据游戏难度的不同有不同的调节范围，可以无条件地提升机器的速度和减慢发电机的燃料消耗速度！")
                    .line()
                    .line("{所有的环境维护仓也有它们的可配置版本}", GRAY.italic()))
            .task(Task.item(GTOMachines.AUTO_CONFIGURATION_MAINTENANCE_HATCH))
            .dependsOn(HPIC, IV_EMITTER_SENSOR_COPY_4));

    public static final Quest CHEMICAL_PLANT = CHAPTER.quest("chemical_plant", q -> q
            .at(26, 10)
            .side()
            .desc(d -> d.line("比一般的反应釜有线圈加成，还可以安装催化剂仓"))
            .task(Task.item(MultiBlockA.CHEMICAL_PLANT))
            .dependsOn(HPIC, IV_EMITTER_SENSOR_COPY_5));

    public static final Quest QUAKE_ORE_VEIN = CHAPTER.quest("quake_ore_vein", q -> q
            .at(6, 16)
            .side()
            .icon(TagPrefix.rawOre, GTMaterials.Naquadah)
            .title("硅岩矿脉")
            .desc(d -> d.line("你在木卫一上拿到的硅岩将会成为你接下来几个阶段最重要的材料之一"))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Naquadah), Task.item(TagPrefix.rawOre, GTMaterials.Plutonium239))
            .dependsOn(RTM_COIL, IV_EMITTER_SENSOR_COPY_3, NewHvChapter.EV_HULL, NewEvChapter.EV_ENERGY_HATCH, NewEvChapter.URANIUM_DUST));

    public static final Quest Q_79A84A1D = CHAPTER.quest("q_79a84a1d", q -> q
            .at(2, 8)
            .side()
            .icon(GCYMMachines.LARGE_AUTOCLAVE)
            .desc(d -> d
                    .line("IV时代解锁了大量的{多方块机器}，这些机器可以安装{并行控制仓}，{加速仓}和至多{8个}{能源仓}。将你的单方块机器升级为大批量加工巨兽吧！", YELLOW, GOLD, GOLD, GREEN, GOLD)
                    .line()
                    .line("详见{大批量加工}章节", link(MassiveManufacturingChapter.Q_68C24A1E)))
            .task(Task.checkmark().title("大批量加工"))
            .dependsOn(IV_MOTOR));

    public static final Quest INTEGRAL_FRAMEWORK_IV = CHAPTER.quest("integral_framework_iv", q -> q
            .at(10, 10)
            .side()
            .subtitle("升级你的大型机器#1")
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_IV))
            .dependsOn(RTM_COIL));

    public static final Quest ADVANCED_TESSERACT_GENERATOR = CHAPTER.quest("advanced_tesseract_generator", q -> q
            .at(26, 14)
            .side()
            .icon(GTOMachines.ADVANCED_TESSERACT_GENERATOR)
            .subtitle("轮椅自动化")
            .desc(d -> d
                    .line("你可以用这个来代替过去的存储总线方案")
                    .line()
                    .line("进阶版本可同时绑定{20}个目标，可以使用{坐标标签枪}批量绑定，{螺丝刀}点击可{切换模式}", AQUA, GOLD, GOLD, YELLOW)
                    .line()
                    .line("{顺序模式}：按照放入的信息卡的{顺序}作为优先级发送物品", GOLD, YELLOW)
                    .line()
                    .line("{轮询调度模式}：往放入的信息卡{均分}发送以样板为单位的物品", GOLD, YELLOW))
            .task(Task.item(GTOMachines.ADVANCED_TESSERACT_GENERATOR), Task.item(GTOItems.TESSERACT_TARGET_MARKER).optional())
            .optional()
            .dependsOn(HPIC, IV_EMITTER_SENSOR_COPY_6));

    public static final Quest CATALYST_HATCH = CHAPTER.quest("catalyst_hatch", q -> q
            .at(26, 8)
            .side()
            .desc(d -> d
                    .line("催化剂仓允许你重复利用催化剂，每次使用只会减少{1点}耐久而非直接消耗", AQUA)
                    .line()
                    .line("注意催化剂仓中只能放入由{催化剂基底}合成的催化剂", GOLD)
                    .line()
                    .line("{如果此类催化剂不放入}{催化剂仓}{或者}{催化剂总成}{，则每次合成会直接消耗}", YELLOW, YELLOW.bold(), YELLOW, YELLOW.bold(), YELLOW))
            .task(Task.item(GTOMachines.CATALYST_HATCH), Task.item(GTOMachines.ADVANCED_CATALYST_HATCH))
            .dependsOn(CHEMICAL_PLANT));

    public static final Quest INFINITE_WATER_HATCH = CHAPTER.quest("infinite_water_hatch", q -> q
            .at(6, 10)
            .side()
            .subtitle("真正意义上的无限水！")
            .task(Task.item(GTOMachines.INFINITE_WATER_HATCH))
            .dependsOn(IV_MOTOR));

    public static final Quest WIRELESS_DIMENSION_REPEATER = CHAPTER.quest("wireless_dimension_repeater", q -> q
            .at(6, 4)
            .side()
            .subtitle("终于不需要在外星放发电机了")
            .desc(d -> d
                    .line("跨维度传无线电")
                    .line()
                    .line("注意搭建时需要在{每个}需要无线电的维度都搭建一个（除了无线能源塔所在的维度），能够使用的{最高电压}取决于其上安装的{整体框架}", YELLOW, YELLOW, GOLD)
                    .line()
                    .line("{如果你需要一些哭泣的黑曜石，可以去看看黑曜石的tooltips}", GRAY.italic()))
            .task(Task.item(MultiBlockG.WIRELESS_DIMENSION_REPEATER))
            .dependsOn(IV_ASSEMBLER));

    public static final Quest IV_256A_LASER_TARGET_HATCH = CHAPTER.quest("iv_256a_laser_target_hatch", q -> q
            .at(24, 22)
            .side()
            .icon(GTMachines.LASER_INPUT_HATCH_256[GTValues.IV])
            .desc(d -> d
                    .line("从IV开始可以使用{激光能源仓}来为机器提供更高的功率", GOLD)
                    .line()
                    .line("同样有其无线版本"))
            .task(Task.item(GTMachines.LASER_INPUT_HATCH_256[GTValues.IV]), Task.item(GTOMachines.WIRELESS_INPUT_HATCH_256[GTValues.IV]))
            .dependsOn(IV_EMITTER_SENSOR, IV_ENERGY_HATCH_COPY_1));

    public static final Quest ASSEMBLY_LINE_EASY = CHAPTER.quest("assembly_line_easy", q -> q
            .at(24, 14)
            .side()
            .icon(GTMultiMachines.ASSEMBLY_LINE)
            .title("装配线{（简单）}", GREEN)
            .subtitle("简单模式下，装配线无需有序输入！")
            .task(Task.checkmark())
            .optional().hideUntilDepsVisible()
            .dependsOn(UtilChapter.Q_056EC4E6, HPIC, IV_EMITTER_SENSOR_COPY_7));

    public static final Quest ASSEMBLY_LINE_EXPERT = CHAPTER.quest("assembly_line_expert", q -> q
            .at(28, 12)
            .side()
            .icon(GTMultiMachines.ASSEMBLY_LINE)
            .title("装配线{（专家）}", LIGHT_PURPLE)
            .subtitle("专家模式下，装配线流体也需要有序输入！")
            .task(Task.checkmark())
            .optional().hideUntilDepsVisible()
            .dependsOn(HPIC, IV_EMITTER_SENSOR_COPY_8, UtilChapter.Q_1FB1EC5B));

    public static final Quest OSTRUM_VAJRA = CHAPTER.quest("ostrum_vajra", q -> q
            .at(8, 10)
            .side()
            .task(Task.item(ToolHelper.get(GTOToolType.VAJRA_EV, GTOMaterials.Ostrum).getItem()))
            .dependsOn(RTM_COIL, NewHvChapter.EV_HULL, NewEvChapter.EV_ENERGY_HATCH, NewEvChapter.URANIUM_DUST, NETHER_STAR_COPY_1));

    public static final Quest Q_0BFFC724 = CHAPTER.quest("q_0bffc724", q -> q
            .at(14, 20)
            .side()
            .icon(SpaceMultiblock.LARGE_EXPANDABLE_SPACE_STATION_CORE_MODULE)
            .desc(d -> d
                    .line("工业空间站在{UV}阶段之前都不是必需品，不过我仍建议你尽早规划建造", UV)
                    .line()
                    .line("这里简单列出了你需要的新原料和新机器，但你还需要编写大量的样板{（真的很多！）}", ITALIC)
                    .line()
                    .line("别忘了去{穹宇居所}章节看看你可以制造的仓室", link(SpaceStationChapter.WE_NEED_A_HOUSE_IN_SPACE)))
            .task(Task.checkmark().title("关于工业空间站"))
            .dependsOn(QUANTUM_COMPUTER));

    public static final Quest FIBER_EXTRUDER = CHAPTER.quest("fiber_extruder", q -> q
            .at(14, 22)
            .side()
            .subtitle("空间站定制材料丝")
            .desc(d -> d.line("工业空间站的很多材料需要它"))
            .task(Task.item(MultiBlockH.FIBER_EXTRUDER))
            .dependsOn(Q_0BFFC724));

    public static final Quest RHENIUM_DUST = CHAPTER.quest("rhenium_dust", q -> q
            .at(12, 20)
            .side()
            .desc(d -> d.line("从辉钼矿粉中来"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Rhenium))
            .dependsOn(Q_0BFFC724));

    public static final Quest THERMO_PRESS = CHAPTER.quest("thermo_press", q -> q
            .at(16, 20)
            .side()
            .subtitle("空间站定制材料块")
            .desc(d -> d.line("工业空间站的很多材料需要它"))
            .task(Task.item(MultiBlockH.THERMO_PRESS))
            .dependsOn(Q_0BFFC724));

    public static final Quest HIGH_PRESSURE_STEAM_INPUT_HATCH = CHAPTER.quest("high_pressure_steam_input_hatch", q -> q
            .at(18, 20)
            .side()
            .subtitle("蒸汽机器死忠粉")
            .desc(d -> d.line("继续提升蒸汽机器的配方电压一级，解锁更多次数的超频"))
            .task(Task.item(GTOMachines.HIGH_PRESSURE_STEAM_INPUT_HATCH))
            .optional()
            .dependsOn(THERMO_PRESS));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewEvChapter.IV_HULL, 0, 0))
                .mainLine(NewEvChapter.IV_HULL, DIGESTION_TANK, CRACKER, CITRIC_ACID, LAMINATED_GLASS, EXTREME_CIRCUIT_BOARD, DISSOLVING_TANK, PHOSPHORIC_ACID, YTTRIUM_NITRATE, GLUCOSE_IRON_SOLUTION, TUNGSTEN_CABLE, IV_ASSEMBLER, ELECTROPLATING_BATH, RHODIUM_NITRATE, RHODIUM_PLATED_PALLADIUM, RTM_ALLOY, RTM_COIL, IV_VOLTAGE_COIL, QBIT_WAFER, QBIT_CPU, IV_ENERGY_HATCH, QUANTUM_PROCESSOR, QUANTUM_ASSEMBLY, QUANTUM_COMPUTER, LUV_HULL));
    }
}
