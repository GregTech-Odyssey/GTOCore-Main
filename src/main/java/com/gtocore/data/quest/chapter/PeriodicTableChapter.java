package com.gtocore.data.quest.chapter;

import com.gtocore.common.data.GTOMaterials;

import com.gtolib.api.quest.Quest;
import com.gtolib.api.quest.QuestChapter;
import com.gtolib.api.quest.task.Task;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import earth.terrarium.adastra.common.registry.ModItems;

public final class PeriodicTableChapter {

    public static final QuestChapter CHAPTER = QuestChapter.of("periodic_table", c -> c
            .title("元素周期表")
            .subtitle("Periodic Table"));

    public static final Quest HYDROGEN_BUCKET = CHAPTER.quest("hydrogen_bucket", q -> q
            .at(-6.5, -4)
            .task(Task.item(ModItems.HYDROGEN_BUCKET.get())));

    public static final Quest LITHIUM_DUST = CHAPTER.quest("lithium_dust", q -> q
            .at(-6.5, -2.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Lithium)));

    public static final Quest SODIUM_DUST = CHAPTER.quest("sodium_dust", q -> q
            .at(-6.5, -1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Sodium)));

    public static final Quest POTASSIUM_DUST = CHAPTER.quest("potassium_dust", q -> q
            .at(-6.5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Potassium)));

    public static final Quest CAESIUM_DUST = CHAPTER.quest("caesium_dust", q -> q
            .at(-6.5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Caesium)));

    public static final Quest BERYLLIUM_DUST = CHAPTER.quest("beryllium_dust", q -> q
            .at(-5, -2.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Beryllium)));

    public static final Quest MAGNESIUM_DUST = CHAPTER.quest("magnesium_dust", q -> q
            .at(-5, -1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Magnesium)));

    public static final Quest CALCIUM_DUST = CHAPTER.quest("calcium_dust", q -> q
            .at(-5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Calcium)));

    public static final Quest BARIUM_DUST = CHAPTER.quest("barium_dust", q -> q
            .at(-5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Barium)));

    public static final Quest SCANDIUM_DUST = CHAPTER.quest("scandium_dust", q -> q
            .at(-3.5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Scandium)));

    public static final Quest YTTRIUM_DUST = CHAPTER.quest("yttrium_dust", q -> q
            .at(-3.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Yttrium)));

    public static final Quest TITANIUM_DUST = CHAPTER.quest("titanium_dust", q -> q
            .at(-2, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Titanium)));

    public static final Quest HAFNIUM_DUST = CHAPTER.quest("hafnium_dust", q -> q
            .at(-2, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Hafnium)));

    public static final Quest VANADIUM_DUST = CHAPTER.quest("vanadium_dust", q -> q
            .at(-0.5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Vanadium)));

    public static final Quest NIOBIUM_DUST = CHAPTER.quest("niobium_dust", q -> q
            .at(-0.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Niobium)));

    public static final Quest TANTALUM_DUST = CHAPTER.quest("tantalum_dust", q -> q
            .at(-0.5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tantalum)));

    public static final Quest DUBNIUM_DUST = CHAPTER.quest("dubnium_dust", q -> q
            .at(-0.5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Dubnium)));

    public static final Quest CHROMIUM_DUST = CHAPTER.quest("chromium_dust", q -> q
            .at(1, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Chromium)));

    public static final Quest MOLYBDENUM_DUST = CHAPTER.quest("molybdenum_dust", q -> q
            .at(1, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Molybdenum)));

    public static final Quest TUNGSTEN_DUST = CHAPTER.quest("tungsten_dust", q -> q
            .at(1, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tungsten)));

    public static final Quest SEABORGIUM_DUST = CHAPTER.quest("seaborgium_dust", q -> q
            .at(1, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Seaborgium)));

    public static final Quest MANGANESE_DUST = CHAPTER.quest("manganese_dust", q -> q
            .at(2.5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Manganese)));

    public static final Quest RHENIUM_DUST = CHAPTER.quest("rhenium_dust", q -> q
            .at(2.5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Rhenium)));

    public static final Quest IRON_DUST = CHAPTER.quest("iron_dust", q -> q
            .at(4, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Iron)));

    public static final Quest RUTHENIUM_DUST = CHAPTER.quest("ruthenium_dust", q -> q
            .at(4, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Ruthenium)));

    public static final Quest OSMIUM_DUST = CHAPTER.quest("osmium_dust", q -> q
            .at(4, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Osmium)));

    public static final Quest HASSIUM_DUST = CHAPTER.quest("hassium_dust", q -> q
            .at(4, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Hassium)));

    public static final Quest COBALT_DUST = CHAPTER.quest("cobalt_dust", q -> q
            .at(5.5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Cobalt)));

    public static final Quest RHODIUM_DUST = CHAPTER.quest("rhodium_dust", q -> q
            .at(5.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Rhodium)));

    public static final Quest IRIDIUM_DUST = CHAPTER.quest("iridium_dust", q -> q
            .at(5.5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Iridium)));

    public static final Quest NICKEL_DUST = CHAPTER.quest("nickel_dust", q -> q
            .at(7, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Nickel)));

    public static final Quest PALLADIUM_DUST = CHAPTER.quest("palladium_dust", q -> q
            .at(7, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Palladium)));

    public static final Quest PLATINUM_DUST = CHAPTER.quest("platinum_dust", q -> q
            .at(7, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Platinum)));

    public static final Quest DARMSTADTIUM_DUST = CHAPTER.quest("darmstadtium_dust", q -> q
            .at(7, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Darmstadtium)));

    public static final Quest COPPER_DUST = CHAPTER.quest("copper_dust", q -> q
            .at(8.5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Copper)));

    public static final Quest SILVER_DUST = CHAPTER.quest("silver_dust", q -> q
            .at(8.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Silver)));

    public static final Quest GOLD_DUST = CHAPTER.quest("gold_dust", q -> q
            .at(8.5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Gold)));

    public static final Quest GALLIUM_DUST = CHAPTER.quest("gallium_dust", q -> q
            .at(11.5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Gallium)));

    public static final Quest ALUMINIUM_DUST = CHAPTER.quest("aluminium_dust", q -> q
            .at(11.5, -1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Aluminium)));

    public static final Quest BORON_DUST = CHAPTER.quest("boron_dust", q -> q
            .at(11.5, -2.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Boron)));

    public static final Quest INDIUM_DUST = CHAPTER.quest("indium_dust", q -> q
            .at(11.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Indium)));

    public static final Quest CARBON_DUST = CHAPTER.quest("carbon_dust", q -> q
            .at(13, -2.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Carbon)));

    public static final Quest SILICON_DUST = CHAPTER.quest("silicon_dust", q -> q
            .at(13, -1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Silicon)));

    public static final Quest GERMANIUM_DUST = CHAPTER.quest("germanium_dust", q -> q
            .at(13, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Germanium)));

    public static final Quest TIN_DUST = CHAPTER.quest("tin_dust", q -> q
            .at(13, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tin)));

    public static final Quest LEAD_DUST = CHAPTER.quest("lead_dust", q -> q
            .at(13, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Lead)));

    public static final Quest NITROGEN_BUCKET = CHAPTER.quest("nitrogen_bucket", q -> q
            .at(14.5, -2.5)
            .task(Task.item(GTMaterials.Nitrogen.getBucket())));

    public static final Quest PHOSPHORUS_DUST = CHAPTER.quest("phosphorus_dust", q -> q
            .at(14.5, -1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Phosphorus)));

    public static final Quest ARSENIC_DUST = CHAPTER.quest("arsenic_dust", q -> q
            .at(14.5, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Arsenic)));

    public static final Quest ANTIMONY_DUST = CHAPTER.quest("antimony_dust", q -> q
            .at(14.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Antimony)));

    public static final Quest BISMUTH_DUST = CHAPTER.quest("bismuth_dust", q -> q
            .at(14.5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Bismuth)));

    public static final Quest OXYGEN_BUCKET = CHAPTER.quest("oxygen_bucket", q -> q
            .at(16, -2.5)
            .task(Task.item(ModItems.OXYGEN_BUCKET.get())));

    public static final Quest SULFUR_DUST = CHAPTER.quest("sulfur_dust", q -> q
            .at(16, -1)
            .task(Task.item(TagPrefix.dust, GTMaterials.Sulfur)));

    public static final Quest SELENIUM_DUST = CHAPTER.quest("selenium_dust", q -> q
            .at(16, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Selenium)));

    public static final Quest TELLURIUM_DUST = CHAPTER.quest("tellurium_dust", q -> q
            .at(16, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tellurium)));

    public static final Quest POLONIUM_DUST = CHAPTER.quest("polonium_dust", q -> q
            .at(16, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Polonium)));

    public static final Quest FLUORINE_BUCKET = CHAPTER.quest("fluorine_bucket", q -> q
            .at(17.5, -2.5)
            .task(Task.item(GTMaterials.Fluorine.getBucket())));

    public static final Quest CHLORINE_BUCKET = CHAPTER.quest("chlorine_bucket", q -> q
            .at(17.5, -1)
            .task(Task.item(GTMaterials.Chlorine.getBucket())));

    public static final Quest IODINE_DUST = CHAPTER.quest("iodine_dust", q -> q
            .at(17.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Iodine)));

    public static final Quest HELIUM_BUCKET = CHAPTER.quest("helium_bucket", q -> q
            .at(19, -4)
            .task(Task.item(GTMaterials.Helium.getBucket())));

    public static final Quest NEON_BUCKET = CHAPTER.quest("neon_bucket", q -> q
            .at(19, -2.5)
            .task(Task.item(GTMaterials.Neon.getBucket())));

    public static final Quest ARGON_BUCKET = CHAPTER.quest("argon_bucket", q -> q
            .at(19, -1)
            .task(Task.item(GTMaterials.Argon.getBucket())));

    public static final Quest KRYPTON_BUCKET = CHAPTER.quest("krypton_bucket", q -> q
            .at(19, 0.5)
            .task(Task.item(GTMaterials.Krypton.getBucket())));

    public static final Quest XENON_BUCKET = CHAPTER.quest("xenon_bucket", q -> q
            .at(19, 2)
            .task(Task.item(GTMaterials.Xenon.getBucket())));

    public static final Quest RADON_BUCKET = CHAPTER.quest("radon_bucket", q -> q
            .at(19, 3.5)
            .task(Task.item(GTMaterials.Radon.getBucket())));

    public static final Quest OGANESSON_DUST = CHAPTER.quest("oganesson_dust", q -> q
            .at(19, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Oganesson)));

    public static final Quest LANTHANUM_DUST = CHAPTER.quest("lanthanum_dust", q -> q
            .at(-2, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Lanthanum)));

    public static final Quest CERIUM_DUST = CHAPTER.quest("cerium_dust", q -> q
            .at(-0.5, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Cerium)));

    public static final Quest PRASEODYMIUM_DUST = CHAPTER.quest("praseodymium_dust", q -> q
            .at(1, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Praseodymium)));

    public static final Quest NEODYMIUM_DUST = CHAPTER.quest("neodymium_dust", q -> q
            .at(2.5, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Neodymium)));

    public static final Quest PROMETHIUM_DUST = CHAPTER.quest("promethium_dust", q -> q
            .at(4, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Promethium)));

    public static final Quest SAMARIUM_DUST = CHAPTER.quest("samarium_dust", q -> q
            .at(5.5, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Samarium)));

    public static final Quest EUROPIUM_DUST = CHAPTER.quest("europium_dust", q -> q
            .at(7, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Europium)));

    public static final Quest GADOLINIUM_DUST = CHAPTER.quest("gadolinium_dust", q -> q
            .at(8.5, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Gadolinium)));

    public static final Quest TERBIUM_DUST = CHAPTER.quest("terbium_dust", q -> q
            .at(10, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Terbium)));

    public static final Quest DYSPROSIUM_DUST = CHAPTER.quest("dysprosium_dust", q -> q
            .at(11.5, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Dysprosium)));

    public static final Quest HOLMIUM_DUST = CHAPTER.quest("holmium_dust", q -> q
            .at(13, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Holmium)));

    public static final Quest ERBIUM_DUST = CHAPTER.quest("erbium_dust", q -> q
            .at(14.5, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Erbium)));

    public static final Quest THULIUM_DUST = CHAPTER.quest("thulium_dust", q -> q
            .at(16, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Thulium)));

    public static final Quest YTTERBIUM_DUST = CHAPTER.quest("ytterbium_dust", q -> q
            .at(17.5, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Ytterbium)));

    public static final Quest LUTETIUM_DUST = CHAPTER.quest("lutetium_dust", q -> q
            .at(19, 7)
            .task(Task.item(TagPrefix.dust, GTMaterials.Lutetium)));

    public static final Quest THORIUM_DUST = CHAPTER.quest("thorium_dust", q -> q
            .at(-0.5, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Thorium)));

    public static final Quest PRASEODYMIUM_DUST_2 = CHAPTER.quest("praseodymium_dust_2", q -> q
            .at(1, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Praseodymium)));

    public static final Quest URANIUM_DUST = CHAPTER.quest("uranium_dust", q -> q
            .at(2.5, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Uranium238)));

    public static final Quest NEPTUNIUM_DUST = CHAPTER.quest("neptunium_dust", q -> q
            .at(4, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Neptunium)));

    public static final Quest PLUTONIUM_DUST = CHAPTER.quest("plutonium_dust", q -> q
            .at(5.5, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Plutonium239)));

    public static final Quest AMERICIUM_DUST = CHAPTER.quest("americium_dust", q -> q
            .at(7, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Americium)));

    public static final Quest CURIUM_DUST = CHAPTER.quest("curium_dust", q -> q
            .at(8.5, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Curium)));

    public static final Quest BERKELIUM_DUST = CHAPTER.quest("berkelium_dust", q -> q
            .at(10, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Berkelium)));

    public static final Quest CALIFORNIUM_DUST = CHAPTER.quest("californium_dust", q -> q
            .at(11.5, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Californium)));

    public static final Quest EINSTEINIUM_DUST = CHAPTER.quest("einsteinium_dust", q -> q
            .at(13, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Einsteinium)));

    public static final Quest FERMIUM_DUST = CHAPTER.quest("fermium_dust", q -> q
            .at(14.5, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Fermium)));

    public static final Quest MENDELEVIUM_DUST = CHAPTER.quest("mendelevium_dust", q -> q
            .at(16, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Mendelevium)));

    public static final Quest RUBIDIUM_DUST = CHAPTER.quest("rubidium_dust", q -> q
            .at(-6.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Rubidium)));

    public static final Quest FRANCIUM_DUST = CHAPTER.quest("francium_dust", q -> q
            .at(-6.5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Francium)));

    public static final Quest STRONTIUM_DUST = CHAPTER.quest("strontium_dust", q -> q
            .at(-5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Strontium)));

    public static final Quest RADIUM_DUST = CHAPTER.quest("radium_dust", q -> q
            .at(-5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Radium)));

    public static final Quest ZINC_DUST = CHAPTER.quest("zinc_dust", q -> q
            .at(10, 0.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Zinc)));

    public static final Quest CADMIUM_DUST = CHAPTER.quest("cadmium_dust", q -> q
            .at(10, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Cadmium)));

    public static final Quest MERCURY_BUCKET = CHAPTER.quest("mercury_bucket", q -> q
            .at(10, 3.5)
            .task(Task.item(GTMaterials.Mercury.getBucket())));

    public static final Quest ZIRCONIUM_DUST = CHAPTER.quest("zirconium_dust", q -> q
            .at(-2, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Zirconium)));

    public static final Quest RUTHERFORDIUM_DUST = CHAPTER.quest("rutherfordium_dust", q -> q
            .at(-2, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Rutherfordium)));

    public static final Quest TECHNETIUM_DUST = CHAPTER.quest("technetium_dust", q -> q
            .at(2.5, 2)
            .task(Task.item(TagPrefix.dust, GTMaterials.Technetium)));

    public static final Quest BOHRIUM_DUST = CHAPTER.quest("bohrium_dust", q -> q
            .at(2.5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Bohrium)));

    public static final Quest MEITNERIUM_DUST = CHAPTER.quest("meitnerium_dust", q -> q
            .at(5.5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Meitnerium)));

    public static final Quest ROENTGENIUM_DUST = CHAPTER.quest("roentgenium_dust", q -> q
            .at(8.5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Roentgenium)));

    public static final Quest COPERNICIUM_DUST = CHAPTER.quest("copernicium_dust", q -> q
            .at(10, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Copernicium)));

    public static final Quest THALLIUM_DUST = CHAPTER.quest("thallium_dust", q -> q
            .at(11.5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Thallium)));

    public static final Quest NIHONIUM_DUST = CHAPTER.quest("nihonium_dust", q -> q
            .at(11.5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Nihonium)));

    public static final Quest FLEROVIUM_DUST = CHAPTER.quest("flerovium_dust", q -> q
            .at(13, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Flerovium)));

    public static final Quest MOSCOVIUM_DUST = CHAPTER.quest("moscovium_dust", q -> q
            .at(14.5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Moscovium)));

    public static final Quest LIVERMORIUM_DUST = CHAPTER.quest("livermorium_dust", q -> q
            .at(16, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Livermorium)));

    public static final Quest TENNESSINE_DUST = CHAPTER.quest("tennessine_dust", q -> q
            .at(17.5, 5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tennessine)));

    public static final Quest ACTINIUM_DUST = CHAPTER.quest("actinium_dust", q -> q
            .at(-2, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Actinium)));

    public static final Quest BROMINE_BUCKET = CHAPTER.quest("bromine_bucket", q -> q
            .at(17.5, 0.5)
            .task(Task.item(GTMaterials.Bromine.getBucket())));

    public static final Quest ASTATINE_DUST = CHAPTER.quest("astatine_dust", q -> q
            .at(17.5, 3.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Astatine)));

    public static final Quest NOBELIUM_DUST = CHAPTER.quest("nobelium_dust", q -> q
            .at(17.5, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Nobelium)));

    public static final Quest LAWRENCIUM_DUST = CHAPTER.quest("lawrencium_dust", q -> q
            .at(19, 8.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Lawrencium)));

    public static final Quest JASPER_DUST = CHAPTER.quest("jasper_dust", q -> q
            .at(-6.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Jasper)));

    public static final Quest NAQUADAH_DUST = CHAPTER.quest("naquadah_dust", q -> q
            .at(-5, 10.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Naquadah)));

    public static final Quest TRITANIUM_DUST = CHAPTER.quest("tritanium_dust", q -> q
            .at(-0.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Tritanium)));

    public static final Quest DURANIUM_DUST = CHAPTER.quest("duranium_dust", q -> q
            .at(1, 10.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Duranium)));

    public static final Quest MITHRIL_DUST = CHAPTER.quest("mithril_dust", q -> q
            .at(2.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Mithril)));

    public static final Quest ORICHALCUM_DUST = CHAPTER.quest("orichalcum_dust", q -> q
            .at(4, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Orichalcum)));

    public static final Quest ENDERIUM_DUST = CHAPTER.quest("enderium_dust", q -> q
            .at(5.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Enderium)));

    public static final Quest INFUSCOLIUM_DUST = CHAPTER.quest("infuscolium_dust", q -> q
            .at(7, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Infuscolium)));

    public static final Quest ADAMANTINE_DUST = CHAPTER.quest("adamantine_dust", q -> q
            .at(8.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Adamantine)));

    public static final Quest VIBRANIUM_DUST = CHAPTER.quest("vibranium_dust", q -> q
            .at(11.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Vibranium)));

    public static final Quest TARANIUM_DUST = CHAPTER.quest("taranium_dust", q -> q
            .at(13, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Taranium)));

    public static final Quest DRACONIUM_DUST = CHAPTER.quest("draconium_dust", q -> q
            .at(14.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Draconium)));

    public static final Quest STARMETAL_DUST = CHAPTER.quest("starmetal_dust", q -> q
            .at(16, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Starmetal)));

    public static final Quest ENRICHED_NAQUADAH_DUST = CHAPTER.quest("enriched_naquadah_dust", q -> q
            .at(-3.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.NaquadahEnriched)));

    public static final Quest NAQUADRIA_DUST = CHAPTER.quest("naquadria_dust", q -> q
            .at(-2, 10.5)
            .task(Task.item(TagPrefix.dust, GTMaterials.Naquadria)));

    public static final Quest ETRIUM_DUST = CHAPTER.quest("etrium_dust", q -> q
            .at(19, 12)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Etrium)));

    public static final Quest URUIUM_DUST = CHAPTER.quest("uruium_dust", q -> q
            .at(10, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Uruium)));

    public static final Quest AMPROSIUM_DUST = CHAPTER.quest("amprosium_dust", q -> q
            .at(-6.5, 12)
            .task(Task.item(TagPrefix.dust, GTMaterials.Neutronium)));

    public static final Quest NEUTRONIUM_DUST = CHAPTER.quest("neutronium_dust", q -> q
            .at(-5, 12)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Neutron)));

    public static final Quest COSMIC_NEUTRONIUM_DUST = CHAPTER.quest("cosmic_neutronium_dust", q -> q
            .at(-3.5, 12)
            .task(Task.item(TagPrefix.dust, GTOMaterials.CosmicNeutronium)));

    public static final Quest INFINITY_DUST = CHAPTER.quest("infinity_dust", q -> q
            .at(-2, 12)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Infinity)));

    public static final Quest CHAOS_INFINITY_ALLOY_DUST = CHAPTER.quest("chaos_infinity_alloy_dust", q -> q
            .at(-0.5, 12)
            .task(Task.item(TagPrefix.dust, GTOMaterials.ChaosInfinityAlloy)));

    public static final Quest ETERNITY_DUST = CHAPTER.quest("eternity_dust", q -> q
            .at(1, 12)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Eternity)));

    public static final Quest HYPOGEN_DUST = CHAPTER.quest("hypogen_dust", q -> q
            .at(17.5, 10.5)
            .task(Task.item(TagPrefix.dust, GTOMaterials.Hypogen)));

    public static final Quest LANTHANIDE_SERIES = CHAPTER.quest("lanthanide_series", q -> q
            .at(-3.5, 3.5)
            .title("镧系")
            .task(Task.item(TagPrefix.dust, GTMaterials.Lanthanum))
            .disableToast());

    public static final Quest ACTINIUM_SERIES = CHAPTER.quest("actinium_series", q -> q
            .at(-3.5, 5)
            .title("锕系")
            .task(Task.item(TagPrefix.dust, GTMaterials.Actinium))
            .disableToast());
}
