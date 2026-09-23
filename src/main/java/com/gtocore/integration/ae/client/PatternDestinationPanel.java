package com.gtocore.integration.ae.client;

import com.gtocore.client.Message;
import com.gtocore.integration.ae.hooks.IExtendedPatternEncodingTerm;
import com.gtocore.integration.jech.PinYinUtils;

import com.gtolib.api.ae2.gui.BlitterHelper;

import net.minecraft.ChatFormatting;
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
 * 外观沿用 ME 二合一终端的面板（me_panel 贴图、扳手拖动标记、右下角缩放角），可拖动、可调整大小，位置与大小在本次游戏内记忆。
 * 面板整体绘制在前景层并抬高 z，盖住终端的物品槽；鼠标与键盘事件由 {@code PatternEncodingTermScreenMixin} 优先转交给本面板。
 * <p>
 * 搜索同时匹配供应器名称与供应器内已有样板的产物；因产物命中的行会在右侧标注命中的产物。
 */
public class PatternDestinationPanel implements ICompositeWidget {

    private static final Blitter PANEL = Blitter.texture("guis/me_panel.png");
    private static final Blitter HEADER_L = PANEL.copy().src(0, 0, 11, 31);
    private static final Blitter HEADER_M = PANEL.copy().src(11, 0, 81, 31);
    private static final Blitter HEADER_R = PANEL.copy().src(92, 0, 9, 31);
    private static final Blitter MARGIN_L = PANEL.copy().src(0, 31, 11, 72);
    private static final Blitter MARGIN_R = PANEL.copy().src(92, 31, 9, 72);
    private static final Blitter SCROLL_U = PANEL.copy().src(11, 31, 9, 4);
    private static final Blitter SCROLL_M = PANEL.copy().src(11, 35, 9, 64);
    private static final Blitter SCROLL_D = PANEL.copy().src(11, 99, 9, 4);
    private static final Blitter FOOTER_L = PANEL.copy().src(0, 103, 11, 9);
    private static final Blitter FOOTER_M = PANEL.copy().src(11, 103, 81, 9);
    private static final Blitter FOOTER_R = PANEL.copy().src(92, 103, 9, 9);
    private static final Blitter RESIZE_GRIP = Blitter.texture("guis/me2in1_resize_grip.png", 8, 8).src(0, 0, 8, 8);
    private static final Blitter DRAG_MARK = Icon.WRENCH_DISABLED.getBlitter();
    private static final Blitter DRAG_MARK_ACTIVE = Icon.WRENCH.getBlitter();
    private static final Blitter CLOSE = Icon.CLEAR.getBlitter();
    private static final Blitter TOGGLE_BACKGROUND = Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter();
    private static final Blitter MATCH_OUTPUTS_ON = Icon.PATTERN_ACCESS_SHOW.getBlitter();
    private static final Blitter MATCH_OUTPUTS_OFF = Icon.PATTERN_ACCESS_HIDE.getBlitter();

    private static final int HEADER_H = 31;
    private static final int FOOTER_H = 9;
    private static final int LIST_X = 20;
    private static final int RIGHT_MARGIN = 9;
    private static final int ROW_H = 18;
    private static final int SEARCH_X = 11;
    private static final int SEARCH_Y = 17;
    private static final int SEARCH_H = 12;
    private static final int SEARCH_PAD_X = 2;
    private static final int SEARCH_PAD_Y = 2;
    private static final Blitter TEXT_FIELD = Blitter.texture("guis/text_field.png", 128, 128);
    private static final int CLOSE_SIZE = 10;
    private static final int RESIZE_HANDLE = 12;
    private static final int MIN_ROWS = 3;
    private static final int MAX_ROWS = 16;
    private static final int MIN_WIDTH = 140;
    private static final int MAX_WIDTH = 420;
    private static final int TEXT_COLOR = 0x404040;
    private static final int MATCH_COLOR = 0xFFFF55;
    private static final int PROVIDER_MATCH_COLOR = 0x55FF55;
    private static final int MACHINE_MATCH_COLOR = 0x7FD7FF;
    private static final int TOGGLE_SIZE = 16;
    // 改名后两行名称的字号缩放
    private static final float SMALL_TEXT_SCALE = 0.75f;
    private static final int TOOLTIP_MATCH_LINES = 8;
    // 盖过物品槽（物品约 z 150，数量文字 z 200），但低于 tooltip（z 400）
    private static final float Z = 250;
    // 面板内图标由 renderItem 自带 +150，先回退，避免盖住 tooltip
    private static final float ICON_Z = Z - 150 + 8;

    // 本次游戏内记忆的位置与大小；位置相对终端界面左上角
    private static boolean positioned = false;
    private static int savedX;
    private static int savedY;
    private static int savedRows = 6;
    private static int savedWidth = 0; // 0 表示按内容自动宽度
    // 搜索是否也匹配目的地已有样板的产物；默认只匹配名称
    private static boolean matchOutputs = false;

    private final AEBaseScreen<?> screen;
    private final IExtendedPatternEncodingTerm term;
    // 不用 AETextField：它的背景贴图中段最多 126px，拉宽后右侧会露出断层；这里改为无边框 EditBox + 平铺同一张贴图
    private final EditBox searchField;
    private final int placeholderColor;
    private final Scrollbar scrollbar;
    private final List<Entry> entries = new ArrayList<>();
    private final IntArrayList visible = new IntArrayList();

    private boolean shown = false;
    private int x;
    private int y;
    private int autoWidth = MIN_WIDTH;

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
        searchField.setResponder(value -> updateSearch());
        scrollbar = new Scrollbar(Scrollbar.SMALL);
        scrollbar.setCaptureMouseWheel(false);
    }

    public void open(Message.PatternDestination[] destinations) {
        entries.clear();
        for (int i = 0; i < destinations.length; i++) {
            entries.add(new Entry(i, destinations[i]));
        }
        searchField.setValue("");
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

    /**
     * 拖动、缩放或拖动滚动条期间，需要接收面板外的鼠标事件。
     */
    public boolean isCapturingMouse() {
        return shown && (dragging || resizing || scrolling);
    }

    private int getRows() {
        return savedRows;
    }

    private int getWidth() {
        return savedWidth > 0 ? savedWidth : autoWidth;
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
        var query = searchField.getValue().trim().toLowerCase(Locale.ROOT);
        visible.clear();
        var font = Minecraft.getInstance().font;
        int widest = 0;
        for (int i = 0, size = entries.size(); i < size; i++) {
            var entry = entries.get(i);
            entry.matched.clear();
            entry.providerNameMatched = false;
            entry.machineNameMatched = false;
            if (!query.isEmpty()) {
                entry.providerNameMatched = entry.customSearchName != null && PinYinUtils.match(entry.customSearchName, query);
                entry.machineNameMatched = PinYinUtils.match(entry.machineSearchName, query);
                if (matchOutputs) {
                    var names = entry.outputSearchNames();
                    for (int j = 0; j < names.length; j++) {
                        if (PinYinUtils.match(names[j], query)) entry.matched.add(j);
                    }
                }
            }
            if (query.isEmpty() || entry.providerNameMatched || entry.machineNameMatched || !entry.matched.isEmpty()) {
                visible.add(i);
                widest = Math.max(widest, entry.preferredWidth(font));
            }
        }
        autoWidth = Mth.clamp(LIST_X + RIGHT_MARGIN + widest, MIN_WIDTH, MAX_WIDTH);
        updateScrollRange();
    }

    private void updateScrollRange() {
        scrollbar.setRange(0, Math.max(0, visible.size() - getRows()), 1);
    }

    // ---------------------------------------------------------------- 布局

    @Override
    public void updateBeforeRender() {
        if (!shown) return;
        // 防止面板被拖出窗口
        int maxX = screen.width - screen.getGuiLeft() - getWidth();
        int maxY = screen.height - screen.getGuiTop() - getHeight();
        x = Math.max(-screen.getGuiLeft(), Math.min(x, maxX));
        y = Math.max(-screen.getGuiTop(), Math.min(y, maxY));

        int absX = x + screen.getGuiLeft();
        int absY = y + screen.getGuiTop();
        searchField.setX(absX + SEARCH_X + SEARCH_PAD_X);
        searchField.setY(absY + SEARCH_Y + SEARCH_PAD_Y);
        searchField.setWidth(getSearchWidth() - SEARCH_PAD_X * 2);
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
        int listHeight = rows * ROW_H;
        var helper = BlitterHelper.of(guiGraphics);
        helper.hBlit(HEADER_L, HEADER_M, HEADER_R, x, y, width, HEADER_H);
        int bodyY = y + HEADER_H;
        MARGIN_L.copy().dest(x, bodyY, MARGIN_L.getSrcWidth(), listHeight).blit(guiGraphics);
        MARGIN_R.copy().dest(x + width - RIGHT_MARGIN, bodyY, RIGHT_MARGIN, listHeight).blit(guiGraphics);
        helper.vBlit(SCROLL_U, SCROLL_M, SCROLL_D, x + MARGIN_L.getSrcWidth(), bodyY, SCROLL_U.getSrcWidth(), listHeight);
        helper.hBlit(FOOTER_L, FOOTER_M, FOOTER_R, x, bodyY + listHeight, width, FOOTER_H);

        // 标题栏：拖动标记、标题、产物匹配开关、关闭按钮
        (dragging ? DRAG_MARK_ACTIVE : DRAG_MARK).copy().dest(x + 3, y + 1).blit(guiGraphics);
        var title = Component.translatable("gtocore.ae.appeng.craft.encode_send.title");
        guiGraphics.drawString(font, ellipsize(font, title.getString(), getToggleX() - 4 - (x + 21)), x + 21, y + 5, TEXT_COLOR, false);
        int toggleX = getToggleX();
        int toggleY = getToggleY();
        TOGGLE_BACKGROUND.copy().dest(toggleX, toggleY).blit(guiGraphics);
        (matchOutputs ? MATCH_OUTPUTS_ON : MATCH_OUTPUTS_OFF).copy().dest(toggleX, toggleY).blit(guiGraphics);
        if (inRect(mouse, toggleX, toggleY, TOGGLE_SIZE, TOGGLE_SIZE)) {
            guiGraphics.fill(toggleX, toggleY, toggleX + TOGGLE_SIZE, toggleY + TOGGLE_SIZE, 0x30FFFFFF);
        }
        int closeX = getCloseX();
        int closeY = getCloseY();
        if (inRect(mouse, closeX, closeY, CLOSE_SIZE, CLOSE_SIZE)) {
            guiGraphics.fill(closeX - 1, closeY - 1, closeX + CLOSE_SIZE + 1, closeY + CLOSE_SIZE + 1, 0x40000000);
        }
        CLOSE.copy().dest(closeX, closeY, CLOSE_SIZE, CLOSE_SIZE).blit(guiGraphics);

        // 列表
        int listX = x + LIST_X;
        int listWidth = getListWidth();
        int start = scrollbar.getCurrentScroll();
        for (int row = 0; row < rows; row++) {
            int ry = bodyY + row * ROW_H;
            int index = start + row;
            if (index >= visible.size()) {
                drawRowBackground(guiGraphics, listX, ry, listWidth, false, false);
                continue;
            }
            var entry = entries.get(visible.getInt(index));
            boolean hovered = !dragging && !resizing && inRect(mouse, listX, ry, listWidth, ROW_H);
            drawRowBackground(guiGraphics, listX, ry, listWidth, entry.full, hovered && !entry.full);
            entry.draw(guiGraphics, font, listX, ry, listWidth);
        }
        scrollbar.drawForegroundLayer(guiGraphics, bounds, mouse);

        RESIZE_GRIP.copy().dest(x + width - 8 - 2, y + getHeight() - 8 - 2).blit(guiGraphics);

        // 搜索框：用 AE 原版 text_field.png，中段平铺（AETextField 只画一段，最宽 126px）
        int sx = x + SEARCH_X;
        int sy = y + SEARCH_Y;
        int sw = getSearchWidth();
        boolean focused = searchField.isFocused();
        int v = focused ? 24 : 0;
        TEXT_FIELD.copy().src(0, v, 1, SEARCH_H).dest(sx, sy).blit(guiGraphics);
        for (int tx = 1; tx < sw - 1; tx += 126) {
            int segment = Math.min(126, sw - 1 - tx);
            TEXT_FIELD.copy().src(1, v, segment, SEARCH_H).dest(sx + tx, sy).blit(guiGraphics);
        }
        TEXT_FIELD.copy().src(127, v, 1, SEARCH_H).dest(sx + sw - 1, sy).blit(guiGraphics);
        if (!focused && searchField.getValue().isEmpty()) {
            var hint = ellipsize(font, Component.translatable(matchOutputs ? "gtocore.ae.appeng.craft.encode_send.search" :
                    "gtocore.ae.appeng.craft.encode_send.search.names").getString(), sw - SEARCH_PAD_X * 2);
            guiGraphics.drawString(font, hint, sx + SEARCH_PAD_X, sy + SEARCH_PAD_Y, placeholderColor, false);
        }

        // 输入框需要在同一层绘制，否则会被面板盖住；EditBox 使用屏幕绝对坐标
        pose.pushPose();
        pose.translate(-screen.getGuiLeft(), -screen.getGuiTop(), 0);
        searchField.render(guiGraphics, mouse.getX() + screen.getGuiLeft(), mouse.getY() + screen.getGuiTop(),
                Minecraft.getInstance().getFrameTime());
        pose.popPose();

        pose.popPose();
    }

    private static void drawRowBackground(GuiGraphics guiGraphics, int rx, int ry, int rw, boolean full, boolean hovered) {
        // 与 ME 面板物品槽一致的内凹样式：左上深色、右下白色
        guiGraphics.fill(rx, ry, rx + rw, ry + ROW_H, 0xFF373737);
        guiGraphics.fill(rx + 1, ry + 1, rx + rw, ry + ROW_H, 0xFFFFFFFF);
        guiGraphics.fill(rx + 1, ry + 1, rx + rw - 1, ry + ROW_H - 1, full ? 0xFF8B6060 : 0xFF8B8B8B);
        if (hovered) {
            guiGraphics.fill(rx + 1, ry + 1, rx + rw - 1, ry + ROW_H - 1, 0x80FFFFFF);
        }
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
        return y + 4;
    }

    private int getToggleX() {
        return getCloseX() - 4 - TOGGLE_SIZE;
    }

    private int getToggleY() {
        return y + 1;
    }

    private boolean inToggle(Point p) {
        return inRect(p, getToggleX(), getToggleY(), TOGGLE_SIZE, TOGGLE_SIZE);
    }

    private static boolean inRect(Point p, int rx, int ry, int rw, int rh) {
        return p.getX() >= rx && p.getX() < rx + rw && p.getY() >= ry && p.getY() < ry + rh;
    }

    private int getSearchWidth() {
        return getWidth() - SEARCH_X - RIGHT_MARGIN;
    }

    private boolean inSearchField(Point p) {
        return inRect(p, x + SEARCH_X, y + SEARCH_Y, getSearchWidth(), SEARCH_H);
    }

    private boolean inDragArea(Point p) {
        // 扳手标记与标题所在的一整条都可以拖动（开关与关闭按钮除外）
        return inRect(p, x, y, getWidth(), SEARCH_Y - 1) && !inToggle(p) &&
                !inRect(p, getCloseX() - 1, getCloseY() - 1, CLOSE_SIZE + 2, CLOSE_SIZE + 2);
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

    // ---------------------------------------------------------------- 输入

    @Override
    public boolean wantsAllMouseUpEvents() {
        return isCapturingMouse();
    }

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        if (!shown || !mousePos.isIn(getBounds())) {
            if (shown) searchField.setFocused(false);
            return false;
        }
        if (inSearchField(mousePos)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) searchField.setValue("");
            searchField.setFocused(true);
            // 点在内边距上也要能定位光标，把坐标夹进 EditBox 的文字区域
            double clickX = Mth.clamp(mousePos.getX() + screen.getGuiLeft(), searchField.getX(), searchField.getX() + searchField.getWidth() - 1);
            double clickY = searchField.getY();
            searchField.mouseClicked(clickX, clickY, GLFW.GLFW_MOUSE_BUTTON_LEFT);
            return true;
        }
        searchField.setFocused(false);
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return true;
        if (inRect(mousePos, getCloseX() - 1, getCloseY() - 1, CLOSE_SIZE + 2, CLOSE_SIZE + 2)) {
            close();
            return true;
        }
        if (inToggle(mousePos)) {
            matchOutputs = !matchOutputs;
            updateSearch();
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inResizeHandle(mousePos)) {
            resizing = true;
            resizeStartMouseX = mousePos.getX();
            resizeStartMouseY = mousePos.getY();
            resizeStartWidth = getWidth();
            resizeStartRows = getRows();
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && inDragArea(mousePos)) {
            dragging = true;
            dragOffsetX = mousePos.getX() - x;
            dragOffsetY = mousePos.getY() - y;
            return true;
        }
        if (mousePos.isIn(scrollbar.getBounds())) {
            scrolling = scrollbar.onMouseDown(mousePos, button);
            return true;
        }
        pressedRow = rowAt(mousePos);
        return true;
    }

    @Override
    public boolean onMouseDrag(Point mousePos, int button) {
        if (!shown) return false;
        if (dragging) {
            x = mousePos.getX() - dragOffsetX;
            y = mousePos.getY() - dragOffsetY;
            return true;
        }
        if (resizing) {
            savedWidth = Mth.clamp(resizeStartWidth + mousePos.getX() - resizeStartMouseX, MIN_WIDTH, MAX_WIDTH);
            savedRows = Mth.clamp(resizeStartRows + Math.round((mousePos.getY() - resizeStartMouseY) / (float) ROW_H), MIN_ROWS, MAX_ROWS);
            updateScrollRange();
            return true;
        }
        if (scrolling) {
            scrollbar.onMouseDrag(mousePos, button);
            return true;
        }
        return mousePos.isIn(getBounds());
    }

    @Override
    public boolean onMouseUp(Point mousePos, int button) {
        if (!shown) return false;
        boolean captured = isCapturingMouse();
        if (dragging || resizing) {
            dragging = false;
            resizing = false;
            savedX = x;
            savedY = y;
            return true;
        }
        if (scrolling) {
            scrolling = false;
            scrollbar.onMouseUp(mousePos, button);
            return true;
        }
        if (!mousePos.isIn(getBounds())) return captured;
        int row = rowAt(mousePos);
        if (row >= 0 && row == pressedRow && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            var entry = entries.get(visible.getInt(row));
            if (!entry.full) {
                term.gto$getMenu().gtolib$sendPattern(entry.index);
                close();
            }
        }
        pressedRow = -1;
        return true;
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        if (!shown || !mousePos.isIn(getBounds())) return false;
        scrollbar.onMouseWheel(mousePos, delta);
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
        if (inRect(mouse, getCloseX() - 1, getCloseY() - 1, CLOSE_SIZE + 2, CLOSE_SIZE + 2)) {
            return new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.close"));
        }
        if (inToggle(mouse)) {
            return new Tooltip(
                    Component.translatable(matchOutputs ? "gtocore.ae.appeng.craft.encode_send.match_outputs.on" :
                            "gtocore.ae.appeng.craft.encode_send.match_outputs.off"),
                    Component.translatable("gtocore.ae.appeng.craft.encode_send.match_outputs.desc"));
        }
        if (inRect(mouse, x + 3, y + 1, 16, 16)) {
            return new Tooltip(Component.translatable("gtocore.ae.appeng.me2in1.draggable_mark.tooltip"));
        }
        if (inResizeHandle(mouse)) {
            return new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.resize"));
        }
        if (inSearchField(mouse)) {
            return new Tooltip(Component.translatable("gtocore.ae.appeng.craft.encode_send.search.desc"));
        }
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

    private static final class Entry {

        final int index;
        // 对接机器的图标与名称
        final AEKey icon;
        final Component name;
        // 目的地本体（样板供应器等）的图标，与机器图标相同时不重复显示
        @Nullable
        final AEKey providerIcon;
        // 目的地被普通改名时的名字；有它时名称区分两行小字显示：上为改名（亮），下为机器名（暗）
        @Nullable
        final String customName;
        final String machineName;
        @Nullable
        final String customSearchName;
        final String machineSearchName;
        final boolean full;
        final AEKey[] outputs;
        final IntArrayList matched = new IntArrayList();
        // 供应器改后的名字命中 / 对接机器名字命中
        boolean providerNameMatched;
        boolean machineNameMatched;
        String[] outputNames;
        String[] outputSearchNames;

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
            this.outputs = destination.outputs();
        }

        private static String stripFormatting(String text) {
            var plain = ChatFormatting.stripFormatting(text);
            return plain == null ? "" : plain;
        }

        private int iconsWidth() {
            return 1 + 16 + (providerIcon != null ? 17 : 0) + 3;
        }

        private int nameWidth(Font font) {
            if (customName == null) return font.width(name);
            return (int) Math.ceil(Math.max(font.width(customName), font.width(machineName)) * SMALL_TEXT_SCALE);
        }

        String[] outputSearchNames() {
            if (outputSearchNames == null) {
                outputNames = new String[outputs.length];
                outputSearchNames = new String[outputs.length];
                for (int i = 0; i < outputs.length; i++) {
                    // 部分物品名自带 § 颜色码（如「§7LV§r输入总线」），会盖掉标注颜色，也会干扰搜索
                    var plain = ChatFormatting.stripFormatting(outputs[i].getDisplayName().getString());
                    outputNames[i] = plain == null ? "" : plain;
                    outputSearchNames[i] = outputNames[i].toLowerCase(Locale.ROOT);
                }
            }
            return outputSearchNames;
        }

        private String matchSuffix() {
            return matched.size() > 1 ? " (+" + (matched.size() - 1) + ")" : "";
        }

        private int fullWidth(Font font) {
            return full ? font.width(Component.translatable("gtocore.ae.appeng.craft.encode_send.full")) + 4 : 0;
        }

        private static String providerMatchLabel() {
            return Component.translatable("gtocore.ae.appeng.craft.encode_send.match_provider").getString();
        }

        private static String machineMatchLabel() {
            return Component.translatable("gtocore.ae.appeng.craft.encode_send.match_machine").getString();
        }

        /**
         * 名称类命中标签（供应器名称匹配、机器名称匹配）占用的总宽度，含间隔。
         */
        private int nameLabelsWidth(Font font) {
            int width = 0;
            if (providerNameMatched) width += font.width(providerMatchLabel()) + 8;
            if (machineNameMatched) width += font.width(machineMatchLabel()) + 8;
            return width;
        }

        /**
         * 不截断时一行所需的宽度（不含左右边框外的面板边距）。
         */
        int preferredWidth(Font font) {
            int width = iconsWidth() + nameWidth(font) + 4 + fullWidth(font) + nameLabelsWidth(font);
            if (!matched.isEmpty()) {
                width += 8 + font.width(Component.translatable("gtocore.ae.appeng.craft.encode_send.match")) +
                        16 + 1 + font.width(outputNames[matched.getInt(0)] + matchSuffix());
            }
            return width;
        }

        void draw(GuiGraphics guiGraphics, Font font, int rx, int ry, int rw) {
            int textY = ry + 5;
            int right = rx + rw - 3;
            if (full) {
                var fullText = Component.translatable("gtocore.ae.appeng.craft.encode_send.full");
                right -= font.width(fullText);
                guiGraphics.drawString(font, fullText, right, textY, 0xFF5555);
                right -= 4;
            }
            int nameX = rx + iconsWidth();
            // 右侧标注命中原因，从左到右：供应器名称匹配、机器名称匹配、产物匹配；同时命中时都显示，名称类标签始终保留位置
            int nameLabelWidth = nameLabelsWidth(font);
            if (!matched.isEmpty()) {
                // 标注：产物匹配：[图标] 铜转子 (+2)
                var prefix = Component.translatable("gtocore.ae.appeng.craft.encode_send.match").getString();
                var suffix = matchSuffix();
                int fixedWidth = font.width(prefix) + 16 + 1 + font.width(suffix);
                // 供应器名称至少保留 40 像素，再扣掉名称匹配标签，剩下的留给命中产物名
                int outputNameWidth = Math.max(0, Math.min(font.width(outputNames[matched.getInt(0)]),
                        right - nameX - 40 - 8 - nameLabelWidth - fixedWidth));
                var outputName = ellipsize(font, outputNames[matched.getInt(0)], outputNameWidth);
                int matchWidth = fixedWidth + font.width(outputName);
                int mx = right - matchWidth;
                guiGraphics.drawString(font, prefix, mx, textY, MATCH_COLOR);
                mx += font.width(prefix);
                drawIcon(guiGraphics, mx, ry + 1, outputs[matched.getInt(0)]);
                mx += 17;
                guiGraphics.drawString(font, outputName + suffix, mx, textY, MATCH_COLOR);
                right -= matchWidth + 8;
            }
            if (machineNameMatched) {
                var label = machineMatchLabel();
                right -= font.width(label);
                guiGraphics.drawString(font, label, right, textY, MACHINE_MATCH_COLOR);
                right -= 8;
            }
            if (providerNameMatched) {
                var label = providerMatchLabel();
                right -= font.width(label);
                guiGraphics.drawString(font, label, right, textY, PROVIDER_MATCH_COLOR);
                right -= 8;
            }
            // 左侧：样板供应器图标 → 对接机器图标
            int iconX = rx + 1;
            if (providerIcon != null) {
                drawIcon(guiGraphics, iconX, ry + 1, providerIcon);
                iconX += 17;
            }
            if (icon != null) drawIcon(guiGraphics, iconX, ry + 1, icon);
            if (customName == null) {
                guiGraphics.drawString(font, ellipsize(font, name.getString(), right - nameX), nameX, textY, full ? 0xAAAAAA : 0xFFFFFF);
                return;
            }
            // 改过名：两行小字，上为改名（亮），下为机器名（暗），体现主附关系
            int scaledWidth = (int) ((right - nameX) / SMALL_TEXT_SCALE);
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(nameX, ry + 2, 0);
            pose.scale(SMALL_TEXT_SCALE, SMALL_TEXT_SCALE, 1);
            guiGraphics.drawString(font, ellipsize(font, customName, scaledWidth), 0, 0, full ? 0xAAAAAA : 0xFFFFFF);
            guiGraphics.drawString(font, ellipsize(font, machineName, scaledWidth), 0, 10, full ? 0x707070 : 0xA0A0A0);
            pose.popPose();
        }

        private static void drawIcon(GuiGraphics guiGraphics, int ix, int iy, AEKey key) {
            var pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(0, 0, ICON_Z - Z);
            AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, ix, iy, key);
            pose.popPose();
        }

        Tooltip tooltip() {
            var lines = new ArrayList<Component>(5 + Math.min(matched.size(), TOOLTIP_MATCH_LINES));
            if (customName != null) {
                lines.add(Component.literal(customName));
                lines.add(Component.literal(machineName).withStyle(ChatFormatting.GRAY));
            } else {
                lines.add(name);
            }
            lines.add(Component.translatable(full ? "gtocore.ae.appeng.craft.encode_send.full.desc" : "gtocore.ae.appeng.craft.encode_send.desc"));
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
            return new Tooltip(lines);
        }
    }
}
