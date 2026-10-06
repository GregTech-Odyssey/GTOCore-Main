package com.gtocore.common.weather;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.network.NetworkEvent;

import org.junit.jupiter.api.Test;

import static com.gtocore.common.weather.WeatherTypes.*;
import static org.junit.jupiter.api.Assertions.*;

class WeatherTimelineTest {

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
        planet = WeatherTimeline.load(planet.save());
        star = WeatherTimeline.load(star.save());
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
        star = WeatherTimeline.load(star.save());
        assertSame(THUNDER, planet.effectiveAt(10999, star));
        assertSame(RAIN, planet.effectiveAt(11000, star));
    }

    @Test
    void manualStarEndUpdatesRecoveryAndPlanetSchedulesRemainIndependent() {
        var a = new WeatherTimeline(1);
        var b = new WeatherTimeline(2);
        a.extend(EARTH, 0);
        b.extend(EARTH, 0);
        var original = b.save();
        a.change(EARTH, THUNDER, 4000, 0);
        assertEquals(original, b.save());
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
        var restored = WeatherTimeline.load(timeline.save());
        for (long now = 0; now < 2400000; now += 1000) {
            timeline.extend(EARTH, now);
            restored.extend(EARTH, now);
            assertEquals(timeline.save(), restored.save());
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
        star = WeatherTimeline.load(star.save());
        assertEquals(7000, star.nextBoundary(1000));
        assertEquals(7000, star.nextBoundary(6999));
        assertEquals(25000, star.nextBoundary(7000));
        star.change(STAR, SOLAR_STORM, 2000, 7000);
        assertEquals(9000, star.nextBoundary(7000));
    }
}
