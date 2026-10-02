package com.gtocore.api.gui

import com.gtocore.common.machine.monitor.DisplayRegistry

import net.minecraft.ChatFormatting
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

import com.gregtechceu.gtceu.api.gui.GuiTextures
import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture
import com.lowdragmc.lowdraglib.gui.util.ClickData
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget
import com.lowdragmc.lowdraglib.gui.widget.SwitchWidget
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup
import com.lowdragmc.lowdraglib.utils.Position
import com.lowdragmc.lowdraglib.utils.Size
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair

import java.util.Collections
import java.util.function.Consumer

@JvmSuppressWildcards
open class DisplayComponentGroup(
    private val originList: List<ResourceLocation>,
    currentWithState: List<ObjectBooleanPair<ResourceLocation>>,
    private val orderedCallback: Consumer<List<ObjectBooleanPair<ResourceLocation>>>?,
    position: Position,
    size: Size
) : WidgetGroup(position, size) {
    companion object {
        private const val PACKET_ID = 1
    }

    // 客户端与服务端都使用此列表。
    private val current = ArrayList<ObjectBooleanPair<ResourceLocation>>()
    private val displayWidgets = O2OOpenCacheHashMap<ResourceLocation, DisplayComponentWidget>()
    private lateinit var scrollArea: DraggableScrollableWidgetGroup

    init {
        current.addAll(currentWithState)
        appendMissingComponents()
        setBackground(GuiTextures.BACKGROUND_INVERSE)
        addWidget(
            ButtonWidget(getSizeWidth() - 16, getSizeHeight() - 12, 10, 10) {
                if (!isRemote) reset()
            }.setButtonTexture(GuiTextures.BUTTON, GuiTextures.BUTTON_VOID.copy().scale(0.8f))
                .setHoverTooltips(Component.translatable("gtocore.machine.monitor.adjust_component.reset"))
        )
        init()
    }

    open fun init() {
        val firstInit = !::scrollArea.isInitialized
        val lastScrollY = if (firstInit) 0 else scrollArea.getScrollYOffset()
        if (firstInit) {
            createScrollArea()
        } else {
            scrollArea.clearAllWidgets()
        }
        updateComponentWidgets(firstInit)
        if (!firstInit) scrollArea.setScrollYOffset(lastScrollY)
    }

    private fun appendMissingComponents() {
        // 只对照原有状态，保持新增组件（包括重复 ID）的原始顺序。
        val savedCount = current.size
        for (id in originList) {
            var alreadySaved = false
            for (i in 0 until savedCount) {
                if (id == current[i].left()) {
                    alreadySaved = true
                    break
                }
            }
            if (!alreadySaved) current.add(ObjectBooleanPair.of(id, false))
        }
    }

    private fun createScrollArea() {
        scrollArea = DraggableScrollableWidgetGroup(4, 4, getSizeWidth() - 8, getSizeHeight() - 8)
        addWidget(scrollArea)
        scrollArea.setScrollable(true)
        scrollArea.setYBarStyle(GuiTextures.BACKGROUND, GuiTextures.BOX_OVERLAY)
    }

    private fun updateComponentWidgets(firstInit: Boolean) {
        var y = 2
        for (entry in current) {
            val displayWidget = if (firstInit) {
                DisplayComponentWidget(entry.left(), entry.rightBoolean())
            } else {
                displayWidgets[entry.left()]!!.setEnabled(entry.rightBoolean())
            }
            displayWidget.setSelfPosition(5, y)
            displayWidget.setSize(155, 20)
            scrollArea.acceptWidget(displayWidget)
            displayWidgets.putIfAbsent(entry.left(), displayWidget)
            y += 10
        }
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
        for (id in originList) {
            current.add(ObjectBooleanPair.of(id, true))
        }
        init()
        writeUpdateInfo(PACKET_ID, this::writeCurrent)
    }

    // 仅在服务端调用。
    private fun updateOrdered() {
        val visuallyOrderedState = collectOrderedState()
        current.clear()
        current.addAll(visuallyOrderedState)
        orderedCallback?.accept(visuallyOrderedState)
        init()
        writeUpdateInfo(PACKET_ID, this::writeCurrent)
    }

    private fun collectOrderedState(): List<ObjectBooleanPair<ResourceLocation>> {
        val ordered = ArrayList<ObjectBooleanPair<ResourceLocation>>(scrollArea.widgets.size)
        for (widget in scrollArea.widgets) {
            if (widget is DisplayComponentWidget) {
                ordered.add(ObjectBooleanPair.of(widget.getRL(), widget.isEnabled()))
            }
        }
        // 回调得到独立、不可修改的快照，后续刷新 current 不会改变它。
        return Collections.unmodifiableList(ordered)
    }

    private fun writeCurrent(buffer: FriendlyByteBuf) {
        buffer.writeVarInt(current.size)
        for (entry in current) {
            buffer.writeResourceLocation(entry.left())
            buffer.writeBoolean(entry.rightBoolean())
        }
    }

    private fun moveWidgetUp(widget: DisplayComponentWidget) {
        val area = scrollArea
        val index = area.widgets.indexOf(widget)
        if (index > 0) {
            area.removeWidget(widget)
            area.widgets.add(index - 1, widget)
        }
        updateOrdered()
    }

    private fun moveWidgetDown(widget: DisplayComponentWidget) {
        val area = scrollArea
        val index = area.widgets.indexOf(widget)
        if (index < area.widgets.size - 1) {
            area.removeWidget(widget)
            area.widgets.add(index + 1, widget)
        }
        updateOrdered()
    }

    private inner class DisplayComponentWidget(
        private val componentId: ResourceLocation,
        enabledInitially: Boolean
    ) : WidgetGroup() {
        private val labelWidget: LabelWidget
        private val switchWidget: SwitchWidget

        init {
            val color = if (enabledInitially) ChatFormatting.GREEN else ChatFormatting.YELLOW
            labelWidget = LabelWidget(
                0, 0, Component.translatable(DisplayRegistry.langKey(componentId)).withStyle(color)
            )
            addWidget(labelWidget)
            labelWidget.setClientSideWidget()

            switchWidget = SwitchWidget(110, 0, 10, 10, this::onSwitchChanged)
                .setPressed(enabledInitially)
                .setBaseTexture(GuiTextures.BUTTON, switchTexture(false))
                .setPressedTexture(GuiTextures.BUTTON, switchTexture(true))
            addWidget(switchWidget)
            switchWidget.setHoverTooltips(Component.translatable("gtocore.machine.monitor.adjust_component.switch"))

            addMoveButton(
                125, GuiTextures.BUTTON_RIGHT, "gtocore.machine.monitor.adjust_component.move_up", this::onMoveUp
            )
            addMoveButton(
                140, GuiTextures.BUTTON_LEFT, "gtocore.machine.monitor.adjust_component.move_down", this::onMoveDown
            )
        }

        private fun switchTexture(pressed: Boolean): TransformTexture =
            GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy()
                .getSubTexture(0.0, if (pressed) 0.5 else 0.0, 1.0, 0.5)
                .scale(0.8f)

        private fun addMoveButton(x: Int, icon: ResourceTexture, tooltip: String, onClick: Consumer<ClickData>) {
            val button = ButtonWidget(x, 0, 10, 10, onClick)
                .setButtonTexture(GuiTextures.BUTTON, icon.copy().rotate(-45f).scale(0.8f))
            addWidget(button)
            button.setHoverTooltips(Component.translatable(tooltip))
        }

        private fun updateLabelColor(enabled: Boolean) {
            val color = if (enabled) ChatFormatting.GREEN else ChatFormatting.YELLOW
            labelWidget.setColor(color.color!!)
        }

        private fun onSwitchChanged(click: ClickData?, enabled: Boolean?) {
            if (!isRemote) {
                updateOrdered()
            } else {
                updateLabelColor(enabled!!)
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
            if (isRemote) updateLabelColor(enabled)
        }
    }
}
