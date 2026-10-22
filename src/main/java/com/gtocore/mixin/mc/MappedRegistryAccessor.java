package com.gtocore.mixin.mc;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor {

    @Accessor("unregisteredIntrusiveHolders")
    Map<Object, Holder.Reference<Object>> gtocore$getUnregisteredIntrusiveHolders();
}
