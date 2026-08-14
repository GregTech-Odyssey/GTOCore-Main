package com.gtocore.api.machine.dynamic;

import net.minecraft.core.Direction;

import org.joml.Matrix4f;

public record RotationMotion(Direction.Axis axis) implements DynamicMotion {

    public static RotationMotion aroundX() {
        return new RotationMotion(Direction.Axis.X);
    }

    public static RotationMotion aroundY() {
        return new RotationMotion(Direction.Axis.Y);
    }

    public static RotationMotion aroundZ() {
        return new RotationMotion(Direction.Axis.Z);
    }

    @Override
    public Matrix4f apply(Matrix4f transform, float value) {
        float angle = (float) Math.toRadians(value);
        return switch (axis) {
            case X -> transform.rotateX(angle);
            case Y -> transform.rotateY(angle);
            case Z -> transform.rotateZ(angle);
        };
    }
}
