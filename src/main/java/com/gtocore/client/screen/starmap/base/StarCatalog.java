package com.gtocore.client.screen.starmap.base;

import com.gtocore.client.screen.starmap.base.StarEntries.AnchorEntry;
import com.gtocore.client.screen.starmap.base.StarEntries.BodyEntry;
import com.gtocore.client.screen.starmap.base.StarEntries.SystemEntry;

import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import earth.terrarium.adastra.common.planets.AdAstraData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * 星图天体目录：星系、星球、卫星、锚点与异界维度。天体按下标访问，先星球与卫星、后异界维度；轨道维度也映射到所属星球。
 */
@OnlyIn(Dist.CLIENT)
public final class StarCatalog {

    @FunctionalInterface
    public interface CatalogFilter {

        CatalogFilter NONE = dimension -> false;
        CatalogFilter UNREACHABLE = dimension -> {
            var planet = AdAstraData.getPlanet(dimension);
            return planet != null && planet.tier() >= GTODimensions.UNREACHABLE_PLANET_TIER;
        };

        boolean isHidden(ResourceKey<Level> dimension);

        default CatalogFilter or(CatalogFilter other) {
            return dimension -> isHidden(dimension) || other.isHidden(dimension);
        }
    }

    final ObjectArrayList<SystemEntry> systems;
    final ObjectArrayList<BodyEntry> bodies = new ObjectArrayList<>();
    final ObjectArrayList<AnchorEntry> anchors = new ObjectArrayList<>();
    private final Reference2IntOpenHashMap<ResourceKey<Level>> byDimension = new Reference2IntOpenHashMap<>();
    @Nullable
    private final SystemEntry home;
    private final int bodyCount;
    private float realmRadius;
    private int current = -1, focus = -1, revision;

    private StarCatalog(ObjectArrayList<SystemEntry> systems) {
        this.systems = systems;
        this.home = StarLayout.home(systems);
        byDimension.defaultReturnValue(-1);
        for (var system : systems) {
            for (var body : system.bodies) {
                body.index = bodies.size();
                bodies.add(body);
            }
            anchors.addAll(system.anchors);
        }
        bodyCount = bodies.size();
        StarLayout.link(systems, anchors);
        if (home != null) {
            for (var dimension : StarLayout.realmDimensions()) {
                addRealm(new BodyEntry(dimension.getResourceKey(), null, null, dimension, dimension.getIcon(), Component.translatable(dimension.getKey()), true));
            }
        }
        placeRealms();
        for (var body : bodies) byDimension.putIfAbsent(body.key, body.index);
        for (var body : bodies) {
            if (body.orbit != null) byDimension.putIfAbsent(body.orbit, body.index);
        }
        locate();
    }

    public static StarCatalog build(CatalogFilter filter) {
        var systems = StarLayout.systems();
        StarLayout.layoutPlanets(systems, filter);
        return new StarCatalog(systems);
    }

    public static Component planetName(ResourceKey<Level> dimension) {
        return Component.translatableWithFallback("planet.%s.%s".formatted(dimension.location().getNamespace(), dimension.location().getPath()),
                PlanetsScreen.title(dimension.location().getPath()));
    }

    public int addExtraRealm(ResourceKey<Level> dimension) {
        int known = indexOf(dimension);
        if (known >= 0 || home == null) return known;
        var data = Dimension.get(dimension);
        var icon = data != null ? data.getIcon() : StarSprites.FALLBACK_PLANET;
        var name = data != null ? Component.translatable(data.getKey()) : planetName(dimension);
        int index = addRealm(new BodyEntry(dimension, null, null, data, icon, name, true));
        byDimension.put(dimension, index);
        placeRealms();
        var player = Minecraft.getInstance().player;
        if (current < 0 && player != null && player.level().dimension() == dimension) current = index;
        revision++;
        return index;
    }

    private int addRealm(BodyEntry realm) {
        realm.index = bodies.size();
        bodies.add(realm);
        return realm.index;
    }

    private void placeRealms() {
        realmRadius = StarLayout.realmRadius(home, realmCount());
        StarLayout.placeSystems(systems, home, realmRadius);
        if (home != null) StarLayout.placeRealms(bodies.subList(bodyCount, bodies.size()), home, realmRadius);
    }

    private void locate() {
        var player = Minecraft.getInstance().player;
        var here = player == null ? null : player.level().dimension();
        int realm = -1;
        for (var body : bodies) {
            if (here == null) break;
            if (!body.realm && (body.key == here || body.orbit == here)) current = body.index;
            if (body.realm && body.key == here) realm = body.index;
        }
        if (current >= 0) {
            focus = bodies.get(current).system;
        } else {
            current = realm;
            var galaxy = here == null ? null : GTODimensions.getGalaxy(here);
            focus = systems.isEmpty() ? -1 : 0;
            for (var system : systems) {
                if (galaxy != null && system.galaxy == galaxy) focus = system.index;
            }
        }
    }

    public int revision() {
        return revision;
    }

    public int size() {
        return bodies.size();
    }

    public int bodyCount() {
        return bodyCount;
    }

    public int realmCount() {
        return bodies.size() - bodyCount;
    }

    public int systemCount() {
        return systems.size();
    }

    public int anchorCount() {
        return anchors.size();
    }

    public int indexOf(@Nullable ResourceKey<Level> dimension) {
        return dimension == null ? -1 : byDimension.getInt(dimension);
    }

    @Nullable
    public ResourceKey<Level> dimension(int body) {
        return body < 0 || body >= bodies.size() ? null : bodies.get(body).key;
    }

    @Nullable
    public ResourceKey<Level> orbit(int body) {
        return bodies.get(body).orbit;
    }

    @Nullable
    public Planet planet(int body) {
        return bodies.get(body).planet;
    }

    @Nullable
    public Dimension dimensionData(int body) {
        return bodies.get(body).dimension;
    }

    public Component name(int body) {
        return body < 0 || body >= bodies.size() ? Component.empty() : bodies.get(body).name;
    }

    @Nullable
    public Component parentName(int body) {
        var entry = bodies.get(body);
        if (entry.parent >= 0) return bodies.get(entry.parent).name;
        return entry.anchor >= 0 ? anchors.get(entry.anchor).name : null;
    }

    public boolean isRealm(int body) {
        return body >= bodyCount && body < bodies.size();
    }

    public boolean isMoon(int body) {
        return body >= 0 && body < bodyCount && bodies.get(body).isMoon();
    }

    public boolean isStellar(int body) {
        return body >= 0 && body < bodyCount && bodies.get(body).stellar;
    }

    public int parent(int body) {
        return body < 0 || body >= bodyCount ? -1 : bodies.get(body).parent;
    }

    public int system(int body) {
        return body < 0 || body >= bodyCount ? -1 : bodies.get(body).system;
    }

    public int order(int body) {
        return bodies.get(body).order;
    }

    public Component systemName(int system) {
        return system < 0 || system >= systems.size() ? Component.empty() : systems.get(system).name;
    }

    public int systemColor(int system) {
        return systems.get(system).color();
    }

    public float systemX(int system) {
        return systems.get(system).cx;
    }

    public float systemY(int system) {
        return systems.get(system).cy;
    }

    public float systemRadius(int system) {
        return systems.get(system).radius;
    }

    public int ringCount(int system) {
        return systems.get(system).orderRadii.size();
    }

    public float ringRadius(int system, int order) {
        return systems.get(system).orderRadii.getFloat(order);
    }

    public int systemBodyCount(int system) {
        return systems.get(system).bodies.size();
    }

    public int systemBody(int system, int k) {
        return systems.get(system).bodies.get(k).index;
    }

    public void systemBounds(int system, float[] out) {
        var entry = systems.get(system);
        out[0] = entry.cx - entry.radius;
        out[1] = entry.cy - entry.radius * StarGeometry.TILT - StarLayout.TITLE_ROOM;
        out[2] = 2 * entry.radius;
        out[3] = 2 * entry.radius * StarGeometry.TILT + StarLayout.TITLE_ROOM;
    }

    public boolean realmBounds(float[] out) {
        if (home == null || realmCount() == 0) return false;
        float half = StarLayout.REALM_CELL / 2;
        out[0] = home.cx - realmRadius - half;
        out[1] = home.cy - realmRadius * StarGeometry.TILT - half;
        out[2] = 2 * (realmRadius + half);
        out[3] = 2 * (realmRadius * StarGeometry.TILT + half);
        return true;
    }

    public int anchorSystem(int anchor) {
        return anchors.get(anchor).system;
    }

    public int anchorOrder(int anchor) {
        return anchors.get(anchor).order;
    }

    public Component anchorName(int anchor) {
        return anchors.get(anchor).name;
    }

    public int current() {
        return current;
    }

    public int focusSystem() {
        return focus;
    }
}
