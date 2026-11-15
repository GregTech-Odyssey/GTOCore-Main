package com.gtocore.mixin.xaero.minimap;

import com.gtocore.integration.xaero.XaeroMinimapHud;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xaero.common.minimap.MinimapProcessor;

@Mixin(value = MinimapProcessor.class, remap = false)
public abstract class MinimapProcessorMixin {

    @ModifyExpressionValue(method = "onRender", at = @At(value = "FIELD", target = "Lxaero/common/minimap/MinimapProcessor;enlargedMap:Z", ordinal = 0))
    private boolean gtocore$keepHudPosition(boolean enlarged) {
        return enlarged && !XaeroMinimapHud.rendering();
    }
}
