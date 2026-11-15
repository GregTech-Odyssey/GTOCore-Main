package com.gtocore.mixin.mc;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;

@Mixin(TimeCommand.class)
public abstract class TimeCommandMixin {

    @ModifyExpressionValue(method = { "setTime", "addTime" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"))
    private static Iterable<ServerLevel> localTime(Iterable<ServerLevel> original, CommandSourceStack source, int time) {
        return Collections.singletonList(source.getLevel());
    }
}
