package com.gtocore.api.gui.ui.styletemplate;

/**
 * 界面元素的标准尺寸。所有元素默认按这里的高度/宽度出图，同一行的元素因此能上下对齐、列宽能凑整。
 * <p>
 * 横向网格照原版容器：窗口宽 {@link #WINDOW_WIDTH}，左右内边距 {@link #WINDOW_PADDING_X}，
 * 内容宽 {@link #CONTENT_WIDTH} 正好 9 个槽——标题栏、页面里的每一行、玩家背包共用同一条左边缘。
 * <p>
 * 竖向节奏：一行控件 14 高（按钮、输入框、步进器、开关同高，Ore 开关贴图也是 14 高），一行物品/流体槽 18 高，
 * 文字行 9 高；元素间距 {@link #GAP}，区块之间 {@link #SECTION_GAP}，只有这两档。
 */
public final class UISizes {

    private UISizes() {}

    // ==================== 槽位 ====================
    /** 物品/流体槽边长。 */
    public static final int SLOT = 18;
    /** 一行槽位数。 */
    public static final int SLOTS_PER_ROW = 9;
    /** 一整行槽位的宽度（162）。 */
    public static final int SLOT_ROW_WIDTH = SLOT * SLOTS_PER_ROW;

    // ==================== 控件高度 ====================
    /** 标准控件行高：按钮、输入框、步进器、开关。 */
    public static final int CONTROL_HEIGHT = 14;
    /** 单行文字高度（字体行高 9）。 */
    public static final int TEXT_HEIGHT = 9;
    /** 状态行高度：一行文字上下各留 1 像素（与单行 Label 同高），状态面板里逐行紧排。 */
    public static final int STATUS_LINE_HEIGHT = TEXT_HEIGHT + 2;

    // ==================== 控件宽度 ====================
    /** 方形图标按钮 / 步进器箭头的宽度，与控件行高相同。 */
    public static final int ICON_BUTTON = CONTROL_HEIGHT;
    /**
     * 常规文字按钮宽度：四个汉字（36）加两侧留白。按内容宽 162 凑整——
     * 一行放下"步进器（VALUE_WIDTH 时 60）+ 两个按钮"：60 + 2 × 48 + 3 × 2 = 162。
     */
    public static final int BUTTON_WIDTH = 48;
    /** 开关宽度（Ore 开关贴图原宽）。 */
    public static final int SWITCH_WIDTH = 24;
    /** 数字显示框的最小宽度（步进器中间那格，放得下 "10/10"）。 */
    public static final int VALUE_WIDTH = 28;

    // ==================== 间距 ====================
    /** 同一组内元素之间。 */
    public static final int GAP = 2;
    /** 区块与区块之间。 */
    public static final int SECTION_GAP = 4;
    /** 按钮文字两侧留白。 */
    public static final int TEXT_PADDING = 4;

    // ==================== 窗口 ====================
    /** 机器窗口的标准宽度（原版容器宽）。 */
    public static final int WINDOW_WIDTH = 176;
    /** 窗口左右内边距：内容区从 x = 7 开始，与原版容器的槽位对齐。 */
    public static final int WINDOW_PADDING_X = 7;
    /** 窗口上内边距。 */
    public static final int WINDOW_PADDING_TOP = 5;
    /** 窗口下内边距：比上边多 2，抵掉 Ore 面板底部的厚边。 */
    public static final int WINDOW_PADDING_BOTTOM = 7;
    /** 窗口内容宽度（162，正好 9 个槽）。 */
    public static final int CONTENT_WIDTH = WINDOW_WIDTH - 2 * WINDOW_PADDING_X;
    /** 玩家背包高度：三行背包 + 4 像素 + 快捷栏。 */
    public static final int PLAYER_INVENTORY_HEIGHT = 4 * SLOT + SECTION_GAP;
    /** 窗口左侧配置按钮（GTM 配置面板）的方块边长。 */
    public static final int SIDE_TAB = 24;
    /** 窗口顶部页面标签：宽、高（未选中时），选中时再向上高出 {@link #PAGE_TAB_RAISE}、向下伸进窗口顶边 {@link #PAGE_TAB_OVERLAP}。 */
    public static final int PAGE_TAB_WIDTH = 24;
    public static final int PAGE_TAB_HEIGHT = 22;
    public static final int PAGE_TAB_RAISE = 2;
    public static final int PAGE_TAB_OVERLAP = 3;
    /** 标签太多放不下时，标签宽度最窄压到这里。 */
    public static final int PAGE_TAB_MIN_WIDTH = 20;

    // ==================== 弹出面板 ====================
    /** 弹出面板与主窗口的水平间隔。 */
    public static final int POPUP_GAP = 4;
    /** 弹出面板内边距（Ore 面板标准）：四周 5，底部 7。 */
    public static final int POPUP_PADDING = 5;
    public static final int POPUP_PADDING_BOTTOM = 7;
    /** 弹出面板与屏幕边缘保持的最小距离；面板高度上限 = 屏幕高度 - 2 × 该值，与主窗口高度无关。 */
    public static final int POPUP_SCREEN_MARGIN = 4;
    /** 弹出面板内容宽度：一个装满 9 槽的区块（162 + 区块左右内边距）。 */
    public static final int POPUP_CONTENT_WIDTH = SLOT_ROW_WIDTH + 2 * UITheme.PANEL_PADDING;
}
