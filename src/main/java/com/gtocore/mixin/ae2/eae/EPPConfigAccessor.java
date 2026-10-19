package com.gtocore.mixin.ae2.eae;

import net.minecraftforge.fml.event.config.ModConfigEvent;

import com.glodblock.github.extendedae.config.EPPConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = EPPConfig.class, remap = false)
public interface EPPConfigAccessor {

    @Invoker("onLoad")
    static void gto$onLoad(ModConfigEvent event) {
        throw new AssertionError();
    }
}
