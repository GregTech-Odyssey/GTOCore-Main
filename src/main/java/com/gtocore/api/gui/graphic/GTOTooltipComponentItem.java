package com.gtocore.api.gui.graphic;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface GTOTooltipComponentItem {

    void attachGTOTooltip(ItemStack itemStack, List<GTOToolTipComponent> tooltips);
}
