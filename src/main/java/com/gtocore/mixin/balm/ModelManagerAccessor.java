package com.gtocore.mixin.balm;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ModelManager.class, remap = false)
public interface ModelManagerAccessor {

    @Accessor("modelBakery")
    ModelBakery gtocore$getModelBakery();

    @Accessor("modelBakery")
    void gtocore$setModelBakery(ModelBakery bakery);
}
