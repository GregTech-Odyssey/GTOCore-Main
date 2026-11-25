package com.gtocore.common.machine.multiblock.part.ae.widget.slot;

import com.gtocore.utils.GuiHelper;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeStackAdapter;
import com.gregtechceu.gtceu.api.transfer.key.StackInventory;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEPatternViewSlotWidget;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.crafting.pattern.EncodedPatternItem;

import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.ObjIntConsumer;

/**
 * 样板槽：显示样板产物图标。给了 {@code popupKey} 时，中键点击在窗口右侧打开/关闭该槽的弹出面板，
 * 面板打开期间这个槽显示框架统一的选中框（{@link UIElement#setSelected}）。面板状态属于所在的 {@link MachineWindow}，各玩家互不影响，关界面即消失。
 */
public class MEPatternViewSlotWidget extends UIElement {

    private final int slotIndex;
    @Nullable
    private final String popupKey;
    private final int popupArgument;
    private final Inner inner;

    public MEPatternViewSlotWidget(int slotIndex, StackInventory itemHandler, @Nullable String popupKey) {
        this(slotIndex, new ForgeStackAdapter(itemHandler), popupKey, slotIndex);
    }

    public MEPatternViewSlotWidget(int slotIndex, IItemHandlerModifiable itemHandler, @Nullable String popupKey, int popupArgument) {
        layout(l -> l.size(UISizes.SLOT_SIZE, UISizes.SLOT_SIZE));
        this.slotIndex = slotIndex;
        this.popupKey = popupKey;
        this.popupArgument = popupArgument;
        this.inner = new Inner(itemHandler, slotIndex);
        inner.setOccupiedTexture(UITheme.ITEM_SLOT);
        inner.setItemHook(MEPatternViewSlotWidget::displayStack);
        inner.setBackground(UITheme.ITEM_SLOT, GuiTextures.PATTERN_OVERLAY);
        addChild(inner);
        setSelected(this::isPopupOpen);
    }

    private static ItemStack displayStack(ItemStack stack) {
        if (stack.getItem() instanceof EncodedPatternItem pattern) {
            var output = pattern.getOutput(stack);
            if (!output.isEmpty()) return output;
        }
        return stack;
    }

    public AEPatternViewSlotWidget getInner() {
        return inner;
    }

    public void setVirtualClick(BooleanSupplier enabled, ObjIntConsumer<ClickType> click) {
        setClientSideWidget();
        inner.virtualEnabled = enabled;
        inner.virtualClick = click;
        inner.setItemHook(inner::cachedDisplayStack);
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    private boolean isPopupOpen() {
        if (popupKey == null) return false;
        var window = MachineWindow.of(this);
        return window != null && window.isPopupOpen(popupKey, popupArgument);
    }

    private void togglePopup() {
        if (popupKey == null) return;
        var window = MachineWindow.of(this);
        if (window != null) window.togglePopup(popupKey, popupArgument);
    }

    private final class Inner extends AEPatternViewSlotWidget {

        @Nullable
        private BooleanSupplier virtualEnabled;
        @Nullable
        private ObjIntConsumer<ClickType> virtualClick;
        private ItemStack lastPattern = ItemStack.EMPTY;
        private ItemStack displayed = ItemStack.EMPTY;

        private Inner(IItemHandlerModifiable itemHandler, int slotIndex) {
            super(itemHandler, slotIndex, 0, 0);
        }

        private ItemStack cachedDisplayStack(ItemStack stack) {
            if (stack != lastPattern) {
                lastPattern = stack;
                displayed = displayStack(stack);
            }
            return displayed;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (virtualEnabled == null || virtualEnabled.getAsBoolean());
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (slotReference != null && gui != null && isEnabled() && isMouseOverElement(mouseX, mouseY)) {
                if (popupKey != null && button == 2) {
                    togglePopup();
                    return true;
                }
                if (virtualClick != null && button >= 0 && button <= 2) {
                    var type = button == 2 ? ClickType.CLONE : Screen.hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP;
                    virtualClick.accept(type, button);
                    return true;
                }
            }
            return virtualClick == null && super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return virtualClick == null && super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return virtualClick == null && super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            if (virtualClick == null) {
                super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
            } else if (gui != null && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
                // A viewport slot has no menu ID and must never become vanilla's hovered slot.
                var stack = getItem();
                gui.getModularUIGui().setHoverTooltip(getFullTooltipTexts(), stack, null, stack.getTooltipImage().orElse(null));
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (virtualClick != null && isEnabled() && gui != null && gui.mainGroup.getHoverElement(GuiHelper.getRealMouseX(), GuiHelper.getRealMouseY()) == this) {
                var options = Minecraft.getInstance().options;
                if (options.keyDrop.matches(keyCode, scanCode)) {
                    virtualClick.accept(ClickType.THROW, Screen.hasControlDown() ? 1 : 0);
                    return true;
                }
                if (options.keySwapOffhand.matches(keyCode, scanCode)) {
                    virtualClick.accept(ClickType.SWAP, 40);
                    return true;
                }
                for (int hotbar = 0; hotbar < options.keyHotbarSlots.length; hotbar++) {
                    if (options.keyHotbarSlots[hotbar].matches(keyCode, scanCode)) {
                        virtualClick.accept(ClickType.SWAP, hotbar);
                        return true;
                    }
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
