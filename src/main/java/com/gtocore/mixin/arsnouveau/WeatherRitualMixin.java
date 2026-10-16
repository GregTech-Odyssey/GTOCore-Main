package com.gtocore.mixin.arsnouveau;

import net.minecraft.world.level.Level;

import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.common.ritual.RitualCloudshaper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RitualCloudshaper.class, remap = false)
public abstract class WeatherRitualMixin extends AbstractRitual {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void gto$overworldOnly(CallbackInfo ci) {
        if (getWorld().dimension() != Level.OVERWORLD) {
            setFinished();
            ci.cancel();
        }
    }
}
