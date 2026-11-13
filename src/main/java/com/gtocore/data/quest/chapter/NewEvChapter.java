package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.item.tool.GTOToolType;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewEvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_ev", c -> c
            .title("7. 超高压时代(EV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.EV]));

    public static final Quest NEODYMIUM = CHAPTER.quest("neodymium", q -> q
            .at(10, -6)
            .title("钕")
            .task(Task.item(TagPrefix.dust, GTMaterials.Neodymium, 11))
            .scaledReward(TagPrefix.dust, GTMaterials.Neodymium, 3)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest NEODYMIUM_MAGNETIC_ROD = CHAPTER.quest("neodymium_magnetic_rod", q -> q
            .at(10, -4)
            .title("钕磁棒")
            .icon(TagPrefix.rod, GTMaterials.NeodymiumMagnetic)
            .task(Task.item(GTMachines.POLARIZER[GTValues.HV]), Task.item(TagPrefix.rod, GTMaterials.NeodymiumMagnetic, 21))
            .scaledReward(TagPrefix.rod, GTMaterials.Neodymium, 5)
            .dependsOn(NEODYMIUM));

    public static final Quest PDMS = CHAPTER.quest("polydimethylsiloxane", q -> q
            .at(14, -6)
            .title("聚二甲基硅氧烷")
            .task(Task.item(TagPrefix.dust, GTMaterials.Polydimethylsiloxane, 36))
            .scaledReward(TagPrefix.dust, GTMaterials.Silicon, 3)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest SILICONE_RUBBER = CHAPTER.quest("silicone_rubber", q -> q
            .at(16, -6)
            .title("硅橡胶")
            .desc(d -> d
                    .line("EV时代开始你开始需要更高级的橡胶来制作讨厌的{传送带}和{电动泵}，以及IV级别的{线缆}", GOLD, GOLD, GOLD)
                    .line()
                    .line("给予它足够的重视，建立一个大量持续产出的产线！"))
            .icon(GTMaterials.SiliconeRubber.getBucket())
            .task(Task.item(GTMaterials.SiliconeRubber.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Polydimethylsiloxane, 9)
            .dependsOn(PDMS));

    public static final Quest EXTRUDER_MV = CHAPTER.quest("extruder_mv", q -> q
            .at(16, -4)
            .title("MV 压模器")
            .subtitle("它的春天尚未到来")
            .desc(d -> d
                    .line("由于电压等级不够高，MV压模机依然不能处理该电压级别的金属{铝}", GOLD)
                    .line()
                    .line("实用性大不如{液化炉}和{流体固化器}", GOLD, GOLD)
                    .line()
                    .line("{下个阶段也许会迎来它的春天？}", YELLOW))
            .task(Task.item(GTMachines.EXTRUDER[GTValues.MV]))
            .scaledReward(TagPrefix.ring, GTMaterials.SiliconeRubber, 1)
            .dependsOn(SILICONE_RUBBER));

    public static final Quest SILICONE_RUBBER_RING = CHAPTER.quest("silicone_rubber_ring", q -> q
            .at(14, -4)
            .title("硅橡胶环")
            .icon(TagPrefix.ring, GTMaterials.SiliconeRubber)
            .task(Task.item(TagPrefix.ring, GTMaterials.SiliconeRubber, 2))
            .scaledReward(TagPrefix.ring, GTMaterials.SiliconeRubber, 1)
            .dependsOn(EXTRUDER_MV));

    public static final Quest PVC_FOIL_AND_TEMPERED_GLASS = CHAPTER.quest("pvc_foil_and_tempered_glass", q -> q
            .at(12, -6)
            .title("聚氯乙烯箔与钢化玻璃")
            .icon(TagPrefix.foil, GTMaterials.PolyvinylChloride)
            .task(Task.item(TagPrefix.foil, GTMaterials.PolyvinylChloride, 56), Task.item(GTBlocks.CASING_TEMPERED_GLASS, 5))
            .scaledReward(TagPrefix.foil, GTMaterials.PolyvinylChloride, 8)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest EV_COMPONENTS = CHAPTER.quest("ev_components", q -> q
            .at(12, -4)
            .title("EV 部件")
            .icon(GTItems.ELECTRIC_MOTOR_EV)
            .task(
                    Task.item(GTItems.ELECTRIC_MOTOR_EV, 2),
                    Task.item(GTItems.ELECTRIC_PUMP_EV),
                    Task.item(GTItems.CONVEYOR_MODULE_EV, 5),
                    Task.item(GTItems.ROBOT_ARM_EV, 2))
            .scaledReward(GTItems.ELECTRIC_MOTOR_EV, 1)
            .dependsOn(NEODYMIUM_MAGNETIC_ROD, SILICONE_RUBBER_RING, PVC_FOIL_AND_TEMPERED_GLASS));

    public static final Quest EV_MIXER = CHAPTER.quest("ev_mixer", q -> q
            .at(12, -2)
            .title("EV 搅拌机")
            .task(Task.item(GTMachines.MIXER[GTValues.EV]))
            .scaledReward(TagPrefix.plate, GTMaterials.Titanium, 4)
            .dependsOn(EV_COMPONENTS));

    public static final QuestLink EV_MIXER_COPY_1 = CHAPTER.copy(EV_MIXER, 16, -2);

    public static final QuestLink EV_MIXER_COPY_2 = CHAPTER.copy(EV_MIXER, 28, 16);

    public static final Quest DAMASCUS_STEEL = CHAPTER.quest("damascus_steel", q -> q
            .at(28, 12)
            .title("大马士革钢")
            .icon(TagPrefix.ingot, GTMaterials.DamascusSteel)
            .task(Task.item(TagPrefix.ingot, GTMaterials.DamascusSteel, 48))
            .scaledReward(TagPrefix.ingot, GTMaterials.DamascusSteel, 12)
            .dependsOn(EV_MIXER_COPY_2));

    public static final Quest ROLLING_MV = CHAPTER.quest("rolling_mv", q -> q
            .at(28, 8)
            .title("MV 辊轧机")
            .desc(d -> d
                    .line("比低压机器更高效的机器")
                    .line()
                    .line("可以辊轧{双层板}和{致密板}", GOLD, GOLD))
            .task(Task.item(GTOMachines.ROLLING[GTValues.MV]))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Titanium, 3)
            .dependsOn(DAMASCUS_STEEL));

    public static final Quest DAMASCUS_STEEL_DOUBLE_PLATE = CHAPTER.quest("damascus_steel_double_plate", q -> q
            .at(28, 2)
            .title("大马士革钢双层板")
            .icon(TagPrefix.plateDouble, GTMaterials.DamascusSteel)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.DamascusSteel, 24))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Titanium, 3)
            .dependsOn(ROLLING_MV));

    public static final Quest HEAVY_DUTY_PLATE_2 = CHAPTER.quest("heavy_duty_plate_2", q -> q
            .at(26, 0)
            .title("二阶重型板")
            .desc(d -> d.line("注意EMI页面中化学浸洗机右下的维度标识！这意味着你需要去到特定维度才能制作配方"))
            .icon(GTOItems.HEAVY_DUTY_PLATE_2)
            .task(Task.item(GTMachines.FORMING_PRESS[GTValues.HV]), Task.item(GTOItems.HEAVY_DUTY_PLATE_2, 12))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 1)
            .dependsOn(DAMASCUS_STEEL_DOUBLE_PLATE));

    public static final Quest TIER_2_ROCKET = CHAPTER.quest("tier_2_rocket", q -> q
            .at(24, -2)
            .title("T2 火箭")
            .desc(d -> d
                    .line("没有什么能阻挡你再次飞向星辰")
                    .line()
                    .line("来一点钛，再来一点\"润滑油泡钢\"...")
                    .line()
                    .line("别忘了通过卫星控制中心解锁{火星}再上天", LIGHT_PURPLE))
            .task(Task.item(ModItems.TIER_2_ROCKET.get()))
            .scaledReward(GTItems.WORKSTATION_EV, 2)
            .dependsOn(HEAVY_DUTY_PLATE_2));

    public static final Quest PYROLYSE_OVEN = CHAPTER.quest("pyrolyse_oven", q -> q
            .at(12, 0)
            .milestone()
            .title("热解炉")
            .desc(d -> d.line("生产生物资源"))
            .task(Task.item(GTMultiMachines.PYROLYSE_OVEN))
            .scaledReward(Items.COAL, 12)
            .dependsOn(EV_MIXER));

    public static final Quest COAL_TAR = CHAPTER.quest("coal_tar", q -> q
            .at(18, -6)
            .title("煤焦油")
            .icon(GTMaterials.CoalTar.getBucket())
            .task(Task.item(GTMaterials.CoalTar.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.COAL, 16)
            .dependsOn(PYROLYSE_OVEN));

    public static final Quest KEROSENE = CHAPTER.quest("kerosene", q -> q
            .at(20, -6)
            .title("煤油")
            .icon(GTOMaterials.Kerosene.getBucket())
            .task(Task.item(GTOMaterials.Kerosene.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Coke, 8)
            .dependsOn(COAL_TAR));

    public static final Quest RP_1 = CHAPTER.quest("rp_1", q -> q
            .at(22, -6)
            .title("RP-1 混合燃料")
            .icon(GTOMaterials.Rp1.getBucket())
            .task(Task.item(GTOMaterials.Rp1.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Kerosene.getBucket(), 2)
            .dependsOn(KEROSENE));

    public static final Quest GAS_COMPRESSOR = CHAPTER.quest("gas_compressor", q -> q
            .at(16, 0)
            .milestone()
            .title("气体压缩机")
            .desc(d -> d.line("你目前需要拿它制作{高压氮气}", GOLD))
            .task(Task.item(MultiBlockG.GAS_COMPRESSOR))
            .scaledReward(GTItems.FLUID_REGULATOR_LV, 1)
            .dependsOn(EV_MIXER_COPY_1));

    public static final Quest HIGH_PRESSURE_OXYGEN = CHAPTER.quest("high_pressure_oxygen", q -> q
            .at(18, -2)
            .title("高压氧")
            .icon(GTOMaterials.HighPressureOxygen.getBucket())
            .task(Task.item(GTOMaterials.HighPressureOxygen.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Oxygen.getBucket(), 4)
            .dependsOn(GAS_COMPRESSOR));

    public static final Quest LIQUID_OXYGEN = CHAPTER.quest("liquid_oxygen", q -> q
            .at(20, -2)
            .title("液氧")
            .icon(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID).getBucket())
            .task(Task.item(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.HighPressureOxygen.getBucket(), 2)
            .dependsOn(HIGH_PRESSURE_OXYGEN));

    public static final Quest ROCKET_FUEL_RP_1 = CHAPTER.quest("rocket_fuel_rp_1", q -> q
            .at(22, -4)
            .title("火箭燃料 RP-1")
            .icon(GTOMaterials.RocketFuelRp1.getBucket())
            .task(Task.item(GTOMaterials.RocketFuelRp1.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelRp1.getBucket(), 2)
            .dependsOn(RP_1, LIQUID_OXYGEN));

    public static final Quest UNLOCK_MARS = CHAPTER.quest("unlock_mars", q -> q
            .at(22, -2)
            .title("解锁火星")
            .icon(ModItems.MARS_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelRp1.getBucket(), 1)
            .dependsOn(ROCKET_FUEL_RP_1, TIER_2_ROCKET));

    public static final Quest MARS = CHAPTER.quest("mars", q -> q
            .at(20, 0)
            .milestone()
            .title("登陆火星")
            .desc(d -> d
                    .line("火星是需要乘载T2火箭到达的星球，简称T2星球")
                    .line()
                    .line("从火星上获得大量白钨矿，开始你的第一个产线挑战"))
            .icon(ModItems.MARS_GLOBE.get())
            .task(Task.dimension(Dimension.MARS).title("造访火星").icon(ModItems.MARS_GLOBE.get()))
            .scaledReward(TagPrefix.plate, GTMaterials.Titanium, 4)
            .dependsOn(UNLOCK_MARS));

    public static final Quest NITRIC_OXIDE = CHAPTER.quest("nitric_oxide", q -> q
            .at(22, 2)
            .title("一氧化氮")
            .icon(GTMaterials.NitricOxide.getBucket())
            .task(Task.item(GTMaterials.NitricOxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 1)
            .dependsOn(MARS));

    public static final Quest NITROGEN_DIOXIDE = CHAPTER.quest("nitrogen_dioxide", q -> q
            .at(22, 4)
            .title("二氧化氮")
            .icon(GTMaterials.NitrogenDioxide.getBucket())
            .task(Task.item(GTMaterials.NitrogenDioxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Oxygen.getBucket(), 2)
            .dependsOn(NITRIC_OXIDE));

    public static final Quest NITRIC_ACID = CHAPTER.quest("nitric_acid", q -> q
            .at(24, 6)
            .title("硝酸")
            .icon(GTMaterials.NitricAcid.getBucket())
            .task(Task.item(GTMaterials.NitricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NitrogenDioxide.getBucket(), 2)
            .dependsOn(NITROGEN_DIOXIDE));

    public static final Quest SHELDONITE = CHAPTER.quest("sheldonite", q -> q
            .at(22, 0)
            .title("谢尔顿矿")
            .icon(TagPrefix.rawOre, GTMaterials.Cooperite)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Cooperite, 10))
            .scaledReward(TagPrefix.rawOre, GTMaterials.Cooperite, 3)
            .dependsOn(MARS));

    public static final Quest FORGE_HAMMER_LV = CHAPTER.quest("forge_hammer_lv", q -> q
            .at(24, 2)
            .title("LV 锻造锤")
            .task(Task.item(GTMachines.FORGE_HAMMER[GTValues.LV]))
            .scaledReward(TagPrefix.crushed, GTMaterials.Cooperite, 3)
            .dependsOn(SHELDONITE));

    public static final Quest CRUSHED_SHELDONITE = CHAPTER.quest("crushed_sheldonite", q -> q
            .at(26, 2)
            .title("粉碎谢尔顿矿")
            .icon(TagPrefix.crushed, GTMaterials.Cooperite)
            .task(Task.item(TagPrefix.crushed, GTMaterials.Cooperite, 10))
            .scaledReward(TagPrefix.crushed, GTMaterials.Cooperite, 3)
            .dependsOn(FORGE_HAMMER_LV));

    public static final Quest ORE_WASHER_LV = CHAPTER.quest("ore_washer_lv", q -> q
            .at(26, 4)
            .title("LV 洗矿厂")
            .task(Task.item(GTMachines.ORE_WASHER[GTValues.LV]))
            .scaledReward(TagPrefix.crushedPurified, GTMaterials.Cooperite, 3)
            .dependsOn(CRUSHED_SHELDONITE));

    public static final Quest PURIFIED_SHELDONITE = CHAPTER.quest("purified_sheldonite", q -> q
            .at(26, 6)
            .title("洗净谢尔顿矿")
            .icon(TagPrefix.crushedPurified, GTMaterials.Cooperite)
            .task(Task.item(TagPrefix.crushedPurified, GTMaterials.Cooperite, 10))
            .scaledReward(TagPrefix.crushedPurified, GTMaterials.Cooperite, 3)
            .dependsOn(ORE_WASHER_LV));

    public static final Quest PLATINUM_GROUP_SLUDGE = CHAPTER.quest("platinum_group_sludge", q -> q
            .at(24, 8)
            .title("铂系矿泥")
            .desc(d -> d
                    .line("你需要投入你所有的铂金属来源：{斑铜矿、黝铜矿、辉铜矿、谢尔顿矿}", GOLD)
                    .line()
                    .line("你无需一次性完成整条铂处产线，可以先进行你马上需要用到的{钌}与{铑}的产出。但我仍建议一定要搭建一条独立完整的铂处产线", GOLD, GOLD)
                    .line()
                    .line("希望你享受铂族处理！"))
            .icon(TagPrefix.dust, GTMaterials.PlatinumGroupSludge)
            .task(Task.item(TagPrefix.dust, GTMaterials.PlatinumGroupSludge, 80))
            .scaledReward(TagPrefix.dust, GTMaterials.PlatinumGroupSludge, 20)
            .dependsOn(NITRIC_ACID, PURIFIED_SHELDONITE));

    public static final QuestLink PLATINUM_GROUP_SLUDGE_COPY_1 = CHAPTER.copy(PLATINUM_GROUP_SLUDGE, 16, 16);

    public static final Quest AQUA_REGIA = CHAPTER.quest("aqua_regia", q -> q
            .at(22, 6)
            .title("王水")
            .icon(GTMaterials.AquaRegia.getBucket())
            .task(Task.item(GTMaterials.AquaRegia.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 2)
            .dependsOn(NITRIC_ACID));

    public static final Quest PLATINUM_RAW = CHAPTER.quest("platinum_raw", q -> q
            .at(20, 8)
            .milestone()
            .title("铂原料")
            .icon(TagPrefix.dust, GTMaterials.PlatinumRaw)
            .task(Task.item(GTMachines.CENTRIFUGE[GTValues.HV]), Task.item(TagPrefix.dust, GTMaterials.PlatinumRaw, 24))
            .scaledReward(TagPrefix.dust, GTMaterials.PlatinumRaw, 6)
            .dependsOn(PLATINUM_GROUP_SLUDGE, AQUA_REGIA));

    public static final QuestLink PLATINUM_RAW_COPY_1 = CHAPTER.copy(PLATINUM_RAW, 14, 10);

    public static final Quest CALCIUM = CHAPTER.quest("calcium", q -> q
            .at(14, 14)
            .title("钙")
            .task(Task.item(TagPrefix.dust, GTMaterials.Calcium, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Apatite, 9)
            .dependsOn(PLATINUM_GROUP_SLUDGE_COPY_1));

    public static final Quest PLATINUM = CHAPTER.quest("platinum", q -> q
            .at(14, 12)
            .title("铂")
            .desc(d -> d.line("无需经过复杂的矿泥处理也可以获取"))
            .icon(TagPrefix.ingot, GTMaterials.Platinum)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Platinum, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Platinum, 2)
            .dependsOn(PLATINUM_RAW_COPY_1, CALCIUM));

    public static final Quest EV_ELECTROLYZER = CHAPTER.quest("ev_electrolyzer", q -> q
            .at(16, 14)
            .title("EV 电解机")
            .task(Task.item(GTMachines.ELECTROLYZER[GTValues.EV]))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Platinum, 2)
            .dependsOn(PLATINUM));

    public static final QuestLink EV_ELECTROLYZER_COPY_1 = CHAPTER.copy(EV_ELECTROLYZER, -2, 2);

    public static final Quest SCHEELITE = CHAPTER.quest("scheelite", q -> q
            .at(12, 10)
            .title("白钨矿")
            .icon(TagPrefix.rawOre, GTMaterials.Scheelite)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Scheelite, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.Scheelite, 12)
            .dependsOn(PLATINUM));

    public static final Quest TUNGSTIC_ACID = CHAPTER.quest("tungstic_acid", q -> q
            .at(10, 10)
            .title("钨酸")
            .icon(TagPrefix.dust, GTMaterials.TungsticAcid)
            .task(Task.item(GTMachines.CHEMICAL_BATH[GTValues.EV]), Task.item(TagPrefix.dust, GTMaterials.TungsticAcid, 56))
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 4)
            .dependsOn(SCHEELITE));

    public static final QuestLink TUNGSTIC_ACID_COPY_1 = CHAPTER.copy(TUNGSTIC_ACID, 18, 14);

    public static final Quest TUNGSTEN = CHAPTER.quest("tungsten", q -> q
            .at(18, 12)
            .title("钨")
            .subtitle("EV阶段的第一个生产挑战")
            .desc(d -> d
                    .line("{钨粉}是EV阶段最重要的单质，你需要大量生产", GOLD)
                    .line()
                    .line("随即解锁的极其有用的三种合金都需要钨元素"))
            .icon(TagPrefix.dust, GTMaterials.Tungsten)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tungsten, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.TungsticAcid, 14)
            .dependsOn(EV_ELECTROLYZER, TUNGSTIC_ACID_COPY_1));

    public static final QuestLink TUNGSTEN_COPY_1 = CHAPTER.copy(TUNGSTEN, 8, 4);

    public static final Quest NICHROME_DUST = CHAPTER.quest("nichrome_dust", q -> q
            .at(18, 16)
            .title("镍铬粉")
            .icon(TagPrefix.dust, GTMaterials.Nichrome)
            .task(Task.item(TagPrefix.dust, GTMaterials.Nichrome, 128))
            .scaledReward(TagPrefix.dust, GTMaterials.Nickel, 16)
            .dependsOn(EV_ELECTROLYZER));

    public static final QuestLink NICHROME_DUST_COPY_1 = CHAPTER.copy(NICHROME_DUST, 22, 10);

    public static final Quest NICHROME_INGOT = CHAPTER.quest("nichrome_ingot", q -> q
            .at(20, 10)
            .title("镍铬锭")
            .icon(TagPrefix.ingot, GTMaterials.Nichrome)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Nichrome, 128))
            .scaledReward(TagPrefix.dust, GTMaterials.Nichrome, 32)
            .dependsOn(NICHROME_DUST_COPY_1));

    public static final Quest NICHROME_COIL = CHAPTER.quest("nichrome_coil", q -> q
            .at(18, 10)
            .title("镍铬线圈")
            .subtitle("是时候升级你的{电力高炉}了", GOLD)
            .desc(d -> d
                    .line("{镍铬合金线圈}的基础热容刚好能使高炉在烧铝和不锈钢时获得一次额外的{无损超频}", GOLD, LIGHT_PURPLE)
                    .line()
                    .line("除了电力高炉能获得其增益，许多带有{线圈结构方块}的多方块机器都能一步步升级线圈获得更高的{生产力}", YELLOW, YELLOW))
            .icon(GTBlocks.COIL_NICHROME)
            .task(Task.item(GTBlocks.COIL_NICHROME, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.StainlessSteel, 32)
            .dependsOn(NICHROME_INGOT));

    public static final Quest TUNGSTEN_STEEL_DUST = CHAPTER.quest("tungsten_steel_dust", q -> q
            .at(16, 10)
            .title("钨钢粉")
            .icon(TagPrefix.dust, GTMaterials.TungstenSteel)
            .task(Task.item(TagPrefix.dust, GTMaterials.TungstenSteel, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Steel, 4)
            .dependsOn(TUNGSTEN));

    public static final Quest TUNGSTEN_STEEL = CHAPTER.quest("tungsten_steel", q -> q
            .at(16, 8)
            .milestone()
            .title("钨钢锭")
            .icon(TagPrefix.ingot, GTMaterials.TungstenSteel)
            .task(Task.item(TagPrefix.ingot, GTMaterials.TungstenSteel, 16))
            .scaledReward(TagPrefix.ingot, GTMaterials.TungstenSteel, 4)
            .dependsOn(TUNGSTEN_STEEL_DUST, NICHROME_COIL));

    public static final QuestLink TUNGSTEN_STEEL_COPY_1 = CHAPTER.copy(TUNGSTEN_STEEL, -2, 18);

    public static final Quest BROWN_DYE = CHAPTER.quest("brown_dye", q -> q
            .at(-4, 18)
            .title("棕色染料")
            .icon(GTMaterials.DyeBrown.getBucket())
            .task(Task.item(GTMaterials.DyeBrown.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.BROWN_DYE, 8)
            .dependsOn(TUNGSTEN_STEEL));

    public static final Quest BROWN_LENS = CHAPTER.quest("brown_lens", q -> q
            .at(-4, 16)
            .title("棕色透镜")
            .icon(GTItems.GLASS_LENSES.get(DyeColor.BROWN))
            .task(Task.item(GTItems.GLASS_LENSES.get(DyeColor.BROWN)))
            .scaledReward(TagPrefix.dust, GTMaterials.Bentonite, 4)
            .dependsOn(BROWN_DYE));

    public static final Quest PIC_WAFER = CHAPTER.quest("pic_wafer", q -> q
            .at(-2, 16)
            .title("功率集成电路晶圆")
            .icon(GTItems.POWER_INTEGRATED_CIRCUIT_WAFER)
            .task(Task.item(GTItems.POWER_INTEGRATED_CIRCUIT_WAFER))
            .scaledReward(GTItems.PHOSPHORUS_WAFER, 1)
            .dependsOn(BROWN_LENS));

    public static final Quest PIC = CHAPTER.quest("pic", q -> q
            .at(0, 16)
            .title("功率集成电路")
            .icon(GTItems.POWER_INTEGRATED_CIRCUIT)
            .task(Task.item(GTMachines.CUTTER[GTValues.EV]), Task.item(GTItems.POWER_INTEGRATED_CIRCUIT, 4))
            .scaledReward(GTItems.POWER_INTEGRATED_CIRCUIT, 1)
            .dependsOn(PIC_WAFER));

    public static final Quest EV_VOLTAGE_COIL = CHAPTER.quest("ev_voltage_coil", q -> q
            .at(-2, 14)
            .title("EV 电压线圈")
            .icon(GTItems.VOLTAGE_COIL_EV)
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.EV]), Task.item(GTItems.VOLTAGE_COIL_EV, 2))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Platinum, 8)
            .dependsOn(PIC_WAFER));

    public static final Quest EV_ENERGY_HATCH = CHAPTER.quest("ev_energy_hatch", q -> q
            .at(0, 14)
            .milestone()
            .title("EV 能源仓")
            .desc(d -> d
                    .line("EV能源仓！")
                    .line()
                    .line("给你的机器来一次更新换代！"))
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.EV], 2))
            .scaledReward(TagPrefix.cableGtDouble, GTMaterials.Aluminium, 4)
            .dependsOn(PIC, EV_VOLTAGE_COIL));

    public static final QuestLink EV_ENERGY_HATCH_COPY_1 = CHAPTER.copy(EV_ENERGY_HATCH, -6, -4);

    public static final Quest IV_CASING = CHAPTER.quest("iv_casing", q -> q
            .at(8, 16)
            .title("IV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_IV))
            .scaledReward(TagPrefix.plate, GTMaterials.TungstenSteel, 1)
            .dependsOn(EV_ENERGY_HATCH));

    public static final Quest PLATINUM_CABLE = CHAPTER.quest("platinum_cable", q -> q
            .at(8, 12)
            .title("铂线缆")
            .icon(TagPrefix.cableGtSingle, GTMaterials.Platinum)
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Platinum, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Platinum, 1)
            .dependsOn(EV_ENERGY_HATCH));

    public static final Quest IV_HULL = CHAPTER.quest("iv_hull", q -> q
            .at(8, 14)
            .milestone()
            .title("IV 机器外壳")
            .subtitle("{你需要天马行空般的想象力}", BLUE)
            .desc(d -> d
                    .line("在{IV马达}之前，你需要面对一条超级长的{稀土产线}。与铂处类似，我同样建议你搭建一条独立产线。", GOLD, YELLOW)
                    .line()
                    .line("在获取IV马达之后，将基地里的通用工厂逐步替换成真正的{多方块机器}是一个不错的选择。", YELLOW)
                    .line()
                    .line("这个阶段的机器需要大量的{钨钢}来制作，因此开始发展该阶段前确保你有持续的{钨}产出。", GOLD, GOLD)
                    .line()
                    .line("好消息是，不是所有东西都需要IV马达，在取得{EV能源仓}后你就可以制作{IV并行控制仓}和{寒冰冷冻机}等机器！", GREEN, GOLD, GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.IV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.IV], 1))
            .dependsOn(EV_ENERGY_HATCH, IV_CASING, PLATINUM_CABLE));

    public static final Quest LARGE_CHEMICAL_REACTOR = CHAPTER.quest("large_chemical_reactor", q -> q
            .at(8, 6)
            .title("大型化学反应釜")
            .subtitle("没有人比我更懂化学")
            .desc(d -> d
                    .line("有条件的{无损超频}，运行低压配方速度更快", YELLOW)
                    .line()
                    .line("{大型化学反应釜}能够执行许多特殊配方，检查EMI配方页查看它对你有什么帮助", GOLD))
            .task(
                    Task.item(GTMultiMachines.LARGE_CHEMICAL_REACTOR),
                    Task.item(GTBlocks.CASING_PTFE_INERT, 10),
                    Task.item(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE))
            .scaledReward(GTBlocks.CASING_PTFE_INERT, 4)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final QuestLink LARGE_CHEMICAL_REACTOR_COPY_1 = CHAPTER.copy(LARGE_CHEMICAL_REACTOR, -4, 6);

    public static final Quest TUNGSTEN_CARBIDE = CHAPTER.quest("tungsten_carbide", q -> q
            .at(10, 4)
            .title("碳化钨")
            .icon(TagPrefix.ingot, GTMaterials.TungstenCarbide)
            .task(Task.item(TagPrefix.ingot, GTMaterials.TungstenCarbide, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.TungstenCarbide, 2)
            .dependsOn(TUNGSTEN_COPY_1));

    public static final Quest CHEMICAL_VAPOR_DEPOSITION = CHAPTER.quest("chemical_vapor_deposition", q -> q
            .at(10, 6)
            .title("化学气相沉积")
            .desc(d -> d.line("化学气相沉积(Chemical Vapor Deposition, CVD)是一种先进的材料制备技术，在今后你需要它来制作大量功能各异的新型材料"))
            .task(Task.item(MultiBlockC.CHEMICAL_VAPOR_DEPOSITION), Task.item(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE, 13))
            .scaledReward(GTBlocks.CASING_PTFE_INERT, 8)
            .dependsOn(TUNGSTEN_CARBIDE, LARGE_CHEMICAL_REACTOR));

    public static final Quest MAINFRAME = CHAPTER.quest("mainframe", q -> q
            .at(6, -2)
            .title("微型处理器主机")
            .subtitle("第一个IV级电路")
            .desc(d -> d
                    .line("HV最贵的单方块机器")
                    .line()
                    .line("IV级电路，目前达到的最高等级，你还暂时用不上它")
                    .line()
                    .line("你应该第一时间想到你需要它来做出下一级别的{电路组装机}", GOLD)
                    .line()
                    .line("{你应该已经发现了，从微型处理器开始，每个序列均存在四种电路，最高级的电路一般都只是你开启下一阶段大门的}{钥匙}", GRAY.italic(), YELLOW.italic()))
            .icon(GTItems.MAINFRAME_IV)
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.HV]), Task.item(GTItems.MAINFRAME_IV, 4))
            .scaledReward(GTItems.WORKSTATION_EV, 2)
            .dependsOn(NewHvChapter.WORKSTATION));

    public static final QuestLink MAINFRAME_COPY_1 = CHAPTER.copy(MAINFRAME, 4, 16);

    public static final Quest ACETIC_ACID = CHAPTER.quest("acetic_acid", q -> q
            .at(-8, 10)
            .title("醋酸")
            .icon(GTMaterials.AceticAcid.getBucket())
            .task(Task.item(GTMaterials.AceticAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ethylene.getBucket(), 2)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest CALCIUM_ACETATE = CHAPTER.quest("dissolved_calcium_acetate", q -> q
            .at(-8, 8)
            .title("醋酸钙溶液")
            .icon(GTMaterials.DissolvedCalciumAcetate.getBucket())
            .task(Task.item(GTMaterials.DissolvedCalciumAcetate.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Calcite, 10)
            .dependsOn(ACETIC_ACID));

    public static final Quest FLUID_HEATER_LV = CHAPTER.quest("fluid_heater_lv", q -> q
            .at(-8, 6)
            .title("LV 流体加热器")
            .task(Task.item(GTMachines.FLUID_HEATER[GTValues.LV]))
            .scaledReward(GTMaterials.AceticAcid.getBucket(), 2)
            .dependsOn(CALCIUM_ACETATE));

    public static final Quest ACETONE = CHAPTER.quest("acetone", q -> q
            .at(-6, 6)
            .title("丙酮")
            .icon(GTMaterials.Acetone.getBucket())
            .task(Task.item(GTMaterials.Acetone.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.AceticAcid.getBucket(), 2)
            .dependsOn(FLUID_HEATER_LV));

    public static final Quest PROPENE = CHAPTER.quest("propene", q -> q
            .at(-4, 10)
            .title("丙烯")
            .icon(GTMaterials.Propene.getBucket())
            .task(Task.item(GTMaterials.Propene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SeverelySteamCrackedNaphtha.getBucket(), 4)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest LITHIUM = CHAPTER.quest("lithium", q -> q
            .at(2, -4)
            .title("锂")
            .icon(TagPrefix.dust, GTMaterials.Lithium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Lithium, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Lithium, 4)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest ALUMINUM_ALLOY_8090 = CHAPTER.quest("aluminum_alloy_8090", q -> q
            .at(4, -6)
            .title("铝合金-8090")
            .icon(TagPrefix.ingot, GTOMaterials.AluminumAlloy8090)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AluminumAlloy8090, 64))
            .scaledReward(TagPrefix.ingot, GTOMaterials.AluminumAlloy8090, 16)
            .dependsOn(LITHIUM));

    public static final Quest STATION_STEELS = CHAPTER.quest("aluminum_alloy_7050_and_stainless_steel_gc4", q -> q
            .at(4, -4)
            .title("铝合金-7050 与不锈钢-GC4")
            .icon(TagPrefix.ingot, GTOMaterials.StainlessSteelGC4)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AluminumAlloy7050, 16), Task.item(TagPrefix.ingot, GTOMaterials.StainlessSteelGC4, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.AluminumAlloy7050, 8)
            .dependsOn(LITHIUM));

    public static final Quest TITANIUM_TI64_AND_VIBRANT_ALLOY = CHAPTER.quest("titanium_ti64_and_vibrant_alloy", q -> q
            .at(4, -2)
            .title("钛合金-Ti64 与振动合金")
            .icon(TagPrefix.ingot, GTOMaterials.TitaniumTi64)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.TitaniumTi64, 16), Task.item(TagPrefix.ingot, GTOMaterials.VibrantAlloy, 8))
            .scaledReward(TagPrefix.ingot, GTOMaterials.TitaniumTi64, 4)
            .dependsOn(LITHIUM));

    public static final Quest EXPLORER_STATION = CHAPTER.quest("explorer_space_station", q -> q
            .at(6, -4)
            .title("探索者号空间站")
            .icon(SpaceMultiblock.SPACE_STATION)
            .task(Task.item(SpaceMultiblock.SPACE_STATION),
                    Task.item(GTOBlocks.ALUMINUM_ALLOY_8090_SKIN_MECHANICAL_BLOCK, 64),
                    Task.item(GTOBlocks.ALUMINUM_ALLOY_7050_SUPPORT_MECHANICAL_BLOCK, 16),
                    Task.item(GTOBlocks.SPACECRAFT_SEALING_MECHANICAL_BLOCK, 16),
                    Task.item(GTOBlocks.PRESSURE_CONTAINMENT_CASING, 16))
            .scaledReward(GTBlocks.CASING_STAINLESS_CLEAN, 16)
            .dependsOn(ALUMINUM_ALLOY_8090, STATION_STEELS, TITANIUM_TI64_AND_VIBRANT_ALLOY, MAINFRAME));

    public static final Quest EARTH_ORBIT = CHAPTER.quest("earth_orbit", q -> q
            .at(8, 0)
            .milestone()
            .title("地球轨道空间站")
            .icon(GTOItems.SPACE_PROBE_MK1)
            .task(Task.dimension(Planet.EARTH_ORBIT).title("抵达地球轨道").icon(GTOItems.SPACE_PROBE_MK1))
            .scaledReward(GTMaterials.RocketFuel.getBucket(), 16)
            .dependsOn(EXPLORER_STATION));

    public static final Quest HV_FLUID_DRILLING_RIG = CHAPTER.quest("hv_fluid_drilling_rig", q -> q
            .at(-2, 20)
            .title("HV 流体钻机")
            .task(Task.item(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.HV]))
            .scaledReward(GTItems.ELECTRIC_PUMP_HV, 2)
            .dependsOn(MARS));

    public static final Quest MARS_RADON = CHAPTER.quest("mars_radon", q -> q
            .at(0, 20)
            .title("氡")
            .icon(GTMaterials.Radon.getBucket())
            .task(Task.item(GTMaterials.Radon.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Radon.getBucket(), 1)
            .dependsOn(HV_FLUID_DRILLING_RIG));

    public static final Quest QUANTUM_EYE = CHAPTER.quest("quantum_eye", q -> q
            .at(2, 20)
            .title("量子之眼")
            .desc(d -> d.line("由于其亚稳态性，{量子之眼}需要在{无重力环境}中用惰性气体{氡气}进行浸泡", GOLD, LIGHT_PURPLE, GOLD))
            .task(Task.item(GTItems.QUANTUM_EYE, 2))
            .scaledReward(GTMaterials.Radon.getBucket(), 1)
            .dependsOn(EARTH_ORBIT, MARS_RADON));

    public static final Quest EV_EMITTER_SENSOR = CHAPTER.quest("ev_emitter_and_sensor", q -> q
            .at(2, 18)
            .title("EV 发射器与传感器")
            .icon(GTItems.EMITTER_EV)
            .task(Task.item(GTItems.EMITTER_EV, 2), Task.item(GTItems.SENSOR_EV, 2))
            .scaledReward(GTItems.QUANTUM_EYE, 1)
            .dependsOn(QUANTUM_EYE));

    public static final QuestLink EV_EMITTER_SENSOR_COPY_1 = CHAPTER.copy(EV_EMITTER_SENSOR, 16, 4);

    public static final Quest EV_CIRCUIT_ASSEMBLER = CHAPTER.quest("ev_circuit_assembler", q -> q
            .at(2, 16)
            .title("EV 电路组装机")
            .desc(d -> d.line("获得了{进阶电路组装机 Ⅲ}？丢进你的超净间，{纳米电路}即将排产！", GOLD, GOLD))
            .icon(GTMachines.CIRCUIT_ASSEMBLER[GTValues.EV])
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.EV]))
            .scaledReward(GTItems.EMITTER_EV, 1)
            .dependsOn(EV_EMITTER_SENSOR, MAINFRAME_COPY_1));

    public static final Quest CARBON_FIBER = CHAPTER.quest("carbon_fiber", q -> q
            .at(12, 6)
            .title("碳纤维")
            .icon(GTItems.CARBON_FIBERS)
            .task(Task.item(GTItems.CARBON_FIBERS, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Carbon, 16)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest NANO_CPU_WAFER = CHAPTER.quest("nano_cpu_wafer", q -> q
            .at(12, 8)
            .milestone()
            .title("纳米 CPU 晶圆")
            .icon(GTItems.NANO_CENTRAL_PROCESSING_UNIT_WAFER)
            .task(Task.item(GTItems.NANO_CENTRAL_PROCESSING_UNIT_WAFER))
            .scaledReward(GTItems.CENTRAL_PROCESSING_UNIT_WAFER, 2)
            .dependsOn(CARBON_FIBER, CHEMICAL_VAPOR_DEPOSITION));

    public static final Quest NANO_CPU = CHAPTER.quest("nano_cpu", q -> q
            .at(8, 8)
            .milestone()
            .title("纳米 CPU")
            .icon(GTItems.NANO_CENTRAL_PROCESSING_UNIT)
            .task(Task.item(GTItems.NANO_CENTRAL_PROCESSING_UNIT, 8))
            .scaledReward(GTItems.NANO_CENTRAL_PROCESSING_UNIT, 2)
            .dependsOn(NANO_CPU_WAFER));

    public static final Quest SMD_COMPONENTS = CHAPTER.quest("smd_components", q -> q
            .at(4, 0)
            .milestone()
            .title("SMD 元件")
            .desc(d -> d.line("你可以无需经过{铂处理}，从化学浸洗过的镍矿中获得{铂金属粉}", YELLOW, GOLD))
            .icon(GTItems.SMD_TRANSISTOR)
            .task(Task.item(GTItems.SMD_RESISTOR, 48), Task.item(GTItems.SMD_TRANSISTOR, 48), Task.item(GTItems.SMD_CAPACITOR, 64), Task.item(GTItems.SMD_INDUCTOR, 32), Task.item(GTItems.SMD_DIODE, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.Tantalum, 4)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest BISPHENOL_A = CHAPTER.quest("bisphenol_a", q -> q
            .at(-6, 8)
            .title("双酚A")
            .icon(GTMaterials.BisphenolA.getBucket())
            .task(Task.item(GTMaterials.BisphenolA.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Acetone.getBucket(), 2)
            .dependsOn(ACETONE));

    public static final Quest EPOXY = CHAPTER.quest("epoxy", q -> q
            .at(-4, 8)
            .title("环氧树脂")
            .icon(GTMaterials.Epoxy.getBucket())
            .task(Task.item(GTMaterials.Epoxy.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.BisphenolA.getBucket(), 1)
            .dependsOn(BISPHENOL_A, PROPENE));

    public static final Quest EPOXY_BOARD = CHAPTER.quest("epoxy_circuit_board", q -> q
            .at(-2, 8)
            .title("环氧电路基板")
            .task(Task.item(GTItems.EPOXY_BOARD, 16))
            .scaledReward(TagPrefix.plate, GTMaterials.Epoxy, 4)
            .dependsOn(EPOXY));

    public static final Quest BARIUM = CHAPTER.quest("barium", q -> q
            .at(-2, 4)
            .title("钡")
            .task(Task.item(TagPrefix.dust, GTMaterials.Barium, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.Barite, 12)
            .dependsOn(EV_ELECTROLYZER_COPY_1));

    public static final Quest BARIUM_HYDROXIDE_AND_TITANIUM_DIOXIDE = CHAPTER.quest("barium_hydroxide_and_titanium_dioxide", q -> q
            .at(-2, 6)
            .title("氢氧化钡与二氧化钛")
            .icon(TagPrefix.dust, GTOMaterials.BariumHydroxide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.BariumHydroxide, 90), Task.item(TagPrefix.dust, GTOMaterials.TitaniumDioxide, 54))
            .scaledReward(TagPrefix.dust, GTMaterials.Barium, 4)
            .dependsOn(BARIUM, LARGE_CHEMICAL_REACTOR_COPY_1));

    public static final Quest BARIUM_TITANATE_CERAMIC_DUST = CHAPTER.quest("barium_titanate_ceramic_dust", q -> q
            .at(0, 6)
            .title("钛酸钡陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.BariumTitanateCeramic, 18))
            .scaledReward(TagPrefix.dust, GTOMaterials.TitaniumDioxide, 12)
            .dependsOn(BARIUM_HYDROXIDE_AND_TITANIUM_DIOXIDE));

    public static final Quest ISOSTATIC_PRESS = CHAPTER.quest("isostatic_press", q -> q
            .at(2, 6)
            .title("等静压成型机")
            .subtitle("绝对相等")
            .desc(d -> d
                    .line("高级陶瓷块需要更为精确的{等静压成型}，否则会因为压力不均产生内部缺陷", GOLD)
                    .line()
                    .line("烧结炉将{陶瓷粗胚}烧结成为{陶瓷块}", GOLD, GOLD))
            .task(Task.item(MultiBlockG.ISOSTATIC_PRESS))
            .scaledReward(GTMaterials.Epoxy.getBucket(), 1)
            .dependsOn(BARIUM_TITANATE_CERAMIC_DUST));

    public static final Quest BARIUM_TITANATE_ROUGH_BLANK = CHAPTER.quest("barium_titanate_ceramic_rough_blank", q -> q
            .at(4, 6)
            .title("钛酸钡陶瓷粗胚")
            .icon(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.BariumTitanateCeramic)
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.BariumTitanateCeramic, 2))
            .scaledReward(GTMaterials.Epoxy.getBucket(), 1)
            .dependsOn(ISOSTATIC_PRESS));

    public static final Quest SINTERING_FURNACE = CHAPTER.quest("sintering_furnace", q -> q
            .at(4, 8)
            .milestone()
            .title("烧结炉")
            .task(Task.item(MultiBlockG.SINTERING_FURNACE))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic, 9)
            .dependsOn(BARIUM_TITANATE_ROUGH_BLANK));

    public static final Quest BARIUM_TITANATE_FLAKE = CHAPTER.quest("barium_titanate_ceramic_flake", q -> q
            .at(2, 10)
            .title("钛酸钡陶瓷薄片")
            .icon(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic, 48))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic, 9)
            .dependsOn(SINTERING_FURNACE));

    public static final Quest ADVANCED_CIRCUIT_BOARD = CHAPTER.quest("advanced_circuit_board", q -> q
            .at(0, 8)
            .milestone()
            .title("高级电路板")
            .icon(GTItems.ADVANCED_CIRCUIT_BOARD)
            .task(Task.item(GTItems.ADVANCED_CIRCUIT_BOARD, 16))
            .scaledReward(TagPrefix.plate, GTMaterials.Epoxy, 8)
            .dependsOn(EPOXY_BOARD, BARIUM_TITANATE_FLAKE));

    public static final Quest NANO_PROCESSOR = CHAPTER.quest("nano_processor", q -> q
            .at(2, 14)
            .milestone()
            .title("纳米处理器")
            .subtitle("更便宜的HV级电路")
            .desc(d -> d
                    .line("在你后续加工新的电路板时，切记要优化一下之前的配方。现在你可以使用更加廉价的贴片系列！")
                    .line()
                    .line("检查清单：电阻，电容，晶体管是否是贴片版本"))
            .icon(GTItems.NANO_PROCESSOR_HV)
            .task(Task.item(GTItems.NANO_PROCESSOR_HV, 6))
            .scaledReward(GTItems.SMD_RESISTOR, 16)
            .dependsOn(NANO_CPU, ADVANCED_CIRCUIT_BOARD, SMD_COMPONENTS, EV_CIRCUIT_ASSEMBLER));

    public static final Quest NANO_ASSEMBLY = CHAPTER.quest("nano_processor_assembly", q -> q
            .at(6, 14)
            .milestone()
            .title("纳米处理器集群")
            .subtitle("更便宜的EV级电路")
            .desc(d -> d.line("检查清单：电感，电容，同样是贴片版本"))
            .icon(GTItems.NANO_PROCESSOR_ASSEMBLY_EV)
            .task(Task.item(GTItems.NANO_PROCESSOR_ASSEMBLY_EV, 6))
            .scaledReward(GTItems.SMD_INDUCTOR, 8)
            .dependsOn(NANO_PROCESSOR));

    public static final Quest NANO_COMPUTER = CHAPTER.quest("nano_processor_supercomputer", q -> q
            .at(12, 14)
            .milestone()
            .title("纳米处理器超级计算机")
            .subtitle("更便宜的IV级电路")
            .desc(d -> d.line("检查清单：二极管，还是贴片版本"))
            .icon(GTItems.NANO_COMPUTER_IV)
            .task(Task.item(GTItems.NANO_COMPUTER_IV, 2))
            .scaledReward(GTItems.SMD_DIODE, 8)
            .dependsOn(NANO_ASSEMBLY));

    public static final QuestLink NANO_COMPUTER_COPY_1 = CHAPTER.copy(NANO_COMPUTER, -8, -2);

    public static final QuestLink NANO_COMPUTER_COPY_2 = CHAPTER.copy(NANO_COMPUTER, -6, 4);

    public static final Quest URANIUM_ORE = CHAPTER.quest("uranium_ore", q -> q
            .at(-2, 0)
            .side()
            .title("铀矿")
            .desc(d -> d.line("你在月球捡到的，还记得吗？"))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Pitchblende), Task.item(TagPrefix.rawOre, GTMaterials.Uraninite))
            .dependsOn(NewHvChapter.MOON, NewHvChapter.EV_HULL));

    public static final Quest DEPLETED_URANIUM_HEXAFLUORIDE_BUCKET = CHAPTER.quest("depleted_uranium_hexafluoride_bucket", q -> q
            .at(-4, 0)
            .side()
            .icon(GTMaterials.DepletedUraniumHexafluoride.getBucket())
            .task(Task.item(GTMaterials.DepletedUraniumHexafluoride.getBucket()).optional(), Task.checkmark())
            .dependsOn(URANIUM_ORE));

    public static final Quest URANIUM_DUST = CHAPTER.quest("uranium_dust", q -> q
            .at(-6, 0)
            .side()
            .subtitle("小孩勿食")
            .task(Task.item(TagPrefix.dust, GTMaterials.Uranium238))
            .dependsOn(DEPLETED_URANIUM_HEXAFLUORIDE_BUCKET));

    public static final Quest EV_ROCKET_ENGINE = CHAPTER.quest("ev_rocket_engine", q -> q
            .at(0, 2)
            .side()
            .desc(d -> d.line("EV初期你最强力的单方块电力来源"))
            .task(Task.item(GTOMachines.ROCKET_ENGINE_GENERATOR[GTValues.EV]))
            .optional()
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest RESERVOIR_HATCH = CHAPTER.quest("reservoir_hatch", q -> q
            .at(0, -2)
            .side()
            .subtitle("很多很多水！")
            .desc(d -> d.line("暂停工作的时候会停止产水哦"))
            .task(Task.item(GTMachines.RESERVOIR_HATCH))
            .dependsOn(NewHvChapter.EV_HULL, NewHvChapter.INFINITE_WATER_COVER));

    public static final Quest HV_FIELD_GENERATOR = CHAPTER.quest("hv_field_generator", q -> q
            .at(4, 20)
            .side()
            .task(Task.item(GTItems.FIELD_GENERATOR_HV))
            .dependsOn(QUANTUM_EYE));

    public static final Quest VIBRANT_PHOTOVOLTAIC_POWER_STATION = CHAPTER.quest("vibrant_photovoltaic_power_station", q -> q
            .at(14, 6)
            .side()
            .desc(d -> d.line("光伏电站的终极方案"))
            .task(Task.item(GeneratorMultiblock.PHOTOVOLTAIC_POWER_STATION_VIBRANT))
            .optional()
            .dependsOn(TUNGSTEN_STEEL));

    public static final Quest DARK_STEEL_VAJRA = CHAPTER.quest("dark_steel_vajra", q -> q
            .at(6, 20)
            .side()
            .iconScale(0.75)
            .desc(d -> d
                    .line("作为一把万能工具，你在基地大部分需要工具的地方都能用到{金刚杵}", GOLD)
                    .line()
                    .line("如果你没改动过按键设置，按{[J]}和{[Shift]+[J]}键调整挖掘速度，调试产线时最好不要设置得太大", YELLOW, YELLOW)
                    .line()
                    .line("{如果你在多人服务器游玩并因此搞砸了某些事情，我预见到你的朋友可能会提着另一根金刚杵来找你……}", GRAY.italic()))
            .task(Task.item(ToolHelper.get(GTOToolType.VAJRA_HV, GTOMaterials.DarkSteel).getItem()))
            .dependsOn(HV_FIELD_GENERATOR));

    public static final Quest CLEANING_MAINTENANCE_HATCH = CHAPTER.quest("cleaning_maintenance_hatch", q -> q
            .at(4, 18)
            .side()
            .desc(d -> d
                    .line("它可以为{多方块机器}提供超净间环境，但{不支持}{通用工厂}", YELLOW, DARK_RED, GOLD)
                    .line()
                    .line("{神奇的是，它可以安装在蒸汽机器上...}", GRAY.italic()))
            .task(Task.item(GTMachines.CLEANING_MAINTENANCE_HATCH))
            .dependsOn(HV_FIELD_GENERATOR));

    public static final Quest RAW_DESH = CHAPTER.quest("raw_desh", q -> q
            .at(18, 6)
            .side()
            .desc(d -> d.line("如果你从其他地方弄到了一定数量也可以直接完成此任务"))
            .task(Task.item(ModItems.RAW_DESH.get()))
            .requireAny()
            .dependsOn(TUNGSTEN_STEEL, EV_EMITTER_SENSOR_COPY_1, UtilChapter.RAW_DESH));

    public static final Quest LET_ME_SEE_YOUR_RESULTS = CHAPTER.quest("let_me_see_your_results", q -> q
            .at(16, 12)
            .side()
            .title("让我看看你的成果！")
            .desc(d -> d.line("EV阶段的第二个单质生产挑战，你终于完成了铂系处理产线！"))
            .task(
                    Task.item(TagPrefix.dust, GTMaterials.Platinum),
                    Task.item(TagPrefix.dust, GTMaterials.Palladium),
                    Task.item(TagPrefix.dust, GTMaterials.Ruthenium),
                    Task.item(TagPrefix.dust, GTMaterials.Rhodium),
                    Task.item(TagPrefix.dust, GTMaterials.Iridium),
                    Task.item(TagPrefix.dust, GTMaterials.Osmium))
            .reward(Reward.item(Items.NETHER_STAR))
            .dependsOn(PLATINUM));

    public static final Quest FILTERED_WATER_BUCKET = CHAPTER.quest("filtered_water_bucket", q -> q
            .at(18, 4)
            .side()
            .icon(GTOMaterials.FilteredSater.getBucket())
            .desc(d -> d.line("水净化处理的第一个步骤，恰好满足当前的洁净度需求"))
            .task(Task.item(GTOMaterials.FilteredSater.getBucket()).optional(), Task.checkmark())
            .dependsOn(RAW_DESH));

    public static final Quest INTEGRAL_FRAMEWORK_EV = CHAPTER.quest("integral_framework_ev", q -> q
            .at(20, 12)
            .side()
            .subtitle("别忘了替换机器中的整体框架")
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_EV))
            .dependsOn(TUNGSTEN));

    public static final Quest EV_DRONE = CHAPTER.quest("ev_drone", q -> q
            .at(0, 18)
            .side()
            .desc(d -> d.line("维护半径大了三倍，一台足以覆盖你的整个基地"))
            .task(Task.item(GTOItems.EV_DRONE))
            .dependsOn(EV_EMITTER_SENSOR, TUNGSTEN_STEEL_COPY_1));

    public static final Quest GREAT_FORCE_CREATES_MIRACLES_EASY = CHAPTER.quest("great_force_creates_miracles_easy", q -> q
            .at(16, 6)
            .side()
            .icon(MultiBlockG.ISOSTATIC_PRESS)
            .title("大力出奇迹{（简单）}", GREEN)
            .desc(d -> d
                    .line("简单模式陶瓷无需压制成型")
                    .line()
                    .line("{史蒂夫的手可以解决一切}", GRAY.italic()))
            .task(Task.checkmark())
            .optional().hideUntilDepsVisible()
            .dependsOn(UtilChapter.Q_056EC4E6, TUNGSTEN_STEEL));

    public static final Quest LARGE_GAS_COLLECTOR = CHAPTER.quest("large_gas_collector", q -> q
            .at(-6, -2)
            .side()
            .desc(d -> d
                    .line("用来大量收集各维度的{空气}，放入{真空冷冻机}以直接收集{液态空气}", GOLD, GOLD, GOLD)
                    .line()
                    .line("蒸馏某些空气能够获得{非常稀有的气态物质}，你应该考虑搭建专门的产线持续产出这些物质（如保护气{氩}）", YELLOW, GREEN))
            .task(Task.item(MultiBlockA.LARGE_GAS_COLLECTOR))
            .dependsOn(NANO_COMPUTER_COPY_1, EV_ENERGY_HATCH_COPY_1, URANIUM_DUST));

    public static final Quest VOID_MINER = CHAPTER.quest("void_miner", q -> q
            .at(10, 12)
            .side()
            .desc(d -> d.line("{进阶基岩采矿机}的延伸版本，仅需在指定维度即可产出矿石", GOLD))
            .task(Task.item(MultiBlockA.VOID_MINER))
            .hideDependencyLines(false)
            .dependsOn(NewHvChapter.HV_BEDROCK_ORE_MINER, NANO_COMPUTER));

    public static final Quest EV_BEDROCK_ORE_MINER = CHAPTER.quest("ev_bedrock_ore_miner", q -> q
            .at(12, 12)
            .side()
            .desc(d -> d.line("{进阶基岩采矿机}的再进阶版本！", GOLD))
            .task(Task.item(GTMultiMachines.BEDROCK_ORE_MINER[GTValues.EV]))
            .hideDependencyLines(false)
            .dependsOn(NANO_COMPUTER));

    public static final Quest ROCKET_LARGE_TURBINE = CHAPTER.quest("rocket_large_turbine", q -> q
            .at(10, 16)
            .side()
            .desc(d -> d.line("火箭引擎的多方块机器版本，效率与发电量远远超过单方块机器"))
            .task(Task.item(GeneratorMultiblock.ROCKET_LARGE_TURBINE))
            .optional()
            .dependsOn(NANO_COMPUTER));

    public static final Quest STEAM_MEGA_TURBINE = CHAPTER.quest("steam_mega_turbine", q -> q
            .at(12, 16)
            .side()
            .task(Task.item(GeneratorMultiblock.STEAM_MEGA_TURBINE))
            .optional()
            .dependsOn(NewHvChapter.STEAM_LARGE_TURBINE, NANO_COMPUTER));

    public static final Quest GAS_MEGA_TURBINE = CHAPTER.quest("gas_mega_turbine", q -> q
            .at(14, 16)
            .side()
            .task(Task.item(GeneratorMultiblock.GAS_MEGA_TURBINE))
            .optional()
            .dependsOn(NANO_COMPUTER));

    public static final Quest LARGE_GREENHOUSE = CHAPTER.quest("large_greenhouse", q -> q
            .at(-6, 2)
            .side()
            .task(Task.item(MultiBlockG.LARGE_GREENHOUSE))
            .dependsOn(NANO_COMPUTER_COPY_2, URANIUM_DUST));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewHvChapter.EV_HULL, 0, 0))
                .mainLine(NewHvChapter.EV_HULL, SMD_COMPONENTS, EARTH_ORBIT, PYROLYSE_OVEN, GAS_COMPRESSOR, MARS, PLATINUM_RAW, TUNGSTEN_STEEL, NANO_CPU_WAFER, NANO_CPU, SINTERING_FURNACE, ADVANCED_CIRCUIT_BOARD, EV_ENERGY_HATCH, NANO_PROCESSOR, NANO_ASSEMBLY, IV_HULL, NANO_COMPUTER));
    }
}
