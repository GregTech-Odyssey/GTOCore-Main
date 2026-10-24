package com.gtocore.mixin.mc;

import net.minecraft.world.level.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Block.class)
public interface BlockAccessor {

    @Accessor("descriptionId")
    void gtocore$setDescriptionId(String descriptionId);
}
