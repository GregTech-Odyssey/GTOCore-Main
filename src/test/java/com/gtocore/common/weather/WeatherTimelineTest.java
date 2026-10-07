package com.gtocore.common.weather;

import com.gtolib.api.dimension.DimensionDataIO;
import com.gtolib.api.dimension.DimensionSaveAttempt;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.iostream.DataIOStream;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.network.NetworkEvent;

import com.gto.datasynclib.datastream.DataComponentMap;
import com.gto.datasynclib.datastream.data.Data;
import com.gto.datasynclib.datastream.data.ListData;
import com.gto.datasynclib.datastream.data.LongData;
import com.gto.datasynclib.datastream.data.StringData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.gtocore.common.weather.WeatherTypes.*;
import static org.junit.jupiter.api.Assertions.*;

class WeatherTimelineTest {

    @TempDir
    Path directory;

    private static Data encoded(WeatherTimeline timeline) {
        return WeatherDataComponents.TIMELINE_CODEC.encode(timeline);
    }

    private static WeatherTimeline roundTrip(WeatherTimeline timeline) {
        return WeatherDataComponents.TIMELINE_CODEC.decode(DimensionDataIO.readPayload(encoded(timeline).writeToBytes()),
                SharedConstants.getCurrentVersion().getDataVersion().getVersion());
    }

    private static byte[] saved(FastSavedData data) throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var output = DataIOStream.of(bytes)) {
            data.save(output);
        }
        return bytes.toByteArray();
    }

    private static byte[] components(DataComponentMap map, boolean global) throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var output = DataIOStream.of(bytes)) {
            WeatherDataComponents.write(output, global ? WeatherDataComponents.GLOBAL : WeatherDataComponents.INSTANCE, map);
        }
        return bytes.toByteArray();
    }

    private static InstanceWeatherData instance(WeatherTimeline timeline) throws Exception {
        var map = new DataComponentMap(1);
        map.put(WeatherDataComponents.INSTANCE_TIMELINE, timeline);
        try (var input = DataIOStream.of(components(map, false))) {
            return InstanceWeatherData.load(input);
        }
    }

    private static WeatherTimeline recoveringStar() {
        var map = new DataComponentMap(3);
        map.put(WeatherDataComponents.RANDOM_STATE, Long.MAX_VALUE);
        map.put(WeatherDataComponents.LAST_STORM_END, 1000L);
        var periods = new ObjectArrayList<WeatherTimeline.Period>(1);
        periods.add(new WeatherTimeline.Period(CALM, 1000, 97000));
        map.put(WeatherDataComponents.PERIODS, periods);
        return WeatherTimeline.load(map);
    }

    @Test
    void binaryGlobalAndInstanceWeatherPreserveSchedulesRecoveryAndDirtyParents() throws Exception {
        var originalPlanet = new WeatherTimeline(Long.MIN_VALUE);
        originalPlanet.periods().add(new WeatherTimeline.Period(RAIN, 0, 24000));
        originalPlanet.periods().add(new WeatherTimeline.Period(CLEAR, 24000, 96000));
        var originalStar = recoveringStar();
        var originalTimelines = new Reference2ObjectOpenHashMap<ResourceKey<Level>, WeatherTimeline>(2);
        originalTimelines.put(Level.OVERWORLD, originalPlanet);
        originalTimelines.put(WeatherSystem.PROXIMA_STAR, originalStar);
        var original = new DataComponentMap(2);
        original.put(WeatherDataComponents.CLOCK, 1000L);
        original.put(WeatherDataComponents.TIMELINES, originalTimelines);
        WeatherSystem system;
        try (var input = DataIOStream.of(components(original, true))) {
            system = WeatherSystem.load(input);
        }
        assertEquals(1000, system.clock());
        var storage = new DimensionDataStorage(directory.toFile(), null);
        var file = storage.getDataFile("global_weather");
        system.setDirty();
        system.save(file);
        assertFalse(system.isDirty());
        assertEquals(0x47545743, ByteBuffer.wrap(Files.readAllBytes(file.toPath())).getInt());
        var restored = FastSavedData.getFromFile("global_weather", storage, WeatherSystem::load);
        assertNotNull(restored);
        assertSame(restored, FastSavedData.getFromFile("global_weather", storage, WeatherSystem::load));
        assertEquals(1000, restored.clock());
        DataComponentMap map;
        try (var input = DataIOStream.of(saved(restored))) {
            map = WeatherDataComponents.read(input, WeatherDataComponents.GLOBAL);
        }
        var timelines = map.getData(WeatherDataComponents.TIMELINES);
        var planet = timelines.get(Level.OVERWORLD);
        var star = timelines.get(WeatherSystem.PROXIMA_STAR);
        assertEquals(encoded(originalPlanet), encoded(planet));
        assertEquals(encoded(originalStar), encoded(star));
        for (long time = 1000; time < 73000; time++) assertSame(originalPlanet.effectiveAt(time, originalStar), planet.effectiveAt(time, star));
        assertEquals(7000, star.nextBoundary(1000));

        // Decode into the actual parent, then mutate that parent's timeline (not a separately decoded snapshot).
        var field = WeatherSystem.class.getDeclaredField("timelines");
        field.setAccessible(true);
        var owned = ((Reference2ObjectOpenHashMap<?, WeatherTimeline>) field.get(restored)).get(Level.OVERWORLD);
        restored.setDirty(false);
        owned.extend(EARTH, 96000);
        assertTrue(restored.isDirty());

        var instance = instance(originalStar);
        instance.setDirty();
        DimensionDataIO.writeFastAtomic(storage.getDataFile("instance_weather").toPath(), instance);
        var instanceRoundTrip = FastSavedData.getFromFile("instance_weather", storage, InstanceWeatherData::load);
        try (var input = DataIOStream.of(saved(instanceRoundTrip))) {
            var restoredTimeline = WeatherDataComponents.read(input, WeatherDataComponents.INSTANCE).getData(WeatherDataComponents.INSTANCE_TIMELINE);
            assertEquals(encoded(originalStar), encoded(restoredTimeline));
            assertEquals(7000, restoredTimeline.nextBoundary(1000));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "magic", "kind", "schema", "length", "truncated", "trailing", "data_trailing", "missing_timeline", "missing_random", "unknown_weather", "period_gap" })
    void invalidWeatherFilesCannotResetThroughSavedDataLookup(String corruption) throws Exception {
        byte[] bytes = saved(instance(recoveringStar()));
        if (corruption.equals("magic")) ByteBuffer.wrap(bytes).putInt(0, 0x0a000000);
        else if (corruption.equals("kind")) bytes[4] = 1;
        else if (corruption.equals("schema")) ByteBuffer.wrap(bytes).putInt(5, 99);
        else if (corruption.equals("length")) ByteBuffer.wrap(bytes).putInt(13, -1);
        else if (corruption.equals("truncated")) bytes = Arrays.copyOf(bytes, bytes.length - 1);
        else if (corruption.equals("trailing")) bytes = Arrays.copyOf(bytes, bytes.length + 1);
        else {
            var root = DimensionDataIO.readPayload(Arrays.copyOfRange(bytes, 17, bytes.length)).asStringMapData();
            if (corruption.equals("missing_timeline")) root.getStringMap().remove("timeline");
            else {
                var timeline = root.getStringMap().get("timeline").asStringMapData();
                if (corruption.equals("missing_random")) timeline.getStringMap().remove("random_state");
                else {
                    var periods = timeline.getStringMap().get("periods").asListData();
                    if (corruption.equals("unknown_weather")) periods.get(0).asListData().set(0, StringData.valueOf("missing"));
                    else periods.add(ListData.of(StringData.valueOf("calm"), LongData.valueOf(98000), LongData.valueOf(99000)));
                }
            }
            byte[] payload = root.writeToBytes();
            if (corruption.equals("data_trailing")) payload = Arrays.copyOf(payload, payload.length + 1);
            bytes = Arrays.copyOf(bytes, 17 + payload.length);
            ByteBuffer.wrap(bytes).putInt(13, payload.length);
            System.arraycopy(payload, 0, bytes, 17, payload.length);
        }
        var storage = new DimensionDataStorage(directory.toFile(), null);
        var file = storage.getDataFile("weather").toPath();
        Files.write(file, bytes);
        byte[] original = Files.readAllBytes(file);
        assertThrows(RuntimeException.class, () -> FastSavedData.get("weather", storage, InstanceWeatherData::load, () -> { throw new AssertionError("Corrupt weather was recreated"); }));
        assertNull(storage.cache.get("weather"));
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    @Test
    void parentDirtySurvivesAtomicFailureAndEmptyTimelineCanGenerateLegitimately() throws Exception {
        var timeline = new WeatherTimeline(0);
        var loaded = instance(timeline);
        // The parent attaches a listener when loading, and extended periods are saved from that same timeline.
        var field = InstanceWeatherData.class.getDeclaredField("timeline");
        field.setAccessible(true);
        var owned = (WeatherTimeline) field.get(loaded);
        loaded.setDirty(false);
        owned.extend(EARTH, 0);
        assertTrue(loaded.isDirty());
        var blocked = directory.resolve("weather.dat");
        Files.createDirectory(blocked);
        Files.writeString(blocked.resolve("blocked"), "failure");
        assertThrows(RuntimeException.class, () -> loaded.save(blocked.toFile()));
        assertTrue(loaded.isDirty());
        var attempt = DimensionSaveAttempt.begin();
        try {
            assertThrows(RuntimeException.class, () -> loaded.save(blocked.toFile()));
            assertTrue(loaded.isDirty());
            var file = directory.resolve("checked-weather.dat");
            loaded.save(file.toFile());
            assertFalse(loaded.isDirty());
            assertArrayEquals(saved(loaded), Files.readAllBytes(file));
            try (var input = DataIOStream.of(Files.readAllBytes(file))) {
                var restored = InstanceWeatherData.load(input);
                assertArrayEquals(saved(loaded), saved(restored));
            }
        } finally {
            attempt.finish();
        }
        loaded.setDirty(false);
        owned.change(EARTH, THUNDER, 5000, 0);
        assertTrue(loaded.isDirty());
        assertSame(THUNDER, roundTrip(owned).at(0).weather());
    }

    static {
        SharedConstants.tryDetectVersion();
        // Plain JUnit has no Forge event transformer to add NetworkEvent's zero-argument constructor.
        // Seed its listener list using the instance path before vanilla's network bootstrap.
        try {
            var listeners = EventListenerHelper.class.getDeclaredMethod("getListenerListInternal", Class.class, boolean.class);
            listeners.setAccessible(true);
            listeners.invoke(null, NetworkEvent.class, true);
            for (var event : NetworkEvent.class.getDeclaredClasses()) {
                if (!Event.class.isAssignableFrom(event)) continue;
                var parent = event.getSuperclass();
                if (parent != NetworkEvent.class && parent != Event.class) listeners.invoke(null, parent, true);
                listeners.invoke(null, event, true);
            }
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
        Bootstrap.bootStrap();
    }

    private static final WeatherProfile EARTH = new WeatherProfile(true,
            new WeatherProfile.Entry(CLEAR, 70, 18000, 6000),
            new WeatherProfile.Entry(RAIN, 25, 12000, 4000),
            new WeatherProfile.Entry(THUNDER, 5, 6000, 2000));
    private static final WeatherProfile STAR = new WeatherProfile(false,
            new WeatherProfile.Entry(CALM, 85, 24000, 8000),
            new WeatherProfile.Entry(SOLAR_STORM, 15, 6000, 2000));

    @Test
    void forecastMatchesEveryExecutedTickAndSurvivesRestart() {
        var planet = new WeatherTimeline(1234);
        var star = new WeatherTimeline(5678);
        planet.extend(EARTH, 0);
        star.extend(STAR, 0);
        var forecast = planet.forecast(0, star);
        planet = roundTrip(planet);
        star = roundTrip(star);
        int index = 0;
        for (long tick = 0; tick < WeatherTimeline.FORECAST_TICKS; tick++) {
            planet.extend(EARTH, tick);
            star.extend(STAR, tick);
            while (forecast.get(index).end() <= tick) index++;
            assertSame(forecast.get(index).weather(), planet.effectiveAt(tick, star), "Forecast diverged at tick " + tick);
        }
    }

    @Test
    void starOverridesNaturalTransitionsThenRecoversAtExactBoundaries() {
        var planet = new WeatherTimeline(1);
        planet.change(EARTH, RAIN, 72001, 0);
        var star = new WeatherTimeline(2);
        star.periods().add(new WeatherTimeline.Period(CALM, 0, 1000));
        star.periods().add(new WeatherTimeline.Period(SOLAR_STORM, 1000, 5000));
        star.periods().add(new WeatherTimeline.Period(CALM, 5000, 100000));
        assertSame(RAIN, planet.effectiveAt(999, star));
        assertSame(CLEAR, planet.effectiveAt(1000, star));
        assertSame(CLEAR, planet.effectiveAt(4999, star));
        assertSame(THUNDER, planet.effectiveAt(5000, star));
        star.extend(STAR, 5000);
        star = roundTrip(star);
        assertSame(THUNDER, planet.effectiveAt(10999, star));
        assertSame(RAIN, planet.effectiveAt(11000, star));
    }

    @Test
    void manualStarEndUpdatesRecoveryAndPlanetSchedulesRemainIndependent() {
        var a = new WeatherTimeline(1);
        var b = new WeatherTimeline(2);
        a.extend(EARTH, 0);
        b.extend(EARTH, 0);
        var original = encoded(b);
        a.change(EARTH, THUNDER, 4000, 0);
        assertEquals(original, encoded(b));
        var star = new WeatherTimeline(3);
        star.change(STAR, SOLAR_STORM, 10000, 0);
        star.change(STAR, CALM, 24000, 2000);
        assertSame(THUNDER, a.effectiveAt(2000, star));
        assertSame(THUNDER, a.effectiveAt(7999, star));
        assertSame(a.at(8000).weather(), a.effectiveAt(8000, star));
    }

    @Test
    void persistedRandomStateKeepsFutureExtensionsStableAndDurationsInRange() {
        var timeline = new WeatherTimeline(99);
        timeline.extend(EARTH, 0);
        var restored = roundTrip(timeline);
        for (long now = 0; now < 2400000; now += 1000) {
            timeline.extend(EARTH, now);
            restored.extend(EARTH, now);
            assertEquals(encoded(timeline), encoded(restored));
            for (var period : timeline.periods()) {
                var entry = EARTH.find(period.weather());
                long duration = period.end() - period.start();
                assertTrue(duration >= entry.duration() - entry.variation());
                assertTrue(duration <= entry.duration() + entry.variation());
            }
        }
    }

    @Test
    void acidRainWeightDominatesWhileSingleWeatherProfilesNeverChangeType() {
        var acid = new WeatherProfile(true, new WeatherProfile.Entry(CLEAR, 10, 100, 0), new WeatherProfile.Entry(ACID_RAIN, 90, 100, 0));
        var timeline = new WeatherTimeline(17);
        timeline.extend(acid, 0);
        int acidCount = 0;
        for (var period : timeline.periods()) if (period.weather() == ACID_RAIN) acidCount++;
        assertTrue(acidCount > timeline.periods().size() * 0.8);
        var airless = new WeatherProfile(false, new WeatherProfile.Entry(CLEAR, 1, 24000, 0));
        timeline.change(airless, CLEAR, 24000, 0);
        for (var period : timeline.forecast(0, null)) assertSame(CLEAR, period.weather());
        assertNull(airless.vanillaWeather(true, true));
        assertSame(SOLAR_STORM, STAR.vanillaWeather(true, false));
    }

    @Test
    void transitionsFadeWithoutTicksAndReverseFromCurrentIntensity() {
        var rain = new WeatherState(RAIN, 0, 0, 1000);
        assertEquals(0.205F, rain.rainLevel(1020, 0.5F), 0.00001F);
        assertEquals(1, rain.rainLevel(1200, 0));
        var clear = new WeatherState(CLEAR, rain.rainLevel(1030, 0), 0, 1030);
        assertEquals(0.1F, clear.rainLevel(1050, 0), 0.00001F);
        var resumed = new WeatherState(RAIN, clear.rainLevel(1050, 0), 0, 1050);
        assertEquals(0.35F, resumed.rainLevel(1075, 0), 0.00001F);
        assertEquals(0, clear.rainLevel(1100, 0));
    }

    @Test
    void joiningMidTransitionPreservesRainAndRawThunderFade() {
        var server = new WeatherState(THUNDER, 0, 0, 1000);
        var client = new WeatherState(THUNDER, server.rainLevel(1040, 0), server.rawThunderLevel(1040, 0), 8000);
        for (int elapsed = 0; elapsed <= 100; elapsed++) {
            assertEquals(server.rainLevel(1040 + elapsed, 0.5F), client.rainLevel(8000 + elapsed, 0.5F), 0.00001F);
            assertEquals(server.thunderLevel(1040 + elapsed, 0.5F), client.thunderLevel(8000 + elapsed, 0.5F), 0.00001F);
        }
    }

    @Test
    void scheduledBoundaryIncludesRecoveryExpiryAndSurvivesRestart() {
        var star = new WeatherTimeline(31);
        star.change(STAR, SOLAR_STORM, 1000, 0);
        assertEquals(1000, star.nextBoundary(0));
        star.change(STAR, CALM, 24000, 1000);
        star = roundTrip(star);
        assertEquals(7000, star.nextBoundary(1000));
        assertEquals(7000, star.nextBoundary(6999));
        assertEquals(25000, star.nextBoundary(7000));
        star.change(STAR, SOLAR_STORM, 2000, 7000);
        assertEquals(9000, star.nextBoundary(7000));
    }
}
