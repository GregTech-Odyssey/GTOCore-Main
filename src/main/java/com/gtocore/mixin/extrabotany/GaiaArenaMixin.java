package com.gtocore.mixin.extrabotany;

/**
 * 召唤盖亚守护者 III 前的物品检查（召唤限制破解）：纳米肌体 / 夸克高科 II 起的装备与能量剑可耗电破解，视为允许携带。
 * 破解成功不提示；电量不足时提示并按原逻辑拒绝召唤。其余不允许的物品照旧拒绝，且此时不扣电。
 */
// TODO @Mixin(value = GaiaArena.class, remap = false)
public class GaiaArenaMixin {

    // @Inject(method = "checkInventoryPass", at = @At("HEAD"), cancellable = true)
    // private static void gtocore$bypassSuitePieces(Player player, CallbackInfoReturnable<Boolean> cir) {
    // if (player.isCreative() || ExtraBotanyConfig.common().disableGaiaDisArm()) return;
    // Inventory inventory = player.getInventory();
    // ObjectArrayList<ItemStack> suitePieces = null;
    // for (int i = 0; i < inventory.getContainerSize(); i++) {
    // ItemStack stack = inventory.getItem(i);
    // if (GaiaArena.checkFeasibility(stack)) continue;
    // if (!ArmorSuiteFeatures.isGaiaResistant(stack)) {
    // cir.setReturnValue(false);
    // return;
    // }
    // if (!ArmorSuiteFeatures.canPayGaiaInventory(stack)) {
    // player.sendSystemMessage(ArmorSuiteFeatures.gaiaNoEnergyMessage(stack));
    // cir.setReturnValue(false);
    // return;
    // }
    // if (suitePieces == null) suitePieces = new ObjectArrayList<>(4);
    // suitePieces.add(stack);
    // }
    // // 全部通过后才扣电
    // if (suitePieces != null) {
    // for (int i = 0; i < suitePieces.size(); i++) {
    // ArmorSuiteFeatures.payGaiaInventory(suitePieces.get(i));
    // }
    // }
    // cir.setReturnValue(true);
    // }
}
