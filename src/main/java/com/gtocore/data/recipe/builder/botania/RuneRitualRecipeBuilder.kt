package com.gtocore.data.recipe.builder.botania

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import mythicbotany.rune.RuneRitualRecipe

class RuneRitualRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var centerRune: Ingredient? = null
    private val runes = ArrayList<RuneRitualRecipe.RunePosition>()
    private var mana = 0
    private var ticks = 200
    private val inputs = ArrayList<Ingredient?>()
    private val outputs = ArrayList<ItemStack?>()

    fun centerRune(centerRune: Ingredient?): RuneRitualRecipeBuilder = apply { this.centerRune = centerRune }

    fun centerRune(centerRune: ItemLike): RuneRitualRecipeBuilder = centerRune(Ingredient.of(centerRune))

    fun centerRune(centerRuneTag: TagKey<Item>): RuneRitualRecipeBuilder = centerRune(Ingredient.of(centerRuneTag))

    fun addOuterRune(rune: Ingredient?, x: Int, z: Int, consume: Boolean): RuneRitualRecipeBuilder = apply {
        runes.add(RuneRitualRecipe.RunePosition(rune, x, z, consume))
    }

    fun addOuterRune(rune: ItemLike, x: Int, z: Int, consume: Boolean): RuneRitualRecipeBuilder = addOuterRune(Ingredient.of(rune), x, z, consume)

    fun addOuterRune(runeTag: TagKey<Item>, x: Int, z: Int, consume: Boolean): RuneRitualRecipeBuilder = addOuterRune(Ingredient.of(runeTag), x, z, consume)

    fun mana(mana: Int): RuneRitualRecipeBuilder = apply { this.mana = mana }

    fun ticks(ticks: Int): RuneRitualRecipeBuilder = apply { this.ticks = ticks }

    // 原实现直接添加元素，包括 null；不套用其他构建器忽略 null 的规则。
    fun addInput(input: Ingredient?): RuneRitualRecipeBuilder = apply { inputs.add(input) }

    fun addInput(input: ItemLike): RuneRitualRecipeBuilder = addInput(Ingredient.of(input))

    fun addInput(inputTag: TagKey<Item>): RuneRitualRecipeBuilder = addInput(Ingredient.of(inputTag))

    fun addOutput(output: ItemStack?): RuneRitualRecipeBuilder = apply { outputs.add(output) }

    fun addOutput(output: ItemLike): RuneRitualRecipeBuilder = addOutput(ItemStack(output))

    fun save() {
        val recipeCenterRune = checkNotNull(centerRune) { "符文仪式配方 $id 未设置中心符文！" }
        if (outputs.isEmpty()) throw IllegalStateException("符文仪式配方 $id 未设置输出物品！")
        GTRecipes.RECIPE_MAP[id] = RuneRitualRecipe(id, recipeCenterRune, runes, mana, ticks, inputs, outputs, null, null)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): RuneRitualRecipeBuilder = RuneRitualRecipeBuilder(GTOCore.id("rune_ritual/$name"))
    }
}
