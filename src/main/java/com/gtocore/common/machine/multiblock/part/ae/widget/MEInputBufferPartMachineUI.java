package com.gtocore.common.machine.multiblock.part.ae.widget;

import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.elements.Switch;
import com.gtocore.api.gui.ui.elements.TextField;
import com.gtocore.api.gui.ui.styletemplate.UITheme;
import com.gtocore.common.machine.multiblock.part.ae.MEInputBufferPartMachine;
import com.gtocore.common.machine.multiblock.part.ae.MEPatternPartUI;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;

import net.minecraft.network.chat.Component;

import static com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine.*;

/**
 * ME 输入总成的单槽配置：物品、流体、电路三个输入区块，外加补货条件（低存量触发、发信合成、请求合成）、
 * 被动输入乘数，以及样板的物品/流体配置。
 */
public final class MEInputBufferPartMachineUI {

    private MEInputBufferPartMachineUI() {}

    public static void buildSlotConfig(UIElement column, MEInputBufferPartMachine machine, int index) {
        var slot = machine.getInternalInventory()[index];

        var itemHandlers = slot.itemUiHandlers;
        var items = MEPatternPartUI.section(column, ITEM_SPECIAL);
        MEPatternPartUI.slotRows(items, itemHandlers.length, i -> {
            var slotWidget = new SlotWidget(itemHandlers[i], i, 0, 0, true, true);
            slotWidget.setBackgroundTexture(UITheme.ITEM_SLOT);
            return slotWidget;
        });

        MEPatternPartUI.fluidSlots(MEPatternPartUI.section(column, FLUID_SPECIAL), slot.getFluidUiHandlers(), null);

        var circuitStorage = slot.circuitInventory.storage;
        MEPatternPartUI.section(column, CIRCUIT_SPECIAL).addChild(MEPatternPartUI.circuitRow(
                MEPatternPartUI.readOnlyCircuitSlot(circuitStorage),
                () -> MEPatternPartUI.circuitOf(circuitStorage.getStackInSlot(0)),
                circuit -> slot.setCircuitConfiguration(MEPatternPartUI.circuitStack(circuit))));

        // 补货条件：低存量阈值只在开关打开时生效，关闭时记为 -1
        var restock = UIElement.section(column.getContentWidth());
        column.addChild(restock);
        int width = restock.getContentWidth();
        var threshold = MEPatternPartUI.longField(0, () -> Math.max(slot.minThreshold, 0), value -> {
            if (slot.minThreshold >= 0) slot.setMinThreshold(value);
        }, 0);
        threshold.layout(l -> l.flexGrow(1));
        threshold.setHoverTooltips(Component.translatable(LOW_STOCK_TRIGGERING_THRESHOLD));
        restock.addChildren(
                MEPatternPartUI.labeledRow(width, LOW_STOCK_TRIGGERING_MODE, LOW_STOCK_TRIGGERING_MODE_TOOLTIP,
                        Switch.of(() -> slot.minThreshold >= 0, enabled -> slot.setMinThreshold(enabled ? 0 : -1))),
                UIElement.row(TextField.HEIGHT).layout(l -> l.width(width)).addChild(threshold),
                MEPatternPartUI.labeledRow(width, EMITTING_CRAFTING_MODE, EMITTING_CRAFTING_MODE_TOOLTIP,
                        Switch.of(slot::isEmitterMode, slot::setEmitterMode)),
                MEPatternPartUI.labeledRow(width, REQUEST_CRAFTING_WHEN_INSUFFICIENT, null,
                        Switch.of(() -> slot.useRequest, slot::setUseRequest)));

        var multiplier = MEPatternPartUI.section(column, PASSIVE_INPUT_MULTIPLIER);
        var multiplierField = MEPatternPartUI.longField(multiplier.getContentWidth(), () -> Math.max(slot.multiplier, 1), slot::setMultiplier, 1);
        multiplierField.setHoverTooltips(Component.translatable(PASSIVE_INPUT_MULTIPLIER_TOOLTIP));
        multiplier.addChild(multiplierField);

        var config = MEPatternPartUI.section(column, PATTERN_CONFIGURATION);
        var itemConfig = new AEItemConfigWidget(0, 0, slot.exportOnlyItemList);
        itemConfig.setShowAmount(true);
        var fluidConfig = new AEFluidConfigWidget(0, 0, slot.exportOnlyFluidList);
        fluidConfig.setShowAmount(true);
        config.addChildren(itemConfig, fluidConfig);
    }
}
