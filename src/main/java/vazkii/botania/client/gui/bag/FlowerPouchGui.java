package vazkii.botania.client.gui.bag;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.FlowerPouchItem;
import vazkii.botania.common.menu.FlowerPouchContainer;

/** 采花袋界面，空格子显示该槽位对应的花的淡影，数量为 1 时也显示数字。 */
public class FlowerPouchGui extends AbstractContainerScreen<FlowerPouchContainer> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("botania", "textures/gui/flower_bag.png");

    public FlowerPouchGui(FlowerPouchContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight += 36;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        gui.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            if (slot.container != menu.flowerBagInv) {
                continue;
            }
            int slotX = leftPos + slot.x;
            int slotY = topPos + slot.y;
            if (!slot.hasItem()) {
                // 空槽：先画花，再盖一层半透明白色让花变浅，和有物品的格子区分开
                gui.renderItem(new ItemStack(FlowerPouchItem.getFlowerForSlot(slot.index)), slotX, slotY);
                gui.fill(slotX, slotY, slotX + 16, slotY + 16, 0x9AFFFFFF);
            } else if (slot.getItem().getCount() == 1) {
                gui.drawString(font, "1", slotX + 11, slotY + 9, 0xFFFFFF);
            }
        }
    }
}
