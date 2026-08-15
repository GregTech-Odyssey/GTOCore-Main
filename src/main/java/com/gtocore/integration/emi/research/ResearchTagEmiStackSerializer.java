package com.gtocore.integration.emi.research;

import com.gtocore.api.research.ResearchTag;

import com.gtolib.GTOCore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;

public final class ResearchTagEmiStackSerializer implements EmiStackSerializer<ResearchTagEmiStack> {

    private static final String TYPE = "research_tag";
    private static final String PATH_PREFIX = "research_tag/";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public EmiStack create(ResourceLocation id, CompoundTag nbt, long amount) {
        if (!GTOCore.MOD_ID.equals(id.getNamespace()) || !id.getPath().startsWith(PATH_PREFIX)) {
            return EmiStack.EMPTY;
        }

        var tag = ResearchTag.TAGS.get(id.getPath().substring(PATH_PREFIX.length()));
        return tag == null ? EmiStack.EMPTY : new ResearchTagEmiStack(tag).setAmount(amount);
    }
}
