package com.gtocore.mixin.inventoryessentials;

import com.gtolib.client.gui.LDLibSlotInteractions;

import net.blay09.mods.balm.api.event.client.screen.ScreenMouseEvent;
import net.blay09.mods.inventoryessentials.client.InventoryEssentialsClient;
import net.blay09.mods.inventoryessentials.mixin.AbstractContainerScreenAccessor;
import net.minecraft.world.inventory.Slot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = InventoryEssentialsClient.class, remap = false)
public abstract class InventoryEssentialsClientMixin {

    @WrapOperation(method = "onMouseDrag", at = @At(value = "INVOKE", target = "Lnet/blay09/mods/inventoryessentials/mixin/AbstractContainerScreenAccessor;getHoveredSlot()Lnet/minecraft/world/inventory/Slot;"))
    private static Slot gtocore$findDraggedOverSlot(AbstractContainerScreenAccessor accessor, Operation<Slot> original,
                                                    @Local(argsOnly = true) ScreenMouseEvent.Drag.Pre event) {
        if (event.getScreen() instanceof ModularUIGuiContainer screen) {
            return LDLibSlotInteractions.findSlot(screen, event.getMouseX(), event.getMouseY());
        }
        return original.call(accessor);
    }
}
