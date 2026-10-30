package com.gtocore.common.data.machines;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.client.renderer.machine.CelestialCondenserRenderer;
import com.gtocore.client.renderer.machine.CompositeMachineRenderer;
import com.gtocore.client.renderer.machine.ManaHeaterRenderer;
import com.gtocore.client.renderer.machine.OverlayManaTieredMachineRenderer;
import com.gtocore.common.cover.HeatInterfaceCover;
import com.gtocore.common.data.GTORecipeTypes;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.common.machine.generator.MagicEnergyMachine;
import com.gtocore.common.machine.generator.ManaGeneratorMachine;
import com.gtocore.common.machine.mana.*;
import com.gtocore.common.machine.mana.part.*;
import com.gtocore.config.GTORules;

import com.gtolib.GTOCore;
import com.gtolib.api.GTOValues;
import com.gtolib.api.machine.SimpleNoEnergyMachine;
import com.gtolib.api.machine.mana.ManaAmplifierPartMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.tooltip.ElectricText;
import com.gregtechceu.gtceu.api.tooltip.StatKind;
import com.gregtechceu.gtceu.api.tooltip.TipRole;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.client.renderer.machine.SimpleGeneratorMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.electric.HullMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gtocore.utils.register.MachineRegisterUtils.*;
import static com.gtolib.api.GTOValues.MANACN;
import static com.gtolib.api.GTOValues.MANAN;

public final class ManaMachine {

    public static void init() {
        ManaMultiBlock.init();
    }

    public static final MachineDefinition[] MANA_HULL = registerTieredMachines("mana_machine_hull", tier -> "%s%s".formatted(MANACN[tier], "魔法机器外壳"),
            HullMachine::new,
            (tier, builder) -> builder
                    .langValue(MANAN[tier] + " Mana Machine Hull")
                    .allRotation()
                    .abilities(GTOPartAbility.PASSTHROUGH_HATCH_MANA)
                    .renderer(() -> new OverlayManaTieredMachineRenderer(tier, GTCEu.id("block/machine/part/hull")))
                    .tips(Tips.lore(Component.translatable("gtceu.machine.hull.tooltip")))
                    .register(),
            GTValues.tiersBetween(0, 13));

    public static final MachineDefinition[] MANA_ASSEMBLER = registerSimpleManaMachines("mana_assembler", "魔力组装机", GTRecipeTypes.ASSEMBLER_RECIPES, GTMachineUtils.defaultTankSizeFunction, GTCEu.id("block/machines/assembler"), MANA_TIERS);

    public static final MachineDefinition[] PRIMITIVE_MAGIC_ENERGY = registerTieredManaMachines(
            "primitive_magic_energy", tier -> "%s原始魔法能源吸收器%s".formatted(GTOValues.VLVHCN[tier], VLVT[tier]),
            MagicEnergyMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Primitive Magic Energy%s".formatted(VLVH[tier], VLVT[tier]))
                    .nonYAxisRotation()
                    .renderer(() -> new SimpleGeneratorMachineRenderer(tier,
                            GTOCore.id("block/generators/primitive_magic_energy")))
                    .tips(Tips.warning(Component.translatable("gtocore.machine.primitive_magic_energy.tooltip.0")))
                    .tips(Tips.warning(Component.translatable("gtocore.machine.primitive_magic_energy.tooltip.1")))
                    .tips(Tips.stat(Component.translatable("gtceu.universal.tooltip.amperage_out", ElectricText.amps(GTORules.MAGIC_GENERATOR_AMPERAGE.get()))))
                    .tips(Tips.stat(Component.translatable("gtceu.universal.tooltip.voltage_out", ElectricText.voltage(V[tier], tier))))
                    .tips(Tips.energy(StatKind.ENERGY_CAPACITY, V[tier] << 8, tier))
                    .register(),
            LV, MV);

    public static final MachineDefinition[] MANA_GENERATOR = registerTieredManaMachines(
            "mana_generator", tier -> "%s%s".formatted(MANACN[tier], "魔力发生器"),
            ManaGeneratorMachine::new,
            (tier, builder) -> builder
                    .langValue(MANAN[tier] + " Mana Generator")
                    .allRotation()
                    .tips(Tips.stat(Component.translatable("gtceu.universal.tooltip.voltage_in", ElectricText.voltage(V[tier], tier))))
                    .tips(Tips.stat(Component.translatable("gtceu.universal.tooltip.amperage_in", ElectricText.amps(1))))
                    .tips(Tips.energy(StatKind.ENERGY_CAPACITY, V[tier] << 6, tier))
                    .tips(Tips.stat(Component.translatable("gtocore.machine.mana_output", Component.literal(FormattingUtil.formatNumbers(ManaGeneratorMachine.manaPerTick(tier)) + " /t").withStyle(ChatFormatting.WHITE))))
                    .tips(Tips.stat(Component.translatable("gtocore.machine.mana_stored", Component.literal(FormattingUtil.formatNumbers(ManaGeneratorMachine.MANA_CAPACITY)).withStyle(ChatFormatting.WHITE))))
                    .tip(TipRole.FEATURE, "按 1 EU : 4 魔力转换电能，每秒转换一次", "Converts energy into mana at 1 EU : 4 Mana, once per second")
                    .tip(TipRole.FEATURE, "从正面输出魔力，可接入魔力管道、魔力池或魔源罐", "Outputs mana from the front face; works with mana pipes, mana pools and source jars")
                    .workableManaTieredHullRenderer(tier, GTOCore.id("block/machines/mana_generator"))
                    .register(),
            ULV, LV, MV, HV);

    public static final MachineDefinition[] MANA_EXTRACT_HATCH = registerTieredManaMachines("mana_extract_hatch", tier -> "%s%s".formatted(MANACN[tier], "魔力抽取仓"),
            ManaExtractHatchPartMachine::new,
            (tier, builder) -> builder
                    .langValue(MANAN[tier] + " Mana Extract Hatch")
                    .allRotation()
                    .abilities(GTOPartAbility.EXTRACT_MANA)
                    .tips(Tips.stat(Component.translatable("gtocore.machine.mana_input", Component.literal(FormattingUtil.formatNumbers(GTOValues.MANA[tier] << 2) + " /t").withStyle(ChatFormatting.WHITE))))
                    .renderer(() -> new OverlayManaTieredMachineRenderer(tier, GTCEu.id("block/machine/part/" + "energy_hatch.input_64a")))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition[] MANA_INPUT_HATCH = registerTieredManaMachines("mana_input_hatch", tier -> "%s%s".formatted(MANACN[tier], "魔力输入仓"),
            (holder, tier) -> new ManaHatchPartMachine(holder, tier, IO.IN, 1),
            (tier, builder) -> builder
                    .langValue(MANAN[tier] + " Mana Input Hatch")
                    .allRotation()
                    .abilities(GTOPartAbility.INPUT_MANA)
                    .tips(Tips.stat(Component.translatable("gtocore.machine.mana_input", Component.literal(FormattingUtil.formatNumbers(4 * GTOValues.MANA[tier]) + " /t").withStyle(ChatFormatting.WHITE))))
                    .renderer(() -> new OverlayManaTieredMachineRenderer(tier, GTCEu.id("block/machine/part/" + "energy_hatch.input_64a")))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition[] MANA_OUTPUT_HATCH = registerTieredManaMachines("mana_output_hatch", tier -> "%s%s".formatted(MANACN[tier], "魔力输出仓"),
            (holder, tier) -> new ManaHatchPartMachine(holder, tier, IO.OUT, 1),
            (tier, builder) -> builder
                    .langValue(MANAN[tier] + " Mana Output Hatch")
                    .allRotation()
                    .abilities(GTOPartAbility.OUTPUT_MANA)
                    .tips(Tips.stat(Component.translatable("gtocore.machine.mana_output", Component.literal(FormattingUtil.formatNumbers(4 * GTOValues.MANA[tier]) + " /t").withStyle(ChatFormatting.WHITE))))
                    .renderer(() -> new OverlayManaTieredMachineRenderer(tier, GTCEu.id("block/machine/part/" + "energy_hatch.output_64a")))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition[] WIRELESS_MANA_INPUT_HATCH = registerTieredManaMachines("wireless_mana_input_hatch", tier -> "%s%s".formatted(MANACN[tier], "无线魔力输入仓"),
            (holder, tier) -> new WirelessManaHatchPartMachine(holder, tier, IO.IN, 1),
            (tier, builder) -> builder
                    .langValue(MANAN[tier] + " Wireless Mana Input Hatch")
                    .allRotation()
                    .abilities(GTOPartAbility.INPUT_MANA)
                    .tips(Tips.stat(Component.translatable("gtocore.machine.mana_input", Component.literal(FormattingUtil.formatNumbers(4 * GTOValues.MANA[tier]) + " /t").withStyle(ChatFormatting.WHITE))))
                    .renderer(() -> new OverlayManaTieredMachineRenderer(tier, GTOCore.id("block/machine/part/" + "wireless_mana_hatch")))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition[] WIRELESS_MANA_OUTPUT_HATCH = registerTieredManaMachines("wireless_mana_output_hatch", tier -> "%s%s".formatted(MANACN[tier], "无线魔力输出仓"),
            (holder, tier) -> new WirelessManaHatchPartMachine(holder, tier, IO.OUT, 1),
            (tier, builder) -> builder
                    .langValue(MANAN[tier] + " Wireless Mana Output Hatch")
                    .allRotation()
                    .abilities(GTOPartAbility.OUTPUT_MANA)
                    .tips(Tips.stat(Component.translatable("gtocore.machine.mana_output", Component.literal(FormattingUtil.formatNumbers(4 * GTOValues.MANA[tier]) + " /t").withStyle(ChatFormatting.WHITE))))
                    .renderer(() -> new OverlayManaTieredMachineRenderer(tier, GTOCore.id("block/machine/part/" + "wireless_mana_hatch")))
                    .register(),
            GTMachineUtils.ELECTRIC_TIERS);

    public static final MachineDefinition MANA_AMPLIFIER_HATCH = manaMachine("mana_amplifier_hatch", "魔力增幅仓", ManaAmplifierPartMachine::new)
            .tier(MV)
            .allRotation()
            .tips(GTOMachineTooltips.ManaAmplifierHatchTooltips)
            .workableManaTieredHullRenderer(6, GTOCore.id("block/multiblock/mana"))
            .register();

    public static final MachineDefinition ME_MANA_AMPLIFIER_HATCH = manaMachine("me_mana_amplifier_hatch", "ME魔力增幅仓", MEManaAmplifierPartMachine::new)
            .tier(HV)
            .allRotation()
            .tips(GTOMachineTooltips.ManaAmplifierHatchTooltips)
            .workableManaTieredHullRenderer(7, GTOCore.id("block/multiblock/mana"))
            .register();

    public static final MachineDefinition ALCHEMY_CAULDRON = manaMachine("alchemy_cauldron", "炼金锅", AlchemyCauldron::new)
            .tier(HV)
            .editableUI(SimpleNoEnergyMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("alchemy_cauldron"), GTORecipeTypes.ALCHEMY_CAULDRON_RECIPES))
            .recipeType(GTORecipeTypes.ALCHEMY_CAULDRON_RECIPES)
            .tips(GTOMachineTooltips.AlchemicalDeviceTooltips)
            .tips(GTOMachineTooltips.AlchemyCauldronTooltips)
            .tips(Tips.stat(Component.translatable("gtocore.machine.mana_input", Component.literal(GTOValues.MANA[HV] + "/t").withStyle(ChatFormatting.WHITE))))
            .tips(workableNoEnergy(GTORecipeTypes.ALCHEMY_CAULDRON_RECIPES, 1600))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.MAX_TEMPERATURE, 1600)))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.HEAT_CAPACITY, 0.5)))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.TRANSFER_RATE, 1)))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.COOLDOWN_RATE, 0.02)))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.CONSUMPTION_RATE, 0.25)))
            .nonYAxisRotation()
            .modelRenderer(() -> GTOCore.id("block/machine/alchemy_cauldron"))
            .blockProp(p -> p.noOcclusion().isViewBlocking((state, level, pos) -> false))
            .register();

    public static final MachineDefinition CELESTIAL_CONDENSER = machine("celestial_condenser", "苍穹凝聚器", CelestialCondenser::new)
            .tier(HV)
            .editableUI(SimpleNoEnergyMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("celestial_condenser"), GTORecipeTypes.CELESTIAL_CONDENSER_RECIPES))
            .recipeType(GTORecipeTypes.CELESTIAL_CONDENSER_RECIPES)
            .tips(GTOMachineTooltips.CelestialCondenserTooltips)
            .nonYAxisRotation()
            .renderer(CelestialCondenserRenderer::new)
            .hasTESR(true)
            .blockProp(p -> p.noOcclusion().isViewBlocking((state, level, pos) -> false))
            .register();

    public static final MachineDefinition MANA_HEATER = manaMachine("mana_heater", "魔力加热器", ManaHeaterMachine::new)
            .tier(MV)
            .editableUI(SimpleNoEnergyMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id("mana_heater"), GTORecipeTypes.MANA_HEATER_RECIPES))
            .recipeType(GTORecipeTypes.MANA_HEATER_RECIPES)
            .noRecipeModifier()
            .nonYAxisRotation()
            .tips(GTOMachineTooltips.ManaHeaterTooltips)
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.MAX_TEMPERATURE, 2400)))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.HEAT_CAPACITY, 4)))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.TRANSFER_RATE, 0.4)))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.COOLDOWN_RATE, 0.01)))
            .tips(Tips.stat(Component.translatable(HeatInterfaceCover.GENERATION_RATE, 1.6)))
            .renderer(() -> new ManaHeaterRenderer(MV))
            .register();

    public static final MachineDefinition SUPER_MANA_TANK = manaMachine("super_mana_tank", "超级魔力缸", SuperManaTankMachine::new)
            .tier(LV)
            .allRotation()
            .tips(Tips.stat(Component.translatable("gtocore.machine.mana_stored", Component.literal("∞").withStyle(ChatFormatting.WHITE))))
            .tip(TipRole.STAT, "内部最多存放 §f2^63-1§r 魔力", "Stores up to §f2^63-1§r mana")
            .tip(TipRole.FEATURE, "每秒自动把存量输出给相邻的魔力接收方与魔源容器", "Automatically outputs to adjacent mana receivers and source containers once per second")
            .tip(TipRole.FEATURE, "魔源按 §f4 魔源 = 1 魔力§r 折算，只输出整 4 的倍数", "Source converts at §f4 source = 1 mana§r; only whole multiples of 4 are output")
            .workableManaTieredHullRenderer(LV, GTOCore.id("block/machines/mana_tank"))
            .register();

    public static final MachineDefinition AREA_DESTRUCTION_TOOLS = machine("area_destruction_tools", "区域破坏器", AreaDestructionToolsMachine::new)
            .tier(EV)
            .tips(GTOMachineTooltips.AreaDestructionToolsTooltips)
            .nonYAxisRotation()
            .workableManaTieredHullRenderer(4, GTOCore.id("block/multiblock/area_destruction_tools"))
            .register();

    public static final MachineDefinition AE_MANA_INTERFACE = machine("me_mana_interface", "ME魔力接口", MEManaInterface::new)
            .tier(ZPM)
            .tips(GTOMachineTooltips.AEManaInterfaceTooltips)
            .allRotation()
            .workableManaTieredHullRenderer(7, GTOCore.id("block/multiblock/mana"))
            .register();

    public static final MachineDefinition XP_OBELISK = machine("exp_obelisk", "经验方尖碑", ExperienceObelisk::new)
            .tier(ULV)
            .tips(GTOMachineTooltips.experienceObeliskTooltips)
            .allRotation()
            .workableManaTieredHullRenderer(0, GTOCore.id("block/multiblock/mana"))
            .register();

    public static final MachineDefinition PULSE_CORE = machine("pulse_core", "脉冲核心", PulseMachineMaintenanceCore::new)
            .allRotation()
            .workableManaTieredHullRenderer(2, GTOCore.id("block/multiblock/pulse_core"))
            .register();

    //////////////////////////////////////
    // ********** 魔法站点单方块 **********//
    //////////////////////////////////////

    /// 魔法站点单方块：只消耗魔力运行、不接电力；配方来自对应的基础站点类型。
    /// 模型拆成父子两份：父模型 machine/magic/frame（共享框架 + 六面单面玻璃），
    /// 子模型 machine/magic/inner 下与机器 id 同名的文件（各自的原作机器模型 80%），由 CompositeMachineRenderer 合成渲染。
    private static MachineDefinition manaStation(String name, String cn, int tier, GTRecipeType recipeType) {
        return manaMachine(name, cn, holder -> new SimpleManaStationMachine(holder, tier, t -> 16000))
                .tier(tier)
                .nonYAxisRotation()
                .editableUI(SimpleNoEnergyMachine.EDITABLE_UI_CREATOR.apply(GTOCore.id(name), recipeType))
                .recipeType(recipeType)
                .noRecipeModifier()
                .renderer(() -> new CompositeMachineRenderer(GTOCore.id("block/machine/magic/frame"),
                        GTOCore.id("block/machine/magic/inner/" + name)))
                .blockProp(p -> p.noOcclusion().isViewBlocking((state, level, pos) -> false))
                .register();
    }

    // 白雏菊：MANAt 1
    public static final MachineDefinition PURE_DAISY_STATION = manaStation("pure_daisy", "白雏菊", LV, GTORecipeTypes.PURE_DAISY_RECIPES);
    // 魔力池：最高 MANAt 1000（生命精华），需要 MANA >= 1024
    public static final MachineDefinition MANA_POOL_STATION = manaStation("mana_pool", "魔力池", ZPM, GTORecipeTypes.MANA_POOL_RECIPES);
    // 花药台：MANAt 32
    public static final MachineDefinition PETAL_APOTHECARY_STATION = manaStation("petal_apothecary", "花药台", LV, GTORecipeTypes.PETAL_APOTHECARY_RECIPES);
    // 符文祭坛：最高 MANAt 40000（嬗变催化剂），需要 MANA >= 40000
    public static final MachineDefinition RUNIC_ALTAR_STATION = manaStation("runic_altar", "符文祭坛", HV, GTORecipeTypes.RUNIC_ALTAR_RECIPES);
    // 凝聚板：暂无配方，先按 EV 注册
    public static final MachineDefinition TERRA_PLATE_STATION = manaStation("terra_plate", "凝聚板", EV, GTORecipeTypes.TERRA_PLATE_RECIPES);
    // 锻造祭坛：最高 MANAt 360（英雄之魂）
    public static final MachineDefinition PEDESTAL_STATION = manaStation("pedestal", "锻造祭坛", MV, GTORecipeTypes.PEDESTAL_RECIPES);
    // 精灵门：最高 MANAt 20000（潘多拉魔盒等兑换袋）
    public static final MachineDefinition ELVEN_PORTAL_STATION = manaStation("elven_portal", "精灵门", ZPM, GTORecipeTypes.ELVEN_PORTAL_RECIPES);
    // 灌注室：最高 MANAt 400（魔源 10000 / 25）
    public static final MachineDefinition IMBUEMENT_CHAMBER_STATION = manaStation("imbuement_chamber", "灌注室", LV, GTORecipeTypes.IMBUEMENT_CHAMBER_RECIPES);
    // 附魔核心：最高 MANAt 400（魔源 10000 / 25）
    public static final MachineDefinition ENCHANTING_APPARATUS_STATION = manaStation("enchanting_apparatus", "附魔核心", HV, GTORecipeTypes.ENCHANTING_APPARATUS_RECIPES);
}
