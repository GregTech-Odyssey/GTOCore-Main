package com.gtocore.api.gui.ui.window;

import com.gtocore.api.gui.InitFancyMachineUIWidget;
import com.gtocore.api.gui.ui.elements.Button;
import com.gtocore.api.gui.ui.elements.ItemView;
import com.gtocore.api.gui.ui.styletemplate.UISizes;
import com.gtocore.api.gui.ui.styletemplate.UITheme;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

/**
 * 机器界面外壳（新式界面的示范实现）：在 GTM {@link com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget}
 * 的导航逻辑（侧边标签页、翻页、左侧配置按钮）之上，重写布局与外观。
 *
 * <pre>
 *            [主页][页2][页3]              ← 页面标签：窗口顶上横排，选中的与窗口连成一体
 *  [配置]  ┌─────────── 176 ───────────┐    ┌──── 弹出面板 ────┐
 *  [配置]  │ [&lt;] 图标 标题 ………… ⓘ [+] │ 4  │ 标题 ……… [×]   │
 *  [配置]  │ 页面（宽 162，x = 7）       │    │ 可滚动内容       │
 *          │ 玩家背包（x = 7，与页面对齐）│    └─────────────────┘
 *          └────────────────────────────┘
 *  ↑ 机器小组件（GTM 配置按钮）：窗口左侧一列，与窗口顶部内边距对齐，放不下才向左加列
 * </pre>
 *
 * <ul>
 * <li>外框 {@link UITheme#WINDOW}；标题栏在窗口内第一行，图标 + 标题 + 悬浮说明图标，历史非空时显示返回键。</li>
 * <li>页面不再居中：宽度不足 {@link UISizes#CONTENT_WIDTH} 时才在内容区内居中，否则左边缘固定在 x = 7。</li>
 * <li>玩家背包改用 Ore 槽位，紧跟页面下方 {@link UISizes#SECTION_GAP}，与页面同一左边缘。</li>
 * <li>页面标签（导航）在窗口顶上横排（{@link WindowTabBar}），机器小组件（开关类）独占左侧——两类东西分开放，不再挤在同一侧。</li>
 * <li>右侧弹出面板见 {@link #registerPopup}：由页面注册，按键打开，每个打开界面的玩家各自独立。</li>
 * </ul>
 * 页面仍通过 {@link IFancyUIProvider#createMainPage} 创建，GTM 的子页面（无线、优先级等）也能直接放进来。
 */
@DataGeneratorScanned
public class MachineWindow extends InitFancyMachineUIWidget {

    @RegisterLanguage(cn = "关闭", en = "Close")
    public static final String POPUP_CLOSE = "gtocore.gui.popup.close";

    private final WindowTitleBar title;
    private final WindowTabBar tabs;
    private final PopupHost popups;
    @Nullable
    private IntFunction<Widget> titleContent;
    private boolean placing;
    /** 客户端：第一次摆放时的窗口宽度，之后切页时窗口左边缘按它固定（见 {@link #applyClientPlacement}）。 */
    private int anchorWidth;

    public MachineWindow(IFancyUIProvider mainPage) {
        this(mainPage, () -> {});
    }

    public MachineWindow(IFancyUIProvider mainPage, Runnable init) {
        super(mainPage, UISizes.WINDOW_WIDTH, UISizes.WINDOW_WIDTH, init);
        setBackground(UITheme.WINDOW);
        // 标题栏、悬浮说明、页面标签由本类自绘；GTM 的三个控件留作数据容器（标签列表、选中项、导航回调），不进控件树
        removeWidget(titleBar);
        removeWidget(tooltipsPanel);
        removeWidget(sideTabsWidget);
        configuratorPanel.setTexture(UITheme.CONFIGURATOR_TAB);
        if (playerInventory != null) layoutPlayerInventory(playerInventory);
        addWidget(title = new WindowTitleBar());
        addWidget(tabs = new WindowTabBar());
        addWidget(popups = new PopupHost(this::updatePlacement));
    }

    /** 从任意子控件向上找到所在的窗口；不在 {@link MachineWindow} 里时返回 null。 */
    @Nullable
    public static MachineWindow of(Widget widget) {
        for (Widget current = widget; current != null; current = current.getParent()) {
            if (current instanceof MachineWindow window) return window;
        }
        return null;
    }

    // ==================== 弹出面板 ====================

    /**
     * 注册一种弹出面板，由页面自己决定注册几种（可以不注册）。页面在 {@link IFancyUIProvider#createMainPage} 里调用
     * （两端都会执行），切换页面时注册表与已打开的面板一并清空。
     * <p>
     * 每种面板同时至多打开一个，{@code factory} 的参数是打开时传入的整数（如槽号），参数不合法时返回 null 拒绝打开；
     * 不同种的面板可以同时打开，在窗口右侧按打开顺序排列。
     */
    public void registerPopup(String key, IntFunction<Popup> factory) {
        popups.register(key, factory);
    }

    /** 打开弹出面板；客户端调用时请求服务端打开，两端随后一起构建。同一种面板已打开时原地替换成新参数。 */
    public void openPopup(String key, int argument) {
        popups.open(key, argument);
    }

    /** 以同样参数打开着时关闭，否则打开（或替换成这个参数）。 */
    public void togglePopup(String key, int argument) {
        if (isPopupOpen(key, argument)) closePopup(key);
        else openPopup(key, argument);
    }

    public void closePopup(String key) {
        popups.close(key);
    }

    public void closeAllPopups() {
        popups.closeAll();
    }

    /** 这种面板是否以该参数打开着（客户端可直接查询，用于高亮对应的槽等）。 */
    public boolean isPopupOpen(String key, int argument) {
        return popups.isOpen(key, argument);
    }

    public boolean isPopupOpen(String key) {
        return popups.isOpen(key);
    }

    /** 这种面板打开时的参数，没打开时返回 {@code -1}。 */
    public int getPopupArgument(String key) {
        return popups.argumentOf(key);
    }

    /** 界面里的机器窗口；界面不是用 {@link MachineWindow} 搭的时返回 null。 */
    @Nullable
    public static MachineWindow find(ModularUI ui) {
        for (var widget : ui.mainGroup.getContainedWidgets(true)) {
            if (widget instanceof MachineWindow window) return window;
        }
        return null;
    }

    // ==================== 标题栏内容 ====================

    /**
     * 由页面自定义标题栏中段（图标与右侧说明图标之间），替代默认的页面标题文字；参数是中段可用宽度，
     * 返回的元素高度不超过 {@link UISizes#CONTROL_HEIGHT}。页面在 {@link IFancyUIProvider#createMainPage} 里设置
     * （两端都会执行），切换页面时清空。设置后页面标题改为鼠标停在图标上时显示。
     */
    public void setTitleContent(IntFunction<Widget> content) {
        this.titleContent = content;
    }

    // ==================== 布局 ====================

    @Override
    protected void setupFancyUI(IFancyUIProvider fancyUI, boolean showInventory) {
        clearUI();
        popups.reset();
        titleContent = null;
        sideTabsWidget.selectTab(fancyUI);
        var page = fancyUI.createMainPage(this);

        int contentWidth = Math.max(UISizes.CONTENT_WIDTH, page.getSizeWidth());
        int width = contentWidth + 2 * UISizes.WINDOW_PADDING_X;
        int y = UISizes.WINDOW_PADDING_TOP;
        title.setSelfPosition(new Position(UISizes.WINDOW_PADDING_X, y));
        y += UISizes.CONTROL_HEIGHT + UISizes.SECTION_GAP;

        page.setSelfPosition(new Position(UISizes.WINDOW_PADDING_X + (contentWidth - page.getSizeWidth()) / 2, y));
        y += page.getSizeHeight();

        boolean inventory = showInventory && playerInventory != null;
        if (playerInventory != null) {
            playerInventory.setSelfPosition(new Position(UISizes.WINDOW_PADDING_X + (contentWidth - UISizes.SLOT_ROW_WIDTH) / 2, y + UISizes.SECTION_GAP));
            playerInventory.setActive(inventory);
            playerInventory.setVisible(inventory);
        }
        if (inventory) y += UISizes.SECTION_GAP + UISizes.PLAYER_INVENTORY_HEIGHT;
        int height = y + UISizes.WINDOW_PADDING_BOTTOM;

        setSize(new Size(width, height));
        pageContainer.setSize(new Size(width, height));
        pageContainer.addWidget(page);

        // 页面标签在窗口顶上横排；左侧只放机器小组件（配置按钮），与窗口顶部内边距对齐，一列放不下才向左加列
        tabs.setup();
        configuratorPanel.setAvailableHeight(height - UISizes.WINDOW_PADDING_TOP - UISizes.WINDOW_PADDING_BOTTOM);
        fancyUI.attachConfigurators(configuratorPanel);
        configuratorPanel.setSelfPosition(new Position(-configuratorPanel.getSizeWidth() - UISizes.GAP, UISizes.WINDOW_PADDING_TOP));
        fancyUI.attachTooltips(tooltipsPanel);
        title.setup(currentHomePage, contentWidth, !previousPages.isEmpty(), allPages.size() > 1 && currentPage != pageSwitcher, titleContent);

        updatePlacement();
    }

    /**
     * 摆放弹出面板并确定界面尺寸。原则：打开面板不挪主窗口——界面尺寸就是主窗口尺寸，面板挂在窗口右侧界外
     * （LDLib 会把界外控件报给 EMI 避让）；只有面板超出屏幕右边时，才把主窗口左移刚好够的距离（不越过左侧悬浮栏）。
     * 面板默认与窗口顶边对齐，超出屏幕底边时自己上移。屏幕尺寸只在客户端知道，服务端只放默认位置（不影响同步）。
     * <p>
     * 注意：LDLib 的 {@code ModularUI.setSize} 只存在于客户端（专用服务器上被剥掉），服务端绝不能调用，
     * 所以界面尺寸只在 {@link #applyClientPlacement} 里设。
     */
    private void updatePlacement() {
        if (getGui() == null || placing) return;
        placing = true;
        try {
            if (isRemote()) applyClientPlacement();
            else popups.setSelfPosition(new Position(getSizeWidth() + UISizes.POPUP_GAP, 0));
        } finally {
            placing = false;
        }
    }

    /**
     * 客户端：界面按自身宽度水平居中。切页时页面宽度可能不同（GTM 的覆盖板页、工具页比 162 宽），直接按窗口宽度设界面尺寸，
     * 窗口左边缘就会随每次切页左右跳。所以以第一次摆放时的窗口宽度为基准（{@link #anchorWidth}）：
     * 窗口比基准宽 {@code extra} 时，界面两侧各加宽 {@code extra}、窗口在界面里右移 {@code extra}，屏幕上的左边缘不变，
     * 多出的宽度只向右长；比基准窄时界面保持基准宽度。窗口始终完整在界面范围内（界面外的点击会被原版当成点到界面外）。
     * 高度照常居中——切页时窗口只上下移动。顶部页面标签的高度也算进界面（窗口在界面里下移同样距离），
     * 页面很高时标签栏不会被挤出屏幕顶边。
     */
    @OnlyIn(Dist.CLIENT)
    private void applyClientPlacement() {
        int width = getSizeWidth(), height = getSizeHeight();
        if (anchorWidth <= 0) anchorWidth = width;
        int extra = Math.max(0, width - anchorWidth);
        int tabsHeight = tabs.reservedHeight();
        int[] placement = clientPlacement(width, height, tabsHeight);
        getGui().setSize(anchorWidth + 2 * extra + 2 * placement[0], height + tabsHeight);
        setSelfPosition(new Position(extra, tabsHeight));
        popups.setSelfPosition(new Position(width + UISizes.POPUP_GAP, placement[1]));
    }

    /** 客户端：{主窗口左移量, 面板相对窗口顶边的纵向偏移}，并按屏幕高度设定面板高度上限。 */
    @OnlyIn(Dist.CLIENT)
    private int[] clientPlacement(int width, int height, int tabsHeight) {
        var window = Minecraft.getInstance().getWindow();
        int screenWidth = window.getGuiScaledWidth(), screenHeight = window.getGuiScaledHeight();
        int margin = UISizes.POPUP_SCREEN_MARGIN;
        popups.setMaxHeight(Math.max(UISizes.SLOT, screenHeight - 2 * margin));
        int popupWidth = popups.getSizeWidth(), popupHeight = popups.getSizeHeight();
        if (popupWidth == 0) return new int[] { 0, 0 };
        // 窗口左边缘固定在按基准宽度居中的位置（见 applyClientPlacement）
        int left = (screenWidth - anchorWidth) / 2;
        int overflow = left + width + UISizes.POPUP_GAP + popupWidth + margin - screenWidth;
        int maxShift = Math.max(0, left - configuratorPanel.getSizeWidth() - UISizes.GAP - margin);
        int shift = Math.max(0, Math.min(overflow, maxShift));
        // 界面（标签栏 + 窗口）整体居中，窗口顶边在标签栏之下
        int top = (screenHeight - height - tabsHeight) / 2 + tabsHeight;
        int offsetY = Math.min(0, screenHeight - margin - top - popupHeight);
        offsetY = Math.max(offsetY, margin - top);
        return new int[] { shift, offsetY };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        updatePlacement();
    }

    /** 背包 3×9 + 快捷栏按 18 格紧排，快捷栏与背包隔 4，整体宽 162，槽位用 Ore 底图。 */
    private static void layoutPlayerInventory(PlayerInventoryWidget inventory) {
        int index = 0;
        for (var widget : inventory.getContainedWidgets(true)) {
            if (!(widget instanceof SlotWidget)) continue;
            // 前 9 个是快捷栏，之后按行排背包
            if (index < UISizes.SLOTS_PER_ROW) {
                widget.setSelfPosition(new Position(index * UISizes.SLOT, 3 * UISizes.SLOT + UISizes.SECTION_GAP));
            } else {
                int slot = index - UISizes.SLOTS_PER_ROW;
                widget.setSelfPosition(new Position(slot % UISizes.SLOTS_PER_ROW * UISizes.SLOT, slot / UISizes.SLOTS_PER_ROW * UISizes.SLOT));
            }
            index++;
        }
        inventory.setSize(new Size(UISizes.SLOT_ROW_WIDTH, UISizes.PLAYER_INVENTORY_HEIGHT));
        inventory.setSlotBackground(UITheme.ITEM_SLOT);
    }

    // ==================== 页面标签 ====================

    /**
     * 窗口顶上横排的页面标签，替代 GTM 左侧竖排的标签列（GTM 的 {@code sideTabsWidget} 只留作数据：标签列表、选中项、导航回调）。
     * 主页在最左，子页面依次往右；未选中的标签坐在窗口顶边上，选中的标签高出一截、向下伸进窗口顶边并抹掉接缝，与窗口连成一体。
     * 只有主页、或 GTM 隐藏了标签（打开"切换部件页"、单页界面）时不显示——这两种状态在切页过程中才变，所以绘制和点击时实时读取。
     * 点击：客户端先发包再切换，服务端收到后同样切换（与 GTM 标签页的做法一致），两端各自重建页面，控件树保持一致。
     */
    private final class WindowTabBar extends Widget {

        /// 标签之间的空隙
        private static final int TAB_GAP = 1;
        /// Ore 边框在左右两侧的可见宽度：第一个标签的内侧与页面左边缘对齐
        private static final int TAB_BORDER = 2;
        /// Ore 边框底部的厚边：选中标签伸进窗口的部分要抹掉
        private static final int TAB_BOTTOM_BORDER = 4;
        private static final int ICON = 16;

        private int tabWidth = UISizes.PAGE_TAB_WIDTH;

        private WindowTabBar() {
            super(0, 0, 0, 0);
        }

        /** 标签的位置与宽度只按标准窗口宽度算、与当前页面宽度无关：切页时标签栏左右不动，只随窗口上下移动。 */
        private void setup() {
            int count = count();
            int left = UISizes.WINDOW_PADDING_X - TAB_BORDER;
            int available = UISizes.WINDOW_WIDTH - 2 * left;
            tabWidth = Math.max(UISizes.PAGE_TAB_MIN_WIDTH, Math.min(UISizes.PAGE_TAB_WIDTH, (available + TAB_GAP) / count - TAB_GAP));
            int top = -UISizes.PAGE_TAB_HEIGHT - UISizes.PAGE_TAB_RAISE;
            setSelfPosition(new Position(left, top));
            setSize(new Size(count * (tabWidth + TAB_GAP) - TAB_GAP, -top + UISizes.PAGE_TAB_OVERLAP));
        }

        /** 标签数：主页 + 子页面。 */
        private int count() {
            return 1 + sideTabsWidget.getSubTabs().size();
        }

        /** 第 {@code index} 个标签：0 是主页，之后是子页面。 */
        private IFancyUIProvider tab(int index) {
            return index == 0 ? sideTabsWidget.getMainTab() : sideTabsWidget.getSubTabs().get(index - 1);
        }

        /**
         * 界面为标签栏预留的高度：有子页面就预留，不看 GTM 此刻是否隐藏标签——隐藏状态在切页过程中才变，
         * 跟着它变会让窗口在打开/关闭"切换部件页"时上下跳。
         */
        private int reservedHeight() {
            return sideTabsWidget.getSubTabs().isEmpty() ? 0 : UISizes.PAGE_TAB_HEIGHT + UISizes.PAGE_TAB_RAISE;
        }

        private boolean shown() {
            return sideTabsWidget.isVisible() && !sideTabsWidget.getSubTabs().isEmpty() && sideTabsWidget.getMainTab() != null;
        }

        private int tabX(int index) {
            return getPositionX() + index * (tabWidth + TAB_GAP);
        }

        /** 窗口顶边的屏幕纵坐标。 */
        private int windowTop() {
            return MachineWindow.this.getPositionY();
        }

        @OnlyIn(Dist.CLIENT)
        private int hoveredIndex(double mouseX, double mouseY) {
            int top = windowTop() - UISizes.PAGE_TAB_HEIGHT - UISizes.PAGE_TAB_RAISE;
            int height = UISizes.PAGE_TAB_HEIGHT + UISizes.PAGE_TAB_RAISE;
            for (int i = 0, count = count(); i < count; i++) {
                if (isMouseOver(tabX(i), top, tabWidth, height, mouseX, mouseY)) return i;
            }
            return -1;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (!shown()) return;
            var selected = sideTabsWidget.getSelectedTab();
            int hovered = hoveredIndex(mouseX, mouseY);
            int base = windowTop();
            int selectedIndex = -1;
            for (int i = 0, count = count(); i < count; i++) {
                var tab = tab(i);
                if (tab == selected) {
                    selectedIndex = i;
                    continue;
                }
                int x = tabX(i), top = base - UISizes.PAGE_TAB_HEIGHT;
                (i == hovered ? UITheme.PAGE_TAB_HOVER : UITheme.PAGE_TAB).draw(graphics, mouseX, mouseY, x, top, tabWidth, UISizes.PAGE_TAB_HEIGHT);
                tab.getTabIcon().draw(graphics, mouseX, mouseY, x + (tabWidth - ICON) / 2f, top + TAB_BORDER, ICON, ICON);
            }
            // 选中的标签最后画：盖住窗口顶边，再把标签底边与窗口顶边之间的接缝涂成窗口底色
            if (selectedIndex < 0) return;
            int x = tabX(selectedIndex), top = base - UISizes.PAGE_TAB_HEIGHT - UISizes.PAGE_TAB_RAISE;
            int bottom = base + UISizes.PAGE_TAB_OVERLAP;
            UITheme.PAGE_TAB_SELECTED.draw(graphics, mouseX, mouseY, x, top, tabWidth, bottom - top);
            graphics.fill(x + TAB_BORDER, bottom - TAB_BOTTOM_BORDER, x + tabWidth - TAB_BORDER, bottom, UITheme.WINDOW_FILL);
            tab(selectedIndex).getTabIcon().draw(graphics, mouseX, mouseY, x + (tabWidth - ICON) / 2f, top + TAB_BORDER + 1, ICON, ICON);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (!shown() || gui == null || gui.getModularUIGui() == null) return;
            int hovered = hoveredIndex(mouseX, mouseY);
            if (hovered < 0) return;
            var tab = tab(hovered);
            gui.getModularUIGui().setHoverTooltip(tab.getTabTooltips(), ItemStack.EMPTY, null, tab.getTabTooltipComponent());
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!shown() || button != 0) return false;
            int index = hoveredIndex(mouseX, mouseY);
            if (index < 0) return false;
            var tab = tab(index);
            if (tab != sideTabsWidget.getSelectedTab()) {
                writeClientAction(0, buf -> buf.writeVarInt(index));
                select(tab);
                playButtonClickSound();
            }
            return true;
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id != 0) {
                super.handleClientAction(id, buffer);
                return;
            }
            int index = buffer.readVarInt();
            if (index >= 0 && index < count() && shown()) select(tab(index));
        }

        private void select(IFancyUIProvider tab) {
            sideTabsWidget.selectTab(tab);
            sideTabsWidget.getOnTabClick().accept(tab);
        }
    }

    // ==================== 标题栏 ====================

    /**
     * 窗口内第一行：{@code [<]} 返回（有历史时）、页面图标、中段（默认是页面标题，页面可用 {@link #setTitleContent} 换成自己的控件）、
     * 右侧 GTM 悬浮说明图标（{@link IFancyTooltip}，12 见方，悬停显示说明；按已挂上的个数预留位置）、{@code [+]} 切换部件页（多于一页时）。
     * 两端在每次切换页面时一起重建子控件，控件树保持一致。
     */
    private final class WindowTitleBar extends WidgetGroup {

        /// 页面图标、说明图标都与控件同高
        private static final int ICON = UISizes.CONTROL_HEIGHT;

        @Nullable
        private IFancyUIProvider page;
        private boolean customContent;
        private int iconLeft;
        private int textLeft;
        private int textRight;
        private int tooltipsRight;

        private WindowTitleBar() {
            super(0, 0, UISizes.CONTENT_WIDTH, UISizes.CONTROL_HEIGHT);
        }

        private void setup(IFancyUIProvider page, int width, boolean showBack, boolean showMenu, @Nullable IntFunction<Widget> content) {
            this.page = page;
            clearAllWidgets();
            setSize(new Size(width, UISizes.CONTROL_HEIGHT));
            int left = 0;
            int right = width;
            if (showBack) {
                var back = Button.glyph("<").setOnClick(MachineWindow.this::navigateBack);
                back.setHoverTooltips("gtceu.gui.title_bar.back");
                addWidget(back);
                left += UISizes.ICON_BUTTON + UISizes.GAP;
            }
            if (showMenu) {
                right -= UISizes.ICON_BUTTON;
                var menu = Button.glyph("+").setOnClick(MachineWindow.this::openPageSwitcher);
                menu.setHoverTooltips("gtceu.gui.title_bar.page_switcher");
                menu.setSelfPosition(new Position(right, 0));
                addWidget(menu);
                right -= UISizes.GAP;
            }
            iconLeft = left;
            var icon = ItemView.of(page.getTabIcon());
            icon.setHoverTooltips(page.getTitle());
            icon.setSelfPosition(new Position(left, 0));
            addWidget(icon);
            textLeft = left + ICON + UISizes.GAP;
            tooltipsRight = right;
            textRight = right - tooltipsPanel.getTooltips().size() * (ICON + UISizes.GAP);
            customContent = content != null;
            if (content != null) {
                var widget = content.apply(Math.max(0, textRight - textLeft));
                widget.setSelfPosition(new Position(textLeft, (UISizes.CONTROL_HEIGHT - widget.getSizeHeight()) / 2));
                addWidget(widget);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            if (page == null) return;
            int x = getPositionX(), y = getPositionY(), h = getSizeHeight();
            int right = tooltipsRight;
            for (var tooltip : tooltipsPanel.getTooltips()) {
                if (!tooltip.showFancyTooltip()) continue;
                right -= ICON;
                tooltip.getFancyTooltipIcon().draw(graphics, mouseX, mouseY, x + right, y + (h - ICON) / 2f, ICON, ICON);
                right -= UISizes.GAP;
            }
            if (customContent) return;
            var font = Minecraft.getInstance().font;
            var text = UITheme.clip(font, page.getTitle().getString(), textRight - textLeft);
            graphics.drawString(font, text, x + textLeft, y + (h - 8) / 2, UITheme.TEXT, false);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (gui == null || gui.getModularUIGui() == null || page == null) return;
            var tooltip = hoveredTooltip(mouseX, mouseY);
            if (tooltip != null) {
                gui.getModularUIGui().setHoverTooltip(tooltip.getFancyTooltip(), ItemStack.EMPTY, null, tooltip.getFancyComponent());
            }
        }

        @Nullable
        private IFancyTooltip hoveredTooltip(int mouseX, int mouseY) {
            int x = getPositionX(), y = getPositionY() + (getSizeHeight() - ICON) / 2;
            int right = tooltipsRight;
            for (var tooltip : tooltipsPanel.getTooltips()) {
                if (!tooltip.showFancyTooltip()) continue;
                right -= ICON;
                if (isMouseOver(x + right, y, ICON, ICON, mouseX, mouseY)) return tooltip;
                right -= UISizes.GAP;
            }
            return null;
        }
    }
}
