package com.gtocore.client.screen.starmap.rocket;

import com.gtocore.client.screen.starmap.base.BodyLayer;
import com.gtocore.client.screen.starmap.base.PixelPen;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarGeometry;
import com.gtocore.client.screen.starmap.base.StarNavigator;
import com.gtocore.client.screen.starmap.base.StarSky;
import com.gtocore.client.screen.starmap.base.SystemLayer;

import com.gtolib.api.adastra.PlanetTravel;
import com.gtolib.api.misc.PlanetManagement;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.ILayoutHost;
import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.elements.Dock;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.view.ZoomBar;
import com.gregtechceu.gtceu.uipro.window.CardHost;
import com.gregtechceu.gtceu.uipro.window.Popup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.network.NetworkHandler;
import earth.terrarium.adastra.common.network.messages.ServerboundLandOnSpaceStationPacket;
import earth.terrarium.adastra.common.network.messages.ServerboundLandPacket;
import org.jetbrains.annotations.Nullable;

/**
 * 火箭星图的根：全屏画布（所有星系同屏）、底部视图工具条、右侧星球详情卡片，背景是方块星点。
 */
@OnlyIn(Dist.CLIENT)
final class RocketMapHost extends UIElement implements ILocalUI, ILayoutHost {

    private final PlanetsMenu planets;
    private final StarCatalog catalog;
    private final StarGeometry geometry;
    private final RocketDecorator decorator;
    private final CanvasView canvas;
    private final CardHost details;
    private final StarNavigator navigator;
    private final StarSky sky = new StarSky();
    @Nullable
    private RocketMapScreen screen;

    RocketMapHost(PlanetsMenu planets, int width, int height) {
        this.planets = planets;
        StarCatalog.CatalogFilter disabled = dimension -> planets.disabledPlanets().contains(dimension.location());
        this.catalog = StarCatalog.build(disabled.or(StarCatalog.CatalogFilter.UNREACHABLE));
        this.geometry = new StarGeometry(catalog);
        setClientSideWidget();
        layout(l -> l.size(width, height));
        details = new CardHost("starmap.details", this::createDetails);
        decorator = new RocketDecorator(catalog, planets, details::getArgument);
        canvas = new CanvasView("starmap", width, height);
        canvas.setFrame(false, 0);
        canvas.setResizable(false);
        canvas.setRememberView(false);
        canvas.setGrid(null);
        canvas.setScaleRange(0.02f, 4f);
        canvas.setMinScaleFits(true);
        canvas.setLodThresholds(0, 0);
        canvas.setFitPadding(StarNavigator.FIT_PADDING);
        navigator = new StarNavigator(canvas, geometry);
        var pen = new PixelPen();
        var bodies = new BodyLayer(geometry, decorator, pen, canvas);
        var systems = new SystemLayer(geometry, decorator, bodies, pen, canvas);
        canvas.setScene(view -> view.addLayer(systems).addLayer(bodies));
        canvas.setOnClientItemClick((item, button, worldX, worldY) -> {
            if (button != 0) return;
            int body = bodies.bodyOf(item);
            if (body >= 0) select(body);
            int anchor = bodies.anchorOf(item);
            if (anchor >= 0) navigator.fitAnchor(anchor);
        });
        canvas.setOnClientBackgroundClick((worldX, worldY, button) -> {
            if (button == 0) details.close();
        });
        canvas.setInitialView(navigator::initialView);
        canvas.addFloatingCard(details);
        var rocketText = rocketText();
        var rocket = TextLine.constant(UIText.width(rocketText) + 2, rocketText).bindClientColor(UITheme::panelText);
        canvas.addOverlay(new Dock().addGroup(navigator.systemButtons(decorator)).addGroup(ZoomBar.dock(canvas)).addGroup(rocket)
                .addGroup(InfoIcon.info(RocketLang.HELP, RocketLang.HELP_KEYS, CanvasView.HELP_PAN, CanvasView.HELP_ZOOM)));
        addChild(canvas);
    }

    void bind(RocketMapScreen screen) {
        this.screen = screen;
    }

    void resize(int width, int height) {
        layout(l -> l.size(width, height));
        canvas.setPreferredSize(width, height);
    }

    StarCatalog catalog() {
        return catalog;
    }

    RocketDecorator decorator() {
        return decorator;
    }

    PlanetsMenu planets() {
        return planets;
    }

    private Component rocketText() {
        var travel = PlanetTravel.of(planets);
        return travel != null ? Component.translatable(travel.source().getTranslationKey(), travel.tier()) : Component.translatable(RocketLang.NO_ROCKET);
    }

    private void select(int body) {
        if (details.toggle(body)) navigator.reveal(body);
    }

    @Nullable
    private Popup createDetails(int index) {
        if (catalog.isRealm(index)) return Popup.of(() -> catalog.name(index), column -> RocketDetails.buildRealm(column, this, index));
        if (index < 0 || index >= catalog.bodyCount()) return null;
        return Popup.of(() -> catalog.name(index), column -> RocketDetails.build(column, this, index));
    }

    void land(int body) {
        ResourceKey<Level> dimension = catalog.dimension(body);
        if (dimension == null) return;
        if (!GTCEu.isDev()) {
            PlanetManagement.checkPlanetIsUnlocked(dimension);
            if (!PlanetManagement.isClientUnlocked(dimension)) {
                planets.player().displayClientMessage(Component.translatable("gtocore.ununlocked"), false);
                leave();
                return;
            }
        }
        NetworkHandler.CHANNEL.sendToServer(new ServerboundLandPacket(dimension, true));
        leave();
    }

    void landOnStation(ResourceKey<Level> orbit, ChunkPos position) {
        NetworkHandler.CHANNEL.sendToServer(new ServerboundLandOnSpaceStationPacket(orbit, position));
        leave();
    }

    void construct(int body) {
        var dimension = catalog.dimension(body);
        var orbit = catalog.orbit(body);
        if (dimension == null || orbit == null) return;
        int owned = planets.getOwnedAndTeamSpaceStations(orbit).size();
        planets.constructSpaceStation(dimension, Component.translatable("text.ad_astra.text.space_station_name", owned + 1));
        leave();
    }

    private void leave() {
        if (screen != null) screen.onClose();
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        navigator.pan(screen);
        sky.draw(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), canvas);
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onContentResized(Widget root) {}
}
