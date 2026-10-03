package com.gtocore.common.machine.multiblock.part.ae.slots;

import com.gtocore.common.machine.multiblock.part.ae.MEStockingHatchPartMachine;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.GenericStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExportOnlyAEStockingFluidList extends ExportOnlyAEFluidList {

    private final MEStockingHatchPartMachine machine;

    public ExportOnlyAEStockingFluidList(MEStockingHatchPartMachine holder, int slots) {
        super(holder, slots, () -> new ExportOnlyAEStockingFluidSlot(holder));
        this.machine = holder;
    }

    @Override
    boolean prepare() {
        if (!machine.isWorkingEnabled() || !machine.isOnline()) return false;
        var grid = machine.getMainNode().getGrid();
        if (grid == null) return false;
        AEKeyMap<AEKey> map = null;
        int time = machine.getOffsetTimer();
        for (var i : inventory) {
            if (i.config == null) continue;
            var stock = i.stock;
            if (stock == null) continue;
            if (map == null) {
                map = grid.getStorageService().getCachedInventory().getMap();
                if (map.isEmpty()) return false;
            }
            ((ExportOnlyAEStockingFluidSlot) i).refresh(map, stock.amount(), stock.what(), time);
        }
        return true;
    }

    // only consumable (chance > 0) contents are handled here (preventing phantom counts).
    @Override
    boolean accepts(boolean consume) {
        return consume;
    }

    @Override
    public boolean isAutoPull() {
        return machine.isAutoPull();
    }

    @Override
    public boolean isStocking() {
        return true;
    }

    @Override
    public boolean hasStackInConfig(GenericStack stack, boolean checkExternal) {
        boolean inThisHatch = super.hasStackInConfig(stack, false);
        if (inThisHatch) return true;
        if (checkExternal) {
            return machine.testConfiguredInOtherPart(stack);
        }
        return false;
    }

    private static final class ExportOnlyAEStockingFluidSlot extends ExportOnlyAEFluidSlot {

        private final MEStockingHatchPartMachine machine;
        private long refreshTime;

        private ExportOnlyAEStockingFluidSlot(MEStockingHatchPartMachine machine) {
            super();
            this.machine = machine;
        }

        private ExportOnlyAEStockingFluidSlot(MEStockingHatchPartMachine machine, @Nullable GenericStack config, @Nullable GenericStack stock) {
            super(config, stock);
            this.machine = machine;
        }

        private long refresh(AEKeyMap<AEKey> map, long amount, AEKey request, int time) {
            if (refreshTime != time) {
                refreshTime = time;
                var storage = map.getAmount(request);
                if (storage > 0) {
                    if (amount != storage) {
                        this.stock = new GenericStack(request, storage);
                    }
                } else {
                    this.stock = null;
                }
                return storage;
            }
            return amount;
        }

        @Override
        public long extract(long amount, boolean simulate, boolean notify) {
            if (this.stock != null && this.config != null) {
                if (!machine.isOnline()) return 0;
                var grid = machine.getMainNode().getGrid();
                if (grid == null) return 0;
                long extracted = simulate ? Math.min(amount, stock.amount()) : grid.getStorageService().getInventory().extract(stock.what(), amount, Actionable.MODULATE, machine.getActionSource());
                if (extracted > 0) {
                    if (!simulate) {
                        machine.getThroughputCounter().remove(stock.what(), extracted);
                        this.stock = ExportOnlyAESlot.copy(stock, stock.amount() - extracted);
                        if (this.stock.amount() == 0) {
                            this.stock = null;
                        }
                        if (notify) onContentsChanged();
                    }
                    return extracted;
                }
            }
            return 0;
        }

        @Override
        void restore(AEFluidKey key, long amount) {
            var grid = machine.getMainNode().getGrid();
            if (grid == null) return;
            long inserted = grid.getStorageService().getInventory().insert(key, amount, Actionable.MODULATE, machine.getActionSource());
            if (inserted > 0) {
                machine.getThroughputCounter().add(key, inserted);
                super.restore(key, inserted);
            }
        }

        @Override
        public @NotNull ExportOnlyAEStockingFluidSlot copy() {
            return new ExportOnlyAEStockingFluidSlot(machine, this.config == null ? null : copy(this.config), this.stock == null ? null : copy(this.stock));
        }
    }
}
