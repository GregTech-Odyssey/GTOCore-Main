package com.gtocore.mixin.mc;

import com.gtocore.api.machine.dynamic.DynamicCollisionManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockCollisions.class)
public abstract class BlockCollisionsMixin {

    @Shadow
    @Final
    private BlockPos.MutableBlockPos pos;

    @Shadow
    @Final
    private CollisionGetter collisionGetter;

    @ModifyExpressionValue(method = "computeNext", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState hideDynamicCollision(BlockState state) {
        return DynamicCollisionManager.isHidden(collisionGetter, pos) ? Blocks.AIR.defaultBlockState() : state;
    }
}
