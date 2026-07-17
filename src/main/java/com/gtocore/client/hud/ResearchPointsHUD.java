package com.gtocore.client.hud;

import com.gtocore.api.research.ResearchTag;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.config.GTOConfig;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public class ResearchPointsHUD implements IMoveableHUD {

    public static final ResearchPointsHUD INSTANCE = new ResearchPointsHUD();

    @RegisterLanguage(en = "Research Points", cn = "研究点数")
    public static final String DISPLAY_NAME = "gtocore.hud.research_points.name";

    @RegisterLanguage(en = "No research points.", cn = "暂无研究点数。")
    public static final String EMPTY_MESSAGE = "gtocore.hud.research_points.empty";

    private static final int MIN_WIDTH = 112;
    private static final int PADDING = 5;
    private static final int HEADER_GAP = 5;
    private static final int ROW_SPACING = 2;
    private static final int SWATCH_SIZE = 5;
    private static final int SWATCH_GAP = 4;
    private static final int COLUMN_GAP = 12;

    private boolean dragging;
    private int dragStartX;
    private int dragStartY;
    private int pendingMovedX;
    private int pendingMovedY;

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(pendingMovedX, pendingMovedY, 1500);
        renderGeneral(guiGraphics, partialTick,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight());
        guiGraphics.pose().popPose();
    }

    @Override
    public void renderGeneral(GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        List<ResearchPointRow> rows = collectRows(mc);
        Rect2i bounds = getBounds(screenWidth, screenHeight, font, rows);

        guiGraphics.fill(bounds.getX(), bounds.getY(),
                bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight(), 0xA0101010);
        IMoveableHUD.drawOutline(guiGraphics, bounds, 0xFF737A83);

        int contentX = bounds.getX() + PADDING;
        int cursorY = bounds.getY() + PADDING;
        guiGraphics.drawString(font, getDisplayName(), contentX, cursorY, 0xFFFFFFFF, false);

        cursorY += font.lineHeight + 2;
        guiGraphics.hLine(contentX, bounds.getX() + bounds.getWidth() - PADDING - 1, cursorY, 0xFF555B63);
        cursorY += HEADER_GAP - 1;

        if (rows.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable(EMPTY_MESSAGE), contentX, cursorY, 0xFFADB5BF, false);
            return;
        }

        int valueRight = bounds.getX() + bounds.getWidth() - PADDING;
        for (ResearchPointRow row : rows) {
            int swatchY = cursorY + (font.lineHeight - SWATCH_SIZE) / 2;
            guiGraphics.fill(contentX, swatchY, contentX + SWATCH_SIZE, swatchY + SWATCH_SIZE, row.tag().getColor());
            guiGraphics.drawString(font, row.tag().getDisplayName(), contentX + SWATCH_SIZE + SWATCH_GAP, cursorY, 0xFFE7EBF0, false);
            guiGraphics.drawString(font, row.formattedAmount(), valueRight - font.width(row.formattedAmount()), cursorY, 0xFFD5DCE5, false);
            cursorY += font.lineHeight + ROW_SPACING;
        }
    }

    @Override
    public Rect2i getBounds(int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        return getBounds(screenWidth, screenHeight, mc.font, collectRows(mc));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(DISPLAY_NAME);
    }

    @Override
    public boolean isEnabled() {
        return GTOConfig.INSTANCE.client.hud.researchPointsHUDEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        GTOConfig.set("researchPointsHUDEnabled", enabled, "client", "hud");
        if (!enabled) {
            resetDragging();
        }
    }

    @Override
    public boolean isPositionDragging() {
        return dragging || pendingMovedX != 0 || pendingMovedY != 0;
    }

    @Override
    public Rect2i getPropertyAnchorBounds(int screenWidth, int screenHeight) {
        Rect2i bounds = getBounds(screenWidth, screenHeight);
        return new Rect2i(bounds.getX() + pendingMovedX, bounds.getY() + pendingMovedY,
                bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        dragging = true;
        dragStartX = (int) mouseX;
        dragStartY = (int) mouseY;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging) {
            return false;
        }
        pendingMovedX = (int) mouseX - dragStartX;
        pendingMovedY = (int) mouseY - dragStartY;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = isPositionDragging();
        if (pendingMovedX != 0 || pendingMovedY != 0) {
            Minecraft mc = Minecraft.getInstance();
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            Rect2i bounds = getBounds(screenWidth, screenHeight);
            setTopLeftPosition(bounds.getX() + pendingMovedX, bounds.getY() + pendingMovedY, screenWidth, screenHeight);
        }
        resetDragging();
        return handled;
    }

    @Override
    public void setTopLeftPosition(int x, int y, int screenWidth, int screenHeight) {
        Rect2i bounds = getBounds(screenWidth, screenHeight);
        int maxX = Math.max(0, screenWidth - bounds.getWidth());
        int maxY = Math.max(0, screenHeight - bounds.getHeight());
        int clampedX = Mth.clamp(x, 0, maxX);
        int clampedY = Mth.clamp(y, 0, maxY);
        int relativeX = (int) Mth.clamp(Math.round(clampedX * 100.0 / Math.max(1, maxX)), 0, 100);
        int relativeY = (int) Mth.clamp(Math.round(clampedY * 100.0 / Math.max(1, maxY)), 0, 100);

        GTOConfig.set("researchPointsHUDDefaultX", relativeX, "client", "hud");
        GTOConfig.set("researchPointsHUDDefaultY", relativeY, "client", "hud");
        resetDragging();
    }

    private Rect2i getBounds(int screenWidth, int screenHeight, Font font, List<ResearchPointRow> rows) {
        Component emptyMessage = Component.translatable(EMPTY_MESSAGE);
        int width = Math.max(MIN_WIDTH, Math.max(font.width(getDisplayName()), font.width(emptyMessage)) + PADDING * 2);
        for (ResearchPointRow row : rows) {
            int rowWidth = SWATCH_SIZE + SWATCH_GAP + font.width(row.tag().getDisplayName()) + COLUMN_GAP + font.width(row.formattedAmount());
            width = Math.max(width, rowWidth + PADDING * 2);
        }

        int rowCount = Math.max(1, rows.size());
        int height = PADDING * 2 + font.lineHeight + HEADER_GAP + rowCount * (font.lineHeight + ROW_SPACING) - ROW_SPACING;
        int x = (int) (GTOConfig.INSTANCE.client.hud.researchPointsHUDDefaultX / 100.0 * Math.max(0, screenWidth - width));
        int y = (int) (GTOConfig.INSTANCE.client.hud.researchPointsHUDDefaultY / 100.0 * Math.max(0, screenHeight - height));
        return new Rect2i(x, y, width, height);
    }

    private List<ResearchPointRow> collectRows(Minecraft mc) {
        List<ResearchPointRow> rows = new ArrayList<>();
        if (mc.player == null) {
            return rows;
        }

        var researchPoints = TeamResearchSavedDtat.getOrCreateContext(mc.player).getResearchPoints();
        for (var entry : researchPoints.reference2LongEntrySet()) {
            if (entry.getLongValue() == 0) continue;
            rows.add(new ResearchPointRow(entry.getKey(), FormattingUtil.formatNumbers(entry.getLongValue())));
        }
        rows.sort(Comparator.comparing(row -> row.tag().getName()));
        return rows;
    }

    private void resetDragging() {
        dragging = false;
        pendingMovedX = 0;
        pendingMovedY = 0;
    }

    private record ResearchPointRow(ResearchTag tag, String formattedAmount) {}
}
