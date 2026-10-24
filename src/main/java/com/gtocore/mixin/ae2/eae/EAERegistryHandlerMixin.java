package com.gtocore.mixin.ae2.eae;

import com.gtocore.integration.ae.TieredInterfaces;

import net.minecraft.world.level.ItemLike;

import com.glodblock.github.extendedae.common.EAERegistryHandler;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EAERegistryHandler.class)
public abstract class EAERegistryHandlerMixin {

    @Deprecated(since = "0.6.0", forRemoval = true)
    @WrapWithCondition(method = "registerAEUpgrade", at = @At(value = "INVOKE", target = "Lappeng/api/upgrades/Upgrades;add(Lnet/minecraft/world/level/ItemLike;Lnet/minecraft/world/level/ItemLike;ILjava/lang/String;)V"), remap = false)
    private boolean gtocore$skipOversizeUpgrades(ItemLike card, ItemLike machine, int max, String group) {
        return !TieredInterfaces.isLegacy(machine);
    }
}
