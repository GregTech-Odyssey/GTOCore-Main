package com.gtocore.mixin.inventoryessentials;

import net.blay09.mods.inventoryessentials.client.InventoryEssentialsClient;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = InventoryEssentialsClient.class, remap = false)
public interface InventoryEssentialsClientAccessor {

    @Accessor("lastDragHoverSlot")
    static void gtocore$setLastDragHoverSlot(Slot slot) {
        throw new AssertionError();
    }
}
