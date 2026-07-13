package com.gtocore.api.research.ui;

import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchContext;
import com.gtocore.api.techtree.TechNode;
import com.gtocore.api.techtree.TechTreeManager;
import com.gtocore.data.recipe.research.AnalyzeData;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEKeyRendering;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@DataGeneratorScanned
public class ResearchTreeSideTab extends WidgetGroup {

    @RegisterLanguage(cn = "[配方奖励]", en = "[Recipe Reward]")
    public static final String RECIPE_REWARD_LABEL = "gtocore.research.side_tab.recipe_reward";
    @RegisterLanguage(cn = "[其他奖励]", en = "[Other Reward]")
    public static final String OTHER_REWARD_LABEL = "gtocore.research.side_tab.other_reward";
    @RegisterLanguage(cn = "可解锁：", en = "Unlockable:")
    public static final String UNLOCKABLE_LABEL = "gtocore.research.side_tab.unlockable";
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
    private static final int MATERIAL_BAR_COLOR = 0xFFD95CE5;
    private static final int MATERIAL_BAR_BORDER = 0xFFF3A5FB;

    private final TechTreeManager<TeamResearchContext> manager;
    private final Function<Player, TeamResearchContext> contextFactory;
    private SyncState currentState = SyncState.hidden();
    private SyncState lastSentState = SyncState.hidden();
    private @Nullable TechNode<TeamResearchContext> selectedNode;

    private final WidgetGroup innerContent;

    public ResearchTreeSideTab(int x, int y, int width, int height,
                               TechTreeManager<TeamResearchContext> manager,
                               Function<Player, TeamResearchContext> contextFactory) {
        this(x, y, width, height, manager, contextFactory, null);
    }

    public ResearchTreeSideTab(int x, int y, int width, int height,
                               TechTreeManager<TeamResearchContext> manager,
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

    public void toggleNode(TechNode<TeamResearchContext> node) {
        selectedNode = selectedNode == node ? null : node;
        syncState();
    }

    public @Nullable TechNode<TeamResearchContext> getSelectedNode() {
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

    private SyncState buildState() {
        if (selectedNode == null) {
            return SyncState.hidden();
        }
        Player player = getGuiPlayer();
        if (player == null) {
            return SyncState.hidden();
        }

        TeamResearchContext context = contextFactory.apply(player);
        ResearchRequirements requirements = getResearchRequirements(selectedNode);
        if (requirements == null) {
            return new SyncState(true, selectedNode.name, false, 0L, 0L, false, false, List.of());
        }

        boolean hasEureka = requirements.getEurekaItem() != null;
        boolean eurekaScanned = hasEureka && context.getScannedItems().contains(requirements.getEurekaItem());
        long cwuNeeded = eurekaScanned ? (long) (requirements.getCwuNeeded() * requirements.getEurekaProgress()) : requirements.getCwuNeeded();
        long cwuCurrent = context.getTechNodeAccCWU().getOrDefault(selectedNode, 0L);

        List<MaterialState> materials = new ArrayList<>();
        List<Map.Entry<ResearchTag, Long>> entries = new ArrayList<>();
        for (var entry : requirements.getMaterialNeeded().reference2LongEntrySet()) {
            entries.add(Map.entry(entry.getKey(), entry.getLongValue()));
        }
        entries.sort(Comparator.comparing(entry -> entry.getKey().name()));
        for (var entry : entries) {
            ResearchTag tag = entry.getKey();
            materials.add(new MaterialState(tag.name(), context.getResearchPoints().getOrDefault(tag, 0L), entry.getValue()));
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
            buffer.writeUtf(material.tagName());
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

    private @Nullable ResearchRequirements getResearchRequirements(TechNode<TeamResearchContext> node) {
        return node.getRequirements() instanceof ResearchRequirements requirements ? requirements : null;
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
            Component label = Component.translatable(currentState.hasEureka() && currentState.eurekaScanned() ? CWU_EUREKA_LABEL : CWU_LABEL);
            rows.add(new RowState(label, currentState.cwuCurrent(), currentState.cwuNeeded(), CWU_BAR_COLOR, CWU_BAR_BORDER, createCwuTooltip()));
        }
        for (MaterialState material : currentState.materials()) {
            // todo tag name
            rows.add(new RowState(Component.literal(FormattingUtil.toEnglishName(material.tagName())), material.current(), material.needed(), MATERIAL_BAR_COLOR, MATERIAL_BAR_BORDER, null));
        }
        return rows;
    }

    @OnlyIn(Dist.CLIENT)
    private @Nullable Component createCwuTooltip() {
        if (!currentState.hasEureka() || selectedNode == null) {
            return null;
        }
        ResearchRequirements requirements = getResearchRequirements(selectedNode);
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
    private @Nullable Component createTierTooltip(TechNode<TeamResearchContext> node) {
        var tierItem = AnalyzeData.INSTANCE.getTierItems().get(node.getTier());
        if (tierItem == null) {
            return null;
        }
        return Component.translatable(TIER_DESC, tierItem.asStack().getHoverName());
    }

    private static boolean isMouseOverRect(int mouseX, int mouseY, int x, int y, int width, int height) {
        return width > 0 && height > 0 && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @OnlyIn(Dist.CLIENT)
    private void drawNodeIcon(GuiGraphics graphics, int x, int y, int size, @Nullable TechNode<TeamResearchContext> node) {
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

    @OnlyIn(Dist.CLIENT)
    private FormattedCharSequence getFirstLine(Font font, Component text, int width) {
        List<FormattedCharSequence> lines = font.split(text, width);
        return lines.isEmpty() ? FormattedCharSequence.EMPTY : lines.getFirst();
    }

    private record SyncState(boolean visible, String nodeName, boolean showCwu, long cwuCurrent, long cwuNeeded,
                             boolean hasEureka, boolean eurekaScanned, List<MaterialState> materials) {

        private static SyncState hidden() {
            return new SyncState(false, null, false, 0L, 0L, false, false, List.of());
        }
    }

    private record MaterialState(String tagName, long current, long needed) {}

    @OnlyIn(Dist.CLIENT)
    private record RowState(Component label, long current, long total, int fillColor, int borderColor,
                            @Nullable Component tooltip) {}

    private final class ContentWidget extends Widget {

        private ContentWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
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
            int rewardTextY = contentY + HEADER_HEIGHT + HEADER_SECTION_GAP;
            List<FormattedCharSequence> rewardTextLines = getRewardTextLines(font, node, contentWidth);
            int rewardTextHeight = rewardTextLines.size() * font.lineHeight;
            int rowsStartY = rewardTextY + rewardTextHeight + (rewardTextLines.isEmpty() ? 0 : HEADER_SECTION_GAP);

            drawHeader(graphics, font, node, contentX, contentY, contentWidth);
            drawRewardLines(graphics, font, rewardTextLines, contentX, rewardTextY);
            List<RowState> rows = buildRows();
            int rowsHeight = rows.isEmpty() ? 0 : rows.size() * ROW_HEIGHT + (rows.size() - 1) * ROW_GAP;
            int preferredInnerContentY = rowsStartY + rowsHeight + INNER_CONTENT_SECTION_GAP;
            int maxInnerContentY = Math.max(rowsStartY, contentBottom - INNER_CONTENT_MIN_HEIGHT);
            int innerContentY = Math.min(preferredInnerContentY, maxInnerContentY);
            drawRows(graphics, font, rows, contentX, rowsStartY, contentWidth, innerContentY - INNER_CONTENT_SECTION_GAP);
            updateInnerContentBounds(contentX - pos.x, innerContentY - pos.y, contentWidth, contentBottom - innerContentY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
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
            int rewardTextY = contentY + HEADER_HEIGHT + HEADER_SECTION_GAP;
            List<FormattedCharSequence> rewardTextLines = getRewardTextLines(font, node, contentWidth);
            int rewardTextHeight = rewardTextLines.size() * font.lineHeight;
            int rowsStartY = rewardTextY + rewardTextHeight + (rewardTextLines.isEmpty() ? 0 : HEADER_SECTION_GAP);

            List<RowState> rows = buildRows();
            int rowsHeight = rows.isEmpty() ? 0 : rows.size() * ROW_HEIGHT + (rows.size() - 1) * ROW_GAP;
            int preferredInnerContentY = rowsStartY + rowsHeight + INNER_CONTENT_SECTION_GAP;
            int maxInnerContentY = Math.max(rowsStartY, contentBottom - INNER_CONTENT_MIN_HEIGHT);
            int innerContentY = Math.min(preferredInnerContentY, maxInnerContentY);

            Component hoveredTooltip = getHoveredTooltip(font, node, rows, mouseX, mouseY, contentX, contentY, contentWidth, rowsStartY, innerContentY - INNER_CONTENT_SECTION_GAP);
            if (hoveredTooltip != null) {
                graphics.renderComponentTooltip(font, List.of(hoveredTooltip), mouseX, mouseY);
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawHeader(GuiGraphics graphics, Font font, TechNode<TeamResearchContext> node, int x, int y, int width) {
            DrawerHelper.drawSolidRect(graphics, x, y, HEADER_ICON_SIZE, HEADER_ICON_SIZE, NODE_BOX_FILL);
            DrawerHelper.drawBorder(graphics, x, y, HEADER_ICON_SIZE, HEADER_ICON_SIZE, NODE_BOX_BORDER, 1);
            drawNodeIcon(graphics, x + 8, y + 8, 16, node);

            int textX = x + HEADER_ICON_SIZE + HEADER_TEXT_GAP;
            int textWidth = Math.max(10, width - HEADER_ICON_SIZE - HEADER_TEXT_GAP);

            graphics.drawString(font, getFirstLine(font, node.getDisplayName().append(Component.translatable(TIER_LABEL, node.getTier()).withStyle(ChatFormatting.BLUE)), textWidth), textX, y + 1, HEADER_NAME_COLOR, false);
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
        private @Nullable Component getHoveredTooltip(Font font, TechNode<TeamResearchContext> node, List<RowState> rows,
                                                      int mouseX, int mouseY, int contentX, int contentY, int contentWidth,
                                                      int rowsStartY, int maxRowsBottomY) {
            Component headerTooltip = getHeaderTooltip(font, node, mouseX, mouseY, contentX, contentY, contentWidth);
            if (headerTooltip != null) {
                return headerTooltip;
            }
            return getRowTooltip(font, rows, mouseX, mouseY, contentX, rowsStartY, contentWidth, maxRowsBottomY);
        }

        @OnlyIn(Dist.CLIENT)
        private @Nullable Component getHeaderTooltip(Font font, TechNode<TeamResearchContext> node, int mouseX, int mouseY, int x, int y, int width) {
            int textX = x + HEADER_ICON_SIZE + HEADER_TEXT_GAP;
            int textWidth = Math.max(10, width - HEADER_ICON_SIZE - HEADER_TEXT_GAP);
            List<FormattedCharSequence> nameLines = font.split(node.getDisplayName(), textWidth);
            if (nameLines.size() != 1) {
                return null;
            }
            int visibleNameWidth = font.width(nameLines.getFirst());
            int remainingWidth = Math.max(0, textWidth - visibleNameWidth);
            if (remainingWidth <= 0) {
                return null;
            }

            Component tierLabel = Component.translatable(TIER_LABEL, node.getTier()).withStyle(ChatFormatting.BLUE);
            int tierWidth = font.width(getFirstLine(font, tierLabel, remainingWidth));
            if (!isMouseOverRect(mouseX, mouseY, textX + visibleNameWidth, y + 1, tierWidth, font.lineHeight)) {
                return null;
            }
            return createTierTooltip(node);
        }

        @OnlyIn(Dist.CLIENT)
        private @Nullable Component getRowTooltip(Font font, List<RowState> rows, int mouseX, int mouseY, int x, int y, int width, int maxBottomY) {
            int currentY = y;
            int progressWidth = Math.max(20, width - VALUE_WIDTH - 6);
            for (RowState row : rows) {
                if (currentY + ROW_HEIGHT > maxBottomY) {
                    return null;
                }
                if (row.tooltip() != null) {
                    int labelWidth = font.width(getFirstLine(font, row.label(), progressWidth - PROGRESS_TEXT_X * 2));
                    if (isMouseOverRect(mouseX, mouseY, x + PROGRESS_TEXT_X, currentY, labelWidth, ROW_HEIGHT)) {
                        return row.tooltip();
                    }
                }
                currentY += ROW_HEIGHT + ROW_GAP;
            }
            return null;
        }

        @OnlyIn(Dist.CLIENT)
        private void drawRows(GuiGraphics graphics, Font font, List<RowState> rows, int x, int y, int width, int maxBottomY) {
            int currentY = y;
            int progressWidth = Math.max(20, width - VALUE_WIDTH - 6);
            for (RowState row : rows) {
                if (currentY + ROW_HEIGHT > maxBottomY) {
                    return;
                }
                drawRow(graphics, font, row, x, currentY, progressWidth, VALUE_WIDTH);
                currentY += ROW_HEIGHT + ROW_GAP;
            }
        }

        @OnlyIn(Dist.CLIENT)
        private void drawRow(GuiGraphics graphics, Font font, RowState row, int x, int y, int progressWidth, int valueWidth) {
            DrawerHelper.drawSolidRect(graphics, x, y, progressWidth, ROW_HEIGHT, ROW_BACKGROUND);
            DrawerHelper.drawBorder(graphics, x, y, progressWidth, ROW_HEIGHT, row.borderColor(), 1);

            float ratio = row.total() <= 0L ? 1.0F : Mth.clamp((float) row.current() / (float) row.total(), 0.0F, 1.0F);
            int fillWidth = Mth.clamp(Math.round((progressWidth - PROGRESS_INSET * 2) * ratio), 0, progressWidth - PROGRESS_INSET * 2);
            if (fillWidth > 0) {
                DrawerHelper.drawSolidRect(graphics, x + PROGRESS_INSET, y + PROGRESS_INSET, fillWidth, ROW_HEIGHT - PROGRESS_INSET * 2, row.fillColor());
            }

            graphics.drawString(font, getFirstLine(font, row.label(), progressWidth - PROGRESS_TEXT_X * 2), x + PROGRESS_TEXT_X, y + 2, ROW_TEXT_COLOR, false);

            String valueText = FormattingUtil.formatNumberReadable(row.current()) + "/" + FormattingUtil.formatNumberReadable(row.total());
            int valueColor = row.total() > 0L && row.current() >= row.total() ? ROW_COMPLETE_VALUE_COLOR : ROW_VALUE_COLOR;
            graphics.drawString(font, valueText, x + progressWidth + 6 + Math.max(0, valueWidth - font.width(valueText)), y + 2, valueColor, false);
        }

        @OnlyIn(Dist.CLIENT)
        private List<FormattedCharSequence> getRewardTextLines(Font font, TechNode<TeamResearchContext> node, int width) {
            List<FormattedCharSequence> lines = new ArrayList<>();
            for (Component rewardLine : AnalyzeData.INSTANCE.getRewardLines(node)) {
                lines.addAll(font.split(rewardLine, width));
            }
            return lines;
        }

        @OnlyIn(Dist.CLIENT)
        private void drawRewardLines(GuiGraphics graphics, Font font, List<FormattedCharSequence> rewardTextLines, int x, int y) {
            int currentY = y;
            for (FormattedCharSequence rewardTextLine : rewardTextLines) {
                graphics.drawString(font, rewardTextLine, x, currentY, HEADER_DESC_COLOR, false);
                currentY += font.lineHeight;
            }
        }
    }
}
