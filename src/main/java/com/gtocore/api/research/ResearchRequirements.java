package com.gtocore.api.research;

import com.gtocore.api.techtree.TechNode;

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

import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@DataGeneratorScanned
public final class ResearchRequirements implements TechNode.IRequirement<TeamResearchContext> {

    private long cwuNeeded;
    private Reference2LongOpenHashMap<ResearchTag> materialNeeded;
    private AEKey eurekaItem;
    private float eurekaProgress;

    private ResearchRequirements() {}

    @Override
    public ActionResult test(TechNode<TeamResearchContext> node, TeamResearchContext teamResource, UUID team, boolean simulate) {
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
        if (!simulate) {
            for (Map.Entry<ResearchTag, Long> entry : materialNeeded.reference2LongEntrySet()) {
                var tag = entry.getKey();
                var amount = entry.getValue();
                teamResource.getResearchPoints().merge(tag, -amount, Long::sum);
            }
        }
        return ActionResult.SUCCESS;
    }

    public static class Builder {

        private long cwuNeeded;
        private final Reference2LongOpenHashMap<ResearchTag> materialNeeded = new Reference2LongOpenHashMap<>();
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
}
