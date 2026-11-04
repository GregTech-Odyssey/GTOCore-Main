package com.gtocore.api.gui.ui_element;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

/**
 * 横向选项条：一排按钮从左到右排，放不下时用滚轮横向滚动，不画滚动条。
 * uipro 的 {@code ScrollerView} 只支持纵向滚动，所以直接用 LDLib 的横向滚动组。
 */
public final class HorizontalScroller extends DraggableScrollableWidgetGroup {

    public static final int HEIGHT = UISizes.CONTROL_HEIGHT;

    /** 内容行：宽度由按钮撑开。 */
    private final UIElement row = UIElement.row(HEIGHT).layout(l -> l.gapAll(UISizes.GAP));

    public HorizontalScroller(int width) {
        super(0, 0, width, HEIGHT);
        setXScrollBarHeight(0);
        setYScrollBarWidth(0);
        setScrollWheelDirection(ScrollWheelDirection.HORIZONTAL);
        setDraggable(false);
        addWidget(row);
    }

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
