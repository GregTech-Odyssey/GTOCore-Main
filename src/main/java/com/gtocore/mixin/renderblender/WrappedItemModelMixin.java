package com.gtocore.mixin.renderblender;

import com.gtocore.client.ClientReferenceHolder;
import com.gtocore.client.ClientWorldLeakFixes;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;

import committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WrappedItemModel.class, remap = false)
public abstract class WrappedItemModelMixin implements ClientReferenceHolder {

    @Shadow
    protected LivingEntity entity;
    @Shadow
    protected ClientLevel world;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtocore$trackModel(CallbackInfo ci) {
        ClientWorldLeakFixes.trackModel(this);
    }

    @Unique
    @Override
    public void gtocore$releaseClientReferences() {
        world = null;
        entity = null;
    }
}
