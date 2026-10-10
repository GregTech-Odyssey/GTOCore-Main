package com.gtocore.api.wireless.energy;

import com.gtolib.GTOCore;
import com.gtolib.api.misc.FastSavedData;
import com.gtolib.utils.FileUtils;

import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.DimensionDataStorage;

import com.gto.fastcollection.fastutil.O2OOpenCacheHashMap;
import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * 全服电网：按队伍保存各维度存量与能源塔、中继登记（主世界 data/gtocore_wireless_energy.dat），持有全服端点调度。
 */
public final class WirelessGrid extends FastSavedData {

    public static final String DATA_NAME = "gtocore_wireless_energy";
    /**
     * 存档文件头写的版本号：库把它作为 VarInt 写在文件最前面（见 {@link com.gtolib.utils.FileUtils#saveVersionedToFile}），
     * 也是这份负载唯一的版本号——负载里不再有第二份。重建后的格式从 1 开始，读取时只认文件头这一个
     * （见 {@link GridCodec#decodeVersioned}）。
     */
    public static final int VERSION = 1;
    private static final int VALIDATE_TICKS = 1200;

    @Nullable
    private static WirelessGrid instance;
    @Nullable
    private static MinecraftServer server;

    final boolean available;
    final O2OOpenCacheHashMap<UUID, EnergyAccount> accounts = new O2OOpenCacheHashMap<>();
    final O2OOpenCacheHashMap<GlobalPos, EnergyAccount> providerOwners = new O2OOpenCacheHashMap<>();
    private final ObjectArrayList<EnergyAccount> active = new ObjectArrayList<>();
    private final ObjectArrayList<EnergyAccount> dirty = new ObjectArrayList<>();
    private final ObjectArrayList<RouteJob> routeJobs = new ObjectArrayList<>();
    private final GridScheduler scheduler = new GridScheduler();

    WirelessGrid(boolean available) {
        this.available = available;
    }

    public static void load(MinecraftServer minecraftServer, DimensionDataStorage storage) {
        if (instance != null && server == minecraftServer) return;
        server = minecraftServer;
        GridClock.advance(minecraftServer.getTickCount());
        WirelessGrid data;
        var file = storage.getDataFile(DATA_NAME);
        if (file.exists()) {
            try {
                data = FileUtils.loadVersionedFromFile(file, GridCodec::decodeVersioned, GridCodec::decodeUnversioned);
            } catch (RuntimeException e) {
                GTOCore.LOGGER.error("[无线电网] {} 存在但无法读取：本次运行电网不可用，不迁移、不写回", DATA_NAME, e);
                data = new WirelessGrid(false);
            }
        } else {
            data = LegacyMigration.migrate(storage);
        }
        storage.cache.put(DATA_NAME, data);
        instance = data;
    }

    public static void unload() {
        instance = null;
        server = null;
        GridSampler.clearAll();
        GridDemo.forget();
    }

    @Nullable
    static WirelessGrid get() {
        return instance;
    }

    @Nullable
    static MinecraftServer server() {
        return server;
    }

    @Nullable
    static GridScheduler scheduler() {
        var grid = instance;
        return grid == null || !grid.available ? null : grid.scheduler;
    }

    @Override
    public boolean isDirty() {
        return available;
    }

    @Override
    public int version() {
        return VERSION;
    }

    @Override
    public void save(FriendlyByteBuf stream) {
        GridCodec.encode(this, stream);
    }

    public static EnergyAccount accountOf(@Nullable UUID owner) {
        var grid = instance;
        if (grid == null || !grid.available || owner == null) return EnergyAccount.NONE;
        return grid.account(TeamUtil.getTeamUUID(owner));
    }

    public static EnergyAccount accountIfPresent(@Nullable UUID owner) {
        var grid = instance;
        if (grid == null || !grid.available || owner == null) return EnergyAccount.NONE;
        var account = grid.accounts.get(TeamUtil.getTeamUUID(owner));
        return account == null ? EnergyAccount.NONE : account;
    }

    EnergyAccount account(UUID team) {
        var account = accounts.get(team);
        if (account == null) {
            account = new EnergyAccount(team);
            accounts.put(team, account);
        }
        return account;
    }

    public static void forEachAccount(Consumer<EnergyAccount> action) {
        var grid = instance;
        if (grid != null && grid.available) grid.accounts.values().forEach(action);
    }

    void attach(EnergyAccount account, Provider provider) {
        if (provider instanceof Provider.Tower tower) account.towers.put(tower.pos(), tower);
        else if (provider instanceof Provider.Relay relay) account.relays.put(relay.pos(), relay);
        providerOwners.put(provider.pos(), account);
    }

    @Nullable
    EnergyAccount detach(GlobalPos pos) {
        var account = providerOwners.remove(pos);
        if (account != null) {
            account.towers.remove(pos);
            account.relays.remove(pos);
        }
        return account;
    }

    static void markActive(EnergyAccount account) {
        var grid = instance;
        if (grid != null) grid.active.add(account);
    }

    static void markDirty(EnergyAccount account) {
        var grid = instance;
        if (grid != null) grid.dirty.add(account);
        else account.rebuild();
    }

    public static void onServerTickStart(MinecraftServer minecraftServer) {
        boolean newSecond = GridClock.advance(minecraftServer.getTickCount());
        var grid = instance;
        if (grid == null || !grid.available) return;
        grid.rebuildDirty();
        grid.pollRouteJobs();
        if (newSecond) grid.rollSeconds();
    }

    public static void onServerTickEnd(MinecraftServer minecraftServer) {
        var grid = instance;
        if (grid == null || !grid.available) return;
        grid.rebuildDirty();
        grid.scheduler.run(GridClock.tick());
        int now = GridClock.tick();
        if (now % GridBinding.RATE_REFRESH_TICKS == 0) GridBinding.refreshRates(grid, minecraftServer);
        if (now % VALIDATE_TICKS == 0) ProviderRegistry.validate(grid, minecraftServer);
    }

    void rebuildDirty() {
        for (int i = 0, n = dirty.size(); i < n; i++) {
            var account = dirty.get(i);
            if (account.dirty && !account.removed) account.rebuild(true);
        }
        dirty.clear();
    }

    static void submit(RouteJob job) {
        var grid = instance;
        if (grid == null) {
            job.runNow();
            return;
        }
        job.submit(Util.backgroundExecutor());
        grid.routeJobs.add(job);
    }

    private void pollRouteJobs() {
        for (int i = routeJobs.size() - 1; i >= 0; i--) {
            if (routeJobs.get(i).poll()) routeJobs.remove(i);
        }
    }

    private void rollSeconds() {
        for (int i = 0, n = active.size(); i < n; i++) {
            var account = active.get(i);
            if (!account.removed) account.meter.roll();
        }
        active.clear();
    }
}
