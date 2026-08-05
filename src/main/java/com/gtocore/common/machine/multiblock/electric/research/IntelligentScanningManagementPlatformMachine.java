package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.ResearchPoints;
import com.gtocore.api.research.TeamResearchSavedDtat;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.common.item.DataCrystalItem;
import com.gtocore.common.machine.electric.ScannerMachine;
import com.gtocore.common.machine.multiblock.electric.research.ui.ScanningInfoProvider;
import com.gtocore.common.machine.multiblock.electric.research.ui.ScanningSelectionTab;
import com.gtocore.common.machine.multiblock.part.research.IntelligentScanningProxyPartMachine;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.RecipeBuilder;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IntelligentScanningManagementPlatformMachine extends ElectricMultiblockMachine implements ScanningInfoProvider, ICustomRecipeLogicHolder {

    @SaveToDisk
    @SyncToClient
    @Getter
    private WorkMode workMode = WorkMode.SCAN_SELECTED_ONLY;
    @SaveToDisk
    @SyncToClient
    @Getter
    private final ReferenceOpenHashSet<AEKey> selectedAEKeys = new ReferenceOpenHashSet<>();
    @SaveToDisk
    @SyncToClient
    private final Queue<AEKey> scanQueue = new LinkedList<>();

    public IntelligentScanningManagementPlatformMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    IntelligentScanningProxyPartMachine scanningProxyPartMachine;

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof IntelligentScanningProxyPartMachine proxyPart) {
            setScanningPartMachine(proxyPart);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        setScanningPartMachine(null);
        scanQueue.clear();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        setScanningPartMachine(null);
    }

    public void setScanningPartMachine(@Nullable IntelligentScanningProxyPartMachine proxyPartMachine) {
        scanningProxyPartMachine = proxyPartMachine;
    }

    @Override
    public Set<AEKey> getAvailableAEKeys() {
        if (scanningProxyPartMachine != null) {
            return scanningProxyPartMachine.getCachedKeys();
        }
        return Collections.emptySet();
    }

    @Override
    public void reloadAvailableAEKeys() {
        updateScanQueue();
        getRecipeLogic().updateTickSubscription();
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    public void updateScanQueue() {
        if (scanningProxyPartMachine != null && (workMode == WorkMode.SCAN_UNLEARNED_ONLY || workMode == WorkMode.SCAN_UNLEARNED_ONCE)) {
            var keys = scanningProxyPartMachine.getCachedKeys();
            if (keys != null) {
                scanQueue.clear();
                for (var k : keys) {
                    if (TeamResearchSavedDtat.hasScanned(k, TeamUtil.getTeamUUID(getOwnerUUID()))) {
                        scanQueue.offer(k);
                    }
                }
            }
        }
    }

    @Override
    public void exportSelectedAEKeys(Set<AEKey> k) {
        selectedAEKeys.clear();
        selectedAEKeys.addAll(k);
    }

    @Override
    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
        updateScanQueue();
        getRecipeLogic().updateTickSubscription();
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        ObjHolder<ItemStack> d = new ObjHolder<>();
        unit.fastForEachItems(true, (stack, amount) -> {
            if (stack.getItem() instanceof DataCrystalItem dataCrystalItem && (d.get() == null || dataCrystalItem.tier > ((DataCrystalItem) d.get().getItem()).tier)) {
                d.set(stack);
            }
        });
        var stack = d.get();
        if (stack == null) return null;
        var remaining = DataCrystalItem.getRemainingCapacity(stack);
        var output = stack.copyWithCount(1);
        if (workMode == WorkMode.SCAN_SELECTED_ONLY) {
            var keys = new AEKey[selectedAEKeys.size()];
            var points = new ResearchPoints[selectedAEKeys.size()];
            var bytes = new long[selectedAEKeys.size()];
            long totalBytes = 0;
            int i = 0;
            for (var k : selectedAEKeys) {
                var p = DataScanningManager.scanData(k, TeamUtil.getTeamUUID(getOwnerUUID()), true);
                if (p.countBytes() <= 0) continue;
                keys[i] = k;
                points[i] = DataScanningManager.scanData(k, TeamUtil.getTeamUUID(getOwnerUUID()), true);
                bytes[i] = points[i].countBytes();
                totalBytes += bytes[i];
                i++;
            }
            var turns = remaining / totalBytes;
            var recipe = RecipeBuilder.ofRaw().inputItems(stack);
            for (var k : keys) {
                if (k instanceof AEItemKey itemKey) {
                    recipe.inputItems(ItemIngredient.of(itemKey.item, turns));
                } else if (k instanceof AEFluidKey fluidKey) {
                    recipe.inputFluids(fluidKey.getFluid(), 1000 * turns);
                }
                DataCrystalItem.addResearchData(output, DataScanningManager.scanData(k, TeamUtil.getTeamUUID(getOwnerUUID()), turns, false));
            }
            return recipe.outputItems(output).EUt(ScannerMachine.eut(totalBytes * turns)).build();

        } else if (workMode == WorkMode.SCAN_UNLEARNED_ONLY || workMode == WorkMode.SCAN_UNLEARNED_ONCE) {
            var recipe = RecipeBuilder.ofRaw().inputItems(stack);
            final var initialRemaining = remaining;
            while (!scanQueue.isEmpty() && remaining > 0) {
                var k = scanQueue.poll();
                long amount = 1;
                if (k instanceof AEFluidKey) {
                    amount = 1000;
                }
                long occupy = DataScanningManager.scanData(k, TeamUtil.getTeamUUID(getOwnerUUID()), true).countBytes();
                if (remaining >= occupy) {
                    if (k instanceof AEItemKey itemKey) {
                        recipe.inputItems(ItemIngredient.of(itemKey.item, amount));
                    } else if (k instanceof AEFluidKey fluidKey) {
                        recipe.inputFluids(fluidKey.getFluid(), amount);
                    }
                    remaining -= occupy;
                    DataCrystalItem.addResearchData(output, DataScanningManager.scanData(k, TeamUtil.getTeamUUID(getOwnerUUID()), false));
                }
            }
            return recipe.outputItems(output).EUt(ScannerMachine.eut(initialRemaining - remaining)).build();
        }
        return null;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new ScanningSelectionTab(this, getOwnerUUID()));
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        if (workMode == WorkMode.SCAN_UNLEARNED_ONCE && scanQueue.isEmpty()) {
            getRecipeLogic().setSuspendAfterFinish(true);
        }
    }
}
