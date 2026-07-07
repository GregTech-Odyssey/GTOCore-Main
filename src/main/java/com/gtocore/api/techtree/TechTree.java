package com.gtocore.api.techtree;

import com.gregtechceu.gtceu.api.recipe.handler.ActionResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

public class TechTree<T> implements ITagSerializable<CompoundTag> {

    @Getter
    private final TechTreeManager<T> manager;
    final Set<TechNode<T>> nodes;

    public TechTree(TechTreeManager<T> manager) {
        this.manager = manager;
        this.nodes = new ReferenceOpenHashSet<>();
    }

    public ActionResult unlock(TechNode<T> definition, T args) {
        var result = definition.tryUnlock(nodes, args);
        if (result.isSuccess()) addUnlockedNode(definition);
        return result;
    }

    public ActionResult tryUnlock(TechNode<T> definition, T args) {
        return definition.tryUnlock(nodes, args);
    }

    public Set<TechNode<T>> getEndNodes() {
        var result = new ReferenceOpenHashSet<TechNode<T>>();
        var prerequisites = new ReferenceOpenHashSet<TechNode<T>>();
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

    public boolean isUnlocked(TechNode<T> node) {
        return nodes.contains(node);
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public Set<TechNode<T>> getUnlockedNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public void clear() {
        nodes.clear();
    }

    void addUnlockedNode(TechNode<T> node) {
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
