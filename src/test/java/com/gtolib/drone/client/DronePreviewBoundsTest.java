package com.gtolib.drone.client;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DronePreviewBoundsTest {

    @Test
    void ordinaryPreviewIsVisibleInsteadOfCulledByTheFormerBillionBlockFarPlane() {
        var bounds = new DronePreviewBounds();
        bounds.include(-16, -64, -16);
        bounds.include(16, 320, 16);
        float far = bounds.farPlane(0, 128, 100);
        var matrix = perspective(far).lookAt(0, 128, 100, 0, 128, 0, 0, 1, 0);
        assertTrue(new FrustumIntersection(matrix).testAab(-8, 120, -8, 8, 136, 8));
        assertFinitePlanes(matrix);
    }

    @Test
    void largeSelectionsAndMovingOutsideTheSnapshotKeepFinitePlanesAndVisibleGeometry() {
        var bounds = new DronePreviewBounds();
        bounds.include(-29999999, -64, -29999999);
        bounds.include(29999999, 320, 29999999);
        for (float eyeZ : new float[] { 100, 90000000 }) {
            float far = bounds.farPlane(0, 128, eyeZ);
            assertTrue(far > eyeZ);
            var matrix = perspective(far).lookAt(0, 128, eyeZ, 0, 128, 0, 0, 1, 0);
            assertTrue(new FrustumIntersection(matrix).testAab(-16, 112, -16, 16, 144, 16));
            assertFinitePlanes(matrix);
        }
    }

    private static Matrix4f perspective(float far) {
        return new Matrix4f().setPerspective((float) Math.toRadians(60), 1.5f, DronePreviewBounds.nearPlane(far), far);
    }

    private static void assertFinitePlanes(Matrix4f matrix) {
        var plane = new Vector4f();
        for (int i = 0; i < 6; i++) {
            matrix.frustumPlane(i, plane);
            assertTrue(Float.isFinite(plane.x) && Float.isFinite(plane.y) && Float.isFinite(plane.z) && Float.isFinite(plane.w));
        }
    }
}
