package com.gtocore.mixin.inventoryessentials;

import net.blay09.mods.inventoryessentials.ServerInventoryTransfers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import com.periut.chisel.gui.ChiselScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerInventoryTransfers.class)
public class ServerInventoryTransfersMixin {

    @Inject(method = "singleTransfer", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtocore$skipChiselSlots(ServerPlayer player, AbstractContainerMenu menu, Slot slot, CallbackInfo ci) {
        if (menu instanceof ChiselScreenHandler && !(slot.container instanceof Inventory)) {
            ci.cancel();
        }
    }
}
