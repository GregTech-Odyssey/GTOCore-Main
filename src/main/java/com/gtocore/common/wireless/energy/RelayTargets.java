package com.gtocore.common.wireless.energy;

import com.gtocore.api.wireless.energy.GridBody;

import com.gtolib.api.data.Dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.Nullable;

/**
 * 中继可选的目标：GTO 登记的全部星球与异界维度（轨道并入所属星球），下标 0 为不连接；排除中继自身所在的星球。
 */
public final class RelayTargets {

    private static final ObjectArrayList<ResourceKey<Level>> ALL = new ObjectArrayList<>();

    static {
        for (var dimension : Dimension.all()) ALL.add(dimension.getResourceKey());
    }

    private RelayTargets() {}

    public static ObjectList<ResourceKey<Level>> options(ResourceKey<Level> self) {
        var body = GridBody.of(self);
        var options = new ObjectArrayList<ResourceKey<Level>>(ALL.size() + 1);
        options.add(null);
        for (var key : ALL) {
            if (key != body) options.add(key);
        }
        return ObjectLists.unmodifiable(options);
    }

    @Nullable
    public static ResourceKey<Level> parse(String location) {
        if (location.isEmpty()) return null;
        var parsed = ResourceLocation.tryParse(location);
        if (parsed == null) return null;
        var key = GridBody.of(ResourceKey.create(Registries.DIMENSION, parsed));
        return ALL.contains(key) ? key : null;
    }
}
