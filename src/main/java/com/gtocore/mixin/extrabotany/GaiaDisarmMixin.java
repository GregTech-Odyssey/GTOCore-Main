package com.gtocore.mixin.extrabotany;

/**
 * 盖亚守护者 III 每 tick 的缴械（缴械魔法破解）：纳米肌体 / 夸克高科 II 起的装备与能量剑可按 1 A/s 耗电抵消，不被卸下；没电时照常卸下
 */
// TODO @Mixin(value = GaiaDisarm.class, remap = false)
public class GaiaDisarmMixin {

    // @WrapOperation(method = "disArm",
    // at = @At(value = "INVOKE",
    // target =
    // "Lio/github/lounode/extrabotany/api/gaia/GaiaArena;checkFeasibility(Lnet/minecraft/world/item/ItemStack;)Z"))
    // private boolean gtocore$resistDisarm(ItemStack stack, Operation<Boolean> original) {
    // if (original.call(stack)) return true;
    // return ArmorSuiteFeatures.isGaiaResistant(stack) && ArmorSuiteFeatures.payGaiaDisarm(stack);
    // }
}
