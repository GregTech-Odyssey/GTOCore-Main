package com.gtocore.common.item

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory
import com.gregtechceu.gtceu.uipro.LayoutStyle
import com.gregtechceu.gtceu.uipro.UIElement
import com.gregtechceu.gtceu.uipro.elements.PhantomItemSlot
import com.gregtechceu.gtceu.uipro.elements.TextLine
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme
import com.gregtechceu.gtceu.uiwidgets.item.HeldItemPage
import com.gtolib.utils.RLUtils
import com.gtolib.utils.RegistriesUtils
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory
import com.lowdragmc.lowdraglib.gui.modular.ModularUI
import com.lowdragmc.lowdraglib.side.item.IItemTransfer

class OrderItem :
    IItemUIFactory,
    ICustomDescriptionId {
    companion object {
        @JvmField
        val INSTANCE = OrderItem()

        private const val TARGET = "item.gtocore.order.target"
        private const val NO_TARGET = "item.gtocore.order.no_target"

        @JvmStatic
        fun setTarget(stack: ItemStack, target: ItemStack): ItemStack {
            val tag = stack.getOrCreateTag()
            val id = BuiltInRegistries.ITEM.getKey(target.getItem())
            tag.putString("marker_id", id.toString())
            if (target.hasTag()) {
                tag.put("marker_nbt", target.getTag()!!.copy())
            } else {
                tag.remove("marker_nbt")
            }
            return stack
        }

        @JvmStatic
        fun getTarget(stack: ItemStack): ItemStack {
            val tag = stack.getOrCreateTag()
            val id = tag.getString("marker_id")
            if (id.isEmpty()) return ItemStack.EMPTY
            val nbt: CompoundTag? = tag.getCompound("marker_nbt")
            return ItemStack(RegistriesUtils.getItem(RLUtils.parse(id))).apply {
                nbt?.let { setTag(it.copy()) }
            }
        }

        @JvmStatic
        fun clearTarget(stack: ItemStack): ItemStack {
            if (!stack.hasTag()) return stack
            val tag = stack.getOrCreateTag()
            tag.remove("marker_id")
            tag.remove("marker_nbt")
            return stack
        }
    }

    override fun createUI(holder: HeldItemUIFactory.HeldItemHolder, player: Player): ModularUI = HeldItemPage.create(holder, player) { _ ->
        val name = TextLine.of(0) {
            val target = getTarget(holder.getHeld())
            if (target.isEmpty) Component.translatable(NO_TARGET) else target.hoverName
        }.bindClientColor(UITheme::panelText)
        name.layout { it.flex(1f) }
        val slot = PhantomItemSlot.of(TargetSlot(holder), 0).xeiPhantom().apply {
            tooltips(TARGET)
        }
        val row = UIElement.row(UISizes.SLOT_SIZE).layout { it.gapAll(UISizes.SECTION_GAP.toFloat()).alignCenter() }.addChildren(slot, name)
        UIElement.section(LayoutStyle.AUTO).layout { it.minWidth(UISizes.CONTENT_WIDTH.toFloat()) }.addChild(row)
    }

    override fun getItemName(stack: ItemStack): Component {
        val name = if (stack.hasTag()) getTarget(stack).hoverName else Component.empty()
        return Component.translatable(stack.descriptionId, name)
    }

    private class TargetSlot(private val holder: HeldItemUIFactory.HeldItemHolder) : IItemTransfer {
        private var clientStack = ItemStack.EMPTY

        override fun getSlots(): Int = 1

        override fun getStackInSlot(slot: Int): ItemStack = if (holder.isRemote) clientStack else getTarget(holder.getHeld())

        override fun setStackInSlot(index: Int, stack: ItemStack) {
            if (holder.isRemote) {
                clientStack = stack
                return
            }
            if (stack.isEmpty) {
                clearTarget(holder.getHeld())
            } else if (isItemValid(index, stack)) {
                setTarget(holder.getHeld(), stack)
            }
        }

        override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean, notifyChanges: Boolean): ItemStack {
            if (stack.isEmpty || !isItemValid(slot, stack)) return stack
            if (!simulate) setStackInSlot(slot, stack)
            return stack.copyWithCount(stack.getCount() - 1)
        }

        override fun extractItem(slot: Int, amount: Int, simulate: Boolean, notifyChanges: Boolean): ItemStack {
            val target = getTarget(holder.getHeld())
            if (!simulate && !target.isEmpty) setStackInSlot(slot, ItemStack.EMPTY)
            return target
        }

        override fun getSlotLimit(slot: Int): Int = 1

        override fun isItemValid(slot: Int, stack: ItemStack): Boolean = stack.getItem() !== holder.getHeld().getItem()

        override fun createSnapshot(): Any = getTarget(holder.getHeld())

        override fun restoreFromSnapshot(snapshot: Any?) {
            if (snapshot is ItemStack) setStackInSlot(0, snapshot)
        }
    }
}
