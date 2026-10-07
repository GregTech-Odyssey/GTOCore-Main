package com.gtocore.common.weather;

import com.gtolib.api.dimension.DimensionDataComponents;
import com.gtolib.api.dimension.DimensionDataIO;
import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.DataSyncCodec;
import com.gto.datasynclib.datastream.DataComponentKey;
import com.gto.datasynclib.datastream.DataComponentMap;
import com.gto.datasynclib.datastream.DataComponentRegistry;
import com.gto.datasynclib.datastream.codec.DataCodec;
import com.gto.datasynclib.datastream.data.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.io.EOFException;
import java.io.IOException;

/** Disk-only weather components written directly to FastSavedData binary streams. */
final class WeatherDataComponents {

    private static final int MAGIC = 0x47545743; // GTWC
    private static final int MAX_PAYLOAD_BYTES = 64 * 1024 * 1024;
    static final int SCHEMA_VERSION = 2;
    static final DataComponentRegistry TIMELINE = registry("weather_timeline");
    static final DataComponentRegistry GLOBAL = registry("global_weather");
    static final DataComponentRegistry INSTANCE = registry("instance_weather");

    private static final DataCodec<WeatherTimeline.Period> PERIOD_CODEC = DataCodec.of(period -> {
        var tuple = new ListData(3);
        tuple.add(WeatherTypes.REGISTRY.dataCodec().encode(period.weather()));
        tuple.addLong(period.start());
        tuple.addLong(period.end());
        return tuple;
    }, (data, version) -> {
        var tuple = data.asListData();
        if (tuple.size() != 3) throw new IllegalArgumentException("Invalid weather period tuple");
        var weather = WeatherTypes.REGISTRY.dataCodec().decode((StringData) tuple.get(0), version);
        if (weather == null) throw new IllegalArgumentException("Unknown saved weather type");
        return new WeatherTimeline.Period(weather, ((LongData) tuple.get(1)).value(), ((LongData) tuple.get(2)).value());
    });
    static final DataCodec<WeatherTimeline> TIMELINE_CODEC = DataCodec.of(value -> TIMELINE.encode(value.save()),
            (data, version) -> WeatherTimeline.load(DimensionDataComponents.decode(TIMELINE, data, version)));

    static final DataComponentKey<Long> RANDOM_STATE = register(TIMELINE, "random_state", DimensionDataComponents.LONG_CODEC);
    static final DataComponentKey<Long> LAST_STORM_END = register(TIMELINE, "last_storm_end", DimensionDataComponents.LONG_CODEC);
    static final DataComponentKey<ObjectArrayList<WeatherTimeline.Period>> PERIODS = register(TIMELINE, "periods", DataCodec.of(periods -> {
        var list = new ListData(periods.size());
        for (var period : periods) list.add(PERIOD_CODEC.encode(period));
        return list;
    }, (data, version) -> {
        var list = data.asListData();
        var periods = new ObjectArrayList<WeatherTimeline.Period>(list.size());
        for (var entry : list) {
            var period = PERIOD_CODEC.decode(entry, version);
            if (!periods.isEmpty() && periods.getLast().end() != period.start()) throw new IllegalArgumentException("Non-contiguous weather schedule");
            periods.add(period);
        }
        return periods;
    }));
    static final DataComponentKey<Long> CLOCK = register(GLOBAL, "clock", DimensionDataComponents.LONG_CODEC);
    static final DataComponentKey<Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline>> TIMELINES = register(GLOBAL, "timelines", DataCodec.of(timelines -> {
        var list = new ListData(timelines.size());
        timelines.forEach((key, value) -> {
            var tuple = new ListData(2);
            tuple.addString(key.location().toString());
            tuple.add(TIMELINE_CODEC.encode(value));
            list.add(tuple);
        });
        return list;
    }, (data, version) -> {
        var list = data.asListData();
        var timelines = new Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline>(list.size());
        for (var entry : list) {
            var tuple = entry.asListData();
            if (tuple.size() != 2) throw new IllegalArgumentException("Invalid dimension weather tuple");
            var key = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(((StringData) tuple.get(0)).value()));
            if (timelines.put(key, TIMELINE_CODEC.decode(tuple.get(1), version)) != null) throw new IllegalArgumentException("Duplicate dimension weather key");
        }
        return timelines;
    }));
    static final DataComponentKey<WeatherTimeline> INSTANCE_TIMELINE = register(INSTANCE, "timeline", TIMELINE_CODEC);

    static {
        TIMELINE.freeze();
        GLOBAL.freeze();
        INSTANCE.freeze();
    }

    private WeatherDataComponents() {}

    private static DataComponentRegistry registry(String name) {
        var registry = new DataComponentRegistry(name);
        registry.unfreeze();
        return registry;
    }

    private static <T> DataComponentKey<T> register(DataComponentRegistry registry, String name, DataCodec<T> codec) {
        return registry.register(name, DataSyncCodec.of(codec));
    }

    static DataComponentMap read(DataIOStream stream, DataComponentRegistry registry) throws IOException {
        if (stream.readInt() != MAGIC) throw new IOException("Unknown weather file format");
        if (stream.readUnsignedByte() != kind(registry)) throw new IOException("Weather file kind mismatch");
        if (stream.readInt() != SCHEMA_VERSION) throw new IOException("Unsupported weather component schema");
        int dataVersion = stream.readInt();
        int length = stream.readInt();
        if (length < 1 || length > MAX_PAYLOAD_BYTES) throw new IOException("Invalid weather payload length");
        byte[] bytes = new byte[length];
        stream.readFully(bytes);
        try {
            stream.readByte();
        } catch (EOFException expected) {
            return DimensionDataComponents.decode(registry, DimensionDataIO.readPayload(bytes), dataVersion);
        }
        throw new IOException("Trailing weather payload");
    }

    static void write(DataIOStream stream, DataComponentRegistry registry, DataComponentMap map) throws IOException {
        byte[] bytes = registry.encode(map).writeToBytes();
        if (bytes.length > MAX_PAYLOAD_BYTES) throw new IOException("Weather component payload exceeds size limit");
        stream.writeInt(MAGIC);
        stream.writeByte(kind(registry));
        stream.writeInt(SCHEMA_VERSION);
        stream.writeInt(currentDataVersion());
        stream.writeInt(bytes.length);
        stream.write(bytes);
    }

    private static int kind(DataComponentRegistry registry) {
        return registry == GLOBAL ? 1 : 2;
    }

    static int currentDataVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }
}
