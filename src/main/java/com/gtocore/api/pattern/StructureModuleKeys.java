package com.gtocore.api.pattern;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.machine.multiblockpro.ParamKey;
import com.gregtechceu.gtceu.api.machine.multiblockpro.PortKey;

@DataGeneratorScanned
public final class StructureModuleKeys {

    public static final PortKey MODULE_OUT = PortKey.of("module_out");
    public static final PortKey MODULE_IN = PortKey.of("module_in");
    public static final PortKey SUB_MODULE_OUT = PortKey.of("sub_module_out");
    public static final PortKey EXT_OUT = PortKey.of("ext_out");
    public static final PortKey EXT_IN = PortKey.of("ext_in");

    @RegisterLanguage(cn = "扩展模块", en = "Extension Module")
    private static final String EXTENSION_NAME = "gtocore.multiblock.extension";

    @RegisterLanguage(cn = "跨配方模块", en = "Cross-Recipe Module")
    private static final String CROSS_RECIPE_NAME = "gtocore.multiblock.cross_recipe_module";
    @RegisterLanguage(cn = "可安装线程仓与超频仓，各限一个", en = "Allows one Thread Hatch and one Overclock Hatch")
    private static final String CROSS_RECIPE_DESC = "gtocore.multiblock.cross_recipe_module.desc";
    public static final ParamKey CROSS_RECIPE_MODULE = ParamKey.of(CROSS_RECIPE_NAME, CROSS_RECIPE_DESC);

    @RegisterLanguage(cn = "可安装输入输出仓、能源仓、激光仓、线程仓、超频仓与加速仓", en = "Allows input and output hatches, Energy Hatches, Laser Hatches, a Thread Hatch, an Overclock Hatch and an Accelerate Hatch")
    private static final String DESULFURIZER_DESC = "gtocore.multiblock.desulfurizer.extension.desc";
    public static final ParamKey DESULFURIZER_EXTENSION = ParamKey.of(EXTENSION_NAME, DESULFURIZER_DESC);

    @RegisterLanguage(cn = "可安装输入输出仓、并行仓与加速仓", en = "Allows input and output hatches, a Parallel Hatch and an Accelerate Hatch")
    private static final String LIQUEFACTION_FURNACE_DESC = "gtocore.multiblock.liquefaction_furnace.extension.desc";
    public static final ParamKey LIQUEFACTION_FURNACE_EXTENSION = ParamKey.of(EXTENSION_NAME, LIQUEFACTION_FURNACE_DESC);

    @RegisterLanguage(cn = "可安装输入输出仓、并行仓与加速仓", en = "Allows input and output hatches, a Parallel Hatch and an Accelerate Hatch")
    private static final String REACTION_FURNACE_DESC = "gtocore.multiblock.reaction_furnace.extension.desc";
    public static final ParamKey REACTION_FURNACE_EXTENSION = ParamKey.of(EXTENSION_NAME, REACTION_FURNACE_DESC);

    @RegisterLanguage(cn = "研磨模块", en = "Grinding Module")
    private static final String NANO_PHAGOCYTOSIS_NAME = "gtocore.multiblock.nano_phagocytosis_plant.grinding";
    @RegisterLanguage(cn = "解锁艾萨研磨与超细研磨配方，可安装输入输出仓", en = "Unlocks Isa Mill and Ultra-Fine Grinding recipes; allows input and output hatches")
    private static final String NANO_PHAGOCYTOSIS_DESC = "gtocore.multiblock.nano_phagocytosis_plant.grinding.desc";
    public static final ParamKey NANO_PHAGOCYTOSIS_GRINDING = ParamKey.of(NANO_PHAGOCYTOSIS_NAME, NANO_PHAGOCYTOSIS_DESC);

    @RegisterLanguage(cn = "可安装输入输出仓、能源仓、激光仓与超频仓", en = "Allows input and output hatches, Energy Hatches, Laser Hatches and an Overclock Hatch")
    private static final String MOLECULAR_TRANSFORMER_DESC = "gtocore.multiblock.molecular_transformer.extension.desc";
    public static final ParamKey MOLECULAR_TRANSFORMER_EXTENSION = ParamKey.of(EXTENSION_NAME, MOLECULAR_TRANSFORMER_DESC);

    @RegisterLanguage(cn = "装配扩展段", en = "Assembly Extension")
    private static final String COMPONENT_ASSEMBLER_ASSEMBLY_NAME = "gtocore.multiblock.component_assembler.assembly";
    @RegisterLanguage(cn = "外壳等级上限由 IV 提升至 UV，可安装输入输出仓与加速仓", en = "Raises the casing tier cap from IV to UV; allows input and output hatches and an Accelerate Hatch")
    private static final String COMPONENT_ASSEMBLER_ASSEMBLY_DESC = "gtocore.multiblock.component_assembler.assembly.desc";
    public static final ParamKey COMPONENT_ASSEMBLER_ASSEMBLY = ParamKey.of(COMPONENT_ASSEMBLER_ASSEMBLY_NAME, COMPONENT_ASSEMBLER_ASSEMBLY_DESC);

    @RegisterLanguage(cn = "能源扩展段", en = "Power Extension")
    private static final String COMPONENT_ASSEMBLER_POWER_NAME = "gtocore.multiblock.component_assembler.power";
    @RegisterLanguage(cn = "需先搭建装配扩展段，可安装激光仓（最多两个）与并行仓", en = "Requires the Assembly Extension; allows up to two Laser Hatches and a Parallel Hatch")
    private static final String COMPONENT_ASSEMBLER_POWER_DESC = "gtocore.multiblock.component_assembler.power.desc";
    public static final ParamKey COMPONENT_ASSEMBLER_POWER = ParamKey.of(COMPONENT_ASSEMBLER_POWER_NAME, COMPONENT_ASSEMBLER_POWER_DESC);

    @RegisterLanguage(cn = "雾化冷凝模块", en = "Atomization Condensation Module")
    private static final String COLD_ICE_FREEZER_NAME = "gtocore.multiblock.cold_ice_freezer.atomization";
    @RegisterLanguage(cn = "解锁雾化冷凝配方，可安装能源仓（最多六个）与加速仓", en = "Unlocks Atomization Condensation recipes; allows up to six Energy Hatches and an Accelerate Hatch")
    private static final String COLD_ICE_FREEZER_DESC = "gtocore.multiblock.cold_ice_freezer.atomization.desc";
    public static final ParamKey COLD_ICE_FREEZER_ATOMIZATION = ParamKey.of(COLD_ICE_FREEZER_NAME, COLD_ICE_FREEZER_DESC);

    @RegisterLanguage(cn = "每级并行数翻倍，可安装输入输出仓、能源仓、催化剂仓与魔力增幅仓", en = "Doubles the parallels per tier; allows input and output hatches, Energy Hatches, a Catalyst Hatch and a Mana Amplifier Hatch")
    private static final String PROCESSING_PLANT_DESC = "gtocore.multiblock.processing_plant.extension.desc";
    public static final ParamKey PROCESSING_PLANT_EXTENSION = ParamKey.of(EXTENSION_NAME, PROCESSING_PLANT_DESC);

    @RegisterLanguage(cn = "扩展罐体", en = "Extended Tank")
    private static final String DISSOLVING_TANK_NAME = "gtocore.multiblock.dissolving_tank.extension";
    @RegisterLanguage(cn = "配方不再要求输入流体符合配方比例，可安装能源仓（最多六个）与加速仓", en = "Recipes no longer require the input fluid ratio to match; allows up to six Energy Hatches and an Accelerate Hatch")
    private static final String DISSOLVING_TANK_DESC = "gtocore.multiblock.dissolving_tank.extension.desc";
    public static final ParamKey DISSOLVING_TANK_EXTENSION = ParamKey.of(DISSOLVING_TANK_NAME, DISSOLVING_TANK_DESC);

    @RegisterLanguage(cn = "可安装输入输出仓、能源仓（最多两个）与加速仓", en = "Allows input and output hatches, up to two Energy Hatches and an Accelerate Hatch")
    private static final String CRYSTALLIZATION_CHAMBER_DESC = "gtocore.multiblock.crystallization_chamber.extension.desc";
    public static final ParamKey CRYSTALLIZATION_CHAMBER_EXTENSION = ParamKey.of(EXTENSION_NAME, CRYSTALLIZATION_CHAMBER_DESC);

    @RegisterLanguage(cn = "AE 能量转换倍率翻倍，可安装激光仓（最多四个）", en = "Doubles the AE energy conversion rate; allows up to four Laser Hatches")
    private static final String ME_ENERGY_SUBSTATION_DESC = "gtocore.multiblock.me_energy_substation.extension.desc";
    public static final ParamKey ME_ENERGY_SUBSTATION_EXTENSION = ParamKey.of(EXTENSION_NAME, ME_ENERGY_SUBSTATION_DESC);

    @RegisterLanguage(cn = "扩展炉体", en = "Furnace Extension")
    private static final String ELECTRIC_BLAST_FURNACE_NAME = "gtocore.multiblock.electric_blast_furnace.extension";
    @RegisterLanguage(cn = "搭建后可在其中额外安装输入输出仓、最多 2 个能源仓和 1 个加速仓，不影响配方运行", en = "When built, additional input/output hatches, up to 2 energy hatches and 1 acceleration hatch can be installed in it; recipe processing is not affected")
    private static final String ELECTRIC_BLAST_FURNACE_DESC = "gtocore.multiblock.electric_blast_furnace.extension.desc";
    public static final ParamKey ELECTRIC_BLAST_FURNACE_EXTENSION = ParamKey.of(ELECTRIC_BLAST_FURNACE_NAME, ELECTRIC_BLAST_FURNACE_DESC);

    @RegisterLanguage(cn = "加热扩展段", en = "Heating Extension")
    private static final String EVAPORATION_NAME = "gtocore.multiblock.evaporation_plant.extension";
    @RegisterLanguage(cn = "搭建后可在其外壳上额外安装输入输出仓、并行仓与加速仓", en = "When built, extra input/output hatches, a parallel hatch and an accelerate hatch can be installed on its casings")
    private static final String EVAPORATION_DESC = "gtocore.multiblock.evaporation_plant.extension.desc";
    public static final ParamKey EVAPORATION_EXTENSION = ParamKey.of(EVAPORATION_NAME, EVAPORATION_DESC);

    @RegisterLanguage(cn = "约束扩展段", en = "Restraint Extension")
    private static final String MATTER_FABRICATOR_NAME = "gtocore.multiblock.matter_fabricator.extension";
    @RegisterLanguage(cn = "搭建后可在其外壳上额外安装输入输出仓与超频仓", en = "When built, extra input/output hatches and an overclock hatch can be installed on its casings")
    private static final String MATTER_FABRICATOR_DESC = "gtocore.multiblock.matter_fabricator.extension.desc";
    public static final ParamKey MATTER_FABRICATOR_EXTENSION = ParamKey.of(MATTER_FABRICATOR_NAME, MATTER_FABRICATOR_DESC);

    @RegisterLanguage(cn = "多线程扩展段", en = "Multithreading Extension")
    private static final String PETROCHEMICAL_PLANT_NAME = "gtocore.multiblock.petrochemical_plant.extension";
    @RegisterLanguage(cn = "搭建后可在其外壳上安装线程仓与超频仓", en = "When built, a thread hatch and an overclock hatch can be installed on its casings")
    private static final String PETROCHEMICAL_PLANT_DESC = "gtocore.multiblock.petrochemical_plant.extension.desc";
    public static final ParamKey PETROCHEMICAL_PLANT_EXTENSION = ParamKey.of(PETROCHEMICAL_PLANT_NAME, PETROCHEMICAL_PLANT_DESC);

    @RegisterLanguage(cn = "激光约束段", en = "Laser Containment Extension")
    private static final String IMPLOSION_NAME = "gtocore.multiblock.electric_implosion_compressor.extension";
    @RegisterLanguage(cn = "搭建后可在其外壳上额外安装激光仓、输入输出仓与超频仓", en = "When built, laser hatches, extra input/output hatches and an overclock hatch can be installed on its casings")
    private static final String IMPLOSION_DESC = "gtocore.multiblock.electric_implosion_compressor.extension.desc";
    public static final ParamKey IMPLOSION_EXTENSION = ParamKey.of(IMPLOSION_NAME, IMPLOSION_DESC);

    @RegisterLanguage(cn = "塔节层数", en = "Tower Layers")
    private static final String DISTILLATION_TOWER_LAYERS_NAME = "gtocore.multiblock.distillation_tower.layers";
    @RegisterLanguage(cn = "底座与塔顶之间的塔节层数", en = "Number of tower layers between the base and the top")
    private static final String DISTILLATION_TOWER_LAYERS_DESC = "gtocore.multiblock.distillation_tower.layers.desc";
    public static final ParamKey DISTILLATION_TOWER_LAYERS = ParamKey.of(DISTILLATION_TOWER_LAYERS_NAME, DISTILLATION_TOWER_LAYERS_DESC);

    @RegisterLanguage(cn = "蒸馏层数", en = "Distillation layers")
    private static final String LARGE_DISTILLERY_LAYERS_NAME = "gtocore.multiblock.large_distillery.layers";
    @RegisterLanguage(cn = "控制器层与塔顶之间的蒸馏层数，每层最多一个流体输出仓", en = "Distillation layers between the controller layer and the top, each holding at most one fluid output hatch")
    private static final String LARGE_DISTILLERY_LAYERS_DESC = "gtocore.multiblock.large_distillery.layers.desc";
    public static final ParamKey LARGE_DISTILLERY_LAYERS = ParamKey.of(LARGE_DISTILLERY_LAYERS_NAME, LARGE_DISTILLERY_LAYERS_DESC);

    @RegisterLanguage(cn = "塔节层数", en = "Tower Layers")
    private static final String PRIMITIVE_DISTILLATION_TOWER_LAYERS_NAME = "gtocore.multiblock.primitive_distillation_tower.layers";
    @RegisterLanguage(cn = "底座与塔顶之间的塔节层数", en = "Number of tower layers between the base and the top")
    private static final String PRIMITIVE_DISTILLATION_TOWER_LAYERS_DESC = "gtocore.multiblock.primitive_distillation_tower.layers.desc";
    public static final ParamKey PRIMITIVE_DISTILLATION_TOWER_LAYERS = ParamKey.of(PRIMITIVE_DISTILLATION_TOWER_LAYERS_NAME, PRIMITIVE_DISTILLATION_TOWER_LAYERS_DESC);

    @RegisterLanguage(cn = "装配节数", en = "Assembly Slices")
    private static final String ADVANCED_ASSEMBLY_LINE_LAYERS_NAME = "gtocore.multiblock.advanced_assembly_line.layers";
    @RegisterLanguage(cn = "控制器切片与末端切片之间的装配节数", en = "Number of assembly slices between the controller slice and the end slice")
    private static final String ADVANCED_ASSEMBLY_LINE_LAYERS_DESC = "gtocore.multiblock.advanced_assembly_line.layers.desc";
    public static final ParamKey ADVANCED_ASSEMBLY_LINE_LAYERS = ParamKey.of(ADVANCED_ASSEMBLY_LINE_LAYERS_NAME, ADVANCED_ASSEMBLY_LINE_LAYERS_DESC);

    @RegisterLanguage(cn = "存储层数", en = "Storage Layers")
    private static final String ME_STORAGE_LAYERS_NAME = "gtocore.multiblock.me_storage.layers";
    @RegisterLanguage(cn = "前端与尾端之间的存储层数", en = "Number of storage layers between the front section and the end")
    private static final String ME_STORAGE_LAYERS_DESC = "gtocore.multiblock.me_storage.layers.desc";
    public static final ParamKey ME_STORAGE_LAYERS = ParamKey.of(ME_STORAGE_LAYERS_NAME, ME_STORAGE_LAYERS_DESC);

    @RegisterLanguage(cn = "储能层数", en = "Storage Layers")
    private static final String WIRELESS_ENERGY_SUBSTATION_LAYERS_NAME = "gtocore.multiblock.wireless_energy_substation.layers";
    @RegisterLanguage(cn = "底板与顶板之间的储能层数", en = "Number of storage layers between the floor and the roof")
    private static final String WIRELESS_ENERGY_SUBSTATION_LAYERS_DESC = "gtocore.multiblock.wireless_energy_substation.layers.desc";
    public static final ParamKey WIRELESS_ENERGY_SUBSTATION_LAYERS = ParamKey.of(WIRELESS_ENERGY_SUBSTATION_LAYERS_NAME, WIRELESS_ENERGY_SUBSTATION_LAYERS_DESC);

    private StructureModuleKeys() {}
}
