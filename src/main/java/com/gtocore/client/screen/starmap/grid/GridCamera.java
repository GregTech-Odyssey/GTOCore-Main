package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarGeometry;
import com.gtocore.client.screen.starmap.base.StarNavigator;
import com.gtocore.common.wireless.energy.map.GridMapNavigator;

import com.gtolib.api.data.Galaxy;

import com.gregtechceu.gtceu.uipro.canvas.CanvasRect;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.view.PlanarView;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * 电网星图的镜头：定位、星系切换、对准线路与节点；取景中心按扣掉汇总面板与弹框后的可用区计算，折叠的卫星先放大到展开档再居中。
 */
@OnlyIn(Dist.CLIENT)
final class GridCamera implements GridMapNavigator {

    private static final int DOCK_ROOM = 28;

    private final GridMapClient client;
    private final CanvasView canvas;
    private final StarCatalog catalog;
    private final StarGeometry geometry;
    private final GridModel model;
    private final StarNavigator navigator;
    private final Reference2IntOpenHashMap<Galaxy> galaxySystems = new Reference2IntOpenHashMap<>();

    GridCamera(GridMapClient client, CanvasView canvas) {
        this.client = client;
        this.canvas = canvas;
        this.catalog = client.catalog();
        this.geometry = client.geometry();
        this.model = client.model();
        this.navigator = new StarNavigator(canvas, geometry);
        galaxySystems.defaultReturnValue(-1);
        for (var galaxy : Galaxy.all()) {
            if (galaxy == Galaxy.NONE) continue;
            for (int s = 0; s < catalog.systemCount(); s++) {
                if (catalog.systemName(s).getContents() instanceof TranslatableContents contents && contents.getKey().equals(galaxy.getTranslationKey())) {
                    galaxySystems.put(galaxy, s);
                }
            }
        }
    }

    void look(float worldX, float worldY, boolean animated) {
        float scale = StarNavigator.VIEW_SCALE, center = canvas.unobstructedLeft() + canvas.unobstructedWidth() / 2f;
        canvas.setView(worldX - center / scale, worldY - (canvas.viewportHeight() - DOCK_ROOM) / (2 * scale), scale, animated);
    }

    void locate(int body, boolean animated) {
        if (body < 0) return;
        geometry.update(StarNavigator.VIEW_SCALE);
        look(geometry.x(body), geometry.y(body), animated);
    }

    void reveal(int body) {
        if (body < 0) return;
        if (geometry.isFolded(body)) locate(body, true);
        else navigator.reveal(body);
    }

    void initialView(PlanarView planar) {
        var focus = GridView.dimension(client.view().focusDimRef());
        int body = focus == null ? -1 : catalog.indexOf(focus);
        if (body < 0) body = catalog.current();
        if (body >= 0) locate(body, false);
        else if (catalog.focusSystem() >= 0) look(catalog.systemX(catalog.focusSystem()), catalog.systemY(catalog.focusSystem()), false);
        else planar.fitContent(false);
    }

    void fitAnchor(int anchor) {
        navigator.fitAnchor(anchor);
    }

    void pan(@Nullable Screen screen) {
        navigator.pan(screen);
    }

    @Override
    public boolean hasGalaxy(Galaxy galaxy) {
        return galaxySystems.getInt(galaxy) >= 0;
    }

    @Override
    public boolean isGalaxyShown(Galaxy galaxy) {
        int system = galaxySystems.getInt(galaxy);
        return system >= 0 && navigator.viewedSystem() == system;
    }

    @Override
    public void showGalaxy(Galaxy galaxy) {
        int system = galaxySystems.getInt(galaxy);
        if (system >= 0) look(catalog.systemX(system), catalog.systemY(system), true);
    }

    @Override
    public void locateCurrent() {
        if (catalog.current() >= 0) locate(catalog.current(), true);
        else canvas.fitContent(true);
    }

    @Override
    public void focusLine(int line) {
        if (line < 0 || line >= model.lineCount() || !model.isPlaced(line)) return;
        geometry.update(canvas.scale());
        int a = model.lineA(line), b = model.lineB(line);
        float x0 = Math.min(model.worldX(a), model.worldX(b)), y0 = Math.min(model.worldY(a), model.worldY(b));
        float x1 = Math.max(model.worldX(a), model.worldX(b)), y1 = Math.max(model.worldY(a), model.worldY(b));
        canvas.focus(CanvasRect.of(x0, y0, Math.max(1, x1 - x0), Math.max(1, y1 - y0)), true);
    }

    @Override
    public void focusNode(int node) {
        int body = model.body(node);
        if (!client.decorator().isPicking() && client.selection().open(body)) reveal(body);
    }
}
