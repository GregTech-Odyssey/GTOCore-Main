package com.gtocore.mixin.ae2;

import com.gtocore.integration.ae.SpeedCards;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import appeng.items.materials.UpgradeCardItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UpgradeCardItem.class)
public abstract class UpgradeCardItemMixin {

    @Inject(method = "onItemUseFirst", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtocore$refuseSpeedCardAtLimit(ItemStack stack, UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        var result = SpeedCards.refuseAtLimit(stack, context);
        if (result != null) cir.setReturnValue(result);
    }
}
