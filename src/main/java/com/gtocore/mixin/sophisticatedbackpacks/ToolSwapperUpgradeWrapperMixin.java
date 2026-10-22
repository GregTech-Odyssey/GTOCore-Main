package com.gtocore.mixin.sophisticatedbackpacks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.toolswapper.ToolSwapperUpgradeWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ToolSwapperUpgradeWrapper.class, remap = false)
public class ToolSwapperUpgradeWrapperMixin {

    @Inject(method = "onBlockClick", at = @At("HEAD"), cancellable = true)
    private void gtocore$disableBlockClick(Player player, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "onAttackEntity", at = @At("HEAD"), cancellable = true)
    private void gtocore$disableAttackEntity(Player player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "onBlockInteract", at = @At("HEAD"), cancellable = true)
    private void gtocore$disableBlockInteract(Level level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "onEntityInteract", at = @At("HEAD"), cancellable = true)
    private void gtocore$disableEntityInteract(Level level, Entity entity, Player player, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = { "canProcessBlockInteract", "canProcessEntityInteract" }, at = @At("HEAD"), cancellable = true)
    private void gtocore$disableProcess(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
