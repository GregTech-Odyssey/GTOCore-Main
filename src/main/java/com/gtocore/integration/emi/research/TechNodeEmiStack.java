package com.gtocore.integration.emi.research;

import com.gtocore.api.research.techtree.TechNode;
import com.gtocore.api.research.techtree.TechTreeManager;

import com.gtolib.GTOCore;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.api.client.AEKeyRendering;

import dev.emi.emi.api.stack.EmiStack;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.gtocore.integration.emi.research.ResearchEmiNameHelper.TECH_NODE_NAME;

public class TechNodeEmiStack extends EmiStack {

    public final TechNode data;

    public TechNodeEmiStack(TechNode data) {
        super();
        this.data = data;
    }

    @Override
    public boolean isEqual(EmiStack stack) {
        return stack instanceof TechNodeEmiStack other && other.data == this.data;
    }

    @Override
    public EmiStack copy() {
        return new TechNodeEmiStack(data);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
        if ((flags & RENDER_ICON) == 0) {
            return;
        }
        if (data.icon != null) {
            AEKeyRendering.drawInGui(Minecraft.getInstance(), graphics, x, y, data.icon);
            return;
        }
        graphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF2F2F34);
        graphics.renderOutline(x + 1, y + 1, 14, 14, 0xFF8BE7DE);
        graphics.drawString(Minecraft.getInstance().font, "?", x + 5, y + 4, 0xFFFFFFFF, false);
    }

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
        return GTOCore.id("tech_node/" + data.getManager().getId() + "/" + data.name);
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(getName());
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return Stream.concat(Stream.of(getName()), data.getRewardLinesWithHeader().stream())
                .map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
    }

    @Override
    public Component getName() {
        return Component.translatable(TECH_NODE_NAME, data.getDisplayName().withStyle(style -> style.withColor(ChatFormatting.AQUA)));
    }

    public static void registerTechNodeEmiStack(Set<EmiStack> c) {
        TechTreeManager.getManagers().forEach(manager -> manager.getAllNodes().forEachRemaining(node -> c.add(new TechNodeEmiStack(node))));
    }
}
