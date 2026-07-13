package com.gtocore.api.techtree;

import com.gtocore.api.research.TeamResearchContext;

import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class TechTree implements ITagSerializable<CompoundTag> {

    @Getter
    private final TechTreeManager manager;
    final Set<TechNode> nodes;

    public TechTree(TechTreeManager manager) {
        this.manager = manager;
        this.nodes = new ReferenceOpenHashSet<>();
    }

    public ActionResult unlock(TechNode definition, TeamResearchContext context, UUID team) {
        var result = definition.tryUnlock(nodes, context, team, true);
        if (result.isSuccess()) {
            definition.tryUnlock(nodes, context, team, false);
            addUnlockedNode(definition);
        }
        return result;
    }

    public ActionResult tryUnlock(TechNode definition, TeamResearchContext context, UUID team) {
        return definition.tryUnlock(nodes, context, team, true);
    }

    public Set<TechNode> getEndNodes() {
        var result = new ReferenceOpenHashSet<TechNode>();
        var prerequisites = new ReferenceOpenHashSet<TechNode>();
        for (var node : nodes) {
            for (var prerequisite : node.prerequisites) {
                if (nodes.contains(prerequisite)) {
                    prerequisites.add(prerequisite);
                }
            }
        }
        for (var node : nodes) {
            if (!prerequisites.contains(node)) {
                result.add(node);
            }
        }
        return result;
    }

    public boolean isUnlocked(TechNode node) {
        return nodes.contains(node);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public Set<TechNode> getUnlockedNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public void clear() {
        nodes.clear();
    }

    void addUnlockedNode(TechNode node) {
        if (!nodes.add(node)) return;
        for (var prerequisite : node.prerequisites) {
            addUnlockedNode(prerequisite);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var list = new ListTag();
        for (var node : getEndNodes()) {
            list.add(StringTag.valueOf(node.name));
        }
        tag.put("nodes", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        nodes.clear();
        var list = nbt.getList("nodes", StringTag.TAG_STRING);
        for (var name : list) {
            var d = manager.definitions.get(name.getAsString());
            if (d != null) {
                addUnlockedNode(d);
            }
        }
    }
}
