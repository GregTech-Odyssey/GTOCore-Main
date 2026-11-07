package com.gtocore.api.gui.graphic.impl

import com.gtocore.api.gui.graphic.GTOClientTooltipComponent
import com.gtocore.api.gui.graphic.GTOToolTipComponent
import com.gtocore.api.gui.helper.LineChartHelper

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

import com.gregtechceu.gtceu.utils.FormattingUtil

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

class GTOLineChartToolTipComponent(
    var data: List<BigInteger> = emptyList(), // 存储从服务器同步来的数据
    var lineColor: Int = 0xFF2ECC71.toInt(),
    var drawAreaFill: Boolean = true,
) : GTOToolTipComponent(height = 60, width = 150) { // 默认宽高
    var monitorStyle: Boolean = false
    var sampleIntervalSeconds: Int = 1
    var valueDivisor: Double = 1.0
    var valueUnit: String = ""
    var milliValues: Boolean = false

    internal var minimum: Double = 0.0
        private set
    internal var maximum: Double = 1.0
        private set
    internal val valueLabels = arrayOf("", "", "")
    internal val timeLabels = arrayOf("", "", "")
    private var cachedData: List<BigInteger>? = null
    private var cachedInterval: Int = 0
    private var cachedDivisor: Double = 0.0
    private var cachedMilli: Boolean = false
    private val labelBuilder = StringBuilder(24)

    internal fun updateAxes() {
        if (cachedData === data && cachedInterval == sampleIntervalSeconds && cachedDivisor == valueDivisor && cachedMilli == milliValues) return
        cachedData = data
        cachedInterval = sampleIntervalSeconds
        cachedDivisor = valueDivisor
        cachedMilli = milliValues
        minimum = 0.0
        maximum = valueDivisor
        if (data.isNotEmpty()) {
            minimum = data[0].toDouble()
            maximum = minimum
            for (index in 1 until data.size) {
                val value = data[index].toDouble()
                if (value < minimum) minimum = value
                if (value > maximum) maximum = value
            }
            if (minimum == maximum) {
                if (minimum == 0.0) {
                    maximum = valueDivisor
                } else {
                    val padding = abs(minimum) * 0.05
                    minimum -= padding
                    maximum += padding
                }
            }
        }
        for (index in valueLabels.indices) {
            val value = (maximum - (maximum - minimum) * index / 2) / valueDivisor
            valueLabels[index] = FormattingUtil.formatNumberReadable2F(if (milliValues) value * 1000 else value, milliValues)
        }
        val duration = max(0, data.size - 1) * sampleIntervalSeconds
        val timeDivisor = if (duration >= 3600) {
            3600
        } else if (duration >= 120) {
            60
        } else {
            1
        }
        val timeUnit = if (timeDivisor == 3600) {
            "h"
        } else if (timeDivisor == 60) {
            "m"
        } else {
            "s"
        }
        for (index in timeLabels.indices) {
            labelBuilder.setLength(0)
            val secondsAgo = duration.toDouble() * (2 - index) / 2 / timeDivisor
            if (secondsAgo > 0) labelBuilder.append('-')
            timeLabels[index] = labelBuilder.append(FormattingUtil.DECIMAL_FORMAT_1F.format(secondsAgo)).append(timeUnit).toString()
        }
    }
}

// 2. 客户端渲染器 (类似 GTOProgressClientComponent)
// 它负责调用 LineChartHelper 来执行实际的绘制
class GTOLineChartClientComponent(data: GTOLineChartToolTipComponent) : GTOClientTooltipComponent<GTOLineChartToolTipComponent>(data) {
    override fun renderImage(font: Font, x: Int, y: Int, guiGraphics: GuiGraphics) {
        guiGraphics.pose().pushPose()
        guiGraphics.pose().translate(x.toDouble(), y.toDouble(), 0.0)

        if (data.monitorStyle) {
            renderMonitorChart(font, guiGraphics)
            guiGraphics.pose().popPose()
            return
        }

        LineChartHelper.drawLineChart(
            graphics = guiGraphics,
            data = data.data, // 将 BigInteger 列表传递给绘制函数
            totalWidth = data.width,
            totalHeight = data.height,
            lineColor = data.lineColor,
            drawAreaFill = data.drawAreaFill,
            // 这里可以添加更多从 data 对象中读取的自定义参数
        )

        guiGraphics.pose().popPose()
    }

    private fun renderMonitorChart(font: Font, graphics: GuiGraphics) {
        data.updateAxes()
        val textScale = 0.75f
        val textHeight = ceil(font.lineHeight * textScale).toInt()
        var labelWidth = 0
        for (label in data.valueLabels) labelWidth = max(labelWidth, ceil(font.width(label) * textScale).toInt())
        val left = labelWidth + 5
        val top = textHeight + 2
        val width = data.width - left - 2
        val height = data.height - top - textHeight - 5
        if (width <= 2 || height <= 2) return

        graphics.pose().pushPose()
        graphics.pose().translate(left.toDouble(), top.toDouble(), 0.0)
        LineChartHelper.drawLineChart(
            graphics = graphics,
            data = data.data,
            totalWidth = width,
            totalHeight = height,
            lineColor = data.lineColor,
            drawAreaFill = false,
            backgroundColor = 0x00000000,
            borderColor = 0xFF64758A.toInt(),
            drawDataPoints = data.data.size == 1,
            autoReboundY = false,
            minYBound = data.minimum,
            maxYBound = data.maximum,
        )
        graphics.pose().popPose()

        val axisColor = 0xFF94A3B8.toInt()
        for (index in data.valueLabels.indices) {
            val tickY = top + 1 + (height - 2) * index / 2
            graphics.hLine(left - 3, left, tickY, axisColor)
            drawLabel(graphics, font, data.valueLabels[index], left - 5 - font.width(data.valueLabels[index]) * textScale, tickY - textHeight / 2f, textScale)
        }
        for (index in data.timeLabels.indices) {
            if (data.data.size <= 1 && index != 1) continue
            val tickX = left + 1 + (width - 2) * index / 2
            graphics.vLine(tickX, top + height - 1, top + height + 2, axisColor)
            val label = data.timeLabels[index]
            val labelX = (tickX - font.width(label) * textScale / 2).coerceIn(left.toFloat(), max(left.toFloat(), data.width - font.width(label) * textScale))
            drawLabel(graphics, font, label, labelX, (top + height + 3).toFloat(), textScale)
        }
        drawLabel(graphics, font, data.valueUnit, left.toFloat(), 0f, textScale)
    }

    private fun drawLabel(graphics: GuiGraphics, font: Font, label: String, x: Float, y: Float, scale: Float) {
        graphics.pose().pushPose()
        graphics.pose().translate(x.toDouble(), y.toDouble(), 0.0)
        graphics.pose().scale(scale, scale, 1f)
        graphics.drawString(font, label, 0, 0, 0xFFCBD5E1.toInt(), false)
        graphics.pose().popPose()
    }
}
