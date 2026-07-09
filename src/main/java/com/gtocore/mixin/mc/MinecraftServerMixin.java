package com.gtocore.mixin.mc;

import com.gtocore.common.saved.VoidWorldTimeSavedData;

import com.gtolib.api.data.GTODimensions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "synchronizeTime", at = @At("HEAD"), cancellable = true)
    private void cancelLockedVoidWorldTimeSynchronization(ServerLevel level, CallbackInfo ci) {
        if (VoidWorldTimeSavedData.INSTANCE.isFixedTime() && GTODimensions.isVoid(level.dimension())) {
            ci.cancel();
        }
    }
}
