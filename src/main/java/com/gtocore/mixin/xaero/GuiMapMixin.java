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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin {

    @Shadow
    private MapTileSelection mapTileSelection;
    @Shadow
    private ResourceKey<Level> rightClickDim;

    @Inject(method = "renderPreDropdown", at = @At("TAIL"))
    private void gtocore$renderMachineOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        XaeroMachineMap.renderOverlay((GuiMap) (Object) this, graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void gtocore$addCapacityOption(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        XaeroMachineMap.addCapacityOption((GuiMap) (Object) this, rightClickDim, mapTileSelection, cir.getReturnValue());
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
