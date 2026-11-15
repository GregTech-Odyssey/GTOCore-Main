package com.gtocore.mixin.forge;

import com.gtocore.common.forge.ConfigEventLists;

import net.minecraftforge.server.loading.ServerModLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerModLoader.class, remap = false)
public class ServerModLoaderMixin {

    @Inject(method = "load", at = @At("HEAD"))
    private static void gtocore$prepareConfigEvents(CallbackInfo ci) {
        ConfigEventLists.prepare();
    }
}
