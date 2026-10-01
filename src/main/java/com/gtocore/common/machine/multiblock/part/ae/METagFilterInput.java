package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.common.machine.multiblock.part.ae.slots.ExportOnlyAESlot;
import com.gtocore.integration.ae.hooks.ITagPriorityListExtension;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.NumberField;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

import com.glodblock.github.extendedae.common.me.taglist.TagPriorityList;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/// 标签过滤 ME 输入总线/仓共用：按标签从网络里挑储量最多的种类自动填配置格，已选中的在网络和本部件都耗尽前不换。
@DataGeneratorScanned
final class METagFilterInput {

    @RegisterLanguage(cn = "每种保持数量", en = "Amount per Type")
    private static final String KEEP_AMOUNT = "gtocore.machine.me_tag_filter_input.keep_amount";
    @RegisterLanguage(cn = "每种匹配的物品在总线内保持的数量", en = "Amount of each matching item kept in the bus")
    private static final String KEEP_AMOUNT_ITEM = "gtocore.machine.me_tag_filter_input.keep_amount.item";
    @RegisterLanguage(cn = "每种匹配的流体在仓内保持的数量（mB）", en = "Amount of each matching fluid kept in the hatch (mB)")
    private static final String KEEP_AMOUNT_FLUID = "gtocore.machine.me_tag_filter_input.keep_amount.fluid";

    private METagFilterInput() {}

    static void refreshConfigs(ExportOnlyAESlot[] slots, Class<? extends AEKey> keyType, TagPriorityList filter, long amount, IStorageService storage, IActionSource source) {
        if (!((ITagPriorityListExtension) filter).gtocore$isReady()) return;
        boolean empty = filter.isEmpty();
        var network = storage.getCachedInventory();
        int free = 0;
        for (var slot : slots) {
            var config = slot.getConfig();
            var stock = slot.getStock();
            if (config == null) {
                if (stock == null) free++;
            } else if (empty || !filter.isListed(config.what())) {
                if (stock == null || storage.getInventory().insert(stock.what(), stock.amount(), Actionable.SIMULATE, source) > 0) {
                    slot.setConfig(null);
                    if (stock == null) free++;
                }
            } else if (stock == null && network.get(config.what()) <= 0) {
                slot.setConfig(null);
                free++;
            } else if (config.amount() != amount) {
                slot.setConfig(new GenericStack(config.what(), amount));
            }
        }
        if (free == 0 || empty) return;
        var queue = new PriorityQueue<GenericStack>(free, Comparator.comparingLong(GenericStack::amount));
        for (var entry : network) {
            long stored = entry.getLongValue();
            if (stored <= 0) continue;
            var what = entry.getKey();
            if (!keyType.isInstance(what)) continue;
            boolean full = queue.size() >= free;
            if (full && queue.peek().amount() >= stored) continue;
            if (!filter.isListed(what) || isConfigured(slots, what)) continue;
            if (full) queue.poll();
            queue.offer(new GenericStack(what, stored));
        }
        var picked = new AEKey[queue.size()];
        for (int i = picked.length - 1; i >= 0; i--) {
            picked[i] = queue.poll().what();
        }
        int next = 0;
        for (var slot : slots) {
            if (next == picked.length) break;
            if (slot.getConfig() == null && slot.getStock() == null) slot.setConfig(new GenericStack(picked[next++], amount));
        }
    }

    private static boolean isConfigured(ExportOnlyAESlot[] slots, AEKey what) {
        for (var slot : slots) {
            var config = slot.getConfig();
            if (config != null && config.what().equals(what)) return true;
        }
        return false;
    }

    static UIElement keepAmountSection(LongSupplier getter, LongConsumer setter, long max, boolean fluid) {
        var field = NumberField.ofLong(LayoutStyle.AUTO, getter, setter, 1, max);
        return UIElement.section(LayoutStyle.AUTO).addChild(Form.inlineNumberRow(KEEP_AMOUNT, field, fluid ? KEEP_AMOUNT_FLUID : KEEP_AMOUNT_ITEM));
    }
}
