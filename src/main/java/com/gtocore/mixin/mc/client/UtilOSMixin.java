package com.gtocore.mixin.mc.client;

import com.gtocore.utils.GTOWebsite;

import net.minecraft.Util;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.net.URL;

@Mixin(Util.OS.class)
public abstract class UtilOSMixin {

    @ModifyVariable(method = "openUrl", at = @At("HEAD"), argsOnly = true)
    private URL gtocore$markWebsite(URL url) {
        return GTOWebsite.mark(url);
    }
}
