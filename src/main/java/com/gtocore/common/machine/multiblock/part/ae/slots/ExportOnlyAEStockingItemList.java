package com.gtocore.common.machine.multiblock.part.ae.slots;

import com.gtocore.common.machine.multiblock.part.ae.MEStockingBusPartMachine;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyMap;
import appeng.api.stacks.GenericStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExportOnlyAEStockingItemList extends ExportOnlyAEItemList {

    private final MEStockingBusPartMachine machine;

    public ExportOnlyAEStockingItemList(MEStockingBusPartMachine holder, int slots) {
        super(holder, slots, () -> new ExportOnlyAEStockingItemSlot(holder));
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
            ((ExportOnlyAEStockingItemSlot) i).refresh(map, stock.amount(), stock.what(), time);
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
        boolean inThisBus = super.hasStackInConfig(stack, false);
        if (inThisBus) return true;
        if (checkExternal) {
            return machine.testConfiguredInOtherPart(stack);
        }
        return false;
    }

    private static final class ExportOnlyAEStockingItemSlot extends ExportOnlyAEItemSlot {

        private final MEStockingBusPartMachine machine;
        private long refreshTime;

        private ExportOnlyAEStockingItemSlot(MEStockingBusPartMachine machine) {
            super();
            this.machine = machine;
        }

        private ExportOnlyAEStockingItemSlot(MEStockingBusPartMachine machine, @Nullable GenericStack config, @Nullable GenericStack stock) {
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
                    this.stock = new GenericStack(request, storage);
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
        void restore(AEItemKey key, long amount) {
            var grid = machine.getMainNode().getGrid();
            if (grid == null) return;
            long inserted = grid.getStorageService().getInventory().insert(key, amount, Actionable.MODULATE, machine.getActionSource());
            if (inserted > 0) {
                machine.getThroughputCounter().add(key, inserted);
                super.restore(key, inserted);
            }
        }

        @Override
        public @NotNull ExportOnlyAEStockingItemSlot copy() {
            return new ExportOnlyAEStockingItemSlot(machine, this.config == null ? null : copy(this.config), this.stock == null ? null : copy(this.stock));
        }
    }
}
