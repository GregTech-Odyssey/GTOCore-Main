package com.gtocore.mixin.xaero;

import com.gtolib.api.wireless.energy.client.XaeroDimensionLang;
import com.gtolib.api.wireless.energy.client.XaeroDimensionScreen;
import com.gtolib.api.wireless.energy.client.XaeroMachineMap;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapMouseButtonPress;
import xaero.map.gui.MapTileSelection;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin {

    @Shadow
    private MapMouseButtonPress rightMouseButton;
    @Shadow
    private MapTileSelection mapTileSelection;
    @Shadow
    private ResourceKey<Level> rightClickDim;

    @Inject(method = "renderPreDropdown", at = @At("TAIL"))
    private void gtocore$renderMachineOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        XaeroMachineMap.renderOverlay((GuiMap) (Object) this, graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "mapClicked", at = @At(value = "INVOKE", target = "Lxaero/map/gui/GuiMap;handleRightClick(Lxaero/map/gui/IRightClickableElement;II)V", ordinal = 1), cancellable = true)
    private void gtocore$selectNodesInArea(int button, int mouseX, int mouseY, CallbackInfo ci) {
        if (rightMouseButton.pressedAtX < 0 || rightMouseButton.pressedAtY < 0 ||
                Math.abs(mouseX - rightMouseButton.pressedAtX) < 5 && Math.abs(mouseY - rightMouseButton.pressedAtY) < 5)
            return;
        if (XaeroMachineMap.selectArea((GuiMap) (Object) this, rightClickDim, mapTileSelection)) {
            mapTileSelection = null;
            ci.cancel();
        }
    }

    @Inject(method = "onDimensionToggleButton", at = @At("HEAD"), cancellable = true)
    private void gtocore$openDimensionChart(Button button, CallbackInfo ci) {
        ci.cancel();
        XaeroDimensionScreen.open((GuiMap) (Object) this);
    }

    @ModifyConstant(method = "init", constant = @Constant(stringValue = "gui.xaero_dimension_toggle_button"), remap = true)
    private String gtocore$dimensionChartTooltip(String key) {
        return XaeroDimensionLang.OPEN_TOOLTIP;
    }
}
