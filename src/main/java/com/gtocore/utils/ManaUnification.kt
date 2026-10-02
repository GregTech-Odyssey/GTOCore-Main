package com.gtocore.utils

object ManaUnification {
    @JvmStatic
    fun manaToSource(mana: Long): Long = mana shr 2

    @JvmStatic
    fun sourceToMana(source: Long): Long = source shl 2
}
