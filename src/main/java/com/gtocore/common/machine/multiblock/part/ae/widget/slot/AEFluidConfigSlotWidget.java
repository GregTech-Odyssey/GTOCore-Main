package com.gtocore.common.machine.multiblock.part.ae.widget.slot;

import com.gtocore.common.machine.multiblock.part.ae.widget.ConfigWidget;

import com.gregtechceu.gtceu.api.gui.misc.IGhostFluidTarget;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AmountFormat;
import appeng.api.stacks.GenericStack;

import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AEFluidConfigSlotWidget extends AEConfigSlotWidget implements IGhostFluidTarget {

    public AEFluidConfigSlotWidget(int x, int y, ConfigWidget widget, int index) {
        super(new Position(x, y), new Size(18, 36), widget, index);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        GenericStack config = slot.getConfig();
        GenericStack stock = slot.getStock();
        drawSlots(graphics, mouseX, mouseY, UITheme.FLUID_SLOT, true);

        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (config != null) {
            var stack = AEUtil.toFluidStack(config);
            if (!stack.isEmpty()) {
                DrawerHelper.drawFluidForGui(graphics, FluidHelperImpl.toFluidStack(stack), config.amount(), stackX,
                        stackY, 16, 16);
                if (parentWidget.isAmountShown()) {
                    UIText.drawItemCount(graphics, config.what().formatAmount(config.amount(), AmountFormat.SLOT_LARGE_FONT), stackX, stackY);
                }
            }
        }
        if (stock != null) {
            var stack = AEUtil.toFluidStack(stock);
            if (!stack.isEmpty()) {
                DrawerHelper.drawFluidForGui(graphics, FluidHelperImpl.toFluidStack(stack), stock.amount(), stackX,
                        stackY + 18, 16,
                        16);
                UIText.drawItemCount(graphics, stock.what().formatAmount(stock.amount(), AmountFormat.SLOT_LARGE_FONT), stackX, stackY + 18);
            }
        }

        drawStates(graphics, mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseOverConfig(mouseX, mouseY)) {
            // don't allow manual interaction with config slots when auto pull is enabled
            if (parentWidget.isAutoPull()) {
                return false;
            }

            if (button == 1) {
                // Right click to clear
                writeClientAction(REMOVE_ID, buf -> {});

                if (parentWidget.isAmountShown()) {
                    this.parentWidget.disableAmountClient();
                }
            } else if (button == 0) {
                // 左键：拿着装有流体的容器时设为配置；否则点空格子无反应，点有配置的格子打开数量面板
                ItemStack hold = this.gui.getModularUIContainer().getCarried();
                var fluid = FluidUtil.getFluidContained(hold);
                if (fluid.isPresent()) {
                    writeClientAction(UPDATE_ID, fluid.get()::writeToPacket);
                } else if (this.parentWidget.getDisplay(this.index).getConfig() == null) {
                    return true;
                }

                if (!parentWidget.isStocking()) {
                    this.parentWidget.enableAmountClient(this.index);
                    this.select = true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (rejectsDisabledAction(id)) return;
        IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
        switch (id) {
            case REMOVE_ID -> {
                slot.setConfig(null);
                this.parentWidget.notifyConfigChanged();
                writeUpdateInfo(REMOVE_ID, buf -> {});
            }
            case UPDATE_ID -> {
                FluidStack fluid = FluidStack.readFromPacket(buffer);
                var stack = AEUtil.fromFluidStack(fluid);
                if (!isStackValidForSlot(stack)) return;
                slot.setConfig(stack);
                this.parentWidget.notifyConfigChanged();
                if (fluid != FluidStack.EMPTY) {
                    writeUpdateInfo(UPDATE_ID, fluid::writeToPacket);
                }
            }
            case AMOUNT_CHANGE_ID -> {
                int amt = buffer.readInt();
                // 与数量面板同一套校验（客户端可以伪造）
                if (amt < this.parentWidget.getMinAmount() || !this.parentWidget.canSetAmount(this.index)) return;
                slot.setConfig(new GenericStack(slot.getConfig().what(), amt));
                this.parentWidget.notifyConfigChanged();
                writeUpdateInfo(AMOUNT_CHANGE_ID, buf -> buf.writeInt(amt));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        switch (id) {
            case REMOVE_ID -> slot.setConfig(null);
            case UPDATE_ID -> {
                FluidStack fluid = new FluidStack(BuiltInRegistries.FLUID.get(buffer.readResourceLocation()),
                        buffer.readVarInt());
                slot.setConfig(new GenericStack(AEFluidKey.of(fluid.getFluid()), fluid.getAmount()));
            }
            case AMOUNT_CHANGE_ID -> {
                if (slot.getConfig() != null) slot.setConfig(new GenericStack(slot.getConfig().what(), buffer.readInt()));
            }
        }
    }

    /** 只在上格可从 EMI 拖入时接受（LDLib2 {@code xeiPhantom}）。 */
    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Target> getPhantomTargets(Object ingredient) {
        return isXeiPhantom() ? IGhostFluidTarget.super.getPhantomTargets(ingredient) : List.of();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Rect2i getRectangleBox() {
        Rect2i rectangle = toRectangleBox();
        rectangle.setHeight(rectangle.getHeight() / 2);
        return rectangle;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void acceptFluid(FluidStack fluidStack) {
        if (fluidStack.getRawFluid() != Fluids.EMPTY && fluidStack.getAmount() <= 0L) {
            fluidStack.setAmount(1000);
        }

        if (!fluidStack.isEmpty()) {
            writeClientAction(UPDATE_ID, fluidStack::writeToPacket);
        }
    }
}
