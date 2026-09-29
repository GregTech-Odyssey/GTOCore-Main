package com.gtocore.common.fluid;

import com.gtocore.common.data.GTOBlocks;
import com.gtocore.common.data.GTOFluids;
import com.gtocore.common.data.GTOItems;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import org.jetbrains.annotations.NotNull;

public abstract class BlazingPyrotheumFluid extends ForgeFlowingFluid {

    private BlazingPyrotheumFluid() {
        super(new Properties(GTOFluids.BLAZING_PYROTHEUM_TYPE, GTOFluids.BLAZING_PYROTHEUM,
                GTOFluids.FLOWING_BLAZING_PYROTHEUM).explosionResistance(100.0f).tickRate(3).bucket(GTOItems.BLAZING_PYROTHEUM_BUCKET).block(GTOBlocks.BLAZING_PYROTHEUM));
    }

    public final static class Source extends BlazingPyrotheumFluid {

        @Override
        public int getAmount(@NotNull FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return true;
        }
    }

    public final static class Flowing extends BlazingPyrotheumFluid {

        @Override
        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return false;
        }
    }
}
