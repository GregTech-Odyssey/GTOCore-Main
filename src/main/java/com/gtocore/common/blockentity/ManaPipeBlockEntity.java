package com.gtocore.common.blockentity;

import com.gtocore.common.pipe.mana.*;
import com.gtocore.utils.ManaUnification;

import com.gtolib.utils.MathUtil;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import com.gto.fastcollection.LoopIterator;
import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

import java.lang.ref.WeakReference;

public final class ManaPipeBlockEntity extends PipeBlockEntity<ManaPipeType, ManaPipeProperties> implements ManaCollector, ISourceTile {

    private WeakReference<ManaPipeNet> currentPipeNet = new WeakReference<>(null);
    private final SideReceiver[] sideReceivers = new SideReceiver[GTUtil.DIRECTIONS.length];

    private long transferredMana;
    private int transferTimer;

    public ManaPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            if ((side == null || isConnected(side)) && getManaPipeNet() != null) {
                ManaReceiver receiver = side == null ? this : sideReceiver(side);
                return BotaniaForgeCapabilities.MANA_RECEIVER.orEmpty(cap, LazyOptional.of(() -> receiver));
            }
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (blockedSide != null && isBlocked(blockedSide)) {
            updateTransferTick(true, this::autoTransfer);
        }
    }

    @Override
    protected void blockedChanged(boolean isBlocked) {
        updateTransferTick(isBlocked && blockedSide != null, this::autoTransfer);
    }

    @Override
    protected void updateNetworkConnection(@NotNull Direction side, boolean connected) {
        super.updateNetworkConnection(side, connected);
        this.updateTransferTick(this.blockedSide != null && this.isBlocked(this.blockedSide), this::autoTransfer);
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        updateTransferTick(blockedSide != null && isBlocked(blockedSide), this::autoTransfer);
    }

    private void autoTransfer() {
        if (getManaPipeNet() == null) return;
        boolean hasHandler = false;
        autoTransfer = true;
        for (var facing : GTUtil.DIRECTIONS) {
            if (facing == blockedSide || !isConnected(facing)) continue;
            var be = getNeighborBlockEntity(facing);
            if (be == null || be instanceof PipeBlockEntity<?, ?>) continue;
            var opposite = facing.getOpposite();
            if (GTCapabilityHelper.getBlockEntityCapability(BotaniaForgeCapabilities.MANA_RECEIVER, be, opposite) instanceof ManaPool pool) {
                hasHandler = true;
                var manaInPool = pool.getCurrentMana();
                if (manaInPool > 0 && !isFull()) {
                    int manaMissing = getMaxMana() - getCurrentMana();
                    int manaToRemove = (int) Math.min(Math.min(manaInPool, manaMissing), availableMana());
                    if (manaToRemove > 0) {
                        pool.receiveMana(-manaToRemove);
                        receiveMana(manaToRemove);
                    }
                }
            } else if (be instanceof ISourceTile sourceTile) {
                hasHandler = true;
                var sourceInTile = sourceTile.getSourceLong();
                var sourceMissing = getMaxSourceLong() - getSourceLong();
                if (sourceInTile > 0 && sourceMissing > 0) {
                    long sourceToRemove = Math.min(Math.min(sourceInTile, sourceMissing), ManaUnification.manaToSource(availableMana()));
                    if (sourceToRemove > 0) {
                        sourceTile.removeSourceLong(sourceToRemove);
                        addSourceLong(sourceToRemove);
                    }
                }
            }
        }
        autoTransfer = false;
        if (!hasHandler) {
            transferSubs.unsubscribe();
            transferSubs = null;
        }
    }

    private ManaPipeNet getManaPipeNet() {
        if (level == null || level.isClientSide) return null;
        var currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(longPos)) return currentPipeNet;
        var worldNet = (LevelManaPipeNet) getPipeBlock().getWorldPipeNet((ServerLevel) getLevel());
        currentPipeNet = worldNet.getNetFromPos(getPipePos(), longPos);
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    @Override
    public void onClientDisplayTick() {}

    @Override
    public float getManaYieldMultiplier(ManaBurst burst) {
        return 1;
    }

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
    }

    @Override
    public int getCurrentMana() {
        return getCurrentMana(null);
    }

    private int getCurrentMana(@Nullable Direction entry) {
        var net = getManaPipeNet();
        if (net == null) return 0;
        return currentMana(net, MathUtil.saturatedCast(acceptableMana(net, entry)), entry);
    }

    private int currentMana(ManaPipeNet net, int acceptable, @Nullable Direction entry) {
        long mana = 0;
        for (var path : net.getNetData(longPos, worldPosition)) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), entry)) continue;
            var handler = path.getHandler(level);
            if (handler == null) continue;
            mana += handler.getCurrentMana();
        }
        for (var path : net.getSourceNetData(longPos, worldPosition)) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), entry)) continue;
            var handler = path.getHandler(level);
            if (handler == null) continue;
            mana += ManaUnification.sourceToMana(handler.getSourceLong());
        }
        return (int) Math.min(mana, Integer.MAX_VALUE - acceptable);
    }

    @Override
    public boolean isFull() {
        return isFull(null);
    }

    private boolean isFull(@Nullable Direction entry) {
        var net = getManaPipeNet();
        return net == null || acceptableMana(net, entry) <= 0;
    }

    @Override
    public void receiveMana(int mana) {
        receiveMana(mana, null);
    }

    private void receiveMana(int mana, @Nullable Direction entry) {
        if (mana <= 0) return;
        var net = getManaPipeNet();
        if (net == null) return;
        long left = availableMana();
        if (left <= 0) return;
        if (mana > left) mana = (int) left;
        var delivered = mana - routeMana(net.getNetData(longPos, worldPosition), mana, entry);
        var undelivered = mana - delivered;
        if (undelivered > 0) {
            var source = ManaUnification.manaToSource(undelivered);
            if (source > 0) {
                var deliveredSource = source - routeSource(net.getSourceNetData(longPos, worldPosition), source, entry);
                delivered += MathUtil.saturatedCast(ManaUnification.sourceToMana(deliveredSource));
            }
        }
        addTransferredMana(delivered);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return canReceiveManaFromBursts(null);
    }

    private boolean canReceiveManaFromBursts(@Nullable Direction entry) {
        var net = getManaPipeNet();
        if (net == null) return false;
        for (var path : net.getNetData(longPos, worldPosition)) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), entry)) continue;
            var handler = path.getHandler(level);
            if (handler != null && handler.canReceiveManaFromBursts()) return true;
        }
        for (var path : net.getSourceNetData(longPos, worldPosition)) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), entry)) continue;
            var handler = path.getHandler(level);
            if (handler != null && canAcceptSource(handler)) return true;
        }
        return false;
    }

    @Override
    public int getMaxMana() {
        return getMaxMana((Direction) null);
    }

    private int getMaxMana(@Nullable Direction entry) {
        var net = getManaPipeNet();
        if (net == null) return 0;
        int acceptable = MathUtil.saturatedCast(acceptableMana(net, entry));
        return currentMana(net, acceptable, entry) + acceptable;
    }

    private static int getMaxMana(@Nullable ManaReceiver receiver) {
        if (receiver instanceof ManaCollector collector) return collector.getMaxMana();
        if (receiver instanceof ManaPool pool) return pool.getMaxMana();
        if (receiver instanceof SparkAttachable attachable) return attachable.getAvailableSpaceForMana();
        return receiver == null || receiver.isFull() ? 0 : 10000000;
    }

    @Override
    public long getTransferRateLong() {
        return ManaUnification.manaToSource(getNodeData().manaPerSecond());
    }

    @Override
    public int getTransferRate() {
        return ISourceTile.clampToInt(getTransferRateLong());
    }

    @Override
    public boolean canAcceptSource() {
        var net = getManaPipeNet();
        return net != null && acceptableSource(net) > 0;
    }

    @Override
    public int getSource() {
        return ISourceTile.clampToInt(getSourceLong());
    }

    @Override
    public long getSourceLong() {
        var net = getManaPipeNet();
        if (net == null) return 0;
        long source = 0;
        for (var path : net.getSourceNetData(longPos, worldPosition)) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), null)) continue;
            var handler = path.getHandler(level);
            if (handler == null) continue;
            source += handler.getSourceLong();
        }
        return source;
    }

    @Override
    public int getMaxSource() {
        return ISourceTile.clampToInt(getMaxSourceLong());
    }

    @Override
    public long getMaxSourceLong() {
        var net = getManaPipeNet();
        if (net == null) return 0;
        return saturatedAdd(getSourceLong(), acceptableSource(net));
    }

    @Override
    public void setMaxSource(int maxSource) {}

    @Override
    public int setSource(int source) {
        return ISourceTile.clampToInt(setSourceLong(source));
    }

    @Override
    public long setSourceLong(long source) {
        var current = getSourceLong();
        if (source > current) addSourceLong(source - current);
        return getSourceLong();
    }

    @Override
    public int addSource(int source) {
        return ISourceTile.clampToInt(addSourceLong(source));
    }

    @Override
    public long addSourceLong(long source) {
        if (source <= 0) return getSourceLong();
        var net = getManaPipeNet();
        if (net == null) return getSourceLong();
        long affordable = ManaUnification.manaToSource(availableMana());
        if (affordable <= 0) return getSourceLong();
        if (source > affordable) source = affordable;
        var delivered = source - routeSource(net.getSourceNetData(longPos, worldPosition), source, null);
        var undelivered = source - delivered;
        if (undelivered > 0) {
            var mana = MathUtil.saturatedCast(ManaUnification.sourceToMana(undelivered));
            if (mana > 0) {
                var deliveredMana = mana - routeMana(net.getNetData(longPos, worldPosition), mana, null);
                delivered += ManaUnification.manaToSource(deliveredMana);
            }
        }
        addTransferredMana(ManaUnification.sourceToMana(delivered));
        return getSourceLong();
    }

    @Override
    public int removeSource(int amount) {
        return ISourceTile.clampToInt(removeSourceLong(amount));
    }

    @Override
    public long removeSourceLong(long amount) {
        if (amount <= 0) return getSourceLong();
        var net = getManaPipeNet();
        if (net == null) return 0;
        var left = amount;
        for (var path : net.getSourceNetData(longPos, worldPosition)) {
            if (left <= 0) break;
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), null)) continue;
            var handler = path.getHandler(level);
            if (handler == null) continue;
            var take = Math.min(left, handler.getSourceLong());
            if (take > 0) {
                handler.removeSourceLong(take);
                left -= take;
            }
        }
        return getSourceLong();
    }

    private long acceptableMana(ManaPipeNet net, @Nullable Direction entry) {
        long space = saturatedAdd(manaRouteSpace(net, entry), ManaUnification.sourceToMana(sourceRouteSpace(net, entry)));
        return Math.max(0, Math.min(availableMana(), space));
    }

    private long acceptableSource(ManaPipeNet net) {
        long space = saturatedAdd(sourceRouteSpace(net, null), ManaUnification.manaToSource(manaRouteSpace(net, null)));
        return Math.max(0, Math.min(ManaUnification.manaToSource(Math.max(0, availableMana())), space));
    }

    private long manaRouteSpace(ManaPipeNet net, @Nullable Direction entry) {
        var routes = net.getNetData(longPos, worldPosition);
        var seen = routes.size > 1 ? new ReferenceOpenHashSet<ManaReceiver>(routes.size) : null;
        long space = 0;
        for (var path : routes) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), entry)) continue;
            var handler = path.getHandler(level);
            if (handler == null || handler.isFull() || !handler.canReceiveManaFromBursts()) continue;
            if (seen != null && !seen.add(handler)) continue;
            space = saturatedAdd(space, Math.max(0, getMaxMana(handler) - handler.getCurrentMana()));
        }
        return space;
    }

    private long sourceRouteSpace(ManaPipeNet net, @Nullable Direction entry) {
        var routes = net.getSourceNetData(longPos, worldPosition);
        var seen = routes.size > 1 ? new ReferenceOpenHashSet<ISourceTile>(routes.size) : null;
        long space = 0;
        for (var path : routes) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), entry)) continue;
            var handler = path.getHandler(level);
            if (handler == null || !canAcceptSource(handler)) continue;
            if (seen != null && !seen.add(handler)) continue;
            space = saturatedAdd(space, handler.getMaxSourceLong() - handler.getSourceLong());
        }
        return space;
    }

    private static long saturatedAdd(long a, long b) {
        long r = a + b;
        return r < 0 ? Long.MAX_VALUE : r;
    }

    private int routeMana(LoopIterator<ManaRoutePath> routes, int mana, @Nullable Direction entry) {
        for (var path : routes) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), entry)) continue;
            var handler = path.getHandler(level);
            if (handler == null || handler.isFull() || !handler.canReceiveManaFromBursts()) continue;
            var canReceive = Math.min(mana, getMaxMana(handler) - handler.getCurrentMana());
            if (canReceive > 0) {
                handler.receiveMana(canReceive);
                mana -= canReceive;
                if (mana <= 0) return 0;
            }
        }
        return Math.max(0, mana);
    }

    private long routeSource(LoopIterator<SourceRoutePath> routes, long source, @Nullable Direction entry) {
        for (var path : routes) {
            if (skipRoute(path.getTargetPipe(), path.getTargetFacing(), entry)) continue;
            var handler = path.getHandler(level);
            if (handler == null || !canAcceptSource(handler)) continue;
            var canReceive = Math.min(source, handler.getMaxSourceLong() - handler.getSourceLong());
            if (canReceive > 0) {
                handler.addSourceLong(canReceive);
                source -= canReceive;
                if (source <= 0) return 0;
            }
        }
        return Math.max(0, source);
    }

    private boolean skipRoute(ManaPipeBlockEntity targetPipe, Direction targetFacing, @Nullable Direction entry) {
        if (targetPipe != this) return false;
        return this.autoTransfer ? this.blockedSide != targetFacing : targetFacing == entry;
    }

    private SideReceiver sideReceiver(Direction side) {
        var receiver = sideReceivers[side.ordinal()];
        if (receiver == null) {
            receiver = new SideReceiver(side);
            sideReceivers[side.ordinal()] = receiver;
        }
        return receiver;
    }

    /// 从某一面送进来的魔力不再从同一面送回去，避免送出方（如超级魔力缸）把魔力绕一圈送回自己
    private final class SideReceiver implements ManaCollector {

        private final Direction side;

        private SideReceiver(Direction side) {
            this.side = side;
        }

        @Override
        public Level getManaReceiverLevel() {
            return level;
        }

        @Override
        public BlockPos getManaReceiverPos() {
            return worldPosition;
        }

        @Override
        public int getCurrentMana() {
            return ManaPipeBlockEntity.this.getCurrentMana(side);
        }

        @Override
        public boolean isFull() {
            return ManaPipeBlockEntity.this.isFull(side);
        }

        @Override
        public void receiveMana(int mana) {
            ManaPipeBlockEntity.this.receiveMana(mana, side);
        }

        @Override
        public boolean canReceiveManaFromBursts() {
            return ManaPipeBlockEntity.this.canReceiveManaFromBursts(side);
        }

        @Override
        public void onClientDisplayTick() {}

        @Override
        public float getManaYieldMultiplier(ManaBurst burst) {
            return 1;
        }

        @Override
        public int getMaxMana() {
            return ManaPipeBlockEntity.this.getMaxMana(side);
        }
    }

    private static boolean canAcceptSource(ISourceTile sourceTile) {
        return sourceTile.canAcceptSource() && sourceTile.getSourceLong() < sourceTile.getMaxSourceLong();
    }

    private void updateTransferredState() {
        int time = getOffsetTimer();
        int dif = time - transferTimer;
        if (dif >= 20 || dif < 0) {
            transferredMana = 0;
            transferTimer = time;
        }
    }

    private long availableMana() {
        updateTransferredState();
        return getNodeData().manaPerSecond() - transferredMana;
    }

    private void addTransferredMana(long amount) {
        updateTransferredState();
        transferredMana += amount;
    }
}
