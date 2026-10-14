package com.gtocore.client.screen.starmap.rocket;

import com.gtocore.client.screen.starmap.base.BodyDecorator;
import com.gtocore.client.screen.starmap.base.StarCatalog;
import com.gtocore.client.screen.starmap.base.StarSprites;

import com.gtolib.api.misc.PlanetManagement;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.uipro.styletemplate.UITheme;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import earth.terrarium.adastra.common.menus.PlanetsMenu;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * 火箭星图的天体装饰：火箭够不着或未解锁的星球变暗，标出锁与空间站，提示所需火箭等级；提示文字按解锁状态缓存。
 */
@OnlyIn(Dist.CLIENT)
final class RocketDecorator implements BodyDecorator {

    private static final int DIM_TINT = 0xFF73737E;

    private final StarCatalog catalog;
    private final IntSupplier selected;
    private final int rocketTier;
    private final int[] requiredTier;
    private final int[] stations;
    private final Component[] subtitles;
    private final Tip[] tips;
    private final ObjectArrayList<List<Component>> anchorTips = new ObjectArrayList<>();

    RocketDecorator(StarCatalog catalog, PlanetsMenu menu, IntSupplier selected) {
        this.catalog = catalog;
        this.selected = selected;
        this.rocketTier = menu.tier();
        var here = menu.player().level().dimension();
        requiredTier = new int[catalog.bodyCount()];
        stations = new int[catalog.bodyCount()];
        for (int i = 0; i < catalog.bodyCount(); i++) {
            var planet = catalog.planet(i);
            var dimension = catalog.dimension(i);
            if (planet == null || dimension == null) continue;
            requiredTier[i] = PlanetManagement.requiredTier(planet, here);
            PlanetManagement.checkPlanetIsUnlocked(dimension);
            var orbit = catalog.orbit(i);
            stations[i] = orbit == null ? 0 : menu.getOwnedAndTeamSpaceStations(orbit).size();
        }
        subtitles = new Component[catalog.systemCount()];
        for (int s = 0; s < subtitles.length; s++) subtitles[s] = Component.translatable(RocketLang.REACHABLE, reachableCount(s), catalog.systemBodyCount(s));
        tips = new Tip[catalog.size()];
        for (int a = 0; a < catalog.anchorCount(); a++) {
            anchorTips.add(List.of(catalog.anchorName(a).copy().withStyle(ChatFormatting.WHITE),
                    Component.translatable(RocketLang.MOONS_ONLY).withStyle(ChatFormatting.GRAY)));
        }
    }

    int rocketTier() {
        return rocketTier;
    }

    int requiredTier(int body) {
        return requiredTier[body];
    }

    boolean reachable(int body) {
        return !catalog.isRealm(body) && rocketTier >= requiredTier[body];
    }

    boolean unlocked(int body) {
        return GTCEu.isDev() || PlanetManagement.isClientUnlocked(catalog.dimension(body));
    }

    private boolean usable(int body) {
        return catalog.isRealm(body) || unlocked(body) && reachable(body);
    }

    private int reachableCount(int system) {
        int count = 0;
        for (int k = 0; k < catalog.systemBodyCount(system); k++) {
            if (reachable(catalog.systemBody(system, k))) count++;
        }
        return count;
    }

    @Override
    public int tint(int body) {
        return usable(body) ? StarSprites.NO_TINT : DIM_TINT;
    }

    @Override
    public int labelColor(int body, int fallback) {
        return usable(body) ? fallback : UITheme.MAP_LABEL_DIM;
    }

    @Override
    public void drawAfter(GuiGraphics graphics, int body, int x, int y, int size, boolean hovered) {
        if (catalog.isRealm(body)) return;
        int half = size / 2;
        if (!unlocked(body)) StarSprites.marker(graphics, StarSprites.MARKER_LOCK, x + half, y - half);
        if (stations[body] > 0) StarSprites.marker(graphics, StarSprites.MARKER_STATION, x - half - 2, y - half);
    }

    @Override
    public List<Component> tooltip(int body) {
        int chosen = selected.getAsInt();
        if (chosen == body) return Collections.emptyList();
        boolean realm = catalog.isRealm(body);
        boolean idle = realm ? !catalog.isRealm(chosen) : chosen < 0 || chosen >= catalog.bodyCount();
        boolean unlocked = realm || unlocked(body);
        var tip = tips[body];
        if (tip == null || tip.unlocked != unlocked) {
            tip = new Tip(unlocked, realm ? realmLines(body) : planetLines(body, unlocked));
            tips[body] = tip;
        }
        return idle ? tip.idle : tip.busy;
    }

    private ObjectArrayList<Component> planetLines(int body, boolean unlocked) {
        var lines = new ObjectArrayList<Component>(6);
        lines.add(catalog.name(body).copy().withStyle(ChatFormatting.WHITE));
        if (catalog.current() == body) lines.add(Component.translatable(RocketLang.CURRENT).withStyle(ChatFormatting.GREEN));
        boolean reachable = reachable(body);
        lines.add(Component.translatable(RocketLang.REQUIRED_ROCKET, requiredTier[body]).withStyle(reachable ? ChatFormatting.GRAY : ChatFormatting.RED));
        if (!reachable) lines.add(Component.translatable(RocketLang.CURRENT_ROCKET, rocketTier).withStyle(ChatFormatting.RED));
        lines.add(Component.translatable(unlocked ? RocketLang.UNLOCKED : RocketLang.LOCKED).withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.RED));
        return lines;
    }

    private ObjectArrayList<Component> realmLines(int body) {
        var lines = new ObjectArrayList<Component>(4);
        lines.add(catalog.name(body).copy().withStyle(ChatFormatting.WHITE));
        if (catalog.current() == body) lines.add(Component.translatable(RocketLang.CURRENT).withStyle(ChatFormatting.GREEN));
        lines.add(Component.translatable(RocketLang.NOT_TRAVELABLE).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    public List<Component> anchorTooltip(int anchor) {
        return anchorTips.get(anchor);
    }

    @Override
    public int anchorLabelColor(int anchor, int fallback) {
        return UITheme.MAP_LABEL_DIM;
    }

    @Override
    public boolean isSelected(int body) {
        return selected.getAsInt() == body;
    }

    @Override
    @Nullable
    public Component systemSubtitle(int system) {
        return subtitles[system];
    }

    private record Tip(boolean unlocked, List<Component> busy, List<Component> idle) {

        Tip(boolean unlocked, ObjectArrayList<Component> lines) {
            this(unlocked, List.copyOf(lines), withClick(lines));
        }

        private static List<Component> withClick(ObjectArrayList<Component> lines) {
            var copy = new ObjectArrayList<>(lines);
            copy.add(Component.translatable(RocketLang.CLICK_FOR_DETAILS).withStyle(ChatFormatting.GRAY));
            return copy;
        }
    }
}
