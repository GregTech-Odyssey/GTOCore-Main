package com.gtocore.mixin.mc;

import com.gtocore.integration.ae.TieredInterfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    @Deprecated(since = "0.6.0", forRemoval = true)
    @Inject(method = "loadStatic", at = @At("HEAD"))
    private static void gtocore$migrateLegacyEntity(BlockPos pos, BlockState state, CompoundTag tag, CallbackInfoReturnable<BlockEntity> cir) {
        TieredInterfaces.migrateBlockEntityTag(tag);
    }
}
