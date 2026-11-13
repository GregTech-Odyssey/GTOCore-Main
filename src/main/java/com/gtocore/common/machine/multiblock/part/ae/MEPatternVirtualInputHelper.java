package com.gtocore.common.machine.multiblock.part.ae;

import com.gtolib.api.ae2.MyPatternDetailsHelper;
import com.gtolib.api.recipe.RecipeBuilder;
import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.machine.trait.NotifiableInventory;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.Circuits;
import com.gregtechceu.gtceu.api.transfer.key.KeyInventory;
import com.gregtechceu.gtceu.api.transfer.key.Keys;

import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.crafting.pattern.AEProcessingPattern;

import com.hepdd.gtmthings.common.item.VirtualFluidProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualProviderData;
import com.hepdd.gtmthings.data.CustomItems;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;
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
                                                   NotifiableInventory<AEItemKey> circuitInventory,
                                                   KeyInventory<AEItemKey> itemStorage,
                                                   KeyInventory<AEFluidKey> fluidStorage,
                                                   @Nullable MEVirtualInputState virtualInputState,
                                                   BooleanSupplier lockOnce) {
        return convertPattern(pattern, gridGetter, actionSourceGetter, circuitInventory, itemStorage, fluidStorage,
                virtualInputState, null, lockOnce);
    }

    static @NotNull IPatternDetails convertPattern(
                                                   @NotNull IPatternDetails pattern,
                                                   Supplier<IGrid> gridGetter,
                                                   Supplier<IActionSource> actionSourceGetter,
                                                   NotifiableInventory<AEItemKey> circuitInventory,
                                                   KeyInventory<AEItemKey> itemStorage,
                                                   KeyInventory<AEFluidKey> fluidStorage,
                                                   @Nullable MEVirtualInputState virtualInputState,
                                                   @Nullable MEVirtualInputAvailability availability,
                                                   BooleanSupplier lockOnce) {
        if (virtualInputState != null) virtualInputState.clearVirtualInputs();
        if (availability != null) availability.clear();
        if (!(pattern instanceof AEProcessingPattern processingPattern)) {
            return pattern;
        }

        var sparseInput = processingPattern.getSparseInputs();
        var input = new ArrayList<GenericStack>(sparseInput.length);
        int targetItemSlot = 0;
        int targetFluidSlot = 0;
        var locked = false;
        Set<Object> virtuals = new ReferenceOpenHashSet<>();
        for (var stack : sparseInput) {
            if (!(stack.what() instanceof AEItemKey what) || !isVirtualProvider(what)) {
                input.add(stack);
                continue;
            }

            if (what.getItem() == CustomItems.VIRTUAL_ITEM_PROVIDER.get()) {
                ItemStack virtualItem = VirtualItemProviderBehavior.getVirtualItem(what.getReadOnlyStack());
                if (virtualItem.isEmpty()) continue;
                if (!virtuals.add(virtualItem.getItem())) continue;
                boolean missingProvider = availability != null && !isProviderAvailable(what, gridGetter, actionSourceGetter);
                if (!locked) {
                    locked = lockOnce.getAsBoolean();
                }
                if (virtualItem.getItem() == Circuits.item()) {
                    var circuit = AEItemKey.of(virtualItem);
                    if (virtualInputState == null) {
                        circuitInventory.storage.set(0, circuit, 1);
                    } else {
                        virtualInputState.setVirtualCircuit(circuit);
                    }
                    if (availability != null) availability.setCircuitMissing(missingProvider);
                    continue;
                }

                int itemSlots = itemStorage.size();
                while (targetItemSlot < itemSlots) {
                    var previous = itemStorage.keyAt(targetItemSlot);
                    if (previous == null || refund(previous, itemStorage.amountAt(targetItemSlot), gridGetter, actionSourceGetter)) break;
                    targetItemSlot++;
                }
                if (targetItemSlot >= itemSlots) continue;
                var virtualKey = AEItemKey.of(virtualItem);
                if (virtualKey == null) continue;
                long virtualAmount = Math.clamp(stack.amount(), 1L, VIRTUAL_ITEM_MAX_AMOUNT);
                if (virtualInputState == null) {
                    itemStorage.set(targetItemSlot, virtualKey, virtualAmount);
                } else {
                    virtualInputState.setVirtualItem(targetItemSlot, virtualKey, virtualAmount);
                }
                if (availability != null) availability.setItemMissing(targetItemSlot, missingProvider);
                targetItemSlot++;
            } else {
                FluidStack virtualFluid = VirtualFluidProviderBehavior.getVirtualFluid(what.getReadOnlyStack());
                if (virtualFluid.isEmpty()) continue;
                if (!virtuals.add(virtualFluid.getFluid())) continue;
                boolean missingProvider = availability != null && !isProviderAvailable(what, gridGetter, actionSourceGetter);
                if (!locked) {
                    locked = lockOnce.getAsBoolean();
                }
                var fluidKey = Keys.fluid(virtualFluid);
                if (fluidKey == null) continue;
                long fluidAmount = Math.clamp(stack.amount(), 1L, Integer.MAX_VALUE);
                int fluidSlots = fluidStorage.size();
                while (targetFluidSlot < fluidSlots) {
                    var previous = fluidStorage.keyAt(targetFluidSlot);
                    if (previous == null || refund(previous, fluidStorage.amountAt(targetFluidSlot), gridGetter, actionSourceGetter)) break;
                    targetFluidSlot++;
                }
                if (targetFluidSlot >= fluidSlots) continue;
                if (virtualInputState == null) {
                    fluidStorage.set(targetFluidSlot, fluidKey, fluidAmount);
                } else {
                    virtualInputState.setVirtualFluid(targetFluidSlot, fluidKey, fluidAmount);
                }
                if (availability != null) availability.setFluidMissing(targetFluidSlot, missingProvider);
                targetFluidSlot++;
            }
        }
        if (input.size() == sparseInput.length || input.isEmpty()) {
            return pattern;
        }
        var stack = PatternDetailsHelper.encodeProcessingPattern(input.toArray(new GenericStack[0]), processingPattern.getSparseOutputs());
        return MyPatternDetailsHelper.decode(AEItemKey.of(stack));
    }

    static boolean isVirtualProvider(AEItemKey key) {
        var stack = key.getReadOnlyStack();
        if (!VirtualProviderData.hasData(stack)) return false;
        var item = key.getItem();
        return item == CustomItems.VIRTUAL_ITEM_PROVIDER.get() ||
                item == CustomItems.VIRTUAL_FLUID_PROVIDER.get();
    }

    private static boolean isProviderAvailable(AEItemKey key, Supplier<IGrid> gridGetter,
                                               Supplier<IActionSource> actionSourceGetter) {
        IGrid grid = gridGetter.get();
        if (grid == null) return false;
        IActionSource source = actionSourceGetter.get();
        if (source == null) return false;
        return grid.getStorageService().getInventory().extract(key, 1, Actionable.SIMULATE, source) == 1;
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
