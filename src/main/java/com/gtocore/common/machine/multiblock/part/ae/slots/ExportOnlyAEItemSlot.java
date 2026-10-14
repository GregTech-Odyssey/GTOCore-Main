package com.gtocore.common.machine.multiblock.part.ae.slots;

import net.minecraft.MethodsReturnNonnullByDefault;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ExportOnlyAEItemSlot extends ExportOnlyAESlot {

    public ExportOnlyAEItemSlot() {
        super();
    }

    ExportOnlyAEItemSlot(@Nullable GenericStack config, @Nullable GenericStack stock) {
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
    public AEItemKey key() {
        var s = this.stock;
        return s != null && s.amount() > 0 && s.what() instanceof AEItemKey k ? k : null;
    }

    public long extract(long amount, boolean simulate, boolean notify) {
        if (this.stock != null) {
            long extracted = Math.min(this.stock.amount(), amount);
            if (!(this.stock.what() instanceof AEItemKey)) return 0;
            if (!simulate) {
                this.stock = ExportOnlyAESlot.copy(this.stock, this.stock.amount() - extracted);
                if (this.stock.amount() == 0) {
                    this.stock = null;
                }
                if (notify) onContentsChanged();
            }
            return extracted;
        }
        return 0;
    }

    void restore(AEItemKey key, long amount) {
        var s = this.stock;
        this.stock = s == null ? new GenericStack(key, amount) : new GenericStack(s.what(), s.amount() + amount);
    }

    @Override
    public ExportOnlyAEItemSlot copy() {
        return new ExportOnlyAEItemSlot(
                this.config == null ? null : copy(this.config),
                this.stock == null ? null : copy(this.stock));
    }
}
