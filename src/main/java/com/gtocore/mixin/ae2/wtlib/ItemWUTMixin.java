package com.gtocore.mixin.ae2.wtlib;

import net.minecraft.world.item.ItemStack;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;

import de.mari_023.ae2wtlib.wut.ItemWUT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cap Wireless Universal Terminal upgrade inventory to a fixed 3 slots.
 * Vanilla ae2wtlib uses {@code countInstalledTerminals(stack) * 2}, so merging
 * many terminals creates an oversized upgrade column in the GUI.
 */
@Mixin(ItemWUT.class)
public abstract class ItemWUTMixin {

    private static final int FIXED_UPGRADE_SLOTS = 3;

    @Inject(method = "getUpgrades", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtocore$fixedUpgradeSlots(ItemStack stack, CallbackInfoReturnable<IUpgradeInventory> cir) {
        ItemWUT self = (ItemWUT) (Object) this;
        cir.setReturnValue(UpgradeInventories.forItem(stack, FIXED_UPGRADE_SLOTS, self::onUpgradesChanged));
    }
}
