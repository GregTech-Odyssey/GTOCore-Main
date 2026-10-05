package com.gtocore.common.machine.multiblock.electric.assembly;

import com.gtocore.common.machine.multiblock.part.HugeBusPartMachine;
import com.gtocore.data.IdleReason;

import com.gtolib.api.machine.multiblock.ElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentList;
import com.gregtechceu.gtceu.api.recipe.content.ContentRoll;
import com.gregtechceu.gtceu.api.recipe.content.KeyIngredient;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;
import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.api.recipe.handler.PlanScratch;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerUnit;
import com.gregtechceu.gtceu.api.recipe.info.FluidRecipeInfo;
import com.gregtechceu.gtceu.api.recipe.info.ItemRecipeInfo;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public final class AdvancedAssemblyLineMachine extends ElectricMultiblockMachine {

    private final ReferenceArrayList<KeyInventory<AEItemKey>> itemStackTransfers = new ReferenceArrayList<>();
    private final ReferenceArrayList<KeyInventory<AEFluidKey>> fluidTankTransfers = new ReferenceArrayList<>();

    public AdvancedAssemblyLineMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    /**
     * 检查给定配方的物品输入是否与机器的物品存储区有序匹配。
     */
    private boolean checkItemInputs(GTRecipe recipe) {
        return checkOrderedInputs(itemStackTransfers, recipe.itemInputs);
    }

    /**
     * 检查给定配方的流体输入是否与机器的流体存储区有序匹配。
     */
    private boolean checkFluidInputs(GTRecipe recipe) {
        return checkOrderedInputs(fluidTankTransfers, recipe.fluidInputs);
    }

    private static boolean checkOrderedInputs(ReferenceArrayList<? extends KeyInventory<?>> machineInputs, ContentList inputs) {
        int n = inputs.size();
        if (n == 0) return true;
        if (machineInputs.size() < n) return false;
        for (int i = 0; i < n; i++) {
            if (inputs.amount(i) > 0) {
                if (!matchSingleKind(machineInputs.get(i), inputs.ingredient(i))) return false;
            }
        }
        return true;
    }

    /**
     * 验证给定的存储区是否仅包含与当前需求匹配的唯一种类物品。
     */
    private static boolean matchSingleKind(KeyInventory<?> storage, KeyIngredient currentIngredient) {
        boolean found = false;
        int kind = 0;
        int size = storage.size();
        for (int slot = 0; slot < size; slot++) {
            if (storage.amountAt(slot) <= 0) continue;
            int uid = storage.uidAt(slot);
            if (!found) {
                if (!KeyIngredient.accepts(currentIngredient, uid, storage.rawKeyAt(slot))) return false;
                kind = uid;
                found = true;
            } else if (uid != kind) {
                return false;
            }
        }
        return found;
    }

    @Override
    public boolean matchRecipeInput(RecipeHandlerUnit unit, GTRecipe recipe) {
        var config = ConfigHolder.INSTANCE.machines;
        if (config.orderedAssemblyLineItems) {
            if (!checkItemInputs(recipe)) {
                setIdleReason(IdleReason.ORDERED_ITEM);
                return false;
            }
        } else {
            if (!unit.matchInputs(only(recipe, true))) {
                setIdleReason(ActionResult.failInsufficientIn(ItemRecipeInfo.INSTANCE.getName()));
                return false;
            }
        }
        if (config.orderedAssemblyLineFluids) {
            if (!checkFluidInputs(recipe)) {
                setIdleReason(IdleReason.ORDERED_FLUID);
                return false;
            }
        } else {
            if (unit.matchInputs(only(recipe, false))) return true;
            setIdleReason(ActionResult.failInsufficientIn(FluidRecipeInfo.INSTANCE.getName()));
            return false;
        }
        return true;
    }

    @Override
    public boolean handleRecipeInput(RecipeHandlerUnit unit, GTRecipe recipe) {
        boolean unitUsed = false;
        var config = ConfigHolder.INSTANCE.machines;
        if (config.orderedAssemblyLineItems) {
            if (!consumeOrderedInputs(itemStackTransfers, recipe, recipe.itemInputs, true)) {
                setIdleReason(IdleReason.ORDERED_ITEM);
                return false;
            }
        } else {
            if (!consumeFromUnit(unit, only(recipe, true))) {
                setIdleReason(ActionResult.failInsufficientIn(ItemRecipeInfo.INSTANCE.getName()));
                return false;
            }
            unitUsed = true;
        }
        if (config.orderedAssemblyLineFluids) {
            if (!consumeOrderedInputs(fluidTankTransfers, recipe, recipe.fluidInputs, false)) {
                setIdleReason(IdleReason.ORDERED_FLUID);
                return false;
            }
        } else {
            if (!consumeFromUnit(unit, only(recipe, false))) {
                setIdleReason(ActionResult.failInsufficientIn(FluidRecipeInfo.INSTANCE.getName()));
                return false;
            }
            unitUsed = true;
        }
        if (unitUsed) unit.onCommitted(recipe);
        return true;
    }

    private static GTRecipe only(GTRecipe recipe, boolean items) {
        var r = recipe.copy();
        r.ocLevel = recipe.ocLevel;
        if (items) {
            r.fluidInputs = ContentList.EMPTY;
        } else {
            r.itemInputs = ContentList.EMPTY;
        }
        return r;
    }

    private static boolean consumeFromUnit(RecipeHandlerUnit unit, GTRecipe recipe) {
        if (recipe.itemInputs.isEmpty() && recipe.fluidInputs.isEmpty()) return true;
        var p = PlanScratch.acquire();
        try {
            unit.rollInputs(recipe, p);
            if (!unit.planInputs(recipe, p, recipe.scale, true)) return false;
            if (!unit.commitFallible(p)) return false;
            unit.commitArrays(p);
            return true;
        } finally {
            PlanScratch.release();
        }
    }

    @SuppressWarnings("unchecked")
    private static <K extends AEKey> boolean consumeOrderedInputs(ReferenceArrayList<KeyInventory<K>> machineInputs, GTRecipe recipe, ContentList inputs, boolean testOnce) {
        int n = inputs.size();
        if (n == 0) return true;
        if (machineInputs.size() < n) return false;
        for (int i = 0; i < n; i++) {
            long need = ContentRoll.rolled(recipe, inputs, i, ContentRoll.RNG);
            if (need <= 0) continue;
            var inputSlot = machineInputs.get(i);
            var ingredient = inputs.ingredient(i);
            boolean tested = false;
            int size = inputSlot.size();
            for (int j = 0; j < size; j++) {
                if (inputSlot.amountAt(j) <= 0) continue;
                var key = (K) inputSlot.rawKeyAt(j);
                if (!tested && !KeyIngredient.accepts(ingredient, inputSlot.uidAt(j), key)) continue;
                if (testOnce) tested = true;
                need -= inputSlot.extract(j, key, need, false);
                if (need <= 0) break;
            }
            if (need > 0) return false;
        }
        return true;
    }

    @Override
    public Comparator<IMultiPart> getPartSorter() {
        return Comparator.comparing(p -> p.self().getPos(), RelativeDirection.RIGHT.getSorter(getFrontFacing(), getUpwardsFacing(), isFlipped()));
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        itemStackTransfers.clear();
        fluidTankTransfers.clear();
    }

    /**
     * 绑定物品和流体存储
     */
    @Override
    public void onStructureFormed() {
        itemStackTransfers.clear();
        fluidTankTransfers.clear();
        super.onStructureFormed();
    }

    @Override
    public void onPartScan(@NotNull IMultiPart part) {
        super.onPartScan(part);
        switch (part) {
            case ItemBusPartMachine itemBusPart -> {
                var inv = itemBusPart.getInventory();
                if (inv.handlerIO == IO.IN || inv.handlerIO == IO.BOTH) {
                    itemStackTransfers.add(inv.storage);
                }
            }
            case HugeBusPartMachine hugeBusPartMachine -> itemStackTransfers.add(hugeBusPartMachine.getInventory().storage);
            case FluidHatchPartMachine fluidHatchPartMachine -> fluidTankTransfers.add(fluidHatchPartMachine.tank.storage);
            default -> {}
        }
    }
}
