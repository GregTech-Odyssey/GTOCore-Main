package com.gtocore.data.recipe.builder.ars

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe

class ImbuementRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var input: Ingredient? = null
    private var output: ItemStack? = null
    private var source = 0
    private val pedestalItems = ArrayList<Ingredient>()

    fun input(input: Ingredient?): ImbuementRecipeBuilder = apply { this.input = input }

    fun input(item: ItemLike): ImbuementRecipeBuilder = input(Ingredient.of(item))

    fun input(tag: TagKey<Item>): ImbuementRecipeBuilder = input(Ingredient.of(tag))

    fun output(output: ItemStack?): ImbuementRecipeBuilder = apply { this.output = output }

    fun output(item: ItemLike): ImbuementRecipeBuilder = output(ItemStack(item))

    fun output(item: ItemLike, count: Int): ImbuementRecipeBuilder = output(ItemStack(item, count))

    fun source(source: Int): ImbuementRecipeBuilder = apply { if (source != 0) this.source = source }

    fun addPedestalItem(ingredient: Ingredient?): ImbuementRecipeBuilder = apply { ingredient?.let { pedestalItems.add(it) } }

    fun addPedestalItem(item: ItemLike): ImbuementRecipeBuilder = addPedestalItem(Ingredient.of(item))

    fun addPedestalItem(tag: TagKey<Item>): ImbuementRecipeBuilder = addPedestalItem(Ingredient.of(tag))

    fun save() {
        val recipeInput = checkNotNull(input) { "No input specified for imbuement recipe" }
        val recipeOutput = checkNotNull(output) { "No output specified for imbuement recipe" }
        GTRecipes.RECIPE_MAP[id] = ImbuementRecipe(id, recipeInput, recipeOutput, source, pedestalItems)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): ImbuementRecipeBuilder = ImbuementRecipeBuilder(GTOCore.id("imbuement/$name"))
    }
}
