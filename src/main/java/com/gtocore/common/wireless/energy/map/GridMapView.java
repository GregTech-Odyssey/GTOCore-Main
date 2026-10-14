package com.gtocore.common.wireless.energy.map;

import com.gtocore.api.wireless.energy.GridView;

import com.gregtechceu.gtceu.uipro.canvas.CanvasView;
import com.gregtechceu.gtceu.uipro.window.CardHost;

import org.jetbrains.annotations.Nullable;

/**
 * 电网星图界面交给客户端绘制层的视图：同步到的快照、本地开关、画布与星球卡片，以及发给服务端的请求。
 */
public interface GridMapView {

    static int nodeCard(int nodeIndex) {
        return nodeIndex >= 0 && nodeIndex < GridView.MAX_NODES ? nodeIndex : -1;
    }

    static int dimensionCard(int dimRef) {
        return dimRef > 0 ? GridView.MAX_NODES + dimRef : -1;
    }

    static int cardNode(int argument) {
        return argument >= 0 && argument < GridView.MAX_NODES ? argument : -1;
    }

    static int cardDimRef(int argument) {
        return argument > GridView.MAX_NODES ? argument - GridView.MAX_NODES : 0;
    }

    GridView.TopologyView topology();

    GridView.LiveView live();

    GridMapToggles toggles();

    CanvasView canvas();

    CardHost card();

    GridMapMode mode();

    int focusDimRef();

    void sendPick(int dimRef);

    void sendBack();

    void setNavigator(@Nullable GridMapNavigator navigator);

    int hoveredCardLine();
}
