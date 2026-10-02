package com.gtocore.common.item

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

import javax.annotation.ParametersAreNonnullByDefault

@ParametersAreNonnullByDefault
class KineticRotorItem(properties: Properties, durability: Int, min: Int, max: Int, material: Int) : Item(properties.durability(durability)) {
    val MinWind = min
    val MaxWind = max
    val material = material

    override fun appendHoverText(itemstack: ItemStack, world: Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(itemstack, world, list, flag)
        list.add(Component.translatable("gtocore.tooltip.item.kinetic_rotor.min", MinWind))
        list.add(Component.translatable("gtocore.tooltip.item.kinetic_rotor.max", MaxWind))
    }
}
