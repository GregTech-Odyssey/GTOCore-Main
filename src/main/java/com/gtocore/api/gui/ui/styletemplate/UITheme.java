package com.gtocore.api.gui.ui.styletemplate;

import com.gtolib.GTOCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

/**
 * 元素的统一外观：形状采用 LDLib2 的 Ore UI（{@link OreSprites}），颜色向原版容器看齐。
 * <ul>
 * <li>窗口、弹出面板：{@link OreSprites#BORDER_7_BRIGHT}（Ore 的圆角厚边，底色换成原版容器的 #C6C6C6）。</li>
 * <li>区块面板：{@link OreSprites#GROOVE}（1 像素灰边、略深的底，像浅浅下凹的区域）。</li>
 * <li>物品槽、滚动条轨道：{@link OreSprites#SLOT_BRIGHT}（原版物品槽配色）；流体槽 {@link OreSprites#FLUID_SLOT} 底色更深，与物品槽区分。</li>
 * <li>按钮：Ore 按钮形状，乘色压暗到约 #9C9C9C（比底色深，和原版一致），悬停/按下再深一档；
 * 绿色为确认、红色为危险操作；深色字无阴影。</li>
 * <li>输入框、数值框：深色框 {@link #INSET}（{@link OreSprites#RECT}），获得焦点时叠白框，白字。</li>
 * <li>文字 #202020：比原版的 #404040 深一些，抗锯齿字体下不发虚。</li>
 * </ul>
 * 配色原则：与原版和 GTM 界面并排也舒服——大面积底色 #C6C6C6、槽 #8B8B8B；
 * 不要出现比底色更亮的大块（Ore 原配色的按钮面、双层边框的高光线都会显得发白），也不要比底色暗得多的大块底色。
 * 所有颜色、贴图都从这里取，元素内部不要写死颜色。
 */
public final class UITheme {

    private UITheme() {}

    /** 正文、标题。 */
    public static final int TEXT = 0xFF202020;
    /** 次要说明文字。 */
    public static final int TEXT_SECONDARY = 0xFF555555;
    /** 区块面板（{@link #PANEL}）里的文字。 */
    public static final int PANEL_TEXT = 0xFF202020;
    /** 深色框里的文字。 */
    public static final int FIELD_TEXT = OreSprites.TEXT_LIGHT;
    /** 深色框里的占位提示文字。 */
    public static final int PLACEHOLDER_TEXT = 0xFF8A8A8A;

    /** 物品槽底图。 */
    public static final IGuiTexture ITEM_SLOT = OreSprites.SLOT_BRIGHT;
    /** 流体槽底图。 */
    public static final IGuiTexture FLUID_SLOT = OreSprites.FLUID_SLOT;
    /** 区块面板底图。 */
    public static final IGuiTexture PANEL = OreSprites.GROOVE;
    /**
     * 状态显示面板（{@code StatusPanel}）的"显示窗"：下凹斜面（左上暗、右下白），底色比区块面板略深。
     * 区块面板是平的细边框，二者并排时一眼能分出"状态"和"设置"。
     */
    public static final IGuiTexture STATUS_PANEL = new OreSprites.Bevel(0xFF7A7A7A, 0xFFFFFFFF, 0xFFB5B5B5);
    /** 输入框、数值框的深色框。 */
    public static final IGuiTexture INSET = OreSprites.RECT;
    /** 滚动条轨道、滑块。 */
    public static final IGuiTexture SCROLL_TRACK = OreSprites.SLOT_BRIGHT;
    public static final IGuiTexture SCROLL_THUMB = OreSprites.BTN_DEFAULT.tinted(OreSprites.BUTTON_TINT);
    /** 机器窗口、弹出面板的外框（里面的字用 {@link #TEXT}）。 */
    public static final IGuiTexture WINDOW = OreSprites.BORDER_7_BRIGHT;
    /**
     * 信息图标图集（{@code info_icons.png}，4 个 18×18 横排）：说明（蓝 i，与 GTM 标题栏机器说明图标逐像素相同）、
     * 警告（黄 !）、错误（红 ×）、成功（绿 ✓）；后三个沿用同一圆形的描边、高光与颗粒，只换颜色和符号。
     */
    private static final ResourceTexture INFO_ICONS = new ResourceTexture(GTOCore.id("textures/gui/info_icons.png"));
    private static final int INFO_ICON_COUNT = 4;

    /** 第 {@code index} 个信息图标（顺序见 {@link #INFO_ICONS}）。 */
    public static IGuiTexture infoIcon(int index) {
        return INFO_ICONS.getSubTexture((double) index / INFO_ICON_COUNT, 0, 1.0 / INFO_ICON_COUNT, 1);
    }

    /** 窗口底色，与 {@link #WINDOW} 的内部一致（标签页与窗口接缝处涂它）。 */
    public static final int WINDOW_FILL = OreSprites.WINDOW_FILL;
    /**
     * 窗口顶部页面标签：与窗口同一套 Ore 边框。选中的标签用窗口底色、向下伸进窗口顶边与窗口连成一体；
     * 未选中的底色暗一档、坐在窗口顶边上，悬停时介于两者之间。
     */
    public static final IGuiTexture PAGE_TAB_SELECTED = OreSprites.BORDER_7_BRIGHT;
    public static final IGuiTexture PAGE_TAB_HOVER = new OreSprites.Refilled(OreSprites.BORDER_7, 0xFFB9B9B9, 2, 2, 2, 4);
    public static final IGuiTexture PAGE_TAB = new OreSprites.Refilled(OreSprites.BORDER_7, 0xFFA8A8A8, 2, 2, 2, 4);
    /** 窗口左侧配置按钮（GTM 配置面板）的底图，下移 1 像素，让居中摆放的图标落在面板中央。 */
    public static final IGuiTexture CONFIGURATOR_TAB = new OreSprites.Shifted(OreSprites.BORDER_7_BRIGHT, 0, 1);
    /** 区块内边距：让开 1 像素细边后留 2 像素空白。 */
    public static final int PANEL_PADDING = 3;
    public static final int PANEL_PADDING_BOTTOM = 3;

    /** 按钮的底部台阶高度：文字/图标要画在台阶以上的区域。 */
    public static final int BUTTON_LIP = 2;

    /** 状态指示色（指示灯等）：在线、正常。 */
    public static final int STATUS_ONLINE = 0xFF55DD55;
    /** 状态指示色：离线、失败。 */
    public static final int STATUS_OFFLINE = 0xFFDD4444;
    /** 状态指示色：需要注意（如权限不足、配置有误）。 */
    public static final int STATUS_WARNING = 0xFFE8B830;
    /** 状态行里浅底上的文字色：正常、注意、错误（比指示灯色深，保证在 #B5B5B5 显示窗上看得清）。 */
    public static final int STATUS_TEXT_GOOD = 0xFF2E7D1E;
    public static final int STATUS_TEXT_WARNING = 0xFF8C5A00;
    public static final int STATUS_TEXT_ERROR = 0xFFA81C1C;
    /** 状态行小灯的描边。 */
    public static final int STATUS_LAMP_OUTLINE = 0xFF373737;

    /** 选中框（{@link #drawSelection}）：金色亮边、深色描边、内部呼吸光的透明度范围与周期。 */
    public static final int SELECTION_COLOR = 0xFFFFC83D;
    /** 描边与原版物品槽的深色边同色：网格里它与邻格的深色线一致，任何一边看起来都一样。 */
    public static final int SELECTION_OUTLINE = 0xFF373737;
    private static final int SELECTION_FILL_ALPHA_MIN = 0x08;
    private static final int SELECTION_FILL_ALPHA_MAX = 0x28;
    private static final long SELECTION_PULSE_MS = 1600;

    /** 按钮配色。 */
    public enum ButtonVariant {

        /** 普通操作。 */
        DEFAULT(OreSprites.BTN_DEFAULT.tinted(OreSprites.BUTTON_TINT), OreSprites.BTN_PRESSED.tinted(OreSprites.BUTTON_TINT), OreSprites.TEXT_DARK),
        /** 确认、开启类操作。 */
        CONFIRM(OreSprites.BTN_DEFAULT_GREEN, OreSprites.BTN_PRESSED_GREEN, OreSprites.TEXT_LIGHT),
        /** 清除、删除等危险操作。 */
        DANGER(OreSprites.BTN_DEFAULT_RED, OreSprites.BTN_PRESSED_RED, OreSprites.TEXT_LIGHT);

        private final OreSprites.Sprite base;
        private final OreSprites.Sprite hover;
        private final int textColor;

        ButtonVariant(OreSprites.Sprite base, OreSprites.Sprite hover, int textColor) {
            this.base = base;
            this.hover = hover;
            this.textColor = textColor;
        }

        public int textColor(boolean enabled) {
            return enabled ? textColor : OreSprites.TEXT_DISABLED;
        }
    }

    /** 按钮底图：悬停换深一档，按下再叠一层灰，禁用统一灰色。 */
    @OnlyIn(Dist.CLIENT)
    public static void drawButton(GuiGraphics graphics, int x, int y, int width, int height, ButtonVariant variant,
                                  boolean hovered, boolean pressed, boolean enabled) {
        OreSprites.Sprite sprite;
        if (!enabled) sprite = OreSprites.BTN_DISABLED;
        else if (pressed) sprite = variant.hover.tinted(multiply(variant.hover.tint(), OreSprites.PRESSED_TINT));
        else sprite = hovered ? variant.hover : variant.base;
        sprite.draw(graphics, x, y, width, height);
    }

    /** 深色框；{@code focused} 时叠白框。 */
    @OnlyIn(Dist.CLIENT)
    public static void drawInset(GuiGraphics graphics, int x, int y, int width, int height, boolean focused) {
        OreSprites.RECT.draw(graphics, x, y, width, height);
        if (focused) OreSprites.WHITE_BORDER.draw(graphics, x, y, width, height);
    }

    /**
     * 统一的选中框，画在物品之上、悬浮提示之下（z = 300）：深色圆角描边 + 金色亮边，内部一层缓慢呼吸的淡金色。
     * 在前景层调用（见 {@code UIElement#drawInForeground}），否则之后才绘制的相邻元素会盖住外扩的描边。
     * <p>
     * 框比元素向外扩 1 像素：原版斜面槽排成网格时，格与格之间是"白色高光 + 深色阴影"两条线，
     * 贴着槽边画框会让上、左两边多出邻格的白线而显得更粗；外扩 1 像素后四边正好各盖住这两条线，上下左右对称。
     */
    @OnlyIn(Dist.CLIENT)
    public static void drawSelection(GuiGraphics graphics, int x, int y, int width, int height) {
        int l = x - 1, t = y - 1, r = x + width + 1, b = y + height + 1;
        double phase = (System.currentTimeMillis() % SELECTION_PULSE_MS) / (double) SELECTION_PULSE_MS;
        int alpha = (int) (SELECTION_FILL_ALPHA_MIN + (SELECTION_FILL_ALPHA_MAX - SELECTION_FILL_ALPHA_MIN) * (0.5 - 0.5 * Math.cos(phase * 2 * Math.PI)));
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 300);
        graphics.fill(l + 2, t + 2, r - 2, b - 2, alpha << 24 | (SELECTION_COLOR & 0xFFFFFF));
        ring(graphics, l, t, r, b, SELECTION_OUTLINE, true);
        ring(graphics, l + 1, t + 1, r - 1, b - 1, SELECTION_COLOR, false);
        pose.popPose();
    }

    /** 1 像素矩形边框；{@code roundCorners} 时空出四个角，呈圆角。 */
    @OnlyIn(Dist.CLIENT)
    private static void ring(GuiGraphics graphics, int l, int t, int r, int b, int color, boolean roundCorners) {
        int c = roundCorners ? 1 : 0;
        graphics.fill(l + c, t, r - c, t + 1, color);
        graphics.fill(l + c, b - 1, r - c, b, color);
        graphics.fill(l, t + 1, l + 1, b - 1, color);
        graphics.fill(r - 1, t + 1, r, b - 1, color);
    }

    /** 居中绘制单行文字，超出 {@code maxWidth} 时截断并加省略号。 */
    @OnlyIn(Dist.CLIENT)
    public static void drawCenteredText(GuiGraphics graphics, String text, int centerX, int y, int maxWidth, int color, boolean shadow) {
        var font = Minecraft.getInstance().font;
        var clipped = clip(font, text, maxWidth);
        graphics.drawString(font, clipped, centerX - font.width(clipped) / 2, y, color, shadow);
    }

    @OnlyIn(Dist.CLIENT)
    public static String clip(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("…"))) + "…";
    }

    /** 两个 ARGB 颜色相乘（-1 视为白色，即不乘色）。 */
    private static int multiply(int a, int b) {
        if (a == -1) return b;
        if (b == -1) return a;
        int r = ((a >> 16) & 0xFF) * ((b >> 16) & 0xFF) / 255;
        int g = ((a >> 8) & 0xFF) * ((b >> 8) & 0xFF) / 255;
        int bl = (a & 0xFF) * (b & 0xFF) / 255;
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }
}
