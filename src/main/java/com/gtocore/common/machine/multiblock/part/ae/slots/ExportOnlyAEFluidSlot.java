package com.gtocore.common.machine.multiblock.part.ae.slots;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExportOnlyAEFluidSlot extends ExportOnlyAESlot {

    public ExportOnlyAEFluidSlot() {
        super();
    }

    ExportOnlyAEFluidSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
        super(config, stock);
    }

    @Override
    public void addStack(GenericStack stack) {
        if (this.stock == null) {
            this.stock = stack;
        } else {
            this.stock = GenericStack.sum(this.stock, stack);
        }
        onContentsChanged();
    }

    @Override
    public void setStock(@Nullable GenericStack stack) {
        if (this.stock == null && stack == null) {
            return;
        } else if (stack == null) {
            this.stock = null;
        } else {
            if (stack.equals(stock)) return;
            this.stock = stack;
        }
        onContentsChanged();
    }

    @Nullable
    public AEFluidKey key() {
        var s = this.stock;
        return s != null && s.amount() > 0 && s.what() instanceof AEFluidKey k ? k : null;
    }

    public long extract(long amount, boolean simulate, boolean notify) {
        if (this.stock == null || !(this.stock.what() instanceof AEFluidKey)) {
            return 0;
        }
        long drained = Math.min(this.stock.amount(), amount);
        if (!simulate) {
            this.stock = new GenericStack(this.stock.what(), this.stock.amount() - drained);
            if (this.stock.amount() == 0) {
                this.stock = null;
            }
            if (notify) onContentsChanged();
        }
        return drained;
    }

    void restore(AEFluidKey key, long amount) {
        var s = this.stock;
        this.stock = s == null ? new GenericStack(key, amount) : new GenericStack(s.what(), s.amount() + amount);
    }

    @Override
    public ExportOnlyAEFluidSlot copy() {
        return new ExportOnlyAEFluidSlot(
                this.config == null ? null : ExportOnlyAESlot.copy(this.config),
                this.stock == null ? null : ExportOnlyAESlot.copy(this.stock));
    }
}
