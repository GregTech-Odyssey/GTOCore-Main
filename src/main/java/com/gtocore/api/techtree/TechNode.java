package com.gtocore.api.techtree;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;

import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import appeng.api.stacks.AEKey;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@DataGeneratorScanned
public final class TechNode<T> {

    @RegisterLanguage(cn = "前置：%s 未解锁", en = "Prerequisites: %s not unlocked")
    private static final String UNLOCKED = "gtocore.tech_node.unlocked";

    @Getter
    final TechTreeManager<T> manager;
    public final String name;
    @Nullable
    public final AEKey icon;
    public final List<TechNode<T>> prerequisites;
    @Getter
    private final IRequirement<T> requirements;
    @Getter
    private final int tier;

    TechNode(TechTreeManager<T> manager, String name, @Nullable AEKey icon, IRequirement<T> requirements, List<TechNode<T>> prerequisites, int tier) {
        this.manager = manager;
        this.name = name;
        this.icon = icon;
        this.requirements = requirements;
        this.prerequisites = prerequisites.isEmpty() ? Collections.emptyList() : prerequisites;
        this.tier = tier;
    }

    ActionResult tryUnlock(Set<TechNode<T>> unlockedNodes, T args, UUID team, boolean simulate) {
        for (var prereq : prerequisites) {
            if (!unlockedNodes.contains(prereq)) return ActionResult.fail(Component.translatable(UNLOCKED, TechTreeManager.getNodeName(prereq)));
        }
        return requirements.test(this, args, team, simulate);
    }

    public MutableComponent desc() {
        return TechTreeManager.getNodeDesc(this);
    }

    public MutableComponent getDisplayName() {
        return TechTreeManager.getNodeName(this);
    }

    @FunctionalInterface
    public interface IRequirement<T> {

        ActionResult test(TechNode<T> node, T args, UUID team, boolean simulate);
    }
}
