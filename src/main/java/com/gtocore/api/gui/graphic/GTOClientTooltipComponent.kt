package com.gtocore.api.gui.graphic

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent

abstract class GTOClientTooltipComponent<T : GTOToolTipComponent>(val data: T) : ClientTooltipComponent {
    override fun getHeight(): Int = data.height
    override fun getWidth(p0: Font): Int = data.width
}
