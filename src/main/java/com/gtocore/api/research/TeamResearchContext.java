package com.gtocore.api.research;

import com.gtocore.api.techtree.TechNode;

import appeng.api.stacks.AEKey;

import com.gto.fastcollection.O2OOpenCacheHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class TeamResearchContext {

    private final Map<ResearchTag, Long> researchPoints;
    private final Set<AEKey> scannedItems;
    private final Map<TechNode, Long> techNodeAccCWU;

    public TeamResearchContext() {
        this(new O2OOpenCacheHashMap<>(), new ReferenceOpenHashSet<>(), new O2OOpenCacheHashMap<>());
    }

    public TeamResearchContext(
                               Map<ResearchTag, Long> researchPoints,
                               Set<AEKey> scannedItems,
                               Map<TechNode, Long> techNodeAccCWU) {
        this.researchPoints = researchPoints;
        this.scannedItems = scannedItems;
        this.techNodeAccCWU = techNodeAccCWU;
    }

    public boolean isEmpty() {
        return researchPoints.isEmpty() && scannedItems.isEmpty() && techNodeAccCWU.isEmpty();
    }

    public void addTechNodeAccCWU(TechNode selectedNode, long cwuBuffer) {
        techNodeAccCWU.merge(selectedNode, cwuBuffer, Long::sum);
        TeamResearchSavedDtat.INSTANCE.setDirty(true);
    }
}
