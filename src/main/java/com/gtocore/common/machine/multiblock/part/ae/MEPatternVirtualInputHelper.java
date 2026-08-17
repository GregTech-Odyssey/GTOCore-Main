package com.gtocore.common.machine.multiblock.part.ae;

import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.crafting.pattern.AEProcessingPattern;

import com.hepdd.gtmthings.common.item.VirtualFluidProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualProviderData;
import com.hepdd.gtmthings.data.CustomItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class MEPatternVirtualInputHelper {

    private static final int VIRTUAL_ITEM_MAX_AMOUNT = 64;

    private MEPatternVirtualInputHelper() {}

    static void readRecipeTag(ItemStack stack, Consumer<GTRecipeDefinition> recipeSetter) {
        if (stack.getOrCreateTag().tags.get("recipe") instanceof StringTag stringTag) {
            recipeSetter.accept(RecipeBuilder.get(RLUtils.parse(stringTag.getAsString())));
        }
    }

    static @NotNull IPatternDetails convertPattern(
                                                   @NotNull IPatternDetails pattern,
                                                   Supplier<IGrid> gridGetter,
                                                   Supplier<IActionSource> actionSourceGetter,
                                                   NotifiableItemStackHandler circuitInventory,
                                                   CustomItemStackHandler itemStorage,
                                                   CustomFluidTank[] fluidStorage,
                                                   @Nullable MEVirtualInputState virtualInputState,
                                                   BooleanSupplier lockOnce) {
        if (virtualInputState != null) virtualInputState.clearVirtualInputs();
        if (!(pattern instanceof AEProcessingPattern processingPattern)) {
            return pattern;
        }

        var sparseInput = processingPattern.getSparseInputs();
        int targetItemSlot = 0;
        int targetFluidSlot = 0;
        var locked = false;
        for (var stack : sparseInput) {
            if (stack == null || !(stack.what() instanceof AEItemKey what) || !isVirtualProvider(what)) continue;

            if (what.getItem() == CustomItems.VIRTUAL_ITEM_PROVIDER.get()) {
                ItemStack virtualItem = VirtualItemProviderBehavior.getVirtualItem(what.getReadOnlyStack());
                if (virtualItem.isEmpty()) continue;
                if (!locked) {
                    locked = lockOnce.getAsBoolean();
                }
                if (GTItems.PROGRAMMED_CIRCUIT.isIn(virtualItem)) {
                    if (virtualInputState == null) {
                        circuitInventory.storage.setStackInSlot(0, virtualItem.copyWithCount(1));
                    } else {
                        virtualInputState.setVirtualCircuit(virtualItem.copyWithCount(1));
                    }
                    continue;
                }

                while (targetItemSlot < itemStorage.getSlots()) {
                    ItemStack previous = itemStorage.getStackInSlot(targetItemSlot);
                    if (previous.isEmpty() || refund(AEItemKey.of(previous), previous.getCount(), gridGetter, actionSourceGetter)) break;
                    targetItemSlot++;
                }
                if (targetItemSlot >= itemStorage.getSlots()) continue;
                virtualItem.setCount((int) Math.clamp(stack.amount(), 1L, VIRTUAL_ITEM_MAX_AMOUNT));
                if (virtualInputState == null) {
                    itemStorage.setStackInSlot(targetItemSlot, virtualItem);
                } else {
                    virtualInputState.setVirtualItem(targetItemSlot, virtualItem);
                }
                targetItemSlot++;
            } else {
                FluidStack virtualFluid = VirtualFluidProviderBehavior.getVirtualFluid(what.getReadOnlyStack());
                if (virtualFluid.isEmpty()) continue;
                if (!locked) {
                    locked = lockOnce.getAsBoolean();
                }
                virtualFluid.setAmount((int) Math.clamp(stack.amount(), 1L, Integer.MAX_VALUE));
                while (targetFluidSlot < fluidStorage.length) {
                    FluidStack previous = fluidStorage[targetFluidSlot].getFluid();
                    if (previous.isEmpty() || refund(AEFluidKey.of(previous), previous.getAmount(), gridGetter, actionSourceGetter)) break;
                    targetFluidSlot++;
                }
                if (targetFluidSlot >= fluidStorage.length) continue;
                if (virtualInputState == null) {
                    fluidStorage[targetFluidSlot].setFluid(virtualFluid);
                } else {
                    virtualInputState.setVirtualFluid(targetFluidSlot, virtualFluid);
                }
                targetFluidSlot++;
            }
        }
        return pattern;
    }

    static boolean isVirtualProvider(AEItemKey key) {
        if (!VirtualProviderData.hasData(key.getReadOnlyStack())) return false;
        var item = key.getItem();
        return item == CustomItems.VIRTUAL_ITEM_PROVIDER.get() ||
                item == CustomItems.VIRTUAL_FLUID_PROVIDER.get();
    }

    private static boolean refund(AEKey key, long amount, Supplier<IGrid> gridGetter,
                                  Supplier<IActionSource> actionSourceGetter) {
        IGrid grid = gridGetter.get();
        if (grid == null) return false;
        IActionSource source = actionSourceGetter.get();
        if (source == null) return false;
        var inventory = grid.getStorageService().getInventory();
        if (inventory.insert(key, amount, Actionable.SIMULATE, source) != amount) return false;
        return inventory.insert(key, amount, Actionable.MODULATE, source) == amount;
    }
}
