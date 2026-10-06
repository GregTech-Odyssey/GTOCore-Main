package com.gtocore.common.weather;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import com.gto.datasynclib.datastream.data.StringData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/** Persisted natural schedule. The stellar overlay never consumes or rerolls this schedule. */
public final class WeatherTimeline {

    public static final long FORECAST_TICKS = 3 * 24000L;
    public static final int RECOVERY_TICKS = 6000;

    public record Period(WeatherType weather, long start, long end) {}

    private final ObjectArrayList<Period> periods = new ObjectArrayList<>(16);
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
        while (!periods.isEmpty() && periods.getFirst().end() <= now) {
            var expired = periods.removeFirst();
            if (expired.weather() == WeatherTypes.SOLAR_STORM) lastStormEnd = expired.end();
        }
        if (periods.isEmpty()) {
            var first = profile.entries()[0];
            periods.add(new Period(first.weather(), now, now + duration(first)));
        }
        long horizon = now + FORECAST_TICKS;
        while (periods.getLast().end() < horizon) {
            var entry = choose(profile);
            long start = periods.getLast().end();
            periods.add(new Period(entry.weather(), start, start + duration(entry)));
        }
    }

    private WeatherProfile.Entry choose(WeatherProfile profile) {
        int weight = 0;
        for (var entry : profile.entries()) weight += entry.weight();
        int choice = nextInt(weight);
        for (var entry : profile.entries()) {
            choice -= entry.weight();
            if (choice < 0) return entry;
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
        if (!periods.isEmpty() && periods.getFirst().weather() == WeatherTypes.SOLAR_STORM && weather != WeatherTypes.SOLAR_STORM) lastStormEnd = now;
        periods.clear();
        periods.add(new Period(weather, now, now + ticks));
        extend(profile, now);
    }

    public Period at(long time) {
        for (int i = 0; i < periods.size(); i++) {
            var period = periods.get(i);
            if (period.start() <= time && time < period.end()) return period;
        }
        throw new IllegalArgumentException("Time outside generated forecast");
    }

    public WeatherType effectiveAt(long time, WeatherTimeline star) {
        var stellar = star.at(time);
        if (stellar.weather() == WeatherTypes.SOLAR_STORM) return WeatherTypes.CLEAR;
        long stormEnd = star.lastStormEnd;
        for (int i = 0; i < star.periods.size(); i++) {
            var period = star.periods.get(i);
            if (period.end() > time) break;
            if (period.weather() == WeatherTypes.SOLAR_STORM) stormEnd = period.end();
        }
        if (stormEnd != Long.MIN_VALUE && time - stormEnd < RECOVERY_TICKS) return WeatherTypes.THUNDER;
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
                long recoveryEnd = star.lastStormEnd == Long.MIN_VALUE ? Long.MIN_VALUE : star.lastStormEnd + RECOVERY_TICKS;
                if (recoveryEnd > start) end = Math.min(end, recoveryEnd);
                for (var period : star.periods) {
                    if (period.weather() != WeatherTypes.SOLAR_STORM) continue;
                    recoveryEnd = period.end() + RECOVERY_TICKS;
                    if (recoveryEnd > start) end = Math.min(end, recoveryEnd);
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

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putLong("random", randomState);
        if (lastStormEnd != Long.MIN_VALUE) tag.putLong("last_storm_end", lastStormEnd);
        var list = new ListTag();
        for (var period : periods) {
            var entry = new CompoundTag();
            entry.putString("weather", WeatherTypes.REGISTRY.dataCodec().encode(period.weather()).getString());
            entry.putLong("start", period.start());
            entry.putLong("end", period.end());
            list.add(entry);
        }
        tag.put("periods", list);
        return tag;
    }

    public static WeatherTimeline load(CompoundTag tag) {
        var timeline = new WeatherTimeline(tag.getLong("random"));
        if (tag.contains("last_storm_end")) timeline.lastStormEnd = tag.getLong("last_storm_end");
        var list = tag.getList("periods", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            var entry = list.getCompound(i);
            var weather = WeatherTypes.REGISTRY.dataCodec().decode(StringData.valueOf(entry.getString("weather")), 0);
            if (weather != null) timeline.periods.add(new Period(weather, entry.getLong("start"), entry.getLong("end")));
        }
        return timeline;
    }
}
