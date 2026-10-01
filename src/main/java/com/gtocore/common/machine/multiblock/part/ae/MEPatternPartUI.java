package com.gtocore.common.machine.multiblock.part.ae;

import com.gtocore.eio_travel.logic.TravelUtils;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.Level;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.data.UIChannel;
import com.gregtechceu.gtceu.uipro.elements.FluidSlot;
import com.gregtechceu.gtceu.uipro.elements.Indicator;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.elements.ItemSlot;
import com.gregtechceu.gtceu.uipro.elements.Label;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.PageView;
import com.gregtechceu.gtceu.uipro.elements.SlotGrid;
import com.gregtechceu.gtceu.uipro.elements.StatusPanel;
import com.gregtechceu.gtceu.uipro.elements.Stepper;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * ME 样板类部件界面的公共片段。页面宽 {@link UISizes#CONTENT_WIDTH}（162），放在 {@link com.gregtechceu.gtceu.uipro.window.MachineWindow} 里：
 *
 * <pre>
 * ┌ 标题栏：网络状态 · AE 显示名称 [输入框吃满剩余宽度] ┐
 * │ 样板网格（每页至多 6×9）                         │
 * └ 底栏：[&lt;] 页码 [&gt;] ……… [重置缓存] [清除配方]   ┘
 * </pre>
 *
 * 中键点样板槽在窗口右侧弹出该槽的独立配置面板（{@link MEPatternPartMachine#SLOT_CONFIG_POPUP}），
 * 内容是若干 {@link #section} 区块（物品、流体、电路、配方……）。
 */
public final class MEPatternPartUI {

    /** 每页样板行数。 */
    public static final int ROWS_PER_PAGE = 6;
    public static final int SLOTS_PER_PAGE = UISizes.SLOTS_PER_ROW * ROWS_PER_PAGE;
    /** 电路编号上限。 */
    public static final int MAX_CIRCUIT = 32;
    /** 电路编号 -1 表示不放电路（0 是有效的 0 号编程电路）。 */
    public static final int NO_CIRCUIT = -1;

    private MEPatternPartUI() {}

    public static int pageCount(MEPatternPartMachine<?> machine) {
        return (machine.getMaxPatternCount() + SLOTS_PER_PAGE - 1) / SLOTS_PER_PAGE;
    }

    // ==================== 标题栏、底栏 ====================

    /**
     * 标题栏中段（放进 {@link com.gregtechceu.gtceu.uipro.window.MachineWindow} 的标题栏）：网络在线指示灯 + AE 显示名称输入框（吃满剩余宽度）。
     * 名称为空时输入框里灰字显示机器名——AE 在没有自定义名称时用的就是它。
     */
    public static UIElement header(MEPatternPartMachine<?> machine, int width) {
        var nameField = TextField.of(0, machine::getCustomName, name -> {
            machine.setCustomName(name);
            TravelUtils.requireResync(Objects.requireNonNull(machine.getLevel()));
        });
        nameField.layout(l -> l.flexGrow(1));
        nameField.bindClientPlaceholder(() -> machine.getDefinition().asItem().getDescription());
        nameField.setHoverTooltips(Component.translatable(MEPatternPartMachine.AE_NAME));
        return UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.width(width).gapAll(UISizes.SECTION_GAP).alignCenter())
                .addChildren(onlineIndicator(machine::getOnlineField), nameField);
    }

    /** ME 网络在线指示灯（红：离线，绿：在线），标题栏行首用；{@code online} 在服务端取值。 */
    public static Indicator onlineIndicator(BooleanSupplier online) {
        return Indicator.of(() -> online.getAsBoolean() ? 1 : 0,
                Indicator.State.of(UITheme.STATUS_OFFLINE, "gtceu.gui.me_network.offline"),
                Indicator.State.of(UITheme.STATUS_ONLINE, "gtceu.gui.me_network.online"));
    }

    /** 底栏：左侧翻页（多于一页时），右侧附加按钮。两者都没有时返回 null。 */
    @Nullable
    public static UIElement footer(MEPatternPartMachine<?> machine, int width, PageView pageView, Widget... actions) {
        int pages = pageCount(machine);
        if (pages <= 1 && actions.length == 0) return null;
        var row = UIElement.centeredRow(UISizes.CONTROL_HEIGHT).layout(l -> l.width(width));
        if (pages > 1) {
            IntFunction<String> format = page -> (page + 1) + "/" + pages;
            row.addChild(Stepper.of(UISizes.VALUE_WIDTH, pageView::getPage, pageView::selectPage, 0, pages - 1).setFormatter(format));
        }
        row.addChild(UIElement.flexSpacer());
        row.addChildren(actions);
        return row;
    }

    // ==================== 样板网格 ====================

    /**
     * 样板网格分页：每页至多 6 行 × 9 槽，整体水平居中。高度固定为"槽位最多的那页"的高度，翻页时窗口尺寸不变。
     * {@code gridHeader} 画在每页网格上方（可为空），占 {@code gridHeaderHeight}（含与网格的间距）；
     * {@code emptyPageText} 在没有任何样板槽时以一行状态面板（黄灯"注意"）显示。
     */
    public static PageView patternPages(MEPatternPartMachine<?> machine, int width, @Nullable Consumer<UIElement> gridHeader,
                                        int gridHeaderHeight, @Nullable Supplier<Component> emptyPageText) {
        int slotCount = machine.getMaxPatternCount();
        int rows = Math.max(1, Math.min(ROWS_PER_PAGE, (slotCount + UISizes.SLOTS_PER_ROW - 1) / UISizes.SLOTS_PER_ROW));
        boolean empty = slotCount == 0 && emptyPageText != null;
        int height = empty ? StatusPanel.heightFor(1) : gridHeaderHeight + rows * UISizes.SLOT_SIZE;
        var pages = new PageView(width, height);
        for (int pageStart = 0; pageStart < slotCount; pageStart += SLOTS_PER_PAGE) {
            int start = pageStart;
            int end = Math.min(slotCount, pageStart + SLOTS_PER_PAGE);
            pages.addPage(page -> {
                page.layout(l -> l.alignCenter().gapAll(UISizes.GAP));
                if (gridHeader != null) gridHeader.accept(page);
                page.addChild(SlotGrid.of(UISizes.SLOTS_PER_ROW, end - start, i -> machine.createPatternSlot(start + i))
                        .layout(l -> l.width(UISizes.SLOT_ROW_WIDTH)));
            });
        }
        if (empty) {
            pages.addPage(page -> {
                var status = new StatusPanel(page.getContentWidth());
                status.addSentence(emptyPageText).bindLevel(() -> Level.WARNING);
                page.addChild(status);
            });
        }
        return pages;
    }

    // ==================== 单槽配置（弹出面板内容） ====================

    /** 面板区块：深灰面板，先放一行标题，调用方再往里加内容。 */
    public static UIElement section(UIElement parent, String titleKey) {
        var section = UIElement.section(parent.getContentWidth());
        section.addChild(Label.translatable(section.getContentWidth(), titleKey).bindClientColor(UITheme::panelText));
        parent.addChild(section);
        return section;
    }

    /** 按每行 9 个排布 {@code count} 个槽，槽由 {@code slotFactory} 按序号创建。 */
    public static void slotRows(UIElement section, int count, java.util.function.IntFunction<Widget> slotFactory) {
        for (int rowStart = 0; rowStart < count; rowStart += UISizes.SLOTS_PER_ROW) {
            var row = UIElement.row(UISizes.SLOT_SIZE);
            for (int i = rowStart; i < Math.min(count, rowStart + UISizes.SLOTS_PER_ROW); i++) {
                row.addChild(slotFactory.apply(i));
            }
            section.addChild(row);
        }
    }

    /** 流体槽行（标准 {@link FluidSlot}）；{@code decorator} 可设置每个槽的状态或给它套一层叠加显示。 */
    public static void fluidSlots(UIElement section, IFluidHandler[] fluidHandlers, @Nullable BiFunction<Integer, FluidSlot, Widget> decorator) {
        slotRows(section, fluidHandlers.length, i -> {
            var tank = FluidSlot.of(fluidHandlers[i]);
            return decorator == null ? tank : decorator.apply(i, tank);
        });
    }

    /**
     * 电路行：只读电路槽 + 步进器 {@code [<] n [>]} + 说明标记。-1 为不放电路（默认），0~32 为对应编号的编程电路，
     * 越过 32 回到 -1。
     */
    public static UIElement circuitRow(Widget circuitSlot, IntSupplier circuit, IntConsumer setCircuit) {
        var stepper = Stepper.of(UISizes.VALUE_WIDTH, circuit, setCircuit, NO_CIRCUIT, MAX_CIRCUIT).wrap().setFormatter(String::valueOf);
        stepper.setHoverTooltips(MEPatternPartMachine.CIRCUIT_HINT);
        return UIElement.row(UISizes.SLOT_SIZE).layout(l -> l.gapAll(UISizes.SECTION_GAP).alignCenter())
                .addChildren(circuitSlot, stepper, InfoIcon.info(MEPatternPartMachine.CIRCUIT_NONE));
    }

    /** 电路物品 → 编号，空槽为 {@link #NO_CIRCUIT}。 */
    public static int circuitOf(ItemStack stack) {
        return stack.isEmpty() ? NO_CIRCUIT : IntCircuitBehaviour.getCircuitConfiguration(stack);
    }

    /** 编号 → 电路物品，小于 0 为空。 */
    public static ItemStack circuitStack(int circuit) {
        return circuit < 0 ? ItemStack.EMPTY : IntCircuitBehaviour.stack(Math.min(circuit, MAX_CIRCUIT));
    }

    /** 电路槽只作展示（由步进器设置），标为只读。 */
    public static ItemSlot readOnlyCircuitSlot(CustomItemStackHandler circuitHandler) {
        return ItemSlot.display(circuitHandler, 0, MEPatternPartMachine.CIRCUIT_READ_ONLY);
    }

    /** 整数输入（标准数值输入 {@link NumberField}），写入值在 [{@code min}, int 上限]；{@code width} 为 0 时由父元素拉伸。 */
    public static NumberField intField(int width, IntSupplier getter, IntConsumer setter, int min) {
        return NumberField.ofInt(width == 0 ? LayoutStyle.AUTO : width, getter, setter, min, Integer.MAX_VALUE);
    }

    /** 长整数输入（标准数值输入 {@link NumberField}），写入值不小于 {@code min}；{@code width} 为 0 时由父元素拉伸。 */
    public static NumberField longField(int width, LongSupplier getter, LongConsumer setter, long min) {
        return NumberField.ofLong(width == 0 ? LayoutStyle.AUTO : width, getter, setter, min, Long.MAX_VALUE);
    }

    /** 一行"说明文字 …… 控件"，控件靠右；用在面板区块里。 */
    public static UIElement labeledRow(int width, String key, @Nullable String tooltipKey, Widget control) {
        return tooltipKey == null ? labeledRow(width, key, control) : labeledRow(width, key, control, tooltipKey);
    }

    /** 同上，说明文字的悬浮提示可以有多行（每个键一行）。 */
    public static UIElement labeledRow(int width, String key, Widget control, String... tooltipKeys) {
        var label = Label.translatable(width - control.getSizeWidth() - UISizes.GAP, key).bindClientColor(UITheme::panelText);
        if (tooltipKeys.length > 0) label.setHoverTooltips(tooltipKeys);
        return UIElement.centeredRow(Math.max(UISizes.CONTROL_HEIGHT, control.getSizeHeight())).layout(l -> l.width(width))
                .addChildren(label, UIElement.flexSpacer(), control);
    }

    // ==================== 缺货标记 ====================

    /** 给 18×18 的槽套一层红框：{@code missing} 为真时表示该虚拟输入在网络里缺货。 */
    public static Widget missingVirtualInputOverlay(Widget child, BooleanSupplier missing) {
        return new MissingVirtualInputOverlay(child, missing);
    }

    private static final class MissingVirtualInputOverlay extends WidgetGroup implements UIChannel.Host {

        private static final int MISSING_FILL = 0x66FF0000;
        private static final int MISSING_BORDER = 0xFFFF0000;

        private final UIChannel channel = new UIChannel(this);
        private boolean missing;

        private MissingVirtualInputOverlay(Widget child, BooleanSupplier missingSupplier) {
            super(0, 0, UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
            addSyncValue(SyncValue.ofBool(missingSupplier).onChanged(value -> missing = value));
            addWidget(child);
        }

        @Override
        public UIChannel getChannel() {
            return channel;
        }

        @Override
        public void initWidget() {
            super.initWidget();
            channel.prime();
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buffer) {
            super.writeInitialData(buffer);
            channel.writeInitialData(buffer);
        }

        @Override
        public void readInitialData(FriendlyByteBuf buffer) {
            super.readInitialData(buffer);
            channel.readInitialData(buffer);
        }

        @Override
        public void detectAndSendChanges() {
            super.detectAndSendChanges();
            channel.detectAndSendChanges();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            if (!channel.readUpdateInfo(id, buffer)) super.readUpdateInfo(id, buffer);
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (!channel.handleClientAction(id, buffer)) super.handleClientAction(id, buffer);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            if (!missing) return;
            int x = getPositionX(), y = getPositionY(), w = getSizeWidth(), h = getSizeHeight();
            DrawerHelper.drawSolidRect(graphics, x, y, w, h, MISSING_FILL);
            DrawerHelper.drawSolidRect(graphics, x, y, w, 1, MISSING_BORDER);
            DrawerHelper.drawSolidRect(graphics, x, y + h - 1, w, 1, MISSING_BORDER);
            DrawerHelper.drawSolidRect(graphics, x, y, 1, h, MISSING_BORDER);
            DrawerHelper.drawSolidRect(graphics, x + w - 1, y, 1, h, MISSING_BORDER);
        }
    }
}
