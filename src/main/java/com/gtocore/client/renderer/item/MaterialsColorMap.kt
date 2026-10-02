package com.gtocore.client.renderer.item

import com.gtocore.common.data.GTOMaterials

import net.minecraft.util.Mth

import com.google.common.collect.ImmutableMap
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gtolib.utils.ColorUtils
import vazkii.botania.client.core.handler.ClientTickHandler

import java.util.function.IntSupplier

class MaterialsColorMap {
    companion object {
        @JvmField
        val quantumColor = IntSupplier {
            var spot = ((System.currentTimeMillis() / 500) % 10).toFloat() / 10
            if (spot > 0.5) spot = 1 - spot
            ColorUtils.getInterpolatedColor(0x00FF84, 0xFF7E00, spot * 2)
        }

        private val shimmer = IntSupplier {
            val time = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks
            Mth.hsvToRgb(time % 200 / 200, 0.4f, 0.9f)
        }

        @JvmField
        val MaterialColors: ImmutableMap<Material, IntSupplier> = ImmutableMap.builder<Material, IntSupplier>().apply {
            put(GTOMaterials.Gaia, IntSupplier { Mth.hsvToRgb((ClientTickHandler.ticksInGame shl 1) % 360 / 360f, 0.25f, 1f) })
            put(GTOMaterials.Shimmerwood, shimmer)
            put(GTOMaterials.Shimmerrock, shimmer)
            put(GTOMaterials.BifrostPerm, shimmer)
            put(GTOMaterials.StarStone, IntSupplier { ColorUtils.getInterpolatedColor(0xb5d9ce, 0xFFFFFF, Math.abs(1 - (System.currentTimeMillis() % 10000) / 5000f)) })
            put(GTOMaterials.ChromaticGlass, IntSupplier { getCurrentRainbowColor() })
            put(GTOMaterials.Hypogen, IntSupplier { ColorUtils.getInterpolatedColor(0xFF3D00, 0xDA9100, Math.abs(1 - (System.currentTimeMillis() % 6000) / 3000f)) })
            put(
                GTOMaterials.HexaphaseCopper,
                IntSupplier {
                    val spot = (System.currentTimeMillis() % 4000) / 4000f
                    ColorUtils.getInterpolatedColor(0xEC7916, 0x00FF15, if ((spot > 0.1 && spot < 0.15) || (spot > 0.18 && spot < 0.22)) 1f else 0f)
                },
            )
            put(
                GTOMaterials.PhotonicKristallite,
                IntSupplier {
                    val alpha = (Math.sin((System.currentTimeMillis() / 10f).toDouble()) * 128 + 128).toFloat()
                    ColorUtils.createARGBColor(0xfcfcfd, alpha.toInt())
                },
            )
            put(
                GTOMaterials.Astrium,
                IntSupplier {
                    com.lowdragmc.lowdraglib.utils.ColorUtils.blendColor(
                        0xe1ee595a.toInt(),
                        0xe131bad5.toInt(),
                        (Math.sin(System.currentTimeMillis() * 0.005) * 0.3f + 0.5f).toFloat(),
                    )
                },
            )
            put(GTOMaterials.HeavyQuarkDegenerateMatter, quantumColor)
            put(GTOMaterials.QuantumChromoDynamicallyConfinedMatter, quantumColor)
        }.build()

        @JvmStatic
        fun getCurrentRainbowColor(): Int = HSBToRGB((System.currentTimeMillis() % 18000) / 18000f)

        private fun HSBToRGB(hue: Float): Int {
            var r = 0
            var g = 0
            var b = 0
            val h = (hue - Math.floor(hue.toDouble()).toFloat()) * 6f
            val f = h - Math.floor(h.toDouble()).toFloat()
            val q = 1f - f
            val t = 1f - (1f - f)
            when (h.toInt()) {
                0 -> {
                    r = (255f + 0.5f).toInt()
                    g = (t * 255f + 0.5f).toInt()
                }

                1 -> {
                    r = (q * 255f + 0.5f).toInt()
                    g = (255f + 0.5f).toInt()
                }

                2 -> {
                    g = (255f + 0.5f).toInt()
                    b = (t * 255f + 0.5f).toInt()
                }

                3 -> {
                    g = (q * 255f + 0.5f).toInt()
                    b = (255f + 0.5f).toInt()
                }

                4 -> {
                    r = (t * 255f + 0.5f).toInt()
                    b = (255f + 0.5f).toInt()
                }

                5 -> {
                    r = (255f + 0.5f).toInt()
                    b = (q * 255f + 0.5f).toInt()
                }
            }
            return 0xff000000.toInt() or (r shl 16) or (g shl 8) or b
        }
    }
}
