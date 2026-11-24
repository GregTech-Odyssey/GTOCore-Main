package com.gtocore.mixin.mousetweaks;

import com.gtolib.client.gui.LDLibSlotInteractions;

import net.minecraft.client.Minecraft;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yalter.mousetweaks.Main;

@Mixin(ModularUIGuiContainer.class)
public abstract class ModularUIGuiContainerMixin {

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void gtocore$wheelOverItemSlot(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        var screen = (ModularUIGuiContainer) (Object) this;
        if (screen != Minecraft.getInstance().screen) return;
        var slot = LDLibSlotInteractions.findSlot(screen, mouseX, mouseY);
        if (slot != null && slot.hasItem() && Main.onMouseScrolled(screen, mouseX, mouseY, delta)) {
            cir.setReturnValue(true);
        }
    }
}
