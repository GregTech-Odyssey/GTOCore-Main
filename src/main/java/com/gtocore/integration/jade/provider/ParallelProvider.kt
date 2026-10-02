package com.gtocore.integration.jade.provider

import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.SimpleGeneratorMachine
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.utils.FormattingUtil
import com.gtolib.api.machine.feature.multiblock.IParallelMachine
import com.gtolib.api.machine.impl.part.ParallelHatchPartMachine
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig

class ParallelProvider :
    IBlockComponentProvider,
    IServerDataProvider<BlockAccessor> {
    override fun appendTooltip(iTooltip: ITooltip, blockAccessor: BlockAccessor, iPluginConfig: IPluginConfig?) {
        val parallel = blockAccessor.serverData.getLong("parallel")
        if (parallel <= 1) return
        val batchParallel = blockAccessor.serverData.getLong("batch_parallel")
        val parallels = Component.literal(FormattingUtil.formatNumbers(parallel)).withStyle(ChatFormatting.DARK_PURPLE)
        val key = if (blockAccessor.serverData.getBoolean("exact")) "gtceu.multiblock.parallel.exact" else "gtceu.multiblock.parallel"
        iTooltip.add(
            Component.translatable(key, parallels).append(
                if (batchParallel > 1) {
                    Component.translatable("gtceu.multiblock.batch_parallel_multiplier", Component.literal("×$batchParallel").withStyle(ChatFormatting.DARK_BLUE))
                } else {
                    Component.empty()
                },
            ),
        )
    }

    override fun appendServerData(compoundTag: CompoundTag, blockAccessor: BlockAccessor) {
        val blockEntity = blockAccessor.blockEntity as? MetaMachineBlockEntity ?: return
        val machine = blockEntity.getMetaMachine()
        if (machine is ParallelHatchPartMachine) {
            compoundTag.putLong("parallel", machine.getCurrentParallel())
            return
        }
        if (machine is SimpleGeneratorMachine) return
        val definition = machine.getDefinition()
        if (definition is MultiblockMachineDefinition && definition.isGenerator()) return
        val parallels = getRecipeParallel(machine)
        val parallel = parallels[0]
        val batchParallel = parallels[1]
        if (parallel > 0) compoundTag.putBoolean("exact", true)
        val originParallel = getOriginParallel(machine)
        if (parallel > 0) {
            compoundTag.putLong("parallel", parallel)
        } else if (originParallel > 1) {
            compoundTag.putLong("parallel", originParallel)
        }
        if (batchParallel > 1) {
            compoundTag.putLong("batch_parallel", batchParallel)
        } else if (parallel / originParallel > 1) {
            compoundTag.putLong("batch_parallel", parallel / originParallel)
        }
    }

    private fun getOriginParallel(machine: MetaMachine): Long {
        var originParallel = 1L
        if (machine is IWorkableMultiController) {
            if (machine is IParallelMachine) {
                originParallel = machine.getParallel()
            } else {
                machine.getParallelHatch()?.let { originParallel = it.getCurrentParallel() }
            }
        }
        return if (originParallel < 1) 1L else originParallel
    }

    private fun getRecipeParallel(machine: MetaMachine): LongArray {
        val parallel = LongArray(2)
        if (machine is IRecipeLogicMachine && machine.getRecipeLogic().isActive()) {
            val recipe = machine.getRecipeLogic().getLastRecipe()
            if (recipe is GTRecipe) {
                parallel[0] = recipe.parallels
                parallel[1] = recipe.batchParallels
            }
        }
        return parallel
    }

    override fun getUid(): ResourceLocation = GTCEu.id("parallel_info")
}
