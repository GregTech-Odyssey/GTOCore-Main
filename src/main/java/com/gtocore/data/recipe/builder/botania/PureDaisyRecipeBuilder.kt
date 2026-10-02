package com.gtocore.data.recipe.builder.botania

import net.minecraft.commands.CommandFunction
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.api.recipe.StateIngredient
import vazkii.botania.common.crafting.PureDaisyRecipe
import vazkii.botania.common.crafting.StateIngredientHelper

class PureDaisyRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var input: StateIngredient? = null
    private var output: Block? = null
    private var time = DEFAULT_TIME

    fun input(input: StateIngredient?): PureDaisyRecipeBuilder = apply { this.input = input }

    fun input(input: Block): PureDaisyRecipeBuilder = input(StateIngredientHelper.of(input))

    fun input(inputTag: TagKey<Block>): PureDaisyRecipeBuilder = input(StateIngredientHelper.of(inputTag))

    fun output(output: Block?): PureDaisyRecipeBuilder = apply { this.output = output }

    fun time(time: Int): PureDaisyRecipeBuilder = apply { this.time = time }

    fun save() {
        GTRecipes.RECIPE_MAP[id] = PureDaisyRecipe(id, input, output!!.defaultBlockState(), time, CommandFunction.CacheableFunction.NONE)
    }

    companion object {
        private const val DEFAULT_TIME = 150

        @JvmStatic
        fun builder(name: String?): PureDaisyRecipeBuilder = PureDaisyRecipeBuilder(GTOCore.id("pure_daisy/$name"))
    }
}
