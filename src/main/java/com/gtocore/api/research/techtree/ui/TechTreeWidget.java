package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.TeamResearchContext;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTree;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.api.research.techtree.TechTreeSavedData;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.utils.ColorUtils;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;

import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@DataGeneratorScanned
public class TechTreeWidget extends DraggableScrollableWidgetGroup {

    @RegisterLanguage(cn = "已解锁：%s", en = "Unlocked: %s")
    private static final String UNLOCK_SUCCESS = "gtocore.techtree.widget.unlock_success";
    @RegisterLanguage(cn = "已解锁", en = "Unlocked")
    private static final String STATUS_UNLOCKED = "gtocore.techtree.widget.status.unlocked";
    @RegisterLanguage(cn = "可解锁", en = "Available")
    private static final String STATUS_AVAILABLE = "gtocore.techtree.widget.status.available";
    @RegisterLanguage(cn = "未解锁", en = "Locked")
    private static final String STATUS_LOCKED = "gtocore.techtree.widget.status.locked";
    @RegisterLanguage(cn = "前置科技", en = "Prerequisites")
    private static final String PREREQUISITES = "gtocore.techtree.widget.prerequisites";

    private static final int UPDATE_NODE_STATES = 100;
    private static final byte STATE_LOCKED = 0;
    private static final byte STATE_AVAILABLE = 1;
    private static final byte STATE_UNLOCKED_VALUE = 2;

    private static final int NODE_SIZE = 26;
    private static final int ICON_SIZE = 16;
    private static final int ICON_OFFSET = 5;
    private static final int CONTENT_PADDING = 24;
    private static final int SCROLL_BAR_SIZE = 4;
    private static final int LINE_THICKNESS = 2;
    private static final int TIER_SEPARATOR_DASH_LENGTH = 6;
    private static final int TIER_SEPARATOR_GAP = 4;
    private static final int TIER_SEPARATOR_COLOR = 0x66FFFFFF;
    private static final int HOVERED_DEPENDENCY_LINE_COLOR = 0xFF4DE3E3;
    private static final double MIN_ZOOM = 0.5D;
    private static final double MAX_ZOOM = 3.0D;
    private static final double ZOOM_STEP = 1.15D;
    private static final double CLICK_DRAG_THRESHOLD = 3.0D;

    private final TechTreeManager manager;
    private final @Nullable Function<Player, TeamResearchContext> unlockArgumentsFactory;
    private final TechTreeLayout layout;
    private final List<TechNode> orderedNodes;
    private final Map<TechNode, Integer> nodeIndices;
    private byte[] nodeStates;
    private int contentOffsetX;
    private int contentOffsetY;
    private int contentWidth;
    private int contentHeight;
    private final int minNodeX;
    private final int minNodeY;
    private final int maxNodeX;
    private final int maxNodeY;
    private double zoom = 1.0D;
    private @Nullable TechNode highlightedNode;
    private boolean capturedMouseInteraction;
    @Setter
    @Nullable
    private Consumer<TechNode> onNodeClicked = this::tryUnlock;

    public TechTreeWidget(int x, int y, int width, int height, TechTreeManager manager) {
        this(x, y, width, height, manager, ignored -> null);
    }

    public TechTreeWidget(int x, int y, int width, int height, TechTreeManager manager,
                          @Nullable Function<Player, TeamResearchContext> unlockArgumentsFactory) {
        super(x, y, width, height);
        this.manager = manager;
        this.unlockArgumentsFactory = unlockArgumentsFactory;
        this.layout = manager.getLayout();
        this.orderedNodes = layout.orderedNodes();
        this.nodeIndices = new IdentityHashMap<>();
        for (int i = 0; i < orderedNodes.size(); i++) {
            nodeIndices.put(orderedNodes.get(i), i);
        }
        this.nodeStates = new byte[orderedNodes.size()];
        this.minNodeX = layout.minX();
        this.minNodeY = layout.minY();
        this.maxNodeX = layout.maxX();
        this.maxNodeY = layout.maxY();

        setBackground(GuiTextures.DISPLAY);
        setDraggable(true);
        setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
        setXBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearAllWidgets();
        calculateContentBounds();
        addWidget(new TechTreeCanvas(0, 0, contentWidth, contentHeight));
        for (int i = 0; i < orderedNodes.size(); i++) {
            var node = orderedNodes.get(i);
            addWidget(new NodeWidget(i, node, toCanvasX(node), toCanvasY(node)));
        }
        refreshScrollState();
    }

    private void calculateContentBounds() {
        if (orderedNodes.isEmpty()) {
            int padding = getScaledPadding();
            contentOffsetX = padding;
            contentOffsetY = padding;
            contentWidth = Math.max(getSize().width, padding * 2);
            contentHeight = Math.max(getSize().height, padding * 2);
            return;
        }

        int padding = getScaledPadding();
        contentOffsetX = padding - scaleValue(minNodeX);
        contentOffsetY = padding - scaleValue(minNodeY);
        contentWidth = scaleValue(maxNodeX - minNodeX) + getScaledNodeSize() + padding * 2;
        contentHeight = scaleValue(maxNodeY - minNodeY) + getScaledNodeSize() + padding * 2;
    }

    private void refreshScrollState() {
        boolean horizontal = contentWidth > getSize().width;
        boolean vertical = contentHeight > getSize().height;
        setScrollable(horizontal || vertical);
        setXScrollBarHeight(horizontal ? SCROLL_BAR_SIZE : 0);
        setYScrollBarWidth(vertical ? SCROLL_BAR_SIZE : 0);
        if (!horizontal) {
            setScrollXOffset(0);
        }
        if (!vertical) {
            setScrollYOffset(0);
        }
    }

    private int toCanvasX(TechNode node) {
        return scaleValue(layout.getX(node)) + contentOffsetX;
    }

    private int toCanvasY(TechNode node) {
        return scaleValue(layout.getY(node)) + contentOffsetY;
    }

    private int scaleValue(int value) {
        return Math.round((float) (value * zoom));
    }

    private int getScaledPadding() {
        return Math.max(8, scaleValue(CONTENT_PADDING));
    }

    private int getScaledNodeSize() {
        return Math.max(12, scaleValue(NODE_SIZE));
    }

    private int getScaledIconOffset() {
        return Math.max(2, scaleValue(ICON_OFFSET));
    }

    private int getScaledIconSize() {
        return Math.max(8, scaleValue(ICON_SIZE));
    }

    private int getScaledLineThickness() {
        return Math.max(1, scaleValue(LINE_THICKNESS));
    }

    private int getHorizontalScrollLimit() {
        return Math.max(getMaxWidth() - getSize().width, 0);
    }

    private int getVerticalScrollLimit() {
        return Math.max(getMaxHeight() - getSize().height, 0);
    }

    private void panViewport(double deltaX, double deltaY) {
        setScrollXOffset(Mth.clamp(scrollXOffset - (int) deltaX, 0, getHorizontalScrollLimit()));
        setScrollYOffset(Mth.clamp(scrollYOffset - (int) deltaY, 0, getVerticalScrollLimit()));
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isMouseWithinBounds(double mouseX, double mouseY) {
        return isMouseOver(getPosition().x, getPosition().y, getSize().width, getSize().height, mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    private void zoomAround(double mouseX, double mouseY, double newZoom) {
        double clampedZoom = Mth.clamp(newZoom, MIN_ZOOM, MAX_ZOOM);
        if (Math.abs(clampedZoom - zoom) < 1.0E-4D) {
            return;
        }

        int localMouseX = Mth.floor(mouseX - getPosition().x);
        int localMouseY = Mth.floor(mouseY - getPosition().y);
        int oldPadding = getScaledPadding();
        double logicalX = minNodeX + (scrollXOffset + localMouseX - oldPadding) / zoom;
        double logicalY = minNodeY + (scrollYOffset + localMouseY - oldPadding) / zoom;

        zoom = clampedZoom;
        rebuildWidgets();

        int newPadding = getScaledPadding();
        int targetScrollX = Mth.floor(newPadding + (logicalX - minNodeX) * zoom - localMouseX);
        int targetScrollY = Mth.floor(newPadding + (logicalY - minNodeY) * zoom - localMouseY);
        setScrollXOffset(Mth.clamp(targetScrollX, 0, getHorizontalScrollLimit()));
        setScrollYOffset(Mth.clamp(targetScrollY, 0, getVerticalScrollLimit()));
    }

    private byte[] collectNodeStates(@Nullable Player player) {
        byte[] states = new byte[orderedNodes.size()];
        if (player == null) {
            return states;
        }

        TechTree tree = TechTreeSavedData.getOrCreateTree(player, manager);
        TeamResearchContext unlockContext = unlockArgumentsFactory == null ? null : unlockArgumentsFactory.apply(player);
        for (int i = 0; i < orderedNodes.size(); i++) {
            var node = orderedNodes.get(i);
            if (tree.isUnlocked(node)) {
                states[i] = STATE_UNLOCKED_VALUE;
            } else if (tree.tryUnlock(node, unlockContext, TeamUtil.getTeamUUID(player.getUUID())).isSuccess()) {
                states[i] = STATE_AVAILABLE;
            }
        }
        return states;
    }

    @Override
    public void initWidget() {
        super.initWidget();
        if (isClientSideWidget) {
            nodeStates = collectNodeStates(getGuiPlayer());
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (isClientSideWidget) {
            nodeStates = collectNodeStates(getGuiPlayer());
        }
    }

    public void syncNodeStates() {
        if (isRemote()) {
            return;
        }
        byte[] currentStates = collectNodeStates(getGuiPlayer());
        if (Arrays.equals(nodeStates, currentStates)) {
            return;
        }
        nodeStates = currentStates;
        writeUpdateInfo(UPDATE_NODE_STATES, this::writeNodeStates);
    }

    private void writeNodeStates(FriendlyByteBuf buffer) {
        buffer.writeVarInt(nodeStates.length);
        for (byte state : nodeStates) {
            buffer.writeByte(state);
        }
    }

    private void readNodeStates(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        byte[] states = new byte[orderedNodes.size()];
        int limit = Math.min(count, states.length);
        for (int i = 0; i < limit; i++) {
            states[i] = buffer.readByte();
        }
        for (int i = limit; i < count; i++) {
            buffer.readByte();
        }
        nodeStates = states;
    }

    private @Nullable Player getGuiPlayer() {
        return getGui() == null ? null : getGui().entityPlayer;
    }

    private byte getNodeState(int index) {
        return index >= 0 && index < nodeStates.length ? nodeStates[index] : STATE_LOCKED;
    }

    private byte getNodeState(TechNode node) {
        Integer index = nodeIndices.get(node);
        return index == null ? STATE_LOCKED : getNodeState(index);
    }

    private List<Component> createTooltip(TechNode node, byte state) {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(TechTreeManager.getNodeName(node).copy().withStyle(getFormattingForState(state)));
        var desc = node.desc();
        if (desc != null) {
            tooltips.add(desc.withStyle(ChatFormatting.GRAY));
        }
        tooltips.add(Component.translatable(switch (state) {
            case STATE_UNLOCKED_VALUE -> STATUS_UNLOCKED;
            case STATE_AVAILABLE -> STATUS_AVAILABLE;
            default -> STATUS_LOCKED;
        }).withStyle(getFormattingForState(state)));
        if (!node.prerequisites.isEmpty()) {
            tooltips.add(Component.empty());
            tooltips.add(Component.translatable(PREREQUISITES).withStyle(ChatFormatting.YELLOW));
            for (var prerequisite : node.prerequisites) {
                boolean unlocked = getNodeState(prerequisite) == STATE_UNLOCKED_VALUE;
                tooltips.add(Component.literal(unlocked ? " - " : " x ")
                        .append(TechTreeManager.getNodeName(prerequisite))
                        .withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
        }
        return tooltips;
    }

    private ChatFormatting getFormattingForState(byte state) {
        return switch (state) {
            case STATE_UNLOCKED_VALUE -> ChatFormatting.GREEN;
            case STATE_AVAILABLE -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
    }

    private int getNodeFillColor(byte state) {
        return switch (state) {
            case STATE_UNLOCKED_VALUE -> 0xFF1E4D2B;
            case STATE_AVAILABLE -> 0xFF5A4815;
            default -> 0xFF2F2F34;
        };
    }

    private int getNodeBorderColor(byte state) {
        return switch (state) {
            case STATE_UNLOCKED_VALUE -> 0xFF6CDA84;
            case STATE_AVAILABLE -> 0xFFF1CE67;
            default -> 0xFF8C8C93;
        };
    }

    @OnlyIn(Dist.CLIENT)
    private int getPulsingHighlightColor(int baseColor) {
        return ColorUtils.getInterpolatedColor(HOVERED_DEPENDENCY_LINE_COLOR, baseColor, (float) (0.5 - Math.sin(System.currentTimeMillis() / 200.0D) * 0.5));
    }

    @OnlyIn(Dist.CLIENT)
    private int getNodeBorderColor(TechNode node, byte state) {
        int baseColor = getNodeBorderColor(state);
        if (highlightedNode != null && highlightedNode.prerequisites.contains(node)) {
            return getPulsingHighlightColor(baseColor);
        }
        return baseColor;
    }

    private int getLineColor(TechNode prerequisite, TechNode node, @Nullable TechNode hoveredNode) {
        int value = 0xFF4F4F57;
        byte prerequisiteState = getNodeState(prerequisite);
        byte nodeState = getNodeState(node);
        if (nodeState == STATE_UNLOCKED_VALUE) {
            value = 0xFF5CC978;
        }
        if (nodeState == STATE_AVAILABLE && prerequisiteState == STATE_UNLOCKED_VALUE) {
            value = 0xFFE3C45D;
        }
        if (prerequisiteState == STATE_UNLOCKED_VALUE) {
            value = 0xFF7A7A82;
        }
        if (hoveredNode == node) {
            return getPulsingHighlightColor(value);
        }
        return value;
    }

    public void tryUnlock(TechNode node) {
        Player player = getGuiPlayer();
        if (player == null) {
            return;
        }

        TechTree tree = TechTreeSavedData.getOrCreateTree(player, manager);
        if (tree.isUnlocked(node)) {
            return;
        }

        TeamResearchContext unlockContext = unlockArgumentsFactory == null ? null : unlockArgumentsFactory.apply(player);
        ActionResult result = tree.tryUnlock(node, unlockContext, TeamUtil.getTeamUUID(player.getUUID()));
        if (!result.isSuccess()) {
            if (result.reason() != null) {
                player.displayClientMessage(result.reason(), true);
            }
            return;
        }

        if (TechTreeSavedData.unlock(player, node, unlockContext)) {
            player.displayClientMessage(Component.translatable(UNLOCK_SUCCESS, TechTreeManager.getNodeName(node)), true);
            syncNodeStates();
        }
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        nodeStates = collectNodeStates(getGuiPlayer());
        writeNodeStates(buffer);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        readNodeStates(buffer);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        syncNodeStates();
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == UPDATE_NODE_STATES) {
            readNodeStates(buffer);
            return;
        }
        super.readUpdateInfo(id, buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseWithinBounds(mouseX, mouseY)) {
            return false;
        }
        if (wheelDelta != 0 && isCtrlDown()) {
            zoomAround(mouseX, mouseY, wheelDelta > 0 ? zoom * ZOOM_STEP : zoom / ZOOM_STEP);
            setFocus(true);
            return true;
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseWithinBounds(mouseX, mouseY)) {
            return false;
        }
        capturedMouseInteraction = super.mouseClicked(mouseX, mouseY, button);
        return capturedMouseInteraction;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!isMouseWithinBounds(mouseX, mouseY)) {
            return false;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean insideBounds = isMouseWithinBounds(mouseX, mouseY);
        if (!insideBounds && !capturedMouseInteraction) {
            return false;
        }
        try {
            return super.mouseReleased(mouseX, mouseY, button);
        } finally {
            capturedMouseInteraction = false;
        }
    }

    private final class TechTreeCanvas extends Widget {

        private TechTreeCanvas(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            Position pos = getPosition();
            int nodeSize = getScaledNodeSize();
            drawTierSeparators(graphics, pos, nodeSize);
            highlightedNode = TechTreeWidget.this.isMouseWithinBounds(mouseX, mouseY) ? findHoveredNode(mouseX, mouseY, pos, nodeSize) : null;
            for (var node : orderedNodes) {
                if (node == highlightedNode) {
                    continue;
                }
                drawDependencyLines(graphics, pos, nodeSize, node, highlightedNode);
            }
            if (highlightedNode != null) {
                drawDependencyLines(graphics, pos, nodeSize, highlightedNode, highlightedNode);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawTierSeparators(GuiGraphics graphics, Position pos, int nodeSize) {
            List<TechTreeLayout.TierRegion> tierRegions = layout.tierRegions();
            if (tierRegions.size() < 2) {
                return;
            }

            int inset = Math.max(2, getScaledPadding() / 2);
            int startY = pos.y + inset;
            int endY = pos.y + getSize().height - inset;
            for (int i = 0; i < tierRegions.size() - 1; i++) {
                var left = tierRegions.get(i);
                var right = tierRegions.get(i + 1);
                int leftRegionRight = pos.x + scaleValue(left.maxX()) + contentOffsetX + nodeSize;
                int rightRegionLeft = pos.x + scaleValue(right.minX()) + contentOffsetX;
                int separatorX = (leftRegionRight + rightRegionLeft) / 2;
                drawVerticalDashedLine(graphics, separatorX, startY, endY, TIER_SEPARATOR_COLOR);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawDependencyLines(GuiGraphics graphics, Position pos, int nodeSize, TechNode node, @Nullable TechNode hoveredNode) {
            int endX = pos.x + toCanvasX(node) + nodeSize / 2;
            int endY = pos.y + toCanvasY(node) + nodeSize / 2;
            for (var prerequisite : node.prerequisites) {
                int startX = pos.x + toCanvasX(prerequisite) + nodeSize / 2;
                int startY = pos.y + toCanvasY(prerequisite) + nodeSize / 2;
                int middleX = (startX + endX) / 2;
                int color = getLineColor(prerequisite, node, hoveredNode);
                drawHorizontalLine(graphics, startX, middleX, startY, color);
                drawVerticalLine(graphics, middleX, startY, endY, color);
                drawHorizontalLine(graphics, middleX, endX, endY, color);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private @Nullable TechNode findHoveredNode(double mouseX, double mouseY, Position canvasPosition, int nodeSize) {
            for (int i = orderedNodes.size() - 1; i >= 0; i--) {
                TechNode node = orderedNodes.get(i);
                int nodeX = canvasPosition.x + toCanvasX(node);
                int nodeY = canvasPosition.y + toCanvasY(node);
                if (isMouseOver(nodeX, nodeY, nodeSize, nodeSize, mouseX, mouseY)) {
                    return node;
                }
            }
            return null;
        }

        @OnlyIn(Dist.CLIENT)
        private void drawHorizontalLine(GuiGraphics graphics, int startX, int endX, int y, int color) {
            int minX = Math.min(startX, endX);
            int maxX = Math.max(startX, endX);
            int thickness = getScaledLineThickness();
            graphics.fill(minX, y - thickness / 2, maxX + 1, y - thickness / 2 + thickness, color);
        }

        @OnlyIn(Dist.CLIENT)
        private void drawVerticalLine(GuiGraphics graphics, int x, int startY, int endY, int color) {
            int minY = Math.min(startY, endY);
            int maxY = Math.max(startY, endY);
            int thickness = getScaledLineThickness();
            graphics.fill(x - thickness / 2, minY, x - thickness / 2 + thickness, maxY + 1, color);
        }

        @OnlyIn(Dist.CLIENT)
        private void drawVerticalDashedLine(GuiGraphics graphics, int x, int startY, int endY, int color) {
            int minY = Math.min(startY, endY);
            int maxY = Math.max(startY, endY);
            int thickness = Math.max(1, scaleValue(1));
            int dashLength = Math.max(2, scaleValue(TIER_SEPARATOR_DASH_LENGTH));
            int gap = Math.max(2, scaleValue(TIER_SEPARATOR_GAP));
            for (int y = minY; y <= maxY; y += dashLength + gap) {
                int dashEnd = Math.min(y + dashLength, maxY + 1);
                graphics.fill(x - thickness / 2, y, x - thickness / 2 + thickness, dashEnd, color);
            }
        }
    }

    private final class NodeWidget extends Widget implements DraggableScrollableWidgetGroup.IDraggable {

        private static final int CLICK_ACTION = 1;
        private final int index;
        private final TechNode node;
        private double totalDraggedDistance;

        private NodeWidget(int index, TechNode node, int x, int y) {
            super(x, y, getScaledNodeSize(), getScaledNodeSize());
            this.index = index;
            this.node = node;
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            if (id == CLICK_ACTION && onNodeClicked != null) {
                onNodeClicked.accept(node);
                return;
            }
            super.handleClientAction(id, buffer);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            Position pos = getPosition();
            int nodeSize = getSize().width;
            byte state = getNodeState(index);
            DrawerHelper.drawSolidRect(graphics, pos.x, pos.y, nodeSize, nodeSize, getNodeFillColor(state));
            DrawerHelper.drawBorder(graphics, pos.x, pos.y, nodeSize, nodeSize, getNodeBorderColor(node, state), 1);
            if (state == STATE_LOCKED) {
                DrawerHelper.drawSolidRect(graphics, pos.x + 1, pos.y + 1, nodeSize - 2, nodeSize - 2, 0x55000000);
            }

            drawNodeIcon(graphics, pos.x + getScaledIconOffset(), pos.y + getScaledIconOffset());
        }

        @OnlyIn(Dist.CLIENT)
        private void drawNodeIcon(GuiGraphics graphics, int x, int y) {
            int iconSize = getScaledIconSize();
            if (node.icon != null) {
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                float scale = iconSize / 16.0F;
                graphics.pose().scale(scale, scale, 1.0F);
                AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, 0, 0, node.icon);
                graphics.pose().popPose();
            } else {
                float textScale = Math.max(iconSize / 8.0F, 1.0F);
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0);
                graphics.pose().scale(textScale, textScale, 1.0F);
                graphics.drawString(Minecraft.getInstance().font, "?", 1, 0, 0xFFFFFFFF, false);
                graphics.pose().popPose();
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            if (!isMouseOverNode(mouseX, mouseY) || !TechTreeWidget.this.isMouseWithinBounds(mouseX, mouseY)) {
                return;
            }

            Position pos = getPosition();
            DrawerHelper.drawBorder(graphics, pos.x - 1, pos.y - 1, getSize().width + 2, getSize().height + 2, 0xFFFFFFFF, 1);
            setHoverTooltips(createTooltip(node, getNodeState(index)));
        }

        @Override
        protected void drawTooltipTexts(int mouseX, int mouseY) {
            if (!isMouseOverNode(mouseX, mouseY) || !TechTreeWidget.this.isMouseWithinBounds(mouseX, mouseY)) {
                return;
            }
            super.drawTooltipTexts(mouseX, mouseY);
        }

        @Override
        public boolean allowSelected(double mouseX, double mouseY, int button) {
            return button == 0 && isMouseOverNode(mouseX, mouseY) && TechTreeWidget.this.isMouseWithinBounds(mouseX, mouseY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void startDrag(double mouseX, double mouseY) {
            totalDraggedDistance = 0;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean dragging(double mouseX, double mouseY, double deltaX, double deltaY) {
            totalDraggedDistance += Math.abs(deltaX) + Math.abs(deltaY);
            panViewport(deltaX, deltaY);
            return false;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void endDrag(double mouseX, double mouseY) {
            if (totalDraggedDistance <= CLICK_DRAG_THRESHOLD && isMouseOverNode(mouseX, mouseY)) {
                if (!isClientSideWidget) writeClientAction(CLICK_ACTION, buf -> {});
                else if (onNodeClicked != null) {
                    onNodeClicked.accept(node);
                }
                playButtonClickSound();
            }
        }

        @OnlyIn(Dist.CLIENT)
        private boolean isMouseOverNode(double mouseX, double mouseY) {
            if (!TechTreeWidget.this.isMouseWithinBounds(mouseX, mouseY)) {
                return false;
            }
            Position position = getPosition();
            return isMouseOver(position.x, position.y, getSize().width, getSize().height, mouseX, mouseY);
        }
    }
}
