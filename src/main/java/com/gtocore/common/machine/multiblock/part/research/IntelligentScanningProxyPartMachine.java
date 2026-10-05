package com.gtocore.common.machine.multiblock.part.research;

import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.item.DataCrystalItem;
import com.gtocore.common.machine.multiblock.electric.research.IntelligentScanningManagementPlatformMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableMultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableContentHandler;
import com.gregtechceu.gtceu.api.misc.TickTimeMonitor;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.PlanScratch;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.*;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.MEStorage;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.recipesearch.IntLongMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class IntelligentScanningProxyPartMachine extends WorkableMultiblockPartMachine implements
                                                 IMachineLife, IGridConnectedMachine, IStorageWatcherNode {

    @SaveToDisk
    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);
    @SyncToClient
    @Getter
    @Setter
    private boolean isOnline;
    @Setter
    private boolean changed = true;
    private IStackWatcher storageWatcher;
    private TickableSubscription tickSubscription;
    /** tick 耗时监控（只有被 Jade 查看时才计时）。 */
    private final TickTimeMonitor scanMonitor = holder.monitorTick(GTOTickTimeMonitors.ME_STORAGE, this::scanStorage);
    @SaveToDisk
    private final ScanningContentHandler contentHandler = new ScanningContentHandler(this);

    @Getter
    private AEKeySet<AEKey> cachedKeys;
    private AEItemKey[] itemKeys = NO_ITEM_KEYS;
    private AEFluidKey[] fluidKeys = NO_FLUID_KEYS;
    private static final AEKey[] NO_KEYS = new AEKey[0];
    private static final AEItemKey[] NO_ITEM_KEYS = new AEItemKey[0];
    private static final AEFluidKey[] NO_FLUID_KEYS = new AEFluidKey[0];
    @Getter
    private final IActionSource actionSource = IActionSource.ofMachine(this);

    public IntelligentScanningProxyPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
        getMainNode().addService(IStorageWatcherNode.class, this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tickSubscription = subscribeServerTick(tickSubscription, scanMonitor, 40);
    }

    /** 扫描 ME 网络存储，刷新缓存的 key 集合。 */
    private void scanStorage() {
        if (getController() != null && changed) {
            changed = false;
            var grid = getMainNode().getGrid();
            if (grid == null) {
                clearKeys();
                return;
            }
            var stack = grid.getStorageService().getCachedInventory();
            if (stack != null) {
                collectKeys(stack);
            } else {
                clearKeys();
            }
            if (getController() instanceof IntelligentScanningManagementPlatformMachine managementPlatform) {
                managementPlatform.reloadAvailableAEKeys();
            }
        }
    }

    private void clearKeys() {
        cachedKeys = null;
        itemKeys = NO_ITEM_KEYS;
        fluidKeys = NO_FLUID_KEYS;
    }

    private void collectKeys(KeyCounter stack) {
        var keys = new AEKeySet<AEKey>(stack.size());
        var items = new ArrayList<AEItemKey>();
        var fluids = new ArrayList<AEFluidKey>();
        for (var entry : stack) {
            var k = entry.getKey();
            if (k instanceof AEItemKey itemKey) {
                if (itemKey.item instanceof DataCrystalItem) continue;
                var base = itemKey.dropSecondary();
                if (keys.add(base)) items.add(base);
            } else if (k instanceof AEFluidKey fluidKey) {
                if (entry.getLongValue() <= 1000) continue;
                var base = fluidKey.dropSecondary();
                if (keys.add(base)) fluids.add(base);
            }
        }
        cachedKeys = keys;
        itemKeys = items.toArray(NO_ITEM_KEYS);
        fluidKeys = fluids.toArray(NO_FLUID_KEYS);
    }

    private AEKey[] keysOf(AEKeyType type) {
        return type == AEKeyTypes.ITEMS ? itemKeys : fluidKeys;
    }

    public MEStorage getMESStorage() {
        var grid = getMainNode().getGrid();
        if (grid == null) return null;
        return grid.getStorageService().getInventory();
    }

    @Override
    public void onUnload() {
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
        }
        super.onUnload();
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void updateWatcher(IStackWatcher iStackWatcher) {
        storageWatcher = iStackWatcher;
        iStackWatcher.setWatchAll(true);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        changed = true;
    }

    @Override
    public void onStackChange(AEKey aeKey, long l) {
        changed = true;
    }

    private static class ScanningContentHandler extends NotifiableContentHandler {

        private final Undo itemUndo = new Undo();
        private final Undo fluidUndo = new Undo();

        protected ScanningContentHandler(IntelligentScanningProxyPartMachine machine) {
            super(machine, IO.IN);
        }

        public IntelligentScanningProxyPartMachine getMachine() {
            return (IntelligentScanningProxyPartMachine) super.getMachine();
        }

        @Override
        protected boolean updateEmpty() {
            return getMachine().getCachedKeys() == null || getMachine().getCachedKeys().isEmpty();
        }

        @Override
        public boolean handlesFluids() {
            return true;
        }

        @Override
        public boolean handlesItems() {
            return true;
        }

        @Override
        public long available(AEKeyType type, KeyIngredient ingredient) {
            var machine = getMachine();
            var grid = machine.getMainNode().getGrid();
            if (grid == null) return 0;
            var stored = grid.getStorageService().getCachedInventory();
            long total = 0;
            for (var key : machine.keysOf(type)) {
                if (KeyIngredient.accepts(ingredient, key.getUid(), key)) {
                    long t = total + stored.get(key);
                    total = t < 0 ? Long.MAX_VALUE : t;
                }
            }
            return total;
        }

        @Override
        public long reserveInput(PlanScratch plan, int member, AEKeyType type, int entry, KeyIngredient ingredient, long need, boolean consume) {
            var machine = getMachine();
            var grid = machine.getMainNode().getGrid();
            if (grid == null) return 0;
            var stored = grid.getStorageService().getCachedInventory();
            var keys = machine.keysOf(type);
            long got = 0;
            for (int s = 0; s < keys.length && got < need; s++) {
                var key = keys[s];
                if (!KeyIngredient.accepts(ingredient, key.getUid(), key)) continue;
                long free = stored.get(key) - plan.reservedOn(member, s);
                if (free <= 0) continue;
                long t = Math.min(free, need - got);
                plan.logCustom(member, s, entry, t, type, consume, false);
                got += t;
            }
            return got;
        }

        @Override
        public boolean commitInput(PlanScratch plan, int member, AEKeyType type) {
            boolean fluid = type == AEKeyTypes.FLUIDS;
            var undo = fluid ? fluidUndo : itemUndo;
            undo.size = 0;
            var machine = getMachine();
            var grid = machine.getMainNode().getGrid();
            if (grid == null) return false;
            var ae = grid.getStorageService().getInventory();
            var source = machine.actionSource;
            var keys = machine.keysOf(type);
            boolean changed = false;
            for (int i = 0, logSize = plan.logSize(); i < logSize; i++) {
                if (plan.logMember(i) != member || plan.logIsFluid(i) != fluid || !plan.logConsumes(i)) continue;
                int token = plan.logToken(i);
                long amount = plan.logAmount(i);
                if (token >= keys.length) {
                    undo.revert(ae, source);
                    return false;
                }
                var key = keys[token];
                long extracted = ae.extract(key, amount, Actionable.MODULATE, source);
                if (extracted > 0) {
                    changed = true;
                    undo.add(key, extracted);
                }
                if (extracted < amount) {
                    undo.revert(ae, source);
                    return false;
                }
            }
            if (changed) onContentsChanged();
            return true;
        }

        @Override
        public boolean isLossyRollback() {
            return true;
        }

        @Override
        public void rollbackInput(PlanScratch plan, int member, AEKeyType type) {
            var undo = type == AEKeyTypes.FLUIDS ? fluidUndo : itemUndo;
            var machine = getMachine();
            var grid = machine.getMainNode().getGrid();
            if (grid == null) {
                undo.size = 0;
                return;
            }
            undo.revert(grid.getStorageService().getInventory(), machine.actionSource);
            onContentsChanged();
        }

        @Override
        protected void fillSearchMap(@NotNull GTRecipeType type, @NotNull IntLongMap map) {
            var machine = getMachine();
            if (machine.isOnline()) {
                var grid = machine.getMainNode().getGrid();
                if (grid == null) return;
                var stored = grid.getStorageService().getCachedInventory();
                if (stored.isEmpty()) return;
                fill(type, map, stored, machine.itemKeys);
                fill(type, map, stored, machine.fluidKeys);
            }
        }
    }

    private static void fill(GTRecipeType type, IntLongMap map, KeyCounter stored, AEKey[] keys) {
        for (var stock : keys) {
            var amount = stored.get(stock);
            if (amount > 0) type.convertKey(stock, amount, map);
        }
    }

    private static final class Undo {

        private AEKey[] keys = NO_KEYS;
        private long[] amounts = new long[0];
        private int size;

        private void add(AEKey key, long amount) {
            if (size == keys.length) {
                int n = Math.max(4, size << 1);
                keys = Arrays.copyOf(keys, n);
                amounts = Arrays.copyOf(amounts, n);
            }
            keys[size] = key;
            amounts[size] = amount;
            size++;
        }

        private void revert(MEStorage ae, IActionSource source) {
            for (int i = 0; i < size; i++) {
                ae.insert(keys[i], amounts[i], Actionable.MODULATE, source);
                keys[i] = null;
            }
            size = 0;
        }
    }
}
