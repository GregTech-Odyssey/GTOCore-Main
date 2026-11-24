package com.gtocore.mixin.mousetweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.Slot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yalter.mousetweaks.Main;

import java.util.Collections;
import java.util.List;

@Mixin(value = Main.class, remap = false)
public abstract class MainMixin {

    @ModifyExpressionValue(method = "findPullSlot", at = @At(value = "INVOKE", target = "Lyalter/mousetweaks/Main;areStacksCompatible(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean gtocore$checkPullPermissions(boolean matches, @Local(argsOnly = true) Slot destination, @Local(ordinal = 1) Slot source) {
        if (!matches || !(Minecraft.getInstance().screen instanceof ModularUIGuiContainer screen)) return matches;
        return source.mayPickup(screen.modularUI.entityPlayer) &&
                destination.mayPlace(source.getItem()) && destination.mayPickup(screen.modularUI.entityPlayer);
    }

    @Inject(method = "findPushSlots", at = @At("HEAD"), cancellable = true)
    private static void gtocore$checkPushSource(List<Slot> slots, Slot source, int amount, boolean requireAll, CallbackInfoReturnable<List<Slot>> cir) {
        if (Minecraft.getInstance().screen instanceof ModularUIGuiContainer screen && !source.mayPickup(screen.modularUI.entityPlayer)) {
            cir.setReturnValue(requireAll ? null : Collections.emptyList());
        }
    }

    @ModifyExpressionValue(method = "findPushSlots", at = @At(value = "INVOKE", target = "Lyalter/mousetweaks/Main;areStacksCompatible(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean gtocore$checkOccupiedPushDestination(boolean matches, @Local(argsOnly = true) Slot source, @Local(ordinal = 1) Slot destination) {
        if (!matches || !(Minecraft.getInstance().screen instanceof ModularUIGuiContainer screen)) return matches;
        return destination.mayPlace(source.getItem()) && destination.mayPickup(screen.modularUI.entityPlayer);
    }
}
