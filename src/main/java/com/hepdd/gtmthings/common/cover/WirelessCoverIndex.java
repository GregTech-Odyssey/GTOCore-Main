package com.hepdd.gtmthings.common.cover;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;

import com.suried.gtoutils.level.LevelStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

public final class WirelessCoverIndex {

    private static final LevelStore<Targets> INDEXES = LevelStore.create(Targets::new);

    private WirelessCoverIndex() {}

    public static void add(Level level, @Nullable GlobalPos target, BlockPos sourcePos, Direction side) {
        if (level.isClientSide || target == null) return;
        INDEXES.get(level).add(target, new Source(sourcePos.immutable(), side));
    }

    public static void remove(Level level, @Nullable GlobalPos target, BlockPos sourcePos, Direction side) {
        if (level.isClientSide || target == null) return;
        var index = INDEXES.getIfPresent(level);
        if (index == null) return;
        index.remove(target, new Source(sourcePos, side));
    }

    public static @Nullable ObjectArrayList<Source> sources(Level level, GlobalPos target) {
        var index = INDEXES.getIfPresent(level);
        return index == null ? null : index.sources(target);
    }

    public record Source(BlockPos pos, Direction side) {}

    static final class Targets {

        private final O2OOpenCacheHashMap<GlobalPos, ObjectArrayList<Source>> sources = new O2OOpenCacheHashMap<>();

        void add(GlobalPos target, Source source) {
            var entries = sources.computeIfAbsent(target, ignored -> new ObjectArrayList<>(1));
            if (!entries.contains(source)) entries.add(source);
        }

        void remove(GlobalPos target, Source source) {
            var entries = sources.get(target);
            if (entries == null) return;
            entries.remove(source);
            if (entries.isEmpty()) sources.remove(target);
        }

        @Nullable
        ObjectArrayList<Source> sources(GlobalPos target) {
            return sources.get(target);
        }
    }
}
