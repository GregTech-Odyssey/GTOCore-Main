package com.gtocore.api.ae2.stacks;

import com.gtolib.utils.MathUtil;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.ICapabilityTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.transfer.fluid.ICustomFluidStackHandler;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.storage.MEStorage;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongSupplier;
import java.util.function.Predicate;

public class AEFluidKeyStackHandler extends MachineTrait implements ICustomFluidStackHandler, ICapabilityTrait {

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
     * 非空时本处理器只指向该流体：读取、抽取与写入都只针对它；为空则沿用「存储里第一个流体」的透传行为。
     */
    @Setter
    @Nullable
    protected AEFluidKey mark;

    public AEFluidKeyStackHandler(MetaMachine machine) {
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
    public void setFluidInTank(int i, FluidStack fluidStack) {}

    @Override
    public int getTanks() {
        if (storageSupplier == null) return 0;
        return storageSupplier.getAsLong() < capacity ? 2 : 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (map == null || tank != 0) return FluidStack.EMPTY;
        if (mark != null) {
            long amount = map.getAmount(mark);
            return amount < 1 ? FluidStack.EMPTY : mark.toStack(MathUtil.saturatedCast(amount));
        }
        for (var e : map) {
            if (e.getKey() instanceof AEFluidKey key) {
                return key.toStack(MathUtil.saturatedCast(e.getLongValue()));
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return storage == null ? 0 : Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return storage != null && (mark == null || mark.matches(stack));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (storage == null || storageSupplier == null || storageSupplier.getAsLong() >= capacity) return 0;
        var amount = resource.getAmount();
        if (amount < 1) return 0;
        AEFluidKey key = mark;
        if (key != null) {
            // 已有标记：内容必与标记一致，直接用标记键，省掉 AEFluidKey.of 的规范化开销
            if (!key.matches(resource)) return 0;
        } else {
            key = AEFluidKey.of(resource);
        }
        return (int) storage.insert(key, amount, action.simulate() ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.empty());
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (storage == null) return FluidStack.EMPTY;
        var amount = resource.getAmount();
        if (amount < 1) return FluidStack.EMPTY;
        AEFluidKey key = mark;
        if (key != null) {
            if (!key.matches(resource)) return FluidStack.EMPTY;
        } else {
            key = AEFluidKey.of(resource);
        }
        amount = (int) storage.extract(key, amount, action.simulate() ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.empty());
        if (amount < 1) return FluidStack.EMPTY;
        return ICustomFluidStackHandler.copy(resource, amount);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (storage == null || onChange == null) return FluidStack.EMPTY;
        if (maxDrain < 1) return FluidStack.EMPTY;
        if (mark != null) {
            var value = map.getAmount(mark);
            var drain = (int) Math.min(maxDrain, value);
            if (drain < 1) return FluidStack.EMPTY;
            if (action.execute()) {
                map.extract(mark, drain);
                onChange.run();
            }
            return mark.toStack(drain);
        }
        for (var it = map.iterator(); it.hasNext();) {
            var e = it.next();
            if (e.getKey() instanceof AEFluidKey key) {
                var value = e.getLongValue();
                maxDrain = (int) Math.min(maxDrain, value);
                if (maxDrain < 1) return FluidStack.EMPTY;
                var stack = key.toStack(maxDrain);
                if (action.execute()) {
                    if (value == maxDrain) {
                        it.remove();
                    } else {
                        e.setValue(value - maxDrain);
                    }
                    onChange.run();
                }
                return stack;
            }
        }
        return FluidStack.EMPTY;
    }
}
