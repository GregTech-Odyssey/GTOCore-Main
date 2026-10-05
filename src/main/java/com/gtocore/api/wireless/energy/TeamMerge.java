package com.gtocore.api.wireless.energy;

import com.gtolib.GTOCore;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class TeamMerge {

    private TeamMerge() {}

    public static void onTeamJoined(UUID player, UUID previousTeam, UUID team) {
        var grid = WirelessGrid.get();
        if (grid == null || !grid.available) return;
        var from = grid.accounts.get(previousTeam);
        var to = grid.account(team);
        if (from != null) {
            mergeInto(grid, from, to);
            GTOCore.LOGGER.info("[无线电网] 玩家 {} 加入队伍 {}，个人电网并入队伍电网", player, team);
        }
        to.invalidatePorts();
    }

    public static void onTeamLeft(UUID player, UUID team, UUID personalTeam, boolean deleted) {
        var grid = WirelessGrid.get();
        if (grid == null || !grid.available) return;
        var from = grid.accounts.get(team);
        var to = grid.account(personalTeam);
        if (from != null) {
            if (deleted) {
                mergeInto(grid, from, to);
                GTOCore.LOGGER.info("[无线电网] 队伍 {} 解散，余额转入原队长 {}", team, player);
            } else {
                moveProviders(grid, player, from, to);
                from.invalidatePorts();
            }
        }
        to.invalidatePorts();
    }

    public static void onTeamChanged(@Nullable UUID previousTeam, UUID team) {
        var grid = WirelessGrid.get();
        if (grid == null || !grid.available) return;
        if (previousTeam != null) {
            var previous = grid.accounts.get(previousTeam);
            if (previous != null) previous.invalidatePorts();
        }
        var current = grid.accounts.get(team);
        if (current != null) current.invalidatePorts();
    }

    private static void moveProviders(WirelessGrid grid, UUID owner, EnergyAccount from, EnergyAccount to) {
        if (from == to) return;
        var moving = new ObjectArrayList<Provider>();
        for (var tower : from.towers.values()) {
            if (tower.owner().equals(owner)) moving.add(tower);
        }
        for (var relay : from.relays.values()) {
            if (relay.owner().equals(owner)) moving.add(relay);
        }
        for (var provider : moving) {
            grid.detach(provider.pos());
            grid.attach(to, provider);
        }
        if (!moving.isEmpty()) {
            from.markDirty();
            to.markDirty();
        }
    }

    private static void mergeInto(WirelessGrid grid, EnergyAccount from, EnergyAccount to) {
        if (from == to) return;
        to.addPending(from.pendingHi, from.pendingLo);
        from.pendingHi = 0;
        from.pendingLo = 0;
        for (var node : from.nodeList) {
            if (!node.isEmpty()) node.moveTo(to.nodeOrCreate(node.dimension));
        }
        var moving = new ObjectArrayList<Provider>(from.towers.values());
        moving.addAll(from.relays.values());
        for (var provider : moving) {
            grid.detach(provider.pos());
            grid.attach(to, provider);
        }
        if (from.rate > to.rate) {
            to.rate = from.rate;
            to.bindPos = from.bindPos;
        }
        from.removed = true;
        grid.accounts.remove(from.team, from);
        from.invalidatePorts();
        from.wakeAll();
        to.markDirty();
    }
}
