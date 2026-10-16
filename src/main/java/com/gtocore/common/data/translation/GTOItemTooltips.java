package com.gtocore.common.data.translation;

import com.gtocore.api.lang.GTOTips;
import com.gtocore.api.lang.Tx;
import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOItems;
import com.gtocore.common.machine.multiblock.generator.BioOscillationElectricStimulator;
import com.gtocore.common.machine.multiblock.generator.BioOscillationGenerator;
import com.gtocore.config.GTOConfig;

import com.gtolib.api.item.IItem;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.tooltip.ElectricText;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.SteamHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.steam.SteamParallelMultiblockMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import appeng.block.crafting.ICraftingUnitType;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;

import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import com.hepdd.gtmthings.data.CustomItems;
import com.hepdd.gtmthings.data.CustomMachines;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import earth.terrarium.adastra.common.registry.ModBlocks;

import java.util.List;

import static com.direwolf20.buildinggadgets2.setup.Registration.TemplateManager;
import static vazkii.botania.common.block.BotaniaBlocks.fabulousPool;

public final class GTOItemTooltips {

    public static void init() {}

    public static final TipSpec DeprecatedItemTooltips = GTOTips.of(t -> t.attention(Component.translatable("gtocore.item.deprecated")));

    public static final TipSpec SpeedUpgradeModuleTooltips = GTOTips.of("upgrade_module", t -> {
        t.bonus(t.tr("提升机器运作速度", "Speed up machine operation"));
        t.formula(t.tr("倍率公式：首次升级为 max(0.5, R)，后续升级为 max(0.5, 当前速度倍率 × √R)", "Multiplier formula: max(0.5, R) for the first upgrade; max(0.5, current speed multiplier × √R) for subsequent upgrades"));
        t.formula(t.tr("随机系数 R = 1 - random[0,1) × min(0.5, 经验等级 / 200)", "Random factor R = 1 - random[0,1) × min(0.5, experience level / 200)"));
    });

    public static final TipSpec EnergyUpgradeModuleTooltips = GTOTips.of("upgrade_module", t -> {
        t.bonus(t.tr("降低机器功耗", "Reduce machine power consumption"));
        t.formula(t.tr("倍率公式：首次升级为 max(0.5, R)，后续升级为 max(0.5, 当前功耗倍率 × √R)", "Multiplier formula: max(0.5, R) for the first upgrade; max(0.5, R × √current power multiplier) for subsequent upgrades"));
        t.formula(t.tr("随机系数 R = 1 - random[0,1) × min(0.5, 经验等级 / 200)", "Random factor R = 1 - random[0,1) × min(0.5, experience level / 200)"));
    });

    public static final TipSpec PatternModifierTooltips = GTOTips.of("pattern_modifier", t -> {
        t.heading(t.tr("便捷地修改样板", "Modify patterns easily"));
        t.feature(t.tr("它可以自动翻倍样板，不用任何操作", "It can automatically double the pattern, no operation is required"));
        t.feature(ComponentSlang.RecommendedToUse(t.tr("样板总成", "Pattern Buffer")));
        t.bonus(t.tr("PRO版本可以批量应用", "PRO version can apply in batch"));
    });

    public static final TipSpec OrderTooltips = GTOTips.of("order", t -> {
        miraculousTools(t, t.tr("AE2 订单", "AE2 Order"));
        t.heading(ComponentSlang.MainFunction);
        t.keepOrder();
        t.rightClick(t.tr("右键可以放入一个虚拟物品，例如多方块主机", "Right click to put a virtual item, such as a multi-block machine"));
        t.feature(t.tr("不需要再在铁砧使用告示牌命名", "No longer need to use a sign to name it in anvil"));
        t.feature(t.tr("可以作为AE自动合成的大型机器产物", "Can be used as a large machine product for AE2 automatic crafting"));
        t.feature(t.tr("当此合成完成时，会自动取消，无需手动取消", "When the crafting is completed, it will automatically cancel, no need to cancel manually"));
    });

    public static final TipSpec GrassHarvesterTooltips = GTOTips.of("grass_harvester", t -> {
        miraculousTools(t, t.tr("割草镰刀", "Grass Harvester"));
        t.heading(ComponentSlang.MainFunction);
        t.keepOrder();
        t.bonus(t.tr("极大地提升小麦种子掉落概率", "Greatly increases the drop rate of wheat seeds"));
        t.rightClick(t.tr("右键草以收割小麦种子和稀有作物", "Right-click grass to harvest wheat seeds and rare crops"));
        t.feature(t.tr("前期大量获取种子去种地的好帮手", "A good helper for obtaining seeds in large quantities in the early game"));
    });

    public static final TipSpec InfiniteSprayCanTooltips = GTOTips.of(t -> {
        t.heading(Tx.gold(Component.literal("妙妙工具: 无尽喷漆罐")));
        t.keepOrder();
        t.rightClick(Component.literal("对方块右键：喷涂"));
        t.rightClick(Component.literal("Shift+右键：打开调色板"));
        t.control(Component.literal("潜行+滚轮：切换颜色"));
        t.control(Component.literal("按住连锁键：连锁喷涂"));
    });

    public static final TipSpec TravelStaffTooltips = GTOTips.of("the_staff_of_travelling", t -> {
        t.lore(t.tr("末影接口经典款式传送工具仿制品", "A replica of the classic teleportation tool of the Ender IO Inc."));
        t.heading(ComponentSlang.MainFunction);
        t.keepOrder();
        t.rightClick(t.tr("右键：传送到准星对准的目标", "Right click: teleport to the target under the crosshair"));
        t.rightClick(t.tr("Shift + 右键：向视线方向短距离折跃", "Shift + right click: blink a short distance forward"));
        t.leftClick(t.tr("左键：设置哪些目标可以传送", "Left click: choose which targets can be teleported to"));
    });

    public static final TipSpec MEWirelessMachineConfigurator = GTOTips.of("me_wireless_machine_configurator", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.rightClick(t.tr("右键空气打开界面配置目标网络", "Right-click air to open the interface and configure the target network"));
        t.rightClick(t.tr("右键ME无线机器可以让该机器加入目标网络", "Right-click a ME wireless machine to make it join the target network"));
        t.rightClick(t.tr("Shift+右键ME无线机器可以导入该机器的网络配置", "Shift + Right-click a ME wireless machine to import its network configuration"));
    });

    public static final TipSpec TimeTwisterTooltips = GTOTips.of("time_twister", t -> {
        miraculousTools(t, t.tr("时间扭曲者", "Time Twister"));
        t.heading(t.tr("启动加速", "Acceleration for normal block entities"));
        t.control(t.tr("普通点击：消耗8192 EU能量，加速一次", "Normal click: Consume 8192 EU energy, accelerate once"));
        t.control(t.tr("Shift点击：消耗819200 EU能量，持续100刻内加速目标方块", "Shift click: Consume 819200 EU energy, accelerate the target block for 100 ticks"));
        t.heading(t.tr("加速方式", "Acceleration methods"));
        t.feature(t.tr("普通机器：不额外消耗EU能量，每次200tick", "For normal machines: No extra EU consumption, 200 ticks per acceleration"));
        t.bonus(t.tr("GT机器：每次使当前正在工作的机器进度立即增加最多50%", "For GT machines: immediately increase current progress by up to 50% per use"));
        t.heading(t.tr("能量消耗", "Energy consumption:"));
        t.feature(t.tr("使用无线能量系统作为能量来源", "Use wireless energy system as energy source"));
        t.feature(t.tr("不同操作消耗不同数量的EU", "Different operations consume different amounts of EU"));
        t.attention(t.tr("加速GT机器时，EU能量消耗会乘以难度模式对应的数值", "When accelerating GT machines, consume EU energy according to the difficulty mode"));
    });

    public static final TipSpec PalmSizedBankTooltips = GTOTips.of("item.palm_sized_bank", t -> {
        miraculousTools(t, t.tr("泛银河系格雷科技掌上银行", "Pan-Galactic Grey Technology Palm-Sized Bank"));
        t.keepOrder();
        t.lore(t.tr("一款集成了跨星系金融服务的便携式终端，由格雷科技星际金融部研发。", "A portable terminal integrated with interstellar financial services, developed by Grey Technology Interstellar Finance Division."));
        t.feature(t.tr("支持跨星系账户实时同步，无论身处哪个殖民星均可管理资产。", "Supports real-time synchronization of interstellar accounts, allowing asset management across any colony."));
        t.feature(t.tr("采用量子加密技术，账户信息无法被破解或篡改，安全等级达到星系标准。", "Uses quantum encryption technology; account information cannot be hacked or tampered with, meeting galactic security standards."));
        t.feature(t.tr("内置能量核心，无需额外供电，可持续运行 730 标准日。", "Built-in energy core, no external power required, can operate continuously for 730 standard days."));
        t.rightClick(t.tr("右键打开银行界面，支持存款、取款及向其他认证账户转账。", "Right-click to open the bank interface, supporting deposit, withdrawal, and transfer to other certified accounts."));
        t.warning(t.tr("请勿向未认证账户转账，星际金融法对跨境诈骗有严格处罚。", "Do not transfer to uncertified accounts; interstellar financial laws have strict penalties for cross-border fraud."));
    });

    public static final TipSpec MEExportBufferTooltips = GTOTips.of(t -> {
        t.feature(Component.translatable("gtceu.machine.dual_hatch.export.tooltip"));
        t.feature(Component.translatable("gtceu.machine.me.export.tooltip"));
        t.supported(Component.translatable("gtceu.part_sharing.enabled"));
        t.include(GTOMachineTooltips.AutoConnectMETooltips);
        t.edition();
    });

    public static final TipSpec PowerSubstationTooltips = GTOTips.of(t -> {
        t.feature(Component.translatable("gtocore.lang.power_substation.tooltip.2"));
        t.edition();
    });

    public static final TipSpec SteamHatchTooltips = GTOTips.of(t -> {
        t.statText(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", FormattingUtil.formatNumbers(SteamHatchPartMachine.INITIAL_TANK_CAPACITY)));
        t.feature(Component.translatable("gtceu.machine.steam.steam_hatch.tooltip"));
        t.statText(Component.translatable("gtocore.machine.conversion_rate", Component.literal(FormattingUtil.formatNumbers(SteamParallelMultiblockMachine.CONVERSION_RATE)).withStyle(ChatFormatting.RED)));
    });

    public static void initLanguage() {
        setTips(GTItems.INFINITE_SPRAY_CAN.asItem(), InfiniteSprayCanTooltips);
        setTips(AEParts.STORAGE_BUS.asItem(), GTOTips.of("storage_bus", t -> {
            t.heading(t.tr("与存储设备进行交互", "Interact with storage devices"));
            t.feature(t.tr("经过优化，吞吐量性能卓越", "Throughput performance is excellent"));
            t.edition();
        }));
        setTips(AEBlocks.CONDENSER.asItem(), GTOTips.of("matter_condenser", t -> t.lore(t.tr("近年垃圾回收商引起的一次事故让大家把目光投向了这台物质聚合器。", "A recent incident involving a waste recycler has turned people's attention to this Matter Condenser."))));

        setTips(EPPItemAndBlock.PATTERN_MODIFIER.asItem(), PatternModifierTooltips);

        for (Item item : List.of(EPPItemAndBlock.TAG_STORAGE_BUS.asItem(), EPPItemAndBlock.MOD_STORAGE_BUS.asItem(), EPPItemAndBlock.PRECISE_STORAGE_BUS)) {
            setTips(item, GTOTips.of("pattern_modifier", t -> {
                t.heading(t.tr("与存储设备进行交互", "Interact with storage devices"));
                t.supported(Tx.join(t.tr("仅支持读取", "Only supports reading "), Tx.gold(t.tr("物品和流体", "items and fluids ")), t.tr("两种类型", "two types")));
                t.feature(Tx.join(t.tr("自动化中，", "In automation, "), ComponentSlang.RecommendedToUse(t.tr("存储总线", "Storage Bus"))));
                t.feature(t.tr("经过优化，吞吐量性能卓越", "Throughput performance is excellent"));
                t.edition();
            }));
        }

        setTips(EPPItemAndBlock.PRECISE_EXPORT_BUS.asItem(), GTOTips.of("precise_export_bus", t -> t.attention(t.tr("提示：当设定的精确输出数量大于输出速度承载时，将停止工作", "Note: The bus stops working when the configured exact amount exceeds what its export speed can carry"))));

        for (Item item : List.of(
                AEItems.ITEM_CELL_1K.asItem(),
                AEItems.ITEM_CELL_4K.asItem(),
                AEItems.ITEM_CELL_16K.asItem(),
                AEItems.ITEM_CELL_64K.asItem(),
                AEItems.FLUID_CELL_1K.asItem(),
                AEItems.FLUID_CELL_4K.asItem(),
                AEItems.FLUID_CELL_16K.asItem(),
                AEItems.FLUID_CELL_64K.asItem())) {
            setTips(item, GTOTips.of("me_storage_cell", t -> {
                t.bonus(t.tr("存储容量最高是原来的二倍", "Storage capacity is doubled compared to the original"));
                t.edition();
            }));
        }

        craftingStorageTips(AEBlocks.CRAFTING_STORAGE_1K.asItem(), AEBlocks.CRAFTING_STORAGE_1K.block().type);
        craftingStorageTips(AEBlocks.CRAFTING_STORAGE_4K.asItem(), AEBlocks.CRAFTING_STORAGE_4K.block().type);
        craftingStorageTips(AEBlocks.CRAFTING_STORAGE_16K.asItem(), AEBlocks.CRAFTING_STORAGE_16K.block().type);
        craftingStorageTips(AEBlocks.CRAFTING_STORAGE_64K.asItem(), AEBlocks.CRAFTING_STORAGE_64K.block().type);
        craftingStorageTips(AEBlocks.CRAFTING_STORAGE_256K.asItem(), AEBlocks.CRAFTING_STORAGE_256K.block().type);
        craftingStorageTips(AEBlocks.CRAFTING_ACCELERATOR.asItem(), AEBlocks.CRAFTING_ACCELERATOR.block().type);
        craftingStorageTips(AEBlocks.CRAFTING_UNIT.asItem(), AEBlocks.CRAFTING_UNIT.block().type);
        craftingStorageTips(AEBlocks.CRAFTING_MONITOR.asItem(), AEBlocks.CRAFTING_MONITOR.block().type);

        bomb(GTOBlocks.NAQUADRIA_CHARGE.get(), GTItems.QUANTUM_STAR.asItem());
        bomb(GTOBlocks.LEPTONIC_CHARGE.get(), GTItems.GRAVI_STAR.asItem());
        bomb(GTOBlocks.QUANTUM_CHROMODYNAMIC_CHARGE.get(), GTOItems.UNSTABLE_STAR.asItem());

        for (Item item : List.of(AEItems.ITEM_CELL_256K.asItem(), AEItems.FLUID_CELL_256K.asItem())) {
            setTips(item, GTOTips.of("me_storage_cell", t -> {
                t.bonus(t.tr("存储容量最高是原来的二倍", "Storage capacity is doubled compared to the original"));
                t.feature(t.tr("你走到了单个存储元件的尽头", "You've reached the end of a single storage cell"));
                t.bonus(Tx.join(ComponentSlang.RecommendedToUsePrefix, Tx.gold(t.tr("ME存储器 (多方块结构)", "ME Storage (MultiBlock)"))));
                t.bonus(t.tr("他最高可以实现不限类型的无限存储", "It can even store unlimited amounts of items and fluids without type limit"));
                t.edition();
            }));
        }

        setTips(fabulousPool.asItem(), GTOTips.of("fabulous_pool", t -> {
            t.feature(t.tr("能存一千普通池子的魔力", "Can hold the mana of one thousand regular pools"));
            t.edition();
        }));
        setTips(GTOBlocks.LAURERIL_JAR.asItem(), GTOTips.of("laureril_jar", t -> t.feature(t.tr("能存一千普通罐子的魔源", "Can hold the source of one thousand regular jars"))));

        for (Item item : List.of(AEItems.CERTUS_QUARTZ_KNIFE.asItem(), AEItems.NETHER_QUARTZ_KNIFE.asItem())) {
            setTips(item, GTOTips.of(t -> {
                t.keepOrder();
                t.rightClick(t.tr("Shift+右键方块可以复制其名称", "Shift+Right click a block to copy its name"));
                t.rightClick(t.tr("右键物品可以复制其名称", "Right click an item to copy its name"));
                t.control(t.tr("重命名以 + 开头可添加后缀", "Rename with a + prefix to add a suffix"));
                t.edition();
            }));
        }

        setTips(ModBlocks.OXYGEN_DISTRIBUTOR.get().asItem(), GTOTips.of(t -> {
            t.requirement(t.tr("直接通入氧气与电以工作", "Needs oxygen and power to work"));
            t.edition();
        }));

        for (Item item : List.of(ModBlocks.OXYGEN_LOADER.get().asItem(), ModBlocks.NASA_WORKBENCH.get().asItem())) {
            setTips(item, GTOTips.of(t -> {
                t.feature(t.tr("仅作为合成材料使用", "Used only as a crafting ingredient"));
                t.edition();
            }));
        }

        setTips(Adventure.Items.BOSS_SUMMONER.get(), GTOTips.of("boss_summoner", t -> t.feature(t.tr("由捕捉附魔击杀神化Boss概率掉落", "Dropped by killing Apotheosis Bosses with the Capture enchantment"))));

        setTips(Blocks.OBSIDIAN.asItem(), GTOTips.of("obsidian", t -> t.lore(t.tr("要不试试在它上面用砧板切洋葱？", "How about trying to chop onions on it with a cutting board?"))));

        setTips(TemplateManager.get().asItem(), GTOTips.of(t -> {
            t.lore(t.tr("要不试试放入AE2样板？", "How about slotting in an AE2 pattern?"));
            t.edition();
        }));

        for (var muffler : GTMachines.MUFFLER_HATCH) {
            if (muffler == null) continue;
            setTips(muffler.asItem(), GTOTips.of("muffler_hatch", t -> {
                if (GTOConfig.INSTANCE.gamePlay.disableMufflerPart) {
                    t.feature(Component.translatable("gtocore.tooltip.item.muffler.disabled"));
                } else {
                    t.feature(Component.translatable("gtocore.tooltip.item.muffler.enabled"));
                }
                t.edition();
            }));
        }

        setTips(CustomItems.PROGRAMMABLE_COVER.get(), GTOTips.of("programmable_cover", t -> {
            t.feature(t.tr("虚拟物品槽：§b1§r", "Virtual item slots: §b1§r"));
            t.edition();
        }));

        CustomMachines.ME_EXPORT_BUFFER.setTooltipBuilder((stack, components) -> components.addAll(MEExportBufferTooltips.get()));

        GTMultiMachines.POWER_SUBSTATION.setTooltipBuilder((stack, components) -> components.addAll(PowerSubstationTooltips.get()));

        GTMachines.STEAM_HATCH.setTooltipBuilder((stack, components) -> components.addAll(SteamHatchTooltips.get()));

        for (Item item : List.of(
                GTOItems.BIO_CARDIOMYOCYTE_CLUSTER.get(),
                GTOItems.MUTANT_CARDIOMYOCYTE_CLUSTER.get(),
                GTOItems.DRAGON_CARDIOMYOCYTE_CLUSTER.get(),
                GTOItems.MODIFIED_DRAGON_HEART.get(),
                GTOItems.AWAKENED_DRAGON_HEART.get())) {
            cardiomyocyteTips(item);
        }
    }

    private static void cardiomyocyteTips(Item item) {
        BioOscillationGenerator.TissueData data = BioOscillationGenerator.TISSUE_MATERIALS_TIER.get(item);
        Component medium = BioOscillationGenerator.BioOscillationGeneratorData.MEDIUM_MATERIALS_TIER.entrySet().stream()
                .filter(e -> e.getValue().tier() == data.tier())
                .<Component>map(e -> e.getKey().getLocalizedName())
                .findFirst()
                .orElseGet(() -> GTOTips.global("未知培养基", "Unknown Medium"));
        setTips(item, GTOTips.of("cardiomyocyte_cluster", t -> {
            t.heading(t.tr("生物振荡发电组织信息", "Bio Oscillation Generator Tissue Information"));
            t.feature(Component.translatable(BioOscillationElectricStimulator.TISSUE_TIER, data.tier()));
            t.feature(Component.translatable(
                    BioOscillationElectricStimulator.TISSUE_GROWTH_STAGE_POINTS,
                    Component.literal(String.valueOf(data.stage1Points())).withStyle(ChatFormatting.GREEN),
                    Component.literal(String.valueOf(data.stage2Points())).withStyle(ChatFormatting.YELLOW),
                    Component.literal(String.valueOf(data.stage3Points())).withStyle(ChatFormatting.GOLD),
                    Component.literal(String.valueOf(data.stage4Points())).withStyle(ChatFormatting.DARK_GRAY)));
            t.feature(Component.translatable(BioOscillationElectricStimulator.TISSUE_GROWTH_STAGE_POINTS_DESC));
            t.feature(Component.translatable(BioOscillationElectricStimulator.TISSUE_MAX_GROW_RATE, data.maxGrowRate()));
            t.feature(Component.translatable(BioOscillationElectricStimulator.TISSUE_SATURATION_FACTOR, data.saturationFactor()));
            t.feature(Component.translatable(
                    BioOscillationElectricStimulator.TISSUE_ELECTRODE_STIMULATION_FACTORS,
                    Component.literal(String.valueOf(data.electrodeStimulationFactors()[0])).withStyle(ChatFormatting.AQUA),
                    Component.literal(String.valueOf(data.electrodeStimulationFactors()[1])).withStyle(ChatFormatting.GREEN),
                    Component.literal(String.valueOf(data.electrodeStimulationFactors()[2])).withStyle(ChatFormatting.YELLOW),
                    Component.literal(String.valueOf(data.electrodeStimulationFactors()[3])).withStyle(ChatFormatting.GOLD)));
            t.feature(Component.translatable(BioOscillationElectricStimulator.TISSUE_ELECTRODE_STIMULATION_FACTORS_DESC));
            t.feature(Component.translatable(BioOscillationElectricStimulator.TISSUE_ELECTRODE_ENERGY_CONSUMPTION, ElectricText.energyExact(data.energyConsumption()), data.vTier()));
            t.requirement(Component.translatable(BioOscillationElectricStimulator.TISSUE_MEDIUM_REQUIREMENT, medium));
            t.requirement(Component.translatable(BioOscillationElectricStimulator.TISSUE_RUNNING_CONTROL_BLOCK_TIER, Math.min(data.tier(), 3)));
        }));
    }

    private static void bomb(Block block, Item activateItem) {
        setTips(block.asItem(), GTOTips.of("charge_bomb", t -> {
            if (GTCEu.isDataGen() || !GTOConfig.INSTANCE.gamePlay.disableChargeBomb) {
                t.feature(Component.translatable("gtocore.tooltip.item.activate_by", activateItem.getDescription()));
                t.feature(t.tr("也可以把它喂给热爆花...", "It can also be fed to entropinnyum flowers..."));
            } else {
                t.unsupported(Component.translatable("gtocore.tooltip.item.charge_bomb.disabled"));
            }
        }));
    }

    private static void craftingStorageTips(Item item, ICraftingUnitType type) {
        int threads = type.getAcceleratorThreads();
        long bytes = type.getStorageBytes();
        setTips(item, GTOTips.of("crafting_storage_" + threads + "_" + bytes, t -> {
            t.feature(t.tr("提供§b" + threads + "§r并行处理", "Provides §b" + threads + "§r parallel processing"));
            if (bytes > 0) {
                t.feature(t.tr("提供§b" + bytes / 1024 + "K§r存储容量", "Provides §b" + bytes / 1024 + "K§r storage capacity"));
            }
            t.edition();
        }));
    }

    private static void miraculousTools(GTOTips t, Component name) {
        t.heading(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), name));
    }

    private static void setTips(Item item, TipSpec spec) {
        ((IItem) item).gtolib$setTips(spec);
    }
}
