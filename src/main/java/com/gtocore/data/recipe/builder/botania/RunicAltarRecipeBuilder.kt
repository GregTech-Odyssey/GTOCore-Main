package com.gtocore.data.recipe.builder.botania

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.common.crafting.RunicAltarRecipe
import vazkii.botania.common.crafting.recipe.HeadRecipe

class RunicAltarRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var output: ItemStack? = null
    private var mana = 0
    private val ingredients = ArrayList<Ingredient>()
    private var isHeadRecipe = false

    fun output(output: ItemLike): RunicAltarRecipeBuilder = output(ItemStack(output))

    fun output(output: ItemStack?): RunicAltarRecipeBuilder = apply { this.output = output }

    fun mana(mana: Int): RunicAltarRecipeBuilder = apply { this.mana = mana }

    fun addIngredient(ingredient: Ingredient?): RunicAltarRecipeBuilder = apply { ingredient?.let { ingredients.add(it) } }

    fun addIngredient(item: ItemLike): RunicAltarRecipeBuilder = addIngredient(Ingredient.of(item))

    fun addIngredient(tag: TagKey<Item>): RunicAltarRecipeBuilder = addIngredient(Ingredient.of(tag))

    // 保留 Java Boolean 参数签名及传入 null 时的拆箱失败行为。
    fun setHeadRecipe(setHeadRecipe: Boolean?): RunicAltarRecipeBuilder = apply { isHeadRecipe = setHeadRecipe!! }

    fun save() {
        if (ingredients.isEmpty()) throw IllegalStateException("No ingredients added to runic altar recipe")
        val recipeOutput = checkNotNull(output) { "No output specified for runic altar recipe" }
        // 两个分支均直接传递数组，不使用 vararg 展开。
        GTRecipes.RECIPE_MAP[id] = if (isHeadRecipe) {
            (::HeadRecipe)(id, recipeOutput, mana, ingredients.toArray(emptyArray<Ingredient>()))
        } else {
            (::RunicAltarRecipe)(id, recipeOutput, mana, ingredients.toArray(emptyArray<Ingredient>()))
        }
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): RunicAltarRecipeBuilder = RunicAltarRecipeBuilder(GTOCore.id("runic_altar/$name"))
    }
}
