package com.gtocore.client.renderer.item;

import com.gtocore.common.block.WirelessEnergyUnitBlock;
import com.gtocore.common.blockentity.WirelessEnergyUnitBlockEntity;

import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public final class WirelessEnergyUnitItemDecorations implements IItemDecorator {

    public static final WirelessEnergyUnitItemDecorations DECORATOR = new WirelessEnergyUnitItemDecorations();
    private static final int BAR_BITS = 60;

    private WirelessEnergyUnitItemDecorations() {}

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!(stack.getItem() instanceof BlockItem item) || !(item.getBlock() instanceof WirelessEnergyUnitBlock block)) return false;
        var stored = WirelessEnergyUnitBlockEntity.stored(stack);
        if (stored == null) return false;
        var capacity = WirelessEnergyUnitBlockEntity.capacity(stack, block, stored);
        int shift = Math.max(0, capacity.bitLength() - BAR_BITS);
        return ToolChargeBarRenderer.renderElectricBar(guiGraphics, stored.shiftRight(shift).longValue(), capacity.shiftRight(shift).longValue(), xOffset, yOffset, false);
    }
}
