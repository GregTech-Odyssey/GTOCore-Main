package com.gtocore.mixin.emitrades;

import com.gtocore.integration.emi.trades.TradeEntitySnapshot;
import com.gtocore.integration.emi.trades.TradeEntityStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import dev.emi.emi.api.stack.EmiStack;
import io.github.prismwork.emitrades.util.EntityEmiStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityEmiStack.class, remap = false)
public abstract class EntityEmiStackMixin implements TradeEntityStack {

    @Shadow
    @Final
    @Mutable
    private Entity entity;

    @Unique
    private TradeEntitySnapshot gtocore$snapshot;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtocore$detachEntity(Entity entity, float scale, CallbackInfo ci) {
        gtocore$snapshot = TradeEntitySnapshot.of(entity);
        this.entity = null;
    }

    @Redirect(method = { "render", "getTooltip", "getName" },
              at = @At(value = "FIELD", target = "Lio/github/prismwork/emitrades/util/EntityEmiStack;entity:Lnet/minecraft/world/entity/Entity;", opcode = Opcodes.GETFIELD))
    private Entity gtocore$getDisplayEntity(EntityEmiStack stack) {
        return gtocore$snapshot == null ? null : gtocore$snapshot.entity();
    }

    @Redirect(method = "copy*",
              at = @At(value = "FIELD", target = "Lio/github/prismwork/emitrades/util/EntityEmiStack;entity:Lnet/minecraft/world/entity/Entity;", opcode = Opcodes.GETFIELD))
    private Entity gtocore$copyWithoutWorld(EntityEmiStack stack) {
        return null;
    }

    @Inject(method = "copy*", at = @At("RETURN"))
    private void gtocore$copySnapshot(CallbackInfoReturnable<EmiStack> cir) {
        ((TradeEntityStack) cir.getReturnValue()).gtocore$setTradeEntitySnapshot(gtocore$snapshot);
    }

    @Unique
    @Override
    public void gtocore$setTradeEntitySnapshot(TradeEntitySnapshot snapshot) {
        gtocore$snapshot = snapshot;
    }

    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    private void gtocore$hasSnapshot(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(gtocore$snapshot == null);
    }

    @Inject(method = "getKey", at = @At("HEAD"), cancellable = true)
    private void gtocore$worldIndependentKey(CallbackInfoReturnable<Object> cir) {
        cir.setReturnValue(gtocore$snapshot);
    }

    @Inject(method = "getId", at = @At("HEAD"), cancellable = true)
    private void gtocore$entityTypeId(CallbackInfoReturnable<ResourceLocation> cir) {
        if (gtocore$snapshot != null) cir.setReturnValue(gtocore$snapshot.id());
    }

    @Inject(method = "getNbt", at = @At("HEAD"), cancellable = true)
    private void gtocore$appearanceTag(CallbackInfoReturnable<CompoundTag> cir) {
        if (gtocore$snapshot != null) cir.setReturnValue(gtocore$snapshot.tag());
    }
}
