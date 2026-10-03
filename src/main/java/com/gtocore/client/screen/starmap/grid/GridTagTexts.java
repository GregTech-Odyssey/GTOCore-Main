package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.common.wireless.energy.map.GridFormat;
import com.gtocore.common.wireless.energy.map.GridMapLang;

import com.gregtechceu.gtceu.uipro.render.UIText;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

/**
 * 信息卡文字与宽度：按节点在数据修订号变化后第一次被画到时重建，绘制只读数组。
 */
@OnlyIn(Dist.CLIENT)
final class GridTagTexts {

    static final Component RISE = Component.literal("▲");
    static final Component FALL = Component.literal("▼");
    private static final String STEADY_RATE = "0";
    private static final String SEPARATOR = " · ";
    private static final String BADGE = "+";

    private final GridModel model;
    private final StarCatalog catalog;
    private final int[] stamp = new int[GridView.MAX_NODES];
    final Component[] name = new Component[GridView.MAX_NODES];
    final Component[] stored = new Component[GridView.MAX_NODES];
    final Component[] delta = new Component[GridView.MAX_NODES];
    final Component[] percent = new Component[GridView.MAX_NODES];
    final Component[] tier = new Component[GridView.MAX_NODES];
    final Component[] relay = new Component[GridView.MAX_NODES];
    final Component[] relayShort = new Component[GridView.MAX_NODES];
    final Component[] summary = new Component[GridView.MAX_NODES];
    final Component[] badge = new Component[GridView.MAX_NODES];
    final int[] nameWidth = new int[GridView.MAX_NODES];
    final int[] storedWidth = new int[GridView.MAX_NODES];
    final int[] deltaWidth = new int[GridView.MAX_NODES];
    final int[] percentWidth = new int[GridView.MAX_NODES];
    final int[] tierWidth = new int[GridView.MAX_NODES];
    final int[] relayWidth = new int[GridView.MAX_NODES];
    final int[] relayShortWidth = new int[GridView.MAX_NODES];
    final int[] summaryWidth = new int[GridView.MAX_NODES];
    final int[] badgeWidth = new int[GridView.MAX_NODES];
    final int[] trend = new int[GridView.MAX_NODES];
    final boolean[] lineOnly = new boolean[GridView.MAX_NODES];
    private Component[] parentTitles = new Component[0];
    private int[] parentWidths = new int[0], parentStamps = new int[0];
    private int glyphWidth = -1, glyphGeneration;

    GridTagTexts(GridModel model, StarCatalog catalog) {
        this.model = model;
        this.catalog = catalog;
        Arrays.fill(stamp, Integer.MIN_VALUE);
    }

    int glyphWidth() {
        if (glyphWidth < 0 || glyphGeneration != UIText.generation()) {
            glyphWidth = Math.max(UIText.width(RISE), UIText.width(FALL));
            glyphGeneration = UIText.generation();
        }
        return glyphWidth;
    }

    void ensure(int node) {
        int key = model.stamp() * 31 + UIText.generation();
        if (stamp[node] == key) return;
        stamp[node] = key;
        var info = model.info(node);
        name[node] = GridFormat.bodyName(catalog.dimension(model.body(node)));
        nameWidth[node] = UIText.width(name[node]);
        int moons = groupedMoons(model.body(node));
        badge[node] = moons > 0 ? Component.literal(BADGE + moons) : null;
        badgeWidth[node] = moons > 0 ? UIText.width(badge[node]) : 0;
        lineOnly[node] = model.isLineOnly(node);
        if (lineOnly[node]) {
            relay[node] = Component.translatable(GridMapLang.TAG_RELAY, GridFormat.tierText(info.reachTier()));
            relayWidth[node] = UIText.width(relay[node]);
            relayShort[node] = Component.translatable(GridMapLang.TAG_RELAY_SHORT, GridFormat.tierText(info.reachTier()));
            relayShortWidth[node] = UIText.width(relayShort[node]);
            return;
        }
        rebuildStorage(node, info.tier());
    }

    Component parentTitle(int owner) {
        int size = catalog.size() + catalog.anchorCount();
        if (parentTitles.length != size) {
            parentTitles = new Component[size];
            parentWidths = new int[size];
            parentStamps = new int[size];
            Arrays.fill(parentStamps, Integer.MIN_VALUE);
        }
        int key = model.topologyStamp() * 31 + UIText.generation();
        if (parentStamps[owner] != key) {
            parentStamps[owner] = key;
            boolean anchor = owner >= catalog.size();
            int moons = anchor ? GridTips.anchorMoons(catalog, model, owner - catalog.size()) : groupedMoons(owner);
            var name = anchor ? catalog.anchorName(owner - catalog.size()) : GridFormat.bodyName(catalog.dimension(owner));
            parentTitles[owner] = moons > 0 ? name.copy().append(" " + BADGE + moons) : null;
            parentWidths[owner] = moons > 0 ? UIText.width(parentTitles[owner]) : 0;
        }
        return parentTitles[owner];
    }

    int parentWidth(int owner) {
        return parentWidths[owner];
    }

    int groupedMoons(int body) {
        int count = 0;
        for (int moon = 0; moon < catalog.bodyCount(); moon++) {
            if (catalog.isMoon(moon) && catalog.parent(moon) == body && model.node(moon) >= 0) count++;
        }
        return count;
    }

    private void rebuildStorage(int node, int tierIndex) {
        float change = model.storageDelta(node);
        int direction = Math.abs(change) < GridStyle.STEADY ? 0 : change > 0 ? 1 : -1;
        trend[node] = direction;
        stored[node] = Component.translatable(GridMapLang.TAG_STORED, GridTips.compact(model.storage(node)));
        storedWidth[node] = UIText.width(stored[node]);
        var rate = Component.translatable(GridMapLang.RATE, direction == 0 ? STEADY_RATE : GridTips.compactSigned(change));
        delta[node] = direction == 0 ? rate : (direction > 0 ? RISE : FALL).copy().append(" ").append(rate);
        deltaWidth[node] = UIText.width(delta[node]);
        percent[node] = Component.translatable(GridMapLang.PERCENT, Math.round(model.fill(node) * 100));
        percentWidth[node] = UIText.width(percent[node]);
        summary[node] = percent[node].copy().append(SEPARATOR).append(stored[node]);
        summaryWidth[node] = UIText.width(summary[node]);
        tier[node] = GridFormat.tierText(tierIndex);
        tierWidth[node] = UIText.width(tier[node]);
    }
}
