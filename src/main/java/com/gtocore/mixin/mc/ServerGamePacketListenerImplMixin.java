package com.gtocore.mixin.mc;

import com.gtocore.common.item.misc.WingTrails;

import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Inject(method = "handlePlayerAbilities", at = @At("TAIL"))
    private void gtocore$syncWingFlight(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        WingTrails.onAbilities(((ServerGamePacketListenerImpl) (Object) this).player);
    }
}
