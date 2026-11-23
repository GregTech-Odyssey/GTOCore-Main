package com.gtocore.api.research;

import com.gtocore.api.research.techtree.TechNode;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

import appeng.api.identity.IdentityKeySet;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

    private static final Reference2ObjectOpenCustomHashMap<AEKey, IdentityKeySet<TechNode>> KEY_EUREKA_REQUIREMENTS = new Reference2ObjectOpenCustomHashMap<>(AE_KEY_STRATEGY);
    private static final Reference2ObjectOpenHashMap<Material, IdentityKeySet<TechNode>> MATERIAL_EUREKA_REQUIREMENTS = new Reference2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Fluid, Material> EUREKA_FLUID_MATERIALS = new Reference2ObjectOpenHashMap<>();

    private long cwuNeeded;
    private ResearchPoints materialNeeded;
    @Nullable
    private Eureka eureka;
    private float eurekaProgress;

    private ResearchRequirements() {}

    public static synchronized void registerEurekaRequirement(Eureka eureka, TechNode node) {
        var material = eureka.material();
        if (material != null) {
            MATERIAL_EUREKA_REQUIREMENTS.computeIfAbsent(material, ResearchRequirements::indexEurekaMaterial).add(node);
        } else {
            KEY_EUREKA_REQUIREMENTS.computeIfAbsent(eureka.key(), ignored -> new IdentityKeySet<>()).add(node);
        }
    }

    private static IdentityKeySet<TechNode> indexEurekaMaterial(Material material) {
        // AE can create fluid keys before GTM can resolve their material from fluid tags.
        var fluids = material.getProperty(PropertyKey.FLUID);
        if (fluids != null) {
            for (var storageKey : FluidStorageKey.allKeys()) {
                var fluid = fluids.get(storageKey);
                if (fluid != null) EUREKA_FLUID_MATERIALS.putIfAbsent(fluid, material);
            }
        }
        return new IdentityKeySet<>();
    }

    @Nullable
    public static Material getEurekaFluidMaterial(Fluid fluid) {
        return EUREKA_FLUID_MATERIALS.get(fluid);
    }

    public static ReferenceSet<TechNode> getKeyEurekaRequirements(AEKey key) {
        var nodes = KEY_EUREKA_REQUIREMENTS.get(key);
        return nodes == null ? ReferenceSets.emptySet() : nodes;
    }

    public static ReferenceSet<TechNode> getMaterialEurekaRequirements(@Nullable Material material) {
        var nodes = MATERIAL_EUREKA_REQUIREMENTS.get(material);
        return nodes == null ? ReferenceSets.emptySet() : nodes;
    }

    public static List<EurekaRequirementEntry> getKeyEurekaRequirementEntries() {
        List<EurekaRequirementEntry> entries = new ArrayList<>(KEY_EUREKA_REQUIREMENTS.size());
        for (var it = KEY_EUREKA_REQUIREMENTS.reference2ObjectEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            List<TechNode> nodes = entry.getValue().stream()
                    .sorted(Comparator.comparing((TechNode node) -> node.getManager().getId()).thenComparing(node -> node.name))
                    .toList();
            entries.add(new EurekaRequirementEntry(entry.getKey(), nodes));
        }
        entries.sort(Comparator.comparing((EurekaRequirementEntry entry) -> entry.key().getType().getId().toString())
                .thenComparing(entry -> entry.key().getId().toString()));
        return entries;
    }

    public boolean hasEnoughCWU(TechNode node, TeamResearchContext context) {
        long required = context.hasScanned(eureka) ? (long) (cwuNeeded * (1 - eurekaProgress)) : cwuNeeded;
        return context.techNodeAccCWU().getLong(node) >= required;
    }

    public ActionResult test(TechNode node, TeamResearchContext teamResource, UUID team, boolean simulate) {
        if (this == NO_REQUIREMENTS) {
            return ActionResult.SUCCESS;
        }
        if (!hasEnoughCWU(node, teamResource)) {
            return FAILURE_NO_CWU;
        }
        for (var it = materialNeeded.reference2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            var tag = entry.getKey();
            var amount = entry.getLongValue();
            if (teamResource.researchPoints().getOrDefault(tag, 0L) < amount) {
                return FAILURE_NO_MATERIAL;
            }
        }
        return ActionResult.SUCCESS;
    }

    public static class Builder {

        private long cwuNeeded;
        private final ResearchPoints materialNeeded = new ResearchPoints();
        private AEKey eurekaKey;
        private Material eurekaMaterial;
        private float eurekaProgress;

        public Builder setCWUNeeded(long cwuNeeded) {
            this.cwuNeeded = cwuNeeded;
            return this;
        }

        public Builder addMaterialNeeded(ResearchTag tag, long amount) {
            materialNeeded.put(tag, amount);
            return this;
        }

        public Builder setEureka(ItemLike eurekaItem, float eurekaProgress) {
            this.eurekaProgress = eurekaProgress;
            this.eurekaKey = AEItemKey.of(eurekaItem);
            return this;
        }

        public Builder setEureka(Material mat, float eurekaProgress) {
            this.eurekaProgress = eurekaProgress;
            this.eurekaMaterial = mat;
            return this;
        }

        public Builder setEureka(ItemStack eurekaItem, float eurekaProgress) {
            this.eurekaProgress = eurekaProgress;
            this.eurekaKey = AEItemKey.of(eurekaItem);
            return this;
        }

        public Builder setEureka(Fluid eurekaFluid, float eurekaProgress) {
            this.eurekaProgress = eurekaProgress;
            this.eurekaKey = AEFluidKey.of(eurekaFluid);
            return this;
        }

        public ResearchRequirements build() {
            var r = new ResearchRequirements();
            r.cwuNeeded = this.cwuNeeded;
            r.materialNeeded = this.materialNeeded;
            if (this.eurekaKey != null && this.eurekaKey.getPrimaryKey() == Blocks.BARRIER.asItem()) {
                throw new IllegalArgumentException("Eureka requires a registered item, fluid or material.");
            }
            if (this.eurekaKey != null || this.eurekaMaterial != null) r.eureka = new Eureka(this.eurekaKey, this.eurekaMaterial);
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

    public record Eureka(@Nullable AEKey key, @Nullable Material material) {

        public Component getDisplayName() {
            return material == null ? key.getDisplayName() : material.getLocalizedName();
        }
    }
}
