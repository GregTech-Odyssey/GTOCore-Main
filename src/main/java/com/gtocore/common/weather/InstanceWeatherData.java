package com.gtocore.common.weather;

import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.api.dimension.InstanceDescriptor;
import com.gtolib.api.dimension.InstanceStore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Each dynamic instance owns its weather schedule; dormant history is never held in the global weather map. */
final class InstanceWeatherData extends SavedData {

    private static final String NAME = "gtocore_instance_weather";
    private final WeatherTimeline timeline;

    private InstanceWeatherData(WeatherTimeline timeline) {
        this.timeline = timeline;
    }

    static WeatherTimeline timeline(MinecraftServer server, InstanceDescriptor descriptor, long clock, WeatherProfile profile) {
        var level = server.getLevel(descriptor.dimension());
        DimensionDataStorage storage = level == null ? new DimensionDataStorage(DimensionManager.get(server).dimensionPath(descriptor.dimension()).resolve("data").toFile(), server.getFixerUpper()) : level.getDataStorage();
        var data = storage.computeIfAbsent(tag -> new InstanceWeatherData(WeatherTimeline.load(tag.getCompound("timeline"))),
                () -> new InstanceWeatherData(new WeatherTimeline(descriptor.seed())), NAME);
        data.timeline.extend(profile, clock);
        data.setDirty();
        if (level == null) {
            var tag = NbtUtils.addCurrentDataVersion(new CompoundTag());
            tag.put("data", data.save(new CompoundTag()));
            try {
                InstanceStore.writeAtomic(storage.getDataFile(NAME).toPath(), tag);
            } catch (IOException exception) {
                throw new UncheckedIOException("Cannot save dormant instance weather", exception);
            }
            data.setDirty(false);
        }
        return data.timeline;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put("timeline", timeline.save());
        return tag;
    }
}
