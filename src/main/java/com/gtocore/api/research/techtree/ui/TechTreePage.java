package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 科技树界面的两种场合，用的是同一个组件（{@link TechTreeView}：画布 + 右侧悬浮的节点详情卡片），只是尺寸不同：
 * <ul>
 * <li>{@link #window}：研究窗口（数据中心的"科技树"独立窗口、科技树调试器）。<b>一个研究类别一个页面标签</b>，
 * 排在窗口顶上（{@code MachineWindow} 的标签栏），画布按屏幕撑大。详情里跳到别的树的节点时先切到那棵树的标签，再定位、打开它的详情。</li>
 * <li>EMI 配方页（{@code TechTreeEmiRecipe}）：纯客户端界面，节点所在的树，一开始打开该节点的详情；
 * 跳到别的树的节点时换成那个节点的配方页。</li>
 * </ul>
 */
public final class TechTreePage {

    /** 研究窗口里画布的最小默认尺寸：20 × 10 格；屏幕大时按屏幕撑大（见 {@code CanvasView.fillScreen}）。 */
    public static final int CANVAS_WIDTH = 20 * UISizes.SLOT;
    public static final int CANVAS_HEIGHT = 10 * UISizes.SLOT;
    /// 撑大的上限：40 × 24 格
    private static final int CANVAS_MAX_WIDTH = 40 * UISizes.SLOT;
    private static final int CANVAS_MAX_HEIGHT = 24 * UISizes.SLOT;
    /// 研究窗口里画布以外占的宽：窗口左右内边距 + 与屏幕边缘的距离（详情卡片浮在画布里，不另占宽度）
    private static final int RESERVED_WIDTH = 2 * UISizes.WINDOW_PADDING_X + 2 * UISizes.POPUP_SCREEN_MARGIN;
    /// 画布以外占的高：顶上标签栏 + 窗口上下内边距 + 标题栏与间距
    private static final int RESERVED_HEIGHT = UISizes.PAGE_TAB_HEIGHT + UISizes.PAGE_TAB_RAISE + UISizes.WINDOW_PADDING_TOP +
            UISizes.CONTROL_HEIGHT + UISizes.SECTION_GAP + UISizes.WINDOW_PADDING_BOTTOM;

    private TechTreePage() {}

    /** 研究窗口的选项。 */
    public static final class Options {

        boolean force;
        @Nullable
        BiConsumer<UIElement, TechNode> extra;
        @Nullable
        Supplier<TechNode> initialFocus;
        @Nullable
        Supplier<TechNode> researching;

        /** 调试器：详情里显示"强制解锁"。 */
        public Options force() {
            this.force = true;
            return this;
        }

        /** 详情末尾追加的区块（两端都会调用），参数是新区块与节点。 */
        public Options extra(BiConsumer<UIElement, TechNode> extra) {
            this.extra = extra;
            return this;
        }

        /** 正在研究的节点（服务端取值，如数据中心当前的研究）：在树上用专门的样式标出。 */
        public Options researching(Supplier<TechNode> researching) {
            this.researching = researching;
            return this;
        }

        /** 打开窗口时切到这个节点所在的树、定位并打开它的详情（服务端取值，如数据中心正在研究的节点）。 */
        public Options initialFocus(Supplier<TechNode> initialFocus) {
            this.initialFocus = initialFocus;
            return this;
        }
    }

    /**
     * 研究窗口（两端都会调用）：主页是第一棵树，窗口顶上每棵树一个标签，{@code extraTabs} 排在所有树之后（如开发用的编辑器）。
     * 画布按屏幕撑大，窗口始终按屏幕居中（拖拽缩放后用动画回到正中）。
     */
    public static MachineWindow window(Options options, IFancyUIProvider... extraTabs) {
        return new MachineWindow(new Tabs(options, List.of(extraTabs)).trees.get(0)).setCentered(true).setTitleFollowsTab(true);
    }

    /** 一个研究窗口的所有标签与跨标签的状态（两端各一份）。 */
    private static final class Tabs {

        private final Options options;
        private final List<TreeTab> trees;
        private final List<IFancyUIProvider> extraTabs;
        /// 客户端：从别的树跳过来、新页面建好后要定位并打开详情的节点
        @Nullable
        private TechNode pendingFocus;
        /// 客户端：初始定位已处理（换标签重建页面后不再重复）
        private boolean initialFocusHandled;

        private Tabs(Options options, List<IFancyUIProvider> extraTabs) {
            this.options = options;
            this.extraTabs = extraTabs;
            var managers = TechTreeManager.managersById();
            this.trees = new ArrayList<>(managers.size());
            for (var manager : managers) trees.add(new TreeTab(this, manager));
        }

        @Nullable
        private TreeTab tabOf(TechTreeManager manager) {
            for (var tab : trees) {
                if (tab.manager == manager) return tab;
            }
            return null;
        }
    }

    /** 一棵树的页面标签。 */
    private static final class TreeTab implements IFancyUIProvider {

        private final Tabs tabs;
        private final TechTreeManager manager;

        private TreeTab(Tabs tabs, TechTreeManager manager) {
            this.tabs = tabs;
            this.manager = manager;
        }

        @Override
        public Widget createMainPage(FancyMachineUIWidget widget) {
            return create(widget, this);
        }

        /** 每棵树的标签都挂同一组标签（第一棵树是主页）。 */
        @Override
        public void attachSideTabs(TabsWidget sideTabs) {
            sideTabs.setMainTab(tabs.trees.get(0));
            for (int i = 1; i < tabs.trees.size(); i++) sideTabs.attachSubTab(tabs.trees.get(i));
            for (var extra : tabs.extraTabs) sideTabs.attachSubTab(extra);
        }

        @Override
        public IGuiTexture getTabIcon() {
            return manager.getIcon();
        }

        @Override
        public List<Component> getTabTooltips() {
            return List.of(TechTreeManager.getTreeName(manager));
        }

        @Override
        public Component getTitle() {
            return TechTreeManager.getTreeName(manager);
        }

        /** 科技树页不放玩家背包：高度留给画布。 */
        @Override
        public boolean hasPlayerInventory() {
            return false;
        }
    }

    /** 一棵树的页面（两端都会执行）。 */
    private static Widget create(FancyMachineUIWidget widget, TreeTab tab) {
        var tabs = tab.tabs;
        var options = tabs.options;
        var view = new TechTreeView(tab.manager, "techtree.canvas", CANVAS_WIDTH, CANVAS_HEIGHT)
                .setDetailsOptions(options.force, options.extra);
        if (options.researching != null) view.setResearching(options.researching);
        // 科技树要拖着看，画布越大越好：默认按屏幕撑大
        view.getCanvas().fillScreen(RESERVED_WIDTH, RESERVED_HEIGHT, CANVAS_MAX_WIDTH, CANVAS_MAX_HEIGHT);
        if (!(widget instanceof MachineWindow window)) return view;
        view.setOnOtherTree(node -> {
            var target = tabs.tabOf(node.getManager());
            if (target == null) return;
            tabs.pendingFocus = node;
            window.selectTab(target);
        });
        // 客户端：从别的树跳过来——切标签重建了本页，定位并打开该节点的详情
        var pending = tabs.pendingFocus;
        if (pending != null && pending.getManager() == tab.manager) {
            tabs.pendingFocus = null;
            view.showDetails(pending);
        }
        if (options.initialFocus != null) bindInitialFocus(view, tabs, options.initialFocus);
        return view;
    }

    /**
     * 初始定位：服务端在打开窗口时取一次节点（之后不再跟随）、下发；客户端第一次收到时定位并打开详情，
     * 节点在别的树里就先切过去（换标签重建页面后不再重复）。
     */
    private static void bindInitialFocus(TechTreeView view, Tabs tabs, Supplier<TechNode> focus) {
        var latched = new int[] { Integer.MIN_VALUE };
        var initial = SyncValue.ofInt(() -> {
            if (latched[0] == Integer.MIN_VALUE) {
                var node = focus.get();
                latched[0] = node == null ? -1 : TechTreeView.encodeNode(node);
            }
            return latched[0];
        }, -1);
        initial.onChanged(code -> {
            if (tabs.initialFocusHandled || !view.isRemote()) return;
            var node = TechTreeView.decodeNode(code);
            if (node == null) return;
            tabs.initialFocusHandled = true;
            view.showDetails(node);
        });
        view.addSyncValue(initial);
    }
}
