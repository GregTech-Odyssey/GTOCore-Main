package com.gtocore.mixin.ae2.wtlib;

import com.gtocore.integration.ae.wtlib.TerminalHotkeys;

import net.minecraft.world.inventory.MenuType;

import de.mari_023.ae2wtlib.terminal.IUniversalWirelessTerminalItem;
import de.mari_023.ae2wtlib.wut.WTDefinition;
import de.mari_023.ae2wtlib.wut.WUTHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cap Universal Terminal energy-card registration to a fixed upgrade budget.
 * Vanilla ae2wtlib uses {@code terminalNames.size() * 2}, which grows with every
 * registered wireless terminal and inflates the upgrade UI.
 */
@Mixin(WUTHandler.class)
public abstract class WUTHandlerMixin {

    private static final int FIXED_UPGRADE_CARD_COUNT = 3;

    @Inject(method = "getUpgradeCardCount", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gtocore$fixedUpgradeCardCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(FIXED_UPGRADE_CARD_COUNT);
    }

    @Inject(method = "addTerminal(Ljava/lang/String;Lde/mari_023/ae2wtlib/wut/WTDefinition$ContainerOpener;Lde/mari_023/ae2wtlib/wut/WTDefinition$WTMenuHostFactory;Lnet/minecraft/world/inventory/MenuType;Lde/mari_023/ae2wtlib/terminal/IUniversalWirelessTerminalItem;Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD"), remap = false)
    private static void gtocore$recordHotkey(String name, WTDefinition.ContainerOpener open, WTDefinition.WTMenuHostFactory hostFactory, MenuType<?> menuType, IUniversalWirelessTerminalItem item, String hotkeyName, String itemID, CallbackInfo ci) {
        TerminalHotkeys.register(name, hotkeyName);
    }
}
