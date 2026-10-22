package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOMaterials;
import com.gtocore.common.data.machines.MultiBlockA;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Items;

import earth.terrarium.adastra.common.registry.ModItems;

import static com.gtolib.api.quest.QuestStyle.*;

public final class OresChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("ores", c -> c
            .title("矿石图鉴")
            .subtitle("Ore Cheatsheet")
            .icon(Items.IRON_ORE));

    public static final Quest PERIDOT_VEIN = CHAPTER.quest("peridot_vein", q -> q
            .at(-20, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Olivine)
            .title("橄榄石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Olivine),
                    Task.item(TagPrefix.rawOre, GTMaterials.Bentonite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Magnetite),
                    Task.item(TagPrefix.rawOre, GTMaterials.GlauconiteSand)));

    public static final Quest SAPPHIRE_VEIN = CHAPTER.quest("sapphire_vein", q -> q
            .at(-18.5, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Sapphire)
            .title("蓝宝石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Sapphire),
                    Task.item(TagPrefix.rawOre, GTMaterials.GreenSapphire),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pyrope),
                    Task.item(TagPrefix.rawOre, GTMaterials.Almandine)));

    public static final Quest TANZANITE_VEIN = CHAPTER.quest("tanzanite_vein", q -> q
            .at(-17, -5)
            .icon(TagPrefix.rawOre, GTOMaterials.Celestine)
            .title("天青石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTOMaterials.Celestine),
                    Task.item(TagPrefix.rawOre, GTMaterials.Cooperite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Trona)));

    public static final Quest IRON_VEIN = CHAPTER.quest("iron_vein", q -> q
            .at(-15.5, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Goethite)
            .title("铁矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Goethite),
                    Task.item(TagPrefix.rawOre, GTMaterials.YellowLimonite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Hematite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Malachite)));

    public static final Quest SOAPSTONE_VEIN = CHAPTER.quest("soapstone_vein", q -> q
            .at(-14, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Soapstone)
            .title("皂滑矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Soapstone),
                    Task.item(TagPrefix.rawOre, GTMaterials.Talc),
                    Task.item(TagPrefix.rawOre, GTMaterials.GlauconiteSand),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pentlandite)));

    public static final Quest PITCHBLENDE_VEIN = CHAPTER.quest("pitchblende_vein", q -> q
            .at(-12.5, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Pitchblende)
            .title("沥青铀矿脉")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Pitchblende), Task.item(TagPrefix.rawOre, GTMaterials.Uraninite)));

    public static final Quest CUPRATE_VEIN = CHAPTER.quest("cuprate_vein", q -> q
            .at(-11, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Tetrahedrite)
            .title("黝铜矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Tetrahedrite),
                    Task.item(Items.RAW_COPPER),
                    Task.item(TagPrefix.rawOre, GTMaterials.Stibnite)));

    public static final Quest MONAZITE_VEIN = CHAPTER.quest("monazite_vein", q -> q
            .at(-9.5, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Monazite)
            .title("独居石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Monazite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Bastnasite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Neodymium)));

    public static final Quest RUBY_VEIN = CHAPTER.quest("ruby_vein", q -> q
            .at(-8, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Ruby)
            .title("红宝石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Ruby),
                    Task.item(TagPrefix.rawOre, GTMaterials.Redstone),
                    Task.item(TagPrefix.rawOre, GTMaterials.Cinnabar)));

    public static final Quest TIN_COPPER_VEIN = CHAPTER.quest("tin_copper_vein", q -> q
            .at(-6.5, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Chalcopyrite)
            .title("铜锡矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Chalcopyrite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Cassiterite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Zeolite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Realgar)));

    public static final Quest NITER_VEIN = CHAPTER.quest("niter_vein", q -> q
            .at(-5, -5)
            .icon(TagPrefix.rawOre, GTMaterials.Saltpeter)
            .title("硝石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Saltpeter),
                    Task.item(TagPrefix.rawOre, GTMaterials.Diatomite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Electrotine),
                    Task.item(TagPrefix.rawOre, GTMaterials.Alunite)));

    public static final Quest COAL_VEIN = CHAPTER.quest("coal_vein", q -> q
            .at(-20, -3.5)
            .title("煤炭矿脉")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Coal)));

    public static final Quest SILICATE_VEIN = CHAPTER.quest("silicate_vein", q -> q
            .at(-18.5, -3.5)
            .icon(TagPrefix.rawOre, GTMaterials.Naquadah)
            .title("硅岩矿脉")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Naquadah), Task.item(TagPrefix.rawOre, GTMaterials.Plutonium239)));

    public static final Quest HEAT_RESISTANT_METAL_VEIN = CHAPTER.quest("heat_resistant_metal_vein", q -> q
            .at(-17, -3.5)
            .icon(ModItems.RAW_CALORITE.get())
            .title("耐热金属矿脉")
            .task(
                    Task.item(ModItems.RAW_CALORITE.get()),
                    Task.item(TagPrefix.rawOre, GTMaterials.Cobalt),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pyrochlore)));

    public static final Quest BERYLLIUM_VEIN = CHAPTER.quest("beryllium_vein", q -> q
            .at(-15.5, -3.5)
            .icon(TagPrefix.rawOre, GTMaterials.Beryllium)
            .title("铍矿脉")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Beryllium), Task.item(TagPrefix.rawOre, GTMaterials.Emerald)));

    public static final Quest COPPER_VEIN = CHAPTER.quest("copper_vein", q -> q
            .at(-14, -3.5)
            .icon(Items.RAW_COPPER)
            .title("铜矿脉")
            .task(
                    Task.item(Items.RAW_COPPER),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pyrite),
                    Task.item(Items.RAW_IRON),
                    Task.item(TagPrefix.rawOre, GTMaterials.Chalcopyrite)));

    public static final Quest LAPIS_LAZULI_VEIN = CHAPTER.quest("lapis_lazuli_vein", q -> q
            .at(-12.5, -3.5)
            .icon(TagPrefix.rawOre, GTMaterials.Lapis)
            .title("青金石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Lapis),
                    Task.item(TagPrefix.rawOre, GTMaterials.Sodalite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Lazurite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Calcite))
            .hideDependencyLines(false));

    public static final Quest MANGANESE_VEIN = CHAPTER.quest("manganese_vein", q -> q
            .at(-11, -3.5)
            .icon(TagPrefix.rawOre, GTMaterials.Pyrolusite)
            .title("锰矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Pyrolusite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Spessartine),
                    Task.item(TagPrefix.rawOre, GTMaterials.Grossular),
                    Task.item(TagPrefix.rawOre, GTMaterials.Tantalite)));

    public static final Quest PURPLE_GOLD_VEIN = CHAPTER.quest("purple_gold_vein", q -> q
            .at(-9.5, -3.5)
            .icon(ModItems.RAW_OSTRUM.get())
            .title("紫金矿脉")
            .task(Task.item(ModItems.RAW_OSTRUM.get()), Task.item(TagPrefix.rawOre, GTMaterials.Tungsten), Task.item(Items.RAW_GOLD)));

    public static final Quest WHITE_TUNGSTEN_VEIN = CHAPTER.quest("white_tungsten_vein", q -> q
            .at(-8, -3.5)
            .icon(TagPrefix.rawOre, GTMaterials.Scheelite)
            .title("白钨矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Scheelite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Tungstate),
                    Task.item(TagPrefix.rawOre, GTMaterials.Lithium)));

    public static final Quest GALENA_VEIN = CHAPTER.quest("galena_vein", q -> q
            .at(-6.5, -3.5)
            .icon(TagPrefix.rawOre, GTMaterials.Galena)
            .title("方铅矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Galena),
                    Task.item(TagPrefix.rawOre, GTMaterials.Silver),
                    Task.item(TagPrefix.rawOre, GTMaterials.Lead)));

    public static final Quest MICA_VEIN = CHAPTER.quest("mica_vein", q -> q
            .at(-5, -3.5)
            .icon(TagPrefix.rawOre, GTMaterials.Mica)
            .title("云母矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Mica),
                    Task.item(TagPrefix.rawOre, GTMaterials.Kyanite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pollucite)));

    public static final Quest TIN_ORE_VEIN = CHAPTER.quest("tin_ore_vein", q -> q
            .at(-20, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Cassiterite)
            .title("锡石矿脉")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Cassiterite), Task.item(TagPrefix.rawOre, GTMaterials.Tin)));

    public static final Quest MAGNETITE_VEIN = CHAPTER.quest("magnetite_vein", q -> q
            .at(-18.5, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Magnetite)
            .title("磁铁矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Magnetite),
                    Task.item(TagPrefix.rawOre, GTMaterials.VanadiumMagnetite),
                    Task.item(Items.RAW_GOLD)));

    public static final Quest GARNET_VEIN = CHAPTER.quest("garnet_vein", q -> q
            .at(-17, -2)
            .icon(TagPrefix.rawOre, GTMaterials.GarnetRed)
            .title("石榴石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.GarnetRed),
                    Task.item(TagPrefix.rawOre, GTMaterials.GarnetYellow),
                    Task.item(TagPrefix.rawOre, GTMaterials.Amethyst),
                    Task.item(TagPrefix.rawOre, GTMaterials.Opal)));

    public static final Quest ORE_SAND_VEIN = CHAPTER.quest("ore_sand_vein", q -> q
            .at(-15.5, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Gypsum)
            .title("矿砂矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.BasalticMineralSand),
                    Task.item(TagPrefix.rawOre, GTMaterials.GraniticMineralSand),
                    Task.item(TagPrefix.rawOre, GTMaterials.FullersEarth),
                    Task.item(TagPrefix.rawOre, GTMaterials.Gypsum)));

    public static final Quest CHROMITE_VEIN = CHAPTER.quest("chromite_vein", q -> q
            .at(-14, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Chromite)
            .title("铬铁矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Chromite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Magnetite),
                    Task.item(TagPrefix.rawOre, GTMaterials.VanadiumMagnetite)));

    public static final Quest MOLYBDENUM_VEIN = CHAPTER.quest("molybdenum_vein", q -> q
            .at(-12.5, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Molybdenum)
            .title("钼矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Molybdenum),
                    Task.item(TagPrefix.rawOre, GTMaterials.Molybdenite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Wulfenite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Powellite)));

    public static final Quest CASSITERITE_VEIN = CHAPTER.quest("cassiterite_vein", q -> q
            .at(-11, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Cooperite)
            .title("谢尔顿矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Cooperite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Bornite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Platinum),
                    Task.item(TagPrefix.rawOre, GTMaterials.Palladium)));

    public static final Quest DIAS_VEIN = CHAPTER.quest("dias_vein", q -> q
            .at(-9.5, -2)
            .icon(ModItems.RAW_DESH.get())
            .title("戴斯矿脉")
            .task(Task.item(ModItems.RAW_DESH.get()), Task.item(TagPrefix.rawOre, GTMaterials.Magnesite)));

    public static final Quest TITANIUM_VEIN = CHAPTER.quest("titanium_vein", q -> q
            .at(-8, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Titanium)
            .title("钛矿脉")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Titanium), Task.item(TagPrefix.rawOre, GTMaterials.Ilmenite)));

    public static final Quest BAOTU_VEIN = CHAPTER.quest("baotu_vein", q -> q
            .at(-6.5, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Bauxite)
            .title("铝土矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Bauxite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Ilmenite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Aluminium)));

    public static final Quest SALT_VEIN = CHAPTER.quest("salt_vein", q -> q
            .at(-5, -2)
            .icon(TagPrefix.rawOre, GTMaterials.Salt)
            .title("盐矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Salt),
                    Task.item(TagPrefix.rawOre, GTMaterials.RockSalt),
                    Task.item(TagPrefix.rawOre, GTMaterials.Lepidolite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Spodumene)));

    public static final Quest BANDED_IRON_VEIN = CHAPTER.quest("banded_iron_vein", q -> q
            .at(-20, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.Hematite)
            .title("带状铁矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Goethite),
                    Task.item(TagPrefix.rawOre, GTMaterials.YellowLimonite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Hematite),
                    Task.item(Items.RAW_GOLD)));

    public static final Quest APATITE_VEIN = CHAPTER.quest("apatite_vein", q -> q
            .at(-18.5, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.Apatite)
            .title("磷灰石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Apatite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pyrochlore),
                    Task.item(TagPrefix.rawOre, GTMaterials.TricalciumPhosphate)));

    public static final Quest TIN_GARNET_VEIN = CHAPTER.quest("tin_garnet_vein", q -> q
            .at(-17, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.GarnetSand)
            .title("锡石榴石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.CassiteriteSand),
                    Task.item(TagPrefix.rawOre, GTMaterials.GarnetSand),
                    Task.item(TagPrefix.rawOre, GTMaterials.Diatomite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Asbestos)));

    public static final Quest QUAKE_QUARTZ_VEIN = CHAPTER.quest("quake_quartz_vein", q -> q
            .at(-15.5, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.CertusQuartz)
            .title("赛特石英矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.CertusQuartz),
                    Task.item(TagPrefix.rawOre, GTMaterials.Quartzite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Barite)));

    public static final Quest HELIODOR_VEIN = CHAPTER.quest("heliodor_vein", q -> q
            .at(-14, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.Topaz)
            .title("黄玉矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Topaz),
                    Task.item(TagPrefix.rawOre, GTMaterials.BlueTopaz),
                    Task.item(TagPrefix.rawOre, GTMaterials.Chalcocite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Bornite)));

    public static final Quest OIL_SANDS_VEIN = CHAPTER.quest("oil_sands_vein", q -> q
            .at(-12.5, -0.5)
            .title("油砂矿脉")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Oilsands)));

    public static final Quest ZIRCON_VEIN = CHAPTER.quest("zircon_vein", q -> q
            .at(-11, -0.5)
            .icon(TagPrefix.rawOre, GTOMaterials.Zircon)
            .title("锆石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTOMaterials.Zircon),
                    Task.item(TagPrefix.rawOre, GTMaterials.Grossular),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pyrolusite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Tantalite)));

    public static final Quest DIAMOND_VEIN = CHAPTER.quest("diamond_vein", q -> q
            .at(-9.5, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.Diamond)
            .title("钻石矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Diamond),
                    Task.item(TagPrefix.rawOre, GTMaterials.Coal),
                    Task.item(TagPrefix.rawOre, GTMaterials.Graphite)));

    public static final Quest NICKEL_VEIN = CHAPTER.quest("nickel_vein", q -> q
            .at(-8, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.Nickel)
            .title("镍矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Nickel),
                    Task.item(TagPrefix.rawOre, GTMaterials.Garnierite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Cobaltite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pentlandite)));

    public static final Quest NETHER_QUARTZ_VEIN = CHAPTER.quest("nether_quartz_vein", q -> q
            .at(-6.5, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.NetherQuartz)
            .title("下界石英矿脉")
            .task(Task.item(TagPrefix.rawOre, GTMaterials.NetherQuartz), Task.item(TagPrefix.rawOre, GTMaterials.Quartzite)));

    public static final Quest SULFUR_VEIN = CHAPTER.quest("sulfur_vein", q -> q
            .at(-5, -0.5)
            .icon(TagPrefix.rawOre, GTMaterials.Sulfur)
            .title("硫矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Sulfur),
                    Task.item(TagPrefix.rawOre, GTMaterials.Pyrite),
                    Task.item(TagPrefix.rawOre, GTMaterials.Sphalerite)));

    public static final Quest EARLY_SOURCE_OF_INDIUM_POWDER = CHAPTER.quest("early_source_of_indium_powder", q -> q
            .at(-20, 4)
            .icon(TagPrefix.dust, GTMaterials.Indium)
            .title("前期铟粉获取途径")
            .subtitle("iv卡脖子技术")
            .desc(d -> d.line("这两个矿要单独隔离，别被矿处吃完了"))
            .task(
                    Task.item(TagPrefix.crushedPurified, GTMaterials.Galena),
                    Task.item(TagPrefix.crushedPurified, GTMaterials.Sphalerite),
                    Task.item(GTMaterials.IndiumConcentrate.getBucket())));

    public static final Quest PLATINUM_GROUP_SLURRY_PROCESSING_BRANCH = CHAPTER.quest("platinum_group_slurry_processing_branch", q -> q
            .at(-18.5, 4)
            .icon(TagPrefix.dust, GTMaterials.PlatinumGroupSludge)
            .title("铂系矿泥处理分支")
            .desc(d -> d.line("和隔壁同理，单独隔离处理"))
            .task(
                    Task.item(TagPrefix.crushedPurified, GTMaterials.Cooperite),
                    Task.item(TagPrefix.crushedPurified, GTMaterials.Chalcocite),
                    Task.item(TagPrefix.crushedPurified, GTMaterials.Bornite),
                    Task.item(TagPrefix.crushedPurified, GTMaterials.Tetrahedrite)));

    public static final Quest YOUR_SOURCE_OF_SAMARIUM_POWDER_EARLY = CHAPTER.quest("your_source_of_samarium_powder_early", q -> q
            .at(-17, 4)
            .icon(TagPrefix.dust, GTMaterials.Samarium)
            .title("你前期的钐粉来源")
            .subtitle("煮屎炉")
            .desc(d -> d
                    .line("你可能觉得合金炖X炉不够爽，那么这条线的煮X可能是你的最爱：）")
                    .line()
                    .line("{稀土处理线也在这里}", YELLOW))
            .task(Task.item(TagPrefix.rawOre, GTMaterials.Monazite), Task.item(TagPrefix.rawOre, GTMaterials.Bastnasite)));

    public static final Quest SMALL_TIPS_FOR_MINERAL_PROCESSING_TAGS = CHAPTER.quest("small_tips_for_mineral_processing_tags", q -> q
            .at(-15.5, 4)
            .icon(MultiBlockA.ADVANCED_INTEGRATED_ORE_PROCESSOR)
            .title("矿物处理标签部分小技巧")
            .subtitle("被筛选的矿是有单独标签的~")
            .desc(d -> d
                    .line("{forge:siftables}是可筛选矿物的标签", GOLD)
                    .line()
                    .line("在你寻思怎么使用标签总线处理矿物的时候，可以在矿石的配方上看EMI标签的页面，该工具可以告诉你具体的标签和所涵盖的矿石范围")
                    .line()
                    .line("每个矿石的处理步骤都有单独的矿石标签（只有粉末没有，所以需要你自己用存储总线慢慢标记了）"))
            .task(Task.checkmark()));

    public static final Quest GOOD_AT_USING_EMI_TO_CHECK = CHAPTER.quest("good_at_using_emi_to_check", q -> q
            .at(-20, -6.5)
            .icon(GTItems.PROSPECTOR_LuV)
            .title("擅于使用EMI查配方")
            .desc(d -> d.line("这里可以让你方便直接跳转对应矿脉查阅相关来源、副产以及它们的用途"))
            .task(Task.checkmark()));

    public static final Quest BORAX_ORE_VEIN = CHAPTER.quest("borax_ore_vein", q -> q
            .at(-20, 1)
            .icon(TagPrefix.rawOre, GTMaterials.Borax)
            .title("硼砂矿脉")
            .task(
                    Task.item(TagPrefix.rawOre, GTMaterials.Borax),
                    Task.item(TagPrefix.rawOre, GTMaterials.Salt),
                    Task.item(TagPrefix.rawOre, GTMaterials.RockSalt),
                    Task.item(TagPrefix.rawOre, GTMaterials.Lepidolite)));
}
