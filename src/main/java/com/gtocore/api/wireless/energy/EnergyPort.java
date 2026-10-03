package com.gtocore.api.wireless.energy;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * 机器、玩家或交易接入电网的唯一通道：withdraw/deposit 在机器 tick 只碰缓冲，pull/push 在 Service 回调里按路由存取，tick 末按优先级服务；
 * 取不到（存不进）的端点挂起到所在节点等待唤醒。整笔扣电走 checkSettle/settle/refund，整笔入账走 canDepositLump/depositLump。
 */
public final class EnergyPort {

    public interface Host {

        @Nullable
        UUID owner();

        @Nullable
        Level level();

        @Nullable
        default MetaMachine machine() {
            return null;
        }
    }

    @FunctionalInterface
    public interface Service {

        int serve(EnergyPort port);
    }

    public static final int IDLE_TICKS = 20;

    private final PortKind kind;
    private final Host host;
    private final int tier;
    private int priority = GridScheduler.DEFAULT_PRIORITY;

    EnergyAccount account = EnergyAccount.NONE;
    private GridNode node = GridNode.EMPTY;
    @Nullable
    private UUID owner;
    private int boundVersion = -1;
    private int extraLoss;
    private boolean released;

    @Nullable
    private Service service;
    private final GridScheduler.Handle handle = new GridScheduler.Handle(this::serveTick);
    private final TokenBucket rateBucket = new TokenBucket();
    private final GridTransfer.Outcome outcome = new GridTransfer.Outcome();
    private final PortBuffer buffer = new PortBuffer();
    final PortCycle cycle = new PortCycle();
    private final PortHold hold = new PortHold();
    private final PortFlow flow = new PortFlow();
    private final PortSettlement settlement = new PortSettlement();

    int indexSlot = -1;
    @Nullable
    PortIndex index;

    public EnergyPort(PortKind kind, MetaMachine machine, int tier) {
        this(kind, new PortHosts.MachineHost(machine), tier);
    }

    public EnergyPort(PortKind kind, Host host, int tier) {
        this.kind = kind;
        this.host = host;
        this.tier = tier;
    }

    public static EnergyPort forPlayer(PortKind kind, Player player) {
        return new EnergyPort(kind, new PortHosts.PlayerHost(player), 0);
    }

    public static EnergyPort forTeam(PortKind kind, UUID team, Level level) {
        return new EnergyPort(kind, new PortHosts.TeamHost(team, level), 0);
    }

    @Nullable
    public MetaMachine machine() {
        return host.machine();
    }

    public int tier() {
        return tier;
    }

    public int priority() {
        return priority;
    }

    public int setPriority(int priority) {
        this.priority = GridScheduler.clampPriority(priority);
        handle.setPriority(this.priority);
        hold.set(node, this.priority, hold.draw(), hold.put());
        return this.priority;
    }

    public void setService(@Nullable Service service) {
        this.service = service;
    }

    public EnergyAccount account() {
        refresh();
        return account;
    }

    public GridNode node() {
        refresh();
        return node;
    }

    public boolean isOpen(int voltageTier) {
        refresh();
        return account != EnergyAccount.NONE && voltageTier <= node.reachTier;
    }

    public long withdraw(long want, int voltageTier) {
        refresh();
        if (want <= 0 || voltageTier > node.reachTier) return 0;
        long take = buffer.withdraw(want, voltageTier);
        request(voltageTier);
        return take;
    }

    public void returnUnused(long amount) {
        buffer.returnUnused(amount);
    }

    public long deposit(long gross, int voltageTier) {
        refresh();
        if (gross <= 0 || voltageTier > node.reachTier) return 0;
        long accept = buffer.roomFor(gross);
        if (accept > 0) addPending(accept, voltageTier);
        return accept;
    }

    public boolean canDepositAll(long gross, int voltageTier) {
        refresh();
        return gross <= 0 || voltageTier <= node.reachTier && buffer.acceptsAll(gross);
    }

    public boolean depositAll(long gross, int voltageTier) {
        if (!canDepositAll(gross, voltageTier)) return false;
        if (gross > 0) addPending(gross, voltageTier);
        return true;
    }

    private void addPending(long amount, int voltageTier) {
        buffer.addPending(amount, voltageTier);
        request(voltageTier);
    }

    private void request(int voltageTier) {
        if (cycle.parkedAt != null && voltageTier < cycle.parkedTier) resumeNow();
        else wake();
    }

    public void wake() {
        released = false;
        if (cycle.parkedAt != null) {
            if (account.removed || boundVersion != account.version) resumeNow();
            return;
        }
        if (!handle.scheduled()) handle.schedule(0);
    }

    void resume() {
        cycle.parkedAt = null;
        handle.schedule(0);
    }

    private void resumeNow() {
        if (cycle.parkedAt != null) cycle.parkedAt.unpark(this);
        resume();
    }

    public long pull(long want) {
        refresh();
        return pullAt(want, tier);
    }

    public long push(long gross) {
        refresh();
        return pushAt(gross, tier);
    }

    private long pullAt(long want, int voltageTier) {
        cycle.drew = true;
        long allowed = allowed(want, voltageTier);
        long got = 0;
        if (allowed > 0) {
            got = GridTransfer.draw(account, node, voltageTier, allowed, priority, outcome);
            rateBucket.take(got);
            flow.addOut(got);
            cycle.record(got, allowed, outcome.lineLimited);
        }
        hold.set(node, priority, shortfall(got, allowed), hold.put());
        return got;
    }

    private long pushAt(long gross, int voltageTier) {
        cycle.pushed = true;
        long allowed = allowed(gross, voltageTier);
        long done = 0;
        if (allowed > 0) {
            done = GridTransfer.put(account, node, voltageTier, allowed, extraLoss, priority, outcome);
            rateBucket.take(done);
            flow.addIn(done);
            cycle.record(done, allowed, outcome.lineLimited);
        }
        hold.set(node, priority, hold.draw(), shortfall(done, allowed));
        return done;
    }

    private long allowed(long amount, int voltageTier) {
        outcome.lineLimited = false;
        if (amount <= 0) return 0;
        if (voltageTier > node.reachTier) {
            cycle.starved = true;
            return 0;
        }
        long allowance = rateBucket.allowance(account.rate, GridClock.tick());
        cycle.rateLimited |= allowance < amount;
        return Math.min(amount, allowance);
    }

    private long shortfall(long moved, long allowed) {
        return moved < allowed && !outcome.lineLimited ? Math.min(allowed - moved, account.rate) : 0;
    }

    private void serveTick() {
        refresh();
        if (account.rate <= 0) {
            hold.clear();
            handle.schedule(GridScheduler.periodIndex(IDLE_TICKS));
            return;
        }
        int servedTier = buffer.servedTier(tier);
        if (cycle.stillBlocked(account, node, servedTier, buffer.demand > 0)) return;
        cycle.begin();
        int next = serveBuffers();
        if (service != null) next = sooner(next, service.serve(this));
        if (!cycle.drew) hold.set(node, priority, 0, hold.put());
        if (!cycle.pushed) hold.set(node, priority, hold.draw(), 0);
        cycle.finish(next, servedTier);
        reschedule(next, servedTier);
    }

    private int serveBuffers() {
        int next = 0;
        if (buffer.pending > 0) {
            buffer.pushed(pushAt(buffer.pending, Math.max(buffer.pendingTier, 0)));
            if (buffer.pending > 0) next = 1;
        }
        if (buffer.demand > 0) {
            long need = buffer.missingCredit();
            if (need > 0) buffer.credit += pullAt(need, Math.max(buffer.creditTier, 0));
            if (!buffer.demandMet()) next = 1;
        }
        return next;
    }

    private static int sooner(int next, int ticks) {
        if (ticks <= 0) return next;
        return next == 0 ? ticks : Math.min(next, ticks);
    }

    private void reschedule(int next, int servedTier) {
        if (cycle.shouldPark(next)) {
            handle.cancel();
            cycle.parkedAt = node;
            cycle.parkedTier = servedTier;
            node.park(this);
        } else if (next > 0) {
            handle.schedule(GridScheduler.periodIndex(next));
        } else {
            handle.cancel();
            hold.clear();
        }
    }

    public SettleResult checkSettle(long hi, long lo, int voltageTier, int duration) {
        refresh();
        return settlement.check(account, node, priority, hi, lo, voltageTier, duration);
    }

    public SettleResult checkSettle(BigInteger amount, int voltageTier, int duration) {
        var wide = PortSettlement.wide(amount);
        return wide == null ? PortSettlement.trivial(amount) : checkSettle(wide[0], wide[1], voltageTier, duration);
    }

    public SettleResult settle(long hi, long lo, int voltageTier, int duration) {
        refresh();
        var result = settlement.settle(account, node, priority, hi, lo, voltageTier, duration);
        if (result.ok()) flow.addOut(U126.clamp(hi, lo));
        return result;
    }

    public SettleResult settle(BigInteger amount, int voltageTier, int duration) {
        var wide = PortSettlement.wide(amount);
        return wide == null ? PortSettlement.trivial(amount) : settle(wide[0], wide[1], voltageTier, duration);
    }

    public boolean settleThen(long hi, long lo, int voltageTier, int duration, BooleanSupplier consume) {
        if (!settle(hi, lo, voltageTier, duration).ok()) return false;
        if (consume.getAsBoolean()) return true;
        refund();
        return false;
    }

    public boolean settleThen(BigInteger amount, int voltageTier, int duration, BooleanSupplier consume) {
        if (!settle(amount, voltageTier, duration).ok()) return false;
        if (consume.getAsBoolean()) return true;
        refund();
        return false;
    }

    public void refund() {
        flow.removeOut((long) Math.min(U126.MASK, settlement.refund(account)));
    }

    public double shortfallStorage() {
        return settlement.shortfallStorage();
    }

    public double reachableStorage(int voltageTier) {
        refresh();
        return account == EnergyAccount.NONE ? 0 : Settlement.reachableStorage(account, node, voltageTier);
    }

    public boolean canDepositLump(BigInteger gross, int voltageTier) {
        return lump(gross, voltageTier, false);
    }

    public boolean depositLump(BigInteger gross, int voltageTier) {
        return lump(gross, voltageTier, true);
    }

    private boolean lump(BigInteger gross, int voltageTier, boolean apply) {
        refresh();
        if (account == EnergyAccount.NONE || voltageTier > node.reachTier) return false;
        long held = node.putHeldAbove(priority);
        if (held > 0 && !GridTransfer.putLump(account, node, voltageTier, gross.add(BigInteger.valueOf(held)), extraLoss, false, priority)) return false;
        boolean ok = GridTransfer.putLump(account, node, voltageTier, gross, extraLoss, apply, priority);
        if (ok && apply) flow.addIn(gross.bitLength() > 63 ? U126.MASK : gross.longValue());
        return ok;
    }

    public void release() {
        handle.cancel();
        if (cycle.parkedAt != null) cycle.parkedAt.unpark(this);
        cycle.parkedAt = null;
        refresh();
        if (account != EnergyAccount.NONE) {
            if (buffer.pending > 0) buffer.pushed(pushAt(buffer.pending, Math.max(buffer.pendingTier, 0)));
            long rest = Math.max(0, buffer.pending);
            long leftover = U126.saturatedAdd(buffer.credit, rest - Loss.of(rest, Loss.combined(node.loss, extraLoss)));
            if (leftover > 0) account.returnToNode(node, leftover);
        }
        hold.clear();
        buffer.clear();
        if (index != null) index.remove(this);
        account = EnergyAccount.NONE;
        node = GridNode.EMPTY;
        owner = null;
        boundVersion = -1;
        released = true;
        settlement.forget();
    }

    public double inRate() {
        return flow.inRate();
    }

    public double outRate() {
        return flow.outRate();
    }

    private void refresh() {
        if (boundVersion != account.version | host.owner() != owner) rebind();
    }

    private void rebind() {
        UUID o = host.owner();
        EnergyAccount a = EnergyAccount.NONE;
        GridNode n = GridNode.EMPTY;
        var level = host.level();
        if (o != null && level instanceof ServerLevel) {
            a = WirelessGrid.accountOf(o);
            if (a != EnergyAccount.NONE) n = a.node(level.dimension());
        }
        owner = o;
        if (account != a) {
            hold.clear();
            if (cycle.parkedAt != null) resumeNow();
            if (index != null) index.remove(this);
            account = a;
            settlement.forget();
            if (!released && a != EnergyAccount.NONE && level instanceof ServerLevel serverLevel && host.machine() != null) PortIndex.of(serverLevel).add(this);
        }
        node = n;
        extraLoss = kind.extraLoss();
        boundVersion = a.version;
    }
}
