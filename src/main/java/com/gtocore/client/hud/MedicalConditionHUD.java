package com.gtocore.client.hud;

import com.gtocore.common.medical.MedicalConditionSync;
import com.gtocore.config.GTOConfig;

import com.gtolib.GTOCore;
import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
@DataGeneratorScanned
public final class MedicalConditionHUD implements IMoveableHUD {

    public static final MedicalConditionHUD INSTANCE = new MedicalConditionHUD();

    @RegisterLanguage(en = "Disease HUD", cn = "疾病 HUD")
    public static final String DISPLAY_NAME = "gtocore.hud.medical_condition.name";

    @RegisterLanguage(en = "No disease (0%)", cn = "无疾病（0%）")
    public static final String HEALTHY_LABEL = "gtocore.hud.medical_condition.healthy";

    private static final ResourceLocation TEXTURE = GTOCore.id("textures/gui/overlay/medical_bar.png");
    private static final int TEXTURE_WIDTH = 33;
    private static final int TEXTURE_HEIGHT = 101;
    private static final int BAR_WIDTH = 11;
    private static final int BAR_HEIGHT = 98;
    private static final int ICON_SIZE = 9;
    private static final int LABEL_X = BAR_WIDTH + 3;
    private static final int TEXT_COLOR = 0xFF7D7D54;
    private static final Component NAME = Component.translatable(DISPLAY_NAME);

    private MedicalCondition labelCondition;
    private Language labelLanguage;
    private int labelPercent = -1;
    private Component label = Component.empty();
    private int labelWidth;
    private final StringBuilder labelBuilder = new StringBuilder(64);
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);
    private Rect2i anchorBounds = bounds;
    private boolean dragging;
    private int dragStartX;
    private int dragStartY;
    private int pendingMovedX;
    private int pendingMovedY;

    private MedicalConditionHUD() {}

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(pendingMovedX, pendingMovedY, 1500);
        renderGeneral(guiGraphics, partialTick, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        guiGraphics.pose().popPose();
    }

    @Override
    public void renderGeneral(GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        var snapshot = MedicalConditionSync.getClientSnapshot();
        if (snapshot.condition() == null && !(mc.screen instanceof HUDScreen)) return;
        float ratio = snapshot.ratio();
        updateLabel(snapshot.condition(), ratio);
        updateBounds(screenWidth, screenHeight);
        int x = bounds.getX();
        int y = bounds.getY();

        // The atlas contains the full bar at x=0, the empty bar at x=11, and the icon at x=23.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(TEXTURE, x, y, 11, 3, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int filled = Mth.ceil(Mth.clamp(ratio, 0, 1) * BAR_HEIGHT);
        if (filled > 0) {
            int offset = BAR_HEIGHT - filled;
            guiGraphics.blit(TEXTURE, x, y + offset, 0, 3 + offset, BAR_WIDTH, filled, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        int labelY = y + BAR_HEIGHT - ICON_SIZE;
        guiGraphics.blit(TEXTURE, x + LABEL_X, labelY, 23, 3, ICON_SIZE, ICON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RenderSystem.disableBlend();
        guiGraphics.drawString(mc.font, label, x + LABEL_X + ICON_SIZE + 2, labelY, TEXT_COLOR, false);
    }

    private void updateLabel(MedicalCondition condition, float ratio) {
        int percent = Mth.floor(ratio * 100);
        Language language = Language.getInstance();
        if (labelCondition == condition && labelPercent == percent && labelLanguage == language) return;
        labelCondition = condition;
        labelLanguage = language;
        labelPercent = percent;
        if (condition == null) {
            label = Component.translatable(HEALTHY_LABEL);
        } else {
            labelBuilder.setLength(0);
            String key = labelBuilder.append("gtceu.medical_condition.").append(condition.name).toString();
            String name = ChatFormatting.stripFormatting(language.getOrDefault(key));
            labelBuilder.setLength(0);
            labelBuilder.append(name).append(" (");
            if (percent == 0) {
                labelBuilder.append("<1");
            } else {
                labelBuilder.append(percent);
            }
            label = Component.literal(labelBuilder.append("%)").toString());
        }
        labelWidth = Minecraft.getInstance().font.width(label);
    }

    private void updateBounds(int screenWidth, int screenHeight) {
        int width = LABEL_X + ICON_SIZE + 2 + labelWidth;
        int x = (int) (GTOConfig.INSTANCE.client.hud.medicalConditionHUDDefaultX / 100.0 * Math.max(0, screenWidth - width));
        int y = (int) (GTOConfig.INSTANCE.client.hud.medicalConditionHUDDefaultY / 100.0 * Math.max(0, screenHeight - BAR_HEIGHT));
        if (bounds.getX() != x || bounds.getY() != y || bounds.getWidth() != width) {
            bounds = new Rect2i(x, y, width, BAR_HEIGHT);
        }
    }

    @Override
    public Rect2i getBounds(int screenWidth, int screenHeight) {
        var snapshot = MedicalConditionSync.getClientSnapshot();
        updateLabel(snapshot.condition(), snapshot.ratio());
        updateBounds(screenWidth, screenHeight);
        return bounds;
    }

    @Override
    public Rect2i getPropertyAnchorBounds(int screenWidth, int screenHeight) {
        Rect2i current = getBounds(screenWidth, screenHeight);
        int x = current.getX() + pendingMovedX;
        int y = current.getY() + pendingMovedY;
        if (anchorBounds.getX() != x || anchorBounds.getY() != y || anchorBounds.getWidth() != current.getWidth()) {
            anchorBounds = new Rect2i(x, y, current.getWidth(), BAR_HEIGHT);
        }
        return anchorBounds;
    }

    @Override
    public Component getDisplayName() {
        return NAME;
    }

    @Override
    public boolean isEnabled() {
        return GTOConfig.INSTANCE.client.hud.medicalConditionHUDEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        GTOConfig.set("medicalConditionHUDEnabled", enabled, "client", "hud");
        if (!enabled) resetDragging();
    }

    @Override
    public boolean isPositionDragging() {
        return dragging || pendingMovedX != 0 || pendingMovedY != 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;
        dragging = true;
        dragStartX = (int) mouseX;
        dragStartY = (int) mouseY;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging) return false;
        pendingMovedX = (int) mouseX - dragStartX;
        pendingMovedY = (int) mouseY - dragStartY;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = isPositionDragging();
        if (pendingMovedX != 0 || pendingMovedY != 0) {
            Minecraft mc = Minecraft.getInstance();
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            Rect2i current = getBounds(width, height);
            setTopLeftPosition(current.getX() + pendingMovedX, current.getY() + pendingMovedY, width, height);
        }
        resetDragging();
        return handled;
    }

    @Override
    public void setTopLeftPosition(int x, int y, int screenWidth, int screenHeight) {
        Rect2i current = getBounds(screenWidth, screenHeight);
        int maxX = Math.max(0, screenWidth - current.getWidth());
        int maxY = Math.max(0, screenHeight - current.getHeight());
        int relativeX = (int) Math.round(Mth.clamp(x, 0, maxX) * 100.0 / Math.max(1, maxX));
        int relativeY = (int) Math.round(Mth.clamp(y, 0, maxY) * 100.0 / Math.max(1, maxY));
        GTOConfig.set("medicalConditionHUDDefaultX", relativeX, "client", "hud");
        GTOConfig.set("medicalConditionHUDDefaultY", relativeY, "client", "hud");
        resetDragging();
    }

    private void resetDragging() {
        dragging = false;
        pendingMovedX = 0;
        pendingMovedY = 0;
    }
}
