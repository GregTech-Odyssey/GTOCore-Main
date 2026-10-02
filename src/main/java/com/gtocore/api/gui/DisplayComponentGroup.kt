package com.gtocore.api.gui

import com.gtocore.common.machine.monitor.DisplayRegistry

import net.minecraft.ChatFormatting
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair

import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

@JvmSuppressWildcards
open class DisplayComponentGroup(private val originList: List<ResourceLocation>, currentWithState: List<ObjectBooleanPair<ResourceLocation>>, private val orderedCallback: Consumer<List<ObjectBooleanPair<ResourceLocation>>>?, position: Position, size: Size) : WidgetGroup(position, size) {
    companion object {
        private const val PACKET_ID = 1
    }

    // 客户端与服务端都使用此列表。
    private val current = ArrayList<ObjectBooleanPair<ResourceLocation>>()
    private val displayWidgets = O2OOpenCacheHashMap<ResourceLocation, DisplayComponentWidget>()
    private var scrollArea: DraggableScrollableWidgetGroup? = null

    init {
        current.addAll(currentWithState)
        // 版本更新后新增的组件作为禁用项追加到已保存列表末尾。
        val loadedRLs = current.stream().map(Function { it.left() }).toList()
        current.addAll(
            originList.stream()
                .filter(Predicate { rl -> loadedRLs.stream().noneMatch(rl::equals) })
                .map(Function { ObjectBooleanPair.of(it, false) })
                .toList(),
        )
        setBackground(GuiTextures.BACKGROUND_INVERSE)
        addWidget(
            ButtonWidget(getSizeWidth() - 16, getSizeHeight() - 12, 10, 10) {
                if (!isRemote) reset()
            }.setButtonTexture(GuiTextures.BUTTON, GuiTextures.BUTTON_VOID.copy().scale(0.8f))
                .setHoverTooltips(Component.translatable("gtocore.machine.monitor.adjust_component.reset")),
        )
        init()
    }

    open fun init() {
        val previousArea = scrollArea
        val lastScrollY = previousArea?.getScrollYOffset() ?: 0
        val firstInit = previousArea == null
        val area = if (previousArea == null) {
            DraggableScrollableWidgetGroup(4, 4, getSizeWidth() - 8, getSizeHeight() - 8).apply {
                scrollArea = this
                this@DisplayComponentGroup.addWidget(this)
                setScrollable(true)
                setYBarStyle(GuiTextures.BACKGROUND, GuiTextures.BOX_OVERLAY)
            }
        } else {
            previousArea.apply { clearAllWidgets() }
        }
        var y = 2
        for (entry in current) {
            val displayWidget = if (firstInit) {
                DisplayComponentWidget(entry.left(), entry.rightBoolean())
            } else {
                displayWidgets[entry.left()]!!.setEnabled(entry.rightBoolean())
            }
            displayWidget.setSelfPosition(5, y)
            displayWidget.setSize(155, 20)
            area.acceptWidget(displayWidget)
            displayWidgets.putIfAbsent(entry.left(), displayWidget)
            y += 10
        }
        if (!firstInit) area.setScrollYOffset(lastScrollY)
    }

    override fun readUpdateInfo(id: Int, buffer: FriendlyByteBuf) {
        if (id != PACKET_ID) {
            super.readUpdateInfo(id, buffer)
            return
        }
        current.clear()
        val count = buffer.readVarInt()
        for (i in 0 until count) {
            val rl = buffer.readResourceLocation()
            val enabled = buffer.readBoolean()
            current.add(ObjectBooleanPair.of(rl, enabled))
        }
        init()
    }

    // 仅在服务端调用。
    private fun reset() {
        current.clear()
        current.addAll(originList.stream().map(Function { ObjectBooleanPair.of(it, true) }).toList())
        init()
        writeUpdateInfo(PACKET_ID, this::writeCurrent)
    }

    // 仅在服务端调用。
    private fun updateOrdered() {
        val visuallyOrderedState = scrollArea!!.widgets.stream()
            .filter(Predicate { it is DisplayComponentWidget })
            .map(Function { it as DisplayComponentWidget })
            .map(Function { ObjectBooleanPair.of(it.getRL(), it.isEnabled()) })
            .toList()
        current.clear()
        current.addAll(visuallyOrderedState)
        orderedCallback?.accept(visuallyOrderedState)
        init()
        writeUpdateInfo(PACKET_ID, this::writeCurrent)
    }

    private fun writeCurrent(buffer: FriendlyByteBuf) {
        buffer.writeVarInt(current.size)
        for (entry in current) {
            buffer.writeResourceLocation(entry.left())
            buffer.writeBoolean(entry.rightBoolean())
        }
    }

    private fun moveWidgetUp(widget: DisplayComponentWidget) {
        val area = scrollArea!!
        val index = area.widgets.indexOf(widget)
        if (index > 0) {
            area.removeWidget(widget)
            area.widgets.add(index - 1, widget)
        }
        updateOrdered()
    }

    private fun moveWidgetDown(widget: DisplayComponentWidget) {
        val area = scrollArea!!
        val index = area.widgets.indexOf(widget)
        if (index < area.widgets.size - 1) {
            area.removeWidget(widget)
            area.widgets.add(index + 1, widget)
        }
        updateOrdered()
    }

    private inner class DisplayComponentWidget(private val componentId: ResourceLocation, enabledInitially: Boolean) : WidgetGroup() {
        private val labelWidget: LabelWidget
        private val switchWidget: SwitchWidget

        init {
            val color = if (enabledInitially) ChatFormatting.GREEN else ChatFormatting.YELLOW
            labelWidget = LabelWidget(0, 0, Component.translatable(DisplayRegistry.langKey(componentId)).withStyle(color))
            addWidget(labelWidget)
            labelWidget.setClientSideWidget()

            switchWidget = SwitchWidget(110, 0, 10, 10, this::onSwitchChanged)
                .setPressed(enabledInitially)
                .setBaseTexture(
                    GuiTextures.BUTTON,
                    GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy().getSubTexture(0.0, 0.0, 1.0, 0.5).scale(0.8f),
                )
                .setPressedTexture(
                    GuiTextures.BUTTON,
                    GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy().getSubTexture(0.0, 0.5, 1.0, 0.5).scale(0.8f),
                )
            addWidget(switchWidget)
            switchWidget.setHoverTooltips(Component.translatable("gtocore.machine.monitor.adjust_component.switch"))

            ButtonWidget(125, 0, 10, 10, this::onMoveUp)
                .setButtonTexture(GuiTextures.BUTTON, GuiTextures.BUTTON_RIGHT.copy().rotate(-45f).scale(0.8f)).apply {
                    this@DisplayComponentWidget.addWidget(this)
                    setHoverTooltips(Component.translatable("gtocore.machine.monitor.adjust_component.move_up"))
                }
            ButtonWidget(140, 0, 10, 10, this::onMoveDown)
                .setButtonTexture(GuiTextures.BUTTON, GuiTextures.BUTTON_LEFT.copy().rotate(-45f).scale(0.8f)).apply {
                    this@DisplayComponentWidget.addWidget(this)
                    setHoverTooltips(Component.translatable("gtocore.machine.monitor.adjust_component.move_down"))
                }
        }

        private fun onSwitchChanged(click: ClickData?, enabled: Boolean?) {
            if (!isRemote) {
                updateOrdered()
            } else {
                labelWidget.setColor((if (enabled!!) ChatFormatting.GREEN else ChatFormatting.YELLOW).color!!)
            }
        }

        private fun onMoveUp(click: ClickData?) {
            if (!isRemote) moveWidgetUp(this)
        }

        private fun onMoveDown(click: ClickData?) {
            if (!isRemote) moveWidgetDown(this)
        }

        fun getRL(): ResourceLocation = componentId

        fun isEnabled(): Boolean = switchWidget.isPressed

        fun setEnabled(enabled: Boolean): DisplayComponentWidget = apply {
            switchWidget.setPressed(enabled)
            if (isRemote) labelWidget.setColor((if (enabled) ChatFormatting.GREEN else ChatFormatting.YELLOW).color!!)
        }
    }
}
