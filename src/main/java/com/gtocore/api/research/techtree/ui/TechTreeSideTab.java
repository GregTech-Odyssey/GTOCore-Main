package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchContext;
import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.integration.emi.research.EmiResearchHelper;
import com.gtocore.integration.emi.research.ResearchTagEmiStack;
import com.gtocore.integration.emi.research.TechNodeEmiStack;

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
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.*;
import java.util.function.Function;

@DataGeneratorScanned
public class TechTreeSideTab extends WidgetGroup {

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

    private static final int UPDATE_SYNC_STATE = 100;

    private static final int OUTER_PADDING = 4;
    private static final int CONTENT_PADDING = 5;
    private static final int HEADER_HEIGHT = 50;
    private static final int HEADER_ICON_SIZE = 32;
    private static final int HEADER_TEXT_GAP = 6;
    private static final int HEADER_SECTION_GAP = 8;
    private static final int ROW_HEIGHT = 12;
    private static final int ROW_GAP = 4;
    private static final int INNER_CONTENT_SECTION_GAP = 8;
    private static final int INNER_CONTENT_MIN_HEIGHT = 28;
    private static final int VALUE_WIDTH = 42;
    private static final int PROGRESS_INSET = 1;
    private static final int PROGRESS_TEXT_X = 4;
    private static final int RECIPE_SLOT_SIZE = 18;
    private static final int RECIPE_SLOT_GAP = 2;
    private static final int RECIPE_LABEL_GAP = 2;

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

    private final TechTreeManager manager;
    private final Function<Player, TeamResearchContext> contextFactory;
    private SyncState currentState = SyncState.hidden();
    private SyncState lastSentState = SyncState.hidden();
    private @Nullable TechNode selectedNode;

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
        this.innerContent = new WidgetGroup(0, 0, 0, 0);
        setInnerContent(contentWidget);
        setBackground(GuiTextures.BACKGROUND_INVERSE);
        addWidget(new ContentWidget(OUTER_PADDING, OUTER_PADDING, width - OUTER_PADDING * 2, height - OUTER_PADDING * 2));
        addWidget(innerContent);
        setVisible(false).setActive(false);
    }

    public void toggleNode(TechNode node) {
        showNode(selectedNode == node ? null : node);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        var mc = Minecraft.getInstance();
        var mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        var mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        var ing = getXEIIngredientOverMouse(mouseX, mouseY);
        if (ing != null) {
            return EmiScreenManager.stackInteraction(new EmiStackInteraction((EmiIngredient) ing, null, true),
                    bind -> bind.matchesKey(keyCode, scanCode));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var ing = getXEIIngredientOverMouse(mouseX, mouseY);
        if (ing instanceof ResearchTagEmiStack || ing instanceof TechNodeEmiStack) {
            return EmiScreenManager.stackInteraction(new EmiStackInteraction((EmiIngredient) ing, null, true),
                    bind -> bind.matchesMouse(button));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void showNode(@Nullable TechNode node) {
        selectedNode = node;
        if (isClientSideWidget) {
            if (getGuiPlayer() != null) {
                applyState(buildState());
            } else {
                setVisible(node != null).setActive(node != null);
            }
            return;
        }
        syncState();
    }

    public @Nullable TechNode getSelectedNode() {
        return selectedNode;
    }

    public void setInnerContent(@Nullable Widget contentWidget) {
        innerContent.clearAllWidgets();
        if (contentWidget == null) {
            return;
        }
        contentWidget.setSelfPosition(0, 0);
        innerContent.addWidget(contentWidget);
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
            return new SyncState(true, selectedNode.name, false, 0L, 0L, false, false, List.of());
        }

        boolean hasEureka = requirements.getEurekaItem() != null;
        boolean eurekaScanned = hasEureka && context.getScannedItems().contains(requirements.getEurekaItem());
        long cwuNeeded = requirements.getCwuNeeded();
        long cwuCurrent = context.getTechNodeAccCWU().getOrDefault(selectedNode, 0L);

        List<MaterialState> materials = new ArrayList<>();
        List<Map.Entry<ResearchTag, Long>> entries = new ArrayList<>();
        for (var entry : requirements.getMaterialNeeded().reference2LongEntrySet()) {
            entries.add(Map.entry(entry.getKey(), entry.getLongValue()));
        }
        entries.sort(Comparator.comparing(entry -> entry.getKey().getName()));
        for (var entry : entries) {
            ResearchTag tag = entry.getKey();
            materials.add(new MaterialState(tag.getName(), context.getResearchPoints().getOrDefault(tag, 0L), entry.getValue()));
        }

        return new SyncState(true, selectedNode.name, true, cwuCurrent, cwuNeeded, hasEureka, eurekaScanned, List.copyOf(materials));
    }

    private void applyState(SyncState state) {
        currentState = state;
        selectedNode = state.nodeName() == null ? null : manager.getNode(state.nodeName());
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

    private void updateInnerContentBounds(int x, int y, int width, int height) {
        innerContent.setSelfPosition(x, y);
        innerContent.setSize(width, Math.max(0, height));
    }

    @OnlyIn(Dist.CLIENT)
    private List<RowState> buildRows() {
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
        return rows;
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
            return new SyncState(false, null, false, 0L, 0L, false, false, List.of());
        }
    }

    private record MaterialState(String tagid, long current, long needed) {}

    @OnlyIn(Dist.CLIENT)
    private record RowState(Component label, long current, long total, float eurekaPercent, int fillColor, int borderColor,
                            @Nullable Component tooltip, @Nullable ResearchTag researchTag) {}

    private final class ContentWidget extends Widget implements IIngredientSlot {

        private int scrollOffset = 0;
        private int recipeScrollOffset = 0;
        private int rowAreaX;
        private int rowAreaY;
        private int rowAreaWidth;
        private int rowAreaBottom;
        private int recipeSlotsX;
        private int recipeSlotsY;
        private int visibleRecipeSlots;
        private @Nullable TechNode cachedRecipeNode;
        private List<EmiStack> cachedRecipeStacks = List.of();

        private ContentWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public Widget setVisible(boolean isVisible) {
            scrollOffset = 0;
            recipeScrollOffset = 0;
            return super.setVisible(isVisible);
        }

        @OnlyIn(Dist.CLIENT)
        private List<EmiStack> getUnlockableRecipeStacks() {
            if (cachedRecipeNode != selectedNode) {
                cachedRecipeNode = selectedNode;
                cachedRecipeStacks = selectedNode == null ? List.of() : EmiResearchHelper.toEmiStacks(selectedNode.getRecipePrimaryOutputs());
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

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
            var node = selectedNode;
            if (node == null || !currentState.visible()) {
                return;
            }

            var pos = getPosition();
            var size = getSize();
            GuiTextures.DISPLAY.draw(graphics, mouseX, mouseY, pos.x, pos.y, size.width, size.height);

            Font font = Minecraft.getInstance().font;
            int contentX = pos.x + CONTENT_PADDING;
            int contentY = pos.y + CONTENT_PADDING;
            int contentWidth = size.width - CONTENT_PADDING * 2;
            int contentHeight = size.height - CONTENT_PADDING * 2;
            int contentBottom = contentY + contentHeight;
            int reqLabelTextY = contentY + HEADER_HEIGHT;
            Component requirementsLabel = Component.translatable(REQUIREMENTS_LABEL);
            int rowsStartY = reqLabelTextY + font.lineHeight + HEADER_SECTION_GAP;

            drawHeader(graphics, font, node, contentX, contentY, contentWidth, mouseX, mouseY);
            graphics.drawString(font, requirementsLabel, contentX, reqLabelTextY, HEADER_DESC_COLOR, false);

            List<RowState> rows = buildRows();
            List<EmiStack> recipeStacks = getUnlockableRecipeStacks();
            int rowsHeight = rows.isEmpty() ? 0 : rows.size() * ROW_HEIGHT + (rows.size() - 1) * ROW_GAP;
            int maxRowsBottom = Math.max(rowsStartY, contentBottom - INNER_CONTENT_MIN_HEIGHT);
            int rowsBottom = Math.min(rowsStartY + rowsHeight, maxRowsBottom);
            drawRows(graphics, font, rows, contentX, rowsStartY, contentWidth, rowsBottom, mouseX, mouseY);

            rowAreaX = contentX;
            rowAreaY = rowsStartY;
            rowAreaWidth = contentWidth;
            rowAreaBottom = rowsBottom;

            int innerContentY;
            if (recipeStacks.isEmpty()) {
                recipeSlotsX = 0;
                recipeSlotsY = 0;
                visibleRecipeSlots = 0;
                innerContentY = Math.min(rowsStartY + rowsHeight + INNER_CONTENT_SECTION_GAP, Math.max(rowsStartY, contentBottom - INNER_CONTENT_MIN_HEIGHT));
            } else {
                int recipeLabelY = rowsBottom + INNER_CONTENT_SECTION_GAP;
                graphics.drawString(font, Component.translatable(TechNode.UNLOCKABLE_LABEL), contentX, recipeLabelY, HEADER_DESC_COLOR, false);
                drawRecipeStacks(graphics, font, recipeStacks, contentX, recipeLabelY + font.lineHeight + RECIPE_LABEL_GAP, contentWidth, partialTicks);
                innerContentY = recipeSlotsY + RECIPE_SLOT_SIZE + INNER_CONTENT_SECTION_GAP;
            }
            updateInnerContentBounds(contentX - pos.x, innerContentY - pos.y, contentWidth, contentBottom - innerContentY);
        }

        @OnlyIn(Dist.CLIENT)
        public List<Component> getTooltipText() {
            var node = selectedNode;
            if (node == null || !currentState.visible()) {
                return super.getTooltipTexts();
            }

            var pos = getPosition();
            var size = getSize();
            var minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            int i = (int) (minecraft.mouseHandler.xpos() * (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth());
            int j = (int) (minecraft.mouseHandler.ypos() * (double) minecraft.getWindow().getGuiScaledHeight() / (double) minecraft.getWindow().getScreenHeight());
            int contentX = pos.x + CONTENT_PADDING;
            int contentY = pos.y + CONTENT_PADDING;
            int contentWidth = size.width - CONTENT_PADDING * 2;
            int textX = contentX + HEADER_ICON_SIZE + HEADER_TEXT_GAP;
            int textWidth = Math.max(10, contentWidth - HEADER_ICON_SIZE - HEADER_TEXT_GAP);
            var headerTooltip = createTierTooltip(node);
            if (Widget.isMouseOver(textX, contentY + 1, textWidth, font.lineHeight, i, j) && headerTooltip != null) {
                return List.of(headerTooltip);
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

            var text = node.getDisplayName().append(Component.translatable(TIER_LABEL, node.getTier()).withStyle(ChatFormatting.BLUE));
            List<FormattedCharSequence> texts = font.split(text, textWidth);
            if (Widget.isMouseOver(textX, y + 1, textWidth, font.lineHeight, mouseX, mouseY) && texts.size() > 1) {
                drawRollTextLine(graphics, textX, y + 1, textWidth, font.lineHeight, font, font.lineHeight, text);
            } else {
                graphics.drawString(font, texts.getFirst(), textX, y + 1, HEADER_NAME_COLOR, false);
            }
            var desc = node.desc();
            if (desc == null) {
                return;
            }

            List<FormattedCharSequence> descLines = font.split(desc, textWidth);
            int maxLines = Math.max(0, (HEADER_HEIGHT - 14) / font.lineHeight);
            for (int i = 0; i < Math.min(maxLines, descLines.size()); i++) {
                graphics.drawString(font, descLines.get(i), textX, y + 12 + i * font.lineHeight, HEADER_DESC_COLOR, false);
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
            if (isMouseOverElement((int) mouseX, (int) mouseY)) {
                List<RowState> rows = buildRows();
                int maxScrollOffset = Math.max(0, rows.size() - 1);
                if (wheelDelta > 0 && scrollOffset > 0) {
                    scrollOffset--;
                } else if (wheelDelta < 0 && scrollOffset < maxScrollOffset) {
                    scrollOffset++;
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
            var highlightWidth = Mth.clamp(Math.round((progressWidth - PROGRESS_INSET * 2) * eurekaPercent), 0, progressWidth - PROGRESS_INSET * 2);
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
