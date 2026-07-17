package com.gtocore.integration.emi.research;

import com.gtocore.api.research.techtree.TechNode;

import com.gtolib.GTOCore;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.stack.EmiStack;

import java.util.List;

public class TechNodeEmiStack extends EmiStack {

    public final TechNode data;

    public TechNodeEmiStack(TechNode data) {
        super();
        this.data = data;
    }

    @Override
    public EmiStack copy() {
        return new TechNodeEmiStack(data);
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
        return data;
    }

    @Override
    public ResourceLocation getId() {
        return GTOCore.id("tech_node/" + data.name);
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of();
    }

    @Override
    public Component getName() {
        return data.getDisplayName();
    }
}
