package com.gtocore.utils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GuiHelper {

    @OnlyIn(Dist.CLIENT)
    public double getRealMouseX() {
        var mc = Minecraft.getInstance();
        return mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
    }

    @OnlyIn(Dist.CLIENT)
    public double getRealMouseY() {
        var mc = Minecraft.getInstance();
        return mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
    }
}
