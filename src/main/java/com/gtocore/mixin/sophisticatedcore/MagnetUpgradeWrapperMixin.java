package com.gtocore.mixin.sophisticatedcore;

import com.gtocore.utils.StxckUtil;

import net.minecraft.world.entity.item.ItemEntity;
import net.p3pp3rf1y.sophisticatedcore.upgrades.magnet.MagnetUpgradeWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MagnetUpgradeWrapper.class, remap = false)
public class MagnetUpgradeWrapperMixin {

    @Unique
    private boolean gtocore$draining;

    @Shadow
    private boolean tryToInsertItem(ItemEntity itemEntity) {
        throw new AssertionError();
    }

    @Inject(method = "tryToInsertItem", at = @At("RETURN"))
    private void gtocore$drainStackedItems(ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        if (gtocore$draining || !cir.getReturnValueZ()) return;
        gtocore$draining = true;
        try {
            while (itemEntity.isAlive() && itemEntity.getItem().isEmpty()) {
                int extra = StxckUtil.getExtraItemCount(itemEntity);
                if (!StxckUtil.refillFromExtra(itemEntity)) break;
                if (itemEntity.getItem().isEmpty()) {
                    StxckUtil.setExtraItemCount(itemEntity, extra);
                    break;
                }
                if (!tryToInsertItem(itemEntity)) break;
            }
        } finally {
            gtocore$draining = false;
        }
    }
}
