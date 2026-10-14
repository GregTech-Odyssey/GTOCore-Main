package com.gtocore.eio_travel.logic;

import com.gtocore.common.machine.multiblock.part.ae.MEPatternPartMachine;
import com.gtocore.config.GTOConfig;
import com.gtocore.eio_travel.TravelEvents;
import com.gtocore.eio_travel.api.ITravelTarget;
import com.gtocore.eio_travel.api.TravelTargetType;
import com.gtocore.eio_travel.implementations.PatternTravelTarget;
import com.gtocore.eio_travel.network.TravelNetworks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import appeng.helpers.patternprovider.PatternProviderLogicHost;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public interface TravelUtils {

    static Stream<ITravelTarget> filterTargets(Player player, Stream<ITravelTarget> targets) {
        int mask = TravelTargetType.getMask(TravelHandler.getHeldStaff(player));
        return targets.filter(target -> target.getTargetType().isEnabled(mask));
    }

    static Optional<Double> gto$isTeleportPositionAndSurroundingClear(BlockGetter level, BlockPos target) {
        var result = TravelHandler.isTeleportPositionClear(level, target);
        if (result.isPresent()) return result;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos offsetPos = target.relative(dir).below();
            result = TravelHandler.isTeleportPositionClear(level, offsetPos);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    static void removeAndReadd(@NotNull Level level, PatternProviderLogicHost host) {
        Optional<ITravelTarget> travelTarget = TravelSavedData.getTravelData(level).getTravelTarget(host.getBlockEntity().getBlockPos());
        if (travelTarget.isPresent() && travelTarget.get() instanceof ITravelTarget anchorTravelTarget) {
            TravelSavedData.getTravelData(level).removeTravelTargetAt(level, anchorTravelTarget.getPos());
        }
        if (!GTOConfig.INSTANCE.travelConfig.staffOfTravellingPatternNodes) return;
        ITravelTarget anchorTravelTarget = new PatternTravelTarget(host);
        TravelSavedData.getTravelData(level).addTravelTarget(level, anchorTravelTarget);
        requireResync(level);
    }

    static void removeAndReadd(@NotNull Level level, MEPatternPartMachine<?> host) {
        Optional<ITravelTarget> travelTarget = TravelSavedData.getTravelData(level).getTravelTarget(host.getHolder().getBlockPos());
        if (travelTarget.isPresent() && travelTarget.get() instanceof ITravelTarget anchorTravelTarget) {
            TravelSavedData.getTravelData(level).removeTravelTargetAt(level, anchorTravelTarget.getPos());
        }
        if (!GTOConfig.INSTANCE.travelConfig.staffOfTravellingPatternNodes) return;
        ITravelTarget anchorTravelTarget = new PatternTravelTarget(host);
        TravelSavedData.getTravelData(level).addTravelTarget(level, anchorTravelTarget);
        requireResync(level);
    }

    static void requireResync(@NotNull Level level) {
        TravelEvents.syncTask = () -> {
            if (level instanceof ServerLevel serverLevel) {
                TravelNetworks.syncTravelData(TravelSavedData.getTravelData(level).save(new CompoundTag()), serverLevel);
            }
            TravelEvents.syncTask = null;
        };
    }
}
