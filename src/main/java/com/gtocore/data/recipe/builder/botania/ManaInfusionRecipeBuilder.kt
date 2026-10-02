package com.gtocore.data.recipe.builder.botania

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.api.recipe.StateIngredient
import vazkii.botania.common.block.BotaniaBlocks
import vazkii.botania.common.crafting.ManaInfusionRecipe
import vazkii.botania.common.crafting.StateIngredientHelper

class ManaInfusionRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var input: Ingredient? = null
    private var output: ItemStack? = null
    private var mana = 0
    private var group = ""
    private var catalyst: StateIngredient? = null

    fun input(input: Ingredient?): ManaInfusionRecipeBuilder = apply { this.input = input }

    fun input(item: ItemLike): ManaInfusionRecipeBuilder = input(Ingredient.of(item))

    fun input(tag: TagKey<Item>): ManaInfusionRecipeBuilder = input(Ingredient.of(tag))

    fun output(output: ItemLike): ManaInfusionRecipeBuilder = output(ItemStack(output))

    fun output(output: ItemStack?): ManaInfusionRecipeBuilder = apply { this.output = output }

    fun mana(mana: Int): ManaInfusionRecipeBuilder = apply { this.mana = mana }

    fun group(group: String?): ManaInfusionRecipeBuilder = apply { group?.let { this.group = it } }

    fun alchemyCatalyst(): ManaInfusionRecipeBuilder = apply { catalyst = StateIngredientHelper.of(BotaniaBlocks.alchemyCatalyst) }

    fun conjurationCatalyst(): ManaInfusionRecipeBuilder = apply { catalyst = StateIngredientHelper.of(BotaniaBlocks.conjurationCatalyst) }

    fun customCatalyst(catalyst: Block?): ManaInfusionRecipeBuilder = apply { catalyst?.let { this.catalyst = StateIngredientHelper.of(it) } }

    fun save() {
        GTRecipes.RECIPE_MAP[id] = ManaInfusionRecipe(id, output, input, mana, group, catalyst)
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): ManaInfusionRecipeBuilder = ManaInfusionRecipeBuilder(GTOCore.id("mana_infusion/$name"))
    }
}
