package com.gtocore.mixin.emi;

import com.gtocore.integration.emi.multipage.MultiblockInfoEmiRecipe;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiFavorite.class, remap = false)
public class EmiFavoriteMixin {

    @Shadow
    @Final
    protected EmiRecipe recipe;

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void gtocore$hideMultiblockPreview(CallbackInfoReturnable<List<ClientTooltipComponent>> cir) {
        if (recipe instanceof MultiblockInfoEmiRecipe) cir.getReturnValue().removeIf(component -> component instanceof RecipeTooltipComponent);
    }
}
