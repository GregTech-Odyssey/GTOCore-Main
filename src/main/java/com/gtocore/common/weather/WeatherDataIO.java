package com.gtocore.common.weather;

import com.gtolib.api.dimension.DimensionDataCodecs;
import com.gtolib.api.dimension.DimensionDataFixes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.gto.datasynclib.datastream.codec.ByteBufCodecs;
import com.gto.datasynclib.datastream.codec.JavaValueOps;
import com.gto.datasynclib.datastream.codec.StreamCodec;
import com.gto.datasynclib.datastream.codec.StreamDecoder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

/** Disk streams use stable weather names; the network retains registry integer IDs. */
final class WeatherDataIO {

    private static final JavaValueOps OPS = JavaValueOps.INSTANCE;

    static final int SCHEMA_VERSION = 3;
    static final StreamCodec<FriendlyByteBuf, WeatherType> WEATHER = StreamCodec.convert(DimensionDataCodecs.UTF8, WeatherType::id,
            WeatherTypes.REGISTRY::get);
    private static final StreamCodec<FriendlyByteBuf, ResourceKey<Level>> DIMENSION = StreamCodec.convert(
            DimensionDataCodecs.RESOURCE_LOCATION,
            ResourceKey::location, location -> ResourceKey.create(Registries.DIMENSION, location));
    private static final StreamDecoder<FriendlyByteBuf, Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline>> TIMELINES = ByteBufCodecs
            .map(Reference2ObjectOpenHashMap::new, DIMENSION, StreamCodec.of((buffer, timeline) -> timeline.save(buffer), WeatherTimeline::load));

    record GlobalData(long clock, Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline> timelines) {}

    private WeatherDataIO() {}

    static GlobalData readGlobal(FriendlyByteBuf stream) {
        return switch (readSchema(stream, 1)) {
            case SCHEMA_VERSION -> new GlobalData(stream.readLong(), TIMELINES.decode(stream));
            case 2 -> globalSchema2(DimensionDataFixes.readSchema2(stream));
            default -> throw new IllegalArgumentException("Unsupported weather schema");
        };
    }

    static WeatherTimeline readInstance(FriendlyByteBuf stream) {
        return switch (readSchema(stream, 2)) {
            case SCHEMA_VERSION -> WeatherTimeline.load(stream);
            case 2 -> WeatherTimeline
                    .fromSchema2(OPS.getStringMap(DimensionDataFixes.readSchema2(stream)).get("timeline"));
            default -> throw new IllegalArgumentException("Unsupported weather schema");
        };
    }

    private static int readSchema(FriendlyByteBuf stream, int kind) {
        int schema = stream.readInt();
        if (schema != 0x47545743) return schema;
        if (stream.readUnsignedByte() != kind) throw new IllegalArgumentException("Invalid weather data kind");
        return stream.readInt();
    }

    private static GlobalData globalSchema2(Object data) {
        var map = OPS.getStringMap(data);
        var saved = map.get("timelines");
        var timelines = new Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline>(
                saved == null ? 0 : OPS.getList(saved).size());
        if (saved != null) {
            for (var entry : OPS.getList(saved)) {
                var list = OPS.getList(entry);
                var dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(OPS.getString(list, 0)));
                timelines.put(dimension, WeatherTimeline.fromSchema2(list.get(1)));
            }
        }
        return new GlobalData(OPS.getLong(map.get("clock")), timelines);
    }

    static void writeGlobal(FriendlyByteBuf stream, long clock,
                            Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline> timelines) {
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

    static void writeInstance(FriendlyByteBuf stream, WeatherTimeline timeline) {
        stream.writeInt(SCHEMA_VERSION);
        timeline.save(stream);
    }
}
