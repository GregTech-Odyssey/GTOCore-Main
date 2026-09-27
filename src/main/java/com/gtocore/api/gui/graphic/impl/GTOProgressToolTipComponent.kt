package com.gtocore.api.gui.graphic.impl

import com.gtocore.api.gui.graphic.GTOClientTooltipComponent
import com.gtocore.api.gui.graphic.GTOToolTipComponent

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

import com.gregtechceu.gtceu.uipro.elements.ProgressBar
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme

class GTOProgressToolTipComponent(var percentage: Float, var label: String = "", var value: String = "", var color: Int = DEFAULT_COLOR) : GTOToolTipComponent(height = ProgressBar.HEIGHT, width = 150) {
    companion object {
        @JvmField
        val DEFAULT_COLOR = 0xFF2ECC71.toInt()
    }
}

class GTOProgressClientComponent(data: GTOProgressToolTipComponent) : GTOClientTooltipComponent<GTOProgressToolTipComponent>(data) {
    override fun renderImage(font: Font, x: Int, y: Int, guiGraphics: GuiGraphics) {
        drawLayered(guiGraphics, x, y, data.width, data.height, data.label, data.value) { from ->
            ProgressBar.drawFill(guiGraphics, x, y, data.width, data.height, from, data.percentage, data.color)
        }
    }
}

class GTOMultiProgressToolTipComponent(val segments: List<Pair<Float, Int>>, var label: String = "", var value: String = "") : GTOToolTipComponent(height = ProgressBar.HEIGHT, width = 150)
class GTOMultiProgressClientComponent(data: GTOMultiProgressToolTipComponent) : GTOClientTooltipComponent<GTOMultiProgressToolTipComponent>(data) {
    override fun renderImage(font: Font, x: Int, y: Int, guiGraphics: GuiGraphics) {
        drawLayered(guiGraphics, x, y, data.width, data.height, data.label, data.value) { start ->
            var from = start
            for ((ratio, color) in data.segments) from = ProgressBar.drawFill(guiGraphics, x, y, data.width, data.height, from, ratio, color)
            from
        }
    }
}

private inline fun drawLayered(graphics: GuiGraphics, x: Int, y: Int, w: Int, h: Int, label: String, value: String, fill: (Int) -> Int) {
    val pose = graphics.pose()
    ProgressBar.drawTrack(graphics, x, y, w, h)
    pose.pushPose()
    pose.translate(0.0, 0.0, 1.0)
    fill(0)
    pose.translate(0.0, 0.0, 1.0)
    ProgressBar.drawText(graphics, x, y, w, h, label, value, UITheme.TEXT)
    pose.popPose()
}

class GTOComponentTooltipComponent(val component: Component) : GTOToolTipComponent(priority = 0)
infix fun Int.toPercentageWith(other: Int): Float = (this.toDouble() / other.toDouble()).toFloat()
infix fun Long.toPercentageWith(other: Long): Float = (this.toDouble() / other.toDouble()).toFloat()
