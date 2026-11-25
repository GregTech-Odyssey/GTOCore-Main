package com.gtocore.mixin.ae2.wtlib;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.client.gui.Icon;

import com.mojang.blaze3d.systems.RenderSystem;
import de.mari_023.ae2wtlib.wut.CycleTerminalButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CycleTerminalButton.class)
public abstract class CycleTerminalButtonMixin extends Button {

    @Unique
    private static final ResourceLocation GTO_WUT_ICON = new ResourceLocation("ae2wtlib", "textures/item/wireless_universal_terminal.png");

    protected CycleTerminalButtonMixin(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void gto$renderModernToolbarButton(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial, CallbackInfo ci) {
        ci.cancel();
        if (!this.visible) return;
        int yOffset = isHovered() ? 1 : 0;
        Icon bgIcon = isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER : isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND;
        RenderSystem.enableBlend();
        bgIcon.getBlitter().dest(getX() - 1, getY() + yOffset, 18, 20).zOffset(2).blit(guiGraphics);
        if (!this.active) {
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 3.0F);
        guiGraphics.blit(GTO_WUT_ICON, getX() + 1, getY() + 2 + yOffset, 14, 14, 0.0F, 0.0F, 512, 512, 512, 512);
        guiGraphics.pose().popPose();
        if (!this.active) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
