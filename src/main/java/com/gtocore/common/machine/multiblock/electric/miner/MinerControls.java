package com.gtocore.common.machine.multiblock.electric.miner;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.machine.feature.IDigitalMiner;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.Switch;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * 两种数字型采矿机操作台共用的紧凑行：过滤器与精准同行、两栏数值格、开关与按钮同行。
 */
@DataGeneratorScanned
final class MinerControls {

    @RegisterLanguage(cn = "过滤器", en = "Filter")
    static final String FILTER = "gtocore.digital_miner.filter";
    @RegisterLanguage(cn = "流体", en = "Fluids")
    static final String FLUID = "gtocore.digital_miner.fluid";
    @RegisterLanguage(cn = "采集半径", en = "Mining radius")
    static final String RADIUS = "gtocore.digital_miner.radius";

    private static final int SLOT_ROW = UISizes.SLOT_SIZE + 2;
    private static final int CELL_WIDTH = (UISizes.CONTENT_WIDTH - 14 - UISizes.GAP) / 2;

    private MinerControls() {}

    static UIElement filterAndSilk(MetaMachine machine, StackInventory filter, BooleanSupplier silk, BooleanConsumer setSilk) {
        var left = UIElement.row(SLOT_ROW).layout(l -> l.gapAll(UISizes.GAP).alignCenter().flex(1))
                .addChildren(ItemSlot.of(filter, 0), Form.label(FILTER));
        var right = UIElement.centeredRow(SLOT_ROW).layout(l -> l.flex(1))
                .addChildren(Form.label(IDigitalMiner.SILK, IDigitalMiner.SILK_TOOLTIP), Switch.of(silk, value -> {
                    setSilk.accept(value);
                    machine.onChanged();
                }));
        return UIElement.row(SLOT_ROW).layout(l -> l.gapAll(UISizes.SECTION_GAP)).addChildren(left, right);
    }

    static UIElement pair(UIElement left, UIElement right) {
        return UIElement.row(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(left, right);
    }

    static UIElement cell(MetaMachine machine, String labelKey, IntSupplier getter, IntConsumer setter, IntSupplier min, IntSupplier max) {
        var field = NumberField.ofInt(CELL_WIDTH, getter, value -> {
            if (value == getter.getAsInt()) return;
            setter.accept(value);
            machine.onChanged();
        }, min, max).setSteps(1, 8);
        return UIElement.column(CELL_WIDTH).layout(l -> l.gapAll(1)).addChildren(Form.fieldLabel(labelKey), field);
    }

    static Button resetButton(MetaMachine machine, Runnable reset) {
        var button = Button.translatable(LayoutStyle.AUTO, IDigitalMiner.RESET).setOnServerClick(() -> {
            reset.run();
            machine.onChanged();
        });
        button.tooltips(IDigitalMiner.RESET_TOOLTIP);
        button.layout(l -> l.flex(1));
        return button;
    }

    static UIElement switchAndButton(MetaMachine machine, String labelKey, String tooltipKey, BooleanSupplier getter, BooleanConsumer setter, Button button) {
        var toggle = UIElement.centeredRow(UISizes.CONTROL_HEIGHT).layout(l -> l.flex(1))
                .addChildren(Form.label(labelKey, tooltipKey), Switch.of(getter, value -> {
                    setter.accept(value);
                    machine.onChanged();
                }));
        return UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.SECTION_GAP)).addChildren(toggle, button);
    }
}
