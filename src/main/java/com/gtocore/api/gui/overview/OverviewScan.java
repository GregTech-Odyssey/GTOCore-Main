package com.gtocore.api.gui.overview;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import com.suried.gtoutils.level.LevelStore;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.jetbrains.annotations.Nullable;

final class OverviewScan {

    private static final LevelStore<Long2ObjectOpenHashMap<OverviewScan>> SCANS = LevelStore.create(Long2ObjectOpenHashMap::new);
    private static final int BUDGET = 8192;
    private static final int REFRESH_MIN = 40;
    private static final int REFRESH_MAX = 640;

    private final MultiblockControllerMachine host;
    private final OverviewAdapter adapter;
    private final LongOpenHashSet own = new LongOpenHashSet();
    private int viewers;
    @Nullable
    private OverviewCapture capture;
    @Nullable
    private OverviewSnapshot snapshot;
    private long signature;
    private OverviewSnapshot.Payload payload = OverviewSnapshot.EMPTY_PAYLOAD;
    private int version;
    private int interval = REFRESH_MIN;
    private int nextAt;
    private int tickedAt;
    private boolean ticked;

    private OverviewScan(MultiblockControllerMachine host, OverviewAdapter adapter) {
        this.host = host;
        this.adapter = adapter;
    }

    static OverviewScan acquire(MultiblockControllerMachine host, OverviewAdapter adapter) {
        var level = host.getLevel();
        if (level == null) return new OverviewScan(host, adapter);
        var scans = SCANS.get(level);
        long key = host.getPos().asLong();
        var scan = scans.get(key);
        if (scan == null || scan.host != host || scan.adapter != adapter) {
            scan = new OverviewScan(host, adapter);
            scans.put(key, scan);
        }
        scan.viewers++;
        return scan;
    }

    void release() {
        if (--viewers > 0) return;
        var level = host.getLevel();
        if (level == null) return;
        var scans = SCANS.getIfPresent(level);
        long key = host.getPos().asLong();
        if (scans != null && scans.get(key) == this) scans.remove(key);
    }

    @Nullable
    OverviewSnapshot snapshot() {
        return snapshot;
    }

    OverviewSnapshot.Payload payload() {
        return payload;
    }

    int version() {
        return version;
    }

    void invalidate() {
        capture = null;
        interval = REFRESH_MIN;
        nextAt = host.getOffsetTimer();
    }

    void tick() {
        var level = host.getLevel();
        if (level == null || host.isRemoved()) return;
        int now = host.getOffsetTimer();
        if (ticked && now == tickedAt) return;
        ticked = true;
        tickedAt = now;
        if (capture == null) {
            if (snapshot != null && now - nextAt < 0) return;
            capture = new OverviewCapture(adapter, host, level, own, reusable());
        }
        if (!capture.step(BUDGET)) return;
        var result = capture.result();
        long next = capture.signature();
        capture = null;
        if (snapshot != null && next == signature) {
            interval = Math.min(REFRESH_MAX, interval * 2);
        } else {
            snapshot = result;
            signature = next;
            payload = result.encode();
            version++;
            interval = REFRESH_MIN;
        }
        nextAt = now + interval;
    }

    private Long2ObjectOpenHashMap<int[]> reusable() {
        var current = snapshot;
        if (current == null) return new Long2ObjectOpenHashMap<>();
        var modules = current.modules();
        var map = new Long2ObjectOpenHashMap<int[]>(modules.size());
        for (var module : modules) map.put(module.pos().asLong(), module.blocks());
        return map;
    }
}
