package com.gtocore.data.recipe.builder.botania

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.common.crafting.PetalsRecipe

class PetalApothecaryRecipeBuilder private constructor(private val id: ResourceLocation) {
    private var output: ItemStack? = null
    private var reagent: Ingredient? = null
    private val ingredients = ArrayList<Ingredient>()

    fun output(output: ItemLike): PetalApothecaryRecipeBuilder = output(ItemStack(output))

    fun output(output: ItemStack?): PetalApothecaryRecipeBuilder = apply { this.output = output }

    fun reagent(reagent: Ingredient?): PetalApothecaryRecipeBuilder = apply { this.reagent = reagent }

    fun reagent(reagent: ItemLike): PetalApothecaryRecipeBuilder = reagent(Ingredient.of(reagent))

    fun reagent(reagentTag: TagKey<Item>): PetalApothecaryRecipeBuilder = reagent(Ingredient.of(reagentTag))

    fun addIngredient(ingredient: Ingredient?): PetalApothecaryRecipeBuilder = apply { ingredient?.let { ingredients.add(it) } }

    fun addIngredient(item: ItemLike): PetalApothecaryRecipeBuilder = addIngredient(Ingredient.of(item))

    fun addIngredient(tag: TagKey<Item>): PetalApothecaryRecipeBuilder = addIngredient(Ingredient.of(tag))

    fun save() {
        // 直接调用构造器引用，避免 vararg 展开造成额外数组复制。
        GTRecipes.RECIPE_MAP[id] = (::PetalsRecipe)(id, output, reagent, ingredients.toArray(emptyArray<Ingredient>()))
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): PetalApothecaryRecipeBuilder = PetalApothecaryRecipeBuilder(GTOCore.id("petal_apothecary/$name"))
    }
}
