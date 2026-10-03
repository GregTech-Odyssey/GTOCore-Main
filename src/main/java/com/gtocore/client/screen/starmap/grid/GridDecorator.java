package com.gtocore.client.screen.starmap.grid;

import com.gtocore.api.wireless.energy.GridView;
import com.gtocore.client.screen.starmap.base.BodyDecorator;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarSprites;
import com.gtocore.common.wireless.energy.RelayTargets;
import com.gtocore.common.wireless.energy.map.GridMapLang;
import com.gtocore.common.wireless.energy.map.GridMapMode;

import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 电网星图的天体装饰：未入网（选目标时为不可选）的天体隐去贴图（由线路层画暗框）、名称转灰；轨道统一用远景色且不点亮；
 * 悬停浮卡的内容按悬停天体与修订号缓存，不再交给原生提示框。
 */
@OnlyIn(Dist.CLIENT)
final class GridDecorator implements BodyDecorator {

    private static final int HIDDEN = 0x00FFFFFF, DIM = 0xFF5C6070;

    private final GridMapClient client;
    private final StarCatalog catalog;
    private final GridModel model;
    private final boolean picking;
    private boolean[] pickable = new boolean[0];
    private int pickRevision = -1, self = -1;
    private List<Component> tip = Collections.emptyList();
    private int tipBody = -1, tipStamp = -1, tipPair = -1, tipSelected = -2;
    private List<Component> anchorTip = Collections.emptyList();
    private int anchorTipIndex = -1, anchorTipStamp = -1, anchorRevision = -1;
    private boolean[] anchorLit = new boolean[0];

    GridDecorator(GridMapClient client, StarCatalog catalog, GridModel model) {
        this.client = client;
        this.catalog = catalog;
        this.model = model;
        this.picking = client.view().mode() == GridMapMode.PICK;
    }

    boolean lit(int body) {
        return picking ? isPickable(body) : model.node(body) >= 0;
    }

    @Override
    public int tint(int body) {
        if (lit(body)) return StarSprites.NO_TINT;
        return client.canvas().scale() >= GridStyle.SPRITE_SCALE ? DIM : HIDDEN;
    }

    boolean anchorLit(int anchor) {
        if (anchorRevision != model.topologyStamp() || anchorLit.length != catalog.anchorCount()) {
            anchorRevision = model.topologyStamp();
            anchorLit = new boolean[catalog.anchorCount()];
            for (int a = 0; a < anchorLit.length; a++) anchorLit[a] = GridTips.anchorMoons(catalog, model, a) > 0;
        }
        return anchor >= 0 && anchor < anchorLit.length && anchorLit[anchor];
    }

    public int anchorTint(int anchor) {
        if (picking || anchorLit(anchor)) return StarSprites.NO_TINT;
        return client.canvas().scale() >= GridStyle.SPRITE_SCALE ? DIM : HIDDEN;
    }

    public int anchorLabelColor(int anchor, int fallback) {
        return picking || anchorLit(anchor) ? fallback : UITheme.MAP_LABEL_DIM;
    }

    @Override
    public int labelColor(int body, int fallback) {
        return lit(body) ? fallback : UITheme.MAP_LABEL_DIM;
    }

    @Override
    public boolean showsLabel(int body) {
        return !client.tags().isNamed(body);
    }

    @Override
    public boolean litOrbits() {
        return false;
    }

    @Override
    public boolean showsHereMarker(int body) {
        return false;
    }

    @Override
    public int orbitColor(int system, int order, int fallback) {
        return UITheme.MAP_ORBIT_FAR;
    }

    @Override
    public boolean isPickable(int body) {
        if (!picking) return true;
        ensurePickable();
        return body >= 0 && body < pickable.length && pickable[body];
    }

    boolean isPicking() {
        return picking;
    }

    private void ensurePickable() {
        if (pickRevision == catalog.revision() && pickable.length == catalog.size()) return;
        pickRevision = catalog.revision();
        pickable = new boolean[catalog.size()];
        var focus = GridView.dimension(client.view().focusDimRef());
        self = catalog.indexOf(focus);
        if (focus == null) return;
        var options = RelayTargets.options(focus);
        for (int i = 0; i < pickable.length; i++) {
            var dimension = catalog.dimension(i);
            pickable[i] = i != self && dimension != null && options.contains(dimension);
        }
    }

    List<Component> anchorLines(int anchor) {
        if (anchor != anchorTipIndex || model.stamp() != anchorTipStamp) {
            anchorTipIndex = anchor;
            anchorTipStamp = model.stamp();
            anchorTip = GridTips.anchor(catalog, model, anchor);
        }
        return anchorTip;
    }

    List<Component> hoverLines(int body) {
        int selected = client.selected();
        client.pairFlow().update(selected, body);
        int pair = client.pairFlow().stamp();
        if (body == tipBody && model.stamp() == tipStamp && pair == tipPair && selected == tipSelected) return tip;
        tipBody = body;
        tipStamp = model.stamp();
        tipPair = pair;
        tipSelected = selected;
        tip = GridTips.body(catalog, model, body, client.pairFlow().tip(body), client.pairFlow().route(body), footer(body, selected));
        return tip;
    }

    @Nullable
    private Component footer(int body, int selected) {
        if (picking) {
            if (isPickable(body)) return Component.translatable(GridMapLang.TIP_PICK).withStyle(ChatFormatting.GREEN);
            String key = body == self ? GridMapLang.TIP_PICK_SELF : GridMapLang.TIP_PICK_INVALID;
            return Component.translatable(key).withStyle(ChatFormatting.RED);
        }
        return body == selected ? null : Component.translatable(GridMapLang.TIP_DETAILS).withStyle(ChatFormatting.DARK_GRAY);
    }
}
