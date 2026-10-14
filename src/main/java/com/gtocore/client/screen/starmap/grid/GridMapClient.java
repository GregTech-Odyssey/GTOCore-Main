package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.BodyLayer;
import com.gtocore.client.screen.starmap.base.PixelPen;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarGeometry;
import com.gtocore.client.screen.starmap.base.StarSky;
import com.gtocore.client.screen.starmap.base.SystemLayer;
import com.gtocore.common.wireless.energy.map.GridMapView;

import com.gregtechceu.gtceu.uipro.animation.UIClock;
import com.gregtechceu.gtceu.uipro.canvas.CanvasItem;
import com.gregtechceu.gtceu.uipro.canvas.CanvasLayer;
import com.gregtechceu.gtceu.uipro.canvas.CanvasPainter;
import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.view.PlanarView;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * 电网星图的客户端场景：用星图底座组装画布各层，持有选中与悬停状态并分发点击（镜头见 {@link GridCamera}）；最底层同时负责背景与每帧的数据对齐。
 */
@OnlyIn(Dist.CLIENT)
public final class GridMapClient implements CanvasLayer {

    private static final int FIT_PADDING = 8, LINE_HOLD = 9;
    private static final long LINE_GRACE_MILLIS = 150;

    private final GridMapView view;
    private final CanvasView canvas;
    private final StarCatalog catalog;
    private final StarGeometry geometry;
    private final GridModel model;
    private final GridPairFlow pairFlow;
    private final GridDecorator decorator;
    private final GridCamera camera;
    private final GridHoverCard hover;
    private final GridEdgeChips edges;
    private final PixelPen pen = new PixelPen();
    private final StarSky sky = new StarSky();
    private final BodyLayer bodies;
    private final GridLinkLayer links;
    private final GridNodeTags tags;
    private final GridObstructions obstructions;
    private final GridSelection selection;
    private int hoveredBody = -1, hoveredAnchor = -1, hoveredLine = -1, cardLine = -1;
    private long lineLostAt;

    private GridMapClient(GridMapView view) {
        this.view = view;
        this.canvas = view.canvas();
        this.catalog = StarCatalog.build(StarCatalog.CatalogFilter.UNREACHABLE);
        this.geometry = new StarGeometry(catalog);
        this.model = new GridModel(view, catalog, geometry);
        this.pairFlow = new GridPairFlow(model, catalog);
        this.selection = new GridSelection(view, catalog, model);
        this.decorator = new GridDecorator(this, catalog, model);
        this.bodies = new BodyLayer(geometry, decorator, pen, canvas);
        this.links = new GridLinkLayer(this);
        this.obstructions = new GridObstructions(view, canvas);
        this.camera = new GridCamera(this, canvas);
        syncModel();
        var systems = new SystemLayer(geometry, decorator, bodies, pen, canvas);
        var guides = new GridGuideLayer(this);
        var particles = new GridParticles(this, links);
        tags = new GridNodeTags(this);
        hover = new GridHoverCard(this);
        edges = new GridEdgeChips(this);
        canvas.setScaleRange(0.02f, 4f);
        canvas.setMinScaleFits(false);
        canvas.setLodThresholds(0, 0);
        canvas.setFitPadding(FIT_PADDING);
        canvas.setInitialView(this::initialView);
        canvas.setOnClientItemClick(this::onItemClick);
        canvas.setOnClientBackgroundClick(this::onBackgroundClick);
        canvas.setScene(scene -> scene.addLayer(this).addLayer(systems).addLayer(guides).addLayer(links).addLayer(particles)
                .addLayer(guides.path()).addLayer(bodies).addLayer(tags).addLayer(edges)
                .addLayer(hover));
        view.setNavigator(camera);
    }

    public static void install(GridMapView view) {
        new GridMapClient(view);
    }

    private void syncModel() {
        if (model.sync()) canvas.invalidateContentBounds();
    }

    private void initialView(PlanarView planar) {
        syncModel();
        camera.initialView(planar);
    }

    GridMapView view() {
        return view;
    }

    StarCatalog catalog() {
        return catalog;
    }

    StarGeometry geometry() {
        return geometry;
    }

    GridModel model() {
        return model;
    }

    GridPairFlow pairFlow() {
        return pairFlow;
    }

    PixelPen pen() {
        return pen;
    }

    PlanarView canvas() {
        return canvas;
    }

    GridObstructions obstructions() {
        return obstructions;
    }

    GridLinkLayer links() {
        return links;
    }

    int hoveredAnchor() {
        return hoveredAnchor;
    }

    int hoveredLine() {
        return hoveredLine;
    }

    GridNodeTags tags() {
        return tags;
    }

    GridSelection selection() {
        return selection;
    }

    GridHoverCard hover() {
        return hover;
    }

    GridDecorator decorator() {
        return decorator;
    }

    BodyLayer bodies() {
        return bodies;
    }

    int selected() {
        return selection.selected();
    }

    int hoveredBody() {
        return hoveredBody;
    }

    boolean isDimmed(int line) {
        int selected = selection.selected();
        return selected >= 0 && !model.touches(line, selected) && !isEmphasized(line);
    }

    boolean isEmphasized(int line) {
        return line == hoveredLine || line == cardLine;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(CanvasPainter painter, @Nullable CanvasItem hovered) {
        syncModel();
        geometry.update(painter.scale());
        if (selection.reconcile()) camera.reveal(selection.selected());
        hover(painter, hovered);
        int line = view.hoveredCardLine();
        cardLine = line >= 0 && line < model.lineCount() && model.isPlaced(line) ? line : -1;
        pairFlow.update(selection.selected(), hoveredBody);
        var gui = canvas.getGui();
        camera.pan(gui == null ? null : gui.getModularUIGui());
        painter.flush();
        obstructions.refresh();
        hover.prepare(painter);
        pen.begin(painter.graphics(), canvas);
        sky.draw(pen.graphics(), canvas.viewportX(), canvas.viewportY(), canvas.viewportWidth(), canvas.viewportHeight(), canvas);
        pen.end();
    }

    private void hover(CanvasPainter painter, @Nullable CanvasItem hovered) {
        boolean inside = !Float.isNaN(painter.mouseX());
        if (hovered == null && inside && (hoveredBody >= 0 || hoveredAnchor >= 0)) {
            int x = Math.round(canvas.viewportX() + (painter.mouseX() - canvas.offsetX()) * canvas.scale());
            int y = Math.round(canvas.viewportY() + (painter.mouseY() - canvas.offsetY()) * canvas.scale());
            if (this.hover.holds(x, y)) return;
        }
        if (hovered == null && inside && hoveredLine >= 0 && holdLine(painter)) return;
        hoveredBody = bodies.bodyOf(hovered);
        hoveredAnchor = bodies.anchorOf(hovered);
        hoveredLine = links.lineOf(hovered);
        lineLostAt = 0;
    }

    private boolean holdLine(CanvasPainter painter) {
        if (links.screenDistance(hoveredLine, painter.mouseX(), painter.mouseY()) <= LINE_HOLD) {
            lineLostAt = 0;
            return true;
        }
        long now = UIClock.millis();
        if (lineLostAt == 0) lineLostAt = now;
        return now - lineLostAt < LINE_GRACE_MILLIS;
    }

    private void onItemClick(CanvasItem item, int button, float worldX, float worldY) {
        if (button != 0) return;
        int edge = edges.edgeOf(item);
        if (edge >= 0) {
            if (!decorator.isPicking()) selection.open(edge);
            camera.reveal(edge);
            return;
        }
        int body = bodies.bodyOf(item);
        if (body >= 0) {
            click(body);
            return;
        }
        int anchor = bodies.anchorOf(item);
        if (anchor >= 0) {
            camera.fitAnchor(anchor);
            return;
        }
        int line = links.lineOf(item);
        if (line >= 0) camera.focusLine(line);
    }

    private void onBackgroundClick(float worldX, float worldY, int button) {
        if (button == 0 && !decorator.isPicking()) selection.clear();
    }

    private void click(int body) {
        if (decorator.isPicking()) {
            int dimRef = decorator.isPickable(body) ? GridView.dimRef(catalog.dimension(body)) : 0;
            if (dimRef > 0) view.sendPick(dimRef);
            return;
        }
        if (selection.toggle(body)) camera.reveal(body);
    }
}
