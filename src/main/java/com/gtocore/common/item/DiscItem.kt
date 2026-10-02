package com.gtocore.common.item

import com.gtocore.api.placeholder.IPlaceholder

import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.registries.ForgeRegistries

import com.gregtechceu.gtceu.core.mixins.StrictNBTIngredientAccessor
import com.gto.fastcollection.fastutil.O2IOpenCacheHashMap
import com.gtolib.api.item.IItem
import com.gtolib.api.recipe.lookup.IngredientConverter
import com.gtolib.api.recipe.lookup.MapIngredient
import com.gtolib.utils.FluidUtils
import com.gtolib.utils.ItemUtils
import com.gtolib.utils.RLUtils

import java.util.Collections
import java.util.function.ToIntFunction

class DiscItem(properties: Properties) :
    Item(properties),
    IPlaceholder<Any, ItemStack, Unit> {
    companion object {
        private var DATA_DISC: Item? = null
        private val NBTS = O2IOpenCacheHashMap<String>()

        @JvmField
        val INGREDIENT_CONVERTER = IngredientConverter<Ingredient> { ingredient, amount, map ->
            if (ingredient is StrictNBTIngredientAccessor) {
                val nbt = ingredient.getStack().getTag()
                if (nbt != null && ingredient.getStack().getItem() === DATA_DISC) {
                    val name = nbt.tags["n"]
                    if (name is StringTag) {
                        map.add(NBTS.getInt(name.asString), amount)
                        return@IngredientConverter
                    }
                }
            }
            MapIngredient.INGREDIENT_CONVERTER.convert(ingredient, amount, map)
        }

        @JvmField
        val ITEM_CONVERTER = IngredientConverter<ItemStack> { stack, amount, map ->
            val nbt = stack.getTag()
            if (nbt != null && stack.getItem() === DATA_DISC) {
                val name = nbt.tags["n"]
                if (name is StringTag) {
                    map.add(NBTS.getInt(name.asString), amount)
                    return@IngredientConverter
                }
            }
            MapIngredient.ITEM_CONVERTER.convert(stack, amount, map)
        }

        private val EMPTY_CONTENT: Any = ItemStack.EMPTY

        /** 读取光盘中的物品；未配置、未注册或为空气时返回 null。 */
        private fun getStoredItem(stack: ItemStack): Item? {
            val tag = stack.getTag() ?: return null
            val namespace = tag.getString("i")
            if (namespace.isEmpty()) return null
            val id = RLUtils.fromNamespaceAndPath(namespace, tag.getString("n"))
            return ForgeRegistries.ITEMS.getValue(id)?.takeUnless { it === Items.AIR }
        }

        /** 读取光盘中的流体；未配置、未注册或为空流体时返回 null。 */
        private fun getStoredFluid(stack: ItemStack): Fluid? {
            val tag = stack.getTag() ?: return null
            val namespace = tag.getString("f")
            if (namespace.isEmpty()) return null
            val id = RLUtils.fromNamespaceAndPath(namespace, tag.getString("n"))
            return ForgeRegistries.FLUIDS.getValue(id)?.takeUnless { it === Fluids.EMPTY }
        }

        private fun getStoredContent(stack: ItemStack): Any {
            getStoredItem(stack)?.let { return (it as IItem).`gtolib$getReadOnlyStack`() }
            getStoredFluid(stack)?.let { return FluidStack(it, 1) }
            return EMPTY_CONTENT
        }
    }

    init {
        DATA_DISC = this
    }

    fun getDisc(itemStack: ItemStack): ItemStack = getDefaultInstance().apply {
        val id = ItemUtils.getIdLocation(itemStack.getItem())
        getOrCreateTag().putString("i", id.namespace)
        getOrCreateTag().putString("n", id.path)
        NBTS.computeIfAbsent(id.path, ToIntFunction { MapIngredient.getCount(null) })
    }

    fun getDisc(fluid: Fluid): ItemStack = getDefaultInstance().apply {
        val id = FluidUtils.getIdLocation(fluid)
        getOrCreateTag().putString("f", id.namespace)
        getOrCreateTag().putString("n", id.path)
        NBTS.computeIfAbsent(id.path, ToIntFunction { MapIngredient.getCount(null) })
    }

    override fun appendHoverText(itemstack: ItemStack, world: Level?, list: MutableList<Component>, flag: TooltipFlag) {
        super.appendHoverText(itemstack, world, list, flag)
        val storedItem = getStoredItem(itemstack)
        if (storedItem != null) {
            list.add(Component.translatable("item.gtocore.disc.data", (storedItem as IItem).`gtolib$getReadOnlyStack`().displayName))
        } else {
            getStoredFluid(itemstack)?.let {
                list.add(Component.translatable("item.gtocore.disc.data", "[${FluidStack(it, 1).displayName.string}]"))
            }
        }
    }

    override fun getTargetLists(sourceDisc: ItemStack): List<List<Any>> {
        val content = getStoredContent(sourceDisc)
        if (content == EMPTY_CONTENT) return Collections.emptyList()
        return java.util.List.of(java.util.List.of(content))
    }

    override fun getCurrentTarget(sourceDisc: ItemStack, context: Unit?): Any? = getStoredContent(sourceDisc).takeUnless { it == EMPTY_CONTENT }
}
