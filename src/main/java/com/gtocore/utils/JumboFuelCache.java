package com.gtocore.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

/**
 * Burn-time cache for Jumbo Furnace fuel checks.
 *
 * The sampled server hot path reaches {@code FuelItemHandler.isItemValid} from item-pipe transfers.
 * Each calling thread owns up to 512 entries so menu prediction cannot race server transfers.
 * Recipe type is part of the key because burn-time event handlers can return different values for
 * different types. Burn-time results can be any int, so a nullable entry distinguishes misses from
 * negative values. Runtime-dependent event handlers can disable this cache with its system property.
 */
public final class JumboFuelCache {

    // -Dgtocore.jumbo.fuelcache=false 关闭缓存；-Dgtocore.debug.jumbo.fuelcache=true 打开命中统计
    private static final boolean ENABLED = !"false".equalsIgnoreCase(System.getProperty("gtocore.jumbo.fuelcache", "true"));
    private static final boolean DEBUG = Boolean.getBoolean("gtocore.debug.jumbo.fuelcache");
    private static final int LIMIT = 512;
    private static final ThreadLocal<CacheState> STATE = ThreadLocal.withInitial(CacheState::new);
    private static final Hash.Strategy<Key> KEY = new Hash.Strategy<>() {

        @Override
        public int hashCode(Key key) {
            return key.hash;
        }

        @Override
        public boolean equals(Key left, Key right) {
            if (left == right) return true;
            if (left == null || right == null) return false;
            if (left.hash != right.hash || left.recipeType != right.recipeType) return false;
            return left.stack.getItem() == right.stack.getItem() && Objects.equals(left.stack.getTag(), right.stack.getTag());
        }
    };
    private static final LongAdder HITS = new LongAdder();
    private static final LongAdder MISSES = new LongAdder();
    private static final LongAdder PASSTHROUGHS = new LongAdder();
    private static final LongAdder EVICTIONS = new LongAdder();

    private JumboFuelCache() {}

    public static int get(ItemStack stack, RecipeType<?> recipeType) {
        if (!ENABLED) {
            if (DEBUG) PASSTHROUGHS.increment();
            return ForgeHooks.getBurnTime(stack, recipeType);
        }
        CacheState state = STATE.get();
        state.lookup.set(stack, recipeType);
        Entry cached = state.cache.get(state.lookup);
        if (cached != null) {
            if (DEBUG) HITS.increment();
            return cached.burnTime;
        }
        if (DEBUG) MISSES.increment();
        int burnTime = ForgeHooks.getBurnTime(stack, recipeType);
        if (state.size == LIMIT) {
            Entry oldest = state.evictionQueue[state.evictionIndex];
            state.cache.remove(oldest.key);
            state.size--;
            if (DEBUG) EVICTIONS.increment();
        }
        Key key = new Key(stack.copyWithCount(1), recipeType, state.lookup.hash);
        Entry entry = new Entry(key, burnTime);
        state.cache.put(key, entry);
        state.evictionQueue[state.evictionIndex] = entry;
        state.size++;
        if (++state.evictionIndex == LIMIT) state.evictionIndex = 0;
        return burnTime;
    }

    public static String debug() {
        CacheState state = ENABLED ? STATE.get() : null;
        int size = state == null ? 0 : state.size;
        return "enabled=" + ENABLED + ", hits=" + HITS.sum() + ", misses=" + MISSES.sum() + ", passthroughs=" + PASSTHROUGHS.sum() + ", evictions=" + EVICTIONS.sum() + ", threadSize=" + size + '/' + LIMIT;
    }

    private static final class CacheState {

        private final Entry[] evictionQueue = new Entry[LIMIT];
        private final Key lookup = new Key();
        private final Object2ObjectOpenCustomHashMap<Key, Entry> cache = new Object2ObjectOpenCustomHashMap<>(LIMIT, KEY);
        private int evictionIndex;
        private int size;
    }

    private static final class Key {

        private ItemStack stack;
        private RecipeType<?> recipeType;
        private int hash;

        private Key() {}

        private Key(ItemStack stack, RecipeType<?> recipeType, int hash) {
            this.stack = stack;
            this.recipeType = recipeType;
            this.hash = hash;
        }

        private void set(ItemStack stack, RecipeType<?> recipeType) {
            this.stack = stack;
            this.recipeType = recipeType;
            Item item = stack.getItem();
            CompoundTag tag = stack.getTag();
            this.hash = 31 * (31 * item.hashCode() + (tag == null ? 0 : tag.hashCode())) + System.identityHashCode(recipeType);
        }
    }

    private record Entry(Key key, int burnTime) {}
}
