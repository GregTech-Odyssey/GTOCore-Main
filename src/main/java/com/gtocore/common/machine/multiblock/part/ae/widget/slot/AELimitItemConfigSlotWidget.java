package com.gtocore.common.machine.multiblock.part.ae.widget.slot;

import com.gtocore.common.machine.multiblock.part.ae.widget.ConfigWidget;

import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.uipro.render.UIDraw;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;

import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.NotNull;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;

/**
 * 限制配置用的物品格：只有 18×18 的配置槽，没有 {@link AEItemConfigSlotWidget} 那格"库存"下半格
 * （可配置存储访问仓的格子只放限制，不放库存）。
 */
public class AELimitItemConfigSlotWidget extends AEItemConfigSlotWidget {

    public AELimitItemConfigSlotWidget(int x, int y, ConfigWidget widget, int index) {
        super(x, y, widget, index);
        setSize(new Size(18, 18));
    }

    /// 没有库存半格，下半格的位置也不响应任何操作
    @Override
    boolean mouseOverStock(double mouseX, double mouseY) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        GenericStack config = slot.getConfig();
        UITheme.ITEM_SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        if (isXeiPhantom()) UIDraw.xeiPhantomMark(graphics, position.x, position.y, 18, 18, false);
        if (config != null) {
            ItemStack stack = config.what() instanceof AEItemKey key ? new ItemStack(key.getItem()) : ItemStack.EMPTY;
            drawItemStack(graphics, stack, position.x + 1, position.y + 1, 0xFFFFFFFF, null);
            UIText.drawItemCount(graphics, config.what().formatAmount(config.amount(), AmountFormat.SLOT_LARGE_FONT), position.x + 1, position.y + 1);
        }
        // 只画配置半格的状态：禁用斜纹、悬停高亮
        if (isConfigDisabled()) UIDraw.disabledHatch(graphics, position.x, position.y, 18, 18);
        if (mouseOverConfig(mouseX, mouseY) && !isConfigDisabled()) UIDraw.hoverOverlay(graphics, position.x, position.y, 18, 18);
    }

    /// 整格都是配置槽：可拖入的范围、滚轮判定都用整格
    @OnlyIn(Dist.CLIENT)
    @Override
    public Rect2i getRectangleBox() {
        return toRectangleBox();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (parentWidget.isStocking()) return false;
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (slot.getConfig() == null || wheelDelta == 0 || !toRectangleBox().contains((int) mouseX, (int) mouseY)) {
            return false;
        }
        GenericStack stack = slot.getConfig();
        long amt;
        if (isCtrlDown()) {
            amt = wheelDelta > 0 ? stack.amount() << 1 : stack.amount() / 2L;
        } else {
            amt = wheelDelta > 0 ? stack.amount() + 1L : stack.amount() - 1L;
        }
        // 允许滚到 0：0 就是"禁止存入/禁止取出"
        if (amt >= 0 && amt <= Integer.MAX_VALUE) {
            writeClientAction(AMOUNT_CHANGE_ID, buf -> buf.writeVarLong(amt));
            return true;
        }
        return false;
    }
}
