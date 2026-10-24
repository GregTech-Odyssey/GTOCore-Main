package com.gtocore.mixin.emi;

import com.gtocore.integration.emi.EmiTagPreference;

import net.minecraft.world.item.crafting.Ingredient;

import appeng.integration.modules.jeirei.EncodingHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = EncodingHelper.class, remap = false)
public class AEEncodingHelperMixin {

    @ModifyVariable(method = "encodeCraftingRecipe", at = @At("STORE"), ordinal = 0)
    private static Ingredient gtocore$preferTagDefault(Ingredient ingredient) {
        return EmiTagPreference.narrow(ingredient);
    }
}
