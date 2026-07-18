package com.gtocore.api.research;

import com.gtocore.api.research.techtree.TechNode;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@DataGeneratorScanned
public final class ResearchRequirements {

    public static final ResearchRequirements NO_REQUIREMENTS = new ResearchRequirements();
    static {
        NO_REQUIREMENTS.materialNeeded = new ResearchPoints();
    }

    public static final Hash.Strategy<AEKey> AE_KEY_STRATEGY = new Hash.Strategy<>() {

        @Override
        public int hashCode(AEKey key) {
            return key == null || key.getPrimaryKey() == null ? 0 : key.getPrimaryKey().hashCode();
        }

        @Override
        public boolean equals(AEKey left, AEKey right) {
            return left == null ? right == null : right != null && Objects.equals(left.getPrimaryKey(), right.getPrimaryKey());
        }
    };

    private static final Reference2ObjectMap<AEKey, Set<TechNode>> EUREKA_REQUIREMENTS = new Reference2ObjectOpenCustomHashMap<>(AE_KEY_STRATEGY);

    private long cwuNeeded;
    private ResearchPoints materialNeeded;
    @Nullable
    private AEKey eurekaItem;
    private float eurekaProgress;

    private ResearchRequirements() {}

    public static synchronized void registerEurekaRequirement(AEKey key, TechNode node) {
        EUREKA_REQUIREMENTS.computeIfAbsent(key, ignored -> new ReferenceOpenHashSet<>()).add(node);
    }

    public static synchronized Set<TechNode> getEurekaRequirements(AEKey key) {
        var nodes = EUREKA_REQUIREMENTS.get(key);
        return nodes == null ? Set.of() : Set.copyOf(nodes);
    }

    public static synchronized List<EurekaRequirementEntry> getEurekaRequirementEntries() {
        List<EurekaRequirementEntry> entries = new ArrayList<>(EUREKA_REQUIREMENTS.size());
        for (var entry : EUREKA_REQUIREMENTS.reference2ObjectEntrySet()) {
            List<TechNode> nodes = entry.getValue().stream()
                    .sorted(Comparator.comparing((TechNode node) -> node.getManager().getId()).thenComparing(node -> node.name))
                    .toList();
            entries.add(new EurekaRequirementEntry(entry.getKey(), nodes));
        }
        entries.sort(Comparator.comparing((EurekaRequirementEntry entry) -> entry.key().getType().getId().toString())
                .thenComparing(entry -> entry.key().getId().toString()));
        return List.copyOf(entries);
    }

    public ActionResult test(TechNode node, TeamResearchContext teamResource, UUID team, boolean simulate) {
        if (this == NO_REQUIREMENTS) {
            return ActionResult.SUCCESS;
        }
        var eurekaScanned = teamResource.getScannedItems().contains(eurekaItem);
        var actualCWUNeeded = eurekaScanned ? (long) (cwuNeeded * eurekaProgress) : cwuNeeded;
        if (teamResource.getTechNodeAccCWU().getOrDefault(node, 0L) < actualCWUNeeded) {
            return FAILURE_NO_CWU;
        }
        for (Map.Entry<ResearchTag, Long> entry : materialNeeded.reference2LongEntrySet()) {
            var tag = entry.getKey();
            var amount = entry.getValue();
            if (teamResource.getResearchPoints().getOrDefault(tag, 0L) < amount) {
                return FAILURE_NO_MATERIAL;
            }
        }
        return ActionResult.SUCCESS;
    }

    public static class Builder {

        private long cwuNeeded;
        private final ResearchPoints materialNeeded = new ResearchPoints();
        private AEKey eurekaItem;
        private float eurekaProgress;

        public Builder setCWUNeeded(long cwuNeeded) {
            this.cwuNeeded = cwuNeeded;
            return this;
        }

        public Builder addMaterialNeeded(ResearchTag tag, long amount) {
            materialNeeded.put(tag, amount);
            return this;
        }

        public Builder setEurekaItem(ItemLike eurekaItem, float eurekaProgress) {
            this.eurekaProgress = eurekaProgress;
            this.eurekaItem = AEItemKey.of(eurekaItem);
            return this;
        }

        public Builder setEurekaItem(ItemStack eurekaItem, float eurekaProgress) {
            this.eurekaProgress = eurekaProgress;
            this.eurekaItem = AEItemKey.of(eurekaItem);
            return this;
        }

        public Builder setEurekaFluid(Fluid eurekaFluid, float eurekaProgress) {
            this.eurekaProgress = eurekaProgress;
            this.eurekaItem = AEFluidKey.of(eurekaFluid);
            return this;
        }

        public ResearchRequirements build() {
            var r = new ResearchRequirements();
            r.cwuNeeded = this.cwuNeeded;
            r.materialNeeded = this.materialNeeded;
            r.eurekaItem = this.eurekaItem;
            r.eurekaProgress = this.eurekaProgress;
            return r;
        }
    }

    @RegisterLanguage(cn = "算力积累不足", en = "Insufficient CWU accumulation")
    public static final String FAIL_NO_CWU = "research.requirements.fail.no_cwu";
    @RegisterLanguage(cn = "研究资料不足", en = "Insufficient research materials")
    public static final String FAIL_NO_MATERIAL = "research.requirements.fail.no_material";
    public static final ActionResult FAILURE_NO_CWU = new ActionResult(false, Component.translatable(FAIL_NO_CWU));
    public static final ActionResult FAILURE_NO_MATERIAL = new ActionResult(false, Component.translatable(FAIL_NO_MATERIAL));

    public record EurekaRequirementEntry(AEKey key, List<TechNode> nodes) {}
}
