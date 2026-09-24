package com.gtocore.common.machine.multiblock.part.ae.widget.slot;

import com.gtocore.common.machine.multiblock.part.ae.widget.ConfigWidget;

import com.gregtechceu.gtceu.integration.ae2.gui.widget.list.AEListGridWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.NotNull;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringFixedCorner;

/**
 * 限制配置用的流体格：只有 18×18 的配置槽，没有 {@link AEFluidConfigSlotWidget} 那格"库存"下半格
 * （可配置存储访问仓的格子只放限制，不放库存）。
 */
public class AELimitFluidConfigSlotWidget extends AEFluidConfigSlotWidget {

    public AELimitFluidConfigSlotWidget(int x, int y, ConfigWidget widget, int index) {
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
        UITheme.FLUID_SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        if (isXeiPhantom()) UITheme.drawXeiPhantom(graphics, position.x, position.y, 18, 18, true);
        if (config != null) {
            var stack = AEUtil.toFluidStack(config);
            if (!stack.isEmpty()) {
                DrawerHelper.drawFluidForGui(graphics, FluidHelperImpl.toFluidStack(stack), config.amount(),
                        position.x + 1, position.y + 1, 16, 16);
                String amountStr = FormattingUtil.formatNumberReadable(config.amount(), true,
                        FormattingUtil.DECIMAL_FORMAT_0F, "B");
                drawStringFixedCorner(graphics, amountStr, position.x + 18, position.y + 18, 16777215, true, 0.5f);
            }
        }
        // 只画配置半格的状态：禁用斜纹、悬停高亮、选中框
        if (isConfigDisabled()) UITheme.drawDisabled(graphics, position.x, position.y, 18, 18);
        if (mouseOverConfig(mouseX, mouseY) && !isConfigDisabled()) {
            AEListGridWidget.drawSelectionOverlay(graphics, position.x + 1, position.y + 1, 16, 16);
        }
        if (this.select) UITheme.drawSelection(graphics, position.x, position.y, 18, 18);
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
        long amount = slot.getConfig().what() instanceof AEFluidKey fluidKey ?
                new FluidStack(fluidKey.getFluid(), GTMath.saturatedCast(slot.getConfig().amount()), fluidKey.getTag())
                        .getAmount() :
                0;
        long amt;
        if (isCtrlDown()) {
            amt = wheelDelta > 0 ? amount * 2L : amount / 2L;
        } else {
            amt = wheelDelta > 0 ? amount + 1L : amount - 1L;
        }
        // 允许滚到 0：0 就是"禁止存入/禁止取出"
        if (amt >= 0 && amt <= Integer.MAX_VALUE) {
            int finalAmt = (int) amt;
            writeClientAction(AMOUNT_CHANGE_ID, buf -> buf.writeInt(finalAmt));
            return true;
        }
        return false;
    }
}
