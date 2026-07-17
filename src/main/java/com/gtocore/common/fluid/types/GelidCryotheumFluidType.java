package com.gtocore.common.fluid.types;

import com.gtolib.GTOCore;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public final class GelidCryotheumFluidType extends FluidType {

    public GelidCryotheumFluidType() {
        super(Properties.create().fallDistanceModifier(0.0F).canExtinguish(true).supportsBoating(true).canHydrate(true).motionScale(0.014D).temperature(0).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH).descriptionId("fluid.gtocore.gelid_cryotheum"));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new MyIClientFluidTypeExtensions());
    }

    private static class MyIClientFluidTypeExtensions implements IClientFluidTypeExtensions {

        @Override
        public ResourceLocation getStillTexture() {
            return GTOCore.id("block/fluid/gelid_cryotheum");
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return GTOCore.id("block/fluid/gelid_cryotheum_flow");
        }
    }
}
