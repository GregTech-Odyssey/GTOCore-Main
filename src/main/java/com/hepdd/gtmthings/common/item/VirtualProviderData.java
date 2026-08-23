package com.hepdd.gtmthings.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class VirtualProviderData {

    private static final String PROVIDER_TAG = "virtual_provider";
    private static final String CONTENT_TAG = "content";
    private static final String LOCKED_TAG = "locked";

    private VirtualProviderData() {}

    public static ItemStack setLocked(ItemStack stack, boolean locked) {
        if (stack.isEmpty()) return stack;
        if (locked) {
            getOrCreateProviderTag(stack).putBoolean(LOCKED_TAG, true);
        } else {
            CompoundTag providerTag = getProviderTag(stack);
            if (providerTag != null) {
                providerTag.remove(LOCKED_TAG);
                removeProviderIfEmpty(stack, providerTag);
            }
        }
        return stack;
    }

    public static boolean isLocked(ItemStack stack) {
        CompoundTag providerTag = getProviderTag(stack);
        return providerTag != null && providerTag.getBoolean(LOCKED_TAG);
    }

    public static boolean hasData(ItemStack stack) {
        CompoundTag providerTag = getProviderTag(stack);
        return providerTag != null && (providerTag.getBoolean(LOCKED_TAG) || hasContent(providerTag));
    }

    public static boolean hasContent(ItemStack stack) {
        CompoundTag providerTag = getProviderTag(stack);
        return providerTag != null && hasContent(providerTag);
    }

    static void setContent(ItemStack stack, CompoundTag content) {
        if (stack.isEmpty()) return;
        if (content.isEmpty()) {
            clearContent(stack);
        } else {
            getOrCreateProviderTag(stack).put(CONTENT_TAG, content);
        }
    }

    static void clearContent(ItemStack stack) {
        CompoundTag providerTag = getProviderTag(stack);
        if (providerTag == null) return;
        providerTag.remove(CONTENT_TAG);
        removeProviderIfEmpty(stack, providerTag);
    }

    @Nullable
    static CompoundTag getContent(ItemStack stack) {
        CompoundTag providerTag = getProviderTag(stack);
        if (providerTag == null || !(providerTag.get(CONTENT_TAG) instanceof CompoundTag content) || content.isEmpty()) {
            return null;
        }
        return content;
    }

    private static boolean hasContent(CompoundTag providerTag) {
        return providerTag.get(CONTENT_TAG) instanceof CompoundTag content && !content.isEmpty();
    }

    private static CompoundTag getOrCreateProviderTag(ItemStack stack) {
        CompoundTag rootTag = stack.getOrCreateTag();
        if (rootTag.get(PROVIDER_TAG) instanceof CompoundTag providerTag) return providerTag;
        CompoundTag providerTag = new CompoundTag();
        rootTag.put(PROVIDER_TAG, providerTag);
        return providerTag;
    }

    @Nullable
    private static CompoundTag getProviderTag(ItemStack stack) {
        CompoundTag rootTag = stack.getTag();
        if (rootTag == null || !(rootTag.get(PROVIDER_TAG) instanceof CompoundTag providerTag)) return null;
        return providerTag;
    }

    private static void removeProviderIfEmpty(ItemStack stack, CompoundTag providerTag) {
        if (!providerTag.isEmpty()) return;
        CompoundTag rootTag = stack.getTag();
        if (rootTag == null) return;
        rootTag.remove(PROVIDER_TAG);
        if (rootTag.isEmpty()) stack.setTag(null);
    }
}
