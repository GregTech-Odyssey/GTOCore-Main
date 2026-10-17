package com.gtocore.mixin.ae2.eae;

import com.gtocore.integration.ae.AccelerationRates;

import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;

import com.glodblock.github.extendedae.common.me.CraftingThread;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingThread.class)
public abstract class CraftingThreadMixin {

    @Shadow(remap = false)
    private int userPower(int ticksPassed, int bonusValue, double acceleratorTax) {
        throw new AssertionError();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lcom/glodblock/github/extendedae/common/me/CraftingThread;userPower(IID)I"), remap = false)
    private int gtocore$progress(CraftingThread self, int ticksPassed, int bonusValue, double acceleratorTax, int cards, int ticksSinceLastCall) {
        return userPower(ticksPassed, AccelerationRates.exAssemblerProgressPerTick(cards), MolecularAssemblerBlockEntity.getPowerFactor(cards));
    }
}
