package com.gtocore.api.ae2.stacks;

import com.gtolib.utils.MathUtil;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.item.ICustomItemStackHandler;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.storage.MEStorage;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongSupplier;
import java.util.function.Predicate;

public class AEItemKeyStackHandler extends MachineTrait implements ICustomItemStackHandler, ICapabilityTrait {

    @Nullable
    @Setter
    protected MEStorage storage;
    @Setter
    protected AEKeyMap<AEKey> map;
    @Setter
    protected Runnable onChange;

    @Setter
    protected long capacity;
    @Setter
    protected LongSupplier storageSupplier;

    @Setter
    protected IO capabilityIO = IO.BOTH;
    @Setter
    protected Predicate<Direction> capabilityValidator = GTUtil.FAVORABLE;

    /**
     * 非空时本处理器只指向该物品：读取、抽取与写入都只针对它；为空则沿用「存储里第一个物品」的透传行为。
     */
    @Setter
    @Nullable
    protected AEItemKey mark;

    public AEItemKeyStackHandler(MetaMachine machine) {
        super(machine);
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
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {}

    @Override
    public int getSlots() {
        if (storageSupplier == null) return 0;
        return storageSupplier.getAsLong() < capacity ? 2 : 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (map == null || slot != 0) return ItemStack.EMPTY;
        if (mark != null) {
            long amount = map.getAmount(mark);
            return amount < 1 ? ItemStack.EMPTY : mark.toStack(MathUtil.saturatedCast(amount));
        }
        for (var e : map) {
            if (e.getKey() instanceof AEItemKey key) {
                return key.toStack(MathUtil.saturatedCast(e.getLongValue()));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (storage == null || storageSupplier == null || storageSupplier.getAsLong() >= capacity) return stack;
        var count = stack.getCount();
        if (count < 1) return stack;
        AEItemKey key = mark;
        if (key != null) {
            // 已有标记：内容必与标记一致，直接用标记键，省掉 AEItemKey.of 的规范化/writeStackCaps 开销
            if (!key.matches(stack)) return stack;
        } else {
            key = AEItemKey.of(stack);
        }
        var r = count - storage.insert(key, count, simulate ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.empty());
        if (r < 1) return ItemStack.EMPTY;
        return stack.copyWithCount((int) r);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (storage == null || slot != 0 || onChange == null) return ItemStack.EMPTY;
        if (amount < 1) return ItemStack.EMPTY;
        if (mark != null) {
            var value = map.getAmount(mark);
            var extract = (int) Math.min(amount, value);
            if (extract < 1) return ItemStack.EMPTY;
            if (!simulate) {
                map.extract(mark, extract);
                onChange.run();
            }
            return mark.toStack(extract);
        }
        for (var it = map.iterator(); it.hasNext();) {
            var e = it.next();
            if (e.getKey() instanceof AEItemKey key) {
                var value = e.getLongValue();
                amount = (int) Math.min(amount, value);
                if (amount < 1) return ItemStack.EMPTY;
                var stack = key.toStack(amount);
                if (!simulate) {
                    if (value == amount) {
                        it.remove();
                    } else {
                        e.setValue(value - amount);
                    }
                    onChange.run();
                }
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return storage == null ? 0 : Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return storage != null && (mark == null || mark.matches(stack));
    }
}
