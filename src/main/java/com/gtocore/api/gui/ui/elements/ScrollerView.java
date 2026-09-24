package com.gtocore.api.gui.ui.elements;

import com.gtocore.api.gui.ui.LayoutStyle;
import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;

import java.util.function.Consumer;

/**
 * 固定尺寸的纵向滚动视图，对应 LDLib2 {@code ScrollerView}：内容放在一个纵向 {@link UIElement} 里，
 * 内容高度变化时自动刷新滚动范围。Ore 风格：滚动条宽 {@link #SCROLL_BAR_WIDTH}（深色轨道 + 浅色凸起滑块），
 * 与内容隔 {@link #SCROLL_BAR_MARGIN}，内容可用宽度为 {@code width - 10}。
 */
public class ScrollerView extends DraggableScrollableWidgetGroup {

    public static final int SCROLL_BAR_WIDTH = 8;
    public static final int SCROLL_BAR_MARGIN = 2;

    private final UIElement content;
    private boolean updatingScrollArea;
    private int fitMaxHeight = -1;

    public ScrollerView(int width, int height) {
        this(width, height, 0);
    }

    public ScrollerView(int width, int height, int gap) {
        super(0, 0, width, height);
        setScrollWheelDirection(ScrollWheelDirection.VERTICAL);
        setYScrollBarWidth(SCROLL_BAR_WIDTH);
        setYBarStyle(UITheme.SCROLL_TRACK, UITheme.SCROLL_THUMB);
        setDraggable(false);
        setScrollable(true);
        setUseScissor(true);
        content = new UIElement() {

            @Override
            protected void onSizeUpdate() {
                super.onSizeUpdate();
                updateScrollArea();
            }
        };
        content.layout(l -> l.column().width(width - SCROLL_BAR_WIDTH - SCROLL_BAR_MARGIN).gapAll(gap));
        super.addWidget(content);
    }

    /** 内容区可用宽度（已扣除滚动条）。 */
    public int getContentWidth() {
        return content.getContentWidth();
    }

    /**
     * 高度跟随内容，最高 {@code maxHeight}，超出才滚动。内容高度只在客户端准确（文字尺寸在客户端测量），
     * 因此两端尺寸可能不同，但控件树一致，不影响同步。
     */
    public ScrollerView fitContentHeight(int maxHeight) {
        this.fitMaxHeight = maxHeight;
        updateScrollArea();
        return this;
    }

    public ScrollerView layoutContent(Consumer<LayoutStyle> layout) {
        content.layout(layout);
        return this;
    }

    public ScrollerView addScrollViewChild(Widget child) {
        content.addWidget(child);
        updateScrollArea();
        return this;
    }

    private void updateScrollArea() {
        if (updatingScrollArea) return;
        updatingScrollArea = true;
        try {
            if (fitMaxHeight > 0) {
                int height = Math.min(content.getSizeHeight(), fitMaxHeight);
                if (height != getSizeHeight()) setSize(new Size(getSizeWidth(), height));
                // 父类 setSize 只会把滚动范围往大改、从不缩小；视口缩到内容高度后要把范围重置回视口，
                // 否则按最初（更高）的视口留下一截空白可滚动区域
                maxHeight = getSizeHeight() - xBarHeight;
            }
            computeMax();
        } finally {
            updatingScrollArea = false;
        }
    }
}
