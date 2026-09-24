package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.integration.emi.research.TechNodeEmiStack;

import com.gtolib.utils.ColorUtils;

import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLod;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRoute;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.canvas.ItemLayer;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 科技树在画布上的内容（只在客户端建）：从下往上依次是数据等级分区线、依赖连线、节点。
 * 坐标就是 {@link TechTreeLayout} 的布局坐标（世界单位），节点 {@link #NODE_SIZE} 见方；连线的走法由布局算好（{@link TechTreeAutoLayout}）。
 * <p>
 * 节点状态：已解锁、可解锁（静态高亮）、未解锁；另外数据中心正在研究的节点用青色呼吸，全树只有它会动，一眼能找到。
 */
final class TechTreeScene {

    static final int NODE_SIZE = TechTreeAutoLayout.NODE_SIZE;
    private static final int ICON_SIZE = 16;
    private static final int ICON_OFFSET = (NODE_SIZE - ICON_SIZE) / 2;
    private static final float LINE_WIDTH = 2;
    private static final float BORDER_WIDTH = 1;
    /// 分区线与分区标题超出节点范围的距离
    private static final float TIER_MARGIN = 20;
    private static final float TIER_DASH = 6, TIER_GAP = 4;
    /// 呼吸动画（正在研究的节点、悬停时高亮的依赖）的角速度：相位 = 当前毫秒数 / 该值，一个周期约 1.26 秒
    private static final double PULSE_MS_PER_RADIAN = 200;

    private TechTreeScene() {}

    /** 节点在世界坐标里的范围。 */
    static CanvasRect nodeRect(TechTreeManager manager, TechNode node) {
        var layout = manager.getLayout();
        return CanvasRect.of(layout.getX(node), layout.getY(node), NODE_SIZE, NODE_SIZE);
    }

    /** 往画布里加这棵树的三层内容（{@link CanvasView#setScene} 的回调，只在客户端执行）。 */
    static void build(CanvasView canvas, TechTreeView view) {
        var manager = view.getManager();
        var layout = manager.getLayout();
        var nodes = layout.orderedNodes();
        var indices = new Reference2IntOpenHashMap<TechNode>(nodes.size());
        indices.defaultReturnValue(-1);
        var items = new ItemLayer<NodeItem>();
        for (int i = 0; i < nodes.size(); i++) {
            var node = nodes.get(i);
            indices.put(node, i);
            items.add(new NodeItem(view, node, i, TechTreeView.encodeNode(node), nodeRect(manager, node)));
        }
        for (var item : items.items()) item.resolvePrerequisites(indices);
        canvas.addLayer(new TierLayer(layout, items.bounds()));
        canvas.addLayer(new EdgeLayer(view, layout, indices));
        canvas.addLayer(items);
    }

    // ==================== 数据等级分区 ====================

    /** 相邻两个数据等级之间一条竖直虚线，每个等级区域上方写"数据等级 N"。 */
    private static final class TierLayer implements CanvasLayer {

        private final List<TechTreeLayout.TierRegion> regions;
        /// 分区线的横坐标（布局在分界处的通道里留好的位置，不与连线的竖段重合）
        private final int[] separators;
        /// 各分区的标题，建层时生成一次
        private final Component[] labels;
        @Nullable
        private final CanvasRect bounds;
        private final float top, bottom;

        private TierLayer(TechTreeLayout layout, @Nullable CanvasRect nodes) {
            this.regions = layout.tierRegions();
            this.separators = layout.tierSeparators().toIntArray();
            this.labels = new Component[regions.size()];
            for (int i = 0; i < labels.length; i++) labels[i] = Component.translatable(TechNodeDetails.TIER_LABEL, regions.get(i).tier());
            this.top = nodes == null ? 0 : nodes.y() - TIER_MARGIN;
            this.bottom = nodes == null ? 0 : nodes.bottom() + TIER_MARGIN / 2;
            // 标题在节点上方，算进内容范围（适应全部时不被切掉）
            this.bounds = nodes == null || regions.size() < 2 ? null : CanvasRect.of(nodes.x(), top, nodes.width(), bottom - top);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
            if (regions.size() < 2) return;
            var style = TechTreeStyle.get();
            for (int x : separators) painter.dashedVLine(x, top, bottom, painter.px(1), TIER_DASH, TIER_GAP, style.tierSeparatorColor);
            if (painter.lod() != CanvasLod.FULL) return;
            painter.flush();
            var font = Minecraft.getInstance().font;
            var graphics = painter.graphics();
            for (int i = 0; i < labels.length; i++) {
                var region = regions.get(i);
                if (!painter.isVisible(region.minX(), top, region.maxX() + NODE_SIZE - region.minX(), TIER_MARGIN)) continue;
                graphics.drawString(font, labels[i], region.minX(), (int) top, UITheme.TEXT_SECONDARY, false);
            }
        }

        @Override
        @Nullable
        public CanvasRect bounds() {
            return bounds;
        }
    }

    // ==================== 依赖连线 ====================

    /** 一条依赖：前置、节点在本树里的序号，折线（布局算好），与包围盒。 */
    private record Edge(int from, int to, float[] points, CanvasRect bounds) {}

    /**
     * 依赖连线。同一节点发出（或汇入同一节点）的线合用一段轨道，重叠的部分后画的盖住先画的：
     * 按颜色的重要程度排序后画（未满足 → 前置已解锁 → 可解锁 → 已解锁），状态变了才重排。
     * 悬停节点与选中节点（打开着详情的）的依赖呼吸高亮，最后画。
     */
    private static final class EdgeLayer implements CanvasLayer {

        private final TechTreeView view;
        private final Reference2IntOpenHashMap<TechNode> indices;
        private final Edge[] edges;
        /// 按绘制顺序排好的连线，及排序时的状态快照
        private final Edge[] drawOrder;
        @Nullable
        private Object orderStates;
        @Nullable
        private final CanvasRect bounds;

        private EdgeLayer(TechTreeView view, TechTreeLayout layout, Reference2IntOpenHashMap<TechNode> indices) {
            this.view = view;
            this.indices = indices;
            var routed = layout.edges();
            this.edges = new Edge[routed.size()];
            CanvasRect union = null;
            for (int i = 0; i < edges.length; i++) {
                var edge = routed.get(i);
                var bounds = CanvasRoute.bounds(edge.points(), LINE_WIDTH);
                edges[i] = new Edge(indices.getInt(edge.from()), indices.getInt(edge.to()), edge.points(), bounds);
                union = CanvasRect.union(union, bounds);
            }
            this.drawOrder = edges.clone();
            this.bounds = union;
        }

        @Override
        @Nullable
        public CanvasRect bounds() {
            return bounds;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
            var states = view.statesSnapshot();
            if (states != orderStates) {
                orderStates = states;
                Arrays.sort(drawOrder, (a, b) -> Integer.compare(priority(a), priority(b)));
            }
            int hoveredIndex = hovered instanceof NodeItem item ? item.index : -1;
            var selected = view.selectedNode();
            int selectedIndex = selected == null ? -1 : indices.getInt(selected);
            for (var edge : drawOrder) {
                if (edge.to != hoveredIndex && edge.to != selectedIndex && painter.isVisible(edge.bounds)) painter.path(edge.points, LINE_WIDTH, color(edge, false));
            }
            if (hoveredIndex < 0 && selectedIndex < 0) return;
            for (var edge : drawOrder) {
                if ((edge.to == hoveredIndex || edge.to == selectedIndex) && painter.isVisible(edge.bounds)) painter.path(edge.points, LINE_WIDTH, color(edge, true));
            }
        }

        /** 绘制先后：越重要越靠后（盖在合用的轨道上）。 */
        private int priority(Edge edge) {
            byte from = view.state(edge.from), to = view.state(edge.to);
            if (to == TechTreeView.UNLOCKED) return 3;
            if (to == TechTreeView.AVAILABLE && from == TechTreeView.UNLOCKED) return 2;
            return from == TechTreeView.UNLOCKED ? 1 : 0;
        }

        private int color(Edge edge, boolean highlighted) {
            var style = TechTreeStyle.get();
            int color = switch (priority(edge)) {
                case 3 -> style.unlockedDependencyLine;
                case 2 -> style.availableDependencyLine;
                case 1 -> style.prerequisiteUnlockedDependencyLine;
                default -> style.defaultDependencyLine;
            };
            return highlighted ? pulse(style.hoveredDependencyLineColor, color) : color;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawMinimap(CanvasPainter painter) {
            for (var edge : edges) painter.path(edge.points, LINE_WIDTH * 2, TechTreeStyle.get().defaultDependencyLine);
        }
    }

    // ==================== 节点 ====================

    /** 一个节点：状态色底、1 像素边框、图标；未解锁的图标蒙一层灰；正在研究的呼吸；前置被悬停节点依赖时边框呼吸高亮。 */
    static final class NodeItem implements CanvasItem {

        private final TechTreeView view;
        private final TechNode node;
        private final int index;
        private final int code;
        private final CanvasRect rect;
        /// 各前置在本树里的序号（别的树的为 -1）
        private int[] prerequisiteIndices = new int[0];
        /// 悬停提示的缓存键：节点状态快照（服务端下发，内容变了才换对象）、客户端解锁数据的修改计数、是否正在研究
        @Nullable
        private Object tooltipStates;
        private int tooltipModCount = -1;
        private boolean tooltipResearching;
        private List<Component> tooltip = List.of();

        private NodeItem(TechTreeView view, TechNode node, int index, int code, CanvasRect rect) {
            this.view = view;
            this.node = node;
            this.index = index;
            this.code = code;
            this.rect = rect;
        }

        TechNode node() {
            return node;
        }

        private void resolvePrerequisites(Reference2IntOpenHashMap<TechNode> indices) {
            prerequisiteIndices = new int[node.prerequisites.size()];
            for (int i = 0; i < prerequisiteIndices.length; i++) prerequisiteIndices[i] = indices.getInt(node.prerequisites.get(i));
        }

        @Override
        public CanvasRect bounds() {
            return rect;
        }

        /** 数据中心正在研究它（还没解锁）。 */
        private boolean isResearching() {
            return view.isResearching(code) && view.state(index) != TechTreeView.UNLOCKED;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawShape(CanvasPainter painter, boolean hovered) {
            var style = TechTreeStyle.get();
            byte state = view.state(index);
            int fill, border;
            if (isResearching()) {
                float t = pulsePhase();
                fill = ColorUtils.getInterpolatedColor(style.researchingNodeFillLow, style.researchingNodeFillHigh, t);
                border = ColorUtils.getInterpolatedColor(style.researchingNodeBorderLow, style.researchingNodeBorderHigh, t);
            } else {
                switch (state) {
                    case TechTreeView.UNLOCKED -> {
                        fill = style.unlockedNodeFill;
                        border = style.unlockedNodeBorder;
                    }
                    case TechTreeView.AVAILABLE -> {
                        fill = style.availableNodeFill;
                        border = style.availableNodeBorder;
                    }
                    default -> {
                        fill = style.lockedNodeFill;
                        border = style.lockedNodeBorder;
                    }
                }
            }
            // 悬停或选中节点的前置：边框与高亮的连线一起呼吸
            var selected = view.selectedNode();
            if ((painter.hovered() instanceof NodeItem other && other != this && other.node.prerequisites.contains(node)) ||
                    (selected != null && selected != node && selected.prerequisites.contains(node))) {
                border = pulse(style.hoveredDependencyLineColor, border);
            }
            painter.fill(rect, fill);
            painter.outline(rect, BORDER_WIDTH, border);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawContent(CanvasPainter painter, boolean hovered) {
            if (painter.lod() != CanvasLod.FULL) return;
            var graphics = painter.graphics();
            int x = (int) rect.x() + ICON_OFFSET, y = (int) rect.y() + ICON_OFFSET;
            if (node.icon != null) {
                AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, x, y, node.icon);
            } else {
                graphics.drawString(Minecraft.getInstance().font, "?", x + 5, y + 4, TechTreeStyle.get().nodeIconFallback, false);
            }
        }

        /** 未解锁的图标蒙一层灰、悬停提亮（所有节点的蒙层抬到图标之上一次提交）。 */
        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawOverlay(CanvasPainter painter, boolean hovered) {
            if (painter.lod() != CanvasLod.FULL) return;
            var style = TechTreeStyle.get();
            int overlay = view.state(index) == TechTreeView.LOCKED && !isResearching() ? style.lockedNodeOverlay : 0;
            if (hovered) overlay = overlay == 0 ? style.nodeHoverOverlay : ColorUtils.getInterpolatedColor(overlay, style.nodeHoverOverlay, 0.5f);
            if (overlay != 0) painter.fill(rect.inflate(-painter.atLeastPixel(BORDER_WIDTH)), overlay);
        }

        @Override
        public int blockColor() {
            var style = TechTreeStyle.get();
            if (isResearching()) return style.researchingNodeBorderHigh;
            return switch (view.state(index)) {
                case TechTreeView.UNLOCKED -> style.unlockedNodeBorder;
                case TechTreeView.AVAILABLE -> style.availableNodeBorder;
                default -> style.lockedNodeBorder;
            };
        }

        @Override
        public boolean isSelected() {
            return view.isSelected(code);
        }

        @Override
        public List<Component> tooltip() {
            var states = view.statesSnapshot();
            int modCount = TechTreeSavedData.getModCount();
            boolean researching = isResearching();
            if (states != tooltipStates || modCount != tooltipModCount || researching != tooltipResearching) {
                tooltipStates = states;
                tooltipModCount = modCount;
                tooltipResearching = researching;
                tooltip = createTooltip(view.state(index), researching);
            }
            return tooltip;
        }

        private List<Component> createTooltip(byte state, boolean researching) {
            var style = TechTreeStyle.get();
            var lines = new ArrayList<Component>(5 + node.prerequisites.size());
            int stateColor = TechTreeView.stateTooltipColor(state);
            lines.add(node.getDisplayName().withStyle(s -> s.withColor(stateColor)));
            var desc = node.desc();
            if (desc != null) lines.add(desc.withStyle(s -> s.withColor(style.tooltipDescription)));
            lines.add(Component.translatable(TechTreeView.stateKey(state)).withStyle(s -> s.withColor(stateColor)));
            if (researching) lines.add(Component.translatable(TechTreeView.STATUS_RESEARCHING).withStyle(s -> s.withColor(style.tooltipResearching)));
            if (!node.prerequisites.isEmpty()) {
                lines.add(Component.translatable(TechTreeView.PREREQUISITES).withStyle(s -> s.withColor(style.tooltipPrerequisites)));
                var player = view.player();
                var team = player == null ? null : TechTreeSavedData.getTeamUUID(player);
                for (int i = 0; i < prerequisiteIndices.length; i++) {
                    var prerequisite = node.prerequisites.get(i);
                    // 本树的前置用服务端下发的状态；别的树的前置只能看客户端的解锁数据
                    boolean unlocked = prerequisiteIndices[i] >= 0 ? view.state(prerequisiteIndices[i]) == TechTreeView.UNLOCKED :
                            team != null && TechTreeSavedData.isUnlocked(team, prerequisite);
                    var line = Component.literal(unlocked ? " ✔ " : " ✘ ").append(prerequisite.getDisplayName());
                    if (prerequisite.getManager() != node.getManager()) {
                        line.append(" (").append(TechTreeManager.getTreeName(prerequisite.getManager())).append(")");
                    }
                    lines.add(line.withStyle(s -> s.withColor(unlocked ? style.tooltipUnlocked : style.tooltipLocked)));
                }
            }
            lines.add(Component.translatable(TechTreeView.CLICK_FOR_DETAILS).withStyle(ChatFormatting.DARK_GRAY));
            return lines;
        }

        @Override
        @Nullable
        public Object ingredient() {
            return new TechNodeEmiStack(node);
        }
    }

    /** 0~1 之间的呼吸相位（正在研究的节点、高亮连线共用，保持同步）。 */
    private static float pulsePhase() {
        return 0.5f + (float) Math.sin(System.currentTimeMillis() / PULSE_MS_PER_RADIAN) * 0.5f;
    }

    private static int pulse(int highlight, int base) {
        return ColorUtils.getInterpolatedColor(highlight, base, 1 - pulsePhase());
    }
}
