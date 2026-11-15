package com.gtocore.mixin.xaero;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.map.animation.Animation;
import xaero.map.gui.GuiMap;

@Mixin(value = GuiMap.class, remap = false)
public interface GuiMapAccessor {

    @Accessor("cameraX")
    double gtocore$cameraX();

    @Accessor("cameraZ")
    double gtocore$cameraZ();

    @Accessor("scale")
    double gtocore$scale();

    @Accessor("cameraDestination")
    void gtocore$destination(int[] destination);

    @Accessor("userScale")
    void gtocore$userScale(double scale);

    @Accessor("zoomAnim")
    void gtocore$zoomAnimation(Animation animation);

    @Accessor("destScale")
    static void gtocore$destinationScale(double scale) {
        throw new AssertionError();
    }
}
