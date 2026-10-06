package com.gtocore.mixin.mc;

import com.gtocore.common.weather.WeatherCommands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WeatherCommand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherCommand.class)
public abstract class WeatherCommandMixin {

    @Inject(method = "setClear", at = @At("HEAD"), cancellable = true)
    private static void gto$localClear(CommandSourceStack source, int ticks, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(WeatherCommands.setVanilla(source, ticks, false, false));
    }

    @Inject(method = "setRain", at = @At("HEAD"), cancellable = true)
    private static void gto$localRain(CommandSourceStack source, int ticks, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(WeatherCommands.setVanilla(source, ticks, true, false));
    }

    @Inject(method = "setThunder", at = @At("HEAD"), cancellable = true)
    private static void gto$localThunder(CommandSourceStack source, int ticks, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(WeatherCommands.setVanilla(source, ticks, true, true));
    }
}
