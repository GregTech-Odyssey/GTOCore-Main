package com.gtocore.api.gui.ui.window;

import com.gtocore.api.gui.ui.IShiftClickPriority;
import com.gtocore.api.gui.ui.UIElement;
import com.gtocore.api.gui.ui.data.SyncValueHost;
import com.gtocore.api.gui.ui.elements.Button;
import com.gtocore.api.gui.ui.elements.Label;
import com.gtocore.api.gui.ui.elements.ScrollerView;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * {@link MachineWindow} 右侧的弹出面板容器。
 * <p>
 * 页面按需注册任意种面板（可以一种都没有）；每种面板（一个键）同时至多打开一个，用不同参数再次打开时原地替换；
 * 不同种的面板可以同时打开，按打开顺序从上往下排，放不下主窗口高度时另起一列往右排。
 * 没有打开任何面板时容器尺寸为 0，不占位置。
 * <p>
 * 面板状态属于这一个打开的界面（每名玩家各自独立），以服务端为准：客户端只发"请求打开/关闭"，
 * 服务端先通知客户端按同样的键和参数构建，再构建自己的那份，新控件的初始数据随后经 {@code addWidget} 的初始化通道下发。
 * 子控件顺序即打开顺序，两端执行同样的增删，下标一致。切换页面时两端各自 {@link #reset()}，不发包。
 */
final class PopupHost extends UIElement {

    private static final int OPEN_ID = SyncValueHost.ID_BASE - 3;
    private static final int CLOSE_ID = SyncValueHost.ID_BASE - 4;
    private static final int MAX_KEY_LENGTH = 64;
    // WidgetGroup 转发子控件消息、下发新子控件初始数据用的 ID，包体第一个值都是子控件下标
    private static final int CHILD_UPDATE_ID = 1;
    private static final int CHILD_INIT_ID = 2;

    private final Map<String, IntFunction<Popup>> factories = new HashMap<>();
    /** 已打开的面板，与子控件一一对应、顺序相同。 */
    private final List<OpenPopup> opened = new ArrayList<>();
    private int maxHeight = Integer.MAX_VALUE;
    /// 容器尺寸变化时通知窗口重算界面尺寸
    private final Runnable onResize;

    private record OpenPopup(String key, int argument) {}

    PopupHost(Runnable onResize) {
        this.onResize = onResize;
    }

    void register(String key, IntFunction<Popup> factory) {
        if (key.length() > MAX_KEY_LENGTH) throw new IllegalArgumentException("Popup key too long: " + key);
        factories.put(key, factory);
    }

    /** 每个面板的高度上限（客户端按屏幕高度设定，见 {@link UISizes#POPUP_SCREEN_MARGIN}；服务端不限），一列排满这个高度后另起一列。 */
    void setMaxHeight(int maxHeight) {
        if (this.maxHeight == maxHeight) return;
        this.maxHeight = maxHeight;
        for (var widget : widgets) {
            if (widget instanceof PopupPanel panel) panel.setMaxHeight(maxHeight);
        }
        recomputeLayout();
    }

    /** 本端清空注册表并关闭所有面板（切换页面时两端各自调用）。 */
    void reset() {
        factories.clear();
        opened.clear();
        clearAllWidgets();
    }

    boolean isOpen(String key, int argument) {
        int index = indexOf(key);
        return index >= 0 && opened.get(index).argument() == argument;
    }

    boolean isOpen(String key) {
        return indexOf(key) >= 0;
    }

    int argumentOf(String key) {
        int index = indexOf(key);
        return index < 0 ? -1 : opened.get(index).argument();
    }

    void open(String key, int argument) {
        if (isRemote()) {
            writeClientAction(OPEN_ID, buf -> {
                buf.writeUtf(key, MAX_KEY_LENGTH);
                buf.writeVarInt(argument);
            });
        } else {
            serverOpen(key, argument);
        }
    }

    void close(String key) {
        if (isRemote()) writeClientAction(CLOSE_ID, buf -> buf.writeUtf(key, MAX_KEY_LENGTH));
        else serverClose(key);
    }

    void closeAll() {
        for (var popup : List.copyOf(opened)) close(popup.key());
    }

    private void serverOpen(String key, int argument) {
        var popup = create(key, argument);
        if (popup == null) return;
        writeUpdateInfo(OPEN_ID, buf -> {
            buf.writeUtf(key, MAX_KEY_LENGTH);
            buf.writeVarInt(argument);
        });
        openLocal(key, argument, popup);
    }

    private void serverClose(String key) {
        if (indexOf(key) < 0) return;
        writeUpdateInfo(CLOSE_ID, buf -> buf.writeUtf(key, MAX_KEY_LENGTH));
        closeLocal(key);
    }

    /** 未注册的键或工厂拒绝该参数（如槽号越界）时返回 null。 */
    @Nullable
    private Popup create(String key, int argument) {
        var factory = factories.get(key);
        return factory == null ? null : factory.apply(argument);
    }

    /** 同一种面板已打开时原地替换（保持下标），否则追加到末尾。 */
    private void openLocal(String key, int argument, Popup popup) {
        var panel = new PopupPanel(popup, maxHeight, () -> close(key));
        int index = indexOf(key);
        if (index >= 0) {
            removeWidget(widgets.get(index));
            opened.set(index, new OpenPopup(key, argument));
            addWidget(index, panel);
        } else {
            opened.add(new OpenPopup(key, argument));
            addWidget(panel);
        }
    }

    private void closeLocal(String key) {
        int index = indexOf(key);
        if (index < 0) return;
        opened.remove(index);
        removeWidget(widgets.get(index));
    }

    private int indexOf(String key) {
        for (int i = 0; i < opened.size(); i++) {
            if (opened.get(i).key().equals(key)) return i;
        }
        return -1;
    }

    // ==================== 布局 ====================

    /** 按打开顺序从上往下排，超出 {@link #maxHeight} 另起一列；容器尺寸为所有面板的外接矩形。 */
    @Override
    protected void recomputeLayout() {
        int x = 0, y = 0, columnWidth = 0, width = 0, height = 0;
        for (var widget : widgets) {
            int w = widget.getSizeWidth(), h = widget.getSizeHeight();
            if (y > 0 && y + h > maxHeight) {
                x += columnWidth + UISizes.POPUP_GAP;
                y = 0;
                columnWidth = 0;
            }
            widget.setSelfPosition(new Position(x, y));
            columnWidth = Math.max(columnWidth, w);
            width = Math.max(width, x + w);
            height = Math.max(height, y + h);
            y += h + UISizes.SECTION_GAP;
        }
        if (getSizeWidth() != width || getSizeHeight() != height) {
            setSize(new Size(width, height));
            if (onResize != null) onResize.run();
        }
    }

    // ==================== 同步 ====================

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == OPEN_ID) {
            serverOpen(buffer.readUtf(MAX_KEY_LENGTH), buffer.readVarInt());
        } else if (id == CLOSE_ID) {
            serverClose(buffer.readUtf(MAX_KEY_LENGTH));
        } else if (id != CHILD_UPDATE_ID || hasChild(buffer)) {
            // 客户端操作的面板已被服务端关掉时丢弃
            super.handleClientAction(id, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == OPEN_ID) {
            var key = buffer.readUtf(MAX_KEY_LENGTH);
            int argument = buffer.readVarInt();
            var popup = create(key, argument);
            if (popup != null) openLocal(key, argument, popup);
            else closeLocal(key);
        } else if (id == CLOSE_ID) {
            closeLocal(buffer.readUtf(MAX_KEY_LENGTH));
        } else if ((id != CHILD_UPDATE_ID && id != CHILD_INIT_ID) || hasChild(buffer)) {
            // 客户端已切换页面、面板已清空，服务端还在为旧面板发数据时丢弃
            super.readUpdateInfo(id, buffer);
        }
    }

    /** 看一眼包里的子控件下标是否存在（不消耗读指针）。 */
    private boolean hasChild(FriendlyByteBuf buffer) {
        buffer.markReaderIndex();
        int index = buffer.readVarInt();
        buffer.resetReaderIndex();
        return index >= 0 && index < widgets.size();
    }

    /** 面板本体：Ore 窗口外框，标题行 + 高度随内容的滚动区。 */
    private static final class PopupPanel extends UIElement implements IShiftClickPriority {

        /// 标题行、内边距、间距占掉的高度，面板高度上限减去它就是滚动区的高度上限
        private static final int CHROME = UISizes.POPUP_PADDING + UISizes.CONTROL_HEIGHT + UISizes.SECTION_GAP + UISizes.POPUP_PADDING_BOTTOM;

        private final ScrollerView scroller;

        private PopupPanel(Popup popup, int maxHeight, Runnable close) {
            int inner = UISizes.POPUP_CONTENT_WIDTH + ScrollerView.SCROLL_BAR_WIDTH + ScrollerView.SCROLL_BAR_MARGIN;
            layout(l -> l.column().width(inner + 2 * UISizes.POPUP_PADDING).gapAll(UISizes.SECTION_GAP)
                    .paddingAll(UISizes.POPUP_PADDING).paddingBottom(UISizes.POPUP_PADDING_BOTTOM));
            setBackground(UITheme.WINDOW);

            var closeButton = Button.glyph("×").setOnClientClick(close);
            closeButton.setHoverTooltips(MachineWindow.POPUP_CLOSE);
            var titleRow = UIElement.row(UISizes.CONTROL_HEIGHT).layout(l -> l.width(inner).gapAll(UISizes.GAP).alignCenter())
                    .addChildren(Label.of(popup.title(), inner - UISizes.ICON_BUTTON - UISizes.GAP), UIElement.flexSpacer(), closeButton);

            int maxContentHeight = contentLimit(maxHeight);
            var content = UIElement.column(UISizes.POPUP_CONTENT_WIDTH).layout(l -> l.gapAll(UISizes.SECTION_GAP));
            popup.content().accept(content);
            // 初始高度取最小，由 fitContentHeight 按内容撑开（见 ScrollerView 对滚动范围的处理）
            scroller = new ScrollerView(inner, UISizes.SLOT);
            scroller.addScrollViewChild(content);
            scroller.fitContentHeight(maxContentHeight);
            addChildren(titleRow, scroller);
        }

        private static int contentLimit(int maxHeight) {
            return Math.max(UISizes.SLOT, maxHeight - CHROME);
        }

        /** 弹出面板是玩家正在操作的窗口：Shift+点击时面板里的槽优先接收。 */
        @Override
        public int getShiftClickPriority() {
            return IShiftClickPriority.POPUP;
        }

        /** 屏幕尺寸变化时更新滚动区高度上限（内容放得下就不滚动）。 */
        private void setMaxHeight(int maxHeight) {
            scroller.fitContentHeight(contentLimit(maxHeight));
        }
    }
}
