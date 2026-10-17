package com.gtocore.api.gui.configurators;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.uiwidgets.mode.ModeSelector;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomModeFancyConfigurator implements IFancyUIProvider {

    private final int modeSize;

    CustomModeFancyConfigurator(int modeSize) {
        this.modeSize = modeSize;
    }

    public abstract void setMode(int index);

    public abstract int getCurrentMode();

    public abstract String getLanguageKey(int index);

    public Component getTitle() {
        return Component.translatable("gtceu.gui.machinemode.title");
    }

    public IGuiTexture getTabIcon() {
        return new ItemStackTexture(GTItems.ROBOT_ARM_LV.get());
    }

    /** 模式页：新式模式选择（{@link ModeSelector}），当前模式由服务端取值下发，客户端不再回写模式（原来每 tick 在客户端调用 setMode）。 */
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return ModeSelector.create(modeSize, i -> Component.translatable(getLanguageKey(i)), this::getCurrentMode, this::setMode);
    }

    public List<Component> getTabTooltips() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("gtceu.gui.machinemode.tab_tooltip"));
        return tooltip;
    }
}
