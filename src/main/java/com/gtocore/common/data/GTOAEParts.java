package com.gtocore.common.data;

import com.gtocore.api.lang.GTOTips;
import com.gtocore.common.data.translation.GTOMachineTooltips;
import com.gtocore.integration.ae.ExchangeStorageMonitorPart;
import com.gtocore.integration.ae.PatternContentAccessTerminalPart;
import com.gtocore.integration.ae.SimpleCraftingTerminal;

import com.gtolib.GTOCore;
import com.gtolib.api.ae2.me2in1.Me2in1TerminalPart;
import com.gtolib.utils.register.ItemRegisterUtils;

import com.gregtechceu.gtceu.api.tooltip.TipSpec;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;

import com.gto.registrate.util.entry.ItemEntry;
import com.gto.registrate.util.nullness.NonNullBiConsumer;

import java.util.function.Function;
import java.util.function.Supplier;

public final class GTOAEParts {

    private static final TipSpec EXCHANGE_STORAGE_MONITOR_TIPS = GTOTips.of(t -> {
        t.feature(GTOTips.global("此物品可以监控物品的交换速率", "This item can monitor the exchange rate of items"));
        t.rightClick(GTOTips.global("锁定状态下右击可切换监控间隔", "In locked state, right click to switch monitoring interval"));
    });

    private static final TipSpec SIMPLE_CRAFTING_TERMINAL_TIPS = GTOTips.of(t -> t.feature(GTOTips.global("将终端贴在箱子、存储器的某一面，打开合成面板就可以使用箱子内的物品进行合成。", "Attach the Terminal to any side of a chest or storage device, then open the crafting interface to use items from the container for crafting.")));

    private static final TipSpec ME_2IN1_TERMINAL_TIPS = GTOTips.of(t -> {
        t.feature(GTOTips.global("整合了ME样板编码终端与样板管理终端的功能，", "Integrates the functions of ME Pattern Encoding Terminal and Pattern Access Terminal, "));
        t.feature(GTOTips.global("支持编码、管理样板，并支持批量编码、自动填充配方等功能。", "supports encoding and managing patterns, batch encoding, auto-filling recipes, and more."));
        t.control(GTOTips.global("拖曳任意子面板的右下角以调整其大小。", "Drag the bottom-right corner of any subpanel to adjust its size."));
    });

    public static final Supplier<ItemDefinition<PartItem<ExchangeStorageMonitorPart>>> EXCHANGE_STORAGE_MONITOR = createPart(
            "exchange_storage_monitor", "Exchange Storage Monitor", "交换率存储监控器",
            ExchangeStorageMonitorPart.class, ExchangeStorageMonitorPart::new, EXCHANGE_STORAGE_MONITOR_TIPS);

    public static final Supplier<ItemDefinition<PartItem<SimpleCraftingTerminal>>> SIMPLE_CRAFTING_TERMINAL = createPart(
            "simple_crafting_terminal", "Simple Crafting Terminal", "简易合成终端",
            SimpleCraftingTerminal.class, SimpleCraftingTerminal::new, SIMPLE_CRAFTING_TERMINAL_TIPS);

    public static final Supplier<ItemDefinition<PartItem<Me2in1TerminalPart>>> ME_2IN1_TERMINAL = createPart(
            "me_2in1_terminal", "ME 2-in-1 Terminal", "ME 2合1终端",
            Me2in1TerminalPart.class, Me2in1TerminalPart::new, ME_2IN1_TERMINAL_TIPS);

    public static final Supplier<ItemDefinition<PartItem<PatternContentAccessTerminalPart>>> Pattern_Content_Access_Terminal = createPart(
            "pattern_content_access_terminal", "Pattern Content Access Terminal", "样板内容管理终端",
            PatternContentAccessTerminalPart.class, PatternContentAccessTerminalPart::new, GTOMachineTooltips.PatternContentAccessTerminalTooltips);

    private GTOAEParts() {}

    public static void init() {}

    private static <T extends IPart> Supplier<ItemDefinition<PartItem<T>>> createPart(String id, String en, String cn, Class<T> partClass, Function<IPartItem<T>, T> factory, TipSpec tooltips) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        ItemEntry<PartItem<T>> item = ItemRegisterUtils.item(id, cn, p -> new PartItem<>(p, partClass, factory))
                .tips(tooltips)
                .lang(en)
                .model(NonNullBiConsumer.noop())
                .register();
        return () -> new ItemDefinition<>(en, GTOCore.id(id), item.get());
    }
}
