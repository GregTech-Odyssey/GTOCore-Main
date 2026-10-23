package com.gtocore.dimensionprobe;

import com.gtocore.common.saved.VoidWorldTimeSavedData;

import com.gtolib.api.data.CelestialBody;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.dimension.DimensionLevelData;
import com.gtolib.api.dimension.DimensionManager;
import com.gtolib.api.dimension.DimensionTemplates;
import com.gtolib.api.dimension.OwnerRef;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** 显式启用的隔离服务器探针，覆盖实际 tick 注入、命令和休眠存盘。 */
final class DaylightProbe {

    DaylightProbe() {
        MinecraftForge.EVENT_BUS.addListener(this::started);
    }

    private void started(ServerStartedEvent event) {
        var server = event.getServer();
        server.execute(() -> {
            try {
                check(server);
                Files.writeString(Path.of("daylight-result.txt"), "DAYLIGHT_PROBE_PASSED");
                System.out.println("DAYLIGHT_PROBE_PASSED");
            } catch (Throwable failure) {
                failure.printStackTrace();
                throw new IllegalStateException("Daylight probe failed", failure);
            }
            server.halt(false);
        });
    }

    private static void check(MinecraftServer server) throws Throwable {
        var manager = DimensionManager.get(server);
        var system = com.gtolib.api.weather.WeatherSystem.get(server);
        var earth = server.overworld();
        var moon = manager.loadNow(GTODimensions.MOON);
        var mars = manager.loadNow(GTODimensions.MARS);
        var mercury = manager.loadNow(GTODimensions.MERCURY);
        var venus = manager.loadNow(GTODimensions.VENUS);
        var ceres = manager.loadNow(GTODimensions.CERES);
        var sun = manager.loadNow(GTODimensions.SOLAR_SURFACE);
        var voidLevel = manager.loadNow(GTODimensions.VOID);
        var flat = manager.loadNow(GTODimensions.FLAT);
        var glacio = manager.loadNow(GTODimensions.GLACIO);
        require(CelestialBody.EARTH.getDayTicks() == 24000, "Earth reference is not 24000 ticks");
        require(data(mercury).dayLength() > 175 * 24000L && data(mercury).dayLength() < 177 * 24000L,
                "Mercury used sidereal rotation instead of its solar day");
        require(data(venus).dayLength() > 116 * 24000L && data(venus).dayLength() < 118 * 24000L,
                "Retrograde Venus solar day is incorrect");
        require(data(moon).dayLength() > 29 * 24000L && data(moon).dayLength() < 30 * 24000L,
                "The Moon used its sidereal month instead of its solar day");
        require(data(glacio).dayLength() == 0, "Tidally locked exoplanet is cycling");
        var tickMethod = ServerLevel.class.getDeclaredMethod("tickTime");
        tickMethod.setAccessible(true);
        MethodHandle tick = MethodHandles.lookup().unreflect(tickMethod);
        earth.setDayTime(12345);
        moon.setDayTime(18000);
        mars.setDayTime(6000);
        mercury.setDayTime(0);
        venus.setDayTime(0);
        ceres.setDayTime(0);
        for (var level : new ServerLevel[] { moon, mars, mercury, venus, ceres }) {
            level.setDayTime(0);
            long start = level.getDayTime();
            for (int i = 0; i < 24000; i++) {
                system.tick();
                tick.invokeExact(level);
            }
            require(level.getDayTime() == start + 24000L * 24000 / data(level).dayLength(),
                    "Incorrect scaled tick: " + level.dimension().location());
        }
        require(earth.getDayTime() == 12345, "Planet tick changed Earth time");
        long marsTime = mars.getDayTime();
        var source = server.createCommandSourceStack().withLevel(moon).withPermission(4);
        server.getCommands().getDispatcher().execute("time set 21000", source);
        server.getCommands().getDispatcher().execute("time add 300", source);
        require(moon.getDayTime() == 21300 && earth.getDayTime() == 12345 && mars.getDayTime() == marsTime,
                "Time command changed another dimension");
        server.getCommands().getDispatcher().execute("ftblibrary day", source);
        require(moon.getDayTime() % 24000 == 6000 && earth.getDayTime() == 12345 && mars.getDayTime() == marsTime,
                "FTB day shortcut changed another dimension");
        server.getCommands().getDispatcher().execute("ftblibrary night", source);
        require(moon.getDayTime() % 24000 == 18000 && earth.getDayTime() == 12345 && mars.getDayTime() == marsTime,
                "FTB night shortcut changed another dimension");
        moon.setDayTime(21300);
        var midnight = com.gtocore.common.block.HappyWaterGodStatue.class.getDeclaredMethod("midnight", ServerLevel.class);
        midnight.setAccessible(true);
        midnight.invoke(null, moon);
        require(moon.getDayTime() == 42000 && earth.getDayTime() == 12345 && mars.getDayTime() == marsTime,
                "Statue midnight changed another dimension");
        checkArsRituals(moon, earth, mars);
        var alfheim = manager.loadNow(mythicbotany.alfheim.Alfheim.DIMENSION);
        var sleepEvent = new net.minecraftforge.event.level.SleepFinishedTimeEvent(alfheim, 7000, 0);
        new mythicbotany.EventListener().sleepFinished(sleepEvent);
        require(alfheim.getDayTime() == 7000 && earth.getDayTime() == 12345 && mars.getDayTime() == marsTime,
                "Alfheim sleep changed another dimension");
        moon.setDayTime(21300);
        earth.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        system.tick();
        tick.invokeExact(moon);
        require(moon.getDayTime() == 21300, "Disabled daylight cycle still advances");
        earth.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, server);
        sun.setDayTime(18000);
        tick.invokeExact(sun);
        require(sun.getDayTime() == 6000 && !data(sun).ticksDaylight(), "Solar surface is not permanent noon");
        VoidWorldTimeSavedData.INSTANCE.setFixedTime(true);
        voidLevel.setDayTime(18000);
        flat.setDayTime(18000);
        tick.invokeExact(voidLevel);
        require(voidLevel.getDayTime() == 1000 && flat.getDayTime() == 1000, "Void/flat time lock changed");
        VoidWorldTimeSavedData.INSTANCE.setFixedTime(false);
        voidLevel.setDayTime(7000);
        flat.setDayTime(18000);
        system.tick();
        tick.invokeExact(voidLevel);
        require(voidLevel.getDayTime() == 7001 && flat.getDayTime() == 18001, "Custom void/flat times are not independent");
        var owner = new OwnerRef(OwnerRef.Kind.PLAYER, UUID.randomUUID());
        var first = manager.getOrCreatePrivate(owner, DimensionTemplates.VOID, "daylight-first", 41L);
        var second = manager.getOrCreatePrivate(owner, DimensionTemplates.VOID, "daylight-second", 42L);
        var firstLevel = manager.loadNow(first.dimension());
        var secondLevel = manager.loadNow(second.dimension());
        firstLevel.setDayTime(8000);
        secondLevel.setDayTime(16000);
        system.tick();
        tick.invokeExact(firstLevel);
        require(firstLevel.getDayTime() == 8001 && secondLevel.getDayTime() == 16001,
                "Private instances share a clock");
        system.forecast(first.dimension());
        require(manager.requestUnload(first.dimension()), "Could not unload private combined data");
        manager.tickEnd();
        for (int i = 0; i < 100003; i++) system.tick();
        earth.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        earth.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        for (int i = 0; i < 37; i++) system.tick();
        earth.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, server);
        earth.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(true, server);
        VoidWorldTimeSavedData.INSTANCE.setFixedTime(true);
        for (int i = 0; i < 100; i++) system.tick();
        VoidWorldTimeSavedData.INSTANCE.setFixedTime(false);
        system.forecast(first.dimension());
        require(server.getLevel(first.dimension()) == null, "Dormant forecast loaded terrain");
        require(system.dormantDaylight(first.dimension()).dayTime() == 108004, "Dormant daylight failed to advance or ignored pauses");
        var privateRestored = manager.loadNow(first.dimension());
        require(privateRestored.getDayTime() == 108004 && com.gtolib.api.weather.WeatherSystem.levelData(privateRestored).weather() != null,
                "Dormant forecast lost private time or weather");
        system.forecast(mercury.dimension());
        mercury.setDayTime(7000);
        for (int i = 0; i < 37; i++) {
            system.tick();
            tick.invokeExact(mercury);
        }
        long expectedRemainder = data(mercury).remainder();
        require(expectedRemainder > 0, "Long day lost fractional progress");
        require(manager.requestUnload(mercury.dimension()), "Could not queue clock dimension for unload");
        manager.tickEnd();
        require(server.getLevel(GTODimensions.MERCURY) == null, "Dimension did not become dormant");
        for (int i = 0; i < 100003; i++) system.tick();
        system.forecast(mercury.dimension());
        var expected = system.dormantDaylight(mercury.dimension());
        long expectedTime = 7000 + 100040L * 24000 / data(mercury).dayLength();
        expectedRemainder = 100040L * 24000 % data(mercury).dayLength();
        require(expected.dayTime() == expectedTime && expected.remainder() == expectedRemainder,
                "Dormant long-day simulation lost fractional progress");
        var expectedWeather = expected.weather().periods().clone();
        var restored = manager.loadNow(GTODimensions.MERCURY);
        require(restored != mercury && restored.getDayTime() == expectedTime && data(restored).remainder() == expectedRemainder,
                "Dormancy lost clock or fractional progress");
        var diskStorage = new net.minecraft.world.level.storage.DimensionDataStorage(
                restored.getDataStorage().getDataFile(com.gtolib.api.dimension.LevelData.DATA_NAME).getParentFile(), server.getFixerUpper());
        var saved = com.gtolib.api.dimension.DimensionSavedData.getFromFile(com.gtolib.api.dimension.LevelData.DATA_NAME,
                diskStorage, com.gtolib.api.dimension.LevelData::load);
        require(saved.remainder() == expectedRemainder, "Clock NBT round trip lost fractional progress");
        require(saved.dayTime() == expectedTime && saved.weather().periods().equals(expectedWeather),
                "Clock and weather were not committed together");
    }

    private static void checkArsRituals(ServerLevel local, ServerLevel earth, ServerLevel other) throws Exception {
        long earthTime = earth.getDayTime();
        long otherTime = other.getDayTime();
        var tile = new com.hollingsworth.arsnouveau.common.block.tile.RitualBrazierTile(net.minecraft.core.BlockPos.ZERO,
                com.hollingsworth.arsnouveau.setup.registry.BlockRegistry.RITUAL_BLOCK.get().defaultBlockState());
        tile.setLevel(local);
        for (var ritual : new com.hollingsworth.arsnouveau.api.ritual.AbstractRitual[] {
                new com.hollingsworth.arsnouveau.common.ritual.RitualSunrise(),
                new com.hollingsworth.arsnouveau.common.ritual.RitualMoonfall() }) {
            long start = ritual instanceof com.hollingsworth.arsnouveau.common.ritual.RitualSunrise ? 18000 : 6000;
            local.setDayTime(start);
            ritual.tile = tile;
            var method = ritual.getClass().getDeclaredMethod("tick");
            method.setAccessible(true);
            method.invoke(ritual);
            require(local.getDayTime() == start + 100 && earth.getDayTime() == earthTime && other.getDayTime() == otherTime,
                    "Ars time ritual changed another dimension");
        }
    }

    private static DimensionLevelData data(ServerLevel level) {
        return (DimensionLevelData) level.getLevelData();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
