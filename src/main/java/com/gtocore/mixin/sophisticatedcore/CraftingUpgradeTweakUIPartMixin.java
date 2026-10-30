package com.gtocore.mixin.sophisticatedcore;

import com.gtocore.client.ClientWorldLeakFixes;
import com.gtocore.client.CraftingUIReferenceHolder;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.compat.craftingtweaks.CraftingUpgradeTweakUIPart;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = CraftingUpgradeTweakUIPart.class, remap = false)
public abstract class CraftingUpgradeTweakUIPartMixin implements CraftingUIReferenceHolder {

    @Shadow
    private StorageScreenBase<?> storageScreen;
    @Shadow
    @Final
    private List<Button> buttons;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtocore$trackCraftingUI(CallbackInfo ci) {
        ClientWorldLeakFixes.trackCraftingUI(this);
    }

    @Inject(method = "setStorageScreen", at = @At("HEAD"))
    private void gtocore$releasePreviousButtons(StorageScreenBase<?> screen, CallbackInfo ci) {
        if (storageScreen != screen) buttons.clear();
    }

    // 上游在 StorageScreenBase 构造期恢复已打开的标签页时会调用，此时 setStorageScreen 尚未执行
    @Inject(method = "onCraftingSlotsDisplayed", at = @At("HEAD"), cancellable = true)
    private void gtocore$skipUntilScreenAssigned(List<Slot> slots, CallbackInfo ci) {
        if (storageScreen == null) ci.cancel();
    }

    @Unique
    @Override
    public void gtocore$releaseScreen(Screen screen) {
        if (storageScreen == screen) gtocore$releaseClientReferences();
    }

    @Unique
    @Override
    public void gtocore$releaseClientReferences() {
        buttons.clear();
        storageScreen = null;
    }
}
