package com.gtocore.data.recipe.builder.ars

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.EnchantingApparatusRecipe

class EnchantingApparatusRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var reagent: Ingredient? = null
    private var result: ItemStack? = null
    private val pedestalItems = ArrayList<Ingredient>()
    private var sourceCost = 0
    private var keepNbtOfReagent = false

    fun input(reagent: Ingredient?): EnchantingApparatusRecipeBuilder = apply { this.reagent = reagent }

    fun input(item: ItemLike): EnchantingApparatusRecipeBuilder = input(Ingredient.of(item))

    fun input(tag: TagKey<Item>): EnchantingApparatusRecipeBuilder = input(Ingredient.of(tag))

    fun output(result: ItemStack?): EnchantingApparatusRecipeBuilder = apply { this.result = result }

    fun output(item: ItemLike): EnchantingApparatusRecipeBuilder = output(ItemStack(item))

    fun output(item: ItemLike, count: Int): EnchantingApparatusRecipeBuilder = output(ItemStack(item, count))

    fun addPedestalItem(ingredient: Ingredient?): EnchantingApparatusRecipeBuilder = apply { ingredient?.let { pedestalItems.add(it) } }

    fun addPedestalItem(item: ItemLike): EnchantingApparatusRecipeBuilder = addPedestalItem(Ingredient.of(item))

    fun addPedestalItem(tag: TagKey<Item>): EnchantingApparatusRecipeBuilder = addPedestalItem(Ingredient.of(tag))

    fun sourceCost(cost: Int): EnchantingApparatusRecipeBuilder = apply { sourceCost = cost }

    fun keepNbtOfReagent(keepNbt: Boolean): EnchantingApparatusRecipeBuilder = apply { keepNbtOfReagent = keepNbt }

    fun save() {
        val recipeReagent = checkNotNull(reagent) { "No input specified for enchanting apparatus recipe" }
        val recipeResult = checkNotNull(result) { "No output specified for enchanting apparatus recipe" }
        if (pedestalItems.isEmpty()) throw IllegalStateException("No pedestal items added to enchanting apparatus recipe")
        GTRecipes.RECIPE_MAP[id] = EnchantingApparatusRecipe(id, pedestalItems, recipeReagent, recipeResult, sourceCost, keepNbtOfReagent)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): EnchantingApparatusRecipeBuilder = EnchantingApparatusRecipeBuilder(GTOCore.id("enchanting_apparatus/$name"))
    }
}
