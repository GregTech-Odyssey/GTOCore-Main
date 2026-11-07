/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.mana.spark;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class SparkHelper {

    public static final int SPARK_SCAN_RANGE = 12;

    public static List<ManaSpark> getSparksAround(Level world, double x, double y, double z, DyeColor color) {
        // int r = SPARK_SCAN_RANGE;
        // AABB box = new AABB(x - r, y - r, z - r, x + r, y + r, z + r);
        // var index = ManaSparkIndex.of(world);
        // if (index != null) {
        // List<ManaSpark> sparks = new ArrayList<>();
        // index.collect(box, color, sparks);
        // return sparks;
        // }
        // Predicate<Entity> predicate = e -> e instanceof ManaSpark spark && spark.getNetwork() == color;
        // @SuppressWarnings("unchecked")
        // List<ManaSpark> entities = (List<ManaSpark>) (List<?>) world.getEntitiesOfClass(Entity.class, box,
        // predicate);
        // return entities;
        return new ArrayList<>();
    }

    @Nullable
    public static ManaSpark getAttachedSpark(Level world, BlockPos pos) {
        // AABB box = new AABB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
        // var index = ManaSparkIndex.of(world);
        // if (index != null) {
        // return index.findSingle(box);
        // }
        // List<Entity> sparks = world.getEntitiesOfClass(Entity.class, box, e -> e instanceof ManaSpark);
        // return sparks.size() == 1 ? (ManaSpark) sparks.get(0) : null;
        return null;
    }
}
