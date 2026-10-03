package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridFlow;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.common.wireless.energy.map.GridFormat;
import com.gtocore.common.wireless.energy.map.GridMapLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

/**
 * 选中天体与悬停天体之间经线路的最大输送：只在两端或拓扑变化时调用 {@link GridFlow} 重算，结果与提示行缓存。
 */
@OnlyIn(Dist.CLIENT)
final class GridPairFlow {

    private final GridModel model;
    private final StarCatalog catalog;
    private int from = -1, to = -1, topologyStamp = -1, stamp;
    @Nullable
    private GridFlow.Result result;
    @Nullable
    private Component tip, route;

    GridPairFlow(GridModel model, StarCatalog catalog) {
        this.model = model;
        this.catalog = catalog;
    }

    void update(int selectedBody, int hoveredBody) {
        int target = selectedBody >= 0 && hoveredBody >= 0 && hoveredBody != selectedBody ? hoveredBody : -1;
        int source = target >= 0 ? selectedBody : -1;
        if (source == from && target == to && topologyStamp == model.topologyStamp()) return;
        from = source;
        to = target;
        topologyStamp = model.topologyStamp();
        stamp++;
        result = null;
        tip = null;
        route = null;
        if (target < 0) return;
        int a = model.node(source), b = model.node(target);
        if (a >= 0 && b >= 0) result = GridFlow.solve(model.topology(), a, b);
        if (result == null || !result.reachable() || result.flow() <= 0) {
            tip = Component.translatable(GridMapLang.TIP_UNREACHABLE).withStyle(ChatFormatting.GRAY);
            return;
        }
        var amount = Component.literal(GridFormat.amount(result.flow())).withStyle(ChatFormatting.AQUA);
        tip = Component.translatable(GridMapLang.TIP_PAIR_MAX, name(source), name(target), amount);
        route = Component.translatable(GridMapLang.TIP_PAIR_ROUTE, result.usedLines(), GridFormat.tierText(result.tier())).withStyle(ChatFormatting.GRAY);
    }

    private Component name(int body) {
        return GridFormat.bodyName(catalog.dimension(body));
    }

    int stamp() {
        return stamp;
    }

    boolean isActive() {
        return result != null && result.reachable() && result.flow() > 0;
    }

    boolean carries(int line) {
        var current = result;
        return current != null && line >= 0 && line < current.lineFlow().length && current.lineFlow()[line] != 0;
    }

    @Nullable
    Component tip(int body) {
        return body == to ? tip : null;
    }

    @Nullable
    Component route(int body) {
        return body == to ? route : null;
    }
}
