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

    public static DynamicMotion aroundAxes(int x, int y, int z) {
        return new MultiAxisRotationMotion(x, y, z);
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

    private record MultiAxisRotationMotion(int x, int y, int z) implements DynamicMotion {

        @Override
        public Matrix4f apply(Matrix4f transform, float value) {
            float angle = (float) Math.toRadians(value);
            return transform.rotateXYZ(angle * x, angle * y, angle * z);
        }

        @Override
        public Matrix4f apply(Matrix4f transform, float value, float returnProgress) {
            if (returnProgress <= 0) return apply(transform, value);
            double halfX = Math.toRadians(value * x) * .5;
            double halfY = Math.toRadians(value * y) * .5;
            double halfZ = Math.toRadians(value * z) * .5;
            double sinX = Math.sin(halfX), cosX = Math.cos(halfX);
            double sinY = Math.sin(halfY), cosY = Math.cos(halfY);
            double sinZ = Math.sin(halfZ), cosZ = Math.cos(halfZ);
            double qx = sinX * cosY * cosZ + cosX * sinY * sinZ;
            double qy = cosX * sinY * cosZ - sinX * cosY * sinZ;
            double qz = cosX * cosY * sinZ + sinX * sinY * cosZ;
            double qw = cosX * cosY * cosZ - sinX * sinY * sinZ;
            if (qw < 0) {
                qx = -qx;
                qy = -qy;
                qz = -qz;
                qw = -qw;
            }
            double sinHalf = Math.sqrt(qx * qx + qy * qy + qz * qz);
            if (sinHalf < 1E-7) return transform;
            float progress = returnProgress * returnProgress * (3 - 2 * returnProgress);
            float angle = (float) (2 * Math.atan2(sinHalf, qw) * (1 - progress));
            return transform.rotate(angle, (float) (qx / sinHalf), (float) (qy / sinHalf), (float) (qz / sinHalf));
        }
    }
}
