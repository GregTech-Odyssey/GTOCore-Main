package com.gtocore.mixin.ae2.screen;

import com.gtocore.integration.ae.client.PatternDestinationPanel;
import com.gtocore.integration.ae.hooks.IExtendedPatternEncodingTerm;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.Point;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.menu.me.items.PatternEncodingTermMenu;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatternEncodingTermScreen.class)
public class PatternEncodingTermScreenMixin<C extends PatternEncodingTermMenu> extends MEStorageScreen<C> implements IExtendedPatternEncodingTerm {

    @Shadow(remap = false)
    @Final
    private ActionButton encodeBtn;
    @Unique
    private PatternDestinationPanel gto$destPanel;

    public PatternEncodingTermScreenMixin(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void gtolib$onInit(PatternEncodingTermMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        gto$destPanel = new PatternDestinationPanel(this);
        widgets.add("gto$destPanel", gto$destPanel);
    }

    @Unique
    private Point gto$relative(double mouseX, double mouseY) {
        return new Point((int) Math.round(mouseX - leftPos), (int) Math.round(mouseY - topPos));
    }

    // 面板浮在终端之上，点击须先于终端自身（右键转发、物品槽、其他组件）处理
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void gtolib$panelMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (gto$destPanel.isVisible() && gto$destPanel.onMouseDown(gto$relative(mouseX, mouseY), button)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (gto$destPanel.isVisible() && (gto$destPanel.isCapturingMouse() || gto$destPanel.isMouseOver(mouseX, mouseY)) &&
                gto$destPanel.onMouseUp(gto$relative(mouseX, mouseY), button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (gto$destPanel.isVisible() && (gto$destPanel.isCapturingMouse() || gto$destPanel.isMouseOver(mouseX, mouseY)) &&
                gto$destPanel.onMouseDrag(gto$relative(mouseX, mouseY), button)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (gto$destPanel.isMouseOver(mouseX, mouseY) && gto$destPanel.onMouseWheel(gto$relative(mouseX, mouseY), delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (gto$destPanel.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (gto$destPanel.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public ActionButton gto$getEncodeButton() {
        return encodeBtn;
    }

    @Override
    public PatternDestinationPanel gto$getPatternDestDisplay() {
        return gto$destPanel;
    }

    @Override
    public IExtendedPatternEncodingTerm.Menu gto$getMenu() {
        return (Menu) menu;
    }
}
