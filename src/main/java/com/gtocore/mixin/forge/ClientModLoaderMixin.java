package com.gtocore.mixin.forge;

import com.gtocore.common.forge.ConfigEventLists;

import net.minecraftforge.client.loading.ClientModLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientModLoader.class, remap = false)
public class ClientModLoaderMixin {

    @Inject(method = "begin", at = @At("HEAD"))
    private static void gtocore$prepareConfigEvents(CallbackInfo ci) {
        ConfigEventLists.prepare();
    }
}
