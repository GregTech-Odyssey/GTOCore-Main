package com.gtocore.api.wireless.energy;

import com.gtolib.api.data.Dimension;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * 电网按星球划分节点：星球维度与它的轨道维度属于同一个节点，共用能源库，对外线路合并计算。
 */
public final class GridBody {

    private GridBody() {}

    public static ResourceKey<Level> of(ResourceKey<Level> dimension) {
        var body = Dimension.getIncludingOrbits(dimension);
        return body == null ? dimension : body.getResourceKey();
    }

    public static boolean same(@Nullable ResourceKey<Level> a, @Nullable ResourceKey<Level> b) {
        if (a == null || b == null) return a == b;
        return of(a) == of(b);
    }
}
