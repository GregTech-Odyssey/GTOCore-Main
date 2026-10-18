package com.gtocore.mixin.emitrades;

import com.gtocore.integration.emi.trades.DetachedTradeProfile;

import io.github.prismwork.emitrades.recipe.VillagerTrade;
import io.github.prismwork.emitrades.util.TradeProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VillagerTrade.class, remap = false)
public class VillagerTradeMixin {

    @Shadow
    @Final
    @Mutable
    private TradeProfile profile;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtocore$detachProfile(CallbackInfo ci) {
        profile = new DetachedTradeProfile(profile);
    }
}
