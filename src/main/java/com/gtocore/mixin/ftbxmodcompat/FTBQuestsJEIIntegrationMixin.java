package com.gtocore.mixin.ftbxmodcompat;

import dev.ftb.mods.ftbxmodcompat.ftbquests.jei.FTBQuestsJEIIntegration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FTBQuestsJEIIntegration.class, remap = false)
public abstract class FTBQuestsJEIIntegrationMixin implements IModPlugin {

    @Shadow
    public static IJeiRuntime runtime;

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }
}
