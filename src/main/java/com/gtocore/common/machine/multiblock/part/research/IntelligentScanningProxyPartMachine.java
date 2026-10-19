package com.gtocore.common.machine.multiblock.part.research;

import com.gtocore.common.data.GTOTickTimeMonitors;
import com.gtocore.common.item.DataCrystalItem;
import com.gtocore.common.machine.multiblock.electric.research.IntelligentScanningManagementPlatformMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
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

import java.util.Arrays;

public class IntelligentScanningProxyPartMachine extends WorkableMultiblockPartMachine implements
                                                 IMachineLife, IGridConnectedMachine, IStorageWatcherNode {

    @SaveToDisk
    private final GridNodeHolder nodeHolder = new GridNodeHolder(this);
    @SyncToClient
    @Getter
    @Setter
    private boolean isOnline;
    private IStackWatcher storageWatcher;
    private TickableSubscription tickSubscription;
    private boolean initializeKeys;
    private boolean keysChanged;
    /** 合并同一批 watcher 回调，避免在配方提交期间替换 key 数组。 */
    private final TickTimeMonitor storageMonitor = holder.monitorTick(GTOTickTimeMonitors.ME_STORAGE, this::updateStorageCache);
    @SaveToDisk
    private final ScanningContentHandler contentHandler = new ScanningContentHandler(this);

    @Getter
    private AEKeySet<AEKey> cachedKeys = new AEKeySet<>();
    private AEKeySet<AEKey> watchedKeys = new AEKeySet<>();
    /** 同一基础 key 可能对应多个仍有库存的 NBT 变体。 */
    private final KeyCounter keyReferences = new KeyCounter();
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
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        requestStorageUpdate();
    }

    private void requestStorageUpdate() {
        tickSubscription = subscribeServerTick(tickSubscription, storageMonitor, 20);
    }

    /** 仅在接入网络时读取一次库存，后续由 watcher 增量维护。 */
    private void updateStorageCache() {
        tickSubscription.unsubscribe();
        tickSubscription = null;
        if (initializeKeys) {
            initializeKeys = false;
            clearKeys();
            var grid = getMainNode().getGrid();
            if (grid != null) {
                collectKeys(grid.getStorageService().getCachedInventory());
            }
        }
        if (keysChanged) {
            keysChanged = false;
            rebuildKeyArrays();
        }
        contentHandler.onContentsChanged();
        if (getController() instanceof IntelligentScanningManagementPlatformMachine managementPlatform) {
            managementPlatform.reloadAvailableAEKeys();
        }
    }

    private void clearKeys() {
        cachedKeys.clear();
        watchedKeys.clear();
        keyReferences.clear();
        itemKeys = NO_ITEM_KEYS;
        fluidKeys = NO_FLUID_KEYS;
    }

    private void collectKeys(KeyCounter stack) {
        cachedKeys = new AEKeySet<>(stack.size());
        watchedKeys = new AEKeySet<>(stack.size());
        keyReferences.ensureCapacity(stack.size());
        stack.fastForEach(this::updateKey);
        keysChanged = true;
    }

    private void updateKey(AEKey key, long amount) {
        AEKey base;
        boolean available;
        if (key instanceof AEItemKey itemKey) {
            if (itemKey.item instanceof DataCrystalItem) return;
            base = itemKey.dropSecondary();
            available = amount > 0;
        } else if (key instanceof AEFluidKey fluidKey) {
            base = fluidKey.dropSecondary();
            available = amount > 1000;
        } else {
            return;
        }
        if (available) {
            if (watchedKeys.add(key)) {
                keyReferences.add(base, 1);
                keysChanged |= cachedKeys.add(base);
            }
        } else if (watchedKeys.remove(key)) {
            keyReferences.remove(base, 1);
            if (keyReferences.get(base) == 0) {
                keyReferences.remove(base);
                keysChanged |= cachedKeys.remove(base);
            }
        }
    }

    private void rebuildKeyArrays() {
        int itemCount = 0;
        int fluidCount = 0;
        for (var key : cachedKeys) {
            if (key instanceof AEItemKey) itemCount++;
            else if (key instanceof AEFluidKey) fluidCount++;
        }
        var items = itemCount == 0 ? NO_ITEM_KEYS : new AEItemKey[itemCount];
        var fluids = fluidCount == 0 ? NO_FLUID_KEYS : new AEFluidKey[fluidCount];
        int itemIndex = 0;
        int fluidIndex = 0;
        for (var key : cachedKeys) {
            if (key instanceof AEItemKey itemKey) {
                items[itemIndex++] = itemKey;
            } else if (key instanceof AEFluidKey fluidKey) {
                fluids[fluidIndex++] = fluidKey;
            }
        }
        itemKeys = items;
        fluidKeys = fluids;
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
        if (storageWatcher != null) {
            storageWatcher.reset();
            storageWatcher = null;
        }
        super.onUnload();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
        clearKeys();
        initializeKeys = false;
        keysChanged = false;
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        storageWatcher = newWatcher;
        newWatcher.setWatchAll(true);
        initializeKeys = true;
        requestStorageUpdate();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        IGridConnectedMachine.super.onMainNodeStateChanged(reason);
        if (getMainNode().getGrid() == null) initializeKeys = true;
        requestStorageUpdate();
    }

    @Override
    public void onStackChange(AEKey key, long amount) {
        if (!initializeKeys) updateKey(key, amount);
        requestStorageUpdate();
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
            return getMachine().getCachedKeys().isEmpty();
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
