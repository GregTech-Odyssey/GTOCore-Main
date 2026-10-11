package com.hepdd.gtmthings.common.item;

import com.gtolib.utils.RLUtils;

import com.gregtechceu.gtceu.api.item.IDataComponentItem;
import com.gregtechceu.gtceu.api.item.ItemDataComponentKey;

import net.minecraft.nbt.ByteTag;
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

    private static final ItemDataComponentKey<Boolean, ByteTag> LOCKED = ItemDataComponentKey.create(LOCKED_TAG, ByteTag::valueOf, t -> t.getAsByte() > 0);
    private static final ItemDataComponentKey<ItemStack, CompoundTag> ITEM_STACK = ItemDataComponentKey.createCompound(null, VirtualProviderData::writeItem, VirtualProviderData::readItem, VirtualProviderData::removeItem);
    private static final ItemDataComponentKey<FluidStack, CompoundTag> FLUID_STACK = ItemDataComponentKey.createCompound(null, VirtualProviderData::writeFluid, VirtualProviderData::readFluid, VirtualProviderData::removeFluid);

    private VirtualProviderData() {}

    private static void writeItem(CompoundTag tag, ItemStack virtualItem) {
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

    private static void removeItem(CompoundTag tag) {
        tag.remove(MOD_TAG);
        tag.remove(NAME_TAG);
        tag.remove(NBT_TAG);
    }

    private static ItemStack readItem(CompoundTag tag) {
        if (tag == null) return ItemStack.EMPTY;
        var mod = tag.getString(MOD_TAG);
        if (mod.isEmpty()) return ItemStack.EMPTY;
        var item = ForgeRegistries.ITEMS.getValue(RLUtils.fromNamespaceAndPath(mod, tag.getString(NAME_TAG)));
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        var stack = item.getDefaultInstance();
        if (tag.get(NBT_TAG) instanceof CompoundTag compoundTag) stack.setTag(compoundTag);
        return stack;
    }

    private static void writeFluid(CompoundTag tag, FluidStack virtualFluid) {
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

    private static void removeFluid(CompoundTag tag) {
        tag.remove(MOD_TAG);
        tag.remove(NAME_TAG);
        tag.remove(NBT_TAG);
    }

    private static FluidStack readFluid(CompoundTag tag) {
        if (tag == null) return FluidStack.EMPTY;
        var mod = tag.getString(MOD_TAG);
        if (mod.isEmpty()) return FluidStack.EMPTY;
        var fluid = ForgeRegistries.FLUIDS.getValue(RLUtils.fromNamespaceAndPath(mod, tag.getString(NAME_TAG)));
        if (fluid == null || fluid == Fluids.EMPTY) return FluidStack.EMPTY;
        var fluidStack = new FluidStack(fluid, 1000);
        if (tag.get(NBT_TAG) instanceof CompoundTag compoundTag) fluidStack.setTag(compoundTag);
        return fluidStack;
    }

    public static ItemStack setVirtualItem(ItemStack stack, ItemStack virtualItem) {
        if (virtualItem.isEmpty()) {
            IDataComponentItem.set(stack, ITEM_STACK, null);
        } else {
            IDataComponentItem.set(stack, ITEM_STACK, virtualItem);
        }
        return stack;
    }

    public static ItemStack getVirtualItem(ItemStack stack) {
        return IDataComponentItem.getOrDefault(stack, ITEM_STACK, ItemStack.EMPTY);
    }

    public static ItemStack setVirtualFluid(ItemStack stack, FluidStack virtualFluid) {
        if (virtualFluid.isEmpty()) {
            IDataComponentItem.set(stack, FLUID_STACK, null);
        } else {
            IDataComponentItem.set(stack, FLUID_STACK, virtualFluid);
        }
        return stack;
    }

    public static FluidStack getVirtualFluid(final ItemStack stack) {
        return IDataComponentItem.getOrDefault(stack, FLUID_STACK, FluidStack.EMPTY);
    }

    public static ItemStack setLocked(ItemStack stack, boolean locked) {
        if (locked) {
            IDataComponentItem.set(stack, LOCKED, true);
        } else {
            IDataComponentItem.set(stack, LOCKED, null);
        }
        return stack;
    }

    public static boolean isLocked(ItemStack stack) {
        return IDataComponentItem.getBoolean(stack, LOCKED);
    }

    public static boolean hasData(ItemStack stack) {
        var tag = stack.getTag();
        return tag != null && !tag.isEmpty();
    }
}
