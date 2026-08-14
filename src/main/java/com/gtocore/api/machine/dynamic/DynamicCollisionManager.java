package com.gtocore.api.machine.dynamic;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.gto.datasynclib.datastream.DataComponentKey;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DynamicCollisionManager {

    private static final int COLLISION_SUBDIVISIONS = 3;
    private static final DataComponentKey<CollisionState> STATE = DataComponentKey.createNoCodec("dynamicCollisionState");

    private DynamicCollisionManager() {}

    public static void hidePart(IDynamicStructureMachine machine, String partName) {
        var level = machine.getDynamicLevel();
        if (level != null) machine.visitDynamicBlocks(partName, (symbol, pos) -> {
            hide(level, pos);
            return true;
        });
    }

    public static void showPart(IDynamicStructureMachine machine, String partName) {
        var level = machine.getDynamicLevel();
        if (level != null) machine.visitDynamicBlocks(partName, (symbol, pos) -> {
            show(level, pos);
            return true;
        });
    }

    public static void register(IDynamicStructureMachine machine) {
        var level = machine.getDynamicLevel();
        if (level == null) return;
        get(level).machines.put(machine.getDynamicOrigin().asLong(), machine);
    }

    public static void unregister(IDynamicStructureMachine machine) {
        var level = machine.getDynamicLevel();
        if (level == null) return;
        CollisionState state = getIfPresent(level);
        if (state != null) state.machines.remove(machine.getDynamicOrigin().asLong());
    }

    public static void hide(CollisionGetter level, BlockPos pos) {
        if (level instanceof Level actualLevel) get(actualLevel).hidden.addTo(pos.asLong(), 1);
    }

    public static void show(CollisionGetter level, BlockPos pos) {
        if (!(level instanceof Level actualLevel)) return;
        CollisionState state = getIfPresent(actualLevel);
        if (state == null) return;
        long posLong = pos.asLong();
        int count = state.hidden.get(posLong);
        if (count > 1) {
            state.hidden.put(posLong, count - 1);
        } else {
            state.hidden.remove(posLong);
        }
    }

    public static boolean isHidden(CollisionGetter level, BlockPos pos) {
        if (!(level instanceof Level actualLevel)) return false;
        CollisionState state = getIfPresent(actualLevel);
        return state != null && state.hidden.containsKey(pos.asLong());
    }

    public static List<VoxelShape> getCollisions(CollisionGetter level, @Nullable Entity entity, AABB box) {
        if (!(level instanceof Level actualLevel)) return Collections.emptyList();
        CollisionState state = getIfPresent(actualLevel);
        if (state == null || state.machines.isEmpty()) return Collections.emptyList();

        List<VoxelShape> collisions = new ArrayList<>();
        CollisionContext context = entity == null ? CollisionContext.empty() : CollisionContext.of(entity);
        for (IDynamicStructureMachine machine : state.machines.values()) {
            if (machine.getDynamicLevel() != actualLevel) continue;
            for (DynamicPartDefinition part : machine.getDynamicParts().values()) {
                String partName = part.getName();
                if (!machine.isDynamicPartVisible(partName)) continue;
                Matrix4f transform = machine.getDynamicTransform(partName, 0);
                Matrix4f previousTransform = machine.getDynamicTransform(partName, -1);
                if (entity != null && (isPartSupporting(level, entity, machine, partName, part, previousTransform) ||
                        isPartSupporting(level, entity, machine, partName, part, transform))) {
                    previousTransform = transform;
                }
                addPartCollisions(collisions, level, context, box, machine, partName, part, transform, previousTransform);
            }
        }
        return collisions;
    }

    public static Vec3 getSupportMovement(CollisionGetter level, Entity entity, Vec3 movement) {
        if (movement.y > .05) return Vec3.ZERO;
        if (!(level instanceof Level actualLevel)) return Vec3.ZERO;
        CollisionState state = getIfPresent(actualLevel);
        if (state == null || state.machines.isEmpty()) return Vec3.ZERO;
        long gameTime = entity.level().getGameTime();
        if (state.supportGameTime != gameTime) {
            state.supportGameTime = gameTime;
            state.supportTicks.clear();
        }
        if (state.supportTicks.getOrDefault(entity.getId(), Long.MIN_VALUE) == gameTime) return Vec3.ZERO;

        for (IDynamicStructureMachine machine : state.machines.values()) {
            if (machine.getDynamicLevel() != actualLevel) continue;
            for (DynamicPartDefinition part : machine.getDynamicParts().values()) {
                String partName = part.getName();
                if (!machine.isDynamicPartVisible(partName)) continue;
                Matrix4f transform = machine.getDynamicTransform(partName, 0);
                Matrix4f previousTransform = machine.getDynamicTransform(partName, -1);
                if (!isPartSupporting(level, entity, machine, partName, part, previousTransform) &&
                        !isPartSupporting(level, entity, machine, partName, part, transform)) continue;
                state.supportTicks.put(entity.getId(), gameTime);
                return getMovement(machine.getDynamicOrigin(), entity.position(), transform, previousTransform);
            }
        }
        return Vec3.ZERO;
    }

    private static boolean isPartSupporting(CollisionGetter level, Entity entity, IDynamicStructureMachine machine,
                                            String partName, DynamicPartDefinition part, Matrix4f transform) {
        AABB query = entity.getBoundingBox().inflate(.05, .12, .05).move(0, -.12, 0);
        List<VoxelShape> collisions = new ArrayList<>();
        addPartCollisions(collisions, level, CollisionContext.of(entity), query, machine, partName, part, transform, transform);
        return isSupported(entity.getBoundingBox(), collisions);
    }

    private static void addPartCollisions(List<VoxelShape> collisions, CollisionGetter level, CollisionContext context, AABB box,
                                          IDynamicStructureMachine machine, String partName, DynamicPartDefinition part,
                                          Matrix4f transform, Matrix4f previousTransform) {
        Matrix4f inverse = new Matrix4f(transform).invert();
        Matrix4f previousInverse = new Matrix4f(previousTransform).invert();
        BlockPos origin = machine.getDynamicOrigin();
        AABB localBox = transformBounds(inverse, box.move(-origin.getX(), -origin.getY(), -origin.getZ()));
        localBox = include(localBox, transformBounds(previousInverse, box.move(-origin.getX(), -origin.getY(), -origin.getZ())));
        String[][] structure = part.getStructure();
        int height = structure[0].length;
        int width = structure[0][0].length();
        AABB structureBounds = new AABB(1 - structure.length / 2F, -height / 2F, -width / 2F, structure.length / 2F + 1, height / 2F, width / 2F);
        if (!localBox.intersects(structureBounds)) return;

        int minX = Mth.clamp(Mth.floor(structure.length / 2F + 1 - localBox.maxX) - 1, 0, structure.length - 1);
        int maxX = Mth.clamp(Mth.floor(structure.length / 2F + 1 - localBox.minX) + 1, 0, structure.length - 1);
        int minY = Mth.clamp(Mth.floor(localBox.minY + height / 2F) - 1, 0, height - 1);
        int maxY = Mth.clamp(Mth.floor(localBox.maxY + height / 2F) + 1, 0, height - 1);
        int minZ = Mth.clamp(Mth.floor(localBox.minZ + width / 2F) - 1, 0, width - 1);
        int maxZ = Mth.clamp(Mth.floor(localBox.maxZ + width / 2F) + 1, 0, width - 1);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                String row = structure[x][y];
                for (int z = minZ; z <= maxZ; z++) {
                    char symbol = row.charAt(z);
                    if (symbol == ' ') continue;
                    BlockState state = machine.getDynamicBlockState(partName, symbol);
                    if (state == null) continue;
                    BlockPos sourcePos = machine.getDynamicSourcePos(partName, x, y, z);
                    VoxelShape shape = state.getCollisionShape(level, sourcePos, context);
                    if (shape.isEmpty()) continue;
                    double offsetX = structure.length / 2F - x;
                    double offsetY = -height / 2F + y;
                    double offsetZ = -width / 2F + z;
                    for (AABB shapeBox : shape.toAabbs()) {
                        addTransformedBox(collisions, box, transform, previousTransform, origin, shapeBox.move(offsetX, offsetY, offsetZ));
                    }
                }
            }
        }
    }

    private static void addTransformedBox(List<VoxelShape> collisions, AABB query, Matrix4f transform, Matrix4f previousTransform, BlockPos origin, AABB box) {
        for (int x = 0; x < COLLISION_SUBDIVISIONS; x++) {
            for (int y = 0; y < COLLISION_SUBDIVISIONS; y++) {
                for (int z = 0; z < COLLISION_SUBDIVISIONS; z++) {
                    AABB part = new AABB(
                            Mth.lerp((double) x / COLLISION_SUBDIVISIONS, box.minX, box.maxX),
                            Mth.lerp((double) y / COLLISION_SUBDIVISIONS, box.minY, box.maxY),
                            Mth.lerp((double) z / COLLISION_SUBDIVISIONS, box.minZ, box.maxZ),
                            Mth.lerp((double) (x + 1) / COLLISION_SUBDIVISIONS, box.minX, box.maxX),
                            Mth.lerp((double) (y + 1) / COLLISION_SUBDIVISIONS, box.minY, box.maxY),
                            Mth.lerp((double) (z + 1) / COLLISION_SUBDIVISIONS, box.minZ, box.maxZ));
                    AABB transformed = transformBounds(transform, part).move(origin);
                    transformed = include(transformed, transformBounds(previousTransform, part).move(origin));
                    if (transformed.intersects(query)) collisions.add(Shapes.create(transformed));
                }
            }
        }
    }

    private static AABB transformBounds(Matrix4f transform, AABB box) {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;
        Vector3f point = new Vector3f();
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    point.set((float) (x == 0 ? box.minX : box.maxX), (float) (y == 0 ? box.minY : box.maxY), (float) (z == 0 ? box.minZ : box.maxZ));
                    transform.transformPosition(point);
                    minX = Math.min(minX, point.x);
                    minY = Math.min(minY, point.y);
                    minZ = Math.min(minZ, point.z);
                    maxX = Math.max(maxX, point.x);
                    maxY = Math.max(maxY, point.y);
                    maxZ = Math.max(maxZ, point.z);
                }
            }
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static boolean isSupported(AABB entityBox, List<VoxelShape> collisions) {
        for (VoxelShape shape : collisions) {
            for (AABB box : shape.toAabbs()) {
                double gap = entityBox.minY - box.maxY;
                if (gap < -.05 || gap > .15) continue;
                if (entityBox.maxX > box.minX && entityBox.minX < box.maxX && entityBox.maxZ > box.minZ && entityBox.minZ < box.maxZ) return true;
            }
        }
        return false;
    }

    private static Vec3 getMovement(BlockPos origin, Vec3 position, Matrix4f transform, Matrix4f previousTransform) {
        Vector3f point = new Vector3f((float) (position.x - origin.getX()), (float) (position.y - origin.getY()), (float) (position.z - origin.getZ()));
        new Matrix4f(previousTransform).invert().transformPosition(point);
        Vector3f current = transform.transformPosition(new Vector3f(point));
        Vector3f previous = previousTransform.transformPosition(point);
        return new Vec3(current.x - previous.x, current.y - previous.y, current.z - previous.z);
    }

    private static AABB include(AABB first, AABB second) {
        return new AABB(
                Math.min(first.minX, second.minX),
                Math.min(first.minY, second.minY),
                Math.min(first.minZ, second.minZ),
                Math.max(first.maxX, second.maxX),
                Math.max(first.maxY, second.maxY),
                Math.max(first.maxZ, second.maxZ));
    }

    private static CollisionState get(Level level) {
        CollisionState state = ILevel.getCapability(level, STATE);
        if (state == null) {
            state = new CollisionState();
            ILevel.setCapability(level, STATE, state);
        }
        return state;
    }

    private static CollisionState getIfPresent(Level level) {
        return ILevel.getCapability(level, STATE);
    }

    private static final class CollisionState {

        private final Long2IntOpenHashMap hidden = new Long2IntOpenHashMap();
        private final Long2ObjectOpenHashMap<IDynamicStructureMachine> machines = new Long2ObjectOpenHashMap<>();
        private final Int2LongOpenHashMap supportTicks = new Int2LongOpenHashMap();
        private long supportGameTime = Long.MIN_VALUE;
    }
}
