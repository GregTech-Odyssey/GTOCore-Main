package com.gtocore.common.fluid

import com.gtocore.common.data.GTOBlocks
import com.gtocore.common.data.GTOFluids
import com.gtocore.common.data.GTOItems

import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraftforge.fluids.ForgeFlowingFluid

abstract class GelidCryotheumFluid private constructor() :
    ForgeFlowingFluid(
        Properties(GTOFluids.GELID_CRYOTHEUM_TYPE, GTOFluids.GELID_CRYOTHEUM, GTOFluids.FLOWING_GELID_CRYOTHEUM)
            .explosionResistance(100.0f)
            .tickRate(3)
            .bucket(GTOItems.GELID_CRYOTHEUM_BUCKET)
            .block(GTOBlocks.GELID_CRYOTHEUM),
    ) {
    class Source : GelidCryotheumFluid() {
        override fun getAmount(state: FluidState): Int = 8

        override fun isSource(state: FluidState): Boolean = true
    }

    class Flowing : GelidCryotheumFluid() {
        protected override fun createFluidStateDefinition(builder: StateDefinition.Builder<Fluid, FluidState>) {
            super.createFluidStateDefinition(builder)
            builder.add(LEVEL)
        }

        override fun getAmount(state: FluidState): Int = state.getValue(LEVEL)

        override fun isSource(state: FluidState): Boolean = false
    }
}
