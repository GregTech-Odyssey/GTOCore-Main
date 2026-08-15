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
import net.minecraft.world.phys.shapes.VoxelShape;

import com.gto.datasynclib.datastream.DataComponentKey;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class DynamicCollisionManager {

    private static final int COLLISION_STEPS = 8;
    private static final double PENETRATION_RECOVERY = .004;
    private static final double CONTACT_NORMAL_MATCH = .65;
    private static final DataComponentKey<CollisionState> STATE = DataComponentKey.createNoCodec("dynamicCollisionState");
    private static final ThreadLocal<CollisionScratch> SCRATCH = ThreadLocal.withInitial(CollisionScratch::new);

    private DynamicCollisionManager() {}

    public static void hidePart(IDynamicStructureMachine machine, String partName) {
        if (!machine.isDynamicStructureEnabled()) return;
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
        if (!machine.isDynamicStructureEnabled()) return;
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

    public static Vec3 getCollisionMovement(CollisionGetter level, Entity entity, Vec3 movement) {
        CollisionScratch scratch = SCRATCH.get();
        scratch.resetMovement(entity.getId());
        if (entity.noPhysics || entity.isSpectator()) return Vec3.ZERO;
        Vec3 supportMovement = getSupportMovement(level, entity, movement);
        Vec3 addedMovement = supportMovement.add(getPushMovement(level, entity));
        scratch.requestedMovement = movement.add(addedMovement);
        return addedMovement;
    }

    public static Vec3 collideMovement(CollisionGetter level, Entity entity, Vec3 requestedMovement, Vec3 movement) {
        CollisionScratch scratch = SCRATCH.get();
        boolean hasMovementContext = scratch.movementEntityId == entity.getId() &&
                scratch.requestedMovement.distanceToSqr(requestedMovement) < 1E-12;
        IDynamicStructureMachine supportMachine = hasMovementContext ? scratch.supportMachine : null;
        String supportPart = hasMovementContext ? scratch.supportPart : null;
        Vec3 supportMovement = hasMovementContext ? scratch.supportMovement : Vec3.ZERO;
        Vec3 supportNormal = hasMovementContext ? scratch.supportNormal : null;
        scratch.resetMovement(Integer.MIN_VALUE);
        if (movement.lengthSqr() < 1E-14 || entity.noPhysics || entity.isSpectator()) return movement;
        if (!(level instanceof Level actualLevel)) return movement;
        CollisionState state = getIfPresent(actualLevel);
        if (state == null || state.machines.isEmpty()) return movement;

        Vec3 result = movement;
        CollisionContext context = CollisionContext.of(entity);
        for (IDynamicStructureMachine machine : state.machines.values()) {
            if (machine.getDynamicLevel() != actualLevel) continue;
            for (DynamicPartDefinition part : machine.getDynamicParts().values()) {
                String partName = part.getName();
                if (!machine.isDynamicPartVisible(partName)) continue;
                if (machine == supportMachine && partName.equals(supportPart)) {
                    Vec3 relativeMovement = result.subtract(supportMovement);
                    result = supportMovement.add(collidePart(level, context,
                            entity.getBoundingBox().move(supportMovement), relativeMovement, machine, partName, part,
                            entity.maxUpStep(), supportNormal, entity));
                } else {
                    result = collidePart(level, context, entity.getBoundingBox(), result, machine, partName, part, 0, null, entity);
                }
                if (result.lengthSqr() < 1E-14) return Vec3.ZERO;
            }
        }
        return result;
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
            if ((gameTime & 31) == 0) state.contacts.values().removeIf(contact -> contact.gameTime < gameTime - 2);
        }
        if (state.supportTicks.getOrDefault(entity.getId(), Long.MIN_VALUE) == gameTime) return Vec3.ZERO;

        for (IDynamicStructureMachine machine : state.machines.values()) {
            if (machine.getDynamicLevel() != actualLevel) continue;
            for (DynamicPartDefinition part : machine.getDynamicParts().values()) {
                String partName = part.getName();
                if (!machine.isDynamicPartVisible(partName)) continue;
                Matrix4f transform = machine.getDynamicTransform(partName, 0);
                Matrix4f previousTransform = machine.getDynamicTransform(partName, -1);
                Vec3 supportNormal = getSupportNormal(level, entity, machine, partName, part, transform);
                if (supportNormal == null) supportNormal = getSupportNormal(level, entity, machine, partName, part, previousTransform);
                if (supportNormal == null) continue;
                state.supportTicks.put(entity.getId(), gameTime);
                Vec3 supportMovement = getMovement(machine.getDynamicOrigin(), entity.position(), transform, previousTransform);
                supportNormal = state.contacts.computeIfAbsent(entity.getId(), ignored -> new ContactState())
                        .update(machine.getDynamicOrigin().asLong(), partName, supportNormal, gameTime);
                CollisionScratch scratch = SCRATCH.get();
                scratch.supportMachine = machine;
                scratch.supportPart = partName;
                scratch.supportMovement = supportMovement;
                scratch.supportNormal = supportNormal;
                return supportMovement;
            }
        }
        return Vec3.ZERO;
    }

    private static Vec3 getPushMovement(CollisionGetter level, Entity entity) {
        if (!(level instanceof Level actualLevel)) return Vec3.ZERO;
        CollisionState state = getIfPresent(actualLevel);
        if (state == null || state.machines.isEmpty()) return Vec3.ZERO;
        long gameTime = entity.level().getGameTime();
        if (state.pushGameTime != gameTime) {
            state.pushGameTime = gameTime;
            state.pushTicks.clear();
        }
        if (state.pushTicks.getOrDefault(entity.getId(), Long.MIN_VALUE) == gameTime) return Vec3.ZERO;

        AABB entityBox = entity.getBoundingBox();
        AABB query = entityBox.inflate(2);
        CollisionContext context = CollisionContext.of(entity);
        CollisionScratch scratch = SCRATCH.get();
        IDynamicStructureMachine supportMachine = scratch.supportMachine;
        String supportPart = scratch.supportPart;
        List<MovingBox> boxes = new ArrayList<>();
        for (IDynamicStructureMachine machine : state.machines.values()) {
            if (machine.getDynamicLevel() != actualLevel) continue;
            for (DynamicPartDefinition part : machine.getDynamicParts().values()) {
                String partName = part.getName();
                if (!machine.isDynamicPartVisible(partName)) continue;
                Matrix4f[] transforms = new Matrix4f[COLLISION_STEPS + 1];
                for (int step = 0; step <= COLLISION_STEPS; step++) {
                    transforms[step] = machine.getDynamicTransform(partName, -1 + (float) step / COLLISION_STEPS);
                }
                collectMovingBoxes(boxes, level, context, query, machine, partName, part, transforms,
                        machine == supportMachine && partName.equals(supportPart));
            }
        }
        if (boxes.isEmpty()) return Vec3.ZERO;

        Vec3 correction = Vec3.ZERO;
        for (MovingBox box : boxes) {
            boolean previousContact = box.previous().intersects(entityBox.inflate(.02));
            for (int step = 1; step <= COLLISION_STEPS; step++) {
                AABB stepBox = entityBox.move(correction);
                OrientedBox previous = box.samples[step - 1];
                OrientedBox current = box.samples[step];
                Vec3 segmentMovement = current.pointMovementFrom(previous, stepBox.getCenter());
                if (!box.support && segmentMovement.lengthSqr() > 1E-14 &&
                        previous.sweep(stepBox, segmentMovement.scale(-1), scratch.tested)) {
                    double pressure = segmentMovement.x * scratch.tested.normalX +
                            segmentMovement.y * scratch.tested.normalY +
                            segmentMovement.z * scratch.tested.normalZ;
                    if (pressure > 1E-6) {
                        double remaining = 1 - Mth.clamp(scratch.tested.time, 0, 1);
                        correction = correction.add(
                                scratch.tested.normalX * pressure * remaining,
                                scratch.tested.normalY * pressure * remaining,
                                scratch.tested.normalZ * pressure * remaining);
                        break;
                    }
                }
                Vec3 minimumTranslation = current.getMinimumTranslation(stepBox, box.samples[step - 1]);
                if (minimumTranslation == null) continue;
                Vec3 normal = minimumTranslation.normalize();
                if (box.support) {
                    if (minimumTranslation.lengthSqr() > PENETRATION_RECOVERY * PENETRATION_RECOVERY)
                        correction = correction.add(minimumTranslation);
                } else if (previousContact) {
                    Vec3 surfaceMovement = current.pointMovementFrom(box.samples[step - 1], stepBox.getCenter());
                    if (surfaceMovement.dot(normal) > 1E-6 || minimumTranslation.lengthSqr() > PENETRATION_RECOVERY * PENETRATION_RECOVERY)
                        correction = correction.add(minimumTranslation);
                } else {
                    Vec3 surfaceMovement = box.current().pointMovementFrom(current, stepBox.getCenter());
                    double intoSurface = surfaceMovement.dot(normal);
                    if (intoSurface > 0) correction = correction.add(normal.scale(intoSurface));
                }
                break;
            }
        }
        for (int pass = 0; pass < 8; pass++) {
            Vec3 minimumTranslation = null;
            for (MovingBox box : boxes) {
                Vec3 candidate = box.current().getMinimumTranslation(entityBox.move(correction), box.previous());
                if (candidate != null && candidate.lengthSqr() <= PENETRATION_RECOVERY * PENETRATION_RECOVERY) {
                    if (box.support || box.previous().intersects(entityBox.inflate(.02)) &&
                            box.current().pointMovementFrom(box.previous(), entityBox.move(correction).getCenter())
                                    .dot(candidate.normalize()) <= 1E-6)
                        continue;
                }
                if (candidate != null && (minimumTranslation == null || candidate.lengthSqr() < minimumTranslation.lengthSqr())) {
                    minimumTranslation = candidate;
                }
            }
            if (minimumTranslation == null) break;
            correction = correction.add(minimumTranslation);
        }
        if (correction.lengthSqr() > 0) state.pushTicks.put(entity.getId(), gameTime);
        return correction;
    }

    private static Vec3 getSupportNormal(CollisionGetter level, Entity entity, IDynamicStructureMachine machine,
                                         String partName, DynamicPartDefinition part, Matrix4f transform) {
        CollisionScratch scratch = SCRATCH.get();
        Matrix4f inverse = new Matrix4f(transform).invert();
        BlockPos origin = machine.getDynamicOrigin();
        AABB entityBox = entity.getBoundingBox().move(-origin.getX(), -origin.getY(), -origin.getZ());
        OrientedBox localEntity = new OrientedBox(inverse, BlockPos.ZERO).set(entityBox);
        Vec3 localMovement = transformDirection(inverse, new Vec3(0, -.15, 0), scratch.vector);
        collectLocalBoxes(scratch, level, CollisionContext.of(entity),
                include(localEntity.bounds(), localEntity.bounds().move(localMovement)).inflate(.05), machine, partName, part);
        if (!findEarliestCollision(scratch, localEntity, localMovement)) return null;
        Vec3 normal = transformDirection(transform,
                new Vec3(scratch.best.normalX, scratch.best.normalY, scratch.best.normalZ), scratch.vector);
        return normal.y > .5 ? normal.normalize() : null;
    }

    private static void collectMovingBoxes(List<MovingBox> boxes, CollisionGetter level, CollisionContext context, AABB query,
                                           IDynamicStructureMachine machine, String partName, DynamicPartDefinition part, Matrix4f[] transforms,
                                           boolean support) {
        BlockPos origin = machine.getDynamicOrigin();
        Matrix4f previousTransform = transforms[0];
        Matrix4f transform = transforms[COLLISION_STEPS];
        visitPartBoxes(level, context, query, machine, partName, part, transform, previousTransform, shapeBox -> {
            OrientedBox[] samples = new OrientedBox[transforms.length];
            for (int step = 0; step < transforms.length; step++) {
                samples[step] = new OrientedBox(transforms[step], origin).set(shapeBox);
            }
            AABB bounds = samples[0].bounds();
            for (int step = 1; step < samples.length; step++) bounds = include(bounds, samples[step].bounds());
            if (bounds.intersects(query)) boxes.add(new MovingBox(samples, support));
        });
    }

    private static void visitPartBoxes(CollisionGetter level, CollisionContext context, AABB box,
                                       IDynamicStructureMachine machine, String partName, DynamicPartDefinition part,
                                       Matrix4f transform, Matrix4f previousTransform, Consumer<AABB> consumer) {
        Matrix4f inverse = new Matrix4f(transform).invert();
        Matrix4f previousInverse = new Matrix4f(previousTransform).invert();
        BlockPos origin = machine.getDynamicOrigin();
        AABB localBox = transformBounds(inverse, box.move(-origin.getX(), -origin.getY(), -origin.getZ()));
        localBox = include(localBox, transformBounds(previousInverse, box.move(-origin.getX(), -origin.getY(), -origin.getZ())));
        visitLocalPartBoxes(level, context, localBox, machine, partName, part, consumer);
    }

    private static void visitLocalPartBoxes(CollisionGetter level, CollisionContext context, AABB localBox,
                                            IDynamicStructureMachine machine, String partName, DynamicPartDefinition part,
                                            Consumer<AABB> consumer) {
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
                        consumer.accept(shapeBox.move(offsetX, offsetY, offsetZ));
                    }
                }
            }
        }
    }

    private static Vec3 collidePart(CollisionGetter level, CollisionContext context, AABB entityBox, Vec3 movement,
                                    IDynamicStructureMachine machine, String partName, DynamicPartDefinition part,
                                    float stepHeight, Vec3 contactNormal, Entity entity) {
        CollisionScratch scratch = SCRATCH.get();
        Matrix4f transform = machine.getDynamicTransform(partName, 0);
        Matrix4f inverse = new Matrix4f(transform).invert();
        BlockPos origin = machine.getDynamicOrigin();
        AABB relativeBox = entityBox.move(-origin.getX(), -origin.getY(), -origin.getZ());
        Vec3 localMovement = transformDirection(inverse, movement, scratch.vector);
        OrientedBox localEntity = new OrientedBox(inverse, BlockPos.ZERO).set(relativeBox);
        AABB query = include(localEntity.bounds(), localEntity.bounds().move(localMovement)).inflate(.25 + stepHeight);
        collectLocalBoxes(scratch, level, context, query, machine, partName, part);
        if (scratch.boxes.isEmpty()) return movement;

        Vec3 localAllowed = collideLocal(scratch, localEntity, localMovement, transform, inverse, contactNormal, stepHeight);
        Vec3 result = transformDirection(transform, localAllowed, scratch.vector);
        if (contactNormal != null && level instanceof Level actualLevel &&
                !actualLevel.noCollision(entity, entityBox.move(result).deflate(1E-6))) {
            localEntity = new OrientedBox(inverse, BlockPos.ZERO).set(relativeBox);
            localAllowed = collideLocal(scratch, localEntity, localMovement, transform, inverse, null, 0);
            result = transformDirection(transform, localAllowed, scratch.vector);
        }
        return result.distanceToSqr(movement) < 1E-12 ? movement : result;
    }

    private static Vec3 collideLocal(CollisionScratch scratch, OrientedBox localEntity, Vec3 movement,
                                     Matrix4f transform, Matrix4f inverse, Vec3 contactNormal, float stepHeight) {
        Vec3 allowed = Vec3.ZERO;
        Vec3 remaining = movement;
        for (int pass = 0; pass < 3; pass++) {
            if (!findEarliestCollision(scratch, localEntity, remaining)) {
                allowed = allowed.add(remaining);
                remaining = Vec3.ZERO;
                break;
            }
            double collisionTime = Mth.clamp(scratch.best.time, 0, 1);
            double advanceTime = Math.max(0, collisionTime - 1E-6);
            Vec3 advance = remaining.scale(advanceTime);
            allowed = allowed.add(advance);
            localEntity.move(advance);
            remaining = remaining.scale(1 - collisionTime);
            double normalX = scratch.best.normalX;
            double normalY = scratch.best.normalY;
            double normalZ = scratch.best.normalZ;
            Vec3 supportNormal = null;
            if (contactNormal != null) {
                Vec3 worldNormal = transformDirection(transform, new Vec3(normalX, normalY, normalZ), scratch.vector).normalize();
                if (worldNormal.y > .45 && worldNormal.dot(contactNormal) > CONTACT_NORMAL_MATCH) {
                    supportNormal = contactNormal;
                    Vec3 localNormal = transformDirection(inverse, contactNormal, scratch.vector).normalize();
                    normalX = localNormal.x;
                    normalY = localNormal.y;
                    normalZ = localNormal.z;
                }
            }
            double intoSurface = remaining.x * normalX + remaining.y * normalY + remaining.z * normalZ;
            if (intoSurface < 0) {
                if (supportNormal != null && stepHeight > 0) {
                    Vec3 worldRemaining = transformDirection(transform, remaining, scratch.vector);
                    double surfaceY = -(worldRemaining.x * supportNormal.x + worldRemaining.z * supportNormal.z) / supportNormal.y;
                    double rise = surfaceY - worldRemaining.y;
                    if (rise <= stepHeight + 1E-6) {
                        remaining = transformDirection(inverse,
                                new Vec3(worldRemaining.x, surfaceY, worldRemaining.z), scratch.vector);
                    } else {
                        remaining = remaining.subtract(normalX * intoSurface, normalY * intoSurface, normalZ * intoSurface);
                    }
                } else {
                    remaining = remaining.subtract(normalX * intoSurface, normalY * intoSurface, normalZ * intoSurface);
                }
            }
            if (remaining.lengthSqr() < 1E-14) {
                remaining = Vec3.ZERO;
                break;
            }
        }
        return allowed;
    }

    private static void collectLocalBoxes(CollisionScratch scratch, CollisionGetter level, CollisionContext context, AABB query,
                                          IDynamicStructureMachine machine, String partName, DynamicPartDefinition part) {
        scratch.boxes.clear();
        visitLocalPartBoxes(level, context, query, machine, partName, part, scratch.collector);
    }

    private static boolean findEarliestCollision(CollisionScratch scratch, OrientedBox box, Vec3 movement) {
        scratch.best.reset();
        for (int i = 0; i < scratch.boxes.size(); i++) {
            if (!box.sweep(scratch.boxes.get(i), movement, scratch.tested)) continue;
            double testedInto = movement.x * scratch.tested.normalX + movement.y * scratch.tested.normalY + movement.z * scratch.tested.normalZ;
            double bestInto = movement.x * scratch.best.normalX + movement.y * scratch.best.normalY + movement.z * scratch.best.normalZ;
            if (scratch.tested.time < scratch.best.time - 1E-7 ||
                    Math.abs(scratch.tested.time - scratch.best.time) <= 1E-7 && testedInto < bestInto) {
                scratch.best.copyFrom(scratch.tested);
            }
        }
        return scratch.best.time <= 1;
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

    private static Vec3 transformDirection(Matrix4f transform, Vec3 value, Vector3f vector) {
        vector.set((float) value.x, (float) value.y, (float) value.z);
        transform.transformDirection(vector);
        return new Vec3(vector.x, vector.y, vector.z);
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

    private record MovingBox(OrientedBox[] samples, boolean support) {

        private OrientedBox current() {
            return samples[COLLISION_STEPS];
        }

        private OrientedBox previous() {
            return samples[0];
        }
    }

    private static final class ContactState {

        private long machine;
        private String part;
        private Vec3 normal = Vec3.ZERO;
        private long gameTime = Long.MIN_VALUE;

        private Vec3 update(long machine, String part, Vec3 normal, long gameTime) {
            if (this.machine == machine && part.equals(this.part) && this.gameTime >= gameTime - 2 &&
                    this.normal.dot(normal) > CONTACT_NORMAL_MATCH) {
                normal = this.normal.scale(.7).add(normal.scale(.3)).normalize();
            }
            this.machine = machine;
            this.part = part;
            this.normal = normal;
            this.gameTime = gameTime;
            return normal;
        }
    }

    private static final class CollisionScratch {

        private final ArrayList<AABB> boxes = new ArrayList<>(64);
        private final Consumer<AABB> collector = boxes::add;
        private final OrientedBox.SweepResult tested = new OrientedBox.SweepResult();
        private final OrientedBox.SweepResult best = new OrientedBox.SweepResult();
        private final Vector3f vector = new Vector3f();
        private int movementEntityId = Integer.MIN_VALUE;
        private IDynamicStructureMachine supportMachine;
        private String supportPart;
        private Vec3 supportMovement = Vec3.ZERO;
        private Vec3 supportNormal;
        private Vec3 requestedMovement = Vec3.ZERO;

        private void resetMovement(int entityId) {
            movementEntityId = entityId;
            supportMachine = null;
            supportPart = null;
            supportMovement = Vec3.ZERO;
            supportNormal = null;
            requestedMovement = Vec3.ZERO;
        }
    }

    private static final class CollisionState {

        private final Long2IntOpenHashMap hidden = new Long2IntOpenHashMap();
        private final Long2ObjectOpenHashMap<IDynamicStructureMachine> machines = new Long2ObjectOpenHashMap<>();
        private final Int2LongOpenHashMap supportTicks = new Int2LongOpenHashMap();
        private final Int2LongOpenHashMap pushTicks = new Int2LongOpenHashMap();
        private final Int2ObjectOpenHashMap<ContactState> contacts = new Int2ObjectOpenHashMap<>();
        private long supportGameTime = Long.MIN_VALUE;
        private long pushGameTime = Long.MIN_VALUE;
    }
}
