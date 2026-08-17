package com.gtocore.common.machine.multiblock.part.ae

import com.gtocore.api.gui.ktflexible.multiPageAdvanced
import com.gtocore.api.gui.ktflexible.textBlock
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineKt.Companion.CIRCUIT_SPECIAL
import com.gtocore.common.machine.multiblock.part.ae.MEPatternBufferPartMachineKt.Companion.FLUID_SPECIAL
import com.gtocore.eio_travel.logic.TravelUtils

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget
import com.gregtechceu.gtceu.api.gui.widget.TankWidget
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler
import com.gtolib.api.gui.ktflexible.VBoxBuilder
import com.gtolib.api.gui.ktflexible.blank
import com.gtolib.api.gui.ktflexible.field
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper
import com.lowdragmc.lowdraglib.gui.widget.Widget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.jei.IngredientIO
import net.minecraftforge.fluids.capability.IFluidHandler

fun buildHeader(container: VBoxBuilder, machine: MEPatternPartMachineKt<*>) {
    val width = container.availableWidth
    with(container) {
        with(machine) {
            hBox(height = 12, alwaysVerticalCenter = true) {
                blank(width = 7)
                textBlock(maxWidth = width, textSupplier = {
                    when (onlineField) {
                        true -> Component.translatable("gtceu.gui.me_network.online")
                        false -> Component.translatable("gtceu.gui.me_network.offline")
                    }
                })
                blank(width = 9)
                textBlock(maxWidth = width, textSupplier = {
                    Component.translatable(MEPatternPartMachineKt.AE_NAME)
                })
                field(height = 12, getter = { customName }, setter = {
                    customName = it
                    TravelUtils.requireResync(level!!)
                })
            }
        }
    }
}

fun createPatternPageWidget(container: VBoxBuilder, machine: MEPatternPartMachineKt<*>, pageHeight: Int, buildToolBoxContent: VBoxBuilder.() -> Unit, emptyPageTextSupplier: (() -> Component)? = null, prioritizeToolbox: Boolean = true) = with(container) {
    with(machine) {
        val width = container.availableWidth
        val chunked: List<List<List<Int>>> = (0 until maxPatternCount).chunked(9).chunked(6)
        multiPageAdvanced(width = width, runOnUpdate = ::runOnUpdate, height = pageHeight, pageSelector = newPageField) {
            chunked.forEach { pageIndices ->
                page {
                    vScroll(width = width, height = pageHeight) {
                        vBox(width = width, alwaysHorizonCenter = true) {
                            if (prioritizeToolbox) {
                                buildToolBoxContent()
                            }
                            pageIndices.forEach { lineIndices ->
                                hBox(height = 18) {
                                    lineIndices.forEach { index ->
                                        widget(createPatternSlot(index))
                                    }
                                }
                            }
                            if (!prioritizeToolbox) {
                                buildToolBoxContent()
                            }
                        }
                    }
                }
            }
            if (chunked.isEmpty()) {
                emptyPageTextSupplier?.let { supplier ->
                    page {
                        textBlock(maxWidth = this.availableWidth, textSupplier = supplier)
                    }
                }
            }
        }
    }
}

fun buildFluidSection(container: VBoxBuilder, width: Int, fluidHandler: Array<out IFluidHandler>, slotDecorator: ((Int, Widget) -> Widget)? = null) {
    with(container) {
        textBlock(maxWidth = width, textSupplier = { Component.translatable(FLUID_SPECIAL) })
        fluidHandler.indices.chunked(9).forEach { indices ->
            hBox(height = 18) {
                indices.forEach { index ->
                    val tankWidget = TankWidget(
                            fluidHandler[index],
                            0,
                            0,
                            18,
                            18,
                            true,
                            true,
                        ).setBackground(GuiTextures.FLUID_SLOT)
                    widget(slotDecorator?.invoke(index, tankWidget) ?: tankWidget)
                }
            }
        }
    }
}

fun missingVirtualInputOverlay(child: Widget, missingSupplier: () -> Boolean): Widget =
    MissingVirtualInputOverlay(child, missingSupplier)

private class MissingVirtualInputOverlay(
    child: Widget,
    private val missingSupplier: () -> Boolean,
) : WidgetGroup(0, 0, 18, 18) {

    private companion object {
        const val MISSING_UPDATE_ID = 1
        const val MISSING_FILL = 0x66FF0000
        const val MISSING_BORDER = -0x10000
    }

    private var missing = false

    init {
        addWidget(child)
    }

    override fun writeInitialData(buffer: FriendlyByteBuf?) {
        super.writeInitialData(buffer)
        if (!isClientSideWidget) {
            missing = missingSupplier()
            buffer?.writeBoolean(missing)
        }
    }

    override fun readInitialData(buffer: FriendlyByteBuf?) {
        super.readInitialData(buffer)
        if (isClientSideWidget && buffer != null) {
            missing = buffer.readBoolean()
        }
    }

    override fun detectAndSendChanges() {
        super.detectAndSendChanges()
        if (isClientSideWidget) return
        val current = missingSupplier()
        if (current != missing) {
            missing = current
            writeUpdateInfo(MISSING_UPDATE_ID) { it.writeBoolean(current) }
        }
    }

    override fun readUpdateInfo(id: Int, buffer: FriendlyByteBuf) {
        if (id == MISSING_UPDATE_ID) {
            missing = buffer.readBoolean()
        } else {
            super.readUpdateInfo(id, buffer)
        }
    }

    override fun drawInBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks)
        if (!missing) return
        DrawerHelper.drawSolidRect(graphics, positionX, positionY, sizeWidth, sizeHeight, MISSING_FILL)
        DrawerHelper.drawSolidRect(graphics, positionX, positionY, sizeWidth, 1, MISSING_BORDER)
        DrawerHelper.drawSolidRect(graphics, positionX, positionY + sizeHeight - 1, sizeWidth, 1, MISSING_BORDER)
        DrawerHelper.drawSolidRect(graphics, positionX, positionY, 1, sizeHeight, MISSING_BORDER)
        DrawerHelper.drawSolidRect(graphics, positionX + sizeWidth - 1, positionY, 1, sizeHeight, MISSING_BORDER)
    }
}

fun buildCircuitSection(container: VBoxBuilder, width: Int, circuitSlot: Widget, getter: () -> String, setter: (String) -> Unit) {
    with(container) {
        textBlock(maxWidth = width, textSupplier = { Component.translatable(CIRCUIT_SPECIAL) })
        hBox(height = 18, style = { spacing = 4 }) {
            widget(circuitSlot)
            field(
                height = 18,
                getter = getter,
                setter = setter,
            )
        }
    }
}

fun createReadOnlyCircuitSlot(circuitHandler: CustomItemStackHandler): SlotWidget = SlotWidget(circuitHandler, 0, 0, 0, false, false).apply {
    setBackgroundTexture(GuiTextures.SLOT)
    setIngredientIO(IngredientIO.RENDER_ONLY)
}

fun buildSectionDivider(container: VBoxBuilder) {
    with(container) {
        blank(height = 4)
        widget(object : Widget(0, 0, availableWidth, 3) {
            override fun drawInBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
                super.drawInBackground(graphics, mouseX, mouseY, partialTicks)
                DrawerHelper.drawSolidRect(graphics, positionX, positionY, sizeWidth, sizeHeight, 0xFFFFFFFF.toInt())
            }
        })
        blank(height = 4)
    }
}
