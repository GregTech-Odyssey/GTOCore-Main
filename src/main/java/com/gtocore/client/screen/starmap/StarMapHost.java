package com.gtocore.client.screen.starmap;

import com.gtolib.api.adastra.PlanetTravel;
import com.gtolib.api.misc.PlanetManagement;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.ILayoutHost;
import com.gregtechceu.gtceu.uipro.ILocalUI;
import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.animation.UIClock;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.elements.Dock;
import com.gregtechceu.gtceu.uipro.elements.InfoIcon;
import com.gregtechceu.gtceu.uipro.elements.TextLine;
import com.gregtechceu.gtceu.uipro.render.UIText;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;
import com.gregtechceu.gtceu.uipro.view.ZoomBar;
import com.gregtechceu.gtceu.uipro.window.CardHost;
import com.gregtechceu.gtceu.uipro.window.Popup;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import earth.terrarium.adastra.common.menus.PlanetsMenu;
import earth.terrarium.adastra.common.network.NetworkHandler;
import earth.terrarium.adastra.common.network.messages.ServerboundLandOnSpaceStationPacket;
import earth.terrarium.adastra.common.network.messages.ServerboundLandPacket;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/**
 * 星图界面的根：全屏画布（所有星系同屏）、底部视图工具条、右侧星球详情卡片，背景是方块星点。
 */
@OnlyIn(Dist.CLIENT)
final class StarMapHost extends UIElement implements ILocalUI, ILayoutHost {

    private static final int NAV_BUTTON_WIDTH = 64;
    private static final int FIT_PADDING = 8;
    private static final int DOCK_ROOM = 28;
    private static final float VIEW_SCALE = 0.8f;
    private static final int SPACE_TOP = 0xFF080A14;
    private static final int SPACE_BOTTOM = 0xFF0B0E1E;
    private static final int STAR_COUNT = 520;
    private static final int SKY_TEXEL = 2;
    private static final float PAN_SPEED = 260;
    private static final ResourceLocation END_SKY = new ResourceLocation("textures/environment/end_sky.png");

    private final PlanetsMenu planets;
    private final StarMapModel model;
    private final CanvasView canvas;
    private final CardHost details;
    private final float[] stars = new float[STAR_COUNT * 4];
    @Nullable
    private StarMapScreen screen;
    private long lastPan;
    private float skyPanX, skyPanY, lastOffsetX = Float.NaN, lastOffsetY, lastScale;

    StarMapHost(PlanetsMenu planets, int width, int height) {
        this.planets = planets;
        this.model = StarMapModel.build(planets);
        setClientSideWidget();
        layout(l -> l.size(width, height));
        var random = new Random(0x5EED);
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i * 4] = random.nextFloat();
            stars[i * 4 + 1] = random.nextFloat();
            stars[i * 4 + 2] = random.nextFloat();
            stars[i * 4 + 3] = random.nextFloat() * 6.2832f;
        }
        details = new CardHost("starmap.details", this::createDetails);
        canvas = new CanvasView("starmap", width, height);
        canvas.setFrame(false, 0);
        canvas.setResizable(false);
        canvas.setRememberView(false);
        canvas.setGrid(null);
        canvas.setScaleRange(0.02f, 4f);
        canvas.setMinScaleFits(true);
        canvas.setLodThresholds(0, 0);
        canvas.setFitPadding(FIT_PADDING);
        canvas.setScene(view -> StarMapScene.build(view, this));
        canvas.setOnClientItemClick((item, button, worldX, worldY) -> {
            if (button == 0 && item instanceof StarMapScene.BodyItem body) select(body.body());
            if (button == 0 && item instanceof StarMapScene.AnchorItem anchor) canvas.fit(anchor.bounds().inflate(40 / canvas.scale()), FIT_PADDING, 3f, true);
        });
        canvas.setOnClientBackgroundClick((worldX, worldY, button) -> {
            if (button == 0) details.close();
        });
        canvas.setInitialView(view -> {
            if (model.current != null) look(StarMapScene.x(model.current, VIEW_SCALE), StarMapScene.y(model.current, VIEW_SCALE), false);
            else if (model.focus != null) look(model.focus.cx, model.focus.cy, false);
            else view.fitContent(false);
        });
        canvas.addFloatingCard(details);
        var rocketText = rocketText();
        var rocket = TextLine.constant(UIText.width(rocketText) + 2, rocketText).bindClientColor(UITheme::panelText);
        canvas.addOverlay(new Dock().addGroup(systemButtons()).addGroup(ZoomBar.dock(canvas)).addGroup(rocket)
                .addGroup(InfoIcon.info(StarMapLang.HELP, StarMapLang.HELP_KEYS, CanvasView.HELP_PAN, CanvasView.HELP_ZOOM)));
        addChild(canvas);
    }

    void bind(StarMapScreen screen) {
        this.screen = screen;
    }

    void resize(int width, int height) {
        layout(l -> l.size(width, height));
        canvas.setPreferredSize(width, height);
    }

    CanvasView canvas() {
        return canvas;
    }

    float viewScale() {
        return canvas.scale();
    }

    StarMapModel model() {
        return model;
    }

    PlanetsMenu planets() {
        return planets;
    }

    @Nullable
    StarMapModel.Body selected() {
        int index = details.getArgument();
        return index >= 0 && index < model.bodies.size() ? model.bodies.get(index) : null;
    }

    private UIElement systemButtons() {
        var row = ZoomBar.dockRow();
        for (var system : model.systems) {
            var button = Button.text(NAV_BUTTON_WIDTH, UISizes.DOCK_BUTTON_SIZE, system.name)
                    .setOnClientClick(() -> look(system.cx, system.cy, true));
            button.setSelected(() -> viewedSystem() == system);
            button.tooltips(Component.translatable(StarMapLang.REACHABLE, model.reachableCount(system), system.bodies.size()));
            row.addChild(button);
        }
        return row;
    }

    @Nullable
    private StarMapModel.SystemInfo viewedSystem() {
        float scale = canvas.scale();
        float centerX = canvas.offsetX() + canvas.getSizeWidth() / 2f / scale, centerY = canvas.offsetY() + canvas.getSizeHeight() / 2f / scale;
        float viewWidth = canvas.getSizeWidth() / scale;
        for (var system : model.systems) {
            var bounds = system.bounds();
            if (bounds.width() >= viewWidth * 0.6f && centerX >= bounds.x() && centerX <= bounds.x() + bounds.width() &&
                    centerY >= bounds.y() && centerY <= bounds.y() + bounds.height())
                return system;
        }
        return null;
    }

    private void look(float worldX, float worldY, boolean animated) {
        canvas.setView(worldX - canvas.getSizeWidth() / (2 * VIEW_SCALE), worldY - (canvas.getSizeHeight() - DOCK_ROOM) / (2 * VIEW_SCALE), VIEW_SCALE, animated);
    }

    private Component rocketText() {
        var travel = PlanetTravel.of(planets);
        return travel != null ? Component.translatable(travel.source().getTranslationKey(), travel.tier()) : Component.translatable(StarMapLang.NO_ROCKET);
    }

    private void select(StarMapModel.Body body) {
        if (details.toggle(body.index())) canvas.reveal(StarMapScene.bounds(body, canvas.scale()).inflate(UISizes.SLOT_SIZE), true);
    }

    @Nullable
    private Popup createDetails(int index) {
        if (index < 0 || index >= model.bodies.size()) return null;
        var body = model.bodies.get(index);
        return Popup.of(body::name, column -> StarMapDetails.build(column, this, body));
    }

    void land(StarMapModel.Body body) {
        ResourceKey<Level> dimension = body.dimensionKey();
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

    void construct(StarMapModel.Body body) {
        int owned = planets.getOwnedAndTeamSpaceStations(body.orbit()).size();
        planets.constructSpaceStation(body.dimensionKey(), Component.translatable("text.ad_astra.text.space_station_name", owned + 1));
        leave();
    }

    private void leave() {
        if (screen != null) screen.onClose();
    }

    private void panWithKeys() {
        long now = UIClock.millis();
        float seconds = lastPan == 0 ? 0 : Math.min(0.1f, (now - lastPan) / 1000f);
        lastPan = now;
        var minecraft = Minecraft.getInstance();
        if (seconds <= 0 || minecraft.screen != screen) return;
        var options = minecraft.options;
        int dx = (held(options.keyRight, GLFW.GLFW_KEY_RIGHT) ? 1 : 0) - (held(options.keyLeft, GLFW.GLFW_KEY_LEFT) ? 1 : 0);
        int dy = (held(options.keyDown, GLFW.GLFW_KEY_DOWN) ? 1 : 0) - (held(options.keyUp, GLFW.GLFW_KEY_UP) ? 1 : 0);
        if (dx == 0 && dy == 0) return;
        float speed = PAN_SPEED * seconds / canvas.scale() * (Screen.hasShiftDown() ? 2.5f : 1);
        canvas.setView(canvas.offsetX() + dx * speed, canvas.offsetY() + dy * speed, canvas.scale(), false);
    }

    private static boolean held(KeyMapping mapping, int arrow) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        var key = mapping.getKey();
        return InputConstants.isKeyDown(window, arrow) ||
                key.getType() == InputConstants.Type.KEYSYM && key.getValue() != InputConstants.UNKNOWN.getValue() && InputConstants.isKeyDown(window, key.getValue());
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        panWithKeys();
        int x = getPositionX(), y = getPositionY(), w = getSizeWidth(), h = getSizeHeight();
        graphics.fillGradient(x, y, x + w, y + h, SPACE_TOP, SPACE_BOTTOM);
        trackSkyPan();
        float panX = skyPanX, panY = skyPanY;
        int shiftX = Math.round(panX * 0.03f / SKY_TEXEL), shiftY = Math.round(panY * 0.03f / SKY_TEXEL);
        int texelsX = (w + SKY_TEXEL - 1) / SKY_TEXEL, texelsY = (h + SKY_TEXEL - 1) / SKY_TEXEL;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(0.3f, 0.34f, 0.6f, 0.1f);
        graphics.blit(END_SKY, x, y, texelsX * SKY_TEXEL, texelsY * SKY_TEXEL, shiftX, shiftY, texelsX, texelsY, 128, 128);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        drawStars(graphics, x, y, w, h, panX, panY);
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }

    private void trackSkyPan() {
        float scale = canvas.scale(), offsetX = canvas.offsetX(), offsetY = canvas.offsetY();
        if (!Float.isNaN(lastOffsetX) && scale == lastScale) {
            skyPanX += (offsetX - lastOffsetX) * scale;
            skyPanY += (offsetY - lastOffsetY) * scale;
        }
        lastOffsetX = offsetX;
        lastOffsetY = offsetY;
        lastScale = scale;
    }

    private void drawStars(GuiGraphics graphics, int x, int y, int w, int h, float panX, float panY) {
        int count = Math.min(STAR_COUNT, Math.max(120, w * h / 900));
        for (int i = 0; i < count; i++) {
            float depth = stars[i * 4 + 2];
            float parallax = 0.02f + 0.1f * depth;
            int sx = Math.floorMod(Math.round(stars[i * 4] * w - panX * parallax), w);
            int sy = Math.floorMod(Math.round(stars[i * 4 + 1] * h - panY * parallax), h);
            int color = depth > 0.75f ? 0xFFFFFFFF : depth > 0.4f ? 0xFF8890B0 : 0xFF4A5070;
            int size = depth > 0.96f ? 2 : 1;
            graphics.fill(x + sx, y + sy, x + sx + size, y + sy + size, color);
        }
    }

    @Override
    public void onContentResized(Widget root) {}
}
