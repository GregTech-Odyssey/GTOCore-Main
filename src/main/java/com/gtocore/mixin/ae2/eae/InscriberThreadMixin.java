package com.gtocore.mixin.ae2.eae;

import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.definitions.AEItems;

import com.glodblock.github.extendedae.common.me.InscriberThread;
import com.glodblock.github.extendedae.common.tileentities.TileExInscriber;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(InscriberThread.class)
public abstract class InscriberThreadMixin {

    @Shadow(remap = false)
    @Final
    private TileExInscriber host;

    @ModifyVariable(method = "lambda$tick$1", at = @At("STORE"), name = "speedFactor", remap = false)
    private int gtocore$speedFactor(int speedFactor) {
        return InscriberBlockEntity.getSpeedFactor(host.getUpgrades().getInstalledUpgrades(AEItems.SPEED_CARD));
    }
}
