package com.gtocore.mixin.xaero;

import com.gtocore.client.ClientReferenceHolder;
import com.gtocore.client.ClientWorldLeakFixes;

import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.file.worldsave.WorldDataHandler;

@Mixin(value = WorldDataHandler.class, remap = false)
public class WorldDataHandlerMixin implements ClientReferenceHolder {

    @Shadow
    private ServerLevel worldServer;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtocore$trackWorldDataHandler(CallbackInfo ci) {
        ClientWorldLeakFixes.trackXaeroReference(this);
    }

    @Override
    public void gtocore$releaseClientReferences() {
        worldServer = null;
    }
}
