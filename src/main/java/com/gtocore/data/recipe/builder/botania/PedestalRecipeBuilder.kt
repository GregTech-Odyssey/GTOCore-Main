package com.gtocore.data.recipe.builder.botania

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import io.github.lounode.extrabotany.common.crafting.PedestalsRecipe

class PedestalRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var output: ItemStack? = null
    private var input: Ingredient? = null
    private var smashTools: Ingredient? = null
    private var strike = 1
    private var exp = 0

    fun output(output: ItemLike): PedestalRecipeBuilder = output(ItemStack(output))

    fun output(output: ItemStack?): PedestalRecipeBuilder = apply { this.output = output }

    fun input(input: Ingredient?): PedestalRecipeBuilder = apply { this.input = input }

    fun input(item: ItemLike): PedestalRecipeBuilder = input(Ingredient.of(item))

    fun input(tag: TagKey<Item>): PedestalRecipeBuilder = input(Ingredient.of(tag))

    fun smashTools(smashTools: Ingredient?): PedestalRecipeBuilder = apply { this.smashTools = smashTools }

    fun smashTools(item: ItemLike): PedestalRecipeBuilder = smashTools(Ingredient.of(item))

    fun smashTools(tag: TagKey<Item>): PedestalRecipeBuilder = smashTools(Ingredient.of(tag))

    fun strike(strike: Int): PedestalRecipeBuilder = apply { this.strike = strike }

    fun exp(exp: Int): PedestalRecipeBuilder = apply { this.exp = exp }

    fun save() {
        val recipeOutput = checkNotNull(output) { "No output specified for pedestal recipe" }
        val recipeInput = checkNotNull(input) { "No input specified for pedestal recipe" }
        val recipeSmashTools = checkNotNull(smashTools) { "No smash tools specified for pedestal recipe" }
        GTRecipes.RECIPE_MAP[id] = PedestalsRecipe(id, recipeOutput, recipeSmashTools, recipeInput, strike, exp)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): PedestalRecipeBuilder = PedestalRecipeBuilder(GTOCore.id("pedestal/$name"))
    }
}
