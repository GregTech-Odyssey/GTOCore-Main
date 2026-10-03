package com.gtocore.client.screen.starmap.base;

import com.gregtechceu.gtceu.uipro.UIElement;
import com.gregtechceu.gtceu.uipro.animation.UIClock;
import com.gregtechceu.gtceu.uipro.elements.Button;
import com.gregtechceu.gtceu.uipro.styletemplate.UISizes;
import com.gregtechceu.gtceu.uipro.view.PlanarView;
import com.gregtechceu.gtceu.uipro.view.ZoomBar;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * 星图导航：镜头对准星系或天体、底部星系按钮行（当前所看星系高亮）、WASD 与方向键平移。
 */
@OnlyIn(Dist.CLIENT)
public final class StarNavigator {

    public static final float VIEW_SCALE = 0.8f;
    public static final int NAV_BUTTON_WIDTH = 64;
    public static final int FIT_PADDING = 8;
    private static final int DOCK_ROOM = 28;
    private static final float PAN_SPEED = 260;
    private static final float ANCHOR_ROOM = 40;
    private static final float ANCHOR_MAX_SCALE = 3f;
    private static final float VIEWED_WIDTH = 0.6f;

    private final PlanarView canvas;
    private final StarGeometry geometry;
    private final StarCatalog catalog;
    private final float[] box = new float[4];
    private long lastPan;

    public StarNavigator(PlanarView canvas, StarGeometry geometry) {
        this.canvas = canvas;
        this.geometry = geometry;
        this.catalog = geometry.catalog();
    }

    public void look(float worldX, float worldY, boolean animated) {
        canvas.setView(worldX - canvas.getSizeWidth() / (2 * VIEW_SCALE), worldY - (canvas.getSizeHeight() - DOCK_ROOM) / (2 * VIEW_SCALE), VIEW_SCALE, animated);
    }

    public void lookAtSystem(int system, boolean animated) {
        look(catalog.systemX(system), catalog.systemY(system), animated);
    }

    public void locate(int body, boolean animated) {
        geometry.update(VIEW_SCALE);
        look(geometry.x(body), geometry.y(body), animated);
    }

    public void initialView(PlanarView view) {
        if (catalog.current() >= 0) locate(catalog.current(), false);
        else if (catalog.focusSystem() >= 0) lookAtSystem(catalog.focusSystem(), false);
        else view.fitContent(false);
    }

    public void reveal(int body) {
        geometry.update(canvas.scale());
        canvas.reveal(geometry.rect(body).inflate(UISizes.SLOT_SIZE), true);
    }

    public void fitAnchor(int anchor) {
        geometry.update(canvas.scale());
        canvas.fit(geometry.anchorRect(anchor).inflate(ANCHOR_ROOM / canvas.scale()), FIT_PADDING, ANCHOR_MAX_SCALE, true);
    }

    public int viewedSystem() {
        float scale = canvas.scale();
        float centerX = canvas.offsetX() + canvas.getSizeWidth() / 2f / scale, centerY = canvas.offsetY() + canvas.getSizeHeight() / 2f / scale;
        float viewWidth = canvas.getSizeWidth() / scale;
        for (int s = 0; s < catalog.systemCount(); s++) {
            catalog.systemBounds(s, box);
            if (box[2] >= viewWidth * VIEWED_WIDTH && centerX >= box[0] && centerX <= box[0] + box[2] && centerY >= box[1] && centerY <= box[1] + box[3]) return s;
        }
        return -1;
    }

    public UIElement systemButtons(BodyDecorator decorator) {
        var row = ZoomBar.dockRow();
        for (int s = 0; s < catalog.systemCount(); s++) {
            int system = s;
            var button = Button.text(NAV_BUTTON_WIDTH, UISizes.DOCK_BUTTON_SIZE, catalog.systemName(system)).setOnClientClick(() -> lookAtSystem(system, true));
            button.setSelected(() -> viewedSystem() == system);
            var subtitle = decorator.systemSubtitle(system);
            if (subtitle != null) button.tooltips(subtitle);
            row.addChild(button);
        }
        return row;
    }

    public void pan(@Nullable Screen screen) {
        long now = UIClock.millis();
        float seconds = lastPan == 0 ? 0 : Math.min(0.1f, (now - lastPan) / 1000f);
        lastPan = now;
        var minecraft = Minecraft.getInstance();
        if (seconds <= 0 || screen == null || minecraft.screen != screen) return;
        var options = minecraft.options;
        int dx = (held(options.keyRight, GLFW.GLFW_KEY_RIGHT) ? 1 : 0) - (held(options.keyLeft, GLFW.GLFW_KEY_LEFT) ? 1 : 0);
        int dy = (held(options.keyDown, GLFW.GLFW_KEY_DOWN) ? 1 : 0) - (held(options.keyUp, GLFW.GLFW_KEY_UP) ? 1 : 0);
        if (dx == 0 && dy == 0) return;
        float speed = PAN_SPEED * seconds / canvas.scale() * (Screen.hasShiftDown() ? 2.5f : 1);
        canvas.panBy(dx * speed, dy * speed);
    }

    private static boolean held(KeyMapping mapping, int arrow) {
        long window = Minecraft.getInstance().getWindow().getWindow();
        var key = mapping.getKey();
        return InputConstants.isKeyDown(window, arrow) ||
                key.getType() == InputConstants.Type.KEYSYM && key.getValue() != InputConstants.UNKNOWN.getValue() && InputConstants.isKeyDown(window, key.getValue());
    }
}
