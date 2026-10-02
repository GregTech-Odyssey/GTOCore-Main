package com.gtocore.data.recipe.builder.botania

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.api.brew.Brew
import vazkii.botania.common.crafting.BotanicalBreweryRecipe

class BrewRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var brew: Brew? = null
    private val ingredients = ArrayList<Ingredient>()

    fun brew(brew: Brew?): BrewRecipeBuilder = apply { this.brew = brew }

    private fun addIngredient(ingredient: Ingredient): BrewRecipeBuilder = apply { ingredients.add(ingredient) }

    fun addIngredient(item: ItemLike): BrewRecipeBuilder = addIngredient(Ingredient.of(item))

    fun addIngredient(tag: TagKey<Item>): BrewRecipeBuilder = addIngredient(Ingredient.of(tag))

    fun save() {
        val recipeBrew = checkNotNull(brew) { "No brew specified for brew recipe" }
        if (ingredients.isEmpty()) throw IllegalStateException("No ingredients added to brew recipe")
        // 直接调用构造器引用，按数组传参；不要改为展开运算符，它会额外复制数组。
        GTRecipes.RECIPE_MAP[id] = (::BotanicalBreweryRecipe)(id, recipeBrew, ingredients.toArray(emptyArray<Ingredient>()))
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): BrewRecipeBuilder = BrewRecipeBuilder(GTOCore.id("brew/$name"))
    }
}
