package com.gtocore.mixin.balm;

import net.blay09.mods.balm.forge.client.rendering.ForgeBalmModels;
import net.minecraft.client.resources.model.ModelBakery;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ForgeBalmModels.class, remap = false)
public interface ForgeBalmModelsAccessor {

    @Accessor("modelBakery")
    static ModelBakery gtocore$getModelBakery() {
        throw new AssertionError();
    }

    @Accessor("modelBakery")
    static void gtocore$setModelBakery(ModelBakery bakery) {
        throw new AssertionError();
    }
}
