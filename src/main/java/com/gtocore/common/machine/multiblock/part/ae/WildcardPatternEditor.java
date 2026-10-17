package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.gui.widget.WildcardAEKeyPhantomSlot;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.Binding;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.PageView;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Stepper;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;

import dev.vfyjxf.taffy.style.AlignItems;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** 当前安装样板的实时编辑面板；输入/输出沿用配方编辑器的两侧槽位布局。 */
@DataGeneratorScanned
final class WildcardPatternEditor extends UIElement {

    @RegisterLanguage(cn = "通配符样板编辑", en = "Wildcard Pattern Editor")
    static final String TITLE = "gtocore.ae.wildcard_editor.title";
    @RegisterLanguage(cn = "拖入物品、流体或通配符；点击槽位在下方修改数量，中键清空。每次修改立即刷新扫描。", en = "Drop items, fluids or wildcards; select a slot to edit its amount below, or middle-click to clear. Every change refreshes the scan.")
    static final String HINT = "gtocore.ae.wildcard_editor.hint";
    @RegisterLanguage(cn = "请先在主界面放入处理样板", en = "Insert a processing pattern in the main panel first")
    private static final String INSERT_PATTERN = "gtocore.ae.wildcard_editor.insert_pattern";
    @RegisterLanguage(cn = "至少需要一项输入和输出；扫描已暂停", en = "At least one input and output required; scanning paused")
    private static final String INCOMPLETE = "gtocore.ae.wildcard_editor.incomplete";
    @RegisterLanguage(cn = "修改已写入样板并刷新扫描", en = "Changes saved to the pattern; scan refreshed")
    private static final String READY = "gtocore.ae.wildcard_editor.ready";
    @RegisterLanguage(cn = "输入", en = "Inputs")
    private static final String INPUT = "gtocore.ae.wildcard_editor.input";
    @RegisterLanguage(cn = "输出", en = "Outputs")
    private static final String OUTPUT = "gtocore.ae.wildcard_editor.output";
    @RegisterLanguage(cn = "%s #%s：%s", en = "%s #%s: %s")
    private static final String SELECTED = "gtocore.ae.wildcard_editor.selected";
    @RegisterLanguage(cn = "空槽", en = "Empty slot")
    private static final String EMPTY = "gtocore.ae.wildcard_editor.empty";
    @RegisterLanguage(cn = "选择一个有内容的槽位后修改数量", en = "Select a non-empty slot to edit its amount")
    private static final String SELECT_SLOT = "gtocore.ae.wildcard_editor.select_slot";
    @RegisterLanguage(cn = "清空选中槽位", en = "Clear Selected Slot")
    private static final String CLEAR = "gtocore.ae.wildcard_editor.clear";

    private static final Component[] STATES = { Component.translatable(INSERT_PATTERN), Component.translatable(INCOMPLETE), Component.translatable(READY) };
    private static final Component INSTRUCTIONS = Component.translatable(HINT);
    private static final int SIDE_COLUMNS = 3;
    private static final int PAGE_SLOTS = SIDE_COLUMNS * SIDE_COLUMNS;
    private static final int SIDE_WIDTH = SIDE_COLUMNS * UISizes.SLOT_SIZE;

    private final MEWildcardPatternBufferPartMachine machine;
    private final Binding<Integer> selection;
    private int selectedSlot;
    private int nameSlot = -1;
    @Nullable
    private GenericStack nameStack;
    private Component selectedName = Component.empty();

    private WildcardPatternEditor(MEWildcardPatternBufferPartMachine machine) {
        this.machine = machine;
        layout(l -> l.column().gapAll(UISizes.GAP));
        selection = addBinding(Binding.bindInt(() -> selectedSlot, index -> selectedSlot = index, 0,
                AEProcessingPattern.MAX_INPUT_SLOTS + AEProcessingPattern.MAX_OUTPUT_SLOTS - 1));
        var inputs = side(false);
        var outputs = side(true);
        var arrow = TextLine.constant(24, Component.literal("→")).setTextAlign(Horizontal.CENTER);
        addChild(UIElement.centeredRow(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP).alignSelf(AlignItems.CENTER))
                .addChildren(inputs, arrow, outputs));
        addChild(TextLine.of(LayoutStyle.AUTO, this::selectedName));
        var amount = NumberField.ofLong(LayoutStyle.AUTO, () -> {
            var stack = selectedStack();
            return stack == null ? 1 : stack.amount();
        }, value -> {
            var stack = selectedStack();
            if (stack != null) machine.setEditorStack(selectedOutput(), selectedIndex(), new GenericStack(stack.what(), value));
        }, () -> 1, () -> {
            var stack = selectedStack();
            return machine.getEditorAmountLimit(selectedOutput(), selectedIndex(), stack == null ? null : stack.what());
        });
        amount.disabled(() -> selectedStack() == null, SELECT_SLOT);
        addChild(amount);
        addChild(Button.translatable(LayoutStyle.AUTO, CLEAR)
                .setOnServerClick(() -> machine.setEditorStack(selectedOutput(), selectedIndex(), null))
                .disabled(() -> selectedStack() == null, SELECT_SLOT));
        disabled(() -> !machine.hasEditablePattern(), INSERT_PATTERN);
    }

    static void build(MEWildcardPatternBufferPartMachine machine, UIElement column) {
        var status = new StatusPanel();
        status.addSentence(() -> STATES[machine.getEditorState()])
                .bindLevel(() -> machine.getEditorState() == 2 ? Level.GOOD : Level.WARNING)
                .bindDetail(() -> INSTRUCTIONS);
        column.addChildren(status, new WildcardPatternEditor(machine));
    }

    private UIElement side(boolean output) {
        int slots = output ? AEProcessingPattern.MAX_OUTPUT_SLOTS : AEProcessingPattern.MAX_INPUT_SLOTS;
        int pagesCount = (slots + PAGE_SLOTS - 1) / PAGE_SLOTS;
        var pages = new PageView(SIDE_WIDTH, SIDE_WIDTH);
        var labels = new String[pagesCount];
        for (int page = 0; page < pagesCount; page++) {
            int start = page * PAGE_SLOTS;
            labels[page] = String.valueOf(page + 1) + '/' + pagesCount;
            pages.addPage(content -> content.addChild(SlotGrid.of(SIDE_COLUMNS, Math.min(PAGE_SLOTS, slots - start),
                    slot -> new GhostSlot(this, output, start + slot))));
        }
        var side = UIElement.column(SIDE_WIDTH).layout(l -> l.gapAll(UISizes.GAP).alignCenter());
        side.addChildren(TextLine.translatable(SIDE_WIDTH, output ? OUTPUT : INPUT).setTextAlign(Horizontal.CENTER), pages,
                Stepper.of(18, pages::getPage, pages::selectPage, 0, pagesCount - 1).setFormatter(page -> labels[page]));
        return side;
    }

    private boolean selectedOutput() {
        return selectedSlot >= AEProcessingPattern.MAX_INPUT_SLOTS;
    }

    private int selectedIndex() {
        return selectedOutput() ? selectedSlot - AEProcessingPattern.MAX_INPUT_SLOTS : selectedSlot;
    }

    @Nullable
    private GenericStack selectedStack() {
        return machine.getEditorStack(selectedOutput(), selectedIndex());
    }

    private Component selectedName() {
        var stack = selectedStack();
        if (nameSlot != selectedSlot || !Objects.equals(nameStack, stack)) {
            nameSlot = selectedSlot;
            nameStack = stack;
            selectedName = Component.translatable(SELECTED, Component.translatable(selectedOutput() ? OUTPUT : INPUT), selectedIndex() + 1,
                    stack == null ? Component.translatable(EMPTY) : stack.what().getDisplayName());
        }
        return selectedName;
    }

    private static final class GhostSlot extends WildcardAEKeyPhantomSlot {

        private final WildcardPatternEditor editor;
        private final int slot;

        GhostSlot(WildcardPatternEditor editor, boolean output, int index) {
            super(() -> editor.machine.getEditorStack(output, index), stack -> {
                editor.selectedSlot = (output ? AEProcessingPattern.MAX_INPUT_SLOTS : 0) + index;
                editor.machine.setEditorStack(output, index, stack);
            });
            this.editor = editor;
            slot = (output ? AEProcessingPattern.MAX_INPUT_SLOTS : 0) + index;
            setSelected(() -> editor.selection.getValue() == slot);
            setHoverTooltips(INSTRUCTIONS);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!isMouseOverElement(mouseX, mouseY) || isDisabled() || button < 0 || button > 2) return false;
            editor.selection.set(slot);
            // 空手左键只选中，数量在下方编辑；其余操作沿用 GTM 幽灵槽。
            if (button == 0 && gui != null && gui.getModularUIContainer().getCarried().isEmpty()) return true;
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
