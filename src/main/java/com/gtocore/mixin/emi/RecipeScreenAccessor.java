package com.gtocore.mixin.emi;

import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = RecipeScreen.class, remap = false)
public interface RecipeScreenAccessor {

    /** 当前页上各配方的控件组（屏幕坐标 x、y 为组的原点）。 */
    @Accessor("currentPage")
    List<WidgetGroup> gtocore$getCurrentPage();
}
