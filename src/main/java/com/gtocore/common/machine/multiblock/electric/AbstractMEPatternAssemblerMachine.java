package com.gtocore.common.machine.multiblock.electric;

import com.gtocore.common.machine.multiblock.part.ae.MECraftPatternPartMachine;
import com.gtocore.integration.jade.AEKeyTooltip;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.IWailaDisplayProvider;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ICustomRecipeLogicHolder;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;

import net.minecraft.nbt.CompoundTag;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyMap;

import com.gto.datasynclib.annotations.SaveToDisk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMEPatternAssemblerMachine extends ElectricMultiblockMachine implements ICustomRecipeLogicHolder, IWailaDisplayProvider {

    protected final List<MECraftPatternPartMachine> partMachines = new ArrayList<>();

    @SaveToDisk
    private final AEKeyMap<AEItemKey> plannedOutputs = new AEKeyMap<>();

    private int plannedSlots;
    private long plannedAmount;

    @Nullable
    private TickableSubscription retrySubs;

    protected AbstractMEPatternAssemblerMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        if (part instanceof MECraftPatternPartMachine machine) {
            partMachines.add(machine);
            machine.setOnContentsChanged(getRecipeLogic()::updateTickSubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        cancelRetry();
    }

    @Override
    public void onStructureFormed() {
        partMachines.clear();
        super.onStructureFormed();
        if (!getRecipeLogic().isWorking()) requestRetry();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        partMachines.clear();
        cancelRetry();
    }

    protected abstract boolean planOutputs();

    protected final void plan(MECraftPatternPartMachine machine, MECraftPatternPartMachine.InternalSlot slot, long amount) {
        var key = slot.getOutput();
        if (key == null) return;
        plannedOutputs.insert(key, amount);
        slot.setAmount(slot.getAmount() - amount);
        machine.onChanged();
        plannedSlots++;
        plannedAmount += amount;
    }

    protected final int plannedCount() {
        return plannedSlots;
    }

    /** 已从样板仓取走、还没送回网络的该产物数量（拉取仓算库存时要算上，避免重复补货）。 */
    public long getPendingAmount(AEItemKey key) {
        return plannedOutputs.getAmount(key);
    }

    @Override
    public GTRecipeDefinition createCustomRecipe(RecipeHandlerUnit unit) {
        long maxEUt = getOverclockVoltage();
        if (maxEUt == 0) return null;
        if (!plannedOutputs.isEmpty()) return null;
        plannedSlots = 0;
        plannedAmount = 0;
        if (!planOutputs()) return null;
        double d = (double) plannedAmount / maxEUt;
        int limit = getOverclockLimit();
        return getRecipeBuilder()
                .EUt(Math.max(1, d >= limit ? maxEUt : (long) (maxEUt * d / limit)))
                .duration((int) Math.max(d, limit))
                .build();
    }

    @Override
    public boolean alwaysSearchRecipe() {
        return true;
    }

    @Override
    public boolean handleRecipeOutput(GTRecipe recipe) {
        if (flushPlanned()) return true;
        requestRetry();
        return false;
    }

    @Override
    public void appendWailaData(CompoundTag data, BlockAccessor blockAccessor) {
        AEKeyTooltip.write(data, AEKeyTooltip.PENDING, plannedOutputs);
    }

    @Override
    public void appendWailaTooltip(CompoundTag data, ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config) {
        AEKeyTooltip.read(tooltip, data, AEKeyTooltip.PENDING);
    }

    private void requestRetry() {
        if (isRemote() || plannedOutputs.isEmpty()) return;
        retrySubs = subscribeServerTick(retrySubs, this::retryDelivery, 40);
    }

    private void cancelRetry() {
        if (retrySubs != null) {
            retrySubs.unsubscribe();
            retrySubs = null;
        }
    }

    private void retryDelivery() {
        if (flushPlanned()) cancelRetry();
    }

    private boolean flushPlanned() {
        if (plannedOutputs.isEmpty()) return true;
        if (partMachines.isEmpty()) return false;
        var changed = false;
        for (var it = plannedOutputs.iterator(); it.hasNext();) {
            var entry = it.next();
            if (!(entry.getKey() instanceof AEItemKey key)) continue;
            var amount = entry.getLongValue();
            var inserted = insertIntoNetwork(key, amount);
            if (inserted >= amount) {
                it.remove();
                changed = true;
            } else if (inserted > 0) {
                entry.setValue(amount - inserted);
                changed = true;
            }
        }
        if (changed) onChanged();
        return plannedOutputs.isEmpty();
    }

    private long insertIntoNetwork(AEItemKey key, long amount) {
        for (var machine : partMachines) {
            var grid = machine.getMainNode().getGrid();
            if (grid == null) continue;
            return grid.getStorageService().getInventory().insert(key, amount, Actionable.MODULATE, machine.getActionSourceField());
        }
        return 0;
    }
}
