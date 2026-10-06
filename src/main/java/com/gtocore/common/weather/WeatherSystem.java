package com.gtocore.common.weather;

import com.gtocore.client.Message;
import com.gtocore.integration.ae.SolarStormHandler;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.data.Galaxy;
import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.api.misc.PlanetManagement;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import com.gto.datasynclib.datastream.DataComponentKey;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/** Cached on the overworld capability and persisted once, with independent schedules per dimension. */
public final class WeatherSystem extends SavedData {

    private static final String DATA_NAME = "gtocore_galaxy_weather";
    private static final DataComponentKey<WeatherSystem> KEY = DataComponentKey.createNoCodec(DATA_NAME);
    private static final DataComponentKey<WeatherState> STATE = DataComponentKey.createNoCodec("galaxy_weather_state");
    public static final DataComponentKey<WeatherType> CLIENT_WEATHER = DataComponentKey.createNoCodec("galaxy_weather_client");
    public static final ResourceKey<Level> PROXIMA_STAR = dimension("gtocore:proxima_weather_star");
    public static final ResourceKey<Level> BARNARDA_STAR = dimension("gtocore:barnarda_weather_star");

    private final Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline> timelines = new Reference2ObjectOpenHashMap<>(48);
    private MinecraftServer server;
    private long clock;
    private long nextUpdate;
    private final BiConsumer<ResourceKey<Level>, WeatherTimeline> extendTimelines = (key, timeline) -> {
        timeline.extend(profile(key), clock);
        nextUpdate = Math.min(nextUpdate, timeline.nextBoundary(clock));
    };

    private WeatherSystem() {}

    public static WeatherSystem get(MinecraftServer server) {
        var overworld = server.overworld();
        var system = ILevel.getCapability(overworld, KEY);
        if (system == null) {
            system = overworld.getDataStorage().computeIfAbsent(WeatherSystem::load, WeatherSystem::new, DATA_NAME);
            system.server = server;
            ILevel.setCapability(overworld, KEY, system);
            system.timeline(SolarStormHandler.SOLAR_SURFACE);
            system.timeline(PROXIMA_STAR);
            system.timeline(BARNARDA_STAR);
            for (var dimension : Dimension.all()) {
                system.timeline(dimension.getResourceKey());
                if (dimension.hasOrbitDimension()) system.timeline(dimension.getOrbit());
            }
        }
        return system;
    }

    public long clock() {
        return clock;
    }

    public static WeatherType current(Level level) {
        if (level.isClientSide) return ILevel.getCapability(level, CLIENT_WEATHER);
        var state = state(level);
        return state == null ? null : state.weather();
    }

    private static WeatherState state(Level level) {
        var state = ILevel.getCapability(level, STATE);
        if (state == null && level instanceof ServerLevel serverLevel) {
            // ServerLevel's constructor queries sky brightness before the overworld is registered.
            // noinspection ConstantValue
            if (serverLevel.getServer().overworld() == null) return null;
            get(serverLevel.getServer()).apply(serverLevel);
            state = ILevel.getCapability(level, STATE);
        }
        return state;
    }

    public static float rainLevel(Level level, float partialTick) {
        var state = state(level);
        return state == null ? 0 : state.rainLevel(level.getGameTime(), partialTick);
    }

    public static float thunderLevel(Level level, float partialTick) {
        var state = state(level);
        return state == null ? 0 : state.thunderLevel(level.getGameTime(), partialTick);
    }

    public static void receiveWeather(Level level, WeatherType weather, float rain, float thunder) {
        ILevel.setCapability(level, CLIENT_WEATHER, weather);
        ILevel.setCapability(level, STATE, new WeatherState(weather, rain, thunder, level.getGameTime()));
    }

    /** Vanilla intensity setters are external weather requests on the server only. */
    public static void setRainLevel(Level level, float strength) {
        if (level instanceof ServerLevel serverLevel && level.dimension() == Level.OVERWORLD) {
            var system = get(serverLevel.getServer());
            var period = system.timeline(level.dimension()).at(system.clock);
            var weather = profile(level.dimension()).vanillaWeather(strength > 0.2F, period.weather().thundering());
            if (weather != period.weather()) system.change(serverLevel, weather, system.remaining(period));
        }
    }

    public static void setThunderLevel(Level level, float strength) {
        if (level instanceof ServerLevel serverLevel && level.dimension() == Level.OVERWORLD) {
            var system = get(serverLevel.getServer());
            var period = system.timeline(level.dimension()).at(system.clock);
            var weather = profile(level.dimension()).vanillaWeather(period.weather().raining(), strength > 0.9F);
            if (weather != period.weather()) system.change(serverLevel, weather, system.remaining(period));
        }
    }

    private int remaining(WeatherTimeline.Period period) {
        return (int) Math.min(Integer.MAX_VALUE, period.end() - clock);
    }

    private static ResourceKey<Level> dimension(String id) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(id));
    }

    public static WeatherProfile profile(ResourceKey<Level> key) {
        return key == PROXIMA_STAR || key == BARNARDA_STAR ? WeatherProfiles.SOLAR : WeatherProfiles.get(key);
    }

    private WeatherTimeline timeline(ResourceKey<Level> key) {
        var descriptor = DimensionManager.get(server).descriptor(key);
        if (descriptor != null) {
            var timeline = InstanceWeatherData.timeline(server, descriptor, clock, profile(key));
            nextUpdate = Math.min(nextUpdate, timeline.nextBoundary(clock));
            return timeline;
        }
        var timeline = timelines.get(key);
        if (timeline == null) {
            timeline = new WeatherTimeline(server.getWorldData().worldGenOptions().seed() ^ key.location().toString().hashCode());
            timeline.extend(profile(key), clock);
            timelines.put(key, timeline);
            nextUpdate = Math.min(nextUpdate, timeline.nextBoundary(clock));
            setDirty();
        }
        return timeline;
    }

    private WeatherTimeline star(ResourceKey<Level> key) {
        if (!profile(key).stellarInfluence()) return null;
        var galaxy = GTODimensions.getGalaxy(key);
        if (galaxy == Galaxy.PROXIMA_CENTAURI) return timeline(PROXIMA_STAR);
        if (galaxy == Galaxy.BARNARDA) return timeline(BARNARDA_STAR);
        if (WeatherProfiles.sameGalaxy(key, SolarStormHandler.SOLAR_SURFACE)) return timeline(SolarStormHandler.SOLAR_SURFACE);
        return null;
    }

    public void tick() {
        if (server.overworld().getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
            clock++;
            setDirty();
        }
        if (clock >= nextUpdate) update();
    }

    private void update() {
        nextUpdate = Long.MAX_VALUE;
        timelines.forEach(extendTimelines);
        for (var level : server.getAllLevels()) apply(level);
    }

    public ObjectArrayList<WeatherTimeline.Period> forecast(ResourceKey<Level> key) {
        var timeline = timeline(key);
        timeline.extend(profile(key), clock);
        var star = star(key);
        if (star != null) star.extend(WeatherProfiles.SOLAR, clock);
        return timeline.forecast(clock, star);
    }

    public boolean change(ServerLevel level, WeatherType weather, int ticks) {
        if (weather == null || profile(level.dimension()).find(weather) == null) return false;
        timeline(level.dimension()).change(profile(level.dimension()), weather, ticks, clock);
        setDirty();
        // Recompute immediately: stellar commands also update their planets and AE links.
        update();
        return true;
    }

    private void apply(ServerLevel level) {
        var timeline = timeline(level.dimension());
        timeline.extend(profile(level.dimension()), clock);
        var stellar = star(level.dimension());
        WeatherType weather = stellar == null ? timeline.at(clock).weather() : timeline.effectiveAt(clock, stellar);
        var previous = ILevel.getCapability(level, STATE);
        if (previous == null || previous.weather() != weather) {
            long now = level.getGameTime();
            float rain = previous == null ? (weather.raining() ? 1 : 0) : previous.rainLevel(now, 0);
            float thunder = previous == null ? (weather.thundering() ? 1 : 0) : previous.rawThunderLevel(now, 0);
            ILevel.setCapability(level, STATE, new WeatherState(weather, rain, thunder, now));
            if (SolarStormHandler.isSolarSurface(level)) SolarStormHandler.update(level);
            for (var player : level.players()) sync(player);
        }
    }

    public void sync(ServerPlayer player) {
        var level = player.serverLevel();
        var state = state(level);
        long now = level.getGameTime();
        Message.WEATHER_S2C.send(buf -> {
            buf.writeResourceLocation(level.dimension().location());
            WeatherTypes.REGISTRY.streamCodec().encode(buf, state.weather());
            buf.writeFloat(state.rainLevel(now, 0));
            buf.writeFloat(state.rawThunderLevel(now, 0));
        }, player);
    }

    public ObjectArrayList<ResourceKey<Level>> forecastDimensions(ServerPlayer player) {
        var result = new ObjectArrayList<ResourceKey<Level>>(32);
        var current = player.level().dimension();
        result.add(current);
        for (var dimension : Dimension.all()) {
            var key = dimension.getResourceKey();
            if (dimension.isWithinGalaxy() && key != current && PlanetManagement.isUnlocked(player, key)) result.add(key);
        }
        return result;
    }

    private static WeatherSystem load(CompoundTag tag) {
        var system = new WeatherSystem();
        system.clock = tag.getLong("clock");
        var dimensions = tag.getCompound("dimensions");
        for (var id : dimensions.getAllKeys()) system.timelines.put(dimension(id), WeatherTimeline.load(dimensions.getCompound(id)));
        return system;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putLong("clock", clock);
        var dimensions = new CompoundTag();
        timelines.forEach((key, timeline) -> dimensions.put(key.location().toString(), timeline.save()));
        tag.put("dimensions", dimensions);
        return tag;
    }
}
