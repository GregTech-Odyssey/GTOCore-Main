package com.gtocore.common.data.machines;

import com.gtocore.api.machine.part.GTOPartAbility;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.common.data.translation.GTOMachineTooltipsA;
import com.gtocore.common.machine.multiblock.part.ae.*;
import com.gtocore.common.machine.multiblock.part.ae.PatternBufferType;
import com.gtocore.common.machine.noenergy.InfiniteResourceProviderMachine;
import com.gtocore.common.machine.noenergy.MEDiskBoxMachine;
import com.gtocore.common.machine.noenergy.MEVoidBoxMachine;
import com.gtocore.common.machine.noenergy.VirtualIngredientProviderMachine;
import com.gtocore.common.machine.noenergy.VirtualItemProviderMachine;

import com.gtolib.GTOCore;
import com.gtolib.api.data.storage.MEHatchMigration;
import com.gtolib.api.machine.impl.part.CraftingInterfacePartMachine;
import com.gtolib.quest.QuestAeDetectorMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.tooltip.TipRole;
import com.gregtechceu.gtceu.api.tooltip.Tips;
import com.gregtechceu.gtceu.client.renderer.machine.OverlayTieredMachineRenderer;

import net.minecraft.network.chat.Component;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gtocore.utils.register.MachineRegisterUtils.machine;
import static com.gtolib.api.registries.GTORegistration.GTM;
import static com.gtolib.api.registries.GTORegistration.GTO;

public final class GTAEMachines {

    public static void init() {}

    @Deprecated
    public static final MachineDefinition VIRTUAL_ITEM_SUPPLY_MACHINE = machine("virtual_item_supply_machine", "虚拟物品供应机", VirtualItemProviderMachine::new)
            .tier(MV)
            .allRotation()
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();

    public static final MachineDefinition VIRTUAL_INGREDIENT_PROVIDER = machine("virtual_ingredient_provider", "虚拟成分提供机", VirtualIngredientProviderMachine::new)
            .tier(MV)
            .allRotation()
            .tips(GTOMachineTooltipsA.virtualIngredientProviderTooltips)
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();

    public static final MachineDefinition INFINITE_RESOURCE_PROVIDER = machine("infinite_resource_provider", "无限资源供应器", InfiniteResourceProviderMachine::new)
            .langValue("Infinite Resource Provider")
            .tips(GTOMachineTooltipsA.infiniteResourceProviderTooltips)
            .tier(MV)
            .allRotation()
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();

    public static final MachineDefinition QUEST_AE_DETECTOR = machine("quest_ae_detector", "ME任务检测器", QuestAeDetectorMachine::new)
            .langValue("ME Quest Detector")
            .tier(LV)
            .allRotation()
            .tips(QuestAeDetectorMachine.tips())
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(LV, GTOCore.id("block/machine/part/quest_ae_detector")))
            .register();

    public static final MachineDefinition CRAFTING_CPU_INTERFACE = machine("crafting_cpu_interface", "合成CPU接口", CraftingInterfacePartMachine::new)
            .langValue("Crafting CPU Interface")
            .tier(HV)
            .allRotation()
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(HV, GTCEu.id("block/machine/part/me_pattern_buffer")))
            .register();

    public static final MachineDefinition ME_BIG_STORAGE_ACCESS_HATCH = machine("me_big_storage_access_hatch", "ME大存储访问仓", StorageAccessPartMachine::createBig)
            .langValue("ME Big Storage Access Hatch")
            .tier(IV)
            .allRotation()
            .tip(TipRole.FEATURE, "使用BigInteger存储", "Use BigInteger Storage")
            .notAllowSharedTooltips()
            .abilities(GTOPartAbility.ME_STORAGE_ACCESS)
            .renderer(() -> new OverlayTieredMachineRenderer(IV, GTCEu.id("block/machine/part/me_pattern_buffer")))
            .register();

    public static final MachineDefinition ME_IO_STORAGE_ACCESS_HATCH = machine("me_io_storage_access_hatch", "ME IO端口仓", StorageAccessPartMachine::createIO)
            .langValue("ME IO Port Hatch")
            .tier(EV)
            .allRotation()
            .tip(TipRole.FEATURE, "集成IO端口", "Integrated with IO Port")
            .tip(TipRole.STAT, "速率限制: 可配置, 默认每Tick 33,554,432 物品", "Rate Limit: configurable, default 33,554,432 Items per Tick")
            .tip(TipRole.WARNING, "小心跳电!", "Beware of power surge!")
            .notAllowSharedTooltips()
            .abilities(GTOPartAbility.ME_STORAGE_ACCESS)
            .renderer(() -> new OverlayTieredMachineRenderer(EV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();
    public static final MachineDefinition ME_STORAGE_ACCESS_HATCH = machine("me_storage_access_hatch", "ME存储访问仓", StorageAccessPartMachine::create)
            .langValue("ME Storage Access Hatch")
            .tips(GTOMachineTooltips.MEStorageAccessHatchTooltips)
            .tier(HV)
            .allRotation()
            .notAllowSharedTooltips()
            .abilities(GTOPartAbility.ME_STORAGE_ACCESS)
            .renderer(() -> new OverlayTieredMachineRenderer(HV, GTCEu.id("block/machine/part/me_pattern_buffer")))
            .register();

    public static final MachineDefinition ME_CONFIGURABLE_STORAGE_ACCESS_HATCH = machine("me_configurable_storage_access_hatch", "可配置存储访问仓", StorageAccessPartMachine::createConfigurable)
            .langValue("Configurable Storage Access Hatch")
            .tips(GTOMachineTooltips.MeConfigurableStorageAccessHatchTooltips)
            .tier(EV)
            .allRotation()
            .notAllowSharedTooltips()
            .abilities(GTOPartAbility.ME_STORAGE_ACCESS)
            .renderer(() -> new OverlayTieredMachineRenderer(EV, GTCEu.id("block/machine/part/me_pattern_buffer")))
            .register();

    public static final MachineDefinition ME_DISK_BOX = machine("me_disk_box", "ME磁盘箱子", MEDiskBoxMachine::new)
            .langValue("ME Disk Box")
            .tips(GTOMachineTooltips.MeDiskBoxTooltips)
            .tier(MV)
            .allRotation()
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();

    public static final MachineDefinition ME_VOID_BOX = machine("me_void_box", "ME虚空箱子", MEVoidBoxMachine::new)
            .langValue("ME Void Box")
            .tips(GTOMachineTooltipsA.meVoidBoxTooltips)
            .tier(MV)
            .allRotation()
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();

    public static final MachineDefinition ME_ENERGY_ACCESS_HATCH = machine("me_energy_access_hatch", "ME能量访问仓", MEEnergyAccessPartMachine::new)
            .langValue("ME Energy Access Hatch")
            .tier(EV)
            .allRotation()
            .tip(TipRole.FEATURE, "为ME网络提供能量", "Provides Energy for ME Network")
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(EV, GTCEu.id("block/machine/part/me_pattern_buffer")))
            .register();

    public static final MachineDefinition ALGAE_ACCESS_HATCH = machine("algae_access_hatch", "ME藻类访问仓", StorageAccessPartMachine::createAlgae)
            .langValue("ME Algae Access Hatch")
            .tier(EV)
            .allRotation()
            .tip(TipRole.FEATURE, "专为大型藻类养殖机器设计", "Specially for Algae Farm Machine")
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(HV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_TAG_FILTER_STOCK_BUS = MEHatchMigration.registerLegacy(GTO, "me_tag_filter_stock_bus", name -> machine(name, "ME标签过滤库存输入总线", METagFilterStockBusPartMachine::new)
            .tier(LuV)
            .abilities(PartAbility.IMPORT_ITEMS)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_item_bus.import")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_TAG_FILTER_STOCK_BUS = machine("me_tag_filter_stock_bus_v2", "ME标签过滤库存输入总线", METagFilterStockBusPartMachineV2::new)
            .tier(LuV)
            .abilities(PartAbility.IMPORT_ITEMS)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_item_bus.import")))
            .tips(Tips.feature(Component.translatable("gtceu.machine.item_bus.import.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.item_import.tooltip")),
                    Tips.rightClick(Component.translatable("gtceu.machine.me.copy_paste.tooltip")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_TAG_FILTER_STOCK_HATCH = MEHatchMigration.registerLegacy(GTO, "me_tag_filter_stock_hatch", name -> machine(name, "ME标签过滤库存输入仓", METagFilterStockHatchPartMachine::new)
            .tier(LuV)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_fluid_hatch.import")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_TAG_FILTER_STOCK_HATCH = machine("me_tag_filter_stock_hatch_v2", "ME标签过滤库存输入仓", METagFilterStockHatchPartMachineV2::new)
            .tier(LuV)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_fluid_hatch.import")))
            .tips(Tips.feature(Component.translatable("gtceu.machine.fluid_hatch.import.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.item_import.tooltip")),
                    Tips.rightClick(Component.translatable("gtceu.machine.me.copy_paste.tooltip")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_TAG_FILTER_INPUT_BUS = MEHatchMigration.registerLegacy(GTO, "me_tag_filter_input_bus", name -> machine(name, "ME标签过滤输入总线", METagFilterInputBusPartMachine::new)
            .langValue("ME Tag Filter Input Bus")
            .tier(EV)
            .abilities(PartAbility.IMPORT_ITEMS)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(EV, GTCEu.id("block/machine/part/me_item_bus.import")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_TAG_FILTER_INPUT_BUS = machine("me_tag_filter_input_bus_v2", "ME标签过滤输入总线", METagFilterInputBusPartMachineV2::new)
            .langValue("ME Tag Filter Input Bus")
            .tier(EV)
            .abilities(PartAbility.IMPORT_ITEMS)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(EV, GTCEu.id("block/machine/part/me_item_bus.import")))
            .tips(Tips.feature(Component.translatable("gtceu.machine.item_bus.import.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.item_import.tooltip")))
            .tip(TipRole.FEATURE, "按标签自动配置网络中储量最多的至多18种物品", "Automatically configures up to 18 networked item types matching the tags, most abundant first")
            .tip(TipRole.FEATURE, "已配置的种类在网络和总线内都耗尽前不会更换", "A configured type is kept until it runs out in both the network and the bus")
            .tips(Tips.rightClick(Component.translatable("gtceu.machine.me.copy_paste.tooltip")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_TAG_FILTER_INPUT_HATCH = MEHatchMigration.registerLegacy(GTO, "me_tag_filter_input_hatch", name -> machine(name, "ME标签过滤输入仓", METagFilterInputHatchPartMachine::new)
            .langValue("ME Tag Filter Input Hatch")
            .tier(EV)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(EV, GTCEu.id("block/machine/part/me_fluid_hatch.import")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_TAG_FILTER_INPUT_HATCH = machine("me_tag_filter_input_hatch_v2", "ME标签过滤输入仓", METagFilterInputHatchPartMachineV2::new)
            .langValue("ME Tag Filter Input Hatch")
            .tier(EV)
            .abilities(PartAbility.IMPORT_FLUIDS)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(EV, GTCEu.id("block/machine/part/me_fluid_hatch.import")))
            .tips(Tips.feature(Component.translatable("gtceu.machine.fluid_hatch.import.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.fluid_import.tooltip")))
            .tip(TipRole.FEATURE, "按标签自动配置网络中储量最多的至多18种流体", "Automatically configures up to 18 networked fluid types matching the tags, most abundant first")
            .tip(TipRole.FEATURE, "已配置的种类在网络和仓内都耗尽前不会更换", "A configured type is kept until it runs out in both the network and the hatch")
            .tips(Tips.rightClick(Component.translatable("gtceu.machine.me.copy_paste.tooltip")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_REQUESTABLE_INPUT_BUS_MACHINE = MEHatchMigration.registerLegacy(GTO, "me_requestable_input_bus_machine", name -> machine(name, "ME可请求输入总线", MERequestableInputBusMachine::new)
            .langValue("ME Requestable Input Bus")
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS)
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_item_bus.import")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_REQUESTABLE_INPUT_BUS_MACHINE = machine("me_requestable_input_bus_machine_v2", "ME可请求输入总线", MERequestableInputBusMachineV2::new)
            .langValue("ME Requestable Input Bus")
            .tips(GTOMachineTooltips.meRequestableInputBusTooltips)
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS)
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_item_bus.import")))
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_REQUESTABLE_INPUT_HATCH_MACHINE = MEHatchMigration.registerLegacy(GTO, "me_requestable_input_hatch_machine", name -> machine(name, "ME可请求输入仓", MERequestableInputHatchMachine::new)
            .langValue("ME Requestable Input Hatch")
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_FLUIDS)
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_fluid_hatch.import")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_REQUESTABLE_INPUT_HATCH_MACHINE = machine("me_requestable_input_hatch_machine_v2", "ME可请求输入仓", MERequestableInputHatchMachineV2::new)
            .langValue("ME Requestable Input Hatch")
            .tips(GTOMachineTooltips.meRequestableInputHatchTooltips)
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_FLUIDS)
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_fluid_hatch.import")))
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_INPUT_BUFFER_PART_MACHINE = MEHatchMigration.registerLegacy(GTO, "me_input_buffer_part_machine", name -> machine(name, "ME样板配置输入总成", MEInputBufferPartMachine::new)
            .langValue("ME Pattern-Configurable Input Buffer")
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, PartAbility.DUAL_INPUT)
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_INPUT_BUFFER_PART_MACHINE = machine("me_input_buffer_part_machine_v2", "ME样板配置输入总成", MEInputBufferPartMachineV2::new)
            .langValue("ME Pattern-Configurable Input Buffer")
            .tips(GTOMachineTooltipsA.meInputBufferPartMachineTooltips)
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, PartAbility.DUAL_INPUT)
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_CRAFT_PATTERN_PART_MACHINE = MEHatchMigration.registerLegacy(GTO, "me_craft_pattern_part_machine", name -> machine(name, "合成样板仓", MECraftPatternPartMachine::new)
            .langValue("ME Craft Pattern Hatch")
            .tier(ZPM)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(ZPM, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_CRAFT_PATTERN_PART_MACHINE = machine("me_craft_pattern_part_machine_v2", "合成样板仓", MECraftPatternPartMachineV2::new)
            .langValue("ME Craft Pattern Hatch")
            .tips(GTOMachineTooltips.MeCraftPatternHatchTooltips)
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tier(ZPM)
            .allRotation()
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(ZPM, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .tips(GTOMachineTooltips.mePatternCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_MOLECULAR_ASSEMBLY_FACTORY_PATTERN_HATCH = MEHatchMigration.registerLegacy(GTO, "molecular_assembly_factory_pattern_hatch", name -> machine(name, "分子装配工厂样板仓", h -> new MECraftPatternPartMachine(h, 18))
            .langValue("Molecular Assembly Factory Pattern Hatch")
            .tier(MV)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition MOLECULAR_ASSEMBLY_FACTORY_PATTERN_HATCH = machine("molecular_assembly_factory_pattern_hatch_v2", "分子装配工厂样板仓", h -> new MECraftPatternPartMachineV2(h, 18))
            .langValue("Molecular Assembly Factory Pattern Hatch")
            .tips(GTOMachineTooltips.MeSmallCraftPatternHatchTooltips)
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tier(MV)
            .allRotation()
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .tips(GTOMachineTooltips.mePatternCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_MOLECULAR_ASSEMBLY_FACTORY_PULL_PATTERN_HATCH = MEHatchMigration.registerLegacy(GTO, "molecular_assembly_factory_pull_pattern_hatch", name -> machine(name, "分子装配工厂拉取样板仓", h -> new MEPullCraftPatternPartMachine(h, 18))
            .langValue("Molecular Assembly Factory Pull Pattern Hatch")
            .tier(MV)
            .allRotation()
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition MOLECULAR_ASSEMBLY_FACTORY_PULL_PATTERN_HATCH = machine("molecular_assembly_factory_pull_pattern_hatch_v2", "分子装配工厂拉取样板仓", h -> new MEPullCraftPatternPartMachineV2(h, 18))
            .langValue("Molecular Assembly Factory Pull Pattern Hatch")
            .tips(GTOMachineTooltips.MePullCraftPatternHatchTooltips)
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tier(MV)
            .allRotation()
            .notAllowSharedTooltips()
            .renderer(() -> new OverlayTieredMachineRenderer(MV, GTCEu.id("block/machine/part/me_pattern_buffer_proxy")))
            .tips(GTOMachineTooltips.mePatternCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_CATALYST_ME_PATTERN_BUFFER = MEHatchMigration.registerLegacy(GTO, "me_catalyst_pattern_buffer", name -> PatternBufferType.builder(GTO, name)
            .name("ME催化剂样板总成", "ME Catalyst Pattern Buffer")
            .slots(36)
            .tier(ZPM)
            .machine(MECatalystPatternBufferPartMachine::new)
            .registerDeprecated());

    public static final MachineDefinition ME_CATALYST_ME_PATTERN_BUFFER = PatternBufferType.builder(GTO, "me_catalyst_pattern_buffer_v2")
            .name("ME催化剂样板总成", "ME Catalyst Pattern Buffer")
            .slots(36)
            .tier(ZPM)
            .machine(MECatalystPatternBufferPartMachineV2::new)
            .tips(GTOMachineTooltips.MeCatalystPatternBufferTooltips)
            .tips(GTOMachineTooltips.mePatternCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_WILDCARD_PATTERN_BUFFER = MEHatchMigration.registerLegacy(GTO, "me_wildcard_pattern_buffer", name -> PatternBufferType.builder(GTO, name)
            .name("ME通配符样板总成", "ME Wildcard Pattern Buffer")
            .slots(1)
            .tier(UHV)
            .texture(GTOCore.id("block/machine/part/me_pattern_buffer_red"))
            .machine(MEWildcardPatternBufferPartMachine::new)
            .noRecipeCacheButtons()
            .registerDeprecated());

    public static final MachineDefinition ME_WILDCARD_PATTERN_BUFFER = PatternBufferType.builder(GTO, "me_wildcard_pattern_buffer_v2")
            .name("ME通配符样板总成", "ME Wildcard Pattern Buffer")
            .slots(1)
            .tier(UHV)
            .texture(GTOCore.id("block/machine/part/me_pattern_buffer_red"))
            .machine(MEWildcardPatternBufferPartMachineV2::new)
            .tips(GTOMachineTooltips.MeWildcardPatternBufferTooltips)
            .noRecipeCacheButtons()
            .tips(GTOMachineTooltips.mePatternCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_EXTEND_PATTERN_BUFFER = MEHatchMigration.registerLegacy(GTO, "me_extend_pattern_buffer", name -> PatternBufferType.builder(GTO, name)
            .name("ME扩展样板总成", "ME Extend Pattern Buffer")
            .slots(108)
            .tier(UV)
            .machine(MEPatternBufferPartMachine::new)
            .registerDeprecated());

    public static final MachineDefinition ME_EXTEND_PATTERN_BUFFER = PatternBufferType.builder(GTO, "me_extend_pattern_buffer_v2")
            .name("ME扩展样板总成", "ME Extend Pattern Buffer")
            .slots(108)
            .tier(UV)
            .upgradable()
            .tips(GTOMachineTooltips.mePatternCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_EXTEND_PATTERN_BUFFER_ULTRA = MEHatchMigration.registerLegacy(GTO, "me_extend_pattern_buffer_ultra", name -> PatternBufferType.builder(GTO, name)
            .name("ME扩展样板总成 Ultra", "ME Extend Pattern Buffer Ultra")
            .slots(324)
            .tier(UHV)
            .machine(MEPatternBufferPartMachine::new)
            .registerDeprecated());

    public static final MachineDefinition ME_EXTEND_PATTERN_BUFFER_ULTRA = PatternBufferType.builder(GTO, "me_extend_pattern_buffer_ultra_v2")
            .name("ME扩展样板总成 Ultra", "ME Extend Pattern Buffer Ultra")
            .slots(324)
            .tier(UHV)
            .upgradable()
            .tips(GTOMachineTooltips.mePatternCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_MUFFLER_HATCH_ME = MEHatchMigration.registerLegacy(GTO, "me_muffler_hatch", name -> machine(name, "ME消声仓", MEMufflerHatchPartMachine::new)
            .langValue("ME Muffler Hatch")
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.MUFFLER)
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_item_bus.import")))
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition MUFFLER_HATCH_ME = machine("me_muffler_hatch_v2", "ME消声仓", MEMufflerHatchPartMachineV2::new)
            .langValue("ME Muffler Hatch")
            .tier(LuV)
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .allRotation()
            .abilities(PartAbility.MUFFLER)
            .renderer(() -> new OverlayTieredMachineRenderer(LuV, GTCEu.id("block/machine/part/me_item_bus.import")))
            .notAllowSharedTooltips()
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ITEM_IMPORT_BUS_ME = MEHatchMigration.registerLegacy(GTM, "me_input_bus", name -> GTM
            .machine(name, MEInputBusPartMachine::new)
            .langValue("ME Input Bus")
            .genLang("ME输入总线")
            .tier(EV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.import")
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ITEM_IMPORT_BUS_ME = GTM
            .machine("me_input_bus_v2", MEInputBusPartMachineV2::new)
            .langValue("ME Input Bus")
            .genLang("ME输入总线")
            .tier(EV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.import")
            .tips(Tips.feature(Component.translatable("gtceu.machine.item_bus.import.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.item_import.tooltip")),
                    Tips.rightClick(Component.translatable("gtceu.machine.me.copy_paste.tooltip")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_STOCKING_IMPORT_BUS_ME = MEHatchMigration.registerLegacy(GTM, "me_stocking_input_bus", name -> GTM
            .machine(name, MEStockingBusPartMachine::new)
            .langValue("ME Stocking Input Bus")
            .genLang("ME库存输入总线")
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.import")
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition STOCKING_IMPORT_BUS_ME = GTM
            .machine("me_stocking_input_bus_v2", MEStockingBusPartMachineV2::new)
            .langValue("ME Stocking Input Bus")
            .genLang("ME库存输入总线")
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.import")
            .tips(Tips.feature(Component.translatable("gtceu.machine.item_bus.import.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.stocking_item.tooltip.0")),
                    Tips.feature(Component.translatable("gtceu.machine.me_import_item_hatch.configs.tooltip")),
                    Tips.rightClick(Component.translatable("gtceu.machine.me.copy_paste.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.stocking_item.tooltip.1")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ITEM_EXPORT_BUS_ME = MEHatchMigration.registerLegacy(GTM, "me_output_bus", name -> GTM
            .machine(name, MEOutputBusPartMachine::new)
            .langValue("ME Output Bus")
            .genLang("ME输出总线")
            .tier(EV)
            .allRotation()
            .abilities(PartAbility.EXPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.export")
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ITEM_EXPORT_BUS_ME = GTM
            .machine("me_output_bus_v2", MEOutputBusPartMachineV2::new)
            .langValue("ME Output Bus")
            .genLang("ME输出总线")
            .tier(EV)
            .allRotation()
            .abilities(PartAbility.EXPORT_ITEMS)
            .overlayTieredHullRenderer("me_item_bus.export")
            .tips(Tips.feature(Component.translatable("gtceu.machine.item_bus.export.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.item_export.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.export.tooltip")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_FLUID_IMPORT_HATCH_ME = MEHatchMigration.registerLegacy(GTM, "me_input_hatch", name -> GTM
            .machine(name, MEInputHatchPartMachine::new)
            .langValue("ME Input Hatch")
            .genLang("ME输入仓")
            .tier(EV)
            .allRotation()
            .abilities(PartAbility.IMPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.import")
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition FLUID_IMPORT_HATCH_ME = GTM
            .machine("me_input_hatch_v2", MEInputHatchPartMachineV2::new)
            .langValue("ME Input Hatch")
            .genLang("ME输入仓")
            .tier(EV)
            .allRotation()
            .abilities(PartAbility.IMPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.import")
            .tips(Tips.feature(Component.translatable("gtceu.machine.fluid_hatch.import.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.fluid_import.tooltip")),
                    Tips.rightClick(Component.translatable("gtceu.machine.me.copy_paste.tooltip")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_STOCKING_IMPORT_HATCH_ME = MEHatchMigration.registerLegacy(GTM, "me_stocking_input_hatch", name -> GTM
            .machine(name, MEStockingHatchPartMachine::new)
            .langValue("ME Stocking Input Hatch")
            .genLang("ME库存输入仓")
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.import")
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition STOCKING_IMPORT_HATCH_ME = GTM
            .machine("me_stocking_input_hatch_v2", MEStockingHatchPartMachineV2::new)
            .langValue("ME Stocking Input Hatch")
            .genLang("ME库存输入仓")
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.import")
            .tips(Tips.feature(Component.translatable("gtceu.machine.fluid_hatch.import.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.stocking_fluid.tooltip.0")),
                    Tips.feature(Component.translatable("gtceu.machine.me_import_fluid_hatch.configs.tooltip")),
                    Tips.rightClick(Component.translatable("gtceu.machine.me.copy_paste.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.stocking_fluid.tooltip.1")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_FLUID_EXPORT_HATCH_ME = MEHatchMigration.registerLegacy(GTM, "me_output_hatch", name -> GTM
            .machine(name, MEOutputHatchPartMachine::new)
            .langValue("ME Output Hatch")
            .genLang("ME输出仓")
            .tier(EV)
            .allRotation()
            .abilities(PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.export")
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition FLUID_EXPORT_HATCH_ME = GTM
            .machine("me_output_hatch_v2", MEOutputHatchPartMachineV2::new)
            .langValue("ME Output Hatch")
            .genLang("ME输出仓")
            .tier(EV)
            .allRotation()
            .abilities(PartAbility.EXPORT_FLUIDS)
            .overlayTieredHullRenderer("me_fluid_hatch.export")
            .tips(Tips.feature(Component.translatable("gtceu.machine.fluid_hatch.export.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.fluid_export.tooltip")),
                    Tips.feature(Component.translatable("gtceu.machine.me.export.tooltip")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .tips(GTOMachineTooltips.AutoConnectMETooltips)
            .tips(GTOMachineTooltips.meCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_PATTERN_BUFFER = MEHatchMigration.registerLegacy(GTM, "me_pattern_buffer", name -> PatternBufferType.builder(GTM, name)
            .name("ME样板总成", "ME Pattern Buffer")
            .slots(36)
            .tier(LuV)
            .machine(MEPatternBufferPartMachine::new)
            .registerDeprecated());

    public static final MachineDefinition ME_PATTERN_BUFFER = PatternBufferType.builder(GTM, "me_pattern_buffer_v2")
            .name("ME样板总成", "ME Pattern Buffer")
            .slots(36)
            .tier(LuV)
            .upgradable()
            .tips(GTOMachineTooltips.mePatternCapacityTooltips)
            .register();

    @Deprecated
    public static final MachineDefinition LEGACY_ME_PATTERN_BUFFER_PROXY = MEHatchMigration.registerLegacy(GTM, "me_pattern_buffer_proxy", name -> GTM
            .machine(name, MEPatternBufferProxyPartMachine::new)
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, PartAbility.DUAL_INPUT)
            .allRotation()
            .overlayTieredHullRenderer("me_pattern_buffer_proxy")
            .langValue("ME Pattern Buffer Proxy")
            .genLang("ME样板总成镜像")
            .tips(GTOMachineTooltips.deprecatedMachineTooltips)
            .register());

    public static final MachineDefinition ME_PATTERN_BUFFER_PROXY = GTM
            .machine("me_pattern_buffer_proxy_v2", MEPatternBufferProxyPartMachineV2::new)
            .tier(LuV)
            .allRotation()
            .abilities(PartAbility.IMPORT_ITEMS, PartAbility.IMPORT_FLUIDS, PartAbility.DUAL_INPUT)
            .allRotation()
            .overlayTieredHullRenderer("me_pattern_buffer_proxy")
            .langValue("ME Pattern Buffer Proxy")
            .genLang("ME样板总成镜像")
            .tips(Tips.feature(Component.translatable("block.gtceu.pattern_buffer_proxy.desc.0")),
                    Tips.rightClick(Component.translatable("gtocore.machine.pattern_buffer_proxy.tooltip.0")),
                    Tips.feature(Component.translatable("block.gtceu.pattern_buffer_proxy.desc.1")),
                    Tips.lore(Component.translatable("block.gtceu.pattern_buffer_proxy.desc.2")),
                    Tips.supported(Component.translatable("gtceu.part_sharing.enabled")))
            .register();
}
