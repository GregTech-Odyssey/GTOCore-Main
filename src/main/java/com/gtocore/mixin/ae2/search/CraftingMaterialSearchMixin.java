package com.gtocore.mixin.ae2.search;

import com.gtocore.integration.jech.PinYinUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "appeng.client.gui.me.crafting.CraftingMaterialSearch", remap = false)
public class CraftingMaterialSearchMixin {

    @Redirect(method = "filter",
              at = @At(
                       value = "INVOKE",
                       target = "Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z",
                       remap = false),
              remap = false)
    private boolean gto$matchMaterialName(String candidate, CharSequence search) {
        return PinYinUtils.match(candidate, search);
    }
}
