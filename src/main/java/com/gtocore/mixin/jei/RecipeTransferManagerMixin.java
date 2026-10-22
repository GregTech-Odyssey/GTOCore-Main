package com.gtocore.mixin.jei;

import net.minecraft.world.inventory.AbstractContainerMenu;

import mezz.jei.library.recipes.RecipeTransferManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

@Mixin(value = RecipeTransferManager.class, remap = false)
public class RecipeTransferManagerMixin {

    @Shadow
    @Final
    @Mutable
    private Set<AbstractContainerMenu> unsupportedContainers;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtocore$weakenUnsupportedMenus(CallbackInfo ci) {
        unsupportedContainers = Collections.newSetFromMap(new WeakHashMap<>());
    }
}
