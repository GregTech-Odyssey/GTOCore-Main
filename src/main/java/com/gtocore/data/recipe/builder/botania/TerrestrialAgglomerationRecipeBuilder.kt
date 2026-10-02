package com.gtocore.data.recipe.builder.botania

import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.common.crafting.RecipeTerraPlate

class TerrestrialAgglomerationRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var output: ItemStack? = null
    private var mana = 0
    private val ingredients = NonNullList.create<Ingredient>()

    fun output(output: ItemLike): TerrestrialAgglomerationRecipeBuilder = output(ItemStack(output))

    fun output(output: ItemStack?): TerrestrialAgglomerationRecipeBuilder = apply { this.output = output }

    fun mana(mana: Int): TerrestrialAgglomerationRecipeBuilder = apply { this.mana = mana }

    fun addIngredient(ingredient: Ingredient?): TerrestrialAgglomerationRecipeBuilder = apply { ingredient?.let { ingredients.add(it) } }

    fun addIngredient(item: ItemLike): TerrestrialAgglomerationRecipeBuilder = addIngredient(Ingredient.of(item))

    fun addIngredient(tag: TagKey<Item>): TerrestrialAgglomerationRecipeBuilder = addIngredient(Ingredient.of(tag))

    fun save() {
        if (ingredients.isEmpty()) throw IllegalStateException("No ingredients added to terrestrial agglomeration recipe")
        val recipeOutput = checkNotNull(output) { "No output specified for terrestrial agglomeration recipe" }
        GTRecipes.RECIPE_MAP[id] = RecipeTerraPlate(id, mana, ingredients, recipeOutput)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): TerrestrialAgglomerationRecipeBuilder = TerrestrialAgglomerationRecipeBuilder(GTOCore.id("terra_plate/$name"))
    }
}
