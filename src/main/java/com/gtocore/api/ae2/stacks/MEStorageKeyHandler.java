package com.gtocore.api.ae2.stacks;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.key.IKeyHandler;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongSupplier;
import java.util.function.Predicate;

public final class MEStorageKeyHandler<K extends AEKey> extends MachineTrait implements IKeyHandler<K>, ICapabilityTrait {

    private final AEKeyType type;

    @Nullable
    @Setter
    private MEStorage storage;
    @Nullable
    @Setter
    private AEKeyMap<AEKey> map;
    @Nullable
    @Setter
    private Runnable onChange;

    @Setter
    private long capacity;
    @Nullable
    @Setter
    private LongSupplier storageSupplier;

    @Setter
    private IO capabilityIO = IO.BOTH;
    @Setter
    private Predicate<Direction> capabilityValidator = GTUtil.FAVORABLE;

    @Setter
    @Nullable
    private K mark;

    public MEStorageKeyHandler(MetaMachine machine, AEKeyType type) {
        super(machine);
        this.type = type;
    }

    @Override
    public IO getCapabilityIO() {
        return capabilityIO;
    }

    @Override
    public Predicate<Direction> getCapabilityValidator() {
        return capabilityValidator;
    }

    @Override
    public AEKeyType keyType() {
        return type;
    }

    @Override
    public int size() {
        var supplier = storageSupplier;
        if (supplier == null) return 0;
        return supplier.getAsLong() < capacity ? 2 : 1;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private K first() {
        var m = map;
        if (m == null) return null;
        if (mark != null) return m.getAmount(mark) > 0 ? mark : null;
        for (var e : m) {
            if (e.getKey().getType() == type && e.getLongValue() > 0) return (K) e.getKey();
        }
        return null;
    }

    @Override
    public @Nullable K keyAt(int slot) {
        return slot == 0 ? first() : null;
    }

    @Override
    public long amountAt(int slot) {
        if (slot != 0) return 0;
        var key = first();
        return key == null ? 0 : map.getAmount(key);
    }

    @Override
    public long slotLimit(int slot) {
        return storage == null ? 0 : Long.MAX_VALUE;
    }

    @Override
    public long insert(int slot, K key, long amount, boolean simulate) {
        return insert(key, amount, simulate);
    }

    @Override
    public long insert(K key, long amount, boolean simulate) {
        var s = storage;
        if (s == null || storageSupplier == null || storageSupplier.getAsLong() >= capacity || amount < 1) return 0;
        if (mark != null && !mark.equals(key)) return 0;
        return s.insert(key, amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.empty());
    }

    @Override
    public long extract(int slot, K key, long amount, boolean simulate) {
        return slot == 0 ? extract(key, amount, simulate) : 0;
    }

    @Override
    public long extract(K key, long amount, boolean simulate) {
        var m = map;
        var change = onChange;
        if (storage == null || m == null || change == null || amount < 1) return 0;
        if (mark != null && !mark.equals(key)) return 0;
        long n = Math.min(amount, m.getAmount(key));
        if (n < 1) return 0;
        if (!simulate) {
            m.extract(key, n);
            change.run();
        }
        return n;
    }

    @Override
    public long count(K key) {
        var m = map;
        if (m == null || (mark != null && !mark.equals(key))) return 0;
        return m.getAmount(key);
    }
}
