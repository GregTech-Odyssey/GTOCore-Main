package com.gtocore.api.machine.dynamic;

import net.minecraft.util.Mth;

public final class DynamicRotationState {

    private final float returnStep;
    private float value;
    private float valueDelta;
    private float returnProgress = 1;
    private float returnDelta;

    public DynamicRotationState(int returnTicks) {
        returnStep = 1F / returnTicks;
    }

    public void tick(boolean active, float speed) {
        valueDelta = 0;
        returnDelta = 0;
        if (active) {
            if (returnProgress >= 1 && value == 0) {
                returnDelta = -1;
                returnProgress = 0;
                advance(speed);
            } else if (returnProgress > 0) {
                float previous = returnProgress;
                returnProgress = Math.max(0, returnProgress - returnStep);
                returnDelta = returnProgress - previous;
            } else {
                advance(speed);
            }
        } else if (returnProgress < 1) {
            float previous = returnProgress;
            returnProgress = Math.min(1, returnProgress + returnStep);
            returnDelta = returnProgress - previous;
            if (returnProgress >= 1) value = 0;
        }
    }

    public float getValue(float partialTicks) {
        return value + valueDelta * Mth.clamp(partialTicks, -1, 1);
    }

    public float getReturnProgress(float partialTicks) {
        return Mth.clamp(returnProgress + returnDelta * Mth.clamp(partialTicks, -1, 1), 0, 1);
    }

    public boolean isAtOrigin() {
        return returnProgress >= 1;
    }

    public void reset() {
        value = 0;
        valueDelta = 0;
        returnProgress = 1;
        returnDelta = 0;
    }

    private void advance(float speed) {
        value = Mth.wrapDegrees(value + speed);
        valueDelta = speed;
    }
}
