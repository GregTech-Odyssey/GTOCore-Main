package com.gtocore.integration.ae;

import com.gregtechceu.gtceu.core.ILevel;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.me.GridConnection;

import com.gto.datasynclib.datastream.DataComponentKey;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

/** Runtime-only suspension of AE links crossing the solar surface boundary. */
public final class SolarStormConnections {

    public static final ResourceKey<Level> SOLAR_SURFACE = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("gtocore:solar_surface"));
    private static final DataComponentKey<SolarStormConnections> KEY = DataComponentKey.createNoCodec("solar_storm_connections");

    /** Keep the original connection object: QNB and addons retain its handle. */
    public interface Suspendable {

        boolean gto$isStormSuspended();

        void gto$setStormSuspended(boolean suspended);

        void gto$retireStormConnection();
    }

    /** Wireless menu hosts must follow a grid replaced by splitting or merging the network. */
    public interface TerminalHost {

        @Nullable
        IGrid gto$refreshStormGrid();
    }

    // Each endpoint is indexed on its own Level. The solar Level also owns the unique edge set.
    private final Reference2ObjectOpenHashMap<IGridNode, ReferenceOpenHashSet<GridConnection>> connectionsByNode = new Reference2ObjectOpenHashMap<>();
    private final ReferenceOpenHashSet<GridConnection> connections = new ReferenceOpenHashSet<>();
    private boolean storm;
    private long revision;

    private SolarStormConnections(Level level) {
        storm = level.getLevelData().isRaining();
    }

    private static SolarStormConnections get(Level level) {
        var state = ILevel.getCapability(level, KEY);
        if (state == null) {
            state = new SolarStormConnections(level);
            ILevel.setCapability(level, KEY, state);
        }
        return state;
    }

    @Nullable
    private static ServerLevel solarLevel(@Nullable Level level) {
        if (level == null || level.isClientSide()) return null;
        if (isSolarSurface(level)) return (ServerLevel) level;
        return level.getServer().getLevel(SOLAR_SURFACE);
    }

    public static long revision(Level level) {
        var solar = solarLevel(level);
        return solar == null ? 0 : get(solar).revision;
    }

    public static boolean isSolarSurface(@Nullable Level level) {
        return level != null && level.dimension() == SOLAR_SURFACE;
    }

    public static boolean isStormActive(@Nullable Level level) {
        var solar = solarLevel(level);
        // The weather flag also covers /weather and saved storms, without the rain fade delay.
        return solar != null && solar.getLevelData().isRaining();
    }

    private static boolean crossesSolarBoundary(IGridNode a, IGridNode b) {
        return a.getLevel() != b.getLevel() && (isSolarSurface(a.getLevel()) || isSolarSurface(b.getLevel()));
    }

    public static boolean isBlocked(Level level, @Nullable IGridNode target) {
        return target != null && level != target.getLevel() &&
                (isSolarSurface(level) || isSolarSurface(target.getLevel())) && isStormActive(level);
    }

    /** Also protect consumers of a terminal's cached storage/grid, without a range check. */
    public static boolean isGridBlocked(Level level, @Nullable IGrid grid) {
        if (grid == null || !isStormActive(level)) return false;
        // Suspended boundary edges keep grids on one side. Inspect the pivot, not every node.
        var pivot = grid.getPivot();
        return pivot == null ? isSolarSurface(level) : isBlocked(level, pivot);
    }

    public static void track(GridConnection connection) {
        var a = connection.a();
        var b = connection.b();
        if (!crossesSolarBoundary(a, b)) return;
        get(a.getLevel()).connectionsByNode.computeIfAbsent(a, key -> new ReferenceOpenHashSet<>(1)).add(connection);
        get(b.getLevel()).connectionsByNode.computeIfAbsent(b, key -> new ReferenceOpenHashSet<>(1)).add(connection);
        get(isSolarSurface(a.getLevel()) ? a.getLevel() : b.getLevel()).connections.add(connection);
    }

    /** A retry during a storm reuses the pending handle instead of creating a duplicate edge. */
    @Nullable
    public static GridConnection findPending(IGridNode a, IGridNode b) {
        var state = ILevel.getCapability(a.getLevel(), KEY);
        if (state == null) return null;
        var connections = state.connectionsByNode.get(a);
        if (connections == null) return null;
        for (var connection : connections) {
            if (connection.getOtherSide(a) == b && ((Suspendable) connection).gto$isStormSuspended()) return connection;
        }
        return null;
    }

    public static void forget(GridConnection connection) {
        var a = connection.a();
        var b = connection.b();
        if (!crossesSolarBoundary(a, b)) return;
        forget(a, connection);
        forget(b, connection);
        var state = ILevel.getCapability(isSolarSurface(a.getLevel()) ? a.getLevel() : b.getLevel(), KEY);
        if (state != null) state.connections.remove(connection);
    }

    private static void forget(IGridNode node, GridConnection connection) {
        var state = ILevel.getCapability(node.getLevel(), KEY);
        if (state == null) return;
        var connections = state.connectionsByNode.get(node);
        if (connections != null && connections.remove(connection) && connections.isEmpty()) state.connectionsByNode.remove(node);
    }

    /** GridNode.destroy skips connection.destroy, and suspended edges are absent from its list. */
    public static void forgetNode(IGridNode node) {
        var state = ILevel.getCapability(node.getLevel(), KEY);
        if (state == null) return;
        var connections = state.connectionsByNode.get(node);
        if (connections == null) return;
        // AE callbacks may destroy other endpoints, so iterate an independent snapshot.
        for (var connection : connections.clone()) {
            forget(connection);
            ((Suspendable) connection).gto$retireStormConnection();
        }
    }

    /** Called only when the solar Level's rain flag actually changes. */
    public static void update(ServerLevel solar) {
        var state = ILevel.getCapability(solar, KEY);
        if (state == null) return;
        boolean active = solar.getLevelData().isRaining();
        if (state.storm == active) return;
        state.storm = active;
        // Detach/merge callbacks can retire edges and mutate the live registry.
        for (var connection : state.connections.clone()) {
            ((Suspendable) connection).gto$setStormSuspended(active);
        }
        state.revision++;
    }
}
