package com.gtocore.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

import java.util.Objects;

/**
 * Burn-time cache for the jumbo furnace fuel slots.
 *
 * Burn times are fixed for the lifetime of the game: they come from registry items, static fuel maps
 * and item NBT, and the project's datapacks are fixed, so the cache has no reload invalidation point.
 * A bounded FIFO keeps recent entries when new fuel variants exceed the cache limit.
 */
public final class JumboFuelCache {

    // -Dgtocore.jumbo.fuelcache=false 关闭缓存；-Dgtocore.debug.jumbo.fuelcache=true 打开命中统计
    private static final boolean ENABLED = !"false".equalsIgnoreCase(System.getProperty("gtocore.jumbo.fuelcache", "true"));
    private static final boolean DEBUG = Boolean.getBoolean("gtocore.debug.jumbo.fuelcache");
    private static final int LIMIT = 512;
    private static final ItemStack[] EVICTION_QUEUE = new ItemStack[LIMIT];
    private static int evictionIndex;
    private static final Hash.Strategy<ItemStack> KEY = new Hash.Strategy<>() {

        @Override
        public int hashCode(ItemStack stack) {
            if (stack == null) return 0;
            Item item = stack.getItem();
            CompoundTag tag = stack.getTag();
            return 31 * item.hashCode() + (tag == null ? 0 : tag.hashCode());
        }

        @Override
        public boolean equals(ItemStack left, ItemStack right) {
            if (left == right) return true;
            if (left == null || right == null) return false;
            return left.getItem() == right.getItem() && Objects.equals(left.getTag(), right.getTag());
        }
    };
    private static final Object2IntOpenCustomHashMap<ItemStack> CACHE = new Object2IntOpenCustomHashMap<>(256, KEY);

    static {
        CACHE.defaultReturnValue(-1);
    }

    private static long hits;
    private static long misses;
    private static long evictions;

    private JumboFuelCache() {}

    public static int get(ItemStack stack, RecipeType<?> recipeType) {
        if (!ENABLED) return ForgeHooks.getBurnTime(stack, recipeType);
        int burnTime = CACHE.getInt(stack);
        if (burnTime != -1) {
            if (DEBUG) hits++;
            return burnTime;
        }
        if (DEBUG) misses++;
        burnTime = ForgeHooks.getBurnTime(stack, recipeType);
        if (CACHE.size() >= LIMIT) {
            CACHE.removeInt(EVICTION_QUEUE[evictionIndex]);
            if (DEBUG) evictions++;
        }
        ItemStack key = stack.copyWithCount(1);
        CACHE.put(key, burnTime);
        EVICTION_QUEUE[evictionIndex] = key;
        if (++evictionIndex == LIMIT) evictionIndex = 0;
        return burnTime;
    }

    public static String debug() {
        return "enabled=" + ENABLED + ", hits=" + hits + ", misses=" + misses + ", evictions=" + evictions + ", size=" + CACHE.size() + '/' + LIMIT;
    }
}
