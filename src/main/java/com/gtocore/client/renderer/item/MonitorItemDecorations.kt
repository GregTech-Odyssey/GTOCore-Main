package com.gtocore.client.renderer.item

import com.gtocore.common.machine.monitor.MonitorBlockItem

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraftforge.client.IItemDecorator

open class MonitorItemDecorations : IItemDecorator {
    companion object {
        @JvmField
        val DECORATOR = MonitorItemDecorations()
    }

    override fun render(guiGraphics: GuiGraphics, font: Font?, itemStack: ItemStack, xOffset: Int, yOffset: Int): Boolean {
        val blockItem = itemStack.getItem() as? MonitorBlockItem ?: return false
        val rl = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock())
        val textureRL = MonitorBlockItem.getTexturePath(rl)
        if (!Minecraft.getInstance().getResourceManager().getResource(textureRL).isPresent) {
            return false // Texture does not exist, do not render
        }
        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.translate(0f, 0f, 199f)
        pose.scale(0.5f, 0.5f, 1f)
        guiGraphics.blit(textureRL, 2 * xOffset, 2 * yOffset, 0f, 0f, 16, 16, 16, 16)
        pose.popPose()
        return true
    }
}
