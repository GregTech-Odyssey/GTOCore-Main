package com.gtocore.mixin.ae2.eae;

import net.minecraft.server.level.ServerPlayer;

import appeng.api.stacks.AEKey;
import appeng.menu.implementations.SetStockAmountMenu;
import appeng.menu.locator.MenuLocator;

import com.glodblock.github.extendedae.container.ContainerExInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ContainerExInterface.class)
public abstract class ContainerExInterfaceMixin {

    @Redirect(method = "openSetAmountMenu", at = @At(value = "INVOKE", target = "Lappeng/menu/implementations/SetStockAmountMenu;open(Lnet/minecraft/server/level/ServerPlayer;Lappeng/menu/locator/MenuLocator;ILappeng/api/stacks/AEKey;I)V"), remap = false)
    private void gtocore$openWithLongAmount(ServerPlayer player, MenuLocator locator, int slot, AEKey what, int truncated) {
        var stack = ((ContainerExInterface) (Object) this).getHost().getConfig().getStack(slot);
        SetStockAmountMenu.open(player, locator, slot, what, stack == null ? truncated : stack.amount());
    }
}
