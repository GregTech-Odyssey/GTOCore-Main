package com.gtocore.mixin.ae2.wtlib;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.mari_023.ae2wtlib.AE2wtlibEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AE2wtlibEvents.class, remap = false)
public abstract class AE2wtlibEventsMixin {

    @WrapOperation(method = "pickBlock(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V",
                   at = @At(value = "INVOKE",
                            target = "Lappeng/api/storage/MEStorage;insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J"),
                   require = 2)
    private static long gto$skipEmptyHotbarInsert(MEStorage storage, AEKey what, long amount, Actionable mode, IActionSource source, Operation<Long> original) {
        // 空快捷栏没有物品要退回网络，AEItemKey.of 此时返回 null，不能传给存储接口。
        return amount == 0 ? 0 : original.call(storage, what, amount, mode, source);
    }
}
