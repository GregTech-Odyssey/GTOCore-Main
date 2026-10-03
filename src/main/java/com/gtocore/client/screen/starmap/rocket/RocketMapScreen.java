package com.gtocore.client.screen.starmap.rocket;

import com.gtolib.api.adastra.PlanetTravel;
import com.gtolib.api.adastra.TravelSource;

import com.gregtechceu.gtceu.uipro.styletemplate.UIStyleManager;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import earth.terrarium.adastra.common.menus.PlanetsMenu;

/**
 * 取代 Ad Astra 选星页的全屏星图。菜单仍是 {@link PlanetsMenu}，降落、空间站等动作沿用 Ad Astra 的网络包。
 */
@OnlyIn(Dist.CLIENT)
public final class RocketMapScreen extends ModularUIGuiContainer {

    private final PlanetsMenu planets;
    private final RocketMapHost host;

    private RocketMapScreen(PlanetsMenu planets, RocketMapHost host, ModularUI ui) {
        super(ui, -1);
        this.planets = planets;
        this.host = host;
        ui.initWidgets();
    }

    public static RocketMapScreen create(PlanetsMenu planets) {
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        int width = window.getGuiScaledWidth(), height = window.getGuiScaledHeight();
        UIStyleManager.pushOverride(UIStyleManager.STARFIELD_ID, UIStyleManager.STARFIELD_ID);
        var host = new RocketMapHost(planets, width, height);
        var ui = new ModularUI(width, height, IUIHolder.EMPTY, minecraft.player).widget(host);
        var screen = new RocketMapScreen(planets, host, ui);
        host.bind(screen);
        return screen;
    }

    public PlanetsMenu planets() {
        return planets;
    }

    @Override
    public void init() {
        modularUI.setSize(width, height);
        host.resize(width, height);
        super.init();
    }

    @Override
    public void removed() {
        super.removed();
        UIStyleManager.popOverride();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        var player = planets.player();
        var travel = PlanetTravel.of(planets);
        if (player.isCreative() || player.isSpectator() || travel == null || travel.source() != TravelSource.ROCKET) super.onClose();
    }
}
