package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.common.wireless.energy.map.GridMapView;

import com.gregtechceu.gtceu.uipro.animation.UIClock;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 选中天体与星球卡片联动：点击时立即选中并请求卡片，之后以卡片的实际参数为准；卡片关闭（×、Esc、服务端拒绝）即取消选中，
 * 卡片换成画布以外发起的天体时报告给调用方移动镜头。
 */
@OnlyIn(Dist.CLIENT)
final class GridSelection {

    private static final int NO_REQUEST = Integer.MIN_VALUE;
    private static final long PENDING_MILLIS = 1000;

    private final GridMapView view;
    private final StarCatalog catalog;
    private final GridModel model;
    private int selected = -1, requested = NO_REQUEST, shownArgument = -1, shownTopology = -1;
    private long requestedAt;

    GridSelection(GridMapView view, StarCatalog catalog, GridModel model) {
        this.view = view;
        this.catalog = catalog;
        this.model = model;
    }

    int selected() {
        return selected;
    }

    boolean reconcile() {
        var card = view.card();
        if (!card.isOpenOrRequested()) {
            selected = -1;
            requested = NO_REQUEST;
            shownArgument = -1;
            return false;
        }
        int argument = card.getArgument();
        boolean clicked = false;
        if (requested != NO_REQUEST) {
            if (argument != requested && UIClock.millis() - requestedAt < PENDING_MILLIS) return false;
            clicked = argument == requested;
            requested = NO_REQUEST;
            shownArgument = -1;
        }
        if (argument < 0 || argument == shownArgument && shownTopology == model.topologyStamp()) return false;
        boolean moved = !clicked && argument != shownArgument;
        shownArgument = argument;
        shownTopology = model.topologyStamp();
        selected = bodyOf(argument);
        return moved && selected >= 0;
    }

    boolean toggle(int body) {
        int argument = argumentOf(body);
        if (argument < 0) return false;
        if (view.card().toggle(argument)) {
            selected = body;
            requested = argument;
            requestedAt = UIClock.millis();
            return true;
        }
        selected = -1;
        requested = NO_REQUEST;
        return false;
    }

    boolean open(int body) {
        return body >= 0 && (selected == body || toggle(body));
    }

    void clear() {
        view.card().close();
    }

    private int bodyOf(int argument) {
        int node = GridMapView.cardNode(argument);
        if (node >= 0) return model.body(node);
        var dimension = GridView.dimension(GridMapView.cardDimRef(argument));
        return dimension == null ? -1 : catalog.indexOf(dimension);
    }

    private int argumentOf(int body) {
        int dimRef = GridView.dimRef(catalog.dimension(body));
        if (dimRef > 0) return GridMapView.dimensionCard(dimRef);
        return GridMapView.nodeCard(model.node(body));
    }
}
