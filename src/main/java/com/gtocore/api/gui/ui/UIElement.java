package com.gtocore.api.gui.ui;

import com.gtocore.api.gui.ui.data.SyncValue;
import com.gtocore.api.gui.ui.data.SyncValueHost;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * 在 LDLib1 上仿照 LDLib2 {@code UIElement} 的通用节点：任何元素都能挂子元素、声明布局、挂同步值。
 * <p>
 * 与 LDLib2 的对应关系：
 * <ul>
 * <li>{@link #layout(Consumer)} ↔ {@code UIElement.layout}，布局只实现单轴排布（见 {@link LayoutStyle}）；</li>
 * <li>{@link #addChild}/{@link #addChildren} ↔ {@code addChild/addChildren}；</li>
 * <li>{@link #addSyncValue} ↔ {@code addSyncValue}，但同步范围是本元素，而非整棵 UI。</li>
 * </ul>
 * 子元素尺寸变化会经 LDLib1 的 {@code onChildSizeUpdate} 触发本元素重新排布，并继续向上冒泡。
 */
public class UIElement extends WidgetGroup {

    protected final LayoutStyle style = new LayoutStyle();
    private final SyncValueHost syncValues = new SyncValueHost(this);
    private boolean layouting;
    @Nullable
    private BooleanSupplier selected;

    public UIElement() {
        super(Position.ORIGIN, Size.ZERO);
    }

    /** 固定宽度、高度随内容增长的纵向容器。 */
    public static UIElement column(int width) {
        return new UIElement().layout(l -> l.column().width(width));
    }

    /** 固定高度、宽度随内容增长的横向容器。 */
    public static UIElement row(int height) {
        return new UIElement().layout(l -> l.row().height(height));
    }

    /** 占位空白，对应 LDLib2 里一个只设了尺寸的空元素。 */
    public static Widget spacer(int width, int height) {
        return new Widget(0, 0, width, height);
    }

    /** 带面板底图（{@link UITheme#PANEL}，浅灰凸起，里面的字用 {@link UITheme#PANEL_TEXT}）和内边距的纵向区块，用来把一组相关控件框在一起。 */
    public static UIElement section(int width) {
        var section = new UIElement().layout(l -> l.column().width(width).gapAll(UISizes.GAP)
                .paddingAll(UITheme.PANEL_PADDING).paddingBottom(UITheme.PANEL_PADDING_BOTTOM));
        section.setBackground(UITheme.PANEL);
        return section;
    }

    /** 横向行里吃掉剩余宽度的空白，用来把后面的元素推到右侧。 */
    public static UIElement flexSpacer() {
        return new UIElement().layout(l -> l.flexGrow(1).height(0));
    }

    public UIElement layout(Consumer<LayoutStyle> layout) {
        layout.accept(style);
        recomputeLayout();
        return this;
    }

    public UIElement addChild(Widget child) {
        addWidget(child);
        return this;
    }

    public UIElement addChildren(Widget... children) {
        for (var child : children) addWidget(child);
        return this;
    }

    public <T> SyncValue<T> addSyncValue(SyncValue<T> value) {
        return syncValues.add(value);
    }

    /**
     * 选中框：{@code selected} 为真时在元素边缘画统一的选中框（{@link UITheme#drawSelection}），物品槽、列表项等都用它。
     * {@code selected} 在客户端每帧取值，只应依赖本端界面状态（如所在窗口的弹出面板是否打开），
     * 不要放进机器的同步字段——选中是"这个界面"的状态，放在机器上多人同时打开时会互相干扰。
     */
    public UIElement setSelected(@Nullable BooleanSupplier selected) {
        this.selected = selected;
        return this;
    }

    /** 选中框画在前景层：所有元素的背景（含之后才绘制的相邻槽）都画完后再画，外扩的那 1 像素不会被邻格盖住。 */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (selected != null && selected.getAsBoolean()) {
            UITheme.drawSelection(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    /** 子元素可用的内容宽度；宽度未固定时返回 {@link Integer#MAX_VALUE}。 */
    public int getContentWidth() {
        return style.width == LayoutStyle.AUTO ? Integer.MAX_VALUE : style.width - style.paddingLeft - style.paddingRight;
    }

    /** 子元素可用的内容高度；高度未固定时返回 {@link Integer#MAX_VALUE}。 */
    public int getContentHeight() {
        return style.height == LayoutStyle.AUTO ? Integer.MAX_VALUE : style.height - style.paddingTop - style.paddingBottom;
    }

    @Override
    protected void recomputeLayout() {
        if (layouting) return;
        layouting = true;
        try {
            if (style.flexDirection == LayoutStyle.FlexDirection.COLUMN) layoutColumn();
            else layoutRow();
        } finally {
            layouting = false;
        }
    }

    private void layoutColumn() {
        int maxWidth = 0;
        for (var widget : widgets) maxWidth = Math.max(maxWidth, widget.getSizeWidth());
        int innerWidth = style.width == LayoutStyle.AUTO ? maxWidth : style.width - style.paddingLeft - style.paddingRight;
        boolean center = style.alignItems == LayoutStyle.AlignItems.CENTER;
        int y = style.paddingTop;
        for (int i = 0; i < widgets.size(); i++) {
            var widget = widgets.get(i);
            if (i > 0) y += style.gap;
            int x = style.paddingLeft + (center ? innerWidth / 2 - widget.getSizeWidth() / 2 : 0);
            widget.setSelfPosition(new Position(x, y));
            y += widget.getSizeHeight();
        }
        int width = style.width == LayoutStyle.AUTO ? maxWidth + style.paddingLeft + style.paddingRight : style.width;
        int height = style.height == LayoutStyle.AUTO ? y + style.paddingBottom : style.height;
        setSize(new Size(width, height));
    }

    private void layoutRow() {
        if (style.width != LayoutStyle.AUTO) distributeFlexWidth();
        int maxHeight = 0;
        for (var widget : widgets) maxHeight = Math.max(maxHeight, widget.getSizeHeight());
        int innerHeight = style.height == LayoutStyle.AUTO ? maxHeight : style.height - style.paddingTop - style.paddingBottom;
        boolean center = style.alignItems == LayoutStyle.AlignItems.CENTER;
        int x = style.paddingLeft;
        for (int i = 0; i < widgets.size(); i++) {
            var widget = widgets.get(i);
            if (i > 0) x += style.gap;
            int y = style.paddingTop + (center ? innerHeight / 2 - widget.getSizeHeight() / 2 : 0);
            widget.setSelfPosition(new Position(x, y));
            x += widget.getSizeWidth();
        }
        int width = style.width == LayoutStyle.AUTO ? x + style.paddingRight : style.width;
        int height = style.height == LayoutStyle.AUTO ? maxHeight + style.paddingTop + style.paddingBottom : style.height;
        setSize(new Size(width, height));
    }

    /** 按 flexGrow 权重把固定宽度里剩下的空间分给子元素。 */
    private void distributeFlexWidth() {
        int fixed = style.paddingLeft + style.paddingRight + Math.max(0, widgets.size() - 1) * style.gap;
        int totalWeight = 0;
        for (var widget : widgets) {
            if (widget instanceof UIElement element && element.style.flexGrow > 0) totalWeight += element.style.flexGrow;
            else fixed += widget.getSizeWidth();
        }
        if (totalWeight == 0) return;
        int remaining = Math.max(0, style.width - fixed);
        for (var widget : widgets) {
            if (widget instanceof UIElement element && element.style.flexGrow > 0) {
                int share = remaining * element.style.flexGrow / totalWeight;
                if (element.style.width != share) element.layout(l -> l.width(share));
            }
        }
    }

    // ==================== 同步 ====================

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        syncValues.writeInitialData(buffer);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        syncValues.readInitialData(buffer);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        syncValues.detectAndSendChanges(this::writeUpdateInfo);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (!syncValues.readUpdateInfo(id, buffer)) super.readUpdateInfo(id, buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        syncValues.pollClient();
    }
}
