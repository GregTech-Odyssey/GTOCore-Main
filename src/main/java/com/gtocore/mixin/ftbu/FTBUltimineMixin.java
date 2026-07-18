package com.gtocore.mixin.ftbu;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.hollingsworth.arsnouveau.common.items.SpellBook;
import dev.architectury.event.EventResult;
import dev.architectury.utils.value.IntValue;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FTBUltimine.class)
public class FTBUltimineMixin {

    @Inject(method = "playerTick", at = @At("HEAD"), remap = false, cancellable = true)
    private void playerTick(Player player, CallbackInfo ci) {
        if (gtolib$notDiggerItem(player)) ci.cancel();
    }

    @Inject(method = "blockBroken", at = @At("HEAD"), remap = false, cancellable = true)
    private void blockBroken(Level world, BlockPos pos, BlockState state, ServerPlayer player, IntValue xp, CallbackInfoReturnable<EventResult> cir) {
        if (gtolib$notDiggerItem(player)) cir.setReturnValue(EventResult.pass());
    }

    @Unique
    private static boolean gtolib$isSprayCan(ItemStack stack) {
        return !stack.isEmpty() && stack.is(GTItems.INFINITE_SPRAY_CAN.get());
    }

    @Unique
    private static boolean gtolib$notDiggerItem(Player player) {
        if (player.isCreative()) {
            return false;
        }
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof DiggerItem || main.getItem() instanceof SpellBook || gtolib$isSprayCan(main)) {
            return false;
        }
        return true;
    }

    @Inject(method = "isValidTool", at = @At("HEAD"), remap = false, cancellable = true)
    private static void isValidTool(ItemStack mainHand, ItemStack offHand, CallbackInfoReturnable<Boolean> cir) {
        if (mainHand.getItem() instanceof SpellBook || gtolib$isSprayCan(mainHand)) {
            cir.setReturnValue(true);
        }
    }
}
