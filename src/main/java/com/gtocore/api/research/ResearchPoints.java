package com.gtocore.api.research;

import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;

public class ResearchPoints extends Reference2LongOpenHashMap<ResearchTag> {

    public ResearchPoints copy() {
        ResearchPoints copy = new ResearchPoints();
        for (var entry : this.reference2LongEntrySet()) {
            copy.put(entry.getKey(), entry.getLongValue());
        }
        return copy;
    }

    public ResearchPoints copyWithWeight(float weight) {
        ResearchPoints copy = new ResearchPoints();
        for (var entry : this.reference2LongEntrySet()) {
            copy.put(entry.getKey(), (long) (entry.getLongValue() * weight));
        }
        return copy;
    }

    public long countBytes() {
        long totalBytes = 0;
        for (var entry : this.reference2LongEntrySet()) {
            totalBytes += entry.getKey().getBytePerPoint() * entry.getLongValue();
        }
        return totalBytes;
    }
}
