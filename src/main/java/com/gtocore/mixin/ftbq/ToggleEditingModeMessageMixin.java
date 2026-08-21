package com.gtocore.mixin.ftbq;

import com.gtocore.integration.ftbquests.EMIRecipeModHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ftb.mods.ftbquests.net.ToggleEditingModeMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ToggleEditingModeMessage.class)
public class ToggleEditingModeMessageMixin {

    @ModifyArg(method = "handle", at = @At(value = "INVOKE", target = "Ldev/ftb/mods/ftbquests/quest/TeamData;setCanEdit(Lnet/minecraft/world/entity/player/Player;Z)Z"), index = 1, remap = false)
    private boolean setCanEdit(boolean newCanEdit, @Local(name = "player") ServerPlayer player) {
        if (!EMIRecipeModHelper.canEdit()) {
            player.sendSystemMessage(Component.translatable("message.gtocore.ftbq_editmode"));
            player.sendSystemMessage(Component.translatable("message.gtocore.ftbq_editmode.1"));
            player.sendSystemMessage(Component.translatable("message.gtocore.ftbq_editmode.2"));
            return false;
        }
        return newCanEdit;
    }
}
