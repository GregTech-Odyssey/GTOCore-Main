package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public final class ProviderRegistry {

    private ProviderRegistry() {}

    static int clampTier(int tier) {
        return Math.max(-1, Math.min(GTValues.MAX, tier));
    }

    static int clampLineTier(int tier) {
        return Math.max(0, Math.min(GTValues.MAX, tier));
    }

    public static void registerTower(MetaMachine machine, List<Provider.Unit> units, int tier, long throughput) {
        register(machine, (pos, owner, level) -> new Provider.Tower(pos, owner, List.copyOf(units), clampTier(tier), Math.max(0, throughput)));
    }

    public static BigInteger towerCapacity(MetaMachine machine) {
        if (!(machine.getLevel() instanceof ServerLevel level)) return BigInteger.ZERO;
        return WirelessGrid.accountIfPresent(machine.getOwnerUUID()).towerCapacity(GlobalPos.of(level.dimension(), machine.getPos()));
    }

    public record TowerCharge(BigInteger energy, long[] scales) {

        public long scale(int tier) {
            return tier >= 0 && tier < scales.length ? Math.max(1, scales[tier]) : 1;
        }
    }

    @Nullable
    public static TowerCharge towerCharge(MetaMachine machine) {
        var grid = WirelessGrid.get();
        if (grid == null || !grid.available || !(machine.getLevel() instanceof ServerLevel level)) return null;
        var pos = GlobalPos.of(level.dimension(), machine.getPos());
        var account = grid.providerOwners.get(pos);
        var tower = account == null ? null : account.towers.get(pos);
        if (tower == null || tower.tier() < 0) return null;
        var node = account.nodes.get(GridBody.of(pos.dimension()));
        if (node == null) return null;
        int bank = tower.tier();
        var stored = U126.toBig(Math.max(0, node.bankHi[bank]), node.bankLo[bank]);
        var bankCapacity = U126.toBig(node.bankCapHi[bank], node.bankCapLo[bank]);
        var capacity = account.towerCapacity(pos);
        if (stored.signum() <= 0 || bankCapacity.signum() <= 0 || capacity.signum() <= 0) return new TowerCharge(BigInteger.ZERO, account.gridScales(tower));
        var share = capacity.compareTo(bankCapacity) >= 0 ? stored : stored.multiply(capacity).divide(bankCapacity);
        return new TowerCharge(share, account.gridScales(tower));
    }

    public static void releaseTower(MetaMachine machine, BigInteger taken) {
        var grid = WirelessGrid.get();
        if (grid == null || !(machine.getLevel() instanceof ServerLevel level)) return;
        var pos = GlobalPos.of(level.dimension(), machine.getPos());
        var account = grid.providerOwners.get(pos);
        var tower = account == null ? null : account.towers.get(pos);
        if (tower != null && tower.tier() >= 0) {
            var node = account.nodes.get(GridBody.of(pos.dimension()));
            if (node != null) node.release(tower.tier(), taken, account.towerCapacity(pos));
            account.towerCapacities.remove(pos);
        }
        unregister(grid, pos);
    }

    public static boolean chargeTower(MetaMachine machine, BigInteger amount) {
        if (amount.signum() <= 0) return true;
        var grid = WirelessGrid.get();
        if (grid == null || !grid.available || !(machine.getLevel() instanceof ServerLevel level)) return false;
        var pos = GlobalPos.of(level.dimension(), machine.getPos());
        var account = grid.providerOwners.get(pos);
        var tower = account == null ? null : account.towers.get(pos);
        if (tower == null || tower.tier() < 0) return false;
        account.nodeOrCreate(pos.dimension()).load(tower.tier(), U126.hi(amount), U126.lo(amount));
        account.markDirty();
        return true;
    }

    public static void registerRelay(MetaMachine machine, int tier, @Nullable ResourceKey<Level> target) {
        register(machine, (pos, owner, level) -> new Provider.Relay(pos, owner, clampLineTier(tier), Provider.Relay.AMPERAGE, target == null ? level.dimension() : target));
    }

    private interface Factory {

        Provider create(GlobalPos pos, UUID owner, ServerLevel level);
    }

    private static void register(MetaMachine machine, Factory factory) {
        var grid = WirelessGrid.get();
        var owner = machine.getOwnerUUID();
        if (grid == null || !grid.available || owner == null || !(machine.getLevel() instanceof ServerLevel level)) return;
        var pos = GlobalPos.of(level.dimension(), machine.getPos());
        var account = grid.account(TeamUtil.getTeamUUID(owner));
        var provider = factory.create(pos, owner, level);
        if (provider.equals(account.towers.get(pos)) || provider.equals(account.relays.get(pos))) return;
        var previous = grid.detach(pos);
        if (previous != null && previous != account) previous.markDirty();
        grid.attach(account, provider);
        account.markDirty();
    }

    public static void unregisterLater(MetaMachine machine, int delay) {
        if (!(machine.getLevel() instanceof ServerLevel level)) return;
        var pos = machine.getPos();
        TaskHandler.enqueueTask(level, () -> {
            if (machine.getHolder().isRemoved() || !level.isLoaded(pos)) return;
            if (machine instanceof IMultiController controller && controller.isFormed()) return;
            unregister(machine);
        }, delay);
    }

    public static void unregister(MetaMachine machine) {
        var grid = WirelessGrid.get();
        if (grid == null || !(machine.getLevel() instanceof ServerLevel level)) return;
        unregister(grid, GlobalPos.of(level.dimension(), machine.getPos()));
    }

    static void unregister(WirelessGrid grid, GlobalPos pos) {
        var account = grid.detach(pos);
        if (account != null) account.markDirty();
    }

    static void validate(WirelessGrid grid, MinecraftServer server) {
        var stale = new ObjectArrayList<GlobalPos>();
        Object2ObjectMaps.fastForEach(grid.providerOwners, entry -> {
            var pos = entry.getKey();
            var level = server.getLevel(pos.dimension());
            if (level == null || !level.isLoaded(pos.pos())) return;
            if (!(MetaMachine.getMachine(level, pos.pos()) instanceof IWirelessGridProvider provider) || !provider.isProvidingWirelessGrid()) stale.add(pos);
        });
        for (var pos : stale) unregister(grid, pos);
    }
}
