package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOGlobes;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.GeneratorMultiblock;
import com.gtocore.common.data.machines.MultiBlockA;
import com.gtocore.common.data.machines.MultiBlockC;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.data.machines.MultiBlockI;
import com.gtocore.common.item.misc.OrganType;
import com.gtocore.data.quest.QuestExternalRefs;
import com.gtocore.data.techtree.BaseNodes;
import com.gtocore.data.techtree.ComponentNodes;

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

import appeng.core.definitions.AEItems;

import earth.terrarium.adastra.common.registry.ModItems;
import vazkii.botania.common.BotaniaBlocks;

import static com.gtolib.api.quest.QuestStyle.*;

public final class NewLuvChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("new_luv", c -> c
            .title("9. 剧差压时代(LuV)")
            .routing(QuestRouting.OCTILINEAR)
            .icon(GTMachines.HULL[GTValues.LuV]));

    public static final Quest BERYLLIUM_AND_RURIDIT = CHAPTER.quest("beryllium_and_ruridit", q -> q
            .at(2, 2)
            .title("铍与钌铱")
            .icon(TagPrefix.plateDouble, GTMaterials.Beryllium)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.Beryllium, 24), Task.item(TagPrefix.plate, GTMaterials.Ruridit, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.Beryllium, 4)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest NEUTRON_REFLECTOR = CHAPTER.quest("neutron_reflector", q -> q
            .at(4, 2)
            .title("中子反射器")
            .icon(GTItems.NEUTRON_REFLECTOR)
            .task(Task.item(GTItems.NEUTRON_REFLECTOR, 6))
            .scaledReward(TagPrefix.plateDouble, GTMaterials.Beryllium, 2)
            .dependsOn(BERYLLIUM_AND_RURIDIT));

    public static final QuestLink NEUTRON_REFLECTOR_COPY_1 = CHAPTER.copy(NEUTRON_REFLECTOR, 2, 14);

    public static final Quest FISSION_REACTOR = CHAPTER.quest("fission_reactor", q -> q
            .at(4, 0)
            .milestone()
            .title("裂变反应堆")
            .desc(d -> d.line("能够获取高压蒸汽和超临界蒸汽用于发电，不过更重要的是获取核废料"))
            .icon(MultiBlockD.FISSION_REACTOR)
            .task(Task.item(MultiBlockD.FISSION_REACTOR), Task.item(GTOBlocks.FISSION_FUEL_COMPONENT), Task.item(GTOBlocks.FISSION_COOLER_COMPONENT))
            .scaledReward(TagPrefix.plate, GTOMaterials.ReactorSteel, 6)
            .dependsOn(NEUTRON_REFLECTOR));

    public static final Quest URANINITE = CHAPTER.quest("uraninite", q -> q
            .at(16, 10)
            .title("晶质铀矿")
            .icon(TagPrefix.dust, GTMaterials.Uraninite)
            .task(Task.item(TagPrefix.dust, GTMaterials.Uraninite, 30))
            .scaledReward(TagPrefix.dust, GTMaterials.Uraninite, 9)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest URANIUM_HEXAFLUORIDE = CHAPTER.quest("uranium_hexafluoride", q -> q
            .at(18, 10)
            .title("六氟化铀")
            .icon(GTMaterials.UraniumHexafluoride.getBucket())
            .task(Task.item(GTMaterials.UraniumHexafluoride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrofluoricAcid.getBucket(), 4)
            .dependsOn(URANINITE));

    public static final Quest ENRICHED_URANIUM_HEXAFLUORIDE = CHAPTER.quest("enriched_uranium_hexafluoride", q -> q
            .at(20, 10)
            .title("浓缩六氟化铀")
            .icon(GTMaterials.EnrichedUraniumHexafluoride.getBucket())
            .task(Task.item(GTMaterials.EnrichedUraniumHexafluoride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.UraniumHexafluoride.getBucket(), 4)
            .dependsOn(URANIUM_HEXAFLUORIDE));

    public static final Quest URANIUM_235 = CHAPTER.quest("uranium_235", q -> q
            .at(22, 10)
            .title("铀235")
            .icon(TagPrefix.dust, GTMaterials.Uranium235)
            .task(Task.item(TagPrefix.dust, GTMaterials.Uranium235, 3), Task.item(TagPrefix.dust, GTMaterials.Uranium238, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Uranium238, 16)
            .dependsOn(ENRICHED_URANIUM_HEXAFLUORIDE));

    public static final Quest EMPTY_FUEL_ROD = CHAPTER.quest("empty_fuel_rod", q -> q
            .at(20, 6)
            .title("空燃料棒")
            .icon(GTOItems.REACTOR_FUEL_ROD)
            .task(Task.item(GTOItems.REACTOR_FUEL_ROD, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.SteelMagnetic, 4)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest URANIUM_FUEL_ROD = CHAPTER.quest("uranium_fuel_rod", q -> q
            .at(20, 8)
            .title("铀燃料棒")
            .icon(GTOItems.REACTOR_URANIUM_SIMPLE)
            .task(Task.item(GTOItems.REACTOR_URANIUM_SIMPLE, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Uranium238, 16)
            .dependsOn(URANIUM_235, EMPTY_FUEL_ROD));

    public static final Quest SPENT_FUEL = CHAPTER.quest("spent_fuel", q -> q
            .at(18, 8)
            .title("乏燃料")
            .icon(GTOItems.DEPLETED_REACTOR_URANIUM_SIMPLE)
            .task(Task.item(GTOItems.DEPLETED_REACTOR_URANIUM_SIMPLE))
            .scaledReward(TagPrefix.dust, GTMaterials.Uranium238, 16)
            .dependsOn(URANIUM_FUEL_ROD, FISSION_REACTOR));

    public static final Quest PLUTONIUM = CHAPTER.quest("plutonium", q -> q
            .at(18, 6)
            .title("钚239")
            .icon(TagPrefix.ingot, GTMaterials.Plutonium239)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Plutonium239, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium239, 2)
            .dependsOn(SPENT_FUEL));

    public static final QuestLink PLUTONIUM_COPY_1 = CHAPTER.copy(PLUTONIUM, -2, 20);

    public static final Quest RADON = CHAPTER.quest("radon", q -> q
            .at(18, 4)
            .title("氡")
            .icon(GTMaterials.Radon.getBucket())
            .task(Task.item(GTMaterials.Radon.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Uranium238, 4)
            .dependsOn(PLUTONIUM));

    public static final QuestLink RADON_COPY_1 = CHAPTER.copy(RADON, -10, 8);

    public static final QuestLink RADON_COPY_2 = CHAPTER.copy(RADON, 2, 16);

    public static final Quest NUCLEAR_WASTE = CHAPTER.quest("nuclear_waste", q -> q
            .at(16, 8)
            .title("核废料")
            .desc(d -> d.line("可以产出些有趣的资源，包括你下个阶段会用到的{钋}", GOLD))
            .icon(GTOItems.NUCLEAR_WASTE)
            .task(Task.item(GTOItems.NUCLEAR_WASTE, 16))
            .scaledReward(GTOItems.NUCLEAR_WASTE, 4)
            .dependsOn(SPENT_FUEL));

    public static final Quest POLONIUM = CHAPTER.quest("tiny_polonium_dust", q -> q
            .at(16, 6)
            .title("钋")
            .desc(d -> d.line("你需要搭建{裂变反应堆}，用离心{核废料}来获得一点点的钋", GOLD, GOLD))
            .icon(TagPrefix.ingot, GTMaterials.Polonium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Polonium, 13))
            .scaledReward(TagPrefix.dustTiny, GTMaterials.Polonium, 9)
            .dependsOn(NUCLEAR_WASTE));

    public static final Quest LIQUID_AIR = CHAPTER.quest("liquid_air", q -> q
            .at(-8, 8)
            .title("液态空气")
            .icon(GTMaterials.LiquidAir.getBucket())
            .task(Task.item(GTMaterials.LiquidAir.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Air.getBucket(), 16)
            .dependsOn(RADON_COPY_1));

    public static final Quest HELIUM = CHAPTER.quest("helium", q -> q
            .at(-8, 10)
            .title("氦")
            .icon(GTMaterials.Helium.getBucket())
            .task(Task.item(GTMaterials.Helium.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.LiquidAir.getBucket(), 4)
            .dependsOn(LIQUID_AIR));

    public static final Quest LIQUID_HELIUM = CHAPTER.quest("liquid_helium", q -> q
            .at(-6, 10)
            .title("液氦")
            .icon(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID).getBucket())
            .task(Task.item(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Helium.getBucket(), 4)
            .dependsOn(HELIUM));

    public static final QuestLink LIQUID_HELIUM_COPY_1 = CHAPTER.copy(LIQUID_HELIUM, 12, 26);

    public static final Quest FLUORINATED_SAMARIUM = CHAPTER.quest("fluorinated_samarium_concentrate", q -> q
            .at(16, 4)
            .title("氟化钐精")
            .icon(TagPrefix.dust, GTOMaterials.FluorinatedSamariumConcentratePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.FluorinatedSamariumConcentratePowder, 12))
            .scaledReward(TagPrefix.dust, GTOMaterials.FluorinatedSamariumConcentratePowder, 4)
            .dependsOn(RADON));

    public static final Quest SAMARIUM_TERBIUM_MIXTURE = CHAPTER.quest("samarium_terbium_mixture", q -> q
            .at(16, 2)
            .title("钐铽混合物")
            .icon(TagPrefix.dust, GTOMaterials.SamariumTerbiumMixturePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumTerbiumMixturePowder, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.Calcium, 12)
            .dependsOn(FLUORINATED_SAMARIUM));

    public static final Quest NITRIDED_SAMARIUM_TERBIUM = CHAPTER.quest("nitrided_samarium_terbium", q -> q
            .at(18, 0)
            .milestone()
            .title("氮化钐铽")
            .icon(TagPrefix.dust, GTOMaterials.NitridedSamariumTerbiumMixturePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.NitridedSamariumTerbiumMixturePowder, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.AmmoniumChloride, 27)
            .dependsOn(SAMARIUM_TERBIUM_MIXTURE));

    public static final Quest SAMARIUM_PRECIPITATE = CHAPTER.quest("samarium_precipitate", q -> q
            .at(18, 2)
            .title("钐沉淀")
            .icon(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.Copper, 3)
            .dependsOn(NITRIDED_SAMARIUM_TERBIUM));

    public static final Quest SAMARIUM = CHAPTER.quest("samarium", q -> q
            .at(20, 2)
            .title("钐")
            .icon(TagPrefix.dust, GTMaterials.Samarium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Samarium, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder, 3)
            .dependsOn(SAMARIUM_PRECIPITATE));

    public static final QuestLink SAMARIUM_COPY_1 = CHAPTER.copy(SAMARIUM, 14, 28);

    public static final Quest HSSG_INGOT = CHAPTER.quest("hssg_ingot", q -> q
            .at(2, 18)
            .title("HSS-G 锭")
            .icon(TagPrefix.ingot, GTMaterials.HSSG)
            .task(Task.item(TagPrefix.ingot, GTMaterials.HSSG, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.HSSG, 9)
            .dependsOn(RADON_COPY_2));

    public static final QuestLink HSSG_INGOT_COPY_1 = CHAPTER.copy(HSSG_INGOT, 12, 24);

    public static final Quest HSSG_COIL = CHAPTER.quest("hssg_coil", q -> q
            .at(14, 24)
            .title("HSS-G 线圈")
            .icon(GTBlocks.COIL_HSSG)
            .task(Task.item(GTBlocks.COIL_HSSG, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.TungstenCarbide, 32)
            .dependsOn(HSSG_INGOT_COPY_1));

    public static final Quest SAMARIUM_IRON_ARSENIC_OXIDE = CHAPTER.quest("samarium_iron_arsenic_oxide", q -> q
            .at(14, 26)
            .title("钐铁砷氧化物")
            .icon(TagPrefix.ingot, GTMaterials.SamariumIronArsenicOxide)
            .task(Task.item(TagPrefix.ingot, GTMaterials.SamariumIronArsenicOxide, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.SamariumIronArsenicOxide, 4)
            .dependsOn(SAMARIUM_COPY_1, LIQUID_HELIUM_COPY_1, HSSG_COIL));

    public static final Quest SAMARIUM_IRON_ARSENIC_OXIDE_WIRE = CHAPTER.quest("samarium_iron_arsenic_oxide_wire", q -> q
            .at(16, 26)
            .title("1x 钐铁砷氧化物导线")
            .icon(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide)
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide, 16))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide, 4)
            .dependsOn(SAMARIUM_IRON_ARSENIC_OXIDE));

    public static final Quest URANIUM_TRIPLATINUM_WIRE = CHAPTER.quest("uranium_triplatinum_wire", q -> q
            .at(6, 4)
            .title("1x 铀三铂导线")
            .icon(TagPrefix.wireGtSingle, GTMaterials.UraniumTriplatinum)
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.UraniumTriplatinum, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.UraniumTriplatinum, 4)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest EV_FIELD_GENERATOR = CHAPTER.quest("ev_field_generator", q -> q
            .at(6, 2)
            .title("EV 力场发生器")
            .icon(GTItems.FIELD_GENERATOR_EV)
            .task(Task.item(GTItems.FIELD_GENERATOR_EV, 5))
            .scaledReward(GTItems.EMITTER_EV, 1)
            .dependsOn(NewEvChapter.EV_EMITTER_SENSOR, URANIUM_TRIPLATINUM_WIRE));

    public static final QuestLink EV_FIELD_GENERATOR_COPY_1 = CHAPTER.copy(EV_FIELD_GENERATOR, 36, -4);

    public static final Quest IV_FIELD_GENERATOR = CHAPTER.quest("iv_field_generator", q -> q
            .at(0, 24)
            .title("IV 力场发生器")
            .icon(GTItems.FIELD_GENERATOR_IV)
            .task(Task.item(GTItems.FIELD_GENERATOR_IV, 5))
            .scaledReward(GTItems.QUANTUM_STAR, 1)
            .dependsOn(NewIvChapter.IV_EMITTER_SENSOR));

    public static final Quest PHTHALIC_ACID = CHAPTER.quest("phthalic_acid", q -> q
            .at(8, 2)
            .title("邻苯二甲酸")
            .icon(GTMaterials.PhthalicAcid.getBucket())
            .task(Task.item(GTMaterials.PhthalicAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.PotassiumDichromate, 2)
            .dependsOn(EV_FIELD_GENERATOR));

    public static final Quest PHTHALIC_ANHYDRIDE = CHAPTER.quest("phthalic_anhydride", q -> q
            .at(8, 4)
            .title("邻苯二甲酸酐")
            .icon(TagPrefix.dust, GTOMaterials.PhthalicAnhydride)
            .task(Task.item(TagPrefix.dust, GTOMaterials.PhthalicAnhydride, 15))
            .scaledReward(GTMaterials.PhthalicAcid.getBucket(), 1)
            .dependsOn(PHTHALIC_ACID));

    public static final Quest ETHYLANTHRAQUINONE = CHAPTER.quest("ethylanthraquinone", q -> q
            .at(10, 6)
            .title("乙基蒽醌")
            .icon(GTOMaterials.Ethylanthraquinone.getBucket())
            .task(Task.item(GTOMaterials.Ethylanthraquinone.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.PhthalicAnhydride, 15)
            .dependsOn(PHTHALIC_ANHYDRIDE));

    public static final Quest HYDROGEN_PEROXIDE = CHAPTER.quest("hydrogen_peroxide", q -> q
            .at(12, 6)
            .title("过氧化氢")
            .icon(GTMaterials.HydrogenPeroxide.getBucket())
            .task(Task.item(GTMaterials.HydrogenPeroxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Ethylanthraquinone.getBucket(), 1)
            .dependsOn(ETHYLANTHRAQUINONE));

    public static final QuestLink HYDROGEN_PEROXIDE_COPY_1 = CHAPTER.copy(HYDROGEN_PEROXIDE, 6, 8);

    public static final Quest HYDRAZINE = CHAPTER.quest("hydrazine", q -> q
            .at(10, 4)
            .title("肼")
            .icon(GTOMaterials.Hydrazine.getBucket())
            .task(Task.item(GTOMaterials.Hydrazine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 4)
            .dependsOn(HYDROGEN_PEROXIDE));

    public static final Quest DENSE_HYDRAZINE = CHAPTER.quest("dense_hydrazine_fuel", q -> q
            .at(12, 4)
            .title("浓缩肼燃料")
            .icon(GTOMaterials.DenseHydrazineFuelMixture.getBucket())
            .task(Task.item(GTOMaterials.DenseHydrazineFuelMixture.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Methanol.getBucket(), 4)
            .dependsOn(HYDRAZINE));

    public static final Quest HEAVY_DUTY_PLATE_3 = CHAPTER.quest("heavy_duty_plate_3", q -> q
            .at(14, 6)
            .title("三阶重型板")
            .icon(GTOItems.HEAVY_DUTY_PLATE_3)
            .task(Task.item(GTOItems.HEAVY_DUTY_PLATE_3, 12))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_2, 4)
            .dependsOn(HYDROGEN_PEROXIDE));

    public static final Quest TIER_3_ROCKET = CHAPTER.quest("tier_3_rocket", q -> q
            .at(14, 4)
            .title("T3 火箭")
            .desc(d -> d.line("别忘了穿上{T2}器官", AQUA))
            .icon(ModItems.TIER_3_ROCKET.get())
            .task(Task.item(ModItems.TIER_3_ROCKET.get()))
            .scaledReward(ModItems.STEEL_ENGINE.get(), 2)
            .dependsOn(HEAVY_DUTY_PLATE_3));

    public static final Quest UNLOCK_VENUS = CHAPTER.quest("unlock_venus", q -> q
            .at(14, 2)
            .title("解锁金星")
            .icon(ModItems.VENUS_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.DenseHydrazineFuelMixture.getBucket(), 4)
            .dependsOn(DENSE_HYDRAZINE, TIER_3_ROCKET));

    public static final Quest REINFORCED_ORGANS = CHAPTER.quest("reinforced_organs", q -> q
            .at(12, 2)
            .title("强化级器官")
            .task(
                    Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.SPINE, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LUNG, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LIVER, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, OrganTier.REINFORCED)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, OrganTier.REINFORCED)))
            .scaledReward(GTItems.ROBOT_ARM_EV, 2)
            .dependsOn(DENSE_HYDRAZINE));

    public static final Quest VENUS = CHAPTER.quest("venus", q -> q
            .at(14, 0)
            .milestone()
            .title("登陆金星")
            .desc(d -> d
                    .line("金星的环境非常恶劣，其地表温度过高，你需要抗火能力")
                    .line()
                    .line("HV阶段解锁的{太空纳米胸甲}免费提供了该能力", GOLD)
                    .line()
                    .line("前往{金星}以完成此任务", GOLD))
            .icon(ModItems.VENUS_GLOBE.get())
            .task(Task.dimension(Dimension.VENUS).title("造访金星").icon(ModItems.VENUS_GLOBE.get()))
            .scaledReward(ModItems.RAW_DESH.get(), 16)
            .dependsOn(UNLOCK_VENUS, REINFORCED_ORGANS));

    public static final Quest IV_SCANNER = CHAPTER.quest("iv_scanner", q -> q
            .at(4, -4)
            .title("IV 扫描仪")
            .desc(d -> d
                    .line("{前往}{科技树}{章节组来查看全新的研究线！}", YELLOW, link(TechtreeChapter.IV_SCANNER), YELLOW)
                    .line()
                    .line("装配线在合成物品时，必须要先经过{研究}", YELLOW)
                    .line()
                    .line("你需要先在{数据导出机}中将配方数据导出到闪存，然后把闪存放入{数据访问仓}中，才能让装配线解锁配方", GOLD, GOLD)
                    .line()
                    .line("在此之后，你还会遇到更加强大的数据载体和研究仪器"))
            .icon(GTMachines.SCANNER[GTValues.IV])
            .task(Task.item(GTMachines.SCANNER[GTValues.IV]))
            .scaledReward(GTItems.EMITTER_IV, 1)
            .dependsOn(NewIvChapter.NANO_MAINFRAME));

    public static final QuestLink IV_SCANNER_COPY_1 = CHAPTER.copy(IV_SCANNER, 20, -2);

    public static final QuestLink IV_SCANNER_COPY_2 = CHAPTER.copy(IV_SCANNER, -6, 26);

    public static final QuestLink IV_SCANNER_COPY_3 = CHAPTER.copy(IV_SCANNER, -6, 24);

    public static final Quest DATA_CRYSTAL = CHAPTER.quest("data_crystal", q -> q
            .at(6, -6)
            .title("数据晶片")
            .icon(GTOItems.DATA_CRYSTAL_MK1)
            .task(Task.item(GTOItems.DATA_CRYSTAL_MK1))
            .scaledReward(GTItems.NOR_MEMORY_CHIP, 16)
            .dependsOn(NewEvChapter.ADVANCED_CIRCUIT_BOARD));

    public static final QuestLink DATA_CRYSTAL_COPY_1 = CHAPTER.copy(DATA_CRYSTAL, 22, -4);

    public static final QuestLink DATA_CRYSTAL_COPY_2 = CHAPTER.copy(DATA_CRYSTAL, -4, 28);

    public static final QuestLink DATA_CRYSTAL_COPY_3 = CHAPTER.copy(DATA_CRYSTAL, -4, 22);

    public static final Quest UNLOCK_TECH_NODE = CHAPTER.quest("unlock_tech_node", q -> q
            .at(6, -4)
            .title("解锁科技节点")
            .icon(GTItems.FIELD_GENERATOR_IV)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineluv))
            .scaledReward(GTItems.QUANTUM_STAR, 1)
            .dependsOn(IV_SCANNER, DATA_CRYSTAL));

    public static final Quest CALCULATION_PRESS = CHAPTER.quest("calculation_processor_press", q -> q
            .at(8, -4)
            .title("计算处理器压印模板")
            .icon(AEItems.CALCULATION_PROCESSOR_PRESS)
            .task(Task.item(AEItems.CALCULATION_PROCESSOR_PRESS))
            .scaledReward(AEItems.CALCULATION_PROCESSOR, 4)
            .dependsOn(UNLOCK_TECH_NODE));

    public static final Quest DATA_EXPORT_MACHINE = CHAPTER.quest("data_export_machine", q -> q
            .at(8, -2)
            .title("数据导出机")
            .icon(GTOMachines.DATA_EXPORT_MACHINE)
            .task(Task.item(GTOMachines.DATA_EXPORT_MACHINE))
            .scaledReward(GTItems.TOOL_DATA_STICK, 2)
            .dependsOn(UNLOCK_TECH_NODE, CALCULATION_PRESS));

    public static final Quest ASSEMBLY_LINE_CASINGS = CHAPTER.quest("assembly_line_casings", q -> q
            .at(6, -2)
            .title("装配线外壳")
            .icon(GTBlocks.CASING_ASSEMBLY_CONTROL)
            .task(Task.item(GTBlocks.CASING_ASSEMBLY_CONTROL, 5), Task.item(GTBlocks.CASING_ASSEMBLY_LINE, 5), Task.item(GTBlocks.CASING_GRATE, 5))
            .scaledReward(GTItems.ROBOT_ARM_IV, 2)
            .dependsOn(UNLOCK_TECH_NODE));

    public static final Quest DATA_ACCESS_HATCH = CHAPTER.quest("data_access_hatch", q -> q
            .at(10, -2)
            .title("数据访问仓")
            .desc(d -> d
                    .line("将{数据载体}——目前是闪存——放入{数据访问仓}，即可在对应装配线解锁配方", GOLD, GOLD)
                    .line()
                    .line("目前一个装配线只能同时解锁{9个}配方，后续你可以制造{数据库}同时解锁更多配方！", GREEN, GOLD)
                    .line()
                    .line("同样的，你之后会遇到更加强大的数据访问仓"))
            .icon(GTResearchMachines.DATA_ACCESS_HATCH)
            .task(Task.item(GTResearchMachines.DATA_ACCESS_HATCH))
            .scaledReward(GTItems.TOOL_DATA_STICK, 4)
            .dependsOn(DATA_EXPORT_MACHINE));

    public static final Quest ASSEMBLY_LINE = CHAPTER.quest("assembly_line", q -> q
            .at(8, 0)
            .milestone()
            .title("装配线")
            .subtitle("新的组装方式")
            .desc(d -> d
                    .line("装配线最长17格，你可以切换EMI多方块结构中的P:0，P:1来查看最小和最大的装配线")
                    .line()
                    .line("装配线需要{有序}输入物品，你必须按照配方顺序给每个格子填充原料，离装配线主机{最近}的{输入总线}为配方中第一个材料", YELLOW, AQUA, GOLD)
                    .line()
                    .line("{流体无需有序}", GRAY.italic()))
            .icon(GTMultiMachines.ASSEMBLY_LINE)
            .task(Task.item(GTMultiMachines.ASSEMBLY_LINE))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 4)
            .dependsOn(DATA_EXPORT_MACHINE, ASSEMBLY_LINE_CASINGS, DATA_ACCESS_HATCH));

    public static final Quest SAMARIUM_INGOT = CHAPTER.quest("samarium_ingot", q -> q
            .at(20, 4)
            .title("钐锭")
            .icon(TagPrefix.ingot, GTMaterials.Samarium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Samarium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Samarium, 2)
            .dependsOn(SAMARIUM));

    public static final Quest SAMARIUM_MAGNETIC = CHAPTER.quest("samarium_magnetic", q -> q
            .at(22, 4)
            .title("磁化钐")
            .icon(TagPrefix.rodLong, GTMaterials.SamariumMagnetic)
            .task(Task.item(TagPrefix.rodLong, GTMaterials.SamariumMagnetic, 4))
            .scaledReward(TagPrefix.rod, GTMaterials.SamariumMagnetic, 2)
            .dependsOn(SAMARIUM_INGOT));

    public static final Quest LUV_MOTOR = CHAPTER.quest("luv_electric_motor", q -> q
            .at(22, 6)
            .title("LuV 马达")
            .desc(d -> d
                    .line("这个阶段开始的部件需要装配线制作，并且需要解锁对应的科技节点")
                    .line()
                    .line("{前往}{科技树}{章节组来查看全新的研究线！}", YELLOW, link(TechtreeChapter.IV_SCANNER), YELLOW))
            .icon(GTItems.ELECTRIC_MOTOR_LuV)
            .task(Task.item(GTItems.ELECTRIC_MOTOR_LuV, 8))
            .scaledReward(TagPrefix.rodLong, GTMaterials.SamariumMagnetic, 2)
            .dependsOn(ASSEMBLY_LINE, SAMARIUM_MAGNETIC));

    public static final Quest LUV_PARTS = CHAPTER.quest("luv_parts", q -> q
            .at(24, 6)
            .title("LuV 部件")
            .icon(GTItems.ROBOT_ARM_LuV)
            .task(Task.item(GTItems.ELECTRIC_PISTON_LuV, 2), Task.item(GTItems.ROBOT_ARM_LuV, 2), Task.item(GTItems.CONVEYOR_MODULE_LuV, 2),
                    Task.item(GTItems.ELECTRIC_PUMP_LuV), Task.item(GTItems.EMITTER_LuV), Task.item(GTItems.SENSOR_LuV))
            .scaledReward(GTItems.ELECTRIC_MOTOR_LuV, 1)
            .dependsOn(LUV_MOTOR));

    public static final QuestLink LUV_PARTS_COPY_1 = CHAPTER.copy(LUV_PARTS, 22, 20);

    public static final Quest LUV_ASSEMBLER = CHAPTER.quest("luv_assembler", q -> q
            .at(26, 4)
            .title("LuV 组装机")
            .icon(GTMachines.ASSEMBLER[GTValues.LuV])
            .task(Task.item(GTMachines.ASSEMBLER[GTValues.LuV]))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 2)
            .dependsOn(LUV_PARTS));

    public static final Quest TACTICAL_ORGANS = CHAPTER.quest("tactical_organs", q -> q
            .at(24, 4)
            .title("战术级器官")
            .task(
                    Task.item(GTOOrganItems.tierOrgan(OrganType.EYE, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.SPINE, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LUNG, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LIVER, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.HEART, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_ARM, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_ARM, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.LEFT_LEG, OrganTier.TACTICAL)),
                    Task.item(GTOOrganItems.tierOrgan(OrganType.RIGHT_LEG, OrganTier.TACTICAL)))
            .scaledReward(GTItems.ELECTRIC_MOTOR_LuV, 2)
            .dependsOn(LUV_ASSEMBLER));

    public static final QuestLink TACTICAL_ORGANS_COPY_1 = CHAPTER.copy(TACTICAL_ORGANS, 18, -6);

    public static final Quest DESH = CHAPTER.quest("desh_ingot", q -> q
            .at(20, -6)
            .title("Desh 锭")
            .desc(d -> d.line("为什么会需要铑才能制作？{也许这就是恶趣味}", GRAY.italic().strike()))
            .icon(ModItems.DESH_INGOT.get())
            .task(Task.item(ModItems.DESH_INGOT.get(), 64))
            .scaledReward(TagPrefix.dust, GTOMaterials.Desh, 16)
            .dependsOn(TACTICAL_ORGANS_COPY_1));

    public static final Quest DENSE_DESH = CHAPTER.quest("dense_desh_plate", q -> q
            .at(22, -6)
            .title("致密 Desh 板")
            .icon(TagPrefix.plateDense, GTOMaterials.Desh)
            .task(Task.item(TagPrefix.plateDense, GTOMaterials.Desh, 16))
            .scaledReward(ModItems.DESH_INGOT.get(), 9)
            .dependsOn(DESH));

    public static final Quest DESH_ENGINE = CHAPTER.quest("desh_engine_and_tank", q -> q
            .at(24, -6)
            .title("Desh 引擎与储罐")
            .icon(ModItems.DESH_ENGINE.get())
            .task(Task.item(ModItems.DESH_ENGINE.get(), 8), Task.item(ModItems.DESH_TANK.get(), 8))
            .scaledReward(ModItems.STEEL_ENGINE.get(), 2)
            .dependsOn(DENSE_DESH));

    public static final Quest TIER_4_ROCKET = CHAPTER.quest("tier_4_rocket", q -> q
            .at(26, -4)
            .title("T4 火箭")
            .desc(d -> d.line("四阶火箭的强大推进力解锁了一颗小行星带的矮行星——谷神星"))
            .icon(ModItems.TIER_4_ROCKET.get())
            .task(Task.item(ModItems.TIER_4_ROCKET.get()))
            .scaledReward(GTItems.FIELD_GENERATOR_EV, 1)
            .dependsOn(DESH_ENGINE));

    public static final Quest MONOMETHYLHYDRAZINE = CHAPTER.quest("monomethylhydrazine", q -> q
            .at(24, -4)
            .title("甲肼")
            .icon(GTOMaterials.Monomethylhydrazine.getBucket())
            .task(Task.item(GTOMaterials.Monomethylhydrazine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Hydrazine.getBucket(), 2)
            .dependsOn(DESH_ENGINE));

    public static final Quest ROCKET_FUEL_CN3H7O3 = CHAPTER.quest("rocket_fuel_cn3h7o3", q -> q
            .at(24, -2)
            .title("火箭燃料 CN3H7O3")
            .icon(GTOMaterials.RocketFuelCn3h7o3.getBucket())
            .task(Task.item(GTOMaterials.RocketFuelCn3h7o3.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Monomethylhydrazine.getBucket(), 4)
            .dependsOn(MONOMETHYLHYDRAZINE));

    public static final Quest UNLOCK_CERES = CHAPTER.quest("unlock_ceres", q -> q
            .at(26, -2)
            .title("解锁谷神星")
            .icon(GTOGlobes.Items.CERES_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelCn3h7o3.getBucket(), 4)
            .dependsOn(TIER_4_ROCKET, ROCKET_FUEL_CN3H7O3));

    public static final Quest CERES = CHAPTER.quest("ceres", q -> q
            .at(26, 0)
            .milestone()
            .title("登陆谷神星")
            .desc(d -> d.line("前往{谷神星}以完成此任务", GOLD))
            .icon(GTOGlobes.Items.CERES_GLOBE.get())
            .task(Task.dimension(Dimension.CERES).title("造访谷神星").icon(GTOGlobes.Items.CERES_GLOBE.get()))
            .scaledReward(ModItems.RAW_OSTRUM.get(), 16)
            .dependsOn(UNLOCK_CERES));

    public static final Quest NITROCHLOROBENZENE = CHAPTER.quest("nitrochlorobenzene", q -> q
            .at(28, -2)
            .title("硝基氯苯")
            .icon(GTMaterials.Nitrochlorobenzene.getBucket())
            .task(Task.item(GTMaterials.Nitrochlorobenzene.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Benzene.getBucket(), 2)
            .dependsOn(CERES));

    public static final Quest DICHLOROBENZIDINE = CHAPTER.quest("dichlorobenzidine", q -> q
            .at(30, -2)
            .title("二氯联苯胺")
            .icon(GTMaterials.Dichlorobenzidine.getBucket())
            .task(Task.item(GTMaterials.Dichlorobenzidine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Nitrochlorobenzene.getBucket(), 1)
            .dependsOn(NITROCHLOROBENZENE));

    public static final Quest DIAMINOBENZIDINE = CHAPTER.quest("diaminobenzidine", q -> q
            .at(30, 0)
            .title("二氨基联苯胺")
            .icon(GTMaterials.Diaminobenzidine.getBucket())
            .task(Task.item(GTMaterials.Diaminobenzidine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 2)
            .dependsOn(DICHLOROBENZIDINE));

    public static final Quest POLYMERIZATION_REACTOR = CHAPTER.quest("polymerization_reactor", q -> q
            .at(28, 0)
            .milestone()
            .title("聚合反应器")
            .task(Task.item(MultiBlockG.POLYMERIZATION_REACTOR))
            .scaledReward(GTMaterials.Polybenzimidazole.getBucket(), 1)
            .dependsOn(DIAMINOBENZIDINE));

    public static final Quest POLYBENZIMIDAZOLE = CHAPTER.quest("polybenzimidazole", q -> q
            .at(30, 6)
            .title("聚苯并咪唑")
            .icon(GTMaterials.Polybenzimidazole.getBucket())
            .task(Task.item(GTMaterials.Polybenzimidazole.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Polybenzimidazole.getBucket(), 1)
            .dependsOn(POLYMERIZATION_REACTOR));

    public static final QuestLink POLYBENZIMIDAZOLE_COPY_1 = CHAPTER.copy(POLYBENZIMIDAZOLE, 24, 18);

    public static final QuestLink POLYBENZIMIDAZOLE_COPY_2 = CHAPTER.copy(POLYBENZIMIDAZOLE, 0, 10);

    public static final Quest OSTRUM = CHAPTER.quest("ostrum_ingot", q -> q
            .at(26, 20)
            .title("紫金锭")
            .desc(d -> d.line("你需要用紫金制作下一阶火箭"))
            .icon(ModItems.OSTRUM_INGOT.get())
            .task(Task.item(ModItems.OSTRUM_INGOT.get(), 64), Task.item(TagPrefix.plateDense, GTOMaterials.Ostrum, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.Ostrum, 16)
            .dependsOn(CERES));

    public static final Quest OSMIUM_OXIDE = CHAPTER.quest("osmium_oxide", q -> q
            .at(36, 0)
            .title("氧化锇")
            .icon(TagPrefix.dust, GTOMaterials.OsmiumOxideMetal)
            .task(Task.item(TagPrefix.dust, GTOMaterials.OsmiumOxideMetal, 14))
            .scaledReward(GTMaterials.AcidicOsmiumSolution.getBucket(), 2)
            .dependsOn(EV_FIELD_GENERATOR_COPY_1));

    public static final Quest OSMIUM_TETROXIDE = CHAPTER.quest("osmium_tetroxide", q -> q
            .at(36, 4)
            .title("四氧化锇")
            .icon(TagPrefix.dust, GTMaterials.OsmiumTetroxide)
            .task(Task.item(TagPrefix.dust, GTMaterials.OsmiumTetroxide, 8))
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 2)
            .dependsOn(OSMIUM_OXIDE));

    public static final Quest OSMIUM = CHAPTER.quest("osmium", q -> q
            .at(34, 6)
            .title("锇")
            .desc(d -> d.line("下个阶段的重要材料"))
            .icon(TagPrefix.dust, GTMaterials.Osmium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Osmium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Iridium, 3)
            .dependsOn(OSMIUM_TETROXIDE));

    public static final Quest HSSS_AND_HSSE = CHAPTER.quest("hsss_and_hsse", q -> q
            .at(32, 8)
            .title("HSS-S 与 HSS-E")
            .icon(TagPrefix.ingot, GTMaterials.HSSS)
            .task(Task.item(TagPrefix.ingot, GTMaterials.HSSS, 16), Task.item(TagPrefix.ingot, GTMaterials.HSSE, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.HSSG, 6)
            .dependsOn(OSMIUM));

    public static final Quest OSMIRIDIUM = CHAPTER.quest("osmiridium", q -> q
            .at(24, 2)
            .title("锇铱")
            .icon(TagPrefix.ingot, GTMaterials.Osmiridium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Osmiridium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Osmium, 1)
            .dependsOn(TACTICAL_ORGANS));

    public static final Quest LUV_VOLTAGE_COIL = CHAPTER.quest("luv_voltage_coil", q -> q
            .at(22, 2)
            .title("LuV 电压线圈")
            .icon(GTItems.VOLTAGE_COIL_LuV)
            .task(Task.item(GTItems.VOLTAGE_COIL_LuV, 2))
            .scaledReward(TagPrefix.wireFine, GTMaterials.Osmiridium, 16)
            .dependsOn(OSMIRIDIUM));

    public static final Quest TECH_ENERGY_IO_LUV = CHAPTER.quest("tech_energy_io_luv", q -> q
            .at(22, -2)
            .title("高压能量输入输出I研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.LuV]))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(IV_SCANNER_COPY_1, DATA_CRYSTAL_COPY_1));

    public static final Quest LUV_ENERGY_HATCH = CHAPTER.quest("luv_energy_input_hatch", q -> q
            .at(22, 0)
            .milestone()
            .title("LuV 能源仓")
            .desc(d -> d
                    .line("这个阶段的升压很容易")
                    .line()
                    .line("{不过这也意味着有些事情变难了……}", GRAY))
            .icon(GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV])
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV], 2))
            .scaledReward(TagPrefix.cableGtDouble, GTMaterials.NiobiumTitanium, 2)
            .dependsOn(LUV_VOLTAGE_COIL, TECH_ENERGY_IO_LUV));

    public static final Quest OSTRUM_ENGINE = CHAPTER.quest("ostrum_engine_and_tank", q -> q
            .at(26, 18)
            .title("紫金引擎与储罐")
            .icon(ModItems.OSTRUM_ENGINE.get())
            .task(Task.item(ModItems.OSTRUM_ENGINE.get(), 8), Task.item(ModItems.OSTRUM_TANK.get(), 8))
            .scaledReward(ModItems.STEEL_ENGINE.get(), 2)
            .dependsOn(OSTRUM));

    public static final Quest TIER_5_ROCKET = CHAPTER.quest("tier_5_rocket", q -> q
            .at(24, 16)
            .title("T5 火箭")
            .desc(d -> d
                    .line("你现在能够前往T5星球")
                    .line()
                    .line("别忘了穿上{T3}器官，它需要你使用{装配线}制作{LuV部件}来合成", AQUA, GOLD, YELLOW))
            .icon(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_5_ROCKET.get())
            .task(Task.item(net.celsiusqc.ad_astra_rocketed.common.registry.ModItems.TIER_5_ROCKET.get()))
            .scaledReward(GTItems.FIELD_GENERATOR_IV, 1)
            .dependsOn(OSTRUM_ENGINE, POLYBENZIMIDAZOLE_COPY_1));

    public static final Quest DINITROGEN_TETROXIDE = CHAPTER.quest("dinitrogen_tetroxide", q -> q
            .at(26, 16)
            .title("四氧化二氮")
            .icon(GTMaterials.DinitrogenTetroxide.getBucket())
            .task(Task.item(GTMaterials.DinitrogenTetroxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 2)
            .dependsOn(OSTRUM_ENGINE));

    public static final Quest ROCKET_FUEL_H8N4C2O4 = CHAPTER.quest("rocket_fuel_h8n4c2o4", q -> q
            .at(26, 14)
            .title("火箭燃料 H8N4C2O4")
            .icon(GTOMaterials.RocketFuelH8n4c2o4.getBucket())
            .task(Task.item(GTOMaterials.RocketFuelH8n4c2o4.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.DinitrogenTetroxide.getBucket(), 4)
            .dependsOn(DINITROGEN_TETROXIDE));

    public static final Quest UNLOCK_IO = CHAPTER.quest("unlock_io", q -> q
            .at(24, 14)
            .title("解锁木卫一")
            .icon(GTOGlobes.Items.IO_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelH8n4c2o4.getBucket(), 4)
            .dependsOn(TIER_5_ROCKET, ROCKET_FUEL_H8N4C2O4));

    public static final Quest IO = CHAPTER.quest("io", q -> q
            .at(24, 12)
            .milestone()
            .title("登陆木卫一")
            .desc(d -> d.line("别忘了穿上{T3}器官", AQUA))
            .icon(GTOGlobes.Items.IO_GLOBE.get())
            .task(Task.dimension(Dimension.IO).title("造访木卫一").icon(GTOGlobes.Items.IO_GLOBE.get()))
            .scaledReward(TagPrefix.rawOre, GTMaterials.Naquadah, 16)
            .dependsOn(UNLOCK_IO));

    public static final Quest NAQUADAH_OXIDE_MIXTURE = CHAPTER.quest("naquadah_oxide_mixture", q -> q
            .at(24, 10)
            .title("氧化硅岩混合物")
            .icon(TagPrefix.dust, GTOMaterials.NaquadahOxideMixture)
            .task(Task.item(TagPrefix.dust, GTOMaterials.NaquadahOxideMixture, 30))
            .scaledReward(TagPrefix.rawOre, GTMaterials.Naquadah, 8)
            .dependsOn(IO));

    public static final Quest ANTIMONY_TRIFLUORIDE = CHAPTER.quest("antimony_trifluoride", q -> q
            .at(26, 10)
            .title("三氟化锑")
            .icon(TagPrefix.dust, GTMaterials.AntimonyTrifluoride)
            .task(Task.item(TagPrefix.dust, GTMaterials.AntimonyTrifluoride, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Antimony, 4)
            .dependsOn(IO));

    public static final Quest FLUOROANTIMONIC_ACID = CHAPTER.quest("fluoroantimonic_acid", q -> q
            .at(26, 8)
            .title("氟锑酸")
            .icon(GTMaterials.FluoroantimonicAcid.getBucket())
            .task(Task.item(GTMaterials.FluoroantimonicAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrofluoricAcid.getBucket(), 4)
            .dependsOn(ANTIMONY_TRIFLUORIDE));

    public static final Quest LOW_PURITY_NAQUADAH_EMULSION = CHAPTER.quest("low_purity_naquadah_emulsion", q -> q
            .at(24, 8)
            .title("低纯硅岩乳液")
            .icon(GTOMaterials.LowPurityNaquadahEmulsion.getBucket())
            .task(Task.item(GTOMaterials.LowPurityNaquadahEmulsion.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.FluoroantimonicAcid.getBucket(), 2)
            .dependsOn(NAQUADAH_OXIDE_MIXTURE, FLUOROANTIMONIC_ACID));

    public static final QuestLink LOW_PURITY_NAQUADAH_EMULSION_COPY_1 = CHAPTER.copy(LOW_PURITY_NAQUADAH_EMULSION, 18, 18);

    public static final Quest NAQUADAH_DUST = CHAPTER.quest("naquadah_dust", q -> q
            .at(18, 16)
            .title("硅岩粉")
            .icon(TagPrefix.dust, GTMaterials.Naquadah)
            .task(Task.item(TagPrefix.dust, GTMaterials.Naquadah, 24))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 6)
            .dependsOn(LOW_PURITY_NAQUADAH_EMULSION_COPY_1));

    public static final Quest NAQUADAH_INGOT = CHAPTER.quest("naquadah_ingot", q -> q
            .at(20, 16)
            .title("硅岩锭")
            .desc(d -> d.line("搅拌低纯硅岩乳液"))
            .icon(TagPrefix.ingot, GTMaterials.Naquadah)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Naquadah, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadah, 4)
            .dependsOn(NAQUADAH_DUST));

    public static final QuestLink NAQUADAH_INGOT_COPY_1 = CHAPTER.copy(NAQUADAH_INGOT, 0, 26);

    public static final Quest NAQUADAH_COIL = CHAPTER.quest("naquadah_coil_block", q -> q
            .at(20, 14)
            .title("硅岩线圈")
            .icon(GTBlocks.COIL_NAQUADAH)
            .task(Task.item(GTBlocks.COIL_NAQUADAH, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.Osmium, 8)
            .dependsOn(NAQUADAH_INGOT));

    public static final QuestLink NAQUADAH_COIL_COPY_1 = CHAPTER.copy(NAQUADAH_COIL, 14, 20);

    public static final QuestLink NAQUADAH_COIL_COPY_2 = CHAPTER.copy(NAQUADAH_COIL, -8, 12);

    public static final Quest ERBIUM = CHAPTER.quest("erbium", q -> q
            .at(14, 18)
            .title("铒")
            .icon(TagPrefix.dust, GTMaterials.Erbium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Erbium, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.ErbiumOxide, 5)
            .dependsOn(NAQUADAH_COIL_COPY_1));

    public static final Quest FALL_KING = CHAPTER.quest("fall_king_ingot", q -> q
            .at(14, 16)
            .title("陨王")
            .subtitle("没什么，我只是想提一下")
            .desc(d -> d
                    .line("Man! What can I say")
                    .line()
                    .line("{元素周期表真是让人着迷}", GRAY.italic()))
            .icon(TagPrefix.ingot, GTOMaterials.FallKing)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.FallKing, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Lithium, 2)
            .dependsOn(ERBIUM));

    public static final Quest THE_END = CHAPTER.quest("the_end", q -> q
            .at(18, 12)
            .milestone()
            .title("末地")
            .icon(Items.END_STONE)
            .task(Task.dimension(Dimension.THE_END).title("进入末地").icon(Items.END_STONE))
            .scaledReward(Items.ENDER_EYE, 4)
            .dependsOn(NAQUADAH_COIL));

    public static final Quest END_STEEL = CHAPTER.quest("end_steel", q -> q
            .at(18, 14)
            .title("4x 末地钢导线")
            .icon(TagPrefix.wireGtQuadruple, GTOMaterials.EndSteel)
            .task(Task.item(TagPrefix.wireGtQuadruple, GTOMaterials.EndSteel, 18))
            .scaledReward(Items.END_STONE, 16)
            .dependsOn(THE_END));

    public static final Quest MAR_M200 = CHAPTER.quest("mar_m200_steel", q -> q
            .at(16, 16)
            .title("MAR-M200 特种钢")
            .icon(TagPrefix.ingot, GTOMaterials.MarM200Steel)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.MarM200Steel, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.Cobalt, 10)
            .dependsOn(END_STEEL));

    public static final Quest PROCESS_MACHINE_CASING = CHAPTER.quest("process_machine_casing", q -> q
            .at(16, 14)
            .title("加工机械外壳")
            .icon(GTOBlocks.PROCESS_MACHINE_CASING)
            .task(Task.item(GTOBlocks.PROCESS_MACHINE_CASING, 18))
            .scaledReward(TagPrefix.plateDouble, GTOMaterials.MarM200Steel, 4)
            .dependsOn(END_STEEL, FALL_KING, MAR_M200));

    public static final QuestLink PROCESS_MACHINE_CASING_COPY_1 = CHAPTER.copy(PROCESS_MACHINE_CASING, 12, 8);

    public static final Quest MBCC_WIRE = CHAPTER.quest("mercury_barium_calcium_cuprate_wire", q -> q
            .at(12, 10)
            .title("1x 汞钡钙铜导线")
            .icon(TagPrefix.wireGtSingle, GTMaterials.MercuryBariumCalciumCuprate)
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.MercuryBariumCalciumCuprate, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.MercuryBariumCalciumCuprate, 4)
            .dependsOn(PROCESS_MACHINE_CASING_COPY_1));

    public static final Quest SPEEDING_PIPE = CHAPTER.quest("speeding_pipe", q -> q
            .at(14, 10)
            .title("高速管道")
            .icon(GTOBlocks.SPEEDING_PIPE)
            .task(Task.item(GTOBlocks.SPEEDING_PIPE, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Beryllium, 32)
            .dependsOn(MBCC_WIRE));

    public static final Quest HUI_CIRCUIT = CHAPTER.quest("hui_circuit", q -> q
            .at(14, 14)
            .title("HUI 电路")
            .icon(GTOItems.HUI_CIRCUIT_1)
            .task(Task.item(GTOItems.HUI_CIRCUIT_1, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.GarnetYellow, 16)
            .dependsOn(NewIvChapter.EXTREME_CIRCUIT_BOARD));

    public static final Quest NEUTRON_ACTIVATOR = CHAPTER.quest("neutron_activator", q -> q
            .at(14, 12)
            .milestone()
            .title("中子活化器")
            .icon(MultiBlockD.NEUTRON_ACTIVATOR)
            .task(Task.item(MultiBlockD.NEUTRON_ACTIVATOR), Task.item(GTOMachines.NEUTRON_ACCELERATOR[GTValues.LV], 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadah, 8)
            .dependsOn(PROCESS_MACHINE_CASING, SPEEDING_PIPE, HUI_CIRCUIT));

    public static final Quest IMPURE_ENRICHED_NAQUADAH = CHAPTER.quest("impure_enriched_naquadah_solution", q -> q
            .at(12, 14)
            .title("不纯富集硅岩溶液")
            .icon(GTMaterials.ImpureEnrichedNaquadahSolution.getBucket())
            .task(Task.item(GTMaterials.ImpureEnrichedNaquadahSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.LowPurityNaquadahSolution.getBucket(), 4)
            .dependsOn(NEUTRON_ACTIVATOR));

    public static final Quest HEXAFLUORIDE_ENRICHED_NAQUADAH = CHAPTER.quest("hexafluoride_enriched_naquadah_solution", q -> q
            .at(10, 16)
            .title("六氟化富集硅岩溶液")
            .icon(GTOMaterials.HexafluorideEnrichedNaquadahSolution.getBucket())
            .task(Task.item(GTOMaterials.HexafluorideEnrichedNaquadahSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Fluorine.getBucket(), 6)
            .dependsOn(IMPURE_ENRICHED_NAQUADAH));

    public static final Quest XENON = CHAPTER.quest("xenon", q -> q
            .at(10, 14)
            .title("氙")
            .icon(GTMaterials.Xenon.getBucket())
            .task(Task.item(GTMaterials.Xenon.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.EnderAir.getBucket(), 16)
            .dependsOn(IMPURE_ENRICHED_NAQUADAH));

    public static final Quest XENON_HEXAFLUORO_ENRICHED_NAQUADATE = CHAPTER.quest("xenon_hexafluoro_enriched_naquadate", q -> q
            .at(8, 14)
            .title("六氟氙酸富集硅岩")
            .icon(GTOMaterials.XenonHexafluoroEnrichedNaquadate.getBucket())
            .task(Task.item(GTOMaterials.XenonHexafluoroEnrichedNaquadate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Xenon.getBucket(), 1)
            .dependsOn(HEXAFLUORIDE_ENRICHED_NAQUADAH, XENON));

    public static final Quest BROMINE = CHAPTER.quest("bromine", q -> q
            .at(8, 16)
            .title("溴")
            .desc(d -> d.line("盐水提纯"))
            .icon(GTMaterials.Bromine.getBucket())
            .task(Task.item(GTMaterials.Bromine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(HEXAFLUORIDE_ENRICHED_NAQUADAH));

    public static final Quest BROMINE_TRIFLUORIDE = CHAPTER.quest("bromine_trifluoride", q -> q
            .at(6, 16)
            .title("三氟化溴")
            .icon(GTOMaterials.BromineTrifluoride.getBucket())
            .task(Task.item(GTOMaterials.BromineTrifluoride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Bromine.getBucket(), 1)
            .dependsOn(BROMINE));

    public static final Quest GOLD_TRIFLUORIDE = CHAPTER.quest("gold_trifluoride", q -> q
            .at(6, 14)
            .title("三氟化金")
            .icon(TagPrefix.dust, GTOMaterials.GoldTrifluoride)
            .task(Task.item(TagPrefix.dust, GTOMaterials.GoldTrifluoride, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Gold, 4)
            .dependsOn(BROMINE_TRIFLUORIDE));

    public static final Quest ENRICHED_NAQUADAH_SOLUTION = CHAPTER.quest("enriched_naquadah_solution", q -> q
            .at(8, 12)
            .milestone()
            .title("富集硅岩溶液")
            .icon(GTMaterials.EnrichedNaquadahSolution.getBucket())
            .task(Task.item(GTMaterials.EnrichedNaquadahSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.GoldTrifluoride, 4)
            .dependsOn(XENON_HEXAFLUORO_ENRICHED_NAQUADATE, GOLD_TRIFLUORIDE));

    public static final Quest ACIDIC_ENRICHED_NAQUADAH = CHAPTER.quest("acidic_enriched_naquadah_solution", q -> q
            .at(8, 10)
            .title("酸性富集硅岩溶液")
            .icon(GTMaterials.AcidicEnrichedNaquadahSolution.getBucket())
            .task(Task.item(GTMaterials.AcidicEnrichedNaquadahSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 4)
            .dependsOn(ENRICHED_NAQUADAH_SOLUTION));

    public static final Quest ENRICHED_NAQUADAH_SULFATE = CHAPTER.quest("enriched_naquadah_sulfate", q -> q
            .at(8, 8)
            .title("硫酸富集硅岩")
            .icon(TagPrefix.dust, GTMaterials.EnrichedNaquadahSulfate)
            .task(Task.item(TagPrefix.dust, GTMaterials.EnrichedNaquadahSulfate, 6))
            .scaledReward(GTMaterials.EnrichedNaquadahSolution.getBucket(), 1)
            .dependsOn(ACIDIC_ENRICHED_NAQUADAH));

    public static final Quest ENRICHED_NAQUADAH = CHAPTER.quest("enriched_naquadah_ingot", q -> q
            .at(8, 6)
            .title("富集硅岩")
            .desc(d -> d.line("电磁选矿洁净硅岩粉"))
            .icon(TagPrefix.ingot, GTMaterials.NaquadahEnriched)
            .task(Task.item(TagPrefix.ingot, GTMaterials.NaquadahEnriched, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.NaquadahEnriched, 2)
            .dependsOn(ENRICHED_NAQUADAH_SULFATE));

    public static final QuestLink ENRICHED_NAQUADAH_COPY_1 = CHAPTER.copy(ENRICHED_NAQUADAH, -6, -4);

    public static final Quest IMPURE_NAQUADRIA = CHAPTER.quest("impure_naquadria_solution", q -> q
            .at(-6, -2)
            .title("不纯超能硅岩溶液")
            .icon(GTMaterials.ImpureNaquadriaSolution.getBucket())
            .task(Task.item(GTMaterials.ImpureNaquadriaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.FluoroantimonicAcid.getBucket(), 2)
            .dependsOn(ENRICHED_NAQUADAH_COPY_1));

    public static final Quest HEXAFLUORIDE_NAQUADRIA = CHAPTER.quest("hexafluoride_naquadria_solution", q -> q
            .at(-4, 0)
            .title("六氟化超能硅岩溶液")
            .icon(GTOMaterials.HexafluorideNaquadriaSolution.getBucket())
            .task(Task.item(GTOMaterials.HexafluorideNaquadriaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Fluorine.getBucket(), 6)
            .dependsOn(IMPURE_NAQUADRIA));

    public static final Quest NAQUADRIA_SOLUTION = CHAPTER.quest("naquadria_solution", q -> q
            .at(-2, 2)
            .title("超能硅岩溶液")
            .icon(GTMaterials.NaquadriaSolution.getBucket())
            .task(Task.item(GTMaterials.NaquadriaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RadonDifluoride.getBucket(), 2)
            .dependsOn(HEXAFLUORIDE_NAQUADRIA));

    public static final Quest ACIDIC_NAQUADRIA = CHAPTER.quest("acidic_naquadria_solution", q -> q
            .at(-2, 4)
            .title("酸性超能硅岩溶液")
            .icon(GTMaterials.AcidicNaquadriaSolution.getBucket())
            .task(Task.item(GTMaterials.AcidicNaquadriaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 4)
            .dependsOn(NAQUADRIA_SOLUTION));

    public static final Quest NAQUADRIA_WASTE = CHAPTER.quest("naquadria_waste", q -> q
            .at(0, 4)
            .title("超能硅岩废液")
            .icon(GTMaterials.NaquadriaWaste.getBucket())
            .task(Task.item(GTMaterials.NaquadriaWaste.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NaquadriaSolution.getBucket(), 1)
            .dependsOn(ACIDIC_NAQUADRIA));

    public static final Quest TRINIUM_SULFIDE = CHAPTER.quest("trinium_sulfide", q -> q
            .at(2, 6)
            .title("硫化凯金")
            .icon(TagPrefix.dust, GTMaterials.TriniumSulfide)
            .task(Task.item(TagPrefix.dust, GTMaterials.TriniumSulfide, 4))
            .scaledReward(GTMaterials.NaquadriaWaste.getBucket(), 2)
            .dependsOn(NAQUADRIA_WASTE));

    public static final Quest TRINIUM = CHAPTER.quest("trinium_ingot", q -> q
            .at(2, 8)
            .title("凯金锭")
            .desc(d -> d.line("硫化凯金粉高温反应"))
            .icon(TagPrefix.ingot, GTMaterials.Trinium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Trinium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Zinc, 2)
            .dependsOn(TRINIUM_SULFIDE));

    public static final QuestLink TRINIUM_COPY_1 = CHAPTER.copy(TRINIUM, -2, 14);

    public static final Quest NAQUADAH_ALLOY_DUST = CHAPTER.quest("naquadah_alloy_dust", q -> q
            .at(2, 10)
            .title("硅岩合金粉")
            .icon(TagPrefix.dust, GTMaterials.NaquadahAlloy)
            .task(Task.item(TagPrefix.dust, GTMaterials.NaquadahAlloy, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadah, 4)
            .dependsOn(TRINIUM));

    public static final Quest NAQUADAH_ALLOY = CHAPTER.quest("naquadah_alloy_ingot", q -> q
            .at(2, 12)
            .milestone()
            .title("硅岩合金锭")
            .icon(TagPrefix.ingot, GTMaterials.NaquadahAlloy)
            .task(Task.item(TagPrefix.ingot, GTMaterials.NaquadahAlloy, 8))
            .scaledReward(TagPrefix.plate, GTMaterials.NaquadahAlloy, 2)
            .dependsOn(NAQUADAH_ALLOY_DUST, LUV_ENERGY_HATCH));

    public static final Quest ZPM_CASING = CHAPTER.quest("zpm_casing", q -> q
            .at(4, 24)
            .title("ZPM 机械方块")
            .icon(GTBlocks.MACHINE_CASING_ZPM)
            .task(Task.item(GTBlocks.MACHINE_CASING_ZPM))
            .scaledReward(TagPrefix.plate, GTMaterials.NaquadahAlloy, 2)
            .dependsOn(NAQUADAH_ALLOY));

    public static final Quest VANADIUM_GALLIUM_CABLE = CHAPTER.quest("vanadium_gallium_cable", q -> q
            .at(6, 20)
            .title("钒镓线缆")
            .icon(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium)
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.VanadiumGallium, 2)
            .dependsOn(NAQUADAH_ALLOY));

    public static final Quest ZPM_HULL = CHAPTER.quest("welcome_to_the_zero_pressure_phase", q -> q
            .at(6, 22)
            .milestone()
            .title("欢迎来到{零点压}阶段", RED)
            .subtitle("{你需要天马行空般的想象力}", RED)
            .desc(d -> d
                    .line("这一章节你将体验到一门独特的产线——{生}{物}{线}", GREEN, DARK_GREEN, GREEN)
                    .line()
                    .line("普通的{晶体处理器}已经接近无机物的极限... 或许{菌}{群}可以帮我们做些计算？", GOLD, GREEN, DARK_GREEN)
                    .line()
                    .line("在GT，所有的科技就像{魔}{法}一样", GREEN, DARK_GREEN)
                    .line()
                    .line("你的目标是制作并{增}{殖}{干细胞}", GREEN, DARK_GREEN, GOLD))
            .task(Task.item(GTMachines.HULL[GTValues.ZPM]))
            .reward(
                    Reward.item(GTMachines.HULL[GTValues.ZPM], 1),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(ZPM_CASING, VANADIUM_GALLIUM_CABLE));

    public static final QuestLink ZPM_HULL_COPY_1 = CHAPTER.copy(ZPM_HULL, 26, 24);

    public static final QuestLink ZPM_HULL_COPY_2 = CHAPTER.copy(ZPM_HULL, 32, 18);

    public static final QuestLink ZPM_HULL_COPY_3 = CHAPTER.copy(ZPM_HULL, 32, 26);

    public static final QuestLink ZPM_HULL_COPY_4 = CHAPTER.copy(ZPM_HULL, 28, 26);

    public static final QuestLink ZPM_HULL_COPY_5 = CHAPTER.copy(ZPM_HULL, 30, 18);

    public static final QuestLink ZPM_HULL_COPY_6 = CHAPTER.copy(ZPM_HULL, 36, 22);

    public static final QuestLink ZPM_HULL_COPY_7 = CHAPTER.copy(ZPM_HULL, 28, 18);

    public static final Quest QUANTUM_MAINFRAME = CHAPTER.quest("quantum_processor_mainframe", q -> q
            .at(2, 20)
            .title("量子处理器主机")
            .subtitle("第一个ZPM电路")
            .task(Task.item(GTItems.QUANTUM_MAINFRAME_ZPM, 16))
            .scaledReward(GTItems.QUANTUM_COMPUTER_LuV, 1)
            .dependsOn(NewIvChapter.QUANTUM_COMPUTER, HSSG_INGOT));

    public static final QuestLink QUANTUM_MAINFRAME_COPY_1 = CHAPTER.copy(QUANTUM_MAINFRAME, 22, 24);

    public static final Quest ITBTC_DUST = CHAPTER.quest("indium_tin_barium_titanium_cuprate_dust", q -> q
            .at(-6, 14)
            .title("铟锡钡钛铜氧化物粉")
            .icon(TagPrefix.dust, GTMaterials.IndiumTinBariumTitaniumCuprate)
            .task(Task.item(GTMachines.MIXER[GTValues.LuV]),
                    Task.item(TagPrefix.dust, GTMaterials.IndiumTinBariumTitaniumCuprate, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Indium, 8)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest ITBTC_INGOT = CHAPTER.quest("indium_tin_barium_titanium_cuprate_ingot", q -> q
            .at(-6, 12)
            .title("铟锡钡钛铜氧化物锭")
            .subtitle("LuV超导")
            .task(Task.item(TagPrefix.ingot, GTMaterials.IndiumTinBariumTitaniumCuprate, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.IndiumTinBariumTitaniumCuprate, 16)
            .dependsOn(ITBTC_DUST, NAQUADAH_COIL_COPY_2, LIQUID_HELIUM));

    public static final QuestLink ITBTC_INGOT_COPY_1 = CHAPTER.copy(ITBTC_INGOT, 28, 28);

    public static final Quest ITBTC_WIRE = CHAPTER.quest("indium_tin_barium_titanium_cuprate_wire", q -> q
            .at(-4, 12)
            .title("1x 铟锡钡钛铜氧化物导线")
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.IndiumTinBariumTitaniumCuprate, 64))
            .scaledReward(TagPrefix.ingot, GTMaterials.IndiumTinBariumTitaniumCuprate, 8)
            .dependsOn(ITBTC_INGOT));

    public static final Quest SUPERCONDUCTING_COIL = CHAPTER.quest("superconducting_coil", q -> q
            .at(-2, 12)
            .title("超导线圈方块")
            .desc(d -> d.line("很贵，在你有更高的电压等级后有更加节省凯金的配方"))
            .task(Task.item(GTBlocks.SUPERCONDUCTING_COIL, 52))
            .scaledReward(TagPrefix.ingot, GTMaterials.Trinium, 24)
            .dependsOn(ITBTC_WIRE, TRINIUM_COPY_1));

    public static final Quest FUSION_CASING = CHAPTER.quest("fusion_casing", q -> q
            .at(0, 12)
            .milestone()
            .title("聚变机械方块与聚变玻璃")
            .icon(GTBlocks.FUSION_CASING)
            .task(Task.item(GTBlocks.FUSION_CASING, 48),
                    Task.item(GTBlocks.FUSION_GLASS, 31))
            .scaledReward(GTItems.NEUTRON_REFLECTOR, 4)
            .dependsOn(SUPERCONDUCTING_COIL, NEUTRON_REFLECTOR_COPY_1, POLYBENZIMIDAZOLE_COPY_2));

    public static final Quest UHPIC_WAFER = CHAPTER.quest("uhpic_wafer", q -> q
            .at(2, 26)
            .title("UHPIC 晶圆")
            .task(Task.item(GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 16))
            .scaledReward(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 2)
            .dependsOn(NewIvChapter.HPIC_WAFER, NAQUADAH_INGOT_COPY_1));

    public static final Quest UHPIC = CHAPTER.quest("uhpic_chip", q -> q
            .at(2, 24)
            .title("UHPIC 芯片")
            .task(Task.item(GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64))
            .scaledReward(GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 8)
            .dependsOn(UHPIC_WAFER));

    public static final Quest PLUTONIUM_241 = CHAPTER.quest("plutonium_241_and_coolant_cell_30k", q -> q
            .at(-2, 22)
            .title("钚-241 与 30K 冷却单元")
            .icon(TagPrefix.plateDouble, GTMaterials.Plutonium241)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.Plutonium241, 8),
                    Task.item(GTOItems.COOLANT_CELL_30K, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium239, 16)
            .dependsOn(PLUTONIUM_COPY_1));

    public static final Quest TECH_SUPER_CONDUCTING_MATERIAL_RESEARCH = CHAPTER.quest("tech_super_conducting_material_research", q -> q
            .at(-4, 24)
            .title("超导材料研究")
            .task(Task.techNode(BaseNodes.SuperConductingMaterialResearch))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(IV_SCANNER_COPY_3, DATA_CRYSTAL_COPY_3));

    public static final Quest TECH_NUCLEAR_PHYSICS = CHAPTER.quest("tech_nuclear_physics", q -> q
            .at(-4, 26)
            .title("核物理研究")
            .task(Task.techNode(BaseNodes.NuclearPhysics))
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium239, 4)
            .dependsOn(IV_SCANNER_COPY_2, DATA_CRYSTAL_COPY_2));

    public static final Quest TECH_TOKAMAK_FUSION_REACTOR = CHAPTER.quest("tech_tokamak_fusion_reactor", q -> q
            .at(-2, 24)
            .title("托卡马克聚变反应堆研究")
            .task(Task.techNode(BaseNodes.TokamakFusionReactor))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(TECH_SUPER_CONDUCTING_MATERIAL_RESEARCH, TECH_NUCLEAR_PHYSICS));

    public static final Quest LUV_FUSION_REACTOR = CHAPTER.quest("luv_fusion_reactor", q -> q
            .at(0, 22)
            .milestone()
            .title("LuV 聚变反应堆")
            .subtitle("最初的甜甜圈")
            .desc(d -> d
                    .line("{聚变机械方块}对于现在来说实在是太贵了！最节省的方案是：1个{主机}，48个{聚变机械方块}，31个{聚变玻璃}，4个{超导线圈方块}，16个{输入仓}，16个{输出仓}，16个{能源仓}，{上下堆叠}可以节省大量机械方块！（在EMI多方块预览界面中点击P即可切换带有玻璃的形态）", GOLD, AQUA, AQUA, AQUA, AQUA, AQUA, AQUA, AQUA, GREEN)
                    .line()
                    .line("需要攒够一定的能量才能开机，如果停止运行会慢慢冷却，需要下一次攒够能量才会运行")
                    .line()
                    .line("{温馨小提示：你可以通过标记输出仓的过滤来保证只有一个仓会输出流体！}", GRAY.italic()))
            .icon(GTMultiMachines.FUSION_REACTOR[GTValues.LuV])
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.LuV]),
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV], 15))
            .scaledReward(TagPrefix.dust, GTMaterials.Neodymium, 16)
            .dependsOn(FUSION_CASING, QUANTUM_MAINFRAME, UHPIC, PLUTONIUM_241, LUV_ENERGY_HATCH, IV_FIELD_GENERATOR, TECH_TOKAMAK_FUSION_REACTOR));

    public static final Quest EUROPIUM = CHAPTER.quest("europium_ingot", q -> q
            .at(8, 20)
            .title("铕")
            .subtitle("第一个聚变金属")
            .desc(d -> d
                    .line("速度很慢，你可能需要挂机来获得你的第一批{铕}", GREEN)
                    .line()
                    .line("获得{三钛线圈}后你可以用{铕粉}直接烧制成{铕锭}，不要忘记更新你的产线", GOLD, YELLOW, YELLOW))
            .task(Task.item(TagPrefix.ingot, GTMaterials.Europium, 16))
            .scaledReward(TagPrefix.ingot, GTMaterials.Europium, 4)
            .dependsOn(LUV_FUSION_REACTOR));

    public static final QuestLink EUROPIUM_COPY_1 = CHAPTER.copy(EUROPIUM, 30, 28);

    public static final QuestLink EUROPIUM_COPY_2 = CHAPTER.copy(EUROPIUM, 8, 26);

    public static final QuestLink EUROPIUM_COPY_3 = CHAPTER.copy(EUROPIUM, 6, 26);

    public static final Quest EXQUISITE_GEMS = CHAPTER.quest("exquisite_gems", q -> q
            .at(10, 24)
            .title("精致宝石")
            .icon(TagPrefix.gemExquisite, GTMaterials.Olivine)
            .task(Task.item(TagPrefix.gemExquisite, GTMaterials.Olivine, 4),
                    Task.item(TagPrefix.gemExquisite, GTMaterials.Diamond, 2),
                    Task.item(TagPrefix.gemExquisite, GTMaterials.Ruby, 1),
                    Task.item(TagPrefix.gemExquisite, GTMaterials.Sapphire, 1),
                    Task.item(TagPrefix.gemExquisite, GTMaterials.Emerald, 1))
            .scaledReward(TagPrefix.gemExquisite, GTMaterials.Olivine, 2)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final QuestLink EXQUISITE_GEMS_COPY_1 = CHAPTER.copy(EXQUISITE_GEMS, 16, 28);

    public static final QuestLink EXQUISITE_GEMS_COPY_2 = CHAPTER.copy(EXQUISITE_GEMS, 20, 28);

    public static final Quest GEM_SEEDS = CHAPTER.quest("gem_crystal_seeds", q -> q
            .at(18, 28)
            .title("宝石晶种")
            .icon(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Ruby)
            .task(Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Diamond, 2),
                    Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Ruby, 2),
                    Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Sapphire, 2),
                    Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Emerald, 2))
            .scaledReward(TagPrefix.gemExquisite, GTMaterials.Diamond, 1)
            .dependsOn(EXQUISITE_GEMS_COPY_1));

    public static final Quest ARTIFICIAL_GEMS = CHAPTER.quest("artificial_gems", q -> q
            .at(18, 26)
            .title("人造宝石")
            .icon(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Ruby)
            .task(Task.item(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Diamond, 1),
                    Task.item(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Ruby, 1),
                    Task.item(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Sapphire, 1),
                    Task.item(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Emerald, 1))
            .scaledReward(TagPrefix.dust, GTMaterials.Chromium, 4)
            .dependsOn(GEM_SEEDS));

    public static final Quest ELF_GLASS = CHAPTER.quest("elf_glass", q -> q
            .at(20, 26)
            .title("精灵玻璃")
            .task(Task.item(BotaniaBlocks.getBlock("elf_glass"), 8))
            .scaledReward(BotaniaBlocks.getBlock("mana_glass"), 8)
            .dependsOn(EXQUISITE_GEMS_COPY_2));

    public static final Quest ELF_GLASS_LENS = CHAPTER.quest("elf_glass_lens", q -> q
            .at(20, 24)
            .title("精灵玻璃透镜")
            .task(Task.item(TagPrefix.lens, GTOMaterials.ElfGlass, 1))
            .scaledReward(TagPrefix.plate, GTOMaterials.ElfGlass, 1)
            .dependsOn(ELF_GLASS));

    public static final Quest CRYSTAL_CIRCUITS = CHAPTER.quest("gem_crystal_circuits", q -> q
            .at(18, 24)
            .title("宝石晶体电路")
            .icon(GTOItems.RUBY_CRYSTAL_CIRCUIT)
            .task(Task.item(GTOItems.DIAMOND_CRYSTAL_CIRCUIT, 4),
                    Task.item(GTOItems.RUBY_CRYSTAL_CIRCUIT, 4),
                    Task.item(GTOItems.EMERALD_CRYSTAL_CIRCUIT, 4),
                    Task.item(GTOItems.SAPPHIRE_CRYSTAL_CIRCUIT, 4))
            .scaledReward(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Ruby, 1)
            .dependsOn(ARTIFICIAL_GEMS, ELF_GLASS_LENS));

    public static final Quest RAW_CRYSTAL_CHIP = CHAPTER.quest("crystal_circuit", q -> q
            .at(10, 22)
            .milestone()
            .title("晶体芯片原料")
            .desc(d -> d.line("{魔法}还是来了——如果你没有将魔法线进度推进到{精灵门}，你无法制作接下来的{晶体CPU}", LIGHT_PURPLE, AQUA, GOLD))
            .task(Task.item(GTItems.RAW_CRYSTAL_CHIP, 4))
            .scaledReward(GTItems.RAW_CRYSTAL_CHIP_PART, 9)
            .dependsOn(EXQUISITE_GEMS, EUROPIUM));

    public static final Quest ENGRAVED_CRYSTAL_CHIP = CHAPTER.quest("engraved_crystal_chip", q -> q
            .at(14, 22)
            .milestone()
            .title("刻蚀水晶芯片")
            .task(Task.item(GTItems.ENGRAVED_CRYSTAL_CHIP, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Olivine, 4)
            .dependsOn(RAW_CRYSTAL_CHIP, HSSG_COIL, NewEvChapter.EARTH_ORBIT));

    public static final Quest CRYSTAL_CPU = CHAPTER.quest("crystal_cpu", q -> q
            .at(18, 22)
            .milestone()
            .title("晶体 CPU")
            .desc(d -> d
                    .line("制作{刻蚀水晶芯片}需要{无重力环境}，这意味着你需要第三次前往空间站", GOLD, AQUA)
                    .line()
                    .line("但小空间站不够高，电力高炉放下后普通消声仓将无法工作，你需要{ME消声仓}来绕过小空间站的高度限制", GOLD)
                    .line()
                    .line("或者，建造{工业空间站}获取足够高的空间", GOLD))
            .task(Task.item(GTItems.CRYSTAL_CENTRAL_PROCESSING_UNIT, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Tantalum, 4)
            .dependsOn(ENGRAVED_CRYSTAL_CHIP, CRYSTAL_CIRCUITS));

    public static final Quest SILICA_CERAMIC_DUST = CHAPTER.quest("silica_ceramic_dust", q -> q
            .at(6, 6)
            .title("氧化硅岩陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.SilicaCeramic, 9))
            .scaledReward(GTMaterials.HydrogenPeroxide.getBucket(), 4)
            .dependsOn(ENRICHED_NAQUADAH, HYDROGEN_PEROXIDE_COPY_1));

    public static final Quest SILICA_CERAMIC_ROUGH_BLANK = CHAPTER.quest("silica_ceramic_rough_blank", q -> q
            .at(4, 6)
            .title("氧化硅岩陶瓷粗胚")
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.SilicaCeramic, 1))
            .scaledReward(GTMaterials.Epoxy.getBucket(), 1)
            .dependsOn(SILICA_CERAMIC_DUST));

    public static final Quest SILICA_CERAMIC_BLOCK = CHAPTER.quest("silica_ceramic_block", q -> q
            .at(4, 8)
            .title("氧化硅岩陶瓷块")
            .task(Task.item(TagPrefix.block, GTOMaterials.SilicaCeramic, 1))
            .scaledReward(TagPrefix.dust, GTOMaterials.SilicaCeramic, 4)
            .dependsOn(SILICA_CERAMIC_ROUGH_BLANK));

    public static final Quest SILICA_CERAMIC_FLAKE = CHAPTER.quest("silica_ceramic_flake", q -> q
            .at(4, 10)
            .title("氧化硅岩陶瓷薄片")
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.SilicaCeramic, 32))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.SilicaCeramic, 8)
            .dependsOn(SILICA_CERAMIC_BLOCK));

    public static final Quest MULTILAYER_FIBER_BOARD = CHAPTER.quest("multilayer_fiber_board", q -> q
            .at(4, 14)
            .title("多层纤维强化电路基板")
            .task(Task.item(GTItems.MULTILAYER_FIBER_BOARD, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.Palladium, 16)
            .dependsOn(NewIvChapter.FIBER_BOARD, NewIvChapter.PALLADIUM));

    public static final Quest ELITE_BOARD = CHAPTER.quest("elite_circuit_board", q -> q
            .at(4, 12)
            .milestone()
            .title("精英电路板")
            .task(Task.item(GTItems.ELITE_CIRCUIT_BOARD, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.Platinum, 32)
            .dependsOn(SILICA_CERAMIC_FLAKE, MULTILAYER_FIBER_BOARD));

    public static final Quest HEXAFLUOROSILICIC_ACID = CHAPTER.quest("hexafluorosilicic_acid", q -> q
            .at(34, 14)
            .title("稀六氟硅酸")
            .icon(GTOMaterials.DiluteHexafluorosilicicAcid.getBucket())
            .task(Task.item(GTOMaterials.DiluteHexafluorosilicicAcid.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Stone, 48)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest FLUOROSILICIC_ACID = CHAPTER.quest("fluorosilicic_acid", q -> q
            .at(36, 14)
            .title("六氟硅酸")
            .icon(GTOMaterials.FluorosilicicAcid.getBucket())
            .task(Task.item(GTOMaterials.FluorosilicicAcid.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOMaterials.DiluteHexafluorosilicicAcid.getBucket(), 3)
            .dependsOn(HEXAFLUOROSILICIC_ACID));

    public static final Quest AMMONIUM_FLUORIDE = CHAPTER.quest("ammonium_fluoride", q -> q
            .at(36, 12)
            .title("氟化铵")
            .icon(GTOMaterials.AmmoniumFluoride.getBucket())
            .task(Task.item(GTOMaterials.AmmoniumFluoride.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 6)
            .dependsOn(FLUOROSILICIC_ACID));

    public static final Quest AMMONIUM_BIFLUORIDE = CHAPTER.quest("ammonium_bifluoride_solution", q -> q
            .at(34, 12)
            .title("氟氢化氨")
            .icon(GTOMaterials.AmmoniumBifluorideSolution.getBucket())
            .task(Task.item(GTOMaterials.AmmoniumBifluorideSolution.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.AmmoniumBifluoride, 8)
            .dependsOn(AMMONIUM_FLUORIDE));

    public static final Quest ETHYLENE_OXIDE = CHAPTER.quest("ethylene_oxide", q -> q
            .at(34, 8)
            .title("环氧乙烷")
            .icon(GTOMaterials.EthyleneOxide.getBucket())
            .task(Task.item(GTOMaterials.EthyleneOxide.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Ethylene.getBucket(), 4)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest ETHYLENE_GLYCOL = CHAPTER.quest("ethylene_glycol", q -> q
            .at(34, 10)
            .title("乙二醇")
            .icon(GTOMaterials.EthyleneGlycol.getBucket())
            .task(Task.item(GTOMaterials.EthyleneGlycol.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOMaterials.EthyleneOxide.getBucket(), 2)
            .dependsOn(ETHYLENE_OXIDE));

    public static final Quest DIETHYLENE_GLYCOL = CHAPTER.quest("diethylene_glycol", q -> q
            .at(32, 10)
            .title("二甘醇")
            .icon(GTOMaterials.DiethyleneGlycol.getBucket())
            .task(Task.item(GTOMaterials.DiethyleneGlycol.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOMaterials.EthyleneGlycol.getBucket(), 2)
            .dependsOn(ETHYLENE_GLYCOL));

    public static final Quest TITANIUM_NANOTUBE_PRECURSOR = CHAPTER.quest("titanium_nanotube_precursor", q -> q
            .at(32, 12)
            .title("钛纳米管前驱体")
            .task(Task.item(TagPrefix.dust, GTOMaterials.TitaniumNanotubePrecursor, 4))
            .scaledReward(TagPrefix.foil, GTMaterials.Titanium, 16)
            .dependsOn(AMMONIUM_BIFLUORIDE, DIETHYLENE_GLYCOL));

    public static final Quest TITANIUM_DIOXIDE_NANOTUBES = CHAPTER.quest("titanium_dioxide_nanotubes", q -> q
            .at(30, 12)
            .title("二氧化钛纳米管")
            .task(Task.item(TagPrefix.foil, GTOMaterials.TitaniumDioxideNanotubes, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.TitaniumNanotubePrecursor, 1)
            .dependsOn(TITANIUM_NANOTUBE_PRECURSOR));

    public static final Quest ADVANCED_SMD = CHAPTER.quest("advanced_smd_components", q -> q
            .at(28, 12)
            .milestone()
            .title("高级 SMD 元件")
            .icon(GTItems.ADVANCED_SMD_CAPACITOR)
            .task(Task.item(GTItems.ADVANCED_SMD_CAPACITOR, 32),
                    Task.item(GTItems.ADVANCED_SMD_TRANSISTOR, 16),
                    Task.item(GTItems.ADVANCED_SMD_INDUCTOR, 16),
                    Task.item(GTItems.ADVANCED_SMD_DIODE, 8))
            .scaledReward(GTItems.ADVANCED_SMD_CAPACITOR, 8)
            .dependsOn(TITANIUM_DIOXIDE_NANOTUBES, POLYBENZIMIDAZOLE, HSSS_AND_HSSE));

    public static final Quest CRYSTAL_PROCESSOR = CHAPTER.quest("crystal_processor", q -> q
            .at(22, 22)
            .milestone()
            .title("晶体处理器")
            .icon(GTItems.CRYSTAL_PROCESSOR_IV)
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.LuV]),
                    Task.item(GTItems.CRYSTAL_PROCESSOR_IV, 6))
            .scaledReward(GTItems.ADVANCED_SMD_TRANSISTOR, 8)
            .dependsOn(ADVANCED_SMD, ELITE_BOARD, CRYSTAL_CPU, QUANTUM_MAINFRAME_COPY_1, LUV_PARTS_COPY_1));

    public static final Quest CRYSTAL_ASSEMBLY = CHAPTER.quest("crystal_processor_assembly", q -> q
            .at(26, 22)
            .milestone()
            .title("晶体处理器集群")
            .task(Task.item(GTItems.CRYSTAL_ASSEMBLY_LuV, 6))
            .scaledReward(GTItems.ADVANCED_SMD_INDUCTOR, 4)
            .dependsOn(CRYSTAL_PROCESSOR));

    public static final Quest CRYSTAL_COMPUTER = CHAPTER.quest("crystal_processor_supercomputer", q -> q
            .at(30, 22)
            .milestone()
            .title("晶体处理器超级计算机")
            .subtitle("更便宜的ZPM级电路")
            .task(Task.item(GTItems.CRYSTAL_COMPUTER_ZPM, 2))
            .scaledReward(GTItems.NOR_MEMORY_CHIP, 32)
            .dependsOn(CRYSTAL_ASSEMBLY));

    public static final Quest FLOCCULENT_WATER_BUCKET = CHAPTER.quest("flocculent_water_bucket", q -> q
            .at(22, 18)
            .side()
            .icon(GTOMaterials.FlocculentWater.getBucket())
            .task(Task.item(GTOMaterials.FlocculentWater.getBucket()).optional(), Task.checkmark())
            .dependsOn(NAQUADAH_INGOT));

    public static final Quest NAQUADAH_BOULE = CHAPTER.quest("naquadah_boule", q -> q
            .at(20, 18)
            .side()
            .desc(d -> d.line("硅岩掺杂的单晶硅，能产出更多的晶圆的同时也解锁了ASoC芯片，其能够简化{纳米处理器}和{量子处理器}的制作", AQUA, GREEN))
            .task(Task.item(GTItems.NAQUADAH_BOULE))
            .dependsOn(NAQUADAH_INGOT));

    public static final Quest POLYURETHANERESIN_BUCKET = CHAPTER.quest("polyurethaneresin_bucket", q -> q
            .at(-2, 0)
            .side()
            .icon(GTOMaterials.PolyurethaneResin.getBucket())
            .task(Task.item(GTOMaterials.PolyurethaneResin.getBucket()).optional(), Task.checkmark())
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest SILICON_NITRIDE_CERAMIC_FLAKE = CHAPTER.quest("silicon_nitride_ceramic_flake", q -> q
            .at(-2, -2)
            .side()
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.SiliconNitrideCeramic))
            .dependsOn(POLYURETHANERESIN_BUCKET));

    public static final QuestLink SILICON_NITRIDE_CERAMIC_FLAKE_COPY_1 = CHAPTER.copy(SILICON_NITRIDE_CERAMIC_FLAKE, 38, 26);

    public static final Quest ENERGY_INJECTOR = CHAPTER.quest("energy_injector", q -> q
            .at(-2, 10)
            .side()
            .task(Task.item(MultiBlockC.ENERGY_INJECTOR))
            .optional()
            .dependsOn(SUPERCONDUCTING_COIL));

    public static final Quest SUPERCRITICAL_STEAM_TURBINE = CHAPTER.quest("supercritical_steam_turbine", q -> q
            .at(0, 2)
            .side()
            .task(Task.item(GeneratorMultiblock.SUPERCRITICAL_STEAM_TURBINE))
            .optional()
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest INTEGRAL_FRAMEWORK_LUV = CHAPTER.quest("integral_framework_luv", q -> q
            .at(26, 6)
            .side()
            .subtitle("升级你的大型机器#2")
            .task(Task.item(GTOBlocks.INTEGRAL_FRAMEWORK_LUV))
            .dependsOn(LUV_ASSEMBLER));

    public static final Quest RAW_CRYSTAL_CHIP_PARTS = CHAPTER.quest("raw_crystal_chip_parts", q -> q
            .at(10, 20)
            .side()
            .desc(d -> d.line("你需要自循环来实现晶体芯片的增殖"))
            .task(Task.item(GTItems.RAW_CRYSTAL_CHIP_PART))
            .dependsOn(RAW_CRYSTAL_CHIP));

    public static final Quest LARGE_CRYSTALLIZATION_CHAMBER = CHAPTER.quest("large_crystallization_chamber", q -> q
            .at(28, 24)
            .side()
            .task(Task.item(MultiBlockG.LARGE_CRYSTALLIZATION_CHAMBER))
            .dependsOn(CRYSTAL_COMPUTER, ZPM_HULL_COPY_1));

    public static final Quest IV_DRONE = CHAPTER.quest("iv_drone", q -> q
            .at(22, 16)
            .side()
            .subtitle("全频域覆盖")
            .task(Task.item(GTOItems.IV_DRONE))
            .dependsOn(NAQUADAH_INGOT));

    public static final Quest SUPERCRITICAL_MEGA_STEAM_TURBINE = CHAPTER.quest("supercritical_mega_steam_turbine", q -> q
            .at(32, 20)
            .side()
            .task(Task.item(GeneratorMultiblock.SUPERCRITICAL_MEGA_STEAM_TURBINE))
            .optional()
            .dependsOn(CRYSTAL_COMPUTER, ZPM_HULL_COPY_2));

    public static final Quest SUPERCRITICAL_STEAM_INPUT_HATCH = CHAPTER.quest("supercritical_steam_input_hatch", q -> q
            .at(34, 20)
            .side()
            .subtitle("真的还有用吗")
            .desc(d -> d.line("继续提升蒸汽机器的配方电压一级，解锁更多次数的超频"))
            .task(Task.item(GTOMachines.SUPERCRITICAL_STEAM_INPUT_HATCH))
            .optional()
            .dependsOn(SUPERCRITICAL_MEGA_STEAM_TURBINE));

    public static final Quest MAGNETIC_FLUID_GENERATOR = CHAPTER.quest("magnetic_fluid_generator", q -> q
            .at(32, 24)
            .side()
            .desc(d -> d.line("可持续性发电机，在MK I阶段你可以使用氖等离子体作为原料"))
            .task(Task.item(GeneratorMultiblock.MAGNETIC_FLUID_GENERATOR))
            .optional()
            .dependsOn(CRYSTAL_COMPUTER, ZPM_HULL_COPY_3));

    public static final Quest LUV_KUANGBIAO_ONE_GIANT_NUCLEAR_FUSION_REACTOR = CHAPTER.quest("luv_kuangbiao_one_giant_nuclear_fusion_reactor", q -> q
            .at(30, 26)
            .side()
            .desc(d -> d
                    .line("并行版本的超强聚变堆！")
                    .line()
                    .line("有史以来你做过最贵的东西，但是这值得。它能够{极大幅}提升你聚变的{速度}。", RED, YELLOW)
                    .line()
                    .line("{你是不是在机器的超频模块描述中看到了几个新的仓室？它们在UV阶段才能够制造}", GRAY.italic()))
            .task(Task.item(MultiBlockD.KUANGBIAO_ONE_GIANT_NUCLEAR_FUSION_REACTOR))
            .requireAny()
            .dependsOn(CRYSTAL_COMPUTER, ZPM_HULL_COPY_4, EUROPIUM_COPY_1, NewEvChapter.ADVANCED_CIRCUIT_BOARD, ITBTC_INGOT_COPY_1));

    public static final Quest ISA_LINE = CHAPTER.quest("isa_line", q -> q
            .at(30, 20)
            .side()
            .title("艾萨线")
            .desc(d -> d
                    .line("{艾}{萨}{线}，这是一条新的矿物处理线，你需要大量的松油和乙醇来搭建这条产线", GREEN, YELLOW, RED)
                    .line()
                    .line("可以跳过繁杂的产线直接产出铂处（没有钌），稀土，硅岩等一些稀有的粉")
                    .line()
                    .line("现在你有了两条矿物处理线，也许你需要想办法改进你的自动化来自动分流你的矿物！"))
            .task(Task.item(MultiBlockD.ISA_MILL), Task.item(MultiBlockA.INDUSTRIAL_FLOTATION_CELL), Task.item(MultiBlockA.VACUUM_DRYING_FURNACE))
            .dependsOn(CRYSTAL_COMPUTER, ZPM_HULL_COPY_5));

    public static final Quest ESSENCE = CHAPTER.quest("essence", q -> q
            .at(34, 22)
            .side()
            .title("精华")
            .desc(d -> d.line("精准虚空采矿模式能挖到一些特殊的矿，但同时你也需要准备{精华}", GOLD))
            .task(Task.tag(QuestExternalRefs.TAG_GTOCORE_VEIN_ESSENCE, 1).title("任意精华"))
            .dependsOn(NewEvChapter.QUANTUM_EYE, CRYSTAL_COMPUTER, ZPM_HULL_COPY_6));

    public static final Quest TELLURIUM_DUST = CHAPTER.quest("tellurium_dust", q -> q
            .at(34, 24)
            .side()
            .desc(d -> d
                    .line("碲是一种半金属元素，它们在世界分布松散，需要用虚空采矿才能找到")
                    .line()
                    .line("碲和若干碲化物是优良的磁共振半导体材料，你未来的共振紫晶会用到它的"))
            .task(Task.item(TagPrefix.dust, GTMaterials.Tellurium))
            .dependsOn(ESSENCE));

    public static final Quest MAGNETO_RESONATIC_DUST = CHAPTER.quest("magneto_resonatic_dust", q -> q
            .at(36, 24)
            .side()
            .task(Task.item(TagPrefix.dust, GTOMaterials.MagnetoResonatic))
            .dependsOn(TELLURIUM_DUST));

    public static final Quest CIRCUIT_COMPOUND_DUST = CHAPTER.quest("circuit_compound_dust", q -> q
            .at(34, 26)
            .side()
            .task(Task.item(TagPrefix.dust, GTOMaterials.CircuitCompound))
            .dependsOn(TELLURIUM_DUST));

    public static final QuestLink CIRCUIT_COMPOUND_DUST_COPY_1 = CHAPTER.copy(CIRCUIT_COMPOUND_DUST, 38, 22);

    public static final Quest IMPRINTED_RESONATIC_CIRCUIT_BOARD = CHAPTER.quest("imprinted_resonatic_circuit_board", q -> q
            .at(38, 24)
            .side()
            .task(Task.item(GTOItems.IMPRINTED_RESONATIC_CIRCUIT_BOARD))
            .dependsOn(MAGNETO_RESONATIC_DUST, SILICON_NITRIDE_CERAMIC_FLAKE_COPY_1, CIRCUIT_COMPOUND_DUST_COPY_1));

    public static final Quest MAGNETIC_RESONANCE_CIRCUIT_SERIES = CHAPTER.quest("magnetic_resonance_circuit_series", q -> q
            .at(40, 24)
            .side()
            .title("磁共振电路系列")
            .subtitle("性价比之王！")
            .desc(d -> d
                    .line("第一次见可能觉得有些贵，但随着不断升级电路板，磁共振的造价优势会越发显著！")
                    .line()
                    .line("你现在就可以做出最高LuV级别的磁共振电路"))
            .task(
                    Task.anyOf(1, GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.ULV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.LV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.MV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.HV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.EV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.IV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.LuV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UHV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UEV], GTOItems.MAGNETO_RESONATIC_CIRCUIT[GTValues.UIV]).title("磁共振电路系列"))
            .dependsOn(IMPRINTED_RESONATIC_CIRCUIT_BOARD));

    public static final Quest ADVANCED_DATA_ACCESS_HATCH = CHAPTER.quest("advanced_data_access_hatch", q -> q
            .at(8, 24)
            .side()
            .task(Task.item(GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH))
            .dependsOn(ZPM_HULL, EUROPIUM_COPY_2));

    public static final Quest LARGE_CRACKER = CHAPTER.quest("large_cracker", q -> q
            .at(28, 20)
            .side()
            .task(Task.item(MultiBlockA.LARGE_CRACKER))
            .dependsOn(CRYSTAL_COMPUTER, ZPM_HULL_COPY_7));

    public static final Quest PROSPECTOR_LUV = CHAPTER.quest("prospector_luv", q -> q
            .at(22, 8)
            .side()
            .desc(d -> d.line("最顶级的探矿仪，极大增加了勘探的范围"))
            .task(Task.item(GTItems.PROSPECTOR_LuV))
            .dependsOn(LUV_MOTOR));

    public static final Quest VIRTUAL_COIN_MINER = CHAPTER.quest("virtual_coin_miner", q -> q
            .at(6, 24)
            .side()
            .task(Task.item(MultiBlockI.VIRTUAL_COIN_MINER))
            .optional()
            .dependsOn(ZPM_HULL, EUROPIUM_COPY_3));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewIvChapter.LUV_HULL, 0, 0))
                .mainLine(NewIvChapter.LUV_HULL, FISSION_REACTOR, ASSEMBLY_LINE, VENUS, NITRIDED_SAMARIUM_TERBIUM, LUV_ENERGY_HATCH, CERES, POLYMERIZATION_REACTOR, ADVANCED_SMD, IO, THE_END, NEUTRON_ACTIVATOR, ENRICHED_NAQUADAH_SOLUTION, ELITE_BOARD, NAQUADAH_ALLOY, FUSION_CASING, LUV_FUSION_REACTOR, ZPM_HULL, RAW_CRYSTAL_CHIP, ENGRAVED_CRYSTAL_CHIP, CRYSTAL_CPU, CRYSTAL_PROCESSOR, CRYSTAL_ASSEMBLY, CRYSTAL_COMPUTER));
    }
}
