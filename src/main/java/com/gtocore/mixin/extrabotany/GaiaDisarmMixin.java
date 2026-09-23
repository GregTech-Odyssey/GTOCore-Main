package com.gtocore.mixin.extrabotany;

import com.gregtechceu.gtceu.common.item.armor.ArmorSuiteFeatures;

import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaDisarm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 盖亚守护者 III 每 tick 的缴械：纳米肌体 / 夸克高科 II 起的装备可按 1 A/s 耗电抵消，不被卸下；没电时照常卸下
 */
@Mixin(value = GaiaDisarm.class, remap = false)
public class GaiaDisarmMixin {

    @WrapOperation(method = "disArm",
                   at = @At(value = "INVOKE",
                            target = "Lio/github/lounode/extrabotany/api/gaia/GaiaArena;checkFeasibility(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtocore$resistDisarm(ItemStack stack, Operation<Boolean> original) {
        if (original.call(stack)) return true;
        return ArmorSuiteFeatures.isGaiaResistant(stack) && ArmorSuiteFeatures.payGaiaDisarm(stack);
    }
}
