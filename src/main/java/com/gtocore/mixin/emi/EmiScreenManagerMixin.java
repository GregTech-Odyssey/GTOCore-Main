package com.gtocore.mixin.emi;

import com.gtocore.integration.emi.EmiRecipeWheel;

import net.minecraft.client.Minecraft;

import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * EMI 配方界面的滚轮先问鼠标下的配方页（{@link EmiRecipeWheel}）：页里有控件消费了滚轮就不再翻页。
 * 挂在 EMI 自己的 {@link EmiScreenManager#mouseScrolled}（配方界面处理滚轮的第一步）上，不必注入原版的 {@code mouseScrolled}。
 */
@Mixin(value = EmiScreenManager.class, remap = false)
public class EmiScreenManagerMixin {

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private static void gtocore$scrollRecipePage(double mouseX, double mouseY, double amount, CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().screen instanceof RecipeScreen screen &&
                EmiRecipeWheel.dispatch(((RecipeScreenAccessor) screen).gtocore$getCurrentPage(), mouseX, mouseY, amount)) {
            cir.setReturnValue(true);
        }
    }
}
