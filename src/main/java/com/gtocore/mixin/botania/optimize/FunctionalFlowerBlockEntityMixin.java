package com.gtocore.mixin.botania.optimize;

import appeng.hooks.ticking.TickHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;

@Mixin(FunctionalFlowerBlockEntity.class)
public class FunctionalFlowerBlockEntityMixin {

    @Redirect(method = "tickFlower", at = @At(value = "INVOKE", target = "Lvazkii/botania/api/block_entity/FunctionalFlowerBlockEntity;acceptsRedstone()Z"), remap = false)
    private boolean acceptsRedstone(FunctionalFlowerBlockEntity flowerBlockEntity) {
        return TickHandler.INSTANCE.tickCounter % 20 == 3 && flowerBlockEntity.acceptsRedstone();
    }
}
