package com.gtocore.integration.emi;

import com.gtocore.common.item.ItemMap;
import com.gtocore.data.tag.Tags;
import com.gtocore.integration.emi.electrochemistry.IonEmiStack;
import com.gtocore.integration.emi.multipage.MultiblockInfoEmiRecipe;

import com.gtolib.api.emi.stack.EmiTagPrefixUtils;
import com.gtolib.api.emi.stack.EmiTagprefixStack;

import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.integration.emi.recipe.GTEmiRecipe;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.FakeSlot;
import appeng.parts.encoding.EncodingMode;

import com.hepdd.gtmthings.api.misc.Hatch;
import com.hepdd.gtmthings.common.item.VirtualFluidProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualItemProviderBehavior;
import com.hepdd.gtmthings.common.item.VirtualProviderData;
import com.hepdd.gtmthings.data.CustomItems;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GTEmiEncodingHelper {

    /**
     * 与 {@code EncodingHelper.ENTRY_COMPARATOR} 一致，用于挑选网络中最合适的那个候选物品。
     */
    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .<GridInventoryEntry>comparingInt(entry -> entry.isCraftable() ? 1 : 0)
            .thenComparingInt(entry -> isUndamaged(entry) ? 1 : 0)
            .thenComparingLong(GridInventoryEntry::getStoredAmount);

    @Nullable
    private static GenericStack ofVirtual(EmiStack stack, long amount) {
        if (stack.getKey() instanceof Item) {
            var item = CustomItems.VIRTUAL_ITEM_PROVIDER.asStack();
            VirtualItemProviderBehavior.setVirtualItem(item, stack.getItemStack());
            VirtualProviderData.setLocked(item, true);
            return new GenericStack(AEItemKey.of(item), amount);
        } else if (stack.getKey() instanceof Fluid fluid) {
            var item = CustomItems.VIRTUAL_FLUID_PROVIDER.asStack();
            VirtualFluidProviderBehavior.setVirtualFluid(item,
                    new FluidStack(fluid, 1, stack.getNbt()));
            VirtualProviderData.setLocked(item, true);
            return new GenericStack(AEItemKey.of(item), amount);
        }
        return null;
    }

    private static @NotNull List<GenericStack> intoGenericStack(@NotNull EmiIngredient ingredient, boolean virtual) {
        if (ingredient.isEmpty() || ingredient instanceof IonEmiStack) return Collections.emptyList();
        boolean circuits = ingredient instanceof TagEmiIngredient tag && Tags.CIRCUITS_ARRAY.containsKey(tag.key);
        if (!circuits) {
            var preferred = EmiTagPreference.get(ingredient);
            if (preferred != null) {
                return Collections.singletonList(fromEmiStackVirtualConvertible(preferred, ingredient.getAmount(), virtual));
            }
        }
        var stacks = ingredient.getEmiStacks();
        var result = new ObjectArrayList<GenericStack>(stacks.size());
        for (var stack : stacks) {
            if (circuits && (!(stack instanceof ItemEmiStack item) || !ItemMap.UNIVERSAL_CIRCUITS.contains(item.getKey()))) continue;
            result.add(fromEmiStackVirtualConvertible(stack, ingredient.getAmount(), virtual));
        }
        return result;
    }

    private static GenericStack fromEmiStackVirtualConvertible(EmiStack stack, long amount, boolean virtual) {
        if (stack instanceof EmiTagprefixStack prefix) {
            return new GenericStack(EmiTagPrefixUtils.toGenericStack(prefix).what(), amount);
        }
        if (stack.getKey() instanceof Item) {
            if (virtual) {
                return ofVirtual(stack, amount);
            }
            return new GenericStack(EmiStackHelper.toAEKey(stack), amount);
        } else if (stack.getKey() instanceof Fluid fluid) {
            if (virtual) {
                return ofVirtual(stack, amount);
            }
            return new GenericStack(EmiStackHelper.toAEKey(stack), amount);
        }
        return new GenericStack(AEItemKey.of(Items.STICK), 0);
    }

    public static List<GenericStack> intoGenericStack(EmiIngredient ingredient) {
        return intoGenericStack(ingredient, false);
    }

    public static @NotNull List<List<GenericStack>> ofInputs(@NotNull EmiRecipe emiRecipe) {
        var inputs = emiRecipe instanceof GTEmiRecipe gt ? gt.getEncodingInputs() : emiRecipe.getInputs();
        if (emiRecipe instanceof MultiblockInfoEmiRecipe) {
            var result = new ObjectArrayList<List<GenericStack>>(inputs.size());
            for (var input : inputs) {
                var converted = bucketAsFluid(input);
                if (isNotHatch(converted)) result.add(intoGenericStack(converted));
            }
            return result;
        }
        var catalysts = emiRecipe instanceof GTEmiRecipe gt ? gt.getEncodingCatalysts() : emiRecipe.getCatalysts();
        var result = new ObjectArrayList<List<GenericStack>>(inputs.size() + catalysts.size() + 2);
        boolean ctrl = GTUtil.isCtrlDown();
        if (GTUtil.isShiftDown() || ctrl) {
            for (var catalyst : catalysts) result.add(intoGenericStack(catalyst, ctrl));
            if (result.isEmpty() && ctrl) {
                var itemProvider = CustomItems.VIRTUAL_ITEM_PROVIDER.asStack();
                VirtualProviderData.setLocked(itemProvider, true);
                result.add(List.of(new GenericStack(AEItemKey.of(itemProvider), 1)));
                var fluidProvider = CustomItems.VIRTUAL_FLUID_PROVIDER.asStack();
                VirtualProviderData.setLocked(fluidProvider, true);
                result.add(List.of(new GenericStack(AEItemKey.of(fluidProvider), 1)));
            }
        }
        for (var input : inputs) result.add(intoGenericStack(input));
        return result;
    }

    private static EmiIngredient bucketAsFluid(EmiIngredient ingredient) {
        if (ingredient instanceof ItemEmiStack itemStack && itemStack.getKey() instanceof BucketItem bucketItem) {
            return EmiStack.of(bucketItem.getFluid(), ingredient.getAmount() * 1000);
        }
        return ingredient;
    }

    private static boolean isNotHatch(EmiIngredient ingredient) {
        if (ingredient instanceof EmiStack stack) {
            return !(stack.getKey() instanceof MetaMachineItem meta) || !Hatch.Set.contains(meta.getBlock());
        }
        return false;
    }

    /**
     * 等价于 {@link EncodingHelper#encodeProcessingRecipe}，但不会把配方里相同的物品合并到同一个槽位，
     * 配方的每一项输入（输出）各占一个槽位，超出槽位数量的部分会被丢弃。
     */
    public static void encodeProcessingRecipeWithoutMerging(PatternEncodingTermMenu menu,
                                                            List<List<GenericStack>> genericIngredients,
                                                            List<GenericStack> genericResults) {
        menu.setMode(EncodingMode.PROCESSING);
        var ingredientPriorities = EncodingHelper.getIngredientPriorities(menu, ENTRY_COMPARATOR);
        encodeIntoSlots(genericIngredients, ingredientPriorities, menu.getProcessingInputSlots());
        var slots = menu.getProcessingOutputSlots();
        for (int i = 0; i < slots.length; i++) {
            slots[i].setFilterTo(i < genericResults.size() ? GenericStack.wrapInItemStack(genericResults.get(i)) : ItemStack.EMPTY);
        }
    }

    private static void encodeIntoSlots(@NotNull List<List<GenericStack>> possibleStacksBySlot,
                                        @NotNull Reference2IntOpenHashMap<AEKey> ingredientPriorities, @NotNull FakeSlot[] slots) {
        int slot = 0;
        for (var candidates : possibleStacksBySlot) {
            if (slot == slots.length) break;
            if (!candidates.isEmpty()) {
                slots[slot++].setFilterTo(GenericStack.wrapInItemStack(EncodingHelper.findBestIngredient(ingredientPriorities, candidates)));
            }
        }
        while (slot < slots.length) slots[slot++].setFilterTo(ItemStack.EMPTY);
    }

    private static boolean isUndamaged(GridInventoryEntry entry) {
        return !(entry.getWhat() instanceof AEItemKey itemKey) || !itemKey.isDamaged();
    }
}
