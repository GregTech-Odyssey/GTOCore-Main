package com.gtocore.common.wireless.energy;

import com.gtocore.common.wireless.energy.map.GridMapMode;
import com.gtocore.common.wireless.energy.map.GridSummaryPanel;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineSubWindows;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.elements.Form;
import com.gregtechceu.gtceu.uipro.elements.ScrollerView;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;

import net.minecraft.world.entity.player.Player;

import com.hepdd.gtmthings.api.capability.IBindable;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class WirelessEnergyMonitorMachine extends MetaMachine implements IFancyUIMachine, IBindable, IMachineSubWindows {

    public WirelessEnergyMonitorMachine(MetaMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public Widget createUIWidget() {
        var scroller = ScrollerView.page("wireless_energy_monitor.page", UISizes.CONTENT_WIDTH).adaptiveWidth();
        scroller.addScrollViewChild(Form.page().addChildren(GridSummaryPanel.of(UISizes.CONTENT_WIDTH, this::getOwnerUUID),
                GridMapEntry.openButton(this, GridMapEntry.OPEN, GridMapEntry.HINT)));
        return UIElement.column(LayoutStyle.AUTO).addChild(scroller);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(GridMapEntry.subWindowButton(this));
    }

    @Override
    public @Nullable ModularUI createSubWindow(String key, Player player) {
        return GridMapEntry.window(this, key, player, GridMapMode.VIEW);
    }

    @Override
    public @Nullable UUID getUUID() {
        return getOwnerUUID();
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public boolean display() {
        return false;
    }
}
