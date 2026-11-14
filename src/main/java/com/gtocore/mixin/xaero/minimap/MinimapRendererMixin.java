package com.gtocore.mixin.xaero.minimap;

import com.gtocore.integration.xaero.XaeroMinimapHud;

import net.minecraft.client.gui.GuiGraphics;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.hud.minimap.module.MinimapRenderer;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.render.module.ModuleRenderContext;

@Mixin(value = MinimapRenderer.class, remap = false)
public abstract class MinimapRendererMixin {

    @Inject(method = "render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At("HEAD"), cancellable = true)
    private void gtocore$place(MinimapSession session, ModuleRenderContext context, GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        if (!XaeroMinimapHud.begin(session, context, graphics)) ci.cancel();
    }

    @Inject(method = "render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At("RETURN"))
    private void gtocore$finish(MinimapSession session, ModuleRenderContext context, GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        XaeroMinimapHud.end(graphics);
    }

    @ModifyExpressionValue(method = "render(Lxaero/hud/minimap/module/MinimapSession;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "INVOKE", target = "Lxaero/hud/minimap/module/MinimapSession;getHideMinimapUnderScreen()Z"))
    private boolean gtocore$hudInteraction(boolean hide) {
        return XaeroMinimapHud.hideUnderScreen(hide);
    }
}
