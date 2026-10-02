package com.gtocore.data.recipe.builder.botania

import net.minecraft.commands.CommandFunction
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.api.recipe.StateIngredient
import vazkii.botania.common.crafting.MarimorphosisRecipe
import vazkii.botania.common.crafting.StateIngredientHelper

class MarimorphosisRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var input: StateIngredient? = null
    private var output: Block? = null
    private var weight = 1
    private var biomeBonus = 11
    private var biomeTag: TagKey<Biome>? = null

    private fun input(input: StateIngredient?): MarimorphosisRecipeBuilder = apply { this.input = input }

    fun input(input: Block): MarimorphosisRecipeBuilder = input(StateIngredientHelper.of(input))

    fun input(inputTag: TagKey<Block>): MarimorphosisRecipeBuilder = input(StateIngredientHelper.of(inputTag))

    fun output(output: Block?): MarimorphosisRecipeBuilder = apply { this.output = output }

    fun weight(weight: Int): MarimorphosisRecipeBuilder = apply { this.weight = weight }

    fun biomeBonus(biomeBonus: Int): MarimorphosisRecipeBuilder = apply { this.biomeBonus = biomeBonus }

    fun biomeTag(biomeTag: TagKey<Biome>?): MarimorphosisRecipeBuilder = apply { this.biomeTag = biomeTag }

    fun save() {
        GTRecipes.RECIPE_MAP[id] = MarimorphosisRecipe(id, input, StateIngredientHelper.of(output!!.defaultBlockState()), weight, CommandFunction.CacheableFunction.NONE, biomeBonus, biomeTag)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): MarimorphosisRecipeBuilder = MarimorphosisRecipeBuilder(GTOCore.id("marimorphosis/$name"))
    }
}
