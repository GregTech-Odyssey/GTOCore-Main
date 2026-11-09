package com.gtocore.integration.ae.client;

import com.gtocore.client.Message;
import com.gtocore.integration.ae.hooks.IExtendedPatternEncodingTerm;
import com.gtocore.integration.jech.PinYinUtils;

import com.gtolib.api.ae2.gui.BlitterHelper;
import com.gtolib.api.ae2.gui.MePanelFrame;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.widgets.Scrollbar;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 右键「编码」后弹出的样板发送目的地面板。
 * <p>
 * 外观沿用 ME 二合一终端的面板（{@link MePanelFrame}），可拖动、可调整大小，位置与大小在本次游戏内记忆。
 * 面板整体绘制在前景层并抬高 z，盖住终端的物品槽。面板只以复合组件身份参与绘制、提示与排除区；
 * 鼠标与键盘事件全部由 {@code PatternEncodingTermScreenMixin} 先转交给这里的同名方法（屏幕绝对坐标）。
 * <p>
 * 搜索匹配供应器名称与对接机器名称；打开「匹配样板产物」后才向服务端请求各目的地已有样板的产物，
 * 因产物命中的行会在右侧标注命中的产物。
 */
public class PatternDestinationPanel implements ICompositeWidget {

    private static final AEKey[] NO_OUTPUTS = new AEKey[0];
    private static final Blitter CLOSE = Icon.CLEAR.getBlitter();
    private static final Blitter PANEL_FILL = Blitter.texture("guis/me_panel.png").src(21, 4, 1, 1);
    private static final Blitter MATCH_OUTPUTS_ON = Icon.PATTERN_ACCESS_SHOW.getBlitter();
    private static final Blitter MATCH_OUTPUTS_OFF = Icon.PATTERN_ACCESS_HIDE.getBlitter();
    private static final Blitter STATUS_READY = Icon.VALID.getBlitter();
    private static final Blitter STATUS_FULL = Icon.INVALID.getBlitter();
    private static final Blitter STATUS_SAME_PATTERN = Icon.COPY_MODE_ON.getBlitter();
    private static final Blitter TEXT_FIELD = Blitter.texture("guis/text_field.png", 128, 128);

    private static final int HEADER_H = 51;
    private static final int FOOTER_H = 20;
    private static final int WINDOW_MARGIN = 4;
    private static final int LIST_X = MePanelFrame.CONTENT_X;
    private static final int RIGHT_MARGIN = MePanelFrame.RIGHT_MARGIN_WIDTH;
    private static final int ROW_H = 30;
    private static final int ROW_TEXTURE_H = 18;
    private static final int SEARCH_X = 12;
    private static final int SEARCH_Y = 28;
    private static final int SEARCH_H = 16;
    private static final int SEARCH_PAD_X = 4;
    private static final int CLEAR_SEARCH_SIZE = 12;
    private static final int SEARCH_PAD_Y = 4;
    private static final int CLOSE_SIZE = 10;
    private static final int RESIZE_HANDLE = 12;
    private static final int MIN_ROWS = 3;
    private static final int MAX_ROWS = 16;
    private static final int MIN_WIDTH = 170;
    private static final int MAX_WIDTH = 420;
    private static final int DEFAULT_WIDTH = 190;
    // 行底色贴图：左框 2px、中段 1px 横向拉伸、右框 2px；整行外观由资源包决定，文字色走 screens 调色板
    private static final Blitter ROW_BG = Blitter.texture("guis/pattern_destination_row.png", 5, ROW_TEXTURE_H);
    private static final Blitter ROW_BG_LEFT = ROW_BG.copy().src(0, 0, 2, ROW_TEXTURE_H);
    private static final Blitter ROW_BG_MIDDLE = ROW_BG.copy().src(2, 0, 1, ROW_TEXTURE_H);
    private static final Blitter ROW_BG_RIGHT = ROW_BG.copy().src(3, 0, 2, ROW_TEXTURE_H);
    // 行内叠加：已满偏红、已有同样样板压暗、悬停提亮。半透明，跟随行底色
    private static final int FULL_ROW_TINT = 0x408B3030;
    private static final int GREYED_ROW_TINT = 0x48000000;
    private static final int HOVER_ROW_TINT = 0x24FFFFFF;
    // 命中高亮用固定色相，亮底主题要压深、暗底主题要提亮，两档在构造时按调色板选定
    private static final int MATCH_COLOR_DARK = 0xFFFF55;
    private static final int MATCH_COLOR_LIGHT = 0xFF8A6B00;
    private static final int PROVIDER_COLOR_DARK = 0xFF55FF55;
    private static final int PROVIDER_COLOR_LIGHT = 0xFF1F7A2C;
    private static final int MACHINE_COLOR_DARK = 0xFF7FD7FF;
    private static final int MACHINE_COLOR_LIGHT = 0xFF0E6E86;
    // 默认文本色亮于该亮度即视为暗底主题
    private static final int DARK_THEME_LUMA = 140;
    private static final int TOGGLE_SIZE = 16;
    private static final int STATUS_ICON_SIZE = 12;
    private static final int TOOLTIP_MATCH_LINES = 8;
    // 匹配样板产物时，停止输入这么久之后才重新搜索（产物可能上千个）
    private static final long SEARCH_DEBOUNCE_MS = 150;
    // 物品槽自带 +100，模型与数量分别在 z 250、300；面板仍低于 tooltip 的 z 400
    private static final float Z = 350;
    private static final float ICON_Z = Z + 8;
    private static final float ICON_DEPTH_SCALE = 1.0F / 16;

    // 本次游戏内记忆的位置与大小；位置相对终端界面左上角
    private static boolean positioned = false;
    private static int savedX;
    private static int savedY;
    private static int savedRows = 6;
    private static int savedWidth = DEFAULT_WIDTH;
    // 搜索是否也匹配目的地已有样板的产物；默认只匹配名称
    private static boolean matchOutputs = false;

    private final AEBaseScreen<?> screen;
    private final IExtendedPatternEncodingTerm term;
    // 不用 AETextField：它的背景贴图中段最多 126px，拉宽后右侧会露出断层；这里改为无边框 EditBox + 平铺同一张贴图
    private final EditBox searchField;
    private final int placeholderColor;
    // 行配色：取自终端 screens 的调色板，资源包覆盖 palette.json 即可跟随主题
    private final int titleColor;
    private final int nameColor;
    private final int subTextColor;
    private final int matchColor;
    private final int providerMatchColor;
    private final int machineMatchColor;
    private final Scrollbar scrollbar;
    private final List<Entry> entries = new ArrayList<>();
    private final IntArrayList visible = new IntArrayList();

    // 界面打开期间不变的文字，构造时取一次
    private final Ellipsized summary = new Ellipsized("");
    private final Ellipsized emptyText;
    private final Ellipsized noResultsText;
    private final Ellipsized noResultsHint;
    private final Ellipsized loadingText;
    private final String matchPrefix;
    private final String providerMatchLabel;
    private final String machineMatchLabel;
    private final Ellipsized title;
    private final Ellipsized namesHint;
    private final Ellipsized outputsHint;
    private final Tooltip closeTooltip;
    private final Tooltip clearSearchTooltip;
    private final Tooltip dragTooltip;
    private final Tooltip resizeTooltip;
    private final Tooltip searchTooltip;
    private final Tooltip matchOutputsOnTooltip;
    private final Tooltip matchOutputsOffTooltip;

    private boolean shown = false;
    private int requestId;
    private boolean outputsRequested;
    private boolean outputsLoaded;
    private boolean searchDirty;
    private long searchChangedAt;
    private int x;
    private int y;

    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;
    private boolean resizing = false;
    private int resizeStartMouseX;
    private int resizeStartMouseY;
    private int resizeStartWidth;
    private int resizeStartRows;
    private boolean scrolling = false;
    private int pressedRow = -1;

    public PatternDestinationPanel(AEBaseScreen<?> screen) {
        this.screen = screen;
        this.term = (IExtendedPatternEncodingTerm) screen;
        var font = Minecraft.getInstance().font;
        searchField = new EditBox(font, 0, 0, 100, font.lineHeight, Component.empty());
        searchField.setBordered(false);
        searchField.setMaxLength(50);
        // 与 AETextField 同一套配色
        var style = screen.getStyle();
        searchField.setTextColor(style.getColor(PaletteColor.TEXTFIELD_TEXT).toARGB());
        placeholderColor = style.getColor(PaletteColor.TEXTFIELD_PLACEHOLDER).toARGB();
        titleColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        nameColor = titleColor;
        // 弱化色压在浅色行底上对比不足（深色主题无此问题），亮底主题下退化为默认文本色
        boolean darkTheme = isDarkTheme(titleColor);
        subTextColor = darkTheme ? style.getColor(PaletteColor.MUTED_TEXT_COLOR).toARGB() : titleColor;
        matchColor = darkTheme ? MATCH_COLOR_DARK : MATCH_COLOR_LIGHT;
        providerMatchColor = darkTheme ? PROVIDER_COLOR_DARK : PROVIDER_COLOR_LIGHT;
        machineMatchColor = darkTheme ? MACHINE_COLOR_DARK : MACHINE_COLOR_LIGHT;
        searchField.setResponder(value -> {
            searchDirty = true;
            searchChangedAt = Util.getMillis();
        });
        scrollbar = new Scrollbar(Scrollbar.SMALL);
        scrollbar.setCaptureMouseWheel(false);

        emptyText = new Ellipsized(Component.translatable("gtocore.ae.appeng.craft.encode_send.empty").getString());
        noResultsText = new Ellipsized(Component.translatable("gtocore.ae.appeng.craft.encode_send.no_results").getString());
        noResultsHint = new Ellipsized(Component.translatable("gtocore.ae.appeng.craft.encode_send.no_results.hint").getString());
        loadingText = new Ellipsized(Component.translatable("gtocore.ae.appeng.craft.encode_send.loading").getString());
        matchPrefix = Component.translatable("gtocore.ae.appeng.craft.encode_send.match").getString();
        providerMatchLabel = Component.translatable("gtocore.ae.appeng.craft.encode_send.match_provider.short").getString();
        machineMatchLabel = Component.translatable("gtocore.ae.appeng.craft.encode_send.match_machine.short").getString();
        title = new Ellipsized(Component.translatable("gtocore.ae.appeng.craft.encode_send.title").getString());
        namesHint = new Ellipsized(Component.translatable("gtocore.ae.appeng.craft.encode_send.search.names").getString());
        outputsHint = new Ellipsized(Component.translatable("gtocore.ae.appeng.craft.encode_send.search").getString());
        closeTooltip = new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.close"));
        clearSearchTooltip = new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.search.clear"));
        dragTooltip = new Tooltip(Component.translatable("gtocore.ae.appeng.me2in1.draggable_mark.tooltip"));
        resizeTooltip = new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.resize"));
        searchTooltip = new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.search.desc"));
        var matchOutputsDesc = Component.translatable("gtocore.ae.appeng.craft.encode_send.match_outputs.desc");
        matchOutputsOnTooltip = new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.match_outputs.on"), matchOutputsDesc);
        matchOutputsOffTooltip = new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.match_outputs.off"), matchOutputsDesc);
    }

    /**
     * 服务端下发了新的目的地列表。
     *
     * @param requestId 列表编号，发送样板、请求产物时带回
     */
    public void open(int requestId, Message.PatternDestination[] destinations) {
        this.requestId = requestId;
        outputsRequested = false;
        outputsLoaded = false;
        entries.clear();
        for (int i = 0; i < destinations.length; i++) {
            entries.add(new Entry(i, destinations[i]));
        }
        searchField.setValue("");
        searchField.setFocused(true);
        scrollbar.setCurrentScroll(0);
        updateSearch();
        if (!positioned) {
            positioned = true;
            savedX = screen.getXSize() + 4;
            savedY = Math.max(0, screen.getYSize() - getHeight() - 20);
        }
        x = savedX;
        y = savedY;
        shown = true;
        if (matchOutputs) requestOutputs();
    }

    /**
     * 服务端按需下发了各目的地已有样板的产物；与当前列表不对应（已关闭或已换了新列表）时忽略。
     */
    public void setOutputs(int requestId, AEKey[][] outputs) {
        if (!shown || requestId != this.requestId) return;
        outputsLoaded = true;
        for (int i = 0, size = Math.min(outputs.length, entries.size()); i < size; i++) {
            entries.get(i).setOutputs(outputs[i]);
        }
        updateSearch();
    }

    private void requestOutputs() {
        if (outputsRequested) return;
        outputsRequested = true;
        term.gto$getMenu().gtolib$requestPatternOutputs(requestId);
    }

    public void close() {
        shown = false;
        dragging = false;
        resizing = false;
        scrolling = false;
        pressedRow = -1;
        searchField.setFocused(false);
        entries.clear();
        visible.clear();
    }

    @Override
    public boolean isVisible() {
        return shown;
    }

    public boolean isSearchFocused() {
        return shown && searchField.isFocused();
    }

    /**
     * 鼠标是否在面板上（屏幕绝对坐标），用于屏蔽面板下方物品槽的悬停与点击。
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!shown) return false;
        double rx = mouseX - screen.getGuiLeft();
        double ry = mouseY - screen.getGuiTop();
        return rx >= x && rx < x + getWidth() && ry >= y && ry < y + getHeight();
    }

    private int getRows() {
        int available = Math.max(1, (screen.height - HEADER_H - FOOTER_H - WINDOW_MARGIN * 2) / ROW_H);
        return Math.min(savedRows, available);
    }

    private int getWidth() {
        return Math.min(savedWidth, Math.max(1, screen.width - WINDOW_MARGIN * 2));
    }

    private int getHeight() {
        return HEADER_H + getRows() * ROW_H + FOOTER_H;
    }

    private int getListWidth() {
        return getWidth() - LIST_X - RIGHT_MARGIN;
    }

    @Override
    public void setPosition(Point position) {}

    @Override
    public void setSize(int width, int height) {}

    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, getWidth(), getHeight());
    }

    // ---------------------------------------------------------------- 搜索

    private void updateSearch() {
        searchDirty = false;
        var query = searchField.getValue().trim().toLowerCase(Locale.ROOT);
        visible.clear();
        for (int i = 0, size = entries.size(); i < size; i++) {
            var entry = entries.get(i);
            entry.match(query);
            if (query.isEmpty() || entry.providerNameMatched || entry.machineNameMatched || !entry.matched.isEmpty()) {
                visible.add(i);
            }
        }
        summary.set(Component.translatable("gtocore.ae.appeng.craft.encode_send.destinations", visible.size(), entries.size()).getString());
        updateScrollRange();
    }

    private void updateScrollRange() {
        scrollbar.setRange(0, Math.max(0, visible.size() - getRows()), 1);
    }

    // ---------------------------------------------------------------- 布局

    @Override
    public void updateBeforeRender() {
        if (!shown) return;
        // 只按名称搜索时开销很小，立即生效；匹配产物时等停止输入后再搜
        if (searchDirty && (!matchOutputs || Util.getMillis() - searchChangedAt >= SEARCH_DEBOUNCE_MS)) {
            updateSearch();
        }
        // 防止面板被拖出窗口
        int maxX = screen.width - screen.getGuiLeft() - getWidth();
        int maxY = screen.height - screen.getGuiTop() - getHeight();
        x = Math.max(WINDOW_MARGIN - screen.getGuiLeft(), Math.min(x, maxX - WINDOW_MARGIN));
        y = Math.max(WINDOW_MARGIN - screen.getGuiTop(), Math.min(y, maxY - WINDOW_MARGIN));

        int absX = x + screen.getGuiLeft();
        int absY = y + screen.getGuiTop();
        searchField.setX(absX + SEARCH_X + SEARCH_PAD_X);
        searchField.setY(absY + SEARCH_Y + SEARCH_PAD_Y);
        searchField.setWidth(Math.max(1, getSearchWidth() - SEARCH_PAD_X * 2 - CLEAR_SEARCH_SIZE));
        scrollbar.setPosition(new Point(x + 12, y + HEADER_H + 1));
        scrollbar.setHeight(getRows() * ROW_H - 2);
        updateScrollRange();
    }

    @Override
    public void tick() {
        if (shown) {
            searchField.tick();
            scrollbar.tick();
        }
    }

    // ---------------------------------------------------------------- 绘制

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        if (!shown) return;
        var font = Minecraft.getInstance().font;
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, Z);

        int width = getWidth();
        int rows = getRows();
        int bodyY = y + HEADER_H;
        MePanelFrame.draw(guiGraphics, x, y, width,
                rows * ROW_H + HEADER_H - MePanelFrame.HEADER_HEIGHT + FOOTER_H - MePanelFrame.FOOTER_HEIGHT);
        PANEL_FILL.copy().dest(x + 3, y + 3, width - 6, HEADER_H - 3).blit(guiGraphics);
        int footerY = bodyY + rows * ROW_H;
        PANEL_FILL.copy().dest(x + 3, footerY, width - 6, FOOTER_H - 3).blit(guiGraphics);

        (dragging ? MePanelFrame.DRAG_MARK_ACTIVE : MePanelFrame.DRAG_MARK).copy().dest(x + 8, y + 4).blit(guiGraphics);
        guiGraphics.drawString(font, title.get(font, getCloseX() - x - 35), x + 29, y + 9, titleColor, false);
        int closeX = getCloseX();
        int closeY = getCloseY();
        if (inClose(mouse)) {
            guiGraphics.fill(closeX - 2, closeY - 2, closeX + CLOSE_SIZE + 2, closeY + CLOSE_SIZE + 2, 0x20000000);
        }
        CLOSE.copy().dest(closeX, closeY, CLOSE_SIZE, CLOSE_SIZE).blit(guiGraphics);

        int toggleX = getToggleX();
        int toggleY = getToggleY();
        int toggleColor = matchOutputs ? providerMatchColor : subTextColor;
        guiGraphics.fill(toggleX, toggleY, toggleX + TOGGLE_SIZE, toggleY + TOGGLE_SIZE,
                (toggleColor & 0xFFFFFF) | (matchOutputs ? 0x30000000 : 0x14000000));
        if (inToggle(mouse)) {
            guiGraphics.fill(toggleX, toggleY, toggleX + TOGGLE_SIZE, toggleY + TOGGLE_SIZE, HOVER_ROW_TINT);
        }
        (matchOutputs ? MATCH_OUTPUTS_ON : MATCH_OUTPUTS_OFF).copy().dest(toggleX, toggleY, TOGGLE_SIZE, TOGGLE_SIZE).blit(guiGraphics);

        // 列表
        int listX = x + LIST_X;
        int listWidth = getListWidth();
        int start = scrollbar.getCurrentScroll();
        if (visible.isEmpty()) {
            ROW_BG.copy().src(2, 8, 1, 1).dest(listX, bodyY, listWidth, rows * ROW_H).blit(guiGraphics);
        }
        for (int row = 0; !visible.isEmpty() && row < rows; row++) {
            int ry = bodyY + row * ROW_H;
            int index = start + row;
            if (index >= visible.size()) {
                drawRowBackground(guiGraphics, listX, ry, listWidth, false, false, false);
                continue;
            }
            var entry = entries.get(visible.getInt(index));
            boolean hovered = !dragging && !resizing && inRect(mouse, listX, ry, listWidth, ROW_H);
            drawRowBackground(guiGraphics, listX, ry, listWidth, entry.full, entry.isGreyed(), hovered && !entry.full);
            entry.draw(guiGraphics, font, listX, ry, listWidth);
        }
        if (visible.isEmpty()) {
            int centerY = bodyY + rows * ROW_H / 2;
            var message = entries.isEmpty() ? emptyText : noResultsText;
            var text = message.get(font, listWidth - 12);
            guiGraphics.drawString(font, text, listX + (listWidth - font.width(text)) / 2, centerY - 10, subTextColor, false);
            if (!entries.isEmpty()) {
                var hint = noResultsHint.get(font, listWidth - 12);
                guiGraphics.drawString(font, hint, listX + (listWidth - font.width(hint)) / 2, centerY + 4, subTextColor, false);
            }
        }
        scrollbar.drawForegroundLayer(guiGraphics, bounds, mouse);
        var footer = matchOutputs && outputsRequested && !outputsLoaded ? loadingText : summary;
        guiGraphics.drawString(font, footer.get(font, width - LIST_X - 22), x + LIST_X, footerY + 6, subTextColor, false);

        MePanelFrame.RESIZE_GRIP.copy().dest(x + width - MePanelFrame.RESIZE_GRIP_SIZE - 2,
                y + getHeight() - MePanelFrame.RESIZE_GRIP_SIZE - 2).blit(guiGraphics);

        int sx = x + SEARCH_X;
        int sy = y + SEARCH_Y;
        int sw = getSearchWidth();
        boolean focused = searchField.isFocused();
        int v = focused ? 24 : 0;
        TEXT_FIELD.copy().src(0, v, 1, 12).dest(sx, sy, 1, SEARCH_H).blit(guiGraphics);
        for (int tx = 1; tx < sw - 1; tx += 126) {
            int segment = Math.min(126, sw - 1 - tx);
            TEXT_FIELD.copy().src(1, v, segment, 12).dest(sx + tx, sy, segment, SEARCH_H).blit(guiGraphics);
        }
        TEXT_FIELD.copy().src(127, v, 1, 12).dest(sx + sw - 1, sy, 1, SEARCH_H).blit(guiGraphics);
        if (searchField.getValue().isEmpty()) {
            if (!focused) {
                var hint = (matchOutputs ? outputsHint : namesHint).get(font, searchField.getWidth());
                guiGraphics.drawString(font, hint, sx + SEARCH_PAD_X, sy + SEARCH_PAD_Y, placeholderColor, false);
            }
        } else {
            CLOSE.copy().dest(sx + sw - CLEAR_SEARCH_SIZE - 1, sy + 3, 10, 10).blit(guiGraphics);
        }

        // 输入框需要在同一层绘制，否则会被面板盖住；EditBox 使用屏幕绝对坐标
        pose.pushPose();
        pose.translate(-screen.getGuiLeft(), -screen.getGuiTop(), 0);
        searchField.render(guiGraphics, mouse.getX() + screen.getGuiLeft(), mouse.getY() + screen.getGuiTop(),
                Minecraft.getInstance().getFrameTime());
        pose.popPose();

        pose.popPose();
    }

    private static void drawRowBackground(GuiGraphics guiGraphics, int rx, int ry, int rw, boolean full, boolean greyed,
                                          boolean hovered) {
        // 与 ME 面板物品槽一致的内凹样式：底色由贴图给（资源包可换），状态只叠一层半透明色
        BlitterHelper.of(guiGraphics).hBlit(ROW_BG_LEFT, ROW_BG_MIDDLE, ROW_BG_RIGHT, rx, ry, rw, ROW_H);
        int innerLeft = rx + 1;
        int innerRight = rx + rw - 1;
        int innerBottom = ry + ROW_H - 1;
        if (full) {
            guiGraphics.fill(innerLeft, ry + 1, innerRight, innerBottom, FULL_ROW_TINT);
        } else if (greyed) {
            guiGraphics.fill(innerLeft, ry + 1, innerRight, innerBottom, GREYED_ROW_TINT);
        }
        if (hovered) {
            guiGraphics.fill(innerLeft, ry + 1, innerRight, innerBottom, HOVER_ROW_TINT);
        }
    }

    /**
     * 默认文本色偏亮说明面板是暗底主题。
     */
    private static boolean isDarkTheme(int textColor) {
        int r = textColor >> 16 & 0xFF;
        int g = textColor >> 8 & 0xFF;
        int b = textColor & 0xFF;
        return 0.2126F * r + 0.7152F * g + 0.0722F * b > DARK_THEME_LUMA;
    }

    private static String ellipsize(Font font, String text, int maxWidth) {
        if (maxWidth <= 0) return "";
        if (font.width(text) <= maxWidth) return text;
        int ellipsisWidth = font.width("...");
        if (maxWidth <= ellipsisWidth) return font.plainSubstrByWidth(text, maxWidth);
        return font.plainSubstrByWidth(text, maxWidth - ellipsisWidth) + "...";
    }

    private int getCloseX() {
        return x + getWidth() - RIGHT_MARGIN - CLOSE_SIZE;
    }

    private int getCloseY() {
        return y + 8;
    }

    private int getToggleX() {
        return x + getWidth() - RIGHT_MARGIN - TOGGLE_SIZE;
    }

    private int getToggleY() {
        return y + SEARCH_Y;
    }

    private boolean inToggle(Point p) {
        return inRect(p, getToggleX(), getToggleY(), TOGGLE_SIZE, TOGGLE_SIZE);
    }

    private boolean inClose(Point p) {
        return inRect(p, getCloseX() - 1, getCloseY() - 1, CLOSE_SIZE + 2, CLOSE_SIZE + 2);
    }

    private static boolean inRect(Point p, int rx, int ry, int rw, int rh) {
        return p.getX() >= rx && p.getX() < rx + rw && p.getY() >= ry && p.getY() < ry + rh;
    }

    private int getSearchWidth() {
        return getToggleX() - x - SEARCH_X - 6;
    }

    private boolean inClearSearch(Point p) {
        return inRect(p, x + SEARCH_X + getSearchWidth() - CLEAR_SEARCH_SIZE - 2, y + SEARCH_Y + 2,
                CLEAR_SEARCH_SIZE, CLEAR_SEARCH_SIZE);
    }

    private boolean inSearchField(Point p) {
        return inRect(p, x + SEARCH_X, y + SEARCH_Y, getSearchWidth(), SEARCH_H);
    }

    private boolean inDragArea(Point p) {
        // 扳手标记与标题所在的一整条都可以拖动（开关与关闭按钮除外）
        return inRect(p, x, y, getWidth(), SEARCH_Y - 4) && !inClose(p);
    }

    private boolean inResizeHandle(Point p) {
        return inRect(p, x + getWidth() - RESIZE_HANDLE, y + getHeight() - RESIZE_HANDLE, RESIZE_HANDLE, RESIZE_HANDLE);
    }

    private int rowAt(Point p) {
        int listX = x + LIST_X;
        int bodyY = y + HEADER_H;
        if (!inRect(p, listX, bodyY, getListWidth(), getRows() * ROW_H)) return -1;
        int index = scrollbar.getCurrentScroll() + (p.getY() - bodyY) / ROW_H;
        return index < visible.size() ? index : -1;
    }

    // ---------------------------------------------------------------- 输入（屏幕绝对坐标）

    private Point toLocal(double mouseX, double mouseY) {
        return new Point((int) Math.floor(mouseX - screen.getGuiLeft()), (int) Math.floor(mouseY - screen.getGuiTop()));
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!shown) return false;
        var p = toLocal(mouseX, mouseY);
        if (!p.isIn(getBounds())) {
            searchField.setFocused(false);
            return false;
        }
        if (inSearchField(p)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || inClearSearch(p)) searchField.setValue("");
            searchField.setFocused(true);
            // 点在内边距上也要能定位光标，把坐标夹进 EditBox 的文字区域
            double clickX = Mth.clamp(mouseX, searchField.getX(), searchField.getX() + searchField.getWidth() - 1);
            searchField.mouseClicked(clickX, searchField.getY(), GLFW.GLFW_MOUSE_BUTTON_LEFT);
            return true;
        }
        searchField.setFocused(false);
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return true;
        if (inClose(p)) {
            close();
            return true;
        }
        if (inToggle(p)) {
            matchOutputs = !matchOutputs;
            if (matchOutputs) requestOutputs();
            updateSearch();
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inResizeHandle(p)) {
            resizing = true;
            resizeStartMouseX = p.getX();
            resizeStartMouseY = p.getY();
            resizeStartWidth = getWidth();
            resizeStartRows = getRows();
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inDragArea(p)) {
            dragging = true;
            dragOffsetX = p.getX() - x;
            dragOffsetY = p.getY() - y;
            return true;
        }
        if (p.isIn(scrollbar.getBounds())) {
            scrolling = scrollbar.onMouseDown(p, button);
            return true;
        }
        pressedRow = rowAt(p);
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!shown) return false;
        var p = toLocal(mouseX, mouseY);
        if (dragging) {
            x = p.getX() - dragOffsetX;
            y = p.getY() - dragOffsetY;
            return true;
        }
        if (resizing) {
            savedWidth = Mth.clamp(resizeStartWidth + p.getX() - resizeStartMouseX, MIN_WIDTH, MAX_WIDTH);
            savedRows = Mth.clamp(resizeStartRows + Math.round((p.getY() - resizeStartMouseY) / (float) ROW_H), MIN_ROWS, MAX_ROWS);
            updateScrollRange();
            return true;
        }
        if (scrolling) {
            scrollbar.onMouseDrag(p, button);
            return true;
        }
        return p.isIn(getBounds());
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!shown) return false;
        var p = toLocal(mouseX, mouseY);
        if (dragging || resizing) {
            dragging = false;
            resizing = false;
            savedX = x;
            savedY = y;
            return true;
        }
        if (scrolling) {
            scrolling = false;
            scrollbar.onMouseUp(p, button);
            return true;
        }
        int pressed = pressedRow;
        pressedRow = -1;
        if (!p.isIn(getBounds())) return false;
        int row = rowAt(p);
        if (row >= 0 && row == pressed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            var entry = entries.get(visible.getInt(row));
            if (!entry.full) {
                term.gto$getMenu().gtolib$sendPattern(requestId, entry.index);
                close();
            }
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!shown) return false;
        var p = toLocal(mouseX, mouseY);
        if (!p.isIn(getBounds())) return false;
        scrollbar.onMouseWheel(p, delta);
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!shown) return false;
        if (!searchField.isFocused()) {
            // Esc 先关面板，再按一次才关终端
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                close();
                return true;
            }
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            searchField.setFocused(false);
            return true;
        }
        searchField.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!isSearchFocused()) return false;
        searchField.charTyped(codePoint, modifiers);
        return true;
    }

    // ---------------------------------------------------------------- 提示

    @Override
    public @Nullable Tooltip getTooltip(int mouseX, int mouseY) {
        if (!shown || dragging || resizing) return null;
        var mouse = new Point(mouseX, mouseY);
        if (inClose(mouse)) return closeTooltip;
        if (inToggle(mouse)) return matchOutputs ? matchOutputsOnTooltip : matchOutputsOffTooltip;
        if (inDragArea(mouse)) return dragTooltip;
        if (inResizeHandle(mouse)) return resizeTooltip;
        if (inClearSearch(mouse) && !searchField.getValue().isEmpty()) return clearSearchTooltip;
        if (inSearchField(mouse)) return searchTooltip;
        int row = rowAt(mouse);
        if (row < 0) return null;
        return entries.get(visible.getInt(row)).tooltip();
    }

    @Override
    public void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
        if (!shown) return;
        exclusionZones.add(new Rect2i(x + screen.getGuiLeft(), y + screen.getGuiTop(), getWidth(), getHeight()));
    }

    // ---------------------------------------------------------------- 条目

    /**
     * 按可用宽度截断并缓存结果的文字，避免每帧重复截断、拼接。
     */
    private static final class Ellipsized {

        private String text;
        private int width = -1;
        private String result = "";

        Ellipsized(String text) {
            this.text = text;
        }

        void set(String text) {
            if (!text.equals(this.text)) {
                this.text = text;
                width = -1;
            }
        }

        String get(Font font, int maxWidth) {
            if (maxWidth != width) {
                width = maxWidth;
                result = ellipsize(font, text, maxWidth);
            }
            return result;
        }
    }

    private final class Entry {

        final int index;
        // 对接机器的图标与名称
        final AEKey icon;
        final Component name;
        // 目的地本体（样板供应器等）的图标，与机器图标相同时不重复显示
        @Nullable
        final AEKey providerIcon;
        // 改名作为主标题，机器名放在第二行说明中。
        @Nullable
        final String customName;
        final String machineName;
        @Nullable
        final String customSearchName;
        final String machineSearchName;
        final boolean full;
        // 已有相同样板：置灰显示，点击仍会发送
        final boolean hasSamePattern;
        final Ellipsized nameText;
        final Blitter statusIcon;
        final String secondaryName;
        // 已有样板的产物，打开「匹配样板产物」后由服务端按需下发
        AEKey[] outputs = NO_OUTPUTS;
        @Nullable
        String[] outputNames;
        @Nullable
        String[] outputSearchNames;
        final IntArrayList matched = new IntArrayList();
        // 供应器改后的名字命中 / 对接机器名字命中
        boolean providerNameMatched;
        boolean machineNameMatched;
        // 第二行的命中原因与机器说明只在搜索结果变化时重建。
        final Ellipsized detailText = new Ellipsized("");
        String matchSuffix = "";
        @Nullable
        Tooltip tooltip;

        Entry(int index, Message.PatternDestination destination) {
            this.index = index;
            this.icon = destination.group().icon();
            this.name = destination.group().name();
            var provider = destination.providerIcon();
            this.providerIcon = provider != null && !provider.equals(icon) ? provider : null;
            this.customName = destination.customName() == null ? null : stripFormatting(destination.customName().getString());
            this.machineName = stripFormatting(name.getString());
            this.customSearchName = customName == null ? null : customName.toLowerCase(Locale.ROOT);
            this.machineSearchName = machineName.toLowerCase(Locale.ROOT);
            this.full = destination.full();
            this.hasSamePattern = destination.hasSamePattern();
            // 置灰时名称去掉颜色码，否则 § 颜色会盖掉灰色
            this.nameText = new Ellipsized(customName != null ? customName : isGreyed() ? machineName : name.getString());
            this.secondaryName = customName != null ? machineName : providerIcon != null ? stripFormatting(providerIcon.getDisplayName().getString()) : "";
            this.statusIcon = full ? STATUS_FULL : hasSamePattern ? STATUS_SAME_PATTERN : STATUS_READY;
        }

        /**
         * 已满时按已满样式显示（红色、不可点），否则已有相同样板时置灰。
         */
        boolean isGreyed() {
            return hasSamePattern && !full;
        }

        private static String stripFormatting(String text) {
            var plain = ChatFormatting.stripFormatting(text);
            return plain == null ? "" : plain;
        }

        void setOutputs(AEKey[] outputs) {
            this.outputs = outputs;
            outputNames = null;
            outputSearchNames = null;
        }

        private String[] outputSearchNames() {
            if (outputSearchNames == null) {
                outputNames = new String[outputs.length];
                outputSearchNames = new String[outputs.length];
                for (int i = 0; i < outputs.length; i++) {
                    // 部分物品名自带 § 颜色码（如「§7LV§r输入总线」），会盖掉标注颜色，也会干扰搜索
                    outputNames[i] = stripFormatting(outputs[i].getDisplayName().getString());
                    outputSearchNames[i] = outputNames[i].toLowerCase(Locale.ROOT);
                }
            }
            return outputSearchNames;
        }

        void match(String query) {
            matched.clear();
            providerNameMatched = false;
            machineNameMatched = false;
            tooltip = null;
            if (!query.isEmpty()) {
                providerNameMatched = customSearchName != null && PinYinUtils.match(customSearchName, query);
                machineNameMatched = PinYinUtils.match(machineSearchName, query);
                if (matchOutputs) {
                    var names = outputSearchNames();
                    for (int j = 0; j < names.length; j++) {
                        if (PinYinUtils.match(names[j], query)) matched.add(j);
                    }
                }
            }
            if (matched.isEmpty()) {
                matchSuffix = "";
            } else {
                matchSuffix = matched.size() > 1 ? " (+" + (matched.size() - 1) + ")" : "";
            }
            var detail = new StringBuilder();
            if (!matched.isEmpty()) {
                detail.append(matchPrefix).append(outputNames[matched.getInt(0)]).append(matchSuffix);
            }
            if (providerNameMatched) {
                if (!detail.isEmpty()) detail.append(" · ");
                detail.append(providerMatchLabel);
            }
            if (machineNameMatched) {
                if (!detail.isEmpty()) detail.append(" · ");
                detail.append(machineMatchLabel);
            }
            if (!secondaryName.isEmpty()) {
                if (!detail.isEmpty()) detail.append(" · ");
                detail.append(secondaryName);
            }
            detailText.set(detail.toString());
        }

        private int iconsWidth() {
            return 1 + 16 + (providerIcon != null ? 17 : 0) + 3;
        }

        void draw(GuiGraphics guiGraphics, Font font, int rx, int ry, int rw) {
            boolean greyed = isGreyed();
            int statusX = rx + rw - STATUS_ICON_SIZE - 4;
            statusIcon.copy().dest(statusX, ry + 4, STATUS_ICON_SIZE, STATUS_ICON_SIZE).blit(guiGraphics);

            int iconX = rx + 3;
            int iconY = ry + (ROW_H - 16) / 2;
            if (providerIcon != null) {
                drawIcon(guiGraphics, iconX, iconY, providerIcon);
                iconX += 17;
            }
            if (icon != null) drawIcon(guiGraphics, iconX, iconY, icon);
            int nameX = rx + iconsWidth() + 2;
            guiGraphics.drawString(font, nameText.get(font, statusX - nameX - 5), nameX, ry + 5,
                    full || greyed ? subTextColor : nameColor, false);
            int detailColor = !matched.isEmpty() ? matchColor : machineNameMatched ? machineMatchColor : providerNameMatched ? providerMatchColor : subTextColor;
            guiGraphics.drawString(font, detailText.get(font, rx + rw - nameX - 5), nameX, ry + 18, detailColor, false);
        }

        private static void drawIcon(GuiGraphics guiGraphics, int ix, int iy, AEKey key) {
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(0, 0, ICON_Z - Z);
            // 只压缩位置矩阵的深度，保留模型光照；renderItem 的 +150 与装饰的 +200 也留在 tooltip 下方
            pose.last().pose().scale(1, 1, ICON_DEPTH_SCALE);
            AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, ix, iy, key);
            pose.popPose();
        }

        Tooltip tooltip() {
            if (tooltip != null) return tooltip;
            var lines = new ArrayList<Component>(5 + Math.min(matched.size(), TOOLTIP_MATCH_LINES));
            if (customName != null) {
                lines.add(Component.literal(customName));
                lines.add(Component.literal(machineName).withStyle(ChatFormatting.GRAY));
            } else {
                lines.add(name);
            }
            lines.add(Component.translatable(full ? "gtocore.ae.appeng.craft.encode_send.full.desc" :
                    hasSamePattern ? "gtocore.ae.appeng.craft.encode_send.same_pattern" : "gtocore.ae.appeng.craft.encode_send.desc"));
            if (providerNameMatched) {
                lines.add(Component.translatable("gtocore.ae.appeng.craft.encode_send.match_provider.desc").withStyle(ChatFormatting.GREEN));
            }
            if (machineNameMatched) {
                lines.add(Component.translatable("gtocore.ae.appeng.craft.encode_send.match_machine.desc").withStyle(ChatFormatting.AQUA));
            }
            if (!matched.isEmpty()) {
                lines.add(Component.translatable("gtocore.ae.appeng.craft.encode_send.match.desc").withStyle(ChatFormatting.YELLOW));
                int shownCount = Math.min(matched.size(), TOOLTIP_MATCH_LINES);
                for (int i = 0; i < shownCount; i++) {
                    lines.add(Component.literal(" - ").append(outputs[matched.getInt(i)].getDisplayName()).withStyle(ChatFormatting.YELLOW));
                }
                if (matched.size() > shownCount) {
                    lines.add(Component.translatable("gtocore.ae.appeng.craft.encode_send.match.more", matched.size() - shownCount));
                }
            }
            return tooltip = new Tooltip(lines);
        }
    }
}
