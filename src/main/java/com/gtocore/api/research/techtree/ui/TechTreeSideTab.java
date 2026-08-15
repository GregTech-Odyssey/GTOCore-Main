package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchContext;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.common.data.GTOCodecs;
import com.gtocore.integration.emi.research.EmiResearchHelper;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;
import com.gtocore.integration.emi.research.TechNodeEmiStack;
import com.gtocore.utils.GuiHelper;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.utils.ColorUtils;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;

import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

@DataGeneratorScanned
public class TechTreeSideTab extends DraggableScrollableWidgetGroup {

    @RegisterLanguage(cn = "CWU", en = "CWU")
    private static final String CWU_LABEL = "gtocore.research.side_tab.cwu";
    @RegisterLanguage(cn = "CWU(尤里卡！)", en = "CWU(Eureka!)")
    private static final String CWU_EUREKA_LABEL = "gtocore.research.side_tab.cwu_eureka";
    @RegisterLanguage(cn = "尤里卡为该节点提供了%s%%的进度加成", en = "Eureka! provides %s%% progress bonus for this node")
    private static final String CWU_EUREKA_DESC = "gtocore.research.side_tab.cwu_eureka_desc";
    @RegisterLanguage(cn = "扫描%s以触发尤里卡，提供%s%%研究进度加成", en = "Scan %s to trigger Eureka! and provide %s%% research progress bonus")
    private static final String CWU_NO_EUREKA_DESC = "gtocore.research.side_tab.cwu_eureka_scan_desc";
    @RegisterLanguage(cn = "[数据等级%s]", en = "[Tier %s]")
    private static final String TIER_LABEL = "gtocore.research.side_tab.tier";
    @RegisterLanguage(cn = "该等级的节点解锁的配方数据需要%s导出", en = "Unlocking recipes at this tier requires %s to export")
    private static final String TIER_DESC = "gtocore.research.side_tab.tier_desc";
    @RegisterLanguage(cn = "解锁需求：", en = "Unlock Requirements:")
    private static final String REQUIREMENTS_LABEL = "gtocore.research.side_tab.requirements";
    @RegisterLanguage(cn = "前置节点：", en = "Prerequisites:")
    private static final String PREREQUISITES_LABEL = "gtocore.research.side_tab.prerequisites";
    @RegisterLanguage(cn = "点击跳转", en = "Click to navigate")
    private static final String NAVIGATE_LABEL = "gtocore.research.side_tab.navigate";

    private static final int UPDATE_SYNC_STATE = 100;
    private static final int ACTION_SET_NODE = 947;

    private static final int OUTER_PADDING = 4;
    private static final int CONTENT_PADDING = 5;
    private static final int HEADER_HEIGHT = 50;
    private static final int HEADER_ICON_SIZE = 32;
    private static final int HEADER_TEXT_GAP = 6;
    private static final int HEADER_SECTION_GAP = 8;
    private static final int ROW_HEIGHT = 12;
    private static final int ROW_GAP = 4;
    private static final int MAX_VISIBLE_REQUIREMENT_ROWS = 3;
    private static final int INNER_CONTENT_SECTION_GAP = 8;
    private static final int INNER_CONTENT_MIN_HEIGHT = 28;
    private static final int VALUE_WIDTH = 42;
    private static final int PROGRESS_INSET = 1;
    private static final int PROGRESS_TEXT_X = 4;
    private static final int RECIPE_SLOT_SIZE = 18;
    private static final int RECIPE_SLOT_GAP = 2;
    private static final int RECIPE_LABEL_GAP = 2;
    private static final int PREREQUISITE_SLOT_SIZE = 18;
    private static final int PREREQUISITE_SLOT_GAP = 2;
    private static final int PREREQUISITE_LABEL_GAP = 4;

    private static final int NODE_BOX_FILL = 0xFF2F2F34;
    private static final int NODE_BOX_BORDER = 0xFF8C8C93;
    private static final int HEADER_NAME_COLOR = 0xFFF3F3F3;
    private static final int HEADER_DESC_COLOR = 0xFFB9B9C0;
    private static final int ROW_BACKGROUND = 0xFF232328;
    private static final int ROW_TEXT_COLOR = 0xFFF3F3F3;
    private static final int ROW_VALUE_COLOR = 0xFFD4D4DB;
    private static final int ROW_COMPLETE_VALUE_COLOR = 0xFF6CDA84;
    private static final int CWU_BAR_COLOR = 0xFF39C5BB;
    private static final int CWU_BAR_BORDER = 0xFF8BE7DE;

    private TechTreeManager manager;
    private final Function<Player, TeamResearchContext> contextFactory;
    private Consumer<TechNode> onNodeNavigate = this::navigateToNode;
    private SyncState currentState = SyncState.hidden();
    private SyncState lastSentState = SyncState.hidden();
    private @Nullable TechNode selectedNode;
    private @Nullable SyncState cachedRowsState;
    private @Nullable TechNode cachedRowsNode;
    private List<RowState> cachedRows = Collections.emptyList();

    private final ContentWidget contentWidget;
    private final WidgetGroup innerContent;

    public TechTreeSideTab(int x, int y, int width, int height,
                           TechTreeManager manager,
                           Function<Player, TeamResearchContext> contextFactory) {
        this(x, y, width, height, manager, contextFactory, null);
    }

    public TechTreeSideTab(int x, int y, int width, int height,
                           TechTreeManager manager,
                           Function<Player, TeamResearchContext> contextFactory,
                           @Nullable Widget contentWidget) {
        super(x, y, width, height);
        this.manager = manager;
        this.contextFactory = contextFactory;
        this.contentWidget = new ContentWidget(OUTER_PADDING, OUTER_PADDING,
                width - OUTER_PADDING * 2, height - OUTER_PADDING * 2);
        this.innerContent = new WidgetGroup(0, 0, 0, 0);
        setYScrollBarWidth(2);
        setYBarStyle(GuiTextures.BACKGROUND_INVERSE, GuiTextures.BUTTON);
        setInnerContent(contentWidget);
        setBackground(GuiTextures.BACKGROUND_INVERSE, GuiTextures.DISPLAY);
        addWidget(this.contentWidget);
        addWidget(innerContent);
        setVisible(false).setActive(false);
    }

    public void toggleNode(TechNode node) {
        showNode(selectedNode == node ? null : node);
    }

    public void setOnNodeNavigate(Consumer<TechNode> onNodeNavigate) {
        this.onNodeNavigate = Objects.requireNonNull(onNodeNavigate, "onNodeNavigate");
    }

    private void navigateToNode(TechNode node) {
        setManager(node.getManager());
        showNode(node);
    }

    public void setManager(TechTreeManager manager) {
        if (this.manager == manager) {
            return;
        }
        this.manager = manager;
        selectedNode = null;
        currentState = SyncState.hidden();
        lastSentState = SyncState.hidden();
        cachedRowsState = null;
        cachedRowsNode = null;
        cachedRows = Collections.emptyList();
        contentWidget.updateNodeLayout(null);
        setScrollYOffset(0);
        setVisible(false).setActive(false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var mouseX = GuiHelper.getRealMouseX();
        var mouseY = GuiHelper.getRealMouseY();
        if (isMouseOver()) {
            var ing = getXEIIngredientOverMouse(mouseX, mouseY);
            if (ing != null) {
                return EmiScreenManager.stackInteraction(new EmiStackInteraction((EmiIngredient) ing, null, true),
                        bind -> bind.matchesKey(keyCode, scanCode));
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver()) {
            var ing = getXEIIngredientOverMouse(mouseX, mouseY);
            if (ing instanceof ResearchTagEmiStack || ing instanceof TechNodeEmiStack) {
                return EmiScreenManager.stackInteraction(new EmiStackInteraction((EmiIngredient) ing, null, true),
                        bind -> bind.matchesMouse(button));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    public void showNode(@Nullable TechNode node) {
        selectedNode = node;
        contentWidget.updateNodeLayout(node);
        setScrollYOffset(0);
        setVisible(node != null).setActive(node != null);
        if (isClientSideWidget) {
            if (getGuiPlayer() != null) {
                applyState(buildState());
            }
            return;
        } else if (isRemote()) {
            writeClientAction(ACTION_SET_NODE, buffer -> GTOCodecs.TECH_NODE_STREAM_CODEC.encode(buffer, node));
        }
        syncState();
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == ACTION_SET_NODE) {
            showNode(GTOCodecs.TECH_NODE_STREAM_CODEC.decode(buffer));
        } else {
            super.handleClientAction(id, buffer);
        }
    }

    public @Nullable TechNode getSelectedNode() {
        return selectedNode;
    }

    public void setInnerContent(@Nullable Widget contentWidget) {
        innerContent.clearAllWidgets();
        if (contentWidget == null) {
            return;
        }
        contentWidget.setSelfPosition(4, 4);
        innerContent.addWidget(contentWidget);
        innerContent.setSize(contentWidget.getSizeWidth() + 8, contentWidget.getSizeHeight() + 8);
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        SyncState state = buildState();
        applyState(state);
        lastSentState = state;
        writeState(buffer, state);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        applyState(readState(buffer));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        syncState();
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == UPDATE_SYNC_STATE) {
            applyState(readState(buffer));
            return;
        }
        super.readUpdateInfo(id, buffer);
    }

    private void syncState() {
        if (isRemote()) {
            return;
        }
        SyncState state = buildState();
        applyState(state);
        if (state.equals(lastSentState)) {
            return;
        }
        lastSentState = state;
        writeUpdateInfo(UPDATE_SYNC_STATE, buffer -> writeState(buffer, state));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (isRemote()) {
            applyState(buildState());
        }
    }

    private SyncState buildState() {
        if (selectedNode == null) {
            return SyncState.hidden();
        }
        Player player = getGuiPlayer();
        if (player == null) {
            return SyncState.hidden();
        }

        TeamResearchContext context = contextFactory.apply(player);
        ResearchRequirements requirements = selectedNode.getRequirements();
        if (requirements == null) {
            return new SyncState(true, selectedNode.name, false, 0L, 0L, false, false, Collections.emptyList());
        }

        boolean hasEureka = requirements.getEurekaItem() != null;
        boolean eurekaScanned = hasEureka && context.scannedItems().contains(requirements.getEurekaItem());
        long cwuNeeded = requirements.getCwuNeeded();
        long cwuCurrent = context.techNodeAccCWU().getOrDefault(selectedNode, 0L);

        List<MaterialState> materials = new ArrayList<>();
        List<Map.Entry<ResearchTag, Long>> entries = new ArrayList<>();
        for (var it = requirements.getMaterialNeeded().reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            entries.add(Map.entry(entry.getKey(), entry.getLongValue()));
        }
        entries.sort(Comparator.comparing(entry -> entry.getKey().getName()));
        for (var entry : entries) {
            ResearchTag tag = entry.getKey();
            materials.add(new MaterialState(tag.getName(), context.researchPoints().getOrDefault(tag, 0L), entry.getValue()));
        }

        return new SyncState(true, selectedNode.name, true, cwuCurrent, cwuNeeded, hasEureka, eurekaScanned, List.copyOf(materials));
    }

    private void applyState(SyncState state) {
        currentState = state;
        selectedNode = state.nodeName() == null ? null : manager.getNode(state.nodeName());
        contentWidget.updateNodeLayout(selectedNode);
        boolean visible = state.visible() && selectedNode != null;
        setVisible(visible).setActive(visible);
    }

    private void writeState(FriendlyByteBuf buffer, SyncState state) {
        buffer.writeBoolean(state.visible());
        if (!state.visible()) {
            return;
        }
        buffer.writeUtf(state.nodeName());
        buffer.writeBoolean(state.showCwu());
        buffer.writeLong(state.cwuCurrent());
        buffer.writeLong(state.cwuNeeded());
        buffer.writeBoolean(state.hasEureka());
        buffer.writeBoolean(state.eurekaScanned());
        buffer.writeVarInt(state.materials().size());
        for (MaterialState material : state.materials()) {
            buffer.writeUtf(material.tagid());
            buffer.writeLong(material.current());
            buffer.writeLong(material.needed());
        }
    }

    private SyncState readState(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return SyncState.hidden();
        }
        String nodeName = buffer.readUtf();
        boolean showCwu = buffer.readBoolean();
        long cwuCurrent = buffer.readLong();
        long cwuNeeded = buffer.readLong();
        boolean hasEureka = buffer.readBoolean();
        boolean eurekaScanned = buffer.readBoolean();
        int materialCount = buffer.readVarInt();
        List<MaterialState> materials = new ArrayList<>(materialCount);
        for (int i = 0; i < materialCount; i++) {
            materials.add(new MaterialState(buffer.readUtf(), buffer.readLong(), buffer.readLong()));
        }
        return new SyncState(true, nodeName, showCwu, cwuCurrent, cwuNeeded, hasEureka, eurekaScanned, List.copyOf(materials));
    }

    private @Nullable Player getGuiPlayer() {
        return getGui() == null ? null : getGui().entityPlayer;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isMouseOver() {
        var mouseX = GuiHelper.getRealMouseX();
        var mouseY = GuiHelper.getRealMouseY();
        return isMouseOverElement(mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    private List<RowState> buildRows() {
        if (currentState.equals(cachedRowsState) && selectedNode == cachedRowsNode) {
            return cachedRows;
        }

        List<RowState> rows = new ArrayList<>(1 + currentState.materials().size());
        if (currentState.showCwu()) {
            var eureka = currentState.hasEureka() && currentState.eurekaScanned();
            Component label = Component.translatable(eureka ? CWU_EUREKA_LABEL : CWU_LABEL);
            if (selectedNode != null) {
                var requirements = selectedNode.getRequirements();
                if (requirements != null) {
                    rows.add(new RowState(label, currentState.cwuCurrent(), currentState.cwuNeeded(), eureka ? requirements.getEurekaProgress() : 0f,
                            CWU_BAR_COLOR, CWU_BAR_BORDER, createCwuTooltip(), null));
                }
            }
        }
        for (MaterialState material : currentState.materials()) {
            var tag = ResearchTag.TAGS.get(material.tagid());
            rows.add(new RowState(tag.getDisplayName(), material.current(), material.needed(), 0f,
                    tag.getColor(),
                    ColorUtils.getInterpolatedColor(0xffffffff, tag.getColor(), 0.5f),
                    null, tag));
        }
        cachedRowsState = currentState;
        cachedRowsNode = selectedNode;
        cachedRows = List.copyOf(rows);
        return cachedRows;
    }

    @OnlyIn(Dist.CLIENT)
    private @Nullable Component createCwuTooltip() {
        if (!currentState.hasEureka() || selectedNode == null) {
            return null;
        }
        ResearchRequirements requirements = selectedNode.getRequirements();
        if (requirements == null || requirements.getEurekaItem() == null) {
            return null;
        }
        String eurekaBonus = FormattingUtil.formatNumber2Places(requirements.getEurekaProgress() * 100f);
        if (currentState.eurekaScanned()) {
            return Component.translatable(CWU_EUREKA_DESC, eurekaBonus);
        }
        return Component.translatable(CWU_NO_EUREKA_DESC, requirements.getEurekaItem().getDisplayName(), eurekaBonus);
    }

    @OnlyIn(Dist.CLIENT)
    private static @Nullable Component createTierTooltip(TechNode node) {
        if (node == null) {
            return null;
        }
        var tierItem = node.getTierItem();
        if (tierItem == null) {
            return null;
        }
        return Component.translatable(TIER_DESC, tierItem.getHoverName().copy().withStyle(ChatFormatting.AQUA));
    }

    @OnlyIn(Dist.CLIENT)
    private void drawNodeIcon(GuiGraphics graphics, int x, int y, int size, @Nullable TechNode node) {
        if (node != null && node.icon != null) {
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            float scale = size / 16.0F;
            graphics.pose().scale(scale, scale, 1.0F);
            AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, 0, 0, node.icon);
            graphics.pose().popPose();
            return;
        }

        Font font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        float scale = Math.max(size / 8.0F, 1.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, "?", 1, 0, 0xFFFFFFFF, false);
        graphics.pose().popPose();
    }

    private record SyncState(boolean visible, String nodeName, boolean showCwu, long cwuCurrent, long cwuNeeded,
                             boolean hasEureka, boolean eurekaScanned, List<MaterialState> materials) {

        private static SyncState hidden() {
            return new SyncState(false, null, false, 0L, 0L, false, false, Collections.emptyList());
        }
    }

    private record MaterialState(String tagid, long current, long needed) {}

    @OnlyIn(Dist.CLIENT)
    private record RowState(Component label, long current, long total, float eurekaPercent, int fillColor, int borderColor,
                            @Nullable Component tooltip, @Nullable ResearchTag researchTag) {}

    private final class ContentWidget extends Widget implements IIngredientSlot {

        private int scrollOffset = 0;
        private int descTextOffset = 0;
        private int recipeScrollOffset = 0;
        private int prerequisiteScrollOffset = 0;
        private int descTextX;
        private int descTextY;
        private int descTextWidth;
        private int descTextHeight;
        private int maxDescTextOffset;
        private int rowAreaX;
        private int rowAreaY;
        private int rowAreaWidth;
        private int rowAreaBottom;
        private int recipeSlotsX;
        private int recipeSlotsY;
        private int visibleRecipeSlots;
        private int prerequisiteAreaX;
        private int prerequisiteAreaY;
        private int prerequisiteAreaWidth;
        private int prerequisiteSlotsX;
        private int visiblePrerequisiteSlots;
        private @Nullable TechNode cachedDescNode;
        private @Nullable TechNode cachedRecipeNode;
        private List<EmiStack> cachedRecipeStacks = Collections.emptyList();
        private final int baseHeight;

        private ContentWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
            baseHeight = height;
        }

        private void updateNodeLayout(@Nullable TechNode node) {
            int prerequisiteHeight = node != null && !node.prerequisites.isEmpty() ?
                    PREREQUISITE_SLOT_SIZE + HEADER_SECTION_GAP : 0;
            int contentHeight = baseHeight + prerequisiteHeight;
            if (getSizeHeight() != contentHeight) {
                setSizeHeight(contentHeight);
                TechTreeSideTab.this.computeMax();
            }
        }

        @Override
        public Widget setVisible(boolean isVisible) {
            scrollOffset = 0;
            descTextOffset = 0;
            recipeScrollOffset = 0;
            prerequisiteScrollOffset = 0;
            return super.setVisible(isVisible);
        }

        @OnlyIn(Dist.CLIENT)
        private List<EmiStack> getUnlockableRecipeStacks() {
            if (cachedRecipeNode != selectedNode) {
                cachedRecipeNode = selectedNode;
                cachedRecipeStacks = selectedNode == null ? Collections.emptyList() : EmiResearchHelper.toEmiStacks(selectedNode.getRecipePrimaryOutputs());
                recipeScrollOffset = 0;
            }
            return cachedRecipeStacks;
        }

        @OnlyIn(Dist.CLIENT)
        protected void drawTooltipTexts(int mouseX, int mouseY) {
            var tt = getTooltipText();
            if (!tt.isEmpty() && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this && gui != null && gui.getModularUIGui() != null) {
                gui.getModularUIGui().setHoverTooltip(tt, ItemStack.EMPTY, null, null);
            }
        }

        @SuppressWarnings("MathClampMigration")
        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            var node = selectedNode;
            if (node == null || !currentState.visible()) {
                return;
            }

            var pos = getPosition();
            var size = getSize();

            Font font = Minecraft.getInstance().font;
            int contentX = pos.x + CONTENT_PADDING;
            int contentY = pos.y + CONTENT_PADDING;
            int contentWidth = size.width - CONTENT_PADDING * 2;
            int contentHeight = size.height - CONTENT_PADDING * 2;
            int contentBottom = contentY + contentHeight;
            int reqLabelTextY = contentY + HEADER_HEIGHT;
            Component requirementsLabel = Component.translatable(REQUIREMENTS_LABEL);

            drawHeader(graphics, font, node, contentX, contentY, contentWidth, mouseX, mouseY);
            if (!node.prerequisites.isEmpty()) {
                Component prerequisitesLabel = Component.translatable(PREREQUISITES_LABEL);
                int labelY = reqLabelTextY + Math.max(0, (PREREQUISITE_SLOT_SIZE - font.lineHeight) / 2);
                graphics.drawString(font, prerequisitesLabel, contentX, labelY, HEADER_DESC_COLOR, false);
                int areaX = contentX + font.width(prerequisitesLabel) + PREREQUISITE_LABEL_GAP;
                drawPrerequisiteNodes(graphics, font, node.prerequisites, areaX, reqLabelTextY,
                        Math.max(PREREQUISITE_SLOT_SIZE, contentX + contentWidth - areaX), mouseX, mouseY);
                reqLabelTextY += PREREQUISITE_SLOT_SIZE + HEADER_SECTION_GAP;
            } else {
                prerequisiteAreaX = 0;
                prerequisiteAreaY = 0;
                prerequisiteAreaWidth = 0;
                prerequisiteSlotsX = 0;
                visiblePrerequisiteSlots = 0;
                prerequisiteScrollOffset = 0;
            }
            graphics.drawString(font, requirementsLabel, contentX, reqLabelTextY, HEADER_DESC_COLOR, false);
            int rowsStartY = reqLabelTextY + font.lineHeight + HEADER_SECTION_GAP;

            List<RowState> rows = buildRows();
            List<EmiStack> recipeStacks = getUnlockableRecipeStacks();
            int rowsHeight = rows.isEmpty() ? 0 : rows.size() * ROW_HEIGHT + (rows.size() - 1) * ROW_GAP;
            int maximumRowsHeight = MAX_VISIBLE_REQUIREMENT_ROWS * ROW_HEIGHT +
                    (MAX_VISIBLE_REQUIREMENT_ROWS - 1) * ROW_GAP;
            int maxRowsBottom = Math.min(rowsStartY + maximumRowsHeight,
                    Math.max(rowsStartY, contentBottom - INNER_CONTENT_MIN_HEIGHT));
            int rowsBottom = Math.min(rowsStartY + rowsHeight, maxRowsBottom);
            drawRows(graphics, font, rows, contentX, rowsStartY, contentWidth, rowsBottom, mouseX, mouseY);

            rowAreaX = contentX;
            rowAreaY = rowsStartY;
            rowAreaWidth = contentWidth;
            rowAreaBottom = rowsBottom;

            int innerContentY;
            var hasRecipes = !recipeStacks.isEmpty();
            var hasAdditionalContent = !node.getAdditionalLines().isEmpty();
            if (hasRecipes || hasAdditionalContent) {
                AtomicInteger recipeLabelY = new AtomicInteger(rowsBottom + INNER_CONTENT_SECTION_GAP);
                graphics.drawString(font, Component.translatable(TechNode.UNLOCKABLE_LABEL), contentX, recipeLabelY.getAndAdd(font.lineHeight + RECIPE_LABEL_GAP), HEADER_DESC_COLOR, false);
                if (hasRecipes) {
                    drawRecipeStacks(graphics, font, recipeStacks, contentX, recipeLabelY.getAndAdd(RECIPE_SLOT_SIZE + INNER_CONTENT_SECTION_GAP), contentWidth, partialTicks);
                } else {
                    recipeSlotsX = 0;
                    recipeSlotsY = 0;
                    visibleRecipeSlots = 0;
                }
                if (hasAdditionalContent) {
                    for (int i = 0; i < node.getAdditionalLines().size(); i++) {
                        graphics.drawString(font, node.getAdditionalLines().get(i), contentX, recipeLabelY.getAndAdd(font.lineHeight), HEADER_DESC_COLOR, false);
                    }
                }
                innerContentY = recipeLabelY.get() + INNER_CONTENT_SECTION_GAP;
            } else {
                recipeSlotsX = 0;
                recipeSlotsY = 0;
                visibleRecipeSlots = 0;
                innerContentY = Math.min(rowsStartY + rowsHeight + INNER_CONTENT_SECTION_GAP, Math.max(rowsStartY, contentBottom - INNER_CONTENT_MIN_HEIGHT));
            }
            innerContent.setSelfPosition(contentX - pos.x, innerContentY - pos.y - scrollYOffset);
        }

        @OnlyIn(Dist.CLIENT)
        public List<Component> getTooltipText() {
            var node = selectedNode;
            if (node == null || !currentState.visible() || !TechTreeSideTab.this.isMouseOver()) {
                return super.getTooltipTexts();
            }

            var pos = getPosition();
            var size = getSize();
            var minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            int i = (int) GuiHelper.getRealMouseX();
            int j = (int) GuiHelper.getRealMouseY();
            int contentX = pos.x + CONTENT_PADDING;
            int contentY = pos.y + CONTENT_PADDING;
            int contentWidth = size.width - CONTENT_PADDING * 2;
            int textX = contentX + HEADER_ICON_SIZE + HEADER_TEXT_GAP;
            int textWidth = Math.max(10, contentWidth - HEADER_ICON_SIZE - HEADER_TEXT_GAP);
            var headerTooltip = createTierTooltip(node);
            if (Widget.isMouseOver(textX, contentY + 1, textWidth, font.lineHeight, i, j) && headerTooltip != null) {
                return List.of(headerTooltip);
            }

            TechNode prerequisite = getHoveredPrerequisite(i, j);
            if (prerequisite != null) {
                return List.of(TechTreeManager.getNodeName(prerequisite),
                        TechTreeManager.getTreeName(prerequisite.getManager()).withStyle(ChatFormatting.GRAY),
                        Component.translatable(NAVIGATE_LABEL).withStyle(ChatFormatting.ITALIC, ChatFormatting.GREEN, ChatFormatting.UNDERLINE));
            }

            EmiStack recipeStack = getXEIIngredientOverMouse(i, j);
            if (recipeStack != null) {
                return recipeStack.getTooltipText();
            }
            RowState hoveredRow = getHoveredRow(buildRows(), i, j);
            return hoveredRow == null || hoveredRow.tooltip() == null ? super.getTooltipTexts() : List.of(hoveredRow.tooltip());
        }

        @OnlyIn(Dist.CLIENT)
        private void drawHeader(GuiGraphics graphics, Font font, TechNode node, int x, int y, int width, int mouseX, int mouseY) {
            DrawerHelper.drawSolidRect(graphics, x, y, HEADER_ICON_SIZE, HEADER_ICON_SIZE, NODE_BOX_FILL);
            DrawerHelper.drawBorder(graphics, x, y, HEADER_ICON_SIZE, HEADER_ICON_SIZE, NODE_BOX_BORDER, 1);
            drawNodeIcon(graphics, x + 8, y + 8, 16, node);

            int textX = x + HEADER_ICON_SIZE + HEADER_TEXT_GAP;
            int textWidth = Math.max(10, width - HEADER_ICON_SIZE - HEADER_TEXT_GAP);
            if (cachedDescNode != node) {
                cachedDescNode = node;
                descTextOffset = 0;
            }

            var text = node.getDisplayName().append(Component.translatable(TIER_LABEL, node.getTier()).withStyle(ChatFormatting.BLUE));
            List<FormattedCharSequence> texts = font.split(text, textWidth);
            if (Widget.isMouseOver(textX, y + 1, textWidth, font.lineHeight, mouseX, mouseY) && texts.size() > 1) {
                drawRollTextLine(graphics, textX, y + 1, textWidth, font.lineHeight, font, font.lineHeight, text);
            } else {
                graphics.drawString(font, texts.getFirst(), textX, y + 1, HEADER_NAME_COLOR, false);
            }
            var desc = node.desc();
            if (desc == null) {
                maxDescTextOffset = 0;
                descTextHeight = 0;
                return;
            }

            List<FormattedCharSequence> descLines = font.split(desc, textWidth);
            int maxLines = Math.max(0, (HEADER_HEIGHT - 14) / font.lineHeight);
            descTextX = textX;
            descTextY = y + 12;
            descTextWidth = textWidth;
            descTextHeight = maxLines * font.lineHeight;
            maxDescTextOffset = Math.max(0, descLines.size() - maxLines);
            descTextOffset = Mth.clamp(descTextOffset, 0, maxDescTextOffset);
            int endLine = Math.min(descTextOffset + maxLines, descLines.size());
            for (int i = descTextOffset; i < endLine; i++) {
                graphics.drawString(font, descLines.get(i), textX, descTextY + (i - descTextOffset) * font.lineHeight, HEADER_DESC_COLOR, false);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawRollTextLine(GuiGraphics graphics, float x, float y, int width, int height, Font fontRenderer, int textH, Component line) {
            float _y = y + (height - textH) / 2f;
            int textW = fontRenderer.width(line);
            int totalW = width + textW + 10;
            float from = x + width;
            var trans = graphics.pose().last().pose();
            var realPos = trans.transform(new Vector4f(x, y, 0, 1));
            var realPos2 = trans.transform(new Vector4f(x + width, y + height, 0, 1));
            graphics.enableScissor((int) realPos.x, (int) realPos.y, (int) realPos2.x, (int) realPos2.y);
            var t = 0.1 * Math.abs((int) (System.currentTimeMillis() % 1000000)) / 10 % totalW / totalW;
            graphics.drawString(fontRenderer, line, (int) (from - t * totalW), (int) _y, HEADER_NAME_COLOR, false);
            graphics.disableScissor();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public EmiStack getXEIIngredientOverMouse(double mouseX, double mouseY) {
            if (!TechTreeSideTab.this.isMouseOver()) return null;
            var pos = getPosition();
            if (Widget.isMouseOver(pos.x, pos.y,
                    HEADER_ICON_SIZE, HEADER_ICON_SIZE, mouseX, mouseY) && selectedNode != null) {
                return new TechNodeEmiStack(selectedNode);
            }
            RowState row = getHoveredRow(buildRows(), mouseX, mouseY);
            if (row != null && row.researchTag() != null) {
                return new ResearchTagEmiStack(row.researchTag()).setAmount(row.total());
            }
            if (!Widget.isMouseOver(recipeSlotsX, recipeSlotsY,
                    visibleRecipeSlots * RECIPE_SLOT_SIZE + Math.max(0, visibleRecipeSlots - 1) * RECIPE_SLOT_GAP,
                    RECIPE_SLOT_SIZE, mouseX, mouseY)) {
                return null;
            }
            int index = (int) (mouseX - recipeSlotsX) / (RECIPE_SLOT_SIZE + RECIPE_SLOT_GAP);
            if (index >= visibleRecipeSlots || (mouseX - recipeSlotsX) % (RECIPE_SLOT_SIZE + RECIPE_SLOT_GAP) >= RECIPE_SLOT_SIZE) {
                return null;
            }
            List<EmiStack> stacks = getUnlockableRecipeStacks();
            int stackIndex = recipeScrollOffset + index;
            return stackIndex < stacks.size() ? stacks.get(stackIndex) : null;
        }

        @OnlyIn(Dist.CLIENT)
        private @Nullable RowState getHoveredRow(List<RowState> rows, double mouseX, double mouseY) {
            int currentY = rowAreaY;
            for (RowState row : rows.subList(Math.min(scrollOffset, rows.size()), rows.size())) {
                if (currentY + ROW_HEIGHT > rowAreaBottom) {
                    break;
                }
                if (Widget.isMouseOver(rowAreaX, currentY, rowAreaWidth, ROW_HEIGHT, mouseX, mouseY)) {
                    return row;
                }
                currentY += ROW_HEIGHT + ROW_GAP;
            }
            return null;
        }

        @OnlyIn(Dist.CLIENT)
        private void drawRecipeStacks(GuiGraphics graphics, Font font, List<EmiStack> stacks, int x, int y, int width, float partialTicks) {
            int maxVisible = Math.max(1, (width + RECIPE_SLOT_GAP) / (RECIPE_SLOT_SIZE + RECIPE_SLOT_GAP));
            visibleRecipeSlots = Math.min(maxVisible, stacks.size());
            recipeScrollOffset = Mth.clamp(recipeScrollOffset, 0, Math.max(0, stacks.size() - visibleRecipeSlots));
            int stripWidth = visibleRecipeSlots * RECIPE_SLOT_SIZE + Math.max(0, visibleRecipeSlots - 1) * RECIPE_SLOT_GAP;
            recipeSlotsX = x + (width - stripWidth) / 2;
            recipeSlotsY = y;

            for (int i = 0; i < visibleRecipeSlots; i++) {
                int slotX = recipeSlotsX + i * (RECIPE_SLOT_SIZE + RECIPE_SLOT_GAP);
                GuiTextures.SLOT.draw(graphics, 0, 0, slotX, y, RECIPE_SLOT_SIZE, RECIPE_SLOT_SIZE);
                stacks.get(recipeScrollOffset + i).render(graphics, slotX + 1, y + 1, partialTicks, EmiIngredient.RENDER_ICON);
            }
            if (recipeScrollOffset > 0) {
                graphics.drawString(font, "<", x + 1, y + 5, ROW_TEXT_COLOR, false);
            }
            if (recipeScrollOffset + visibleRecipeSlots < stacks.size()) {
                graphics.drawString(font, ">", x + width - font.width(">") - 1, y + 5, ROW_TEXT_COLOR, false);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawPrerequisiteNodes(GuiGraphics graphics, Font font, List<TechNode> prerequisites,
                                           int x, int y, int width, int mouseX, int mouseY) {
            prerequisiteAreaX = x;
            prerequisiteAreaY = y;
            prerequisiteAreaWidth = width;
            boolean overflowing = prerequisites.size() * PREREQUISITE_SLOT_SIZE +
                    Math.max(0, prerequisites.size() - 1) * PREREQUISITE_SLOT_GAP > width;
            int arrowWidth = overflowing ? font.width(">") + 3 : 0;
            int slotsWidth = Math.max(PREREQUISITE_SLOT_SIZE, width - arrowWidth * 2);
            int maxVisible = Math.max(1, (slotsWidth + PREREQUISITE_SLOT_GAP) /
                    (PREREQUISITE_SLOT_SIZE + PREREQUISITE_SLOT_GAP));
            visiblePrerequisiteSlots = Math.min(maxVisible, prerequisites.size());
            prerequisiteScrollOffset = Mth.clamp(prerequisiteScrollOffset, 0,
                    Math.max(0, prerequisites.size() - visiblePrerequisiteSlots));
            int stripWidth = visiblePrerequisiteSlots * PREREQUISITE_SLOT_SIZE +
                    Math.max(0, visiblePrerequisiteSlots - 1) * PREREQUISITE_SLOT_GAP;
            prerequisiteSlotsX = x + arrowWidth + Math.max(0, (slotsWidth - stripWidth) / 2);

            for (int i = 0; i < visiblePrerequisiteSlots; i++) {
                int slotX = prerequisiteSlotsX + i * (PREREQUISITE_SLOT_SIZE + PREREQUISITE_SLOT_GAP);
                TechNode prerequisite = prerequisites.get(prerequisiteScrollOffset + i);
                GuiTextures.SLOT.draw(graphics, mouseX, mouseY, slotX, y,
                        PREREQUISITE_SLOT_SIZE, PREREQUISITE_SLOT_SIZE);
                drawNodeIcon(graphics, slotX + 1, y + 1, 16, prerequisite);
                if (Widget.isMouseOver(slotX, y, PREREQUISITE_SLOT_SIZE, PREREQUISITE_SLOT_SIZE, mouseX, mouseY)) {
                    DrawerHelper.drawBorder(graphics, slotX, y, PREREQUISITE_SLOT_SIZE,
                            PREREQUISITE_SLOT_SIZE, 0xFF39C5BB, 1);
                }
            }
            if (prerequisiteScrollOffset > 0) {
                graphics.drawString(font, "<", x + 1, y + 5, ROW_TEXT_COLOR, false);
            }
            if (prerequisiteScrollOffset + visiblePrerequisiteSlots < prerequisites.size()) {
                graphics.drawString(font, ">", x + width - font.width(">") - 1, y + 5, ROW_TEXT_COLOR, false);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private @Nullable TechNode getHoveredPrerequisite(double mouseX, double mouseY) {
            if (selectedNode == null || visiblePrerequisiteSlots <= 0 || !Widget.isMouseOver(
                    prerequisiteSlotsX, prerequisiteAreaY,
                    visiblePrerequisiteSlots * PREREQUISITE_SLOT_SIZE +
                            Math.max(0, visiblePrerequisiteSlots - 1) * PREREQUISITE_SLOT_GAP,
                    PREREQUISITE_SLOT_SIZE, mouseX, mouseY)) {
                return null;
            }
            int localX = (int) mouseX - prerequisiteSlotsX;
            int index = localX / (PREREQUISITE_SLOT_SIZE + PREREQUISITE_SLOT_GAP);
            if (index >= visiblePrerequisiteSlots ||
                    localX % (PREREQUISITE_SLOT_SIZE + PREREQUISITE_SLOT_GAP) >= PREREQUISITE_SLOT_SIZE) {
                return null;
            }
            int prerequisiteIndex = prerequisiteScrollOffset + index;
            return prerequisiteIndex < selectedNode.prerequisites.size() ?
                    selectedNode.prerequisites.get(prerequisiteIndex) : null;
        }

        @OnlyIn(Dist.CLIENT)
        private void drawRows(GuiGraphics graphics, Font font, List<RowState> rows, int x, int y, int width, int maxBottomY, int mouseX, int mouseY) {
            int currentY = y;
            int progressWidth = Math.max(20, width - VALUE_WIDTH - 6);
            var rowCount = Math.min(rows.size(), (maxBottomY - y + ROW_GAP) / (ROW_HEIGHT + ROW_GAP));
            scrollOffset = Mth.clamp(scrollOffset, 0, Math.max(0, rows.size() - rowCount));
            for (RowState row : rows.subList(scrollOffset, rows.size())) {
                if (currentY + ROW_HEIGHT > maxBottomY) {
                    break;
                }
                drawRow(graphics, font, row, x, currentY, progressWidth, VALUE_WIDTH, mouseX, mouseY);
                currentY += ROW_HEIGHT + ROW_GAP;
            }
            // △▼▷▾▷▽▵▿
            if (scrollOffset + rowCount < rows.size()) {
                graphics.drawString(font, "▽", x + progressWidth / 2 - font.width("△") / 2, currentY - 7, ROW_TEXT_COLOR, false);
            }
            if (scrollOffset > 0) {
                graphics.drawString(font, "△", x + progressWidth / 2 - font.width("▽") / 2, y - 7, ROW_TEXT_COLOR, false);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!TechTreeSideTab.this.isMouseOver()) return super.mouseClicked(mouseX, mouseY, button);
            if (button == 0 && visiblePrerequisiteSlots > 0 && Widget.isMouseOver(
                    prerequisiteAreaX, prerequisiteAreaY, prerequisiteAreaWidth,
                    PREREQUISITE_SLOT_SIZE, mouseX, mouseY)) {
                TechNode prerequisite = getHoveredPrerequisite(mouseX, mouseY);
                if (prerequisite != null) {
                    onNodeNavigate.accept(prerequisite);
                    playButtonClickSound();
                    return true;
                }
                if (mouseX < prerequisiteSlotsX && prerequisiteScrollOffset > 0) {
                    prerequisiteScrollOffset--;
                    playButtonClickSound();
                    return true;
                }
                if (selectedNode != null &&
                        mouseX >= prerequisiteSlotsX + visiblePrerequisiteSlots *
                                (PREREQUISITE_SLOT_SIZE + PREREQUISITE_SLOT_GAP) - PREREQUISITE_SLOT_GAP &&
                        prerequisiteScrollOffset + visiblePrerequisiteSlots < selectedNode.prerequisites.size()) {
                    prerequisiteScrollOffset++;
                    playButtonClickSound();
                    return true;
                }
            }
            if (visibleRecipeSlots > 0 && Widget.isMouseOver(rowAreaX, recipeSlotsY, rowAreaWidth, RECIPE_SLOT_SIZE, mouseX, mouseY)) {
                EmiStack stack = getXEIIngredientOverMouse(mouseX, mouseY);
                if (stack != null && (button == 0 || button == 1)) {
                    if (button == 0) {
                        EmiApi.displayRecipes(stack);
                    } else {
                        EmiApi.displayUses(stack);
                    }
                    playButtonClickSound();
                    return true;
                }
                if (button == 0 && mouseX < recipeSlotsX && recipeScrollOffset > 0) {
                    recipeScrollOffset--;
                    playButtonClickSound();
                    return true;
                }
                List<EmiStack> stacks = getUnlockableRecipeStacks();
                if (button == 0 && mouseX >= recipeSlotsX + visibleRecipeSlots * (RECIPE_SLOT_SIZE + RECIPE_SLOT_GAP) - RECIPE_SLOT_GAP &&
                        recipeScrollOffset + visibleRecipeSlots < stacks.size()) {
                    recipeScrollOffset++;
                    playButtonClickSound();
                    return true;
                }
            }

            RowState row = getHoveredRow(buildRows(), mouseX, mouseY);
            if (row != null && row.researchTag() != null && (button == 0 || button == 1)) {
                EmiApi.displayRecipes(new ResearchTagEmiStack(row.researchTag()));
                playButtonClickSound();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
            if (!TechTreeSideTab.this.isMouseOver()) return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
            if (maxDescTextOffset > 0 && wheelDelta != 0 && Widget.isMouseOver(
                    descTextX, descTextY, descTextWidth, descTextHeight, mouseX, mouseY)) {
                descTextOffset = Mth.clamp(descTextOffset + (wheelDelta > 0 ? -1 : 1), 0, maxDescTextOffset);
                return true;
            }
            if (selectedNode != null && visiblePrerequisiteSlots > 0 && wheelDelta != 0 && Widget.isMouseOver(
                    prerequisiteAreaX, prerequisiteAreaY, prerequisiteAreaWidth,
                    PREREQUISITE_SLOT_SIZE, mouseX, mouseY)) {
                int maxPrerequisiteScrollOffset = Math.max(0,
                        selectedNode.prerequisites.size() - visiblePrerequisiteSlots);
                if (wheelDelta > 0 && prerequisiteScrollOffset > 0) {
                    prerequisiteScrollOffset--;
                    return true;
                } else if (wheelDelta < 0 && prerequisiteScrollOffset < maxPrerequisiteScrollOffset) {
                    prerequisiteScrollOffset++;
                    return true;
                }
            }
            if (visibleRecipeSlots > 0 && Widget.isMouseOver(rowAreaX, recipeSlotsY, rowAreaWidth, RECIPE_SLOT_SIZE, mouseX, mouseY)) {
                List<EmiStack> stacks = getUnlockableRecipeStacks();
                int maxRecipeScrollOffset = Math.max(0, stacks.size() - visibleRecipeSlots);
                if (wheelDelta > 0 && recipeScrollOffset > 0) {
                    recipeScrollOffset--;
                    return true;
                } else if (wheelDelta < 0 && recipeScrollOffset < maxRecipeScrollOffset) {
                    recipeScrollOffset++;
                    return true;
                }
            }
            if (isMouseOverElement((int) mouseX, (int) mouseY) && getHoveredRow(buildRows(), mouseX, mouseY) != null) {
                List<RowState> rows = buildRows();
                int maxScrollOffset = Math.max(0, rows.size() - 1);
                if (wheelDelta > 0 && scrollOffset > 0) {
                    scrollOffset--;
                } else if (wheelDelta < 0 && scrollOffset < maxScrollOffset) {
                    scrollOffset++;
                } else {
                    return false;
                }
                return true;
            }
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }

        @OnlyIn(Dist.CLIENT)
        private void drawRow(GuiGraphics graphics, Font font, RowState row, int x, int y, int progressWidth, int valueWidth, int mouseX, int mouseY) {
            DrawerHelper.drawSolidRect(graphics, x, y, progressWidth, ROW_HEIGHT, ROW_BACKGROUND);
            DrawerHelper.drawBorder(graphics, x, y, progressWidth, ROW_HEIGHT, row.borderColor(), 1);

            float ratio = row.total() <= 0L ? 1.0F : Mth.clamp((float) row.current() / (float) row.total(), 0.0F, 1.0F);
            int fillWidth = Mth.clamp(Math.round((progressWidth - PROGRESS_INSET * 2) * ratio), 0, progressWidth - PROGRESS_INSET * 2);
            if (fillWidth > 0) {
                DrawerHelper.drawSolidRect(graphics, x + PROGRESS_INSET, y + PROGRESS_INSET, fillWidth, ROW_HEIGHT - PROGRESS_INSET * 2, row.fillColor());
            }
            var eurekaPercent = row.eurekaPercent();
            var highlightWidth = Mth.clamp(Math.round((progressWidth - PROGRESS_INSET * 2) * eurekaPercent), 0, progressWidth - PROGRESS_INSET * 2 - fillWidth);
            var hilightStartX = x + PROGRESS_INSET + fillWidth;
            if (highlightWidth > 0 && hilightStartX < x + progressWidth - PROGRESS_INSET) {
                DrawerHelper.drawSolidRect(graphics, hilightStartX, y + PROGRESS_INSET, highlightWidth, ROW_HEIGHT - PROGRESS_INSET * 2,
                        ColorUtils.getInterpolatedColor(0x00e2e2e2, row.fillColor(), (float) (0.5 + 0.25 * Math.sin(System.currentTimeMillis() / 1000.0))));
            }

            var text = row.label();
            var width = progressWidth - PROGRESS_TEXT_X * 2;
            List<FormattedCharSequence> texts = font.split(text, width);
            if (Widget.isMouseOver(x, y, width, font.lineHeight, mouseX, mouseY) && texts.size() > 1) {
                drawRollTextLine(graphics, x, y, width, font.lineHeight, font, font.lineHeight, text);
            } else {
                graphics.drawString(font, texts.getFirst(), x + PROGRESS_TEXT_X, y + 2, ROW_TEXT_COLOR, false);
            }

            String valueText = FormattingUtil.formatNumberReadable((long) (row.current() + row.total() * eurekaPercent)) + "/" + FormattingUtil.formatNumberReadable(row.total());
            int valueColor = row.total() > 0L && row.current() >= row.total() ? ROW_COMPLETE_VALUE_COLOR : ROW_VALUE_COLOR;
            graphics.drawString(font, valueText, x + progressWidth + 6 + Math.max(0, valueWidth - font.width(valueText)), y + 2, valueColor, false);
        }
    }
}
