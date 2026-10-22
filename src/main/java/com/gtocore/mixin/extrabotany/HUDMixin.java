package com.gtocore.mixin.extrabotany;

import net.minecraft.client.gui.GuiGraphics;

import io.github.lounode.extrabotany.client.gui.HUD;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HUD.class, remap = false)
public class HUDMixin {

    @Inject(method = "renderManaBar", at = @At("HEAD"), cancellable = true)
    private void gtocore$useBotaniaManaBar(GuiGraphics gui, float partialTicks, CallbackInfo ci) {
        ci.cancel();
    }
}
