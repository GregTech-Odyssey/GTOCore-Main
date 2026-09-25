package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTree;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.TechTreeSavedData;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.canvas.CanvasControls;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.data.SyncValue;
import com.gregtechceu.gtceu.uipro.elements.Dock;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.CardHost;
import com.gregtechceu.gtceu.uipro.window.Popup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 一棵科技树的视图：画布（{@link CanvasView}）+ 底部悬浮的视图按钮与说明 + 右侧悬浮的节点详情卡片。一个研究类别一个视图：
 * 机器窗口里每棵树是一个页面标签（见 {@link TechTreePage}），EMI 配方页里是该节点所在的树。两处是同一个组件，只是尺寸不同。
 * <p>
 * 点击节点在画布右侧打开它的详情卡片（{@link TechNodeDetails}，卡片参数是节点编码），再点同一个节点关闭；
 * 详情里点前置 / 后续节点会跳过去，在别的树里时交给使用方（{@link #setOnOtherTree}）。选中项就是打开着的卡片的参数。
 * <p>
 * 数据与同步（按框架的 C/S 分工）：
 * <ul>
 * <li>节点状态（未解锁 / 可解锁 / 已解锁）由服务端按打开界面的玩家所在队伍计算，经 {@link #states} 下发；
 * 只在解锁数据变化（{@link TechTreeSavedData#getModCount()}）或换队时重算，不是每 tick 遍历整棵树。</li>
 * <li>详情卡片以服务端为准（{@link CardHost}）：客户端请求打开，服务端校验节点编码（只接受本树的节点）后两端一起构建。
 * 卡片状态属于这个打开的界面，不进任何存档或机器字段。</li>
 * <li>画布内容（{@link TechTreeScene}）只在客户端存在，两端控件树与节点多少无关。</li>
 * </ul>
 * 纯客户端界面（EMI）里没有服务端，同步值由本端取值、卡片在本端直接构建（框架处理）。
 */
@DataGeneratorScanned
public class TechTreeView extends UIElement {

    @RegisterLanguage(cn = "已解锁", en = "Unlocked")
    public static final String STATUS_UNLOCKED = "gtocore.techtree.widget.status.unlocked";
    @RegisterLanguage(cn = "可解锁", en = "Available")
    public static final String STATUS_AVAILABLE = "gtocore.techtree.widget.status.available";
    @RegisterLanguage(cn = "未解锁", en = "Locked")
    public static final String STATUS_LOCKED = "gtocore.techtree.widget.status.locked";
    @RegisterLanguage(cn = "正在研究", en = "Researching")
    static final String STATUS_RESEARCHING = "gtocore.techtree.widget.status.researching";
    @RegisterLanguage(cn = "前置科技", en = "Prerequisites")
    static final String PREREQUISITES = "gtocore.techtree.widget.prerequisites";
    @RegisterLanguage(cn = "点击节点查看详情", en = "Click a node to see its details")
    private static final String HELP_CLICK = "gtocore.techtree.view.help.click";
    @RegisterLanguage(cn = "定位到选中的科技", en = "Locate the selected node")
    private static final String LOCATE = "gtocore.techtree.view.locate";
    @RegisterLanguage(cn = "点击查看详情", en = "Click for details")
    static final String CLICK_FOR_DETAILS = "gtocore.techtree.view.click_for_details";

    public static final byte LOCKED = 0;
    public static final byte AVAILABLE = 1;
    public static final byte UNLOCKED = 2;

    private final TechTreeManager manager;
    private final CanvasView canvas;
    private final CardHost details;
    private final SyncValue<NodeStates> states;
    /// 正在研究的节点编码（数据中心的研究窗口里由服务端取值下发，其他场合为 -1）
    private final SyncValue<Integer> researching;
    private Supplier<TechNode> researchingSource = () -> null;
    /// 详情里显示"强制解锁"、末尾追加的区块（两端设置要一致）
    private boolean force;
    @Nullable
    private BiConsumer<UIElement, TechNode> extraDetails;
    /// 服务端状态缓存：解锁数据修改计数与队伍都没变时直接复用
    private int cachedModCount = -1;
    @Nullable
    private UUID cachedTeam;
    private NodeStates cachedStates = NodeStates.EMPTY;

    // ==================== 客户端 ====================
    /// 本帧的选中编码与节点：每帧绘制前读一次，节点逐个比对时不再查卡片（编码变了才重新查节点）
    private int frameSelection = -1;
    @Nullable
    private TechNode frameSelectedNode;
    private Consumer<TechNode> onOtherTree = node -> {};

    /**
     * @param manager      显示的树
     * @param canvasId     画布的固定 id（锁定的尺寸按它保存；不同场合的画布用不同 id）
     * @param canvasWidth  画布首选宽度
     * @param canvasHeight 画布首选高度
     */
    public TechTreeView(TechTreeManager manager, String canvasId, int canvasWidth, int canvasHeight) {
        this.manager = manager;
        layout(l -> l.column());
        states = addSyncValue(SyncValue.of(this::computeStates, NodeStates.CODEC, NodeStates.EMPTY));
        researching = addSyncValue(SyncValue.ofInt(() -> {
            var node = researchingSource.get();
            return node == null || node.getManager() != manager ? -1 : encodeNode(node);
        }, -1));

        canvas = new CanvasView(canvasId, canvasWidth, canvasHeight);
        details = new CardHost("techtree.details", this::createDetails);
        canvas.setScene(view -> TechTreeScene.build(view, this))
                .setInitialView(view -> view.showStart(UISizes.SLOT, false))
                .setOnItemClick((item, button) -> {
                    if (button == 0 && item instanceof TechTreeScene.NodeItem node) toggleDetails(node.node());
                });
        var locate = CanvasControls.button(UITheme.CANVAS_LOCATE, LOCATE, () -> {
            var selected = decodeNode(details.getArgument());
            if (selected != null) navigateTo(selected);
        });
        var help = new InfoIcon(InfoIcon.Kind.INFO, Component.translatable(HELP_CLICK),
                Component.translatable(CanvasView.HELP_PAN), Component.translatable(CanvasView.HELP_ZOOM));
        canvas.addOverlay(new Dock().addGroup(CanvasControls.of(canvas))
                .addGroup(CanvasControls.row().addChild(locate))
                .addGroup(help));
        canvas.addFloatingCard(details);
        addChild(canvas);
    }

    /** 节点详情卡片（两端都会调用）：只接受本树的节点，编码不合法（可能来自客户端）时拒绝。 */
    @Nullable
    private Popup createDetails(int code) {
        var node = decodeNode(code);
        if (node == null || node.getManager() != manager) return null;
        return Popup.of(nameOf(node), column -> TechNodeDetails.build(column, node, new Navigator(this), force, extraDetails));
    }

    // ==================== 配置（使用方） ====================

    /** 正在研究的节点（服务端取值，如数据中心当前的研究；不在本树时不显示）。两端建界面时设置。 */
    public TechTreeView setResearching(Supplier<TechNode> researching) {
        this.researchingSource = researching;
        return this;
    }

    /** {@code code} 是不是正在研究的节点（客户端读下发的值）。 */
    boolean isResearching(int code) {
        return researching.getValue() == code;
    }

    /**
     * 详情卡片的内容选项（两端建界面时设置要一致）：{@code force} 显示"强制解锁"（调试器），
     * {@code extra} 在详情末尾追加区块（参数是新区块与节点，如数据中心的"启动研究"）。
     */
    public TechTreeView setDetailsOptions(boolean force, @Nullable BiConsumer<UIElement, TechNode> extra) {
        this.force = force;
        this.extraDetails = extra;
        return this;
    }

    /** 要跳到的节点在别的树里（客户端）：由使用方切到那棵树。 */
    public TechTreeView setOnOtherTree(Consumer<TechNode> onOtherTree) {
        this.onOtherTree = onOtherTree;
        return this;
    }

    public TechTreeManager getManager() {
        return manager;
    }

    public CanvasView getCanvas() {
        return canvas;
    }

    /** {@code code} 为节点编码（{@link #encodeNode}，由调用方预先算好，避免每帧查注册表）；与本帧读到的选中项比对。 */
    boolean isSelected(int code) {
        return frameSelection == code;
    }

    /** 本帧选中的节点（客户端，没有为 null）：选中节点的依赖与悬停时一样高亮。 */
    @Nullable
    TechNode selectedNode() {
        return frameSelectedNode;
    }

    /** 每帧绘制前读一次选中项（画布里的节点在绘制时逐个比对）。 */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int selection = details.getArgument();
        if (selection != frameSelection) {
            frameSelection = selection;
            frameSelectedNode = decodeNode(selection);
        }
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    /** 客户端：点击节点——打开它的详情（被卡片挡住时移到露出的部分），已打开时关闭。 */
    private void toggleDetails(TechNode node) {
        if (details.toggle(encodeNode(node))) canvas.reveal(TechTreeScene.nodeRect(manager, node), true);
    }

    /**
     * 客户端：打开 {@code node} 的详情并定位过去；在别的树里时交给 {@link #setOnOtherTree}（使用方切到那棵树后再调用它的这个方法）。
     */
    public void showDetails(TechNode node) {
        if (node.getManager() != manager) {
            onOtherTree.accept(node);
            return;
        }
        // 先打开卡片：定位按卡片左边露出的部分居中
        details.open(encodeNode(node));
        navigateTo(node);
    }

    /**
     * 界面打开时就显示 {@code node}（本树的节点）：初始视图按 {@code scale} 倍缩放、以它为中心
     * （卡片左边露出的部分）。用于纯客户端界面（EMI 配方页）；详情等界面初始化后再打开。
     */
    public TechTreeView setInitialNode(TechNode node, float scale, boolean openDetails) {
        if (node.getManager() != manager) return this;
        var rect = TechTreeScene.nodeRect(manager, node);
        canvas.setInitialView(view -> {
            view.setView(view.offsetX(), view.offsetY(), scale, false);
            view.centerOn(rect.centerX(), rect.centerY(), false);
        });
        if (openDetails) details.open(encodeNode(node));
        return this;
    }

    /**
     * 客户端：把视图移到 {@code node}。在本树里时定位过去（界面刚打开、第一帧还没画时替换初始视图，
     * 否则首帧会把视图放回起点）；在别的树里时交给 {@link #setOnOtherTree}。
     */
    public void navigateTo(TechNode node) {
        if (node.getManager() != manager) {
            onOtherTree.accept(node);
            return;
        }
        var rect = TechTreeScene.nodeRect(manager, node);
        canvas.whenReady(view -> view.focus(rect, view.isViewInitialized()));
    }

    // ==================== 节点状态 ====================

    /** 第 {@code index} 个节点（按布局顺序）的状态；数据还没到时为 {@link #LOCKED}。 */
    byte state(int index) {
        var array = states.getValue().states();
        return index >= 0 && index < array.length ? array[index] : LOCKED;
    }

    /** 当前的节点状态快照：内容变了才是新对象，可作缓存键。 */
    Object statesSnapshot() {
        return states.getValue();
    }

    /** 服务端（或纯客户端界面的本端）：按打开界面的玩家所在队伍算每个节点的状态。 */
    private NodeStates computeStates() {
        Player player = player();
        if (player == null) return NodeStates.EMPTY;
        UUID team = TechTreeSavedData.getTeamUUID(player);
        int modCount = TechTreeSavedData.getModCount();
        if (modCount == cachedModCount && team.equals(cachedTeam)) return cachedStates;
        cachedModCount = modCount;
        cachedTeam = team;

        var nodes = manager.getLayout().orderedNodes();
        var array = new byte[nodes.size()];
        TechTree tree = TechTreeSavedData.findTree(team, manager);
        for (int i = 0; i < array.length; i++) {
            var node = nodes.get(i);
            if (tree != null && tree.isUnlocked(node)) array[i] = UNLOCKED;
            else if (prerequisitesUnlocked(team, node)) array[i] = AVAILABLE;
        }
        cachedStates = new NodeStates(array);
        return cachedStates;
    }

    /** 状态对应的翻译键（写全，不拼接）。 */
    static String stateKey(byte state) {
        return switch (state) {
            case UNLOCKED -> STATUS_UNLOCKED;
            case AVAILABLE -> STATUS_AVAILABLE;
            default -> STATUS_LOCKED;
        };
    }

    /** 状态在悬停提示（深色底）里的颜色。 */
    static int stateTooltipColor(byte state) {
        var style = TechTreeStyle.get();
        return switch (state) {
            case UNLOCKED -> style.tooltipUnlocked;
            case AVAILABLE -> style.tooltipAvailable;
            default -> style.tooltipLocked;
        };
    }

    static boolean prerequisitesUnlocked(UUID team, TechNode node) {
        return TechTreeSavedData.isPrerequisitesUnlocked(team, node);
    }

    /** 一棵树所有节点的状态（按布局顺序）。数组按内容比较，状态没变就不重发。 */
    record NodeStates(byte[] states) {

        static final NodeStates EMPTY = new NodeStates(new byte[0]);
        /// 一棵树节点数的上限（节点注册序号只占 16 位）
        private static final int MAX_NODES = 1 << 16;

        static final SyncValue.Codec<NodeStates> CODEC = new SyncValue.Codec<>() {

            @Override
            public void write(FriendlyByteBuf buf, NodeStates value) {
                buf.writeByteArray(value.states);
            }

            @Override
            public NodeStates read(FriendlyByteBuf buf) {
                return new NodeStates(buf.readByteArray(MAX_NODES));
            }
        };

        @Override
        public boolean equals(Object obj) {
            return obj instanceof NodeStates other && Arrays.equals(other.states, states);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(states);
        }
    }

    // ==================== 节点编码（卡片参数、选中项） ====================

    /** 节点的整数编码：树的注册序号 × 65536 + 节点在树里的注册序号（两端一致）。 */
    public static int encodeNode(TechNode node) {
        var manager = node.getManager();
        return TechTreeManager.REGISTRY.getId(manager) << 16 | manager.getId(node);
    }

    /** {@link #encodeNode} 的逆运算；编码不合法（可能来自客户端）时返回 null。 */
    @Nullable
    public static TechNode decodeNode(int code) {
        if (code < 0) return null;
        int managerId = code >>> 16, nodeId = code & 0xFFFF;
        if (managerId >= TechTreeManager.REGISTRY.values().size()) return null;
        var manager = TechTreeManager.REGISTRY.get(managerId);
        if (manager == null || nodeId >= manager.values().size()) return null;
        return manager.get(nodeId);
    }

    /** 打开界面的玩家（服务端、或纯客户端界面的本端）。 */
    @Nullable
    Player player() {
        return getGui() == null ? null : getGui().entityPlayer;
    }

    /** 节点详情与所在视图的连接：取打开界面的玩家、"跳到节点"。 */
    static final class Navigator {

        private final TechTreeView view;

        Navigator(TechTreeView view) {
            this.view = view;
        }

        @Nullable
        Player player() {
            return view.player();
        }

        /** 客户端：打开节点的详情并定位过去（别的树时交给视图的使用方切树）。 */
        void navigateTo(TechNode node) {
            view.showDetails(node);
        }
    }

    /** 两端都能调用的节点名称（悬停提示、卡片标题用）。 */
    static Supplier<Component> nameOf(TechNode node) {
        return node::getDisplayName;
    }
}
