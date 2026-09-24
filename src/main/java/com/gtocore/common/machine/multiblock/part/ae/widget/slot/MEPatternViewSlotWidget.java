package com.gtocore.common.machine.multiblock.part.ae.widget.slot;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEPatternViewSlotWidget;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.window.MachineWindow;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.crafting.pattern.EncodedPatternItem;

import org.jetbrains.annotations.Nullable;

/**
 * 样板槽：显示样板产物图标。给了 {@code popupKey} 时，中键点击在窗口右侧打开/关闭该槽的弹出面板，
 * 面板打开期间这个槽显示框架统一的选中框（{@link UIElement#setSelected}）。面板状态属于所在的 {@link MachineWindow}，各玩家互不影响，关界面即消失。
 */
public class MEPatternViewSlotWidget extends UIElement {

    private final int slotIndex;
    @Nullable
    private final String popupKey;
    private final Inner inner;

    public MEPatternViewSlotWidget(int slotIndex, ICustomItemStackHandler itemHandler, @Nullable String popupKey) {
        layout(l -> l.size(18, 18));
        this.slotIndex = slotIndex;
        this.popupKey = popupKey;
        this.inner = new Inner(itemHandler, slotIndex);
        inner.setOccupiedTexture(UITheme.ITEM_SLOT);
        inner.setItemHook(stack -> {
            if (stack.getItem() instanceof EncodedPatternItem pattern) {
                var output = pattern.getOutput(stack);
                if (!output.isEmpty()) return output;
            }
            return stack;
        });
        inner.setBackground(UITheme.ITEM_SLOT, GuiTextures.PATTERN_OVERLAY);
        addChild(inner);
        setSelected(this::isPopupOpen);
    }

    public AEPatternViewSlotWidget getInner() {
        return inner;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    private boolean isPopupOpen() {
        if (popupKey == null) return false;
        var window = MachineWindow.of(this);
        return window != null && window.isPopupOpen(popupKey, slotIndex);
    }

    private void togglePopup() {
        if (popupKey == null) return;
        var window = MachineWindow.of(this);
        if (window != null) window.togglePopup(popupKey, slotIndex);
    }

    private final class Inner extends AEPatternViewSlotWidget {

        private Inner(ICustomItemStackHandler itemHandler, int slotIndex) {
            super(itemHandler, slotIndex, 0, 0);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (popupKey != null && slotReference != null && gui != null && button == 2 && isMouseOverElement(mouseX, mouseY)) {
                togglePopup();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
