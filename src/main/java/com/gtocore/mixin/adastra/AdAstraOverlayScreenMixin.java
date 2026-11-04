package com.gtocore.mixin.adastra;

import net.minecraft.world.entity.LivingEntity;

import earth.terrarium.adastra.client.screens.player.OverlayScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(OverlayScreen.class)
public abstract class AdAstraOverlayScreenMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/common/items/armor/SpaceSuitItem;hasFullSet(Lnet/minecraft/world/entity/LivingEntity;)Z", remap = false), remap = false)
    private static boolean gtocore$renderOxygenInMovableHud(LivingEntity player) {
        return false;
    }
}
