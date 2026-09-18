package com.hepdd.gtmthings.common.item;

import com.gtolib.utils.RLUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class VirtualProviderData {

    private static final String NBT_TAG = "t";
    private static final String MOD_TAG = "m";
    private static final String NAME_TAG = "n";
    private static final String LOCKED_TAG = "marked";

    private VirtualProviderData() {}

    public static ItemStack setVirtualItem(ItemStack stack, ItemStack virtualItem) {
        CompoundTag tag = stack.getOrCreateTag();
        if (virtualItem.isEmpty()) {
            tag.remove(MOD_TAG);
            tag.remove(NAME_TAG);
            tag.remove(NBT_TAG);
        } else {
            var id = ForgeRegistries.ITEMS.getKey(virtualItem.getItem());
            tag.putString(MOD_TAG, id.getNamespace());
            tag.putString(NAME_TAG, id.getPath());
            CompoundTag itemTag = virtualItem.getTag();
            if (itemTag != null) {
                tag.put(NBT_TAG, itemTag);
            } else {
                tag.remove(NBT_TAG);
            }
        }
        return stack;
    }

    public static ItemStack getVirtualItem(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return ItemStack.EMPTY;
        var mod = tag.getString(MOD_TAG);
        if (mod.isEmpty()) return ItemStack.EMPTY;
        var item = ForgeRegistries.ITEMS.getValue(RLUtils.fromNamespaceAndPath(mod, tag.getString(NAME_TAG)));
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        stack = item.getDefaultInstance();
        if (tag.get(NBT_TAG) instanceof CompoundTag compoundTag) stack.setTag(compoundTag);
        return stack;
    }

    public static ItemStack setVirtualFluid(ItemStack stack, FluidStack virtualFluid) {
        CompoundTag tag = stack.getOrCreateTag();
        if (virtualFluid.isEmpty()) {
            tag.remove(MOD_TAG);
            tag.remove(NAME_TAG);
            tag.remove(NBT_TAG);
        } else {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(virtualFluid.getFluid());
            tag.putString(MOD_TAG, id.getNamespace());
            tag.putString(NAME_TAG, id.getPath());
            CompoundTag itemTag = virtualFluid.getTag();
            if (itemTag != null) {
                tag.put(NBT_TAG, itemTag);
            } else {
                tag.remove(NBT_TAG);
            }
        }
        return stack;
    }

    public static FluidStack getVirtualFluid(final ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return FluidStack.EMPTY;
        var mod = tag.getString(MOD_TAG);
        if (mod.isEmpty()) return FluidStack.EMPTY;
        var fluid = ForgeRegistries.FLUIDS.getValue(RLUtils.fromNamespaceAndPath(mod, tag.getString(NAME_TAG)));
        if (fluid == null || fluid == Fluids.EMPTY) return FluidStack.EMPTY;
        var fluidStack = new FluidStack(fluid, 1000);
        if (tag.get(NBT_TAG) instanceof CompoundTag compoundTag) fluidStack.setTag(compoundTag);
        return fluidStack;
    }

    public static ItemStack setLocked(ItemStack stack, boolean locked) {
        if (stack.isEmpty()) return stack;
        if (locked) {
            stack.getOrCreateTag().putBoolean(LOCKED_TAG, true);
        } else {
            var tag = stack.getTag();
            if (tag != null) {
                tag.remove(LOCKED_TAG);
            }
        }
        return stack;
    }

    public static boolean isLocked(ItemStack stack) {
        var tag = stack.getTag();
        return tag != null && tag.contains(LOCKED_TAG);
    }

    public static boolean hasData(ItemStack stack) {
        var tag = stack.getTag();
        return tag != null && !tag.isEmpty();
    }
}
