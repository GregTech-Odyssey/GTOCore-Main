package com.gtocore.common.machine.multiblock.part.research;

import com.gtocore.common.item.DataCrystalItem;
import com.gtocore.common.machine.multiblock.electric.research.IntelligentScanningManagementPlatformMachine;

import com.gtolib.api.recipe.RecipeType;
import com.gtolib.api.recipe.lookup.IIngredientConvertible;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.WorkableMultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableContentHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;

import com.fast.recipesearch.IntLongMap;
import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;

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
    @SaveToDisk
    private final ScanningContentHandler contentHandler = new ScanningContentHandler(this);

    @Getter
    private Set<AEKey> cachedKeys;

    public IntelligentScanningProxyPartMachine(MetaMachineBlockEntity holder) {
        super(holder);
        getMainNode().addService(IStorageWatcherNode.class, this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        tickSubscription = subscribeServerTick(tickSubscription, () -> {
            if (getController() != null && changed) {
                changed = false;
                var grid = getMainNode().getGrid();
                if (grid == null) {
                    cachedKeys = null;
                    return;
                }
                var stack = grid.getStorageService().getCachedInventory();
                if (stack != null) {
                    cachedKeys = stack.keySet().stream().map(k -> {
                        if (k instanceof AEItemKey aeItemKey) {
                            if (aeItemKey.item instanceof DataCrystalItem) return null;
                            if (aeItemKey.hasTag()) {
                                return AEItemKey.of(aeItemKey.getItem());
                            }
                            return k;
                        } else if (k instanceof AEFluidKey aeFluidKey) {
                            if (stack.get(aeFluidKey) <= 1000) return null;
                            if (aeFluidKey.hasTag()) {
                                return AEFluidKey.of(aeFluidKey.getFluid());
                            }
                            return k;
                        }
                        return null;
                    }).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
                } else {
                    cachedKeys = null;
                }
                if (getController() instanceof IntelligentScanningManagementPlatformMachine managementPlatform) {
                    managementPlatform.reloadAvailableAEKeys();
                }
            }
        }, 40);
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
        public boolean canHandleFluid() {
            return true;
        }

        @Override
        public boolean canHandleItem() {
            return true;
        }

        @Override
        public boolean handleRecipeItem(IO io, GTRecipe recipe, List<Content<ItemIngredient>> items, boolean simulate) {
            if (io == IO.IN) {
                boolean changed = false;
                var grid = getMachine().getMainNode().getGrid();
                if (grid == null) return false;
                var ae = grid.getStorageService().getInventory();
                for (var it = items.iterator(); it.hasNext();) {
                    var ingredient = it.next();
                    if (ingredient.isEmpty()) {
                        it.remove();
                        continue;
                    }
                    for (var i : getMachine().cachedKeys) {
                        if (i instanceof AEItemKey itemKey && ingredient.inner.testAeKay(itemKey)) {
                            var extracted = ae.extract(i, ingredient.amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.ofMachine(getMachine()));
                            if (extracted > 0) {
                                changed = true;
                                ingredient.shrink(extracted);
                                if (ingredient.amount <= 0) {
                                    it.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!simulate && changed) {
                    onContentsChanged();
                }
            }
            return items.isEmpty();
        }

        @Override
        public boolean handleRecipeFluid(IO io, GTRecipe recipe, List<Content<FluidIngredient>> fluids, boolean simulate) {
            if (io == IO.IN) {
                boolean changed = false;
                var grid = getMachine().getMainNode().getGrid();
                if (grid == null) return false;
                var ae = grid.getStorageService().getInventory();
                for (var it = fluids.iterator(); it.hasNext();) {
                    var ingredient = it.next();
                    if (ingredient.isEmpty()) {
                        it.remove();
                        continue;
                    }
                    for (var i : getMachine().cachedKeys) {
                        if (i instanceof AEFluidKey fluidKey && ingredient.inner.testAeKay(fluidKey)) {
                            var extracted = ae.extract(i, ingredient.amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE, IActionSource.ofMachine(getMachine()));
                            if (extracted > 0) {
                                changed = true;
                                ingredient.shrink(extracted);
                                if (ingredient.amount <= 0) {
                                    it.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!simulate && changed) {
                    onContentsChanged();
                }
            }
            return fluids.isEmpty();
        }

        @Override
        protected void fillSearchMap(@NotNull GTRecipeType type, @NotNull IntLongMap map) {
            var machine = getMachine();
            if (machine.isOnline()) {
                var grid = machine.getMainNode().getGrid();
                if (grid == null) return;
                AEKeyMap<AEKey> keyMap = null;
                boolean specialConverter = ((RecipeType) type).specialConverter;
                for (var stock : machine.cachedKeys) {
                    if (keyMap == null) {
                        keyMap = grid.getStorageService().getCachedInventory().getMap();
                        if (keyMap.isEmpty()) return;
                    }
                    var amount = keyMap.getAmount(stock);
                    if (amount < 1) continue;
                    if (specialConverter) {
                        if (stock instanceof AEItemKey i) {
                            type.convertItem(i.getReadOnlyStack(), amount, map);
                        } else if (stock instanceof AEFluidKey f) {
                            type.convertFluid(f.getReadOnlyStack(), amount, map);
                        }
                    } else {
                        ((IIngredientConvertible) stock).gtolib$convert(amount, map);
                    }
                }
            }
        }
    }
}
