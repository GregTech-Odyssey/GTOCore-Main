package com.gtocore.common.item

import net.minecraft.client.color.item.ItemColor
import net.minecraft.world.item.Item
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import com.gregtechceu.gtceu.api.item.ComponentItem

open class ColoringItems protected constructor(properties: Item.Properties, private val itemColor: Int, private val tintLayer: Int) : ComponentItem(properties) {
    companion object {
        @JvmStatic
        fun create(properties: Item.Properties, color: Int, tintLayer: Int): ColoringItems = ColoringItems(properties, color, tintLayer)

        @JvmStatic
        @OnlyIn(Dist.CLIENT)
        fun color(): ItemColor = ItemColor { itemStack, index ->
            val item = itemStack.getItem() as? ColoringItems
            if (item != null && index == item.tintLayer) item.itemColor else -1
        }
    }
}
