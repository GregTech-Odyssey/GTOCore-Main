package com.gtocore.common.item

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

import java.util.UUID

class PlanetDataChipItem(properties: Properties) : Item(properties) {
    fun getPlanetDataChip(uuid: UUID, resourceLocation: ResourceLocation): ItemStack = ItemStack(this).apply {
        val tag = getOrCreateTag()
        tag.putUUID("uuid", uuid)
        tag.putString("planet", resourceLocation.toString())
    }

    override fun appendHoverText(itemstack: ItemStack, world: Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(itemstack, world, list, flag)
        val tag = itemstack.getTag() ?: return
        if (!tag.hasUUID("uuid")) return
        list.add(Component.literal("UUID: ").append(tag.getUUID("uuid").toString()))
        list.add(Component.translatable("gtceu.jei.ore_vein_diagram.dimensions").append(tag.getString("planet")))
    }
}
