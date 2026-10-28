package com.gtocore.common.machine.noenergy.game;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

/**
 * 横向选项条：一排按钮从左到右排列，放不下时用鼠标滚轮横向滚动，不画滚动条。
 *
 * <p>
 * uipro 的 {@code ScrollerView} 只做纵向滚动（内容宽度固定为视口宽度），所以这里直接用 LDLib 的
 * {@link DraggableScrollableWidgetGroup} 配成横向：内容是一行 {@link UIElement}，宽度由按钮自己撑开，
 * 滚轮方向设为横向，视口用滚动区自带的裁剪。
 */
public final class HorizontalScroller extends DraggableScrollableWidgetGroup {

    /** 控件高度：一行按钮。 */
    public static final int HEIGHT = UISizes.CONTROL_HEIGHT;

    /** 内容行：按钮按加入顺序从左到右排，宽度由内容撑开。 */
    private final UIElement row = UIElement.row(HEIGHT).layout(l -> l.gapAll(UISizes.GAP));

    public HorizontalScroller(int width) {
        super(0, 0, width, HEIGHT);
        setXScrollBarHeight(0);
        setYScrollBarWidth(0);
        setScrollWheelDirection(ScrollWheelDirection.HORIZONTAL);
        setDraggable(false);
        addWidget(row);
    }

    /** 加一个选项；按加入顺序从左到右排列。 */
    public HorizontalScroller addOption(Widget option) {
        row.addWidget(option);
        row.getSize();
        computeMax();
        return this;
    }

    @Override
    public void initWidget() {
        row.getSize();
        computeMax();
        super.initWidget();
    }
}
