package com.gtocore.integration.emi.research;

import com.gtocore.api.research.ResearchTag;

import com.gtolib.GTOCore;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

public class ResearchTagEmiStack extends EmiStack {

    public final ResearchTag tag;

    public ResearchTagEmiStack(ResearchTag tag) {
        super();
        this.tag = tag;
    }

    @Override
    public EmiStack copy() {
        return new ResearchTagEmiStack(tag);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int i1, float v, int i2) {}

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public CompoundTag getNbt() {
        return null;
    }

    @Override
    public Object getKey() {
        return tag;
    }

    @Override
    public ResourceLocation getId() {
        return GTOCore.id("research_tag/" + tag.getName());
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of();
    }

    @Override
    public Component getName() {
        return tag.getDisplayName();
    }
}
