package com.gtocore.eio_travel.logic;

import com.gtocore.eio_travel.api.ITravelTarget;
import com.gtocore.eio_travel.api.TravelRegistry;
import com.gtocore.eio_travel.network.TravelNetworks;

import com.gtolib.GTOCore;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import com.suried.gtoutils.spatial.BlockIndex;
import com.suried.gtoutils.spatial.BlockRange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class TravelSavedData extends SavedData {

    private static final TravelSavedData CLIENT_INSTANCE = new TravelSavedData();
    public static final String TARGETS = "targets";
    private final Map<BlockPos, ITravelTarget> travelTargets = new HashMap<>();
    private final BlockIndex<ITravelTarget> targetIndex = new BlockIndex<>();

    public TravelSavedData() {}

    public TravelSavedData(CompoundTag nbt) {
        this.loadNBT(nbt);
    }

    public static TravelSavedData getTravelData(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(TravelSavedData::new, TravelSavedData::new, "enderio_traveldata");
        } else {
            return CLIENT_INSTANCE;
        }
    }

    public void loadNBT(CompoundTag nbt) {
        this.travelTargets.clear();
        this.targetIndex.clear();
        ListTag targets = nbt.getList(TARGETS, Tag.TAG_COMPOUND);
        targets.stream().map(anchorData -> (CompoundTag) anchorData)
                .map(TravelRegistry::deserialize)
                .flatMap(Optional::stream)
                .forEach(this::putTarget);
    }

    public Optional<ITravelTarget> getTravelTarget(BlockPos pos) {
        return Optional.ofNullable(travelTargets.get(pos));
    }

    public Stream<ITravelTarget> getTravelTargetsInItemRange(BlockPos center) {
        return getTravelTargetsWithin(center, BlockRange.sphere(TravelHandler.maxTargetRange())).stream()
                .filter(target -> center.distSqr(target.getPos()) < (long) target.getItem2BlockRange() * target.getItem2BlockRange());
    }

    public List<ITravelTarget> getTravelTargetsWithin(BlockPos center, BlockRange range) {
        if (targetIndex.isEmpty()) return Collections.emptyList();
        List<ITravelTarget> found = new ArrayList<>();
        targetIndex.query(center, range, null, found);
        return found;
    }

    private void putTarget(ITravelTarget target) {
        travelTargets.put(target.getPos(), target);
        targetIndex.put(target.getPos(), target);
    }

    public void addTravelTarget(Level level, ITravelTarget target) {
        if (level instanceof ServerLevel serverLevel) {
            TravelNetworks.onServerTravelAdded(serverLevel, target);
        }
        if (TravelRegistry.isRegistered(target)) {
            putTarget(target);
        } else {
            GTOCore.LOGGER.warn("Tried to add a not registered TravelTarget to the TravelSavedData with name {}", target);
        }
    }

    public void removeTravelTargetAt(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            TravelNetworks.onServerTravelRemoved(serverLevel, pos);
        }
        travelTargets.remove(pos);
        targetIndex.remove(pos);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt) {
        ListTag tag = new ListTag();
        tag.addAll(travelTargets.values().stream().map(TravelRegistry::serialize).toList());
        nbt.put(TARGETS, tag);
        return nbt;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
