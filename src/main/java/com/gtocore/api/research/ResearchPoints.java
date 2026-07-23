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

    public static ResearchPoints of(ResearchTag tag, long points) {
        ResearchPoints researchPoints = new ResearchPoints();
        researchPoints.put(tag, points);
        return researchPoints;
    }

    public static ResearchPoints of(ResearchTag tag0, long points0, ResearchTag tag1, long points1) {
        ResearchPoints researchPoints = new ResearchPoints();
        researchPoints.put(tag0, points0);
        return researchPoints;
    }

    public static ResearchPoints of(ResearchTag tag0, long points0, ResearchTag tag1, long points1, ResearchTag tag2, long points2) {
        ResearchPoints researchPoints = new ResearchPoints();
        researchPoints.put(tag0, points0);
        researchPoints.put(tag1, points1);
        researchPoints.put(tag2, points2);
        return researchPoints;
    }

    public static ResearchPoints of(ResearchTag tag0, long points0, ResearchTag tag1, long points1, ResearchTag tag2, long points2, Object... additionalTagsAndPoints) {
        ResearchPoints researchPoints = new ResearchPoints();
        researchPoints.put(tag0, points0);
        researchPoints.put(tag1, points1);
        researchPoints.put(tag2, points2);
        for (int i = 0; i < additionalTagsAndPoints.length - 1; i += 2) {
            ResearchTag tag = (ResearchTag) additionalTagsAndPoints[i];
            long points = (long) additionalTagsAndPoints[i + 1];
            researchPoints.put(tag, points);
        }
        return researchPoints;
    }
}
