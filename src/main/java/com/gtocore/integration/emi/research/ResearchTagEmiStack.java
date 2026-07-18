package com.gtocore.integration.emi.research;

import com.gtocore.api.research.ResearchTag;

import com.gtolib.GTOCore;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.gtocore.integration.emi.research.ResearchEmiNameHelper.DOMAIN_DATA_NAME;

public class ResearchTagEmiStack extends EmiStack {

    public final ResearchTag tag;

    public ResearchTagEmiStack(ResearchTag tag) {
        super();
        this.tag = tag;
    }

    @Override
    public boolean isEqual(EmiStack stack) {
        return stack instanceof ResearchTagEmiStack other && other.tag == this.tag;
    }

    @Override
    public EmiStack copy() {
        return new ResearchTagEmiStack(tag).setAmount(amount).setChance(chance);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
        if ((flags & RENDER_ICON) != 0) {
            graphics.fill(x + 1, y + 1, x + 15, y + 15, tag.getColor());
            graphics.renderOutline(x + 1, y + 1, 14, 14, 0xFFFFFFFF);
            String initial = tag.getName().substring(0, 1).toUpperCase(Locale.ROOT);
            int red = tag.getColor() >> 16 & 0xFF;
            int green = tag.getColor() >> 8 & 0xFF;
            int blue = tag.getColor() & 0xFF;
            int textColor = red * 299 + green * 587 + blue * 114 > 128000 ? 0xFF202020 : 0xFFFFFFFF;
            var font = Minecraft.getInstance().font;
            graphics.drawString(font, initial, x + (16 - font.width(initial)) / 2, y + 4, textColor, false);
        }
        if ((flags & RENDER_AMOUNT) != 0 && amount != 1L) {
            EmiRenderHelper.renderAmount(EmiDrawContext.wrap(graphics), x, y, Component.literal(Long.toString(amount)));
        }
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
        return tag;
    }

    @Override
    public ResourceLocation getId() {
        return GTOCore.id("research_tag/" + tag.getName());
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(Component.translatable(DOMAIN_DATA_NAME, getName()).append(Component.literal("x" + FormattingUtil.formatNumbers(amount))));
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return getTooltipText().stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
    }

    @Override
    public Component getName() {
        return tag.getDisplayName().withStyle(style -> style.withColor(tag.getColor()));
    }

    public static void registerResearchTagEmiStack(Set<EmiStack> c) {
        for (var it : ResearchTag.TAGS.values()) {
            c.add(new ResearchTagEmiStack(it));
        }
    }
}
