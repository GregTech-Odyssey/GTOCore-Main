package com.gtocore.data.recipe.builder.botania

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike

import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gtolib.GTOCore
import vazkii.botania.common.crafting.ElvenTradeRecipe

class ElvenTradeRecipeBuilder private constructor(private val id: ResourceLocation) {
    private val inputs = ArrayList<Ingredient>()
    private val outputs = ArrayList<ItemStack>()

    private fun addInput(input: Ingredient?): ElvenTradeRecipeBuilder = apply { input?.let { inputs.add(it) } }

    fun addInput(item: ItemLike): ElvenTradeRecipeBuilder = addInput(Ingredient.of(item))

    fun addInput(tag: TagKey<Item>): ElvenTradeRecipeBuilder = addInput(Ingredient.of(tag))

    private fun addOutput(output: ItemStack?): ElvenTradeRecipeBuilder = apply { output?.let { outputs.add(it) } }

    fun addOutput(output: ItemLike): ElvenTradeRecipeBuilder = addOutput(ItemStack(output))

    fun addOutput(output: ItemLike, count: Int): ElvenTradeRecipeBuilder = addOutput(ItemStack(output, count))

    fun save() {
        if (inputs.isEmpty()) throw IllegalStateException("No inputs added to elven trade recipe")
        if (outputs.isEmpty()) throw IllegalStateException("No outputs specified for elven trade recipe")
        // 直接调用构造器引用，按数组传参；不要改为展开运算符，它会额外复制数组。
        GTRecipes.RECIPE_MAP[id] = (::ElvenTradeRecipe)(id, outputs.toArray(emptyArray<ItemStack>()), inputs.toArray(emptyArray<Ingredient>()))
    }

    companion object {
        @JvmStatic
        fun builder(name: String?): ElvenTradeRecipeBuilder = ElvenTradeRecipeBuilder(GTOCore.id("elven_trade/$name"))
    }
}
