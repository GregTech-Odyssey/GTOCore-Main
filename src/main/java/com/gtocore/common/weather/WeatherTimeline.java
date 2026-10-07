package com.gtocore.common.weather;

import com.gtolib.utils.iostream.DataIOStream;

import com.gto.datasynclib.datastream.data.Data;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;

/** Persisted natural schedule. The stellar overlay never consumes or rerolls this schedule. */
public final class WeatherTimeline {

    public static final long FORECAST_TICKS = 3 * 24000L;
    public static final int RECOVERY_TICKS = 6000;

    public record Period(WeatherType weather, long start, long end) {

        public Period {
            if (weather == null || end <= start) {
                throw new IllegalArgumentException("Invalid weather period");
            }
        }
    }

    private static final Runnable NO_CHANGE = () -> {};
    private ObjectArrayList<Period> periods = new ObjectArrayList<>(16);
    private Runnable changed = NO_CHANGE;

    void onChange(Runnable listener) {
        changed = listener;
    }

    private long randomState;
    private long lastStormEnd = Long.MIN_VALUE;

    public WeatherTimeline(long seed) {
        randomState = seed;
    }

    public ObjectArrayList<Period> periods() {
        return periods;
    }

    /** Include stellar recovery expiry even when the star's calm period continues. */
    public long nextBoundary(long now) {
        long boundary = periods.getFirst().end();
        if (lastStormEnd != Long.MIN_VALUE && lastStormEnd + RECOVERY_TICKS > now) {
            boundary = Math.min(boundary, lastStormEnd + RECOVERY_TICKS);
        }
        return boundary;
    }

    public void extend(WeatherProfile profile, long now) {
        boolean modified = false;
        while (!periods.isEmpty() && periods.getFirst().end() <= now) {
            modified = true;
            var expired = periods.removeFirst();
            if (expired.weather() == WeatherTypes.SOLAR_STORM) lastStormEnd = expired.end();
        }

        if (periods.isEmpty()) {
            modified = true;
            var first = profile.entries()[0];
            periods.add(new Period(first.weather(), now, now + duration(first)));
        }

        long horizon = now + FORECAST_TICKS;
        while (periods.getLast().end() < horizon) {
            modified = true;
            var entry = choose(profile);
            long start = periods.getLast().end();
            periods.add(new Period(entry.weather(), start, start + duration(entry)));
        }
        if (modified) changed.run();
    }

    private WeatherProfile.Entry choose(WeatherProfile profile) {
        int weight = 0;
        for (var entry : profile.entries()) {
            weight += entry.weight();
        }

        int choice = nextInt(weight);
        for (var entry : profile.entries()) {
            choice -= entry.weight();
            if (choice < 0) {
                return entry;
            }
        }

        throw new IllegalStateException("Empty weather profile");
    }

    private int nextInt(int bound) {
        randomState += 0x9E3779B97F4A7C15L;
        long value = randomState;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (int) Long.remainderUnsigned(value, bound);
    }

    private int duration(WeatherProfile.Entry entry) {
        return entry.duration() - entry.variation() + nextInt(entry.variation() * 2 + 1);
    }

    public void change(WeatherProfile profile, WeatherType weather, int ticks, long now) {
        if (!periods.isEmpty() && periods.getFirst().weather() == WeatherTypes.SOLAR_STORM &&
                weather != WeatherTypes.SOLAR_STORM) {
            lastStormEnd = now;
        }
        periods.clear();
        periods.add(new Period(weather, now, now + ticks));
        extend(profile, now);
        changed.run();
    }

    public Period at(long time) {
        for (Period period : periods) {
            if (period.start() <= time && time < period.end()) {
                return period;
            }
        }
        throw new IllegalArgumentException("Time outside generated forecast");
    }

    public WeatherType effectiveAt(long time, WeatherTimeline star) {
        var stellar = star.at(time);
        if (stellar.weather() == WeatherTypes.SOLAR_STORM) {
            return WeatherTypes.CLEAR;
        }

        long stormEnd = star.lastStormEnd;
        for (int i = 0; i < star.periods.size(); i++) {
            var period = star.periods.get(i);
            if (period.end() > time) {
                break;
            }
            if (period.weather() == WeatherTypes.SOLAR_STORM) {
                stormEnd = period.end();
            }
        }

        if (stormEnd != Long.MIN_VALUE && time - stormEnd < RECOVERY_TICKS) {
            return WeatherTypes.THUNDER;
        }
        return at(time).weather();
    }

    /** Merge natural and stellar boundaries, so a forecast describes exactly the executed weather. */
    public ObjectArrayList<Period> forecast(long now, WeatherTimeline star) {
        var result = new ObjectArrayList<Period>(16);
        long horizon = now + FORECAST_TICKS;
        long start = now;
        while (start < horizon) {
            long end = Math.min(at(start).end(), horizon);
            var weather = at(start).weather();
            if (star != null) {
                weather = effectiveAt(start, star);
                end = Math.min(end, star.at(start).end());
                long recoveryEnd = star.lastStormEnd == Long.MIN_VALUE ? Long.MIN_VALUE :
                        star.lastStormEnd + RECOVERY_TICKS;
                if (recoveryEnd > start) {
                    end = Math.min(end, recoveryEnd);
                }
                for (var period : star.periods) {
                    if (period.weather() != WeatherTypes.SOLAR_STORM) {
                        continue;
                    }
                    recoveryEnd = period.end() + RECOVERY_TICKS;
                    if (recoveryEnd > start) {
                        end = Math.min(end, recoveryEnd);
                    }
                }
            }

            if (!result.isEmpty() && result.getLast().weather() == weather) {
                var previous = result.removeLast();
                result.add(new Period(weather, previous.start(), end));
            } else {
                result.add(new Period(weather, start, end));
            }
            start = end;
        }
        return result;
    }

    public void save(DataIOStream stream) throws IOException {
        stream.writeLong(randomState);
        stream.writeBoolean(lastStormEnd != Long.MIN_VALUE);
        if (lastStormEnd != Long.MIN_VALUE) {
            stream.writeLong(lastStormEnd);
        }

        stream.writeVarInt(periods.size());
        for (var period : periods) {
            WeatherDataIO.WEATHER.encode(stream, period.weather());
            stream.writeLong(period.start());
            stream.writeVarLong(period.end() - period.start());
        }
    }

    public static WeatherTimeline load(DataIOStream stream) throws IOException {
        var timeline = new WeatherTimeline(stream.readLong());
        if (stream.readBoolean()) {
            timeline.lastStormEnd = stream.readLong();
        }
        int size = stream.readVarInt();
        timeline.periods = new ObjectArrayList<>(size);
        for (int i = 0; i < size; i++) {
            var weather = WeatherDataIO.WEATHER.decode(stream);
            long start = stream.readLong();
            timeline.periods.add(new Period(weather, start, start + stream.readVarLong()));
        }
        return timeline;
    }

    static WeatherTimeline fromSchema2(Data data) {
        var map = data.asStringMapData().getStringMap();
        var timeline = new WeatherTimeline(map.get("random_state").getLong());
        var stormEnd = map.get("last_storm_end");
        if (stormEnd != null) {
            timeline.lastStormEnd = stormEnd.getLong();
        }
        var periods = map.get("periods").asListData();
        timeline.periods = new ObjectArrayList<>(periods.size());
        for (var period : periods) {
            var list = period.asListData();
            timeline.periods
                    .add(new Period(WeatherTypes.REGISTRY.get(list.getString(0)), list.getLong(1), list.getLong(2)));
        }
        return timeline;
    }
}
