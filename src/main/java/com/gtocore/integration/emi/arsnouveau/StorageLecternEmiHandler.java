package com.gtocore.integration.emi.arsnouveau;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import com.gto.fastcollection.fastutil.OpenCacheHashSet;

import com.hollingsworth.arsnouveau.client.container.CraftingTerminalMenu;
import com.hollingsworth.arsnouveau.client.container.StoredItemStack;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

// 改写自 Ars Nouveau（Bailey Hollingsworth，LGPLv3）的 JEI 集成，本文件依 LGPLv3 提供
// https://github.com/baileyholl/Ars-Nouveau
public final class StorageLecternEmiHandler implements EmiRecipeHandler<CraftingTerminalMenu> {

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<CraftingTerminalMenu> screen) {
        NonNullList<ItemStack> items = Minecraft.getInstance().player.getInventory().items;
        List<StoredItemStack> stored = screen.getMenu().getStoredItems();
        ObjectArrayList<EmiStack> stacks = new ObjectArrayList<>(items.size() + stored.size());
        for (ItemStack item : items) {
            if (!item.isEmpty()) stacks.add(EmiStack.of(item));
        }
        for (StoredItemStack s : stored) {
            stacks.add(EmiStack.of(s.getStack(), s.getQuantity()));
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe instanceof EmiCraftingRecipe && recipe.getInputs().size() <= 9;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context) {
        return true;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context) {
        CraftingTerminalMenu menu = context.getScreenHandler();
        NonNullList<ItemStack> items = Minecraft.getInstance().player.getInventory().items;
        OpenCacheHashSet<StoredItemStack> stored = new OpenCacheHashSet<>(menu.getStoredItems());
        List<EmiIngredient> inputs = recipe.getInputs();
        ListTag list = new ListTag();
        for (int i = 0; i < inputs.size(); i++) {
            EmiIngredient ingredient = inputs.get(i);
            if (ingredient.isEmpty()) continue;
            CompoundTag slot = new CompoundTag();
            slot.putByte("s", (byte) i);
            int k = 0;
            for (EmiStack candidate : ingredient.getEmiStacks()) {
                if (k >= 9) break;
                ItemStack stack = candidate.getItemStack();
                if (stack.isEmpty()) continue;
                if (stored.contains(new StoredItemStack(stack)) || hasMatching(items, stack)) {
                    slot.put("i" + k++, stack.save(new CompoundTag()));
                }
            }
            slot.putByte("l", (byte) k);
            list.add(slot);
        }
        CompoundTag message = new CompoundTag();
        message.put("i", list);
        Minecraft.getInstance().setScreen(context.getScreen());
        menu.sendMessage(message);
        return true;
    }

    private static boolean hasMatching(NonNullList<ItemStack> items, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        boolean emptyTag = tag != null && tag.isEmpty();
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item.isEmpty()) continue;
            if (emptyTag ? ItemStack.isSameItem(stack, item) : ItemStack.isSameItemSameTags(item, stack)) return true;
        }
        return false;
    }
}
