package com.gtocore.common.item

import com.gregtechceu.gtceu.api.item.ComponentItem
import net.minecraft.client.color.item.ItemColor
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

open class ColoringItems protected constructor(
    properties: Properties,
    private val itemColor: Int,
    private val tintLayer: Int
) : ComponentItem(properties) {
    companion object {
        @JvmStatic
        fun create(properties: Properties, itemColor: Int, tintLayer: Int): ColoringItems =
            ColoringItems(properties, itemColor, tintLayer)

        @JvmStatic
        @OnlyIn(Dist.CLIENT)
        fun color(): ItemColor = ItemColor { itemStack, index ->
            val item = itemStack.getItem() as? ColoringItems
            if (item != null && index == item.tintLayer) item.itemColor else -1
        }
    }
}
