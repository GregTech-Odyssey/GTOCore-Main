package com.gtocore.mixin.ae2.eae;

import appeng.api.behaviors.StackTransferContext;
import appeng.api.stacks.AEKey;
import appeng.parts.automation.IOBusPart;

import com.glodblock.github.extendedae.common.parts.PartPreciseExportBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PartPreciseExportBus.class)
public abstract class PartPreciseExportBusMixin {

    @Shadow(remap = false)
    private static long simulateExtract(StackTransferContext context, AEKey what, long amount) {
        throw new AssertionError();
    }

    @Redirect(method = "doBusWork",
              at = @At(value = "INVOKE",
                       target = "Lcom/glodblock/github/extendedae/common/parts/PartPreciseExportBus;simulateExtract(Lappeng/api/behaviors/StackTransferContext;Lappeng/api/stacks/AEKey;J)J",
                       ordinal = 0),
              remap = false)
    private long gtocore$exactBatches(StackTransferContext context, AEKey what, long amount) {
        long fit = context.getOperationsRemaining() * what.getAmountPerOperation() / amount;
        long batches = Math.max(1, Math.min(((IOBusPart) (Object) this).getTickRateCompensation(), fit));
        return simulateExtract(context, what, batches * amount);
    }
}
