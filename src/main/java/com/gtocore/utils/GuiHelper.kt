package com.gtocore.utils

import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

object GuiHelper {
    @JvmStatic
    @OnlyIn(Dist.CLIENT)
    fun getRealMouseX(): Double {
        val mc = Minecraft.getInstance()
        return mc.mouseHandler.xpos() * mc.window.guiScaledWidth / mc.window.screenWidth
    }

    @JvmStatic
    @OnlyIn(Dist.CLIENT)
    fun getRealMouseY(): Double {
        val mc = Minecraft.getInstance()
        return mc.mouseHandler.ypos() * mc.window.guiScaledHeight / mc.window.screenHeight
    }
}
