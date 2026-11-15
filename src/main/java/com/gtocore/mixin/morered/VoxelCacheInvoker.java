package com.gtocore.mixin.morered;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "commoble.morered.wires.VoxelCache", remap = false)
public interface VoxelCacheInvoker {

    @Invoker("clearClientCache")
    static void gtocore$clearClientCache() {
        throw new AssertionError();
    }
}
