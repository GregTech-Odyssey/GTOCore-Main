package com.gtocore.mixin.mc;

import com.gtocore.api.machine.dynamic.DynamicCollisionManager;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 gto$moveWithDynamicStructure(Vec3 movement) {
        Entity entity = (Entity) (Object) this;
        return movement.add(DynamicCollisionManager.getCollisionMovement(entity.level(), entity, movement));
    }

    @Inject(method = "collide", at = @At("RETURN"), cancellable = true)
    private void gto$collideWithDynamicStructure(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity) (Object) this;
        cir.setReturnValue(DynamicCollisionManager.collideMovement(entity.level(), entity, movement, cir.getReturnValue()));
    }
}
