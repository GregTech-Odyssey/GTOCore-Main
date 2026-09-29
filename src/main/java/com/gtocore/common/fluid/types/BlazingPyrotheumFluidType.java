package com.gtocore.common.fluid.types;

import com.gtolib.GTOCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public final class BlazingPyrotheumFluidType extends FluidType {

    public BlazingPyrotheumFluidType() {
        super(Properties.create().fallDistanceModifier(0.0F).motionScale(0.014D).temperature(28000).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH).descriptionId("fluid.gtocore.blazing_pyrotheum"));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new MyIClientFluidTypeExtensions());
    }

    private static class MyIClientFluidTypeExtensions implements IClientFluidTypeExtensions {

        @Override
        public ResourceLocation getStillTexture() {
            return GTOCore.id("block/fluid/blazing_pyrotheum");
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return GTOCore.id("block/fluid/blazing_pyrotheum_flow");
        }
    }
}
