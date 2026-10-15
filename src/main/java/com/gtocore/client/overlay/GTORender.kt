package com.gtocore.client.overlay

import com.gtocore.client.forge.GTORenderManager

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.common.MinecraftForge

@OnlyIn(Dist.CLIENT)
object GTORender {

    @JvmStatic
    fun init() {
        MinecraftForge.EVENT_BUS.addListener(GTORender::onRender)
    }

    @JvmStatic
    fun onRender(event: RenderLevelStageEvent) {
        val it = GTORenderManager.tasks.iterator()
        while (it.hasNext()) {
            val renderType = it.next()
            if (renderType.renderData.willBeDelete) it.remove()
            if (renderType.renderData.willBeCalled) renderType.render(event)
        }
    }
}
