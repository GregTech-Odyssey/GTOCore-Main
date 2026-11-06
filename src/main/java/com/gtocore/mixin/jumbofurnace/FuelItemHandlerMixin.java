package com.gtocore.mixin.jumbofurnace;

import com.gtocore.utils.JumboFuelCache;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

import commoble.jumbofurnace.jumbo_furnace.FuelItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Routes the jumbo furnace fuel-slot check through {@link JumboFuelCache}: item pipes call
 * isItemValid on every insert attempt and the burn-time lookup is dominated by third-party fuel
 * events, not by the item's own value.
 */
@Mixin(value = FuelItemHandler.class, remap = false)
public abstract class FuelItemHandlerMixin {

    @Redirect(
              method = "isItemValid",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraftforge/common/ForgeHooks;getBurnTime(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/crafting/RecipeType;)I",
                       remap = false),
              remap = false)
    private int gtocore$cachedBurnTime(ItemStack stack, RecipeType<?> recipeType) {
        return JumboFuelCache.get(stack, recipeType);
    }
}
