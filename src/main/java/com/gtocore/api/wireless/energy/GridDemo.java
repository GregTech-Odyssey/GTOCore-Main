package com.gtocore.api.wireless.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

/**
 * 开发环境电网演示：在不会加载的远处坐标登记虚拟能源塔与中继，用常驻端点制造真实流量。
 * 每个队伍一份登记；切换或停止时释放端点、移除虚拟设施，并把存量与速率还原到演示前。
 */
public final class GridDemo {

    public enum Scenario {

        SOLAR("solar"),
        REALMS("realms"),
        SATURATED("saturated"),
        LARGE("large"),
        EMPTY("empty");

        private final String id;

        Scenario(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        @Nullable
        public static Scenario of(String id) {
            for (var scenario : values()) {
                if (scenario.id.equals(id)) return scenario;
            }
            return null;
        }
    }

    public record Result(int nodes, int lines, int ports) {}

    static final int DEMO_X = 29_000_000, DEMO_Y = -2000, DEMO_Z = 29_000_000;
    private static final long DEMO_RATE = 1L << 40;
    private static final Reference2ObjectOpenHashMap<EnergyAccount, Session> SESSIONS = new Reference2ObjectOpenHashMap<>();

    private static final class Session {

        final ObjectArrayList<EnergyPort> ports = new ObjectArrayList<>();
        final Reference2ObjectOpenHashMap<ResourceKey<Level>, long[]> storage = new Reference2ObjectOpenHashMap<>();
        long pendingHi, pendingLo, rate;
    }

    private GridDemo() {}

    @Nullable
    public static Result start(ServerPlayer player, Scenario scenario) {
        var grid = WirelessGrid.get();
        if (grid == null || !grid.available) return null;
        var account = WirelessGrid.accountOf(player.getUUID());
        if (account.isNone()) return null;
        clear(grid, account);
        int ports = 0;
        if (scenario != Scenario.EMPTY) {
            var session = snapshot(account);
            var builder = new GridDemoBuilder(grid, account, player);
            GridDemoScenarios.build(scenario, builder);
            account.rate = DEMO_RATE;
            builder.apply(session.ports);
            SESSIONS.put(account, session);
            ports = session.ports.size();
        }
        return new Result(account.nodeList.size(), account.arcs.size() >> 1, ports);
    }

    @Nullable
    public static Result stop(ServerPlayer player) {
        return start(player, Scenario.EMPTY);
    }

    static void forget() {
        SESSIONS.clear();
    }

    static GlobalPos position(ResourceKey<Level> dimension, int slot) {
        return GlobalPos.of(dimension, new BlockPos(DEMO_X + slot, DEMO_Y, DEMO_Z));
    }

    static boolean isDemo(GlobalPos pos) {
        return pos.pos().getY() == DEMO_Y && pos.pos().getX() >= DEMO_X && pos.pos().getZ() == DEMO_Z;
    }

    private static Session snapshot(EnergyAccount account) {
        var session = new Session();
        for (var node : account.nodeList) session.storage.put(node.dimension, new long[] { node.hi, node.lo });
        session.pendingHi = account.pendingHi;
        session.pendingLo = account.pendingLo;
        session.rate = account.rate;
        return session;
    }

    private static void clear(WirelessGrid grid, EnergyAccount account) {
        var session = SESSIONS.remove(account);
        if (session != null) {
            for (var port : session.ports) port.release();
        }
        boolean found = detachDemo(grid, account);
        if (session == null && !found) return;
        if (session != null) restore(account, session);
        else dropOrphaned(account);
        account.rebuild();
        clampToCapacity(account);
    }

    private static boolean detachDemo(WirelessGrid grid, EnergyAccount account) {
        var demo = new ObjectArrayList<GlobalPos>();
        for (var pos : account.towers.keySet()) {
            if (isDemo(pos)) demo.add(pos);
        }
        for (var pos : account.relays.keySet()) {
            if (isDemo(pos)) demo.add(pos);
        }
        for (var pos : demo) grid.detach(pos);
        return !demo.isEmpty();
    }

    private static void restore(EnergyAccount account, Session session) {
        for (var node : account.nodeList) {
            var saved = session.storage.get(node.dimension);
            if (saved == null) {
                node.clearStorage();
            } else {
                node.hi = saved[0];
                node.lo = saved[1];
            }
        }
        account.pendingHi = session.pendingHi;
        account.pendingLo = session.pendingLo;
        account.rate = session.rate;
    }

    private static void dropOrphaned(EnergyAccount account) {
        var real = new ReferenceOpenHashSet<ResourceKey<Level>>();
        for (var tower : account.towers.values()) real.add(GridBody.of(tower.pos().dimension()));
        for (var node : account.nodeList) {
            if (!real.contains(node.dimension)) node.clearStorage();
        }
    }

    private static void clampToCapacity(EnergyAccount account) {
        for (var node : account.nodeList) {
            long hi = Math.max(0, node.hi), lo = node.hi < 0 ? 0 : node.lo;
            if (U126.compare(hi, lo, node.capHi, node.capLo) <= 0) continue;
            node.hi = node.capHi;
            node.lo = node.capLo;
        }
    }
}
