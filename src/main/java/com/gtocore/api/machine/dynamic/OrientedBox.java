package com.gtocore.api.machine.dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class OrientedBox {

    private static final double AXIS_EPSILON = 1.0E-10;
    private static final double SAT_EPSILON = 1.0E-7;

    private final Matrix4f transform;
    private final Vector3f point = new Vector3f();
    private final int originX;
    private final int originY;
    private final int originZ;
    private final double axisXX;
    private final double axisXY;
    private final double axisXZ;
    private final double axisYX;
    private final double axisYY;
    private final double axisYZ;
    private final double axisZX;
    private final double axisZY;
    private final double axisZZ;
    private final double scaleX;
    private final double scaleY;
    private final double scaleZ;
    private double centerX;
    private double centerY;
    private double centerZ;
    private double halfX;
    private double halfY;
    private double halfZ;
    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    OrientedBox(Matrix4f transform, BlockPos origin) {
        this.transform = transform;
        originX = origin.getX();
        originY = origin.getY();
        originZ = origin.getZ();

        Vector3f axisX = transform.transformDirection(new Vector3f(1, 0, 0));
        Vector3f axisY = transform.transformDirection(new Vector3f(0, 1, 0));
        Vector3f axisZ = transform.transformDirection(new Vector3f(0, 0, 1));
        scaleX = axisX.length();
        scaleY = axisY.length();
        scaleZ = axisZ.length();
        axisXX = axisX.x() / scaleX;
        axisXY = axisX.y() / scaleX;
        axisXZ = axisX.z() / scaleX;
        axisYX = axisY.x() / scaleY;
        axisYY = axisY.y() / scaleY;
        axisYZ = axisY.z() / scaleY;
        axisZX = axisZ.x() / scaleZ;
        axisZY = axisZ.y() / scaleZ;
        axisZZ = axisZ.z() / scaleZ;
    }

    OrientedBox set(AABB box) {
        point.set(
                (float) ((box.minX + box.maxX) * .5),
                (float) ((box.minY + box.maxY) * .5),
                (float) ((box.minZ + box.maxZ) * .5));
        transform.transformPosition(point);
        centerX = point.x + originX;
        centerY = point.y + originY;
        centerZ = point.z + originZ;
        halfX = (box.maxX - box.minX) * .5 * scaleX;
        halfY = (box.maxY - box.minY) * .5 * scaleY;
        halfZ = (box.maxZ - box.minZ) * .5 * scaleZ;

        double extentX = Math.abs(axisXX) * halfX + Math.abs(axisYX) * halfY + Math.abs(axisZX) * halfZ;
        double extentY = Math.abs(axisXY) * halfX + Math.abs(axisYY) * halfY + Math.abs(axisZY) * halfZ;
        double extentZ = Math.abs(axisXZ) * halfX + Math.abs(axisYZ) * halfY + Math.abs(axisZZ) * halfZ;
        minX = centerX - extentX;
        minY = centerY - extentY;
        minZ = centerZ - extentZ;
        maxX = centerX + extentX;
        maxY = centerY + extentY;
        maxZ = centerZ + extentZ;
        return this;
    }

    AABB includeBounds(OrientedBox other) {
        return new AABB(
                Math.min(minX, other.minX),
                Math.min(minY, other.minY),
                Math.min(minZ, other.minZ),
                Math.max(maxX, other.maxX),
                Math.max(maxY, other.maxY),
                Math.max(maxZ, other.maxZ));
    }

    AABB bounds() {
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    Vec3 centerMovementFrom(OrientedBox previous) {
        return new Vec3(centerX - previous.centerX, centerY - previous.centerY, centerZ - previous.centerZ);
    }

    Vec3 pointMovementFrom(OrientedBox previous, Vec3 position) {
        double deltaX = position.x - centerX;
        double deltaY = position.y - centerY;
        double deltaZ = position.z - centerZ;
        double localX = Mth.clamp(deltaX * axisXX + deltaY * axisXY + deltaZ * axisXZ, -halfX, halfX);
        double localY = Mth.clamp(deltaX * axisYX + deltaY * axisYY + deltaZ * axisYZ, -halfY, halfY);
        double localZ = Mth.clamp(deltaX * axisZX + deltaY * axisZY + deltaZ * axisZZ, -halfZ, halfZ);
        double currentX = centerX + axisXX * localX + axisYX * localY + axisZX * localZ;
        double currentY = centerY + axisXY * localX + axisYY * localY + axisZY * localZ;
        double currentZ = centerZ + axisXZ * localX + axisYZ * localY + axisZZ * localZ;
        double previousX = previous.centerX + previous.axisXX * localX + previous.axisYX * localY + previous.axisZX * localZ;
        double previousY = previous.centerY + previous.axisXY * localX + previous.axisYY * localY + previous.axisZY * localZ;
        double previousZ = previous.centerZ + previous.axisXZ * localX + previous.axisYZ * localY + previous.axisZZ * localZ;
        return new Vec3(currentX - previousX, currentY - previousY, currentZ - previousZ);
    }

    void move(Vec3 movement) {
        centerX += movement.x;
        centerY += movement.y;
        centerZ += movement.z;
        minX += movement.x;
        minY += movement.y;
        minZ += movement.z;
        maxX += movement.x;
        maxY += movement.y;
        maxZ += movement.z;
    }

    boolean sweep(AABB box, Vec3 movement, SweepResult result) {
        result.reset();
        double boxCenterX = (box.minX + box.maxX) * .5;
        double boxCenterY = (box.minY + box.maxY) * .5;
        double boxCenterZ = (box.minZ + box.maxZ) * .5;
        double deltaX = boxCenterX - centerX;
        double deltaY = boxCenterY - centerY;
        double deltaZ = boxCenterZ - centerZ;
        double boxHalfX = (box.maxX - box.minX) * .5;
        double boxHalfY = (box.maxY - box.minY) * .5;
        double boxHalfZ = (box.maxZ - box.minZ) * .5;

        if (!sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, 1, 0, 0) ||
                !sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, 0, 1, 0) ||
                !sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, 0, 0, 1) ||
                !sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, axisXX, axisXY, axisXZ) ||
                !sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, axisYX, axisYY, axisYZ) ||
                !sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, axisZX, axisZY, axisZZ) ||
                !sweepCrosses(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, axisXX, axisXY, axisXZ) ||
                !sweepCrosses(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, axisYX, axisYY, axisYZ) ||
                !sweepCrosses(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, axisZX, axisZY, axisZZ))
            return false;
        if (result.entryTime > result.exitTime + SAT_EPSILON || result.exitTime < -SAT_EPSILON || result.entryTime > 1 + SAT_EPSILON) return false;
        if (result.entryTime >= -SAT_EPSILON && result.hasEntryNormal) {
            result.time = Math.max(0, result.entryTime);
            return true;
        }
        if (!result.hasContactNormal || result.contactInto >= -SAT_EPSILON) return false;
        result.time = 0;
        result.normalX = result.contactX;
        result.normalY = result.contactY;
        result.normalZ = result.contactZ;
        return true;
    }

    boolean intersects(AABB box) {
        if (maxX <= box.minX || minX >= box.maxX || maxY <= box.minY || minY >= box.maxY || maxZ <= box.minZ || minZ >= box.maxZ) return false;
        double centerX = (box.minX + box.maxX) * .5;
        double centerY = (box.minY + box.maxY) * .5;
        double centerZ = (box.minZ + box.maxZ) * .5;
        double deltaX = centerX - this.centerX;
        double deltaY = centerY - this.centerY;
        double deltaZ = centerZ - this.centerZ;
        double boxHalfX = (box.maxX - box.minX) * .5;
        double boxHalfY = (box.maxY - box.minY) * .5;
        double boxHalfZ = (box.maxZ - box.minZ) * .5;

        if (separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisXX, axisXY, axisXZ) ||
                separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisYX, axisYY, axisYZ) ||
                separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisZX, axisZY, axisZZ) ||
                separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, 1, 0, 0) ||
                separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, 0, 1, 0) ||
                separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, 0, 0, 1))
            return false;

        return !separatedCrosses(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisXX, axisXY, axisXZ) &&
                !separatedCrosses(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisYX, axisYY, axisYZ) &&
                !separatedCrosses(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisZX, axisZY, axisZZ);
    }

    @Nullable
    Vec3 getMinimumTranslation(AABB box, OrientedBox previous) {
        if (maxX <= box.minX || minX >= box.maxX || maxY <= box.minY || minY >= box.maxY || maxZ <= box.minZ || minZ >= box.maxZ) return null;
        double boxCenterX = (box.minX + box.maxX) * .5;
        double boxCenterY = (box.minY + box.maxY) * .5;
        double boxCenterZ = (box.minZ + box.maxZ) * .5;
        double deltaX = boxCenterX - centerX;
        double deltaY = boxCenterY - centerY;
        double deltaZ = boxCenterZ - centerZ;
        double previousDeltaX = boxCenterX - previous.centerX;
        double previousDeltaY = boxCenterY - previous.centerY;
        double previousDeltaZ = boxCenterZ - previous.centerZ;
        double movementX = centerX - previous.centerX;
        double movementY = centerY - previous.centerY;
        double movementZ = centerZ - previous.centerZ;
        double boxHalfX = (box.maxX - box.minX) * .5;
        double boxHalfY = (box.maxY - box.minY) * .5;
        double boxHalfZ = (box.maxZ - box.minZ) * .5;
        MinimumTranslation result = new MinimumTranslation();

        if (!testTranslation(result, deltaX, deltaY, deltaZ, previousDeltaX, previousDeltaY, previousDeltaZ, movementX, movementY, movementZ,
                boxHalfX, boxHalfY, boxHalfZ, axisXX, axisXY, axisXZ) ||
                !testTranslation(result, deltaX, deltaY, deltaZ, previousDeltaX, previousDeltaY, previousDeltaZ, movementX, movementY, movementZ,
                        boxHalfX, boxHalfY, boxHalfZ, axisYX, axisYY, axisYZ) ||
                !testTranslation(result, deltaX, deltaY, deltaZ, previousDeltaX, previousDeltaY, previousDeltaZ, movementX, movementY, movementZ,
                        boxHalfX, boxHalfY, boxHalfZ, axisZX, axisZY, axisZZ) ||
                !testTranslation(result, deltaX, deltaY, deltaZ, previousDeltaX, previousDeltaY, previousDeltaZ, movementX, movementY, movementZ,
                        boxHalfX, boxHalfY, boxHalfZ, 1, 0, 0) ||
                !testTranslation(result, deltaX, deltaY, deltaZ, previousDeltaX, previousDeltaY, previousDeltaZ, movementX, movementY, movementZ,
                        boxHalfX, boxHalfY, boxHalfZ, 0, 1, 0) ||
                !testTranslation(result, deltaX, deltaY, deltaZ, previousDeltaX, previousDeltaY, previousDeltaZ, movementX, movementY, movementZ,
                        boxHalfX, boxHalfY, boxHalfZ, 0, 0, 1) ||
                separatedCrosses(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisXX, axisXY, axisXZ) ||
                separatedCrosses(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisYX, axisYY, axisYZ) ||
                separatedCrosses(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisZX, axisZY, axisZZ))
            return null;
        return new Vec3(result.x, result.y, result.z).scale(result.distance + .001);
    }

    private boolean testTranslation(MinimumTranslation result,
                                    double deltaX, double deltaY, double deltaZ,
                                    double previousDeltaX, double previousDeltaY, double previousDeltaZ,
                                    double movementX, double movementY, double movementZ,
                                    double boxHalfX, double boxHalfY, double boxHalfZ,
                                    double testX, double testY, double testZ) {
        double length = Math.sqrt(testX * testX + testY * testY + testZ * testZ);
        if (length * length < AXIS_EPSILON) return true;
        testX /= length;
        testY /= length;
        testZ /= length;
        double centerDistance = deltaX * testX + deltaY * testY + deltaZ * testZ;
        double radius = halfX * Math.abs(axisXX * testX + axisXY * testY + axisXZ * testZ) +
                halfY * Math.abs(axisYX * testX + axisYY * testY + axisYZ * testZ) +
                halfZ * Math.abs(axisZX * testX + axisZY * testY + axisZZ * testZ) +
                boxHalfX * Math.abs(testX) + boxHalfY * Math.abs(testY) + boxHalfZ * Math.abs(testZ);
        if (radius - Math.abs(centerDistance) <= SAT_EPSILON) return false;

        double direction = previousDeltaX * testX + previousDeltaY * testY + previousDeltaZ * testZ;
        if (Math.abs(direction) < SAT_EPSILON) direction = movementX * testX + movementY * testY + movementZ * testZ;
        if (Math.abs(direction) < SAT_EPSILON) direction = centerDistance;
        double distance = direction >= 0 ? radius - centerDistance : radius + centerDistance;
        if (distance >= result.distance) return true;
        result.distance = distance;
        result.x = direction >= 0 ? testX : -testX;
        result.y = direction >= 0 ? testY : -testY;
        result.z = direction >= 0 ? testZ : -testZ;
        return true;
    }

    private boolean separatedCrosses(double deltaX, double deltaY, double deltaZ,
                                     double boxHalfX, double boxHalfY, double boxHalfZ,
                                     double axisX, double axisY, double axisZ) {
        return separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, 0, axisZ, -axisY) ||
                separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, -axisZ, 0, axisX) ||
                separated(deltaX, deltaY, deltaZ, boxHalfX, boxHalfY, boxHalfZ, axisY, -axisX, 0);
    }

    private boolean sweepCrosses(SweepResult result, double deltaX, double deltaY, double deltaZ, Vec3 movement,
                                 double boxHalfX, double boxHalfY, double boxHalfZ,
                                 double axisX, double axisY, double axisZ) {
        return sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, 0, axisZ, -axisY) &&
                sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, -axisZ, 0, axisX) &&
                sweepAxis(result, deltaX, deltaY, deltaZ, movement, boxHalfX, boxHalfY, boxHalfZ, axisY, -axisX, 0);
    }

    private boolean sweepAxis(SweepResult result, double deltaX, double deltaY, double deltaZ, Vec3 movement,
                              double boxHalfX, double boxHalfY, double boxHalfZ,
                              double testX, double testY, double testZ) {
        double length = Math.sqrt(testX * testX + testY * testY + testZ * testZ);
        if (length * length < AXIS_EPSILON) return true;
        testX /= length;
        testY /= length;
        testZ /= length;
        double distance = deltaX * testX + deltaY * testY + deltaZ * testZ;
        double radius = halfX * Math.abs(axisXX * testX + axisXY * testY + axisXZ * testZ) +
                halfY * Math.abs(axisYX * testX + axisYY * testY + axisYZ * testZ) +
                halfZ * Math.abs(axisZX * testX + axisZY * testY + axisZZ * testZ) +
                boxHalfX * Math.abs(testX) + boxHalfY * Math.abs(testY) + boxHalfZ * Math.abs(testZ);
        double velocity = movement.x * testX + movement.y * testY + movement.z * testZ;
        double overlap = radius - Math.abs(distance);
        if (overlap >= -SAT_EPSILON) {
            double direction = distance > SAT_EPSILON ? -1 : distance < -SAT_EPSILON ? 1 : velocity > 0 ? -1 : 1;
            double contactX = testX * direction;
            double contactY = testY * direction;
            double contactZ = testZ * direction;
            double intoSurface = movement.x * contactX + movement.y * contactY + movement.z * contactZ;
            double contactDepth = Math.max(0, overlap);
            if (contactDepth < result.contactDepth - SAT_EPSILON ||
                    Math.abs(contactDepth - result.contactDepth) <= SAT_EPSILON && intoSurface < result.contactInto) {
                result.contactDepth = contactDepth;
                result.contactInto = intoSurface;
                result.contactX = contactX;
                result.contactY = contactY;
                result.contactZ = contactZ;
                result.hasContactNormal = true;
            }
        }
        if (Math.abs(velocity) < AXIS_EPSILON) return overlap >= -SAT_EPSILON;

        double first = (distance - radius) / velocity;
        double second = (distance + radius) / velocity;
        if (first > second) {
            double swap = first;
            first = second;
            second = swap;
        }
        if (first > result.entryTime) {
            result.entryTime = first;
            result.normalX = velocity > 0 ? -testX : testX;
            result.normalY = velocity > 0 ? -testY : testY;
            result.normalZ = velocity > 0 ? -testZ : testZ;
            result.hasEntryNormal = true;
        }
        result.exitTime = Math.min(result.exitTime, second);
        if (result.entryTime > result.exitTime + SAT_EPSILON) return false;

        return true;
    }

    private boolean separated(double deltaX, double deltaY, double deltaZ,
                              double boxHalfX, double boxHalfY, double boxHalfZ,
                              double testX, double testY, double testZ) {
        if (testX * testX + testY * testY + testZ * testZ < AXIS_EPSILON) return false;
        double distance = Math.abs(deltaX * testX + deltaY * testY + deltaZ * testZ);
        double radius = halfX * Math.abs(axisXX * testX + axisXY * testY + axisXZ * testZ) +
                halfY * Math.abs(axisYX * testX + axisYY * testY + axisYZ * testZ) +
                halfZ * Math.abs(axisZX * testX + axisZY * testY + axisZZ * testZ) +
                boxHalfX * Math.abs(testX) + boxHalfY * Math.abs(testY) + boxHalfZ * Math.abs(testZ);
        return distance > radius + SAT_EPSILON;
    }

    private static final class MinimumTranslation {

        private double x;
        private double y;
        private double z;
        private double distance = Double.POSITIVE_INFINITY;
    }

    static final class SweepResult {

        private double entryTime;
        private double exitTime;
        private double contactDepth;
        private double contactInto;
        double time;
        double normalX;
        double normalY;
        double normalZ;
        private double contactX;
        private double contactY;
        private double contactZ;
        private boolean hasEntryNormal;
        private boolean hasContactNormal;

        void reset() {
            entryTime = Double.NEGATIVE_INFINITY;
            exitTime = Double.POSITIVE_INFINITY;
            contactDepth = Double.POSITIVE_INFINITY;
            contactInto = Double.POSITIVE_INFINITY;
            time = Double.POSITIVE_INFINITY;
            normalX = 0;
            normalY = 0;
            normalZ = 0;
            contactX = 0;
            contactY = 0;
            contactZ = 0;
            hasEntryNormal = false;
            hasContactNormal = false;
        }

        void copyFrom(SweepResult other) {
            time = other.time;
            normalX = other.normalX;
            normalY = other.normalY;
            normalZ = other.normalZ;
        }
    }
}
