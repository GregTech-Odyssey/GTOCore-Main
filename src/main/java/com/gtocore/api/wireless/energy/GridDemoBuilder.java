package com.gtocore.api.wireless.energy;

import com.gtolib.api.data.Dimension;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

final class GridDemoBuilder {

    private record Flow(ResourceKey<Level> dimension, long rate, boolean produce) {}

    private final WirelessGrid grid;
    private final EnergyAccount account;
    private final ServerPlayer player;
    private final Reference2DoubleOpenHashMap<ResourceKey<Level>> fills = new Reference2DoubleOpenHashMap<>();
    private final ObjectArrayList<Flow> flows = new ObjectArrayList<>();
    private int slot;

    GridDemoBuilder(WirelessGrid grid, EnergyAccount account, ServerPlayer player) {
        this.grid = grid;
        this.account = account;
        this.player = player;
    }

    GridDemoBuilder tower(Dimension dimension, int capacityBits, int tier, double fill) {
        var key = dimension.getResourceKey();
        var pos = GridDemo.position(key, slot++);
        var capacity = BigInteger.TWO.pow(capacityBits);
        int lossPermille = Math.max(5, (12 - tier) * 6);
        grid.attach(account, new Provider.Tower(pos, player.getUUID(), Collections.singletonList(new Provider.Unit(Provider.Unit.FIXED, 1, capacity, lossPermille)), ProviderRegistry.clampTier(tier)));
        fills.put(key, fill);
        return this;
    }

    GridDemoBuilder relay(Dimension a, Dimension b, int tier) {
        return relays(a, b, tier, 1);
    }

    GridDemoBuilder relays(Dimension a, Dimension b, int tier, int count) {
        for (int i = 0; i < count; i++) {
            var pos = GridDemo.position(a.getResourceKey(), slot++);
            grid.attach(account, new Provider.Relay(pos, player.getUUID(), ProviderRegistry.clampLineTier(tier), Provider.Relay.AMPERAGE, b.getResourceKey()));
        }
        return this;
    }

    GridDemoBuilder consumer(Dimension dimension, long perTick) {
        flows.add(new Flow(dimension.getResourceKey(), perTick, false));
        return this;
    }

    GridDemoBuilder producer(Dimension dimension, long perTick) {
        flows.add(new Flow(dimension.getResourceKey(), perTick, true));
        return this;
    }

    void apply(List<EnergyPort> ports) {
        account.rebuild();
        for (var entry : fills.reference2DoubleEntrySet()) fill(entry.getKey(), entry.getDoubleValue());
        for (var flow : flows) {
            var port = port(flow);
            if (port != null) ports.add(port);
        }
    }

    private void fill(ResourceKey<Level> dimension, double ratio) {
        var node = account.node(dimension);
        if (!node.hasCapacity()) return;
        var value = new BigDecimal(node.capacity()).multiply(BigDecimal.valueOf(Math.max(0, Math.min(1, ratio)))).toBigInteger();
        node.clearStorage();
        node.absorb(U126.hi(value), U126.lo(value), null);
        account.supplyArrived(node);
    }

    @Nullable
    private EnergyPort port(Flow flow) {
        var level = player.server.getLevel(flow.dimension);
        if (level == null) return null;
        var port = EnergyPort.forTeam(PortKind.HATCH, player.getUUID(), level);
        long rate = flow.rate;
        if (flow.produce) {
            port.setService(p -> {
                p.push(rate);
                return 1;
            });
        } else {
            port.setService(p -> {
                p.pull(rate);
                return 1;
            });
        }
        port.wake();
        return port;
    }
}
