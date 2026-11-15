package com.gtocore.integration.xaero;

import com.gtolib.monitor.HudBoard;
import com.gtolib.monitor.HudPanels;
import com.gtolib.monitor.client.HudPanelClient;
import com.gtolib.monitor.client.MonitorHudClient;
import com.gtolib.monitor.client.MonitorHudEditorClient;
import com.gtolib.monitor.client.MonitorHudScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.render.module.ModuleRenderContext;

/**
 * Positions Xaero's native render pass with the implant HUD layout, preserving its framebuffer and shader lifecycle.
 */
@OnlyIn(Dist.CLIENT)
public final class XaeroMinimapHud implements HudPanelClient {

    public static final String ID = "xaero.minimap";
    public static final XaeroMinimapHud INSTANCE = new XaeroMinimapHud();
    private static final Rect2i BOUNDS = new Rect2i(0, 0, 0, 0);
    @Nullable
    private static HudBoard layout;
    @Nullable
    private static HudBoard.Entry placed;
    private static float renderScale;

    private XaeroMinimapHud() {}

    public static void install() {
        MonitorHudClient.registerPanel(ID, INSTANCE);
        XaeroMinimapPreview.install();
    }

    @Nullable
    private static HudBoard.Entry placement() {
        var board = MonitorHudClient.board();
        if (layout != board) {
            layout = board;
            placed = null;
            for (var entry : board.cards) {
                if (HudPanels.isPanel(entry.card()) && ID.equals(entry.card().channel())) {
                    placed = entry;
                    break;
                }
            }
        }
        return placed;
    }

    public static boolean begin(MinimapSession session, ModuleRenderContext context, GuiGraphics graphics) {
        var mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.options.renderDebug || !MonitorHudClient.visible(mc)) return false;
        var entry = placement();
        if (entry == null) return false;
        boolean editing = XaeroMinimapPreview.editing();
        if (entry.interactionOnly() && !(mc.screen instanceof MonitorHudScreen) && !editing) return false;
        BOUNDS.setPosition(entry.left(context.screenWidth), entry.top(context.screenHeight));
        BOUNDS.setWidth(entry.width());
        BOUNDS.setHeight(entry.height());
        if (editing) MonitorHudEditorClient.previewBounds(entry.card().id(), BOUNDS);
        int side = Math.min(BOUNDS.getWidth(), BOUNDS.getHeight());
        context.x = BOUNDS.getX() + (BOUNDS.getWidth() - side) / 2;
        context.y = BOUNDS.getY();
        context.w = side;
        context.h = side;
        context.flippedHorizontally = false;
        context.flippedVertically = false;
        renderScale = (float) (side * context.screenScale / session.getConfiguredWidth());
        if (editing) XaeroMinimapPreview.begin(graphics, BOUNDS, context.screenWidth, context.screenHeight);
        return true;
    }

    public static void end(GuiGraphics graphics) {
        XaeroMinimapPreview.end(graphics);
        renderScale = 0;
    }

    public static boolean rendering() {
        return renderScale > 0;
    }

    public static float scale() {
        return renderScale;
    }

    public static boolean hideUnderScreen(boolean hide) {
        return hide && !(rendering() && (Minecraft.getInstance().screen instanceof MonitorHudScreen || XaeroMinimapPreview.capturing()));
    }

    @Override
    public boolean available() {
        var session = BuiltInHudModules.MINIMAP.getCurrentSession();
        return session != null && session.isActive();
    }

    @Override
    public void draw(GuiGraphics graphics, Rect2i rect, int mouseX, int mouseY, float partialTick, boolean interactive) {
        // Xaero owns the draw pass; the main-module mixins apply this panel's saved placement there.
    }

    @Override
    public boolean drawPreview(GuiGraphics graphics, Rect2i rect) {
        return XaeroMinimapPreview.draw(graphics, rect);
    }
}
