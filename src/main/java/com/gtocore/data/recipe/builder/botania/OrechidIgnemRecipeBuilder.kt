package com.gtocore.data.recipe.builder.botania

import net.minecraft.commands.CommandFunction
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.api.recipe.StateIngredient
import vazkii.botania.common.crafting.OrechidIgnemRecipe
import vazkii.botania.common.crafting.StateIngredientHelper

class OrechidIgnemRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var input: StateIngredient? = null
    private var output: Block? = null
    private var weight = 1

    private fun input(input: StateIngredient?): OrechidIgnemRecipeBuilder = apply { this.input = input }

    fun input(input: Block): OrechidIgnemRecipeBuilder = input(StateIngredientHelper.of(input))

    fun input(inputTag: TagKey<Block>): OrechidIgnemRecipeBuilder = input(StateIngredientHelper.of(inputTag))

    fun output(output: Block?): OrechidIgnemRecipeBuilder = apply { this.output = output }

    fun weight(weight: Int): OrechidIgnemRecipeBuilder = apply { this.weight = weight }

    fun save() {
        GTRecipes.RECIPE_MAP[id] = OrechidIgnemRecipe(id, input, StateIngredientHelper.of(output!!.defaultBlockState()), weight, CommandFunction.CacheableFunction.NONE)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): OrechidIgnemRecipeBuilder = OrechidIgnemRecipeBuilder(GTOCore.id("orechid_ignem/$name"))
    }
}
