package com.gtocore.mixin.ae2.menu;

import com.gtolib.api.wireless.energy.RemotePatternMenuLocator;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.api.networking.security.IActionHost;
import appeng.core.sync.packets.GuiDataSyncPacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.me.common.MEStorageMenu;

import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AEBaseMenu.class)
public abstract class AEBaseMenuMixin {

    @Shadow(remap = false)
    protected abstract IActionHost getActionHost();

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    private void gto$validateRemoteProvider(Player player, CallbackInfoReturnable<Boolean> cir) {
        var menu = (AEBaseMenu) (Object) this;
        if (player instanceof ServerPlayer serverPlayer && menu.getLocator() instanceof RemotePatternMenuLocator locator) {
            cir.setReturnValue(menu.isValidMenu() && locator.isValid(serverPlayer, menu.getTarget()));
        }
    }

    @Inject(method = "receiveClientAction", at = @At("HEAD"), cancellable = true, remap = false)
    private void gto$validateRemoteProviderAction(GuiDataSyncPacket packet, CallbackInfo ci) {
        var menu = (AEBaseMenu) (Object) this;
        if (menu.getLocator() instanceof RemotePatternMenuLocator locator && menu.getPlayer() instanceof ServerPlayer player &&
                (!menu.isValidMenu() || !locator.isValid(player, menu.getTarget())))
            ci.cancel();
    }

    @Inject(method = "stillValid", at = @At("RETURN"), cancellable = true)
    private void gtolib$stillValid(Player PlayerEntity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (getActionHost() instanceof WTMenuHost) {
            if ((Object) this instanceof MEStorageMenu menu) {
                ((MEStorageMenuAccessor) menu).callUpdatePowerStatus();
            }
            cir.setReturnValue(true);
        }
    }
}
