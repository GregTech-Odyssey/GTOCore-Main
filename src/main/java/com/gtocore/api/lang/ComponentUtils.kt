@file:Suppress("unused")

package com.gtocore.api.lang

import com.gtocore.common.data.translation.ComponentSlang

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

import com.gregtechceu.gtceu.client.util.TooltipHelper
import com.gtolib.api.annotation.NewDataAttributes
import com.gtolib.api.annotation.component_builder.TranslationKeyProvider
import com.gtolib.utils.StringUtils
import dev.shadowsoffire.placebo.color.GradientColor

import java.util.function.Supplier

class ComponentListSupplier(
    var list: MutableList<ComponentSupplier> = mutableListOf()
) : Supplier<@JvmSuppressWildcards List<Component>> {
    var translationPrefix: String = ""
        private set
    var line: Int = 0

    override fun get(): List<Component> {
        val result = ArrayList<Component>(list.size)
        for (supplier in list) {
            result.add(supplier.get())
        }
        return result
    }

    fun getSupplier(): Supplier<List<Component>> = this

    fun getArray(): Array<Component> {
        val suppliers = list.iterator()
        return Array(list.size) { suppliers.next().get() }
    }

    fun add(component: ComponentSupplier, style: ComponentSupplier.() -> ComponentSupplier = { this }) {
        list.add(style(component))
        line += 1
    }

    fun add(other: ComponentListSupplier) {
        // 批量拼接不推进翻译行号；现有翻译键依赖此规则。
        list.addAll(other.list)
    }

    fun add(other: ComponentListSupplier, style: ComponentSupplier.() -> ComponentSupplier = { this }) {
        for (supplier in other.list) {
            add(supplier, style)
        }
    }

    fun addTranslatable(key: String, vararg args: Any?, style: ComponentSupplier.() -> ComponentSupplier = { this }) {
        add((::translatable)(key, args), style)
    }

    fun apply(tooltips: MutableList<Component>) {
        tooltips.addAll(get())
    }

    // 翻译键与行号
    fun setTranslationPrefix(prefix: String) {
        this.translationPrefix = prefix
    }

    infix fun String.translatedTo(other: String): ComponentSupplier {
        if (this@translatedTo == other) return this.toLiteralSupplier()
        val translationKey = translationKey(this@translatedTo, other)
        return Component.translatable(translationKey).toComponentSupplier()
    }

    infix fun String.multiTranslatedToGray(other: String) = ComponentListSupplier {
        val cns = this@multiTranslatedToGray.lines()
        val ens = other.lines()
        require(cns.size == ens.size) { "翻译错误: 中文和英文数量不一致,于 $translationPrefix - $line 行" }
        for (i in cns.indices) {
            add(cns[i].translatedTo(ens[i])) { gray() }
        }
    }

    fun String.translatedWithArgs(en: String, vararg args: Any?): ComponentSupplier {
        if (this@translatedWithArgs == en) return this.toLiteralSupplier()
        val translationKey = translationKey(this@translatedWithArgs, en)
        return translatableWithArguments(translationKey, args)
    }

    private fun translationKey(cn: String, en: String): String {
        val prefix = if (translationPrefix.isNotEmpty()) {
            "${NewDataAttributes.PREFIX}.$translationPrefix.$line"
        } else {
            "${NewDataAttributes.PREFIX}.$line"
        }
        return TranslationKeyProvider.getTranslationKey(cn, en, prefix)
    }

    // 编辑标记
    fun editionByGTONormal(): ComponentListSupplier = this.apply {
        add(ComponentSlang.GTOSignal_Edition_ByGTONormal)
    }
    fun editionByGTORemix(): ComponentListSupplier = this.apply {
        add(ComponentSlang.GTOSignal_Edition_ByGTORemix)
    }
}

fun ComponentListSupplier(op: ComponentListSupplier.() -> Unit): ComponentListSupplier {
    val supplier = ComponentListSupplier()
    supplier.op()
    supplier.initialize()
    return supplier
}

class ComponentSupplier(
    var component: Component,
    private val delayed: MutableList<(MutableComponent) -> Unit> = mutableListOf(),
    private val transform: MutableList<(ComponentSupplier) -> ComponentSupplier> = mutableListOf()
) : Supplier<Component> {
    override fun get(): MutableComponent {
        var supplier = this
        // 变换先执行；延迟样式应用到变换结果的副本，不修改模板组件。
        for (transformation in transform) {
            supplier = transformation(supplier)
        }
        val result = supplier.component.copy()
        for (operation in supplier.delayed) {
            operation(result)
        }
        return result
    }

    fun apply(tooltips: MutableList<Component>) {
        tooltips.add(get())
    }

    operator fun plus(other: ComponentSupplier): ComponentSupplier {
        val newSupplier = copySupplier()
        newSupplier.delayed.add { result ->
            result.append(other.get())
        }
        return newSupplier
    }

    private fun operatorComponent(op: MutableComponent.() -> Unit): ComponentSupplier {
        val newSupplier = copySupplier()
        newSupplier.delayed.add { result ->
            op.invoke(result)
        }
        return newSupplier
    }
    private fun transformComponent(trans: (ComponentSupplier) -> ComponentSupplier): ComponentSupplier {
        val newSupplier = copySupplier()
        newSupplier.transform.add(trans)
        return newSupplier
    }

    private fun copySupplier() = ComponentSupplier(component, delayed.toMutableList(), transform.toMutableList())

    // 颜色（动态颜色仍在 get() 时求值）
    fun gray(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.GRAY) }
    }

    fun white(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.WHITE) }
    }

    fun gold(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.GOLD) }
    }

    fun red(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.RED) }
    }

    fun yellow(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.YELLOW) }
    }

    fun green(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.GREEN) }
    }

    fun orange(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(0xFFA500) }
    }

    fun aqua(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.AQUA) }
    }

    fun lightPurple(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.LIGHT_PURPLE) }
    }

    fun blue(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(ChatFormatting.BLUE) }
    }

    fun rainbow(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(TooltipHelper.RAINBOW.current) }
    }

    fun rainbowFast(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(TooltipHelper.RAINBOW_FAST.current) }
    }

    fun rainbowSlow(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(TooltipHelper.RAINBOW_SLOW.current) }
    }

    fun rainbowGradient(): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(GradientColor.RAINBOW) }
    }

    fun rainbowGradient(float: Float, int: Int, boolean: Boolean): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(OffsetGradientColor(float, int, boolean)) }
    }

    fun rainbowGradient(int: Int): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(OffsetGradientColor(1f, int)) }
    }

    fun color(int: Int): ComponentSupplier = operatorComponent {
        withStyle { it.withColor(int) }
    }

    // 滚动文字
    fun scrollSuprachronal(): ComponentSupplier = transformComponent { supplier ->
        StringUtils.white_blue(supplier.component.string).toLiteralSupplier()
    }
    fun scrollFullColor(): ComponentSupplier = transformComponent { supplier ->
        StringUtils.full_color(supplier.component.string).toLiteralSupplier()
    }
    fun scrollBioware(): ComponentSupplier = transformComponent { supplier ->
        StringUtils.dark_green(supplier.component.string).toLiteralSupplier()
    }
    fun scrollOptical(): ComponentSupplier = transformComponent { supplier ->
        StringUtils.golden(supplier.component.string).toLiteralSupplier()
    }
    fun scrollExotic(): ComponentSupplier = transformComponent { supplier ->
        StringUtils.purplish_red(supplier.component.string).toLiteralSupplier()
    }
    fun scrollCosmic(): ComponentSupplier = transformComponent { supplier ->
        StringUtils.dark_purplish_red(supplier.component.string).toLiteralSupplier()
    }

    // 格式操作与颜色操作一样返回独立的 supplier。
    fun italic(): ComponentSupplier = operatorComponent { withStyle(ChatFormatting.ITALIC) }

    fun bold(): ComponentSupplier = operatorComponent { withStyle(ChatFormatting.BOLD) }

    fun underline(): ComponentSupplier = operatorComponent { withStyle(ChatFormatting.UNDERLINE) }

    fun strikethrough(): ComponentSupplier = operatorComponent { withStyle(ChatFormatting.STRIKETHROUGH) }

    fun obfuscated(): ComponentSupplier = operatorComponent { withStyle(ChatFormatting.OBFUSCATED) }

    fun reset(): ComponentSupplier = operatorComponent { withStyle(ChatFormatting.RESET) }
}

fun Component.toComponentSupplier() = ComponentSupplier(this.copy())
fun <T> T.toLiteralSupplier() = (Component.literal(this.toString())).toComponentSupplier()
fun translatable(key: String, vararg args: Any?) = translatableWithArguments(key, args)

private fun translatableWithArguments(key: String, args: Array<out Any?>): ComponentSupplier {
    // 保留参数快照，避免调用方后续修改数组影响已构建的提示。
    val component = if (args.isEmpty()) {
        Component.translatable(key)
    } else {
        Component.translatable(key, *args)
    }
    return component.toComponentSupplier()
}
infix fun String.translatedTo(other: String): ComponentSupplier {
    val translationKey = TranslationKeyProvider.getTranslationKey(this, other)
    return Component.translatable(translationKey).toComponentSupplier()
}

@JvmName("initialize")
fun ComponentSupplier.initialize(): ComponentSupplier = this.also { it.get() }

@JvmName("initializeList")
fun ComponentListSupplier.initialize(): ComponentListSupplier = this.also { it.get() }

@JvmName("initializeFloat")
fun ((Float) -> ComponentSupplier).initialize(): (Float) -> ComponentSupplier = this.also { it(0f) }

@JvmName("initializeInt")
fun ((Int) -> ComponentSupplier).initialize(): (Int) -> ComponentSupplier = this.also { it(0) }

@JvmName("initializeListInt")
fun ((Int) -> ComponentListSupplier).initialize(): (Int) -> ComponentListSupplier = this.also { it(0) }

@JvmName("initializeBoolean")
fun ((Boolean) -> ComponentSupplier).initialize(): (Boolean) -> ComponentSupplier = this.also { it(false) }

@JvmName("initializeString")
fun ((String) -> ComponentSupplier).initialize(): (String) -> ComponentSupplier = this.also { it("") }

@JvmName("initializeLong")
fun ((Long) -> ComponentSupplier).initialize(): (Long) -> ComponentSupplier = this.also { it(0L) }

@JvmName("initializeItem")
fun ((ComponentSupplier) -> ComponentSupplier).initialize(): (ComponentSupplier) -> ComponentSupplier = this.also { it("".toLiteralSupplier()) }

@JvmName("initializeListItem")
fun ((ComponentSupplier) -> ComponentListSupplier).initialize(): (ComponentSupplier) -> ComponentListSupplier = this.also { it("".toLiteralSupplier()) }

@JvmName("initializeListFloat")
fun ((Float) -> ComponentListSupplier).initialize(): (Float) -> ComponentListSupplier = this.also { it(0f) }

@JvmName("initializeListString")
fun ((String) -> ComponentListSupplier).initialize(): (String) -> ComponentListSupplier = this.also { it("") }

@JvmName("initializeListString2")
fun ((String, String) -> ComponentListSupplier).initialize(): (String, String) -> ComponentListSupplier = this.also { it("", "") }
