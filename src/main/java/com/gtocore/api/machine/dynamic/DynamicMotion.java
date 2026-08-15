package com.gtocore.api.machine.dynamic;

import org.joml.Matrix4f;

@FunctionalInterface
public interface DynamicMotion {

    DynamicMotion NONE = (transform, value) -> transform;

    Matrix4f apply(Matrix4f transform, float value);

    default Matrix4f apply(Matrix4f transform, float value, float returnProgress) {
        return apply(transform, value * (1 - returnProgress));
    }
}
