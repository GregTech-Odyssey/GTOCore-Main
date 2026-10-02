package com.gtocore.common.fluid

import com.gtocore.common.data.GTOBlocks
import com.gtocore.common.data.GTOFluids
import com.gtocore.common.data.GTOItems

import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraftforge.fluids.ForgeFlowingFluid

abstract class BlazingPyrotheumFluid private constructor() :
    ForgeFlowingFluid(
        Properties(GTOFluids.BLAZING_PYROTHEUM_TYPE, GTOFluids.BLAZING_PYROTHEUM, GTOFluids.FLOWING_BLAZING_PYROTHEUM)
            .explosionResistance(100.0f)
            .tickRate(3)
            .bucket(GTOItems.BLAZING_PYROTHEUM_BUCKET)
            .block(GTOBlocks.BLAZING_PYROTHEUM),
    ) {
    class Source : BlazingPyrotheumFluid() {
        override fun getAmount(state: FluidState): Int = 8

        override fun isSource(state: FluidState): Boolean = true
    }

    class Flowing : BlazingPyrotheumFluid() {
        protected override fun createFluidStateDefinition(builder: StateDefinition.Builder<Fluid, FluidState>) {
            super.createFluidStateDefinition(builder)
            builder.add(LEVEL)
        }

        override fun getAmount(state: FluidState): Int = state.getValue(LEVEL)

        override fun isSource(state: FluidState): Boolean = false
    }
}
