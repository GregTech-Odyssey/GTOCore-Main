package com.gtocore.mixin.ae2.wtlib;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.client.gui.Icon;

import com.mojang.blaze3d.systems.RenderSystem;
import de.mari_023.ae2wtlib.terminal.ItemButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemButton.class)
public abstract class ItemButtonMixin extends Button {

    @Shadow(remap = false)
    @Final
    private ResourceLocation texture;

    protected ItemButtonMixin(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
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
        guiGraphics.blit(this.texture, getX(), getY() + 1 + yOffset, 16, 16, 0.0F, 0.0F, 16, 16, 16, 16);
        guiGraphics.pose().popPose();
        if (!this.active) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
