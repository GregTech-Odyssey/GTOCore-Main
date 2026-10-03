package com.gtocore.client.screen.starmap.grid;

import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.common.wireless.energy.map.GridFormat;
import com.gtocore.common.wireless.energy.map.GridMapLang;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

/**
 * 天体与线路的悬停提示：数值缩写与提示行的拼装；调用方按悬停项与修订号缓存结果。
 */
@OnlyIn(Dist.CLIENT)
final class GridTips {

    private static final String[] SUFFIX = { "", "K", "M", "G", "T", "P", "E", "Z", "Y", "R", "Q" };
    private static final String RISE = "▲ ", FALL = "▼ ", MOON_MARK = "· ";

    private GridTips() {}

    static String compact(double value) {
        if (!Double.isFinite(value) || value <= 0) return "0";
        int unit = 0;
        while (value >= 999.5 && unit < SUFFIX.length - 1) {
            value /= 1000;
            unit++;
        }
        int decimals = value >= 99.95 ? 0 : value >= 9.995 ? 1 : 2;
        return BigDecimal.valueOf(value).setScale(unit == 0 ? 0 : decimals, RoundingMode.HALF_UP).toPlainString() + SUFFIX[unit];
    }

    static String compactSigned(double value) {
        if (!Double.isFinite(value) || Math.abs(value) < GridStyle.STEADY) return "0";
        return (value > 0 ? "+" : "-") + compact(Math.abs(value));
    }

    static String arrow(double value) {
        if (!Double.isFinite(value) || Math.abs(value) < GridStyle.STEADY) return "";
        return value > 0 ? RISE : FALL;
    }

    static List<Component> body(StarCatalog catalog, GridModel model, int body, @Nullable Component pair, @Nullable Component route,
                                @Nullable Component footer) {
        var lines = new ObjectArrayList<Component>(7);
        lines.add(GridFormat.bodyName(catalog.dimension(body)).copy().withStyle(ChatFormatting.WHITE));
        int node = model.node(body);
        if (node < 0) {
            lines.add(Component.translatable(GridMapLang.STATE_OUTSIDE).withStyle(ChatFormatting.GRAY));
        } else if (model.isLineOnly(node)) {
            lines.add(Component.translatable(GridMapLang.STATE_LINE_ONLY, GridFormat.tierText(model.info(node).reachTier()))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            var info = model.info(node);
            lines.add(Component.translatable(GridMapLang.STATE_NODE, GridFormat.tierText(info.tier())).withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable(GridMapLang.STORED_DETAIL, GridFormat.amount(model.storage(node)), GridFormat.amount(info.capacity()))
                    .withStyle(ChatFormatting.GRAY));
            float delta = model.storageDelta(node);
            var trend = Math.abs(delta) < GridStyle.STEADY ? ChatFormatting.GRAY : delta > 0 ? ChatFormatting.GREEN : ChatFormatting.GOLD;
            lines.add(Component.translatable(GridMapLang.TIP_DELTA, arrow(delta) + GridFormat.signed(delta)).withStyle(trend));
        }
        if (node >= 0) lines.add(Component.translatable(GridMapLang.LINES, model.degree(node)).withStyle(ChatFormatting.GRAY));
        moons(catalog, model, body, -1, lines);
        if (pair != null) lines.add(pair);
        if (route != null) lines.add(route);
        if (footer != null) lines.add(footer);
        return Collections.unmodifiableList(lines);
    }

    static boolean onAnchor(StarCatalog catalog, int moon, int anchor) {
        return catalog.isMoon(moon) && catalog.parent(moon) < 0 && catalog.system(moon) == catalog.anchorSystem(anchor) &&
                catalog.order(moon) == catalog.anchorOrder(anchor);
    }

    static int anchorMoons(StarCatalog catalog, GridModel model, int anchor) {
        int count = 0;
        for (int moon = 0; moon < catalog.bodyCount(); moon++) {
            if (onAnchor(catalog, moon, anchor) && model.node(moon) >= 0) count++;
        }
        return count;
    }

    static List<Component> anchor(StarCatalog catalog, GridModel model, int anchor) {
        var lines = new ObjectArrayList<Component>(4);
        lines.add(catalog.anchorName(anchor).copy().withStyle(ChatFormatting.WHITE));
        int count = anchorMoons(catalog, model, anchor);
        lines.add(count > 0 ? Component.translatable(GridMapLang.TIP_ANCHOR_MOONS, count).withStyle(ChatFormatting.GRAY) :
                Component.translatable(GridMapLang.STATE_OUTSIDE).withStyle(ChatFormatting.GRAY));
        moons(catalog, model, -1, anchor, lines);
        return Collections.unmodifiableList(lines);
    }

    private static void moons(StarCatalog catalog, GridModel model, int body, int anchor, ObjectArrayList<Component> lines) {
        for (int moon = 0; moon < catalog.bodyCount(); moon++) {
            int node = model.node(moon);
            boolean child = body >= 0 ? catalog.isMoon(moon) && catalog.parent(moon) == body : onAnchor(catalog, moon, anchor);
            if (node < 0 || !child) continue;
            var state = model.isLineOnly(node) ? Component.translatable(GridMapLang.TAG_RELAY_SHORT, GridFormat.tierText(model.info(node).reachTier())) :
                    Component.translatable(GridMapLang.PERCENT, Math.round(model.fill(node) * 100));
            lines.add(Component.literal(MOON_MARK).append(GridFormat.bodyName(catalog.dimension(moon))).append(" ").append(state.withStyle(ChatFormatting.GRAY)));
        }
    }

    static List<Component> line(StarCatalog catalog, GridModel model, int line) {
        var info = model.lineInfo(line);
        var nameA = GridFormat.bodyName(catalog.dimension(model.lineA(line)));
        var nameB = GridFormat.bodyName(catalog.dimension(model.lineB(line)));
        long amps = info.tier() >= 0 && info.tier() < GTValues.V.length && GTValues.V[info.tier()] > 0 ? info.budget() / GTValues.V[info.tier()] : 0;
        var lines = new ObjectArrayList<Component>(6);
        lines.add(Component.translatable(GridMapLang.TIP_LINE_ENDS, nameA, nameB).withStyle(ChatFormatting.WHITE));
        lines.add(Component.translatable(GridMapLang.TIP_LINE_CAPACITY, GridFormat.tierText(info.tier()), GridFormat.compact(amps), GridFormat.amount(info.budget()))
                .withStyle(ChatFormatting.GRAY));
        lines.add(direction(nameA, nameB, model.flowAB(line), info.budget()));
        lines.add(direction(nameB, nameA, model.flowBA(line), info.budget()));
        return Collections.unmodifiableList(lines);
    }

    private static Component direction(Component from, Component to, float flow, long budget) {
        long percent = budget > 0 ? Math.round(Math.min(1, flow / (double) budget) * 100) : 0;
        var style = flow >= GridStyle.STEADY ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY;
        return Component.translatable(GridMapLang.TIP_LINE_DIRECTION, from, to, GridFormat.amount(flow), percent).withStyle(style);
    }
}
