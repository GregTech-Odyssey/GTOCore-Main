package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.TechTreeSavedData;
import com.gtocore.integration.emi.research.TechNodeEmiStack;

import com.gregtechceu.gtceu.uipro.animation.ColorMath;
import com.gregtechceu.gtceu.uipro.animation.UIClock;
import com.gregtechceu.gtceu.uipro.canvas.CanvasGrid;
import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLod;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.canvas.CanvasRoute;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.canvas.ItemLayer;
import com.gregtechceu.gtceu.uipro.render.UIPixels;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 科技树在画布上的内容（只在客户端建）。
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
    private static final float TIER_MARGIN = 20;
    private static final int TIER_HEADER_HEIGHT = 12;
    private static final float TIER_HEADER_WORLD = 12;
    private static final int TIER_CROSS_ARM = 3;

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
        var tiers = new TierBands(canvas, layout, items.bounds());
        canvas.addLayer(tiers.bands);
        canvas.addLayer(new EdgeLayer(view, layout, indices));
        canvas.addLayer(items);
        canvas.addLayer(tiers.headers);
    }

    // ==================== 数据等级分区 ====================

    /** 数据等级分区：整个画布按等级切成竖向色带（首尾两条延伸到无穷），深浅交替；标题条始终贴在视口顶部，文字按 1 倍像素画。 */
    private static final class TierBands {

        private final CanvasView canvas;
        private final List<TechTreeLayout.TierRegion> regions;
        private final float[] separators;
        private final Component[] labels;
        private final Component[] shortLabels;
        @Nullable
        private final CanvasRect bounds;

        private TierBands(CanvasView canvas, TechTreeLayout layout, @Nullable CanvasRect nodes) {
            this.canvas = canvas;
            this.regions = layout.tierRegions();
            int count = regions.size();
            this.separators = new float[Math.max(0, count - 1)];
            for (int i = 0; i < separators.length; i++) separators[i] = layout.tierSeparators().getInt(i);
            this.labels = new Component[count];
            this.shortLabels = new Component[count];
            for (int i = 0; i < count; i++) {
                String tier = Integer.toString(regions.get(i).tier());
                labels[i] = Component.translatable(TechNodeDetails.TIER).append(" " + tier);
                shortLabels[i] = Component.literal(tier);
            }
            this.bounds = nodes == null || count < 2 ? null :
                    CanvasRect.of(nodes.x(), nodes.y() - TIER_MARGIN - TIER_HEADER_WORLD, nodes.width(), nodes.height() + 2 * TIER_MARGIN + TIER_HEADER_WORLD);
        }

        @OnlyIn(Dist.CLIENT)
        private void toScreen(CanvasPainter painter) {
            float scale = painter.scale();
            var view = canvas.visibleRect();
            var pose = painter.graphics().pose();
            pose.scale(1 / scale, 1 / scale, 1);
            pose.translate(-UIPixels.snap(canvas.viewportX() - view.x() * scale), -UIPixels.snap(canvas.viewportY() - view.y() * scale), 0);
        }

        private boolean shown() {
            return bounds != null;
        }

        private int[] laneEdges(float scale, CanvasRect view, int vx, int vw) {
            int count = regions.size();
            int[] xs = new int[count + 1];
            xs[0] = vx;
            xs[count] = vx + vw;
            for (int i = 1; i < count; i++) xs[i] = Mth.clamp(Math.round(vx + (separators[i - 1] - view.x()) * scale), vx, vx + vw);
            return xs;
        }

        private final CanvasLayer bands = new CanvasLayer() {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
                var style = TechTreeStyle.get();
                var pose = painter.graphics().pose();
                var view = canvas.visibleRect();
                int vx = canvas.viewportX(), vy = canvas.viewportY(), vw = canvas.viewportWidth(), vh = canvas.viewportHeight();
                painter.flush();
                pose.pushPose();
                toScreen(painter);
                if (shown()) {
                    int[] xs = laneEdges(painter.scale(), view, vx, vw);
                    for (int i = 0; i < regions.size(); i++) {
                        painter.fill(xs[i], vy, xs[i + 1], vy + vh, (i & 1) == 0 ? style.tierBandEven : style.tierBandOdd);
                    }
                    painter.flush();
                }
                Backdrop.draw(painter, style, view, vx, vy, vw, vh);
                pose.popPose();
            }

            @Override
            @Nullable
            public CanvasRect bounds() {
                return bounds;
            }
        };

        private final CanvasLayer headers = new CanvasLayer() {

            @Override
            @OnlyIn(Dist.CLIENT)
            public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
                if (!shown()) return;
                var style = TechTreeStyle.get();
                var graphics = painter.graphics();
                var pose = graphics.pose();
                var font = Minecraft.getInstance().font;
                int vx = canvas.viewportX(), vy = canvas.viewportY(), vw = canvas.viewportWidth(), visibleRight = vx + canvas.unobstructedWidth();
                painter.flush();
                pose.pushPose();
                toScreen(painter);
                int count = regions.size();
                int[] xs = laneEdges(painter.scale(), canvas.visibleRect(), vx, vw);
                for (int i = 0; i < count; i++) {
                    painter.fill(xs[i], vy, xs[i + 1], vy + TIER_HEADER_HEIGHT, (i & 1) == 0 ? style.tierHeaderEven : style.tierHeaderOdd);
                    if (i > 0 && xs[i] > vx && xs[i] < vx + vw) painter.fill(xs[i], vy, xs[i] + 1, vy + TIER_HEADER_HEIGHT - 1, style.tierHeaderDivider);
                }
                painter.fill(vx, vy + TIER_HEADER_HEIGHT - 1, vx + vw, vy + TIER_HEADER_HEIGHT, style.tierHeaderShade);
                painter.flush();
                boolean first = true;
                for (int i = 0; i < count; i++) {
                    int left = xs[i] + 1, right = Math.min(xs[i + 1] - 1, visibleRight);
                    if (right - left < 4) continue;
                    var label = first && font.width(labels[i]) + 4 <= right - left ? labels[i] : shortLabels[i];
                    first = false;
                    int textWidth = font.width(label);
                    if (textWidth + 2 > right - left) continue;
                    UIText.drawLeft(graphics, label, left + (right - left - textWidth + 1) / 2, UIText.centerY(vy, TIER_HEADER_HEIGHT), style.tierHeaderText);
                }
                pose.popPose();
            }
        };
    }

    /** 背景几何网格：两级大格（细格不画）的线与交点十字，都比底色亮；依赖连线比底色暗，两者不会混。 */
    private static final class Backdrop {

        private static final int MAX_CROSSES = 4096;
        @Nullable
        private static TechTreeStyle gridStyle;
        @Nullable
        private static CanvasGrid grid;

        @OnlyIn(Dist.CLIENT)
        static void draw(CanvasPainter painter, TechTreeStyle style, CanvasRect view, int vx, int vy, int vw, int vh) {
            if (gridStyle != style || grid == null) {
                gridStyle = style;
                grid = new CanvasGrid(UISizes.CANVAS_GRID_SIZE, UISizes.CANVAS_GRID_MIN_PIXELS, 4, style.gridLine & 0xFFFFFF, style.gridLine);
            }
            float scale = painter.scale(), ox = view.x(), oy = view.y();
            grid.draw(painter, scale, ox, oy, vx, vy, vw, vh);
            int lineAlpha = Math.max(1, style.gridLine >>> 24), crossAlpha = style.gridCross >>> 24;
            for (var level : grid.levels(scale)) {
                int alpha = crossAlpha * (level.color() >>> 24) / lineAlpha;
                if (alpha <= 0) continue;
                int color = alpha << 24 | (style.gridCross & 0xFFFFFF);
                float cell = level.cellSize();
                long firstX = (long) Math.floor(ox / cell), lastX = (long) Math.ceil((ox + view.width()) / cell);
                long firstY = (long) Math.floor(oy / cell), lastY = (long) Math.ceil((oy + view.height()) / cell);
                if ((lastX - firstX + 1) * (lastY - firstY + 1) > MAX_CROSSES) continue;
                int skip = level.skipEvery();
                for (long i = firstX; i <= lastX; i++) {
                    float sx = UIPixels.snap(vx + (i * cell - ox) * scale);
                    if (sx - TIER_CROSS_ARM < vx || sx + TIER_CROSS_ARM >= vx + vw) continue;
                    for (long j = firstY; j <= lastY; j++) {
                        if (skip > 0 && Math.floorMod(i, skip) == 0 && Math.floorMod(j, skip) == 0) continue;
                        float sy = UIPixels.snap(vy + (j * cell - oy) * scale);
                        if (sy - TIER_CROSS_ARM < vy || sy + TIER_CROSS_ARM >= vy + vh) continue;
                        painter.fill(sx - TIER_CROSS_ARM, sy, sx, sy + 1, color);
                        painter.fill(sx + 1, sy, sx + 1 + TIER_CROSS_ARM, sy + 1, color);
                        painter.fill(sx, sy - TIER_CROSS_ARM, sx + 1, sy + 1 + TIER_CROSS_ARM, color);
                    }
                }
            }
            painter.flush();
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
            return highlighted ? UIClock.mix(style.hoveredDependencyLineColor, color) : color;
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
                float t = UIClock.pulse();
                fill = ColorMath.lerp(style.researchingNodeFillLow, style.researchingNodeFillHigh, t);
                border = ColorMath.lerp(style.researchingNodeBorderLow, style.researchingNodeBorderHigh, t);
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
                border = UIClock.mix(style.hoveredDependencyLineColor, border);
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
            if (hovered) overlay = overlay == 0 ? style.nodeHoverOverlay : ColorMath.lerp(overlay, style.nodeHoverOverlay, 0.5f);
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
}
