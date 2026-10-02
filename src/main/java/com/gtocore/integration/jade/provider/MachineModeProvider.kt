package com.gtocore.integration.jade.provider

import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.data.GTRecipeTypes
import com.gtolib.utils.RLUtils
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

open class MachineModeProvider :
    IBlockComponentProvider,
    IServerDataProvider<BlockAccessor> {
    override fun appendTooltip(iTooltip: ITooltip, blockAccessor: BlockAccessor, iPluginConfig: IPluginConfig?) {
        val serverData = blockAccessor.serverData
        if (!serverData.contains("RecipeTypes") || !serverData.contains("CurrentRecipeType")) return

        val currentRecipeTypeIndex = serverData.getInt("CurrentRecipeType")
        val recipeTypes = serverData.getList("RecipeTypes", StringTag.TAG_STRING.toInt())
        if (blockAccessor.showDetails()) {
            iTooltip.add(Component.translatable("gtceu.top.machine_mode"))
            for (i in 0 until recipeTypes.size) {
                val recipeType = RLUtils.parse(recipeTypes.getString(i))
                val text = if (currentRecipeTypeIndex == i) {
                    Component.literal(" > ").withStyle(ChatFormatting.BLUE)
                } else {
                    Component.literal("   ")
                }
                text.append(Component.translatable("${recipeType.namespace}.${recipeType.path}"))
                iTooltip.add(text)
            }
        } else {
            val recipeType = RLUtils.parse(recipeTypes.getString(currentRecipeTypeIndex))
            iTooltip.add(
                Component.translatable("gtceu.top.machine_mode")
                    .append(Component.translatable("${recipeType.namespace}.${recipeType.path}")),
            )
        }
    }

    override fun appendServerData(compoundTag: CompoundTag, blockAccessor: BlockAccessor) {
        val blockEntity = blockAccessor.blockEntity as? MetaMachineBlockEntity ?: return
        val machine = blockEntity.getMetaMachine()
        val recipeTypes = machine.getDefinition().recipeTypes ?: return
        if (recipeTypes.size <= 1) return
        val recipeMachine = machine as? IRecipeLogicMachine ?: return

        val recipeTypesTagList = ListTag()
        var currentRecipeType: GTRecipeType? = recipeMachine.recipeType
        if (recipeMachine.recipeLogic.isWorking) {
            recipeMachine.recipeLogic.lastOriginRecipe?.recipeType?.let {
                if (it !== GTRecipeTypes.DUMMY_RECIPES) currentRecipeType = it
            }
        }
        var currentRecipeTypeIndex = -1
        for (i in recipeTypes.indices) {
            if (recipeTypes[i] === currentRecipeType) currentRecipeTypeIndex = i
            recipeTypesTagList.add(StringTag.valueOf(recipeTypes[i]!!.registryName.toString()))
        }
        compoundTag.put("RecipeTypes", recipeTypesTagList)
        compoundTag.putInt("CurrentRecipeType", currentRecipeTypeIndex)
    }

    override fun getUid(): ResourceLocation = GTCEu.id("machine_mode")
}
