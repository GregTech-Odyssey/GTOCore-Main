package com.gtocore.mixin.inventoryessentials;

import com.gtolib.client.gui.LDLibSlotInteractions;

import net.blay09.mods.inventoryessentials.client.ModKeyMappings;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerScreenAccessor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ModKeyMappings.class, remap = false)
public abstract class ModKeyMappingsMixin {

    @WrapOperation(method = "handleSlotInput", at = @At(value = "INVOKE", target = "Lnet/blay09/mods/inventoryessentials/mixin/AbstractContainerScreenAccessor;getHoveredSlot()Lnet/minecraft/world/inventory/Slot;"))
    private static Slot gtocore$findWidgetSlot(AbstractContainerScreenAccessor accessor, Operation<Slot> original) {
        if ((AbstractContainerScreen<?>) accessor instanceof ModularUIGuiContainer screen) {
            return LDLibSlotInteractions.findSlot(screen);
        }
        return original.call(accessor);
    }
}
