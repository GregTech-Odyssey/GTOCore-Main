package com.gtocore.common.machine.tesseract;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeFluidAdapter;
import com.gregtechceu.gtceu.api.transfer.forge.ForgeItemAdapter;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.api.transfer.key.KeyHandlerList;
import com.gregtechceu.gtceu.utils.LazyOptionalUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 超立方体按方向缓存代理处理器与 Forge 能力：每次查询逐目标比对处理器身份，变了才重建并作废已发出的 LazyOptional；
 * 目标失效经监听/依赖即时作废，查询链成环（目标处于调用中）时结果不入缓存。
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class TesseractCapCache<K extends AEKey> {

    public interface Holder extends IMachineFeature {

        boolean isCalled();

        void setCalled(boolean call);

        int getTotalBlockEntities();

        @Nullable
        BlockEntity getBlockEntity(int i);

        default Direction getSideForBlockEntity(int i, @Nullable Direction side) {
            return side;
        }

        TesseractCapCache<AEItemKey> getItemCaps();

        TesseractCapCache<AEFluidKey> getFluidCaps();
    }

    private static final IKeyHandler[] NO_PARTS = new IKeyHandler[0];
    private boolean lastBase;

    private final boolean items;
    private final Entry<K>[] slots = new Entry[7];
    private final NonNullConsumer listener = optional -> invalidate();
    @Nullable
    private ReferenceOpenHashSet<TesseractCapCache<?>> dependents;
    private boolean tainted;

    private TesseractCapCache(boolean items) {
        this.items = items;
    }

    public static TesseractCapCache<AEItemKey> items() {
        return new TesseractCapCache<>(true);
    }

    public static TesseractCapCache<AEFluidKey> fluids() {
        return new TesseractCapCache<>(false);
    }

    private static int index(@Nullable Direction side) {
        return side == null ? 6 : side.ordinal();
    }

    public boolean lastHadTargets() {
        return lastBase;
    }

    @Nullable
    public IKeyHandler<K> collect(Holder holder, @Nullable Direction side) {
        lastBase = false;
        if (holder.isCalled()) return null;
        tainted = false;
        var entry = slots[index(side)];
        int size = holder.getTotalBlockEntities();
        IKeyHandler<K>[] parts = entry != null && entry.parts.length == size ? entry.parts : null;
        boolean same = parts != null;
        for (int i = 0; i < size; i++) {
            var be = holder.getBlockEntity(i);
            var handler = be == null ? null : fetch(holder, be, holder.getSideForBlockEntity(i, side));
            if (same) {
                if (parts[i] == handler) continue;
                same = false;
                parts = Arrays.copyOf(parts, size);
            } else if (parts == null) {
                parts = new IKeyHandler[size];
            }
            parts[i] = handler;
        }
        var machine = holder.self();
        if (same) {
            lastBase = entry.base != null;
            var handler = wrap(machine, side, entry.base);
            if (handler == entry.handler || tainted || machine.isRemote()) return handler;
            return store(holder, side, entry.parts, entry.base, handler, false);
        }
        if (parts == null) parts = NO_PARTS;
        var base = compose(parts);
        lastBase = base != null;
        var handler = wrap(machine, side, base);
        if (tainted || machine.isRemote()) return handler;
        return store(holder, side, parts, base, handler, true);
    }

    public <T> LazyOptional<T> optional(@Nullable Direction side, IKeyHandler<K> handler) {
        var entry = slots[index(side)];
        if (entry == null || entry.handler != handler) return (LazyOptional<T>) create(handler);
        var optional = entry.optional;
        if (optional == null) {
            optional = create(handler);
            entry.optional = optional;
        }
        return (LazyOptional<T>) optional;
    }

    private LazyOptional<?> create(IKeyHandler<K> handler) {
        if (items) {
            var adapter = new ForgeItemAdapter((IKeyHandler) handler);
            return LazyOptional.of(() -> adapter);
        }
        var adapter = new ForgeFluidAdapter((IKeyHandler) handler);
        return LazyOptional.of(() -> adapter);
    }

    public void invalidate() {
        for (int i = 0; i < 7; i++) {
            drop(i);
        }
        notifyDependents();
    }

    public void invalidate(@Nullable Direction side) {
        drop(index(side));
        notifyDependents();
    }

    private void drop(int i) {
        var entry = slots[i];
        if (entry == null) return;
        slots[i] = null;
        if (entry.optional != null) entry.optional.invalidate();
    }

    private void notifyDependents() {
        var set = dependents;
        if (set == null || set.isEmpty()) return;
        dependents = null;
        for (var dependent : set) {
            dependent.invalidate();
        }
    }

    @Nullable
    private IKeyHandler<K> fetch(Holder holder, BlockEntity be, @Nullable Direction side) {
        if (be instanceof MetaMachineBlockEntity machineBlockEntity) {
            var machine = machineBlockEntity.metaMachine;
            var target = machine != holder && machine instanceof Holder h ? h : null;
            if (target != null && target.isCalled()) tainted = true;
            holder.setCalled(true);
            var handler = items ? machine.getItemHandlerCap(side, true) : machine.getFluidHandlerCap(side, true);
            holder.setCalled(false);
            if (target != null && cacheOf(target).tainted) tainted = true;
            return (IKeyHandler<K>) handler;
        }
        holder.setCalled(true);
        Object capability = items ? LazyOptionalUtil.get(be.getCapability(ForgeCapabilities.ITEM_HANDLER, side)) : LazyOptionalUtil.get(be.getCapability(ForgeCapabilities.FLUID_HANDLER, side));
        holder.setCalled(false);
        if (capability instanceof ForgeItemAdapter adapter) return items ? (IKeyHandler<K>) adapter.getHandler() : null;
        if (capability instanceof ForgeFluidAdapter adapter) return items ? null : (IKeyHandler<K>) adapter.getHandler();
        return null;
    }

    private TesseractCapCache<?> cacheOf(Holder holder) {
        return items ? holder.getItemCaps() : holder.getFluidCaps();
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
        return new KeyHandlerList<>(items ? AEKeyType.items() : AEKeyType.fluids(), handlers);
    }

    @Nullable
    private IKeyHandler<K> wrap(MetaMachine machine, @Nullable Direction side, @Nullable IKeyHandler<K> base) {
        if (base == null || side == null) return base;
        var cover = machine.getCoverContainer().getCoverAtSide(side);
        if (cover == null) return base;
        return items ? (IKeyHandler<K>) cover.getItemHandlerCap((IKeyHandler) base) : (IKeyHandler<K>) cover.getFluidHandlerCap((IKeyHandler) base);
    }

    @Nullable
    private IKeyHandler<K> store(Holder holder, @Nullable Direction side, IKeyHandler<K>[] parts, @Nullable IKeyHandler<K> base, @Nullable IKeyHandler<K> handler, boolean listen) {
        int i = index(side);
        var old = slots[i];
        var entry = new Entry<>(parts, base, handler);
        slots[i] = entry;
        if (listen) listen(holder, side);
        if (old == null) return handler;
        if (old.handler == handler) {
            entry.optional = old.optional;
            return handler;
        }
        if (old.optional != null) old.optional.invalidate();
        notifyDependents();
        return handler;
    }

    private void listen(Holder holder, @Nullable Direction side) {
        int size = holder.getTotalBlockEntities();
        for (int i = 0; i < size; i++) {
            var be = holder.getBlockEntity(i);
            if (be == null) continue;
            if (be instanceof MetaMachineBlockEntity machineBlockEntity && machineBlockEntity.metaMachine instanceof Holder target) {
                if (target != holder) cacheOf(target).addDependent(this);
                continue;
            }
            var face = holder.getSideForBlockEntity(i, side);
            holder.setCalled(true);
            LazyOptional<?> optional = items ? be.getCapability(ForgeCapabilities.ITEM_HANDLER, face) : be.getCapability(ForgeCapabilities.FLUID_HANDLER, face);
            holder.setCalled(false);
            if (optional.isPresent()) optional.addListener(listener);
        }
    }

    private void addDependent(TesseractCapCache<?> dependent) {
        var set = dependents;
        if (set == null) {
            set = new ReferenceOpenHashSet<>();
            dependents = set;
        }
        set.add(dependent);
    }

    private static final class Entry<K extends AEKey> {

        private final IKeyHandler<K>[] parts;
        @Nullable
        private final IKeyHandler<K> base;
        @Nullable
        private final IKeyHandler<K> handler;
        @Nullable
        private LazyOptional<?> optional;

        private Entry(IKeyHandler<K>[] parts, @Nullable IKeyHandler<K> base, @Nullable IKeyHandler<K> handler) {
            this.parts = parts;
            this.base = base;
            this.handler = handler;
        }
    }
}
