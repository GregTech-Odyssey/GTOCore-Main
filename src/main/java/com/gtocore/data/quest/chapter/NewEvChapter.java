package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.SpaceMultiblock;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.QuestLink;
import com.gtolib.api.quest.QuestRouting;
import com.gtolib.api.quest.reward.Reward;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

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
            .at(6, -6)
            .title("钕")
            .task(Task.item(TagPrefix.dust, GTMaterials.Neodymium, 11))
            .scaledReward(TagPrefix.dust, GTMaterials.Neodymium, 3)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest NEODYMIUM_MAGNETIC_ROD = CHAPTER.quest("neodymium_magnetic_rod", q -> q
            .at(8, -6)
            .title("钕磁棒")
            .icon(TagPrefix.rod, GTMaterials.NeodymiumMagnetic)
            .task(Task.item(GTMachines.POLARIZER[GTValues.HV]), Task.item(TagPrefix.rod, GTMaterials.NeodymiumMagnetic, 21))
            .scaledReward(TagPrefix.rod, GTMaterials.Neodymium, 5)
            .dependsOn(NEODYMIUM));

    public static final Quest PDMS = CHAPTER.quest("polydimethylsiloxane", q -> q
            .at(10, -6)
            .title("聚二甲基硅氧烷")
            .task(Task.item(TagPrefix.dust, GTMaterials.Polydimethylsiloxane, 36))
            .scaledReward(TagPrefix.dust, GTMaterials.Silicon, 3)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest SILICONE_RUBBER = CHAPTER.quest("silicone_rubber", q -> q
            .at(12, -6)
            .title("硅橡胶")
            .icon(GTMaterials.SiliconeRubber.getBucket())
            .task(Task.item(GTMaterials.SiliconeRubber.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Polydimethylsiloxane, 9)
            .dependsOn(PDMS));

    public static final Quest EXTRUDER_MV = CHAPTER.quest("extruder_mv", q -> q
            .at(12, -4)
            .title("MV 压模器")
            .task(Task.item(GTMachines.EXTRUDER[GTValues.MV]))
            .scaledReward(TagPrefix.ring, GTMaterials.SiliconeRubber, 1)
            .dependsOn(SILICONE_RUBBER));

    public static final Quest SILICONE_RUBBER_RING = CHAPTER.quest("silicone_rubber_ring", q -> q
            .at(10, -4)
            .title("硅橡胶环")
            .icon(TagPrefix.ring, GTMaterials.SiliconeRubber)
            .task(Task.item(TagPrefix.ring, GTMaterials.SiliconeRubber, 2))
            .scaledReward(TagPrefix.ring, GTMaterials.SiliconeRubber, 1)
            .dependsOn(EXTRUDER_MV));

    public static final Quest PVC_FOIL_AND_TEMPERED_GLASS = CHAPTER.quest("pvc_foil_and_tempered_glass", q -> q
            .at(6, -4)
            .title("聚氯乙烯箔与钢化玻璃")
            .icon(TagPrefix.foil, GTMaterials.PolyvinylChloride)
            .task(Task.item(TagPrefix.foil, GTMaterials.PolyvinylChloride, 56), Task.item(GTBlocks.CASING_TEMPERED_GLASS, 5))
            .scaledReward(TagPrefix.foil, GTMaterials.PolyvinylChloride, 8)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest EV_COMPONENTS = CHAPTER.quest("ev_components", q -> q
            .at(8, -4)
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
            .at(10, -2)
            .title("EV 搅拌机")
            .task(Task.item(GTMachines.MIXER[GTValues.EV]))
            .scaledReward(TagPrefix.plate, GTMaterials.Titanium, 4)
            .dependsOn(EV_COMPONENTS));

    public static final QuestLink EV_MIXER_COPY_1 = CHAPTER.copy(EV_MIXER, 14, 2);

    public static final QuestLink EV_MIXER_COPY_2 = CHAPTER.copy(EV_MIXER, 20, -6);

    public static final Quest DAMASCUS_STEEL = CHAPTER.quest("damascus_steel", q -> q
            .at(22, -6)
            .title("大马士革钢")
            .icon(TagPrefix.ingot, GTMaterials.DamascusSteel)
            .task(Task.item(TagPrefix.ingot, GTMaterials.DamascusSteel, 48))
            .scaledReward(TagPrefix.ingot, GTMaterials.DamascusSteel, 12)
            .dependsOn(EV_MIXER_COPY_2));

    public static final Quest ROLLING_MV = CHAPTER.quest("rolling_mv", q -> q
            .at(24, -6)
            .title("MV 辊轧机")
            .task(Task.item(GTOMachines.ROLLING[GTValues.MV]))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Titanium, 3)
            .dependsOn(DAMASCUS_STEEL));

    public static final Quest DAMASCUS_STEEL_DOUBLE_PLATE = CHAPTER.quest("damascus_steel_double_plate", q -> q
            .at(24, -4)
            .title("大马士革钢双层板")
            .icon(TagPrefix.plateDouble, GTMaterials.DamascusSteel)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.DamascusSteel, 24))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Titanium, 3)
            .dependsOn(ROLLING_MV));

    public static final Quest HEAVY_DUTY_PLATE_2 = CHAPTER.quest("heavy_duty_plate_2", q -> q
            .at(22, -4)
            .title("二阶重型板")
            .icon(GTOItems.HEAVY_DUTY_PLATE_2)
            .task(Task.item(GTMachines.FORMING_PRESS[GTValues.HV]), Task.item(GTOItems.HEAVY_DUTY_PLATE_2, 12))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_1, 1)
            .dependsOn(DAMASCUS_STEEL_DOUBLE_PLATE));

    public static final Quest TIER_2_ROCKET = CHAPTER.quest("tier_2_rocket", q -> q
            .at(20, -4)
            .title("T2 火箭")
            .task(Task.item(ModItems.TIER_2_ROCKET.get()))
            .scaledReward(GTItems.WORKSTATION_EV, 2)
            .dependsOn(HEAVY_DUTY_PLATE_2));

    public static final Quest PYROLYSE_OVEN = CHAPTER.quest("pyrolyse_oven", q -> q
            .at(10, 0)
            .milestone()
            .title("热解炉")
            .task(Task.item(GTMultiMachines.PYROLYSE_OVEN))
            .scaledReward(Items.COAL, 12)
            .dependsOn(EV_MIXER));

    public static final Quest COAL_TAR = CHAPTER.quest("coal_tar", q -> q
            .at(16, -6)
            .title("煤焦油")
            .icon(GTMaterials.CoalTar.getBucket())
            .task(Task.item(GTMaterials.CoalTar.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.COAL, 16)
            .dependsOn(PYROLYSE_OVEN));

    public static final Quest KEROSENE = CHAPTER.quest("kerosene", q -> q
            .at(16, -4)
            .title("煤油")
            .icon(GTOMaterials.Kerosene.getBucket())
            .task(Task.item(GTOMaterials.Kerosene.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Coke, 8)
            .dependsOn(COAL_TAR));

    public static final Quest RP_1 = CHAPTER.quest("rp_1", q -> q
            .at(18, -4)
            .title("RP-1 混合燃料")
            .icon(GTOMaterials.Rp1.getBucket())
            .task(Task.item(GTOMaterials.Rp1.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Kerosene.getBucket(), 2)
            .dependsOn(KEROSENE));

    public static final Quest GAS_COMPRESSOR = CHAPTER.quest("gas_compressor", q -> q
            .at(14, 0)
            .milestone()
            .title("气体压缩机")
            .task(Task.item(MultiBlockG.GAS_COMPRESSOR))
            .scaledReward(GTItems.FLUID_REGULATOR_LV, 1)
            .dependsOn(EV_MIXER_COPY_1));

    public static final Quest HIGH_PRESSURE_OXYGEN = CHAPTER.quest("high_pressure_oxygen", q -> q
            .at(14, -2)
            .title("高压氧")
            .icon(GTOMaterials.HighPressureOxygen.getBucket())
            .task(Task.item(GTOMaterials.HighPressureOxygen.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Oxygen.getBucket(), 4)
            .dependsOn(GAS_COMPRESSOR));

    public static final Quest LIQUID_OXYGEN = CHAPTER.quest("liquid_oxygen", q -> q
            .at(16, -2)
            .title("液氧")
            .icon(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID).getBucket())
            .task(Task.item(GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.HighPressureOxygen.getBucket(), 2)
            .dependsOn(HIGH_PRESSURE_OXYGEN));

    public static final Quest ROCKET_FUEL_RP_1 = CHAPTER.quest("rocket_fuel_rp_1", q -> q
            .at(18, -2)
            .title("火箭燃料 RP-1")
            .icon(GTOMaterials.RocketFuelRp1.getBucket())
            .task(Task.item(GTOMaterials.RocketFuelRp1.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelRp1.getBucket(), 2)
            .dependsOn(RP_1, LIQUID_OXYGEN));

    public static final Quest UNLOCK_MARS = CHAPTER.quest("unlock_mars", q -> q
            .at(20, -2)
            .title("解锁火星")
            .icon(ModItems.MARS_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelRp1.getBucket(), 1)
            .dependsOn(ROCKET_FUEL_RP_1, TIER_2_ROCKET));

    public static final Quest MARS = CHAPTER.quest("mars", q -> q
            .at(18, 0)
            .milestone()
            .title("登陆火星")
            .icon(ModItems.MARS_GLOBE.get())
            .task(Task.dimension(Dimension.MARS).title("造访火星").icon(ModItems.MARS_GLOBE.get()))
            .scaledReward(TagPrefix.plate, GTMaterials.Titanium, 4)
            .dependsOn(UNLOCK_MARS));

    public static final Quest NITRIC_OXIDE = CHAPTER.quest("nitric_oxide", q -> q
            .at(20, 2)
            .title("一氧化氮")
            .icon(GTMaterials.NitricOxide.getBucket())
            .task(Task.item(GTMaterials.NitricOxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 1)
            .dependsOn(MARS));

    public static final Quest NITROGEN_DIOXIDE = CHAPTER.quest("nitrogen_dioxide", q -> q
            .at(22, 2)
            .title("二氧化氮")
            .icon(GTMaterials.NitrogenDioxide.getBucket())
            .task(Task.item(GTMaterials.NitrogenDioxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Oxygen.getBucket(), 2)
            .dependsOn(NITRIC_OXIDE));

    public static final Quest NITRIC_ACID = CHAPTER.quest("nitric_acid", q -> q
            .at(22, 4)
            .title("硝酸")
            .icon(GTMaterials.NitricAcid.getBucket())
            .task(Task.item(GTMaterials.NitricAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NitrogenDioxide.getBucket(), 2)
            .dependsOn(NITROGEN_DIOXIDE));

    public static final Quest SHELDONITE = CHAPTER.quest("sheldonite", q -> q
            .at(20, 0)
            .title("谢尔顿矿")
            .icon(TagPrefix.rawOre, GTMaterials.Cooperite)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Cooperite, 10))
            .scaledReward(TagPrefix.rawOre, GTMaterials.Cooperite, 3)
            .dependsOn(MARS));

    public static final Quest FORGE_HAMMER_LV = CHAPTER.quest("forge_hammer_lv", q -> q
            .at(22, 0)
            .title("LV 锻造锤")
            .task(Task.item(GTMachines.FORGE_HAMMER[GTValues.LV]))
            .scaledReward(TagPrefix.crushed, GTMaterials.Cooperite, 3)
            .dependsOn(SHELDONITE));

    public static final Quest CRUSHED_SHELDONITE = CHAPTER.quest("crushed_sheldonite", q -> q
            .at(24, 2)
            .title("粉碎谢尔顿矿")
            .icon(TagPrefix.crushed, GTMaterials.Cooperite)
            .task(Task.item(TagPrefix.crushed, GTMaterials.Cooperite, 10))
            .scaledReward(TagPrefix.crushed, GTMaterials.Cooperite, 3)
            .dependsOn(FORGE_HAMMER_LV));

    public static final Quest ORE_WASHER_LV = CHAPTER.quest("ore_washer_lv", q -> q
            .at(24, 4)
            .title("LV 洗矿厂")
            .task(Task.item(GTMachines.ORE_WASHER[GTValues.LV]))
            .scaledReward(TagPrefix.crushedPurified, GTMaterials.Cooperite, 3)
            .dependsOn(CRUSHED_SHELDONITE));

    public static final Quest PURIFIED_SHELDONITE = CHAPTER.quest("purified_sheldonite", q -> q
            .at(22, 6)
            .title("洗净谢尔顿矿")
            .icon(TagPrefix.crushedPurified, GTMaterials.Cooperite)
            .task(Task.item(TagPrefix.crushedPurified, GTMaterials.Cooperite, 10))
            .scaledReward(TagPrefix.crushedPurified, GTMaterials.Cooperite, 3)
            .dependsOn(ORE_WASHER_LV));

    public static final Quest PLATINUM_GROUP_SLUDGE = CHAPTER.quest("platinum_group_sludge", q -> q
            .at(20, 6)
            .title("铂系矿泥")
            .icon(TagPrefix.dust, GTMaterials.PlatinumGroupSludge)
            .task(Task.item(TagPrefix.dust, GTMaterials.PlatinumGroupSludge, 80))
            .scaledReward(TagPrefix.dust, GTMaterials.PlatinumGroupSludge, 20)
            .dependsOn(NITRIC_ACID, PURIFIED_SHELDONITE));

    public static final QuestLink PLATINUM_GROUP_SLUDGE_COPY_1 = CHAPTER.copy(PLATINUM_GROUP_SLUDGE, 24, 12);

    public static final Quest AQUA_REGIA = CHAPTER.quest("aqua_regia", q -> q
            .at(20, 4)
            .title("王水")
            .icon(GTMaterials.AquaRegia.getBucket())
            .task(Task.item(GTMaterials.AquaRegia.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 2)
            .dependsOn(NITRIC_ACID));

    public static final Quest PLATINUM_RAW = CHAPTER.quest("platinum_raw", q -> q
            .at(18, 6)
            .milestone()
            .title("铂原料")
            .icon(TagPrefix.dust, GTMaterials.PlatinumRaw)
            .task(Task.item(GTMachines.CENTRIFUGE[GTValues.HV]), Task.item(TagPrefix.dust, GTMaterials.PlatinumRaw, 24))
            .scaledReward(TagPrefix.dust, GTMaterials.PlatinumRaw, 6)
            .dependsOn(PLATINUM_GROUP_SLUDGE, AQUA_REGIA));

    public static final QuestLink PLATINUM_RAW_COPY_1 = CHAPTER.copy(PLATINUM_RAW, 22, 16);

    public static final Quest CALCIUM = CHAPTER.quest("calcium", q -> q
            .at(24, 14)
            .title("钙")
            .task(Task.item(TagPrefix.dust, GTMaterials.Calcium, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Apatite, 9)
            .dependsOn(PLATINUM_GROUP_SLUDGE_COPY_1));

    public static final Quest PLATINUM = CHAPTER.quest("platinum", q -> q
            .at(22, 14)
            .title("铂")
            .icon(TagPrefix.ingot, GTMaterials.Platinum)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Platinum, 8))
            .scaledReward(TagPrefix.ingot, GTMaterials.Platinum, 2)
            .dependsOn(PLATINUM_RAW_COPY_1, CALCIUM));

    public static final Quest EV_ELECTROLYZER = CHAPTER.quest("ev_electrolyzer", q -> q
            .at(22, 12)
            .title("EV 电解机")
            .task(Task.item(GTMachines.ELECTROLYZER[GTValues.EV]))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Platinum, 2)
            .dependsOn(PLATINUM));

    public static final QuestLink EV_ELECTROLYZER_COPY_1 = CHAPTER.copy(EV_ELECTROLYZER, -2, 0);

    public static final Quest SCHEELITE = CHAPTER.quest("scheelite", q -> q
            .at(20, 16)
            .title("白钨矿")
            .icon(TagPrefix.rawOre, GTMaterials.Scheelite)
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Scheelite, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.Scheelite, 12)
            .dependsOn(PLATINUM));

    public static final Quest TUNGSTIC_ACID = CHAPTER.quest("tungstic_acid", q -> q
            .at(20, 14)
            .title("钨酸")
            .icon(TagPrefix.dust, GTMaterials.TungsticAcid)
            .task(Task.item(GTMachines.CHEMICAL_BATH[GTValues.EV]), Task.item(TagPrefix.dust, GTMaterials.TungsticAcid, 56))
            .scaledReward(GTMaterials.HydrochloricAcid.getBucket(), 4)
            .dependsOn(SCHEELITE));

    public static final Quest TUNGSTEN = CHAPTER.quest("tungsten", q -> q
            .at(20, 12)
            .title("钨")
            .icon(TagPrefix.dust, GTMaterials.Tungsten)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tungsten, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.TungsticAcid, 14)
            .dependsOn(EV_ELECTROLYZER, TUNGSTIC_ACID));

    public static final QuestLink TUNGSTEN_COPY_1 = CHAPTER.copy(TUNGSTEN, 10, 2);

    public static final Quest NICHROME_DUST = CHAPTER.quest("nichrome_dust", q -> q
            .at(20, 10)
            .title("镍铬粉")
            .icon(TagPrefix.dust, GTMaterials.Nichrome)
            .task(Task.item(TagPrefix.dust, GTMaterials.Nichrome, 128))
            .scaledReward(TagPrefix.dust, GTMaterials.Nickel, 16)
            .dependsOn(EV_ELECTROLYZER));

    public static final Quest NICHROME_INGOT = CHAPTER.quest("nichrome_ingot", q -> q
            .at(20, 8)
            .title("镍铬锭")
            .icon(TagPrefix.ingot, GTMaterials.Nichrome)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Nichrome, 128))
            .scaledReward(TagPrefix.dust, GTMaterials.Nichrome, 32)
            .dependsOn(NICHROME_DUST));

    public static final Quest NICHROME_COIL = CHAPTER.quest("nichrome_coil", q -> q
            .at(18, 8)
            .title("镍铬线圈")
            .icon(GTBlocks.COIL_NICHROME)
            .task(Task.item(GTBlocks.COIL_NICHROME, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.StainlessSteel, 32)
            .dependsOn(NICHROME_INGOT));

    public static final Quest TUNGSTEN_STEEL_DUST = CHAPTER.quest("tungsten_steel_dust", q -> q
            .at(16, 8)
            .title("钨钢粉")
            .icon(TagPrefix.dust, GTMaterials.TungstenSteel)
            .task(Task.item(TagPrefix.dust, GTMaterials.TungstenSteel, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Steel, 4)
            .dependsOn(TUNGSTEN));

    public static final Quest TUNGSTEN_STEEL = CHAPTER.quest("tungsten_steel", q -> q
            .at(16, 6)
            .milestone()
            .title("钨钢锭")
            .icon(TagPrefix.ingot, GTMaterials.TungstenSteel)
            .task(Task.item(TagPrefix.ingot, GTMaterials.TungstenSteel, 16))
            .scaledReward(TagPrefix.ingot, GTMaterials.TungstenSteel, 4)
            .dependsOn(TUNGSTEN_STEEL_DUST, NICHROME_COIL));

    public static final Quest BROWN_DYE = CHAPTER.quest("brown_dye", q -> q
            .at(-6, 14)
            .title("棕色染料")
            .icon(GTMaterials.DyeBrown.getBucket())
            .task(Task.item(GTMaterials.DyeBrown.getBucket()).optional(), Task.checkmark())
            .scaledReward(Items.BROWN_DYE, 8)
            .dependsOn(TUNGSTEN_STEEL));

    public static final Quest BROWN_LENS = CHAPTER.quest("brown_lens", q -> q
            .at(-6, 12)
            .title("棕色透镜")
            .icon(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Brown))
            .task(Task.item(GTItems.GLASS_LENSES.get(MarkerMaterials.Color.Brown)))
            .scaledReward(TagPrefix.dust, GTMaterials.Bentonite, 4)
            .dependsOn(BROWN_DYE));

    public static final Quest PIC_WAFER = CHAPTER.quest("pic_wafer", q -> q
            .at(-4, 12)
            .title("功率集成电路晶圆")
            .icon(GTItems.POWER_INTEGRATED_CIRCUIT_WAFER)
            .task(Task.item(GTItems.POWER_INTEGRATED_CIRCUIT_WAFER))
            .scaledReward(GTItems.PHOSPHORUS_WAFER, 1)
            .dependsOn(BROWN_LENS));

    public static final Quest PIC = CHAPTER.quest("pic", q -> q
            .at(-2, 12)
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
            .at(0, 12)
            .milestone()
            .title("EV 能源仓")
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.EV], 2))
            .scaledReward(TagPrefix.cableGtDouble, GTMaterials.Aluminium, 4)
            .dependsOn(PIC, EV_VOLTAGE_COIL));

    public static final Quest IV_CASING = CHAPTER.quest("iv_casing", q -> q
            .at(14, 10)
            .title("IV 机械方块")
            .task(Task.item(GTBlocks.MACHINE_CASING_IV))
            .scaledReward(TagPrefix.plate, GTMaterials.TungstenSteel, 1)
            .dependsOn(EV_ENERGY_HATCH));

    public static final Quest PLATINUM_CABLE = CHAPTER.quest("platinum_cable", q -> q
            .at(14, 14)
            .title("铂线缆")
            .icon(TagPrefix.cableGtSingle, GTMaterials.Platinum)
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.Platinum, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.Platinum, 1)
            .dependsOn(EV_ENERGY_HATCH));

    public static final Quest IV_HULL = CHAPTER.quest("iv_hull", q -> q
            .at(14, 12)
            .milestone()
            .title("IV 机器外壳")
            .task(Task.item(GTMachines.HULL[GTValues.IV]))
            .reward(Reward.item(GTMachines.HULL[GTValues.IV], 1))
            .dependsOn(EV_ENERGY_HATCH, IV_CASING, PLATINUM_CABLE));

    public static final Quest LARGE_CHEMICAL_REACTOR = CHAPTER.quest("large_chemical_reactor", q -> q
            .at(12, 2)
            .title("大型化学反应釜")
            .task(
                    Task.item(GTMultiMachines.LARGE_CHEMICAL_REACTOR),
                    Task.item(GTBlocks.CASING_PTFE_INERT, 10),
                    Task.item(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE))
            .scaledReward(GTBlocks.CASING_PTFE_INERT, 4)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final QuestLink LARGE_CHEMICAL_REACTOR_COPY_1 = CHAPTER.copy(LARGE_CHEMICAL_REACTOR, 0, 4);

    public static final Quest TUNGSTEN_CARBIDE = CHAPTER.quest("tungsten_carbide", q -> q
            .at(10, 4)
            .title("碳化钨")
            .icon(TagPrefix.ingot, GTMaterials.TungstenCarbide)
            .task(Task.item(TagPrefix.ingot, GTMaterials.TungstenCarbide, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.TungstenCarbide, 2)
            .dependsOn(TUNGSTEN_COPY_1));

    public static final Quest CHEMICAL_VAPOR_DEPOSITION = CHAPTER.quest("chemical_vapor_deposition", q -> q
            .at(12, 4)
            .title("化学气相沉积")
            .task(Task.item(MultiBlockC.CHEMICAL_VAPOR_DEPOSITION), Task.item(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE, 13))
            .scaledReward(GTBlocks.CASING_PTFE_INERT, 8)
            .dependsOn(TUNGSTEN_CARBIDE, LARGE_CHEMICAL_REACTOR));

    public static final Quest MAINFRAME = CHAPTER.quest("mainframe", q -> q
            .at(4, -2)
            .title("微型处理器主机")
            .icon(GTItems.MAINFRAME_IV)
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.HV]), Task.item(GTItems.MAINFRAME_IV, 4))
            .scaledReward(GTItems.WORKSTATION_EV, 2)
            .dependsOn(NewHvChapter.WORKSTATION));

    public static final QuestLink MAINFRAME_COPY_1 = CHAPTER.copy(MAINFRAME, 4, 16);

    public static final Quest ACETIC_ACID = CHAPTER.quest("acetic_acid", q -> q
            .at(-6, -2)
            .title("醋酸")
            .icon(GTMaterials.AceticAcid.getBucket())
            .task(Task.item(GTMaterials.AceticAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ethylene.getBucket(), 2)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest CALCIUM_ACETATE = CHAPTER.quest("dissolved_calcium_acetate", q -> q
            .at(-4, -2)
            .title("醋酸钙溶液")
            .icon(GTMaterials.DissolvedCalciumAcetate.getBucket())
            .task(Task.item(GTMaterials.DissolvedCalciumAcetate.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Calcite, 10)
            .dependsOn(ACETIC_ACID));

    public static final Quest FLUID_HEATER_LV = CHAPTER.quest("fluid_heater_lv", q -> q
            .at(-4, 0)
            .title("LV 流体加热器")
            .task(Task.item(GTMachines.FLUID_HEATER[GTValues.LV]))
            .scaledReward(GTMaterials.AceticAcid.getBucket(), 2)
            .dependsOn(CALCIUM_ACETATE));

    public static final Quest ACETONE = CHAPTER.quest("acetone", q -> q
            .at(-4, 2)
            .title("丙酮")
            .icon(GTMaterials.Acetone.getBucket())
            .task(Task.item(GTMaterials.Acetone.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.AceticAcid.getBucket(), 2)
            .dependsOn(FLUID_HEATER_LV));

    public static final Quest PROPENE = CHAPTER.quest("propene", q -> q
            .at(-4, 8)
            .title("丙烯")
            .icon(GTMaterials.Propene.getBucket())
            .task(Task.item(GTMaterials.Propene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SeverelySteamCrackedNaphtha.getBucket(), 4)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest LITHIUM = CHAPTER.quest("lithium", q -> q
            .at(0, -4)
            .title("锂")
            .icon(TagPrefix.dust, GTMaterials.Lithium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Lithium, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Lithium, 4)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest ALUMINUM_ALLOY_8090 = CHAPTER.quest("aluminum_alloy_8090", q -> q
            .at(2, -4)
            .title("铝合金-8090")
            .icon(TagPrefix.ingot, GTOMaterials.AluminumAlloy8090)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AluminumAlloy8090, 64))
            .scaledReward(TagPrefix.ingot, GTOMaterials.AluminumAlloy8090, 16)
            .dependsOn(LITHIUM));

    public static final Quest STATION_STEELS = CHAPTER.quest("aluminum_alloy_7050_and_stainless_steel_gc4", q -> q
            .at(2, -2)
            .title("铝合金-7050 与不锈钢-GC4")
            .icon(TagPrefix.ingot, GTOMaterials.StainlessSteelGC4)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.AluminumAlloy7050, 16), Task.item(TagPrefix.ingot, GTOMaterials.StainlessSteelGC4, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.AluminumAlloy7050, 8)
            .dependsOn(LITHIUM));

    public static final Quest TITANIUM_TI64_AND_VIBRANT_ALLOY = CHAPTER.quest("titanium_ti64_and_vibrant_alloy", q -> q
            .at(2, -6)
            .title("钛合金-Ti64 与振动合金")
            .icon(TagPrefix.ingot, GTOMaterials.TitaniumTi64)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.TitaniumTi64, 16), Task.item(TagPrefix.ingot, GTOMaterials.VibrantAlloy, 8))
            .scaledReward(TagPrefix.ingot, GTOMaterials.TitaniumTi64, 4)
            .dependsOn(LITHIUM));

    public static final Quest EXPLORER_STATION = CHAPTER.quest("explorer_space_station", q -> q
            .at(4, -4)
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
            .at(6, 0)
            .milestone()
            .title("地球轨道空间站")
            .icon(GTOItems.SPACE_PROBE_MK1)
            .task(Task.dimension(Planet.EARTH_ORBIT).title("抵达地球轨道").icon(GTOItems.SPACE_PROBE_MK1))
            .scaledReward(GTMaterials.RocketFuel.getBucket(), 16)
            .dependsOn(EXPLORER_STATION));

    public static final Quest HV_FLUID_DRILLING_RIG = CHAPTER.quest("hv_fluid_drilling_rig", q -> q
            .at(0, 18)
            .title("HV 流体钻机")
            .task(Task.item(GTMultiMachines.FLUID_DRILLING_RIG[GTValues.HV]))
            .scaledReward(GTItems.ELECTRIC_PUMP_HV, 2)
            .dependsOn(MARS));

    public static final Quest MARS_RADON = CHAPTER.quest("mars_radon", q -> q
            .at(0, 16)
            .title("氡")
            .icon(GTMaterials.Radon.getBucket())
            .task(Task.item(GTMaterials.Radon.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Radon.getBucket(), 1)
            .dependsOn(HV_FLUID_DRILLING_RIG));

    public static final Quest QUANTUM_EYE = CHAPTER.quest("quantum_eye", q -> q
            .at(0, 14)
            .title("量子之眼")
            .task(Task.item(GTItems.QUANTUM_EYE, 2))
            .scaledReward(GTMaterials.Radon.getBucket(), 1)
            .dependsOn(EARTH_ORBIT, MARS_RADON));

    public static final Quest EV_EMITTER_SENSOR = CHAPTER.quest("ev_emitter_and_sensor", q -> q
            .at(2, 14)
            .title("EV 发射器与传感器")
            .icon(GTItems.EMITTER_EV)
            .task(Task.item(GTItems.EMITTER_EV, 2), Task.item(GTItems.SENSOR_EV, 2))
            .scaledReward(GTItems.QUANTUM_EYE, 1)
            .dependsOn(QUANTUM_EYE));

    public static final Quest EV_CIRCUIT_ASSEMBLER = CHAPTER.quest("ev_circuit_assembler", q -> q
            .at(4, 14)
            .title("EV 电路组装机")
            .icon(GTMachines.CIRCUIT_ASSEMBLER[GTValues.EV])
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.EV]))
            .scaledReward(GTItems.EMITTER_EV, 1)
            .dependsOn(EV_EMITTER_SENSOR, MAINFRAME_COPY_1));

    public static final Quest CARBON_FIBER = CHAPTER.quest("carbon_fiber", q -> q
            .at(12, 8)
            .title("碳纤维")
            .icon(GTItems.CARBON_FIBERS)
            .task(Task.item(GTItems.CARBON_FIBERS, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Carbon, 16)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest NANO_CPU_WAFER = CHAPTER.quest("nano_cpu_wafer", q -> q
            .at(12, 6)
            .milestone()
            .title("纳米 CPU 晶圆")
            .icon(GTItems.NANO_CENTRAL_PROCESSING_UNIT_WAFER)
            .task(Task.item(GTItems.NANO_CENTRAL_PROCESSING_UNIT_WAFER))
            .scaledReward(GTItems.CENTRAL_PROCESSING_UNIT_WAFER, 2)
            .dependsOn(CARBON_FIBER, CHEMICAL_VAPOR_DEPOSITION));

    public static final Quest NANO_CPU = CHAPTER.quest("nano_cpu", q -> q
            .at(8, 6)
            .milestone()
            .title("纳米 CPU")
            .icon(GTItems.NANO_CENTRAL_PROCESSING_UNIT)
            .task(Task.item(GTItems.NANO_CENTRAL_PROCESSING_UNIT, 8))
            .scaledReward(GTItems.NANO_CENTRAL_PROCESSING_UNIT, 2)
            .dependsOn(NANO_CPU_WAFER));

    public static final Quest SMD_COMPONENTS = CHAPTER.quest("smd_components", q -> q
            .at(2, 0)
            .milestone()
            .title("SMD 元件")
            .icon(GTItems.SMD_TRANSISTOR)
            .task(Task.item(GTItems.SMD_RESISTOR, 48), Task.item(GTItems.SMD_TRANSISTOR, 48), Task.item(GTItems.SMD_CAPACITOR, 64), Task.item(GTItems.SMD_INDUCTOR, 32), Task.item(GTItems.SMD_DIODE, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.Tantalum, 4)
            .dependsOn(NewHvChapter.EV_HULL));

    public static final Quest BISPHENOL_A = CHAPTER.quest("bisphenol_a", q -> q
            .at(-4, 4)
            .title("双酚A")
            .icon(GTMaterials.BisphenolA.getBucket())
            .task(Task.item(GTMaterials.BisphenolA.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Acetone.getBucket(), 2)
            .dependsOn(ACETONE));

    public static final Quest EPOXY = CHAPTER.quest("epoxy", q -> q
            .at(-4, 6)
            .title("环氧树脂")
            .icon(GTMaterials.Epoxy.getBucket())
            .task(Task.item(GTMaterials.Epoxy.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.BisphenolA.getBucket(), 1)
            .dependsOn(BISPHENOL_A, PROPENE));

    public static final Quest EPOXY_BOARD = CHAPTER.quest("epoxy_circuit_board", q -> q
            .at(-2, 6)
            .title("环氧电路基板")
            .task(Task.item(GTItems.EPOXY_BOARD, 16))
            .scaledReward(TagPrefix.plate, GTMaterials.Epoxy, 4)
            .dependsOn(EPOXY));

    public static final Quest BARIUM = CHAPTER.quest("barium", q -> q
            .at(-2, 2)
            .title("钡")
            .task(Task.item(TagPrefix.dust, GTMaterials.Barium, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.Barite, 12)
            .dependsOn(EV_ELECTROLYZER_COPY_1));

    public static final Quest BARIUM_HYDROXIDE_AND_TITANIUM_DIOXIDE = CHAPTER.quest("barium_hydroxide_and_titanium_dioxide", q -> q
            .at(0, 2)
            .title("氢氧化钡与二氧化钛")
            .icon(TagPrefix.dust, GTOMaterials.BariumHydroxide)
            .task(Task.item(TagPrefix.dust, GTOMaterials.BariumHydroxide, 90), Task.item(TagPrefix.dust, GTOMaterials.TitaniumDioxide, 54))
            .scaledReward(TagPrefix.dust, GTMaterials.Barium, 4)
            .dependsOn(BARIUM, LARGE_CHEMICAL_REACTOR_COPY_1));

    public static final Quest BARIUM_TITANATE_CERAMIC_DUST = CHAPTER.quest("barium_titanate_ceramic_dust", q -> q
            .at(2, 2)
            .title("钛酸钡陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.BariumTitanateCeramic, 18))
            .scaledReward(TagPrefix.dust, GTOMaterials.TitaniumDioxide, 12)
            .dependsOn(BARIUM_HYDROXIDE_AND_TITANIUM_DIOXIDE));

    public static final Quest ISOSTATIC_PRESS = CHAPTER.quest("isostatic_press", q -> q
            .at(2, 4)
            .title("等静压成型机")
            .task(Task.item(MultiBlockG.ISOSTATIC_PRESS))
            .scaledReward(GTMaterials.Epoxy.getBucket(), 1)
            .dependsOn(BARIUM_TITANATE_CERAMIC_DUST));

    public static final Quest BARIUM_TITANATE_ROUGH_BLANK = CHAPTER.quest("barium_titanate_ceramic_rough_blank", q -> q
            .at(4, 4)
            .title("钛酸钡陶瓷粗胚")
            .icon(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.BariumTitanateCeramic)
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.BariumTitanateCeramic, 2))
            .scaledReward(GTMaterials.Epoxy.getBucket(), 1)
            .dependsOn(ISOSTATIC_PRESS));

    public static final Quest SINTERING_FURNACE = CHAPTER.quest("sintering_furnace", q -> q
            .at(4, 6)
            .milestone()
            .title("烧结炉")
            .task(Task.item(MultiBlockG.SINTERING_FURNACE))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic, 9)
            .dependsOn(BARIUM_TITANATE_ROUGH_BLANK));

    public static final Quest BARIUM_TITANATE_FLAKE = CHAPTER.quest("barium_titanate_ceramic_flake", q -> q
            .at(2, 8)
            .title("钛酸钡陶瓷薄片")
            .icon(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic)
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic, 48))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.BariumTitanateCeramic, 9)
            .dependsOn(SINTERING_FURNACE));

    public static final Quest ADVANCED_CIRCUIT_BOARD = CHAPTER.quest("advanced_circuit_board", q -> q
            .at(0, 6)
            .milestone()
            .title("高级电路板")
            .icon(GTItems.ADVANCED_CIRCUIT_BOARD)
            .task(Task.item(GTItems.ADVANCED_CIRCUIT_BOARD, 16))
            .scaledReward(TagPrefix.plate, GTMaterials.Epoxy, 8)
            .dependsOn(EPOXY_BOARD, BARIUM_TITANATE_FLAKE));

    public static final Quest NANO_PROCESSOR = CHAPTER.quest("nano_processor", q -> q
            .at(4, 12)
            .milestone()
            .title("纳米处理器")
            .icon(GTItems.NANO_PROCESSOR_HV)
            .task(Task.item(GTItems.NANO_PROCESSOR_HV, 6))
            .scaledReward(GTItems.SMD_RESISTOR, 16)
            .dependsOn(NANO_CPU, ADVANCED_CIRCUIT_BOARD, SMD_COMPONENTS, EV_CIRCUIT_ASSEMBLER));

    public static final Quest NANO_ASSEMBLY = CHAPTER.quest("nano_processor_assembly", q -> q
            .at(10, 12)
            .milestone()
            .title("纳米处理器集群")
            .icon(GTItems.NANO_PROCESSOR_ASSEMBLY_EV)
            .task(Task.item(GTItems.NANO_PROCESSOR_ASSEMBLY_EV, 6))
            .scaledReward(GTItems.SMD_INDUCTOR, 8)
            .dependsOn(NANO_PROCESSOR));

    public static final Quest NANO_COMPUTER = CHAPTER.quest("nano_processor_supercomputer", q -> q
            .at(18, 12)
            .milestone()
            .title("纳米处理器超级计算机")
            .icon(GTItems.NANO_COMPUTER_IV)
            .task(Task.item(GTItems.NANO_COMPUTER_IV, 2))
            .scaledReward(GTItems.SMD_DIODE, 8)
            .dependsOn(NANO_ASSEMBLY));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewHvChapter.EV_HULL, 0, 0))
                .mainLine(NewHvChapter.EV_HULL, SMD_COMPONENTS, EARTH_ORBIT, PYROLYSE_OVEN, GAS_COMPRESSOR, MARS, PLATINUM_RAW, TUNGSTEN_STEEL, NANO_CPU_WAFER, NANO_CPU, SINTERING_FURNACE, ADVANCED_CIRCUIT_BOARD, EV_ENERGY_HATCH, NANO_PROCESSOR, NANO_ASSEMBLY, IV_HULL, NANO_COMPUTER));
    }
}
