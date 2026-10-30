package com.gtocore.common.blockentity;

import com.gtocore.common.pipe.steam.*;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityWatch;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutput;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.MEStorageHost;
import appeng.capabilities.Capabilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class SteamPipeBlockEntity extends PipeBlockEntity<SteamPipeType, SteamPipeProperties> implements MEStorageHost {

    private final SteamNetHandler[] handlers = new SteamNetHandler[6];
    @SuppressWarnings("unchecked")
    private final LazyOptional<MEStorage>[] storageCache = new LazyOptional[6];
    private WeakReference<SteamPipeNet> currentPipeNet = new WeakReference<>(null);
    private int storageEpoch;
    private boolean routing;

    public SteamPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.STORAGE) {
            return side != null && isConnected(side) ? storageCapability(side).cast() : LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public @Nullable MEStorage getMEStorage(@Nullable Direction side) {
        return side != null && isConnected(side) ? handler(side) : null;
    }

    @Override
    public int storageEpoch() {
        return storageEpoch;
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
    public void setRemoved() {
        super.setRemoved();
        invalidateCapabilityCache();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        invalidateCapabilityCache();
    }

    private SteamNetHandler handler(Direction side) {
        int i = side.ordinal();
        var handler = handlers[i];
        if (handler == null) handlers[i] = handler = new SteamNetHandler(this, side);
        return handler;
    }

    private LazyOptional<MEStorage> storageCapability(Direction side) {
        var cached = storageCache[side.ordinal()];
        if (cached != null) return cached;
        cached = LazyOptional.of(() -> handler(side));
        storageCache[side.ordinal()] = cached;
        return cached;
    }

    @Override
    protected void updateNetworkConnection(Direction side, boolean connected) {
        super.updateNetworkConnection(side, connected);
        invalidateCapabilityCache();
        updateAutoTransferTick();
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        updateAutoTransferTick();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        updateAutoTransferTick();
    }

    @Override
    protected void blockedChanged(boolean isBlocked) {
        updateAutoTransferTick();
    }

    @Override
    protected void onWorkingEnabledChanged() {
        updateAutoTransferTick();
    }

    private void updateAutoTransferTick() {
        updateTransferTick(isWorkingEnabled() && blockedSide != null && isBlocked(blockedSide), this::autoTransfer);
    }

    public long insert(AEKey what, long amount, Actionable mode, @Nullable Direction from) {
        if (amount <= 0 || isRemote() || routing || !isWorkingEnabled() || !SteamFluidFilter.isSteam(what)) return 0;
        var net = getSteamPipeNet();
        if (net == null) return 0;
        var actionSource = IActionSource.empty();
        var routes = net.getNetData(getPipeLongPos(), getPipePos());
        var remaining = amount;
        var accepted = 0L;
        if (mode == Actionable.SIMULATE) {
            for (var path : routes.array) {
                if (remaining <= 0) break;
                if (skipRoute(path, from)) continue;
                var handler = path.getHandler(getLevel());
                if (handler == null) continue;
                var moved = handler.insert(what, remaining, Actionable.SIMULATE, actionSource);
                if (moved <= 0) continue;
                remaining -= moved;
                accepted += moved;
            }
        } else {
            routing = true;
            for (var path : routes) {
                if (remaining <= 0) break;
                if (skipRoute(path, from)) continue;
                var handler = path.getHandler(getLevel());
                if (handler == null) continue;
                var moved = handler.insert(what, remaining, Actionable.MODULATE, actionSource);
                if (moved <= 0) continue;
                remaining -= moved;
                accepted += moved;
            }
            routing = false;
        }
        return accepted;
    }

    private boolean skipRoute(SteamRoutePath path, @Nullable Direction from) {
        if (path.getTargetPipe() != this) return false;
        var targetFacing = path.getTargetFacing();
        return targetFacing == from || this.autoTransfer && targetFacing != this.blockedSide;
    }

    private void autoTransfer() {
        if (!isWorkingEnabled()) {
            // a paused pipe releases its tick, it is restarted once the pipe is resumed
            updateTransferTick(false, this::autoTransfer);
            return;
        }
        if (getSteamPipeNet() == null) return;
        boolean hasStorage = false;
        autoTransfer = true;
        for (var facing : GTUtil.DIRECTIONS) {
            if (facing == blockedSide || !isConnected(facing)) continue;
            var be = getNeighborBlockEntity(facing);
            if (be == null || be instanceof PipeBlockEntity<?, ?>) continue;
            // the neighbour pushes into the pipe on its own, so do not pull from it as well
            var opposite = facing.getOpposite();
            if (be instanceof MetaMachineBlockEntity machineBlockEntity && machineBlockEntity instanceof IAutoOutput autoOutput && autoOutput.isAutoOutputtingFluids(opposite)) {
                continue;
            }
            var storage = SteamStorages.getStorage(be, opposite);
            if (storage == null) continue;
            hasStorage = true;
            var target = handler(facing);
            for (var key : SteamFluidFilter.steamKeys()) {
                SteamStorages.transferKey(storage, target, key, Long.MAX_VALUE);
            }
        }
        autoTransfer = false;
        if (!hasStorage) {
            transferSubs.unsubscribe();
            transferSubs = null;
        }
    }

    private SteamPipeNet getSteamPipeNet() {
        if (level == null || level.isClientSide) return null;
        var currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(getPipeLongPos())) return currentPipeNet;
        var worldNet = (LevelSteamPipeNet) getPipeBlock().getWorldPipeNet((ServerLevel) getLevel());
        currentPipeNet = worldNet.getNetFromPos(getPipePos(), getPipeLongPos());
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }
}
