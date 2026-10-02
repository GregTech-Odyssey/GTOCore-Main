package com.gtocore.common.fluid.types

import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions
import net.minecraftforge.common.SoundActions
import net.minecraftforge.fluids.FluidType

import com.gtolib.GTOCore

import java.util.function.Consumer

class BlazingPyrotheumFluidType :
    FluidType(
        Properties.create()
            .fallDistanceModifier(0f)
            .motionScale(0.014)
            .temperature(28000)
            .lightLevel(15)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
            .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
            .descriptionId("fluid.gtocore.blazing_pyrotheum"),
    ) {
    override fun initializeClient(consumer: Consumer<IClientFluidTypeExtensions>) {
        consumer.accept(MyIClientFluidTypeExtensions())
    }

    private class MyIClientFluidTypeExtensions : IClientFluidTypeExtensions {
        override fun getStillTexture(): ResourceLocation = GTOCore.id("block/fluid/blazing_pyrotheum")

        override fun getFlowingTexture(): ResourceLocation = GTOCore.id("block/fluid/blazing_pyrotheum_flow")
    }
}
