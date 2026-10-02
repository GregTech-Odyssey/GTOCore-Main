package com.gtocore.data.recipe.builder.botania

import net.minecraft.commands.CommandFunction
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.api.recipe.StateIngredient
import vazkii.botania.common.crafting.OrechidRecipe
import vazkii.botania.common.crafting.StateIngredientHelper

class OrechidRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var input: StateIngredient? = null
    private var output: Block? = null
    private var weight = 1

    private fun input(input: StateIngredient?): OrechidRecipeBuilder = apply { this.input = input }

    fun input(input: Block): OrechidRecipeBuilder = input(StateIngredientHelper.of(input))

    fun input(inputTag: TagKey<Block>): OrechidRecipeBuilder = input(StateIngredientHelper.of(inputTag))

    fun output(output: Block?): OrechidRecipeBuilder = apply { this.output = output }

    fun weight(weight: Int): OrechidRecipeBuilder = apply { this.weight = weight }

    fun save() {
        GTRecipes.RECIPE_MAP[id] = OrechidRecipe(id, input, StateIngredientHelper.of(output!!.defaultBlockState()), weight, CommandFunction.CacheableFunction.NONE)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): OrechidRecipeBuilder = OrechidRecipeBuilder(GTOCore.id("orechid/$name"))
    }
}
