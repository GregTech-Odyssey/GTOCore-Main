package com.gtocore.mixin.xaero;

import com.gtocore.client.ClientReferenceHolder;
import com.gtocore.client.ClientWorldLeakFixes;

import net.minecraft.client.multiplayer.ClientLevel;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.MapProcessor;

@Mixin(value = MapProcessor.class, remap = false)
public class MapProcessorMixin implements ClientReferenceHolder {

    @Shadow
    private ClientLevel world;
    @Shadow
    private ClientLevel newWorld;
    @Shadow
    public ClientLevel mainWorld;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtocore$trackMapProcessor(CallbackInfo ci) {
        ClientWorldLeakFixes.trackXaeroReference(this);
    }

    @Override
    public void gtocore$releaseClientReferences() {
        world = null;
        newWorld = null;
        mainWorld = null;
    }
}
