package com.gtocore.client.hud;

import com.gtocore.utils.GuiHelper;

import com.gtolib.api.gui.HudUIStyle;
import com.gtolib.api.gui.WirelessEnergyFormatControl;

import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UIStyleManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class HUDScreen extends Screen {

    private static final int BUTTON_SIZE = UISizes.DOCK_BUTTON_SIZE;
    private static final int BUTTON_MARGIN = UISizes.WINDOW_PADDING_X;
    private static final int DROPDOWN_GAP = UISizes.SECTION_GAP;
    private static final int DROPDOWN_ENTRY_HEIGHT = UISizes.SLOT_SIZE;
    private static final int DROPDOWN_TEXT_PADDING = UISizes.TEXT_PADDING;
    private static final int DROPDOWN_MIN_WIDTH = 110;

    private final ObjectArrayList<IMoveableHUD> enabledHuds = new ObjectArrayList<>();
    private final ObjectArrayList<IMoveableHUD> hiddenHuds = new ObjectArrayList<>();
    private IMoveableHUD activeHud;
    private IMoveableHUD selectedHud;
    private HudAnchor activeHudOrigin;
    private int activeMouseButton = -1;
    private boolean dropdownOpen = false;
    private WirelessEnergyFormatControl wirelessEnergyFormat;
    private boolean stylePushed;

    public HUDScreen() {
        super(Component.literal("HUD Editor"));
    }

    @Override
    protected void init() {
        super.init();
        if (!stylePushed) {
            UIStyleManager.pushOverride(UIStyleManager.STARFIELD_ID, UIStyleManager.STARFIELD_ID);
            stylePushed = true;
        }
        ensureSelectedHud();
        wirelessEnergyFormat = addRenderableWidget(new WirelessEnergyFormatControl());
        updateWirelessEnergyFormat();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        refreshHuds();
        if (hiddenHuds.isEmpty()) {
            dropdownOpen = false;
        }

        for (IMoveableHUD hud : enabledHuds) {
            hud.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 2000);
        if (selectedHud != null && selectedHud.isEnabled()) {
            var bounds = selectedHud.getPropertyAnchorBounds(width, height);
            UIDraw.strokeRoundRect(guiGraphics, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), HudUIStyle.get().accent);
        }

        renderCornerButton(guiGraphics, mouseX, mouseY, hiddenHuds);
        if (dropdownOpen) {
            renderHiddenHudDropdown(guiGraphics, mouseX, mouseY, hiddenHuds);
        }
        guiGraphics.pose().popPose();

        updateWirelessEnergyFormat();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (wirelessEnergyFormat.visible && wirelessEnergyFormat.isMouseOver(mouseX, mouseY)) return true;

        refreshHuds();
        Rect2i buttonBounds = getCornerButtonBounds();

        if (!isPositionDragActive() && button == 0 && dropdownOpen) {
            IMoveableHUD hiddenHud = getHiddenHudAt(mouseX, mouseY, hiddenHuds);
            if (hiddenHud != null) {
                dropdownOpen = false;
                hiddenHud.setEnabled(true);
                hiddenHud.setTopLeftPosition((int) mouseX, (int) mouseY, width, height);
                setSelectedHud(hiddenHud);
                return beginHudInteraction(hiddenHud, mouseX, mouseY, button);
            }
        }

        if (button == 0 && contains(buttonBounds, mouseX, mouseY)) {
            if (isPositionDragActive()) {
                return true;
            }
            dropdownOpen = !hiddenHuds.isEmpty() && !dropdownOpen;
            return true;
        }

        if (dropdownOpen && !contains(getDropdownBounds(hiddenHuds), mouseX, mouseY)) {
            dropdownOpen = false;
        }

        for (int i = enabledHuds.size() - 1; i >= 0; i--) {
            IMoveableHUD hud = enabledHuds.get(i);
            if (hud.mouseClicked(mouseX, mouseY, button)) {
                dropdownOpen = false;
                setSelectedHud(hud);
                return captureHudInteraction(hud, button);
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (activeHud != null) {
            boolean handled = activeHud.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            if (handled || activeHud.isPositionDragging()) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeHud != null) {
            IMoveableHUD hud = activeHud;
            HudAnchor origin = activeHudOrigin;
            boolean shouldHide = hud.isPositionDragging() && contains(getCornerButtonBounds(), mouseX, mouseY);
            hud.mouseReleased(mouseX, mouseY, activeMouseButton >= 0 ? activeMouseButton : button);
            if (shouldHide && origin != null) {
                hud.setTopLeftPosition(origin.x(), origin.y(), width, height);
                hud.setEnabled(false);
            }
            clearHudInteraction();
            ensureSelectedHud();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void removed() {
        releaseActiveHudAtCursor();
        dropdownOpen = false;
        super.removed();
        if (stylePushed) {
            UIStyleManager.popOverride();
            stylePushed = false;
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {}

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private boolean beginHudInteraction(IMoveableHUD hud, double mouseX, double mouseY, int button) {
        if (!hud.mouseClicked(mouseX, mouseY, button)) {
            return false;
        }
        return captureHudInteraction(hud, button);
    }

    private boolean captureHudInteraction(IMoveableHUD hud, int button) {
        Rect2i bounds = hud.getBounds(width, height);
        activeHud = hud;
        activeHudOrigin = new HudAnchor(bounds.getX(), bounds.getY());
        activeMouseButton = button;
        return true;
    }

    private void clearHudInteraction() {
        activeHud = null;
        activeHudOrigin = null;
        activeMouseButton = -1;
    }

    private void releaseActiveHudAtCursor() {
        if (activeHud == null) {
            return;
        }
        activeHud.mouseReleased(getGuiMouseX(), getGuiMouseY(), Math.max(activeMouseButton, 0));
        clearHudInteraction();
    }

    private void renderCornerButton(GuiGraphics guiGraphics, int mouseX, int mouseY, ObjectArrayList<IMoveableHUD> hiddenHuds) {
        Rect2i buttonBounds = getCornerButtonBounds();
        boolean positionDragging = isPositionDragActive();
        boolean enabled = positionDragging || !hiddenHuds.isEmpty();
        boolean hovered = contains(buttonBounds, mouseX, mouseY);
        var style = HudUIStyle.get();
        int textColor = enabled ? style.text : style.disabledText;
        String label = positionDragging ? "\uE21E" : "+";

        style.button(guiGraphics, buttonBounds, hovered, enabled, positionDragging);
        guiGraphics.drawCenteredString(font, label,
                buttonBounds.getX() + buttonBounds.getWidth() / 2,
                buttonBounds.getY() + (buttonBounds.getHeight() - font.lineHeight) / 2,
                textColor);
    }

    private void renderHiddenHudDropdown(GuiGraphics guiGraphics, int mouseX, int mouseY, ObjectArrayList<IMoveableHUD> hiddenHuds) {
        if (hiddenHuds.isEmpty()) {
            return;
        }
        Rect2i dropdownBounds = getDropdownBounds(hiddenHuds);
        var style = HudUIStyle.get();
        style.window(guiGraphics, dropdownBounds);

        for (int i = 0; i < hiddenHuds.size(); i++) {
            Rect2i entryBounds = getDropdownEntryBounds(dropdownBounds, i);
            boolean hovered = contains(entryBounds, mouseX, mouseY);
            if (hovered) {
                UIDraw.fillRect(guiGraphics, entryBounds.getX(), entryBounds.getY(),
                        entryBounds.getWidth(), entryBounds.getHeight(), 0x20000000 | style.accent & 0xFFFFFF);
            }
            guiGraphics.drawString(font, hiddenHuds.get(i).getDisplayName(),
                    entryBounds.getX() + DROPDOWN_TEXT_PADDING,
                    entryBounds.getY() + (entryBounds.getHeight() - font.lineHeight) / 2,
                    hovered ? style.accent : style.text,
                    false);
        }
    }

    private boolean isPositionDragActive() {
        return activeHud != null && activeHud.isPositionDragging();
    }

    private void ensureSelectedHud() {
        if (selectedHud != null && selectedHud.isEnabled()) {
            return;
        }
        selectedHud = null;
        for (IMoveableHUD hud : IMoveableHUD.REGISTERED_HUDS.values()) {
            if (hud.isEnabled()) {
                selectedHud = hud;
                break;
            }
        }
    }

    private void setSelectedHud(IMoveableHUD hud) {
        selectedHud = hud;
    }

    private void updateWirelessEnergyFormat() {
        wirelessEnergyFormat.visible = selectedHud == WirelessEnergyHUD.INSTANCE && selectedHud.isEnabled() &&
                !isPositionDragActive() && !dropdownOpen;
        wirelessEnergyFormat.active = wirelessEnergyFormat.visible;
        if (wirelessEnergyFormat.visible) {
            wirelessEnergyFormat.position(selectedHud.getPropertyAnchorBounds(width, height), width, height);
        }
    }

    private void refreshHuds() {
        enabledHuds.clear();
        hiddenHuds.clear();
        for (IMoveableHUD hud : IMoveableHUD.REGISTERED_HUDS.values()) {
            if (hud.isEnabled()) {
                enabledHuds.add(hud);
            } else {
                hiddenHuds.add(hud);
            }
        }
    }

    private IMoveableHUD getHiddenHudAt(double mouseX, double mouseY, ObjectArrayList<IMoveableHUD> hiddenHuds) {
        if (hiddenHuds.isEmpty()) {
            return null;
        }
        Rect2i dropdownBounds = getDropdownBounds(hiddenHuds);
        if (!contains(dropdownBounds, mouseX, mouseY)) {
            return null;
        }
        for (int i = 0; i < hiddenHuds.size(); i++) {
            if (contains(getDropdownEntryBounds(dropdownBounds, i), mouseX, mouseY)) {
                return hiddenHuds.get(i);
            }
        }
        return null;
    }

    private Rect2i getCornerButtonBounds() {
        return new Rect2i(width - BUTTON_MARGIN - BUTTON_SIZE, BUTTON_MARGIN, BUTTON_SIZE, BUTTON_SIZE);
    }

    private Rect2i getDropdownBounds(ObjectArrayList<IMoveableHUD> hiddenHuds) {
        Rect2i buttonBounds = getCornerButtonBounds();
        int dropdownWidth = DROPDOWN_MIN_WIDTH;
        for (IMoveableHUD hud : hiddenHuds) {
            dropdownWidth = Math.max(dropdownWidth, font.width(hud.getDisplayName()) + 2 * (DROPDOWN_TEXT_PADDING + UISizes.PANEL_PADDING));
        }
        int dropdownX = buttonBounds.getX() + buttonBounds.getWidth() - dropdownWidth;
        int dropdownY = buttonBounds.getY() + buttonBounds.getHeight() + DROPDOWN_GAP;
        return new Rect2i(dropdownX, dropdownY, dropdownWidth, hiddenHuds.size() * DROPDOWN_ENTRY_HEIGHT + 2 * UISizes.PANEL_PADDING);
    }

    private Rect2i getDropdownEntryBounds(Rect2i dropdownBounds, int index) {
        return new Rect2i(dropdownBounds.getX() + UISizes.PANEL_PADDING,
                dropdownBounds.getY() + UISizes.PANEL_PADDING + (index * DROPDOWN_ENTRY_HEIGHT),
                dropdownBounds.getWidth() - 2 * UISizes.PANEL_PADDING,
                DROPDOWN_ENTRY_HEIGHT);
    }

    private boolean contains(Rect2i bounds, double mouseX, double mouseY) {
        return bounds != null && bounds.contains((int) mouseX, (int) mouseY);
    }

    private double getGuiMouseX() {
        Minecraft mc = minecraft;
        if (mc == null || mc.getWindow().getScreenWidth() == 0) {
            return 0;
        }
        return GuiHelper.getRealMouseX();
    }

    private double getGuiMouseY() {
        Minecraft mc = minecraft;
        if (mc == null || mc.getWindow().getScreenHeight() == 0) {
            return 0;
        }
        return GuiHelper.getRealMouseY();
    }

    private record HudAnchor(int x, int y) {}
}
