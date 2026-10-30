package com.gtocore.common.data.translation;

import com.gtocore.api.lang.GTOTips;
import com.gtocore.api.lang.Tx;
import com.gtocore.common.machine.multiblock.generator.TurbineMachine;
import com.gtocore.common.machine.multiblock.storage.MEStorageMachine;
import com.gtocore.common.machine.noenergy.game.game2048.Game2048Machine;
import com.gtocore.common.machine.noenergy.game.game2048.Game2048Rules;
import com.gtocore.common.machine.noenergy.game.justGet10.JustGet10Machine;
import com.gtocore.common.machine.noenergy.game.justGet10.JustGet10Rules;
import com.gtocore.common.machine.noenergy.game.slotMachine.SlotMachine;
import com.gtocore.common.machine.noenergy.game.slotMachine.SlotMachineRules;
import com.gtocore.common.machine.noenergy.game.slotMachine.SlotSymbol;
import com.gtocore.config.GTORules;

import com.gtolib.utils.NumberUtils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.tooltip.ElectricText;
import com.gregtechceu.gtceu.api.tooltip.StatKind;
import com.gregtechceu.gtceu.api.tooltip.TipRole;
import com.gregtechceu.gtceu.api.tooltip.TipSpec;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import com.google.common.collect.ImmutableMap;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;

import java.util.List;
import java.util.function.Consumer;

public final class GTOMachineTooltips {

    private GTOMachineTooltips() {}

    public static final TipSpec deprecatedStructureTooltips = GTOTips.of(t -> t.warning(Component.translatable("gtocore.structure.deprecated")));

    public static final TipSpec experienceObeliskTooltips = GTOTips.of("experience_obelisk", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("存储并管理经验值，以便随时取用", "Store and manage experience points for convenient access"));
    });

    public static final TipSpec AEManaInterfaceTooltips = GTOTips.of("me_mana_interface", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("将无线魔力网络中的魔力纳入ME网络管理", "Incorporate mana from the wireless mana network into ME network management"));
    });

    public static final TipSpec AreaDestructionToolsTooltips = GTOTips.of("area_destruction_tools", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("区域破坏器", "Area Destruction Tools")));
        t.lore(Tx.rainbowSlow(t.tr("专业的世界摧毁者", "Professional world destroyer")));
        t.warning(t.tr("一键清空方块！！！", "One click to clear the blocks!!!"));
        t.warning(t.tr("无视一切条件！！！", "Ignore all conditions!!!"));
        t.warning(t.tr("注意爆破安全！！！", "Pay attention to blasting safety!!!"));
        t.attention(t.tr("多人游戏请务必提醒其他成员！", "Be sure to warn other players in the multiplayer game! "));
        t.heading(t.tr("可用模式", "Available Modes"));
        t.control(t.tr("向库存放入物品以切换模式", "Add items to inventory to switch modes"));
        t.control(t.tr("放入§b模具(球)§r为§6球模式§r", "Put §bCasting Mold (Ball)§r for §6Ball mode§r"));
        t.control(t.tr("放入§b模具(圆柱)§r为§6圆柱模式§r", "Put §bCasting Mold (Cylinder)§r for §Cylinder mode§r"));
        t.control(t.tr("放入§b模具(块)§r为§6区块模式§r", "Put §bCasting Mold (Block)§r for §6Chunk mode§r"));
        t.control(t.tr("放入§b奇点§r为§6指定区域模式§r", "Put §bSingularity§r for §6Designated Area mode§r"));
        t.control(t.tr("此时用两个§9坐标信息卡§r来确定区域", "Now use two §9Coordinate Cards§r to determine the area"));
        t.heading(t.tr("爆炸当量", "Explosive Yield"));
        t.bonus(t.tr("向库存添加以下物品以提高爆炸当量：", "Add the following items to inventory to increase explosive yield:"));
        t.feature(t.tr("§c工业TNT§r/§c核弹§r/§c超能硅岩爆弹§r/§c轻子爆弹§r/§c量子色动力学爆弹§r", "§cIndustrial TNT§r/§cNuke Bomb§r/§cNaquadria Charge§r/§cLeptonic Charge§r/§cQuantum Chromodynamic Charge§r"));
    });

    public static final TipSpec IndustrialPlatformDeploymentToolsTooltips = GTOTips.of("industrial_platform_deployment_tools", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("工业平台展开工具", "Industrial Platform Deployment Tools")));
        t.feature(t.tr("基于预设蓝图快速部署工业基地", "Rapidly deploy industrial bases via preset blueprints"));
        t.requirement(t.tr("需消耗指定型号的工业组件作为基础构建材料", "Requires specific industrial components as basic construction materials."));
        t.requirement(t.tr("部分高级蓝图需额外配备辅助材料", "Some advanced blueprints require supplementary materials."));
        t.lore(t.tr("专业级工业基地搭建工具", "Professional-grade industrial base construction tool"));
        t.lore(t.tr("泛银河格雷科技销售部核心产品", "Core product of Pan-Galaxy Greg Technology Sales Department"));
        t.lore(t.tr("扩展包欢迎投稿你的蓝图", "Expansion packs welcome submissions of your blueprints"));
        t.feature(t.tr("详细操作说明请查阅工具内置手册", "For detailed instructions, refer to the tool's built-in manual"));
    });

    public static final TipSpec VillageTradingStationTooltips = GTOTips.of("village_trading_station", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("村民贸易站", "Village Trading Station")));
        t.feature(t.tr("企业与村庄友好协作的智能交易枢纽", "Intelligent trading hub for friendly enterprise-village collaboration"));
        t.feature(t.tr("支持9名村民同步对接，配备256槽大容量共享输入输出仓库", "Supports 9 simultaneous villagers with 256-slot high-capacity shared input/output warehouses"));
        t.feature(t.tr("无需工作方块匹配，放入村民并锁定信息即可启动运行", "No work block matching required—starts by placing villagers and locking their information"));
        t.feature(t.tr("交易站系统自动协调补货，通过增强模块可优化补货间隔与交易乘数", "Trading station system auto-coordinates restocks; enhancement modules optimize restock intervals and trade multiples"));
        t.lore(t.tr("政企村协同发展的核心装备", "Core equipment for enterprise-village collaborative development"));
        t.lore(t.tr("泛银河格雷科技研发部匠心研发", "Crafted with precision by Pan-Galaxy Greg Technology R&D"));
        t.feature(t.tr("详细操作说明请查阅机器内置手册", "For detailed instructions, refer to the in-machine manual"));
    });

    public static final TipSpec PanGalaxyGregTechTradingStationTooltips = GTOTips.of("pan_galaxy_greg_tech_trading_station", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("泛银河系格雷科技贸易站", "Pan-Galaxy Greg Technology Trading Station")));
        t.feature(t.tr("插入格雷会员卡，即可激活并绑定你的专属贸易枢纽", "Insert a Greg Membership Card to activate and bind your exclusive trading hub"));
        t.feature(t.tr("物品与流体双存储系统，容量随等级提升而指数级增长", "Dual storage system for items and fluids, with capacity growing exponentially with tier"));
        t.feature(t.tr("多级贸易目录，完成挑战即可解锁更高级、更稀有的交易配方", "Multi-level trade catalogs; complete challenges to unlock more advanced and rare trade recipes"));
        t.feature(t.tr("完整的交易记录，助你分析市场，优化你的贸易策略", "Comprehensive transaction logs to help you analyze the market and optimize your trading strategy"));
        t.lore(t.tr("你的星际贸易帝国，从这里开始", "Your interstellar trade empire starts here"));
        t.lore(t.tr("效率、规模与协作的终极贸易解决方案", "The ultimate trading solution for efficiency, scale, and collaboration"));
        t.feature(t.tr("快速上手：放置贸易站 → 插入会员卡 → 存入资源 → 解锁并执行交易 → 坐享其成！", "Quick Start: Place the Station → Insert Membership Card → Deposit Resources → Unlock & Execute Trades → Enjoy the Rewards!"));
    });

    public static final TipSpec SlotMachineTooltips = GTOTips.of("slot_machine", t -> {
        Component wildName = GTCEu.isDataGen() ? Component.empty() : SlotSymbol.symbolName(SlotSymbol.SYMBOL8);
        Component topName = GTCEu.isDataGen() ? Component.empty() : SlotSymbol.symbolName(SlotSymbol.SYMBOL7);

        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("老虎机", "Slot Machine")));
        t.heading(t.tr("三列卷轴，五条中奖线", "Three reels, five paylines"));

        t.heading(t.tr("赔付表（单线倍率，×0 不赔）", "Pay table (per-line multipliers; ×0 pays nothing)"));
        for (SlotSymbol symbol : SlotSymbol.values()) {
            if (symbol.wild()) continue;
            t.feature(t.tr("%s：二连 ×%s / 三连 ×%s", "%s: pair ×%s / triple ×%s",
                    GTCEu.isDataGen() ? Component.empty() : SlotSymbol.symbolName(symbol),
                    SlotMachineRules.CLASSIC.multiplier(symbol, 2),
                    SlotMachineRules.CLASSIC.multiplier(symbol, 3)));
        }
        t.feature(t.tr("%s：百搭，可替代任意符号；三个%s连成一线按%s三连赔 ×%s",
                "%s: wild, substitutes for any symbol; three %s in a line pay as a %s triple, ×%s",
                wildName, wildName, topName, SlotSymbol.SYMBOL7.threeMatchMultiplier()));

        t.heading(t.tr("中奖规则", "Winning Rules"));
        t.feature(t.tr("五条中奖线：上、中、下三条横线与两条对角线（竖向三格不赔）；只认从最左列开始的连续匹配，右起两格不赔",
                "Five paylines: the top, middle and bottom rows plus both diagonals (a vertical column does not pay). Matches must start at the leftmost reel; the two right reels alone pay nothing"));
        t.feature(t.tr("一条线只按最长的连中结算（三连不叠加它自己的二连）；不同线的奖励相加",
                "A line pays its longest run only (a triple does not also pay its pair); payouts from different paylines add up"));
        t.formula(t.tr("总奖励 = 下注 × 各线倍率之和，本金不返还", "Total payout = bet × sum of line multipliers; the bet is not returned"));

        slotBonusTips(t);
    });

    public static final TipSpec WideSlotMachineTooltips = GTOTips.of("wide_slot_machine", t -> {
        Component wildName = GTCEu.isDataGen() ? Component.empty() : SlotSymbol.symbolName(SlotSymbol.SYMBOL8);
        Component topName = GTCEu.isDataGen() ? Component.empty() : SlotSymbol.symbolName(SlotSymbol.SYMBOL7);

        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("宽幅老虎机", "Wide Slot Machine")));
        t.heading(t.tr("五列卷轴，九条中奖线", "Five reels, nine paylines"));

        t.heading(t.tr("赔付表（单线倍率，×0 不赔）", "Pay table (per-line multipliers; ×0 pays nothing)"));
        for (SlotSymbol symbol : SlotSymbol.values()) {
            if (symbol.wild()) continue;
            t.feature(t.tr("%s：三连 ×%s / 四连 ×%s / 五连 ×%s", "%s: triple ×%s / four ×%s / five ×%s",
                    GTCEu.isDataGen() ? Component.empty() : SlotSymbol.symbolName(symbol),
                    SlotMachineRules.WIDE.multiplier(symbol, 3),
                    SlotMachineRules.WIDE.multiplier(symbol, 4),
                    SlotMachineRules.WIDE.multiplier(symbol, 5)));
        }
        t.feature(t.tr("%s：百搭，可替代任意符号；五格全为%s时按%s五连赔 ×%s",
                "%s: wild, substitutes for any symbol; five %s pay as a %s five-of-a-kind, ×%s",
                wildName, wildName, topName, SlotMachineRules.WIDE.multiplier(SlotSymbol.SYMBOL7, 5)));

        t.heading(t.tr("中奖规则", "Winning Rules"));
        t.feature(t.tr("九条中奖线：三条横线、V 形与倒 V 形各一条、四条中间列或两端错位的折线（竖向不计奖）；只认从最左列开始的三连及以上",
                "Nine paylines: three rows, one V and one inverted V, and four lines that step on the middle reel or at the ends (vertical columns do not pay). Matches must start at the leftmost reel; three or more pay"));
        t.feature(t.tr("一条线只按最长的连中结算（不叠加它较短的连中）；不同线的奖励相加",
                "A line pays its longest run only (shorter runs on it are not added on top); payouts from different paylines add up"));
        t.formula(t.tr("总奖励 = 下注 × 各线倍率之和，本金不返还", "Total payout = bet × sum of line multipliers; the bet is not returned"));

        slotBonusTips(t);
    });

    /** 两台老虎机共用：中奖附赠不在界面里，保留这一条核心说明。 */
    private static void slotBonusTips(GTOTips t) {
        t.heading(t.tr("中奖附赠", "Bonus on Wins"));
        t.feature(t.tr("每次中奖有 %s%% 概率获得「机械之神降临」：带着它跳跃会推进附近工作中单方块机器的配方进度；最高加速 %s 档、最长 %s 分钟",
                "%s%% of wins grant Descent of the Machine God: jumping advances the recipe progress of nearby working single-block machines; it boosts recipes up to tier %s for at most %s minutes",
                (int) (SlotMachine.BOOST_CHANCE * 100),
                GTValues.VNF[SlotMachine.BOOST_MAX_AMPLIFIER], SlotMachine.BOOST_MAX_DURATION / (20 * 60)));
    }

    // 合成十 - 只保留规则与计分公式；盘面、分数、重开等以机器界面为准
    public static final TipSpec JustGet10MachineTooltips = GTOTips.of("just_get_10", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("合成UHV", "Just Get UHV")));
        t.feature(t.tr("点一格提起四向相连、数字相同的一整组（两格及以上），再点组内格合并成「该数字 + 1」",
                "Click a cell to lift the whole four-way connected group of the same number (two or more), then click inside it to merge the group into that number plus 1"));
        t.feature(t.tr("合并得分 = 新数字 × 组内格数；每个 10 及以上的数字首次合出时额外加分",
                "A merge scores the merged number × group size; each number of 10 or more also pays a one-time bonus the first time it is made"));
        t.feature(t.tr("一局超过 %s 分的部分，每 %s 分折 1 枚游戏币",
                "Points above %s pay 1 Game Coin per %s points",
                FormattingUtil.formatNumbers(JustGet10Machine.REDEMPTION_BASELINE),
                JustGet10Rules.POINTS_PER_COIN));
    });

    // 2048 - 只保留规则与计分公式；盘面、分数、重开等以机器界面为准
    public static final TipSpec Game2048MachineTooltips = GTOTips.of("game_2048", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("电路2048", "Circuit 2048")));
        t.feature(t.tr("WASD 或方向键推格：相邻同值两两合并（每块每步只并一次），空位补一个新数字",
                "WASD or the arrow keys push the board: equal neighbours merge (once per tile per move) and a new tile appears"));
        t.feature(t.tr("合并得分就是新数字；首次合出 1024 及以上额外加分",
                "A merge scores the tile it creates; making 1024 or more for the first time pays a bonus"));
        t.feature(t.tr("一局超过 %s 分的部分，每 %s 分折 1 枚游戏币",
                "Points above %s pay 1 Game Coin per %s points",
                FormattingUtil.formatNumbers(Game2048Machine.REDEMPTION_BASELINE),
                FormattingUtil.formatNumbers(Game2048Rules.POINTS_PER_COIN)));
    });

    // 24点 - 只保留表达式规则；按钮、牌范围、奖励进度以机器界面为准
    public static final TipSpec Game24MachineTooltips = GTOTips.of("game_24", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("24点", "24 Game")));
        t.feature(t.tr("用四张牌各一次，配合 +、-、×、÷ 与括号拼出 24；语法正确、结果正好 24 才通过，答错不扣钱",
                "Use each of the four cards exactly once with +, -, ×, ÷ and brackets to make 24; the expression must be valid and equal 24, and a wrong answer costs nothing"));
        t.feature(t.tr("每题都有解", "Every puzzle is solvable"));
    });

    // 数字华容道 - 只保留滑块规则与奖励概览
    public static final TipSpec NumberPuzzleTooltips = GTOTips.of("number_puzzle", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("数字华容道", "Number Puzzle")));
        t.feature(t.tr("点击空格上下左右相邻的数字块滑动，把 1..N²-1 按行排好、空格留在右下",
                "Click a tile next to the gap to slide it; order 1..N²-1 by row with the gap at the bottom-right"));
        t.feature(t.tr("完成一局按尺寸发放 1/4/8/15/25/40 游戏币，并记录该尺寸的最快完成时间",
                "Each clear pays 1/4/8/15/25/40 Game Coins by size and records the best time for that size"));
    });

    // 扫雷 - 只保留方块操作与通关条件；难度、奖励、按钮等以机器界面为准
    public static final TipSpec MinesweeperCoreTooltips = GTOTips.of("minesweeper_core", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("扫雷", "Minesweeper")));
        t.feature(t.tr("右击方块翻开，潜行右击插旗；数字格周围旗数对上时一次掀开一圈",
                "Right-click a block to open it, sneak-right-click to flag it; a numbered cell opens its neighbours once all its flags are placed"));
        t.feature(t.tr("第一下翻开的格子连同周围 3×3 保证没有雷；翻开所有非雷格或插满所有雷即通关",
                "The first opened cell and its 3×3 surroundings are always free of mines; open every safe cell or flag every mine to clear the board"));
    });

    public static final TipSpec PatternContentAccessTerminalTooltips = GTOTips.of("pattern_content_access_terminal", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("样板内容管理终端", "Pattern Content Access Terminal")));
        t.feature(t.tr("动态替换样板内容", "Dynamic Pattern Content Replacement"));
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("将此机器连接至 ME 网络后，样板在被调用时将遵循优先级配置，逐行进行内容匹配与替换。", "When connected to an ME Network, patterns will follow a priority configuration for line-by-line content matching and replacement when accessed."));
        t.feature(t.tr("每组配置中，替换的顺序为从右到左。", "In each configuration group, the replacement order is from right to left."));
        t.feature(t.tr("例如，按顺序填入“橡木木板，白桦木板，云杉木板”的配置，那么当有样板原料中用到白桦或云杉木板时，就会动态修改样板，使用橡木木板作为原料。", "For example, if you fill in the configuration with 'Oak Planks, Birch Planks, Spruce Planks' in order, then when a pattern's ingredient uses Birch or Spruce Planks, the pattern will be dynamically modified to use Oak Planks as the ingredient."));
    });

    public static final TipSpec ManaAmplifierHatchTooltips = GTOTips.of("mana_amplifier_hatch", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.requirement(t.tr("如果运行前输入了等同机器最大功率的魔力", "If mana equivalent to the machine's maximum power is input prior to operation"));
        t.bonus(t.tr("则将本次配方改为无损超频", "The current recipe will switch to perfect overclocking."));
        t.bonus(t.tr("对于跨配方机器，则将其超频系数额外x2 ", "For cross-recipe machines, the overclocking factor is further multiplied by x2."));
        t.penalty(t.tr("否则，机器不执行配方", "Otherwise, the machine will not execute the recipe."));
    });

    public static final TipSpec ManaHeaterTooltips = GTOTips.of("mana_heater", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("使用魔力加热，热量仅从顶面输出", "Uses mana to heat, heat only outputs from the top side"));
        t.bonus(t.tr("如果输入§c火元素蒸汽§r，则加热速度乘以5", "If §cSalamander Vapor§r is input, the heating speed will be 5 times faster"));
    });

    public static final TipSpec ManaCondenserTooltips = GTOTips.of("mana_condenser", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("在特定环境下只有特定的结构能够工作", "Only specific structure forms can work in specific environments"));
        t.feature(t.tr("亚尔夫海姆以外的维度使用常规形态，亚尔夫海姆使用亚尔夫海姆形态", "Dimensions other than Alfheim use the standard form; Alfheim uses the Alfheim form"));
        t.attention(t.tr("阵列形态与所在维度不符时，机器可以成型但不会工作", "If the array form does not match the current dimension, the machine forms but does not work"));
    });

    public static final TipSpec CelestialCondenserTooltips = GTOTips.of("celestial_condenser", t -> {
        t.feature(t.tr("凝聚苍穹之上的能量", "Condenses energy from beyond the celestial vault"));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("暴露于天空之下，不可有遮挡", "Must be directly exposed to the open sky with no obstructions"));
        t.supported(t.tr("在白天可以凝聚 - 曦煌", "Can condense Solaris during daytime"));
        t.supported(t.tr("在夜晚可以凝聚 - 胧华", "Can condense Lunara during nighttime"));
        t.supported(t.tr("在末地可以凝聚 - 虚湮", "Can condense Voidflux in the End"));
        t.penalty(t.tr("在虚空和超平坦可以以较慢的速度凝聚 - 曦煌/胧华", "Can condense Solaris/Lunara at a slower rate in the Void or Superflat"));
        t.bonus(t.tr("在亚尔夫海姆可以更快地凝聚 - 曦煌/胧华", "Can condense Solaris/Lunara faster in Alfheim"));
        t.bonus(t.tr("在幽冥可以更快地凝聚 - 虚湮", "Can condense Voidflux faster in the Otherside"));
        t.supported(t.tr("在太空可以凝聚 - 星髓", "Can condense Stellarm in the Space"));
        t.requirement(t.tr("运行配方时需要消耗这些能量", "Consumes this energy when processing recipes"));
    });

    public static final TipSpec CosmicCelestialSpireOfConvergenceTooltips = GTOTips.of("cosmic_celestial_spire_of_convergence", t -> {
        t.feature(t.tr("凝聚群星之中的能量", "Condense the power of the gathered stars"));
        t.supported(t.tr("可消耗魔力超频", "Consumable Mana Overclocking"));
        t.bonus(t.tr("每次消耗 2^(超频等级*4+10) 点魔力，使凝聚效率乘以 2^(超频等级*5)", "Consumes 2^(overclockingLevel * 4 + 10) mana points each time, boosts condensation efficiency by 2^(overclockingLevel * 5)"));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("暴露于天空之下，不可有遮挡", "Must be directly exposed to the open sky with no obstructions"));
        t.supported(t.tr("在白天可以凝聚 - 曦煌", "Can condense Solaris during daytime"));
        t.supported(t.tr("在夜晚可以凝聚 - 胧华", "Can condense Lunara during nighttime"));
        t.supported(t.tr("在末地可以凝聚 - 虚湮", "Can condense Voidflux in the End"));
        t.penalty(t.tr("在虚空和超平坦可以以较慢的速度凝聚 - 曦煌/胧华", "Can condense Solaris/Lunara at a slower rate in the Void or Superflat"));
        t.bonus(t.tr("在亚尔夫海姆可以更快地凝聚 - 曦煌/胧华", "Can condense Solaris/Lunara faster in Alfheim"));
        t.bonus(t.tr("在幽冥可以更快地凝聚 - 虚湮", "Can condense Voidflux faster in the Otherside"));
        t.supported(t.tr("在太空可以凝聚 - 星髓", "Can condense Stellarm in the Space"));
        t.requirement(t.tr("运行配方时需要消耗这些能量", "Consumes this energy when processing recipes"));
    });

    public static final TipSpec spaceShieldHatchTooltips = GTOTips.of("space_shield_hatch", t -> {
        t.feature(t.tr("在GTO寰宇重工的空间站技术还没完全完善的时候科研人员紧急开发出的小型防护罩", "A small protective shield urgently developed by researchers when GTO Universal Heavy Industries' space station technology was not fully developed"));
        t.feature(t.tr("经过技术改良，现在可以保护机器免受太空环境干扰", "After technical improvements, it can now protect machines from space environment interference"));
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("保护机器免受太空环境干扰而无法运行", "Protects machines from space environment interference"));
        t.requirement(t.tr("需要所在机器安装至少一个超高安仓", "Requires at least one High-Amp Hatch installed in the machine"));
    });

    public static final TipSpec RotorHatchTooltips = GTOTips.of("rotor_hatch", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("不同类型的转子的效率，耐久不同", "Different types of rotors have different efficiency and durability"));
    });

    public static final TipSpec RadiationHatchTooltips = GTOTips.of("radiation_hatch", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("仓中的辐射遵循以下规则：", "The radiation in the hatch follows the following rules:"));
        t.formula(t.tr("初始辐射=(配方辐射-抑制量)x(1+放射材料数量/64)", "Initial radiation = (Recipe radiation - inhibition) * (1 + count of radiation materials / 64)"));
        t.feature(t.tr("当仓中没有放射性材料时辐射随时间逐渐衰减", "When there are no radiation materials in the barn, the radioactivity gradually decreases over time"));
        t.control(t.tr("通入红石脉冲以清除残余辐射", "Input a redstone pulse to clear residual radiation"));
    });

    public static final TipSpec TempInterfaceTooltips = GTOTips.of("temp_vacuum_interface", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("与此部件连接可以为多方块机器传导热量", "Conduct heat for multiblock machines through connections with this part"));
    });

    public static final TipSpec VacuumInterfaceTooltips = GTOTips.of("temp_vacuum_interface", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("与此部件连接可以为多方块机器提供真空", "Provide vacuum for multiblock machines through connections with this part"));
    });

    public static TipSpec NeutronAcceleratorTooltips(long voltage, String voltageName, long euConsume, long euCapacity) {
        return GTOTips.of("neutron_accelerator", t -> {
            t.heading(ComponentSlang.RunningRequirements);
            t.statText(t.tr("最大输入电压：" + voltage + " (" + voltageName + "§r)", "Max Voltage Input: " + voltage + " (" + voltageName + "§r)"));
            t.statText(t.tr("最大EU消耗: " + euConsume, "Max EU Consumption: " + euConsume));
            t.feature(t.tr("每点EU都会转化为§e10~20-eV§b中子动能", "Each point of EU converts to §e10~20-eV§b neutron kinetic energy"));
            t.statText(ComponentSlang.Capacity(String.valueOf(euCapacity)));
        });
    }

    public static final TipSpec RedstoneTimerTooltips = GTOTips.of("redstone_timer", t -> {
        t.heading(t.tr("定时输出", "Timed Output"));
        t.feature(t.tr("每经过一次间隔，从火把一端输出一段持续信号，间隔、持续时长与信号强度可调", "After each interval, emits a signal of set duration from the torch end; interval, duration and strength are adjustable"));
        t.feature(t.tr("与中继器相同，强充能输出方向上的方块", "Strongly powers the block in front of the output, like a repeater"));
        t.heading(t.tr("控制", "Control"));
        t.feature(t.tr("可按火把对侧输入的控制信号运行或暂停，软锤可切换启用", "Can run or pause by a control signal entering opposite the torch; a soft mallet toggles it"));
        t.rightClick(t.tr("右键打开界面进行设置", "Right-click to configure"));
    });

    public static final TipSpec SensorTooltips = GTOTips.of("sensor", t -> {
        t.heading(t.tr("红石信号", "Redstone Signal"));
        t.feature(t.tr("未反转时，数值低于最低值或高于最高值时输出0，介于两者之间时输出递增的1-15的红石信号", "When not inverted, outputs 0 when the value is below the minimum or above the maximum, or an increasing redstone signal between 1-15 when in between"));
        t.feature(t.tr("反转时，数值低于最低值或高于最高值时输出15，介于两者之间时输出递减的1-15的红石信号", "When inverted, outputs 15 when the value is above the maximum or below the minimum, or a decreasing redstone signal between 1-15 when in between"));
    });

    public static final TipSpec CatalystHatchTooltips = GTOTips.of("catalyst_hatch", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("使用催化剂仓输入催化剂", "Use catalyst hatch to input catalysts"));
        t.bonus(t.tr("每次运行只消耗一点催化剂耐久，可以让催化剂重复使用", "Only consumes ONE catalyst durability each time, allowing the catalyst to be reused"));
    });

    public static final TipSpec MEStorageAccessHatchTooltips = GTOTips.of("me_storage_access_hatch", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("访问ME存储器内的存储", "Access storage in ME storage"));
        t.feature(t.tr("直接让ME线缆连上就好，不推荐无线连接", "Directly let ME cable connect, not recommended to use wireless connection"));
        t.feature(t.tr("装在磁盘存储器里时，数据索引与容量由机器写入，无需手动绑定", "Inside the ME Disk Storage the machine supplies the data index and capacity, no manual binding needed"));
    });

    public static final TipSpec MeCraftPatternHatchTooltips = GTOTips.of("me_craft_pattern_part_machine", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("合成样板仓用于存储合成样板", "Craft Pattern Hatch is used to store crafting patterns"));
        t.feature(t.tr("配合超级分子装配室使用", "Use it with Super Molecular Assembler"));
        t.statText(ComponentSlang.Capacity("72"));
    });

    public static final TipSpec MeSmallCraftPatternHatchTooltips = GTOTips.of("me_small_craft_pattern_part_machine", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("小型合成样板仓用于存储合成样板", "Small Craft Pattern Hatch is used to store crafting patterns"));
        t.feature(t.tr("配合分子装配工厂使用", "Use it with Molecular Assembly Factory"));
        t.statText(ComponentSlang.Capacity("18"));
    });

    public static final TipSpec MePullCraftPatternHatchTooltips = GTOTips.of("molecular_assembly_factory_pull_pattern_hatch", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("拉取样板仓用于存储合成样板", "Pull Pattern Hatch is used to store crafting patterns"));
        t.feature(t.tr("不参与 AE 发配，只在产物不足阈值时从网络拉取原料并合成", "Does not take part in AE pattern distribution; pulls ingredients from the network to craft only when the product is below the threshold"));
        t.middleClick(t.tr("对着样板按鼠标中键可单独设置拉取开关与产物阈值", "Middle-click a pattern to set its pull mode and product threshold"));
        t.attention(t.tr("补货数量按差额补齐到阈值，上限为网络原料能支撑的次数", "Restocks by the deficit up to the threshold, capped by what the network's ingredients can support"));
        t.feature(t.tr("每 2 秒检查一次", "Checked once every 2 seconds"));
        t.statText(ComponentSlang.Capacity("18"));
    });

    public static TipSpec MePatternHatchTooltips(int capacity) {
        return GTOTips.of("me_pattern_hatch", t -> {
            t.heading(t.tr("提供样板", "Provides Patterns"));
            t.feature(t.tr("可以放入样板，并进行一键发配", "Can put patterns and distribute them one click"));
            t.feature(t.tr("样板间分别隔离，互不干扰", "Patterns are isolated from each other, do not interfere with each other"));
            t.middleClick(t.tr("对着样板按鼠标中键可单独设置电路或者提供特别输入", "Press the middle mouse button on the pattern to set the circuit or provide special input"));
            t.feature(t.tr("可独立指定仓室的配方类型，覆盖机器的默认设定", "Can independently select recipe types, overriding the machine’s default setting"));
            t.stats();
            t.stat(StatKind.PATTERN_SLOTS, StatKind.number(capacity));
            t.statText(t.tr("单样板虚拟物品槽：§b9§r", "Virtual item slots per pattern: §b9§r"));
        });
    }

    public static final TipSpec MeCatalystPatternBufferTooltips = GTOTips.of("me_catalyst_pattern_buffer", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.bonus(t.tr("不消耗催化剂耐久，可以让催化剂重复使用", "Not consume catalyst durability each time, allowing the catalyst to be reused"));
    });

    public static final TipSpec MeWildcardPatternBufferTooltips = GTOTips.of("me_wildcard_pattern_buffer", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.keepOrder();
        t.supported(t.tr("允许使用通配符生成样板", "Allows the use of wildcards to generate patterns"));
        t.control(t.tr("在样板终端中编码带有通配符的样板以使用", "Encode patterns with wildcards in the Pattern Terminal to use"));
        t.feature(t.tr("机器将自动生成所有可制作的配方", "The machine will automatically generate all craftable recipes"));
        t.feature(t.tr("例如，编写一个§6任何锭->任何板材§r的样板", "For example, write a §6any ingot -> any plate§r pattern"));
        t.feature(t.tr("机器将自动生成§6铁锭->铁板§r、§6金锭->金板§r等所有可制作的§6锭->板材§r配方", "The machine will automatically generate all craftable §6ingot -> plate§r recipes like §6iron ingot -> iron plate§r, §6gold ingot -> gold plate§r, etc."));
        t.attention(t.tr("注意：生成时，机器会检查当前配方类型与存储的电路/模具等是否与样板匹配", "Note: When generating, the machine will check whether the current recipe type matches the stored circuit/mold, etc. with the pattern"));
        t.feature(t.tr("不匹配的配方将不会被生成", "Mismatched recipes will not be generated"));
        t.control(t.tr("在机器中设置黑名单材料以跳过该材料相关的配方生成", "Set blacklist materials in the machine to skip recipe generation related to that material"));
    });

    public static final TipSpec AutoConnectMETooltips = GTOTips.of("auto_connect_me", t -> {
        t.heading(t.tr("无线ME网络", "Wireless ME Network"));
        t.feature(t.tr("在机器界面中加入或断开已有的无线网络，并查看网络详情", "Join or leave an existing wireless network and view its details in the machine interface"));
        t.feature(t.tr("新建网络需使用ME无线连接机", "New networks can only be created with an ME Wireless Connection Machine"));
        t.control(t.tr("[Shift + 放置] 自动加入已收藏的网络", "[Shift + Place] Join the favorited network automatically"));
        t.feature(t.tr("在网络列表中点击星标收藏网络", "Click the star in the network list to favorite a network"));
    });

    public static final TipSpec MeWirelessConnectionMachineTooltips = GTOTips.of("me_wireless_connection_machine", t -> {
        t.attention(Tx.join(t.tr("妙妙工具: ", "Miraculous Tools: "), t.tr("ME无线连接机", "ME Wireless Connection Machine")));
        t.feature(t.tr("同一无线网络中的机器共享同一个ME网络", "Machines in the same wireless network share one ME network"));
        t.supported(t.tr("支持跨维度连接", "Works across dimensions"));
        t.heading(t.tr("网络管理", "Network Management"));
        t.feature(t.tr("在机器界面中新建、加入或断开无线网络", "Create, join or leave wireless networks in the machine interface"));
        t.feature(t.tr("唯一可以新建网络的机器，ME部件只能加入已有网络", "The only machine that can create networks; ME parts can only join existing ones"));
        t.control(t.tr("[Shift + 放置] 自动加入已收藏的网络", "[Shift + Place] Join the favorited network automatically"));
        t.feature(t.tr("在网络列表中点击星标收藏网络", "Click the star in the network list to favorite a network"));
    });

    public static final TipSpec VaultTooltips = GTOTips.of("vault", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("可以存储大量资源", "Can store many many resources"));
        t.feature(t.tr("使用简易合成终端或者ME存储总线读取内部资源", "Use Simple Crafting Terminal or ME Storage Bus to read internal resources"));
        t.feature(t.tr("容量由密封机械方块数量和等级决定", "The capacity is determined by the number and tier of Hermetic Casings"));
        t.statText(t.tr("大小：3x3x3到15x15x15", "Size: 3x3x3 to 15x15x15"));
    });

    public static final TipSpec VaultHatchTooltips = GTOTips.of("vault_hatch", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.supported(t.tr("可为保险库IO", "Can serve as vault IO"));
        t.formula(t.tr("最大安装数 = 密封机械方块数", "Maximum installations = hermetic casing amount"));
        t.supported(t.tr("可标记一种物品和一种流体", "Can mark one item and one fluid"));
        t.feature(t.tr("标记后该仓只读写被标记的资源，未标记的资源不进出", "Once marked, this hatch only reads and writes the marked resource; unmarked resources are rejected"));
    });

    public static final TipSpec DrawerStorageTooltips = GTOTips.of("drawer_storage", t -> {
        t.line(TipRole.SUMMARY, t.tr("用超级箱/缸作存储介质的多方块仓库", "Multiblock storage that uses Super Chests/Tanks as its medium"));

        t.heading(t.tr("怎么用", "Setup"));
        t.requirement(t.tr("主机槽放超级箱或超级缸（最多 64 个）：超级箱存物品，超级缸存流体", "Put Super Chests or Super Tanks in the controller slot (up to 64): chests store items, tanks store fluids"));
        t.detail(t.tr("主机等级：木质按 0.25，青春版按 0.5，其余按电压等级计算", "Controller level: wooden units count as 0.25, Lite units as 0.5, and other units use their voltage tier"));
        t.requirement(t.tr("输入总线放超级箱/缸；只有与主机同类的箱缸生效（箱配箱，缸配缸），支持木质及量子系列", "Put Super Chests/Tanks in the input buses; only the controller's kind counts (chests with chests, tanks with tanks), including wooden and quantum variants"));
        t.requirement(t.tr("超级存储升级也放入输入总线：青铜、钢、铝、钛、钨钢分别提供 2、4、8、16、32 倍容量", "Put Super Storage Upgrades in the input buses too: Bronze, Steel, Aluminium, Titanium and Tungsten Steel provide x2, x4, x8, x16 and x32 capacity"));
        t.unsupported(t.tr("功能存储的抽屉和存储升级不再提供收益", "Functional Storage drawers and storage upgrades no longer provide benefits"));

        t.heading(t.tr("容量", "Capacity"));
        t.feature(t.tr("能存的种类数：生效箱缸的格数之和，普通、1x2、2x2 每个分别提供 1、2、4 种", "Type count: the slot total of the counted storage units; normal, 1x2 and 2x2 units provide 1, 2 and 4 types each"));
        t.feature(t.tr("每种能存的数量：√(主机等级 × 主机数量 × 密封等级) × 最低箱缸容量 ÷ 最大箱缸格数 × √(输入箱缸总数量) × 升级倍率", "Amount per type: sqrt(controller tier x controller count x hermetic tier) x lowest storage capacity / maximum slot count x sqrt(total input unit count) x upgrade multiplier"));
        t.detail(t.tr("混放时最低容量与最大格数分别取值，数量只合计与主机同类的箱缸", "With mixed units, select the lowest capacity and the maximum slot count independently; count only units matching the controller's kind"));
        t.detail(t.tr("升级倍率：所有升级倍率相乘后按生效箱缸数量均分，每个箱缸最多计入 4 个；物品和流体使用相同倍率", "Upgrade multiplier: multiply all upgrade factors and share them over the counted storage units, at most 4 upgrades per unit; items and fluids use the same multipliers"));
        t.feature(t.tr("不限总量，只限每种的上限", "No overall limit, only a per-type cap"));

        t.heading(t.tr("其他", "Other"));
        t.warning(t.tr("开启溢出销毁后，超过每种上限或种类已满时，放不下的部分直接销毁", "With overflow voiding on, whatever does not fit (per-type cap reached or no free type) is destroyed"));
        t.feature(t.tr("和保险库一样：存储挂在主机正面，外部存取用保险库仓", "Like the vault: storage is exposed on the controller's front, and the vault hatch handles external IO"));
    });

    public static final TipSpec MeDiskBoxTooltips = GTOTips.of("me_disk_box", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("ME 磁盘存储器的单方块版本，槽里的存储组件（1k…256M）提供容量", "Single-block version of the ME Disk Storage; the storage components in its slot (1k...256M) provide the capacity"));
        t.feature(t.tr("数据索引可选玩家或机器；机器模式的索引随物品带走，拆下再放数据还在", "The data index can be the player or the machine; in machine mode the index travels with the item, so the data survives being broken and placed again"));
        t.feature(t.tr("数据索引选玩家时，同一玩家的这类设备共用同一份存储：不同 ME 网络之间可以共通", "With the player data index, such machines of the same player share one storage, so different ME networks can share contents"));
        t.feature(t.tr("存储转移：把网络里其它 ME 存储的内容全部搬进本箱，装不下的留在原处", "Transfer Storage: moves everything held by the other ME storages in the network into this box, what does not fit stays where it is"));
    });

    public static final TipSpec MEDiskStorageTooltips = GTOTips.of("me_disk_storage", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("以输入总线里的存储组件（1k…256m）提供容量，存储由结构里的存储访问仓承担", "Storage components in the input bus (1k...256m) provide the capacity; the Storage Access Hatch in the structure holds the data"));
        t.formula(t.tr("容量 = 组件字节之和 × 密封机械方块等级（ULV=1、LV=2…）；没有无限存储", "Capacity = the components' bytes x Hermetic Casing tier (ULV=1, LV=2, ...); there is no infinite storage"));
        t.feature(t.tr("数据索引可选玩家或机器；机器模式的索引随物品带走，同一台重建后数据还在", "The data index can be the player or the machine; in machine mode the index travels with the item, so rebuilding the same machine keeps the data"));
        t.feature(t.tr("数据索引选玩家时，同一玩家的这类设备共用同一份存储：不同 ME 网络之间可以共通", "With the player data index, such machines of the same player share one storage, so different ME networks can share contents"));
    });

    public static final TipSpec MeConfigurableStorageAccessHatchTooltips = GTOTips.of("me_configurable_storage_access_hatch", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("访问 ME 存储器内的存储，并可为每种东西单独设置上下限", "Accesses storage in ME storage and sets a limit for every key"));
        t.formula(t.tr("输入限制：每种最多存多少；未配置 = 不限，0 = 禁止存入", "Input limit: the most of each key that may be stored; unconfigured = unlimited, 0 = forbidden"));
        t.formula(t.tr("输出限制：每种至少保留多少；未配置 = 不限，0 = 不设下限", "Output limit: the least of each key to keep; unconfigured = unlimited, 0 = no floor"));
        t.feature(t.tr("两张表各有开关、互不影响；都不开时就是普通访问仓", "The two tables have their own switches and do not affect each other; with both off this is an ordinary access hatch"));
        t.feature(t.tr("配置面板：81 个物品格 + 81 个流体格，选中格子后在中间的输入框改数量", "Config panel: 81 item slots + 81 fluid slots; select a slot and edit the amount in the middle field"));
    });

    public static final TipSpec fishingFarmTooltips = GTOTips.of("fishing_farm", t -> {
        t.heading(Tx.join(Tx.gold(t.tr("设置电路以启用", "Set the circuit to enable")), Tx.scrollOptical(t.tr("自动钓鱼", "automatic fishing"))));
        t.feature(Tx.join(Tx.gold(t.tr("1号电路：", "Circuit 1:")), Tx.aqua(t.tr("随机垂钓", "random fishing"))));
        t.feature(Tx.join(Tx.gold(t.tr("2号电路：", "Circuit 2:")), Tx.aqua(t.tr("捕捉鱼类", "catch fish"))));
        t.feature(Tx.join(Tx.gold(t.tr("3号电路：", "Circuit 3:")), Tx.aqua(t.tr("捕捉垃圾", "catch junk"))));
        t.feature(Tx.join(Tx.gold(t.tr("4号电路：", "Circuit 4:")), Tx.aqua(t.tr("捕捉宝藏", "catch treasure"))));
    });

    // 外置热源锅炉
    public static final TipSpec BoilWaterMachineTooltips = GTOTips.of("boil_water_machine", t -> {
        t.heading(t.tr("需要外部热源工作", "Requires external heat source to operate"));
        t.warning(Tx.join(t.tr("当蒸汽溢出后继续工作会", "When steam overflows, continuing to work will "), ComponentSlang.Explosion));
    });

    // 性能监控器
    public static final TipSpec PerformanceMonitorMachineTooltips = GTOTips.of("performance_monitor_machine", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("能监测全部机器或AE网络最近一段时间的平均耗时，并支持高亮显示", "Can monitor all machines or AE grids' average tick time over a recent window and support highlighting"));
        t.rightClick(t.tr("右键点击机器以打开性能监测界面", "Right click on the machine to open performance monitoring interface"));
    });

    // 监控器系列
    private static TipSpec monitor(Consumer<GTOTips> body) {
        return GTOTips.of("monitor", t -> {
            t.line(TipRole.SUMMARY, Tx.rainbowSlow(t.tr("监控器系列", "Monitor series")));
            body.accept(t);
        });
    }

    // 基础监控器
    public static final TipSpec BasicMonitorTooltips = monitor(t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("是监控器的基础组成部分", "Is the basic component of the monitor"));
        t.supported(t.tr("可以与其他§6多个监控器§r系列相连", "Can be connected to other §6multiple monitors§r series"));
        t.supported(t.tr("多个监控器可以组成一个大屏", "Multiple monitors can form a large screen"));
        t.supported(t.tr("可以使用§d喷漆§r对监控器分组", "Can use§d paint spray§r to group monitors"));
        t.feature(t.tr("与§6监控器组件§r相连来显示不同信息", "Use §6monitor components§r to display different information"));
    });

    // 监控器电网组件
    public static final TipSpec MonitorPowerComponentTooltips = monitor(t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("显示§6无线电网§r的数据", "Display§6 wireless energy grid§r data"));
    });

    // 监控器魔力组件
    public static final TipSpec MonitorManaComponentTooltips = monitor(t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("显示§6无线魔力网§r的数据", "Display§6 wireless mana grid§r data"));
    });

    // 监控器算力组件
    public static final TipSpec MonitorComputingComponentTooltips = monitor(t -> {
        t.heading(ComponentSlang.MainFunction);
        t.requirement(t.tr("接入光缆网络以显示§6算力使用情况§r", "Connect to the optical cable network to display §6computing power usage§r"));
    });

    // 监控器ME吞吐量监控组件
    public static final TipSpec MonitorMEThroughputComponentTooltips = monitor(t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("显示ME中§6指定物品§r的吞吐量数据", "Display the throughput data of §6specified items §rin ME"));
    });

    // 监控器机器通用组件
    public static final TipSpec MonitorMachineComponentTooltips = monitor(t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("显示§6机器§r的通用数据", "Display the general data of §6machines§r"));
        t.feature(t.tr("例如耗电，产电，输入，输出等", "For example, power consumption, power production, input, output, etc."));
        t.requirement(t.tr("需要放入坐标信息卡", "Need to put coordinate card"));
    });

    // 监控器合成处理单元组件
    public static final TipSpec MonitorCraftingComponentTooltips = monitor(t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("显示§6ME合成处理单元§r的合成数据", "Display the crafting data of §6crafting unit§r"));
    });

    // 监控器自定义文本组件
    public static final TipSpec MonitorCustomTextComponentTooltips = monitor(t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("用于显示§6自定义文本§r在监控器上", "Display§6 custom text§r"));
        t.feature(t.tr("同一块监控器大屏的多个文本组件将被顺序显示", "Multiple text components of the same block monitor screen will be displayed in sequence"));
    });

    // 超立方体
    public static final TipSpec HyperCubeMachineTooltips = GTOTips.of("hyper_cube_machine", t -> {
        t.line(TipRole.SUMMARY, t.tr("代理一个流体或物品存储器", "Proxy a fluid or item or both storages"));
        t.control(t.tr("使用§b坐标信息卡§r绑定方块", "Use the §bCordinate Card§r to bind a storage block"));
        t.feature(t.tr("绑定某方块后，对此机器进行物品或流体操作视同对被绑定的方块操作", "Bind a storage to this machine to operate it as if it were the bound storage"));
        t.rightClick(t.tr("右键点击以打开界面", "Right click to open the interface"));
        t.feature(t.tr("此方块有升级版本", "Has upgrade version"));
    });

    // 进阶超立方体
    public static final TipSpec AdvancedHyperCubeMachineTooltips = GTOTips.of("advanced_hyper_cube_machine", t -> {
        t.line(TipRole.SUMMARY, t.tr("代理多个流体或物品存储器", "Proxy (a or multi) (fluid or item or both)storage"));
        t.control(t.tr("使用§b坐标信息卡§r绑定方块", "Use the §bCoordinate Card§r to bind a storage block"));
        t.feature(t.tr("绑定某方块后，对此机器进行物品或流体操作视同对被绑定的方块操作", "Bind a storage to this machine to operate it as if it were the bound storage"));
        t.feature(t.tr("若绑定多个方块，则依序对它们操作", "Operate them in order if bind multiple storages"));
        t.supported(t.tr("支持跨维度连接", "Works across dimensions"));
        t.rightClick(t.tr("右键点击以打开界面", "Right click to open the interface"));
        t.feature(t.tr("对装配线自动化很好用", "Good for assembly line automation"));
    });

    // 光伏电站
    public static final TipSpec PhotovoltaicPlantTooltips = GTOTips.of("photovoltaic_plant", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("维度和天气决定输出功率", "Calculate power output based on dimension and weather"));
        t.unsupported(t.tr("无法在星球轨道中工作，轨道发电请使用探索者号空间站光伏帆板控制器", "Cannot operate in planetary orbit; use the Explorer Space Station Photovoltaic Sail Controller for orbital power generation"));

        t.heading(ComponentSlang.RecommendedUseAs(t.tr("生产魔力", "mana production")));
        t.control(t.tr("在机器内放置64朵太阳花以使机器不再发电，转而采集魔力", "Place 64 dayblooms in the machine to stop power generation and start collecting mana"));
    });

    public static final TipSpec PhotovoltaicSailTooltips = GTOTips.of("photovoltaic_sail", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("仅能在星球轨道中工作", "Only operates in planetary orbit"));
        t.feature(t.tr("输出功率由所在轨道的太阳能强度决定，不受昼夜与天气影响", "Power output depends on the solar power of the current orbit and is unaffected by day, night and weather"));
        t.formula(t.tr("每秒消耗蒸馏水：输出功率(EU/t)/64 mB", "Consumes Distilled Water per second: Output Power (EU/t) / 64 mB"));
        t.feature(t.tr("与探索者号空间站两侧的对接接口相连时，由空间站供给蒸馏水并接收电能", "When connected to the docking ports on both sides of the Explorer Space Station, the station supplies Distilled Water and receives the generated power"));
        t.requirement(t.tr("控制器须水平朝向，帆板上方不得有遮挡", "The controller must face horizontally, and the space above the sails must be unobstructed"));

        t.heading(ComponentSlang.RecommendedUseAs(t.tr("生产魔力", "mana production")));
        t.control(t.tr("在机器内放置64朵太阳花以使机器不再发电，转而采集魔力", "Place 64 dayblooms in the machine to stop power generation and start collecting mana"));
    });

    // 加热器
    public static final TipSpec HeaterMachineTooltips = GTOTips.of("heater_machine", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("使用燃料加热，热量从顶面和侧面输出", "Uses fuel to heat, heat outputs from the top and sides"));
        t.requirement(t.tr("前方被阻挡后停止加热", "Stop heating after front side is blocked."));
        t.warning(Tx.join(t.tr("机器过热会", "When machine is too hot, it will "), ComponentSlang.Explosion));
        t.warning(ComponentSlang.BeAwareOfBurn);
    });

    // 冷却器
    public static final TipSpec CoolerMachineTooltips = GTOTips.of("cooler_machine", t -> {
        t.heading(t.tr("通过消耗水对侧面机器冷却", "Cooling the side machine by consuming water"));
    });

    // 高级冷却器
    public static final TipSpec AdvancedCoolerMachineTooltips = GTOTips.of("advanced_cooler_machine", t -> {
        t.heading(t.tr("通过消耗流体对侧面机器冷却", "Cooling the side machine by consuming fluid"));
        t.feature(t.tr("冷却效率取决于流体的温度", "Cooling efficiency depends on the temperature of the fluid"));
        t.requirement(t.tr("仅320K以下的流体可以参与冷却", "Only fluids below 320K can participate in cooling"));
        t.formula(t.tr("冷却效率 = min(200, (流体与机器温度差 / 4)) (HU/秒)", "Cooling efficiency = min(200, (fluid temperature - machine temperature) / 4) (HU/sec)"));
    });

    // 热阀
    public static final TipSpec HeatValveMachineTooltips = GTOTips.of("heat_valve_machine", t -> {
        t.feature(t.tr("当通入红石信号时，阀门关闭，阻止热量流动", "When a redstone signal is input, the valve closes, preventing heat flow"));
        t.feature(t.tr("当红石信号关闭时，阀门打开，允许热量流动", "When the redstone signal is off, the valve opens, allowing heat flow"));
        t.control(t.tr("使用螺丝刀以反转信号控制逻辑", "Use a screwdriver to reverse the signal control logic"));
    });

    // 电力加热器
    public static final TipSpec ElectricHeaterMachineTooltips = GTOTips.of("electric_heater_machine", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("使用电力加热，热量仅从顶面输出", "Uses electricity to heat, heat only outputs from the top side"));
        t.supported(t.tr("此机器不会爆炸", "This machine will not explode"));
        t.warning(ComponentSlang.BeAwareOfBurn);
    });

    // 裂变反应堆
    public static final TipSpec FissionReactorTooltips = GTOTips.of("fission_reactor", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("运行前需在反应堆内部放置燃料组件和冷却组件", "Fuel components and cooling components must be placed inside the reactor before operation"));
        t.feature(t.tr("燃料组件：提供最大并行，相邻的燃料组件越多，反应堆产热越高", "Fuel components: provide maximum parallelism; the more adjacent fuel components there are, the more heat the reactor generates"));
        t.requirement(t.tr("冷却组件：必须与燃料组件相邻，决定反应堆的冷却上限", "Cooling components: must be adjacent to fuel components and determine the reactor's cooling limit"));

        t.heading(t.tr("温度管理系统", "Temperature Management System"));
        t.feature(t.tr("初始温度为 298K；停机时每秒降低 1K", "Initial temperature is 298K; temperature decreases by 1K per second while idle"));
        t.formula(t.tr("冷却不生效时，每秒升温 = 配方基础产热 × (相邻燃料组件数量 + 1)", "When cooling has no effect, heat gained per second = base recipe heat × (adjacent fuel components + 1)"));
        t.feature(t.tr("每种燃料配方拥有不同的基础产热，可在配方页面查看", "Each fuel recipe has a different base heat value, shown on its recipe page"));
        t.warning(Tx.join(t.tr("温度超过 1500K 后反应堆将持续损坏，完全损坏时", "Above 1500K, the reactor continuously takes damage and, when fully damaged, "), ComponentSlang.Explosion));

        t.heading(t.tr("冷却系统", "Cooling System"));
        t.feature(t.tr("冷却液输入量大于冷却需求时触发冷却；每秒冷却液消耗量受冷却上限限制", "Cooling activates when the coolant input exceeds the cooling demand; coolant consumption per second is limited by the cooling limit"));
        t.feature(t.tr("每 800 mB 蒸馏水或 20 mB 钠钾合金可满足 1 单位冷却需求", "Every 800 mB of Distilled Water or 20 mB of Sodium Potassium can satisfy 1 unit of cooling demand"));
        t.formula(t.tr("冷却需求 = 配方基础产热 × 实际并行 × 当前温度 / 1500", "Cooling demand = base recipe heat × actual parallel × current temperature / 1500"));
        t.formula(t.tr("冷却上限 = (冷却组件数量 - 相邻数 / 3) × 8", "Cooling limit = (cooling components - adjacency / 3) × 8"));
        t.penalty(t.tr("若冷却液输入量低于冷却需求，本次冷却不生效", "If the coolant input is below the cooling demand, cooling has no effect this time"));

        t.heading(t.tr("超频机制", "Overclocking mechanism"));
        t.requirement(t.tr("触发条件: 冷却液输入量 ≥ n × 冷却需求", "Trigger condition: Coolant input ≥ n × cooling demand"));
        t.bonus(t.tr("超频效果: 额外增加基础运行速度×n", "Effect: Adds n times the base processing speed"));

        t.heading(t.tr("冷却液产出", "Cooling liquid output"));
        t.plain(t.tr("蒸馏水冷却: ", "Distilled Water cooling: "));
        t.formula(t.tr("产出蒸汽，产量 = 消耗量 × min(160, 160/(1.4^(373-温度)))", "Produces steam, Output = consumption × min(160, 160/(1.4^(373-temperature)))"));
        t.plain(t.tr("钠钾合金冷却:", "Sodium Potassium cooling:"));
        t.feature(t.tr("≤825K: 热钠钾合金；>825K: 超临界钠钾合金", "≤825K: Hot Sodium Potassium; >825K: Supercritical Sodium Potassium"));
    });

    // 计算中心
    public static final TipSpec SupercomputingTooltips = GTOTips.of("supercomputing", t -> {
        t.line(TipRole.SUMMARY, Tx.rainbow(t.tr("计算机超级计算中心", "Computer Supercomputing Center")));
        t.feature(t.tr("将多台计算机集成在一起，提供大规模并行计算能力", "Integrates multiple computers together to provide massive parallel computing power"));

        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("结构内部空间必须全部填满，可用对应等级的空组件替代", "The interior must be completely filled; empty components of the matching tier can be used instead"));

        t.heading(t.tr("等级系统", "Level System"));
        t.control(t.tr("通过在主机内放置特定物品切换等级", "Switch tiers by placing specific items in the mainframe"));
        t.requirement(t.tr("结构方块等级必须与机器等级匹配", "Structure block tiers must match machine tier"));

        t.heading(t.tr("算力计算系统", "Computing Power Calculation System"));
        t.formula(t.tr("最大输出算力 = 计算组件算力和 × 算力修正系数", "Max output = sum of component power × correction factor"));
        t.feature(t.tr("等级2/3时修正系数会随时间衰减", "At levels 2/3, correction factor decays over time"));
        t.formula(t.tr("衰减公式: ((系数-0.4)²/5000)×(0.8/log(系数+6))，最低0.8", "Decay: ((factor-0.4)²/5000)×(0.8/log(factor+6)), at least 0.8"));

        t.heading(t.tr("导热剂冷却增效", "Thermal Conductivity Cooling Enhancement"));
        t.bonus(t.tr("通过导热剂仓输入导热剂提升算力修正系数", "Input thermal conductivity via hatch to boost correction factor"));
        t.statText(t.tr("提升上限: 等级2(4) / 等级3(16)", "Enhancement limits: Tier 2(4) / Tier 3(16)"));
        t.attention(t.tr("导热剂使用后会失效", "Thermal conductivity becomes invalid after use"));
        t.statText(t.tr("MFPC效率: 块(0.18) 条(0.02) 粒(0.0022)", "MFPC efficiency: Block(0.18) Ingot(0.02) Nugget(0.0022)"));
        t.statText(t.tr("Cascade-MFPC效率: 块(0.54) 条(0.06) 粒(0.0066)", "Cascade-MFPC efficiency: Block(0.54) Ingot(0.06) Nugget(0.0066)"));
        t.statText(t.tr("寒冰碎片: 0.0001 (极低效率)", "Ice Shards: 0.0001 (extremely low efficiency)"));

        // Tier 1 组件支持
        t.heading(t.tr("Tier 1 : 支持HPCA系列组件", "Tier 1 : Supports HPCA Series Components"));
        t.statText(t.tr("槽位需求: 无", "Slot requirement: None"));
        t.statText(t.tr("结构材料需求: 钨强化硼玻璃 + 计算机外壳 + 计算机散热口", "Structure material requirements: Tungsten Borosilicate Glass + Computer Casing + Computer Heat Vent"));
        t.statText(t.tr("使用冷却剂: 多氯联苯冷却剂", "Coolant used: PCB coolant"));

        // Tier 2 组件支持
        t.heading(t.tr("Tier 2 : 支持NICH系列组件", "Tier 2 : Supports NICH Series Components"));
        t.statText(Tx.join(t.tr("槽位需求: 放入", "Slot requirement: Place "), Tx.scrollBioware(t.tr("生物活性主机", "Biological Mainframe"))));
        t.statText(t.tr("结构材料需求: 安普洛强化硼玻璃 + 生物计算机外壳 + 相变计算机散热口", "Structure material requirements: Neutronium Borosilicate Glass + Biocomputer Casing + Phase Change Biocomputer Cooling Vents"));
        t.statText(t.tr("使用冷却剂: 液态氦(会输出气态氦)", "Coolant: Liquid helium (will output gaseous helium)"));

        // Tier 3 组件支持
        t.heading(t.tr("Tier 3 : 支持GWCA系列组件", "Tier 3 : Supports GWCA Series Components"));
        t.statText(Tx.join(t.tr("槽位需求: 放入", "Slot requirement: Place "), Tx.rainbowGradient(t.tr("超因果主机", "Supracausal Mainframe"))));
        t.statText(t.tr("结构材料需求: 塔兰强化硼玻璃 + 引力子计算机外壳 + 逆熵计算机冷凝矩阵", "Structure material requirements: Taranium Borosilicate Glass + Graviton Computer Casing + Anti Entropy Computer Condensation Matrix"));
        t.statText(t.tr("使用冷却剂: 液态氦(会输出气态氦)", "Coolant: Liquid helium (will output gaseous helium)"));
        t.supported(t.tr("自带跨维度桥接功能", "Built-in cross-dimensional bridging capability"));
    });

    // 数字型采矿机
    public static final TipSpec DigitalMinerTooltips = GTOTips.of("digital_miner", t -> {
        t.heading(t.tr("让机器替代你挖矿", "Mine for You"));
        t.feature(t.tr("固定每两秒采掘一次", "Mines once every two seconds"));
        t.feature(t.tr("可通过GUI设置采掘范围和目标方块", "Mining range and target blocks can be set via GUI"));

        t.heading(t.tr("机器电压等级每高出一级：", "For each increase in machine voltage level:"));
        t.bonus(t.tr("可采掘最大范围翻倍(最高256)", "Maximum mining range is doubled (up to 256)"));
        t.bonus(t.tr("每次采掘的方块数量翻倍(最高4096)", "The number of blocks mined each time is doubled (up to 4096)"));
        t.penalty(t.tr("耗电量乘以4", "Power consumption is quadrupled"));
        t.control(t.tr("通入红石信号以重新计算采掘区域并执行", "Input a redstone signal to recalculate the mining area and execute mining"));
    });

    // 超级分子装配室
    public static final TipSpec SuperMolecularAssemblerTooltips = GTOTips.of("super_molecular_assembler", t -> {
        t.line(TipRole.SUMMARY, Tx.rainbowSlow(t.tr("分子装配室 Pro Max 版！", "Molecular assembler Pro Max Edition!")));

        t.heading(ComponentSlang.RunningRequirements);
        t.control(t.tr("通过合成样板仓放入合成样板", "Insert crafting patterns via craft pattern hatches"));
        t.feature(t.tr("每次运行同时处理所有的配方以及所有的输入物品", "Processes all recipes and all input items at once each operation"));
        t.feature(t.tr("产物直接进入 ME 网络，无需输出仓", "Outputs go straight into the ME network, no output bus needed"));
        t.feature(t.tr("每个物品合成消耗1EU", "Each item crafted consumes 1 EU"));
    });

    // 分子装配工厂
    public static final TipSpec MolecularAssemblyFactoryTooltips = GTOTips.of("molecular_assembly_factory", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.control(t.tr("通过分子装配工厂样板仓放入合成样板", "Insert crafting patterns via molecular assembly factory pattern hatches"));
        t.feature(t.tr("每轮只处理一个样板仓，从中最多取线程数个内部槽位", "Each operation handles a single hatch, taking at most one internal slot per thread"));
        t.feature(t.tr("拉取样板仓可按产物阈值被动补货，不需要 AE 合成任务", "Pull pattern hatches top products up to a threshold on their own, without an AE crafting job"));
        t.feature(t.tr("取到的槽位整份产物一起合成，数量不设上限", "The entire stored output of every taken slot is crafted at once, with no amount cap"));
        t.feature(t.tr("产物直接进入 ME 网络，无需输出仓", "Outputs go straight into the ME network, no output bus needed"));

        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("每个物品合成消耗1EU", "Each item crafted consumes 1 EU"));
        t.feature(t.tr("线程数按整体框架等级：ULV 1、LV 4，之后每级 +4（MV 8…）", "Threads scale with the Integral Framework tier: ULV 1, LV 4, then +4 per tier (MV 8, ...)"));
    });

    // ME 超算核心
    public static final TipSpec MECPUTooltips = GTOTips.of("me_cpu", t -> {
        t.line(TipRole.SUMMARY, Tx.rainbowSlow(t.tr("多方块合成 CPU，为 ME 网络执行合成任务", "Multiblock crafting CPU that runs ME network crafting jobs")));

        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("内部 481 格须全部填满：合成存储核心（T1~T5），或 AE 合成单元（最多 480 个）", "All 481 interior blocks must be filled: Crafting Storage Cores (T1-T5) or AE Crafting Units (at most 480)"));
        t.requirement(t.tr("需要 1 个合成CPU接口，可再放 1 个并行仓", "Needs 1 Crafting CPU Interface; 1 Parallel Hatch is optional"));

        t.heading(t.tr("CPU 性能", "CPU Performance"));
        t.feature(t.tr("容量：所有合成存储核心的容量之和，决定能接多大的合成任务；合成单元不提供容量", "Capacity: the sum of all Crafting Storage Core capacities, limiting the job size; Crafting Units add none"));
        t.bonus(Tx.rainbowSlow(t.tr("481 格全部是 T5 合成存储核心时，容量无限", "With all 481 blocks being T5 Crafting Storage Cores, capacity is unlimited")));
        t.feature(t.tr("并行：合成存储核心数量 × 并行仓并行数，即一次能发出的样板数", "Parallel: Crafting Storage Core count x Parallel Hatch parallel, the patterns sent out at once"));
        t.feature(t.tr("线程：2 的玻璃等级次方（最多 256），即能同时进行的合成任务数", "Threads: 2 to the power of the glass tier (at most 256), the crafting jobs that can run at once"));
    });

    // 大型内燃机
    public static TipSpec LargeCombustionTooltips(long baseEUt, long oxygenBoost, boolean canExtremeBoost, long liquidOxygenBoost) {
        return GTOTips.of("large_combustion", t -> {
            t.heading(ComponentSlang.PowerGenerationEfficiency);
            t.feature(ComponentSlang.BaseProductionEut(baseEUt));
            t.requirement(ComponentSlang.UsePerHourLubricant(FluidHelper.getBucket()));
            if (canExtremeBoost) {
                t.bonus(t.tr("提供80mB/s的§a液态氧§r，并消耗§4双倍§r燃料以产生§e" + liquidOxygenBoost + " EU/t§r的功率", "Provide 80mB/s of §eLiquid Oxygen§r, consuming §adouble§r fuel to produce up to §e" + liquidOxygenBoost + " §rEU/t"));
            } else {
                t.bonus(t.tr("提供20mB/s的§a氧气§r，并消耗§4双倍§r燃料以产生§e" + oxygenBoost + " EU/t§r的功率", "Provide 20mB/s of §eOxygen§r, consuming §adouble§r fuel to produce up to §e" + oxygenBoost + " §rEU/t"));
            }

            t.heading(ComponentSlang.AfterModuleInstallation);
            t.bonus(t.tr("并行能力提升至 §e4§r", "Parallelism increases to §e4x§r"));
            t.bonus(t.tr("空气进气速度乘以 §e4§r", "Air intake speed becomes §e4x§r faster"));
        });
    }

    // 高速模式
    public static final TipSpec TurbineHighSpeedTooltips = GTOTips.of("turbine_high_speed", t -> {
        t.heading(t.tr("高速模式", "High-Speed Mode"));
        t.bonus(t.tr("高速模式可进一步提升运行速度，与模块乘算", "High-speed mode can further increase operating speed, multiplied with modules"));
    });

    // 大型涡轮
    public static TipSpec LargeTurbineTooltips(long baseEUt, int rotorTier) {
        return GTOTips.of("large_turbine", t -> {
            t.heading(ComponentSlang.PowerGenerationEfficiency);
            t.feature(ComponentSlang.BaseProductionEut(baseEUt));
            t.bonus(ComponentSlang.RotorEfficiency(rotorTier));

            t.heading(ComponentSlang.AfterModuleInstallation);
            t.bonus(t.tr("运行速度乘以2", "Gains 2x speed"));
            t.bonus(t.tr("获得额外20%涡轮效率", "Gains additional 20% turbine efficiency"));
            t.penalty(t.tr("转子损耗速度乘以2", "Rotor wear rate becomes 2x"));
        });
    }

    // 特大涡轮
    public static TipSpec MegaTurbineGenerateTooltips(long baseEUt, int rotorTier) {
        return GTOTips.of("mega_turbine", t -> {
            t.heading(ComponentSlang.PowerGenerationEfficiency);
            t.feature(ComponentSlang.BaseProductionEut(baseEUt));
            t.bonus(ComponentSlang.RotorEfficiency(rotorTier));
            t.feature(t.tr("运行效率相当于16台同类大型涡轮", "Operating efficiency is equivalent to 16 large turbines of the same type"));
            t.feature(t.tr("启动速度为同类大型涡轮的4倍", "Startup speed is 4 times that of similar large turbines"));
            t.supported(t.tr("可使用更多动力仓", "Can use more power hatch"));
            t.supported(t.tr("可安装转子仓，从中自动取出转子安装到空转子支架", "Rotors can be installed in the rotor chamber, automatically extracting rotor for installation onto empty rotor brackets"));

            t.heading(ComponentSlang.AfterModuleInstallation);
            t.bonus(t.tr("运行速度乘以3", "Gains 3x speed"));
            t.bonus(t.tr("获得额外30%涡轮效率", "Gains additional 30% turbine efficiency"));
            t.penalty(t.tr("转子损耗速度乘以3", "Rotor wear rate becomes 3x"));

            t.heading(ComponentSlang.CoilEfficiencyBonus);
            t.bonus(t.tr("线圈等级每高出白铜一级，转子启动速度增加20%", "Each coil tier above Cupronickel increases rotor startup speed by 20%"));

            Component cs1 = t.tr("高速模式调节器(专家模式专属)", "High-Speed Mode Regulator (Expert Mode Exclusive)");
            Component cf1 = t.tr("允许调节涡轮的高速乘数以换取转子寿命", "Allows adjustment of turbine high-speed multiplier in exchange for rotor lifespan");
            Component ci1 = t.tr("高速乘数范围为0.1到5.0，并自动与原涡轮乘数相乘", "The high-speed multiplier ranges from 0.1x to 5.0x, and the multiplier is automatically multiplied by the original turbine multiplier");

            Component cs2 = t.tr("玻璃等级加成(专家模式专属)", "Glass Tier Bonus (Expert Mode Exclusive)");
            Component ci2 = t.tr("专家模式下，玻璃等级每一级，高速调节器的损坏基数-0.08", "In expert mode, each glass tier reduces the damage base of the high-speed regulator by 0.08");
            Component cf2 = t.tr("最低的损坏基数为1.2", "The minimum damage base is 1.2");

            if (GTORules.MEGA_TURBINE_REGULATOR.get()) {
                t.heading(cs1);
                t.feature(cf1);
                t.feature(ci1);
                t.feature(Component.translatable(TurbineMachine.DESC4));
                t.feature(Component.translatable(TurbineMachine.DESC5));

                t.heading(cs2);
                t.bonus(ci2);
                t.feature(cf2);
            }
        });
    }

    // 化学能吞噬者
    public static final TipSpec ChemicalEnergyDevourerGenerateTooltips = GTOTips.of("chemical_energy_devourer", t -> {
        t.heading(ComponentSlang.PowerGenerationEfficiency);
        t.feature(ComponentSlang.BaseProductionEut(GTValues.V[GTValues.ZPM]));
        t.requirement(ComponentSlang.UsePerHourLubricant(10 * FluidHelper.getBucket()));
        t.bonus(Tx.join(
                t.tr("提供320mB/s的§a液态氧§r，并消耗§4双倍§r燃料以产生", "Provide 80mB/s of §eLiquid Oxygen§r, consuming §adouble§r fuel to produce up to "),
                Tx.yellow(ElectricText.power(GTValues.V[GTValues.UV]))));
        t.bonus(Tx.join(
                t.tr("再提供480mB/s的§a四氧化二氮§r，并消耗§4四倍§r燃料以产生", "Provide extra 480mB/s of §eDinitrogen Tetroxide§r, consuming §afour times§r fuel to produce up to "),
                Tx.yellow(ElectricText.power(GTValues.V[GTValues.UHV]))));
    });

    // 化工厂
    public static final TipSpec ChemicalFactoryTooltips = GTOTips.of("chemical_factory", t -> {
        t.heading(ComponentSlang.CoilEfficiencyBonus);
        t.bonus(t.tr("线圈等级每高出白铜一级能耗与时间减少5%", "Each coil tier above Cupronickel, Reduces energy consumption and duration by 5%"));
    });

    // 大型虚空采矿厂
    public static final TipSpec LargeVoidMinerTooltips = GTOTips.of("large_void_miner", t -> {
        t.heading(t.tr("精准模式", "Precision Mode"));
        t.feature(t.tr("消耗精华采集指定矿脉", "Consumes resources to collect specified veins"));

        t.heading(t.tr("随机模式", "Random Mode"));
        t.requirement(t.tr("消耗10KB的钻井液", "Consumes 10KB of Drilling Fluid"));
        t.feature(t.tr("耗时更长随机采集所有矿石", "Longer duration to randomly collect all ores"));
        t.attention(t.tr("注意: 确保输出空间足够", "Note: Ensure enough output space"));
    });

    // 通用工厂
    public static final TipSpec ProcessingPlantTooltips = GTOTips.of("processing_plant", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("小机器电压应与能源仓匹配，§b双仓升压§r时能源仓电压+1级", "The small machine tier must match that of the Energy Hatch; §bDual Hatch Boost§r increases its tier by one"));
        t.unsupported(t.tr("无法通过超净维护仓获得洁净环境，仅能通过超净间获得", "Cannot obtain a clean environment through a Clean Maintenance Hatch; it can only be obtained through a Cleanroom"));

        t.heading(t.tr("配方等级", "Recipe Tier"));
        t.formula(t.tr("最终配方等级 = min(小机器, 整体框架)", "Final recipe tier = min(small machine, whole structure)"));

        t.heading(ComponentSlang.AfterModuleInstallation);
        t.bonus(t.tr("并行翻倍", "Parallelism is doubled"));
    });

    // 培养缸
    public static final TipSpec CulturingTankTooltips = GTOTips.of("culturing_tank", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.attention(t.tr("玻璃等级决定配方等级上限", "The glass casing tier determines the upper limit of recipe tier"));
    });

    // 大型培养缸
    public static final TipSpec LargeCulturingTankTooltips = GTOTips.of("large_culturing_tank", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.attention(t.tr("玻璃等级决定配方等级上限", "The glass casing tier determines the upper limit of recipe tier"));
    });

    // 纳米锻炉
    public static final TipSpec NanoForgeTooltips = GTOTips.of("nano_forge", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("结构等级由已搭建的扩展模块决定：未搭建为 1，搭建主扩展模块为 2，进阶装配线内芯的主扩展模块与副扩展模块同时搭建为 3", "Structure tier depends on the extension modules built: 1 with none, 2 with the primary module, 3 with an advanced-core primary module plus the secondary module"));
        t.control(t.tr("往主机内放入纳米蜂群以提供蜂群等级", "Place nanites in the controller to provide the nanoswarm tier"));
        t.feature(t.tr("碳：1，安普洛：2，龙：3", "Carbon: 1, Amprosium: 2, Draconium: 3"));
        t.feature(t.tr("生效等级取结构等级与蜂群等级中的较低者，未放入蜂群时无法运行", "The effective tier is the lower of the structure tier and the nanoswarm tier; the machine cannot run without nanites"));
    });

    // 中子活化器
    public static final TipSpec NeutronActivatorTooltips = GTOTips.of("neutron_activator", t -> {
        t.heading(t.tr("超光速运动！", "Superluminal Movement!"));
        t.bonus(t.tr("额外高速管道方块提供时间减免", "Additional high-speed pipeline blocks provide time reduction"));
        t.penalty(t.tr("同时降低中子加速器效率", "While lowering neutron accelerator efficiency"));
        t.formula(t.tr("效率公式: 0.95^额外方块数量", "Efficiency formula: 0.95^Number of Additional Blocks"));

        t.heading(t.tr("中子动能系统", "Neutron Kinetic Energy System"));
        t.feature(t.tr("无中子加速器运行时每秒降低§e72KeV§r", "When no neutron accelerator is running, decreases §e72KeV§r per second"));
        t.feature(t.tr("输入石墨/铍粉可立即吸收§e10MeV§r", "Input graphite/beryllium powder can immediately absorb §e10MeV§r"));
    });

    // 热交换机
    public static final TipSpec HeatExchangerTooltips = GTOTips.of("heat_exchanger", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("每次处理全部输入的热流体", "Processes all input hot fluids every time"));
        t.requirement(t.tr("需要保证输入的冷却液能将流体全部冷却", "Must ensure the cooling liquid input can cool all fluids"));
        t.bonus(t.tr("连续运行4次后将输出高级蒸汽", "Outputs high-level steam after running continuously 4 times"));
    });

    // 太空电梯
    public static final TipSpec SpaceElevatorTooltips = GTOTips.of("space_elevator", t -> {
        t.heading(t.tr("模块运行优化系统", "Module Operation Optimization System"));
        t.supported(t.tr("可安装最多12个拓展模块", "Can install up to 12 expansion modules"));
        t.bonus(t.tr("提升电压等级可为模块提供耗时减免", "Increasing voltage tier can provide Duration reductions for modules"));
        t.requirement(t.tr("运行前需提供128*(电压等级-7)的算力", "Before starting, it is necessary to provide 128 * (voltage tier - 7) computation power"));
    });

    // 工业屠宰场
    public static final TipSpec SlaughterhouseTooltips = GTOTips.of("slaughterhouse", t -> {
        t.heading(t.tr("电动刷怪塔，自动杀怪", "Electric Spawner, automatically kills mobs"));
        t.bonus(t.tr("电压等级每高出LV1级，每次处理次数x3", "Voltage tier above LV1 increases the number of processes x3"));
        t.bonus(t.tr("高等级的电压同时可以提供更高的幸运加成", "Higher voltage tiers can also provide higher luck bonus"));
        t.formula(t.tr("幸运加成值：电压等级*0.5", "Luck bonus value: Voltage tier * 0.5"));
        t.attention(t.tr("机器使用的玻璃等级将会限制电压等级", "Glass tier used by the machine will limit the voltage tier"));
        t.requirement(t.tr("运行前需设置电路，1号电路为非敌对生物，2号为敌对生物", "Circuit must be set up before running; Circuit 1 is for non-hostile mobs, 2 is for hostile mobs"));
        t.feature(t.tr("如果在机器GUI内放置了§b刷怪蛋/收容罐§r则只会刷出§b刷怪蛋/收容罐对应生物§r的内容", "If a §bspawn egg/mob jar§r is placed in the machine GUI, only §bthe corresponding mob§r will be spawned"));
        t.feature(t.tr("如果在机器GUI内放置了§b神化Boss召唤器（物品）§r，则只会刷出§b所在维度对应的神化Boss§r", "If a §bApotheosis Boss Summoner (item)§r is placed in the machine GUI, only §bthe corresponding Apotheosis Boss in that dimension§r will be spawned"));
        t.unsupported(t.tr("无法刷出boss级生物，如末影龙和凋零", "Boss-level mobs, such as the Ender Dragon and Wither, cannot be spawned"));
        t.feature(t.tr("当输入多把剑时，只会使用检测到的第一把武器去尝试击杀其中的生物", "Will only use the first weapon detected to try to kill the creature inside when multiple swords are input"));
        t.bonus(t.tr("其余的剑可以用来提供额外的伤害加成", "The rest of the swords can be used to provide additional damage bonus"));
        t.feature(t.tr("每把剑提供其基础伤害点数作为伤害加成", "Each sword provides its base damage points as damage bonus"));
        t.feature(t.tr("安装输出仓后输出经验", "Outputs XP after installing output hatch"));
    });

    // 基岩钻机
    public static final TipSpec BedrockDrillingRigTooltips = GTOTips.of("bedrock_drilling_rig", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("需要基岩在钻头下方", "Requires bedrock below the drill head"));
        t.penalty(t.tr("每次运行有10%概率破坏基岩", "Each run has 10% chance to destroy bedrock"));
    });

    // 寒冰冷冻机
    public static final TipSpec ColdIceFreezerTooltips = GTOTips.of("cold_ice_freezer", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("需提供§6 2^( 电压等级 - 2 ) * 10mb/s§r的§b液态冰§r", "Requires to provide §b 2^(Voltage tier - 2 ) * 10mb/s§r of §6Liquid Ice§r"));
        t.feature(t.tr("配方开始工作时立刻消耗一次液态冰", "Consumes Liquid Ice once immediately when the recipe starts"));
    });

    // 烈焰高炉
    public static final TipSpec BlazeBlastFurnaceTooltips = GTOTips.of("blaze_blast_furnace", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("需提供§b 2^( 电压等级 - 2 ) * 10mb/s§r（最低§b 10mb/s§r）的§6液态烈焰§r", "Requires to provide §b 2^(Voltage tier - 2 ) * 10mb/s§r (at least §b10mb/s§r) of §6Liquid Blaze§r"));
        t.feature(t.tr("配方开始工作时立刻消耗一次液态烈焰", "Consumes Liquid Blaze once immediately when the recipe starts"));
    });

    // PCB工厂
    public static final TipSpec PCBFactoryTooltips = GTOTips.of("pcb_factory", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("结构等级由已搭建的扩展结构决定：主体为 1，搭建二级扩展结构为 2，在其后方再搭建三级扩展结构为 3", "Structure tier depends on the extensions built: 1 for the main body, 2 with the tier 2 extension, 3 with the tier 3 extension behind it"));
        t.control(t.tr("往主机内放入纳米蜂群以提供蜂群等级", "Place nanites in the controller to provide the nanoswarm tier"));
        t.feature(t.tr("金：1，山铜：2，末影素：3", "Gold: 1, Orichalcum: 2, Enderium: 3"));
        t.feature(t.tr("生效等级取结构等级与蜂群等级中的较低者，未放入蜂群时按 1 级运行", "The effective tier is the lower of the structure tier and the nanoswarm tier; without nanites the machine runs at tier 1"));
    });

    // 进阶装配线
    public static final TipSpec AdvancedAssemblyLineTooltips = GTOTips.of("advanced_assembly_line", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.supported(t.tr("可以使用更大的输入总线", "Can use larger input buses"));
        t.requirement(t.tr("需要保证每层的输入与配方对应", "Must ensure each layer input corresponds to the recipe"));
        t.requirement(t.tr("只能使用数据靶仓", "Only data target chambers can be used"));
    });

    // 方块转换室
    public static final TipSpec BlockConversionRoomTooltips = GTOTips.of("block_conversion_room", t -> {
        t.line(TipRole.SUMMARY, t.tr("每秒随机转化机器内部方块", "Randomly converts blocks inside the machine every second"));

        t.heading(t.tr("电压等级加成", "Voltage Tier Bonus"));
        t.bonus(t.tr("每高出MV1级，转换方块数量+4", "For each tier above MV1, block conversion +4"));
        t.attention(t.tr("不会重复转换同一方块", "Will not repeatedly convert the same block"));
    });

    // 大型方块转换室
    public static final TipSpec LargeBlockConversionRoomTooltips = GTOTips.of("large_block_conversion_room", t -> {
        t.line(TipRole.SUMMARY, t.tr("每秒随机转化机器内部一个方块", "Randomly converts one block inside the machine every second"));

        t.heading(t.tr("电压等级加成", "Voltage Tier Bonus"));
        t.bonus(t.tr("UHV以下每高出MV1级，转换方块数量+64", "Below UHV, each tier above MV increases block conversion by +64"));
        t.bonus(t.tr("从UHV开始，每升高1级电压，转换方块数量翻倍", "Starting from UHV, each voltage tier doubles the block conversion amount"));
        t.attention(t.tr("不会重复转换同一方块", "Will not repeatedly convert the same block"));

        t.feature(t.tr("在主机内放置§6转换模拟卡§r，且安装方块总线后，可直接转换方块总线内的方块", "By placing §6Conversion Simulation Card§r in the mainframe and installing a block bus, blocks inside the block bus can be directly converted"));
    });

    // 宇宙探测器地面接收单元
    public static final TipSpec SpaceProbeSurfaceReceptionTooltips = GTOTips.of("space_probe_surface_reception", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("只能运行在星球轨道上", "Can only operate in orbit"));

        t.heading(t.tr("戴森球连接", "Dyson Sphere Connection"));
        t.feature(t.tr("自动连接星系内未使用的戴森球", "Automatically connects to unused Dyson spheres in the galaxy"));
        t.bonus(t.tr("根据戴森球模块数量提升产出", "Increases production based on Dyson sphere module count"));
        t.formula(t.tr("产出乘数 = max(1, floor(模块数量 / 100))", "Output multiplier = max(1, ⌊module count / 100⌋)"));
        t.supported(t.tr("该操作不会损坏戴森球", "This operation will not damage the Dyson sphere"));
        t.requirement(t.tr("一个星系只能有一个接收单元提升产出", "Only one Receiver per orbit can increase output"));
    });

    // 鸿蒙之眼
    public static final TipSpec EyeOfHarmonyTooltips = GTOTips.of("eye_of_harmony", t -> {
        t.line(TipRole.SUMMARY, Tx.rainbowSlow(t.tr("创造微缩宇宙并获取资源", "Creates a mini-universe and gathers resources inside")));

        t.heading(t.tr("供电系统", "Power System"));
        t.attention(t.tr("需要太多EU，无法用常规手段供能", "Requires too much EU — cannot be powered by conventional means"));
        t.attention(t.tr("由无线EU网络直接供给", "Directly supplied by wireless EU network"));
        t.feature(t.tr("配方启动时从所有者的无线电网一次性扣除启动耗能，运行期间不再耗电", "Startup energy is taken from the owner's wireless network once when a recipe starts; no power is drawn while running"));
        t.requirement(t.tr("无线电网储能需大于启动耗能才能启动配方", "The wireless network must hold more than the startup energy to start a recipe"));
        t.formula(t.tr("实际启动耗能 = 基础启动耗能 × 配方倍率", "Actual startup energy = base startup energy × recipe multiplier"));
        t.feature(t.tr("1号电路的基础启动耗能约为2.1×10^16 EU，具体数值可在GUI内查看", "Base startup energy with circuit 1 is about 2.1×10^16 EU; specific values can be viewed in the GUI"));
        t.formula(t.tr("配方倍率 = (配方等级 - 1) × 4，至少为1", "Recipe multiplier = (recipe tier - 1) × 4, at least 1"));
        t.feature(t.tr("例：等级10配方为36倍", "e.g. a tier 10 recipe costs 36x"));

        t.heading(t.tr("特殊超频", "Special Overclocking"));
        t.bonus(t.tr("每次超频使速度乘以2、启动耗能乘以8", "Each overclock multiplies speed by 2 and startup energy by 8"));
        t.requirement(t.tr("超频由编程电路调节，未放入1~4号电路时无法运行", "Overclocking is set via programmed circuits; the machine cannot run without circuit 1~4"));
        t.statText(t.tr("电路1: 不执行超频", "Circuit 1: No overclocking"));
        t.statText(t.tr("电路2-4: 分别执行1-3次超频", "Circuits 2-4: Execute 1-3 stages of overclocking"));

        t.heading(t.tr("启动需求", "Startup Requirements"));
        t.requirement(t.tr("1024B宇宙素", "1024B Cosmic Element"));
        t.requirement(t.tr("1024KB氢", "1024KB Hydrogen"));
        t.requirement(t.tr("1024KB氦", "1024KB Helium"));
        t.feature(t.tr("氢氦每秒从输入仓抽入机器内部储存，每次启动配方各消耗1024KB", "Hydrogen & Helium are drawn from input hatches into internal storage every second; each recipe start consumes 1024KB of each"));

        t.heading(t.tr("模拟等级", "Simulation Tier"));
        t.requirement(t.tr("只能运行等级不高于模拟等级的配方", "Only recipes whose tier does not exceed the simulation tier can run"));
        t.feature(t.tr("模拟等级从1开始，启动与模拟等级相同的配方累计17+4×等级次后提升1级", "The simulation tier starts at 1 and rises by 1 after 17 + 4 × tier starts of recipes at the simulation tier"));
    });

    // 温室
    public static final TipSpec GreenhouseTooltips = GTOTips.of("greenhouse", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("需要阳光才能运行", "Requires sunlight to operate"));
        t.penalty(t.tr("太阳光照不足时速度减缓", "Speed slows down when sunlight is insufficient"));
    });

    // 蜂群之心
    public static final TipSpec SwarmCoreTooltips = GTOTips.of("swarm_core", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("能够运行任意等级的纳米锻炉配方", "Can run nano forge recipes of any tier"));
        t.bonus(t.tr("处理速度固定为基础速度×20", "Processing speed fixed at 20x"));
    });

    // 藻类农场
    public static final TipSpec AlgaeFarmTooltips = GTOTips.of("algae_farm", t -> {
        t.heading(t.tr("基础产出", "Base Production"));
        t.feature(t.tr("每10秒随机消耗5-10B水，随机输出1-10个藻类", "Every 10 seconds, randomly consumes 5-10B water and outputs 1-10 algae"));

        t.heading(t.tr("增产机制", "Yield Boost Mechanisms"));
        t.bonus(t.tr("输入10B发酵生物质 → 产量×10", "Input 10B Fermentation Biomass → 10x output"));
        t.bonus(t.tr("输入n个指定藻类 → 锁定产物 & 产量×(n/4)", "Input n specific algae → Lock output & ×(n/4) yield"));
    });

    // 聚合反应器
    public static final TipSpec PolymerizationReactorTooltips = GTOTips.of("polymerization_reactor", t -> {
        t.heading(ComponentSlang.CoilEfficiencyBonus);
        t.bonus(t.tr("线圈等级每高出白铜一级能耗与时间减少5%", "Each coil tier above Cupronickel, reduces energy consumption and duration by 5%"));
    });

    // 卫星控制中心
    public static final TipSpec SatelliteControlCenterTooltips = GTOTips.of("satellite_control_center", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("发射卫星，带回星球数据", "Launch a satellite and bring back planet data"));
        t.attention(t.tr("无法超频，每次发射固定耗时§e300§r秒", "Cannot be overclocked; each launch takes a fixed §e300§r seconds"));
        t.feature(t.tr("发射完成后返还火箭与星球数据芯片", "Returns the rocket and planet data chip after launch"));
    });

    // 原木拟生场
    public static final TipSpec TreeGrowthSimulatorTooltips = GTOTips.of("tree_growth_simulator", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("需要安装伐木工具，仅支持GT工具", "Requires GT-compatible tree cutting tool"));
        t.feature(t.tr("工具材料的采掘速度决定运行速度，采掘等级越高越快", "Tool material harvest speed sets the running speed; higher harvest level is faster"));
        t.feature(t.tr("电锯额外提升主产物产出", "A Chainsaw additionally boosts the main output"));
        t.heading(t.tr("附魔加成", "Enchantment Bonuses"));
        t.bonus(t.tr("时运：每级提升全部产物产出", "Fortune: each level boosts all outputs"));
        t.bonus(t.tr("效率：每级加快运行速度", "Efficiency: each level speeds up the machine"));
        t.bonus(t.tr("耐久：按原版规则减免工具磨损", "Unbreaking: reduces tool wear by vanilla rules"));
    });

    // 大型温室
    public static final TipSpec LargeGreenhouseTooltips = GTOTips.of("large_greenhouse", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.supported(t.tr("可以培育树木和一般作物", "Can cultivate trees and general crops"));
        t.supported(t.tr("无需阳光就能运行", "Can operate without sunlight"));
    });

    // 雕刻中心
    public static final TipSpec CarvingCenterTooltips = GTOTips.of("carving_center", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("根据全部电路之和决定输出", "Output determined by the sum of all circuits"));
        t.bonus(t.tr("电压等级每高出LV 1级，最大并行数×4", "Each tier above LV multiplies max parallel by 4"));
    });

    // BOSS召唤器
    public static final TipSpec BossSummonerTooltips = GTOTips.of("boss_summoner", t -> {
        t.line(TipRole.SUMMARY, t.tr("电力与反应核的作用", "Electricity and Reactor Core Function"));
    });

    // 钻井控制中枢
    public static final TipSpec DrillingControlCenterTooltips = GTOTips.of("drilling_control_center", t -> {
        t.heading(t.tr("范围增产", "Area Yield Boost"));
        t.bonus(t.tr("电压等级每高出IV一级，16m内钻机产量×1.5", "Each tier above IV → ×1.5 output for fluid drills within 16M"));
    });

    // 无人机控制中心
    public static final TipSpec DroneControlCenterTooltips = GTOTips.of("drone_control_center", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("范围内的维护仓出现故障时，派出无人机自动维修", "Dispatches drones to automatically repair malfunctioning Maintenance Hatches within range"));
        t.feature(t.tr("范围内的消声仓将满时，派出无人机自动清灰", "Dispatches drones to automatically clean Silencer Hatches when full within range"));
        t.feature(t.tr("主机GUI内可查看无人机工作范围", "Drone working range can be viewed in the controller GUI"));

        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("需在无人机仓内放入有电的无人机", "Requires charged drones in the Drone Hatch"));
        t.feature(t.tr("无人机等级决定工作范围与工作速度", "Drone tier determines operating range and work speed"));
    });

    // 无线能源塔
    public static final TipSpec WirelessEnergySubstationTooltips = GTOTips.of("wireless_energy_substation", t -> {
        t.line(TipRole.SUMMARY, t.tr("所在维度的储能设施，本维度的无线设备由此存取电量", "Energy storage for its dimension; wireless devices there store and draw energy from it"));

        t.heading(t.tr("结构", "Structure"));
        t.requirement(t.tr("宽为 3 至 11 之间的奇数，深为 3 至 11，高为 3 至 20", "Width is an odd number from 3 to 11, depth 3 to 11, height 3 to 20"));
        t.requirement(t.tr("棱使用无线能源塔框架，六个面使用同一等级的玻璃", "Edges use Wireless Energy Tower Frames; all six faces use glass of a single tier"));
        t.requirement(t.tr("控制器位于正面底边的正中", "The controller sits at the middle of the front bottom edge"));
        t.supported(t.tr("内部放存储单元与传输单元，无需全部填满", "Storage and transfer units go inside; the interior does not need to be filled"));
        t.heading(t.tr("电压", "Voltage"));
        t.feature(t.tr("提供电压 = 内部生效普通单元的最高等级 + 1，最高为 MAX；超级单元不提高提供电压", "Supplied voltage = highest effective ordinary unit tier + 1, at most MAX; super units do not raise the supplied voltage"));
        t.requirement(t.tr("玻璃等级是单元等级的上限，高于玻璃等级的单元不生效", "The glass tier caps the unit tier; units above it have no effect"));
        t.detail(t.tr("例如 HV 玻璃中放 HV 单元，能源塔提供 EV", "For example, HV units inside HV glass make the tower supply EV"));
        t.heading(t.tr("容量与吞吐", "Capacity and Throughput"));
        t.feature(t.tr("基础容量为全部生效普通存储单元的容量之和，损耗按容量加权平均", "Base capacity is the sum over the effective ordinary storage units; loss is averaged by capacity"));
        t.feature(t.tr("每个普通存储单元提供本单元电压 4A 的吞吐，普通传输单元按等级提供更大的吞吐", "Each ordinary storage unit adds 4A at its own voltage; ordinary transfer units add more throughput by tier"));
        t.detail(t.tr("吞吐按功率计：以更高电压取电时安数按电压折算，例如 HV 4A 相当于 EV 1A", "Throughput is power: drawing at a higher voltage converts the amps, e.g. 4A of HV equals 1A of EV"));
        t.bonus(t.tr("UHV 起可使用超级储存与超级传输单元，分别为本站整体容量与吞吐提供额外的叠加乘区", "From UHV, super storage and transfer units add separate tower-wide multipliers to capacity and throughput; multipliers of the same kind add together"));
        t.detail(t.tr("输入与输出各自计算，每 tick 重新计额", "Input and output are counted separately and refresh every tick"));
        t.detail(t.tr("有持续时间的整笔扣电在整个时长内按每 tick 功率占用吞吐，一次性的整笔扣电与存入不占用吞吐", "Lump charges with a duration hold throughput per tick for their whole duration; one-off lump charges and deposits use none"));
        t.heading(t.tr("同维度合并", "Combining in a Dimension"));
        t.feature(t.tr("同一维度中提供电压相同的能源塔合为一档：容量、存量与吞吐相加", "Towers in one dimension that supply the same voltage form one tier: capacity, stored energy and throughput add up"));
        t.heading(t.tr("存取规则", "Access Rules"));
        t.requirement(t.tr("取电：设备电压不能高于档位电压，从满足条件的最低档开始取，受各档输出吞吐限制", "Drawing: the device voltage may not exceed the tier voltage; the lowest qualifying tier is used first, limited by each tier's output throughput"));
        t.supported(t.tr("存电：不限电压，只受各档输入吞吐限制，从最高档开始存", "Storing: any voltage, limited only by each tier's input throughput, highest tier first"));
        t.detail(t.tr("本维度不够时，经维度中继器的线路就近从其他维度存取电，取电时线路电压也不能低于设备电压", "When this dimension falls short, energy moves to or from the nearest dimensions over relay lines; for drawing, the line voltage may not be below the device voltage either"));
        t.detail(t.tr("跨维度存取计入电量所在维度的吞吐", "Cross-dimension draws and stores count against the throughput of the dimension holding the energy"));
    });

    // 无线电网维度中继器
    public static final TipSpec WirelessDimensionRepeaterTooltips = GTOTips.of("wireless_dimension_repeater", t -> {
        t.heading(t.tr("在两个维度之间架设电网线路", "Builds a grid line between two dimensions"));
        t.feature(t.tr("在所在维度和界面选定的维度之间建立跨维度电力传输线路", "Builds a cross-dimension power line between its own dimension and the one chosen in its interface"));
        t.feature(t.tr("每台为线路提供 99,999,999A，电压取整体框架等级", "Each repeater adds 99,999,999A to the line at the integral framework tier"));
        t.feature(t.tr("同一对维度之间的多台叠加，按功率折算到其中最高的电压", "Repeaters between the same pair of dimensions stack, converted by power to the highest voltage"));
        t.feature(t.tr("线路两个方向各自独立，互不占用", "The two directions of a line are independent of each other"));
        t.feature(t.tr("取电时优先使用本维度能源塔，不足时按跳数由近及远经线路从其他维度取电", "Devices draw from local towers first, then from other dimensions over lines, nearest first"));
        t.requirement(t.tr("取电经过的线路电压不能低于设备电压，存电不限线路电压", "Lines a draw passes through may not be below the device voltage; storing ignores the line voltage"));
    });

    // 拉丝塔
    public static final TipSpec DrawingTowerTooltips = GTOTips.of("drawing_tower", t -> {
        t.heading(ComponentSlang.EfficiencyBonus);
        t.formula(t.tr("时间乘数 = 2 / 1.2^[(高度/8)×(温度-5000)/900] ≥ 0.00001", "Time Multiplier = 2 / 1.2^[(height/8)×(temp−5000)/900] ≥ 0.00001"));
        t.formula(t.tr("并行数 = log₁.₀₈(温度−9600) − 84 ≥ 1", "Parallel = log₁.₀₈(temp−9600) − 84 ≥ 1"));
    });

    // ME存储器
    public static final TipSpec MEStorageTooltips = GTOTips.of("me_storage", t -> {
        t.heading(t.tr("ME存储", "ME Storage"));
        t.supported(t.tr("不受存储类型限制", "Without storage type restrictions"));
        t.control(t.tr("你需要在结构中安装存储核心来提升容量", "Install Storage Cores in structure to increase capacity."));
        t.control(t.tr("使用§eME数据访问仓§r连接ME线缆来访问储存器。", "Connect via §eME Data Access Hatch§r to access storage."));
        t.feature(t.tr("数据索引可选玩家或机器；选玩家时，同一玩家的这类设备共用同一份存储，不同 ME 网络之间可以共通", "The data index can be the player or the machine; with the player index, such machines of the same player share one storage, so different ME networks can share contents"));
        t.feature(t.tr("结构可以延长，在EMI看看能有多长吧！", "Structure is extendable — check max length in EMI."));

        String bytes = NumberUtils.formatLongToKorM(MEStorageMachine.infinite);

        t.heading(Tx.rainbowSlow(t.tr("无限容量模式：满足下列条件时自动启用", "Infinite Capacity Mode: Automatically enable when the following conditions are met")));
        t.requirement(t.tr("至少安装有 " + bytes + " Bytes 容量", "At least " + bytes + " Bytes capacity installed"));
        t.requirement(t.tr("至少安装有 64 个无限存储组件", "At least 64 Infinite Storage Components installed"));
    });

    // 原始蒸馏塔
    public static final TipSpec PrimitiveDistillationTowerTooltips = GTOTips.of("primitive_distillation_tower", t -> {
        t.heading(t.tr("运行机制", "Operation Mechanics"));
        t.bonus(t.tr("更高的底部温度和更长的连续运行时间能显著加快配方速度", "Higher bottom temperatures and longer continuous operation significantly speed up recipes"));
        t.requirement(t.tr("最高13层，每个产物都需要一个对应高度的输出仓", "Each fluid product requires a Fluid Hatch at a corresponding Y-level"));
        t.feature(t.tr("每层都可以不安装输出仓，但是对应层会没有产物", "Each layer can operate without installing an output hatch, but the corresponding layer will then produce no output"));
        t.requirement(t.tr("只能处理MV及以下的配方", "Can only process recipes of MV tier or lower"));

        t.heading(t.tr("热管理机制", "Heat Management"));
        t.requirement(t.tr("需要 >400K 的温度才能运行配方", "Requires >400K Temperature to process recipes"));
        t.feature(t.tr("运行配方会按1HU/t速度消耗底部热量，按0.5HU/t速度产生顶部热量", "Processing recipes will consume bottom heat at a speed of 1HU/t and generate top heat at a speed of 0.5HU/t"));
    });

    public static final TipSpec ChemicalVaporDepositionTooltips = GTOTips.of("chemical_vapor_deposition", t -> {
        t.heading(ComponentSlang.EfficiencyBonus);
        t.bonus(t.tr("线圈温度越高，运行速度越快", "Higher coil temperature → faster operation"));
        t.formula(t.tr("时间乘数: 0.8×0.4^((线圈温度-5400)/9000)", "Time Multiplier: 0.8x0.4^((Temperature-5400)/9000)"));
        t.heading(ComponentSlang.ParallelBonus);
        t.feature(t.tr("由电压等级决定", "Determined by Voltage Tier"));
        t.formula(t.tr("公式 : 4^(电压等级 - 1)", "Formula: 4^(Voltage Tier - 1)"));
    });

    public static final TipSpec PhysicalVaporDepositionTooltips = GTOTips.of("physical_vapor_deposition", t -> {
        t.heading(ComponentSlang.EfficiencyBonus);
        t.bonus(t.tr("玻璃等级越高，运行速度越快", "Higher glass tier → faster operation"));
        t.formula(t.tr("时间乘数: √(1 / 玻璃等级)", "Time Multiplier: √(1 / Glass Tier)"));
        t.heading(ComponentSlang.ParallelBonus);
        t.feature(t.tr("由电压等级决定", "Determined by Voltage Tier"));
        t.formula(t.tr("公式 : 4^(电压等级 - 1)", "Formula: 4^(Voltage Tier - 1)"));
    });

    public static final TipSpec plasmaCondenserTooltips = GTOTips.of("plasma_condenser", t -> {
        t.heading(ComponentSlang.EfficiencyBonus);
        t.bonus(t.tr("玻璃等级越高，运行速度越快", "Higher glass tier → faster operation"));
        t.formula(t.tr("耗时减免公式 : 1/1.1^玻璃等级", "Duration reduction formula : 1/1.1^Glass Tier"));
    });

    public static final TipSpec BiochemicalExtractionTooltips = GTOTips.of("biochemical_extraction", t -> {
        t.heading(t.tr("运行机制", "Operation Mechanism"));
        t.requirement(t.tr("机器运行时需要输入特定流体，且只能输入当前要求的那一种，否则中断配方", "The machine requires specific fluids during operation, and only the one currently required may be input; otherwise, the recipe is interrupted"));
        t.requirement(t.tr("每秒需要输入1B营养精华一次，成功后发出一次红石信号", "It needs to input 1B nutrient distillation once per second, and upon success, emits a redstone signal once"));
        t.requirement(t.tr("连续运行5秒后需要输入1B浓缩云之精华一次", "After continuous operation for 5 seconds, it needs to input 1B cloud seed concentrated once"));
        t.requirement(t.tr("连续运行15秒后需要输入1B火焰水一次", "After continuous operation for 15 seconds, it needs to input 1B fire water once"));
        t.requirement(t.tr("连续运行20秒后需要输入1B轻盈之气一次", "After continuous operation for 20 seconds, it needs to input 1B vapor of levity once"));
        t.warning(t.tr("如果连续运行要求输入的流体不符合要求，则中断配方", "If the required fluids for continuous operation do not meet the requirements, the recipe is interrupted"));
        t.supported(t.tr("营养精华可无视限制与其他流体同时输入", "Nutrient distillation can be input simultaneously with other fluids, ignoring the restrictions"));
        t.feature(t.tr("20秒后配方开始输出，此后只需提供1B/s的营养精华", "After 20 seconds, the recipe begins outputting, and from then on it only requires 1B/s of nutrient distillation"));
        t.heading(t.tr("ME延缓机制（实验性）", "ME Startup Delay (Experimental)"));
        t.requirement(t.tr("结构成型后，若有ME部件尚未上线，则暂不开始运行，直到全部上线", "After the structure forms, the machine holds off running while any ME part is still offline, until all are online"));
        t.feature(t.tr("最多暂缓5秒，期间不消耗原料、不推进进度，连续运行时间保持不变", "The hold lasts at most 5 seconds; nothing is consumed or advanced meanwhile, and the continuous running time is kept"));
    });

    public static final TipSpec PlanetCoreDrillingTooltips = GTOTips.of("planet_core_drilling", t -> {
        t.feature(t.tr("每秒产出当前世界的全部矿石65536份", "Produces a total of 65536 ores from the current world per second"));
    });

    public static final TipSpec AdvancedInfiniteDrillerTooltips = GTOTips.of("advanced_infinite_driller", t -> {
        t.lore(t.tr("利用维度技术和坚不可摧的钻头无情地抽取星球的每一分血液", "Using dimensional technology and indestructible drills, they relentlessly extract every drop of blood from the planet."));
        t.heading(t.tr("启动与温控", "Startup & Thermal Control"));
        t.requirement(t.tr("需要升温启动，可通入液态烈焰或液态炽骨立方", "Requires heating to start, input Liquid Blaze or Liquid Blazecube"));
        t.formula(t.tr("升温材料消耗公式(单位：mB): 温度^1.3", "Heating material consumption formula(unit: mB): Temperature^1.3"));
        t.statText(t.tr("升温速度：(每5tick) 液态烈焰：1K / 每次输入，液态炽骨立方：1000K / 每次输入", "Heating speed: (every 5 ticks) Liquid Blaze: 1K / per input, Liquid Blazecube: 1000K / per input"));
        t.bonus(t.tr("随着温度提升，效率也会提升", "Higher temperature will give higher efficiency"));
        t.warning(t.tr("当温度超过临界值，钻头将会熔毁", "If the machine overheats, drill head will melt"));
        t.formula(t.tr("产热公式: 温度 / 2000", "Heat generation formula: Temperature / 2000"));
        t.heading(t.tr("冷却系统", "Cooling System"));
        t.statText(t.tr("冷却固定消耗(每5tick): 20B", "Fixed cooling consumption(every 5 ticks): 20B"));
        t.feature(t.tr("蒸馏水 1K/每次输入", "Distilled Water 1K/per input"));
        t.feature(t.tr("液态氧 2K/每次输入", "Liquid Oxygen 2K/per input"));
        t.feature(t.tr("液态氦 4K/每次输入", "Liquid Helium 4K/per input"));
        t.requirement(t.tr("仅可放入中子素钻头(更多钻头待定)", "Only Neutron-element drill heads can be placed (more to be determined)"));
    });

    public static final TipSpec ThermalPowerPumpTooltips = GTOTips.of("thermal_power_pump", t -> {
        t.heading(t.tr("高效供水", "Efficient Water Supply"));
        t.feature(t.tr("输入蒸汽，产生同等数量的水", "Input steam → outputs equal amount of water"));
    });

    public static final TipSpec VoidFluidDrillTooltips = GTOTips.of("void_fluid_drilling_rig", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.control(t.tr("在主机中放入维度数据来设置钻取维度", "Insert dimension data into the controller to set the drilling dimension"));
        t.requirement(t.tr("需要最低30720EU/t的功率", "Requires minimum 30720 EU/t"));
        t.heading(t.tr("产出机制", "Output Mechanism"));
        t.feature(t.tr("放入数据后，主机将显示可钻取流体列表", "After inserting the data, the controller will display a list of drillable fluids"));
        t.feature(t.tr("根据机器设置的电路按顺序选择钻取目标", "Select the drilling target in order according to the set circuit"));
    });

    public static final ImmutableMap<String, Float> MATERIAL_MAP = ImmutableMap.<String, Float>builder()
            .put("gtceu.iron", 1.0f)
            .put("gtceu.iridium", 1.1f)
            .put("gtocore.orichalcum", 1.2f)
            .put("gtocore.infuscolium", 1.3f)
            .put("gtocore.draconium", 1.4f)
            .put("gtocore.cosmic_neutronium", 1.5f)
            .put("gtocore.eternity", 1.6f)
            .build();

    public static final ImmutableMap<String, String> MATERIAL_TIER_MAP = ImmutableMap.<String, String>builder()
            .put("gtceu.iron", "ZPM")
            .put("gtceu.iridium", "UV")
            .put("gtocore.orichalcum", "UHV")
            .put("gtocore.infuscolium", "UEV")
            .put("gtocore.draconium", "UIV")
            .put("gtocore.cosmic_neutronium", "UXV")
            .put("gtocore.eternity", "OpV")
            .build();

    public static final TipSpec NanitesIntegratedProcessingCenterTooltips = GTOTips.of("nanites_integrated_processing_center", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.control(t.tr("安装对应模块解锁对应配方", "Install the corresponding module to unlock the corresponding recipe"));
        t.bonus(t.tr("主机中放入纳米蜂群可减少污染概率", "Placing nanites in the host can reduce pollution probability"));
        t.feature(t.tr("每放入一个纳米蜂群，最低主机电压和污染概率减少数如下所示", "Each nanite placed requires the minimum voltage and reduces pollution probability as follows"));
        MATERIAL_MAP.forEach((material, reduction) -> t.statText(Tx.join(
                Component.translatable("material." + material),
                Tx.lit("[" + MATERIAL_TIER_MAP.get(material) + "]: -" + reduction + "%"))));
    });

    public static final TipSpec NanitesModuleTooltips = GTOTips.of("nanites_module", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("需要安装到纳米集成加工中心上才能工作", "Needs to be installed on a Nanites Integrated Processing Center to work"));
    });

    public static final TipSpec VoidMinerTooltips = GTOTips.of("void_miner", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.control(t.tr("在主机中放入维度数据来设置采矿维度", "Insert dimension data into the controller to set the mining dimension"));
        t.requirement(t.tr("需要输入1B钻井液和最低1920EU/t的功率", "Requires 1B drilling fluid and minimum 1920 EU/t"));
        t.heading(t.tr("产出机制", "Output Mechanism"));
        t.feature(t.tr("按维度随机选取4种矿石输出", "Randomly selects 4 ore types based on dimension"));
        t.bonus(t.tr("电压等级决定单次最大输出数量", "Voltage tier determines max output quantity per cycle"));
        t.bonus(t.tr("电流决定并行数", "Amperage determines parallel count"));
    });

    public static final TipSpec PrecisionAssemblerTooltips = GTOTips.of("precision_assembler", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("外壳等级决定配方等级上限", "Machine casing tier determines the upper limit of recipe tier"));
    });

    public static final TipSpec LavaFurnaceTooltips = GTOTips.of("lava_furnace", t -> {
        t.heading(t.tr("地热转化", "Geothermal Conversion"));
        t.feature(t.tr("每提供一个任意类型的圆石或石头可输出§c1B§7熔岩", "Each cobblestone or stone provided outputs §c1B§7 lava"));
    });

    public static final TipSpec EngravingLaserPlantTooltips = GTOTips.of("engraving_laser_plant", t -> {
        t.heading(ComponentSlang.EfficiencyBonus);
        t.bonus(t.tr("运行激光焊接配方时速度×5", "Running Laser Welder recipes at 5x speed"));
    });

    public static final TipSpec IntegratedOreProcessorTooltips = GTOTips.of("integrated_ore_processor", t -> {
        t.feature(t.tr("一步完成矿石处理", "Completes ore processing in one step"));
        t.heading(t.tr("电路配置", "Circuit Configuration"));
        t.feature(t.tr("1号电路: 破碎-研磨-离心", "Circuit 1: Crusher → Macerator → Centrifuging"));
        t.feature(t.tr("2号电路: 破碎-洗矿-热离-研磨", "Circuit 2: Crusher → Ore Washer → Thermal Separation → Macerator"));
        t.feature(t.tr("3号电路: 破碎-洗矿-研磨-离心", "Circuit 3: Crusher → Ore Washer → Macerator → Centrifuging"));
        t.feature(t.tr("4号电路: 破碎-洗矿-筛选-离心", "Circuit 4: Crusher → Ore Washer → Sifter → Centrifuging"));
        t.feature(t.tr("5号电路: 破碎-浸洗-热离-研磨", "Circuit 5: Crusher → Chemical Bath → Thermal Centrifuging → Macerator"));
        t.feature(t.tr("6号电路: 破碎-浸洗-研磨-离心", "Circuit 6: Crusher → Chemical Bath → Macerator → Centrifuging"));
        t.feature(t.tr("7号电路: 破碎-浸洗-筛选-离心", "Circuit 7: Crusher → Chemical Bath → Sifter → Centrifuging"));
        t.feature(t.tr("8号电路: 破碎-洗矿-研磨-电磁选矿", "Circuit 8: Crusher → Ore Washer → Macerator → Electromagnetic Separation"));
        t.heading(ComponentSlang.AfterModuleInstallation);
        t.supported(t.tr("解锁8线程处理", "Unlock 8-thread processing"));
    });

    public static final TipSpec LargeSteamCircuitAssemblerTooltips = GTOTips.of("large_steam_circuit_assembler", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("只能执行GT电路配方", "Can only execute GT circuit recipes"));
        t.requirement(t.tr("蚀刻电路后机器才能工作，并且只能加工此电路", "The machine requires engraving a circuit to operate and only processes that specific circuit"));
        t.heading(t.tr("配方等级", "Recipe Tier"));
        t.requirement(ComponentSlang.RecipeLevelBelow(GTValues.MV));
        String name = GTValues.VNF[GTValues.HV] + "§r";
        t.bonus(t.tr("可安装大型蒸汽输入仓提升至" + name + "等级", "Install Large Steam Input Hatch to upgrade to " + name + " recipe tier"));
        t.supported(t.tr("同时解锁超频功能", "Unlocks overclocking function"));
        t.heading(t.tr("电路倍产", "Circuit Multiplication"));
        t.bonus(t.tr("开启增产模式后倍增产出 (默认开启)", "Multiply Mode multiplies circuit output (Enabled by default)"));
    });

    public static final TipSpec LargeSteamSolarBoilerTooltips = GTOTips.of("large_steam_solar_boiler", t -> {
        t.heading(t.tr("蒸汽产出", "Steam Production"));
        t.feature(t.tr("根据集热管数量决定蒸汽产量", "Steam production determined by number of collector tubes"));
        t.requirement(t.tr("只能在太阳下工作", "Can only operate under the sun"));
        t.warning(t.tr("供水不足时将爆炸", "It will explode when the water supply is insufficient"));
        t.heading(t.tr("可用大小", "Usable Size"));
        t.statText(t.tr("最小：5x5", "Minimum: 5x5"));
        t.statText(t.tr("最大：31x31", "Maximum: 31x31"));
    });

    public static final TipSpec ComponentAssemblerTooltips = GTOTips.of("component_assembler", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(ComponentSlang.RecipeLevelBelow(GTValues.IV));
        t.bonus(Tx.join(
                t.tr("升级结构后支持到", "After upgrading the structure, it supports tier "),
                Tx.lit(GTValues.VNF[GTValues.UV]),
                t.tr("，并允许安装加速仓", ", and allows installing Accelerate Hatches")));
        t.bonus(t.tr("再次升级结构后支持并行控制仓和超高安仓", "After upgrading the structure again, it supports Parallel Control Hatches and High-Amp Hatches"));
    });

    public static final TipSpec SteamMixerTooltips = GTOTips.of("steam_mixer", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.unsupported(t.tr("无法处理流体配方", "Cannot process fluids"));
    });

    public static final TipSpec LeapForwardOneBlastFurnaceTooltips = GTOTips.of("leap_forward_one_blast_furnace", t -> {
        t.lore(t.tr("我们走在大路上！", "We're on the road!"));
        t.heading(ComponentSlang.ParallelBonus);
        t.feature(t.tr("结构的高度决定了基础并行数量", "The height of the structure determines the base parallel count"));
        t.bonus(t.tr("每层提供 2 并行", "Each layer of Steel Frames provides 2 parallels"));
        t.bonus(t.tr("每 500K 温度为配方提供 1× 并行乘数", "Provides a 1x parallel multiplier for recipes every 500K of temperature"));
        t.heading(ComponentSlang.EfficiencyBonus);
        t.feature(t.tr("随着配方运行，温度升高，空闲时则会降低", "As the recipe operates, the temperature increases, and it will decrease when idle"));
        t.bonus(t.tr("下次配方的耗时乘以 (400 / 温度)", "The next recipe gains a duration multiplier reduction of (400 / temperature)"));
        t.warning(t.tr("运行时，烟囱内部会对生物造成致命高温伤害！", "When running, the inside of the chimney will cause fatal high-temperature damage to living beings!"));
    });

    public static final TipSpec NeutronVortexTooltips = GTOTips.of("neutron_vortex", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.bonus(t.tr("运行时不消耗中子动能", "Does not consume neutron kinetic energy while running."));
        t.control(t.tr("搭建能源接收段后，可在主界面的能源接收器节点中启用能源接收器", "With the Energy Acceptor Section built, the Energy Acceptor can be enabled in its node on the main page"));
        t.unsupported(t.tr("未搭建能源接收段时，能源接收器不可用", "Without the Energy Acceptor Section, the Energy Acceptor is unavailable"));
        t.bonus(t.tr("能源接收器激活后，将消耗电力自动适应配方的中子动能", "After activating the Energy Acceptor, it will consume electricity to automatically adapt to the neutron kinetic energy of the recipe"));
    });

    public static final TipSpec MicroorganismMasterTooltips = GTOTips.of("microorganism_master", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.bonus(t.tr("无视辐射与洁净要求", "Ignores radiation and cleanliness requirements"));
    });

    public static final TipSpec DisassemblyTooltips = GTOTips.of("disassembly", t -> {
        t.heading(t.tr("回收机器", "Disassemble Machines"));
        Component comma = Tx.lit(", ");
        t.feature(Tx.join(
                t.tr("可拆解: ", "Can disassemble: "),
                Component.translatable("gtceu.assembler"),
                comma,
                Component.translatable("gtceu.precision_assembler"),
                comma,
                Component.translatable("gtceu.assembler_module"),
                comma,
                Component.translatable("gtceu.assembly_line"),
                comma,
                Component.translatable("gtceu.circuit_assembly_line"),
                comma,
                Component.translatable("gtceu.suprachronal_assembly_line")));
        t.feature(t.tr("同对应配方时间与耗能", "Same duration and energy consumption as original recipe"));
        t.unsupported(t.tr("可由多个配方获取的物品无法拆解", "Items obtainable from multiple recipes cannot be disassembled"));
    });

    public static final TipSpec IndustrialFlotationCellTooltips = GTOTips.of("industrial_flotation_cell", t -> {
        t.feature(t.tr("工业级浮游选矿池", "Industrial Flotation Mining Pool"));
    });

    public static final TipSpec CompoundExtremeCoolingUnitTooltips = GTOTips.of("compound_extreme_cooling_unit", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("机器模式为等离子冷凝模式且已搭建等离子冷凝翼时，才可以运行等离子冷凝配方", "Plasma Condenser recipes require Plasma Condenser mode and the Plasma Condensing Wings"));
        t.feature(t.tr("等离子冷凝翼为可选结构，不影响其他模式的运行", "The Plasma Condensing Wings are optional and do not affect other modes"));
    });

    public static final TipSpec StellarForgeTooltips = GTOTips.of("stellar_forge", t -> {
        t.heading(t.tr("连续运行加成", "Continuous Operation Bonus"));
        t.bonus(t.tr("配方等级不受能源仓限制，连续运行优化", "Recipe tier is not limited by energy hatch. Continuous Operation can Optimize"));
        t.bonus(t.tr("首次运行后继续运行，后续配方时间减少50%", "After first run, continue → 50% duration reduction on subsequent runs"));
        t.warning(Tx.join(t.tr("运行中供电不足会产生巨大", "Power shortage during operation causes massive "), ComponentSlang.Explosion));
        t.heading(ComponentSlang.AfterModuleInstallation);
        t.supported(t.tr("安装魔法模块后，可以运行魔法花朵的产魔配方", "After installing the Magic Module, you can run magic flower producing recipes"));
        t.bonus(t.tr("连续运行对数级提升魔力产出量", "Continuous operation logarithmically increases magic output"));
        t.unsupported(t.tr("产魔配方不执行超频", "Magic producing recipes do not execute overclocking"));
        t.warning(t.tr("机器内魔力储存量满后，多余的魔力将被销毁", "After the machine's magic storage is full, excess magic will be destroyed"));
    });

    public static final TipSpec RoadOfHeavenTooltips = GTOTips.of("road_of_heaven", t -> {
        t.heading(t.tr("模块运行优化系统", "Module Operation Optimization System"));
        t.supported(t.tr("可安装最多64个拓展模块", "Can install up to 64 expansion modules"));
        t.bonus(t.tr("提升电压等级可为模块提供大幅耗时减免", "Increasing voltage tier can provide large Duration reductions for modules"));
        t.bonus(t.tr("额外提升为模块提供的并行数", " Additional increase in the parallelism provided by the module"));
        t.requirement(t.tr("运行前需提供128*(电压等级-7)的算力", "Before starting, it is necessary to provide 128 * (voltage tier - 7) computation power"));
        t.bonus(t.tr("连接的模块将获得耗时×0.707的速度加成", "Connected modules will receive a 0.707x Duration speed bonus"));
    });

    public static final TipSpec WaterPurificationPlantTooltips = GTOTips.of("water_purification_plant", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("净水系统的总控中枢", "The central control hub of the water purification system"));
        t.feature(t.tr("链接净化单元，同步所有净化单元的控制器的处理周期并供电", "Link purification units, synchronize the processing cycles of all controllers, and provide power"));
        t.heading(t.tr("处理单元链接系统", "Processing Unit Link System"));
        t.feature(t.tr("自动链接§e32§r个方块半径内的净化单元控制器", "Automatically link purification unit controllers within a §e32§r-blocks radius"));
        t.feature(t.tr("主机GUI中可查看链接范围", "The linking range can be viewed in the controller GUI"));
        t.feature(t.tr("为链接的单元控制器提供电力", "Provide power to linked unit controllers"));
        t.formula(t.tr("默认耗能 = 输出水量 × 2^(净化水等级 - 2)", "Default energy = output water × 2^(purification tier - 2)"));
        t.heading(t.tr("处理周期系统", "Processing Cycle System"));
        t.statText(t.tr("固定处理周期: 120 秒", "Fixed processing cycle: 120 seconds"));
        t.formula(t.tr("所有控制器同步周期，净化水输出转化率 = 输入水量 × 90%", "All controllers sync cycle — purified output = input water × 90%"));
        t.warning(t.tr("前三道净化工序概率判定失败时，返还90%的输入水；其余工序判定失败时不返还净化水", "If the chance roll fails during the first three purification steps, 90% of the input water is returned; failed rolls in later steps return no purified water"));
    });

    public static final TipSpec ClarifierPurificationUnitTooltips = GTOTips.of("clarifier_purification_unit", t -> {
        t.feature(ComponentSlang.PurifyLevel(1));
        t.heading(ComponentSlang.RunningRequirements);
        t.attention(t.tr("处理一定水量后过滤器堵塞", "Filter blocks clog after processing certain water volume"));
        t.requirement(t.tr("需输入空气(1-8KB) + 水(200-300B)进行反冲洗", "Requires air (1-8KB) + water (200-300B) for backflushing"));
        t.feature(t.tr("反冲洗时输出废料", "Outputs waste during backflushing"));
        t.heading(ComponentSlang.OutputProbability);
        t.statText(t.tr("基础概率: 70%", "Base probability: 70%"));
        t.bonus(t.tr("输入少量同等级净化水可提升15%", "Adding a small amount of purified water of the same grade increases probability by 15%"));
        t.bonus(t.tr("输入更高等级净化水每级额外增加5%，最高4级达到100%", "Each higher grade of purified water adds 5% per level, up to 4 levels maximum for 100% probability"));
    });

    public static final TipSpec OzonationPurificationUnitTooltips = GTOTips.of("ozonation_purification_unit", t -> {
        t.feature(ComponentSlang.PurifyLevel(2));
        t.heading(ComponentSlang.RunningRequirements);
        t.formula(t.tr("臭氧消耗量=输入水量/10,000mB", "Ozone consumption = input water amount / 10,000mB"));
        t.warning(Tx.join(t.tr("如果输入口含有超过1,024B的臭氧气体，将发生", "If the input contains more than 1,024B of ozone gas, it will cause an "), ComponentSlang.Explosion));
        t.heading(ComponentSlang.OutputProbability);
        t.feature(t.tr("臭氧气体在0-1,024B范围内的产出概率为0-80%", "Output probability ranges from 0-80% based on ozone gas amount (0-1,024B)"));
        t.bonus(t.tr("输入少量同等级净化水可提升15%", "Adding a small amount of purified water of the same grade increases probability by 15%"));
        t.bonus(t.tr("输入更高一级净化水可额外提升5%", "Adding a higher grade of purified water provides an additional 5% increase"));
    });

    public static final TipSpec FlocculationPurificationUnitTooltips = GTOTips.of("flocculation_purification_unit", t -> {
        t.feature(ComponentSlang.PurifyLevel(3));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("提供聚合氯化铝以进行操作", "Provide Polymeric Aluminum Chloride for operation."));
        t.feature(t.tr("输出可循环利用的絮凝废液", "Outputs recyclable Flocculent Waste Liquid."));
        t.attention(t.tr("在操作过程中，将消耗输入仓中的所有聚合氯化铝", "During operation, all Polymeric Aluminum Chloride in the input chamber will be consumed."));
        t.heading(ComponentSlang.OutputProbability);
        t.bonus(t.tr("每消耗100,000mB聚合氯化铝，成功率额外增加10.0%", "For every 100,000mB of Polymeric Aluminum Chloride consumed, success rate increases by 10.0%."));
        t.penalty(t.tr("如果提供的液体总量不是100,000mB的倍数，则应用成功率惩罚：", "If total liquid provided is not a multiple of 100,000mB, apply success rate penalty:"));
        t.formula(t.tr("成功率 = 成功率 * 2 ^ (-10 * 溢出比率)", "Success Rate = Success Rate * 2 ^ (-10 * Overflow Ratio)"));
    });

    public static final TipSpec PHNeutralizationPurificationUnitTooltips = GTOTips.of("ph_neutralization_purification_unit", t -> {
        t.feature(ComponentSlang.PurifyLevel(4));
        t.heading(ComponentSlang.RunningRequirements);
        t.feature(t.tr("每个周期的初始pH值在4.5至9.5之间变化", "The initial pH value of each cycle varies between 4.5 to 9.5."));
        t.supported(t.tr("机器工作时可使用pH传感器读取当前pH值并输出红石信号", "During operation, use pH sensor to read current pH and output redstone signal."));
        t.attention(t.tr("每秒消耗所有输入的§e氢氧化钠§r和§e盐酸§r", "Consumes all input Sodium Hydroxide and Hydrochloric Acid every second."));
        t.heading(ComponentSlang.OutputProbability);
        t.bonus(t.tr("周期结束时pH值在7.0 ±0.05范围内，则配方必定成功", "If pH is within 7.0 ±0.05 at cycle end, then recipe always succeeds."));
        t.warning(t.tr("否则配方必定失败", "Otherwise recipe always fails."));
        t.heading(t.tr("PH调节", "PH Adjustment Mechanism"));
        t.feature(t.tr("每消耗1个氢氧化钠粉：pH提高0.01", "Each Sodium Hydroxide Powder consumed: pH +0.01"));
        t.feature(t.tr("每消耗10mB盐酸：pH降低0.01", "Each 10mB Hydrochloric Acid consumed: pH -0.01"));
    });

    public static final TipSpec ExtremeTemperatureFluctuationPurificationUnitTooltips = GTOTips.of("extreme_temperature_fluctuation_purification_unit", t -> {
        t.feature(ComponentSlang.PurifyLevel(5));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("完成加热周期：先加热水至10,000K以上，再冷却至10K以下", "Complete heating cycle: first heat Water above 10,000K, then cool below 10K."));
        t.feature(t.tr("配方开始时初始温度重置为298K", "Initial temperature reset to 298K at recipe start."));
        t.attention(t.tr("每秒最多消耗10mB氦等离子体和100mB液氦", "Consumes up to 10mB Helium Plasma and 100mB Liquid Helium per second."));
        t.heading(ComponentSlang.OutputProbability);
        t.bonus(t.tr("每完成一个加热周期，成功率增加33%", "For each completed heating cycle, success rate increases by 33%."));
        t.heading(t.tr("温度调节", "Temperature Regulation Mechanism"));
        t.feature(t.tr("每消耗1mB氦等离子体：温度升高80-120K", "Each 1mB Helium Plasma consumed: temperature +80-120K"));
        t.feature(t.tr("每消耗1mB液氦：温度降低4-6K", "Each 1mB Liquid Helium consumed: temperature -4-6K"));
        t.warning(t.tr("温度达到12,500K：配方失败并输出超临界蒸汽", "If temperature reaches 12,500K: recipe fails and outputs Supercritical Steam"));
    });

    public static final TipSpec HighEnergyLaserPurificationUnitTooltips = GTOTips.of("high_energy_laser_purification_unit", t -> {
        t.feature(ComponentSlang.PurifyLevel(6));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("在操作过程中，需要更换透镜仓内的透镜", "During operation, you need replace the Lens in the lens chamber."));
        t.feature(t.tr("当前透镜需要更换时，多方块结构将通过透镜指示仓输出信号", "When the current Lens needs to be replaced, the multi-block structure will output a signal through the Lens indicator chamber."));
        t.feature(t.tr("透镜更换请求将在6到12秒的随机间隔内出现", "Lens replacement requests will occur at random intervals between 6 and 12 seconds."));
        t.requirement(t.tr("需要在信号输出后的4秒内更换透镜", "The lens must be replaced within 4 seconds of the signal output."));
        t.feature(t.tr("透镜顺序依次为红/橙/棕/黄/绿/青/蓝/紫/品红/粉", "The lens order is Red/Orange/Brown/Yellow/Green/Cyan/Blue/Purple/Magenta/Pink."));
        t.feature(t.tr("透镜需求可在GUI内查看", "The requirements for Lens can be viewed in the GUI."));
        t.heading(ComponentSlang.OutputProbability);
        t.bonus(t.tr("每次成功更换后运行4秒将成功率提高10%", "Each successful replacement followed by 4 seconds of operation increases the success rate by 10%."));
    });

    public static final TipSpec ResidualDecontaminantDegasserPurificationUnitTooltips = GTOTips.of("residual_decontaminant_degasser_purification_unit", t -> {
        t.feature(ComponentSlang.PurifyLevel(7));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("要成功完成配方，需要根据要求输入材料", "To successfully complete the recipe, materials must be inputted as required."));
        t.attention(t.tr("操作开始时，脱气控制仓将输出红石信号，机器每秒将消耗全部输入的材料", "At the operation start, the degassing control chamber will output a redstone signal, and the machine will consume all input materials every second."));
        t.feature(t.tr("红石信号与需求相对应", "The redstone signal corresponds to the demand."));
        t.heading(t.tr("信号需求", "Signal Requirement"));
        t.requirement(t.tr("1, 3, 5, 7, 9：通过惰性气体进行臭氧曝气", "1, 3, 5, 7, 9: Ozone aeration via Inert Gases"));
        t.feature(t.tr("对应 10,000mB气态氦 / 8,000mB气态氖 / 6,000mB气态氩 / 4,000mB氪 / 2,000mB气态氙", "Which is 10,000mB Helium / 8,000mB Neon / 6,000mB Argon / 4,000mB Krypton / 2,000mB Xenon"));
        t.requirement(t.tr("2, 4, 6, 8, 10：超导去离子", "2, 4, 6, 8, 10: Superconductive deionization"));
        t.feature(t.tr("需要输入1,000mB对应IV，LuV，ZPM，UV，UHV的液态超导", "Needs input of 1,000mB of liquid superconductors corresponding to IV, LuV, ZPM, UV, UHV."));
        t.requirement(t.tr("11, 13, 15：引力生成差异真空提取", "11, 13, 15: Gravitational Differential Vacuum Extraction"));
        t.feature(t.tr("需要输入2,000mB液态安普洛", "Requires input of 2,000mB Liquid Amprosiums."));
        t.requirement(t.tr("12, 14：塞尔多尼安沉淀过程", "12, 14: Seldenian precipitation process"));
        t.feature(t.tr("不输入任何东西", "Do not input anything."));
        t.warning(t.tr("0：机器过载", "0: Machine overload"));
        t.feature(t.tr("在罕见情况下，机器可能会过载并且不会输出任何控制信号", "In rare situations, the machine may overload and not output any control signals."));
        t.attention(t.tr("为防止机器损坏，输入10,000mB液态氦", "To prevent machine damage, input 10,000mB Liquid Helium."));
        t.warning(t.tr("输入信号未请求的任何流体将始终导致配方失败", "Any liquid not requested by the input signal will always cause the recipe to fail."));
    });

    public static final TipSpec AbsoluteBaryonicPerfectionPurificationUnitTooltips = GTOTips.of("absolute_baryonic_perfection_purification_unit", t -> {
        t.feature(ComponentSlang.PurifyLevel(8));
        t.heading(ComponentSlang.RunningRequirements);
        t.control(t.tr("将§b夸克释放催化剂§r放入输入总线中运行", "Put §bQuark Releasing Catalyst§r into the input bus to operate."));
        t.attention(t.tr("每秒消耗输入槽中的所有催化剂", "Consumes all Catalysts in the input slot every second."));
        t.requirement(t.tr("每消耗一个夸克催化剂还需额外消耗144mB§b夸克胶子等离子体§r", "For each quark catalyst consumed, an additional 144mB of §bquark gluon plasma§r is required."));
        t.feature(t.tr("在配方结束时，所有错误插入的催化剂将返回输出槽", "At the end of the recipe, all incorrectly inserted Catalysts will return to the output slot."));
        t.heading(ComponentSlang.OutputProbability);
        t.feature(t.tr("每个配方循环中，不同的两种夸克释放催化剂的组合将正确识别出孤立的夸克并完成配方", "Each recipe cycle, different combinations of two Quark Releasing Catalysts will correctly identify the Isolated Quarks and complete the recipe."));
        t.feature(t.tr("如果最近插入的两种催化剂是正确的组合，则立即输出稳定重子物质", "If the last two inserted Catalysts are the correct combination, Stable Baryonic Matter will be output immediately."));
    });

    public static final TipSpec ThePrimordialReconstructorTooltips = GTOTips.of("the_primordial_reconstructor", t -> {
        t.heading(t.tr("通过电路选择工作模式", "Select the operating mode through the circuit"));
        t.feature(t.tr("电路 1：物品解构", "Circuit 1: Item Deconstruction"));
        t.requirement(t.tr("需要输入 书 铭刻之布 摘除符石", "Requires input Book  Affix Canvas  Sigil of Withdrawal"));
        t.feature(t.tr("电路 2：物品 + 附魔 解构", "Circuit 2: Item + Enchantments Deconstruction"));
        t.requirement(t.tr("需要输入 铭刻之布 摘除符石", "Requires input Affix Canvas  Sigil of Withdrawal"));
        t.feature(t.tr("电路 3：物品 + 刻印 解构", "Circuit 3: Item + Affixes Deconstruction"));
        t.requirement(t.tr("需要输入 书 摘除符石", "Requires input Book  Sigil of Withdrawal"));
        t.feature(t.tr("电路 4：物品 + 附魔 + 刻印 解构", "Circuit 4: Item + Enchantments + Affixes Deconstruction"));
        t.requirement(t.tr("需要输入 摘除符石", "Requires input Sigil of Withdrawal"));
        t.feature(t.tr("电路 5：附魔精粹合成附魔书", "Circuit 5: Essence crafting Enchanted Book"));
        t.requirement(t.tr("需要输入一本书，消耗附魔精粹和魔力合成", "Need to input a book, consume enchantment essence and magic to craft"));
        t.feature(t.tr("电路 6：附魔书合并", "Circuit 6: Enchantment Enchanted Book Merge"));
        t.feature(t.tr("消耗魔力合成，会输出额外的书", "Consume magic power to synthesize, and output additional books"));
        t.feature(t.tr("电路 7：刻印精粹合成铭刻之布", "Circuit 7: Affix Enchanted Book Merge"));
        t.requirement(t.tr("消耗魔力合成，需要一个铭刻之布", "Consume magic power to synthesize, and requires an Affix Canvas"));
        t.feature(t.tr("电路 8：宝石合并", "Circuit 8: Gem Merge"));
        t.feature(t.tr("使用同级的珍宝材料和宝石粉合并宝石", "Use the same level of rarity materials and gem dust to merge gems"));
        t.feature(t.tr("电路 9：宝石粉碎", "Circuit 9: Gem Crushing"));
        t.feature(t.tr("将宝石粉碎得到宝石粉", "Crushing gems to obtain gem dust"));
        t.feature(t.tr("电路 10：强行附魔", "Circuit 10: Forced enchantment"));
        t.feature(t.tr("消耗魔力强行将附魔书上的附魔添加到物品上", "Consume magic power to forcibly adds the enchantment from the enchanted book to the item"));
        t.feature(t.tr("电路 11：强行刻印", "Circuit 11: Forced add affixes"));
        t.feature(t.tr("消耗魔力强行将铭刻之布上的刻印添加到物品上", "Consume magic power to forcibly adds the affixes from the affix canvas to the item"));
        t.feature(t.tr("电路 12：强行修改物品稀有度", "Circuit 12: Forcefully modify item rarity"));
        t.feature(t.tr("消耗魔力，珍宝材料和新生符文强行改变稀有度", "Consume magic power and enter rarity material and sigil of rebirth to forcibly change the rarity"));
        t.feature(t.tr("电路 13：强行添加镶孔", "Circuit 13: Forced addition of sockets"));
        t.feature(t.tr("消耗魔力，镶孔符文强行添加镶孔", "Consume magic power and enter sigil of socketing to forcibly addition of sockets"));
        t.feature(t.tr("电路 14：强行镶嵌宝石", "Circuit 14: Forced gem inlay"));
        t.feature(t.tr("消耗魔力，强行将宝石镶嵌到物品上", "Consume magic power to forcibly inserting gems into items"));
    });

    public static final TipSpec ResonanceFlowerTooltips = GTOTips.of("resonance_flower", t -> {
        t.lore(t.tr("诞生于虚空的共鸣之花，携带着野性的魔力能量，经格雷科技的工业锚定后，成为魔力工业化的核心枢纽", "The Resonance Flower, born from the Void with wild mana energy, became the core hub of mana industrialization after being anchored by GregTech's industry."));
        t.heading(t.tr("魔力工业的核心转化枢纽", "Core Conversion Hub of Mana Industry"));
        t.lore(Tx.rainbowSlow(t.tr("虚空能量的工业驯服者", "Industrial Tamer of Void Energy")));
        t.heading(t.tr("累计配方次数解锁等级，提升转化效率与并行乘数", "Accumulate recipe runs to unlock tiers, boosting conversion efficiency and parallel multipliers"));
        t.warning(t.tr("波动效应失控会加剧消耗！！！", "Uncontrolled fluctuation effects will exacerbate consumption!!!"));
        t.heading(t.tr("需补充稳定资源抑制波动", "Requires stability resources to suppress fluctuations. "));
        t.lore(Tx.rainbowSlow(t.tr("泛银河魔力工业体系核心装备", "Core Equipment of the Pan-Galactic Mana Industrial System")));
        t.heading(t.tr("核心机制", "Core Mechanism"));
        t.feature(t.tr("配方运行次数累计解锁等级", "Accumulate recipe runs to unlock tiers"));
        t.feature(t.tr("等级越高，耗时减免越多，并行处理乘数越高（每台机器最多10条记录）", "Higher tiers grant more duration reduction and higher parallel multiplier (max 10 records per Machine)"));
        t.feature(t.tr("机器消耗系数随机跳变", "Random jump of machine consumption coefficient"));
        t.feature(t.tr("时间消耗波动系数[0.05 ~ 20] · 元素消耗波动系数[0.1 ~ 16]", "Time consumption fluctuation coefficient [0.05~20] · Element consumption fluctuation coefficient [0.1~16]"));
        t.control(t.tr("向主机存储放入物品延长稳定次数", "Add items to host storage to extend stable time"));
        t.control(t.tr("放入§b下界之星§r：每颗增加5次的稳定次数", "Put §bNether Star§r: +5 of the stable time per star"));
        t.control(t.tr("放入§b稳定核心§r：每个增加10000000次的稳定次数", "Put §bStabilizer Core§r: +10000000 for each stable time"));
        t.heading(t.tr("共鸣消耗", "Resonance Consumption"));
        t.feature(t.tr("配方带共鸣标签时，按频率消耗指定资源", "Recipes with resonance tags consume specified resources at set frequency"));
        t.warning(t.tr("消耗失败直接中断配方执行！", "Consumption failure interrupts recipe execution directly!"));
        t.heading(t.tr("等级加成", "Tier Bonuses"));
        t.bonus(t.tr("耗时减免 - 1-8级每级减2.5% | 9-64级每级额外减1.25% | ≥64级固定减90%", "Duration Reduction - Tier1-8: -2.5% per tier | Tier9-64: Extra -1.25% per tier | ≥Tier64: Fixed -90%"));
        t.bonus(t.tr("并行乘数 - 1-64级线性递增 | 65-255级乘数陡增 | ≥256级Long.MAX_VALUE", "Parallel Multiplier - Tier1-64: Linear increase | Tier65-255: Sharp multiplier increase | ≥Tier256: Long.MAX_VALUE"));
    });

    public static final TipSpec AlchemicalDeviceTooltips = GTOTips.of("alchemical_device", t -> {
        t.lore(t.tr("炼金是一个神秘的过程", "Alchemy is a mysterious process"));
        t.lore(t.tr("物质在连续不断的转换中获得升华", "Matter attains sublimation through continuous transformation"));
        t.feature(t.tr("部分配方产出概率随运行次数增长", "§7The probability of partial recipe output increases with the number of runs"));
    });

    public static final TipSpec AlchemyCauldronTooltips = GTOTips.of("alchemy_cauldron", t -> {
        t.attention(t.tr("不要用它来做饭", "Do not use it for cooking food"));
    });

    public static final TipSpec LargeAlchemicalDeviceTooltips = GTOTips.of("large_alchemical_device", t -> {
        t.heading(ComponentSlang.AfterModuleInstallation);
        t.supported(t.tr("激活完美嬗变模式", "Activate perfect transmutation mode"));
        t.bonus(t.tr("获得0.01x耗时减免", "Gain 0.01x Duration reduction"));
    });

    public static final TipSpec ElfExchangeMachine = GTOTips.of("elf_exchange_machine", t -> {
        t.heading(t.tr("下界模式", "Nether Mode"));
        t.feature(t.tr("在下界中工作，且设置电路时，交易对象不再是亚尔夫海姆世界的居民", "When working in the Nether, and setting the circuit meta, the trading partner is no longer a resident of Alfheim."));
        t.feature(t.tr("而是猪灵堡垒中的§6猪灵§r", "Instead, it is the §6Piglins§r in the Bastion Remnants."));
        t.feature(t.tr("每一笔交易将花费§e金锭§r作为交易货币", "Each trade will cost §eGold Ingots§r as the trading currency."));
        t.control(t.tr("设置§b2号电路§r时，将过滤交易物品，不允许§b装备/药水/附魔书§r", "When setting §bCircuit 2§r, it will filter traded items, disallowing §bEquipment/Potions/Enchanted Books§r."));
        t.attention(t.tr("此法过滤的物品仍然需要支付§e金锭§r", "Items filtered by this method still require payment in §eGold Ingots§r."));
    });

    public static final TipSpec FastNeutronBreederTooltips = GTOTips.of("fast_neutron_breeder", t -> {
        t.heading(t.tr("运行机制", "Operating Mechanism"));
        t.supported(t.tr("最高支持2048并行，无法通过其他方式加速", "Supports up to 2048 parallel, cannot be accelerated by other means"));
        t.requirement(t.tr("配方需满足最低中子通量", "Recipe requires minimum neutron flux"));
        t.feature(t.tr("中子通量可通过输入粒子源或配方运行时增加", "Neutron flux can be increased by inputting neutron sources or during recipe operation"));
        t.feature(t.tr("中子通量越高，堆升温速率越大", "The higher the neutron flux, the faster the pile heats up"));
        t.feature(t.tr("输入石墨粉可吸收中子通量", "Inputting Graphite Dust can absorb neutron flux"));
        t.feature(t.tr("输入冷却剂可降低温度", "Inputting coolant can lower the temperature"));
        t.warning(t.tr("温度超过2098K时配方失败，输出(一点可怜的)核废料，燃料组件方块全部熔毁", "If the temperature exceeds 2098K, the recipe fails, outputs waste, and all fuel component blocks melt."));
        t.heading(t.tr("配方相关", "Recipe Related"));
        t.requirement(t.tr("输入：增殖棒与对应元素粉，不同配方需不同中子通量", "Input: Breeding Rods and corresponding Element Dust, different recipes require different neutron flux"));
        t.feature(t.tr("输出：枯竭增殖棒", "Output: Depleted Breeding Rods"));
        t.formula(t.tr("实际并行数P越大，运行时间越短，公式：T = t * (0.9 - (当前中子通量 - 需要的中子通量) / 100MeV)^(P^0.5)，其中t为原始运行时间", "The larger the actual parallelism P, the shorter the running time, formula: T = t * (0.9 - (current neutron flux - required neutron flux) / 100MeV)^(P^0.5), where t is raw running time"));
        t.heading(t.tr("数值机制", "Numerical Mechanism"));
        t.feature(t.tr("消耗粒子源提供初始通量：锑-铍10keV，钚-铍100keV，锎-252 1MeV", "Consume neutron sources to provide initial flux: Sb-Be 10keV, Pu-Be 100keV, Cf-252 1MeV"));
        t.feature(t.tr("中子通量每秒减少10keV", "Neutron flux decreases by 10keV per second"));
        t.feature(t.tr("小撮/小堆/石墨粉分别降低0.1/0.25/1MeV", "Small Pile/Big Pile/Graphite Dust reduce by 0.1/0.25/1MeV respectively"));
        t.feature(t.tr("中子通量为E(keV)时，在主机内放入N个铱中子反射板后，中子通量每秒增加 (EN)^0.5 keV", "When neutron flux is E (keV), after placing N Iridium Neutron Reflectors in the mainframe, neutron flux increases by (EN)^0.5 keV per second"));
        t.statText(t.tr("初始温度298K，临界点2098K", "Initial temperature 298K, critical point 2098K"));
        t.formula(t.tr("当前温度为K时，每秒产热公式：H=K×1.27×(E/100)^1.88，结果向上取整", "Heat generation formula per second: H=K×1.27×(E/100)^1.88 where K is the current temperature. Result rounded up"));
        t.statText(t.tr("冷却液系数(K/mB/s)：蒸馏水1，液氮4，液氦80", "Coolant coefficients(K/mB/s): Distilled Water 1, Liquid Nitrogen 4, Liquid Helium 80"));
        t.feature(t.tr("冷却后分别输出蒸汽、气态氮、气态氦", "Outputs Steam, Gaseous Nitrogen, Gaseous Helium respectively after cooling"));
        t.attention(t.tr("每秒将消耗全部输入的中子调节剂，不会过量地消耗冷却剂", "Will consume all neutron moderators input per second. Won't consume excessive amounts of coolant(s)."));
        t.formula(t.tr("每秒温度变化：ΔT=H-C×M，C×M为消耗的冷却剂提供的冷却量，结果向下取整", "Temperature change per second: ΔT=H-C×M, where C×M is the cooling capacity provided by the consumed coolant(s). Result rounded down"));
    });

    public static final TipSpec AdvancedMembraneHatchTooltips = GTOTips.of("advanced_semipermeable_membrane_hatch", t -> {
        t.feature(t.tr("拥有9格膜槽，按配方所需膜类型自动选择，同类型膜优先使用槽位靠前且有剩余耐久的膜", "Stores 9 membranes and automatically selects a compatible membrane for each recipe; membranes of the same type are used in slot order while durability remains"));
        t.formula(t.tr("每次配方消耗膜耐久=⌈(配方并行数)^0.25⌉", "Membrane durability consumed per recipe = ceil((recipe parallels)^0.25)"));
        t.requirement(t.tr("每次配方只使用选中膜的属性与剩余耐久，并扣除对应槽位的耐久；反应期间所有膜槽均锁定", "Each recipe uses the selected membrane's properties and remaining durability and consumes durability from that slot; all membrane slots are locked during reactions"));
    });

    public static final TipSpec FuelCellGeneratorTooltips = GTOTips.of("fuel_cell_generator", t -> {
        t.feature(t.tr("通过燃料氧化半反应将能量储存在电解液中，或释放储能电解液中的能量", "Store energy in electrolyte through fuel oxidation half-reactions, or release energy from charged electrolyte"));
        t.heading(t.tr("模式一：储能", "Mode 1: Energy Storage"));
        t.requirement(t.tr("机器的多方块顶部区域，以主机为中心，可以将仓室放置区划分为两侧，分别用于输入不同的半反应", "The multi-block top area of the machine, centered on the mainframe, can be divided into two sides for inputting different half-reactions"));
        t.requirement(t.tr("两侧区域的输入原料分别独立寻找半反应配方", "The input materials in each side area independently search for half-reaction recipes"));
        t.requirement(t.tr("当两侧半反应均有配方、属于同一电解质体系、安装有半透膜、且它们产生的电荷符号相反时，会自动合成为一个总反应运行", "When both half-reactions have recipes, belong to the same electrolyte system, have a semipermeable membrane installed, and produce opposite charge signs, they will automatically combine into a total reaction"));
        t.formula(t.tr("总反应的功率=两侧半反应功率之和/2", "Total reaction power = sum of both half-reaction powers / 2"));
        t.formula(t.tr("储能基础耗时=1秒×膜的耗时乘数；", "Base storage duration = 1 second × membrane time multiplier;"));
        t.feature(t.tr("不同种类的半透膜为反应提供了不同的时间、耗电乘数", "Different types of semipermeable membranes provide different time and energy consumption multipliers for the reactions"));
        t.feature(t.tr("且提供的燃料效率乘数会与转化的热值相乘，影响总反应的能量储存", "And the provided fuel efficiency multiplier will multiply with the converted heat value, affecting the total energy storage of the reaction"));
        t.penalty(t.tr("每次总反应消耗膜耐久：普通仓为⌈√(总反应并行数)⌉，高级仓为⌈(总反应并行数)^0.25⌉", "Membrane durability consumed per total reaction: ceil(sqrt(total reaction parallels)) with a basic hatch, or ceil((total reaction parallels)^0.25) with an advanced hatch"));
        t.supported(t.tr("本机器模式提供无限并行数", "This machine mode provides unlimited parallelism"));
        t.requirement(t.tr("需要在中央顶部的半透膜仓内放入可透过配对离子的膜；反应期间膜被锁定", "Place a membrane permeable to the paired ions in a membrane hatch at the top center; membranes are locked during reactions"));
        t.heading(t.tr("模式二：放电", "Mode 2: Energy Release"));
        t.requirement(t.tr("放电需要半透膜仓内有膜，任意膜均可使配方运行", "Discharge requires a membrane in the membrane hatch; any membrane allows the recipe to run"));
        t.requirement(t.tr("此时的最大并行数由膜的放电并行度决定", "The maximum parallelism at this time is determined by the discharge parallelism of the membrane"));
        t.feature(t.tr("放电期间膜被锁定，固定消耗与配方并行数相同的耐久", "Membranes are locked during energy release: fixed durability consumption equal to the recipe parallelism"));
        t.requirement(t.tr("储能时连接能源输入仓，放电时连接能源输出仓；中央顶部用于物品、流体输出及半透膜仓", "Connect energy input hatches for storage and energy output hatches for release; the top center accepts item/fluid outputs and the membrane hatch"));
    });

    public static final TipSpec electroplatingBathTooltips = GTOTips.of("electroplating_bath", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("机器的多方块顶部区域，以主机为中心，可以将仓室放置区划分为两侧，分别用于输入不同的半反应", "The multi-block top area of the machine, centered on the mainframe, can be divided into two sides for inputting different half-reactions"));
        t.requirement(t.tr("两侧区域的输入原料分别独立寻找半反应配方", "The input materials in each side area independently search for half-reaction recipes"));
        t.requirement(t.tr("当两侧半反应均有配方、属于同一电解质体系、安装有半透膜、且它们产生的电荷符号相反时，会自动合成为一个总反应运行", "When both half-reactions have recipes, belong to the same electrolyte system, have a semipermeable membrane installed, and produce opposite charge signs, they will automatically combine into a total reaction"));
        t.formula(t.tr("总反应的功率=两侧半反应功率之和/2", "Total reaction power = sum of both half-reaction powers / 2"));
        t.formula(t.tr("总反应的时间=两侧半反应（时间×功率）之和/总反应功率", "Total reaction time = sum of both half-reaction (time × power) / total reaction power"));
        t.heading(t.tr("半透膜", "Semipermeable Membrane"));
        t.requirement(t.tr("阳离子交换膜：当反应产生的离子为除质子以外的阳离子时使用", "Cation Exchange Membrane: Use when the reaction produces cations other than protons"));
        t.requirement(t.tr("阴离子交换膜：当反应产生的离子为除氢氧根以外的阴离子时使用", "Anion Exchange Membrane: Use when the reaction produces anions other than hydroxide"));
        t.requirement(t.tr("质子交换膜：当反应产生的离子为质子时使用", "Proton Exchange Membrane: Use when the reaction produces protons"));
        t.control(t.tr("当反应有多种离子产生时，半透膜仅需能透过其中一种离子即可", "When the reaction produces multiple ions, the semipermeable membrane only needs to allow one of them to pass through"));
        t.feature(t.tr("不同种类的半透膜为反应提供了不同的时间、耗电乘数", "Different types of semipermeable membranes provide different time and energy consumption multipliers for the reactions"));
        t.penalty(t.tr("每次总反应消耗膜耐久：普通仓为⌈√(总反应并行数)⌉，高级仓为⌈(总反应并行数)^0.25⌉", "Membrane durability consumed per total reaction: ceil(sqrt(total reaction parallels)) with a basic hatch, or ceil((total reaction parallels)^0.25) with an advanced hatch"));
    });

    public static final TipSpec SpaceStationTooltips = GTOTips.of("space_station", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("提供一个保护机器正常工作，免受太空辐射等复杂环境影响的空间", "Provides a space that protects machines from complex environments such as space radiation"));
        t.feature(t.tr("空间站内部自带供玩家呼吸的空气", "The interior of the space station comes with air for players to breathe"));
        t.feature(t.tr("且会根据内部安装的过滤器方块种类提供超净环境", "And provides a super clean environment based on the types of filter blocks installed inside"));
        t.supported(t.tr("可连接§b光伏阵列§r以获取太阳能发电", "Can connect to §bPhotovoltaic Arrays§r to obtain solar power"));
        t.heading(t.tr("建造要求", "Construction Requirements"));
        t.requirement(t.tr("必须在太空中建造", "Must be constructed in space"));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("运行需要消耗1920EU/t的基础能量", "Operation requires a base energy consumption of 1920EU/t"));
        t.feature(t.tr("每10秒固定消耗：", "Fixed consumption every 10 seconds:"));
        t.attention(t.tr("15mB 蒸馏水", "15mB Distilled Water"));
        t.attention(t.tr("10mB 火箭燃料", "10mB Rocket Fuel"));
        t.attention(t.tr("100mB 空气", "100mB Air"));
        t.feature(t.tr("运行时每秒从空间站自身的输入仓扣除蒸馏水，经对接接口处的供水仓输送给已连接的§b光伏帆板控制器§r", "While running, Distilled Water is drawn from the station's own input hatches every second and sent through the supply hatches at the docking ports to connected §bPhotovoltaic Sail Controllers§r"));
        t.feature(t.tr("每仓每秒供水量默认为§b8mB§r，可在界面中调节（0~1000mB），设为0时停止供水", "The supply per hatch per second defaults to §b8mB§r and is adjustable in the GUI (0-1000mB); 0 stops the supply"));
        t.attention(t.tr("供水与配方共用同一批蒸馏水，库存不足时供水仓可能得不到供给", "The supply shares Distilled Water with the recipe; supply hatches may receive nothing when stock is insufficient"));
        t.feature(t.tr("运行时每10秒将排出30mB废水", "When operating, it will discharge 30mB of Waste Water every 10 seconds"));
    });

    public static final TipSpec LargeSpaceStationTooltips = GTOTips.of("large_space_station", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("提供一个保护机器正常工作，免受太空辐射等复杂环境影响的空间", "Provides a space that protects machines from complex environments such as space radiation"));
        t.feature(t.tr("空间站内部自带供玩家呼吸的空气", "The interior of the space station comes with air for players to breathe"));
        t.supported(t.tr("可以向外侧安装拓展舱体以增加内部空间/更多功能", "Can install expansion modules on the outside to increase internal space/more functions"));
        t.heading(t.tr("建造要求", "Construction Requirements"));
        t.requirement(t.tr("必须在太空中建造", "Must be constructed in space"));
        t.requirement(t.tr("必须安装§b核心舱§r，且只能安装一个", "Must install §bCore Module§r, and only one can be installed"));
        t.requirement(t.tr("必须在固定位置安装§b拓展舱体§r", "Must install §bExpansion Modules§r in fixed positions"));
        t.control(t.tr("安装的具体位置可在机器GUI内点击左下高亮按钮以在世界中高亮显示", "The specific installation position can be highlighted in the world by clicking the highlight button at the bottom left in the GUI"));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("运行需要消耗7680EU/t的基础能量", "Operation requires a base energy consumption of 7680EU/t"));
        t.penalty(t.tr("会根据安装的拓展舱体数量增加额外能耗以及资源消耗", "Will increase additional energy consumption and resource consumption based on the number of expansion modules installed"));
        t.feature(t.tr("每秒固定消耗：", "Fixed consumption every second:"));
        t.attention(t.tr("15mB * (1 + 拓展舱体数量) 蒸馏水", "15mB * (1 + number of expansion modules) Distilled Water"));
        t.attention(t.tr("10mB * (1 + 拓展舱体数量) 火箭燃料", "15mB * (1 + number of expansion modules) Rocket Fuel"));
        t.attention(t.tr("100mB * (1 + 拓展舱体数量) 空气", "100mB * (1 + number of expansion modules) Air"));
        t.feature(t.tr("能耗增加与拓展舱体的具体种类与数量有关", "Energy consumption increase is related to the specific types and quantities of expansion modules"));
        t.heading(t.tr("拓展舱体", "Expansion Modules"));
        t.feature(t.tr("§b核心舱§r：大型空间站的核心部分，必须安装且只能安装一个", "§bCore Module§r: The core part of the large space station, must be installed and only one can be installed"));
        t.feature(t.tr("§b衔接舱§r：用于连接核心舱/模块仓与其他拓展舱体", "§bConnection Module§r: Used to connect the core module with other expansion modules"));
        t.feature(t.tr("§b模块舱§r：提供额外的空间与功能", "§bFunctional Module§r: Provides additional space and functions"));
    });

    public static final TipSpec SpaceStationWorkspaceExtensionTooltips = GTOTips.of("space_station_workspace_extension", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("提供额外的空间以容纳更多机器", "Provides additional space to accommodate more machines"));
        t.supported(t.tr("且可安装在§b大型空间站§r的任意一侧", "And can be installed on any side of the §bLarge Space Station§r"));
        t.attention(Tx.scrollExotic(t.tr("该模块为变长模块", "This module is a variable-length module")));
        t.control(t.tr("在搭建模块前请在机器GUI内点击左下调整器调整模块的长度", "Before building the module, please click the adjuster in the bottom left of the machine GUI to adjust the length of the module"));
    });

    public static final TipSpec SpaceStationEnvironmentalMaintenanceModuleTooltips = GTOTips.of("space_station_environmental_maintenance_module", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("给当前空间站里的所有机器提供超净环境", "Provides a super clean environment for all machines in the current space station"));
        t.feature(t.tr("提供的环境等级取决于安装的过滤器方块种类", "The level of environment provided depends on the types of filter blocks installed"));
        t.feature(Tx.join(
                t.tr("且不同于常规超净间，高级超净环境", "And unlike conventional super clean rooms, advanced super clean environments"),
                Tx.scrollOptical(t.tr("向下兼容低级超净环境", " are backward compatible with lower-level super clean environments"))));
        t.feature(t.tr("提供无人机仓，可供无人机清理空间站内机器的垃圾，或自动维护机器", "Provides a drone bay for drones to clean up machine waste or automatically maintain machines in the space station"));
    });

    public static final TipSpec NoExtensionAvailableTooltips = GTOTips.of("no_extension_available", t -> {
        t.unsupported(t.tr("该种类空间站舱段无法向外侧安装拓展舱体", "This type of space station module cannot install expansion modules on the outside"));
    });

    public static final TipSpec RecipeExtensionTooltips = GTOTips.of("recipe_extension", t -> {
        t.heading(t.tr("配方拓展舱室", "Recipe Extension Module"));
        t.feature(t.tr("§6被动耗能§r由§b核心舱§r提供，而§6配方耗能§r需由安装在§b此舱§r的§d能源仓§r提供", "§6Passive energy consumption§r is provided by the §bCore Module§r, while §6recipe energy consumption§r needs to be provided by the §dEnergy Input Hatch§r installed in the §bthis module§r"));
        t.requirement(t.tr("无核心舱连接时，无法运行配方", "Cannot run recipes without a linked core module"));
        t.requirement(t.tr("如果解锁了[激光太空工程]科技节点", "If the [Laser Space Engineering] Node is unlocked"));
        t.bonus(t.tr("则解锁§d超高安仓§r/§d超频仓§r/§d线程仓§r等高级舱体的使用权限", "The use of advanced modules such as §dHigh-Amp Hatches§r/§dOverclocking Hatches§r/§dThread Hatches§r will be unlocked"));
    });

    public static final TipSpec OrbitalSmeltingFacilityTooltips = GTOTips.of("orbital_smelting_facility", t -> {
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("仅接收带有无重力条件的电力高炉和合金冶炼炉配方，普通配方无法运行", "Only accepts electric blast furnace and alloy blast furnace recipes with a zero-gravity condition; normal recipes cannot run here"));
    });

    public static final TipSpec CoreSpaceStationModuleTooltips = GTOTips.of("core_space_station_module", t -> {
        t.heading(t.tr("工业空间站太空舱种类", "Types of Industrial Space Station Modules"));
        t.feature(ComponentSlang.IsWhatTypeSpaceModule(ComponentSlang.CoreModuleSpaceModule));
        t.supported(ComponentSlang.CanConnectToWhatTypeSpaceModule(List.of(ComponentSlang.ConjunctionModuleSpaceModule)));
    });

    public static final TipSpec ConjunctionSpaceStationModuleTooltips = GTOTips.of("conjunction_space_station_module", t -> {
        t.heading(t.tr("工业空间站太空舱种类", "Types of Industrial Space Station Modules"));
        t.feature(ComponentSlang.IsWhatTypeSpaceModule(ComponentSlang.ConjunctionModuleSpaceModule));
        t.supported(ComponentSlang.CanConnectToWhatTypeSpaceModule(List.of(ComponentSlang.ConjunctionModuleSpaceModule, ComponentSlang.FunctionModuleSpaceModule)));
    });

    public static final TipSpec FunctionSpaceStationModuleTooltips = GTOTips.of("function_space_station_module", t -> {
        t.heading(t.tr("工业空间站太空舱种类", "Types of Industrial Space Station Modules"));
        t.feature(ComponentSlang.IsWhatTypeSpaceModule(ComponentSlang.FunctionModuleSpaceModule));
        t.supported(ComponentSlang.CanConnectToWhatTypeSpaceModule(List.of(ComponentSlang.ConjunctionModuleSpaceModule)));
    });

    public static final TipSpec SpaceDroneDockTooltips = GTOTips.of("space_drone_dock", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("向太空发送无人机以收集宇宙尘埃", "Send drones into space to collect cosmic dust"));
        t.feature(t.tr("在不同的星系能够收集到不同种类的宇宙尘埃", "Different types of cosmic dust can be collected in different galaxies"));
        t.heading(ComponentSlang.RunningRequirements);
        t.requirement(t.tr("每次运行时，需要配方提供的无人机/电池有能量时才能运行", "Each operation requires the drones/batteries provided by the recipe to have energy to operate"));
        t.attention(t.tr("每次消耗其内部存储的全部电量", "Consumes all the internal stored energy each time"));
        t.bonus(t.tr("每消耗600,000EU，配方最大并行数+1", "For every 600,000EU consumed, the maximum parallelism of the recipe +1"));
        t.requirement(t.tr("无人机/电池缺电时，配方无法运行", "The recipe cannot run when the drone has no power"));
    });

    public static final TipSpec SpaceStationEnergyConversionModuleTooltips = GTOTips.of("space_station_energy_conversion_module", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("安装后，为空间站内的其他拓展舱体的超频仓提供1点增幅", "After installation, provides 1 point of amplification for the Overclocking Chamber in other expansion modules within the space station"));
        t.feature(t.tr("并为已安装太空电梯连接仓提供额外增幅", "And provides additional amplification for the installed Space Elevator Connection Chamber"));
        t.requirement(t.tr("多个高能转换调配舱仅生效一个", "Only one of multiple High-Energy Conversion Chambers will take effect"));
    });

    public static final TipSpec meRequestableInputBusTooltips = GTOTips.of("me_requestable_input_bus", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("将物品从ME网络中提取出来，放入机器中进行加工", "Extract items from the ME network and insert them into machines for processing"));
        t.feature(t.tr("提取不出来则会自己下单请求", "If items cannot be extracted, it will place its own order request"));
    });

    public static final TipSpec meRequestableInputHatchTooltips = GTOTips.of("me_requestable_input_hatch", t -> {
        t.heading(ComponentSlang.MainFunction);
        t.feature(t.tr("将流体从ME网络中提取出来，放入机器中进行加工", "Extract fluids from the ME network and insert them into machines for processing"));
        t.feature(t.tr("提取不出来则会自己下单请求", "If items cannot be extracted, it will place its own order request"));
    });
}
