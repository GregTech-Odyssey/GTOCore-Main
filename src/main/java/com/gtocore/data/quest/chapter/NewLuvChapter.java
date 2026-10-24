package com.gtocore.data.quest.chapter;

import com.gtocore.api.data.tag.GTOTagPrefix;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOGlobes;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.data.GTOMachines;
import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.GTOOrganItems;
import com.gtocore.common.data.machines.MultiBlockD;
import com.gtocore.common.data.machines.MultiBlockG;
import com.gtocore.common.item.misc.OrganType;
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
import vazkii.botania.common.block.BotaniaBlocks;

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

    public static final QuestLink NEUTRON_REFLECTOR_COPY_1 = CHAPTER.copy(NEUTRON_REFLECTOR, 0, 8);

    public static final Quest FISSION_REACTOR = CHAPTER.quest("fission_reactor", q -> q
            .at(4, 0)
            .milestone()
            .title("裂变反应堆")
            .icon(MultiBlockD.FISSION_REACTOR)
            .task(Task.item(MultiBlockD.FISSION_REACTOR), Task.item(GTOBlocks.FISSION_FUEL_COMPONENT), Task.item(GTOBlocks.FISSION_COOLER_COMPONENT))
            .scaledReward(TagPrefix.plate, GTOMaterials.ReactorSteel, 6)
            .dependsOn(NEUTRON_REFLECTOR));

    public static final Quest URANINITE = CHAPTER.quest("uraninite", q -> q
            .at(10, 6)
            .title("晶质铀矿")
            .icon(TagPrefix.dust, GTMaterials.Uraninite)
            .task(Task.item(TagPrefix.dust, GTMaterials.Uraninite, 30))
            .scaledReward(TagPrefix.dust, GTMaterials.Uraninite, 9)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest URANIUM_HEXAFLUORIDE = CHAPTER.quest("uranium_hexafluoride", q -> q
            .at(12, 6)
            .title("六氟化铀")
            .icon(GTMaterials.UraniumHexafluoride.getBucket())
            .task(Task.item(GTMaterials.UraniumHexafluoride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrofluoricAcid.getBucket(), 4)
            .dependsOn(URANINITE));

    public static final Quest ENRICHED_URANIUM_HEXAFLUORIDE = CHAPTER.quest("enriched_uranium_hexafluoride", q -> q
            .at(12, 4)
            .title("浓缩六氟化铀")
            .icon(GTMaterials.EnrichedUraniumHexafluoride.getBucket())
            .task(Task.item(GTMaterials.EnrichedUraniumHexafluoride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.UraniumHexafluoride.getBucket(), 4)
            .dependsOn(URANIUM_HEXAFLUORIDE));

    public static final Quest URANIUM_235 = CHAPTER.quest("uranium_235", q -> q
            .at(12, 2)
            .title("铀235")
            .icon(TagPrefix.dust, GTMaterials.Uranium235)
            .task(Task.item(TagPrefix.dust, GTMaterials.Uranium235, 3), Task.item(TagPrefix.dust, GTMaterials.Uranium238, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Uranium238, 16)
            .dependsOn(ENRICHED_URANIUM_HEXAFLUORIDE));

    public static final Quest EMPTY_FUEL_ROD = CHAPTER.quest("empty_fuel_rod", q -> q
            .at(14, 4)
            .title("空燃料棒")
            .icon(GTOItems.REACTOR_FUEL_ROD)
            .task(Task.item(GTOItems.REACTOR_FUEL_ROD, 4))
            .scaledReward(TagPrefix.ingot, GTMaterials.SteelMagnetic, 4)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest URANIUM_FUEL_ROD = CHAPTER.quest("uranium_fuel_rod", q -> q
            .at(14, 2)
            .title("铀燃料棒")
            .icon(GTOItems.REACTOR_URANIUM_SIMPLE)
            .task(Task.item(GTOItems.REACTOR_URANIUM_SIMPLE, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Uranium238, 16)
            .dependsOn(URANIUM_235, EMPTY_FUEL_ROD));

    public static final Quest SPENT_FUEL = CHAPTER.quest("spent_fuel", q -> q
            .at(14, -2)
            .title("乏燃料")
            .icon(GTOItems.DEPLETED_REACTOR_URANIUM_SIMPLE)
            .task(Task.item(GTOItems.DEPLETED_REACTOR_URANIUM_SIMPLE))
            .scaledReward(TagPrefix.dust, GTMaterials.Uranium238, 16)
            .dependsOn(URANIUM_FUEL_ROD, FISSION_REACTOR));

    public static final Quest PLUTONIUM = CHAPTER.quest("plutonium", q -> q
            .at(12, -4)
            .title("钚239")
            .icon(TagPrefix.ingot, GTMaterials.Plutonium239)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Plutonium239, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium239, 2)
            .dependsOn(SPENT_FUEL));

    public static final QuestLink PLUTONIUM_COPY_1 = CHAPTER.copy(PLUTONIUM, 0, 24);

    public static final Quest RADON = CHAPTER.quest("radon", q -> q
            .at(12, -2)
            .title("氡")
            .icon(GTMaterials.Radon.getBucket())
            .task(Task.item(GTMaterials.Radon.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Uranium238, 4)
            .dependsOn(PLUTONIUM));

    public static final QuestLink RADON_COPY_1 = CHAPTER.copy(RADON, -8, 2);

    public static final QuestLink RADON_COPY_2 = CHAPTER.copy(RADON, 2, 26);

    public static final Quest NUCLEAR_WASTE = CHAPTER.quest("nuclear_waste", q -> q
            .at(14, -4)
            .title("核废料")
            .icon(GTOItems.NUCLEAR_WASTE)
            .task(Task.item(GTOItems.NUCLEAR_WASTE, 16))
            .scaledReward(GTOItems.NUCLEAR_WASTE, 4)
            .dependsOn(SPENT_FUEL));

    public static final Quest POLONIUM = CHAPTER.quest("tiny_polonium_dust", q -> q
            .at(14, -6)
            .title("钋")
            .icon(TagPrefix.ingot, GTMaterials.Polonium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Polonium, 13))
            .scaledReward(TagPrefix.dustTiny, GTMaterials.Polonium, 9)
            .dependsOn(NUCLEAR_WASTE));

    public static final Quest LIQUID_AIR = CHAPTER.quest("liquid_air", q -> q
            .at(-8, 4)
            .title("液态空气")
            .icon(GTMaterials.LiquidAir.getBucket())
            .task(Task.item(GTMaterials.LiquidAir.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Air.getBucket(), 16)
            .dependsOn(RADON_COPY_1));

    public static final Quest HELIUM = CHAPTER.quest("helium", q -> q
            .at(-8, 6)
            .title("氦")
            .icon(GTMaterials.Helium.getBucket())
            .task(Task.item(GTMaterials.Helium.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.LiquidAir.getBucket(), 4)
            .dependsOn(LIQUID_AIR));

    public static final Quest LIQUID_HELIUM = CHAPTER.quest("liquid_helium", q -> q
            .at(-8, 8)
            .title("液氦")
            .icon(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID).getBucket())
            .task(Task.item(GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID).getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Helium.getBucket(), 4)
            .dependsOn(HELIUM));

    public static final QuestLink LIQUID_HELIUM_COPY_1 = CHAPTER.copy(LIQUID_HELIUM, 10, 16);

    public static final Quest FLUORINATED_SAMARIUM = CHAPTER.quest("fluorinated_samarium_concentrate", q -> q
            .at(10, -2)
            .title("氟化钐精")
            .icon(TagPrefix.dust, GTOMaterials.FluorinatedSamariumConcentratePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.FluorinatedSamariumConcentratePowder, 12))
            .scaledReward(TagPrefix.dust, GTOMaterials.FluorinatedSamariumConcentratePowder, 4)
            .dependsOn(RADON));

    public static final Quest SAMARIUM_TERBIUM_MIXTURE = CHAPTER.quest("samarium_terbium_mixture", q -> q
            .at(12, 0)
            .milestone()
            .title("钐铽混合物")
            .icon(TagPrefix.dust, GTOMaterials.SamariumTerbiumMixturePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumTerbiumMixturePowder, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.Calcium, 12)
            .dependsOn(FLUORINATED_SAMARIUM));

    public static final Quest NITRIDED_SAMARIUM_TERBIUM = CHAPTER.quest("nitrided_samarium_terbium", q -> q
            .at(26, 2)
            .title("氮化钐铽")
            .icon(TagPrefix.dust, GTOMaterials.NitridedSamariumTerbiumMixturePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.NitridedSamariumTerbiumMixturePowder, 12))
            .scaledReward(TagPrefix.dust, GTMaterials.AmmoniumChloride, 27)
            .dependsOn(SAMARIUM_TERBIUM_MIXTURE));

    public static final Quest SAMARIUM_PRECIPITATE = CHAPTER.quest("samarium_precipitate", q -> q
            .at(26, 4)
            .title("钐沉淀")
            .icon(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder)
            .task(Task.item(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.Copper, 3)
            .dependsOn(NITRIDED_SAMARIUM_TERBIUM));

    public static final Quest SAMARIUM = CHAPTER.quest("samarium", q -> q
            .at(26, 6)
            .title("钐")
            .icon(TagPrefix.dust, GTMaterials.Samarium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Samarium, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.SamariumPrecipitatePowder, 3)
            .dependsOn(SAMARIUM_PRECIPITATE));

    public static final QuestLink SAMARIUM_COPY_1 = CHAPTER.copy(SAMARIUM, 12, 14);

    public static final Quest HSSG_INGOT = CHAPTER.quest("hssg_ingot", q -> q
            .at(2, 24)
            .title("HSS-G 锭")
            .icon(TagPrefix.ingot, GTMaterials.HSSG)
            .task(Task.item(TagPrefix.ingot, GTMaterials.HSSG, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.HSSG, 9)
            .dependsOn(RADON_COPY_2));

    public static final QuestLink HSSG_INGOT_COPY_1 = CHAPTER.copy(HSSG_INGOT, 10, 18);

    public static final Quest HSSG_COIL = CHAPTER.quest("hssg_coil", q -> q
            .at(12, 18)
            .title("HSS-G 线圈")
            .icon(GTBlocks.COIL_HSSG)
            .task(Task.item(GTBlocks.COIL_HSSG, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.TungstenCarbide, 32)
            .dependsOn(HSSG_INGOT_COPY_1));

    public static final Quest SAMARIUM_IRON_ARSENIC_OXIDE = CHAPTER.quest("samarium_iron_arsenic_oxide", q -> q
            .at(12, 16)
            .title("钐铁砷氧化物")
            .icon(TagPrefix.ingot, GTMaterials.SamariumIronArsenicOxide)
            .task(Task.item(TagPrefix.ingot, GTMaterials.SamariumIronArsenicOxide, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.SamariumIronArsenicOxide, 4)
            .dependsOn(SAMARIUM_COPY_1, LIQUID_HELIUM_COPY_1, HSSG_COIL));

    public static final Quest SAMARIUM_IRON_ARSENIC_OXIDE_WIRE = CHAPTER.quest("samarium_iron_arsenic_oxide_wire", q -> q
            .at(14, 16)
            .title("1x 钐铁砷氧化物导线")
            .icon(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide)
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide, 16))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.SamariumIronArsenicOxide, 4)
            .dependsOn(SAMARIUM_IRON_ARSENIC_OXIDE));

    public static final Quest URANIUM_TRIPLATINUM_WIRE = CHAPTER.quest("uranium_triplatinum_wire", q -> q
            .at(6, -8)
            .title("1x 铀三铂导线")
            .icon(TagPrefix.wireGtSingle, GTMaterials.UraniumTriplatinum)
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.UraniumTriplatinum, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.UraniumTriplatinum, 4)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest EV_FIELD_GENERATOR = CHAPTER.quest("ev_field_generator", q -> q
            .at(8, -8)
            .title("EV 力场发生器")
            .icon(GTItems.FIELD_GENERATOR_EV)
            .task(Task.item(GTItems.FIELD_GENERATOR_EV, 5))
            .scaledReward(GTItems.EMITTER_EV, 1)
            .dependsOn(NewEvChapter.EV_EMITTER_SENSOR, URANIUM_TRIPLATINUM_WIRE));

    public static final QuestLink EV_FIELD_GENERATOR_COPY_1 = CHAPTER.copy(EV_FIELD_GENERATOR, 30, 16);

    public static final Quest IV_FIELD_GENERATOR = CHAPTER.quest("iv_field_generator", q -> q
            .at(-2, 20)
            .title("IV 力场发生器")
            .icon(GTItems.FIELD_GENERATOR_IV)
            .task(Task.item(GTItems.FIELD_GENERATOR_IV, 5))
            .scaledReward(GTItems.QUANTUM_STAR, 1)
            .dependsOn(NewIvChapter.IV_EMITTER_SENSOR));

    public static final Quest PHTHALIC_ACID = CHAPTER.quest("phthalic_acid", q -> q
            .at(10, -8)
            .title("邻苯二甲酸")
            .icon(GTMaterials.PhthalicAcid.getBucket())
            .task(Task.item(GTMaterials.PhthalicAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.PotassiumDichromate, 2)
            .dependsOn(EV_FIELD_GENERATOR));

    public static final Quest PHTHALIC_ANHYDRIDE = CHAPTER.quest("phthalic_anhydride", q -> q
            .at(12, -8)
            .title("邻苯二甲酸酐")
            .icon(TagPrefix.dust, GTOMaterials.PhthalicAnhydride)
            .task(Task.item(TagPrefix.dust, GTOMaterials.PhthalicAnhydride, 15))
            .scaledReward(GTMaterials.PhthalicAcid.getBucket(), 1)
            .dependsOn(PHTHALIC_ACID));

    public static final Quest ETHYLANTHRAQUINONE = CHAPTER.quest("ethylanthraquinone", q -> q
            .at(14, -8)
            .title("乙基蒽醌")
            .icon(GTOMaterials.Ethylanthraquinone.getBucket())
            .task(Task.item(GTOMaterials.Ethylanthraquinone.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.PhthalicAnhydride, 15)
            .dependsOn(PHTHALIC_ANHYDRIDE));

    public static final Quest HYDROGEN_PEROXIDE = CHAPTER.quest("hydrogen_peroxide", q -> q
            .at(16, -8)
            .title("过氧化氢")
            .icon(GTMaterials.HydrogenPeroxide.getBucket())
            .task(Task.item(GTMaterials.HydrogenPeroxide.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Ethylanthraquinone.getBucket(), 1)
            .dependsOn(ETHYLANTHRAQUINONE));

    public static final QuestLink HYDROGEN_PEROXIDE_COPY_1 = CHAPTER.copy(HYDROGEN_PEROXIDE, 6, 18);

    public static final Quest HYDRAZINE = CHAPTER.quest("hydrazine", q -> q
            .at(18, -6)
            .title("肼")
            .icon(GTOMaterials.Hydrazine.getBucket())
            .task(Task.item(GTOMaterials.Hydrazine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 4)
            .dependsOn(HYDROGEN_PEROXIDE));

    public static final Quest DENSE_HYDRAZINE = CHAPTER.quest("dense_hydrazine_fuel", q -> q
            .at(18, -4)
            .title("浓缩肼燃料")
            .icon(GTOMaterials.DenseHydrazineFuelMixture.getBucket())
            .task(Task.item(GTOMaterials.DenseHydrazineFuelMixture.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Methanol.getBucket(), 4)
            .dependsOn(HYDRAZINE));

    public static final Quest HEAVY_DUTY_PLATE_3 = CHAPTER.quest("heavy_duty_plate_3", q -> q
            .at(16, -6)
            .title("三阶重型板")
            .icon(GTOItems.HEAVY_DUTY_PLATE_3)
            .task(Task.item(GTOItems.HEAVY_DUTY_PLATE_3, 12))
            .scaledReward(GTOItems.HEAVY_DUTY_PLATE_2, 4)
            .dependsOn(HYDROGEN_PEROXIDE));

    public static final Quest TIER_3_ROCKET = CHAPTER.quest("tier_3_rocket", q -> q
            .at(16, -4)
            .title("T3 火箭")
            .icon(ModItems.TIER_3_ROCKET.get())
            .task(Task.item(ModItems.TIER_3_ROCKET.get()))
            .scaledReward(ModItems.STEEL_ENGINE.get(), 2)
            .dependsOn(HEAVY_DUTY_PLATE_3));

    public static final Quest UNLOCK_VENUS = CHAPTER.quest("unlock_venus", q -> q
            .at(16, -2)
            .title("解锁金星")
            .icon(ModItems.VENUS_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.DenseHydrazineFuelMixture.getBucket(), 4)
            .dependsOn(DENSE_HYDRAZINE, TIER_3_ROCKET));

    public static final Quest REINFORCED_ORGANS = CHAPTER.quest("reinforced_organs", q -> q
            .at(18, -2)
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
            .at(18, 0)
            .milestone()
            .title("登陆金星")
            .icon(ModItems.VENUS_GLOBE.get())
            .task(Task.dimension(Dimension.VENUS).title("造访金星").icon(ModItems.VENUS_GLOBE.get()))
            .scaledReward(ModItems.RAW_DESH.get(), 16)
            .dependsOn(UNLOCK_VENUS, REINFORCED_ORGANS));

    public static final Quest IV_SCANNER = CHAPTER.quest("iv_scanner", q -> q
            .at(10, 4)
            .title("IV 扫描仪")
            .icon(GTMachines.SCANNER[GTValues.IV])
            .task(Task.item(GTMachines.SCANNER[GTValues.IV]))
            .scaledReward(GTItems.EMITTER_IV, 1)
            .dependsOn(NewIvChapter.NANO_MAINFRAME));

    public static final QuestLink IV_SCANNER_COPY_1 = CHAPTER.copy(IV_SCANNER, 18, 2);

    public static final QuestLink IV_SCANNER_COPY_2 = CHAPTER.copy(IV_SCANNER, -4, 24);

    public static final QuestLink IV_SCANNER_COPY_3 = CHAPTER.copy(IV_SCANNER, -6, 22);

    public static final Quest DATA_CRYSTAL = CHAPTER.quest("data_crystal", q -> q
            .at(8, 6)
            .title("数据晶片")
            .icon(GTOItems.DATA_CRYSTAL_MK1)
            .task(Task.item(GTOItems.DATA_CRYSTAL_MK1))
            .scaledReward(GTItems.NOR_MEMORY_CHIP, 16)
            .dependsOn(NewEvChapter.ADVANCED_CIRCUIT_BOARD));

    public static final QuestLink DATA_CRYSTAL_COPY_1 = CHAPTER.copy(DATA_CRYSTAL, 20, 4);

    public static final QuestLink DATA_CRYSTAL_COPY_2 = CHAPTER.copy(DATA_CRYSTAL, -2, 26);

    public static final QuestLink DATA_CRYSTAL_COPY_3 = CHAPTER.copy(DATA_CRYSTAL, -6, 24);

    public static final Quest UNLOCK_TECH_NODE = CHAPTER.quest("unlock_tech_node", q -> q
            .at(8, 4)
            .title("解锁科技节点")
            .icon(GTItems.FIELD_GENERATOR_IV)
            .task(Task.techNode(ComponentNodes.ComponentInAssemblyLineluv))
            .scaledReward(GTItems.QUANTUM_STAR, 1)
            .dependsOn(IV_SCANNER, DATA_CRYSTAL));

    public static final Quest CALCULATION_PRESS = CHAPTER.quest("calculation_processor_press", q -> q
            .at(6, 4)
            .title("计算处理器压印模板")
            .icon(AEItems.CALCULATION_PROCESSOR_PRESS)
            .task(Task.item(AEItems.CALCULATION_PROCESSOR_PRESS))
            .scaledReward(AEItems.CALCULATION_PROCESSOR, 4)
            .dependsOn(UNLOCK_TECH_NODE));

    public static final Quest DATA_EXPORT_MACHINE = CHAPTER.quest("data_export_machine", q -> q
            .at(8, 2)
            .title("数据导出机")
            .icon(GTOMachines.DATA_EXPORT_MACHINE)
            .task(Task.item(GTOMachines.DATA_EXPORT_MACHINE))
            .scaledReward(GTItems.TOOL_DATA_STICK, 2)
            .dependsOn(UNLOCK_TECH_NODE, CALCULATION_PRESS));

    public static final Quest ASSEMBLY_LINE_CASINGS = CHAPTER.quest("assembly_line_casings", q -> q
            .at(10, 2)
            .title("装配线外壳")
            .icon(GTBlocks.CASING_ASSEMBLY_CONTROL)
            .task(Task.item(GTBlocks.CASING_ASSEMBLY_CONTROL, 5), Task.item(GTBlocks.CASING_ASSEMBLY_LINE, 5), Task.item(GTBlocks.CASING_GRATE, 5))
            .scaledReward(GTItems.ROBOT_ARM_IV, 2)
            .dependsOn(UNLOCK_TECH_NODE));

    public static final Quest DATA_ACCESS_HATCH = CHAPTER.quest("data_access_hatch", q -> q
            .at(6, 2)
            .title("数据访问仓")
            .icon(GTResearchMachines.DATA_ACCESS_HATCH)
            .task(Task.item(GTResearchMachines.DATA_ACCESS_HATCH))
            .scaledReward(GTItems.TOOL_DATA_STICK, 4)
            .dependsOn(DATA_EXPORT_MACHINE));

    public static final Quest ASSEMBLY_LINE = CHAPTER.quest("assembly_line", q -> q
            .at(8, 0)
            .milestone()
            .title("装配线")
            .icon(GTMultiMachines.ASSEMBLY_LINE)
            .task(Task.item(GTMultiMachines.ASSEMBLY_LINE))
            .scaledReward(TagPrefix.cableGtSingle, GTMaterials.NiobiumTitanium, 4)
            .dependsOn(DATA_EXPORT_MACHINE, ASSEMBLY_LINE_CASINGS, DATA_ACCESS_HATCH));

    public static final Quest SAMARIUM_INGOT = CHAPTER.quest("samarium_ingot", q -> q
            .at(24, 6)
            .title("钐锭")
            .icon(TagPrefix.ingot, GTMaterials.Samarium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Samarium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Samarium, 2)
            .dependsOn(SAMARIUM));

    public static final Quest SAMARIUM_MAGNETIC = CHAPTER.quest("samarium_magnetic", q -> q
            .at(24, 8)
            .title("磁化钐")
            .icon(TagPrefix.rodLong, GTMaterials.SamariumMagnetic)
            .task(Task.item(TagPrefix.rodLong, GTMaterials.SamariumMagnetic, 4))
            .scaledReward(TagPrefix.rod, GTMaterials.SamariumMagnetic, 2)
            .dependsOn(SAMARIUM_INGOT));

    public static final Quest LUV_MOTOR = CHAPTER.quest("luv_electric_motor", q -> q
            .at(22, 8)
            .title("LuV 马达")
            .icon(GTItems.ELECTRIC_MOTOR_LuV)
            .task(Task.item(GTItems.ELECTRIC_MOTOR_LuV, 8))
            .scaledReward(TagPrefix.rodLong, GTMaterials.SamariumMagnetic, 2)
            .dependsOn(ASSEMBLY_LINE, SAMARIUM_MAGNETIC));

    public static final Quest LUV_PARTS = CHAPTER.quest("luv_parts", q -> q
            .at(22, 6)
            .title("LuV 部件")
            .icon(GTItems.ROBOT_ARM_LuV)
            .task(Task.item(GTItems.ELECTRIC_PISTON_LuV, 2), Task.item(GTItems.ROBOT_ARM_LuV, 2), Task.item(GTItems.CONVEYOR_MODULE_LuV, 2),
                    Task.item(GTItems.ELECTRIC_PUMP_LuV), Task.item(GTItems.EMITTER_LuV), Task.item(GTItems.SENSOR_LuV))
            .scaledReward(GTItems.ELECTRIC_MOTOR_LuV, 1)
            .dependsOn(LUV_MOTOR));

    public static final QuestLink LUV_PARTS_COPY_1 = CHAPTER.copy(LUV_PARTS, 22, 22);

    public static final Quest LUV_ASSEMBLER = CHAPTER.quest("luv_assembler", q -> q
            .at(22, 4)
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

    public static final QuestLink TACTICAL_ORGANS_COPY_1 = CHAPTER.copy(TACTICAL_ORGANS, 36, -4);

    public static final Quest DESH = CHAPTER.quest("desh_ingot", q -> q
            .at(34, -6)
            .title("Desh 锭")
            .icon(ModItems.DESH_INGOT.get())
            .task(Task.item(ModItems.DESH_INGOT.get(), 64))
            .scaledReward(TagPrefix.dust, GTOMaterials.Desh, 16)
            .dependsOn(TACTICAL_ORGANS_COPY_1));

    public static final Quest DENSE_DESH = CHAPTER.quest("dense_desh_plate", q -> q
            .at(32, -6)
            .title("致密 Desh 板")
            .icon(TagPrefix.plateDense, GTOMaterials.Desh)
            .task(Task.item(TagPrefix.plateDense, GTOMaterials.Desh, 16))
            .scaledReward(ModItems.DESH_INGOT.get(), 9)
            .dependsOn(DESH));

    public static final Quest DESH_ENGINE = CHAPTER.quest("desh_engine_and_tank", q -> q
            .at(30, -8)
            .title("Desh 引擎与储罐")
            .icon(ModItems.DESH_ENGINE.get())
            .task(Task.item(ModItems.DESH_ENGINE.get(), 8), Task.item(ModItems.DESH_TANK.get(), 8))
            .scaledReward(ModItems.STEEL_ENGINE.get(), 2)
            .dependsOn(DENSE_DESH));

    public static final Quest TIER_4_ROCKET = CHAPTER.quest("tier_4_rocket", q -> q
            .at(30, -6)
            .title("T4 火箭")
            .icon(ModItems.TIER_4_ROCKET.get())
            .task(Task.item(ModItems.TIER_4_ROCKET.get()))
            .scaledReward(GTItems.FIELD_GENERATOR_EV, 1)
            .dependsOn(DESH_ENGINE));

    public static final Quest MONOMETHYLHYDRAZINE = CHAPTER.quest("monomethylhydrazine", q -> q
            .at(28, -8)
            .title("甲肼")
            .icon(GTOMaterials.Monomethylhydrazine.getBucket())
            .task(Task.item(GTOMaterials.Monomethylhydrazine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Hydrazine.getBucket(), 2)
            .dependsOn(DESH_ENGINE));

    public static final Quest ROCKET_FUEL_CN3H7O3 = CHAPTER.quest("rocket_fuel_cn3h7o3", q -> q
            .at(28, -6)
            .title("火箭燃料 CN3H7O3")
            .icon(GTOMaterials.RocketFuelCn3h7o3.getBucket())
            .task(Task.item(GTOMaterials.RocketFuelCn3h7o3.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.Monomethylhydrazine.getBucket(), 4)
            .dependsOn(MONOMETHYLHYDRAZINE));

    public static final Quest UNLOCK_CERES = CHAPTER.quest("unlock_ceres", q -> q
            .at(28, -4)
            .title("解锁谷神星")
            .icon(GTOGlobes.Items.CERES_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelCn3h7o3.getBucket(), 4)
            .dependsOn(TIER_4_ROCKET, ROCKET_FUEL_CN3H7O3));

    public static final Quest CERES = CHAPTER.quest("ceres", q -> q
            .at(26, 0)
            .milestone()
            .title("登陆谷神星")
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
            .at(30, -4)
            .title("二氯联苯胺")
            .icon(GTMaterials.Dichlorobenzidine.getBucket())
            .task(Task.item(GTMaterials.Dichlorobenzidine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Nitrochlorobenzene.getBucket(), 1)
            .dependsOn(NITROCHLOROBENZENE));

    public static final Quest DIAMINOBENZIDINE = CHAPTER.quest("diaminobenzidine", q -> q
            .at(30, -2)
            .title("二氨基联苯胺")
            .icon(GTMaterials.Diaminobenzidine.getBucket())
            .task(Task.item(GTMaterials.Diaminobenzidine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 2)
            .dependsOn(DICHLOROBENZIDINE));

    public static final Quest POLYMERIZATION_REACTOR = CHAPTER.quest("polymerization_reactor", q -> q
            .at(30, 0)
            .milestone()
            .title("聚合反应器")
            .task(Task.item(MultiBlockG.POLYMERIZATION_REACTOR))
            .scaledReward(GTMaterials.Polybenzimidazole.getBucket(), 1)
            .dependsOn(DIAMINOBENZIDINE));

    public static final Quest POLYBENZIMIDAZOLE = CHAPTER.quest("polybenzimidazole", q -> q
            .at(32, 6)
            .title("聚苯并咪唑")
            .icon(GTMaterials.Polybenzimidazole.getBucket())
            .task(Task.item(GTMaterials.Polybenzimidazole.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Polybenzimidazole.getBucket(), 1)
            .dependsOn(POLYMERIZATION_REACTOR));

    public static final QuestLink POLYBENZIMIDAZOLE_COPY_1 = CHAPTER.copy(POLYBENZIMIDAZOLE, 22, 14);

    public static final QuestLink POLYBENZIMIDAZOLE_COPY_2 = CHAPTER.copy(POLYBENZIMIDAZOLE, -2, 10);

    public static final Quest OSTRUM = CHAPTER.quest("ostrum_ingot", q -> q
            .at(22, 16)
            .title("紫金锭")
            .icon(ModItems.OSTRUM_INGOT.get())
            .task(Task.item(ModItems.OSTRUM_INGOT.get(), 64), Task.item(TagPrefix.plateDense, GTOMaterials.Ostrum, 16))
            .scaledReward(TagPrefix.dust, GTOMaterials.Ostrum, 16)
            .dependsOn(CERES));

    public static final Quest OSMIUM_OXIDE = CHAPTER.quest("osmium_oxide", q -> q
            .at(32, 16)
            .title("氧化锇")
            .icon(TagPrefix.dust, GTOMaterials.OsmiumOxideMetal)
            .task(Task.item(TagPrefix.dust, GTOMaterials.OsmiumOxideMetal, 14))
            .scaledReward(GTMaterials.AcidicOsmiumSolution.getBucket(), 2)
            .dependsOn(EV_FIELD_GENERATOR_COPY_1));

    public static final Quest OSMIUM_TETROXIDE = CHAPTER.quest("osmium_tetroxide", q -> q
            .at(32, 14)
            .title("四氧化锇")
            .icon(TagPrefix.dust, GTMaterials.OsmiumTetroxide)
            .task(Task.item(TagPrefix.dust, GTMaterials.OsmiumTetroxide, 8))
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 2)
            .dependsOn(OSMIUM_OXIDE));

    public static final Quest OSMIUM = CHAPTER.quest("osmium", q -> q
            .at(32, 12)
            .title("锇")
            .icon(TagPrefix.dust, GTMaterials.Osmium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Osmium, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Iridium, 3)
            .dependsOn(OSMIUM_TETROXIDE));

    public static final Quest HSSS_AND_HSSE = CHAPTER.quest("hsss_and_hsse", q -> q
            .at(30, 12)
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
            .at(20, 2)
            .title("高压能量输入输出I研究")
            .task(Task.techNode(ComponentNodes.EnergyIOs[GTValues.LuV]))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(IV_SCANNER_COPY_1, DATA_CRYSTAL_COPY_1));

    public static final Quest LUV_ENERGY_HATCH = CHAPTER.quest("luv_energy_input_hatch", q -> q
            .at(22, 0)
            .milestone()
            .title("LuV 能源仓")
            .icon(GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV])
            .task(Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV], 2))
            .scaledReward(TagPrefix.cableGtDouble, GTMaterials.NiobiumTitanium, 2)
            .dependsOn(LUV_VOLTAGE_COIL, TECH_ENERGY_IO_LUV));

    public static final Quest OSTRUM_ENGINE = CHAPTER.quest("ostrum_engine_and_tank", q -> q
            .at(24, 16)
            .title("紫金引擎与储罐")
            .icon(ModItems.OSTRUM_ENGINE.get())
            .task(Task.item(ModItems.OSTRUM_ENGINE.get(), 8), Task.item(ModItems.OSTRUM_TANK.get(), 8))
            .scaledReward(ModItems.STEEL_ENGINE.get(), 2)
            .dependsOn(OSTRUM));

    public static final Quest TIER_5_ROCKET = CHAPTER.quest("tier_5_rocket", q -> q
            .at(24, 14)
            .title("T5 火箭")
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
            .at(24, 12)
            .title("解锁木卫一")
            .icon(GTOGlobes.Items.IO_GLOBE.get())
            .task(Task.checkmark())
            .scaledReward(GTOMaterials.RocketFuelH8n4c2o4.getBucket(), 4)
            .dependsOn(TIER_5_ROCKET, ROCKET_FUEL_H8N4C2O4));

    public static final Quest IO = CHAPTER.quest("io", q -> q
            .at(26, 10)
            .milestone()
            .title("登陆木卫一")
            .icon(GTOGlobes.Items.IO_GLOBE.get())
            .task(Task.dimension(Dimension.IO).title("造访木卫一").icon(GTOGlobes.Items.IO_GLOBE.get()))
            .scaledReward(TagPrefix.rawOre, GTMaterials.Naquadah, 16)
            .dependsOn(UNLOCK_IO));

    public static final Quest NAQUADAH_OXIDE_MIXTURE = CHAPTER.quest("naquadah_oxide_mixture", q -> q
            .at(30, 14)
            .title("氧化硅岩混合物")
            .icon(TagPrefix.dust, GTOMaterials.NaquadahOxideMixture)
            .task(Task.item(TagPrefix.dust, GTOMaterials.NaquadahOxideMixture, 30))
            .scaledReward(TagPrefix.rawOre, GTMaterials.Naquadah, 8)
            .dependsOn(IO));

    public static final Quest ANTIMONY_TRIFLUORIDE = CHAPTER.quest("antimony_trifluoride", q -> q
            .at(26, 12)
            .title("三氟化锑")
            .icon(TagPrefix.dust, GTMaterials.AntimonyTrifluoride)
            .task(Task.item(TagPrefix.dust, GTMaterials.AntimonyTrifluoride, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Antimony, 4)
            .dependsOn(IO));

    public static final Quest FLUOROANTIMONIC_ACID = CHAPTER.quest("fluoroantimonic_acid", q -> q
            .at(28, 14)
            .title("氟锑酸")
            .icon(GTMaterials.FluoroantimonicAcid.getBucket())
            .task(Task.item(GTMaterials.FluoroantimonicAcid.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.HydrofluoricAcid.getBucket(), 4)
            .dependsOn(ANTIMONY_TRIFLUORIDE));

    public static final Quest LOW_PURITY_NAQUADAH_EMULSION = CHAPTER.quest("low_purity_naquadah_emulsion", q -> q
            .at(28, 16)
            .title("低纯硅岩乳液")
            .icon(GTOMaterials.LowPurityNaquadahEmulsion.getBucket())
            .task(Task.item(GTOMaterials.LowPurityNaquadahEmulsion.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.FluoroantimonicAcid.getBucket(), 2)
            .dependsOn(NAQUADAH_OXIDE_MIXTURE, FLUOROANTIMONIC_ACID));

    public static final Quest NAQUADAH_DUST = CHAPTER.quest("naquadah_dust", q -> q
            .at(26, 18)
            .title("硅岩粉")
            .icon(TagPrefix.dust, GTMaterials.Naquadah)
            .task(Task.item(TagPrefix.dust, GTMaterials.Naquadah, 24))
            .scaledReward(TagPrefix.dust, GTMaterials.SodiumHydroxide, 6)
            .dependsOn(LOW_PURITY_NAQUADAH_EMULSION));

    public static final Quest NAQUADAH_INGOT = CHAPTER.quest("naquadah_ingot", q -> q
            .at(22, 18)
            .title("硅岩锭")
            .icon(TagPrefix.ingot, GTMaterials.Naquadah)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Naquadah, 16))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadah, 4)
            .dependsOn(NAQUADAH_DUST));

    public static final QuestLink NAQUADAH_INGOT_COPY_1 = CHAPTER.copy(NAQUADAH_INGOT, -6, 18);

    public static final Quest NAQUADAH_COIL = CHAPTER.quest("naquadah_coil_block", q -> q
            .at(20, 14)
            .title("硅岩线圈")
            .icon(GTBlocks.COIL_NAQUADAH)
            .task(Task.item(GTBlocks.COIL_NAQUADAH, 16))
            .scaledReward(TagPrefix.foil, GTMaterials.Osmium, 8)
            .dependsOn(NAQUADAH_INGOT));

    public static final QuestLink NAQUADAH_COIL_COPY_1 = CHAPTER.copy(NAQUADAH_COIL, 20, 18);

    public static final QuestLink NAQUADAH_COIL_COPY_2 = CHAPTER.copy(NAQUADAH_COIL, -6, 10);

    public static final Quest ERBIUM = CHAPTER.quest("erbium", q -> q
            .at(18, 18)
            .title("铒")
            .icon(TagPrefix.dust, GTMaterials.Erbium)
            .task(Task.item(TagPrefix.dust, GTMaterials.Erbium, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.ErbiumOxide, 5)
            .dependsOn(NAQUADAH_COIL_COPY_1));

    public static final Quest FALL_KING = CHAPTER.quest("fall_king_ingot", q -> q
            .at(18, 16)
            .title("陨王")
            .icon(TagPrefix.ingot, GTOMaterials.FallKing)
            .task(Task.item(TagPrefix.ingot, GTOMaterials.FallKing, 4))
            .scaledReward(TagPrefix.dust, GTMaterials.Lithium, 2)
            .dependsOn(ERBIUM));

    public static final Quest THE_END = CHAPTER.quest("the_end", q -> q
            .at(20, 10)
            .milestone()
            .title("末地")
            .icon(Items.END_STONE)
            .task(Task.dimension(Dimension.THE_END).title("进入末地").icon(Items.END_STONE))
            .scaledReward(Items.ENDER_EYE, 4)
            .dependsOn(NAQUADAH_COIL));

    public static final Quest END_STEEL = CHAPTER.quest("end_steel", q -> q
            .at(18, 12)
            .title("4x 末地钢导线")
            .icon(TagPrefix.wireGtQuadruple, GTOMaterials.EndSteel)
            .task(Task.item(TagPrefix.wireGtQuadruple, GTOMaterials.EndSteel, 18))
            .scaledReward(Items.END_STONE, 16)
            .dependsOn(THE_END));

    public static final QuestLink END_STEEL_COPY_1 = CHAPTER.copy(END_STEEL, 16, 16);

    public static final Quest MAR_M200 = CHAPTER.quest("mar_m200_steel", q -> q
            .at(18, 14)
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
            .dependsOn(END_STEEL_COPY_1, FALL_KING, MAR_M200));

    public static final Quest MBCC_WIRE = CHAPTER.quest("mercury_barium_calcium_cuprate_wire", q -> q
            .at(14, 14)
            .title("1x 汞钡钙铜导线")
            .icon(TagPrefix.wireGtSingle, GTMaterials.MercuryBariumCalciumCuprate)
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.MercuryBariumCalciumCuprate, 32))
            .scaledReward(TagPrefix.dust, GTMaterials.MercuryBariumCalciumCuprate, 4)
            .dependsOn(PROCESS_MACHINE_CASING));

    public static final Quest SPEEDING_PIPE = CHAPTER.quest("speeding_pipe", q -> q
            .at(14, 12)
            .title("高速管道")
            .icon(GTOBlocks.SPEEDING_PIPE)
            .task(Task.item(GTOBlocks.SPEEDING_PIPE, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Beryllium, 32)
            .dependsOn(MBCC_WIRE));

    public static final Quest HUI_CIRCUIT = CHAPTER.quest("hui_circuit", q -> q
            .at(12, 12)
            .title("HUI 电路")
            .icon(GTOItems.HUI_CIRCUIT_1)
            .task(Task.item(GTOItems.HUI_CIRCUIT_1, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.GarnetYellow, 16)
            .dependsOn(NewIvChapter.EXTREME_CIRCUIT_BOARD));

    public static final Quest NEUTRON_ACTIVATOR = CHAPTER.quest("neutron_activator", q -> q
            .at(14, 10)
            .milestone()
            .title("中子活化器")
            .icon(MultiBlockD.NEUTRON_ACTIVATOR)
            .task(Task.item(MultiBlockD.NEUTRON_ACTIVATOR), Task.item(GTOMachines.NEUTRON_ACCELERATOR[GTValues.LV], 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadah, 8)
            .dependsOn(PROCESS_MACHINE_CASING, SPEEDING_PIPE, HUI_CIRCUIT));

    public static final Quest IMPURE_ENRICHED_NAQUADAH = CHAPTER.quest("impure_enriched_naquadah_solution", q -> q
            .at(16, 8)
            .title("不纯富集硅岩溶液")
            .icon(GTMaterials.ImpureEnrichedNaquadahSolution.getBucket())
            .task(Task.item(GTMaterials.ImpureEnrichedNaquadahSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.LowPurityNaquadahSolution.getBucket(), 4)
            .dependsOn(NEUTRON_ACTIVATOR));

    public static final Quest HEXAFLUORIDE_ENRICHED_NAQUADAH = CHAPTER.quest("hexafluoride_enriched_naquadah_solution", q -> q
            .at(14, 6)
            .title("六氟化富集硅岩溶液")
            .icon(GTOMaterials.HexafluorideEnrichedNaquadahSolution.getBucket())
            .task(Task.item(GTOMaterials.HexafluorideEnrichedNaquadahSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Fluorine.getBucket(), 6)
            .dependsOn(IMPURE_ENRICHED_NAQUADAH));

    public static final QuestLink HEXAFLUORIDE_ENRICHED_NAQUADAH_COPY_1 = CHAPTER.copy(HEXAFLUORIDE_ENRICHED_NAQUADAH, 4, 8);

    public static final Quest XENON = CHAPTER.quest("xenon", q -> q
            .at(14, 8)
            .title("氙")
            .icon(GTMaterials.Xenon.getBucket())
            .task(Task.item(GTMaterials.Xenon.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.EnderAir.getBucket(), 16)
            .dependsOn(IMPURE_ENRICHED_NAQUADAH));

    public static final Quest XENON_HEXAFLUORO_ENRICHED_NAQUADATE = CHAPTER.quest("xenon_hexafluoro_enriched_naquadate", q -> q
            .at(12, 8)
            .title("六氟氙酸富集硅岩")
            .icon(GTOMaterials.XenonHexafluoroEnrichedNaquadate.getBucket())
            .task(Task.item(GTOMaterials.XenonHexafluoroEnrichedNaquadate.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Xenon.getBucket(), 1)
            .dependsOn(HEXAFLUORIDE_ENRICHED_NAQUADAH, XENON));

    public static final Quest BROMINE = CHAPTER.quest("bromine", q -> q
            .at(6, 8)
            .title("溴")
            .icon(GTMaterials.Bromine.getBucket())
            .task(Task.item(GTMaterials.Bromine.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Chlorine.getBucket(), 4)
            .dependsOn(HEXAFLUORIDE_ENRICHED_NAQUADAH_COPY_1));

    public static final Quest BROMINE_TRIFLUORIDE = CHAPTER.quest("bromine_trifluoride", q -> q
            .at(8, 8)
            .title("三氟化溴")
            .icon(GTOMaterials.BromineTrifluoride.getBucket())
            .task(Task.item(GTOMaterials.BromineTrifluoride.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Bromine.getBucket(), 1)
            .dependsOn(BROMINE));

    public static final Quest GOLD_TRIFLUORIDE = CHAPTER.quest("gold_trifluoride", q -> q
            .at(10, 8)
            .title("三氟化金")
            .icon(TagPrefix.dust, GTOMaterials.GoldTrifluoride)
            .task(Task.item(TagPrefix.dust, GTOMaterials.GoldTrifluoride, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Gold, 4)
            .dependsOn(BROMINE_TRIFLUORIDE));

    public static final Quest ENRICHED_NAQUADAH_SOLUTION = CHAPTER.quest("enriched_naquadah_solution", q -> q
            .at(10, 10)
            .milestone()
            .title("富集硅岩溶液")
            .icon(GTMaterials.EnrichedNaquadahSolution.getBucket())
            .task(Task.item(GTMaterials.EnrichedNaquadahSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.GoldTrifluoride, 4)
            .dependsOn(XENON_HEXAFLUORO_ENRICHED_NAQUADATE, GOLD_TRIFLUORIDE));

    public static final Quest ACIDIC_ENRICHED_NAQUADAH = CHAPTER.quest("acidic_enriched_naquadah_solution", q -> q
            .at(10, 12)
            .title("酸性富集硅岩溶液")
            .icon(GTMaterials.AcidicEnrichedNaquadahSolution.getBucket())
            .task(Task.item(GTMaterials.AcidicEnrichedNaquadahSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 4)
            .dependsOn(ENRICHED_NAQUADAH_SOLUTION));

    public static final Quest ENRICHED_NAQUADAH_SULFATE = CHAPTER.quest("enriched_naquadah_sulfate", q -> q
            .at(10, 14)
            .title("硫酸富集硅岩")
            .icon(TagPrefix.dust, GTMaterials.EnrichedNaquadahSulfate)
            .task(Task.item(TagPrefix.dust, GTMaterials.EnrichedNaquadahSulfate, 6))
            .scaledReward(GTMaterials.EnrichedNaquadahSolution.getBucket(), 1)
            .dependsOn(ACIDIC_ENRICHED_NAQUADAH));

    public static final Quest ENRICHED_NAQUADAH = CHAPTER.quest("enriched_naquadah_ingot", q -> q
            .at(8, 16)
            .title("富集硅岩")
            .icon(TagPrefix.ingot, GTMaterials.NaquadahEnriched)
            .task(Task.item(TagPrefix.ingot, GTMaterials.NaquadahEnriched, 6))
            .scaledReward(TagPrefix.dust, GTMaterials.NaquadahEnriched, 2)
            .dependsOn(ENRICHED_NAQUADAH_SULFATE));

    public static final QuestLink ENRICHED_NAQUADAH_COPY_1 = CHAPTER.copy(ENRICHED_NAQUADAH, -6, -4);

    public static final Quest IMPURE_NAQUADRIA = CHAPTER.quest("impure_naquadria_solution", q -> q
            .at(-4, -4)
            .title("不纯超能硅岩溶液")
            .icon(GTMaterials.ImpureNaquadriaSolution.getBucket())
            .task(Task.item(GTMaterials.ImpureNaquadriaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.FluoroantimonicAcid.getBucket(), 2)
            .dependsOn(ENRICHED_NAQUADAH_COPY_1));

    public static final Quest HEXAFLUORIDE_NAQUADRIA = CHAPTER.quest("hexafluoride_naquadria_solution", q -> q
            .at(-2, -4)
            .title("六氟化超能硅岩溶液")
            .icon(GTOMaterials.HexafluorideNaquadriaSolution.getBucket())
            .task(Task.item(GTOMaterials.HexafluorideNaquadriaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.Fluorine.getBucket(), 6)
            .dependsOn(IMPURE_NAQUADRIA));

    public static final Quest NAQUADRIA_SOLUTION = CHAPTER.quest("naquadria_solution", q -> q
            .at(-2, -2)
            .title("超能硅岩溶液")
            .icon(GTMaterials.NaquadriaSolution.getBucket())
            .task(Task.item(GTMaterials.NaquadriaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTOMaterials.RadonDifluoride.getBucket(), 2)
            .dependsOn(HEXAFLUORIDE_NAQUADRIA));

    public static final Quest ACIDIC_NAQUADRIA = CHAPTER.quest("acidic_naquadria_solution", q -> q
            .at(-2, 0)
            .title("酸性超能硅岩溶液")
            .icon(GTMaterials.AcidicNaquadriaSolution.getBucket())
            .task(Task.item(GTMaterials.AcidicNaquadriaSolution.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.SulfuricAcid.getBucket(), 4)
            .dependsOn(NAQUADRIA_SOLUTION));

    public static final Quest NAQUADRIA_WASTE = CHAPTER.quest("naquadria_waste", q -> q
            .at(-2, 2)
            .title("超能硅岩废液")
            .icon(GTMaterials.NaquadriaWaste.getBucket())
            .task(Task.item(GTMaterials.NaquadriaWaste.getBucket()).optional(), Task.checkmark())
            .scaledReward(GTMaterials.NaquadriaSolution.getBucket(), 1)
            .dependsOn(ACIDIC_NAQUADRIA));

    public static final Quest TRINIUM_SULFIDE = CHAPTER.quest("trinium_sulfide", q -> q
            .at(0, 4)
            .title("硫化凯金")
            .icon(TagPrefix.dust, GTMaterials.TriniumSulfide)
            .task(Task.item(TagPrefix.dust, GTMaterials.TriniumSulfide, 4))
            .scaledReward(GTMaterials.NaquadriaWaste.getBucket(), 2)
            .dependsOn(NAQUADRIA_WASTE));

    public static final Quest TRINIUM = CHAPTER.quest("trinium_ingot", q -> q
            .at(2, 6)
            .title("凯金锭")
            .icon(TagPrefix.ingot, GTMaterials.Trinium)
            .task(Task.item(TagPrefix.ingot, GTMaterials.Trinium, 2))
            .scaledReward(TagPrefix.dust, GTMaterials.Zinc, 2)
            .dependsOn(TRINIUM_SULFIDE));

    public static final QuestLink TRINIUM_COPY_1 = CHAPTER.copy(TRINIUM, -2, 6);

    public static final Quest NAQUADAH_ALLOY_DUST = CHAPTER.quest("naquadah_alloy_dust", q -> q
            .at(2, 8)
            .title("硅岩合金粉")
            .icon(TagPrefix.dust, GTMaterials.NaquadahAlloy)
            .task(Task.item(TagPrefix.dust, GTMaterials.NaquadahAlloy, 8))
            .scaledReward(TagPrefix.dust, GTMaterials.Naquadah, 4)
            .dependsOn(TRINIUM));

    public static final Quest NAQUADAH_ALLOY = CHAPTER.quest("naquadah_alloy_ingot", q -> q
            .at(2, 10)
            .milestone()
            .title("硅岩合金锭")
            .icon(TagPrefix.ingot, GTMaterials.NaquadahAlloy)
            .task(Task.item(TagPrefix.ingot, GTMaterials.NaquadahAlloy, 8))
            .scaledReward(TagPrefix.plate, GTMaterials.NaquadahAlloy, 2)
            .dependsOn(NAQUADAH_ALLOY_DUST, LUV_ENERGY_HATCH));

    public static final Quest ZPM_CASING = CHAPTER.quest("zpm_casing", q -> q
            .at(4, 18)
            .title("ZPM 机械方块")
            .icon(GTBlocks.MACHINE_CASING_ZPM)
            .task(Task.item(GTBlocks.MACHINE_CASING_ZPM))
            .scaledReward(TagPrefix.plate, GTMaterials.NaquadahAlloy, 2)
            .dependsOn(NAQUADAH_ALLOY));

    public static final Quest VANADIUM_GALLIUM_CABLE = CHAPTER.quest("vanadium_gallium_cable", q -> q
            .at(4, 22)
            .title("钒镓线缆")
            .icon(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium)
            .task(Task.item(TagPrefix.cableGtSingle, GTMaterials.VanadiumGallium, 2))
            .scaledReward(TagPrefix.wireGtSingle, GTMaterials.VanadiumGallium, 2)
            .dependsOn(NAQUADAH_ALLOY));

    public static final Quest ZPM_HULL = CHAPTER.quest("welcome_to_the_zero_pressure_phase", q -> q
            .at(4, 20)
            .milestone()
            .title("欢迎来到{零点压}阶段", RED)
            .task(Task.item(GTMachines.HULL[GTValues.ZPM]))
            .reward(
                    Reward.item(GTMachines.HULL[GTValues.ZPM], 1),
                    Reward.command("/playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1").elevated().silent().autoClaim(AutoClaim.INVISIBLE))
            .dependsOn(ZPM_CASING, VANADIUM_GALLIUM_CABLE));

    public static final Quest QUANTUM_MAINFRAME = CHAPTER.quest("quantum_processor_mainframe", q -> q
            .at(2, 22)
            .title("量子处理器主机")
            .task(Task.item(GTItems.QUANTUM_MAINFRAME_ZPM, 16))
            .scaledReward(GTItems.QUANTUM_COMPUTER_LuV, 1)
            .dependsOn(NewIvChapter.QUANTUM_COMPUTER, HSSG_INGOT));

    public static final QuestLink QUANTUM_MAINFRAME_COPY_1 = CHAPTER.copy(QUANTUM_MAINFRAME, 26, 24);

    public static final Quest ITBTC_DUST = CHAPTER.quest("indium_tin_barium_titanium_cuprate_dust", q -> q
            .at(-6, 6)
            .title("铟锡钡钛铜氧化物粉")
            .icon(TagPrefix.dust, GTMaterials.IndiumTinBariumTitaniumCuprate)
            .task(Task.item(GTMachines.MIXER[GTValues.LuV]),
                    Task.item(TagPrefix.dust, GTMaterials.IndiumTinBariumTitaniumCuprate, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.Indium, 8)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest ITBTC_INGOT = CHAPTER.quest("indium_tin_barium_titanium_cuprate_ingot", q -> q
            .at(-6, 8)
            .title("铟锡钡钛铜氧化物锭")
            .task(Task.item(TagPrefix.ingot, GTMaterials.IndiumTinBariumTitaniumCuprate, 64))
            .scaledReward(TagPrefix.dust, GTMaterials.IndiumTinBariumTitaniumCuprate, 16)
            .dependsOn(ITBTC_DUST, NAQUADAH_COIL_COPY_2, LIQUID_HELIUM));

    public static final Quest ITBTC_WIRE = CHAPTER.quest("indium_tin_barium_titanium_cuprate_wire", q -> q
            .at(-4, 8)
            .title("1x 铟锡钡钛铜氧化物导线")
            .task(Task.item(TagPrefix.wireGtSingle, GTMaterials.IndiumTinBariumTitaniumCuprate, 64))
            .scaledReward(TagPrefix.ingot, GTMaterials.IndiumTinBariumTitaniumCuprate, 8)
            .dependsOn(ITBTC_INGOT));

    public static final Quest SUPERCONDUCTING_COIL = CHAPTER.quest("superconducting_coil", q -> q
            .at(-2, 8)
            .title("超导线圈方块")
            .task(Task.item(GTBlocks.SUPERCONDUCTING_COIL, 52))
            .scaledReward(TagPrefix.ingot, GTMaterials.Trinium, 24)
            .dependsOn(ITBTC_WIRE, TRINIUM_COPY_1));

    public static final Quest FUSION_CASING = CHAPTER.quest("fusion_casing", q -> q
            .at(0, 10)
            .milestone()
            .title("聚变机械方块与聚变玻璃")
            .icon(GTBlocks.FUSION_CASING)
            .task(Task.item(GTBlocks.FUSION_CASING, 48),
                    Task.item(GTBlocks.FUSION_GLASS, 31))
            .scaledReward(GTItems.NEUTRON_REFLECTOR, 4)
            .dependsOn(SUPERCONDUCTING_COIL, NEUTRON_REFLECTOR_COPY_1, POLYBENZIMIDAZOLE_COPY_2));

    public static final Quest UHPIC_WAFER = CHAPTER.quest("uhpic_wafer", q -> q
            .at(-4, 18)
            .title("UHPIC 晶圆")
            .task(Task.item(GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 16))
            .scaledReward(GTItems.HIGH_POWER_INTEGRATED_CIRCUIT_WAFER, 2)
            .dependsOn(NewIvChapter.HPIC_WAFER, NAQUADAH_INGOT_COPY_1));

    public static final Quest UHPIC = CHAPTER.quest("uhpic_chip", q -> q
            .at(-2, 18)
            .title("UHPIC 芯片")
            .task(Task.item(GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64))
            .scaledReward(GTItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 8)
            .dependsOn(UHPIC_WAFER));

    public static final Quest PLUTONIUM_241 = CHAPTER.quest("plutonium_241_and_coolant_cell_30k", q -> q
            .at(0, 22)
            .title("钚-241 与 30K 冷却单元")
            .icon(TagPrefix.plateDouble, GTMaterials.Plutonium241)
            .task(Task.item(TagPrefix.plateDouble, GTMaterials.Plutonium241, 8),
                    Task.item(GTOItems.COOLANT_CELL_30K, 18))
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium239, 16)
            .dependsOn(PLUTONIUM_COPY_1));

    public static final Quest TECH_SUPER_CONDUCTING_MATERIAL_RESEARCH = CHAPTER.quest("tech_super_conducting_material_research", q -> q
            .at(-4, 22)
            .title("超导材料研究")
            .task(Task.techNode(BaseNodes.SuperConductingMaterialResearch))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(IV_SCANNER_COPY_3, DATA_CRYSTAL_COPY_3));

    public static final Quest TECH_NUCLEAR_PHYSICS = CHAPTER.quest("tech_nuclear_physics", q -> q
            .at(-2, 24)
            .title("核物理研究")
            .task(Task.techNode(BaseNodes.NuclearPhysics))
            .scaledReward(TagPrefix.dust, GTMaterials.Plutonium239, 4)
            .dependsOn(IV_SCANNER_COPY_2, DATA_CRYSTAL_COPY_2));

    public static final Quest TECH_TOKAMAK_FUSION_REACTOR = CHAPTER.quest("tech_tokamak_fusion_reactor", q -> q
            .at(-2, 22)
            .title("托卡马克聚变反应堆研究")
            .task(Task.techNode(BaseNodes.TokamakFusionReactor))
            .scaledReward(GTItems.TOOL_DATA_STICK, 1)
            .dependsOn(TECH_SUPER_CONDUCTING_MATERIAL_RESEARCH, TECH_NUCLEAR_PHYSICS));

    public static final Quest LUV_FUSION_REACTOR = CHAPTER.quest("luv_fusion_reactor", q -> q
            .at(0, 20)
            .milestone()
            .title("LuV 聚变反应堆")
            .icon(GTMultiMachines.FUSION_REACTOR[GTValues.LuV])
            .task(Task.item(GTMultiMachines.FUSION_REACTOR[GTValues.LuV]),
                    Task.item(GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV], 15))
            .scaledReward(TagPrefix.dust, GTMaterials.Neodymium, 16)
            .dependsOn(FUSION_CASING, QUANTUM_MAINFRAME, UHPIC, PLUTONIUM_241, LUV_ENERGY_HATCH, IV_FIELD_GENERATOR, TECH_TOKAMAK_FUSION_REACTOR));

    public static final Quest EUROPIUM = CHAPTER.quest("europium_ingot", q -> q
            .at(8, 22)
            .title("铕")
            .task(Task.item(TagPrefix.ingot, GTMaterials.Europium, 16))
            .scaledReward(TagPrefix.ingot, GTMaterials.Europium, 4)
            .dependsOn(LUV_FUSION_REACTOR));

    public static final Quest EXQUISITE_GEMS = CHAPTER.quest("exquisite_gems", q -> q
            .at(8, 18)
            .title("精致宝石")
            .icon(TagPrefix.gemExquisite, GTMaterials.Olivine)
            .task(Task.item(TagPrefix.gemExquisite, GTMaterials.Olivine, 4),
                    Task.item(TagPrefix.gemExquisite, GTMaterials.Diamond, 2),
                    Task.item(TagPrefix.gemExquisite, GTMaterials.Ruby, 1),
                    Task.item(TagPrefix.gemExquisite, GTMaterials.Sapphire, 1),
                    Task.item(TagPrefix.gemExquisite, GTMaterials.Emerald, 1))
            .scaledReward(TagPrefix.gemExquisite, GTMaterials.Olivine, 2)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final QuestLink EXQUISITE_GEMS_COPY_1 = CHAPTER.copy(EXQUISITE_GEMS, 14, 26);

    public static final QuestLink EXQUISITE_GEMS_COPY_2 = CHAPTER.copy(EXQUISITE_GEMS, 12, 24);

    public static final Quest GEM_SEEDS = CHAPTER.quest("gem_crystal_seeds", q -> q
            .at(16, 26)
            .title("宝石晶种")
            .icon(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Ruby)
            .task(Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Diamond, 2),
                    Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Ruby, 2),
                    Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Sapphire, 2),
                    Task.item(GTOTagPrefix.CRYSTAL_SEED, GTMaterials.Emerald, 2))
            .scaledReward(TagPrefix.gemExquisite, GTMaterials.Diamond, 1)
            .dependsOn(EXQUISITE_GEMS_COPY_1));

    public static final Quest ARTIFICIAL_GEMS = CHAPTER.quest("artificial_gems", q -> q
            .at(16, 24)
            .title("人造宝石")
            .icon(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Ruby)
            .task(Task.item(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Diamond, 1),
                    Task.item(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Ruby, 1),
                    Task.item(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Sapphire, 1),
                    Task.item(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Emerald, 1))
            .scaledReward(TagPrefix.dust, GTMaterials.Chromium, 4)
            .dependsOn(GEM_SEEDS));

    public static final Quest ELF_GLASS = CHAPTER.quest("elf_glass", q -> q
            .at(12, 22)
            .title("精灵玻璃")
            .task(Task.item(BotaniaBlocks.elfGlass, 8))
            .scaledReward(BotaniaBlocks.manaGlass, 8)
            .dependsOn(EXQUISITE_GEMS_COPY_2));

    public static final Quest ELF_GLASS_LENS = CHAPTER.quest("elf_glass_lens", q -> q
            .at(14, 22)
            .title("精灵玻璃透镜")
            .task(Task.item(TagPrefix.lens, GTOMaterials.ElfGlass, 1))
            .scaledReward(TagPrefix.plate, GTOMaterials.ElfGlass, 1)
            .dependsOn(ELF_GLASS));

    public static final Quest CRYSTAL_CIRCUITS = CHAPTER.quest("gem_crystal_circuits", q -> q
            .at(16, 22)
            .title("宝石晶体电路")
            .icon(GTOItems.RUBY_CRYSTAL_CIRCUIT)
            .task(Task.item(GTOItems.DIAMOND_CRYSTAL_CIRCUIT, 4),
                    Task.item(GTOItems.RUBY_CRYSTAL_CIRCUIT, 4),
                    Task.item(GTOItems.EMERALD_CRYSTAL_CIRCUIT, 4),
                    Task.item(GTOItems.SAPPHIRE_CRYSTAL_CIRCUIT, 4))
            .scaledReward(GTOTagPrefix.ARTIFICIAL_GEM, GTMaterials.Ruby, 1)
            .dependsOn(ARTIFICIAL_GEMS, ELF_GLASS_LENS));

    public static final Quest RAW_CRYSTAL_CHIP = CHAPTER.quest("crystal_circuit", q -> q
            .at(8, 20)
            .milestone()
            .title("晶体芯片原料")
            .task(Task.item(GTItems.RAW_CRYSTAL_CHIP, 4))
            .scaledReward(GTItems.RAW_CRYSTAL_CHIP_PART, 9)
            .dependsOn(EXQUISITE_GEMS, EUROPIUM));

    public static final Quest ENGRAVED_CRYSTAL_CHIP = CHAPTER.quest("engraved_crystal_chip", q -> q
            .at(12, 20)
            .milestone()
            .title("刻蚀水晶芯片")
            .task(Task.item(GTItems.ENGRAVED_CRYSTAL_CHIP, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Olivine, 4)
            .dependsOn(RAW_CRYSTAL_CHIP, HSSG_COIL, NewEvChapter.EARTH_ORBIT));

    public static final Quest CRYSTAL_CPU = CHAPTER.quest("crystal_cpu", q -> q
            .at(16, 20)
            .milestone()
            .title("晶体 CPU")
            .task(Task.item(GTItems.CRYSTAL_CENTRAL_PROCESSING_UNIT, 4))
            .scaledReward(TagPrefix.plate, GTMaterials.Tantalum, 4)
            .dependsOn(ENGRAVED_CRYSTAL_CHIP, CRYSTAL_CIRCUITS));

    public static final Quest SILICA_CERAMIC_DUST = CHAPTER.quest("silica_ceramic_dust", q -> q
            .at(6, 16)
            .title("氧化硅岩陶瓷粉")
            .task(Task.item(TagPrefix.dust, GTOMaterials.SilicaCeramic, 9))
            .scaledReward(GTMaterials.HydrogenPeroxide.getBucket(), 4)
            .dependsOn(ENRICHED_NAQUADAH, HYDROGEN_PEROXIDE_COPY_1));

    public static final Quest SILICA_CERAMIC_ROUGH_BLANK = CHAPTER.quest("silica_ceramic_rough_blank", q -> q
            .at(6, 14)
            .title("氧化硅岩陶瓷粗胚")
            .task(Task.item(GTOTagPrefix.ROUGH_BLANK, GTOMaterials.SilicaCeramic, 1))
            .scaledReward(GTMaterials.Epoxy.getBucket(), 1)
            .dependsOn(SILICA_CERAMIC_DUST));

    public static final Quest SILICA_CERAMIC_BLOCK = CHAPTER.quest("silica_ceramic_block", q -> q
            .at(4, 14)
            .title("氧化硅岩陶瓷块")
            .task(Task.item(TagPrefix.block, GTOMaterials.SilicaCeramic, 1))
            .scaledReward(TagPrefix.dust, GTOMaterials.SilicaCeramic, 4)
            .dependsOn(SILICA_CERAMIC_ROUGH_BLANK));

    public static final Quest SILICA_CERAMIC_FLAKE = CHAPTER.quest("silica_ceramic_flake", q -> q
            .at(4, 12)
            .title("氧化硅岩陶瓷薄片")
            .task(Task.item(GTOTagPrefix.FLAKES, GTOMaterials.SilicaCeramic, 32))
            .scaledReward(GTOTagPrefix.FLAKES, GTOMaterials.SilicaCeramic, 8)
            .dependsOn(SILICA_CERAMIC_BLOCK));

    public static final Quest MULTILAYER_FIBER_BOARD = CHAPTER.quest("multilayer_fiber_board", q -> q
            .at(6, 12)
            .title("多层纤维强化电路基板")
            .task(Task.item(GTItems.MULTILAYER_FIBER_BOARD, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.Palladium, 16)
            .dependsOn(NewIvChapter.FIBER_BOARD, NewIvChapter.PALLADIUM));

    public static final Quest ELITE_BOARD = CHAPTER.quest("elite_circuit_board", q -> q
            .at(6, 10)
            .milestone()
            .title("精英电路板")
            .task(Task.item(GTItems.ELITE_CIRCUIT_BOARD, 8))
            .scaledReward(TagPrefix.foil, GTMaterials.Platinum, 32)
            .dependsOn(SILICA_CERAMIC_FLAKE, MULTILAYER_FIBER_BOARD));

    public static final Quest HEXAFLUOROSILICIC_ACID = CHAPTER.quest("hexafluorosilicic_acid", q -> q
            .at(36, 24)
            .title("稀六氟硅酸")
            .icon(GTOMaterials.DiluteHexafluorosilicicAcid.getBucket())
            .task(Task.item(GTOMaterials.DiluteHexafluorosilicicAcid.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTMaterials.Stone, 48)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest FLUOROSILICIC_ACID = CHAPTER.quest("fluorosilicic_acid", q -> q
            .at(36, 20)
            .title("六氟硅酸")
            .icon(GTOMaterials.FluorosilicicAcid.getBucket())
            .task(Task.item(GTOMaterials.FluorosilicicAcid.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOMaterials.DiluteHexafluorosilicicAcid.getBucket(), 3)
            .dependsOn(HEXAFLUOROSILICIC_ACID));

    public static final Quest AMMONIUM_FLUORIDE = CHAPTER.quest("ammonium_fluoride", q -> q
            .at(36, 18)
            .title("氟化铵")
            .icon(GTOMaterials.AmmoniumFluoride.getBucket())
            .task(Task.item(GTOMaterials.AmmoniumFluoride.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Ammonia.getBucket(), 6)
            .dependsOn(FLUOROSILICIC_ACID));

    public static final Quest AMMONIUM_BIFLUORIDE = CHAPTER.quest("ammonium_bifluoride_solution", q -> q
            .at(34, 16)
            .title("氟氢化氨")
            .icon(GTOMaterials.AmmoniumBifluorideSolution.getBucket())
            .task(Task.item(GTOMaterials.AmmoniumBifluorideSolution.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(TagPrefix.dust, GTOMaterials.AmmoniumBifluoride, 8)
            .dependsOn(AMMONIUM_FLUORIDE));

    public static final Quest ETHYLENE_OXIDE = CHAPTER.quest("ethylene_oxide", q -> q
            .at(36, 6)
            .title("环氧乙烷")
            .icon(GTOMaterials.EthyleneOxide.getBucket())
            .task(Task.item(GTOMaterials.EthyleneOxide.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTMaterials.Ethylene.getBucket(), 4)
            .dependsOn(NewIvChapter.LUV_HULL));

    public static final Quest ETHYLENE_GLYCOL = CHAPTER.quest("ethylene_glycol", q -> q
            .at(36, 8)
            .title("乙二醇")
            .icon(GTOMaterials.EthyleneGlycol.getBucket())
            .task(Task.item(GTOMaterials.EthyleneGlycol.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOMaterials.EthyleneOxide.getBucket(), 2)
            .dependsOn(ETHYLENE_OXIDE));

    public static final Quest DIETHYLENE_GLYCOL = CHAPTER.quest("diethylene_glycol", q -> q
            .at(34, 10)
            .title("二甘醇")
            .icon(GTOMaterials.DiethyleneGlycol.getBucket())
            .task(Task.item(GTOMaterials.DiethyleneGlycol.getBucket()).optional(),
                    Task.checkmark())
            .scaledReward(GTOMaterials.EthyleneGlycol.getBucket(), 2)
            .dependsOn(ETHYLENE_GLYCOL));

    public static final Quest TITANIUM_NANOTUBE_PRECURSOR = CHAPTER.quest("titanium_nanotube_precursor", q -> q
            .at(34, 12)
            .title("钛纳米管前驱体")
            .task(Task.item(TagPrefix.dust, GTOMaterials.TitaniumNanotubePrecursor, 4))
            .scaledReward(TagPrefix.foil, GTMaterials.Titanium, 16)
            .dependsOn(AMMONIUM_BIFLUORIDE, DIETHYLENE_GLYCOL));

    public static final Quest TITANIUM_DIOXIDE_NANOTUBES = CHAPTER.quest("titanium_dioxide_nanotubes", q -> q
            .at(32, 10)
            .title("二氧化钛纳米管")
            .task(Task.item(TagPrefix.foil, GTOMaterials.TitaniumDioxideNanotubes, 4))
            .scaledReward(TagPrefix.dust, GTOMaterials.TitaniumNanotubePrecursor, 1)
            .dependsOn(TITANIUM_NANOTUBE_PRECURSOR));

    public static final Quest ADVANCED_SMD = CHAPTER.quest("advanced_smd_components", q -> q
            .at(30, 10)
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
            .at(22, 20)
            .milestone()
            .title("晶体处理器")
            .icon(GTItems.CRYSTAL_PROCESSOR_IV)
            .task(Task.item(GTMachines.CIRCUIT_ASSEMBLER[GTValues.LuV]),
                    Task.item(GTItems.CRYSTAL_PROCESSOR_IV, 6))
            .scaledReward(GTItems.ADVANCED_SMD_TRANSISTOR, 8)
            .dependsOn(ADVANCED_SMD, ELITE_BOARD, CRYSTAL_CPU, QUANTUM_MAINFRAME_COPY_1, LUV_PARTS_COPY_1));

    public static final Quest CRYSTAL_ASSEMBLY = CHAPTER.quest("crystal_processor_assembly", q -> q
            .at(26, 20)
            .milestone()
            .title("晶体处理器集群")
            .task(Task.item(GTItems.CRYSTAL_ASSEMBLY_LuV, 6))
            .scaledReward(GTItems.ADVANCED_SMD_INDUCTOR, 4)
            .dependsOn(CRYSTAL_PROCESSOR));

    public static final Quest CRYSTAL_COMPUTER = CHAPTER.quest("crystal_processor_supercomputer", q -> q
            .at(30, 20)
            .milestone()
            .title("晶体处理器超级计算机")
            .task(Task.item(GTItems.CRYSTAL_COMPUTER_ZPM, 2))
            .scaledReward(GTItems.NOR_MEMORY_CHIP, 32)
            .dependsOn(CRYSTAL_ASSEMBLY));

    static {
        CHAPTER.decorate(d -> d
                .link(QuestLink.of(NewIvChapter.LUV_HULL, 0, 0))
                .mainLine(NewIvChapter.LUV_HULL, FISSION_REACTOR, ASSEMBLY_LINE, SAMARIUM_TERBIUM_MIXTURE, VENUS, LUV_ENERGY_HATCH, CERES, POLYMERIZATION_REACTOR, ADVANCED_SMD, IO, THE_END, NEUTRON_ACTIVATOR, ENRICHED_NAQUADAH_SOLUTION, ELITE_BOARD, NAQUADAH_ALLOY, FUSION_CASING, LUV_FUSION_REACTOR, ZPM_HULL, RAW_CRYSTAL_CHIP, ENGRAVED_CRYSTAL_CHIP, CRYSTAL_CPU, CRYSTAL_PROCESSOR, CRYSTAL_ASSEMBLY, CRYSTAL_COMPUTER));
    }
}
