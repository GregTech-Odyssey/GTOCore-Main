package com.gtocore.api.gui.configurators

import net.minecraft.network.chat.Component

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider
import com.gregtechceu.gtceu.common.data.GTItems
import com.gregtechceu.gtceu.uiwidgets.mode.ModeSelector
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture
import com.lowdragmc.lowdraglib.gui.widget.Widget

abstract class CustomModeFancyConfigurator protected constructor(private val modeSize: Int) : IFancyUIProvider {
    abstract fun setMode(index: Int)

    abstract fun getCurrentMode(): Int

    abstract fun getLanguageKey(index: Int): String

    override fun getTitle(): Component = Component.translatable("gtceu.gui.machinemode.title")

    override fun getTabIcon(): IGuiTexture = ItemStackTexture(GTItems.ROBOT_ARM_LV.get())

    // 当前模式由服务端下发，客户端不在每 tick 回写模式。
    override fun createMainPage(widget: FancyMachineUIWidget?): Widget = ModeSelector.create(modeSize, { Component.translatable(getLanguageKey(it)) }, ::getCurrentMode, ::setMode)

    override fun getTabTooltips(): MutableList<Component> = ArrayList<Component>(1).apply {
        add(Component.translatable("gtceu.gui.machinemode.tab_tooltip"))
    }
}
