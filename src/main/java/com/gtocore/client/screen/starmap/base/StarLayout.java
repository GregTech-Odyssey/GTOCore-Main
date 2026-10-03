package com.gtocore.client.screen.starmap.base;

import com.gtocore.client.screen.starmap.base.StarEntries.AnchorEntry;
import com.gtocore.client.screen.starmap.base.StarEntries.BodyEntry;
import com.gtocore.client.screen.starmap.base.StarEntries.SystemEntry;

import com.gtolib.GTOCore;
import com.gtolib.api.data.CelestialBody;
import com.gtolib.api.data.Dimension;
import com.gtolib.api.data.GTODimensions;
import com.gtolib.api.data.Galaxy;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.common.planets.AdAstraData;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 星图布局：星系横向排开，星球按轨道环排布、卫星绕母星，异界维度均匀挂在太阳系外圈。
 */
@OnlyIn(Dist.CLIENT)
final class StarLayout {

    static final float BODY_SIZE = 18;
    static final float RING_BASE = 42;
    static final float RING_STEP = 18;
    static final float DISTANCE_BASE = 20;
    static final float DISTANCE_SCALE = 60;
    static final float SPREAD = 10;
    static final float SATELLITE_BASE = 15;
    static final float SATELLITE_STEP = 10;
    static final float SYSTEM_GAP = 70 * SPREAD;
    static final float EMPTY_RADIUS = 50 * SPREAD;
    static final float TITLE_ROOM = 28;
    static final float REALM_CELL = 18 * SPREAD;
    static final float REALM_GAP = 30 * SPREAD;
    private static final float GOLDEN_ANGLE = 2.39996f;
    private static final float REALM_STAGGER = 0.18f;
    private static final float REALM_STAGGER_MIN = 0.12f;
    private static final float REALM_STAGGER_MAX = 0.3f;
    @Nullable
    private static Object2ObjectOpenHashMap<ResourceLocation, CelestialBody> virtualPrimaries;

    private StarLayout() {}

    static ObjectArrayList<SystemEntry> systems() {
        var systems = new Object2ObjectLinkedOpenHashMap<ResourceLocation, SystemEntry>();
        var known = AdAstraData.solarSystems();
        for (var galaxy : Galaxy.all()) {
            if (galaxy == Galaxy.NONE || !known.contains(galaxy.getSolarSystem()) || systems.containsKey(galaxy.getSolarSystem())) continue;
            systems.put(galaxy.getSolarSystem(), new SystemEntry(systems.size(), galaxy.getSolarSystem(), galaxy));
        }
        var others = new ArrayList<>(known);
        others.sort(Comparator.comparing(ResourceLocation::getPath));
        for (var id : others) {
            if (!systems.containsKey(id)) systems.put(id, new SystemEntry(systems.size(), id, null));
        }
        return new ObjectArrayList<>(systems.values());
    }

    static void layoutPlanets(ObjectArrayList<SystemEntry> systems, StarCatalog.CatalogFilter filter) {
        var byId = new Object2ObjectOpenHashMap<ResourceLocation, SystemEntry>();
        for (var system : systems) byId.put(system.id, system);
        var pending = new ArrayList<Candidate>();
        var planets = new ArrayList<Candidate>();
        for (var planet : AdAstraData.planets().values()) {
            if (planet.isSpace() || filter.isHidden(planet.dimension())) continue;
            var system = byId.get(planet.solarSystem());
            if (system == null) continue;
            planets.add(new Candidate(planet, GTODimensions.getDimensionIncludingOrbits(planet.dimension()), system, StarCatalog.planetName(planet.dimension())));
        }
        planets.sort(Comparator.comparingInt((Candidate c) -> c.planet.tier()).thenComparing(c -> c.name.getString()));
        for (var candidate : planets) {
            var dimension = candidate.dimension;
            candidate.ring = dimension != null && dimension.getStellarDistance() > 0 ? dimension.getStellarDistance() : 0;
            pending.add(candidate);
        }
        for (var system : systems) layoutSystem(system, pending);
    }

    private static void layoutSystem(SystemEntry system, List<Candidate> pending) {
        int nextRing = 0;
        for (var entry : pending) {
            if (entry.system == system && !entry.stellar()) nextRing = Math.max(nextRing, entry.ring);
        }
        var byRing = new Object2ObjectLinkedOpenHashMap<Integer, List<Candidate>>();
        for (var entry : pending) {
            if (entry.system != system) continue;
            if (entry.stellar()) {
                var body = entry.body();
                body.placeStellar(system.index);
                system.bodies.add(body);
                continue;
            }
            if (entry.ring <= 0) entry.ring = ++nextRing;
            byRing.computeIfAbsent(entry.ring, r -> new ArrayList<>()).add(entry);
        }
        var order = new ArrayList<>(byRing.keySet());
        order.sort(Integer::compare);
        float radius = EMPTY_RADIUS;
        for (int ring : order) radius = Math.max(radius, layoutRing(system, ring, byRing.get(ring)));
        system.radius = radius;
    }

    private static float layoutRing(SystemEntry system, int ring, List<Candidate> members) {
        var primaries = virtualPrimaries();
        var groups = new Object2ObjectLinkedOpenHashMap<ResourceLocation, List<Candidate>>();
        for (var member : members) {
            var id = member.planet.dimension().location();
            var primary = primaryKey(member);
            boolean primaryVisible = primary == null || primaries.containsKey(primary) ||
                    members.stream().anyMatch(m -> m.planet.dimension().location().equals(primary));
            groups.computeIfAbsent(primary != null && primaryVisible ? primary : id, k -> new ArrayList<>()).add(member);
        }
        int moons = 0;
        for (var group : groups.entrySet()) {
            boolean orbiting = primaries.containsKey(group.getKey()) || group.getValue().stream().anyMatch(m -> m.planet.dimension().location().equals(group.getKey()));
            if (orbiting) moons = Math.max(moons, (int) group.getValue().stream().filter(m -> !m.planet.dimension().location().equals(group.getKey())).count());
        }
        float extent = BODY_SIZE / 2 + (moons > 0 ? StarGeometry.MOON_FIRST + (moons - 1) * StarGeometry.MOON_STEP : 0);
        float ringRadius = RING_BASE + system.rings.size() * RING_STEP;
        for (var member : members) {
            var body = member.dimension != null ? member.dimension.getBody() : null;
            if (body != null && body.getMapDistanceRatio() > 0) {
                ringRadius = (DISTANCE_BASE + (float) Math.sqrt(body.getMapDistanceRatio()) * DISTANCE_SCALE) * SPREAD;
                break;
            }
        }
        int ringOrder = system.rings.size();
        system.ringRadii.put(ring, ringRadius);
        system.rings.add(ring);
        system.orderRadii.add(ringRadius);
        int slot = 0;
        for (var group : groups.entrySet()) {
            float angle = (float) (ring * GOLDEN_ANGLE + slot++ * (2 * Math.PI / groups.size()));
            placeGroup(system, ringOrder, angle, ringRadius, group.getKey(), group.getValue(), primaries);
        }
        return ringRadius + extent + 4;
    }

    private static void placeGroup(SystemEntry system, int ringOrder, float angle, float ringRadius, ResourceLocation key, List<Candidate> list,
                                   Object2ObjectOpenHashMap<ResourceLocation, CelestialBody> primaries) {
        boolean hasPrimaryBody = list.stream().anyMatch(m -> m.planet.dimension().location().equals(key));
        var virtual = hasPrimaryBody ? null : primaries.get(key);
        if (virtual != null) system.anchors.add(new AnchorEntry(system.index, ringOrder, angle, virtual.getTexture(), Component.translatable(virtual.getTranslationKey())));
        boolean orbiting = hasPrimaryBody || virtual != null;
        int moon = 0;
        for (var member : list) {
            var body = member.body();
            boolean primary = member.planet.dimension().location().equals(key);
            if (primary || !orbiting) {
                body.place(system.index, ringOrder, angle, -1, 0, ringRadius);
            } else {
                body.place(system.index, ringOrder, (float) (angle + 2.1 + moon * 2.4), moon, angle, SATELLITE_BASE + moon * SATELLITE_STEP);
                moon++;
            }
            system.bodies.add(body);
        }
    }

    static void link(ObjectArrayList<SystemEntry> systems, ObjectArrayList<AnchorEntry> anchors) {
        for (var system : systems) {
            for (var body : system.bodies) {
                if (!body.isMoon()) continue;
                for (var other : system.bodies) {
                    if (!other.isMoon() && other.order == body.order && other.angle == body.parentAngle) {
                        body.parent = other.index;
                        break;
                    }
                }
                if (body.parent >= 0) continue;
                for (var anchor : system.anchors) {
                    if (anchor.order == body.order) {
                        body.anchor = anchors.indexOf(anchor);
                        break;
                    }
                }
            }
        }
    }

    @Nullable
    static SystemEntry home(ObjectArrayList<SystemEntry> systems) {
        for (var system : systems) {
            if (system.galaxy == Galaxy.SOLAR) return system;
        }
        return systems.isEmpty() ? null : systems.get(0);
    }

    static float realmRadius(@Nullable SystemEntry home, int realms) {
        return home == null || realms == 0 ? 0 : Math.max(home.radius + REALM_GAP, realms * REALM_CELL / (2 * (float) Math.PI));
    }

    static void placeSystems(ObjectArrayList<SystemEntry> systems, @Nullable SystemEntry home, float realmRadius) {
        float x = 0;
        for (var system : systems) {
            float footprint = system == home ? Math.max(system.radius, realmRadius + REALM_CELL / 2) : system.radius;
            system.cx = x + footprint;
            system.cy = 0;
            x += 2 * footprint + SYSTEM_GAP;
        }
    }

    static void placeRealms(List<BodyEntry> realms, SystemEntry home, float radius) {
        if (realms.isEmpty()) return;
        int n = realms.size();
        float step = 2 * (float) Math.PI / n;
        float stagger = n % 2 == 0 ? Math.min(Math.max(REALM_STAGGER * step, REALM_STAGGER_MIN), REALM_STAGGER_MAX * step) : 0;
        for (int i = 0; i < n; i++) {
            var realm = realms.get(i);
            int parity = i + (n / 2 % 2 == 0 ? 2 * i / n : 0);
            float angle = (i + 0.5f) * step - (float) Math.PI / 2 + (parity % 2 == 0 ? stagger : -stagger);
            realm.realmX = home.cx + radius * Mth.cos(angle);
            realm.realmY = home.cy + radius * StarGeometry.TILT * Mth.sin(angle);
        }
    }

    static List<Dimension> realmDimensions() {
        var dimensions = new ArrayList<Dimension>();
        for (var dimension : Dimension.all()) {
            if (!dimension.isWithinGalaxy()) dimensions.add(dimension);
        }
        return dimensions;
    }

    @Nullable
    private static ResourceLocation primaryKey(Candidate entry) {
        var body = entry.dimension != null ? entry.dimension.getBody() : null;
        var primary = body != null ? body.getPrimary() : null;
        if (primary == null) return null;
        var dimension = Dimension.get(primary);
        return dimension != null ? dimension.getLocation() : GTOCore.id(primary.getId());
    }

    private static Object2ObjectOpenHashMap<ResourceLocation, CelestialBody> virtualPrimaries() {
        if (virtualPrimaries == null) {
            var map = new Object2ObjectOpenHashMap<ResourceLocation, CelestialBody>();
            for (var body : CelestialBody.all()) {
                var primary = body.getPrimary();
                if (primary != null && Dimension.get(primary) == null) map.put(GTOCore.id(primary.getId()), primary);
            }
            virtualPrimaries = map;
        }
        return virtualPrimaries;
    }

    private static final class Candidate {

        final Planet planet;
        @Nullable
        final Dimension dimension;
        final SystemEntry system;
        final Component name;
        int ring;

        Candidate(Planet planet, @Nullable Dimension dimension, SystemEntry system, Component name) {
            this.planet = planet;
            this.dimension = dimension;
            this.system = system;
            this.name = name;
        }

        boolean stellar() {
            var body = dimension != null ? dimension.getBody() : null;
            return body != null && body.isStar();
        }

        BodyEntry body() {
            var icon = dimension != null ? dimension.getIcon() : StarSprites.FALLBACK_PLANET;
            return new BodyEntry(planet.dimension(), planet.orbitIfPresent(), planet, dimension, icon, name, false);
        }
    }
}
