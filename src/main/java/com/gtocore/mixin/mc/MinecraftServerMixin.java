package com.gtocore.mixin.mc;

import com.gtolib.api.dimension.DimensionLevelData;
import com.gtolib.api.dimension.DimensionSync;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "synchronizeTime", at = @At("HEAD"), cancellable = true)
    private void synchronizeIndependentTime(ServerLevel level, CallbackInfo ci) {
        if (level.getLevelData() instanceof DimensionLevelData) {
            DimensionSync.syncTime(level);
            ci.cancel();
        }
    }
}
