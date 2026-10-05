package com.gtocore.common.machine.tesseract;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyHandlerList;
import com.gregtechceu.gtceu.api.transfer.key.RemoteKeyTarget;

import net.minecraft.core.Direction;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.StorageAccess;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@SuppressWarnings({ "unchecked", "rawtypes" })
public final class TesseractCapCache<K extends AEKey> {

    public interface Holder extends IMachineFeature {

        boolean isCalled();

        void setCalled(boolean call);

        TesseractTargets getRemoteTargets();

        @Nullable
        default Direction getTargetSide(int i, @Nullable Direction side) {
            return side;
        }

        TesseractCapCache<AEItemKey> getItemCaps();

        TesseractCapCache<AEFluidKey> getFluidCaps();
    }

    private static final IKeyHandler[] NO_PARTS = new IKeyHandler[0];
    private static final Object NONE = new Object();

    private final Holder holder;
    private final AEKeyType type;
    private final Object[] slots = new Object[7];
    private boolean tainted;

    private TesseractCapCache(Holder holder, AEKeyType type) {
        this.holder = holder;
        this.type = type;
    }

    public static TesseractCapCache<AEItemKey> items(Holder holder) {
        return new TesseractCapCache<>(holder, AEKeyTypes.ITEMS);
    }

    public static TesseractCapCache<AEFluidKey> fluids(Holder holder) {
        return new TesseractCapCache<>(holder, AEKeyTypes.FLUIDS);
    }

    private static int index(@Nullable Direction side) {
        return side == null ? 6 : side.ordinal();
    }

    @Nullable
    public IKeyHandler<K> collect(@Nullable Direction side) {
        var holder = this.holder;
        if (holder.isCalled()) return null;
        tainted = false;
        int index = index(side);
        var cached = slots[index];
        if (cached != null) return cached == NONE ? null : (IKeyHandler<K>) cached;
        var targets = holder.getRemoteTargets();
        int size = targets.size();
        IKeyHandler<K>[] parts = size == 0 ? NO_PARTS : new IKeyHandler[size];
        for (int i = 0; i < size; i++) {
            parts[i] = fetch(targets.get(i), holder.getTargetSide(i, side));
        }
        var machine = holder.self();
        var handler = wrap(machine, side, compose(parts));
        if (!tainted && !machine.isRemote()) slots[index] = handler == null ? NONE : handler;
        return handler;
    }

    public void invalidate() {
        Arrays.fill(slots, null);
    }

    public void invalidate(@Nullable Direction side) {
        slots[index(side)] = null;
    }

    @Nullable
    private IKeyHandler<K> fetch(RemoteKeyTarget target, @Nullable Direction side) {
        var be = target.blockEntity();
        if (be == null) return null;
        var holder = this.holder;
        var nested = be instanceof MetaMachineBlockEntity machineBlockEntity && machineBlockEntity.metaMachine != holder && machineBlockEntity.metaMachine instanceof Holder h ? h : null;
        if (nested != null && nested.isCalled()) {
            tainted = true;
            return null;
        }
        holder.setCalled(true);
        var handler = (IKeyHandler<K>) target.find(side, type, StorageAccess.EXTRACT);
        holder.setCalled(false);
        if (nested != null && cacheOf(nested).tainted) tainted = true;
        return handler;
    }

    private TesseractCapCache<?> cacheOf(Holder holder) {
        return type == AEKeyTypes.ITEMS ? holder.getItemCaps() : holder.getFluidCaps();
    }

    @Nullable
    private IKeyHandler<K> compose(IKeyHandler<K>[] parts) {
        int count = 0;
        IKeyHandler<K> first = null;
        for (var part : parts) {
            if (part != null && count++ == 0) first = part;
        }
        if (count <= 1) return first;
        IKeyHandler<K>[] handlers = new IKeyHandler[count];
        int j = 0;
        for (var part : parts) {
            if (part != null) handlers[j++] = part;
        }
        return new KeyHandlerList<>(type, handlers);
    }

    @Nullable
    private IKeyHandler<K> wrap(MetaMachine machine, @Nullable Direction side, @Nullable IKeyHandler<K> base) {
        if (base == null || side == null) return base;
        var cover = machine.getCoverContainer().getCoverAtSide(side);
        if (cover == null) return base;
        return type == AEKeyTypes.ITEMS ? (IKeyHandler<K>) cover.getItemHandlerCap((IKeyHandler) base) : (IKeyHandler<K>) cover.getFluidHandlerCap((IKeyHandler) base);
    }
}
