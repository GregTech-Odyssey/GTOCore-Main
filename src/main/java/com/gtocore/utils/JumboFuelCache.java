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
 * Item-pipe transfers reach {@code FuelItemHandler.isItemValid} on every insert attempt and repeat
 * the burn-time lookup. The key is item, NBT, stack count and recipe type: the lookup and the fuel
 * event can read any of them, and 1.20.1 stores damage inside the NBT, so damage needs no key field.
 * Burn-time results can be any int, so a nullable entry distinguishes misses from negative values.
 * State is per thread, so an insert off the server thread cannot corrupt the server's entries.
 * Runtime-dependent event handlers can disable this cache with its system property.
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
            if (left.hash != right.hash || left.recipeType != right.recipeType || left.count != right.count) return false;
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
        Key key = new Key(stack.copy(), recipeType, state.lookup.count, state.lookup.hash);
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
        // 计数只在 DEBUG 打开时累加，关着时这些 0 没有意义，直接标明而不是让人误以为从不命中。
        if (!DEBUG) return "enabled=" + ENABLED + ", stats=off, threadSize=" + size + '/' + LIMIT;
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
        private int count;
        private int hash;

        private Key() {}

        private Key(ItemStack stack, RecipeType<?> recipeType, int count, int hash) {
            this.stack = stack;
            this.recipeType = recipeType;
            this.count = count;
            this.hash = hash;
        }

        private void set(ItemStack stack, RecipeType<?> recipeType) {
            this.stack = stack;
            this.recipeType = recipeType;
            this.count = stack.getCount();
            Item item = stack.getItem();
            CompoundTag tag = stack.getTag();
            this.hash = 31 * (31 * (31 * item.hashCode() + (tag == null ? 0 : tag.hashCode())) + count) + System.identityHashCode(recipeType);
        }
    }

    private record Entry(Key key, int burnTime) {}
}
