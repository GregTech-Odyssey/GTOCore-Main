package com.gtocore.common.wireless.energy;

import com.gtocore.common.wireless.energy.map.GridMapLang;
import com.gtocore.common.wireless.energy.map.GridMapMode;
import com.gtocore.common.wireless.energy.map.GridMapUIFactory;

import com.gtolib.api.annotation.DataGeneratorScanned;
import com.gtolib.api.annotation.language.RegisterLanguage;
import com.gtolib.api.wireless.energy.GridBody;
import com.gtolib.api.wireless.energy.GridView;

import com.gregtechceu.gtceu.api.gui.factory.MachineSubWindowFactory;
import com.gregtechceu.gtceu.api.gui.fancy.SubWindowButton;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineSubWindows;
import com.gregtechceu.gtceu.uipro.LayoutStyle;
import com.gregtechceu.gtceu.uipro.elements.Button;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;

import com.hepdd.gtmthings.data.CustomItems;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import org.jetbrains.annotations.Nullable;

@DataGeneratorScanned
public final class GridMapEntry {

    @RegisterLanguage(cn = "打开电网星图", en = "Open Grid Map")
    public static final String OPEN = "gtocore.wireless_energy.entry.open";
    @RegisterLanguage(cn = "查看队伍电网的节点、线路与流量", en = "View the team grid's nodes, lines and flow")
    public static final String HINT = "gtocore.wireless_energy.entry.hint";

    public static final String WINDOW = "wireless_grid_map";

    private GridMapEntry() {}

    public static int focus(MetaMachine machine) {
        var level = machine.getLevel();
        return level == null ? 0 : GridView.dimRef(GridBody.of(level.dimension()));
    }

    public static Button openButton(MetaMachine machine, Component buttonText, Component tooltip) {
        var button = Button.text(LayoutStyle.AUTO, buttonText);
        var request = button.addRPC(player -> {
            if (player instanceof ServerPlayer serverPlayer) MachineSubWindowFactory.open(serverPlayer, machine, WINDOW);
        }).limit(1);
        button.setOnClientClick(() -> request.send(Unit.INSTANCE));
        return button.tooltips(tooltip);
    }

    public static Button openButton(MetaMachine machine, String buttonKey, String tooltipKey) {
        return openButton(machine, Component.translatable(buttonKey), Component.translatable(tooltipKey));
    }

    public static SubWindowButton subWindowButton(IMachineSubWindows machine) {
        return new SubWindowButton(machine, WINDOW, new ItemStackTexture(CustomItems.WIRELESS_ENERGY_TERMINAL.asStack()),
                GridMapLang.TITLE, Component.translatable(HINT).withStyle(ChatFormatting.GRAY));
    }

    @Nullable
    public static ModularUI window(IMachineSubWindows machine, String key, Player player, GridMapMode mode) {
        if (!WINDOW.equals(key)) return null;
        var self = machine.self();
        return GridMapUIFactory.createUI(machine, player, self, focus(self), mode);
    }
}
