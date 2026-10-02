package com.gtocore.utils

import com.gtocore.api.lang.ComponentListSupplier

import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item

import com.gtolib.api.item.IItem

import java.util.function.Supplier

fun Item.setTooltips(vararg components: Supplier<Component>) {
    // 对调用方数组保留一次防御性复制；新建列表数组则可直接传递。
    ((this as IItem)::`gtolib$setToolTips`)(components.copyOf())
}

fun Item.setTooltips(listSupplier: ComponentListSupplier) {
    val suppliers = listSupplier.list.iterator()
    val components = Array<Supplier<Component>>(listSupplier.list.size) { suppliers.next() }
    ((this as IItem)::`gtolib$setToolTips`)(components)
}
