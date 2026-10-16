package com.gtocore.common.machine.tesseract;

import com.gregtechceu.gtceu.api.transfer.key.RemoteKeyTarget;
import com.gregtechceu.gtceu.utils.TaskHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class TesseractTargets implements RemoteKeyTarget.Listener {

    private static final RemoteKeyTarget[] NONE = new RemoteKeyTarget[0];

    private final TesseractCapCache.Holder holder;
    private RemoteKeyTarget[] targets = NONE;
    private int size;
    private boolean subscribed;
    @Nullable
    private TaskHandler.ReusableTask refresh;

    public TesseractTargets(TesseractCapCache.Holder holder) {
        this.holder = holder;
    }

    public int size() {
        return size;
    }

    public RemoteKeyTarget get(int i) {
        return targets[i];
    }

    @Nullable
    public RemoteKeyTarget find(int i) {
        return i < size ? targets[i] : null;
    }

    public void resize(int size) {
        var array = targets;
        if (size > array.length) targets = array = Arrays.copyOf(array, size);
        for (int i = size; i < this.size; i++) array[i].bind(null, null);
        for (int i = this.size; i < size; i++) {
            if (array[i] == null) {
                var target = new RemoteKeyTarget();
                if (subscribed) target.subscribe(this);
                array[i] = target;
            }
        }
        this.size = size;
    }

    public void bind(int i, @Nullable Level level, @Nullable BlockPos pos) {
        targets[i].bind(level, pos);
    }

    public void bind(int i, net.minecraft.server.MinecraftServer server, net.minecraft.resources.ResourceKey<Level> key, BlockPos pos) {
        ((com.gtolib.api.dimension.DimensionRemoteTarget) (Object) targets[i]).gtolib$bindDimension(server, key, pos);
    }

    public void subscribe(boolean subscribed) {
        if (this.subscribed == subscribed) return;
        this.subscribed = subscribed;
        var listener = subscribed ? this : null;
        for (var target : targets) {
            if (target != null) target.subscribe(listener);
        }
    }

    @Override
    public void onTargetChanged(RemoteKeyTarget target) {
        var machine = holder.self();
        if (machine.isRemoved()) return;
        if (!holder.isCalled()) {
            machine.notifyExposureChanged();
            return;
        }
        var level = machine.getLevel();
        if (level == null) return;
        var task = refresh;
        if (task == null) refresh = task = new TaskHandler.ReusableTask(this::refresh);
        TaskHandler.enqueueTask(level, task);
    }

    private void refresh() {
        var machine = holder.self();
        if (!machine.isRemoved()) machine.notifyExposureChanged();
    }
}
