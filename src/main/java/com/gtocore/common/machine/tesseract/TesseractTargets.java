package com.gtocore.common.machine.tesseract;

import com.gregtechceu.gtceu.api.transfer.key.RemoteKeyTarget;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import com.suried.gtoutils.schedule.Cell;
import com.suried.gtoutils.schedule.CoalescedTask;
import com.suried.gtoutils.schedule.Schedulers;
import com.suried.gtoutils.schedule.Scope;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class TesseractTargets {

    private static final RemoteKeyTarget[] NONE = new RemoteKeyTarget[0];

    private final TesseractCapCache.Holder holder;
    private final Cell<Integer> bound = Cell.of(0);
    private final Runnable changed = this::onTargetChanged;
    private RemoteKeyTarget[] targets = NONE;
    private int size;
    @Nullable
    private CoalescedTask refresh;

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
            if (array[i] == null) array[i] = new RemoteKeyTarget();
        }
        if (this.size == size) return;
        this.size = size;
        rebound();
    }

    public void bind(int i, @Nullable Level level, @Nullable BlockPos pos) {
        if (targets[i].bind(level, pos)) rebound();
    }

    private void rebound() {
        bound.set(bound.peek() + 1);
    }

    public void mount(Scope scope) {
        scope.select(bound::get, (mounted, version) -> {
            var array = targets;
            for (int i = 0, n = size; i < n; i++) array[i].mount(mounted, changed);
            if (holder.getItemCaps().handedOut() || holder.getFluidCaps().handedOut()) onTargetChanged();
        });
    }

    private void onTargetChanged() {
        var machine = holder.self();
        if (machine.isRemoved()) return;
        if (!holder.isCalled()) {
            machine.notifyExposureChanged();
            return;
        }
        var level = machine.getLevel();
        if (level == null) return;
        var task = refresh;
        if (task == null) refresh = task = new CoalescedTask(this::refresh);
        Schedulers.of(level).submit(task);
    }

    private void refresh() {
        var machine = holder.self();
        if (!machine.isRemoved()) machine.notifyExposureChanged();
    }
}
