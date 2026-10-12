package com.gtocore.api.gui.widget;

import com.gtocore.api.electrochemistry.IonAmount;
import com.gtocore.integration.emi.electrochemistry.IonEmiStack;
import com.gtocore.utils.GuiHelper;

import com.gregtechceu.gtceu.api.recipe.handler.IO;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.ingredient.IIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.screen.EmiScreenManager;

import java.util.Collections;
import java.util.List;

/** Ion ingredient slot with the same liquid sprite and frame as material fluid slots. */
public final class IonSlot extends Widget implements IIngredientSlot {

    private final IonEmiStack stack;
    private final IO io;

    public IonSlot(IonAmount ion, IO io) {
        super(0, 0, UISizes.SLOT_SIZE, UISizes.SLOT_SIZE);
        this.io = io;
        setBackground(UITheme.FLUID_SLOT);
        stack = new IonEmiStack(ion.ion()).setAmount(ion.amount());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        return isMouseOverElement(mouseX, mouseY) ? stack : null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawTooltipTexts(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null && gui.getModularUIGui() != null) {
            List<Component> key = switch (io) {
                case IN -> Collections.singletonList(Component.translatable("gtocore.recipe.half_reaction.required_ion", stack.getTooltipText().getFirst()));
                case OUT -> Collections.singletonList(Component.translatable("gtocore.recipe.half_reaction.ion", stack.getTooltipText().getFirst()));
                default -> stack.getTooltipText();
            };
            gui.getModularUIGui().setHoverTooltip(key, ItemStack.EMPTY, null, null);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) &&
                EmiScreenManager.stackInteraction(new EmiStackInteraction(stack, null, true), bind -> bind.matchesMouse(button))) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isMouseOverElement(GuiHelper.getRealMouseX(), GuiHelper.getRealMouseY()) &&
                EmiScreenManager.stackInteraction(new EmiStackInteraction(stack, null, true), bind -> bind.matchesKey(keyCode, scanCode))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        var pos = getPosition();
        stack.render(graphics, pos.x + 1, pos.y + 1, partialTicks, EmiStack.RENDER_ICON | EmiStack.RENDER_AMOUNT);
        if (isMouseOverElement(mouseX, mouseY)) graphics.fill(pos.x + 1, pos.y + 1, pos.x + 17, pos.y + 17, 0x40FFFFFF);
    }
}
