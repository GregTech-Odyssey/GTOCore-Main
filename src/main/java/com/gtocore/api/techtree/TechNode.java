package com.gtocore.api.techtree;

import com.gtocore.api.research.TeamResearchContext;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import appeng.api.stacks.AEKey;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@DataGeneratorScanned
public final class TechNode {

    @RegisterLanguage(cn = "前置：%s 未解锁", en = "Prerequisites: %s not unlocked")
    private static final String UNLOCKED = "gtocore.tech_node.unlocked";
    @RegisterLanguage(cn = "[配方奖励]", en = "[Recipe Reward]")
    public static final String RECIPE_REWARD_LABEL = "gtocore.research.side_tab.recipe_reward";
    @RegisterLanguage(cn = "[其他奖励]", en = "[Other Reward]")
    public static final String OTHER_REWARD_LABEL = "gtocore.research.side_tab.other_reward";
    @RegisterLanguage(cn = "可解锁：", en = "Unlockable:")
    public static final String UNLOCKABLE_LABEL = "gtocore.research.side_tab.unlockable";

    @Getter
    final TechTreeManager manager;
    public final String name;
    @Nullable
    public final AEKey icon;
    public final List<TechNode> prerequisites;
    @Getter
    private final IRequirement requirements;
    @Getter
    private final int tier;
    @Getter
    private final Set<GTRecipeDefinition> recipes = new ReferenceOpenHashSet<>();
    @Getter
    private final List<Component> rewardLines = new ArrayList<>();

    TechNode(TechTreeManager manager, String name, @Nullable AEKey icon, IRequirement requirements, List<TechNode> prerequisites, int tier) {
        this.manager = manager;
        this.name = name;
        this.icon = icon;
        this.requirements = requirements;
        this.prerequisites = prerequisites.isEmpty() ? Collections.emptyList() : prerequisites;
        this.tier = tier;
    }

    ActionResult tryUnlock(Set<TechNode> unlockedNodes, TeamResearchContext context, UUID team, boolean simulate) {
        for (var prereq : prerequisites) {
            if (!unlockedNodes.contains(prereq)) return ActionResult.fail(Component.translatable(UNLOCKED, TechTreeManager.getNodeName(prereq)));
        }
        return requirements.test(this, context, team, simulate);
    }

    public MutableComponent desc() {
        return TechTreeManager.getNodeDesc(this);
    }

    public MutableComponent getDisplayName() {
        return TechTreeManager.getNodeName(this);
    }

    @FunctionalInterface
    public interface IRequirement {

        ActionResult test(TechNode node, TeamResearchContext context, UUID team, boolean simulate);
    }

    public void addRecipeToNode(GTRecipeDefinition recipe) {
        recipes.add(recipe);
        rewardLines.add(
                Component.translatable(RECIPE_REWARD_LABEL).withStyle(net.minecraft.ChatFormatting.DARK_PURPLE)
                        .append(getMainOutputText(recipe).withStyle(net.minecraft.ChatFormatting.GRAY)));
    }

    private static MutableComponent getMainOutputText(GTRecipeDefinition recipe) {
        var outputs0 = recipe.itemOutputs;
        if (!outputs0.isEmpty()) {
            return outputs0.getFirst().getName();
        }
        var outputs1 = recipe.fluidOutputs;
        if (!outputs1.isEmpty()) {
            return outputs1.getFirst().getName();
        }
        return Component.empty();
    }

    public List<Component> getRewardLinesWithHeader() {
        if (rewardLines.isEmpty()) {
            return Collections.emptyList();
        }
        var firstLine = Component.translatable(UNLOCKABLE_LABEL).withStyle(net.minecraft.ChatFormatting.GRAY);
        List<Component> lines = new ArrayList<>();
        lines.add(firstLine);
        lines.addAll(rewardLines);
        return lines;
    }
}
