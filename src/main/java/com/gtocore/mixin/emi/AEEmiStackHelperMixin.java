package com.gtocore.mixin.emi;

import com.gtocore.integration.emi.EmiTagPreference;

import appeng.api.stacks.GenericStack;
import appeng.integration.modules.emi.EmiStackHelper;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = EmiStackHelper.class, remap = false)
public class AEEmiStackHelperMixin {

    @Inject(method = "ofInputs", at = @At("HEAD"), cancellable = true)
    private static void gtocore$preferTagDefault(EmiRecipe emiRecipe, CallbackInfoReturnable<List<List<GenericStack>>> cir) {
        cir.setReturnValue(emiRecipe.getInputs().stream().map(ingredient -> {
            var preferred = EmiTagPreference.get(ingredient);
            return EmiStackHelper.toGenericStack(preferred == null ? ingredient : (EmiIngredient) preferred.copy().setAmount(ingredient.getAmount()));
        }).toList());
    }
}
