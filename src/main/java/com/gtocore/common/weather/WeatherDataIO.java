package com.gtocore.common.weather;

import com.gtolib.api.dimension.DimensionDataFixes;
import com.gtolib.utils.iostream.DataIOStream;
import com.gtolib.utils.iostream.IOStreamCodec;
import com.gtolib.utils.iostream.IOStreamCodecs;
import com.gtolib.utils.iostream.IOStreamDecoder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.datastream.data.Data;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.io.IOException;

/** Disk streams use stable weather names; the network retains registry integer IDs. */
final class WeatherDataIO {

    static final int SCHEMA_VERSION = 3;
    static final IOStreamCodec<WeatherType> WEATHER = IOStreamCodec.convert(IOStreamCodecs.UTF8, WeatherType::id,
            WeatherTypes.REGISTRY::get);
    private static final IOStreamCodec<ResourceKey<Level>> DIMENSION = IOStreamCodec.convert(
            IOStreamCodecs.RESOURCE_LOCATION,
            ResourceKey::location, location -> ResourceKey.create(Registries.DIMENSION, location));
    private static final IOStreamDecoder<Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline>> TIMELINES = IOStreamDecoder
            .map(Reference2ObjectOpenHashMap::new, DIMENSION, WeatherTimeline::load);

    record GlobalData(long clock, Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline> timelines) {}

    private WeatherDataIO() {}

    static GlobalData readGlobal(DataIOStream stream) throws IOException {
        return switch (stream.readInt()) {
            case SCHEMA_VERSION -> new GlobalData(stream.readLong(), TIMELINES.decode(stream));
            case 2 -> globalSchema2(DimensionDataFixes.readSchema2(stream));
            default -> throw new IOException("Unsupported weather schema");
        };
    }

    static WeatherTimeline readInstance(DataIOStream stream) throws IOException {
        return switch (stream.readInt()) {
            case SCHEMA_VERSION -> WeatherTimeline.load(stream);
            case 2 -> WeatherTimeline
                    .fromSchema2(DimensionDataFixes.readSchema2(stream).getStringMap().get("timeline"));
            default -> throw new IOException("Unsupported weather schema");
        };
    }

    private static GlobalData globalSchema2(Data data) {
        var map = data.asStringMapData().getStringMap();
        var saved = map.get("timelines");
        var timelines = new Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline>(
                saved == null ? 0 : saved.asListData().size());
        if (saved != null) {
            for (var entry : saved.asListData()) {
                var list = entry.asListData();
                var dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(list.getString(0)));
                timelines.put(dimension, WeatherTimeline.fromSchema2(list.get(1)));
            }
        }
        return new GlobalData(map.get("clock").getLong(), timelines);
    }

    static void writeGlobal(DataIOStream stream, long clock,
                            Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline> timelines) throws IOException {
        stream.writeInt(SCHEMA_VERSION);
        stream.writeLong(clock);
        stream.writeVarInt(timelines.size());
        var iterator = timelines.reference2ObjectEntrySet().fastIterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            DIMENSION.encode(stream, entry.getKey());
            entry.getValue().save(stream);
        }
    }

    static void writeInstance(DataIOStream stream, WeatherTimeline timeline) throws IOException {
        stream.writeInt(SCHEMA_VERSION);
        timeline.save(stream);
    }
}
