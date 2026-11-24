package com.gtocore.mixin.mousetweaks;

import com.gtocore.common.machine.noenergy.VirtualIngredientProviderMachine;

import com.gtolib.client.gui.LDLibSlotInteractions;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

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

    @Inject(method = "getSlotUnderMouse", at = @At("HEAD"), cancellable = true)
    private void gtocore$findWidgetSlot(double mouseX, double mouseY, CallbackInfoReturnable<Slot> cir) {
        if (screen instanceof ModularUIGuiContainer container) {
            cir.setReturnValue(LDLibSlotInteractions.findSlot(container, mouseX, mouseY));
        }
    }

    @Inject(method = "isIgnored", at = @At("HEAD"), cancellable = true)
    private void gtocore$ignoreUnavailableWidgetSlots(Slot slot, CallbackInfoReturnable<Boolean> cir) {
        if (screen instanceof ModularUIGuiContainer container && !LDLibSlotInteractions.isTransferSlot(container.getMenu(), slot)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isCraftingOutput", at = @At("RETURN"), cancellable = true)
    private void gtocore$handleTakeOnlySlots(Slot slot, CallbackInfoReturnable<Boolean> cir) {
        if (screen instanceof ModularUIGuiContainer container && !cir.getReturnValueZ()) {
            var stack = slot.getItem();
            // Mouse Tweaks' output path moves the whole stack, so it never has to put a remainder back here.
            if (!stack.isEmpty() && !slot.mayPlace(stack) && slot.mayPickup(container.modularUI.entityPlayer)) {
                cir.setReturnValue(true);
            }
        }
    }
}
