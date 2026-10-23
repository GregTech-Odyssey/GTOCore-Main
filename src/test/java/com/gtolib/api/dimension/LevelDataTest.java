package com.gtolib.api.dimension;

import com.gtolib.api.weather.WeatherProfile;
import com.gtolib.api.weather.WeatherTimeline;
import com.gtolib.api.weather.WeatherTypes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DimensionDataStorage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class LevelDataTest {

    private static final WeatherProfile PROFILE = new WeatherProfile(false,
            new WeatherProfile.Entry(WeatherTypes.CLEAR, 1, 24000, 0),
            new WeatherProfile.Entry(WeatherTypes.RAIN, 1, 12000, 0));

    @TempDir
    Path directory;

    private LevelData sample() {
        var data = new LevelData();
        data.initializeTime(18000);
        for (int i = 0; i < 37; i++) data.advanceDaylight(4223376);
        var weather = new WeatherTimeline(1234);
        weather.extend(PROFILE, 1000);
        data.setWeather(weather);
        data.setWeatherClock(1000);
        data.setStar(Level.NETHER, new WeatherTimeline(41));
        return data;
    }

    @Test
    void clockAndWeatherRoundTripInOneFileAndShareDirtyState() throws Exception {
        var data = sample();
        var storage = new DimensionDataStorage(directory.toFile(), null);
        var file = storage.getDataFile(LevelData.DATA_NAME);
        data.save(file);
        assertFalse(data.isDirty());
        var restored = LevelData.get(storage);
        assertEquals(18000, restored.dayTime());
        assertEquals(37 * 24000L, restored.remainder());
        assertEquals(1000, restored.weatherClock());
        assertEquals(data.weather().periods(), restored.weather().periods());
        assertNotNull(restored.star(Level.NETHER));
        assertFalse(restored.isDirty());
        restored.weather().change(PROFILE, WeatherTypes.RAIN, 4000, 1000);
        assertTrue(restored.isDirty());
        restored.setDirty(false);
        restored.advanceDaylight(4223376);
        assertTrue(restored.isDirty());
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void failedAtomicSavePreservesBothCommittedFieldsAndDirtyState() throws Exception {
        var data = sample();
        var file = directory.resolve("level.dat");
        data.save(file.toFile());
        byte[] committed = Files.readAllBytes(file);
        data.setDayTime(6000);
        data.weather().change(PROFILE, WeatherTypes.RAIN, 4000, 1000);
        Files.createDirectory(directory.resolve("level.dat.tmp"));
        assertThrows(RuntimeException.class, () -> data.save(file.toFile()));
        assertTrue(data.isDirty());
        assertArrayEquals(committed, Files.readAllBytes(file));
        Files.delete(directory.resolve("level.dat.tmp"));
        data.save(file.toFile());
        assertFalse(data.isDirty());
    }

    @Test
    void newerDimensionCommitRebasesWeatherWithoutRerollingOrChangingDaylight() {
        var data = sample();
        data.weather().change(PROFILE, WeatherTypes.SOLAR_STORM, 500, 1000);
        data.weather().extend(PROFILE, 1500);
        data.setWeatherClock(1500);
        var file = directory.resolve(LevelData.DATA_NAME + ".dat").toFile();
        data.save(file);
        var restored = LevelData.get(new DimensionDataStorage(directory.toFile(), null));
        var clock = new AtomicLong(100);
        restored.bindWeatherClock(clock.get(), clock::get);
        assertEquals(18000, restored.dayTime());
        assertEquals(37 * 24000L, restored.remainder());
        assertEquals(WeatherTypes.THUNDER, restored.weather().effectiveAt(100, restored.weather()));
        assertTrue(restored.isDirty());
        data.weather().extend(PROFILE, 100000);
        restored.weather().extend(PROFILE, 98600);
        var original = data.weather().periods();
        var shifted = restored.weather().periods();
        assertEquals(original.size(), shifted.size());
        for (int i = 0; i < original.size(); i++) {
            assertSame(original.get(i).weather(), shifted.get(i).weather());
            assertEquals(original.get(i).start() - 1400, shifted.get(i).start());
            assertEquals(original.get(i).end() - 1400, shifted.get(i).end());
        }
        clock.set(98650);
        restored.save(file);
        var committed = LevelData.get(new DimensionDataStorage(directory.toFile(), null));
        assertEquals(98650, committed.weatherClock());
        assertEquals(shifted, committed.weather().periods());
    }

    @Test
    void olderDimensionCommitKeepsScheduleWhenSharedClockAdvanced() {
        var data = sample();
        var original = data.weather().periods().clone();
        data.bindWeatherClock(2000, () -> 2000);
        data.bindWeatherClock(500, () -> 500);
        assertEquals(original, data.weather().periods());
        assertEquals(2000, data.weatherClock());
        assertEquals(WeatherTypes.CLEAR, data.weather().at(2000).weather());
    }

    @Test
    void dormantDaylightCatchesUpAfterSavingAndRespectsPausedAndVoidCounters() {
        var global = new LevelData();
        var planet = new LevelData();
        var voidWorld = new LevelData();
        planet.configureDaylight(18000, 0, 4223376, false, true);
        voidWorld.configureDaylight(8000, 0, 24000, true, true);
        for (int i = 0; i < 20000; i++) global.tickSimulationClock(true, true);
        for (int i = 0; i < 30000; i++) global.tickSimulationClock(false, true);
        for (int i = 0; i < 20000; i++) global.tickSimulationClock(true, false);
        planet.catchUpDaylight(global.daylightTicks(false));
        voidWorld.catchUpDaylight(global.daylightTicks(true));
        assertEquals(18000 + 40000L * 24000 / 4223376, planet.dayTime());
        assertEquals(40000L * 24000 % 4223376, planet.remainder());
        assertEquals(28000, voidWorld.dayTime());
        var storage = new DimensionDataStorage(directory.toFile(), null);
        planet.save(storage.getDataFile(LevelData.DATA_NAME));
        var restored = LevelData.get(storage);
        global.tickSimulationClock(true, true);
        restored.catchUpDaylight(global.daylightTicks(false));
        assertEquals(18000 + 40001L * 24000 / 4223376, restored.dayTime());
        assertEquals(40001L * 24000 % 4223376, restored.remainder());
        restored.catchUpDaylight(0);
        assertEquals(18000 + 40001L * 24000 / 4223376, restored.dayTime());
    }

    @Test
    void legacyClockImportsWithoutRewritingSourceAndMergedFileBecomesAuthoritative() throws Exception {
        var storage = new DimensionDataStorage(directory.toFile(), null);
        var oldClock = new DimensionClock(7000);
        oldClock.advance(4223376);
        var tag = new CompoundTag();
        tag.put("data", oldClock.save(new CompoundTag()));
        var legacyFile = storage.getDataFile(DimensionClock.DATA_NAME).toPath();
        NbtIo.writeCompressed(tag, legacyFile.toFile());
        byte[] legacyBytes = Files.readAllBytes(legacyFile);
        var data = LevelData.get(storage);
        assertEquals(7000, data.dayTime());
        assertEquals(24000, data.remainder());
        data.setWeather(new WeatherTimeline(41));
        data.save(storage.getDataFile(LevelData.DATA_NAME));
        assertArrayEquals(legacyBytes, Files.readAllBytes(legacyFile));
        Files.write(legacyFile, new byte[] { 0 });
        var freshStorage = new DimensionDataStorage(directory.toFile(), null);
        var restored = LevelData.get(freshStorage);
        assertEquals(7000, restored.dayTime());
        assertEquals(24000, restored.remainder());
        assertNotNull(restored.weather());
    }

    @ParameterizedTest
    @ValueSource(strings = { "schema", "truncated", "trailing" })
    void unreadableCombinedFileNeverCreatesReplacement(String corruption) throws Exception {
        var data = sample();
        var storage = new DimensionDataStorage(directory.toFile(), null);
        var file = storage.getDataFile(LevelData.DATA_NAME).toPath();
        data.save(file.toFile());
        byte[] bytes = Files.readAllBytes(file);
        if (corruption.equals("schema")) ByteBuffer.wrap(bytes).putInt(0, 99);
        else if (corruption.equals("truncated")) bytes = Arrays.copyOf(bytes, bytes.length - 1);
        else bytes = Arrays.copyOf(bytes, bytes.length + 1);
        Files.write(file, bytes);
        byte[] original = Files.readAllBytes(file);
        assertThrows(RuntimeException.class, () -> LevelData.get(storage));
        assertNull(storage.cache.get(LevelData.DATA_NAME));
        assertArrayEquals(original, Files.readAllBytes(file));
    }
}
