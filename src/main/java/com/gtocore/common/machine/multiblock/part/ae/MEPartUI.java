package com.gtocore.common.machine.multiblock.part.ae;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.NumberField;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.elements.TextField;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.elements.ae.AEStackGrid;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

/**
 * ME 系列部件界面的公共片段（输入 / 输出总线与仓、双输出、消声仓、能源接入、存储访问等），放在 {@link MachineWindow} 里：
 *
 * <pre>
 * ┌ 标题栏：[●] 机器名             ┐   网络在线指示灯（红离线 / 绿在线）
 * │ 页面：每行 9 格的网格、区块……   │
 * └───────────────────────────────┘
 * </pre>
 * 
 * 电路、优先级、自动拉取等设置仍在左侧小组件与子页面里。ME 样板类部件的标题栏带 AE 名称输入框，见 {@link MEPatternPartUI}。
 */
public final class MEPartUI {

    /// "等待输出"列表至少显示几行（空格子占位，列表增减时界面不跳），最多显示几行（再多滚动）
    private static final int WAITING_MIN_ROWS = 3;
    private static final int WAITING_MAX_ROWS = 6;

    private MEPartUI() {}

    /**
     * 主页：放进 {@link MachineWindow} 时网络状态放在窗口标题栏里，省下一行；其他外壳里放在页面第一行。
     * {@code online} 在服务端取值。
     */
    public static Widget mainPage(BooleanSupplier online, Component title, FancyMachineUIWidget widget, UIElement page) {
        if (widget instanceof MachineWindow window) {
            window.setTitleContent(width -> header(online, title, width));
            return page;
        }
        return UIElement.column(UISizes.CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP))
                .addChildren(header(online, title, UISizes.CONTENT_WIDTH), page);
    }

    /** ME 部件的主页（网络状态取部件的在线字段）。 */
    public static Widget mainPage(MEPartMachine machine, FancyMachineUIWidget widget, UIElement page) {
        return mainPage(machine::getOnlineField, machine.getTitle(), widget, page);
    }

    /** 页面：标准内容宽的一列，区块间距 {@link UISizes#SECTION_GAP}。 */
    public static UIElement page() {
        return UIElement.column(LayoutStyle.AUTO).layout(l -> l.minWidth(UISizes.CONTENT_WIDTH).gapAll(UISizes.SECTION_GAP));
    }

    /** 标题栏中段：网络在线指示灯 + 机器名（吃满剩余宽度）。 */
    private static UIElement header(BooleanSupplier online, Component title, int width) {
        var name = TextLine.constant(0, title).setColor(UITheme.TEXT);
        name.layout(l -> l.flex(1));
        return UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.width(width).gapAll(UISizes.SECTION_GAP).alignCenter())
                .addChildren(MEPatternPartUI.onlineIndicator(online), name);
    }

    /**
     * "等待输出"：还没送进 ME 网络的物品 / 流体（只读），一行标题 + 每行 9 格的网格；至少 3 行，超过 6 行滚动。
     * {@code scrollerId} 是滚动区的固定 id（锁定高度按它记）。
     */
    public static UIElement waitingList(String scrollerId, KeyStorage storage, boolean fluid, @Nullable String titleKey) {
        var scroller = new ScrollerView(scrollerId, UISizes.SLOT_ROW_WIDTH, UISizes.SLOT)
                .adaptiveWidth().adaptiveHeight(WAITING_MAX_ROWS * UISizes.SLOT);
        scroller.addScrollViewChild(new AEStackGrid(storage, fluid, WAITING_MIN_ROWS));
        return UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(
                TextLine.translatable(LayoutStyle.AUTO, titleKey == null ? "gtceu.gui.waiting_list" : titleKey).setColor(UITheme.TEXT),
                scroller);
    }

    /** 区块里一行"说明 …… [数值输入框]"：输入框标准按钮宽，说明悬停显示 {@code tooltipKeys}。 */
    public static UIElement fieldRow(String labelKey, TextField field, String... tooltipKeys) {
        field.layout(l -> l.width(UISizes.BUTTON_WIDTH));
        return controlRow(labelKey, field, tooltipKeys);
    }

    /**
     * 区块里的数值设置：上一行说明（悬停显示 {@code tooltipKeys}），下一行整宽的标准数值输入。
     * 数值输入两侧有加减按钮，和说明挤在一行时输入框太窄，所以分两行。
     */
    public static UIElement numberRow(String labelKey, NumberField field, String... tooltipKeys) {
        var label = TextLine.translatable(LayoutStyle.AUTO, labelKey).setColor(UITheme.PANEL_TEXT);
        if (tooltipKeys.length > 0) label.setHoverTooltips(tooltipKeys);
        return UIElement.column(LayoutStyle.AUTO).layout(l -> l.gapAll(UISizes.GAP)).addChildren(label, field);
    }

    /** 区块里一行"说明 …… [控件]"（开关、输入框等靠右），说明悬停显示 {@code tooltipKeys}。 */
    public static UIElement controlRow(String labelKey, Widget control, String... tooltipKeys) {
        var label = TextLine.translatable(0, labelKey).setColor(UITheme.PANEL_TEXT);
        label.layout(l -> l.flex(1));
        if (tooltipKeys.length > 0) label.setHoverTooltips(tooltipKeys);
        return UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.gapAll(UISizes.GAP).alignCenter())
                .addChildren(label, control);
    }
}
