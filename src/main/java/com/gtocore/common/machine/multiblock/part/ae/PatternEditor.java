package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.api.gui.widget.WildcardAEKeyPhantomSlot;
import com.gtocore.common.data.GTOCodecs;

import com.gtolib.api.ae2.stacks.TagPrefixKey;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.utils.AEChemicalHelper;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.uipro.Horizontal;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.Binding;
import com.gregtechceu.gtceu.uipro.data.RPC;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.data.UICodecs;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.PageView;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Stepper;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;

import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.datasynclib.util.ByteBufCodecExtends;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.vfyjxf.taffy.style.AlignItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/** 样板槽的实时编辑面板；输入/输出沿用配方编辑器的两侧槽位布局，空槽编辑时从网络取入空白样板。 */
@DataGeneratorScanned
public final class PatternEditor extends UIElement {

    @RegisterLanguage(cn = "样板编辑器", en = "Pattern Editor")
    static final String TITLE = "gtocore.ae.pattern_editor.title";
    @RegisterLanguage(cn = "拖入物品、流体或通配符；点击槽位在下方修改数量，中键清空。每次修改立即生效，主产物不能与其他样板槽重复。", en = "Drop items, fluids or wildcards; select a slot to edit its amount below, or middle-click to clear. Changes take effect immediately; primary outputs must be unique across pattern slots.")
    static final String HINT = "gtocore.ae.pattern_editor.hint";
    @RegisterLanguage(cn = "请放入空白样板或处理样板", en = "Insert a blank or processing pattern, or ensure the ME network can provide one blank pattern")
    static final String INSERT_PATTERN = "gtocore.ae.pattern_editor.insert_pattern";
    @RegisterLanguage(cn = "可提取样板并编码", en = "Pattern can be extracted and encoded")
    private static final String EMPTY_PATTERN = "gtocore.ae.pattern_editor.empty_pattern";
    @RegisterLanguage(cn = "此样板不完整", en = "This pattern is incomplete")
    private static final String INCOMPLETE = "gtocore.ae.pattern_editor.incomplete";
    @RegisterLanguage(cn = "修改已写入样板", en = "Changes saved to the pattern")
    private static final String READY = "gtocore.ae.pattern_editor.ready";
    @RegisterLanguage(cn = "输入", en = "Inputs")
    private static final String INPUT = "gtocore.ae.pattern_editor.input";
    @RegisterLanguage(cn = "输出", en = "Outputs")
    private static final String OUTPUT = "gtocore.ae.pattern_editor.output";
    @RegisterLanguage(cn = "%s #%s：%s", en = "%s #%s: %s")
    private static final String SELECTED = "gtocore.ae.pattern_editor.selected";
    @RegisterLanguage(cn = "空槽", en = "Empty slot")
    private static final String EMPTY = "gtocore.ae.pattern_editor.empty";
    @RegisterLanguage(cn = "选择一个有内容的槽位后修改数量", en = "Select a non-empty slot to edit its amount")
    private static final String SELECT_SLOT = "gtocore.ae.pattern_editor.select_slot";
    @RegisterLanguage(cn = "清空选中槽位", en = "Clear Selected Slot")
    private static final String CLEAR = "gtocore.ae.pattern_editor.clear";
    @RegisterLanguage(cn = "转化为通配符", en = "Convert to Wildcard")
    private static final String CONVERT_TO_WILDCARD = "gtocore.ae.pattern_editor.convert_to_wildcard";

    private static final Component[] STATES = { Component.translatable(INSERT_PATTERN), Component.translatable(INCOMPLETE), Component.translatable(READY), Component.translatable(EMPTY_PATTERN) };
    private static final Component INSTRUCTIONS = Component.translatable(HINT);
    private static final int SIDE_COLUMNS = 3;
    private static final int PAGE_SLOTS = SIDE_COLUMNS * SIDE_COLUMNS;
    private static final int SIDE_WIDTH = SIDE_COLUMNS * UISizes.SLOT_SIZE;
    private static final int ARROW_WIDTH = 24;
    static final int CONTENT_WIDTH = 2 * SIDE_WIDTH + ARROW_WIDTH + 2 * UISizes.GAP;

    private final MEPatternPartMachine<?> machine;
    private final int patternIndex;
    private final RPC<Template> fillRecipe;
    private final Binding<Integer> selection;
    private int selectedSlot;
    private int nameSlot = -1;
    @Nullable
    private GenericStack nameStack;
    private Component selectedName = Component.empty();
    @Nullable
    private AEKey wildcardSource;
    @Nullable
    private TagPrefixKey wildcardTarget;

    private PatternEditor(MEPatternPartMachine<?> machine, int patternIndex) {
        this.machine = machine;
        this.patternIndex = patternIndex;
        fillRecipe = addRPC(Template.CODEC, (player, template) -> {
            if (player != null && !player.isSpectator()) {
                machine.fillEditorPattern(patternIndex, template.inputs(), template.outputs(), template.recipeId());
            }
        });
        layout(l -> l.column().gapAll(UISizes.GAP));
        selection = addBinding(Binding.bindInt(() -> selectedSlot, index -> selectedSlot = index, 0,
                AEProcessingPattern.MAX_INPUT_SLOTS + AEProcessingPattern.MAX_OUTPUT_SLOTS - 1));
        var inputs = side(false);
        var outputs = side(true);
        var arrow = TextLine.constant(ARROW_WIDTH, Component.literal("→")).setTextAlign(Horizontal.CENTER);
        addChild(UIElement.centeredRow(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP).alignSelf(AlignItems.CENTER))
                .addChildren(inputs, arrow, outputs));
        addChild(TextLine.of(LayoutStyle.AUTO, this::selectedName));
        var amount = NumberField.ofLong(LayoutStyle.AUTO, () -> {
            var stack = selectedStack();
            return stack == null ? 1 : stack.amount();
        }, value -> {
            var stack = selectedStack();
            if (stack != null) machine.setEditorStack(patternIndex, selectedOutput(), selectedIndex(), new GenericStack(stack.what(), value));
        }, () -> 1, () -> {
            var stack = selectedStack();
            return machine.getEditorAmountLimit(patternIndex, selectedOutput(), selectedIndex(), stack == null ? null : stack.what());
        });
        amount.disabled(() -> selectedStack() == null, SELECT_SLOT);
        addChild(amount);
        if (machine instanceof MEWildcardPatternBufferPartMachine) {
            var wildcard = UIElement.column(LayoutStyle.AUTO)
                    .addChild(Button.translatable(LayoutStyle.AUTO, CONVERT_TO_WILDCARD).setOnServerClick(this::convertSelectedToWildcard))
                    .setDisplay(false);
            addChild(wildcard);
            // 显隐同步挂在编辑器上，隐藏按钮后仍能收到重新显示的更新。
            addSyncValue(SyncValue.ofBool(() -> selectedWildcardKey() != null).onChanged(wildcard::setDisplay));
        }
        addChild(Button.translatable(LayoutStyle.AUTO, CLEAR)
                .setOnServerClick(() -> machine.setEditorStack(patternIndex, selectedOutput(), selectedIndex(), null))
                .disabled(() -> selectedStack() == null, SELECT_SLOT));
        disabled(() -> !machine.canEditPattern(patternIndex), INSERT_PATTERN);
    }

    static void build(MEPatternPartMachine<?> machine, int patternIndex, UIElement column) {
        var status = new StatusPanel();
        status.addSentence(() -> STATES[machine.getEditorState(patternIndex)])
                .bindLevel(() -> machine.getEditorState(patternIndex) == 2 ? Level.GOOD : Level.WARNING)
                .bindDetail(() -> INSTRUCTIONS);
        column.addChildren(status, new PatternEditor(machine, patternIndex));
    }

    static void buildSection(MEPatternPartMachine<?> machine, int patternIndex, UIElement column) {
        if (machine instanceof MEWildcardPatternBufferPartMachine) {
            build(machine, patternIndex, MEPatternPartUI.section(column, TITLE));
            return;
        }
        var section = UIElement.section(column.getContentWidth());
        column.addChild(section);
        var content = UIElement.column(section.getContentWidth()).layout(l -> l.gapAll(UISizes.GAP)).setDisplay(false);
        // 绑定留在可见的标题区，收起内容后仍可同步展开状态；状态只属于当前玩家的面板。
        var expanded = section.addBinding(Binding.bindBool(content::isDisplayed, content::setDisplay).onChanged(content::setDisplay));
        String title = Component.translatable(TITLE).getString();
        String expandedTitle = "▼ " + title;
        String collapsedTitle = "▶ " + title;
        section.addChildren(Button.translatable(LayoutStyle.AUTO, TITLE)
                .bindClientText(() -> expanded.getValue() ? expandedTitle : collapsedTitle)
                .setOnClientClick(() -> expanded.set(!expanded.getValue()))
                .disabled(() -> !expanded.getValue() && !machine.canEditPattern(patternIndex), INSERT_PATTERN), content);
        build(machine, patternIndex, content);
    }

    /** 只查当前打开的页面；后打开的弹出面板优先，关闭后不会保留填充目标。 */
    @Nullable
    public static PatternEditor find(ModularUI ui) {
        if (!(ui.holder instanceof MEPatternPartMachine<?> machine) || !machine.supportsPatternEditor()) return null;
        return find(ui.mainGroup);
    }

    @Nullable
    private static PatternEditor find(WidgetGroup group) {
        if (!group.isVisible() || !group.isActive()) return null;
        if (group instanceof PatternEditor editor) return editor;
        for (int i = group.widgets.size() - 1; i >= 0; i--) {
            if (group.widgets.get(i) instanceof WidgetGroup child) {
                var editor = find(child);
                if (editor != null) return editor;
            }
        }
        return null;
    }

    public boolean canFillRecipe(List<GenericStack> inputs, List<GenericStack> outputs) {
        return !isDisabled() && MEPatternPartMachine.validEditorStacks(inputs, AEProcessingPattern.MAX_INPUT_SLOTS) && MEPatternPartMachine.validEditorStacks(outputs, AEProcessingPattern.MAX_OUTPUT_SLOTS);
    }

    public boolean fillRecipe(List<GenericStack> inputs, List<GenericStack> outputs, @Nullable ResourceLocation recipeId) {
        return canFillRecipe(inputs, outputs) && fillRecipe.send(new Template(inputs, outputs, recipeId));
    }

    private record Template(List<GenericStack> inputs, List<GenericStack> outputs, @Nullable ResourceLocation recipeId) {

        private static final StreamCodec<FriendlyByteBuf, List<GenericStack>> INPUTS = UICodecs.list(GTOCodecs.GENERIC_STACK_STREAM_CODEC, AEProcessingPattern.MAX_INPUT_SLOTS);
        private static final StreamCodec<FriendlyByteBuf, List<GenericStack>> OUTPUTS = UICodecs.list(GTOCodecs.GENERIC_STACK_STREAM_CODEC, AEProcessingPattern.MAX_OUTPUT_SLOTS);
        private static final StreamCodec<FriendlyByteBuf, Template> CODEC = StreamCodec.of((buf, template) -> {
            INPUTS.encode(buf, template.inputs());
            OUTPUTS.encode(buf, template.outputs());
            buf.writeBoolean(template.recipeId() != null);
            if (template.recipeId() != null) ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.encode(buf, template.recipeId());
        }, buf -> new Template(INPUTS.decode(buf), OUTPUTS.decode(buf), buf.readBoolean() ? ByteBufCodecExtends.RESOURCE_LOCATION_CODEC.decode(buf) : null));
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
        return machine.getEditorStack(patternIndex, selectedOutput(), selectedIndex());
    }

    @Nullable
    private TagPrefixKey selectedWildcardKey() {
        var stack = selectedStack();
        var key = stack == null ? null : stack.what();
        if (key == wildcardSource) return wildcardTarget;
        wildcardSource = key;
        wildcardTarget = null;
        if (key == null || key instanceof TagPrefixKey) return null;
        // 非材料流体没有 FLUID 属性，不能交给材料形式转换器解析。
        if (key instanceof AEFluidKey fluid && !ChemicalHelper.getMaterial(fluid.getFluid()).hasProperty(PropertyKey.FLUID)) return null;
        wildcardTarget = AEChemicalHelper.getTagPrefixKey(key);
        return wildcardTarget;
    }

    private void convertSelectedToWildcard() {
        var stack = selectedStack();
        var wildcard = selectedWildcardKey();
        if (stack != null && wildcard != null) {
            machine.setEditorStack(patternIndex, selectedOutput(), selectedIndex(), new GenericStack(wildcard, stack.amount()));
        }
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

        private final PatternEditor editor;
        private final int slot;

        GhostSlot(PatternEditor editor, boolean output, int index) {
            super(() -> editor.machine.getEditorStack(editor.patternIndex, output, index), stack -> {
                editor.selectedSlot = (output ? AEProcessingPattern.MAX_INPUT_SLOTS : 0) + index;
                editor.machine.setEditorStack(editor.patternIndex, output, index, stack);
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
