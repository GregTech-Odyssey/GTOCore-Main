package com.gtocore.mixin.ftbu;

import net.minecraft.core.BlockPos;

import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = FTBUltimineClient.class, remap = false)
public interface FTBUltimineClientAccessor {

    @Accessor("shapeBlocks")
    List<BlockPos> gto$getShapeBlocks();
}
