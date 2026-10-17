package com.gtocore.mixin.mousetweaks;

import com.gtocore.common.machine.noenergy.VirtualIngredientProviderMachine;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yalter.mousetweaks.handlers.GuiContainerHandler;

@Mixin(value = GuiContainerHandler.class, remap = false)
public class GuiContainerHandlerMixin {

    @Shadow
    @Final
    private AbstractContainerScreen<?> screen;

    @Inject(method = "isWheelTweakDisabled", at = @At("HEAD"), cancellable = true)
    private void gtocore$disableWheelTweak(CallbackInfoReturnable<Boolean> cir) {
        if (screen instanceof ModularUIGuiContainer container && container.modularUI.holder instanceof VirtualIngredientProviderMachine) {
            cir.setReturnValue(true);
        }
    }
}
