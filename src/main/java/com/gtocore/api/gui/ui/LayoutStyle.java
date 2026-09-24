package com.gtocore.api.gui.ui;

/**
 * 元素布局参数，概念上对应 LDLib2 的 {@code LayoutStyle}（flexbox 的最小子集）。
 * <p>
 * 只支持单轴顺序排布：主轴按 {@link FlexDirection} 依次摆放子元素、子元素之间留 {@link #gap} 间距；
 * 交叉轴按 {@link AlignItems} 靠前或居中。宽高为 {@link #AUTO} 时由子元素撑开，否则固定。
 * 横向固定宽度时，设了 {@link #flexGrow} 的子元素分走剩余宽度。
 */
public final class LayoutStyle {

    public static final int AUTO = -1;

    public enum FlexDirection {
        ROW,
        COLUMN
    }

    public enum AlignItems {
        START,
        CENTER
    }

    FlexDirection flexDirection = FlexDirection.COLUMN;
    AlignItems alignItems = AlignItems.START;
    int width = AUTO;
    int height = AUTO;
    int gap;
    int paddingTop;
    int paddingLeft;
    int paddingRight;
    int paddingBottom;
    int flexGrow;

    /**
     * 在固定宽度的横向父元素里，按权重分走剩余宽度（对应 LDLib2 {@code flex(n)}）。
     * 只对横向排布生效，且子元素本身必须是 {@link UIElement}。
     */
    public LayoutStyle flexGrow(int weight) {
        this.flexGrow = weight;
        return this;
    }

    public LayoutStyle flexDirection(FlexDirection direction) {
        this.flexDirection = direction;
        return this;
    }

    public LayoutStyle row() {
        return flexDirection(FlexDirection.ROW);
    }

    public LayoutStyle column() {
        return flexDirection(FlexDirection.COLUMN);
    }

    public LayoutStyle alignItems(AlignItems align) {
        this.alignItems = align;
        return this;
    }

    public LayoutStyle alignCenter() {
        return alignItems(AlignItems.CENTER);
    }

    public LayoutStyle width(int width) {
        this.width = width;
        return this;
    }

    public LayoutStyle height(int height) {
        this.height = height;
        return this;
    }

    public LayoutStyle size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public LayoutStyle gapAll(int gap) {
        this.gap = gap;
        return this;
    }

    public LayoutStyle paddingAll(int padding) {
        this.paddingTop = padding;
        this.paddingLeft = padding;
        this.paddingRight = padding;
        this.paddingBottom = padding;
        return this;
    }

    public LayoutStyle paddingTop(int padding) {
        this.paddingTop = padding;
        return this;
    }

    public LayoutStyle paddingLeft(int padding) {
        this.paddingLeft = padding;
        return this;
    }

    public LayoutStyle paddingRight(int padding) {
        this.paddingRight = padding;
        return this;
    }

    public LayoutStyle paddingBottom(int padding) {
        this.paddingBottom = padding;
        return this;
    }
}
