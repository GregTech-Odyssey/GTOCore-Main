package com.gtocore.common.blockentity;

import com.gtocore.common.pipe.universal.*;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityWatch;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.MEStorageHost;
import appeng.capabilities.Capabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.function.ObjLongConsumer;

public final class UniversalPipeBlockEntity extends PipeBlockEntity<UniversalPipeType, UniversalPipeProperties> implements MEStorageHost {

    private final EnumMap<Direction, NetStorage> handlers = new EnumMap<>(Direction.class);
    @SuppressWarnings("unchecked")
    private final LazyOptional<MEStorage>[] storageCache = new LazyOptional[6];
    private final KeyCounter scratch = new KeyCounter();
    private final ObjLongConsumer<AEKey> keyTransfer = this::transferKey;
    private WeakReference<UniversalPipeNet> currentPipeNet = new WeakReference<>(null);
    private int storageEpoch;
    private boolean routing;
    @Nullable
    private MEStorage transferSource;
    @Nullable
    private MEStorage transferTarget;
    private long transferredItems;
    private long transferredFluids;
    private int transferTimer;

    public UniversalPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        invalidateCapabilityCache();
    }

    @Override
    public void invalidateCapabilityCache() {
        storageEpoch = storageEpoch + 1 & Integer.MAX_VALUE;
        for (int i = 0; i < 6; i++) {
            var cached = storageCache[i];
            if (cached != null) {
                storageCache[i] = null;
                cached.invalidate();
            }
        }
        BlockEntityWatch.changed(this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.STORAGE) {
            return side != null && isConnected(side) ? storageCapability(side).cast() : LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    private LazyOptional<MEStorage> storageCapability(Direction side) {
        var cached = storageCache[side.ordinal()];
        if (cached != null) return cached;
        var handler = handler(side);
        if (handler == null) return LazyOptional.empty();
        cached = LazyOptional.of(() -> handler);
        storageCache[side.ordinal()] = cached;
        return cached;
    }

    @Override
    public @Nullable MEStorage getMEStorage(@Nullable Direction side) {
        return side != null && isConnected(side) ? handler(side) : null;
    }

    @Override
    public int storageEpoch() {
        return storageEpoch;
    }

    @Nullable
    private NetStorage handler(Direction side) {
        if (isRemote()) return null;
        if (getUniversalPipeNet() == null) return null;
        var handlers = this.handlers;
        if (handlers.isEmpty()) initHandlers();
        return handlers.get(side);
    }

    private void initHandlers() {
        for (var facing : GTUtil.DIRECTIONS) {
            handlers.put(facing, new NetStorage(this, facing));
        }
    }

    @Nullable
    public UniversalPipeNet getUniversalPipeNet() {
        if (level == null || level.isClientSide) return null;
        var currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(longPos)) return currentPipeNet;
        var worldNet = (LevelUniversalPipeNet) getPipeBlock().getWorldPipeNet((ServerLevel) getLevel());
        currentPipeNet = worldNet.getNetFromPos(getPipePos(), longPos);
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
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
    protected void updateNetworkConnection(Direction side, boolean connected) {
        super.updateNetworkConnection(side, connected);
        invalidateCapabilityCache();
        updateTransferTick(blockedSide != null && isBlocked(blockedSide), this::autoTransfer);
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        updateTransferTick(blockedSide != null && isBlocked(blockedSide), this::autoTransfer);
    }

    private void autoTransfer() {
        if (getUniversalPipeNet() == null) return;
        boolean hasSource = false;
        autoTransfer = true;
        for (var facing : GTUtil.DIRECTIONS) {
            if (available(true) <= 0 && available(false) <= 0) break;
            if (facing == blockedSide || !isConnected(facing)) continue;
            var neighbour = getNeighborBlockEntity(facing);
            if (neighbour == null || neighbour instanceof PipeBlockEntity<?, ?>) continue;
            var source = UniversalStorages.getStorage(neighbour, facing.getOpposite());
            if (source == null) continue;
            var target = handler(facing);
            if (target == null) continue;
            hasSource = true;
            transfer(source, target);
        }
        autoTransfer = false;
        if (!hasSource) {
            transferSubs.unsubscribe();
            transferSubs = null;
        }
    }

    private void transfer(MEStorage from, MEStorage to) {
        transferSource = from;
        transferTarget = to;
        scratch.clear();
        from.getAvailableStacks(scratch);
        scratch.fastForEach(keyTransfer);
        transferSource = null;
        transferTarget = null;
    }

    private void transferKey(AEKey key, long amount) {
        if (amount <= 0) return;
        var from = transferSource;
        var to = transferTarget;
        if (from == null || to == null) return;
        if (available(key.getType() == AEKeyTypes.FLUIDS) <= 0) return;
        UniversalStorages.transferKey(from, to, key, amount);
    }

    private void updateTransferredState() {
        int time = getOffsetTimer();
        int dif = time - transferTimer;
        if (dif >= 20 || dif < 0) {
            transferredItems = 0;
            transferredFluids = 0;
            transferTimer = time;
        }
    }

    private long available(boolean fluid) {
        updateTransferredState();
        var properties = getNodeData();
        return fluid ? properties.fluidThroughput() - transferredFluids : properties.itemThroughput() - transferredItems;
    }

    private void addTransferred(boolean fluid, long amount) {
        updateTransferredState();
        if (fluid) {
            transferredFluids += amount;
        } else {
            transferredItems += amount;
        }
    }

    public static final class NetStorage implements MEStorage {

        private final UniversalPipeBlockEntity pipe;
        private final Direction facing;

        private NetStorage(UniversalPipeBlockEntity pipe, Direction facing) {
            this.pipe = pipe;
            this.facing = facing;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (amount <= 0) return 0;
            if (pipe.isInValid() || pipe.routing || pipe.isBlocked(facing)) return 0;
            boolean fluid = what.getType() == AEKeyTypes.FLUIDS;
            long allowed = Math.min(amount, pipe.available(fluid));
            if (allowed <= 0) return 0;
            var net = pipe.getUniversalPipeNet();
            if (net == null) return 0;
            long inserted = 0;
            var level = net.getLevel();
            pipe.routing = true;
            for (var route : net.getNetData(pipe.getPipeLongPos(), pipe.getPipePos(), facing)) {
                long left = allowed - inserted;
                if (left <= 0) break;
                if (pipe.autoTransfer && route.getTargetPipe() == pipe && route.getTargetFacing() != pipe.blockedSide) continue;
                var target = route.getHandler(level);
                if (target == null) continue;
                inserted += target.insert(what, left, mode, source);
            }
            pipe.routing = false;
            if (mode == Actionable.MODULATE) pipe.addTransferred(fluid, inserted);
            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            return 0;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {}

        @Override
        public KeyCounter getAvailableStacks() {
            return KeyCounter.empty();
        }

        @Override
        public Component getDescription() {
            return Component.translatable(pipe.getBlockState().getBlock().getDescriptionId());
        }
    }
}
