package com.gtocore.common.machine.multiblock.electric.research;

import com.gtocore.api.research.ResearchRequirements;
import com.gtocore.api.research.recipe.ScanningRecipeExtion;
import com.gtocore.api.research.scanning.DataScanningManager;
import com.gtocore.common.item.DataCrystalItem;
import com.gtocore.common.machine.multiblock.electric.research.ui.ScanningInfoProvider;
import com.gtocore.common.machine.multiblock.electric.research.ui.ScanningSelectionTab;
import com.gtocore.common.machine.multiblock.part.research.IntelligentScanningProxyPartMachine;

import com.gtolib.GTOCore;
import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.AEChemicalHelper;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.ingredient.ItemIngredient;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import com.gto.datasynclib.annotations.SaveToDisk;
import com.gto.datasynclib.annotations.SyncToClient;
import com.gto.datasynclib.util.holder.ObjHolder;
import com.hepdd.gtmthings.utils.TeamUtil;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.common.data.GTMaterials.NULL;
import static com.gtocore.api.research.scanning.DataScanningManager.hasScanned;

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
                    if (!hasScanned(k, TeamUtil.getTeamUUID(getOwnerUUID()))) {
                        scanQueue.offer(k);
                    }
                }
            }
        } else if (workMode == WorkMode.SCAN_SELECTED_ONCE) {
            scanQueue.clear();
            scanQueue.addAll(selectedAEKeys);
        }
    }

    @Override
    public void exportSelectedAEKeys(Set<AEKey> k) {
        selectedAEKeys.clear();
        selectedAEKeys.addAll(k);
        if (workMode == WorkMode.SCAN_SELECTED_ONCE) {
            scanQueue.clear();
            scanQueue.addAll(selectedAEKeys);
        }
        getRecipeLogic().updateTickSubscription();
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
        var input = stack.copyWithCount(1);
        var output = stack.copyWithCount(1);
        var team = TeamUtil.getTeamUUID(getOwnerUUID());
        var keyCounter = new KeyCounter();
        var recipe = RecipeBuilder.ofRaw().inputItems(input);
        ReferenceOpenHashSet<Material> materials = new ReferenceOpenHashSet<>();

        DataCrystalItem.setTeamUUID(output, team);
        if (workMode == WorkMode.SCAN_SELECTED_ONLY) {
            var keys = new ReferenceOpenHashSet<AEKey>(selectedAEKeys.size());
            long totalBytes = 0;
            for (ObjectIterator<AEKey> iterator = selectedAEKeys.iterator(); iterator.hasNext();) {
                var k = iterator.next();
                var mat = AEChemicalHelper.getMaterial(k);
                var containsMaterial = false;
                if (mat != NULL) {
                    containsMaterial = !materials.add(mat);
                }
                if (containsMaterial) {
                    iterator.remove();
                    continue;
                }
                var p = DataScanningManager.scanData(k, team, true);
                if (p.countBytes() <= 0 && (ResearchRequirements.getEurekaRequirements(k).isEmpty() || hasScanned(k, team))) {
                    iterator.remove();
                    continue;
                }
                keys.add(k);
                var pts = DataScanningManager.scanData(k, team, true);
                totalBytes += pts.countBytes();
            }
            if (totalBytes <= 0) return null;
            var turns = remaining / totalBytes;
            for (var k : keys) {
                long actualAmount = turns;
                MEStorage me = null;
                if (scanningProxyPartMachine != null) {
                    me = scanningProxyPartMachine.getMESStorage();
                }
                if (k instanceof AEItemKey itemKey) {
                    if (me != null) {
                        actualAmount = me.extract(k, turns,
                                Actionable.SIMULATE, IActionSource.ofMachine(scanningProxyPartMachine));
                    }
                    recipe.inputItems(ItemIngredient.of(itemKey.item, actualAmount));
                } else if (k instanceof AEFluidKey fluidKey) {
                    if (me != null) {
                        actualAmount = me.extract(k, turns * 1000,
                                Actionable.SIMULATE, IActionSource.ofMachine(scanningProxyPartMachine)) / 1000;
                    }
                    recipe.inputFluids(fluidKey.getFluid(), 1000 * actualAmount);
                }
                keyCounter.add(k, actualAmount);
            }
            if (keyCounter.isEmpty()) return null;
            return recipe.EUt(eut(totalBytes * turns))
                    .addExtension(ScanningRecipeExtion.INSTANCE)
                    .addData(ScanningRecipeExtion.INSTANCE, ScanningRecipeExtion.create(keyCounter, output, team))
                    .duration(200 * GTOCore.difficulty)
                    .build();

        } else {
            final var initialRemaining = remaining;
            while (!scanQueue.isEmpty() && remaining > 0) {
                var k = scanQueue.poll();
                var mat = AEChemicalHelper.getMaterial(k);
                var containsMaterial = false;
                if (mat != NULL) {
                    containsMaterial = !materials.add(mat);
                }
                if (containsMaterial) {
                    continue;
                }
                long occupy = DataScanningManager.scanData(k, team, true).countBytes();
                if (occupy <= 0 && (ResearchRequirements.getEurekaRequirements(k).isEmpty() || hasScanned(k, team))) {
                    continue;
                }
                if (remaining >= occupy) {
                    if (k instanceof AEItemKey itemKey) {
                        recipe.inputItems(ItemIngredient.of(itemKey.item, 1));
                    } else if (k instanceof AEFluidKey fluidKey) {
                        recipe.inputFluids(fluidKey.getFluid(), 1000);
                    }
                    remaining -= occupy;
                    keyCounter.add(k, 1);
                }
            }
            if (keyCounter.isEmpty()) return null;
            return recipe.EUt(eut(initialRemaining - remaining))
                    .addExtension(ScanningRecipeExtion.INSTANCE)
                    .addData(ScanningRecipeExtion.INSTANCE, ScanningRecipeExtion.create(keyCounter, output, team))
                    .duration(200 * GTOCore.difficulty)
                    .build();
        }
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new ScanningSelectionTab(this, getOwnerUUID()));
    }

    @Override
    public void beforeWorking(@NotNull RecipeHandlerUnit unit, @NotNull GTRecipe recipe) {
        super.beforeWorking(unit, recipe);
        if ((workMode == WorkMode.SCAN_UNLEARNED_ONCE || workMode == WorkMode.SCAN_SELECTED_ONCE) && scanQueue.isEmpty()) {
            getRecipeLogic().setSuspendAfterFinish(true);
        }
    }

    private static long eut(long bytesScanned) {
        return 8 * bytesScanned + 8;
    }
}
