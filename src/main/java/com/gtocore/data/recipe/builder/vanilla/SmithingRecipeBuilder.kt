package com.gtocore.data.recipe.builder.vanilla

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.SmithingTransformRecipe
import net.minecraft.world.level.ItemLike
import net.minecraftforge.common.crafting.StrictNBTIngredient

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.common.data.GTRecipes
import com.gregtechceu.gtceu.data.recipe.builder.ShapedRecipeBuilder
import com.gregtechceu.gtceu.utils.GTUtil
import com.gtolib.GTOCore

open class SmithingRecipeBuilder(@JvmField protected var id: ResourceLocation?) {
    @JvmField
    protected var template: Ingredient? = null

    @JvmField
    protected var base: Ingredient? = null

    @JvmField
    protected var addition: Ingredient? = null

    @JvmField
    protected var result: ItemStack? = null

    open fun id(id: ResourceLocation?): SmithingRecipeBuilder = apply { this.id = id }

    open fun template(tagKey: TagKey<Item>): SmithingRecipeBuilder = template(ShapedRecipeBuilder.INGREDIENT_TAG_FUNCTION.apply(tagKey))

    open fun template(itemStack: ItemStack): SmithingRecipeBuilder = template(
        if (itemStack.hasTag()) StrictNBTIngredient.of(itemStack) else ShapedRecipeBuilder.INGREDIENT_ITEM_FUNCTION.apply(itemStack.getItem()),
    )

    open fun template(itemLike: ItemLike): SmithingRecipeBuilder = template(ShapedRecipeBuilder.INGREDIENT_ITEM_FUNCTION.apply(itemLike.asItem()))

    open fun template(ingredient: Ingredient?): SmithingRecipeBuilder = apply { template = ingredient }

    open fun input(tagKey: TagKey<Item>): SmithingRecipeBuilder = input(ShapedRecipeBuilder.INGREDIENT_TAG_FUNCTION.apply(tagKey))

    open fun input(itemStack: ItemStack): SmithingRecipeBuilder = input(
        if (itemStack.hasTag()) StrictNBTIngredient.of(itemStack) else ShapedRecipeBuilder.INGREDIENT_ITEM_FUNCTION.apply(itemStack.getItem()),
    )

    open fun input(itemLike: ItemLike): SmithingRecipeBuilder = input(ShapedRecipeBuilder.INGREDIENT_ITEM_FUNCTION.apply(itemLike.asItem()))

    open fun input(ingredient: Ingredient?): SmithingRecipeBuilder = apply { base = ingredient }

    open fun addition(tagKey: TagKey<Item>): SmithingRecipeBuilder = addition(ShapedRecipeBuilder.INGREDIENT_TAG_FUNCTION.apply(tagKey))

    open fun addition(itemStack: ItemStack): SmithingRecipeBuilder = addition(
        if (itemStack.hasTag()) StrictNBTIngredient.of(itemStack) else ShapedRecipeBuilder.INGREDIENT_ITEM_FUNCTION.apply(itemStack.getItem()),
    )

    open fun addition(itemLike: ItemLike): SmithingRecipeBuilder = addition(ShapedRecipeBuilder.INGREDIENT_ITEM_FUNCTION.apply(itemLike.asItem()))

    open fun addition(tagPrefix: TagPrefix, material: Material): SmithingRecipeBuilder = addition(ChemicalHelper.getItem(tagPrefix, material))

    open fun addition(ingredient: Ingredient?): SmithingRecipeBuilder = apply { addition = ingredient }

    open fun output(itemLike: ItemLike): SmithingRecipeBuilder = apply { result = ItemStack(itemLike.asItem()) }

    open fun output(itemStack: ItemStack): SmithingRecipeBuilder = apply { result = itemStack.copy() }

    open fun output(itemStack: ItemStack, count: Int): SmithingRecipeBuilder = apply {
        result = itemStack.copy()
        result!!.setCount(count)
    }

    open fun output(itemStack: ItemStack, count: Int, nbt: CompoundTag?): SmithingRecipeBuilder = apply {
        result = itemStack.copy()
        result!!.setCount(count)
        result!!.setTag(nbt)
    }

    protected open fun defaultId(): ResourceLocation = GTUtil.ITEM_ID.apply(result!!.getItem())

    open fun getId(): ResourceLocation {
        val recipeId = id ?: defaultId()
        return GTUtil.getResourceLocation(recipeId.namespace, "smithing/${recipeId.path}")
    }

    // 保留 Java 构建器向配方构造器传递未设置字段的行为，不在此处新增校验。
    @Suppress("TYPE_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    open fun save() {
        val recipeId = getId()
        if (GTRecipes.RECIPE_MAP.put(recipeId, SmithingTransformRecipe(recipeId, template, base, addition, result)) != null) throw IllegalStateException()
    }

    companion object {
        @JvmStatic
        fun builder(id: String): SmithingRecipeBuilder = SmithingRecipeBuilder(GTOCore.id(id))

        @JvmStatic
        fun builder(): SmithingRecipeBuilder = SmithingRecipeBuilder(null)
    }
}
