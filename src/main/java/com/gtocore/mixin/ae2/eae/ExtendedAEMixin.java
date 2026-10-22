package com.gtocore.mixin.ae2.eae;

import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.glodblock.github.extendedae.ExtendedAE;
import com.glodblock.github.extendedae.config.EPPConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExtendedAE.class, remap = false)
public class ExtendedAEMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void gto$bakeConfigOnLoading(CallbackInfo ci) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOWEST, false, ModConfigEvent.Loading.class, event -> {
            if (EPPConfig.infCellFluid == null) EPPConfigAccessor.gto$onLoad(event);
        });
    }
}
