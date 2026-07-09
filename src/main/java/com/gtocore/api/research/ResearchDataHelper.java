package com.gtocore.api.research;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class ResearchDataHelper {

    private static final Reference2ObjectOpenHashMap<AEKey, Reference2LongOpenHashMap<ResearchTag>> CACHE = new Reference2ObjectOpenHashMap<>();

    public static Reference2LongOpenHashMap<ResearchTag> scanForTags(AEKey key) {
        if (key instanceof AEItemKey itemKey && itemKey.hasTag()) {
            return innerScan(key);
        }
        return CACHE.computeIfAbsent(key, k -> innerScan(key));
    }

    private static Reference2LongOpenHashMap<ResearchTag> innerScan(AEKey key) {
        Reference2LongOpenHashMap<ResearchTag> map = new Reference2LongOpenHashMap<>();
        for (ResearchTag tag : ResearchTag.TAGS.values()) {
            long value = tag.valueSupplier().applyAsLong(key);
            if (value > 0) {
                map.put(tag, value);
            }
        }
        return map;
    }
}
