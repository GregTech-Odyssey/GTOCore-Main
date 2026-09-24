package com.gtocore.common.machine.multiblock.part.ae.widget.slot;

import com.gtocore.common.machine.multiblock.part.ae.widget.ConfigWidget;

import com.gregtechceu.gtceu.api.gui.misc.IGhostItemTarget;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawItemStack;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringFixedCorner;

public class AEItemConfigSlotWidget extends AEConfigSlotWidget implements IGhostItemTarget {

    public AEItemConfigSlotWidget(int x, int y, ConfigWidget widget, int index) {
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
        drawSlots(graphics, mouseX, mouseY, UITheme.ITEM_SLOT, false);
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (config != null) {
            ItemStack stack = config.what() instanceof AEItemKey key ? new ItemStack(key.getItem()) : ItemStack.EMPTY;
            drawItemStack(graphics, stack, stackX, stackY, 0xFFFFFFFF, null);

            if (parentWidget.showAmount()) {
                String amountStr = TextFormattingUtil.formatLongToCompactString(config.amount(), 4);
                drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 17, 16777215, true, 0.5f);
            }
        }
        if (stock != null) {
            ItemStack stack = stock.what() instanceof AEItemKey key ? new ItemStack(key.getItem()) : ItemStack.EMPTY;
            drawItemStack(graphics, stack, stackX, stackY + 18, 0xFFFFFFFF, null);
            String amountStr = TextFormattingUtil.formatLongToCompactString(stock.amount(), 4);
            drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 18 + 17, 16777215, true, 0.5f);
        }
        drawStates(graphics, mouseX, mouseY);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseOverConfig(mouseX, mouseY)) {
            if (parentWidget.isAutoPull()) {
                return false;
            }

            if (button == 1) {
                writeClientAction(REMOVE_ID, buf -> {});

                if (parentWidget.showAmount()) {
                    this.parentWidget.disableAmountClient();
                }
            } else if (button == 0) {
                // 左键：拿着物品时设为配置；空手点空格子无反应，点有配置的格子打开数量面板
                ItemStack item = this.gui.getModularUIContainer().getCarried();

                if (!item.isEmpty()) {
                    writeClientAction(UPDATE_ID, buf -> GenericStack.writeBuffer(GenericStack.fromItemStack(item), buf));
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
        var slot = this.parentWidget.getConfig(this.index);
        switch (id) {
            case REMOVE_ID -> {
                slot.setConfig(null);
                this.parentWidget.notifyConfigChanged();
                writeUpdateInfo(REMOVE_ID, buf -> {});
            }
            case UPDATE_ID -> {
                // writeItem 的数量只占一个有符号字节，EMI 带过来的 130、420、1024 这类数量会被读成空物品，故改用 GenericStack
                var stack = GenericStack.readBuffer(buffer);
                if (stack != null && (!(stack.what() instanceof AEItemKey) || stack.amount() <= 0)) return;
                if (!isStackValidForSlot(stack)) return;
                slot.setConfig(stack);
                this.parentWidget.notifyConfigChanged();
                if (stack != null) {
                    writeUpdateInfo(UPDATE_ID, buf -> GenericStack.writeBuffer(stack, buf));
                }
            }
            case AMOUNT_CHANGE_ID -> {
                long amt = buffer.readVarLong();
                // 与数量面板同一套校验（客户端可以伪造）
                if (amt < this.parentWidget.minAmount() || !this.parentWidget.canSetAmount(this.index)) return;
                slot.setConfig(new GenericStack(slot.getConfig().what(), amt));
                this.parentWidget.notifyConfigChanged();
                writeUpdateInfo(AMOUNT_CHANGE_ID, buf -> buf.writeVarLong(amt));
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
            case UPDATE_ID -> slot.setConfig(GenericStack.readBuffer(buffer));
            case AMOUNT_CHANGE_ID -> {
                if (slot.getConfig() != null) slot.setConfig(new GenericStack(slot.getConfig().what(), buffer.readVarLong()));
            }
        }
    }

    /** 只在上格可从 EMI 拖入时接受（LDLib2 {@code xeiPhantom}）。 */
    @OnlyIn(Dist.CLIENT)
    @Override
    public List<Target> getPhantomTargets(Object ingredient) {
        return isXeiPhantom() ? IGhostItemTarget.super.getPhantomTargets(ingredient) : List.of();
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
    public void acceptItem(ItemStack itemStack) {
        writeClientAction(UPDATE_ID, buf -> GenericStack.writeBuffer(GenericStack.fromItemStack(itemStack), buf));
    }
}
