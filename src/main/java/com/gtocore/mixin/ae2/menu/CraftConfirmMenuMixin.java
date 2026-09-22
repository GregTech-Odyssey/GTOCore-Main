package com.gtocore.mixin.ae2.menu;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.menu.me.crafting.CraftConfirmMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftConfirmMenu.class)
public abstract class CraftConfirmMenuMixin {

    /**
     * {@code getGrid()} 无条件把 target 转型成 {@link IActionHost}，但便携式存储元件一类的 target
     * （{@code PortableCellMenuHost}）并未实现该接口；转型抛出的 ClassCastException 会从
     * {@code broadcastChanges()} 一路冒泡进 ServerPlayer tick，直接崩服。
     * 这里改为返回 null，复用 AE2 自身「取不到网络」的分支：置菜单无效并关闭。
     */
    @Inject(method = "getGrid", at = @At("HEAD"), cancellable = true, remap = false)
    private void gto$guardNonActionHostTarget(CallbackInfoReturnable<IGrid> cir) {
        if (!(((CraftConfirmMenu) (Object) this).getTarget() instanceof IActionHost)) {
            cir.setReturnValue(null);
        }
    }
}
