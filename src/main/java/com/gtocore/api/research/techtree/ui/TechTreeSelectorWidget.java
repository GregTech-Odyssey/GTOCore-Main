package com.gtocore.api.research.techtree.ui;

import com.gtocore.api.research.techtree.TechTreeManager;
import com.gtocore.utils.GuiHelper;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

public final class TechTreeSelectorWidget extends Widget {

    public static final int HEIGHT = 22;

    private static final int SWITCH_MANAGER_ACTION = 10;
    private static final int PADDING = 2;
    private static final int SLOT_SIZE = 18;

    private final ArrayList<TechTreeManager> managers;
    private final ArrayList<ArrayList<Component>> managerTooltips;
    private final Consumer<TechTreeManager> onManagerChanged;
    private TechTreeManager manager;

    public TechTreeSelectorWidget(int x, int y, int width, TechTreeManager manager,
                                  Consumer<TechTreeManager> onManagerChanged) {
        super(x, y, width, HEIGHT);
        this.manager = manager;
        this.onManagerChanged = onManagerChanged;
        this.managers = new ArrayList<>(TechTreeManager.getManagers().size() + 1);
        managers.addAll(TechTreeManager.getManagers());
        if (!managers.contains(manager)) {
            managers.add(manager);
        }
        this.managerTooltips = new ArrayList<>(managers.size());
        for (TechTreeManager registeredManager : managers) {
            var tooltip = new ArrayList<Component>(1);
            tooltip.add(TechTreeManager.getTreeName(registeredManager));
            managerTooltips.add(tooltip);
        }
        setBackground(GuiTextures.BACKGROUND_INVERSE);
    }

    public void setManager(TechTreeManager newManager) {
        if (manager == newManager) {
            return;
        }
        if (!isClientSideWidget && isRemote()) {
            writeClientAction(SWITCH_MANAGER_ACTION,
                    buffer -> buffer.writeVarInt(TechTreeManager.REGISTRY.getId(newManager)));
        }
        manager = newManager;
        onManagerChanged.accept(newManager);
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == SWITCH_MANAGER_ACTION) {
            int managerId = buffer.readVarInt();
            TechTreeManager requestedManager = TechTreeManager.REGISTRY.get(managerId);
            if (requestedManager != null && managers.contains(requestedManager)) {
                setManager(requestedManager);
            }
            return;
        }
        super.handleClientAction(id, buffer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position pos = getPosition();
        for (int i = 0; i < managers.size(); i++) {
            int slotX = pos.x + PADDING + i * SLOT_SIZE;
            if (slotX + SLOT_SIZE > pos.x + getSize().width - PADDING) {
                break;
            }
            GuiTextures.SLOT.draw(graphics, mouseX, mouseY, slotX, pos.y + PADDING, SLOT_SIZE, SLOT_SIZE);
            TechTreeManager buttonManager = managers.get(i);
            if (buttonManager == manager) {
                DrawerHelper.drawSolidRect(graphics, slotX + 1, pos.y + PADDING + 1, SLOT_SIZE - 2, SLOT_SIZE - 2, 0x5539C5BB);
            }
            buttonManager.getIcon().draw(graphics, mouseX, mouseY, slotX + 1, pos.y + PADDING + 1, 16, 16);
            if (buttonManager == manager) {
                DrawerHelper.drawBorder(graphics, slotX, pos.y + PADDING, SLOT_SIZE, SLOT_SIZE, 0xFF39C5BB, 1);
            } else if (isMouseOver(slotX, pos.y + PADDING, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
                DrawerHelper.drawBorder(graphics, slotX, pos.y + PADDING, SLOT_SIZE, SLOT_SIZE, 0xFFF3F3F3, 1);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        int managerIndex = getHoveredManagerIndex(mouseX, mouseY);
        if (managerIndex >= 0 && gui != null && gui.getModularUIGui() != null) {
            gui.getModularUIGui().setHoverTooltip(managerTooltips.get(managerIndex), ItemStack.EMPTY, null, null);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int managerIndex = getHoveredManagerIndex(mouseX, mouseY);
        if (managerIndex < 0) {
            return false;
        }
        if (button == 0) {
            TechTreeManager selectedManager = managers.get(managerIndex);
            if (selectedManager != manager) {
                setManager(selectedManager);
                playButtonClickSound();
            }
        }
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private int getHoveredManagerIndex(double mouseX, double mouseY) {
        if (!isMouseWithinBounds()) return -1;
        Position pos = getPosition();
        int localX = Mth.floor(mouseX) - pos.x - PADDING;
        int localY = Mth.floor(mouseY) - pos.y - PADDING;
        if (localX < 0 || localY < 0 || localY >= SLOT_SIZE) {
            return -1;
        }
        int index = localX / SLOT_SIZE;
        return index < managers.size() && PADDING + (index + 1) * SLOT_SIZE <= getSize().width - PADDING ? index : -1;
    }

    @OnlyIn(Dist.CLIENT)
    private boolean isMouseWithinBounds() {
        return isMouseOver(getPosition().x, getPosition().y, getSize().width, getSize().height, GuiHelper.getRealMouseX(), GuiHelper.getRealMouseY());
    }
}
