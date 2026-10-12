package com.gtocore.integration.emi.electrochemistry;

import com.gtocore.api.electrochemistry.IonDefinition;
import com.gtocore.api.electrochemistry.Ions;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import dev.emi.emi.api.stack.EmiStack;

import java.util.Collections;
import java.util.List;

public final class IonEmiStack extends EmiStack {

    private static final ResourceLocation STILL = GTCEu.id("block/material_sets/dull/liquid");
    private final IonDefinition ion;
    private String amountText = "1";
    private int amountWidth = -1;

    public IonEmiStack(IonDefinition ion) {
        this.ion = ion;
    }

    @Override
    public EmiStack copy() {
        return new IonEmiStack(ion).setAmount(amount).setChance(chance).comparison(comparison).setRemainder(getRemainder().copy());
    }

    @Override
    public IonEmiStack setAmount(long amount) {
        super.setAmount(amount);
        amountText = FormattingUtil.formatNumberReadable(amount);
        amountWidth = -1;
        return this;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
        var minecraft = Minecraft.getInstance();
        if ((flags & RENDER_ICON) != 0) {
            var sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(STILL);
            int color = ion.getColor();
            var red = (color >> 16 & 0xFF) / 255F;
            var green = (color >> 8 & 0xFF) / 255F;
            var blue = (color & 0xFF) / 255F;
            graphics.blit(x, y, 0, 16, 16, sprite, red, green, blue, 1);
        }
        if ((flags & RENDER_AMOUNT) != 0) {
            if (amountWidth < 0) amountWidth = minecraft.font.width(amountText);
            graphics.pose().pushPose();
            graphics.pose().scale(0.5F, 0.5F, 1);
            graphics.drawString(minecraft.font, amountText, (x + 16) * 2 - amountWidth, (y + 11) * 2, 0xFFFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IonEmiStack other && ion == other.ion && amount == other.amount;
    }

    @Override
    public int hashCode() {
        return ion.hashCode() ^ Long.hashCode(amount);
    }

    @Override
    public boolean isEmpty() {
        return amount <= 0;
    }

    @Override
    public CompoundTag getNbt() {
        return null;
    }

    @Override
    public Object getKey() {
        return ion;
    }

    @Override
    public ResourceLocation getId() {
        return Ions.REGISTRY_KEY.getKey(ion);
    }

    @Override
    public Component getName() {
        return Component.translatable(ion.translationKey()).withStyle(style -> style.withColor(ion.getColor()));
    }

    @Override
    public List<Component> getTooltipText() {
        return Collections.singletonList(Component.translatable("gtocore.recipe.half_reaction.generic_ion", getName(), amount, ion.getSymbol()));
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return Collections.singletonList(ClientTooltipComponent.create(getTooltipText().getFirst().getVisualOrderText()));
    }

    public static void registerIonEmiStacks(List<EmiStack> stacks) {
        Ions.REGISTRY_KEY.forEach(ion -> stacks.add(new IonEmiStack(ion)));
    }
}
