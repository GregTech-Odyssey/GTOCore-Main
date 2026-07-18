package com.gtocore.integration.emi.research;

import com.gtocore.api.research.techtree.TechTreeManager;

import com.gtolib.GTOCore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;

public final class TechNodeEmiStackSerializer implements EmiStackSerializer<TechNodeEmiStack> {

    private static final String TYPE = "tech_node";
    private static final String PATH_PREFIX = "tech_node/";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public EmiStack create(ResourceLocation id, CompoundTag nbt, long amount) {
        if (!GTOCore.MOD_ID.equals(id.getNamespace()) || !id.getPath().startsWith(PATH_PREFIX)) {
            return EmiStack.EMPTY;
        }

        String[] parts = id.getPath().substring(PATH_PREFIX.length()).split("/", 2);
        if (parts.length != 2) {
            return EmiStack.EMPTY;
        }

        var manager = TechTreeManager.getManager(parts[0]);
        if (manager == null) {
            return EmiStack.EMPTY;
        }

        var node = manager.getNode(parts[1]);
        return node == null ? EmiStack.EMPTY : new TechNodeEmiStack(node).setAmount(amount);
    }
}
