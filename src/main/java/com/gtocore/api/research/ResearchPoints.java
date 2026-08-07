package com.gtocore.api.research;

import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;

public class ResearchPoints extends Reference2LongOpenHashMap<ResearchTag> {

    public ResearchPoints() {
        super();
    }

    public ResearchPoints(int expected) {
        super(expected);
    }

    public ResearchPoints copy() {
        // fastutil clone() 走底层数组批量拷贝（Arrays.copyOf），比逐项迭代 reopen 快得多
        // 且 super.clone() 保留运行时类型，返回的即 ResearchPoints 实例
        return (ResearchPoints) clone();
    }

    public ResearchPoints copyWithWeight(float weight) {
        // 按源条目数预分配，避免默认容量下的多次扩容 rehash；仍须逐项算权重并过滤 <=0 的项
        ResearchPoints copy = new ResearchPoints(this.size());
        for (var it = this.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            long weightedValue = (long) (entry.getLongValue() * weight);
            if (weightedValue > 0) {
                copy.put(entry.getKey(), weightedValue);
            }
        }
        return copy;
    }

    public long countBytes() {
        long totalBytes = 0;
        for (var it = this.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
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
