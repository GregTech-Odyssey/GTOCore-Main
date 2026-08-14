package com.gtocore.mixin.mc;

import com.gtocore.api.machine.dynamic.DynamicCollisionManager;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 gto$moveWithDynamicStructure(Vec3 movement) {
        Entity entity = (Entity) (Object) this;
        return movement.add(DynamicCollisionManager.getSupportMovement(entity.level(), entity, movement));
    }

    @WrapOperation(method = "collideBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockCollisions(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/lang/Iterable;"))
    private static Iterable<VoxelShape> gto$addDynamicCollisions(Level level, Entity entity, AABB box, Operation<Iterable<VoxelShape>> original) {
        Iterable<VoxelShape> collisions = original.call(level, entity, box);
        var dynamicCollisions = DynamicCollisionManager.getCollisions(level, entity, box);
        return dynamicCollisions.isEmpty() ? collisions : Iterables.concat(collisions, dynamicCollisions);
    }
}
